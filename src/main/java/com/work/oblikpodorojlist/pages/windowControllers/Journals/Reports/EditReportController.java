package com.work.oblikpodorojlist.pages.windowControllers.Journals.Reports;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.model._Report;
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

public class EditReportController extends WindowController {
    private MainPage mainPage;
    private DBManager dbManager;

    public EditReportController(){}

    public void openWindow(_Report selectedReport, ReportsJournalController controller) {
        String windowTitle = "Редагувати звіт";
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

            TextField orderNumberField = new TextField(dbManager.getOrderNumber(selectedReport.getOrderId()));
            DatePicker datePicker = new DatePicker(selectedReport.getDate());
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            TextField workerField = new TextField(dbManager.getOrderWorkerName(selectedReport.getOrderId()));
            TextField positionField = new TextField(dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(selectedReport.getOrderId())));
            TextArea goalField = new TextArea(dbManager.getOrderGoal(selectedReport.getOrderId()));
            TextField headField = new TextField(dbManager.getOrderHead(selectedReport.getOrderId()));
            TextArea  commentsField = new TextArea (selectedReport.getComments());
            commentsField.setPrefRowCount(3);
            commentsField.setWrapText(true);
            goalField.setPrefRowCount(3);
            goalField.setWrapText(true);

            orderNumberField.setDisable(true);
            datePicker.setDisable(true);
            workerField.setDisable(true);
            positionField.setDisable(true);
            goalField.setDisable(false);
            headField.setDisable(true);
            commentsField.setDisable(false);

            grid.add(new Label("№ наказу:"), 0, 0);
            grid.add(orderNumberField, 1, 0);
            grid.add(new Label("Дата звіту:"), 0, 1);
            grid.add(datePicker, 1, 1);
            grid.add(new Label("Працівник:"), 0, 2);
            grid.add(workerField, 1, 2);
            grid.add(new Label("Посада:"), 0, 3);
            grid.add(positionField, 1, 3);
            grid.add(new Label("Мета:"), 0, 4);
            grid.add(goalField, 1, 4);
            grid.add(new Label("Керівник:"), 0, 5);
            grid.add(headField, 1, 5);
            grid.add(new Label("Додатковий коментар:"), 0, 6);
            grid.add(commentsField, 1, 6);

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                selectedReport.setComments(commentsField.getText());
                Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати звіт");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if(dbManager.changeReport(selectedReport)) {
                            mainPage.closeInternalWindow(windowTitle);
                        }
                        controller.updateValues();
                    }
                });
            });
        }
    }
}
