package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Workers;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model._Position;
import com.work.oblikpodorojlist.model._Worker;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditWorkerController extends WindowController {

    private MainPage mainPage ;
    private DBManager dbManager;

    public EditWorkerController(){}



    public void openWindow(_Worker selectedWorker) {
        String windowTitle = "Редагувати працівника";
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

            TextField nameNField = new TextField(selectedWorker.getNameN());
            TextField nameRField = new TextField(selectedWorker.getNameR());

            List<_Position> positions = dbManager.getPositions();

            ComboBox<String> position = new ComboBox<>();
            Map<String, Integer> positionsT = new HashMap<>();
            for(_Position p : positions) {
                positionsT.put(p.getNameN(), p.getId());
                position.getItems().add(p.getNameN());
            }

            position.setValue(selectedWorker.getPositionN());

            TextField licenceField = new TextField(selectedWorker.getDrivingLicense());
            TextField startOrderNumberField = new TextField(selectedWorker.getStartOrderNumber());
            TextField endOrderNumberField = new TextField(selectedWorker.getEndOrderNumber());
            DatePicker datePickerStart = new DatePicker(selectedWorker.getStartDate());
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

            grid.add(new Label("ПІБ(називний відмінок):"), 0, 0);
            grid.add(nameNField, 1, 0);
            grid.add(new Label("ПІБ(родовий відмінок):"), 0, 1);
            grid.add(nameRField, 1, 1);
            grid.add(new Label("Посада:"), 0, 2);
            grid.add(position, 1, 2);
            grid.add(new Label("Водійське посвідчення:"), 0, 3);
            grid.add(licenceField, 1, 3);
            grid.add(new Label("Дата працевлаштування:"), 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(new Label("Номер наказу працевлаштування:"), 0, 5);
            grid.add(startOrderNumberField, 1, 5);
            grid.add(new Label("Дата звільнення:"), 0, 6);
            grid.add(datePickerEnd, 1, 6);
            grid.add(new Label("Номер наказу звільнення:"), 0, 7);
            grid.add(endOrderNumberField, 1, 7);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);


            StackPane internalWindow =  mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( (datePickerEnd.getValue() != null && isEmptyOrWhitespace(endOrderNumberField.getText())) ||
                        (datePickerEnd.getValue() == null && !isEmptyOrWhitespace(endOrderNumberField.getText()))
                        || isEmptyOrWhitespace(nameNField.getText()) || isEmptyOrWhitespace(nameRField.getText()) ||
                        isEmptyOrWhitespace(licenceField.getText()) || datePickerStart.getValue() == null ||
                        isEmptyOrWhitespace(startOrderNumberField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Worker worker = new _Worker(
                                selectedWorker.getId(),
                                nameNField.getText(),
                                nameRField.getText(),
                                positionsT.get(position.getValue()),
                                licenceField.getText(),
                                datePickerStart.getValue(),
                                startOrderNumberField.getText()
                        );

                        worker.setPositionN(dbManager.getPositionNameN(positionsT.get(position.getValue())));
                        worker.setPositionR(dbManager.getPositionNameR(positionsT.get(position.getValue())));

                        if(datePickerEnd.getValue() != null) {
                            worker.setEndDate(datePickerEnd.getValue());
                            worker.setEndOrderNumber(endOrderNumberField.getText());
                            worker.setValid(false);
                        }

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати працівника");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.changeWorker(worker)) {
                                    mainPage.closeInternalWindow(windowTitle);
                                }
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
