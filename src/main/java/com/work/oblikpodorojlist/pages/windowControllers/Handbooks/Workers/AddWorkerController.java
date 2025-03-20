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

public class AddWorkerController extends WindowController {

    private MainPage mainPage ;
    private DBManager dbManager;

    public AddWorkerController(){}


    public void openWindow(WorkersHandbookController controller) {
        String windowTitle = "Додати працівника";
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

            TextField nameNField = new TextField();
            TextField nameRField = new TextField();

            List<_Position> positions = dbManager.getPositions();

            ComboBox<String> position = new ComboBox<>();
            Map<String, Integer> positionsT = new HashMap<>();
            for(_Position p : positions) {
                positionsT.put(p.getNameN(), p.getId());
                position.getItems().add(p.getNameN());
            }

            TextField licenceField = new TextField();DatePicker datePickerStart = new DatePicker();
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

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);


            StackPane internalWindow = mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if (isEmptyOrWhitespace(nameNField.getText()) || isEmptyOrWhitespace(nameRField.getText()) ||
                        isEmptyOrWhitespace(licenceField.getText()) || datePickerStart.getValue() == null ||
                        isEmptyOrWhitespace(startOrderNumberField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _Worker worker = new _Worker(
                                nameNField.getText(),
                                nameRField.getText(),
                                positionsT.get(position.getValue()),
                                licenceField.getText(),
                                datePickerStart.getValue(),
                                startOrderNumberField.getText()
                        );

                        worker.setPositionN(dbManager.getPositionNameN(positionsT.get(position.getValue())));
                        worker.setPositionR(dbManager.getPositionNameR(positionsT.get(position.getValue())));

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати працівника" );
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.addWorker(worker)) {
                                    mainPage.closeInternalWindow(windowTitle);
                                }
                                controller.updateValues();
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
