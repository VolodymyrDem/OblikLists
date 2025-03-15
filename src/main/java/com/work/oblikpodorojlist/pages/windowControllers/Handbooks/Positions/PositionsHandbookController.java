package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Positions;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model._Position;
import com.work.oblikpodorojlist.pages.MainPage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class PositionsHandbookController {

    private ObservableList<_Position> positions = FXCollections.observableArrayList();
    private MainPage mainPage ;
    private DBManager dbManager;
    private EditPositionController editPositionController;
    private AddPositionController addPositionController;

    public PositionsHandbookController(){}

    public void openWindow() {
        mainPage = MainPage.getInstance();
        String windowTitle = "Довідник: посади";
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            dbManager = DBManager.getInstance();
            editPositionController = new EditPositionController();
            addPositionController = new AddPositionController();

            updateValues();

            Button addButton = new Button("Додати посаду");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.getStyleClass().add("green-button");

            Button editButton = new Button("Редагувати посаду");
            editButton.setDisable(true);
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.getStyleClass().add("yellow-button");

            Button deleteButton = new Button("Позначити посаду на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);
            deleteButton.getStyleClass().add("red-button");

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());

            TableView<_Position> tableView = new TableView<>();

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

            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити посаду");
                deleteButton.setOnAction(e -> {
                    _Position selectedPosition = tableView.getSelectionModel().getSelectedItem();
                    if (selectedPosition != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити посаду");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.removePosition(selectedPosition);
                                updateValues();
                            }
                        });

                    }
                });
            }

            editButton.setOnAction(e -> {
                _Position selectedPosition = tableView.getSelectionModel().getSelectedItem();
                if (selectedPosition != null) {
                    editPositionController.openWindow(selectedPosition);
                }
            });

            updateButton.setOnAction(e->{
                updateValues();
            });

            addButton.setOnAction(e -> addPositionController.openWindow());

            tableView.getColumns().addAll(nameNCol, nameRCol);

            tableView.setItems(positions);

            tableView.scrollTo(tableView.getItems().size()-1);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);

            VBox table = new VBox();
            VBox.setVgrow(table, Priority.ALWAYS);

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            table.getChildren().addAll(buttonBox,tableView);

            mainPage.openInternalWindow(table, windowTitle);
        }
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Position> newPositions = dbManager.getPositions(); // Отримання даних у фоновому потоці
                Platform.runLater(() -> {
                    positions.setAll(newPositions); // Оновлення UI у JavaFX потоці
                });
                return null;
            }
        };
        new Thread(task).start();
    }

}
