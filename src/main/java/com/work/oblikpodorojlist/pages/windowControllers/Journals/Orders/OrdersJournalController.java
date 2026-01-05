package com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders;

import com.work.oblikpodorojlist.utils.*;

import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrdersJournalController extends WindowController {
    private ObservableList<_Order> orders = FXCollections.observableArrayList();
    private MainPage mainPage;
    private DBUtil dbUtil;
    private DocumentsUtil documentsUtil;
    private AddOrderController addOrderController;
    private EditOrderController editOrderController;
    private TableView<_Order> tableView;
    private Pagination pagination;
    private PaginationUtil paginationUtil;
    private VBox tableContainer;
    private ComboBox<String> yearFilter = new ComboBox<>();
    private String selectedYear = "Всі роки";
    private boolean isUpdating = false; // Флаг для запобігання рекурсивних оновлень

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
            tableView = new TableView<>();
            addOrderController = new AddOrderController();
            editOrderController = new EditOrderController();
            documentsUtil = DocumentsUtil.getInstance();
            dbUtil = DBUtil.getInstance();

            Button addButton = new Button("Додати наказ");
            addButton.setGraphic(IconsUtil.getPlusIcon());
            addButton.getStyleClass().add("green-button");

            Button editButton = new Button("Редагувати наказ");
            editButton.setGraphic(IconsUtil.getPencilIcon());
            editButton.setDisable(true);
            editButton.getStyleClass().add("yellow-button");

            Button copyButton = new Button("Копіювати наказ");
            copyButton.setGraphic(IconsUtil.getCopyIcon());
            copyButton.setDisable(true);
            copyButton.getStyleClass().add("grey-button");

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsUtil.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");

            Button openFileButton = new Button("Відкрити наказ");
            openFileButton.setGraphic(IconsUtil.getFileIcon());
            openFileButton.setDisable(true);
            openFileButton.getStyleClass().add("grey-button");

            Button deleteButton = new Button("Позначити наказ на видалення");
            deleteButton.setGraphic(IconsUtil.getRubbishIcon());
            deleteButton.setDisable(true);
            deleteButton.getStyleClass().add("red-button");

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsUtil.getUpdateIcon());


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
                return new SimpleStringProperty(dbUtil.getWorkerName(true, cellData.getValue().getIdWorker()));
            });

            TableColumn<_Order, String> positionCol = new TableColumn<>("Посада");
            positionCol.setCellValueFactory(cellData -> {
                return new SimpleStringProperty(dbUtil.getWorkerPosition(true, cellData.getValue().getIdWorker()));
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
                return new SimpleStringProperty(dbUtil.isOrderModifiable(cellData.getValue().getId()) ? "Невиконаний" : "Виконаний");
            });

            tableView.getColumns().addAll( orderDateCol, orderNumberCol, workerCol,positionCol, startDateCol,
                    endDateCol, routeCol, moneyCol, goalCol, headCol, validCol);

            updateValues();
            tableView.setItems(orders);

            tableView.getSortOrder().add(orderNumberCol);
            orderNumberCol.setSortType(TableColumn.SortType.ASCENDING);


            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                editButton.setDisable(newSelection == null || (!dbUtil.isOrderModifiable(newSelection.getId()) && !dbUtil.getUsername().equals("root")));
                copyButton.setDisable(newSelection == null);
                openFileButton.setDisable(newSelection == null);
                deleteButton.setDisable(!(newSelection != null && dbUtil.getUsername().equals("root")));
            });

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            tableView.setRowFactory(tv -> {
                TableRow<_Order> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _Order selectedOrder = row.getItem();
                    if (selectedOrder != null && dbUtil.isOrderModifiable(selectedOrder.getId())) {
                        editOrderController.openWindow(selectedOrder, this);
                    }
                });

                MenuItem openItem = new MenuItem("Відкрити документ");
                openItem.setOnAction(event -> {
                    _Order selectedOrder = row.getItem();
                    if (selectedOrder != null) {
                        documentsUtil.createOrderDocument(dbUtil, selectedOrder);
                    }
                });

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        _Order selectedOrder = row.getItem();
                        if (selectedOrder != null) {
                            documentsUtil.createOrderDocument(dbUtil, selectedOrder);
                        }
                    }
                });

                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    rowMenu.getItems().clear();
                    if (newItem != null ) {
                        rowMenu.getItems().add(openItem);
                        if(dbUtil.isOrderModifiable(newItem.getId())) {
                            rowMenu.getItems().add(editItem);
                        }
                    }

                });

                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));

                return row;
            });

            addButton.setOnAction(e -> {
                addOrderController.openWindow(null, this);
            });

            openFolderButton.setOnAction(e -> {
                openFolder(documentsUtil.getDocsFolderPath() + "DocFiles\\"+ dbUtil.getCompany() + "\\" + documentsUtil.getFolders()[2] + "\\");
            });

            openFileButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    documentsUtil.createOrderDocument(dbUtil, selectedOrder);
                }
            });

            updateButton.setOnAction(e->{
                updateValues();
                tableView.sort();
            });

            editButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    editOrderController.openWindow(selectedOrder, this);
                }
            });

            copyButton.setOnAction(e -> {
                _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    addOrderController.openWindow(selectedOrder, this);
                }
            });

            // Налаштування фільтру по року
            yearFilter.getItems().add("Всі роки");
            yearFilter.setValue("Всі роки");
            yearFilter.setOnAction(e -> {
                if (!isUpdating) { // Запобігаємо рекурсивним викликам
                    selectedYear = yearFilter.getValue();
                    updateValues();
                }
            });

            HBox buttonBox = new HBox(10, updateButton, addButton, copyButton, editButton, new Label("Рік:"), yearFilter, openFileButton, openFolderButton ,deleteButton);
            buttonBox.getStyleClass().add("buttonBox");
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            if(dbUtil.getUsername().equals("root")) {
                deleteButton.setText("Видалити Наказ");
                deleteButton.setOnAction(e->{
                    _Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити наказ");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.deleteOrder(selectedOrder);
                                updateValues();
                                tableView.sort();
                            }
                        });

                    }
                });
            }

            pagination = new Pagination(1, 0);
            pagination.setPageFactory(this::createPage);
            paginationUtil = new PaginationUtil(pagination);

            VBox.setVgrow(tableView, Priority.ALWAYS);

            tableContainer = new VBox(tableView);
            VBox.setVgrow(tableContainer, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

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

            // Створення панелі управління пагінацією
            HBox paginationControls = paginationUtil.createPaginationControls();

            table.getChildren().addAll(buttonBox,tableContainer, pagination, paginationControls);

            mainPage.openInternalWindow(table, windowTitle, true);
        }
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, orders.size());

        if (fromIndex > toIndex) {
            tableView.setItems(FXCollections.observableArrayList());
        } else {
            tableView.setItems(FXCollections.observableArrayList(orders.subList(fromIndex, toIndex)));
        }

        return new VBox(); // нічого не повертаємо, бо ми напряму працюємо з tableContainer
    }


    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Order> newOrders = dbUtil.getOrders();

                // Оновлення списку років в ComboBox (з УСІХ даних, не тільки відфільтрованих!)
                Set<String> years = newOrders.stream()
                        .flatMap(order -> {
                            Set<Integer> orderYears = new HashSet<>();
                            if (order.getOrderDate() != null) orderYears.add(order.getOrderDate().getYear());
                            if (order.getStartDate() != null) orderYears.add(order.getStartDate().getYear());
                            if (order.getEndDate() != null) orderYears.add(order.getEndDate().getYear());
                            return orderYears.stream();
                        })
                        .map(String::valueOf)
                        .collect(Collectors.toSet());

                // Фільтрація по року (ПІСЛЯ того як взяли всі роки)
                if (!selectedYear.equals("Всі роки")) {
                    int year = Integer.parseInt(selectedYear);
                    newOrders = newOrders.stream()
                            .filter(order -> {
                                boolean orderDateMatch = order.getOrderDate() != null && order.getOrderDate().getYear() == year;
                                boolean startMatch = order.getStartDate() != null && order.getStartDate().getYear() == year;
                                boolean endMatch = order.getEndDate() != null && order.getEndDate().getYear() == year;
                                return orderDateMatch || startMatch || endMatch;
                            })
                            .collect(Collectors.toList());
                }

                Platform.runLater(() -> {
                    isUpdating = true; // Блокуємо onAction під час оновлення
                    try {
                        String currentSelection = yearFilter.getValue();
                        yearFilter.getItems().clear();
                        yearFilter.getItems().add("Всі роки");
                        yearFilter.getItems().addAll(years.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
                        if (yearFilter.getItems().contains(currentSelection)) {
                            yearFilter.setValue(currentSelection);
                        } else {
                            yearFilter.setValue("Всі роки");
                            selectedYear = "Всі роки";
                        }
                    } finally {
                        isUpdating = false; // Розблокуємо після завершення
                    }
                });

                // Сортування
                if (selectedYear.equals("Всі роки")) {
                    // Групування за роками (від старіших до новіших) та сортування всередині року за номером наказу
                    newOrders.sort(Comparator
                            .comparing((_Order order) -> {
                                // Беремо мінімальний рік з дат
                                int year = 9999;
                                if (order.getOrderDate() != null) year = Math.min(year, order.getOrderDate().getYear());
                                if (order.getStartDate() != null) year = Math.min(year, order.getStartDate().getYear());
                                if (order.getEndDate() != null) year = Math.min(year, order.getEndDate().getYear());
                                return year;
                            })
                            .thenComparing(order -> extractNumber(order.getOrderNumber())));
                } else {
                    // Якщо обрано конкретний рік - просто сортуємо за номером наказу
                    newOrders.sort(Comparator.comparing(order -> extractNumber(order.getOrderNumber())));
                }

                List<_Order> finalOrders = newOrders;
                Platform.runLater(() -> {
                    orders.setAll(finalOrders);

                    int pageCount = (int) Math.ceil((double) orders.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    // примусово оновлюємо таблицю
                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, orders.size());
                    tableView.setItems(FXCollections.observableArrayList(orders.subList(fromIndex, toIndex)));

                    // на всяк випадок оновлюємо контейнер
                    tableContainer.getChildren().setAll(tableView);

                    moveTableDown(tableView);
                });

                return null;
            }
        };
        new Thread(task).start();
    }


}
