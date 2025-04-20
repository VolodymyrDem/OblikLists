package com.work.oblikpodorojlist.pages.windowControllers.Registers.Lists;

import com.work.oblikpodorojlist.managers.*;
import com.work.oblikpodorojlist.model.PeriodParameters;
import com.work.oblikpodorojlist.model._List;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListsRegisterController extends WindowController {
    private MainPage mainPage;
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    private CheckComboBox<String> carField = new CheckComboBox<>();
    private List<String> validCars = new ArrayList<>();
    private ListPeriodController listPeriodController;
    private List<String> numbersG;
    private ObservableList<_List> FilteredLists = FXCollections.observableArrayList();
    public DatePicker datePickerStart;
    public DatePicker datePickerEnd;
    private TableView<_List> tableView;
    private Pagination pagination;
    private VBox tableContainer;
    private static final Logger logger = LoggerUtil.getLogger();


    public ListsRegisterController(){
        datePickerStart = new DatePicker();
        datePickerEnd = new DatePicker();
    }

    public void openWindow(){
        String windowTitle = "Реєстр: подорожні листи";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            listPeriodController = new ListPeriodController();
            documentsManager = DocumentsManager.getInstance();
            dbManager = DBManager.getInstance();

            validCars = dbManager.getUniqueCarsNumbers();

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

            carField.setPrefWidth(200);
            carField.setMaxWidth(200);
            carField.setMinWidth(200);

            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

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

            Label carLabel = new Label("Авто:");
            Label timeLabel = new Label("Період: з ");
            Label timeLabel2 = new Label("по");
            Button filterButton = new Button("Застосувати фільтр");
            filterButton.setGraphic(IconsManager.getFilterIcon());
            Button saveButton = new Button("Зберегти реєстр");
            saveButton.setGraphic(IconsManager.getTikIcon());
            saveButton.setDisable(true);

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[7] + "\\");
            });


            Button settingsButton = new Button();

            Button updateButton = new Button();
            updateButton.setDisable(true);
            updateButton.getStyleClass().add("grey-button");

            carField.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
                if (change.next()) {
                    updateButton.setDisable(true);
                }
            });

            updateButton.setGraphic(IconsManager.getUpdateIcon());

            updateButton.setOnAction(e->{
                updateValues();
            });

            settingsButton.setGraphic(IconsManager.getClockIcon());

            settingsButton.setOnAction(event -> {
                updateButton.setDisable(true);
                listPeriodController.openWindow(this);
            });

            carLabel.getStyleClass().add("filter-label");
            timeLabel.getStyleClass().add("filter-label");
            timeLabel2.getStyleClass().add("filter-label");
            filterButton.getStyleClass().add("green-button");
            saveButton.getStyleClass().add("green-button");

            carField.getItems().addAll(validCars);

            filterButton.setOnAction(e -> {
                if (datePickerStart.getValue() == null) {
                    updateButton.setDisable(true);
                    Alerts.ErrorAlert("Помилка вводу", "Введіть усі дані").showAndWait();
                } else {
                    updateValues();
                    updateButton.setDisable(false);
                    saveButton.setDisable(false);
                }

            });

            saveButton.setOnAction(e -> {
                documentsManager.createRegisterLists(dbManager, numbersG, FilteredLists, datePickerStart.getValue(), datePickerEnd.getValue());
            });

            HBox buttonBox = new HBox(10, timeLabel, datePickerStart, timeLabel2, datePickerEnd,settingsButton, carLabel, carField, filterButton, updateButton, saveButton, openFolderButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            datePickerStart.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerEnd.setOnAction(e->{
                updateButton.setDisable(true);
            });

            tableView = new TableView<>();
            tableView.setItems(FilteredLists);

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

            TableColumn<_List, String> routeCol = new TableColumn<>("Маршрут");
            routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));

            TableColumn<_List, String> goalCol = new TableColumn<>("Мета");
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

            pagination = new Pagination(1, 0);
            pagination.setPageFactory(this::createPage);

            tableView.getColumns().addAll( listNumberCol, orderCol, workerCol, CarNumberCol,startDateCol, startMCol, startFCol,
                    endDateCol, endMCol, endFCol, reFCol, routeCol, goalCol);

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

                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                    System.out.println("empty");
                    numbersG = validCars.stream()
                            .map(s -> s.split("\\s+")[0])
                            .collect(Collectors.toList());
                } else {
                    numbersG = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])
                            .collect(Collectors.toList());
                    System.out.println(numbersG);
                }
                List<_List> newLists = dbManager.getListsFiltered(numbersG, datePickerStart.getValue(), datePickerEnd.getValue());
                newLists.sort(Comparator.comparingInt(_List::getNumber));
                Platform.runLater(() -> {
                    FilteredLists.setAll(newLists);
                    int pageCount = (int) Math.ceil((double) FilteredLists.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);
                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, FilteredLists.size());
                    tableView.setItems(FXCollections.observableArrayList(FilteredLists.subList(fromIndex, toIndex)));
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
        int toIndex = Math.min(fromIndex + rowsPerPage, FilteredLists.size());

        if (fromIndex > toIndex) {
            tableView.setItems(FXCollections.observableArrayList());
        } else {
            tableView.setItems(FXCollections.observableArrayList(FilteredLists.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}
