package com.work.oblikpodorojlist.pages.windowControllers.Registers.Orders;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import eu.hansolo.tilesfx.colors.Wan;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class OrdersRegisterController extends WindowController {
    private MainPage mainPage;
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    ComboBox<String> workerField = new ComboBox<>();
    private OrdersPeriodController ordersPeriodController;
    private ObservableList<_Order> filteredOrders = FXCollections.observableArrayList();
    public DatePicker datePickerStart;
    public DatePicker datePickerEnd;

    public OrdersRegisterController(){
        datePickerStart = new DatePicker();
        datePickerEnd = new DatePicker();
    }

    public void openWindow(){
        String windowTitle = "Реєстр: накази на відрядження";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            documentsManager = DocumentsManager.getInstance();
            dbManager = DBManager.getInstance();
            ordersPeriodController = new OrdersPeriodController();
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            List<String> validWorkers = dbManager.getUniqueWorkersNames();

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
                updateValues();
            });

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[5] + "\\");
            });


            settingsButton.setOnAction(event -> {
                updateButton.setDisable(true);
                ordersPeriodController.openWindow(this);
            });

            workerLabel.getStyleClass().add("filter-label");
            timeLabel.getStyleClass().add("filter-label");
            timeLabel2.getStyleClass().add("filter-label");
            filterButton.getStyleClass().add("green-button");
            saveButton.getStyleClass().add("green-button");

            filterButton.setOnAction(e -> {
                if (datePickerStart.getValue() == null) {
                    updateButton.setDisable(true);
                    Alerts.ErrorAlert("Помилка вводу", "Введіть дату початку").showAndWait();
                } else {
                    updateButton.setDisable(false);
                    updateValues();
                    saveButton.setDisable(false);
                }

            });

            saveButton.setOnAction(e -> {
                documentsManager.createRegisterOrders(dbManager, filteredOrders, workerField.getValue(), datePickerStart.getValue(), datePickerEnd.getValue());
            });

            for(String d  : validWorkers) {
                workerField.getItems().add(d);
            }
            workerField.getItems().add(null);
            workerField.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerStart.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerEnd.setOnAction(e->{
                updateButton.setDisable(true);
            });

            HBox buttonBox = new HBox(10, timeLabel, datePickerStart, timeLabel2, datePickerEnd, settingsButton,  workerLabel, workerField, filterButton, updateButton, saveButton, openFolderButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            updateValues();
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

            mainPage.openInternalWindow(table, windowTitle);
        }
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if(datePickerEnd.getValue() == null) {
                    datePickerEnd.setValue(LocalDate.now());
                }
                String tempWorkerName = (workerField.getValue() == null)?"-1":workerField.getValue();
                List<_Order> newLists = dbManager.getOrdersFiltered(tempWorkerName, datePickerStart.getValue(), datePickerEnd.getValue());
                newLists.sort(Comparator.comparing(_Order::getOrderDate));
                Platform.runLater(() -> {
                    filteredOrders.setAll(newLists);
                });
                return null;
            }
        };
        new Thread(task).start();
    }
}
