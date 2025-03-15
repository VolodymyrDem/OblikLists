package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model._Car;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class EditCarController extends WindowController {
    private MainPage mainPage ;
    private DBManager dbManager;

    public EditCarController(){}

    public void openWindow(_Car selectedCar) {
        String windowTitle = "Редагувати авто";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        } else {
            dbManager = DBManager.getInstance();
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField numberField = new TextField(selectedCar.getNumber());
            TextField modelField = new TextField(selectedCar.getModel());
            TextField fuelTypeField = new TextField(selectedCar.getFuelType());
            TextField fuelUsageField = new TextField(String.valueOf(selectedCar.getFuelUsage()));
            TextField engineVolumeField = new TextField(String.valueOf(selectedCar.getEngineVolume()));
            TextField startOrderNumberField = new TextField(selectedCar.getStartOrderNumber());
            TextField endOrderNumberField = new TextField(selectedCar.getEndOrderNumber());
            TextField startFuelField = new TextField(String.valueOf(selectedCar.getStartFuel()));
            TextField startMileageField = new TextField(String.valueOf(selectedCar.getStartMileage()));
            DatePicker datePickerStart = new DatePicker(selectedCar.getStartDate());
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
            DatePicker datePickerEnd = new DatePicker();
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

            grid.add(new Label("Номер:"), 0, 0);
            grid.add(numberField, 1, 0);
            grid.add(new Label("Модель:"), 0, 1);
            grid.add(modelField, 1, 1);
            grid.add(new Label("Тип палива:"), 0, 2);
            grid.add(fuelTypeField, 1, 2);
            grid.add(new Label("Використання палива(л/100км):"), 0, 3);
            grid.add(fuelUsageField, 1, 3);
            grid.add(new Label("Об'єм двигуна:"), 0, 4);
            grid.add(engineVolumeField, 1, 4);
            grid.add(new Label("Початок експлуатації: дата:"), 0, 5);
            grid.add(datePickerStart, 1, 5);
            grid.add(new Label("Початок експлуатації: номер наказу:"), 0, 6);
            grid.add(startOrderNumberField, 1, 6);
            grid.add(new Label("Початок експлуатації: пробіг:"), 0, 7);
            grid.add(startMileageField, 1, 7);
            grid.add(new Label("Початок експлуатації: паливо:"), 0, 8);
            grid.add(startFuelField, 1, 8);
            grid.add(new Label("Кінець експлуатації: дата:"), 0, 9);
            grid.add(datePickerEnd, 1, 9);
            grid.add(new Label("Кінець експлуатації: номер наказу:"), 0, 10);
            grid.add(endOrderNumberField, 1, 10);

            startMileageField.setDisable(!dbManager.getUsername().equals("root"));
            startFuelField.setDisable(!dbManager.getUsername().equals("root"));

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = mainPage.openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( (datePickerEnd.getValue() != null && isEmptyOrWhitespace(endOrderNumberField.getText())) ||
                        (datePickerEnd.getValue() == null && !isEmptyOrWhitespace(endOrderNumberField.getText()))
                        || isEmptyOrWhitespace(numberField.getText()) || isEmptyOrWhitespace(modelField.getText()) ||
                        isEmptyOrWhitespace(fuelTypeField.getText()) || isEmptyOrWhitespace(fuelUsageField.getText()) ||
                        isEmptyOrWhitespace(engineVolumeField.getText()) || isEmptyOrWhitespace(startOrderNumberField.getText()) ||
                        isEmptyOrWhitespace(startFuelField.getText())||isEmptyOrWhitespace(startMileageField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Car car = new _Car(
                                selectedCar.getIdCar(),
                                numberField.getText(),
                                modelField.getText(),
                                fuelTypeField.getText(),
                                Double.parseDouble(fuelUsageField.getText().replace(',', '.')),
                                Double.parseDouble(engineVolumeField.getText().replace(',', '.')),
                                datePickerStart.getValue(),
                                startOrderNumberField.getText(),
                                datePickerEnd.getValue() != null ? datePickerEnd.getValue() : null,
                                endOrderNumberField.getText() == null ? null : endOrderNumberField.getText(),
                                datePickerEnd.getValue() == null &&
                                        (endOrderNumberField.getText() == null || endOrderNumberField.getText().isEmpty()),
                                Double.parseDouble(startFuelField.getText().replace(',', '.')),
                                Double.parseDouble(startMileageField.getText().replace(',', '.'))
                        );


                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.changeCar(car);
                                mainPage.closeInternalWindow(windowTitle);
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }
}
