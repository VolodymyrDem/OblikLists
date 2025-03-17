package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Workers;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model._Worker;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class RemoveWorkerController extends WindowController {
    private MainPage mainPage ;
    private DBManager dbManager;

    public RemoveWorkerController(){}

    public void openWindow(_Worker selectedWorker) {
        String windowTitle = "Звільнити працівника";
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

            grid.add(new Label("Дата звільнення:"), 0, 0);
            grid.add(datePickerEnd, 1, 0);
            grid.add(new Label("Номер наказу звільнення:"), 0, 1);
            grid.add(endOrderNumberField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow =  mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e -> {
                try {
                    selectedWorker.setEndDate(datePickerEnd.getValue());
                    selectedWorker.setEndOrderNumber(endOrderNumberField.getText());

                    if(selectedWorker.getEndDate() == null || selectedWorker.getEndOrderNumber() == ""){
                        Alert alert = Alerts.ErrorAlert("Введіть дані", "Не введено номер наказу або дату закінчення");
                        alert.showAndWait();
                    }
                    else {
                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Видалити працівника");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if (dbManager.removeWorker(selectedWorker)) {
                                    mainPage.closeInternalWindow(windowTitle);
                                }
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
