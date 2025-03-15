package com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model._List;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders.AddOrderController;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders.EditOrderController;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders.OrdersJournalController;
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
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListsJournalController extends WindowController {
    private ObservableList<_List> lists = FXCollections.observableArrayList();
    private MainPage mainPage;
    private List<String> numbersGL;
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    private CheckComboBox<String> carField = new CheckComboBox<>();
    private List<String> validCars = new ArrayList<>();
    private AddListController addListController;
    private EditListController editListController;
    private CloseListController closeListController;

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
            addListController = new AddListController();
            editListController = new EditListController();
            closeListController = new CloseListController();
            documentsManager = DocumentsManager.getInstance();
            dbManager = DBManager.getInstance();

            Button addButton = new Button("Додати лист");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> addListController.openWindow());

            validCars = dbManager.getUniqueCarsNumbers();

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
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[4] + "\\");
            });



            updateButton.setOnAction(e->{
                updateValues();
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

            TableView<_List> tableView = new TableView<>();


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
                    editListController.openWindow(selectedList);
                }
            });

            removeButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null && !selectedList.isDone()) {
                    closeListController.openWindow(selectedList);
                }
            });

            openFileButton.setOnAction(e -> {
                _List selectedList = tableView.getSelectionModel().getSelectedItem();
                if (selectedList != null) {
                    documentsManager.createList(dbManager, selectedList);
                }
            });

            HBox buttonBox = new HBox(10, updateButton, addButton, editButton, new Label("Фільтрувати по авто:"), carField, removeButton, openFileButton, openFolderButton, deleteButton);
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
                    if (selectedList != null && ( !selectedList.isDone() || dbManager.getUsername().equals("root"))) {
                        editListController.openWindow(selectedList);
                    }
                });

                MenuItem removeItem = new MenuItem("Закрити");
                removeItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null && !selectedList.isDone()) {
                        closeListController.openWindow(selectedList);
                    }
                });


                MenuItem openItem = new MenuItem("Переглянути лист");
                openItem.setGraphic(IconsManager.getFileIcon());
                openItem.setOnAction(event -> {
                    _List selectedList = row.getItem();
                    if (selectedList != null) {
                        documentsManager.createList(dbManager, selectedList);
                    }
                });

                // Double-click event handler
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        _List selectedList = row.getItem();
                        if (selectedList != null) {
                            documentsManager.createList(dbManager, selectedList);
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
                                updateValues();
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
                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                    numbersGL = validCars.stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                            .collect(Collectors.toList());
                } else {
                    numbersGL = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                            .collect(Collectors.toList());
                }

                List<_List> newLists = dbManager.getListsForCars(numbersGL);

                Platform.runLater(() -> {
                    lists.setAll(newLists); // Оновлення UI у JavaFX потоці
                });
                return null;
            }
        };
        new Thread(task).start();
    }
}
