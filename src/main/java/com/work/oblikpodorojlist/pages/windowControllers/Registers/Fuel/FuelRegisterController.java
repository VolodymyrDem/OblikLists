package com.work.oblikpodorojlist.pages.windowControllers.Registers.Fuel;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.managers.DocumentsManager;
import com.work.oblikpodorojlist.managers.IconsManager;
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
    private DBManager dbManager;
    private DocumentsManager documentsManager;
    private CheckComboBox<String> carField = new CheckComboBox<>();
    private List<String> validCars = new ArrayList<>();
    private FuelPeriodController fuelPeriodController;
    private List<String> numbersG;
    public DatePicker datePickerStart;
    public DatePicker datePickerEnd;

    public FuelRegisterController(){
        datePickerStart = new DatePicker();
        datePickerEnd = new DatePicker();
    }


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
            fuelPeriodController = new FuelPeriodController();
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

            carField = new CheckComboBox<>();

            Label carLabel = new Label("Авто:");
            Label timeLabel = new Label("Період: з ");
            Label timeLabel2 = new Label("по");
            Label podilLabel = new Label("Групувати");
            Button filterButton = new Button("Застосувати фільтр");
            filterButton.setGraphic(IconsManager.getFilterIcon());
            Button saveButton = new Button("Зберегти реєстр");
            saveButton.setGraphic(IconsManager.getTikIcon());
            saveButton.setDisable(true);
            podilField.getItems().addAll("По днях", "По тижнях", "По місяцях", "По кварталах", "По роках");
            podilField.setValue("По днях");

            Button openFolderButton = new Button("Відкрити папку");
            openFolderButton.setGraphic(IconsManager.getFolderIcon());
            openFolderButton.getStyleClass().add("grey-button");
            openFolderButton.setOnAction(e -> {
                openFolder(documentsManager.getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + documentsManager.getFolders()[6] + "\\");
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
                    Alerts.ErrorAlert("Помилка вводу", "Введіть усі дані").showAndWait();
                } else {
                    updateButton.setDisable(false);
                    updateValues();
                    saveButton.setDisable(false);
                }
            });

            saveButton.setOnAction(e -> {
                documentsManager.createRegisterFuel(dbManager, numbersG, FilteredFuelUsage, datePickerStart.getValue(), datePickerEnd.getValue(), parametersFuelUsage.getPeriod());
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




            TableView<FuelUsage> tableView = new TableView<>();
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
            DecimalFormat df = new DecimalFormat("#.##"); // 2 знаки після коми

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

            VBox.setVgrow(tableView, Priority.ALWAYS);

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
                if(datePickerEnd.getValue() == null) {
                    datePickerEnd.setValue(LocalDate.now());
                }

                if (carField.getCheckModel().getCheckedItems().isEmpty()) {
                    numbersG = validCars.stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each string
                            .collect(Collectors.toList());
                } else {
                    numbersG = carField.getCheckModel().getCheckedItems().stream()
                            .map(s -> s.split("\\s+")[0])  // Get the first word from each checked item
                            .collect(Collectors.toList());
                }
                Period podil = switch (podilField.getValue()) {
                    case "По днях" -> Period.ofDays(1);
                    case "По тижнях" -> Period.ofWeeks(1);
                    case "По місяцях" -> Period.ofMonths(1);
                    case "По кварталах" -> Period.ofMonths(3);
                    case "По роках" -> Period.ofYears(1);
                    default -> Period.ofDays(1);
                };

                parametersFuelUsage.setPeriod(podil);
                List<FuelUsage> newUsage = dbManager.getListsFuelFiltered(numbersG, parametersFuelUsage);
                Platform.runLater(() -> {
                    FilteredFuelUsage.setAll(newUsage);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

}
