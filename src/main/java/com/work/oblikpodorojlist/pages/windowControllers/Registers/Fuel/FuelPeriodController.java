package com.work.oblikpodorojlist.pages.windowControllers.Registers.Fuel;

import com.work.oblikpodorojlist.MonthYearSpinnerValueFactory;
import com.work.oblikpodorojlist.QuarterYearSpinnerValueFactory;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;

public class FuelPeriodController extends WindowController {
    private MainPage mainPage;
    private DBManager dbManager;

    public FuelPeriodController() {
    }

    public void openWindow(FuelRegisterController controller){
        String windowTitle = "Реєстр палива: Параметри періоду";
        mainPage = MainPage.getInstance();
        dbManager = DBManager.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            ToggleGroup group = new ToggleGroup();

            int currentYear = Year.now().getValue();
            int currentMonth = LocalDate.now().getMonthValue();
            CheckBox fromStartOfYear = new CheckBox("З початку року");
            CheckBox fromStartOfQuarter = new CheckBox("З початку кварталу");
            CheckBox fromStartOfMonth = new CheckBox("З початку місяця");

            Spinner<Integer> yearSpinner = new Spinner<>();
            yearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(currentYear-1000, currentYear+1000, currentYear));
            yearSpinner.setEditable(true);

            Spinner<String> quarterYearSpinner = new Spinner<>();
            QuarterYearSpinnerValueFactory valueFactory = new QuarterYearSpinnerValueFactory(1, 4, currentYear);
            quarterYearSpinner.setValueFactory(valueFactory);
            quarterYearSpinner.setEditable(false);

            Spinner<String> monthYearSpinner = new Spinner<>();
            MonthYearSpinnerValueFactory valueFactoryMY = new MonthYearSpinnerValueFactory(currentMonth, currentYear);
            monthYearSpinner.setValueFactory(valueFactoryMY);
            monthYearSpinner.setEditable(false);

            DatePicker datePickerDay = new DatePicker(LocalDate.now());
            datePickerDay.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });

            DatePicker datePickerStart = new DatePicker(LocalDate.now());

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


            DatePicker datePickerEnd = new DatePicker(LocalDate.now());

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

            RadioButton year = new RadioButton("Рік");
            RadioButton quarter = new RadioButton("Квартал");
            RadioButton month = new RadioButton("Місяць");
            RadioButton day = new RadioButton("День");
            RadioButton custom = new RadioButton("Інтервал з");
            Label po = new Label("по");
            year.setToggleGroup(group);
            quarter.setToggleGroup(group);
            month.setToggleGroup(group);
            day.setToggleGroup(group);
            custom.setToggleGroup(group);



            group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                // Оновлюємо доступність елементів в залежності від вибору
                if (year.isSelected()) {
                    yearSpinner.setDisable(false);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (quarter.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(false);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(false);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                } else if (month.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(false);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(false);
                    fromStartOfMonth.setDisable(true);
                } else if (day.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(false);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(false);
                } else if (custom.isSelected()) {
                    yearSpinner.setDisable(true);
                    quarterYearSpinner.setDisable(true);
                    monthYearSpinner.setDisable(true);
                    datePickerDay.setDisable(true);
                    datePickerStart.setDisable(false);
                    datePickerEnd.setDisable(false);
                    fromStartOfYear.setDisable(true);
                    fromStartOfQuarter.setDisable(true);
                    fromStartOfMonth.setDisable(true);
                }
            });


            grid.add(year, 0, 0);
            grid.add(yearSpinner, 1, 0);
            grid.add(quarter, 0, 1);
            grid.add(quarterYearSpinner, 1, 1);
            grid.add(fromStartOfYear, 2, 1);
            grid.add(month, 0, 2);
            grid.add(monthYearSpinner, 1, 2);
            grid.add(fromStartOfQuarter, 2, 2);
            grid.add(day, 0, 3);
            grid.add(datePickerDay, 1, 3);
            grid.add(fromStartOfMonth, 2, 3);
            grid.add(custom, 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(po, 0, 5);
            grid.add(datePickerEnd, 1, 5);


            Button saveButton = new Button("Зберегти");



            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(event -> {

                LocalDate StartDate = LocalDate.now();
                LocalDate EndDate = LocalDate.now();
                Period period = Period.between(StartDate, EndDate);
                boolean fromStart = false;

                if (year.isSelected()) {
                    yearSpinner.getValue();
                    StartDate = LocalDate.of(yearSpinner.getValue(), 1, 1);
                    EndDate = LocalDate.of(yearSpinner.getValue(), 12, 31);
                } else if (quarter.isSelected()) {
                    int initialQuarter = valueFactory.getQuarter();
                    int initialYear = valueFactory.getYear();
                    if (fromStartOfYear.isSelected()) {
                        StartDate = LocalDate.of(initialYear, 1, 1);  // Початок року
                    } else {
                        StartDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear);  // Початок кварталу
                    }
                    EndDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear).plusMonths(3).minusDays(1);  // Кінець кварталу

                    fromStart = fromStartOfYear.isSelected();

                } else if (month.isSelected()) {
                    StartDate = valueFactoryMY.getStartDate();
                    EndDate = valueFactoryMY.getEndDate();
                    fromStart = fromStartOfQuarter.isSelected();

                    if (fromStartOfQuarter.isSelected()) {
                        int monthOfYear = StartDate.getMonthValue();
                        int quarterI = (monthOfYear - 1) / 3 + 1;  // Визначення кварталу (1 - січень-березень, 2 - квітень-червень тощо)
                        StartDate = valueFactoryMY.getStartDate();  // Початок місяця
                        LocalDate quarterStart = valueFactory.getQuarterStartDate(quarterI, StartDate.getYear()); // Початок кварталу
                        StartDate = quarterStart;
                    }

                } else if (day.isSelected()) {
                    StartDate = datePickerDay.getValue();
                    if (fromStartOfMonth.isSelected()) {
                        StartDate = LocalDate.of(StartDate.getYear(), StartDate.getMonth(), 1);  // Початок місяця
                    }
                    EndDate = StartDate;

                } else if (custom.isSelected()) {
                    StartDate = datePickerStart.getValue();
                    EndDate = datePickerEnd.getValue();
                }

                controller.datePickerStart.setValue(StartDate);
                controller.datePickerEnd.setValue(EndDate);

                controller.parametersFuelUsage.setStartDate(StartDate);
                controller.parametersFuelUsage.setEndDate(EndDate);

                mainPage.closeInternalWindow(windowTitle);
            });
        }
    }
}
