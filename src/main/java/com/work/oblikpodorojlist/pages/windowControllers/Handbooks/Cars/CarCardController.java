package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Cars;

import com.work.oblikpodorojlist.utils.AlertsUtil;
import com.work.oblikpodorojlist.utils.DBUtil;
import com.work.oblikpodorojlist.model._Car;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import com.work.oblikpodorojlist.utils.DateUtil;
import com.work.oblikpodorojlist.utils.LoggerUtil;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CarCardController extends WindowController {
    private MainPage mainPage ;
    private DBUtil dbUtil;
    private boolean isEditing;
    private GridPane grid;
    private String windowTitle;
    private _Car car;
    private CarsHandbookController controller;

    private TextField numberField ;
    private TextField modelField;
    private TextField fuelTypeField;
    private TextField fuelUsageField;
    private TextField engineVolumeField;
    private TextField startOrderNumberField;
    private TextField endOrderNumberField;
    private TextField startFuelField;
    private TextField startMileageField;
    private DatePicker datePickerStart;
    private DatePicker datePickerEnd;

    private static final Logger logger = LoggerUtil.getLogger();

    public CarCardController(){}

    public void openWindow(boolean isRemoving ,_Car selectedCar, CarsHandbookController controller) {
        isEditing = (selectedCar != null);

        if(isRemoving) windowTitle = "Зняти з експулатації авто";
        else windowTitle = isEditing ? "Редагувати авто" : "Додати авто";

        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;

        dbUtil = DBUtil.getInstance();
        grid = new GridPane();

        car = selectedCar;
        this.controller = controller;

        numberField = new TextField();
        modelField = new TextField();
        fuelTypeField = new TextField();
        fuelUsageField = new TextField();
        engineVolumeField = new TextField();
        startOrderNumberField = new TextField();
        endOrderNumberField = new TextField();
        startFuelField = new TextField();
        startMileageField = new TextField();
        datePickerStart = new DatePicker();
        datePickerEnd = new DatePicker();


        datePickerStart.setConverter(DateUtil.dateConverter(dateFormatter));

        datePickerEnd.setConverter(DateUtil.dateConverter(dateFormatter));

        if(isEditing) {
            numberField.setText(selectedCar.getNumber());
            modelField.setText(selectedCar.getModel());
            fuelTypeField.setText(selectedCar.getFuelType());
            fuelUsageField.setText(String.valueOf(selectedCar.getFuelUsage()));
            engineVolumeField.setText(String.valueOf(selectedCar.getEngineVolume()));
            startOrderNumberField.setText(selectedCar.getStartOrderNumber());
            startFuelField.setText(String.valueOf(selectedCar.getStartFuel()));
            startMileageField.setText(String.valueOf(selectedCar.getStartMileage()));
            datePickerStart.setValue(selectedCar.getStartDate());
            if(selectedCar.getEndDate() != null) {
                datePickerEnd.setValue(selectedCar.getEndDate());
                endOrderNumberField.setText(selectedCar.getEndOrderNumber());
            }
        }

        startMileageField.setDisable(!dbUtil.getUsername().equals("root"));
        startFuelField.setDisable(!dbUtil.getUsername().equals("root"));

        if(isRemoving) {
            numberField.setDisable(true);
            modelField.setDisable(true);
            fuelTypeField.setDisable(true);
            fuelUsageField.setDisable(true);
            engineVolumeField.setDisable(true);
            startOrderNumberField.setDisable(true);
            startFuelField.setDisable(true);
            startMileageField.setDisable(true);
            datePickerStart.setDisable(true);
            datePickerEnd.setValue(LocalDate.now());
        }

        Label NumberLabel = new Label("Номер:");
        Label ModelLabel =new Label("Модель:");
        Label FuelTypeLabel = new Label("Тип палива:");
        Label FuelUsageLabel = new Label("Використання палива(л/100км):");
        Label VolumeLabel = new Label("Об'єм двигуна:");
        Label StartDateLabel = new Label("Початок експлуатації: дата:");
        Label OrderStartLabel = new Label("Початок експлуатації: номер наказу:");
        Label MileageStartLabel = new Label("Початок експлуатації: пробіг:");
        Label FuelStartLabel = new Label("Початок експлуатації: паливо:");
        Label EndDateLabel = new Label("Кінець експлуатації: дата:");
        Label EndOrderLabel = new Label("Кінець експлуатації: номер наказу:");



        Button saveButton = new Button("Зберегти");

        VBox vbox = new VBox();

        grid = buildGridDouble(
                NumberLabel, numberField,
                ModelLabel, modelField,
                FuelTypeLabel, fuelTypeField,
                FuelUsageLabel, fuelUsageField,
                VolumeLabel, engineVolumeField,
                StartDateLabel, datePickerStart,
                OrderStartLabel, startOrderNumberField,
                MileageStartLabel, startMileageField,
                FuelStartLabel, startFuelField,
                EndDateLabel, datePickerEnd,
                EndOrderLabel, endOrderNumberField
        );
        vbox.getChildren().addAll(grid, saveButton);
        mainPage.openInternalWindow(vbox, windowTitle, false);

        saveButton.setOnAction(e ->{
            handleAction(isEditing);
        });
    }

    private void handleAction(boolean isEditing) {
        List<String> errors = validateInput();
        if (!errors.isEmpty()) {
            String msg = String.join("\n", errors);

            Alert alert = AlertsUtil.ErrorAlert("Error", msg);
            alert.showAndWait();

            logger.warning((isEditing ? "While editing car " : "While adding car ")
                    + (car != null ? car.getId() + " " + car.getNumber() : "")
                    + " Errors:\n" + msg);
            return;
        }
        double fuelUsage = Double.parseDouble(fuelUsageField.getText().replace(',', '.'));
        double engineVolume = Double.parseDouble(engineVolumeField.getText().replace(',', '.'));
        double startFuel = Double.parseDouble(startFuelField.getText().replace(',', '.'));
        double startMileage = Double.parseDouble(startMileageField.getText().replace(',', '.'));
        _Car tempCar;
        if(isEditing) {
            tempCar = new _Car(
                    car.getId(),
                    numberField.getText(),
                    modelField.getText(),
                    fuelTypeField.getText(),
                    fuelUsage,
                    engineVolume,
                    datePickerStart.getValue(),
                    startOrderNumberField.getText(),
                    datePickerEnd.getValue() != null ? datePickerEnd.getValue() : null,
                    Objects.equals(endOrderNumberField.getText(), "") ? null : endOrderNumberField.getText(),
                    datePickerEnd.getValue() == null &&
                            (endOrderNumberField.getText() == null || endOrderNumberField.getText().isEmpty()),
                    startFuel,
                    startMileage
            );
        } else{
            tempCar = new _Car(
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
            tempCar.setValid(true);
        }

        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати авто":"Додати авто");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (isEditing)
                    dbUtil.changeCar(tempCar);
                else
                    dbUtil.addCar(tempCar);

                makeLog(isEditing, tempCar);

                mainPage.closeInternalWindow(windowTitle);
                controller.updateValues();
            }

        });
    }


    private List<String> validateInput() {
        List<String> errors = new ArrayList<>();

        if (numberField.getText().isBlank()) {
            errors.add("Поле «Номер» не може бути порожнім");
        }
        if (modelField.getText().isBlank()) {
            errors.add("Поле «Модель» не може бути порожнім");
        }
        if (fuelTypeField.getText().isBlank()) {
            errors.add("Поле «Тип палива» не може бути порожнім");
        }
        try {
            Double.parseDouble(fuelUsageField.getText().replace(',', '.'));
        } catch (Exception e) {
            errors.add("Поле «Використання палива» повинно бути числом");
        }
        try {
            Double.parseDouble(engineVolumeField.getText().replace(',', '.'));
        } catch (Exception e) {
            errors.add("Поле «Об'єм двигуна» повинно бути числом");
        }
        if (startOrderNumberField.getText().isBlank()) {
            errors.add("Поле «Номер наказу початку» не може бути порожнім");
        }
        try {
            Double.parseDouble(startFuelField.getText().replace(',', '.'));
        } catch (Exception e) {
            errors.add("Поле «Паливо на початок» повинно бути числом");
        }
        try {
            Double.parseDouble(startMileageField.getText().replace(',', '.'));
        } catch (Exception e) {
            errors.add("Поле «Пробіг на початок» повинно бути числом");
        }

        LocalDate endDate = datePickerEnd.getValue();
        String endOrder = endOrderNumberField.getText().trim();
        if (endDate != null && endOrder.isEmpty()) {
            errors.add("Вкажіть номер наказу для дати закінчення");
        }
        if (endDate == null && !endOrder.isEmpty()) {
            errors.add("Вкажіть дату закінчення експлуатації");
        }

        return errors;
    }


    private void makeLog(boolean isEditing, _Car tempCar) {
        if(isEditing) {
            Map<String,String[]> diffs = car.diff(tempCar);
            if (diffs.isEmpty()) {
                logger.info("Edited car " + car.getId() + " " + car.getNumber() + " — no changes");
            } else {
                String changes = diffs.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue()[0] + "→" + e.getValue()[1])
                        .collect(Collectors.joining(", "));
                logger.info("Edited car " + car.getId() + " " + car.getNumber() + " — changes: " + changes);
            }
        }
        else {
            logger.info("Added car: " + tempCar.toSingleLine());
        }
    }
}