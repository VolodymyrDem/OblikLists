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

public class AddCarController extends WindowController {
    private MainPage mainPage ;
    private DBManager dbManager;

    public AddCarController(){}



    public void openWindow() {
        String windowTitle = "Додати авто";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }  else {
            dbManager = DBManager.getInstance();
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            TextField numberField = new TextField();
            TextField modelField = new TextField();
            TextField fuelTypeField = new TextField();
            TextField fuelUsageField = new TextField();
            TextField engineVolumeField = new TextField();
            DatePicker datePickerStart = new DatePicker();
            TextField startFuelField = new TextField();
            TextField startMileageField = new TextField();
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
            datePickerStart.setValue(LocalDate.now());
            TextField startOrderNumberField = new TextField();

            grid.add(new Label("Номер:"), 0, 0);
            grid.add(numberField, 1, 0);
            grid.add(new Label("Модель:"), 0, 1);
            grid.add(modelField, 1, 1);
            grid.add(new Label("Тип палива:"), 0, 2);
            grid.add(fuelTypeField, 1, 2);
            grid.add(new Label("Використання палива:"), 0, 3);
            grid.add(fuelUsageField, 1, 3);
            grid.add(new Label("Об'єм двигуна:"), 0, 4);
            grid.add(engineVolumeField, 1, 4);
            grid.add(new Label("Початок експлуатації: дата:"), 0, 5);
            grid.add(datePickerStart, 1, 5);
            grid.add(new Label("Початок експлуатації: Номер наказу:"), 0, 6);
            grid.add(startOrderNumberField, 1, 6);
            grid.add(new Label("Початок експлуатації: паливо:"), 0, 7);
            grid.add(startFuelField, 1, 7);
            grid.add(new Label("Початок експлуатації: пробіг:"), 0, 8);
            grid.add(startMileageField, 1, 8);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = mainPage.openInternalWindow(vbox, windowTitle);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(numberField.getText()) || isEmptyOrWhitespace(modelField.getText()) ||
                        isEmptyOrWhitespace(fuelTypeField.getText()) || isEmptyOrWhitespace(fuelUsageField.getText()) ||
                        isEmptyOrWhitespace(engineVolumeField.getText()) || isEmptyOrWhitespace(startOrderNumberField.getText()) || isEmptyOrWhitespace(startMileageField.getText()) || isEmptyOrWhitespace(startFuelField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        double fuelUsage = Double.parseDouble(fuelUsageField.getText().replace(',', '.'));
                        double engineVolume = Double.parseDouble(engineVolumeField.getText().replace(',', '.'));
                        double startFuel = Double.parseDouble(startFuelField.getText().replace(',', '.'));
                        double startMileage = Double.parseDouble(startMileageField.getText().replace(',', '.'));

                        _Car car = new _Car(
                                numberField.getText(),
                                modelField.getText(),
                                fuelTypeField.getText(),
                                fuelUsage,
                                engineVolume,
                                datePickerStart.getValue(),
                                startOrderNumberField.getText(),
                                startFuel,
                                startMileage
                        );
                        car.setValid(true);

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.addCar(car);
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
