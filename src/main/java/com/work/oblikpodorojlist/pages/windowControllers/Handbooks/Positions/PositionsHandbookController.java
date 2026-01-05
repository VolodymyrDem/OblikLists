package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Positions;

import com.work.oblikpodorojlist.utils.*;

import com.work.oblikpodorojlist.model._Position;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.application.Platform;
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

import java.util.Comparator;
import java.util.List;

public class PositionsHandbookController extends WindowController {

    private ObservableList<_Position> positions = FXCollections.observableArrayList();
    private MainPage mainPage ;
    private DBUtil dbUtil;
    private PositionCardController PositionCardController;
    private TableView<_Position> tableView;
    private Pagination pagination;
    private PaginationUtil paginationUtil;
    private VBox tableContainer;

    public PositionsHandbookController(){}

    public void openWindow() {
        mainPage = MainPage.getInstance();
        String windowTitle = "Довідник: посади";

        if(mainPage.checkOpenWindow(windowTitle))return;

        tableView = new TableView<>();
        dbUtil = DBUtil.getInstance();
        PositionCardController = new PositionCardController();

        updateValues();

        Button addButton = new Button("Додати посаду");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати посаду");
        editButton.setDisable(true);
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.getStyleClass().add("yellow-button");

        Button deleteButton = new Button("Позначити посаду на видалення");
        deleteButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteButton.setDisable(true);
        deleteButton.getStyleClass().add("red-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        paginationUtil = new PaginationUtil(pagination);

        TableColumn<_Position, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<_Position, String> nameNCol = new TableColumn<>("Назва(називний відмінок)");
        nameNCol.setCellValueFactory(new PropertyValueFactory<>("nameN"));

        TableColumn<_Position, String> nameRCol = new TableColumn<>("Назва(родовий відмінок)");
        nameRCol.setCellValueFactory(new PropertyValueFactory<>("nameR"));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
            deleteButton.setDisable(!(newSelection != null && dbUtil.getUsername().equals("root")));
        });

        if(dbUtil.getUsername().equals("root")) {
            deleteButton.setText("Видалити посаду");
            deleteButton.setOnAction(e -> {
                _Position selectedPosition = tableView.getSelectionModel().getSelectedItem();
                if (selectedPosition != null) {
                    Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити посаду");
                    confirmationAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            dbUtil.removePosition(selectedPosition);
                            updateValues();
                        }
                    });

                }
            });
        }

        editButton.setOnAction(e -> {
            _Position selectedPosition = tableView.getSelectionModel().getSelectedItem();
            if (selectedPosition != null) {
                PositionCardController.openWindow(selectedPosition, this);
            }
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        addButton.setOnAction(e -> PositionCardController.openWindow(null, this));

        tableView.getColumns().addAll(nameNCol, nameRCol);

        tableView.setItems(positions);

        tableView.scrollTo(tableView.getItems().size()-1);

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



        HBox buttonBox = new HBox(10,updateButton, addButton, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Створення панелі управління пагінацією
        HBox paginationControls = paginationUtil.createPaginationControls();

        table.getChildren().addAll(buttonBox,tableContainer, pagination, paginationControls);

        mainPage.openInternalWindow(table, windowTitle, true);

    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Position> newPositions = dbUtil.getPositions();
                Platform.runLater(() -> {
                    newPositions.sort(Comparator.comparing(_Position::getNameN));
                    positions.setAll(newPositions);
                    int pageCount = (int) Math.ceil((double) positions.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);
                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, positions.size());
                    tableView.setItems(FXCollections.observableArrayList(positions.subList(fromIndex, toIndex)));
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
        int toIndex = Math.min(fromIndex + rowsPerPage, positions.size());

        if (fromIndex > toIndex) {
            tableView.setItems(FXCollections.observableArrayList());
        } else {
            tableView.setItems(FXCollections.observableArrayList(positions.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

}
