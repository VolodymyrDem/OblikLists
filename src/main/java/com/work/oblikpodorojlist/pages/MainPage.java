package com.work.oblikpodorojlist.pages;

import java.awt.Desktop;
import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.Documents.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.EventHandler;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MainPage {
    private Pane workspace;
    private HBox navigationBar;
    private int windowCount = 0;
    private Map<String, StackPane> openWindows = new HashMap<>();
    private static final int RESIZE_MARGIN = 5;
    private StackPane maximizedWindow = null;
    private DBManager dbManager;
    private ObservableList<_Car> cars;
    private ObservableList<_Report> reports;
    private ObservableList<_Order> orders;
    private ObservableList<_Worker> workers;
    private ObservableList<_Position> positions;
    private ObservableList<_List> lists;
    private ObservableList<_Order> filteredOrders;
    private ObservableList<FuelUsage> FilteredFuelUsage = FXCollections.observableArrayList();
    private ObservableList<_List> FilteredLists = FXCollections.observableArrayList();
    private PeriodParameters parametersOrders = new PeriodParameters();
    private PeriodParameters parametersLists = new PeriodParameters();
    private PeriodParameters parametersFuelUsage = new PeriodParameters();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DocumentsManager dM = new DocumentsManager();
    List<String> numbersG;
    List<String> numbersGL;
    private static final String LIGHT_THEME = "/css/styleWhite.css";
    private static final String DARK_THEME = "/css/styleDark.css";

    private DatePicker datePickerStartG = new DatePicker();
    private DatePicker datePickerEndG = new DatePicker();
    private DatePicker datePickerStartGL = new DatePicker();
    private DatePicker datePickerEndGL = new DatePicker();
    private DatePicker datePickerStartOR = new DatePicker();
    private DatePicker datePickerEndOR = new DatePicker();

    private void switchTheme(Scene scene, String theme) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
    }

    private boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }

    public void StartMainPage(DBManager _dbManager, Stage primaryStage) {

        dbManager = _dbManager;
        dbManager.deleteOldBackups();
        dM.createFolders(dbManager.getCompany());
        VBox root = new VBox();
        Scene scene = new Scene(root, 500, 400);

        String _companyName = _dbManager.getCompany();
        String _username = dbManager.getUsername();

        MenuBar menuBar = new MenuBar();
        switchTheme(scene, LIGHT_THEME);

        Menu menuFile = new Menu("Файл");
        MenuItem closeApp = new MenuItem("Вийти з програми");
        Menu menuTheme = new Menu("Тема");
        MenuItem changeUser = new MenuItem("Завершити сеанс");

        ToggleGroup themeGroup = new ToggleGroup();
        RadioMenuItem lightTheme = new RadioMenuItem("Світла");
        RadioMenuItem darkTheme = new RadioMenuItem("Темна");

        lightTheme.setToggleGroup(themeGroup);
        darkTheme.setToggleGroup(themeGroup);
        lightTheme.setSelected(true); // За замовчуванням світла тема

        menuTheme.getItems().addAll(lightTheme, darkTheme);
        menuFile.getItems().addAll(menuTheme, changeUser, closeApp);
        menuBar.getMenus().add(menuFile);

        changeUser.setOnAction(event -> {
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {}});
            Alert al= Alerts.ConfirmAlert("Створити резервну копію?","Підвердіть створення резервної копії");
            al.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    dbManager.createBackup();
                    dbManager.deleteOldBackups();
                }
            });
            CompanyPage cp = new CompanyPage();
            cp.start(primaryStage);
        });

        closeApp.setOnAction(e->{
            primaryStage.close();
        });

        lightTheme.setOnAction(event -> switchTheme(scene, LIGHT_THEME));
        darkTheme.setOnAction(event -> switchTheme(scene, DARK_THEME));

        Menu handbookMenu = new Menu("Довідники");
        MenuItem carsHandbook = new MenuItem("Довідник автомобілів");
        carsHandbook.setOnAction(e -> openСarsHandbookWindow());
        MenuItem workersHandbook = new MenuItem("Довідник працівників");
        workersHandbook.setOnAction(e -> openWorkersHandbookWindow());
        MenuItem positionsHandbook = new MenuItem("Довідник посад");
        positionsHandbook.setOnAction(e -> openPositionsHandbookWindow());
        handbookMenu.getItems().addAll(carsHandbook,workersHandbook,positionsHandbook);

        Menu JournalMenu = new Menu("Журнали");
        MenuItem orderJournal = new MenuItem("Журнал наказів на відрядженння");
        orderJournal.setOnAction(e -> openOrderJournal());
        MenuItem listJournal = new MenuItem("Журнал подорожніх листів");
        listJournal.setOnAction(e -> openListJournal());
        MenuItem reportJournal = new MenuItem("Журнал звітів");
        reportJournal.setOnAction(e -> openReportsHandbookWindow());
        JournalMenu.getItems().addAll(orderJournal, listJournal, reportJournal);

        Menu RegisterMenu = new Menu("Реєстри");
        MenuItem OrderRegister = new MenuItem("Реєстр наказів на відрядження");
        MenuItem FuelRegister = new MenuItem("Реєстр використання палива");
        MenuItem ListRegister = new MenuItem("Реєстр подорожніх листів");

        OrderRegister.setOnAction(e -> openOrdersRegister());
        FuelRegister.setOnAction(e -> openFuelRegister());
        ListRegister.setOnAction(e -> openListRegister());
        RegisterMenu.getItems().addAll(OrderRegister, FuelRegister, ListRegister);

        menuBar.getMenus().addAll(handbookMenu, JournalMenu, RegisterMenu);

        if(dbManager.getUsername().equals("root")) {
            Menu adminTools = new Menu("Адміністрування");

            MenuItem createCompany = new MenuItem("Додати компанію");
            createCompany.setOnAction(e->{
                openAddCompanyWindow();
            });

            MenuItem editCompany = new MenuItem("Редагувати компанію");
            editCompany.setOnAction(e->{
                openEditCompanyWindow( primaryStage);
            });

            MenuItem editUser = new MenuItem("Користувачі");
            editUser.setOnAction(e->{
                openUsersWindow();
            });

            MenuItem deleteCompany = new MenuItem("Видалити компанію");
            deleteCompany.setOnAction(e->{
                Alert a = Alerts.ConfirmAlert("Підтеврдіть операцію", "Видалити компанію");
                a.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dbManager.deleteCompany(_companyName);
                        CompanyPage cp = new CompanyPage();
                        cp.start(primaryStage);
                    }
                });

            });

            MenuItem updateGuestUser = new MenuItem("Оновити гостьового користувача");
            updateGuestUser.setOnAction(e->{
                dbManager.CreateGuestUser();
            });

            MenuItem createBackup = new MenuItem("Створити резервну копію");
            createBackup.setOnAction(e->{
                dbManager.createBackup();
                dbManager.deleteOldBackups();
            });

            MenuItem loadBackup = new MenuItem("Завантажити резервну копію");
            loadBackup.setOnAction(e->{
                dbManager.loadBackup();
            });

            MenuItem changeHost = new MenuItem("Редагувати адресу бази даних");
            changeHost.setOnAction(e->{
                openChangeHostWindow();
            });
            adminTools.getItems().addAll(createCompany, editCompany, editUser, deleteCompany, createBackup,loadBackup, updateGuestUser, changeHost);
            menuBar.getMenus().add(adminTools);
        }

        workspace = new Pane();
        workspace.getStyleClass().add("pane");

        navigationBar = new HBox(2);
        navigationBar.getStyleClass().add("navigation-bar");
        navigationBar.setPrefHeight(30);

        root.getChildren().addAll(menuBar, workspace, navigationBar);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> resizeWorkspace(primaryStage));
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> resizeWorkspace(primaryStage));

        primaryStage.setScene(scene);
        primaryStage.setTitle(dbManager.getCompanyInfo().getName() + ": " + _username);
        primaryStage.show();
        resizeWorkspace(primaryStage);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                // Тут можна виконати необхідні дії перед закриттям вікна
                Alert al= Alerts.ConfirmAlert("Створити резервну копію?","Підвердіть створення резервної копії");
                al.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dbManager.createBackup();
                        dbManager.deleteOldBackups();
                    }
                });
                // Можна відмінити закриття вікна, якщо це потрібно
                // event.consume();  // Закоментуйте, якщо не хочете скасувати закриття
            }
        });

    }

    private void resizeWorkspace(Stage stage) {
        workspace.setPrefWidth(stage.getWidth());
        workspace.setPrefHeight(stage.getHeight() - navigationBar.getHeight() - 25); // Adjust for menu bar
    }

    private void openChangeHostWindow() {
        String windowTitle = "Редагувати адресу бази даних";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField();

            grid.add(new Label("ІП вдреса:"), 0, 0);
            grid.add(nameField, 1, 0);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати хост");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.setHost(nameField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    private void openAddUserWindow() {
        String windowTitle = "Додати користувача";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField();
            TextField passwordField = new TextField();

            grid.add(new Label("ПІБ:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Пароль:"), 0, 1);
            grid.add(passwordField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати користувача");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.createUser(nameField.getText(), passwordField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    private void  openAddCompanyWindow() {
        String windowTitle = "Додати компанію";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField();
            TextField addressField = new TextField();
            TextField codeField = new TextField();
            TextField ceoField = new TextField();
            TextField accountantField = new TextField();
            TextField typeFullField = new TextField();
            TextField typeShortField = new TextField();

            grid.add(new Label("Назва:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Адреса:"), 0, 1);
            grid.add(addressField, 1, 1);
            grid.add(new Label("Код ЄРДПОУ:"), 0, 2);
            grid.add(codeField, 1, 2);
            grid.add(new Label("Директор:"), 0, 3);
            grid.add(ceoField, 1, 3);
            grid.add(new Label("Головний бухгалтер:"), 0, 4);
            grid.add(accountantField, 1, 4);
            grid.add(new Label("Тип компанії(повністю):"), 0, 5);
            grid.add(typeFullField, 1, 5);
            grid.add(new Label("Тип компанії(скорочено):"), 0, 6);
            grid.add(typeShortField, 1, 6);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText()) || isEmptyOrWhitespace(addressField.getText()) || isEmptyOrWhitespace(codeField.getText()) || isEmptyOrWhitespace(ceoField.getText()) ||
                        isEmptyOrWhitespace(accountantField.getText()) || isEmptyOrWhitespace(typeFullField.getText()) || isEmptyOrWhitespace(typeShortField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати компанію");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                //todo: add password
                                dbManager.createCompany(nameField.getText(),addressField.getText(), codeField.getText(), ceoField.getText(), accountantField.getText(), typeFullField.getText(), typeShortField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    private void openEditCompanyWindow( Stage primaryStage) {
        String windowTitle = "Редагувати компанію";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            _Company comm = dbManager.getCompanyInfo();


            TextField nameField = new TextField(comm.getName());
            TextField addressField = new TextField(comm.getAddress());
            TextField codeField = new TextField(String.valueOf(comm.getCode()));
            codeField.setDisable(true);
            TextField ceoField = new TextField(comm.getCeo());
            TextField accountantField = new TextField(comm.getAccountant());
            TextField typeFullField = new TextField(comm.getTypeFull());
            TextField typeShortField = new TextField(comm.getTypeShort());

            grid.add(new Label("Назва:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Адреса:"), 0, 1);
            grid.add(addressField, 1, 1);
            grid.add(new Label("Код ЄРДПОУ:"), 0, 2);
            grid.add(codeField, 1, 2);
            grid.add(new Label("Директор:"), 0, 3);
            grid.add(ceoField, 1, 3);
            grid.add(new Label("Головний бухгалтер:"), 0, 4);
            grid.add(accountantField, 1, 4);
            grid.add(new Label("Тип компанії(повністю):"), 0, 5);
            grid.add(typeFullField, 1, 5);
            grid.add(new Label("Тип компанії(скорочено):"), 0, 6);
            grid.add(typeShortField, 1, 6);

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText()) || isEmptyOrWhitespace(addressField.getText()) || isEmptyOrWhitespace(ceoField.getText()) ||
                        isEmptyOrWhitespace(accountantField.getText()) || isEmptyOrWhitespace(typeFullField.getText()) || isEmptyOrWhitespace(typeShortField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати компанію");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.changeParametersCompany(nameField.getText(),addressField.getText(), Integer.parseInt(codeField.getText()), ceoField.getText(), accountantField.getText(), typeFullField.getText(), typeShortField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
                StartMainPage(dbManager, primaryStage);
            });
        }
    }

    private void openUsersWindow() {
        String windowTitle = "Користувачі";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            Button addButton = new Button("Додати користувача");
            addButton.setGraphic(IconsManager.getPlusIcon());


            Button editPasswordButton = new Button("Редагувати пароль");
            editPasswordButton.setGraphic(IconsManager.getPencilIcon());
            editPasswordButton.setDisable(true);


            Button removeButton = new Button("Видалити користувача");
            removeButton.setGraphic(IconsManager.getRubbishIcon());
            removeButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());


            addButton.getStyleClass().add("green-button");
            editPasswordButton.getStyleClass().add("yellow-button");
            removeButton.getStyleClass().add("green-button");


            ListView<String> tableView = new ListView<>();
            tableView.getItems().addAll(dbManager.getUsers());


            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                editPasswordButton.setDisable(newSelection == null);
                removeButton.setDisable(newSelection == null );
            });

            addButton.setOnAction(e -> {
                openAddUserWindow();
                tableView.getItems().clear();
                tableView.getItems().addAll(dbManager.getUsers());
            });

            updateButton.setOnAction(e->{
                tableView.getItems().clear();
                tableView.getItems().addAll(dbManager.getUsers());
            });

            editPasswordButton.setOnAction(e -> {
                String selectedUser = tableView.getSelectionModel().getSelectedItem();
                if (selectedUser != null ) {
                    openEditPasswordWindow(selectedUser);
                    tableView.getItems().clear();
                    tableView.getItems().addAll(dbManager.getUsers());
                }
            });

            removeButton.setOnAction(e -> {
                String selectedUser = tableView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити користувача");
                    confirmationAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            dbManager.deleteUser(selectedUser);
                            tableView.getItems().clear();
                            tableView.getItems().addAll(dbManager.getUsers());
                        }
                    });
                }
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editPasswordButton, removeButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private void openEditPasswordWindow(String SelectedUser) {
        String windowTitle = "Редагувати пароль користувача";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);
            TextField nameField = new TextField(SelectedUser);
            nameField.setDisable(true);
            TextField passworField = new TextField();

            grid.add(new Label("ПІБ:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Пароль:"), 0, 1);
            grid.add(passworField, 1, 1);

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                    try {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Змінити пароль користувача");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteUser(SelectedUser);
                                dbManager.createUser(SelectedUser, passworField.getText());

                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
            });


        }
    }

    //----------------------------------

    private void openOrdersRegister() {
        String windowTitle = "Реєстр: накази на відрядження";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            List<String> validWorkers = dbManager.getUniqueWorkersNames();

            //List<_Car> validCars = dbManager.getCars();
            //Map<String, Integer> carsT = new HashMap<>();

            datePickerStartOR.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });


            datePickerEndOR.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            //ComboBox<String> carField = new ComboBox<>();
            ComboBox<String> workerField = new ComboBox<>();
            //Label carLabel = new Label("авто:");
            Label workerLabel = new Label("Працівник:");
            Label timeLabel = new Label("Період: з ");
            Label timeLabel2 = new Label("по");
            Button filterButton = new Button("Застосувати фільтр");
            filterButton.setGraphic(IconsManager.getFilterIcon());
            Button saveButton = new Button("Зберегти реєстр");
            saveButton.setGraphic(IconsManager.getTikIcon());
            saveButton.setDisable(true);

            Button settingsButton = new Button();
            settingsButton.setGraphic(IconsManager.getClockIcon());


            Button updateButton = new Button();
            updateButton.setDisable(true);
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());
            updateButton.setOnAction(e->{
                if(datePickerEndOR.getValue() == null) {
                    datePickerEndOR.setValue(LocalDate.now());
                }
                filteredOrders.clear();
                String tempWorkerName = (workerField.getValue() == null)?"-1":workerField.getValue();
                filteredOrders.addAll(dbManager.getOrdersFiltered(tempWorkerName, datePickerStartOR.getValue(), datePickerEndOR.getValue()));
                filteredOrders.sort(Comparator.comparing(_Order::getOrderDate));
            });

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[5] + "\\");
            });


            settingsButton.setOnAction(event -> {
                updateButton.setDisable(true);
                openOrderPeriodParameters();
            });

            //carLabel.getStyleClass().add("filter-label");
            workerLabel.getStyleClass().add("filter-label");
            timeLabel.getStyleClass().add("filter-label");
            timeLabel2.getStyleClass().add("filter-label");
            filterButton.getStyleClass().add("green-button");
            saveButton.getStyleClass().add("green-button");

            filterButton.setOnAction(e -> {
                if (datePickerStartOR.getValue() == null) {
                    updateButton.setDisable(true);
                    Alerts.ErrorAlert("Помилка вводу", "Введіть дату початку").showAndWait();
                } else {
                    updateButton.setDisable(false);
                    if(datePickerEndOR.getValue() == null) {
                        datePickerEndOR.setValue(LocalDate.now());
                    }
                    filteredOrders.clear();
                    String tempWorkerName = (workerField.getValue() == null)?"-1":workerField.getValue();
                    filteredOrders.addAll(dbManager.getOrdersFiltered(tempWorkerName, datePickerStartOR.getValue(), datePickerEndOR.getValue()));
                    filteredOrders.sort(Comparator.comparing(_Order::getOrderDate));
                    saveButton.setDisable(false);
                }

            });

            saveButton.setOnAction(e -> {
                dM.createRegisterOrders(dbManager, filteredOrders, workerField.getValue(), datePickerStartOR.getValue(), datePickerEndOR.getValue());
            });

            for(String d  : validWorkers) {
                workerField.getItems().add(d);
            }
            workerField.getItems().add(null);
            workerField.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerStartOR.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerEndOR.setOnAction(e->{
                updateButton.setDisable(true);
            });
        /*

        for(_Car d  : validCars) {
            carsT.put(d.getNumber() + " " +d.getModel(), d.getIdCar());
            carField.getItems().add(d.getNumber() + " " +d.getModel());
        }
        */

            HBox buttonBox = new HBox(10, timeLabel, datePickerStartOR, timeLabel2, datePickerEndOR, settingsButton,  workerLabel, workerField, filterButton, updateButton, saveButton, openFolderButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            filteredOrders = FXCollections.observableArrayList(dbManager.getOrders());

            TableView<_Order> tableView = new TableView<>();
            tableView.setItems(filteredOrders);

            TableColumn<_Order, LocalDate> orderDateCol = new TableColumn<>("Дата наказу");
            orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

            orderDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Order, String> orderNumberCol = new TableColumn<>("№ наказу");
            orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            TableColumn<_Order, String> workerCol = new TableColumn<>("ПІБ працівник");
            workerCol.setCellValueFactory(cellData -> {
                String Name = dbManager.getWorkerName(true, cellData.getValue().getIdWorker());
                return new SimpleStringProperty(Name);
            });

            TableColumn<_Order, LocalDate> startDateCol = new TableColumn<>("Виїзд: дата");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Order, LocalDate> endDateCol = new TableColumn<>("Повернення: дата");
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

            endDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Order, String> routeCol = new TableColumn<>("Маршрут");
            routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));

            TableColumn<_Order, String> goalCol = new TableColumn<>("Мета");
            goalCol.setCellValueFactory(new PropertyValueFactory<>("goal"));


            tableView.getColumns().addAll(orderDateCol, orderNumberCol, workerCol,startDateCol,
                    endDateCol, routeCol, goalCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            StackPane internalWindow = openInternalWindow(table, windowTitle);

           internalWindow.setOnMouseEntered(event -> {
               //todo: add update of parameters
           });
        }
    }

    private void openListRegister() {
        String windowTitle = "Реєстр: подорожні листи";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            List<String> validCars = dbManager.getUniqueCarsNumbers();

            datePickerStartGL.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            datePickerEndGL.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            CheckComboBox<String> carField = new CheckComboBox<>();

            Label carLabel = new Label("Авто:");
            Label timeLabel = new Label("Період: з ");
            Label timeLabel2 = new Label("по");
            Button filterButton = new Button("Застосувати фільтр");
            filterButton.setGraphic(IconsManager.getFilterIcon());
            Button saveButton = new Button("Зберегти реєстр");
            saveButton.setGraphic(IconsManager.getTikIcon());
            saveButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[7] + "\\");
            });


            Button settingsButton = new Button();

            Button updateButton = new Button();
            updateButton.setDisable(true);
            updateButton.getStyleClass().add("grey-button");

            carField.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
                if (change.next()) {
                    updateButton.setDisable(true);
                }
            });

            updateButton.setGraphic(IconsManager.getUpdateIcon());

            updateButton.setOnAction(e->{

                if(datePickerEndGL.getValue() == null) {
                    datePickerEndGL.setValue(LocalDate.now());
                }
                if(!FilteredLists.isEmpty()) FilteredLists.clear();

                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                    numbersG = validCars.stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                            .collect(Collectors.toList());
                } else {
                    numbersG = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                            .collect(Collectors.toList());
                }

                FilteredLists.addAll(dbManager.getListsFiltered(numbersG, parametersLists));
            });

            settingsButton.setGraphic(IconsManager.getClockIcon());

            settingsButton.setOnAction(event -> {
                updateButton.setDisable(true);
                openListPeriodParameters();
            });

            carLabel.getStyleClass().add("filter-label");
            timeLabel.getStyleClass().add("filter-label");
            timeLabel2.getStyleClass().add("filter-label");
            filterButton.getStyleClass().add("green-button");
            saveButton.getStyleClass().add("green-button");

            carField.getItems().addAll(validCars);

            filterButton.setOnAction(e -> {
                if (datePickerStartGL.getValue() == null) {
                    updateButton.setDisable(true);
                    Alerts.ErrorAlert("Помилка вводу", "Введіть усі дані").showAndWait();
                } else {
                    updateButton.setDisable(false);
                    if(datePickerEndGL.getValue() == null) {
                        datePickerEndGL.setValue(LocalDate.now());
                    }
                    if(!FilteredLists.isEmpty()) FilteredLists.clear();


                    if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                        numbersG = validCars.stream()
                                .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                                .collect(Collectors.toList());
                    } else {
                        numbersG = carField.getCheckModel().getCheckedItems().stream()
                                .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                                .collect(Collectors.toList());
                    }
                    parametersLists.setStartDate(datePickerStartGL.getValue());
                    parametersLists.setEndDate(datePickerEndGL.getValue());

                    FilteredLists.addAll(dbManager.getListsFiltered(numbersG, parametersLists));
                    saveButton.setDisable(false);
                }

            });

            saveButton.setOnAction(e -> {
                dM.createRegisterLists(dbManager, numbersG, FilteredLists, datePickerStartGL.getValue(), datePickerEndGL.getValue());
            });

            HBox buttonBox = new HBox(10, timeLabel, datePickerStartGL, timeLabel2, datePickerEndGL,settingsButton, carLabel, carField, filterButton, updateButton, saveButton, openFolderButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            datePickerStartGL.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerEndGL.setOnAction(e->{
                updateButton.setDisable(true);
            });

            TableView<_List> tableView = new TableView<>();
            tableView.setItems(FilteredLists);

            TableColumn<_List, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_List, String> listNumberCol = new TableColumn<>("№ листа");
            listNumberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

            TableColumn<_List, String> workerCol = new TableColumn<>("ПІБ працівник");
            workerCol.setCellValueFactory(cellData -> {
                String Name = dbManager.getWorkerName(true, cellData.getValue().getIdWorker());
                return new SimpleStringProperty(Name);
            });

            TableColumn<_List, LocalDate> startDateCol = new TableColumn<>("Виїзд: дата");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_List, LocalDate> endDateCol = new TableColumn<>("Поверення: дата");
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            endDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_List, String> CarNumberCol = new TableColumn<>("Номер авто");
            CarNumberCol.setCellValueFactory(cellData -> {
                int orderNumber = cellData.getValue().getIdCar();
                return new SimpleStringProperty(dbManager.getCarNumber(orderNumber));
            });

            TableColumn<_List, String> orderCol = new TableColumn<>("№ наказу");
            orderCol.setCellValueFactory(cellData -> {
                int idOrder = cellData.getValue().getIdOrder();
                if (idOrder == -1) {
                    return new SimpleStringProperty("по місту");
                }
                return new SimpleStringProperty(dbManager.getOrderNumber(idOrder));
            });

            TableColumn<_List, String> routeCol = new TableColumn<>("Маршрут");
            routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));

            TableColumn<_List, String> goalCol = new TableColumn<>("Мета");
            goalCol.setCellValueFactory(new PropertyValueFactory<>("goal"));

            TableColumn<_List, String> startMCol = new TableColumn<>("Виїзд: пробіг");
            startMCol.setCellValueFactory(new PropertyValueFactory<>("startMileage"));

            TableColumn<_List, String> startFCol = new TableColumn<>("Виїзд: паливо ");
            startFCol.setCellValueFactory(new PropertyValueFactory<>("startFuel"));

            TableColumn<_List, String> endMCol = new TableColumn<>("Повернення: пробіг");
            endMCol.setCellValueFactory(cellData -> {
                if(cellData.getValue().isDone()) {
                    return new SimpleStringProperty(String.valueOf(cellData.getValue().getEndMileage()));
                }
                else {
                    return new SimpleStringProperty("");
                }
            });

            TableColumn<_List, String> endFCol = new TableColumn<>("Повернення: паливо");
            endFCol.setCellValueFactory(cellData -> {
                if(cellData.getValue().isDone()) {
                    return new SimpleStringProperty(String.valueOf(cellData.getValue().getEndFuel()));
                }
                else {
                    return new SimpleStringProperty("");
                }
            });

            TableColumn<_List, String> reFCol = new TableColumn<>("Заправка");
            reFCol.setCellValueFactory(cellData -> {
                if(cellData.getValue().isDone()) {
                    return new SimpleStringProperty(String.valueOf(cellData.getValue().getRefuel()));
                }
                else {
                    return new SimpleStringProperty("");
                }
            });

            tableView.getColumns().addAll( listNumberCol, orderCol, workerCol, CarNumberCol,startDateCol, startMCol, startFCol,
                    endDateCol, endMCol, endFCol, reFCol, routeCol, goalCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private void openOrderPeriodParameters() {

        String windowTitle = "Реєстр наказів: Параметри періоду";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            ToggleGroup group = new ToggleGroup();

            int currentYear = Year.now().getValue();
            int currentMonth = LocalDate.now().getMonthValue();
            CheckBox fromStartOfYear = new CheckBox("З початку року");
            CheckBox fromStartOfQuarter = new CheckBox("З початку кварталу");
            CheckBox fromStartOfMonth = new CheckBox("З початку місяця");

            Spinner<Integer> yearSpinner = new Spinner<>();
            yearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(currentYear-1000, currentYear+1000, currentYear));
            yearSpinner.setEditable(true);

            Spinner<String> quarterYearSpinner = new Spinner<>();
            QuarterYearSpinnerValueFactory valueFactory = new QuarterYearSpinnerValueFactory(1, 4, currentYear);
            quarterYearSpinner.setValueFactory(valueFactory);
            quarterYearSpinner.setEditable(false);

            Spinner<String> monthYearSpinner = new Spinner<>();
            MonthYearSpinnerValueFactory valueFactoryMY = new MonthYearSpinnerValueFactory(currentMonth, currentYear);
            monthYearSpinner.setValueFactory(valueFactoryMY);
            monthYearSpinner.setEditable(false);

            DatePicker datePickerDay = new DatePicker(LocalDate.now());
            datePickerDay.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            DatePicker datePickerStart = new DatePicker(LocalDate.now());

            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });


            DatePicker datePickerEnd = new DatePicker(LocalDate.now());

            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            RadioButton year = new RadioButton("Рік");
            RadioButton quarter = new RadioButton("Квартал");
            RadioButton month = new RadioButton("Місяць");
            RadioButton day = new RadioButton("День");
            RadioButton custom = new RadioButton("Інтервал з");
            Label po = new Label("по");
            year.setToggleGroup(group);
            quarter.setToggleGroup(group);
            month.setToggleGroup(group);
            day.setToggleGroup(group);
            custom.setToggleGroup(group);



            group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                // Оновлюємо доступність елементів в залежності від вибору
                if (year.isSelected()) {
                    yearSpinner.setDisable(false);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (quarter.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(false);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(false);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (month.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(false);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(false);
                    fromStartOfMonth.setDisable(true);
                } else if (day.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(false);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(false);
                } else if (custom.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(false);
                    datePickerEnd.setDisable(false);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                }
            });


            grid.add(year, 0, 0);
            grid.add(yearSpinner, 1, 0);
            grid.add(quarter, 0, 1);
            grid.add(quarterYearSpinner, 1, 1);
            grid.add(fromStartOfYear, 2, 1);
            grid.add(month, 0, 2);
            grid.add(monthYearSpinner, 1, 2);
            grid.add(fromStartOfQuarter, 2, 2);
            grid.add(day, 0, 3);
            grid.add(datePickerDay, 1, 3);
            grid.add(fromStartOfMonth, 2, 3);
            grid.add(custom, 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(po, 0, 5);
            grid.add(datePickerEnd, 1, 5);


            Button saveButton = new Button("Зберегти");



            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(event -> {

                LocalDate StartDate = LocalDate.now();
                LocalDate EndDate = LocalDate.now();
                Period period = Period.between(StartDate, EndDate);
                boolean fromStart = false;

                if (year.isSelected()) {
                    yearSpinner.getValue();
                    StartDate = LocalDate.of(yearSpinner.getValue(), 1, 1);
                    EndDate = LocalDate.of(yearSpinner.getValue(), 12, 31);
                } else if (quarter.isSelected()) {
                    int initialQuarter = valueFactory.getQuarter();
                    int initialYear = valueFactory.getYear();
                    if (fromStartOfYear.isSelected()) {
                        StartDate = LocalDate.of(initialYear, 1, 1);  // Початок року
                    } else {
                        StartDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear);  // Початок кварталу
                    }
                    EndDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear).plusMonths(3).minusDays(1);  // Кінець кварталу

                    fromStart = fromStartOfYear.isSelected();

                } else if (month.isSelected()) {
                    StartDate = valueFactoryMY.getStartDate();
                    EndDate = valueFactoryMY.getEndDate();
                    fromStart = fromStartOfQuarter.isSelected();

                    if (fromStartOfQuarter.isSelected()) {
                        int monthOfYear = StartDate.getMonthValue();
                        int quarterI = (monthOfYear - 1) / 3 + 1;  // Визначення кварталу (1 - січень-березень, 2 - квітень-червень тощо)
                        StartDate = valueFactoryMY.getStartDate();  // Початок місяця
                        LocalDate quarterStart = valueFactory.getQuarterStartDate(quarterI, StartDate.getYear()); // Початок кварталу
                        StartDate = quarterStart;
                    }

                } else if (day.isSelected()) {
                    StartDate = datePickerDay.getValue();
                    if (fromStartOfMonth.isSelected()) {
                        StartDate = LocalDate.of(StartDate.getYear(), StartDate.getMonth(), 1);  // Початок місяця
                    }
                    EndDate = StartDate;

                } else if (custom.isSelected()) {
                    StartDate = datePickerStart.getValue();
                    EndDate = datePickerEnd.getValue();
                }
                datePickerStartOR.setValue(StartDate);
                datePickerEndOR.setValue(EndDate);

                parametersOrders.setStartDate(StartDate);
                parametersOrders.setEndDate(EndDate);

                workspace.getChildren().remove(internalWindow);
                openWindows.remove(windowTitle);
                updateNavigationBar();

            });
        }
    }

    private void openListPeriodParameters() {

        String windowTitle = "Реєстр листів: Параметри періоду";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            ToggleGroup group = new ToggleGroup();

            int currentYear = Year.now().getValue();
            int currentMonth = LocalDate.now().getMonthValue();
            CheckBox fromStartOfYear = new CheckBox("З початку року");
            CheckBox fromStartOfQuarter = new CheckBox("З початку кварталу");
            CheckBox fromStartOfMonth = new CheckBox("З початку місяця");

            Spinner<Integer> yearSpinner = new Spinner<>();
            yearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(currentYear-1000, currentYear+1000, currentYear));
            yearSpinner.setEditable(true);

            Spinner<String> quarterYearSpinner = new Spinner<>();
            QuarterYearSpinnerValueFactory valueFactory = new QuarterYearSpinnerValueFactory(1, 4, currentYear);
            quarterYearSpinner.setValueFactory(valueFactory);
            quarterYearSpinner.setEditable(false);

            Spinner<String> monthYearSpinner = new Spinner<>();
            MonthYearSpinnerValueFactory valueFactoryMY = new MonthYearSpinnerValueFactory(currentMonth, currentYear);
            monthYearSpinner.setValueFactory(valueFactoryMY);
            monthYearSpinner.setEditable(false);

            DatePicker datePickerDay = new DatePicker(LocalDate.now());
            datePickerDay.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            DatePicker datePickerStart = new DatePicker(LocalDate.now());

            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });


            DatePicker datePickerEnd = new DatePicker(LocalDate.now());

            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            RadioButton year = new RadioButton("Рік");
            RadioButton quarter = new RadioButton("Квартал");
            RadioButton month = new RadioButton("Місяць");
            RadioButton day = new RadioButton("День");
            RadioButton custom = new RadioButton("Інтервал з");
            Label po = new Label("по");
            year.setToggleGroup(group);
            quarter.setToggleGroup(group);
            month.setToggleGroup(group);
            day.setToggleGroup(group);
            custom.setToggleGroup(group);



            group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                // Оновлюємо доступність елементів в залежності від вибору
                if (year.isSelected()) {
                    yearSpinner.setDisable(false);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (quarter.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(false);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(false);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (month.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(false);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(false);
                    fromStartOfMonth.setDisable(true);
                } else if (day.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(false);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(false);
                } else if (custom.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(false);
                    datePickerEnd.setDisable(false);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                }
            });


            grid.add(year, 0, 0);
            grid.add(yearSpinner, 1, 0);
            grid.add(quarter, 0, 1);
            grid.add(quarterYearSpinner, 1, 1);
            grid.add(fromStartOfYear, 2, 1);
            grid.add(month, 0, 2);
            grid.add(monthYearSpinner, 1, 2);
            grid.add(fromStartOfQuarter, 2, 2);
            grid.add(day, 0, 3);
            grid.add(datePickerDay, 1, 3);
            grid.add(fromStartOfMonth, 2, 3);
            grid.add(custom, 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(po, 0, 5);
            grid.add(datePickerEnd, 1, 5);


            Button saveButton = new Button("Зберегти");



            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(event -> {

                LocalDate StartDate = LocalDate.now();
                LocalDate EndDate = LocalDate.now();
                Period period = Period.between(StartDate, EndDate);
                boolean fromStart = false;

                if (year.isSelected()) {
                    yearSpinner.getValue();
                    StartDate = LocalDate.of(yearSpinner.getValue(), 1, 1);
                    EndDate = LocalDate.of(yearSpinner.getValue(), 12, 31);
                } else if (quarter.isSelected()) {
                    int initialQuarter = valueFactory.getQuarter();
                    int initialYear = valueFactory.getYear();
                    if (fromStartOfYear.isSelected()) {
                        StartDate = LocalDate.of(initialYear, 1, 1);  // Початок року
                    } else {
                        StartDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear);  // Початок кварталу
                    }
                    EndDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear).plusMonths(3).minusDays(1);  // Кінець кварталу

                    fromStart = fromStartOfYear.isSelected();

                } else if (month.isSelected()) {
                    StartDate = valueFactoryMY.getStartDate();
                    EndDate = valueFactoryMY.getEndDate();
                    fromStart = fromStartOfQuarter.isSelected();

                    if (fromStartOfQuarter.isSelected()) {
                        int monthOfYear = StartDate.getMonthValue();
                        int quarterI = (monthOfYear - 1) / 3 + 1;  // Визначення кварталу (1 - січень-березень, 2 - квітень-червень тощо)
                        StartDate = valueFactoryMY.getStartDate();  // Початок місяця
                        LocalDate quarterStart = valueFactory.getQuarterStartDate(quarterI, StartDate.getYear()); // Початок кварталу
                        StartDate = quarterStart;
                    }

                } else if (day.isSelected()) {
                    StartDate = datePickerDay.getValue();
                    if (fromStartOfMonth.isSelected()) {
                        StartDate = LocalDate.of(StartDate.getYear(), StartDate.getMonth(), 1);  // Початок місяця
                    }
                    EndDate = StartDate;

                } else if (custom.isSelected()) {
                    StartDate = datePickerStart.getValue();
                    EndDate = datePickerEnd.getValue();
                }

                parametersLists.setStartDate(StartDate);
                parametersLists.setEndDate(EndDate);

                datePickerStartGL.setValue(StartDate);
                datePickerEndGL.setValue(EndDate);

                workspace.getChildren().remove(internalWindow);
                openWindows.remove(windowTitle);
                updateNavigationBar();
            });

        }
    }

    private void openFuelPeriodParameters() {

        String windowTitle = "Реєстр палива: Параметри періоду";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            ToggleGroup group = new ToggleGroup();

            int currentYear = Year.now().getValue();
            int currentMonth = LocalDate.now().getMonthValue();
            CheckBox fromStartOfYear = new CheckBox("З початку року");
            CheckBox fromStartOfQuarter = new CheckBox("З початку кварталу");
            CheckBox fromStartOfMonth = new CheckBox("З початку місяця");

            Spinner<Integer> yearSpinner = new Spinner<>();
            yearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(currentYear-1000, currentYear+1000, currentYear));
            yearSpinner.setEditable(true);

            Spinner<String> quarterYearSpinner = new Spinner<>();
            QuarterYearSpinnerValueFactory valueFactory = new QuarterYearSpinnerValueFactory(1, 4, currentYear);
            quarterYearSpinner.setValueFactory(valueFactory);
            quarterYearSpinner.setEditable(false);

            Spinner<String> monthYearSpinner = new Spinner<>();
            MonthYearSpinnerValueFactory valueFactoryMY = new MonthYearSpinnerValueFactory(currentMonth, currentYear);
            monthYearSpinner.setValueFactory(valueFactoryMY);
            monthYearSpinner.setEditable(false);

            DatePicker datePickerDay = new DatePicker(LocalDate.now());
            datePickerDay.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            DatePicker datePickerStart = new DatePicker(LocalDate.now());

            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });


            DatePicker datePickerEnd = new DatePicker(LocalDate.now());

            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            RadioButton year = new RadioButton("Рік");
            RadioButton quarter = new RadioButton("Квартал");
            RadioButton month = new RadioButton("Місяць");
            RadioButton day = new RadioButton("День");
            RadioButton custom = new RadioButton("Інтервал з");
            Label po = new Label("по");
            year.setToggleGroup(group);
            quarter.setToggleGroup(group);
            month.setToggleGroup(group);
            day.setToggleGroup(group);
            custom.setToggleGroup(group);



            group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                // Оновлюємо доступність елементів в залежності від вибору
                if (year.isSelected()) {
                    yearSpinner.setDisable(false);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (quarter.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(false);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(false);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (month.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(false);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(false);
                    fromStartOfMonth.setDisable(true);
                } else if (day.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(false);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(false);
                } else if (custom.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(false);
                    datePickerEnd.setDisable(false);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                }
            });


            grid.add(year, 0, 0);
            grid.add(yearSpinner, 1, 0);
            grid.add(quarter, 0, 1);
            grid.add(quarterYearSpinner, 1, 1);
            grid.add(fromStartOfYear, 2, 1);
            grid.add(month, 0, 2);
            grid.add(monthYearSpinner, 1, 2);
            grid.add(fromStartOfQuarter, 2, 2);
            grid.add(day, 0, 3);
            grid.add(datePickerDay, 1, 3);
            grid.add(fromStartOfMonth, 2, 3);
            grid.add(custom, 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(po, 0, 5);
            grid.add(datePickerEnd, 1, 5);


            Button saveButton = new Button("Зберегти");



            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(event -> {

                LocalDate StartDate = LocalDate.now();
                LocalDate EndDate = LocalDate.now();
                Period period = Period.between(StartDate, EndDate);
                boolean fromStart = false;

                if (year.isSelected()) {
                    yearSpinner.getValue();
                    StartDate = LocalDate.of(yearSpinner.getValue(), 1, 1);
                    EndDate = LocalDate.of(yearSpinner.getValue(), 12, 31);
                } else if (quarter.isSelected()) {
                    int initialQuarter = valueFactory.getQuarter();
                    int initialYear = valueFactory.getYear();
                    if (fromStartOfYear.isSelected()) {
                        StartDate = LocalDate.of(initialYear, 1, 1);  // Початок року
                    } else {
                        StartDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear);  // Початок кварталу
                    }
                    EndDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear).plusMonths(3).minusDays(1);  // Кінець кварталу

                    fromStart = fromStartOfYear.isSelected();

                } else if (month.isSelected()) {
                    StartDate = valueFactoryMY.getStartDate();
                    EndDate = valueFactoryMY.getEndDate();
                    fromStart = fromStartOfQuarter.isSelected();

                    if (fromStartOfQuarter.isSelected()) {
                        int monthOfYear = StartDate.getMonthValue();
                        int quarterI = (monthOfYear - 1) / 3 + 1;  // Визначення кварталу (1 - січень-березень, 2 - квітень-червень тощо)
                        StartDate = valueFactoryMY.getStartDate();  // Початок місяця
                        LocalDate quarterStart = valueFactory.getQuarterStartDate(quarterI, StartDate.getYear()); // Початок кварталу
                        StartDate = quarterStart;
                    }

                } else if (day.isSelected()) {
                    StartDate = datePickerDay.getValue();
                    if (fromStartOfMonth.isSelected()) {
                        StartDate = LocalDate.of(StartDate.getYear(), StartDate.getMonth(), 1);  // Початок місяця
                    }
                    EndDate = StartDate;

                } else if (custom.isSelected()) {
                    StartDate = datePickerStart.getValue();
                    EndDate = datePickerEnd.getValue();
                }
                datePickerStartG.setValue(StartDate);
                datePickerEndG.setValue(EndDate);

                parametersFuelUsage.setStartDate(StartDate);
                parametersFuelUsage.setEndDate(EndDate);

                workspace.getChildren().remove(internalWindow);
                openWindows.remove(windowTitle);
                updateNavigationBar();


            });

        }
    }

    private static class MonthYearSpinnerValueFactory extends SpinnerValueFactory<String> {
        private int month;
        private int year;

        public MonthYearSpinnerValueFactory(int initialMonth, int initialYear) {
            this.month = initialMonth;
            this.year = initialYear;
            setValue(formatValue());
        }

        @Override
        public void decrement(int steps) {
            for (int i = 0; i < steps; i++) {
                month--;
                if (month < 1) {
                    month = 12;
                    year--;
                }
            }
            setValue(formatValue());
        }

        @Override
        public void increment(int steps) {
            for (int i = 0; i < steps; i++) {
                month++;
                if (month > 12) {
                    month = 1;
                    year++;
                }
            }
            setValue(formatValue());
        }

        private String formatValue() {
            return getMonthName(month) + " " + year;
        }

        public String getFormattedDate() {
            return formatValue();
        }

        /**
         * Повертає початкову дату у форматі LocalDate.
         */
        public LocalDate getStartDate() {
            return LocalDate.of(year, month, 1);  // Перший день місяця
        }

        /**
         * Повертає кінцеву дату у форматі LocalDate.
         */
        public LocalDate getEndDate() {
            int lastDay = YearMonth.of(year, month).lengthOfMonth();  // Отримуємо останній день місяця
            return LocalDate.of(year, month, lastDay);  // Останній день місяця
        }

        /**
         * Отримує назву місяця за його номером.
         */
        private String getMonthName(int month) {
            return Month.of(month).toString().toLowerCase(); // Назви місяців англійською
        }
    }
    private static class QuarterYearSpinnerValueFactory extends SpinnerValueFactory<String> {
        private int quarter;
        private int year;
        private final int minQuarter;
        private final int maxQuarter;

        // Відповідність кварталів місяцям
        private static final Map<Integer, int[]> QUARTER_MONTHS = new HashMap<>();
        static {
            QUARTER_MONTHS.put(1, new int[]{1, 3});  // Січень - Березень
            QUARTER_MONTHS.put(2, new int[]{4, 6});  // Квітень - Червень
            QUARTER_MONTHS.put(3, new int[]{7, 9});  // Липень - Вересень
            QUARTER_MONTHS.put(4, new int[]{10, 12}); // Жовтень - Грудень
        }

        public QuarterYearSpinnerValueFactory(int minQuarter, int maxQuarter, int initialYear) {
            this.minQuarter = minQuarter;
            this.maxQuarter = maxQuarter;
            this.quarter = minQuarter;
            this.year = initialYear;
            setValue(formatValue());
        }

        @Override
        public void decrement(int steps) {
            for (int i = 0; i < steps; i++) {
                quarter--;
                if (quarter < minQuarter) {
                    quarter = maxQuarter;
                    year--;
                }
            }
            setValue(formatValue());
        }

        @Override
        public void increment(int steps) {
            for (int i = 0; i < steps; i++) {
                quarter++;
                if (quarter > maxQuarter) {
                    quarter = minQuarter;
                    year++;
                }
            }
            setValue(formatValue());
        }

        private String formatValue() {
            return quarter + " квартал " + year;
        }

        public int getQuarter() {
            return quarter;
        }

        public int getYear() {
            return year;
        }

        /**
         * Отримує початкову дату кварталу.
         */
        public LocalDate getQuarterStartDate(int quarter, int year) {
            int startMonth = QUARTER_MONTHS.get(quarter)[0];
            return LocalDate.of(year, startMonth, 1);
        }

        /**
         * Отримує кінцеву дату кварталу.
         */
        public LocalDate getQuarterEndDate(int quarter, int year) {
            int endMonth = QUARTER_MONTHS.get(quarter)[1];
            int lastDay = LocalDate.of(year, endMonth, 1).lengthOfMonth();
            return LocalDate.of(year, endMonth, lastDay);
        }
    }


    private void openFuelRegister() {
        String windowTitle = "Реєстр: використання палива";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            List<String> validCars = dbManager.getUniqueCarsNumbers();

            datePickerStartG.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            datePickerEndG.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            CheckComboBox<String> carField = new CheckComboBox<>();

            Label carLabel = new Label("Авто:");
            Label timeLabel = new Label("Період: з ");
            Label timeLabel2 = new Label("по");
            Label podilLabel = new Label("Групувати");
            Button filterButton = new Button("Застосувати фільтр");
            filterButton.setGraphic(IconsManager.getFilterIcon());
            Button saveButton = new Button("Зберегти реєстр");
            saveButton.setGraphic(IconsManager.getTikIcon());
            saveButton.setDisable(true);
            ComboBox<String> podilField = new ComboBox<>();
            podilField.getItems().addAll("По днях", "По тижнях", "По місяцях", "По кварталах", "По роках");
            podilField.setValue("По днях");

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[6] + "\\");
            });


            Button settingsButton = new Button();


            Button updateButton = new Button();
            updateButton.setDisable(true);
            updateButton.getStyleClass().add("grey-button");

            carField.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
                if (change.next()) {
                    updateButton.setDisable(true);
                }
            });

            updateButton.setGraphic(IconsManager.getUpdateIcon());
            updateButton.setOnAction(e->{
                if(datePickerEndG.getValue() == null) {
                    datePickerEndG.setValue(LocalDate.now());
                }
                if(!FilteredFuelUsage.isEmpty()) FilteredFuelUsage.clear();


                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                    numbersG = validCars.stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                            .collect(Collectors.toList());
                } else {
                    numbersG = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                            .collect(Collectors.toList());
                }
                Period podil = switch (podilField.getValue()) {
                    case "По днях" -> Period.ofDays(1);
                    case "По тижнях" -> Period.ofWeeks(1);
                    case "По місяцях" -> Period.ofMonths(1);
                    case "По кварталах" -> Period.ofMonths(3);
                    case "По роках" -> Period.ofYears(1);
                    default -> Period.ofDays(1);
                };
                parametersFuelUsage.setPeriod(podil);

                FilteredFuelUsage.addAll(dbManager.getListsFuelFiltered(numbersG, parametersFuelUsage));
            });



            settingsButton.setGraphic(IconsManager.getClockIcon());
            settingsButton.setOnAction(event -> {
                updateButton.setDisable(true);
                openFuelPeriodParameters();
            });

            carLabel.getStyleClass().add("filter-label");
            podilLabel.getStyleClass().add("filter-label");
            timeLabel.getStyleClass().add("filter-label");
            timeLabel2.getStyleClass().add("filter-label");
            filterButton.getStyleClass().add("green-button");
            saveButton.getStyleClass().add("green-button");

            for(String d  : validCars) {
                carField.getItems().add(d);
            }

            filterButton.setOnAction(e -> {
                if (datePickerStartG.getValue() == null) {
                    updateButton.setDisable(true);
                    Alerts.ErrorAlert("Помилка вводу", "Введіть усі дані").showAndWait();
                } else {
                    updateButton.setDisable(false);
                    if(datePickerEndG.getValue() == null) {
                        datePickerEndG.setValue(LocalDate.now());
                    }
                    if(!FilteredFuelUsage.isEmpty()) FilteredFuelUsage.clear();


                    if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                        numbersG = validCars.stream()
                                .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                                .collect(Collectors.toList());
                    } else {
                        numbersG = carField.getCheckModel().getCheckedItems().stream()
                                .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                                .collect(Collectors.toList());
                    }
                    Period podil = Period.ofDays(1);
                    if(podilField.getValue() != null) {
                        podil = switch (podilField.getValue()) {
                            case "По днях" -> Period.ofDays(1);
                            case "По тижнях" -> Period.ofWeeks(1);
                            case "По місяцях" -> Period.ofMonths(1);
                            case "По кварталах" -> Period.ofMonths(3);
                            case "По роках" -> Period.ofYears(1);
                            default -> Period.ofDays(1);
                        };
                    }
                    parametersFuelUsage.setPeriod(podil);
                    parametersFuelUsage.setEndDate(datePickerEndG.getValue());
                    parametersFuelUsage.setStartDate(datePickerStartG.getValue());

                    FilteredFuelUsage.addAll(dbManager.getListsFuelFiltered(numbersG, parametersFuelUsage));
                    saveButton.setDisable(false);
                }

            });

            saveButton.setOnAction(e -> {
                dM.createRegisterFuel(dbManager, numbersG, FilteredFuelUsage, datePickerStartG.getValue(), datePickerEndG.getValue(), parametersFuelUsage.getPeriod());
            });

            HBox buttonBox = new HBox(10, timeLabel, datePickerStartG, timeLabel2, datePickerEndG,settingsButton, carLabel, carField, podilLabel, podilField, filterButton, updateButton, saveButton, openFolderButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            datePickerStartG.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerEndG.setOnAction(e->{
                updateButton.setDisable(true);
            });
            podilField.setOnAction(e->{
                updateButton.setDisable(true);
            });




            TableView<FuelUsage> tableView = new TableView<>();
            tableView.setItems(FilteredFuelUsage);


            TableColumn<FuelUsage, LocalDate> startDateCol = new TableColumn<>("Дата початку");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<FuelUsage, LocalDate> endDateCol = new TableColumn<>("Дата кінця");
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

            endDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });
            DecimalFormat df = new DecimalFormat("#.##"); // 2 знаки після коми

            TableColumn<FuelUsage, String> carNumbercol = new TableColumn<>("Номер авто");
            carNumbercol.setCellValueFactory(new PropertyValueFactory<>("carNumber"));

            TableColumn<FuelUsage, String> mileageCol = new TableColumn<>("Пробіг за період");
            mileageCol.setCellValueFactory(new PropertyValueFactory<>("mileage"));

            TableColumn<FuelUsage, Double> fuelFactCol = new TableColumn<>("Фактичне використання палива");
            fuelFactCol.setCellValueFactory(new PropertyValueFactory<>("fuelFact"));

            fuelFactCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });
            TableColumn<FuelUsage, Double> fuelNormCol = new TableColumn<>("Норма використання палива");
            fuelNormCol.setCellValueFactory(new PropertyValueFactory<>("fuelNorm"));

            fuelNormCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });

            TableColumn<FuelUsage, Double> overUseCol = new TableColumn<>("Перевикористання палива");
            overUseCol.setCellValueFactory(new PropertyValueFactory<>("overUse"));

            overUseCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });

            TableColumn<FuelUsage, Double> underUseCol = new TableColumn<>("Економія палива");
            underUseCol.setCellValueFactory(new PropertyValueFactory<>("underUse"));

            underUseCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });



            tableView.getColumns().addAll(startDateCol, endDateCol, carNumbercol, mileageCol, fuelFactCol, fuelNormCol, overUseCol, underUseCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    //----------------------------------

    private void openListJournal() {
        String windowTitle = "Журнал: подорожні листи";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            Button addButton = new Button("Додати лист");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> openAddListWindow());

            CheckComboBox<String> carField = new CheckComboBox<>();

            List<String> validCars = dbManager.getUniqueCarsNumbers();

            for(String d  : validCars) {
                carField.getItems().add(d);
            }

            Button editButton = new Button("Редагувати лист");
            editButton.setGraphic(IconsManager.getPencilIcon());

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[4] + "\\");
            });



            updateButton.setOnAction(e->{
                if(!lists.isEmpty())  lists.clear();

                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                    numbersGL = validCars.stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                            .collect(Collectors.toList());
                } else {
                    numbersGL = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                            .collect(Collectors.toList());
                }
                lists.addAll(dbManager.getListsForCars(numbersGL));

            });


            editButton.setDisable(true);

            Button removeButton = new Button("Закрити лист");
            removeButton.setGraphic(IconsManager.getTikIcon());
            removeButton.setDisable(true);

            Button deleteButton = new Button("Позначити лист на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);

            deleteButton.getStyleClass().add("red-button");
            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");
            removeButton.getStyleClass().add("green-button");
            Button openFileButton = new Button("Перегляд листа");
            openFileButton.setGraphic(IconsManager.getFileIcon());
            openFileButton.getStyleClass().add("grey-button");
            openFileButton.setDisable(true);

            lists = FXCollections.observableArrayList(dbManager.getLists());

            TableView<_List> tableView = new TableView<>();
            tableView.setItems(lists);

            TableColumn<_List, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_List, String> listNumberCol = new TableColumn<>("№ листа");
            listNumberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

            TableColumn<_List, String> workerCol = new TableColumn<>("ПІБ працівник");
            workerCol.setCellValueFactory(cellData -> {
                String Name = dbManager.getWorkerName(true, cellData.getValue().getIdWorker());
                return new SimpleStringProperty(Name);
            });

            TableColumn<_List, LocalDate> startDateCol = new TableColumn<>("Виїзд: дата");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_List, LocalDate> endDateCol = new TableColumn<>("Поверення: дата");
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            endDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_List, String> CarNumberCol = new TableColumn<>("Номер авто");
            CarNumberCol.setCellValueFactory(cellData -> {
                int orderNumber = cellData.getValue().getIdCar();
                return new SimpleStringProperty(dbManager.getCarNumber(orderNumber));
            });

            TableColumn<_List, String> orderCol = new TableColumn<>("№ наказу");
            orderCol.setCellValueFactory(cellData -> {
                int idOrder = cellData.getValue().getIdOrder();
                if (idOrder == -1) {
                    return new SimpleStringProperty("по місту");
                }
                return new SimpleStringProperty(dbManager.getOrderNumber(idOrder));
            });

            TableColumn<_List, String> routeCol = new TableColumn<>("маршрут");
            routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));

            TableColumn<_List, String> goalCol = new TableColumn<>("мета");
            goalCol.setCellValueFactory(new PropertyValueFactory<>("goal"));

            TableColumn<_List, String> startMCol = new TableColumn<>("Виїзд: пробіг");
            startMCol.setCellValueFactory(new PropertyValueFactory<>("startMileage"));

            TableColumn<_List, String> startFCol = new TableColumn<>("Виїзд: паливо ");
            startFCol.setCellValueFactory(new PropertyValueFactory<>("startFuel"));

            TableColumn<_List, String> endMCol = new TableColumn<>("Повернення: пробіг");
            endMCol.setCellValueFactory(cellData -> {
                if(cellData.getValue().isDone()) {
                    return new SimpleStringProperty(String.valueOf(cellData.getValue().getEndMileage()));
                }
                else {
                    return new SimpleStringProperty("");
                }
            });

            TableColumn<_List, String> endFCol = new TableColumn<>("Повернення: паливо");
            endFCol.setCellValueFactory(cellData -> {
                if(cellData.getValue().isDone()) {
                    return new SimpleStringProperty(String.valueOf(cellData.getValue().getEndFuel()));
                }
                else {
                    return new SimpleStringProperty("");
                }
            });

            TableColumn<_List, String> reFCol = new TableColumn<>("Заправка");
            reFCol.setCellValueFactory(cellData -> {
                if(cellData.getValue().isDone()) {
                    return new SimpleStringProperty(String.valueOf(cellData.getValue().getRefuel()));
                }
                else {
                    return new SimpleStringProperty("");
                }
            });

            TableColumn<_List, String> validCol = new TableColumn<>("Актуальність");
            validCol.setCellValueFactory(cellData -> {
                boolean valid = cellData.getValue().isDone();
                return new SimpleStringProperty(valid ? "Виконаний" : "Невиконаний");
            });


            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                editButton.setDisable(newSelection == null || (newSelection.isDone() && !dbManager.getUsername().equals("root")));
                removeButton.setDisable(newSelection == null || newSelection.isDone());
                openFileButton.setDisable(newSelection == null);
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));
            });

            editButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null && ( !selectedList.isDone() || dbManager.getUsername().equals("root")) ) {
                    openEditListWindow(selectedList);
                }
            });

            removeButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null && !selectedList.isDone()) {
                    openCloseListWindow(selectedList);
                }
            });

            openFileButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null) {
                    dM.createList(dbManager, selectedList);
                }
            });

            HBox buttonBox = new HBox(10, updateButton, addButton, editButton, new Label("Фільтрувати по авто:"), carField, removeButton, openFileButton, openFolderButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll( listNumberCol, orderCol, workerCol, CarNumberCol,startDateCol, startMCol, startFCol,
                    endDateCol, endMCol, endFCol, reFCol, routeCol, goalCol, validCol);

            tableView.getSortOrder().setAll(listNumberCol);
            listNumberCol.setSortType(TableColumn.SortType.ASCENDING);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            tableView.setRowFactory(tv -> {
                TableRow<_List> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null && ( !selectedList.isDone() || dbManager.getUsername().equals("root"))) {
                        openEditListWindow(selectedList);
                    }
                });

                MenuItem removeItem = new MenuItem("Закрити");
                removeItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null && !selectedList.isDone()) {
                        openCloseListWindow(selectedList);
                    }
                });


                MenuItem openItem = new MenuItem("Переглянути лист");
                openItem.setGraphic(IconsManager.getFileIcon());
                openItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null) {
                        DocumentsManager dM = new DocumentsManager();
                        dM.createList(dbManager, selectedList);
                    }
                });

                // Double-click event handler
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        _List selectedList = row.getItem();
                        if (selectedList != null) {
                            DocumentsManager dM = new DocumentsManager();
                            dM.createList(dbManager, selectedList);
                        }
                    }
                });

                // Оновлення контекстного меню залежно від значення valid
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    rowMenu.getItems().clear(); // Очищуємо попередні пункти
                    if (newItem != null) {
                        rowMenu.getItems().add(openItem);
                        if(!newItem.isDone()) {
                            rowMenu.getItems().add(editItem);
                            rowMenu.getItems().add(removeItem);
                        }
                    }
                });

                // Прив'язка контекстного меню до рядка лише, коли він не порожній
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));

                return row;
            });
            tableView.scrollTo(tableView.getItems().size() - 1);

            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити лист");
                deleteButton.setOnAction(e->{
                    _List selectedList = tableView.getSelectionModel().getSelectedItem();
                    if (selectedList != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteList(selectedList);
                                if(!lists.isEmpty())  lists.clear();

                                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                                    numbersGL = validCars.stream()
                                            .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                                            .collect(Collectors.toList());
                                } else {
                                    numbersGL = carField.getCheckModel().getCheckedItems().stream()
                                            .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                                            .collect(Collectors.toList());
                                }
                                lists.addAll(dbManager.getListsForCars(numbersGL));
                            }
                        });

                    }
                });
            }


            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private List<_Order> getValidOrders() {
        List<_Order> validOrders = dbManager.getFreeOrders();
        return validOrders;
    }
    private List<_Worker> getValidWorkers() {
        List<_Worker> validWorkers = dbManager.getFreeWorkers();
        return validWorkers;
    }
    private List<_Car> getValidCars() {
        List<_Car> validWorkers = dbManager.getFreeCars();
        return validWorkers;
    }

    private void openAddListWindow() {
        String windowTitle = "Додати подорожній лист";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            List<_Worker> validWorkers = dbManager.getValidWorkers();
            Map<String, Integer> workersT = new HashMap<>();

            List<_Car> validCars = dbManager.getFreeCars();
            Map<String, Integer> carsT = new HashMap<>();

            Map<String, Integer> ordersT = new HashMap<>();

            DatePicker datePickerStart = new DatePicker();
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            DatePicker datePickerEnd = new DatePicker();
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField routeField = new TextField();
            TextField goalField = new TextField();
            ComboBox<String> carField = new ComboBox<>();
            ComboBox<String> worker = new ComboBox<>();
            TextField positionField = new TextField();
            ComboBox<String> ListType = new ComboBox<>();
            ComboBox<String> order = new ComboBox<>();

            for(_Worker d  : validWorkers) {
                workersT.put(d.getNameN(), d.getId());
                worker.getItems().add(d.getNameN());
            }
            positionField.setDisable(true);


            for(_Car d  : validCars) {
                carsT.put(d.getNumber() + " " +d.getModel(), d.getIdCar());
                carField.getItems().add(d.getNumber() + " " +d.getModel());
            }


            ListType.getItems().add("по місту");
            ListType.getItems().add("за наказом");



            ListType.setOnAction(e -> {
                String selectedOption = ListType.getValue();
                if (Objects.equals(selectedOption, "за наказом")) {
                    ordersT.clear();
                    order.getItems().clear();
                    List<_Order> orders = getValidOrders();
                    for(_Order d  : orders) {
                        ordersT.put(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()), d.getIdOrder());
                        order.getItems().add(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()));
                    }
                    order.setDisable(false);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    routeField.setDisable(true);
                    goalField.setDisable(true);
                    worker.setDisable(true);
                }
                else {
                    workersT.clear();
                    worker.getItems().clear();
                    List<_Worker> workers = dbManager.getValidWorkers();
                    for(_Worker d  : workers) {
                        workersT.put(d.getNameN(), d.getId());
                        worker.getItems().add(d.getNameN());
                    }
                    order.setDisable(true);
                    order.getItems().clear();
                    datePickerStart.setDisable(false);
                    datePickerEnd.setDisable(false);
                    routeField.setDisable(false);
                    goalField.setDisable(false);
                    worker.setDisable(false);
                }
            });

            worker.setOnAction(e -> {
                String selectedWorker = worker.getValue();
                if (selectedWorker != null) {
                    positionField.setText(dbManager.getWorkerPosition(true, workersT.get(selectedWorker)));
                }
            });

            order.setOnAction(e -> {
                String selectedOrder = order.getValue();
                if (selectedOrder != null) {
                    datePickerStart.setValue(dbManager.getStartOrderDate(ordersT.get(selectedOrder)));
                    datePickerEnd.setValue(dbManager.getEndOrderDate(ordersT.get(selectedOrder)));
                    routeField.setText(dbManager.getOrderRoute(ordersT.get(selectedOrder)));
                    goalField.setText(dbManager.getOrderGoal(ordersT.get(selectedOrder)));
                    worker.getItems().clear();
                    workersT.clear();
                    workersT.put(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)), dbManager.getOrderIdWorker(ordersT.get(selectedOrder)) );
                    worker.getItems().add(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)));
                    worker.setValue(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)));
                }
            });

            grid.add(new Label("Авто:"), 0, 0);
            grid.add(carField, 1, 0);
            grid.add(new Label("Тип листа:"), 0, 1);
            grid.add(ListType, 1, 1);
            grid.add(new Label("Наказ:"), 0, 2);
            grid.add(order, 1, 2);
            grid.add(new Label("Виїзд: дата:"), 0, 3);
            grid.add(datePickerStart, 1, 3);
            grid.add(new Label("Поверення: дата:"), 0, 4);
            grid.add(datePickerEnd, 1, 4);
            grid.add(new Label("Маршрут:"), 0, 5);
            grid.add(routeField, 1, 5);
            grid.add(new Label("Мета:"), 0, 6);
            grid.add(goalField, 1, 6);
            grid.add(new Label("Працівник:"), 0, 7);
            grid.add(worker, 1, 7);
            grid.add(new Label("Посада:"), 0, 8);
            grid.add(positionField, 1, 8);

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( carField.getValue() == null || ListType.getValue() == null || (ListType.getValue() == "за наказом" && order.getValue() == null) ||
                        datePickerStart.getValue() == null || datePickerEnd.getValue() == null || isEmptyOrWhitespace(routeField.getText()) ||
                        isEmptyOrWhitespace(goalField.getText()) || worker.getValue() == null ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _List newList = new _List(
                                carsT.get(carField.getValue()),
                                (Objects.equals(ListType.getValue(), "за наказом"))?ordersT.get(order.getValue()): -1,
                                workersT.get(worker.getValue()),
                                datePickerStart.getValue(),
                                datePickerEnd.getValue(),
                                routeField.getText(),
                                goalField.getText()
                        );

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.addList(newList)) {
                                    lists.clear();
                                    lists.addAll(dbManager.getLists());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();

                                }
                            }
                        });


                        carsT.clear();
                        carField.getItems().clear();
                        List<_Car> cars = getValidCars();
                        for(_Car d  : cars) {
                            carsT.put(d.getNumber() + " " +d.getModel(), d.getIdCar());
                            carField.getItems().add(d.getNumber() + " " +d.getModel());
                        }

                        workersT.clear();
                        worker.getItems().clear();
                        List<_Worker> workers = getValidWorkers();
                        for(_Worker d  : workers) {
                            workersT.put(d.getNameN(), d.getId());
                            worker.getItems().add(d.getNameN());
                        }

                        ordersT.clear();
                        order.getItems().clear();
                        List<_Order> orders = getValidOrders();
                        for(_Order d  : orders) {
                            ordersT.put(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()), d.getIdOrder());
                            order.getItems().add(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()));
                        }

                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });


        }
    }

    private void openEditListWindow(_List selectedList) {
        String windowTitle = "Редагувати подорожній лист";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            List<_Worker> validWorkers = dbManager.getFreeWorkers();
            Map<String, Integer> workersT = new HashMap<>();

            List<_Car> validCars = dbManager.getFreeCars();
            validCars.add(dbManager.getCar(selectedList.getIdCar()));
            Map<String, Integer> carsT = new HashMap<>();

            Map<String, Integer> ordersT = new HashMap<>();


            DatePicker datePickerStart = new DatePicker(selectedList.getStartDate());
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            DatePicker datePickerEnd = new DatePicker(selectedList.getEndDate());
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField routeField = new TextField(selectedList.getRoute());
            TextField goalField = new TextField(selectedList.getGoal());
            ComboBox<String> carField = new ComboBox<>();
            ComboBox<String> worker = new ComboBox<>();
            TextField positionField = new TextField();
            ComboBox<String> ListType = new ComboBox<>();
            ComboBox<String> order = new ComboBox<>();
            TextField endMileageField = new TextField(String.valueOf(selectedList.getEndMileage()));
            TextField endFuelField = new TextField(String.valueOf(selectedList.getEndFuel()));
            TextField refuelField = new TextField(String.valueOf(selectedList.getRefuel()));
            TextField startFuelField = new TextField(String.valueOf(selectedList.getStartFuel()));
            TextField startMileageField = new TextField(String.valueOf(selectedList.getStartMileage()));
            TextField numberField = new TextField(String.valueOf(selectedList.getNumber()));

            startFuelField.setDisable(!dbManager.getUsername().equals("root"));
            endFuelField.setDisable(!dbManager.getUsername().equals("root"));
            startMileageField.setDisable(!dbManager.getUsername().equals("root"));
            refuelField.setDisable(!dbManager.getUsername().equals("root"));
            endMileageField.setDisable(!dbManager.getUsername().equals("root"));

            ordersT.clear();
            order.getItems().clear();
            List<_Order> ordersL = getValidOrders();
            for(_Order d  : ordersL) {
                ordersT.put(d.getOrderNumber() + " - "+ dbManager.getWorkerName(true, d.getIdWorker()), d.getIdOrder());
                order.getItems().add(d.getOrderNumber() + " - "+ dbManager.getWorkerName(true, d.getIdWorker()));
            }


            for(_Worker d  : validWorkers) {
                workersT.put(d.getNameN(), d.getId());
                worker.getItems().add(d.getNameN());
            }
            workersT.put(dbManager.getWorker(selectedList.getIdWorker()).getNameN(), selectedList.getIdWorker());
            worker.getItems().add(dbManager.getWorker(selectedList.getIdWorker()).getNameN());
            positionField.setDisable(true);
            worker.setValue(dbManager.getWorkerName(true, selectedList.getIdWorker()));
            positionField.setText(dbManager.getWorkerPosition(true, selectedList.getIdWorker()));

            for(_Car d  : validCars) {
                carsT.put(d.getNumber() + " " +d.getModel(), d.getIdCar());
                carField.getItems().add(d.getNumber() + " " +d.getModel());
            }
            carsT.put(dbManager.getCar(selectedList.getIdCar()).getNumber() + " " + dbManager.getCar(selectedList.getIdCar()).getModel(), selectedList.getIdCar());
            carField.getItems().add(dbManager.getCar(selectedList.getIdCar()).getNumber() + " " + dbManager.getCar(selectedList.getIdCar()).getModel());
            carField.setValue(dbManager.getCar(selectedList.getIdCar()).getNumber() + " " + dbManager.getCar(selectedList.getIdCar()).getModel());

            ListType.getItems().add("по місту");
            ListType.getItems().add("за наказом");
            if(selectedList.getIdOrder() != -1) {
                order.setValue(dbManager.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbManager.getWorkerName(true, selectedList.getIdWorker()));
            }

            if (selectedList.getIdOrder() != -1) {
                ListType.setValue("за наказом");
                order.setDisable(false);
                datePickerStart.setDisable(true);
                datePickerEnd.setDisable(true);
                routeField.setDisable(true);
                goalField.setDisable(true);
                worker.setDisable(true);
                ordersT.put(dbManager.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbManager.getWorkerName(true, selectedList.getIdWorker()), selectedList.getIdOrder());
                order.getItems().add(dbManager.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbManager.getWorkerName(true, selectedList.getIdWorker()));
            } else {
                ListType.setValue("\"по місту\"");
                order.setDisable(true);
                order.getItems().clear();
                datePickerStart.setDisable(false);
                datePickerEnd.setDisable(false);
                routeField.setDisable(false);
                goalField.setDisable(false);
                worker.setDisable(false);
            }


            ListType.setOnAction(e -> {
                String selectedOption = ListType.getValue();
                if (Objects.equals(selectedOption, "за наказом")) {
                    ordersT.clear();
                    order.getItems().clear();
                    List<_Order> orders = getValidOrders();
                    for(_Order d  : orders) {
                        ordersT.put(d.getOrderNumber() + " - "+ dbManager.getWorkerName(true, d.getIdWorker()), d.getIdOrder());
                        order.getItems().add(d.getOrderNumber() + " - "+ dbManager.getWorkerName(true, d.getIdWorker()));
                    }
                    if(selectedList.getIdOrder() != -1) {
                        ordersT.put(dbManager.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbManager.getWorkerName(true, selectedList.getIdWorker()), selectedList.getIdOrder());
                        order.getItems().add(dbManager.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbManager.getWorkerName(true, selectedList.getIdWorker()));
                    }
                    order.setDisable(false);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    routeField.setDisable(true);
                    goalField.setDisable(true);
                    worker.setDisable(true);
                }
                else {
                    workersT.clear();
                    worker.getItems().clear();
                    List<_Worker> workers = getValidWorkers();
                    for(_Worker d  : workers) {
                        workersT.put(d.getNameN(), d.getId());
                        worker.getItems().add(d.getNameN());
                    }
                    if(selectedList.getIdOrder() == -1) {
                        workersT.put(dbManager.getWorker(selectedList.getIdWorker()).getNameN(), selectedList.getIdWorker());
                        worker.getItems().add(dbManager.getWorker(selectedList.getIdWorker()).getNameN());
                    }

                    order.setDisable(true);
                    order.getItems().clear();
                    datePickerStart.setDisable(false);
                    datePickerEnd.setDisable(false);
                    routeField.setDisable(false);
                    goalField.setDisable(false);
                    worker.setDisable(false);
                }
            });

            worker.setOnAction(e -> {
                String selectedWorker = worker.getValue();
                if (selectedWorker != null) {
                    positionField.setText(dbManager.getWorkerPosition(true, workersT.get(selectedWorker)));
                }
            });

            order.setOnAction(e -> {
                String selectedOrder = order.getValue();
                if (selectedOrder != null) {
                    datePickerStart.setValue(dbManager.getStartOrderDate(ordersT.get(selectedOrder)));
                    datePickerEnd.setValue(dbManager.getEndOrderDate(ordersT.get(selectedOrder)));
                    routeField.setText(dbManager.getOrderRoute(ordersT.get(selectedOrder)));
                    goalField.setText(dbManager.getOrderGoal(ordersT.get(selectedOrder)));
                    worker.getItems().clear();
                    workersT.clear();
                    workersT.put(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)), dbManager.getOrderIdWorker(ordersT.get(selectedOrder)) );
                    worker.getItems().add(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)));
                    worker.setValue(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)));
                }
            });
            int i = 0;
            if(dbManager.getUsername().equals("root")){
                grid.add(new Label("№ листа"), 0, 0);
                grid.add(numberField, 1, 0);
                i++;
            }

            grid.add(new Label("Авто:"), 0, 0+i);
            grid.add(carField, 1, 0+i);
            grid.add(new Label("Тип листа:"), 0, 1+i);
            grid.add(ListType, 1, 1+i);
            grid.add(new Label("Наказ:"), 0, 2+i);
            grid.add(order, 1, 2+i);
            grid.add(new Label("Виїзд: дата:"), 0, 3+i);
            grid.add(datePickerStart, 1, 3+i);
            grid.add(new Label("Поверення: дата:"), 0, 4+i);
            grid.add(datePickerEnd, 1, 4+i);
            grid.add(new Label("Маршрут:"), 0, 5+i);
            grid.add(routeField, 1, 5+i);
            grid.add(new Label("Мета:"), 0, 6+i);
            grid.add(goalField, 1, 6+i);
            grid.add(new Label("Працівник:"), 0, 7+i);
            grid.add(worker, 1, 7+i);
            grid.add(new Label("Посада:"), 0, 8+i);
            grid.add(positionField, 1, 8+i);
            grid.add(new Label("Виїзд: пробіг:"), 0, 9+i);
            grid.add(startMileageField, 1, 9+i);
            grid.add(new Label("Виїзд: паливо:"), 0, 10+i);
            grid.add(startFuelField, 1, 10+i);
            grid.add(new Label("Повернення: пробіг:"), 0, 11+i);
            grid.add(endMileageField, 1, 11+i);
            grid.add(new Label("Повернення: паливо:"), 0, 12+i);
            grid.add(endFuelField, 1, 12+i);
            grid.add(new Label("Заправка:"), 0, 13+i);
            grid.add(refuelField, 1, 13+i);

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( carField.getValue() == null || ListType.getValue() == null || (ListType.getValue() == "за наказом" && order.getValue() == null) ||
                        datePickerStart.getValue() == null || datePickerEnd.getValue() == null || isEmptyOrWhitespace(routeField.getText()) ||
                        isEmptyOrWhitespace(goalField.getText()) || worker.getValue() == null || isEmptyOrWhitespace(numberField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _List newList = new _List(
                                selectedList.getId(),
                                carsT.get(carField.getValue()),
                                (Objects.equals(ListType.getValue(), "за наказом"))?ordersT.get(order.getValue()): -1,
                                workersT.get(worker.getValue()),
                                datePickerStart.getValue(),
                                datePickerEnd.getValue(),
                                routeField.getText(),
                                goalField.getText()
                        );
                        newList.setDone(selectedList.isDone());
                        newList.setNumber(Integer.parseInt(numberField.getText()));

                        if(dbManager.getUsername().equals("root")) {
                            if(selectedList.getEndMileage() != Double.parseDouble(endMileageField.getText())) {
                                newList.setEndMileage(Double.parseDouble(endMileageField.getText()));
                            }
                            if(selectedList.getEndFuel() != Double.parseDouble(endFuelField.getText())) {
                                newList.setEndFuel(Double.parseDouble(endFuelField.getText()));
                            }
                            if(selectedList.getRefuel() != Double.parseDouble(refuelField.getText())) {
                                newList.setRefuel(Double.parseDouble(refuelField.getText()));
                            }
                        }

                        if(dbManager.getUsername().equals("root")) {
                            if(selectedList.getEndFuel() != Double.parseDouble(endFuelField.getText())) {
                                newList.setEndFuel(Double.parseDouble(endFuelField.getText()));
                            } else {
                                newList.setEndFuel(selectedList.getEndFuel());
                            }
                            if(selectedList.getEndMileage() != Double.parseDouble(endMileageField.getText())) {
                                newList.setEndMileage(Double.parseDouble(endMileageField.getText()));
                            } else {
                                newList.setEndMileage(selectedList.getEndMileage());
                            }
                            if(selectedList.getRefuel() != Double.parseDouble(refuelField.getText())) {
                                newList.setRefuel(Double.parseDouble(refuelField.getText()));
                            } else {
                                newList.setRefuel(selectedList.getRefuel());
                            }
                            if(selectedList.getStartFuel() != Double.parseDouble(startFuelField.getText())) {
                                newList.setStartFuel(Double.parseDouble(startFuelField.getText()));
                            } else {
                                newList.setStartFuel(selectedList.getStartFuel());
                            }
                            if(selectedList.getStartMileage() != Double.parseDouble(startMileageField.getText())) {
                                newList.setStartMileage(Double.parseDouble(startMileageField.getText()));
                            } else {
                                newList.setStartMileage(selectedList.getStartMileage());
                            }
                        }

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.updateList(newList)) {
                                    lists.clear();
                                    lists.addAll(dbManager.getLists());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();

                                }
                            }
                        });


                        carsT.clear();
                        carField.getItems().clear();
                        List<_Car> cars = getValidCars();
                        for(_Car d  : cars) {
                            carsT.put(d.getNumber() + " " +d.getModel(), d.getIdCar());
                            carField.getItems().add(d.getNumber() + " " +d.getModel());
                        }
                        carsT.put(dbManager.getCar(selectedList.getIdCar()).getNumber() + " " + dbManager.getCar(selectedList.getIdCar()).getModel(), selectedList.getIdCar());
                        carField.getItems().add(dbManager.getCar(selectedList.getIdCar()).getNumber() + " " + dbManager.getCar(selectedList.getIdCar()).getModel());


                        workersT.clear();
                        worker.getItems().clear();
                        List<_Worker> workers = getValidWorkers();
                        for(_Worker d  : workers) {
                            workersT.put(d.getNameN(), d.getId());
                            worker.getItems().add(d.getNameN());
                        }
                        workersT.put(dbManager.getWorker(selectedList.getIdWorker()).getNameN(), selectedList.getIdWorker());
                        worker.getItems().add(dbManager.getWorker(selectedList.getIdWorker()).getNameN());

                        ordersT.clear();
                        order.getItems().clear();
                        List<_Order> orders = getValidOrders();
                        for(_Order d  : orders) {
                            ordersT.put(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()), d.getIdOrder());
                            order.getItems().add(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()));
                        }
                        if(selectedList.getIdOrder() != -1) {
                            ordersT.put(dbManager.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbManager.getWorkerName(true, selectedList.getIdWorker()), selectedList.getIdOrder());
                            order.getItems().add(dbManager.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbManager.getWorkerName(true, selectedList.getIdWorker()));
                        }
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });


        }
    }

    private void openCloseListWindow(_List selectedList) {
        String windowTitle = "Закрити подорожній лист";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            _Car car = dbManager.getCar(selectedList.getIdCar());



            DatePicker datePickerStart = new DatePicker(selectedList.getStartDate());
            DatePicker datePickerEnd = new DatePicker(selectedList.getEndDate());

            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField routeField = new TextField(selectedList.getRoute());
            TextField goalField = new TextField(selectedList.getGoal());
            TextField carField = new TextField(car.getNumber() + " " + car.getModel());
            TextField worker = new TextField(dbManager.getWorkerName(true, selectedList.getIdWorker()));
            TextField positionField = new TextField(dbManager.getWorkerPosition(true, selectedList.getIdWorker()));
            TextField ListType = new TextField((selectedList.getIdOrder() == -1)?"по місту":"за наказом");
            TextField order = new TextField((selectedList.getIdOrder() == -1)?"":dbManager.getOrderNumber(selectedList.getIdOrder()));
            TextField endFuelField = new TextField();
            TextField endMileageField = new TextField();
            TextField refuelField = new TextField();

            order.setDisable(true);
            datePickerStart.setDisable(true);
            datePickerEnd.setDisable(true);
            routeField.setDisable(true);
            goalField.setDisable(true);
            worker.setDisable(true);
            carField.setDisable(true);
            positionField.setDisable(true);
            ListType.setDisable(true);

            grid.add(new Label("Авто:"), 0, 0);
            grid.add(carField, 1, 0);
            grid.add(new Label("Тип листа:"), 0, 1);
            grid.add(ListType, 1, 1);
            grid.add(new Label("Наказ:"), 0, 2);
            grid.add(order, 1, 2);
            grid.add(new Label("Виїзд: дата:"), 0, 3);
            grid.add(datePickerStart, 1, 3);
            grid.add(new Label("Поверення: дата:"), 0, 4);
            grid.add(datePickerEnd, 1, 4);
            grid.add(new Label("Маршрут:"), 0, 5);
            grid.add(routeField, 1, 5);
            grid.add(new Label("Мета:"), 0, 6);
            grid.add(goalField, 1, 6);
            grid.add(new Label("Працівник:"), 0, 7);
            grid.add(worker, 1, 7);
            grid.add(new Label("Посада:"), 0, 8);
            grid.add(positionField, 1, 8);
            grid.add(new Label("Повернення: паливо"), 0, 9);
            grid.add(endFuelField, 1, 9);
            grid.add(new Label("Повернення: пробіг"), 0, 10);
            grid.add(endMileageField, 1, 10);
            grid.add(new Label("Заправка"), 0, 11);
            grid.add(refuelField, 1, 11);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if (isEmptyOrWhitespace(endFuelField.getText()) || isEmptyOrWhitespace(endMileageField.getText()) || isEmptyOrWhitespace(refuelField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _List newList = selectedList;
                        newList.setEndFuel(Double.parseDouble(endFuelField.getText().replace(',', '.')));
                        newList.setEndMileage(Double.parseDouble(endMileageField.getText().replace(',', '.')));
                        newList.setRefuel(Double.parseDouble(refuelField.getText().replace(',', '.')));
                        newList.setDone(true);

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Закрити лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.updateList(newList)) {
                                    lists.clear();
                                    lists.addAll(dbManager.getLists());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();

                                    if(selectedList.getIdOrder() != -1) {
                                        Alert CreateReportAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Створити звіт про завершення виконання за наказом");
                                        CreateReportAlert.showAndWait().ifPresent(response2 -> {
                                            if (response2 == ButtonType.OK){
                                                openAddReportWindow(selectedList.getIdOrder());
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    //----------------------------------

    private int extractNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isEmpty()) {
            return Integer.MAX_VALUE; // Якщо порожнє значення, відправити в кінець
        }
        // Виділяємо початкове число з наказу
        String numPart = orderNumber.replaceAll("^(\\d+).*", "$1");
        try {
            return Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // Якщо не вдалося розпарсити, відправити в кінець
        }
    }

    private void openOrderJournal() {
        String windowTitle = "Журнал: накази";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            Button addButton = new Button("Додати наказ");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> openAddOrderWindow(null));

            Button editButton = new Button("Редагувати наказ");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.setDisable(true);

            Button copyButton = new Button("Копіювати наказ");
            copyButton.setGraphic(IconsManager.getCopyIcon());
            copyButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[2] + "\\");
            });


            Button openFileButton = new Button("Відкрити наказ");
            openFileButton.setGraphic(IconsManager.getFileIcon());
            openFileButton.setDisable(true);

            Button deleteButton = new Button("Позначити наказ на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());


            deleteButton.getStyleClass().add("red-button");
            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");
            openFileButton.getStyleClass().add("grey-button");
            copyButton.getStyleClass().add("grey-button");

            orders = FXCollections.observableArrayList(dbManager.getOrders());

            TableView<_Order> tableView = new TableView<>();
            tableView.setItems(orders);
            TableColumn<_Order, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("idOrder"));

            TableColumn<_Order, LocalDate> orderDateCol = new TableColumn<>("Дата наказу");
            orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
            orderDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Order, String> orderNumberCol = new TableColumn<>("№ наказу");
            orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            orderNumberCol.setComparator((o1, o2) -> {
                int num1 = extractNumber(o1);
                int num2 = extractNumber(o2);
                return Integer.compare(num1, num2);
            });

            TableColumn<_Order, String> workerCol = new TableColumn<>("ПІБ працівник");
            workerCol.setCellValueFactory(cellData -> {
                String Name = dbManager.getWorkerName(true, cellData.getValue().getIdWorker());
                return new SimpleStringProperty(Name);
            });

            TableColumn<_Order, String> positionCol = new TableColumn<>("Посада");
            positionCol.setCellValueFactory(cellData -> {
                String Name = dbManager.getWorkerPosition(true, cellData.getValue().getIdWorker());
                return new SimpleStringProperty(Name);
            });

            TableColumn<_Order, LocalDate> startDateCol = new TableColumn<>("Виїзд: дата");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Order, LocalDate> endDateCol = new TableColumn<>("Повернення: дата");
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            endDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Order, String> routeCol = new TableColumn<>("Маршрут");
            routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));

            TableColumn<_Order, Double> moneyCol = new TableColumn<>("Гроші/доба");
            moneyCol.setCellValueFactory(new PropertyValueFactory<>("money"));

            TableColumn<_Order, String> goalCol = new TableColumn<>("Мета");
            goalCol.setCellValueFactory(new PropertyValueFactory<>("goal"));

            TableColumn<_Order, String> headCol = new TableColumn<>("Керівник");
            headCol.setCellValueFactory(new PropertyValueFactory<>("head"));

            TableColumn<_Order, String> validCol = new TableColumn<>("Актуальність");
            validCol.setCellValueFactory(cellData -> {
                boolean valid = dbManager.isOrderModifiable(cellData.getValue().getIdOrder());
                return new SimpleStringProperty(valid ? "Невиконаний" : "Виконаний");
            });

            tableView.getColumns().addAll( orderDateCol, orderNumberCol, workerCol,positionCol, startDateCol,
                    endDateCol, routeCol, moneyCol, goalCol, headCol, validCol);

            tableView.getSortOrder().add(orderNumberCol);
            orderNumberCol.setSortType(TableColumn.SortType.ASCENDING);


            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                editButton.setDisable(newSelection == null || (!dbManager.isOrderModifiable(newSelection.getIdOrder()) && !dbManager.getUsername().equals("root")));
                copyButton.setDisable(newSelection == null);
                openFileButton.setDisable(newSelection == null);
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));
            });
            tableView.scrollTo(tableView.getItems().size() - 1);//todo: add everywhere

            openFileButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    dM.createOrderDocument(dbManager, selectedOrder);
                }
            });

            updateButton.setOnAction(e->{
                orders.clear();
                orders.addAll(dbManager.getOrders());
                tableView.sort();
            });

            editButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    openOrderEditWindow(selectedOrder);
                }
            });

            copyButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    openAddOrderWindow(selectedOrder);
                }
            });

            HBox buttonBox = new HBox(10, updateButton, addButton, copyButton, editButton, openFileButton, openFolderButton ,deleteButton);
            buttonBox.getStyleClass().add("buttonBox");
            buttonBox.setAlignment(Pos.CENTER_LEFT);



            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            tableView.setRowFactory(tv -> {
                TableRow<_Order> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _Order selectedOrder = row.getItem();
                    if (selectedOrder != null && dbManager.isOrderModifiable(selectedOrder.getIdOrder())) {
                        openOrderEditWindow(selectedOrder);
                    }
                });

                MenuItem openItem = new MenuItem("Відкрити документ");
                openItem.setOnAction(event -> {
                    _Order selectedOrder = row.getItem();
                    if (selectedOrder != null) {
                        dM.createOrderDocument(dbManager, selectedOrder);
                    }
                });

                // Double-click event handler
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        _Order selectedOrder = row.getItem();
                        if (selectedOrder != null) {
                            dM.createOrderDocument(dbManager, selectedOrder);
                        }
                    }
                });

                // Оновлення контекстного меню залежно від значення valid
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    rowMenu.getItems().clear(); // Очищуємо попередні пункти
                    if (newItem != null ) {
                        rowMenu.getItems().add(openItem);
                        if(dbManager.isOrderModifiable(newItem.getIdOrder())) {
                            rowMenu.getItems().add(editItem);
                        }
                    }

                });

                // Прив'язка контекстного меню до рядка лише, коли він не порожній
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));

                return row;
            });

            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити Наказ");
                deleteButton.setOnAction(e->{
                    _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити наказ");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteOrder(selectedOrder);
                                orders.clear();
                                orders.addAll(dbManager.getOrders());
                            }
                        });

                    }
                });
            }


            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private void openAddOrderWindow(_Order order) {
        String windowTitle = "Додати наказ";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            DatePicker datePickerOrderDate = new DatePicker();
            datePickerOrderDate.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            datePickerOrderDate.setValue(LocalDate.now());
            TextField orderNumberField = new TextField();

            List<_Worker> validWorkers = dbManager.getValidWorkers();

            ComboBox<String> worker = new ComboBox<>();
            Map<String, Integer> workersT = new HashMap<>();

            for(_Worker d  : validWorkers) {
                workersT.put(d.getNameN(), d.getId());
                worker.getItems().add(d.getNameN());
            }


            TextField positionField = new TextField();
            positionField.setDisable(true);

            worker.setOnAction(e -> {
                String selectedWorker = worker.getValue();
                if (selectedWorker != null) {
                    positionField.setText(dbManager.getWorkerPosition(true, workersT.get(selectedWorker)));
                }
            });


            DatePicker datePickerStart = new DatePicker();
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            DatePicker datePickerEnd = new DatePicker();
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField routeField = new TextField();
            TextField moneyField = new TextField();
            TextField goalField = new TextField();

            ComboBox<String> headType = new ComboBox<>();
            headType.getItems().add("директор");
            headType.getItems().add("тимчасово виконуючий обов'язки");

            TextField headField = new TextField();

            headType.setOnAction(e -> {
                String selectedOption = headType.getValue();
                if (selectedOption == "директор") {
                    _Company _company = dbManager.getCompanyInfo();
                    headField.setText(_company.getCeo());
                    headField.setDisable(true);
                }
                else {
                    headField.clear();
                    headField.setDisable(false);
                }
            });

            if(order != null) {
                _Company _company = dbManager.getCompanyInfo();
                datePickerOrderDate.setValue(order.getOrderDate());
                datePickerStart.setValue(order.getStartDate());
                datePickerEnd.setValue(order.getEndDate());
                routeField.setText(order.getRoute());
                goalField.setText(order.getGoal());
                moneyField.setText(String.valueOf(order.getMoney()));
                headType.setValue((_company.getCeo().equals(order.getHead()))?"директор":"тимчасово виконуючий обов'язки");
                headField.setText(order.getHead());
            }


            grid.add(new Label("Дата наказу:"), 0, 0);
            grid.add(datePickerOrderDate, 1, 0);
            grid.add(new Label("№ наказу:"), 0, 1);
            grid.add(orderNumberField, 1, 1);
            grid.add(new Label("Працівник:"), 0, 2);
            grid.add(worker, 1, 2);
            grid.add(new Label("Посада:"), 0, 3);
            grid.add(positionField, 1, 3);
            grid.add(new Label("Виїзд: дата:"), 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(new Label("Повернення: дата:"), 0, 5);
            grid.add(datePickerEnd, 1, 5);
            grid.add(new Label("Маршрут:"), 0, 6);
            grid.add(routeField, 1, 6);
            grid.add(new Label("Гроші/доба:"), 0, 7);
            grid.add(moneyField, 1, 7);
            grid.add(new Label("Мета"), 0, 8);
            grid.add(goalField, 1, 8);
            grid.add(new Label("Керівник"), 0, 9);
            grid.add(headType, 1, 9);
            grid.add(new Label("ПІБ керівника"), 0, 10);
            grid.add(headField, 1, 10);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( datePickerOrderDate.getValue() == null || isEmptyOrWhitespace(orderNumberField.getText()) ||
                        worker.getValue() == null || datePickerStart.getValue() == null ||
                        datePickerEnd.getValue() == null || isEmptyOrWhitespace(moneyField.getText()) || isEmptyOrWhitespace(routeField.getText()) ||
                        isEmptyOrWhitespace(goalField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        double money = Double.parseDouble(moneyField.getText().replace(',', '.'));
                        _Order newOrder = new _Order(
                                datePickerOrderDate.getValue(),
                                orderNumberField.getText(),
                                workersT.get(worker.getValue()),
                                datePickerStart.getValue(),
                                datePickerEnd.getValue(),
                                routeField.getText(),
                                money,
                                goalField.getText(),
                                headField.getText()
                        );

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати наказ");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.addOrder(newOrder)) {
                                    orders.clear();
                                    orders.addAll(dbManager.getOrders());
                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    private void openOrderEditWindow(_Order selectedOrder) {
        String windowTitle = "Редагувати наказ";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            DatePicker datePickerOrderDate = new DatePicker(selectedOrder.getOrderDate());
            datePickerOrderDate.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField orderNumberField = new TextField(selectedOrder.getOrderNumber());

            List<_Worker> validWorkers = dbManager.getValidWorkers();
            validWorkers.add(dbManager.getWorker(selectedOrder.getIdWorker()));

            ComboBox<String> worker = new ComboBox<>();
            Map<String, Integer> workersT = new HashMap<>();

            for(_Worker d  : validWorkers) {
                workersT.put(d.getNameN(), d.getId());
                worker.getItems().add(d.getNameN());
            }

            worker.setValue(dbManager.getWorkerName(true, selectedOrder.getIdWorker()));

            TextField positionField = new TextField();
            positionField.setDisable(true);

            positionField.setText(dbManager.getWorkerPosition(true, selectedOrder.getIdWorker()));

            worker.setOnAction(e -> {
                String selectedWorker = worker.getValue();
                if (selectedWorker != null) {
                    positionField.setText(dbManager.getWorkerPosition(true, workersT.get(selectedWorker)));
                }
            });

            DatePicker datePickerStart = new DatePicker(selectedOrder.getStartDate());
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            DatePicker datePickerEnd = new DatePicker(selectedOrder.getEndDate());
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField routeField = new TextField(selectedOrder.getRoute());
            TextField moneyField = new TextField(String.valueOf(selectedOrder.getMoney()));
            TextField goalField = new TextField(selectedOrder.getGoal());

            ComboBox<String> headType = new ComboBox<>();
            headType.getItems().add("директор");
            headType.getItems().add("тимчасово виконуючий обов'язки");
            TextField headField = new TextField();

            if(selectedOrder.getHead().equals(dbManager.getCompanyInfo().getCeo())){
                headType.setValue("директор");
                headField.setDisable(true);
            } else {
                headType.setValue("тимчасово виконуючий обов'язки");
                headField.setDisable(false);
            }
            headField.setText(selectedOrder.getHead());

            headType.setOnAction(e -> {
                String selectedOption = headType.getValue();
                if (selectedOption == "директор") {
                    _Company _company = dbManager.getCompanyInfo();
                    headField.setText(_company.getCeo());
                    headField.setDisable(true);
                }
                else {
                    headField.clear();
                    headField.setDisable(false);
                }
            });


            grid.add(new Label("Дата наказу:"), 0, 0);
            grid.add(datePickerOrderDate, 1, 0);
            grid.add(new Label("№ наказу:"), 0, 1);
            grid.add(orderNumberField, 1, 1);
            grid.add(new Label("Працівник:"), 0, 2);
            grid.add(worker, 1, 2);
            grid.add(new Label("Посада:"), 0, 3);
            grid.add(positionField, 1, 3);
            grid.add(new Label("Виїзд: дата:"), 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(new Label("Повернення: дата:"), 0, 5);
            grid.add(datePickerEnd, 1, 5);
            grid.add(new Label("Маршрут:"), 0, 6);
            grid.add(routeField, 1, 6);
            grid.add(new Label("Гроші/доба"), 0, 7);
            grid.add(moneyField, 1, 7);
            grid.add(new Label("Мета"), 0, 8);
            grid.add(goalField, 1, 8);
            grid.add(new Label("Керівник"), 0, 9);
            grid.add(headType, 1, 9);
            grid.add(new Label("ПІБ керівника"), 0, 10);
            grid.add(headField, 1, 10);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( datePickerOrderDate.getValue() == null || isEmptyOrWhitespace(orderNumberField.getText()) ||
                        worker.getValue() == null || datePickerStart.getValue() == null ||
                        datePickerEnd.getValue() == null || isEmptyOrWhitespace(moneyField.getText()) || isEmptyOrWhitespace(routeField.getText()) ||
                        isEmptyOrWhitespace(goalField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        double money = Double.parseDouble(moneyField.getText().replace(',', '.'));
                        _Order newOrder = new _Order(
                                selectedOrder.getIdOrder(),
                                datePickerOrderDate.getValue(),
                                orderNumberField.getText(),
                                workersT.get(worker.getValue()),
                                datePickerStart.getValue(),
                                datePickerEnd.getValue(),
                                routeField.getText(),
                                money,
                                goalField.getText(),
                                headField.getText()
                        );

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати наказ");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.changeOrder(newOrder)) {
                                    orders.clear();
                                    orders.addAll(dbManager.getOrders());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    //----------------------------------

    private void openPositionsHandbookWindow() {

        String windowTitle = "Довідник: посади";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {

            Button addButton = new Button("Додати посаду");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> openAddPositionWindow());

            Button editButton = new Button("Редагувати посаду");
            editButton.setDisable(true);
            editButton.setGraphic(IconsManager.getPencilIcon());

            Button deleteButton = new Button("Позначити посаду на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());
            updateButton.setOnAction(e->{
                positions.clear();
                positions.addAll(dbManager.getPositions());
            });


            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");
            deleteButton.getStyleClass().add("red-button");

            positions = FXCollections.observableArrayList(dbManager.getPositions());

            TableView<_Position> tableView = new TableView<>();
            tableView.setItems(positions);

            TableColumn<_Position, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_Position, String> nameNCol = new TableColumn<>("Назва(називний відмінок)");
            nameNCol.setCellValueFactory(new PropertyValueFactory<>("nameN"));

            TableColumn<_Position, String> nameRCol = new TableColumn<>("Назва(родовий відмінок)");
            nameRCol.setCellValueFactory(new PropertyValueFactory<>("nameR"));


            // Активуємо кнопку "Редагувати авто" при виборі рядка
            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                editButton.setDisable(newSelection == null);
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));
            });

            editButton.setOnAction(e -> {
                _Position selectedPosition = tableView.getSelectionModel().getSelectedItem();
                if (selectedPosition != null) {
                    openPositionEditWindow(selectedPosition);
                }
            });

            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити посаду");
                deleteButton.setOnAction(e -> {
                    _Position selectedPosition = tableView.getSelectionModel().getSelectedItem();
                    if (selectedPosition != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити посаду");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.removePosition(selectedPosition);
                                positions.clear();
                                positions.addAll(dbManager.getPositions());
                            }
                        });

                    }
                });
            }



            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll( nameNCol, nameRCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);

            // Adding a context menu to each row
            tableView.setRowFactory(tv -> {
                TableRow<_Position> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _Position selectedPosition = row.getItem();
                    if (selectedPosition != null) {
                        openPositionEditWindow(selectedPosition);
                    }
                });

                // Пункт "Видалити", додаватиметься лише для актуальних авто
                MenuItem removeItem = new MenuItem("Видалити");
                removeItem.setOnAction(event -> {
                    _Position selectedPosition = row.getItem();
                    if (selectedPosition != null) {
                        Alert a = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити посаду");
                        Optional<ButtonType> result = a.showAndWait();

                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            dbManager.removePosition(selectedPosition);
                            positions.clear();
                            positions.addAll(dbManager.getPositions());
                        }
                    }
                });

                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    rowMenu.getItems().clear();
                    if (newItem != null) {
                        rowMenu.getItems().add(editItem);
                        rowMenu.getItems().add(removeItem);

                    }
                });

                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));

                return row;
            });


            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private void openPositionEditWindow(_Position selectedPosition) {
        String windowTitle = "Редагувати посаду";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameNField = new TextField(selectedPosition.getNameN());
            TextField nameRField = new TextField(selectedPosition.getNameR());

            grid.add(new Label("Назва(називний відмінок):"), 0, 0);
            grid.add(nameNField, 1, 0);
            grid.add(new Label("Назва(родовий відмінок):"), 0, 1);
            grid.add(nameRField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameNField.getText()) || isEmptyOrWhitespace(nameRField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Position position = new _Position(selectedPosition.getId(), nameNField.getText(), nameRField.getText());

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати посаду");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.changePosition(position)) {
                                    positions.clear();
                                    positions.addAll(dbManager.getPositions());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    private void openAddPositionWindow() {
        String windowTitle = "Додати посаду";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameNField = new TextField();
            TextField nameRField = new TextField();

            grid.add(new Label("Назва(називний відмінок):"), 0, 0);
            grid.add(nameNField, 1, 0);
            grid.add(new Label("Назва(родовий відмінок):"), 0, 1);
            grid.add(nameRField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);


            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameNField.getText()) || isEmptyOrWhitespace(nameRField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Position position = new _Position(nameNField.getText(), nameRField.getText());

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати посаду");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.addPosition(position)) {
                                    positions.clear();
                                    positions.addAll(dbManager.getPositions());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    //----------------------------------

    private void openСarsHandbookWindow() {
        String windowTitle = "Довідник: автомобілі";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            cars = FXCollections.observableArrayList(dbManager.getCars());


            ComboBox<String> selectionModel = new ComboBox<>();

            selectionModel.getItems().addAll("актуальні", "неактуальні", "");
            selectionModel.setValue("");

            selectionModel.setOnAction(event -> {
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    cars.clear();
                    cars.addAll(dbManager.getValidCars());
                } else if(selected.equals("неактуальні")) {
                    cars.clear();
                    cars.addAll(dbManager.getUnValidCars());
                } else {
                    cars.clear();
                    cars.addAll(dbManager.getCars());
                }
            });

            Button addButton = new Button("Додати авто");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> {
                openAddCarWindow();
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    cars.clear();
                    cars.addAll(dbManager.getValidCars());
                } else if(selected.equals("неактуальні")) {
                    cars.clear();
                    cars.addAll(dbManager.getUnValidCars());
                } else {
                    cars.clear();
                    cars.addAll(dbManager.getCars());
                }
            });

            Button createFileButton = new Button("Зберегти довідник");
            createFileButton.setGraphic(IconsManager.getFileIcon());
            createFileButton.getStyleClass().add("grey-button");
            createFileButton.setOnAction(e -> {
                dM.createCarsHandbook(dbManager, cars);
            });

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[0] + "\\");
            });

            Button editButton = new Button("Редагувати авто");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.setDisable(true);

            Button removeButton = new Button("Зняти авто з експлуатації");
            removeButton.setGraphic(IconsManager.getCrossIcon());
            removeButton.setDisable(true);

            Button deleteButton = new Button("Позначити авто на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());
            updateButton.setOnAction(e->{
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    cars.clear();
                    cars.addAll(dbManager.getValidCars());
                } else if(selected.equals("неактуальні")) {
                    cars.clear();
                    cars.addAll(dbManager.getUnValidCars());
                } else {
                    cars.clear();
                    cars.addAll(dbManager.getCars());
                }
            });

            deleteButton.getStyleClass().add("red-button");
            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");
            removeButton.getStyleClass().add("red-button");

            TableView<_Car> tableView = new TableView<>();
            tableView.setItems(cars);
            TableColumn<_Car, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("idCar"));

            TableColumn<_Car, String> numberCol = new TableColumn<>("Номер");
            numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

            TableColumn<_Car, String> modelCol = new TableColumn<>("Модель");
            modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

            TableColumn<_Car, String> fuelTypeCol = new TableColumn<>("Тип палива");
            fuelTypeCol.setCellValueFactory(new PropertyValueFactory<>("fuelType"));

            TableColumn<_Car, Double> fuelUsageCol = new TableColumn<>("Використання \nпалива(л/100км)");
            fuelUsageCol.setCellValueFactory(new PropertyValueFactory<>("fuelUsage"));

            TableColumn<_Car, Double> engineVolCol = new TableColumn<>("Об'єм двигуна");
            engineVolCol.setCellValueFactory(new PropertyValueFactory<>("engineVolume"));

            TableColumn<_Car, LocalDate> startDateCol = new TableColumn<>("Дата початку експлуатації");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Car, String> startOrderCol = new TableColumn<>("Номер наказу \nпочатку експлуатації");
            startOrderCol.setCellValueFactory(new PropertyValueFactory<>("startOrderNumber"));

            TableColumn<_Car, LocalDate> endDateCol = new TableColumn<>("Дата закінчення експлуатації");
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            endDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Car, String> endOrderCol = new TableColumn<>("Номер наказу \nзакінчення експлуатації");
            endOrderCol.setCellValueFactory(new PropertyValueFactory<>("endOrderNumber"));

            TableColumn<_Car, String> validCol = new TableColumn<>("Актуальність");
            validCol.setCellValueFactory(cellData -> {
                boolean valid = cellData.getValue().isValid();
                return new SimpleStringProperty(valid ? "Дійсний" : "Недійсний");
            });

            // Активуємо кнопку "Редагувати авто" при виборі рядка
            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean isItemSelected = newSelection != null;
                editButton.setDisable(!isItemSelected);
                removeButton.setDisable(!isItemSelected || !newSelection.isValid());
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));
            });



            editButton.setOnAction(e -> {
                _Car selectedCar = tableView.getSelectionModel().getSelectedItem();
                if (selectedCar != null) {
                    openCarEditWindow(selectedCar);
                    String selected = selectionModel.getValue();
                    if(selected.equals("актуальні")) {
                        cars.clear();
                        cars.addAll(dbManager.getValidCars());
                    } else if(selected.equals("неактуальні")) {
                        cars.clear();
                        cars.addAll(dbManager.getUnValidCars());
                    } else {
                        cars.clear();
                        cars.addAll(dbManager.getCars());
                    }
                }
            });

            removeButton.setOnAction(e -> {
                _Car selectedCar = tableView.getSelectionModel().getSelectedItem();
                if (selectedCar != null) {
                    openCarRemoveWindow(selectedCar);
                    String selected = selectionModel.getValue();
                    if(selected.equals("актуальні")) {
                        cars.clear();
                        cars.addAll(dbManager.getValidCars());
                    } else if(selected.equals("неактуальні")) {
                        cars.clear();
                        cars.addAll(dbManager.getUnValidCars());
                    } else {
                        cars.clear();
                        cars.addAll(dbManager.getCars());
                    }
                }
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, new Label("Фільтрувати актуальність"), selectionModel, createFileButton, openFolderButton, removeButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll( numberCol, modelCol, fuelTypeCol, fuelUsageCol,
                    engineVolCol, startDateCol, startOrderCol, endDateCol, endOrderCol, validCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);

            // Adding a context menu to each row
            tableView.setRowFactory(tv -> {
                TableRow<_Car> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _Car selectedCar = row.getItem();
                    if (selectedCar != null) {
                        openCarEditWindow(selectedCar);
                    }
                });

                // Пункт "Видалити", додаватиметься лише для актуальних авто
                MenuItem removeItem = new MenuItem("Видалити");
                removeItem.setOnAction(event -> {
                    _Car selectedCar = row.getItem();
                    if (selectedCar != null) {
                        openCarRemoveWindow(selectedCar);
                    }
                });

                // Оновлення контекстного меню залежно від значення valid
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    rowMenu.getItems().clear(); // Очищуємо попередні пункти
                    if (newItem != null) {
                        rowMenu.getItems().add(editItem); // Додаємо пункт "Редагувати"
                        if (newItem.isValid()) {          // Перевіряємо поле valid
                            rowMenu.getItems().add(removeItem); // Додаємо "Видалити", якщо valid == true
                        }
                    }
                });

                // Прив'язка контекстного меню до рядка лише, коли він не порожній
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));

                return row;
            });
            tableView.scrollTo(tableView.getItems().size() - 1);
            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити авто");
                deleteButton.setOnAction(e->{
                    _Car selectedCar = tableView.getSelectionModel().getSelectedItem();
                    if (selectedCar != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteCar(selectedCar);
                                String selected = selectionModel.getValue();
                                if(selected.equals("актуальні")) {
                                    cars.clear();
                                    cars.addAll(dbManager.getValidCars());
                                } else if(selected.equals("неактуальні")) {
                                    cars.clear();
                                    cars.addAll(dbManager.getUnValidCars());
                                } else {
                                    cars.clear();
                                    cars.addAll(dbManager.getCars());
                                }
                            }
                        });

                    }
                });
            }


            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private void openCarRemoveWindow(_Car selectedCar) {
        String windowTitle = "Зняти авто з експлуатації";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            DatePicker datePickerEnd = new DatePicker();
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField endOrderNumberField = new TextField();
            datePickerEnd.setValue(LocalDate.now());

            grid.add(new Label("Дата закінчення експлуатації:"), 0, 0);
            grid.add(datePickerEnd, 1, 0);
            grid.add(new Label("Номер наказу закінчення експлуатації:"), 0, 1);
            grid.add(endOrderNumberField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow =  openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e -> {
                try {
                    selectedCar.setEndDate(datePickerEnd.getValue());
                    selectedCar.setEndOrderNumber(endOrderNumberField.getText());

                    if(selectedCar.getEndDate() == null || selectedCar.getEndOrderNumber() == ""){
                        Alert alert = Alerts.ErrorAlert("Введіть дані", "Не введено номер наказу або дату закінчення");
                        alert.showAndWait();
                    }
                    else {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if (dbManager.removeCar(selectedCar)) {
                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();

                                }
                            }
                        });
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Please enter valid numbers");
                }
            });
        }
    }

    private void openCarEditWindow(_Car selectedCar) {
        String windowTitle = "Редагувати авто";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField numberField = new TextField(selectedCar.getNumber());
            TextField modelField = new TextField(selectedCar.getModel());
            TextField fuelTypeField = new TextField(selectedCar.getFuelType());
            TextField fuelUsageField = new TextField(String.valueOf(selectedCar.getFuelUsage()));
            TextField engineVolumeField = new TextField(String.valueOf(selectedCar.getEngineVolume()));
            TextField startOrderNumberField = new TextField(selectedCar.getStartOrderNumber());
            TextField endOrderNumberField = new TextField(selectedCar.getEndOrderNumber());
            TextField startFuelField = new TextField(String.valueOf(selectedCar.getStartFuel()));
            TextField startMileageField = new TextField(String.valueOf(selectedCar.getStartMileage()));
            DatePicker datePickerStart = new DatePicker(selectedCar.getStartDate());
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            DatePicker datePickerEnd = new DatePicker();
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            grid.add(new Label("Номер:"), 0, 0);
            grid.add(numberField, 1, 0);
            grid.add(new Label("Модель:"), 0, 1);
            grid.add(modelField, 1, 1);
            grid.add(new Label("Тип палива:"), 0, 2);
            grid.add(fuelTypeField, 1, 2);
            grid.add(new Label("Використання палива(л/100км):"), 0, 3);
            grid.add(fuelUsageField, 1, 3);
            grid.add(new Label("Об'єм двигуна:"), 0, 4);
            grid.add(engineVolumeField, 1, 4);
            grid.add(new Label("Початок експлуатації: дата:"), 0, 5);
            grid.add(datePickerStart, 1, 5);
            grid.add(new Label("Початок експлуатації: номер наказу:"), 0, 6);
            grid.add(startOrderNumberField, 1, 6);
            grid.add(new Label("Початок експлуатації: пробіг:"), 0, 7);
            grid.add(startMileageField, 1, 7);
            grid.add(new Label("Початок експлуатації: паливо:"), 0, 8);
            grid.add(startFuelField, 1, 8);
            grid.add(new Label("Кінець експлуатації: дата:"), 0, 9);
            grid.add(datePickerEnd, 1, 9);
            grid.add(new Label("Кінець експлуатації: номер наказу:"), 0, 10);
            grid.add(endOrderNumberField, 1, 10);

            startMileageField.setDisable(!dbManager.getUsername().equals("root"));
            startFuelField.setDisable(!dbManager.getUsername().equals("root"));

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( (datePickerEnd.getValue() != null && isEmptyOrWhitespace(endOrderNumberField.getText())) ||
                        (datePickerEnd.getValue() == null && !isEmptyOrWhitespace(endOrderNumberField.getText()))
                        || isEmptyOrWhitespace(numberField.getText()) || isEmptyOrWhitespace(modelField.getText()) ||
                        isEmptyOrWhitespace(fuelTypeField.getText()) || isEmptyOrWhitespace(fuelUsageField.getText()) ||
                        isEmptyOrWhitespace(engineVolumeField.getText()) || isEmptyOrWhitespace(startOrderNumberField.getText()) ||
                        isEmptyOrWhitespace(startFuelField.getText())||isEmptyOrWhitespace(startMileageField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Car car = new _Car(
                                selectedCar.getIdCar(),
                                numberField.getText(),
                                modelField.getText(),
                                fuelTypeField.getText(),
                                Double.parseDouble(fuelUsageField.getText().replace(',', '.')),
                                Double.parseDouble(engineVolumeField.getText().replace(',', '.')),
                                datePickerStart.getValue(),
                                startOrderNumberField.getText(),
                                datePickerEnd.getValue() != null ? datePickerEnd.getValue() : null,
                                endOrderNumberField.getText() == null ? null : endOrderNumberField.getText(),
                                datePickerEnd.getValue() == null &&
                                        (endOrderNumberField.getText() == null || endOrderNumberField.getText().isEmpty()),
                                Double.parseDouble(startFuelField.getText().replace(',', '.')),
                                Double.parseDouble(startMileageField.getText().replace(',', '.'))
                        );


                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.changeCar(car)) {

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();

                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    private void openAddCarWindow() {
        String windowTitle = "Додати авто";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField numberField = new TextField();
            TextField modelField = new TextField();
            TextField fuelTypeField = new TextField();
            TextField fuelUsageField = new TextField();
            TextField engineVolumeField = new TextField();
            DatePicker datePickerStart = new DatePicker();
            TextField startFuelField = new TextField();
            TextField startMileageField = new TextField();
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            datePickerStart.setValue(LocalDate.now());
            TextField startOrderNumberField = new TextField();

            grid.add(new Label("Номер:"), 0, 0);
            grid.add(numberField, 1, 0);
            grid.add(new Label("Модель:"), 0, 1);
            grid.add(modelField, 1, 1);
            grid.add(new Label("Тип палива:"), 0, 2);
            grid.add(fuelTypeField, 1, 2);
            grid.add(new Label("Використання палива:"), 0, 3);
            grid.add(fuelUsageField, 1, 3);
            grid.add(new Label("Об'єм двигуна:"), 0, 4);
            grid.add(engineVolumeField, 1, 4);
            grid.add(new Label("Початок експлуатації: дата:"), 0, 5);
            grid.add(datePickerStart, 1, 5);
            grid.add(new Label("Початок експлуатації: Номер наказу:"), 0, 6);
            grid.add(startOrderNumberField, 1, 6);
            grid.add(new Label("Початок експлуатації: паливо:"), 0, 7);
            grid.add(startFuelField, 1, 7);
            grid.add(new Label("Початок експлуатації: пробіг:"), 0, 8);
            grid.add(startMileageField, 1, 8);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(numberField.getText()) || isEmptyOrWhitespace(modelField.getText()) ||
                        isEmptyOrWhitespace(fuelTypeField.getText()) || isEmptyOrWhitespace(fuelUsageField.getText()) ||
                        isEmptyOrWhitespace(engineVolumeField.getText()) || isEmptyOrWhitespace(startOrderNumberField.getText()) || isEmptyOrWhitespace(startMileageField.getText()) || isEmptyOrWhitespace(startFuelField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        double fuelUsage = Double.parseDouble(fuelUsageField.getText().replace(',', '.'));
                        double engineVolume = Double.parseDouble(engineVolumeField.getText().replace(',', '.'));
                        double startFuel = Double.parseDouble(startFuelField.getText().replace(',', '.'));
                        double startMileage = Double.parseDouble(startMileageField.getText().replace(',', '.'));

                        _Car car = new _Car(
                                numberField.getText(),
                                modelField.getText(),
                                fuelTypeField.getText(),
                                fuelUsage,
                                engineVolume,
                                datePickerStart.getValue(),
                                startOrderNumberField.getText(),
                                startFuel,
                                startMileage
                        );
                        car.setValid(true);

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.addCar(car)) {

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();

                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    //----------------------------------

    private void openReportsHandbookWindow() {
        String windowTitle = "Довідник: звіти про виконання завдання";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            reports = FXCollections.observableArrayList(dbManager.getReports());

            Button addButton = new Button("Додати звіт");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> {
                openAddReportWindow(-1);
            });

            Button editButton = new Button("Редагувати звіт");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[3] + "\\");
            });


            Button openFileButton = new Button("Відкрити звіт");
            openFileButton.setGraphic(IconsManager.getFileIcon());
            openFileButton.setDisable(true);

            Button deleteButton = new Button("Позначити звіт на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());
            updateButton.setOnAction(e->{
                reports.clear();
                reports.addAll(dbManager.getReports());
            });

            openFileButton.getStyleClass().add("grey-button");
            deleteButton.getStyleClass().add("red-button");
            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");

            TableView<_Report> tableView = new TableView<>();
            tableView.setItems(reports);
            TableColumn<_Report, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_Report, String> orderIdCol = new TableColumn<>("ID наказу");
            orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

            TableColumn<_Report, String> orderNumberCol = new TableColumn<>("№ наказу");
            orderNumberCol.setCellValueFactory(cellData -> {
                String order = dbManager.getOrderNumber(cellData.getValue().getOrderId());
                return new SimpleStringProperty(order);
            });

            TableColumn<_Report, LocalDate> startDateCol = new TableColumn<>("Дата звіту");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Report, String> workerCol = new TableColumn<>("Працівник");
            workerCol.setCellValueFactory(cellData -> {
                String worker =  dbManager.getOrderWorkerName(cellData.getValue().getOrderId());
                return new SimpleStringProperty(worker);
            });

            TableColumn<_Report, String> positionCol = new TableColumn<>("Посада");
            positionCol.setCellValueFactory(cellData -> {
                String pos =  dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(cellData.getValue().getOrderId()));
                return new SimpleStringProperty(pos);
            });

            TableColumn<_Report, String> goalCol = new TableColumn<>("Мета");
            goalCol.setCellValueFactory(cellData -> {
                String goal = dbManager.getOrderGoal(cellData.getValue().getOrderId());
                return new SimpleStringProperty(goal);
            });

            TableColumn<_Report, String> headCol = new TableColumn<>("Керівник");
            headCol.setCellValueFactory(cellData -> {
                String head = dbManager.getOrderHead(cellData.getValue().getOrderId());
                return new SimpleStringProperty(head);
            });


            TableColumn<_Report, String> commentsCol = new TableColumn<>("Додатковий коментар");
            commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));

            commentsCol.setCellValueFactory(cellData -> {
                String comments = cellData.getValue().getComments();
                if(comments.length()>= 19) {
                    comments = comments.substring(0, 19) + "...";
                }
                return new SimpleStringProperty(comments);
            });

            // Активуємо кнопку "Редагувати авто" при виборі рядка
            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean isItemSelected = newSelection != null;
                editButton.setDisable(!isItemSelected);
                openFileButton.setDisable(!isItemSelected);
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));
            });

            editButton.setOnAction(e -> {
                _Report rep = tableView.getSelectionModel().getSelectedItem();
                if (rep != null) {
                    openReportEditWindow(rep);
                   }
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, openFileButton, openFolderButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll(orderNumberCol, startDateCol, workerCol, positionCol,
                    goalCol, headCol, commentsCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            tableView.scrollTo(tableView.getItems().size() - 1);
            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити звіт");
                deleteButton.setOnAction(e->{
                    _Report rep = tableView.getSelectionModel().getSelectedItem();
                    if (rep != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteReport(rep);
                                }
                        });

                    }
                });
            }

            openFileButton.setOnAction(e -> {
                _Report selectedReport = tableView.getSelectionModel().getSelectedItem();
                if (selectedReport != null) {
                    dM.createReportDocument(dbManager, selectedReport);
                }
            });


            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private void openReportEditWindow(_Report selectedReport) {
        String windowTitle = "Редагувати звіт";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField orderNumberField = new TextField(dbManager.getOrderNumber(selectedReport.getOrderId()));
            DatePicker datePicker = new DatePicker(selectedReport.getDate());
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField workerField = new TextField(dbManager.getOrderWorkerName(selectedReport.getOrderId()));
            TextField positionField = new TextField(dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(selectedReport.getOrderId())));
            TextField goalField = new TextField(dbManager.getOrderGoal(selectedReport.getOrderId()));
            TextField headField = new TextField(dbManager.getOrderHead(selectedReport.getOrderId()));
            TextArea  commentsField = new TextArea (selectedReport.getComments());
            commentsField.setPrefRowCount(3); // Задати кількість рядків
            commentsField.setWrapText(true);  // Дозволити перенесення тексту

            orderNumberField.setDisable(true);
            datePicker.setDisable(true);
            workerField.setDisable(true);
            positionField.setDisable(true);
            goalField.setDisable(true);
            headField.setDisable(true);
            commentsField.setDisable(false);

            grid.add(new Label("№ наказу:"), 0, 0);
            grid.add(orderNumberField, 1, 0);
            grid.add(new Label("Дата звіту:"), 0, 1);
            grid.add(datePicker, 1, 1);
            grid.add(new Label("Працівник:"), 0, 2);
            grid.add(workerField, 1, 2);
            grid.add(new Label("Посада:"), 0, 3);
            grid.add(positionField, 1, 3);
            grid.add(new Label("Мета:"), 0, 4);
            grid.add(goalField, 1, 4);
            grid.add(new Label("Керівник:"), 0, 5);
            grid.add(headField, 1, 5);
            grid.add(new Label("Додатковий коментар:"), 0, 6);
            grid.add(commentsField, 1, 6);

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                selectedReport.setComments(commentsField.getText());
                Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати звіт");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if(dbManager.changeReport(selectedReport)) {
                            workspace.getChildren().remove(internalWindow);
                            openWindows.remove(windowTitle);
                            updateNavigationBar();
                        }
                    }
                });
            });
        }
    }

    private void openAddReportWindow(int idOrder) {
        String windowTitle = "Додати звіт";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {

            List<_Order> validOrders = dbManager.getOpenOrders();
            Map<String, Integer> ordersT = new HashMap<>();

            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            ComboBox<String> order = new ComboBox();
            DatePicker datePicker = new DatePicker(LocalDate.now());
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField workerField = new TextField();
            TextField positionField = new TextField();
            TextField goalField = new TextField();
            TextField headField = new TextField();
            TextArea  commentsField = new TextArea ();
            commentsField.setPrefRowCount(3);
            commentsField.setWrapText(true);

            for(_Order d  : validOrders) {
                ordersT.put(d.getOrderNumber(), d.getIdOrder());
                order.getItems().add(d.getOrderNumber());
            }

            if(idOrder != -1) {
                order.setValue(dbManager.getOrderNumber(idOrder));
                workerField.setText(dbManager.getOrderWorkerName(idOrder));
                positionField.setText(dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(idOrder)));
                goalField.setText(dbManager.getOrderGoal(idOrder));
                headField.setText(dbManager.getOrderHead(idOrder));
            }

            order.setOnAction(e->{
                String slectedOrder = order.getValue();
                if(slectedOrder != null) {
                    int selectedORderID = ordersT.get(slectedOrder);
                    workerField.setText(dbManager.getOrderWorkerName(selectedORderID));
                    positionField.setText(dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(selectedORderID)));
                    goalField.setText(dbManager.getOrderGoal(selectedORderID));
                    headField.setText(dbManager.getOrderHead(selectedORderID));
                }
            });

            workerField.setDisable(true);
            positionField.setDisable(true);
            goalField.setDisable(true);
            headField.setDisable(true);
            commentsField.setDisable(false);

            grid.add(new Label("№ наказу:"), 0, 0);
            grid.add(order, 1, 0);
            grid.add(new Label("Дата звіту:"), 0, 1);
            grid.add(datePicker, 1, 1);
            grid.add(new Label("Працівник:"), 0, 2);
            grid.add(workerField, 1, 2);
            grid.add(new Label("Посада:"), 0, 3);
            grid.add(positionField, 1, 3);
            grid.add(new Label("Мета:"), 0, 4);
            grid.add(goalField, 1, 4);
            grid.add(new Label("Керівник:"), 0, 5);
            grid.add(headField, 1, 5);
            grid.add(new Label("Додатковий коментар:"), 0, 6);
            grid.add(commentsField, 1, 6);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( order.getValue() == null) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати звіт");
                    confirmationAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            _Report rep = new _Report(ordersT.get(order.getValue()), commentsField.getText(), datePicker.getValue());
                            if(dbManager.addReport(rep)) {
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        }
                    });
                }
            });
        }
    }

    //----------------------------------

    private void openWorkersHandbookWindow() {


        String windowTitle = "Довідник: працівники";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            workers = FXCollections.observableArrayList(dbManager.getWorkers());

            ComboBox<String> selectionModel = new ComboBox<>();
            Button createFileButton = new Button("Зберегти довідник");
            createFileButton.setGraphic(IconsManager.getFileIcon());
            createFileButton.getStyleClass().add("grey-button");
            createFileButton.setOnAction(e -> {
                dM.createWorkersHandbook(dbManager, workers);
            });

            selectionModel.getItems().addAll("актуальні", "неактуальні", "");
            selectionModel.setValue("");

            selectionModel.setOnAction(event -> {
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    workers.clear();
                    workers.addAll(dbManager.getValidWorkers());
                } else if(selected.equals("неактуальні")) {
                    workers.clear();
                    workers.addAll(dbManager.getUnValidWorkers());
                } else {
                    workers.clear();
                    workers.addAll(dbManager.getWorkers());
                }
            });

            Button addButton = new Button("Додати працівника");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> {
                openAddWorkerWindow();
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    workers.clear();
                    workers.addAll(dbManager.getValidWorkers());
                } else if(selected.equals("неактуальні")) {
                    workers.clear();
                    workers.addAll(dbManager.getUnValidWorkers());
                } else {
                    workers.clear();
                    workers.addAll(dbManager.getWorkers());
                }
            });

            Button editButton = new Button("Редагувати працівника");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(dM.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + dM.getFolders()[1] + "\\");
            });


            Button removeButton = new Button("Звільнити працівника");
            removeButton.setGraphic(IconsManager.getCrossIcon());
            removeButton.setDisable(true);

            Button deleteButton = new Button("Позначити працівника на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);
            deleteButton.getStyleClass().add("red-button");

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());
            updateButton.setOnAction(e->{
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    workers.clear();
                    workers.addAll(dbManager.getValidWorkers());
                } else if(selected.equals("неактуальні")) {
                    workers.clear();
                    workers.addAll(dbManager.getUnValidWorkers());
                } else {
                    workers.clear();
                    workers.addAll(dbManager.getWorkers());
                }
            });

            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");
            removeButton.getStyleClass().add("red-button");


            TableView<_Worker> tableView = new TableView<>();
            ScrollPane scrollPane = new ScrollPane(tableView);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            tableView.prefWidthProperty().bind(scrollPane.widthProperty());

            tableView.setItems(workers);
            TableColumn<_Worker, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_Worker, String> nameCol = new TableColumn<>("ПІБ");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("nameN"));

            TableColumn<_Worker, String> positionCol = new TableColumn<>("Посада");
            positionCol.setCellValueFactory(new PropertyValueFactory<>("positionN"));

            TableColumn<_Worker, String> licenceCol = new TableColumn<>("№ водійського посвідчення");
            licenceCol.setCellValueFactory(new PropertyValueFactory<>("drivingLicense"));

            TableColumn<_Worker, LocalDate> startDateCol = new TableColumn<>("Дата працевлаштування");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            startDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Worker, String> startOrderCol = new TableColumn<>("Номер наказу \nпрацевлаштування");
            startOrderCol.setCellValueFactory(new PropertyValueFactory<>("startOrderNumber"));

            TableColumn<_Worker, LocalDate> endDateCol = new TableColumn<>("Дата звільнення");
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            endDateCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(dateFormatter));
                    }
                }
            });

            TableColumn<_Worker, String> endOrderCol = new TableColumn<>("Номер наказу \nзвільнення");
            endOrderCol.setCellValueFactory(new PropertyValueFactory<>("endOrderNumber"));

            TableColumn<_Worker, String> validCol = new TableColumn<>("Актуальність");
            validCol.setCellValueFactory(cellData -> {
                boolean valid = cellData.getValue().isValid();
                return new SimpleStringProperty(valid ? "Дійсний" : "Недійсний");
            });

            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean isItemSelected = newSelection != null;
                editButton.setDisable(!isItemSelected);
                removeButton.setDisable(!isItemSelected || !newSelection.isValid());
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));

            });

            editButton.setOnAction(e -> {
                _Worker selectedWorker = tableView.getSelectionModel().getSelectedItem();
                if (selectedWorker != null) {
                    openWorkerEditWindow(selectedWorker);
                    String selected = selectionModel.getValue();
                    if(selected.equals("актуальні")) {
                        workers.clear();
                        workers.addAll(dbManager.getValidWorkers());
                    } else if(selected.equals("неактуальні")) {
                        workers.clear();
                        workers.addAll(dbManager.getUnValidWorkers());
                    } else {
                        workers.clear();
                        workers.addAll(dbManager.getWorkers());
                    }
                }
            });

            removeButton.setOnAction(e -> {
                _Worker selectedWorker = tableView.getSelectionModel().getSelectedItem();
                if (selectedWorker != null) {
                    openWorkerRemoveWindow(selectedWorker);
                    String selected = selectionModel.getValue();
                    if(selected.equals("актуальні")) {
                        workers.clear();
                        workers.addAll(dbManager.getValidWorkers());
                    } else if(selected.equals("неактуальні")) {
                        workers.clear();
                        workers.addAll(dbManager.getUnValidWorkers());
                    } else {
                        workers.clear();
                        workers.addAll(dbManager.getWorkers());
                    }
                }
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, new Label("Фільтрувати актуальність"), selectionModel, createFileButton, openFolderButton,removeButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll( nameCol,positionCol,licenceCol, startDateCol, startOrderCol, endDateCol, endOrderCol, validCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);

            // Adding a context menu to each row
            tableView.setRowFactory(tv -> {
                TableRow<_Worker> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _Worker selectedWorker = row.getItem();
                    if (selectedWorker != null) {
                        openWorkerEditWindow(selectedWorker);
                    }
                });

                // Пункт "Видалити", додаватиметься лише для актуальних авто
                MenuItem removeItem = new MenuItem("Видалити");
                removeItem.setOnAction(event -> {
                    _Worker selectedWorker = row.getItem();
                    if (selectedWorker != null) {
                        openWorkerRemoveWindow(selectedWorker);
                    }
                });

                // Оновлення контекстного меню залежно від значення valid
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    rowMenu.getItems().clear(); // Очищуємо попередні пункти
                    if (newItem != null) {
                        rowMenu.getItems().add(editItem); // Додаємо пункт "Редагувати"
                        if (newItem.isValid()) {          // Перевіряємо поле valid
                            rowMenu.getItems().add(removeItem); // Додаємо "Видалити", якщо valid == true
                        }
                    }
                });

                // Прив'язка контекстного меню до рядка лише, коли він не порожній
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));

                return row;
            });
            tableView.scrollTo(tableView.getItems().size() - 1);
            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити працівника");
                deleteButton.setOnAction(e->{
                    _Worker selectedWorker = tableView.getSelectionModel().getSelectedItem();
                    if (selectedWorker != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteWorker(selectedWorker);
                                String selected = selectionModel.getValue();
                                if(selected.equals("актуальні")) {
                                    workers.clear();
                                    workers.addAll(dbManager.getValidWorkers());
                                } else if(selected.equals("неактуальні")) {
                                    workers.clear();
                                    workers.addAll(dbManager.getUnValidWorkers());
                                } else {
                                    workers.clear();
                                    workers.addAll(dbManager.getWorkers());
                                }
                            }
                        });

                    }
                });
            }


            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            openInternalWindow(table, windowTitle);
        }
    }

    private void openWorkerRemoveWindow(_Worker selectedWorker) {
        String windowTitle = "Звільнити працівника";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);


            DatePicker datePickerEnd = new DatePicker();
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField endOrderNumberField = new TextField();
            datePickerEnd.setValue(LocalDate.now());

            grid.add(new Label("Дата звільнення:"), 0, 0);
            grid.add(datePickerEnd, 1, 0);
            grid.add(new Label("Номер наказу звільнення:"), 0, 1);
            grid.add(endOrderNumberField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow =  openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e -> {
                try {
                    selectedWorker.setEndDate(datePickerEnd.getValue());
                    selectedWorker.setEndOrderNumber(endOrderNumberField.getText());

                    if(selectedWorker.getEndDate() == null || selectedWorker.getEndOrderNumber() == ""){
                        Alert alert = Alerts.ErrorAlert("Введіть дані", "Не введено номер наказу або дату закінчення");
                        alert.showAndWait();
                    }
                    else {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити працівника");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if (dbManager.removeWorker(selectedWorker)) {
                                    workers.clear();
                                    workers.addAll(dbManager.getWorkers());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();
                                }
                            }
                        });
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Please enter valid numbers");
                }
            });
        }
    }

    private void openAddWorkerWindow() {
        String windowTitle = "Додати працівника";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameNField = new TextField();
            TextField nameRField = new TextField();

            List<_Position> positions = dbManager.getPositions();

            ComboBox<String> position = new ComboBox<>();
            Map<String, Integer> positionsT = new HashMap<>();
            for(_Position p : positions) {
                positionsT.put(p.getNameN(), p.getId());
                position.getItems().add(p.getNameN());
            }

            TextField licenceField = new TextField();DatePicker datePickerStart = new DatePicker();
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            datePickerStart.setValue(LocalDate.now());
            TextField startOrderNumberField = new TextField();

            grid.add(new Label("ПІБ(називний відмінок):"), 0, 0);
            grid.add(nameNField, 1, 0);
            grid.add(new Label("ПІБ(родовий відмінок):"), 0, 1);
            grid.add(nameRField, 1, 1);
            grid.add(new Label("Посада:"), 0, 2);
            grid.add(position, 1, 2);
            grid.add(new Label("Водійське посвідчення:"), 0, 3);
            grid.add(licenceField, 1, 3);
            grid.add(new Label("Дата працевлаштування:"), 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(new Label("Номер наказу працевлаштування:"), 0, 5);
            grid.add(startOrderNumberField, 1, 5);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);


            StackPane internalWindow =  openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if (isEmptyOrWhitespace(nameNField.getText()) || isEmptyOrWhitespace(nameRField.getText()) ||
                        isEmptyOrWhitespace(licenceField.getText()) || datePickerStart.getValue() == null ||
                        isEmptyOrWhitespace(startOrderNumberField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Worker worker = new _Worker(
                                nameNField.getText(),
                                nameRField.getText(),
                                positionsT.get(position.getValue()),
                                licenceField.getText(),
                                datePickerStart.getValue(),
                                startOrderNumberField.getText()
                        );

                        worker.setPositionN(dbManager.getPositionNameN(positionsT.get(position.getValue())));
                        worker.setPositionR(dbManager.getPositionNameR(positionsT.get(position.getValue())));

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати працівника" );
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.addWorker(worker)) {
                                    workers.clear();
                                    workers.addAll(dbManager.getWorkers());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    private void openWorkerEditWindow(_Worker selectedWorker) {
        String windowTitle = "Редагувати працівника";
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameNField = new TextField(selectedWorker.getNameN());
            TextField nameRField = new TextField(selectedWorker.getNameR());

            List<_Position> positions = dbManager.getPositions();

            ComboBox<String> position = new ComboBox<>();
            Map<String, Integer> positionsT = new HashMap<>();
            for(_Position p : positions) {
                positionsT.put(p.getNameN(), p.getId());
                position.getItems().add(p.getNameN());
            }

            position.setValue(selectedWorker.getPositionN());

            TextField licenceField = new TextField(selectedWorker.getDrivingLicense());
            TextField startOrderNumberField = new TextField(selectedWorker.getStartOrderNumber());
            TextField endOrderNumberField = new TextField(selectedWorker.getEndOrderNumber());
            DatePicker datePickerStart = new DatePicker(selectedWorker.getStartDate());
            datePickerStart.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            DatePicker datePickerEnd = new DatePicker();
            datePickerEnd.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            grid.add(new Label("ПІБ(називний відмінок):"), 0, 0);
            grid.add(nameNField, 1, 0);
            grid.add(new Label("ПІБ(родовий відмінок):"), 0, 1);
            grid.add(nameRField, 1, 1);
            grid.add(new Label("Посада:"), 0, 2);
            grid.add(position, 1, 2);
            grid.add(new Label("Водійське посвідчення:"), 0, 3);
            grid.add(licenceField, 1, 3);
            grid.add(new Label("Дата працевлаштування:"), 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(new Label("Номер наказу працевлаштування:"), 0, 5);
            grid.add(startOrderNumberField, 1, 5);
            grid.add(new Label("Дата звільнення:"), 0, 6);
            grid.add(datePickerEnd, 1, 6);
            grid.add(new Label("Номер наказу звільнення:"), 0, 7);
            grid.add(endOrderNumberField, 1, 7);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);


            StackPane internalWindow =  openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( (datePickerEnd.getValue() != null && isEmptyOrWhitespace(endOrderNumberField.getText())) ||
                        (datePickerEnd.getValue() == null && !isEmptyOrWhitespace(endOrderNumberField.getText()))
                        || isEmptyOrWhitespace(nameNField.getText()) || isEmptyOrWhitespace(nameRField.getText()) ||
                        isEmptyOrWhitespace(licenceField.getText()) || datePickerStart.getValue() == null ||
                        isEmptyOrWhitespace(startOrderNumberField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Worker worker = new _Worker(
                                selectedWorker.getId(),
                                nameNField.getText(),
                                nameRField.getText(),
                                positionsT.get(position.getValue()),
                                licenceField.getText(),
                                datePickerStart.getValue(),
                                startOrderNumberField.getText()
                        );

                        worker.setPositionN(dbManager.getPositionNameN(positionsT.get(position.getValue())));
                        worker.setPositionR(dbManager.getPositionNameR(positionsT.get(position.getValue())));

                        if(datePickerEnd.getValue() != null) {
                            worker.setEndDate(datePickerEnd.getValue());
                            worker.setEndOrderNumber(endOrderNumberField.getText());
                            worker.setValid(false);
                        }


                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати працівника");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.changeWorker(worker)) {
                                    workers.clear();
                                    workers.addAll(dbManager.getWorkers());

                                    workspace.getChildren().remove(internalWindow);
                                    openWindows.remove(windowTitle);
                                    updateNavigationBar();

                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }

    //----------------------------------

    private StackPane openInternalWindow(VBox content, String windowTitle) {
        windowCount++;

        StackPane internalWindow = new StackPane();
        internalWindow.getStyleClass().add("internal-window");



        workspace.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (maximizedWindow == internalWindow) {
                internalWindow.setPrefWidth(newVal.doubleValue());
            }
        });

        workspace.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (maximizedWindow == internalWindow) {
                internalWindow.setPrefHeight(newVal.doubleValue());
            }
        });

        // Create Header Bar
        Rectangle header = new Rectangle(300, 30, Color.LIGHTGRAY);
        HBox headerBar = new HBox();
        headerBar.setPrefSize(300, 30);
        headerBar.getStyleClass().add("header");

        Button minimizeButton = new Button();
        minimizeButton.setGraphic(IconsManager.getHideWindowIcon());
        Button maximizeButton = new Button();
        maximizeButton.setGraphic(IconsManager.getMaxWindowIcon());
        Button closeButton = new Button();
        closeButton.setGraphic(IconsManager.getCloseWindowIcon());
        minimizeButton.setMaxHeight(25);
        minimizeButton.setMinHeight(25);
        maximizeButton.setMaxHeight(25);
        maximizeButton.setMinHeight(25);
        closeButton.setMaxHeight(25);
        closeButton.setMinHeight(25);
        double[] previousSize = new double[4];

        closeButton.setOnAction(e -> {
            workspace.getChildren().remove(internalWindow);
            openWindows.remove(windowTitle);
            updateNavigationBar();
        });

        minimizeButton.setOnAction(e -> {
            internalWindow.setVisible(false);
        });

        maximizeButton.setOnAction(e -> {
            internalWindow.toFront();
            if (maximizedWindow != null && maximizedWindow != internalWindow) {
                maximizedWindow.setVisible(true);
            }
            if (internalWindow.getPrefWidth() == workspace.getWidth() && internalWindow.getPrefHeight() == workspace.getHeight()) {
                internalWindow.setPrefSize(previousSize[0], previousSize[1]);
                internalWindow.setLayoutX(previousSize[2]);
                internalWindow.setLayoutY(previousSize[3]);
                maximizedWindow = null;
                internalWindow.setVisible(true);
            } else {
                maximizedWindow = internalWindow;
                previousSize[0] = internalWindow.getPrefWidth();
                previousSize[1] = internalWindow.getPrefHeight();
                previousSize[2] = internalWindow.getLayoutX();
                previousSize[3] = internalWindow.getLayoutY();

                internalWindow.setLayoutX(0);
                internalWindow.setLayoutY(0);
                internalWindow.setPrefSize(workspace.getWidth(), workspace.getHeight());
                internalWindow.setVisible(true);
            }
        });

        openWindows.put(windowTitle, internalWindow);

        // Create a Label to display the window's title
        Label windowTitleLabel = new Label(windowTitle);
        windowTitleLabel.getStyleClass().add("window-title");

        HBox controlButtons = new HBox(minimizeButton, maximizeButton, closeButton);
        controlButtons.getStyleClass().add("control-buttons");
        controlButtons.setSpacing(5);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBar.getChildren().addAll(windowTitleLabel, spacer, controlButtons);

        headerBar.setOnMousePressed(event -> {
            internalWindow.setUserData(new double[]{event.getSceneX() - internalWindow.getLayoutX(),
                    event.getSceneY() - internalWindow.getLayoutY()});
            internalWindow.toFront();
        });
        headerBar.setOnMouseDragged(event -> {
            double[] offset = (double[]) internalWindow.getUserData();
            double newX = event.getSceneX() - offset[0];
            double newY = event.getSceneY() - offset[1];

            // Restrict the window's position to the workspace bounds
            double workspaceWidth = workspace.getWidth();
            double workspaceHeight = workspace.getHeight();
            double windowWidth = internalWindow.getWidth();
            double windowHeight = internalWindow.getHeight();

            // Prevent the window from going beyond the workspace
            newX = Math.max(0, Math.min(newX, workspaceWidth - windowWidth));
            newY = Math.max(0, Math.min(newY, workspaceHeight - windowHeight));

            internalWindow.setLayoutX(newX);
            internalWindow.setLayoutY(newY);
        });

        internalWindow.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();
            double width = internalWindow.getWidth();
            double height = internalWindow.getHeight();

            // Prevent resizing when the pointer is over the header bar
            if (y > headerBar.getHeight()) {
                if (x >= width - RESIZE_MARGIN && y >= height - RESIZE_MARGIN) {
                    internalWindow.setCursor(Cursor.SE_RESIZE);
                } else if (x >= width - RESIZE_MARGIN) {
                    internalWindow.setCursor(Cursor.E_RESIZE);
                } else if (y >= height - RESIZE_MARGIN) {
                    internalWindow.setCursor(Cursor.S_RESIZE);
                } else {
                    internalWindow.setCursor(Cursor.DEFAULT);
                }
            } else {
                internalWindow.setCursor(Cursor.DEFAULT);  // Default cursor over header bar
            }
        });

        internalWindow.setOnMousePressed(event -> {
            if (event.getY() > headerBar.getHeight()) {  // Prevent resizing logic near the header
                Cursor cursor = internalWindow.getCursor();
                if (cursor == Cursor.SE_RESIZE) {
                    internalWindow.setUserData(new double[]{event.getSceneX(), event.getSceneY(), internalWindow.getWidth(), internalWindow.getHeight()});
                } else if (cursor == Cursor.E_RESIZE) {
                    internalWindow.setUserData(new double[]{event.getSceneX(), internalWindow.getWidth()});
                } else if (cursor == Cursor.S_RESIZE) {
                    internalWindow.setUserData(new double[]{event.getSceneY(), internalWindow.getHeight()});
                }
            }
            internalWindow.toFront();
        });

        internalWindow.setOnMouseDragged(event -> {
            if (event.getY() > headerBar.getHeight()) {  // Prevent resizing near the header
                Cursor cursor = internalWindow.getCursor();
                double[] data = (double[]) internalWindow.getUserData();

                // Отримуємо розміри робочої області
                double workspaceWidth = workspace.getWidth();
                double workspaceHeight = workspace.getHeight();

                if (cursor == Cursor.SE_RESIZE) {
                    double deltaX = event.getSceneX() - data[0];
                    double deltaY = event.getSceneY() - data[1];

                    // Обмежуємо розміри вікна, щоб вони не виходили за межі робочої області
                    double newWidth = Math.max(100, data[2] + deltaX);
                    double newHeight = Math.max(100, data[3] + deltaY);

                    // Перевіряємо, щоб нові розміри не перевищували розміри робочої області
                    internalWindow.setPrefSize(
                            Math.min(newWidth, workspaceWidth - internalWindow.getLayoutX()),  // Ширина
                            Math.min(newHeight, workspaceHeight - internalWindow.getLayoutY())  // Висота
                    );
                } else if (cursor == Cursor.E_RESIZE) {
                    double deltaX = event.getSceneX() - data[0];
                    double newWidth = Math.max(100, data[1] + deltaX);

                    // Перевірка ширини
                    internalWindow.setPrefSize(
                            Math.min(newWidth, workspaceWidth - internalWindow.getLayoutX()),
                            internalWindow.getHeight()
                    );
                } else if (cursor == Cursor.S_RESIZE) {
                    double deltaY = event.getSceneY() - data[0];
                    double newHeight = Math.max(100, data[1] + deltaY);

                    // Перевірка висоти
                    internalWindow.setPrefSize(
                            internalWindow.getWidth(),
                            Math.min(newHeight, workspaceHeight - internalWindow.getLayoutY())
                    );
                }
            }
        });


        VBox fullLayout = new VBox(headerBar);

        content.setSpacing(10);
        content.setPadding(new Insets(10));
        fullLayout.getChildren().add(content);
        internalWindow.getChildren().add(fullLayout);

        workspace.getChildren().add(internalWindow);

        double xPosition = 0;
        double yPosition = 0;


        internalWindow.setLayoutX(xPosition);
        internalWindow.setLayoutY(yPosition);

        internalWindow.toFront();

        updateNavigationBar();

        return internalWindow;
    }

    private void updateNavigationBar() {
        navigationBar.getChildren().clear();
        for (String windowTitle : openWindows.keySet()) {
            Button windowButton = new Button(windowTitle);
            windowButton.setOnAction(e -> {
                StackPane window = openWindows.get(windowTitle);
                if (window != null) {
                    window.setVisible(true);
                    window.toFront();
                }
            });
            navigationBar.getChildren().add(windowButton);
        }
    }

    private void openFolder(String path) {
        File folder = new File(path);

        if (folder.exists() && folder.isDirectory()) {
            try {
                Desktop.getDesktop().open(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Папка не існує: " + path);
        }
    }

}
