package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model._Car;
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
import java.util.List;

public class CarsHandbookController extends WindowController {
    private ObservableList<_Car> cars = FXCollections.observableArrayList();
    private MainPage mainPage;
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    private AddCarController addCarController;
    private EditCarController editCarController;
    private RemoveCarController removeCarController;
    ComboBox<String> selectionModel = new ComboBox<>();


    public CarsHandbookController(){}


    public void openWindow(){
        String windowTitle = "Довідник: автомобілі";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            addCarController = new AddCarController();
            editCarController = new EditCarController();
            removeCarController = new RemoveCarController();
            documentsManager = DocumentsManager.getInstance();
            dbManager = DBManager.getInstance();


            selectionModel.getItems().addAll("актуальні", "неактуальні", "");
            selectionModel.setValue("");

            Button addButton = new Button("Додати авто");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.getStyleClass().add("green-button");

            Button createFileButton = new Button("Зберегти довідник");
            createFileButton.setGraphic(IconsManager.getFileIcon());
            createFileButton.getStyleClass().add("grey-button");

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");

            Button editButton = new Button("Редагувати авто");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.setDisable(true);
            editButton.getStyleClass().add("yellow-button");

            Button removeButton = new Button("Зняти авто з експлуатації");
            removeButton.setGraphic(IconsManager.getCrossIcon());
            removeButton.setDisable(true);
            removeButton.getStyleClass().add("red-button");

            Button deleteButton = new Button("Позначити авто на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);
            deleteButton.getStyleClass().add("red-button");

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());



            TableView<_Car> tableView = new TableView<>();

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


            selectionModel.setOnAction(event -> {
                updateValues();
            });

            editButton.setOnAction(e -> {
                _Car selectedCar = tableView.getSelectionModel().getSelectedItem();
                if (selectedCar != null) {
                    editCarController.openWindow(selectedCar);
                }
            });

            removeButton.setOnAction(e -> {
                _Car selectedCar = tableView.getSelectionModel().getSelectedItem();
                if (selectedCar != null) {
                    removeCarController.openWindow(selectedCar);
                }
            });

            updateButton.setOnAction(e->{
                updateValues();
            });

            openFolderButton.setOnAction(e -> {
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[0] + "\\");
            });

            createFileButton.setOnAction(e -> {
                documentsManager.createCarsHandbook(dbManager, cars);
            });

            addButton.setOnAction(e -> {
                addCarController.openWindow();
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, new Label("Фільтрувати актуальність"),
                    selectionModel, createFileButton, openFolderButton, removeButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll(numberCol, modelCol, fuelTypeCol, fuelUsageCol,
                    engineVolCol, startDateCol, startOrderCol, endDateCol, endOrderCol, validCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);

            updateValues();
            tableView.setItems(cars);

            tableView.scrollTo(tableView.getItems().size()-1);

            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити авто");
                deleteButton.setOnAction(e->{
                    _Car selectedCar = tableView.getSelectionModel().getSelectedItem();
                    if (selectedCar != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteCar(selectedCar);
                                updateValues();
                            }
                        });

                    }
                });
            }

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
                List<_Car> newCars;
                String selected = selectionModel.getValue();
                if(selected.equals("актуальні")) {
                    newCars = dbManager.getValidCars();
                } else if(selected.equals("неактуальні")) {
                    newCars = dbManager.getUnValidCars();
                } else {
                    newCars = dbManager.getCars();
                }
                Platform.runLater(() -> {
                    cars.setAll(newCars); // Оновлення UI у JavaFX потоці
                });
                return null;
            }
        };
        new Thread(task).start();
    }
}
