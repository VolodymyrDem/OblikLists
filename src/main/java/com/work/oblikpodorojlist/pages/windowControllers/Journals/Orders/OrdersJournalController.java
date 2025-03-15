package com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model._Car;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars.AddCarController;
import com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars.CarsHandbookController;
import com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars.EditCarController;
import com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars.RemoveCarController;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class OrdersJournalController extends WindowController {
    private ObservableList<_Order> orders = FXCollections.observableArrayList();
    private MainPage mainPage;
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    private AddOrderController addOrderController;
    private EditOrderController editOrderController;

    public OrdersJournalController(){}


    public void openWindow(){
        String windowTitle = "Журнал: накази";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            addOrderController = new AddOrderController();
            editOrderController = new EditOrderController();
            documentsManager = DocumentsManager.getInstance();
            dbManager = DBManager.getInstance();

            Button addButton = new Button("Додати наказ");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.getStyleClass().add("green-button");

            Button editButton = new Button("Редагувати наказ");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.setDisable(true);
            editButton.getStyleClass().add("yellow-button");

            Button copyButton = new Button("Копіювати наказ");
            copyButton.setGraphic(IconsManager.getCopyIcon());
            copyButton.setDisable(true);
            copyButton.getStyleClass().add("grey-button");

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");

            Button openFileButton = new Button("Відкрити наказ");
            openFileButton.setGraphic(IconsManager.getFileIcon());
            openFileButton.setDisable(true);
            openFileButton.getStyleClass().add("grey-button");

            Button deleteButton = new Button("Позначити наказ на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);
            deleteButton.getStyleClass().add("red-button");

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());


            TableView<_Order> tableView = new TableView<>();

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



            updateValues();
            tableView.setItems(orders);

            tableView.getSortOrder().add(orderNumberCol);
            orderNumberCol.setSortType(TableColumn.SortType.ASCENDING);
            tableView.sort();

            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                editButton.setDisable(newSelection == null || (!dbManager.isOrderModifiable(newSelection.getIdOrder()) && !dbManager.getUsername().equals("root")));
                copyButton.setDisable(newSelection == null);
                openFileButton.setDisable(newSelection == null);
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));
            });

            tableView.scrollTo(tableView.getItems().size()-1);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            tableView.setRowFactory(tv -> {
                TableRow<_Order> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _Order selectedOrder = row.getItem();
                    if (selectedOrder != null && dbManager.isOrderModifiable(selectedOrder.getIdOrder())) {
                        editOrderController.openWindow(selectedOrder);
                    }
                });

                MenuItem openItem = new MenuItem("Відкрити документ");
                openItem.setOnAction(event -> {
                    _Order selectedOrder = row.getItem();
                    if (selectedOrder != null) {
                        documentsManager.createOrderDocument(dbManager, selectedOrder);
                    }
                });

                // Double-click event handler
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        _Order selectedOrder = row.getItem();
                        if (selectedOrder != null) {
                            documentsManager.createOrderDocument(dbManager, selectedOrder);
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

            addButton.setOnAction(e -> {
                addOrderController.openWindow(null);
            });

            openFolderButton.setOnAction(e -> {
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[2] + "\\");
            });

            openFileButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    documentsManager.createOrderDocument(dbManager, selectedOrder);
                }
            });

            updateButton.setOnAction(e->{
                updateValues();
                tableView.sort();
            });

            editButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    editOrderController.openWindow(selectedOrder);
                }
            });

            copyButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    addOrderController.openWindow(selectedOrder);
                }
            });

            HBox buttonBox = new HBox(10, updateButton, addButton, copyButton, editButton, openFileButton, openFolderButton ,deleteButton);
            buttonBox.getStyleClass().add("buttonBox");
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити Наказ");
                deleteButton.setOnAction(e->{
                    _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити наказ");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteOrder(selectedOrder);
                                updateValues();
                                tableView.sort();
                            }
                        });

                    }
                });
            }


            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            mainPage.openInternalWindow(table, windowTitle);
        }
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Order> newOrders;

                newOrders = dbManager.getOrders();

                Platform.runLater(() -> {
                    orders.setAll(newOrders); // Оновлення UI у JavaFX потоці
                });
                return null;
            }
        };
        new Thread(task).start();
    }
}
