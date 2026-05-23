package com.work.oblikpodorojlist.pages;

import com.work.oblikpodorojlist.util.*;
import com.work.oblikpodorojlist.model.*;
import com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars.CarsHandbookController;
import com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Positions.PositionsHandbookController;
import com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Workers.WorkersHandbookController;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists.ListsJournalController;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders.OrdersJournalController;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Reports.ReportsJournalController;
import com.work.oblikpodorojlist.pages.windowControllers.Registers.Fuel.FuelRegisterController;
import com.work.oblikpodorojlist.pages.windowControllers.Registers.Lists.ListsRegisterController;
import com.work.oblikpodorojlist.pages.windowControllers.Registers.Orders.OrdersRegisterController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.EventHandler;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.*;
import java.util.logging.Logger;

public class MainPage {
    private static MainPage instance;
    private Pane workspace;
    private HBox navigationBar;
    private int windowCount = 0;
    public Map<String, StackPane> openWindows = new HashMap<>();
    private static final int RESIZE_MARGIN = 5;
    private StackPane maximizedWindow = null;
    private DBUtil dbUtil;
    DocumentsUtil dM = DocumentsUtil.getInstance();
    private static final String LIGHT_THEME = "/css/styleWhite.css";
    private static final String DARK_THEME = "/css/styleDark.css";
    private static final Logger logger = LoggerUtil.getLogger();

    private MainPage(){}

    public static MainPage getInstance() {
        if(instance == null) {
            instance = new MainPage();
        }
        return instance;
    }

    private void switchTheme(Scene scene, String theme) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
    }

    private boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }

    public void StartMainPage(Stage primaryStage) {
        dbUtil = DBUtil.getInstance();
        dbUtil.deleteOldBackups();
        dbUtil.Migrate();
        dM.createFolders(dbUtil.getCompany());
        VBox root = new VBox();
        Scene scene = new Scene(root, 500, 400);

        String _companyName = dbUtil.getCompany();
        String _username = dbUtil.getUsername();

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
        lightTheme.setSelected(true);

        menuTheme.getItems().addAll(lightTheme, darkTheme);
        menuFile.getItems().addAll(menuTheme, changeUser, closeApp);
        menuBar.getMenus().add(menuFile);

        changeUser.setOnAction(event -> {
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {}});
            Alert al= AlertsUtil.ConfirmAlert("Створити резервну копію?","Підвердіть створення резервної копії");
            al.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    dbUtil.createBackup();
                    dbUtil.deleteOldBackups();
                }
            });
            CompanyPage cp = new CompanyPage();
            instance = null;
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
        reportJournal.setOnAction(e -> openReportsJournalWindow());
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

        if(dbUtil.getUsername().equals("root")) {
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
                Alert a = AlertsUtil.ConfirmAlert("Підтеврдіть операцію", "Видалити компанію");
                a.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dbUtil.deleteCompany(_companyName);
                        CompanyPage cp = new CompanyPage();
                        instance = null;
                        cp.start(primaryStage);
                    }
                });

            });

            MenuItem updateGuestUser = new MenuItem("Оновити гостьового користувача");
            updateGuestUser.setOnAction(e->{
                dbUtil.CreateGuestUser();
            });

            MenuItem createBackup = new MenuItem("Створити резервну копію");
            createBackup.setOnAction(e->{
                dbUtil.createBackup();
                dbUtil.deleteOldBackups();
            });

            MenuItem loadBackup = new MenuItem("Завантажити резервну копію");
            loadBackup.setOnAction(e->{
                dbUtil.loadBackup();
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
        primaryStage.setMaximized(true);
        primaryStage.setTitle(dbUtil.getCompanyInfo().getName() + ": " + _username);
        primaryStage.show();
        resizeWorkspace(primaryStage);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert al= AlertsUtil.ConfirmAlert("Створити резервну копію?","Підвердіть створення резервної копії");
                al.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dbUtil.createBackup();
                        dbUtil.deleteOldBackups();
                    }
                });
            }
        });

    }

    private void resizeWorkspace(Stage stage) {
        workspace.setPrefWidth(stage.getWidth());
        workspace.setPrefHeight(stage.getHeight() - navigationBar.getHeight() - 25);
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

            StackPane internalWindow = openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText())) {
                    Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Редагувати хост");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.setHost(nameField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані");
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

            StackPane internalWindow = openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText())) {
                    Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Додати користувача");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.createUser(nameField.getText(), passwordField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані");
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

            StackPane internalWindow = openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText()) || isEmptyOrWhitespace(addressField.getText()) || isEmptyOrWhitespace(codeField.getText()) || isEmptyOrWhitespace(ceoField.getText()) ||
                        isEmptyOrWhitespace(accountantField.getText()) || isEmptyOrWhitespace(typeFullField.getText()) || isEmptyOrWhitespace(typeShortField.getText())) {
                    Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Додати компанію");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.createCompany(nameField.getText(),addressField.getText(), codeField.getText(), ceoField.getText(), accountantField.getText(), typeFullField.getText(), typeShortField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані");
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

            _Company comm = dbUtil.getCompanyInfo();


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

            StackPane internalWindow = openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameField.getText()) || isEmptyOrWhitespace(addressField.getText()) || isEmptyOrWhitespace(ceoField.getText()) ||
                        isEmptyOrWhitespace(accountantField.getText()) || isEmptyOrWhitespace(typeFullField.getText()) || isEmptyOrWhitespace(typeShortField.getText())) {
                    Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Редагувати компанію");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.changeParametersCompany(nameField.getText(),addressField.getText(), Integer.parseInt(codeField.getText()), ceoField.getText(), accountantField.getText(), typeFullField.getText(), typeShortField.getText());
                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
                StartMainPage(primaryStage);
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
            addButton.setGraphic(IconsUtil.getPlusIcon());


            Button editPasswordButton = new Button("Редагувати пароль");
            editPasswordButton.setGraphic(IconsUtil.getPencilIcon());
            editPasswordButton.setDisable(true);


            Button removeButton = new Button("Видалити користувача");
            removeButton.setGraphic(IconsUtil.getRubbishIcon());
            removeButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsUtil.getUpdateIcon());


            addButton.getStyleClass().add("green-button");
            editPasswordButton.getStyleClass().add("yellow-button");
            removeButton.getStyleClass().add("green-button");


            ListView<String> tableView = new ListView<>();
            tableView.getItems().addAll(dbUtil.getUsers());


            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                editPasswordButton.setDisable(newSelection == null);
                removeButton.setDisable(newSelection == null );
            });

            addButton.setOnAction(e -> {
                openAddUserWindow();
                tableView.getItems().clear();
                tableView.getItems().addAll(dbUtil.getUsers());
            });

            updateButton.setOnAction(e->{
                tableView.getItems().clear();
                tableView.getItems().addAll(dbUtil.getUsers());
            });

            editPasswordButton.setOnAction(e -> {
                String selectedUser = tableView.getSelectionModel().getSelectedItem();
                if (selectedUser != null ) {
                    openEditPasswordWindow(selectedUser);
                    tableView.getItems().clear();
                    tableView.getItems().addAll(dbUtil.getUsers());
                }
            });

            removeButton.setOnAction(e -> {
                String selectedUser = tableView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити користувача");
                    confirmationAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            dbUtil.deleteUser(selectedUser);
                            tableView.getItems().clear();
                            tableView.getItems().addAll(dbUtil.getUsers());
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

            openInternalWindow(table, windowTitle, true);
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

            StackPane internalWindow = openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                    try {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Змінити пароль користувача");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.deleteUser(SelectedUser);
                                dbUtil.createUser(SelectedUser, passworField.getText());

                                workspace.getChildren().remove(internalWindow);
                                openWindows.remove(windowTitle);
                                updateNavigationBar();
                            }
                        });

                    } catch (NumberFormatException ex) {
                        Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
            });


        }
    }


    private void openOrdersRegister() {
        OrdersRegisterController controller = new OrdersRegisterController();
        controller.openWindow();
    }

    private void openListRegister() {
        ListsRegisterController controller = new ListsRegisterController();
        controller.openWindow();
    }

    private void openFuelRegister() {
        FuelRegisterController controller = new FuelRegisterController();
        controller.openWindow();
    }


    private void openListJournal() {
        ListsJournalController controller = new ListsJournalController();
        controller.openWindow();
    }



    private void openOrderJournal() {
        OrdersJournalController controller = new OrdersJournalController();
        controller.openWindow();
    }


    private void openPositionsHandbookWindow() {
        PositionsHandbookController controller = new PositionsHandbookController();
        controller.openWindow();
    }

    private void openСarsHandbookWindow() {
        CarsHandbookController controller = new CarsHandbookController();
        controller.openWindow();
    }

    private void openReportsJournalWindow() {
        ReportsJournalController controller = new ReportsJournalController();
        controller.openWindow();
    }


    private void openWorkersHandbookWindow() {
        WorkersHandbookController controller = new WorkersHandbookController();
        controller.openWindow();
    }


    public StackPane openInternalWindow(VBox content, String windowTitle, boolean full) {
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

        Rectangle header = new Rectangle(300, 30, Color.LIGHTGRAY);
        HBox headerBar = new HBox();
        headerBar.setPrefSize(300, 30);
        headerBar.getStyleClass().add("header");

        Button minimizeButton = new Button();
        minimizeButton.setGraphic(IconsUtil.getHideWindowIcon());
        Button maximizeButton = new Button();
        maximizeButton.setGraphic(IconsUtil.getMaxWindowIcon());
        Button closeButton = new Button();
        closeButton.setGraphic(IconsUtil.getCloseWindowIcon());
        minimizeButton.setMaxHeight(25);
        minimizeButton.setMinHeight(25);
        maximizeButton.setMaxHeight(25);
        maximizeButton.setMinHeight(25);
        closeButton.setMaxHeight(25);
        closeButton.setMinHeight(25);
        double[] previousSize = new double[4];

        closeButton.setOnAction(e -> {
            closeInternalWindow(windowTitle);
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

            double workspaceWidth = workspace.getWidth();
            double workspaceHeight = workspace.getHeight();
            double windowWidth = internalWindow.getWidth();
            double windowHeight = internalWindow.getHeight();

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
                internalWindow.setCursor(Cursor.DEFAULT);
            }
        });

        internalWindow.setOnMousePressed(event -> {
            if (event.getY() > headerBar.getHeight()) {
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
            if (event.getY() > headerBar.getHeight()) {
                Cursor cursor = internalWindow.getCursor();
                double[] data = (double[]) internalWindow.getUserData();

                double workspaceWidth = workspace.getWidth();
                double workspaceHeight = workspace.getHeight();

                if (cursor == Cursor.SE_RESIZE) {
                    double deltaX = event.getSceneX() - data[0];
                    double deltaY = event.getSceneY() - data[1];

                    double newWidth = Math.max(100, data[2] + deltaX);
                    double newHeight = Math.max(100, data[3] + deltaY);

                    internalWindow.setPrefSize(
                            Math.min(newWidth, workspaceWidth - internalWindow.getLayoutX()),
                            Math.min(newHeight, workspaceHeight - internalWindow.getLayoutY())
                    );
                } else if (cursor == Cursor.E_RESIZE) {
                    double deltaX = event.getSceneX() - data[0];
                    double newWidth = Math.max(100, data[1] + deltaX);

                    internalWindow.setPrefSize(
                            Math.min(newWidth, workspaceWidth - internalWindow.getLayoutX()),
                            internalWindow.getHeight()
                    );
                } else if (cursor == Cursor.S_RESIZE) {
                    double deltaY = event.getSceneY() - data[0];
                    double newHeight = Math.max(100, data[1] + deltaY);

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
        if(full) {
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

        }

        updateNavigationBar();

        return internalWindow;
    }

    public void closeInternalWindow(String windowTitle) {
        workspace.getChildren().remove(openWindows.get(windowTitle));
        openWindows.remove(windowTitle);
        updateNavigationBar();
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

    public boolean checkOpenWindow(String windowTitle) {
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(!openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
            return true;
        }
        return false;
    }


}
