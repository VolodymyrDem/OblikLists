package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Workers;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model._Position;
import com.work.oblikpodorojlist.model._Worker;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.application.Platform;
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
import java.util.Comparator;
import java.util.List;

public class WorkersHandbookController extends WindowController {
    private ObservableList<_Worker> workers = FXCollections.observableArrayList();
    private MainPage mainPage;
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    private AddWorkerController addWorkerController;
    private EditWorkerController editWorkerController;
    private RemoveWorkerController removeWorkerController;
    private ComboBox<String> selectionModel;
    private TableView<_Worker> tableView;
    public WorkersHandbookController(){}

    public void openWindow() {
        mainPage = MainPage.getInstance();
        String windowTitle = "Довідник: працівники";
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            tableView = new TableView<>();
            selectionModel = new ComboBox<>();
            dbManager = DBManager.getInstance();
            documentsManager = DocumentsManager.getInstance();
            addWorkerController = new AddWorkerController();
            editWorkerController = new EditWorkerController();
            removeWorkerController = new RemoveWorkerController();

            updateValues();

            selectionModel.getItems().addAll("актуальні", "неактуальні", "");
            selectionModel.setValue("");

            Button createFileButton = new Button("Зберегти довідник");
            createFileButton.setGraphic(IconsManager.getFileIcon());
            createFileButton.getStyleClass().add("grey-button");

            Button addButton = new Button("Додати працівника");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.getStyleClass().add("green-button");

            Button editButton = new Button("Редагувати працівника");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.getStyleClass().add("yellow-button");
            editButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");

            Button removeButton = new Button("Звільнити працівника");
            removeButton.setGraphic(IconsManager.getCrossIcon());
            removeButton.getStyleClass().add("red-button");
            removeButton.setDisable(true);

            Button deleteButton = new Button("Позначити працівника на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);
            deleteButton.getStyleClass().add("red-button");

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());



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
                    editWorkerController.openWindow(selectedWorker, this);
                }
            });
            removeButton.setOnAction(e -> {
                _Worker selectedWorker = tableView.getSelectionModel().getSelectedItem();
                if (selectedWorker != null) {
                    removeWorkerController.openWindow(selectedWorker, this);
                }
            });
            updateButton.setOnAction(e->{
                updateValues();
            });
            openFolderButton.setOnAction(e -> {
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[1] + "\\");
            });
            addButton.setOnAction(e -> {
                addWorkerController.openWindow(this);
            });
            createFileButton.setOnAction(e -> {
                documentsManager.createWorkersHandbook(dbManager, workers);
            });
            selectionModel.setOnAction(event -> {
                updateValues();
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, new Label("Фільтрувати актуальність"), selectionModel, createFileButton, openFolderButton,removeButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll( nameCol,positionCol,licenceCol, startDateCol, startOrderCol, endDateCol, endOrderCol, validCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);

            tableView.setItems(workers);

            tableView.scrollTo(tableView.getItems().size()-1);

            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити працівника");
                deleteButton.setOnAction(e->{
                    _Worker selectedWorker = tableView.getSelectionModel().getSelectedItem();
                    if (selectedWorker != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteWorker(selectedWorker);
                            }
                        });

                    }
                });
            }


            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            table.getChildren().addAll(buttonBox,tableView);

            mainPage.openInternalWindow(table, windowTitle, true);
        }
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Worker> newWorkers;
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    newWorkers = dbManager.getValidWorkers();
                } else if(selected.equals("неактуальні")) {
                    newWorkers = dbManager.getUnValidWorkers();
                } else {
                    newWorkers = dbManager.getWorkers();
                }
                newWorkers.sort(Comparator.comparing(_Worker::getNameN));
                Platform.runLater(() -> {
                    workers.setAll(newWorkers);
                    moveTableDown(tableView);
                });
                return null;
            }
        };
        new Thread(task).start();
    }
}
