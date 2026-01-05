package com.work.oblikpodorojlist.pages.windowControllers.Journals.Reports;

import com.work.oblikpodorojlist.utils.*;
import com.work.oblikpodorojlist.model._Report;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportsJournalController extends WindowController {
    private ObservableList<_Report> reports = FXCollections.observableArrayList();
    private MainPage mainPage;
    private DBUtil dbUtil;
    private DocumentsUtil documentsUtil;
    private AddReportController addReportController;
    private EditReportController editReportController;
    private TableView<_Report> tableView;
    private Pagination pagination;
    private PaginationUtil paginationUtil;
    private VBox tableContainer;
    private ComboBox<String> yearFilter = new ComboBox<>();
    private String selectedYear = "Всі роки";
    private boolean isUpdating = false; // Флаг для запобігання рекурсивних оновлень

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
            documentsUtil = DocumentsUtil.getInstance();
            dbUtil = DBUtil.getInstance();

            Button addButton = new Button("Додати звіт");
            addButton.setGraphic(IconsUtil.getPlusIcon());
            addButton.setOnAction(e -> {
                addReportController.openWindow(-1, this);
            });

            Button editButton = new Button("Редагувати звіт");
            editButton.setGraphic(IconsUtil.getPencilIcon());
            editButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsUtil.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsUtil.getDocsFolderPath() + "DocFiles\\"+ dbUtil.getCompany() + "\\" + documentsUtil.getFolders()[3] + "\\");
            });


            Button openFileButton = new Button("Відкрити звіт");
            openFileButton.setGraphic(IconsUtil.getFileIcon());
            openFileButton.setDisable(true);

            Button deleteButton = new Button("Позначити звіт на видалення");
            deleteButton.setGraphic(IconsUtil.getRubbishIcon());
            deleteButton.setDisable(true);

            Button updateButton = new Button();
            updateButton.getStyleClass().add("grey-button");
            updateButton.setGraphic(IconsUtil.getUpdateIcon());
            updateButton.setOnAction(e->{
                updateValues();
            });

            openFileButton.getStyleClass().add("grey-button");
            deleteButton.getStyleClass().add("red-button");
            addButton.getStyleClass().add("green-button");
            editButton.getStyleClass().add("yellow-button");

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

            TableColumn<_Report, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<_Report, String> orderIdCol = new TableColumn<>("ID наказу");
            orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

            TableColumn<_Report, String> orderNumberCol = new TableColumn<>("№ наказу");
            orderNumberCol.setCellValueFactory(cellData -> {
                String order = dbUtil.getOrderNumber(cellData.getValue().getOrderId());
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
                String worker =  dbUtil.getOrderWorkerName(cellData.getValue().getOrderId());
                return new SimpleStringProperty(worker);
            });

            TableColumn<_Report, String> positionCol = new TableColumn<>("Посада");
            positionCol.setCellValueFactory(cellData -> {
                String pos =  dbUtil.getWorkerPosition(true, dbUtil.getOrderIdWorker(cellData.getValue().getOrderId()));
                return new SimpleStringProperty(pos);
            });

            TableColumn<_Report, String> goalCol = new TableColumn<>("Мета");
            goalCol.setCellValueFactory(cellData -> {
                String goal = dbUtil.getOrderGoal(cellData.getValue().getOrderId());
                return new SimpleStringProperty(goal);
            });

            TableColumn<_Report, String> headCol = new TableColumn<>("Керівник");
            headCol.setCellValueFactory(cellData -> {
                String head = dbUtil.getOrderHead(cellData.getValue().getOrderId());
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

            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean isItemSelected = newSelection != null;
                editButton.setDisable(!isItemSelected);
                openFileButton.setDisable(!isItemSelected);
                deleteButton.setDisable(!(newSelection != null && dbUtil.getUsername().equals("root")));
            });

            editButton.setOnAction(e -> {
                _Report rep = tableView.getSelectionModel().getSelectedItem();
                if (rep != null) {
                    editReportController.openWindow(rep, this);
                }
            });

            HBox buttonBox = new HBox(10,updateButton, addButton, editButton, new Label("Рік:"), yearFilter, openFileButton, openFolderButton, deleteButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            tableView.getColumns().addAll(orderNumberCol, startDateCol, workerCol, positionCol,
                    goalCol, headCol, commentsCol);

            updateValues();
            tableView.setItems(reports);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            tableView.scrollTo(tableView.getItems().size() - 1);
            if(dbUtil.getUsername().equals("root")) {
                deleteButton.setText("Видалити звіт");
                deleteButton.setOnAction(e->{
                    _Report rep = tableView.getSelectionModel().getSelectedItem();
                    if (rep != null) {
                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbUtil.deleteReport(rep);
                            }
                        });

                    }
                });
            }

            openFileButton.setOnAction(e -> {
                _Report selectedReport = tableView.getSelectionModel().getSelectedItem();
                if (selectedReport != null) {
                    documentsUtil.createReportDocument(dbUtil, selectedReport);
                }
            });

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

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Report> reportsNew = dbUtil.getReports();

                // Оновлення списку років в ComboBox (з УСІХ даних, не тільки відфільтрованих!)
                Set<String> years = reportsNew.stream()
                        .filter(report -> report.getDate() != null)
                        .map(report -> String.valueOf(report.getDate().getYear()))
                        .collect(Collectors.toSet());

                // Фільтрація по року (ПІСЛЯ того як взяли всі роки)
                if (!selectedYear.equals("Всі роки")) {
                    int year = Integer.parseInt(selectedYear);
                    reportsNew = reportsNew.stream()
                            .filter(report -> report.getDate() != null && report.getDate().getYear() == year)
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
                    reportsNew.sort(Comparator
                            .comparing((_Report report) -> {
                                if (report.getDate() != null) {
                                    return report.getDate().getYear();
                                }
                                return 9999;
                            })
                            .thenComparing(report -> extractNumber(dbUtil.getOrderNumber(report.getOrderId()))));
                } else {
                    // Якщо обрано конкретний рік - просто сортуємо за номером наказу
                    reportsNew.sort(Comparator.comparing(report -> extractNumber(dbUtil.getOrderNumber(report.getOrderId()))));
                }

                List<_Report> finalReports = reportsNew;
                Platform.runLater(() -> {
                    reports.setAll(finalReports);
                    int pageCount = (int) Math.ceil((double) reports.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);
                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, reports.size());
                    tableView.setItems(FXCollections.observableArrayList(reports.subList(fromIndex, toIndex)));
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
        int toIndex = Math.min(fromIndex + rowsPerPage, reports.size());

        if (fromIndex > toIndex) {
            tableView.setItems(FXCollections.observableArrayList());
        } else {
            tableView.setItems(FXCollections.observableArrayList(reports.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}
