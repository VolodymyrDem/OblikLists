package com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model._Car;
import com.work.oblikpodorojlist.model._List;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.model._Worker;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Reports.AddReportController;
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

public class CloseListController extends WindowController {
    private MainPage mainPage ;
    private DBManager dbManager;
    private AddReportController addReportController;

    public CloseListController(){}




    public void openWindow(_List selectedList)  {
        String windowTitle = "Закрити лист";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            addReportController = new AddReportController();
            dbManager = DBManager.getInstance();
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            _Car car = dbManager.getCar(selectedList.getIdCar());



            DatePicker datePickerStart = new DatePicker(selectedList.getStartDate());
            DatePicker datePickerEnd = new DatePicker(selectedList.getEndDate());

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
            TextField routeField = new TextField(selectedList.getRoute());
            TextField goalField = new TextField(selectedList.getGoal());
            TextField carField = new TextField(car.getNumber() + " " + car.getModel());
            TextField worker = new TextField(dbManager.getWorkerName(true, selectedList.getIdWorker()));
            TextField positionField = new TextField(dbManager.getWorkerPosition(true, selectedList.getIdWorker()));
            TextField ListType = new TextField((selectedList.getIdOrder() == -1)?"по місту":"за наказом");
            TextField order = new TextField((selectedList.getIdOrder() == -1)?"":dbManager.getOrderNumber(selectedList.getIdOrder()));
            TextField endFuelField = new TextField();
            TextField endMileageField = new TextField();
            TextField refuelField = new TextField();

            order.setDisable(true);
            datePickerStart.setDisable(true);
            datePickerEnd.setDisable(true);
            routeField.setDisable(true);
            goalField.setDisable(true);
            worker.setDisable(true);
            carField.setDisable(true);
            positionField.setDisable(true);
            ListType.setDisable(true);

            grid.add(new Label("Авто:"), 0, 0);
            grid.add(carField, 1, 0);
            grid.add(new Label("Тип листа:"), 0, 1);
            grid.add(ListType, 1, 1);
            grid.add(new Label("Наказ:"), 0, 2);
            grid.add(order, 1, 2);
            grid.add(new Label("Виїзд: дата:"), 0, 3);
            grid.add(datePickerStart, 1, 3);
            grid.add(new Label("Поверення: дата:"), 0, 4);
            grid.add(datePickerEnd, 1, 4);
            grid.add(new Label("Маршрут:"), 0, 5);
            grid.add(routeField, 1, 5);
            grid.add(new Label("Мета:"), 0, 6);
            grid.add(goalField, 1, 6);
            grid.add(new Label("Працівник:"), 0, 7);
            grid.add(worker, 1, 7);
            grid.add(new Label("Посада:"), 0, 8);
            grid.add(positionField, 1, 8);
            grid.add(new Label("Повернення: паливо"), 0, 9);
            grid.add(endFuelField, 1, 9);
            grid.add(new Label("Повернення: пробіг"), 0, 10);
            grid.add(endMileageField, 1, 10);
            grid.add(new Label("Заправка"), 0, 11);
            grid.add(refuelField, 1, 11);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if (isEmptyOrWhitespace(endFuelField.getText()) || isEmptyOrWhitespace(endMileageField.getText()) || isEmptyOrWhitespace(refuelField.getText()) ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _List newList = selectedList;
                        newList.setEndFuel(Double.parseDouble(endFuelField.getText().replace(',', '.')));
                        newList.setEndMileage(Double.parseDouble(endMileageField.getText().replace(',', '.')));
                        newList.setRefuel(Double.parseDouble(refuelField.getText().replace(',', '.')));
                        newList.setDone(true);

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Закрити лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.updateList(newList)) {
                                    mainPage.closeInternalWindow(windowTitle);

                                    if(selectedList.getIdOrder() != -1) {
                                        Alert CreateReportAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Створити звіт про завершення виконання за наказом");
                                        CreateReportAlert.showAndWait().ifPresent(response2 -> {
                                            if (response2 == ButtonType.OK){
                                                addReportController.openWindow(selectedList.getIdOrder());
                                            }
                                        });
                                    }
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
