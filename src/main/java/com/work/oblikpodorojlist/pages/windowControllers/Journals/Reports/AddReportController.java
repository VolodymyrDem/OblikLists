package com.work.oblikpodorojlist.pages.windowControllers.Journals.Reports;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model.*;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists.AddListController;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists.ListsJournalController;
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
import java.util.Objects;

public class AddReportController extends WindowController {

    private MainPage mainPage;
    private DBManager dbManager;

    public AddReportController(){}



    public void openWindow(int idOrder, ReportsJournalController controller) {
        String windowTitle = "Додати звіт";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            dbManager = DBManager.getInstance();

            List<_Order> validOrders = dbManager.getOpenOrders();
            Map<String, Integer> ordersT = new HashMap<>();

            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            ComboBox<String> order = new ComboBox();
            DatePicker datePicker = new DatePicker(LocalDate.now());
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
            TextField workerField = new TextField();
            TextField positionField = new TextField();
            TextArea goalField = new TextArea();
            TextField headField = new TextField();
            TextArea  commentsField = new TextArea ();
            commentsField.setPrefRowCount(3);
            goalField.setPrefRowCount(3);
            commentsField.setWrapText(true);
            goalField.setWrapText(true);

            for(_Order d  : validOrders) {
                ordersT.put(d.getOrderNumber(), d.getIdOrder());
                order.getItems().add(d.getOrderNumber());
            }

            if(idOrder != -1) {
                order.setValue(dbManager.getOrderNumber(idOrder));
                workerField.setText(dbManager.getOrderWorkerName(idOrder));
                positionField.setText(dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(idOrder)));
                goalField.setText(dbManager.getOrderGoal(idOrder));
                headField.setText(dbManager.getOrderHead(idOrder));
            }

            order.setOnAction(e->{
                String slectedOrder = order.getValue();
                if(slectedOrder != null) {
                    int selectedORderID = ordersT.get(slectedOrder);
                    workerField.setText(dbManager.getOrderWorkerName(selectedORderID));
                    positionField.setText(dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(selectedORderID)));
                    goalField.setText( dbManager.getOrderGoal(selectedORderID));
                    headField.setText(dbManager.getOrderHead(selectedORderID));
                }
            });

            workerField.setDisable(true);
            positionField.setDisable(true);
            goalField.setDisable(false);
            headField.setDisable(true);
            commentsField.setDisable(false);

            grid.add(new Label("№ наказу:"), 0, 0);
            grid.add(order, 1, 0);
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
                if ( order.getValue() == null) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати звіт");
                    confirmationAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            _Report rep = new _Report(ordersT.get(order.getValue()), commentsField.getText(), datePicker.getValue());
                            if(dbManager.addReport(rep)) {
                                mainPage.closeInternalWindow(windowTitle);
                            }
                            if(controller != null) {
                                controller.updateValues();
                            }
                        }
                    });
                }
            });
        }
    }
}
