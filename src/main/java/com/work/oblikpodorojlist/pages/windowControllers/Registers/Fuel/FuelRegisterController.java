package com.work.oblikpodorojlist.pages.windowControllers.Registers.Fuel;

import com.work.oblikpodorojlist.utils.AlertsUtil;
import com.work.oblikpodorojlist.utils.DBUtil;
import com.work.oblikpodorojlist.utils.DocumentsUtil;
import com.work.oblikpodorojlist.utils.IconsUtil;
import com.work.oblikpodorojlist.utils.PaginationUtil;
import com.work.oblikpodorojlist.model.FuelUsage;
import com.work.oblikpodorojlist.model.PeriodParameters;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FuelRegisterController extends WindowController {
    private ObservableList<FuelUsage> FilteredFuelUsage = FXCollections.observableArrayList();
    public PeriodParameters parametersFuelUsage = new PeriodParameters();
    private MainPage mainPage;
    private ComboBox<String> podilField = new ComboBox<>();
    private DBUtil dbUtil;
    private DocumentsUtil documentsUtil;
    private CheckComboBox<String> carField = new CheckComboBox<>();
    private List<String> validCars = new ArrayList<>();
    private FuelPeriodController fuelPeriodController;
    private List<String> numbersG;
    public DatePicker datePickerStart;
    public DatePicker datePickerEnd;
    private Pagination pagination;
    private PaginationUtil paginationUtil;
    private VBox tableContainer;
    private TableView<FuelUsage> tableView;
    public FuelRegisterController(){}


    public void openWindow(){

        String windowTitle = "Реєстр: використання палива";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            datePickerStart = new DatePicker();
            datePickerEnd = new DatePicker();
            fuelPeriodController = new FuelPeriodController();
            documentsUtil = DocumentsUtil.getInstance();
            dbUtil = DBUtil.getInstance();

            validCars = dbUtil.getUniqueCarsNumbers();

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



            Label carLabel = new Label("Авто:");
            Label timeLabel = new Label("Період: з ");
            Label timeLabel2 = new Label("по");
            Label podilLabel = new Label("Групувати");
            Button filterButton = new Button("Застосувати фільтр");
            filterButton.setGraphic(IconsUtil.getFilterIcon());
            Button saveButton = new Button("Зберегти реєстр");
            saveButton.setGraphic(IconsUtil.getTikIcon());
            saveButton.setDisable(true);
            podilField.getItems().addAll("По днях", "По тижнях", "По місяцях", "По кварталах", "По роках", "По листах");
            podilField.setValue("По листах");

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsUtil.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsUtil.getDocsFolderPath() + "DocFiles\\"+ dbUtil.getCompany() + "\\" + documentsUtil.getFolders()[6] + "\\");
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

            updateButton.setGraphic(IconsUtil.getUpdateIcon());
            updateButton.setOnAction(e->{
                updateValues();
            });



            settingsButton.setGraphic(IconsUtil.getClockIcon());
            settingsButton.setOnAction(event -> {
                updateButton.setDisable(true);
                fuelPeriodController.openWindow(this);
            });

            carLabel.getStyleClass().add("filter-label");
            podilLabel.getStyleClass().add("filter-label");
            timeLabel.getStyleClass().add("filter-label");
            timeLabel2.getStyleClass().add("filter-label");
            filterButton.getStyleClass().add("green-button");
            saveButton.getStyleClass().add("green-button");

            for(String d  : validCars) {
                carField.getItems().add(d);
            }

            filterButton.setOnAction(e -> {
                if (datePickerStart.getValue() == null) {
                    updateButton.setDisable(true);
                    AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі дані").showAndWait();
                } else {
                    updateButton.setDisable(false);
                    updateValues();
                    saveButton.setDisable(false);
                }
            });

            saveButton.setOnAction(e -> {
                documentsUtil.createRegisterFuel(dbUtil, numbersG, FilteredFuelUsage, datePickerStart.getValue(), datePickerEnd.getValue(), parametersFuelUsage.getPeriod());
            });

            HBox buttonBox = new HBox(10, timeLabel, datePickerStart, timeLabel2, datePickerEnd,settingsButton, carLabel, carField, podilLabel, podilField, filterButton, updateButton, saveButton, openFolderButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);

            datePickerStart.setOnAction(e->{
                updateButton.setDisable(true);
            });
            datePickerEnd.setOnAction(e->{
                updateButton.setDisable(true);
            });
            podilField.setOnAction(e->{
                updateButton.setDisable(true);
            });

            pagination = new Pagination(1, 0);
            pagination.setPageFactory(this::createPage);
            pagination.setManaged(false);
            pagination.setVisible(false);
            paginationUtil = new PaginationUtil(pagination);


            tableView = new TableView<>();
            tableView.setItems(FilteredFuelUsage);


            TableColumn<FuelUsage, LocalDate> startDateCol = new TableColumn<>("Дата початку");
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

            TableColumn<FuelUsage, LocalDate> endDateCol = new TableColumn<>("Дата кінця");
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
            DecimalFormat df = new DecimalFormat("#.##");

            TableColumn<FuelUsage, String> carNumbercol = new TableColumn<>("Номер авто");
            carNumbercol.setCellValueFactory(new PropertyValueFactory<>("carNumber"));

            TableColumn<FuelUsage, String> mileageCol = new TableColumn<>("Пробіг за період");
            mileageCol.setCellValueFactory(new PropertyValueFactory<>("mileage"));

            TableColumn<FuelUsage, Double> fuelFactCol = new TableColumn<>("Фактичне використання палива");
            fuelFactCol.setCellValueFactory(new PropertyValueFactory<>("fuelFact"));

            fuelFactCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });
            TableColumn<FuelUsage, Double> fuelNormCol = new TableColumn<>("Норма використання палива");
            fuelNormCol.setCellValueFactory(new PropertyValueFactory<>("fuelNorm"));

            fuelNormCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });

            TableColumn<FuelUsage, Double> overUseCol = new TableColumn<>("Перевикористання палива");
            overUseCol.setCellValueFactory(new PropertyValueFactory<>("overUse"));

            overUseCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });

            TableColumn<FuelUsage, Double> underUseCol = new TableColumn<>("Економія палива");
            underUseCol.setCellValueFactory(new PropertyValueFactory<>("underUse"));

            underUseCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(df.format(value));
                    }
                }
            });


            tableView.getColumns().addAll(startDateCol, endDateCol, carNumbercol, mileageCol, fuelFactCol, fuelNormCol, overUseCol, underUseCol);

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            tableView.setSortPolicy(tv -> {
                java.util.Comparator<FuelUsage> comparator = tv.getComparator();
                if (comparator != null) {
                    FXCollections.sort(FilteredFuelUsage, comparator);
                }
                int pageIndex = pagination.getCurrentPageIndex();
                int fromIndex = pageIndex * rowsPerPage;
                int toIndex = Math.min(fromIndex + rowsPerPage, FilteredFuelUsage.size());
                if (!FilteredFuelUsage.isEmpty() && fromIndex <= toIndex) {
                    tv.setItems(FXCollections.observableArrayList(FilteredFuelUsage.subList(fromIndex, toIndex)));
                }
                return true;
            });

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

            table.getChildren().addAll(buttonBox, tableContainer, pagination, paginationUtil.createPaginationControls());

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
                    numbersG = validCars.stream()
                            .map(s -> s.split("\\s+")[0])
                            .collect(Collectors.toList());
                } else {
                    numbersG = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])
                            .collect(Collectors.toList());
                }
                Period podil = switch (podilField.getValue()) {
                    case "По днях" -> Period.ofDays(1);
                    case "По тижнях" -> Period.ofWeeks(1);
                    case "По місяцях" -> Period.ofMonths(1);
                    case "По кварталах" -> Period.ofMonths(3);
                    case "По роках" -> Period.ofYears(1);
                    case "По листах" -> Period.ofYears(2);
                    default -> Period.ofDays(1);
                };

                parametersFuelUsage.setPeriod(podil);
                parametersFuelUsage.setStartDate(datePickerStart.getValue());
                parametersFuelUsage.setEndDate(datePickerEnd.getValue());

                List<FuelUsage> newUsage = dbUtil.getListsFuelFiltered(numbersG, parametersFuelUsage);
                Platform.runLater(() -> {
                    FilteredFuelUsage.setAll(newUsage);
                    int pageCount = (int) Math.ceil((double) FilteredFuelUsage.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);
                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, FilteredFuelUsage.size());
                    tableView.setItems(FXCollections.observableArrayList(FilteredFuelUsage.subList(fromIndex, toIndex)));
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
        int toIndex = Math.min(fromIndex + rowsPerPage, FilteredFuelUsage.size());

        if (fromIndex > toIndex) {
            tableView.setItems(FXCollections.observableArrayList());
        } else {
            tableView.setItems(FXCollections.observableArrayList(FilteredFuelUsage.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

}
