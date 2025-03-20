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

public class RemoveCarController extends WindowController {
    private MainPage mainPage ;
    private DBManager dbManager;

    public RemoveCarController(){}



    public void openWindow(_Car selectedCar, CarsHandbookController controller) {
        String windowTitle = "Зняти з експулатації авто";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            dbManager = DBManager.getInstance();
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

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
            TextField endOrderNumberField = new TextField();
            datePickerEnd.setValue(LocalDate.now());

            grid.add(new Label("Дата закінчення експлуатації:"), 0, 0);
            grid.add(datePickerEnd, 1, 0);
            grid.add(new Label("Номер наказу закінчення експлуатації:"), 0, 1);
            grid.add(endOrderNumberField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow =  mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e -> {
                try {
                    selectedCar.setEndDate(datePickerEnd.getValue());
                    selectedCar.setEndOrderNumber(endOrderNumberField.getText());

                    if(selectedCar.getEndDate() == null || selectedCar.getEndOrderNumber() == ""){
                        Alert alert = Alerts.ErrorAlert("Введіть дані", "Не введено номер наказу або дату закінчення");
                        alert.showAndWait();
                    }
                    else {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити авто");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                dbManager.removeCar(selectedCar);
                                mainPage.closeInternalWindow(windowTitle);
                                controller.updateValues();
                            }

                        });
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Please enter valid numbers");
                }
            });
        }
    }

}
