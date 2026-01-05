package com.work.oblikpodorojlist.utils;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.sql.Date;
import com.work.oblikpodorojlist.model.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBUtil {

    private String host;
    private String URL = "jdbc:mysql://"+host+":3306/";
    private String GuestUSERNAME = "GUEST";
    private String GuestPASSWORD = "GUEST";
    private String company;
    private String username;
    private String password;
    private static DBUtil instance;
    private HikariDataSource dataSource;
    private HikariDataSource guestDataSource;

    // Кеші для покращення продуктивності
    private Map<Integer, String> workerNameCache = new HashMap<>();
    private Map<Integer, String> carNumberCache = new HashMap<>();
    private Map<Integer, String> orderNumberCache = new HashMap<>();

    private DBUtil() {
        getHost();
    }

    public void Migrate() {
        // отримуємо папку з ресурсами (IDE чи JAR)
        Path resources = ResourceUtils.getResourcesFolder();

        // будуємо CompositeResourceAccessor, щоб спрацювало і в dev, і в JAR
        CompositeResourceAccessor ra = new CompositeResourceAccessor(
                new FileSystemResourceAccessor(resources.toFile()),      // файлова ресурси
                new ClassLoaderResourceAccessor(getClass().getClassLoader()) // ресурси з classpath
        );

        try (Connection conn = Connect()) {
            Liquibase lb = new Liquibase(
                    "db/changelog/db.changelog-master.xml", // шлях відносно папки ресурсів / classpath root
                    ra,
                    new JdbcConnection(conn)
            );
            lb.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            throw new RuntimeException("Error during DB migration: " + e.getMessage(), e);
        }
    }


    public static DBUtil getInstance() {
        if(instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }

    public String getHost() {
        host = ConfigUtil.loadIpAddress();
        URL = "jdbc:mysql://"+host+":3306/";
        return host;
    }

    public void setHost(String host) {
        URL = "jdbc:mysql://"+host+":3306/";
        ConfigUtil.saveIpAddress(host);
        this.host = host;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
        // Очищаємо кеш та закриваємо старий пул при зміні компанії
        clearCaches();
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }

    private void clearCaches() {
        workerNameCache.clear();
        carNumberCache.clear();
        orderNumberCache.clear();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        // Закриваємо старий пул при зміні користувача
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }

    public void deleteUser(String selectedUser) {
        String deletelist = "DROP USER '"+selectedUser+"'@'%';";
        try (Connection connection = Connect();
             CallableStatement deletelistStmt = connection.prepareCall(deletelist)) {
            deletelistStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        // Закриваємо старий пул при зміні пароля
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }

    public Connection GuestConnect() {
        try {
            if (guestDataSource == null || guestDataSource.isClosed()) {
                initGuestDataSource();
            }
            return guestDataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initGuestDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":3306/");
        config.setUsername(GuestUSERNAME);
        config.setPassword(GuestPASSWORD);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        guestDataSource = new HikariDataSource(config);
    }

    public void CreateGuestUser() {
        List<String> dbnames = new ArrayList<>();
        try (Connection connection = Connect();
             Statement statement = connection.createStatement()) {


            String createUserQuery = "CREATE USER IF NOT EXISTS '"+GuestUSERNAME+"'@'%' IDENTIFIED BY '"+GuestPASSWORD+"';";
            statement.executeUpdate(createUserQuery);

            String grantPrivilegesQuery = "GRANT SHOW DATABASES ON *.* TO '"+GuestUSERNAME+"'@'%';";
            String grantDB = "GRANT SELECT ON mysql.db TO '"+GuestUSERNAME+"'@'%'";

            statement.executeUpdate(grantPrivilegesQuery);
            statement.executeUpdate(grantDB);

            String getDatabasesQuery = "SHOW DATABASES";
            try (ResultSet resultSet = statement.executeQuery(getDatabasesQuery)) {
                while (resultSet.next()) {
                    String dbName = resultSet.getString(1);

                    if (dbName.equalsIgnoreCase("information_schema") ||
                            dbName.equalsIgnoreCase("mysql") ||
                            dbName.equalsIgnoreCase("performance_schema") ||
                            dbName.equalsIgnoreCase("sys")) {
                        continue;
                    }
                    dbnames.add(dbName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection con = Connect();
        Statement stm = con.createStatement();) {

            for(String dbName : dbnames) {
                String grantQuery = "GRANT SELECT ON `" + dbName + "`.parameters TO '"+GuestUSERNAME+"'@'%';";
                stm.executeUpdate(grantQuery);
            }
            stm.executeUpdate("FLUSH PRIVILEGES");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection Connect() {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                initDataSource();
            }
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":3306/" + (company == null ? "" : company) +
                         "?useServerPrepStmts=true&rewriteBatchedStatements=true&cachePrepStmts=true&useLocalSessionState=true&useLocalTransactionState=true");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20); // Збільшено для кращої паралельності
        config.setMinimumIdle(5); // Більше готових з'єднань
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000); // Виявлення витоків з'єднань

        // Оптимізація кешування prepared statements
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "500");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(config);
    }

    public void closeDataSources() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        if (guestDataSource != null && !guestDataSource.isClosed()) {
            guestDataSource.close();
        }
    }

    public List<String> getCompanies() {
        List<String> companyNames = new ArrayList<>();
        String showDatabasesQuery = "SHOW DATABASES";

        try (Connection connection = GuestConnect();
             Statement statement = connection.createStatement();
             ResultSet databases = statement.executeQuery(showDatabasesQuery)) {

            while (databases.next()) {
                String dbName = databases.getString(1);

                if (!dbName.equalsIgnoreCase("information_schema") &&
                        !dbName.equalsIgnoreCase("mysql") &&
                        !dbName.equalsIgnoreCase("performance_schema") &&
                        !dbName.equalsIgnoreCase("sys")) {

                    if (tableExists(connection, dbName, "parameters")) {
                        String query = "SELECT name FROM `" + dbName + "`.parameters";

                        try (Statement stmt = connection.createStatement();
                             ResultSet rs = stmt.executeQuery(query)) {

                            while (rs.next()) {
                                companyNames.add(dbName + " "+rs.getString("name"));
                            }
                        } catch (SQLException e) {
                            System.err.println("Помилка при зчитуванні таблиці parameters у базі " + dbName);
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return companyNames;
    }

    private boolean tableExists(Connection connection, String database, String tableName) {
        String checkQuery = "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setString(1, database);
            stmt.setString(2, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка перевірки існування таблиці " + tableName + " у базі " + database);
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getUsers() {
        List<String> users = new ArrayList<>();
        String query = "SELECT DISTINCT  User " +
                "FROM mysql.db " +
                "WHERE Db = ? AND (" +
                "Insert_priv = 'Y' OR " +
                "Update_priv = 'Y' OR " +
                "Delete_priv = 'Y' OR " +
                "Alter_priv = 'Y' OR " +
                "Create_priv = 'Y' OR " +
                "Drop_priv = 'Y' OR " +
                "Grant_priv = 'Y')";

        try (Connection connection = GuestConnect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, company);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String user = resultSet.getString("User");
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void createUser(String username, String password) {
        try (Connection connection = Connect();
             Statement statement = connection.createStatement()) {

            String createUserQuery = "CREATE USER IF NOT EXISTS '"+username+"'@'%' IDENTIFIED BY '"+password+"';";
            statement.executeUpdate(createUserQuery);

            String grantPrivilegesQuery = "GRANT ALL PRIVILEGES ON `"+company+"`.* TO '"+username+"'@'%';";
            statement.executeUpdate(grantPrivilegesQuery);

            String flushPrivilegesQuery = "FLUSH PRIVILEGES;";
            statement.executeUpdate(flushPrivilegesQuery);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private String getBackupFolderPath() {
        URL resource = DBUtil.class.getClassLoader().getResource("config");
        if (resource != null) {
            String resourcePath = resource.getPath();
            return new File(resourcePath).getParent() + File.separator + "backups";
        } else {
            String currentDir = System.getProperty("user.dir");
            return currentDir + File.separator + "backups";
        }
    }

    public void createBackup() {
        String backupFolderPath = getBackupFolderPath() + "\\" + company;
        backupFolderPath = backupFolderPath.replace("\\\\", "\\");

        File backupFolder = new File(backupFolderPath);
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            System.out.println("Не вдалося створити папку для бекапів.");
            return;
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalTime now = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("(HH-mm)");
        String formattedTime = now.format(timeFormatter);

        String backupFilePath = backupFolderPath + "\\backup_(" + today.format(formatter) + ")_" + formattedTime + ".sql";
        String mysqlDumpPath = findMySQLDump();

        // Перевірка: чи знайдено mysqldump
        if (mysqlDumpPath == null) {
            System.err.println("УВАГА: mysqldump не знайдено на системі!");
            System.out.println("Спроба використати Docker для резервного копіювання...");
            createBackupUsingDocker(backupFilePath);
            return;
        }

        List<String> commandList = new ArrayList<>();
        commandList.add(mysqlDumpPath);

        if (username.contains(" ")) {
            commandList.add("-u");
            commandList.add("\"" + username + "\"");
        } else {
            commandList.add("-u");
            commandList.add(username);
        }

        if (password != null && !password.isEmpty()) {
            commandList.add("--password=" + password);
        }

        commandList.add(company);

        commandList.add("-r");
        commandList.add(backupFilePath);

        String[] command = commandList.toArray(new String[0]);

        System.out.println("Виконується команда: " + Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println("Помилка: " + line);
            }

            int processComplete = process.waitFor();

            if (processComplete == 0) {
                System.out.println("Бекап успішно створено: " + backupFilePath);
            } else {
                System.out.println("Помилка при створенні бекапу.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Створює резервну копію за допомогою Docker (якщо MySQL запущений в Docker контейнері)
     */
    private void createBackupUsingDocker(String backupFilePath) {
        try {
            // Спроба знайти Docker контейнер з MySQL
            String containerName = "oblik-mysql";
            
            // Перевірка, чи працює Docker
            ProcessBuilder checkDocker = new ProcessBuilder("docker", "ps", "--format", "{{.Names}}");
            Process checkProcess = checkDocker.start();
            BufferedReader checkReader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            
            boolean containerFound = false;
            String line;
            while ((line = checkReader.readLine()) != null) {
                if (line.contains(containerName)) {
                    containerFound = true;
                    break;
                }
            }
            checkProcess.waitFor();

            if (!containerFound) {
                System.err.println("Docker контейнер '" + containerName + "' не знайдено або не запущений.");
                System.out.println("Запустіть MySQL через Docker: docker-compose up -d");
                Alert alert = AlertsUtil.ErrorAlert(
                    "Резервне копіювання недоступне",
                    "MySQL не знайдено ні локально, ні в Docker.\n" +
                    "Встановіть MySQL локально або запустіть Docker контейнер."
                );
                alert.showAndWait();
                return;
            }

            // Виконання mysqldump через Docker
            String[] dockerCommand = new String[]{
                "docker", "exec", containerName,
                "mysqldump",
                "-u", username,
                "-p" + password,
                company
            };

            System.out.println("Виконується Docker команда: " + Arrays.toString(dockerCommand));

            ProcessBuilder processBuilder = new ProcessBuilder(dockerCommand);
            processBuilder.redirectErrorStream(false);
            Process process = processBuilder.start();

            // Зчитування виводу і запис у файл
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(backupFilePath))) {
                
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            // Перевірка на помилки
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("Docker помилка: " + line);
                }
            }

            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("✓ Бекап успішно створено через Docker: " + backupFilePath);
            } else {
                System.err.println("✗ Помилка при створенні бекапу через Docker. Код: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Помилка при створенні бекапу через Docker: " + e.getMessage());
            e.printStackTrace();
            Alert alert = AlertsUtil.ErrorAlert(
                "Помилка резервного копіювання",
                "Не вдалося створити бекап:\n" + e.getMessage()
            );
            alert.showAndWait();
        }
    }


    public void deleteOldBackups() {
        String backupFolderPath = getBackupFolderPath() + "\\" + company;
        File backupDir = new File(backupFolderPath);

        if (!backupDir.exists() || !backupDir.isDirectory()) {
            System.out.println("Папка резервних копій не знайдена: " + backupFolderPath);
            return;
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));

        if (backupFiles == null || backupFiles.length == 0) {
            System.out.println("Немає резервних копій для перевірки.");
            return;
        }

        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (File file : backupFiles) {
            String fileName = file.getName();
            Matcher matcher = Pattern.compile("backup_\\((\\d{2}\\.\\d{2}\\.\\d{4})\\).*\\.sql").matcher(fileName);

            if (matcher.find()) {
                String dateString = matcher.group(1);
                LocalDate backupDate = LocalDate.parse(dateString, formatter);
                if (backupDate.isBefore(sixMonthsAgo)) {
                    if (file.delete()) {
                        System.out.println("Старий бекап видалено: " + file.getName());
                    } else {
                        System.out.println("Не вдалося видалити файл: " + file.getName());
                    }
                }
            }
        }
    }

    public void loadBackup() {
        String backupFolderPath = getBackupFolderPath()+ "\\"+company;
        File backupDir = new File(backupFolderPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        FileDialog fileDialog = new FileDialog((Frame) null, "Оберіть файл для відновлення", FileDialog.LOAD);
        fileDialog.setDirectory(backupFolderPath);
        fileDialog.setFile("*.sql");

        fileDialog.setVisible(true);

        String selectedFile = fileDialog.getFile();
        if (selectedFile == null) {
            System.out.println("Відновлення скасовано користувачем.");
            fileDialog.dispose();
            return;
        }

        String backupFilePath = fileDialog.getDirectory() + selectedFile;
        fileDialog.dispose();
        System.out.println("Обрано файл: " + backupFilePath);

        String mysqlPath = findMySQL();

        // Перевірка: чи знайдено mysql
        if (mysqlPath == null) {
            System.err.println("УВАГА: mysql не знайдено на системі!");
            System.out.println("Спроба використати Docker для відновлення...");
            loadBackupUsingDocker(backupFilePath);
            return;
        }

        String[] command = new String[]{
                mysqlPath,
                "-u", username,
                "-p" + password,
                company,
                "-e", "source " + backupFilePath
        };

        System.out.println("Виконується команда: " + Arrays.toString(command));


        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int processComplete = process.waitFor();

            if (processComplete == 0) {
                System.out.println("База даних успішно відновлена!");
            } else {
                System.out.println("Помилка при відновленні бази даних.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Відновлює БД з резервної копії за допомогою Docker
     */
    private void loadBackupUsingDocker(String backupFilePath) {
        try {
            String containerName = "oblik-mysql";
            
            // Перевірка, чи працює Docker контейнер
            ProcessBuilder checkDocker = new ProcessBuilder("docker", "ps", "--format", "{{.Names}}");
            Process checkProcess = checkDocker.start();
            BufferedReader checkReader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            
            boolean containerFound = false;
            String line;
            while ((line = checkReader.readLine()) != null) {
                if (line.contains(containerName)) {
                    containerFound = true;
                    break;
                }
            }
            checkProcess.waitFor();

            if (!containerFound) {
                System.err.println("Docker контейнер '" + containerName + "' не знайдено або не запущений.");
                Alert alert = AlertsUtil.ErrorAlert(
                    "Відновлення недоступне",
                    "MySQL не знайдено ні локально, ні в Docker.\n" +
                    "Встановіть MySQL локально або запустіть Docker контейнер."
                );
                alert.showAndWait();
                return;
            }

            // Копіювання файлу бекапу в контейнер
            String containerBackupPath = "/tmp/restore_backup.sql";
            String[] copyCommand = new String[]{
                "docker", "cp", backupFilePath, containerName + ":" + containerBackupPath
            };

            System.out.println("Копіювання бекапу в контейнер: " + Arrays.toString(copyCommand));
            ProcessBuilder copyBuilder = new ProcessBuilder(copyCommand);
            Process copyProcess = copyBuilder.start();
            copyProcess.waitFor();

            // Виконання відновлення через Docker
            String[] dockerCommand = new String[]{
                "docker", "exec", "-i", containerName,
                "mysql",
                "-u", username,
                "-p" + password,
                company,
                "-e", "source " + containerBackupPath
            };

            System.out.println("Виконується Docker команда відновлення: " + Arrays.toString(dockerCommand));

            ProcessBuilder processBuilder = new ProcessBuilder(dockerCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("✓ БД успішно відновлена через Docker!");
            } else {
                System.err.println("✗ Помилка при відновленні БД через Docker. Код: " + exitCode);
            }

            // Видалення тимчасового файлу з контейнера
            String[] cleanupCommand = new String[]{
                "docker", "exec", containerName,
                "rm", containerBackupPath
            };
            new ProcessBuilder(cleanupCommand).start().waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println("Помилка при відновленні через Docker: " + e.getMessage());
            e.printStackTrace();
            Alert alert = AlertsUtil.ErrorAlert(
                "Помилка відновлення",
                "Не вдалося відновити БД:\n" + e.getMessage()
            );
            alert.showAndWait();
        }
    }

    /**
     * Шукає mysql клієнт на системі
     */
    private String findMySQL() {
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(";")) {
                File file = new File(dir, "mysql.exe");
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }

        String[] commonPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\mysql.exe",
                "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
                "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.6\\bin\\mysql.exe"
        };

        for (String pathOption : commonPaths) {
            File file = new File(pathOption);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    private String findMySQLDump() {
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(";")) {
                File file = new File(dir, "mysqldump.exe");
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }

        String[] commonPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\mysqldump.exe",
                "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
                "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.6\\bin\\mysqldump.exe"
        };

        for (String pathOption : commonPaths) {
            File file = new File(pathOption);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    public boolean tryConnection() {
        try {
            java.sql.DriverManager.getConnection(URL, username, password);
            return true;
        } catch (SQLException e) {
            Alert a = AlertsUtil.ErrorAlert(e.toString(), e.getMessage());
            a.showAndWait();
            throw new RuntimeException(e);
        }
    }

    public void createCompany(String companyName, String address, String code, String ceo, String accountant, String typeFull, String typeShort) {
        try (Connection connection = Connect();
             Statement statement = connection.createStatement()) {
            String createDB = "CREATE DATABASE IF NOT EXISTS `"+code+"`;";
            statement.executeUpdate(createDB);

            String useDB = "USE `" + code + "`;";
            statement.executeUpdate(useDB);

            String createCarsTable = "CREATE TABLE IF NOT EXISTS `cars` (" +
                    "  `id-car` int NOT NULL AUTO_INCREMENT," +
                    "  `number` MEDIUMTEXT NOT NULL," +
                    "  `model` MEDIUMTEXT NOT NULL," +
                    "  `fuel-type` MEDIUMTEXT NOT NULL," +
                    "  `fuel-usage` double NOT NULL," +
                    "  `engine-volume` double NOT NULL," +
                    "  `start-date` date NOT NULL," +
                    "  `start-order-number` MEDIUMTEXT NOT NULL," +
                    "  `end-date` date DEFAULT NULL," +
                    "  `end-order-number` MEDIUMTEXT DEFAULT NULL," +
                    "  `valid` tinyint NOT NULL," +
                    "  `start-fuel` double NOT NULL," +
                    "  `start-mileage` double NOT NULL," +
                    "  PRIMARY KEY (`id-car`)," +
                    "  UNIQUE KEY `idCar_UNIQUE` (`id-car`)" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";

            statement.executeUpdate(createCarsTable);

            String createReportsTable = "CREATE TABLE IF NOT EXISTS `reports` (" +
                    "  `id` INT NOT NULL AUTO_INCREMENT," +
                    "  `id-order` INT NOT NULL," +
                    "  `comments` LONGTEXT NULL," +
                    "  `date` DATE NOT NULL," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";

            statement.executeUpdate(createReportsTable);


            String createListsTable = " CREATE TABLE IF NOT EXISTS " + "`lists` (\n" +
                    "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `number` int NOT NULL,\n" +
                    "  `id-order` int DEFAULT NULL,\n" +
                    "  `id-car` int NOT NULL,\n" +
                    "  `start-mileage` double NOT NULL,\n" +
                    "  `start-fuel` double NOT NULL,\n" +
                    "  `end-mileage` double DEFAULT NULL,\n" +
                    "  `end-fuel` double DEFAULT NULL,\n" +
                    "  `refuel` double DEFAULT NULL,\n" +
                    "  `done` tinyint NOT NULL,\n" +
                    "  `start-date` date NOT NULL,\n" +
                    "  `end-date` date NOT NULL,\n" +
                    "  `route` LONGTEXT NOT NULL,\n" +
                    "  `goal` LONGTEXT NOT NULL,\n" +
                    "  `id-worker` int NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n";

            statement.executeUpdate(createListsTable);

            String createOrdersTable = "CREATE TABLE IF NOT EXISTS `orders` (\n" +
                    "  `id-order` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `order-date` date NOT NULL,\n" +
                    "  `order-number` MEDIUMTEXT NOT NULL,\n" +
                    "  `id-worker` int NOT NULL,\n" +
                    "  `start-date` date NOT NULL,\n" +
                    "  `end-date` date NOT NULL,\n" +
                    "  `route` LONGTEXT NOT NULL,\n" +
                    "  `money` double NOT NULL,\n" +
                    "  `goal` LONGTEXT NOT NULL,\n" +
                    "  `head` MEDIUMTEXT NOT NULL,\n" +
                    "  PRIMARY KEY (`id-order`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";

            statement.executeUpdate(createOrdersTable);

            String createParametersTable = "CREATE TABLE IF NOT EXISTS `parameters` (\n" +
                    "  `idparameters` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `name` MEDIUMTEXT NOT NULL,\n" +
                    "  `address` MEDIUMTEXT NOT NULL,\n" +
                    "  `code` int NOT NULL,\n" +
                    "  `ceo` MEDIUMTEXT NOT NULL,\n" +
                    "  `accountant` MEDIUMTEXT NOT NULL,\n" +
                    "  `typeFull` LONGTEXT NOT NULL,\n" +
                    "  `typeShort` MEDIUMTEXT NOT NULL,\n" +
                    "  PRIMARY KEY (`idparameters`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";

            statement.executeUpdate(createParametersTable);

            String checkQuery = "SELECT COUNT(*) FROM `parameters`";
            String insertParametersTable = "INSERT INTO `parameters` (`name`, `address`, `code`, `ceo`, `accountant`, `typeFull`, `typeShort`) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                 ResultSet rs = checkStmt.executeQuery()) {

                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement pstmt = connection.prepareStatement(insertParametersTable)) {
                        pstmt.setString(1, companyName);
                        pstmt.setString(2, address);
                        pstmt.setString(3, code);
                        pstmt.setString(4, ceo);
                        pstmt.setString(5, accountant);
                        pstmt.setString(6, typeFull);
                        pstmt.setString(7, typeShort);

                        pstmt.executeUpdate();
                        System.out.println("New record inserted.");
                    }
                } else {
                    System.out.println("A record already exists. No new record inserted.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }


            String createPositionsTable = "CREATE TABLE IF NOT EXISTS `positions` (\n" +
                    "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `nameN` LONGTEXT NOT NULL,\n" +
                    "  `nameR` LONGTEXT NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n";

            statement.executeUpdate(createPositionsTable);

            String createWorkersTable = "CREATE TABLE IF NOT EXISTS `workers` (\n" +
                    "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `nameN` MEDIUMTEXT NOT NULL,\n" +
                    "  `nameR` MEDIUMTEXT NOT NULL,\n" +
                    "  `nameD` MEDIUMTEXT NOT NULL,\n" +
                    "  `positionId` int NOT NULL,\n" +
                    "  `drivingLicence` MEDIUMTEXT DEFAULT NULL,\n" +
                    "  `start-date` date NOT NULL,\n" +
                    "  `start-order-number` MEDIUMTEXT NOT NULL,\n" +
                    "  `end-date` date DEFAULT NULL,\n" +
                    "  `end-order-number` MEDIUMTEXT DEFAULT NULL,\n" +
                    "  `valid` tinyint NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n";

            statement.executeUpdate(createWorkersTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        CreateGuestUser();
    }

    public void deleteCompany(String code) {
        try (Connection connection = Connect();
             Statement statement = connection.createStatement()) {
            String createDB = "DROP DATABASE `"+code+"`;";
            statement.executeUpdate(createDB);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeParametersCompany(String companyName, String address, int code, String ceo, String accountant, String typeFull, String typeShort) {
        String updateListsSQL = "UPDATE `parameters` SET " +
                "`name` = ?, `address` = ?, `code` = ?, `ceo` = ?, `accountant` = ?, `typeFull` = ?, `typeShort` = ?" +
                "WHERE `code` = ?";

        try (Connection connection = Connect();
             CallableStatement updateListsStmt = connection.prepareCall(updateListsSQL)) {

            updateListsStmt.setString(1, companyName);
            updateListsStmt.setString(2, address);
            updateListsStmt.setInt(3, code);
            updateListsStmt.setString(4, ceo);
            updateListsStmt.setString(5, accountant);
            updateListsStmt.setString(6, typeFull);
            updateListsStmt.setString(7, typeShort);
            updateListsStmt.setInt(8, code);

            updateListsStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error company: " + e.getMessage());
        }

        createCompany(companyName, address, String.valueOf(code), ceo, accountant, typeFull ,typeShort);
    }

    public List<_Report> getReports() {
        String sql = "SELECT * FROM reports";
        ObservableList<_Report> reports = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int idOrder = resultSet.getInt("id-order");
                String comments = resultSet.getString("comments");
                LocalDate date = resultSet.getDate("date").toLocalDate();

                _Report report = new _Report(id, idOrder, comments, date);

                reports.add(report);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return reports;
    }

    public _Company getCompanyInfo() {
        String sql = "SELECT * FROM parameters;";

        _Company company = new _Company();
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                company.setName(resultSet.getString("name"));
                company.setAddress(resultSet.getString("address"));
                company.setCode(resultSet.getInt("code"));
                company.setCeo(resultSet.getString("ceo"));
                company.setAccountant(resultSet.getString("accountant"));
                company.setTypeFull(resultSet.getString("typeFull"));
                company.setTypeShort(resultSet.getString("typeShort"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return company;
    }

    public List<_List> getListsFiltered(List<String> Numbers, LocalDate startDate, LocalDate endDate) {
        if (Numbers == null || Numbers.isEmpty()) {
            return new ArrayList<>();
        }

        List<_List> listsAll = new ArrayList<>();

        // Створюємо плейсхолдери для IN clause
        String placeholders = String.join(",", Numbers.stream().map(n -> "?").toArray(String[]::new));

        String sql = "SELECT l.*, " +
                    "c.number as car_number, c.`id-car`, " +
                    "o.`order-number`, o.`start-date` as order_start_date, o.`end-date` as order_end_date, " +
                    "o.route as order_route, o.goal as order_goal, o.`id-worker` as order_worker_id " +
                    "FROM lists l " +
                    "JOIN cars c ON l.`id-car` = c.`id-car` " +
                    "LEFT JOIN orders o ON l.`id-order` = o.`id-order` " +
                    "WHERE c.number IN (" + placeholders + ") " +
                    "AND l.done = TRUE " +
                    "AND l.`end-date` BETWEEN ? AND ?";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Встановлюємо параметри для IN clause
            int paramIndex = 1;
            for (String carNumber : Numbers) {
                statement.setString(paramIndex++, carNumber);
            }
            statement.setDate(paramIndex++, Date.valueOf(startDate));
            statement.setDate(paramIndex, Date.valueOf(endDate));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int number = resultSet.getInt("number");
                int idCar = resultSet.getInt("id-car");
                int idOrder = resultSet.getInt("id-order");
                double startM = resultSet.getDouble("start-mileage");
                double startF = resultSet.getDouble("start-fuel");
                boolean done = resultSet.getBoolean("done");

                _List list = new _List(id, number, idOrder, idCar, startM, startF, done);

                if (done) {
                    double endM = resultSet.getDouble("end-mileage");
                    double endF = resultSet.getDouble("end-fuel");
                    double refuel = resultSet.getDouble("refuel");
                    list.setEndFuel(endF);
                    list.setEndMileage(endM);
                    list.setRefuel(refuel);
                }

                if (idOrder == -1) {
                    LocalDate startData = resultSet.getDate("start-date").toLocalDate();
                    LocalDate endData = resultSet.getDate("end-date").toLocalDate();
                    String route = resultSet.getString("route");
                    String goal = resultSet.getString("goal");
                    int workerId = resultSet.getInt("id-worker");
                    list.setStartDate(startData);
                    list.setEndDate(endData);
                    list.setRoute(route);
                    list.setGoal(goal);
                    list.setIdWorker(workerId);
                    list.setIdOrder(-1);
                } else {
                    list.setIdOrder(idOrder);
                    Date orderStartDate = resultSet.getDate("order_start_date");
                    Date orderEndDate = resultSet.getDate("order_end_date");
                    list.setStartDate(orderStartDate != null ? orderStartDate.toLocalDate() : null);
                    list.setEndDate(orderEndDate != null ? orderEndDate.toLocalDate() : null);
                    list.setRoute(resultSet.getString("order_route"));
                    list.setGoal(resultSet.getString("order_goal"));
                    list.setIdWorker(resultSet.getInt("order_worker_id"));
                }
                listsAll.add(list);
            }
        } catch (Exception e) {
            System.err.println("Error getting filtered lists: " + e.getMessage());
            e.printStackTrace();
        }
        return listsAll;
    }

    public List<FuelUsage> getListsFuelFiltered(List<String> Numbers, PeriodParameters params) {
        if (Numbers == null || Numbers.isEmpty()) {
            return new ArrayList<>();
        }

        List<FuelUsage> fuelUsages = new ArrayList<>();

        // Створюємо плейсхолдери для IN clause
        String placeholders = String.join(",", Numbers.stream().map(n -> "?").toArray(String[]::new));

        String sql = "SELECT l.*, " +
                    "c.number as car_number, c.`id-car`, c.`fuel-usage` as car_fuel_usage, " +
                    "o.`start-date` as order_start_date, o.`end-date` as order_end_date " +
                    "FROM lists l " +
                    "JOIN cars c ON l.`id-car` = c.`id-car` " +
                    "LEFT JOIN orders o ON l.`id-order` = o.`id-order` " +
                    "WHERE c.number IN (" + placeholders + ") " +
                    "AND l.done = TRUE " +
                    "AND l.`end-date` BETWEEN ? AND ?";

        Map<String, List<_List>> carListsMap = new HashMap<>();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int paramIndex = 1;
            for (String carNumber : Numbers) {
                statement.setString(paramIndex++, carNumber);
            }
            statement.setDate(paramIndex++, Date.valueOf(params.getStartDate()));
            statement.setDate(paramIndex, Date.valueOf(params.getEndDate()));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int number = resultSet.getInt("number");
                int idCar = resultSet.getInt("id-car");
                int idOrder = resultSet.getInt("id-order");
                double startM = resultSet.getDouble("start-mileage");
                double startF = resultSet.getDouble("start-fuel");
                double endM = resultSet.getDouble("end-mileage");
                double endF = resultSet.getDouble("end-fuel");
                double refuel = resultSet.getDouble("refuel");
                String carNumber = resultSet.getString("car_number");

                _List list = new _List(id, number, idOrder, idCar, startM, startF, true);
                list.setEndFuel(endF);
                list.setEndMileage(endM);
                list.setRefuel(refuel);

                if (idOrder == -1) {
                    LocalDate startData = resultSet.getDate("start-date").toLocalDate();
                    LocalDate endData = resultSet.getDate("end-date").toLocalDate();
                    list.setStartDate(startData);
                    list.setEndDate(endData);
                } else {
                    Date orderStartDate = resultSet.getDate("order_start_date");
                    Date orderEndDate = resultSet.getDate("order_end_date");
                    list.setStartDate(orderStartDate != null ? orderStartDate.toLocalDate() : null);
                    list.setEndDate(orderEndDate != null ? orderEndDate.toLocalDate() : null);
                }

                carListsMap.computeIfAbsent(carNumber, k -> new ArrayList<>()).add(list);
            }
        } catch (Exception e) {
            System.err.println("Error getting fuel filtered lists: " + e.getMessage());
            e.printStackTrace();
        }

        // Обробка кожного автомобіля
        for (String carNumber : Numbers) {
            List<_List> carLists = carListsMap.getOrDefault(carNumber, new ArrayList<>());

            if (params.getPeriod().equals(Period.ofYears(2))) {
                // "По листах" — кожен лист окремо
                for (_List list : carLists) {
                    double mileage = list.getEndMileage() - list.getStartMileage();
                    double fuelFact = (list.getStartFuel() - list.getEndFuel()) + list.getRefuel();
                    double fuelNorm = mileage * getCarFuelUsage(list.getIdCar()) / 100;

                    fuelUsages.add(new FuelUsage(
                            list.getStartDate(),
                            list.getEndDate(),
                            carNumber,
                            mileage,
                            fuelFact,
                            fuelNorm
                    ));
                }
            } else {
                // періодичний варіант
                fuelUsages.addAll(getFuelUsagesPeriodics(carNumber, carLists, params));
            }
        }
        return fuelUsages;
    }

    private List<FuelUsage> getFuelUsagesPeriodics(String carNumber, List<_List> carLists, PeriodParameters params) {
        List<FuelUsage> fuelUsages = new ArrayList<>();
        Set<_List> processedLists = new HashSet<>();
        LocalDate currentStart = params.getStartDate();
        LocalDate endDate = params.getEndDate();

        if(params.getPeriod().equals(Period.ofWeeks(1))){
            currentStart = params.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = params.getEndDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }
        if(params.getPeriod().equals(Period.ofMonths(1))){
            currentStart = params.getStartDate().withDayOfMonth(1);
            endDate = params.getEndDate().withDayOfMonth(params.getEndDate().lengthOfMonth());

        }
        if(params.getPeriod().equals(Period.ofMonths(3))){
            Month startQuarterMonth = Month.of(((params.getStartDate().getMonthValue() - 1) / 3) * 3 + 1);
            currentStart = params.getStartDate().withMonth(startQuarterMonth.getValue()).withDayOfMonth(1);

            Month endQuarterMonth = Month.of(((params.getEndDate().getMonthValue() - 1) / 3) * 3 + 3);
            endDate = params.getEndDate().withMonth(endQuarterMonth.getValue()).withDayOfMonth(params.getEndDate().lengthOfMonth());

        }
        if(params.getPeriod().equals(Period.ofYears(1))){
            currentStart = params.getStartDate().withDayOfYear(1);
            endDate = params.getEndDate().withDayOfYear(params.getEndDate().lengthOfYear());
        }

        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plus(params.getPeriod()).minus(Period.ofDays(1));
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }

            double totalMileage = 0;
            double totalFuelFact = 0;
            double fuelNorm = 0;


            for (_List list : carLists) {
                if (!list.getEndDate().isBefore(currentStart) && !list.getEndDate().isAfter(currentEnd)) {
                    if (!processedLists.contains(list)) {
                        totalMileage += list.getEndMileage() - list.getStartMileage();
                        totalFuelFact += (list.getStartFuel() - list.getEndFuel()) + list.getRefuel();
                        processedLists.add(list);
                    }
                }
            }

            if (totalMileage > 0) {
                fuelNorm = totalMileage * getCarFuelUsage(carLists.get(0).getIdCar()) / 100;
                fuelUsages.add(new FuelUsage(currentStart, currentEnd, carNumber, totalMileage, totalFuelFact, fuelNorm));
            }

            currentStart = currentStart.plus(params.getPeriod());
        }
        return fuelUsages;
    }

    public List<_List> getListsForCars(List<String> Numbers) {
        if (Numbers == null || Numbers.isEmpty()) {
            return new ArrayList<>();
        }

        List<_List> ListsCars = new ArrayList<>();

        // Створюємо плейсхолдери для IN clause
        String placeholders = String.join(",", Numbers.stream().map(n -> "?").toArray(String[]::new));

        String sql = "SELECT l.*, " +
                    "c.number as car_number, c.`id-car`, " +
                    "o.`order-number`, o.`start-date` as order_start_date, o.`end-date` as order_end_date, " +
                    "o.route as order_route, o.goal as order_goal, o.`id-worker` as order_worker_id " +
                    "FROM lists l " +
                    "JOIN cars c ON l.`id-car` = c.`id-car` " +
                    "LEFT JOIN orders o ON l.`id-order` = o.`id-order` " +
                    "WHERE c.number IN (" + placeholders + ")";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Встановлюємо параметри для IN clause
            for (int i = 0; i < Numbers.size(); i++) {
                statement.setString(i + 1, Numbers.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int number = resultSet.getInt("number");
                int idCar = resultSet.getInt("id-car");
                int idOrder = resultSet.getInt("id-order");
                double startM = resultSet.getDouble("start-mileage");
                double startF = resultSet.getDouble("start-fuel");
                boolean done = resultSet.getBoolean("done");

                _List list = new _List(id, number, idOrder, idCar, startM, startF, done);

                if (done) {
                    double endM = resultSet.getDouble("end-mileage");
                    double endF = resultSet.getDouble("end-fuel");
                    double refuel = resultSet.getDouble("refuel");
                    list.setEndFuel(endF);
                    list.setEndMileage(endM);
                    list.setRefuel(refuel);
                }

                if (idOrder == -1) {
                    // Дані безпосередньо з таблиці lists
                    LocalDate startData = resultSet.getDate("start-date").toLocalDate();
                    LocalDate endData = resultSet.getDate("end-date").toLocalDate();
                    String route = resultSet.getString("route");
                    String goal = resultSet.getString("goal");
                    int workerId = resultSet.getInt("id-worker");
                    list.setStartDate(startData);
                    list.setEndDate(endData);
                    list.setRoute(route);
                    list.setGoal(goal);
                    list.setIdWorker(workerId);
                    list.setIdOrder(-1);
                } else {
                    // Дані з JOIN з таблицею orders
                    list.setIdOrder(idOrder);
                    Date orderStartDate = resultSet.getDate("order_start_date");
                    Date orderEndDate = resultSet.getDate("order_end_date");
                    list.setStartDate(orderStartDate != null ? orderStartDate.toLocalDate() : null);
                    list.setEndDate(orderEndDate != null ? orderEndDate.toLocalDate() : null);
                    list.setRoute(resultSet.getString("order_route"));
                    list.setGoal(resultSet.getString("order_goal"));
                    list.setIdWorker(resultSet.getInt("order_worker_id"));
                }
                ListsCars.add(list);
            }
        } catch (Exception e) {
            System.err.println("Error getting lists for cars: " + e.getMessage());
            e.printStackTrace();
        }
        return ListsCars;
    }

    private List<FuelUsage> getFuelUsagesByLists(String carNumber, List<_List> carLists) {
        List<FuelUsage> usages = new ArrayList<>();
        for (_List list : carLists) {
            if (list.getEndDate() != null && list.getStartDate() != null) {
                double mileage = list.getEndMileage() - list.getStartMileage();
                double fuelFact = (list.getStartFuel() - list.getEndFuel()) + list.getRefuel();
                double fuelNorm = mileage * getCarFuelUsage(list.getIdCar()) / 100;

                usages.add(new FuelUsage(
                        list.getStartDate(),
                        list.getEndDate(),
                        carNumber,
                        mileage,
                        fuelFact,
                        fuelNorm
                ));
            }
        }
        return usages;
    }



    public List<_List> getLists() {
        String sql = "SELECT l.*, " +
                    "o.`order-number`, o.`start-date` as order_start_date, o.`end-date` as order_end_date, " +
                    "o.route as order_route, o.goal as order_goal, o.`id-worker` as order_worker_id " +
                    "FROM lists l " +
                    "LEFT JOIN orders o ON l.`id-order` = o.`id-order`";
        ObservableList<_List> lists = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int number = resultSet.getInt("number");
                int idCar = resultSet.getInt("id-car");
                int idOrder = resultSet.getInt("id-order");
                double startM = resultSet.getDouble("start-mileage");
                double startF = resultSet.getDouble("start-fuel");
                boolean done = resultSet.getBoolean("done");

                _List list = new _List(id, number, idOrder, idCar, startM, startF, done);

                if(done) {
                    double endM = resultSet.getDouble("end-mileage");
                    double endF = resultSet.getDouble("end-fuel");
                    double refuel = resultSet.getDouble("refuel");
                    list.setEndFuel(endF);
                    list.setEndMileage(endM);
                    list.setRefuel(refuel);
                }

                if(idOrder == -1) {
                    LocalDate startData = resultSet.getDate("start-date").toLocalDate();
                    LocalDate endData = resultSet.getDate("end-date").toLocalDate();
                    String route = resultSet.getString("route");
                    String goal  = resultSet.getString("goal");
                    int workerId = resultSet.getInt("id-worker");
                    list.setStartDate(startData);
                    list.setEndDate(endData);
                    list.setRoute(route);
                    list.setGoal(goal);
                    list.setIdWorker(workerId);
                    list.setIdOrder(-1);
                } else {
                    list.setIdOrder(idOrder);
                    Date orderStartDate = resultSet.getDate("order_start_date");
                    Date orderEndDate = resultSet.getDate("order_end_date");
                    list.setStartDate(orderStartDate != null ? orderStartDate.toLocalDate() : null);
                    list.setEndDate(orderEndDate != null ? orderEndDate.toLocalDate() : null);
                    list.setRoute(resultSet.getString("order_route"));
                    list.setGoal(resultSet.getString("order_goal"));
                    list.setIdWorker(resultSet.getInt("order_worker_id"));
                }
                lists.add(list);
            }

        } catch (Exception e) {
            System.err.println("Error getting all lists: " + e.getMessage());
            e.printStackTrace();
        }
        return lists;
    }

    public boolean addList(_List list) {
        try (Connection connection = Connect();
             PreparedStatement checkCarStmt = connection.prepareStatement(
                     "SELECT `start-mileage`, `start-fuel` FROM cars WHERE `id-car` = ?");
             PreparedStatement checkMaxNumberStmt = connection.prepareStatement(
                     "SELECT MAX(number) FROM lists WHERE YEAR(`start-date`) = YEAR(?)");
             PreparedStatement checkPrevListStmt = connection.prepareStatement(
                     "SELECT `end-mileage`, `end-fuel` FROM lists WHERE `id-car` = ? ORDER BY `start-date` DESC, id DESC LIMIT 1");
             PreparedStatement insertListStmt = connection.prepareStatement(
                     "INSERT INTO lists (number, `id-car`, `id-order`, `start-mileage`, `start-fuel`, `done`, `end-mileage`, `end-fuel`, `refuel`, `start-date`, `end-date`, `route`, `goal`, `id-worker`) VALUES (?, ?, ?, ?, ?, false, ?, ?, ?, ?, ?, ?, ?, ?)")
        ) {
            checkCarStmt.setInt(1, list.getIdCar());
            ResultSet carResult = checkCarStmt.executeQuery();
            double carStartMileage = 0, carStartFuel = 0;
            if (carResult.next()) {
                carStartMileage = carResult.getDouble(1);
                carStartFuel = carResult.getDouble(2);
            }


            checkMaxNumberStmt.setDate(1, java.sql.Date.valueOf(list.getStartDate()));
            ResultSet maxNumberResult = checkMaxNumberStmt.executeQuery();
            int newNumber = 1;
            if (maxNumberResult.next() && maxNumberResult.getInt(1) != 0) {
                newNumber = maxNumberResult.getInt(1) + 1;
            }

            checkPrevListStmt.setInt(1, list.getIdCar());
            ResultSet prevListResult = checkPrevListStmt.executeQuery();
            double prevMileage = carStartMileage, prevFuel = carStartFuel;
            if (prevListResult.next()) {
                if( prevListResult.getDouble(1) != 0 && prevListResult.getDouble(2) != 0) {
                    prevMileage = prevListResult.getDouble(1);
                    prevFuel = prevListResult.getDouble(2);
                }
            }



            insertListStmt.setInt(1, newNumber);
            insertListStmt.setInt(2, list.getIdCar());
            insertListStmt.setInt(3, list.getIdOrder());
            insertListStmt.setDouble(4, prevMileage);
            insertListStmt.setDouble(5, prevFuel);
            insertListStmt.setDouble(6, list.getEndMileage());
            insertListStmt.setDouble(7, list.getEndFuel());
            insertListStmt.setDouble(8, list.getRefuel());
            insertListStmt.setDate(9, java.sql.Date.valueOf(list.getStartDate()));
            insertListStmt.setDate(10, java.sql.Date.valueOf(list.getEndDate()));
            insertListStmt.setString(11, list.getRoute());
            insertListStmt.setString(12, list.getGoal());
            insertListStmt.setInt(13, list.getIdWorker());

            insertListStmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Error adding list: " + e.getMessage());
            return false;
        }
    }

    public boolean updateList(_List list) {
        String updateListsSQL = "UPDATE `lists` SET " +
                "`start-date` = ?, `end-date` = ?, `route` = ?, `goal` = ?, `id-worker` = ?, `id-order` = ?, `id-car` = ?," +
                " `start-mileage` = ?, `start-fuel` = ?, `end-mileage` = ?, `end-fuel` = ?, `refuel` = ?, `done` = ?, `number` = ? " +
                "WHERE `id` = ?";

        try (Connection connection = Connect();
             CallableStatement updateListsStmt = connection.prepareCall(updateListsSQL)) {

            updateListsStmt.setDate(1, java.sql.Date.valueOf(list.getStartDate()));
            updateListsStmt.setDate(2, java.sql.Date.valueOf(list.getEndDate()));
            updateListsStmt.setString(3, list.getRoute());
            updateListsStmt.setString(4, list.getGoal());
            updateListsStmt.setInt(5, list.getIdWorker());
            updateListsStmt.setInt(6, list.getIdOrder());
            updateListsStmt.setInt(7, list.getIdCar());
            updateListsStmt.setDouble(8, list.getStartMileage());
            updateListsStmt.setDouble(9, list.getStartFuel());
            updateListsStmt.setDouble(10, list.getEndMileage());
            updateListsStmt.setDouble(11, list.getEndFuel());
            updateListsStmt.setDouble(12, list.getRefuel());
            updateListsStmt.setBoolean(13, list.isDone());
            updateListsStmt.setInt(14, list.getNumber());
            updateListsStmt.setInt(15, list.getId());

            updateListsStmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
            return false;
        }
    }

    public void deleteCar(_Car car) {
        String deletelist = "DELETE FROM `cars` WHERE (`id-car` = '"+car.getId()+"');";
        try (Connection connection = Connect();
             CallableStatement deletelistStmt = connection.prepareCall(deletelist)) {
            deletelistStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
        }
    }

    public void deleteReport(_Report report) {
        String deletelist = "DELETE FROM `reports` WHERE (`id` = '"+report.getId()+"');";
        try (Connection connection = Connect();
             CallableStatement deletelistStmt = connection.prepareCall(deletelist)) {
            deletelistStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
        }
    }

    public void deleteWorker(_Worker worker) {
        String deletelist = "DELETE FROM `workers` WHERE (`id` = '"+worker.getId()+"');";
        try (Connection connection = Connect();
             CallableStatement deletelistStmt = connection.prepareCall(deletelist)) {
            deletelistStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
        }
    }

    public void deleteList(_List list) {
        String deletelist = "DELETE FROM `lists` WHERE (`id` = '"+list.getId()+"');";
        try (Connection connection = Connect();
             CallableStatement deletelistStmt = connection.prepareCall(deletelist)) {
            deletelistStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
        }
    }

    public void deleteOrder(_Order order) {
        String deletelist = "DELETE FROM `orders` WHERE (`id-order` = '"+order.getId()+"');";
        try (Connection connection = Connect();
             CallableStatement deletelistStmt = connection.prepareCall(deletelist)) {
            deletelistStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
        }
    }


    public boolean isOrderModifiable(int orderId) {
        String query = "SELECT COUNT(*) FROM reports WHERE `id-order` = ?;";
        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<_Order> getOrders() {

        String sql = "SELECT * FROM orders";
        ObservableList<_Order> orders = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id-order");
                LocalDate orderDate = resultSet.getDate("order-date").toLocalDate();
                String orderNumber = resultSet.getString("order-number");
                int idWorker = resultSet.getInt("id-worker");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                LocalDate endDate = resultSet.getDate("end-date").toLocalDate();
                String route = resultSet.getString("route");
                double money = resultSet.getDouble("money");
                String goal = resultSet.getString("goal");
                String head = resultSet.getString("head");

                _Order order = new _Order(id, orderDate, orderNumber, idWorker, startDate, endDate, route, money, goal, head);
                orders.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        return orders;
    }

    public List<_Order> getOrdersFiltered(String workerNameN, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM orders");
        List<Object> parameters = new ArrayList<>();
        boolean hasCondition = false;

        if (workerNameN != null && !workerNameN.isEmpty() && !workerNameN.equals("-1")) {
            String workerSql = "SELECT id FROM workers WHERE nameN = ?";
            List<Integer> workerIds = new ArrayList<>();

            try (Connection connection = Connect();
                 PreparedStatement workerStatement = connection.prepareStatement(workerSql)) {
                workerStatement.setString(1, workerNameN);
                try (ResultSet workerResultSet = workerStatement.executeQuery()) {
                    while (workerResultSet.next()) {
                        workerIds.add(workerResultSet.getInt("id"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!workerIds.isEmpty()) {
                sql.append(hasCondition ? " AND " : " WHERE ").append("`id-worker` IN (");
                for (int i = 0; i < workerIds.size(); i++) {
                    sql.append("?");
                    if (i < workerIds.size() - 1) {
                        sql.append(", ");
                    }
                }
                sql.append(")");
                parameters.addAll(workerIds);
                hasCondition = true;
            }
        }

        if (startDate != null) {
            sql.append(hasCondition ? " AND " : " WHERE ").append("`order-date` >= ?");
            parameters.add(Date.valueOf(startDate));
            hasCondition = true;
        }

        if (endDate != null) {
            sql.append(hasCondition ? " AND " : " WHERE ").append("`order-date` <= ?");
            parameters.add(Date.valueOf(endDate));
        }

        if (startDate == null && endDate == null) {
            sql.append(hasCondition ? " AND " : " WHERE ").append("`order-date` IS NOT NULL");
        }

        ObservableList<_Order> orders = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id-order");
                    LocalDate orderDate = resultSet.getDate("order-date").toLocalDate();
                    String orderNumber = resultSet.getString("order-number");
                    int idWorker = resultSet.getInt("id-worker");
                    LocalDate startDate_ = resultSet.getDate("start-date").toLocalDate();
                    LocalDate endDate_ = resultSet.getDate("end-date").toLocalDate();
                    String route = resultSet.getString("route");
                    double money = resultSet.getDouble("money");
                    String goal = resultSet.getString("goal");
                    String head = resultSet.getString("head");

                    _Order order = new _Order(id, orderDate, orderNumber, idWorker, startDate_, endDate_, route, money, goal, head);
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<_Order> getFreeOrders() {
        String sql = "SELECT * FROM orders "
                + "WHERE `id-order` NOT IN ("
                + "    SELECT DISTINCT `id-order` FROM lists"
                + ")";

        List<_Order> orders = new ArrayList<>();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id-order");
                LocalDate orderDate = resultSet.getDate("order-date").toLocalDate();
                String orderNumber = resultSet.getString("order-number");
                int idWorker = resultSet.getInt("id-worker");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                LocalDate endDate = resultSet.getDate("end-date").toLocalDate();
                String route = resultSet.getString("route");
                double money = resultSet.getDouble("money");
                String goal = resultSet.getString("goal");
                String head = resultSet.getString("head");

                _Order order = new _Order(id, orderDate, orderNumber, idWorker, startDate, endDate, route, money, goal, head);
                orders.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<_Order> getOpenOrders() {
        String sql = "SELECT * FROM orders "
                + "WHERE `id-order` NOT IN ("
                + "    SELECT DISTINCT `id-order` FROM reports"
                + ")";

        List<_Order> orders = new ArrayList<>();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id-order");
                LocalDate orderDate = resultSet.getDate("order-date").toLocalDate();
                String orderNumber = resultSet.getString("order-number");
                int idWorker = resultSet.getInt("id-worker");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                LocalDate endDate = resultSet.getDate("end-date").toLocalDate();
                String route = resultSet.getString("route");
                double money = resultSet.getDouble("money");
                String goal = resultSet.getString("goal");
                String head = resultSet.getString("head");

                _Order order = new _Order(id, orderDate, orderNumber, idWorker, startDate, endDate, route, money, goal, head);
                orders.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean addOrder(_Order order) {
        String sql = "INSERT INTO orders (`order-date`, `order-number`, `id-worker`, `start-date`, `end-date`, `route`, `money`, `goal`, `head`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = Connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, java.sql.Date.valueOf(order.getOrderDate()));
            preparedStatement.setString(2, order.getOrderNumber());
            preparedStatement.setInt(3, order.getIdWorker());
            preparedStatement.setDate(4, java.sql.Date.valueOf(order.getStartDate()));
            preparedStatement.setDate(5, java.sql.Date.valueOf(order.getEndDate()));
            preparedStatement.setString(6, order.getRoute());
            preparedStatement.setDouble(7, order.getMoney());
            preparedStatement.setString(8, order.getGoal());
            preparedStatement.setString(9, order.getHead());

            preparedStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Error adding order: " + e.getMessage());
            return false;
        }
    }

    public String getNextOrderNumber(LocalDate targetDate) {
        String query = "SELECT MAX(CAST(SUBSTRING_INDEX(`order-number`, '-', 1) AS UNSIGNED)) AS max_num " +
                "FROM orders WHERE YEAR(`order-date`) = YEAR(?)";

        Integer maxNumber = null;
        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, java.sql.Date.valueOf(targetDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt("max_num");
                    if (!rs.wasNull()) {
                        maxNumber = value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int next = (maxNumber == null || maxNumber == 0) ? 1 : maxNumber + 1;
        return String.valueOf(next) + "-вд";
    }

    public boolean changeOrder(_Order order) {
        String sql = "UPDATE `orders` SET" +
                " `order-date` = '" + order.getOrderDate() + "'," +
                " `order-number` = '" + order.getOrderNumber() + "'," +
                " `id-worker` = '" + order.getIdWorker() + "'," +
                " `start-date` = '" + order.getStartDate() + "'," +
                " `end-date` = '" + order.getEndDate() + "'," +
                " `route` = '" + order.getRoute() + "'," +
                " `money` = '" + order.getMoney() + "'," +
                " `goal` = '" + order.getGoal() + "'," +
                " `head` = '" + order.getHead() + "'" +
                " WHERE `id-order` = '" + order.getId() + "';";

        String updateListsSQL = "UPDATE `lists` SET " +
                "`start-date` = ?, `end-date` = ?, `route` = ?, `goal` = ?, `id-worker` = ? " +
                "WHERE `id-order` = ?";

        try (Connection connection = Connect();
             CallableStatement callableStatement = connection.prepareCall(sql);
        CallableStatement updateListsStmt = connection.prepareCall(updateListsSQL)) {

            callableStatement.execute();
            updateListsStmt.setDate(1, java.sql.Date.valueOf(order.getStartDate()));
            updateListsStmt.setDate(2, java.sql.Date.valueOf(order.getEndDate()));
            updateListsStmt.setString(3, order.getRoute());
            updateListsStmt.setString(4, order.getGoal());
            updateListsStmt.setInt(5, order.getIdWorker());
            updateListsStmt.setInt(6, order.getId());

            updateListsStmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
            return false;
        }
    }

    public boolean changeReport(_Report report) {
        String sql = "UPDATE `reports` SET" +
                " `comments` = '" + report.getComments() + "', " +
                " `date`  = '" + report.getDate() + "'" +
                " WHERE `id` = '" + report.getId() + "';";

        try (Connection connection = Connect();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            callableStatement.execute();
            return true;
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
            return false;
        }

    }

    public boolean addReport(_Report report) {
        String insertSql = "INSERT INTO reports (comments, `id-order`, `date`) "
                + "VALUES (?, ?, ?)";

        try (Connection connection = Connect();
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

                insertStmt.setString(1, report.getComments());
                insertStmt.setInt(2, report.getOrderId());
                insertStmt.setDate(3, Date.valueOf(report.getDate()));

                insertStmt.executeUpdate();
                return true;
        } catch (SQLException e) {
            System.err.println("Error adding car: " + e.getMessage());
        }
        return false;
    }

    public String getOrderNumber(int id) {
        // Перевірка кешу
        if (orderNumberCache.containsKey(id)) {
            return orderNumberCache.get(id);
        }

        String sql = "SELECT `order-number` FROM orders WHERE `id-order` = ?";
        String orderNumber = "";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                orderNumber = resultSet.getString("order-number");
            }

        } catch (Exception e) {
            System.err.println("Error getting order number: " + e.getMessage());
            e.printStackTrace();
        }

        // Зберігаємо в кеш
        orderNumberCache.put(id, orderNumber);
        return orderNumber;
    }

    public String getOrderWorkerName(int id) {
        String sql = "select `id-worker` from orders where `id-order` = " + String.valueOf(id);
        int idW = 0;
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                idW = resultSet.getInt("id-worker");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return getWorkerName(true, idW);
    }

    public String getOrderRoute(int id) {
        String sql = "select `route` from orders where `id-order` = " + String.valueOf(id);
        String r = "";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                r = resultSet.getString("route");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public String getOrderHead(int id) {
        String sql = "select `head` from orders where `id-order` = " + String.valueOf(id);
        String r = "";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                r = resultSet.getString("head");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public int getOrderIdWorker(int id) {
        String sql = "select `id-worker` from orders where `id-order` = " + String.valueOf(id);
        int r = 0;
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                r = resultSet.getInt("id-worker");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public String getOrderGoal(int id) {
        String sql = "select `goal` from orders where `id-order` = " + String.valueOf(id);
        String r = "";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                r = resultSet.getString("goal");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public LocalDate getStartOrderDate(int id) {
        String sql = "select `start-date` from orders where `id-order` = " + String.valueOf(id);
        LocalDate r = LocalDate.now();
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                r = resultSet.getDate("start-date").toLocalDate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public LocalDate getOrderDate(int id) {
        String sql = "select `order-date` from orders where `id-order` = " + String.valueOf(id);
        LocalDate r = LocalDate.now();
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                r = resultSet.getDate("order-date").toLocalDate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public LocalDate getEndOrderDate(int id) {
        String sql = "select `end-date` from orders where `id-order` = " + String.valueOf(id);
        LocalDate date = LocalDate.now();
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                date = resultSet.getDate("end-date").toLocalDate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }


    public List<_Car> getCars() {
        String sql = "SELECT * FROM cars";
        ObservableList<_Car> cars = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id-car");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuelType = resultSet.getString("fuel-type");
                double fuelUsage = resultSet.getDouble("fuel-usage");
                double engineVolume = resultSet.getDouble("engine-volume");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");

                double startFuel = resultSet.getDouble("start-fuel");
                double startMileage = resultSet.getDouble("start-mileage");

                Date endDateSql = resultSet.getDate("end-date");
                LocalDate endDate = (endDateSql != null) ? ((java.sql.Date) endDateSql).toLocalDate() : null;

                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, number, model, fuelType, fuelUsage, engineVolume, startDate, startOrderNumber,endDate,endOrderNumber,valid, startFuel, startMileage);
                cars.add(car);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cars;
    }

    public _Car getCar(int idCar) {
        String sql = "SELECT * FROM cars where `id-car` = " + idCar;
        _Car car = new _Car();
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int id = resultSet.getInt("id-car");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuelType = resultSet.getString("fuel-type");
                double fuelUsage = resultSet.getDouble("fuel-usage");
                double engineVolume = resultSet.getDouble("engine-volume");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");

                double startFuel = resultSet.getDouble("start-fuel");
                double startMileage = resultSet.getDouble("start-mileage");

                Date endDateSql = resultSet.getDate("end-date");
                LocalDate endDate = (endDateSql != null) ? ((java.sql.Date) endDateSql).toLocalDate() : null;

                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                car = new _Car(id, number, model, fuelType, fuelUsage, engineVolume, startDate, startOrderNumber,endDate,endOrderNumber,valid, startFuel, startMileage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return car;
    }

    public Double getCarFuelUsage(int idCar) {
        String sql = "SELECT `fuel-usage` FROM cars where `id-car` = " + idCar;
        double fuelUsage = 0.0;
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                fuelUsage = resultSet.getDouble("fuel-usage");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fuelUsage;
    }

    public boolean addCar(_Car car) {
        String checkSql = "SELECT COUNT(*) FROM cars WHERE number = ? AND valid = 1";
        String insertSql = "INSERT INTO cars (number, model, `fuel-type`, `fuel-usage`, `engine-volume`, `start-date`, `start-order-number`, valid, `start-fuel`, `start-mileage`) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?)";

        try (Connection connection = Connect();
             PreparedStatement checkStmt = connection.prepareStatement(checkSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

            checkStmt.setString(1, car.getNumber());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) {
                insertStmt.setString(1, car.getNumber());
                insertStmt.setString(2, car.getModel());
                insertStmt.setString(3, car.getFuelType());
                insertStmt.setDouble(4, car.getFuelUsage());
                insertStmt.setDouble(5, car.getEngineVolume());
                insertStmt.setDate(6, java.sql.Date.valueOf(car.getStartDate()));
                insertStmt.setString(7, car.getStartOrderNumber());
                insertStmt.setDouble(8, car.getStartFuel());
                insertStmt.setDouble(9, car.getStartMileage());

                insertStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding car: " + e.getMessage());
        }
        return false;
    }

    public boolean removeCar(_Car car) {
        String sql = "UPDATE cars " +
                "SET `end-date` = ?, " +
                "`end-order-number` = ?, " +
                "valid = FALSE " +
                "WHERE `id-car` = ?";

        try (Connection connection = Connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, java.sql.Date.valueOf(car.getEndDate()));
            preparedStatement.setString(2, car.getEndOrderNumber());
            preparedStatement.setInt(3, car.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error removing car: " + e.getMessage());
            return false;
        }
    }

    public boolean changeCar(_Car car) {
        String sql;
        if (car.getEndDate() == null) {
            sql = "UPDATE `cars` SET" +
                    " `number` = '" + car.getNumber() + "'," +
                    " `model` = '" + car.getModel() + "'," +
                    " `fuel-type` = '" + car.getFuelType() + "'," +
                    " `fuel-usage` = '" + car.getFuelUsage() + "'," +
                    " `engine-volume` = '" + car.getEngineVolume() + "'," +
                    " `start-date` = '" + car.getStartDate() + "'," +
                    " `start-order-number` = '" + car.getStartOrderNumber() + "'," +
                    " `end-date` = NULL," +
                    " `end-order-number` = NULL," +
                    " `valid` = '1'," +
                    " `start-fuel` = '" + car.getStartFuel() + "'," +
                    " `start-mileage` = '" + car.getStartMileage() + "'," +
                    " `valid` = '1' " +
                    " WHERE `id-car` = '" + car.getId() + "';";
        } else {
            sql = "UPDATE `cars` SET" +
                    " `number` = '" + car.getNumber() + "'," +
                    " `model` = '" + car.getModel() + "'," +
                    " `fuel-type` = '" + car.getFuelType() + "'," +
                    " `fuel-usage` = '" + car.getFuelUsage() + "'," +
                    " `engine-volume` = '" + car.getEngineVolume() + "'," +
                    " `start-fuel` = '" + car.getStartFuel() + "'," +
                    " `start-mileage` = '" + car.getStartMileage() + "'," +
                    " `start-date` = '" + car.getStartDate() + "'," +
                    " `start-order-number` = '" + car.getStartOrderNumber() + "'," +
                    " `end-date` = '" + car.getEndDate() + "'," +
                    " `end-order-number` = '" + car.getEndOrderNumber() + "'," +
                    " `valid` = '0' " +
                    " WHERE `id-car` = '" + car.getId() + "';";
        }

        try (Connection connection = Connect();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            callableStatement.execute();
            return true;
        } catch (Exception e) {
            System.err.println("Error changing car: " + e.getMessage());
            return false;
        }
    }

    public String getCarNumber(int id) {
        String sql = "select `number` from cars where `id-car` = " + id;
        String Number = "";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                Number = resultSet.getString("number");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Number;
    }

    public List<_Car> getFreeCars() {
        String sql = "SELECT * FROM cars "
                + "WHERE cars.valid = true "
                + "AND cars.`id-car` NOT IN ("
                + "    SELECT lists.`id-car` "
                + "    FROM lists "
                + "    WHERE lists.done = false"
                + ")";

        List<_Car> cars = new ArrayList<>();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id-car");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuelType = resultSet.getString("fuel-type");
                double fuelUsage = resultSet.getDouble("fuel-usage");
                double engineVolume = resultSet.getDouble("engine-volume");
                double startFuel = resultSet.getDouble("start-fuel");
                double startMileage = resultSet.getDouble("start-mileage");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");

                Date endDateSql = resultSet.getDate("end-date");
                LocalDate endDate = (endDateSql != null) ? endDateSql.toLocalDate() : null;

                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, number, model, fuelType, fuelUsage, engineVolume, startDate, startOrderNumber, endDate, endOrderNumber, valid, startFuel, startMileage);
                cars.add(car);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cars;
    }

    public List<_Car> getValidCars() {
        String sql = "SELECT * FROM cars "
                + "WHERE cars.valid = true ";

        List<_Car> cars = new ArrayList<>();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id-car");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuelType = resultSet.getString("fuel-type");
                double fuelUsage = resultSet.getDouble("fuel-usage");
                double engineVolume = resultSet.getDouble("engine-volume");
                double startFuel = resultSet.getDouble("start-fuel");
                double startMileage = resultSet.getDouble("start-mileage");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");

                Date endDateSql = resultSet.getDate("end-date");
                LocalDate endDate = (endDateSql != null) ? endDateSql.toLocalDate() : null;

                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, number, model, fuelType, fuelUsage, engineVolume, startDate, startOrderNumber, endDate, endOrderNumber, valid, startFuel, startMileage);
                cars.add(car);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cars;
    }

    public List<_Car> getUnValidCars() {
        String sql = "SELECT * FROM cars "
                + "WHERE cars.valid = false ";

        List<_Car> cars = new ArrayList<>();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id-car");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuelType = resultSet.getString("fuel-type");
                double fuelUsage = resultSet.getDouble("fuel-usage");
                double engineVolume = resultSet.getDouble("engine-volume");
                double startFuel = resultSet.getDouble("start-fuel");
                double startMileage = resultSet.getDouble("start-mileage");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");

                Date endDateSql = resultSet.getDate("end-date");
                LocalDate endDate = (endDateSql != null) ? endDateSql.toLocalDate() : null;

                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, number, model, fuelType, fuelUsage, engineVolume, startDate, startOrderNumber, endDate, endOrderNumber, valid, startFuel, startMileage);
                cars.add(car);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cars;
    }



    public List<_Worker> getWorkers() {
        String sql = "SELECT * FROM workers";
        ObservableList<_Worker> workers = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nameN = resultSet.getString("nameN");
                String nameR = resultSet.getString("nameR");
                String nameD = resultSet.getString("nameD");
                int positionId = resultSet.getInt("positionId");
                String drivingLicense = resultSet.getString("drivingLicence");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");
                LocalDate endDate = (resultSet.getDate("end-date") != null) ? resultSet.getDate("end-date").toLocalDate() : null;
                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Worker worker = new _Worker(id, nameN, nameR,nameD, positionId, drivingLicense, startDate, startOrderNumber);

                worker.setPositionN(getPositionNameN(positionId));
                worker.setPositionR(getPositionNameR(positionId));


                if(!valid) {
                    worker.setEndDate(endDate);
                    worker.setEndOrderNumber(endOrderNumber);
                    worker.setValid(valid);
                }
                workers.add(worker);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return workers;
    }

    public List<String> getUniqueWorkersNames() {
        List<String> workerNames = new ArrayList<>();

        String sql = "SELECT DISTINCT NameN FROM workers";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String nameN = resultSet.getString("nameN");
                workerNames.add(nameN);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return workerNames;
    }

    public List<String> getUniqueCarsNumbers() {
        List<String> carNumbers = new ArrayList<>();

        String sql = "SELECT number, MAX(model) AS model FROM cars GROUP BY number";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String carNumber = resultSet.getString("number");
                String model = resultSet.getString("model");
                carNumbers.add(carNumber + " " + model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return carNumbers;
    }

    public List<_Worker> getFreeWorkers() {
        String sql = "SELECT workers.*, positions.nameN, positions.nameR FROM workers "
                + "JOIN positions ON workers.positionId = positions.id "
                + "WHERE workers.valid = true "
                + "AND workers.id NOT IN ("
                + "    SELECT DISTINCT lists.`id-worker` FROM lists WHERE lists.done = false"
                + ")\n"
                + "AND workers.id NOT IN (\n"
                + "SELECT DISTINCT orders.`id-worker` FROM orders LEFT JOIN reports ON orders.`id-order` = reports.`id-order` WHERE reports.`id-order` IS NULL);";

        List<_Worker> workers = new ArrayList<>();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nameN = resultSet.getString("nameN");
                String nameR = resultSet.getString("nameR");
                String nameD = resultSet.getString("nameD");
                int positionId = resultSet.getInt("positionId");
                String drivingLicense = resultSet.getString("drivingLicence");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");
                LocalDate endDate = (resultSet.getDate("end-date") != null) ? resultSet.getDate("end-date").toLocalDate() : null;
                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Worker worker = new _Worker(id, nameN, nameR,nameD, positionId, drivingLicense, startDate, startOrderNumber);

                worker.setPositionN(nameN);
                worker.setPositionR(nameR);

                if (!valid) {
                    worker.setEndDate(endDate);
                    worker.setEndOrderNumber(endOrderNumber);
                    worker.setValid(valid);
                }

                workers.add(worker);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return workers;
    }

    public _Worker getWorker(int workerId) {
        String sql = "SELECT * FROM workers WHERE id =" + String.valueOf(workerId);

        _Worker worker = new _Worker();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nameN = resultSet.getString("nameN");
                String nameR = resultSet.getString("nameR");
                String nameD = resultSet.getString("nameD");
                int positionId = resultSet.getInt("positionId");
                String drivingLicense = resultSet.getString("drivingLicence");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");
                LocalDate endDate = (resultSet.getDate("end-date") != null) ? resultSet.getDate("end-date").toLocalDate() : null;
                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                worker = new _Worker(id, nameN, nameR,nameD, positionId, drivingLicense, startDate, startOrderNumber);

                worker.setPositionN(getPositionNameN(positionId));
                worker.setPositionR(getPositionNameR(positionId));


                if(!valid) {
                    worker.setEndDate(endDate);
                    worker.setEndOrderNumber(endOrderNumber);
                    worker.setValid(valid);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return worker;
    }

    public List<_Worker> getValidWorkers() {
        String sql = "SELECT * FROM workers WHERE valid = true";
        ObservableList<_Worker> workers = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nameN = resultSet.getString("nameN");
                String nameR = resultSet.getString("nameR");
                String nameD = resultSet.getString("nameD");
                int positionId = resultSet.getInt("positionId");
                String drivingLicense = resultSet.getString("drivingLicence");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");
                LocalDate endDate = (resultSet.getDate("end-date") != null) ? resultSet.getDate("end-date").toLocalDate() : null;
                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Worker worker = new _Worker(id, nameN, nameR,nameD, positionId, drivingLicense, startDate, startOrderNumber);

                worker.setPositionN(getPositionNameN(positionId));
                worker.setPositionR(getPositionNameR(positionId));


                if(!valid) {
                    worker.setEndDate(endDate);
                    worker.setEndOrderNumber(endOrderNumber);
                    worker.setValid(valid);
                }
                workers.add(worker);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return workers;
    }

    public List<_Worker> getUnValidWorkers() {
        String sql = "SELECT * FROM workers WHERE valid = false";
        ObservableList<_Worker> workers = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nameN = resultSet.getString("nameN");
                String nameR = resultSet.getString("nameR");
                String nameD = resultSet.getString("nameD");
                int positionId = resultSet.getInt("positionId");
                String drivingLicense = resultSet.getString("drivingLicence");
                LocalDate startDate = resultSet.getDate("start-date").toLocalDate();
                String startOrderNumber = resultSet.getString("start-order-number");
                LocalDate endDate = (resultSet.getDate("end-date") != null) ? resultSet.getDate("end-date").toLocalDate() : null;
                String endOrderNumber = resultSet.getString("end-order-number");
                boolean valid = resultSet.getBoolean("valid");

                _Worker worker = new _Worker(id, nameN, nameR, nameD, positionId, drivingLicense, startDate, startOrderNumber);

                worker.setPositionN(getPositionNameN(positionId));
                worker.setPositionR(getPositionNameR(positionId));


                if(!valid) {
                    worker.setEndDate(endDate);
                    worker.setEndOrderNumber(endOrderNumber);
                    worker.setValid(valid);
                }
                workers.add(worker);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return workers;
    }

    public boolean addWorker(_Worker worker) {
        String sqlCheckExistence = "SELECT 1 FROM workers WHERE nameN = ? AND valid = 1";
        String sqlInsertWorker = "INSERT INTO workers (nameN, nameR, nameD, positionId, drivingLicence, `start-date`, `start-order-number`, valid) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection connection = Connect();
             PreparedStatement checkExistenceStmt = connection.prepareStatement(sqlCheckExistence);
             PreparedStatement insertWorkerStmt = connection.prepareStatement(sqlInsertWorker)) {

            checkExistenceStmt.setString(1, worker.getNameN());
            try (ResultSet rs = checkExistenceStmt.executeQuery()) {
                if (rs.next()) {
                    System.err.println("A valid worker with the same name already exists.");
                    return false;
                }
            }

            insertWorkerStmt.setString(1, worker.getNameN());
            insertWorkerStmt.setString(2, worker.getNameR());
            insertWorkerStmt.setString(3, worker.getNameD());
            insertWorkerStmt.setInt(4, worker.getPositionId());
            insertWorkerStmt.setString(5, worker.getDrivingLicense());
            insertWorkerStmt.setDate(6, java.sql.Date.valueOf(worker.getStartDate()));
            insertWorkerStmt.setString(7, worker.getStartOrderNumber());

            insertWorkerStmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Error adding worker: " + e.getMessage());
            return false;
        }
    }

    public boolean removeWorker(_Worker worker) {
        String sql = "UPDATE workers SET `end-date` = ?, `end-order-number` = ?, valid = FALSE WHERE `id` = ?";

        try (Connection connection = Connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, java.sql.Date.valueOf(worker.getEndDate()));
            preparedStatement.setString(2, worker.getEndOrderNumber());
            preparedStatement.setInt(3, worker.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error removing worker: " + e.getMessage());
            return false;
        }
    }

    public boolean changeWorker(_Worker worker) {
        String sql;
        if (worker.getEndDate() == null) {
            sql = "UPDATE `workers` SET" +
                    " `nameN` = '" + worker.getNameN() + "'," +
                    " `nameR` = '" + worker.getNameR() + "'," +
                    "`nameD` = '" + worker.getNameD() + "'," +
                    " `positionId` = '" + String.valueOf(worker.getPositionId()) + "'," +
                    " `drivingLicence` = '" + worker.getDrivingLicense() + "'," +
                    " `start-date` = '" + worker.getStartDate() + "'," +
                    " `start-order-number` = '" + worker.getStartOrderNumber() + "'," +
                    " `end-date` = NULL," +
                    " `end-order-number` = NULL," +
                    " `valid` = '1'" +
                    " WHERE `id` = '" + worker.getId() + "';";
        } else {
            sql = "UPDATE `workers` SET" +
                    " `nameN` = '" + worker.getNameN() + "'," +
                    " `nameR` = '" + worker.getNameR() + "'," +
                    "`nameD` = '" + worker.getNameD() + "'," +
                    " `positionId` = '" + String.valueOf(worker.getPositionId()) + "'," +
                    " `drivingLicence` = '" + worker.getDrivingLicense() + "'," +
                    " `start-date` = '" + worker.getStartDate() + "'," +
                    " `start-order-number` = '" + worker.getStartOrderNumber() + "'," +
                    " `end-date` = '" + worker.getEndDate() + "'," +
                    " `end-order-number` = '" + worker.getEndOrderNumber() + "'," +
                    " `valid` = '0'" +
                    " WHERE `id` = '" + worker.getId() + "';";
        }

        try (Connection connection = Connect();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            callableStatement.execute();
            return true;
        } catch (Exception e) {
            System.err.println("Error changing worker: " + e.getMessage());
            return false;
        }
    }

    public String getWorkerName(boolean isN, int workerId) {
        // Перевірка кешу
        int cacheKey = workerId * 10 + (isN ? 1 : 0); // Унікальний ключ для N/R
        if (workerNameCache.containsKey(cacheKey)) {
            return workerNameCache.get(cacheKey);
        }

        String sql = "SELECT * FROM workers WHERE `id` = ?";

        String name="";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, workerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                name = resultSet.getString(isN?"nameN":"nameR");
            }

        } catch (Exception e) {
            System.err.println("Error getting worker name: " + e.getMessage());
            e.printStackTrace();
        }

        // Зберігаємо в кеш
        workerNameCache.put(cacheKey, name);
        return name;
    }

    public String getWorkerNameD(int workerId) {
        String sql = "SELECT * FROM workers WHERE `id` = "+workerId+";";

        String name="";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                name = resultSet.getString("NameD");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public String getWorkerPosition(boolean isN, int workerId) {
        String positionName = "";
        String sql;

        if (isN) {
            sql = "SELECT positions.nameN AS PositionName " +
                    "FROM workers " +
                    "JOIN positions ON workers.positionId = positions.id " +
                    "WHERE workers.id = ?";
        } else {
            sql = "SELECT positions.nameR AS PositionName " +
                    "FROM workers " +
                    "JOIN positions ON workers.positionId = positions.id " +
                    "WHERE workers.id = ?";
        }

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, workerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    positionName = resultSet.getString("PositionName");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return positionName;
    }


    public List<_Position> getPositions() {
        String sql = "SELECT * FROM positions";
        ObservableList<_Position> positions = FXCollections.observableArrayList();

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nameN = resultSet.getString("nameN");
                String nameR = resultSet.getString("nameR");

                _Position pos = new _Position(id, nameN, nameR);

                positions.add(pos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return positions;
    }

    public boolean changePosition(_Position position) {
       String sql = "UPDATE `positions` SET" +
                    " `nameN` = '" + position.getNameN() + "'," +
                    " `nameR` = '" + position.getNameR() + "'" +
                    " WHERE `id` = '" + String.valueOf(position.getId()) + "';";

       try (Connection connection = Connect();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            callableStatement.execute();
            return true;
       } catch (Exception e) {
            System.err.println("Error changing position: " + e.getMessage());
            return false;
       }
    }

    public boolean addPosition(_Position position) {
        String checkSql = "SELECT 1 FROM positions WHERE nameN = ? OR nameR = ?";
        String insertSql = "INSERT INTO positions (nameN, nameR) VALUES (?, ?)";

        try (Connection connection = Connect();
             PreparedStatement checkStmt = connection.prepareStatement(checkSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

            checkStmt.setString(1, position.getNameN());
            checkStmt.setString(2, position.getNameR());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                System.err.println("Error adding position: Position with the same nameN or nameR already exists.");
                return false;
            }

            insertStmt.setString(1, position.getNameN());
            insertStmt.setString(2, position.getNameR());
            insertStmt.executeUpdate();

            return true;
        } catch (Exception e) {
            System.err.println("Error adding position: " + e.getMessage());
            return false;
        }
    }

    public boolean removePosition(_Position position) {
        String sql = "DELETE FROM positions WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, position.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error removing position: " + e.getMessage());
            return false;
        }
    }

    public String getPositionNameN(int positionId) {
        String sql = "SELECT * FROM positions WHERE `id` = "+positionId+";";

        String positionNameN="";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                positionNameN = resultSet.getString("nameN");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return positionNameN;
    }

    public String getPositionNameR(int positionId) {
        String sql = "SELECT * FROM positions WHERE `id` = "+positionId+";";

        String positionNameR="";
        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                positionNameR = resultSet.getString("nameR");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return positionNameR;
    }
}
