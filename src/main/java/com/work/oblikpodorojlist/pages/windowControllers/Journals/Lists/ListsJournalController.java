package com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists;

import com.work.oblikpodorojlist.utils.*;
import com.work.oblikpodorojlist.model._List;
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
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ListsJournalController extends WindowController {
    private ObservableList<_List> lists = FXCollections.observableArrayList();
    private MainPage mainPage;
    private List<String> numbersGL;
    private DBUtil dbUtil;
    private DocumentsUtil documentsUtil;
    private CheckComboBox<String> carField = new CheckComboBox<>();
    private List<String> validCars = new ArrayList<>();
    private AddListController addListController;
    private EditListController editListController;
    private CloseListController closeListController;
    private TableView<_List> tableView;
    private Pagination pagination;
    private PaginationUtil paginationUtil;
    private VBox tableContainer;
    private ComboBox<String> yearFilter = new ComboBox<>();
    private String selectedYear = "Всі роки";
    private boolean isUpdating = false; // Флаг для запобігання рекурсивних оновлень

    public ListsJournalController(){}


    public void openWindow(){
        String windowTitle = "Журнал: листи";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            tableView = new TableView<>();
            addListController = new AddListController();
            editListController = new EditListController();
            closeListController = new CloseListController();
            documentsUtil = DocumentsUtil.getInstance();
            dbUtil = DBUtil.getInstance();

            Button addButton = new Button("Додати лист");
            addButton.setGraphic(IconsUtil.getPlusIcon());
            addButton.setOnAction(e -> addListController.openWindow(this));

            validCars = dbUtil.getUniqueCarsNumbers();

            for(String d  : validCars) {
                carField.getItems().add(d);
            }

            Button editButton = new Button("Редагувати лист");
            editButton.setGraphic(IconsUtil.getPencilIcon());

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsUtil.getUpdateIcon());

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsUtil.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsUtil.getDocsFolderPath() + "DocFiles\\"+ dbUtil.getCompany() + "\\" + documentsUtil.getFolders()[4] + "\\");
            });

            pagination = new Pagination(1, 0);
            pagination.setPageFactory(this::createPage);
            paginationUtil = new PaginationUtil(pagination);

            // Налаштування фільтру по року
            yearFilter.getItems().add("Всі роки");
            yearFilter.setValue("Всі роки");
            yearFilter.setOnAction(e -> {
                if (!isUpdating) { // Запобігаємо рекурсивним викликам
                    selectedYear = yearFilter.getValue();
                    updateValues();
                }
            });

            updateButton.setOnAction(e->{
                updateValues();
            });


            editButton.setDisable(true);

            Button removeButton = new Button("Закрити лист");
            removeButton.setGraphic(IconsUtil.getTikIcon());
            removeButton.setDisable(true);

            Button deleteButton = new Button("Позначити лист на видалення");
            deleteButton.setGraphic(IconsUtil.getRubbishIcon());
            deleteButton.setDisable(true);

            deleteButton.getStyleClass().add("red-button");
            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");
            removeButton.getStyleClass().add("green-button");
            Button openFileButton = new Button("Перегляд листа");
            openFileButton.setGraphic(IconsUtil.getFileIcon());
            openFileButton.getStyleClass().add("grey-button");
            openFileButton.setDisable(true);




            TableColumn<_List, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_List, String> listNumberCol = new TableColumn<>("№ листа");
            listNumberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

            TableColumn<_List, String> workerCol = new TableColumn<>("ПІБ працівник");
            workerCol.setCellValueFactory(cellData -> {
                String Name = dbUtil.getWorkerName(true, cellData.getValue().getIdWorker());
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
                return new SimpleStringProperty(dbUtil.getCarNumber(orderNumber));
            });

            TableColumn<_List, String> orderCol = new TableColumn<>("№ наказу");
            orderCol.setCellValueFactory(cellData -> {
                int idOrder = cellData.getValue().getIdOrder();
                if (idOrder == -1) {
                    return new SimpleStringProperty("по місту");
                }
                return new SimpleStringProperty(dbUtil.getOrderNumber(idOrder));
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
                editButton.setDisable(newSelection == null || (newSelection.isDone() && !dbUtil.getUsername().equals("root")));
                removeButton.setDisable(newSelection == null || newSelection.isDone());
                openFileButton.setDisable(newSelection == null);
                deleteButton.setDisable(!(newSelection != null && dbUtil.getUsername().equals("root")));
            });

            editButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null && ( !selectedList.isDone() || dbUtil.getUsername().equals("root")) ) {
                    editListController.openWindow(selectedList);
                }
            });

            removeButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null && !selectedList.isDone()) {
                    closeListController.openWindow(selectedList, this);
                }
            });

            openFileButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null) {
                    documentsUtil.createList(dbUtil, selectedList);
                }
            });

            HBox buttonBox = new HBox(10, updateButton, addButton, editButton, new Label("Рік:"), yearFilter, new Label("Фільтрувати по авто:"), carField, removeButton, openFileButton, openFolderButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll( listNumberCol, orderCol, workerCol, CarNumberCol,startDateCol, startMCol, startFCol,
                    endDateCol, endMCol, endFCol, reFCol, routeCol, goalCol, validCol);

            updateValues();
            tableView.setItems(lists);

            tableView.getSortOrder().setAll(listNumberCol);
            tableView.sort();
            listNumberCol.setSortType(TableColumn.SortType.ASCENDING);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            tableView.setRowFactory(tv -> {
                TableRow<_List> row = new TableRow<>();
                ContextMenu rowMenu = new ContextMenu();

                MenuItem editItem = new MenuItem("Редагувати");
                editItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null && ( !selectedList.isDone() || dbUtil.getUsername().equals("root"))) {
                        editListController.openWindow(selectedList);
                    }
                });

                MenuItem removeItem = new MenuItem("Закрити");
                removeItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null && !selectedList.isDone()) {
                        closeListController.openWindow(selectedList, this);
                    }
                });


                MenuItem openItem = new MenuItem("Переглянути лист");
                openItem.setGraphic(IconsUtil.getFileIcon());
                openItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null) {
                        documentsUtil.createList(dbUtil, selectedList);
                    }
                });

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        _List selectedList = row.getItem();
                        if (selectedList != null) {
                            documentsUtil.createList(dbUtil, selectedList);
                        }
                    }
                });

                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    rowMenu.getItems().clear();
                    if (newItem != null) {
                        rowMenu.getItems().add(openItem);
                        if(!newItem.isDone()) {
                            rowMenu.getItems().add(editItem);
                            rowMenu.getItems().add(removeItem);
                        }
                    }
                });

                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));

                return row;
            });
            tableView.scrollTo(tableView.getItems().size() - 1);

            if(dbUtil.getUsername().equals("root")) {
                deleteButton.setText("Видалити лист");
                deleteButton.setOnAction(e->{
                    _List selectedList = tableView.getSelectionModel().getSelectedItem();
                    if (selectedList != null) {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.deleteList(selectedList);
                                updateValues();
                            }
                        });

                    }
                });
            }

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


            VBox.setVgrow(tableView, Priority.ALWAYS);



            tableContainer = new VBox(tableView);
            VBox.setVgrow(tableContainer, Priority.ALWAYS);

            // Створення панелі управління пагінацією
            HBox paginationControls = paginationUtil.createPaginationControls();

            table.getChildren().addAll(buttonBox,tableContainer, pagination, paginationControls);

            mainPage.openInternalWindow(table, windowTitle, true);
        }
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                    numbersGL = validCars.stream()
                            .map(s -> s.split("\\s+")[0])
                            .collect(Collectors.toList());
                } else {
                    numbersGL = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])
                            .collect(Collectors.toList());
                }

                List<_List> newLists = dbUtil.getListsForCars(numbersGL);

                // Оновлення списку років в ComboBox (з УСІХ даних, не тільки відфільтрованих!)
                java.util.Set<String> years = newLists.stream()
                        .flatMap(list -> {
                            java.util.Set<Integer> listYears = new java.util.HashSet<>();
                            if (list.getStartDate() != null) listYears.add(list.getStartDate().getYear());
                            if (list.getEndDate() != null) listYears.add(list.getEndDate().getYear());
                            return listYears.stream();
                        })
                        .map(String::valueOf)
                        .collect(Collectors.toSet());

                // Фільтрація по року (ПІСЛЯ того як взяли всі роки)
                if (!selectedYear.equals("Всі роки")) {
                    int year = Integer.parseInt(selectedYear);
                    newLists = newLists.stream()
                            .filter(list -> {
                                boolean startMatch = list.getStartDate() != null && list.getStartDate().getYear() == year;
                                boolean endMatch = list.getEndDate() != null && list.getEndDate().getYear() == year;
                                return startMatch || endMatch;
                            })
                            .collect(Collectors.toList());
                }

                Platform.runLater(() -> {
                    ListsJournalController.this.isUpdating = true; // Блокуємо onAction під час оновлення
                    try {
                        String currentSelection = yearFilter.getValue();
                        yearFilter.getItems().clear();
                        yearFilter.getItems().add("Всі роки");
                        yearFilter.getItems().addAll(years.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
                        if (yearFilter.getItems().contains(currentSelection)) {
                            yearFilter.setValue(currentSelection);
                        } else {
                            yearFilter.setValue("Всі роки");
                            ListsJournalController.this.selectedYear = "Всі роки";
                        }
                    } finally {
                        ListsJournalController.this.isUpdating = false; // Розблокуємо після завершення
                    }
                });

                // Сортування
                if (selectedYear.equals("Всі роки")) {
                    // Групування за роками (від старіших до новіших) та сортування всередині року
                    newLists.sort(Comparator
                            .comparing((_List list) -> {
                                // Беремо мінімальний рік з дат
                                int year = 9999;
                                if (list.getStartDate() != null) year = Math.min(year, list.getStartDate().getYear());
                                if (list.getEndDate() != null) year = Math.min(year, list.getEndDate().getYear());
                                return year;
                            })
                            .thenComparing(_List::getNumber));
                } else {
                    // Якщо обрано конкретний рік - просто сортуємо за номером
                    newLists.sort(Comparator.comparing(_List::getNumber));
                }

                List<_List> finalLists = newLists;
                Platform.runLater(() -> {
                    lists.setAll(finalLists);
                    int pageCount = (int) Math.ceil((double) lists.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);
                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());
                    tableView.setItems(FXCollections.observableArrayList(lists.subList(fromIndex, toIndex)));
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
        int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());

        if (fromIndex > toIndex) {
            tableView.setItems(FXCollections.observableArrayList());
        } else {
            tableView.setItems(FXCollections.observableArrayList(lists.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}
