package com.work.oblikpodorojlist.pages.windowControllers.Registers.Orders;

import com.work.oblikpodorojlist.utils.AlertsUtil;
import com.work.oblikpodorojlist.utils.DBUtil;
import com.work.oblikpodorojlist.utils.DocumentsUtil;
import com.work.oblikpodorojlist.utils.IconsUtil;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class OrdersRegisterController extends WindowController {
    private MainPage mainPage;
    private DBUtil dbUtil;
    private DocumentsUtil documentsUtil;
    ComboBox<String> workerField = new ComboBox<>();
    private OrdersPeriodController ordersPeriodController;
    private ObservableList<_Order> filteredOrders = FXCollections.observableArrayList();
    public DatePicker datePickerStart;
    public DatePicker datePickerEnd;

    private TableView<_Order> tableView;
    private Pagination pagination;
    private VBox tableContainer;

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
            documentsUtil = DocumentsUtil.getInstance();
            dbUtil = DBUtil.getInstance();
            ordersPeriodController = new OrdersPeriodController();
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            List<String> validWorkers = dbUtil.getUniqueWorkersNames();

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
            filterButton.setGraphic(IconsUtil.getFilterIcon());
            Button saveButton = new Button("Зберегти реєстр");
            saveButton.setGraphic(IconsUtil.getTikIcon());
            saveButton.setDisable(true);

            Button settingsButton = new Button();
            settingsButton.setGraphic(IconsUtil.getClockIcon());


            Button updateButton = new Button();
            updateButton.setDisable(true);
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsUtil.getUpdateIcon());
            updateButton.setOnAction(e->{
                updateValues();
            });

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsUtil.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsUtil.getDocsFolderPath() + "DocFiles\\"+ dbUtil.getCompany() + "\\" + documentsUtil.getFolders()[5] + "\\");
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
                    AlertsUtil.ErrorAlert("Помилка вводу", "Введіть дату початку").showAndWait();
                } else {
                    updateButton.setDisable(false);
                    updateValues();
                    saveButton.setDisable(false);
                }

            });

            saveButton.setOnAction(e -> {
                documentsUtil.createRegisterOrders(dbUtil, filteredOrders, workerField.getValue(), datePickerStart.getValue(), datePickerEnd.getValue());
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
            tableView = new TableView<>();
            tableView.setItems(filteredOrders);

            pagination = new Pagination(1, 0);
            pagination.setPageFactory(this::createPage);

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
                String Name = dbUtil.getWorkerName(true, cellData.getValue().getIdWorker());
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

            tableContainer = new VBox(tableView);
            VBox.setVgrow(tableContainer, Priority.ALWAYS);

            table.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case RIGHT:
                        if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1) {
                            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
                        }
                        break;
                    case LEFT:
                        if (pagination.getCurrentPageIndex() > 0) {
                            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
                        }
                        break;
                }
            });

            tableView.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case RIGHT:
                        if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1) {
                            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
                        }
                        break;
                    case LEFT:
                        if (pagination.getCurrentPageIndex() > 0) {
                            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
                        }
                        break;
                }
            });

            table.getChildren().addAll(buttonBox,tableContainer, pagination);

            mainPage.openInternalWindow(table, windowTitle, true);
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
                List<_Order> newOrders = dbUtil.getOrdersFiltered(tempWorkerName, datePickerStart.getValue(), datePickerEnd.getValue());
                newOrders.sort(Comparator.comparing(_Order::getOrderDate));
                newOrders.sort((o1, o2) -> {
                    int num1 = extractNumber(o1.getOrderNumber());
                    int num2 = extractNumber(o2.getOrderNumber());
                    return Integer.compare(num1, num2);
                });

                Platform.runLater(() -> {
                    filteredOrders.setAll(newOrders);
                    int pageCount = (int) Math.ceil((double) filteredOrders.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);
                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, filteredOrders.size());
                    tableView.setItems(FXCollections.observableArrayList(filteredOrders.subList(fromIndex, toIndex)));
                    tableContainer.getChildren().setAll(tableView);
                    moveTableDown(tableView);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, filteredOrders.size());

        if (fromIndex > toIndex) {
            tableView.setItems(FXCollections.observableArrayList());
        } else {
            tableView.setItems(FXCollections.observableArrayList(filteredOrders.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}
