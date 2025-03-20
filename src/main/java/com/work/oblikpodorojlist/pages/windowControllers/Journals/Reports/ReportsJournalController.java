package com.work.oblikpodorojlist.pages.windowControllers.Journals.Reports;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.model._Report;
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

import java.time.LocalDate;
import java.util.List;

public class ReportsJournalController extends WindowController {
    private ObservableList<_Report> reports = FXCollections.observableArrayList();
    private MainPage mainPage;
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    private AddReportController addReportController;
    private EditReportController editReportController;
    private TableView<_Report> tableView;

    public ReportsJournalController(){}


    public void openWindow(){
        String windowTitle = "Журнал: звіти";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            tableView = new TableView<>();
            addReportController = new AddReportController();
            editReportController = new EditReportController();
            documentsManager = DocumentsManager.getInstance();
            dbManager = DBManager.getInstance();

            Button addButton = new Button("Додати звіт");
            addButton.setGraphic(IconsManager.getPlusIcon());
            addButton.setOnAction(e -> {
                addReportController.openWindow(-1, this);
            });

            Button editButton = new Button("Редагувати звіт");
            editButton.setGraphic(IconsManager.getPencilIcon());
            editButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[3] + "\\");
            });


            Button openFileButton = new Button("Відкрити звіт");
            openFileButton.setGraphic(IconsManager.getFileIcon());
            openFileButton.setDisable(true);

            Button deleteButton = new Button("Позначити звіт на видалення");
            deleteButton.setGraphic(IconsManager.getRubbishIcon());
            deleteButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsManager.getUpdateIcon());
            updateButton.setOnAction(e->{
                updateValues();
            });

            openFileButton.getStyleClass().add("grey-button");
            deleteButton.getStyleClass().add("red-button");
            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");



            TableColumn<_Report, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_Report, String> orderIdCol = new TableColumn<>("ID наказу");
            orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

            TableColumn<_Report, String> orderNumberCol = new TableColumn<>("№ наказу");
            orderNumberCol.setCellValueFactory(cellData -> {
                String order = dbManager.getOrderNumber(cellData.getValue().getOrderId());
                return new SimpleStringProperty(order);
            });

            TableColumn<_Report, LocalDate> startDateCol = new TableColumn<>("Дата звіту");
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
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

            TableColumn<_Report, String> workerCol = new TableColumn<>("Працівник");
            workerCol.setCellValueFactory(cellData -> {
                String worker =  dbManager.getOrderWorkerName(cellData.getValue().getOrderId());
                return new SimpleStringProperty(worker);
            });

            TableColumn<_Report, String> positionCol = new TableColumn<>("Посада");
            positionCol.setCellValueFactory(cellData -> {
                String pos =  dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(cellData.getValue().getOrderId()));
                return new SimpleStringProperty(pos);
            });

            TableColumn<_Report, String> goalCol = new TableColumn<>("Мета");
            goalCol.setCellValueFactory(cellData -> {
                String goal = dbManager.getOrderGoal(cellData.getValue().getOrderId());
                return new SimpleStringProperty(goal);
            });

            TableColumn<_Report, String> headCol = new TableColumn<>("Керівник");
            headCol.setCellValueFactory(cellData -> {
                String head = dbManager.getOrderHead(cellData.getValue().getOrderId());
                return new SimpleStringProperty(head);
            });


            TableColumn<_Report, String> commentsCol = new TableColumn<>("Додатковий коментар");
            commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));

            commentsCol.setCellValueFactory(cellData -> {
                String comments = cellData.getValue().getComments();
                if(comments.length()>= 19) {
                    comments = comments.substring(0, 19) + "...";
                }
                return new SimpleStringProperty(comments);
            });

            // Активуємо кнопку "Редагувати авто" при виборі рядка
            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean isItemSelected = newSelection != null;
                editButton.setDisable(!isItemSelected);
                openFileButton.setDisable(!isItemSelected);
                deleteButton.setDisable(!(newSelection != null && dbManager.getUsername().equals("root")));
            });

            editButton.setOnAction(e -> {
                _Report rep = tableView.getSelectionModel().getSelectedItem();
                if (rep != null) {
                    editReportController.openWindow(rep, this);
                }
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, openFileButton, openFolderButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll(orderNumberCol, startDateCol, workerCol, positionCol,
                    goalCol, headCol, commentsCol);

            updateValues();
            tableView.setItems(reports);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            tableView.scrollTo(tableView.getItems().size() - 1);
            if(dbManager.getUsername().equals("root")) {
                deleteButton.setText("Видалити звіт");
                deleteButton.setOnAction(e->{
                    _Report rep = tableView.getSelectionModel().getSelectedItem();
                    if (rep != null) {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.deleteReport(rep);
                            }
                        });

                    }
                });
            }

            openFileButton.setOnAction(e -> {
                _Report selectedReport = tableView.getSelectionModel().getSelectedItem();
                if (selectedReport != null) {
                    documentsManager.createReportDocument(dbManager, selectedReport);
                }
            });


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
                List<_Report> reportsNew = dbManager.getReports();

                reportsNew.sort((o1, o2) -> {
                    int num1 = extractNumber(dbManager.getOrderNumber(o1.getOrderId()));
                    int num2 = extractNumber(dbManager.getOrderNumber(o2.getOrderId()));
                    return Integer.compare(num1, num2);
                });

                Platform.runLater(() -> {
                    reports.setAll(reportsNew); // Оновлення UI у JavaFX потоці
                    moveTableDown(tableView);
                });

                return null;
            }
        };
        new Thread(task).start();
    }
}
