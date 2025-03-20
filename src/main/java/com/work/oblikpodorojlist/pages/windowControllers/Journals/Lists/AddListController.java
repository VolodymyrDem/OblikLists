package com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model.*;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders.AddOrderController;
import com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders.OrdersJournalController;
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

public class AddListController extends WindowController {
    private MainPage mainPage ;
    private DBManager dbManager;

    public AddListController(){}



    private List<_Order> getValidOrders() {
        List<_Order> validOrders = dbManager.getFreeOrders();
        return validOrders;
    }
    private List<_Worker> getValidWorkers() {
        List<_Worker> validWorkers = dbManager.getFreeWorkers();
        return validWorkers;
    }
    private List<_Car> getValidCars() {
        List<_Car> validWorkers = dbManager.getFreeCars();
        return validWorkers;
    }

    public void openWindow(ListsJournalController controller) {
        String windowTitle = "Додати лист";
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

            List<_Worker> validWorkers = dbManager.getValidWorkers();
            Map<String, Integer> workersT = new HashMap<>();

            List<_Car> validCars = dbManager.getFreeCars();
            Map<String, Integer> carsT = new HashMap<>();

            Map<String, Integer> ordersT = new HashMap<>();

            DatePicker datePickerStart = new DatePicker();
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
            TextField routeField = new TextField();
            TextField goalField = new TextField();
            ComboBox<String> carField = new ComboBox<>();
            ComboBox<String> worker = new ComboBox<>();
            TextField positionField = new TextField();
            ComboBox<String> ListType = new ComboBox<>();
            ComboBox<String> order = new ComboBox<>();

            for(_Worker d  : validWorkers) {
                workersT.put(d.getNameN(), d.getId());
                worker.getItems().add(d.getNameN());
            }
            positionField.setDisable(true);


            for(_Car d  : validCars) {
                carsT.put(d.getNumber() + " " +d.getModel(), d.getIdCar());
                carField.getItems().add(d.getNumber() + " " +d.getModel());
            }


            ListType.getItems().add("по місту");
            ListType.getItems().add("за наказом");



            ListType.setOnAction(e -> {
                String selectedOption = ListType.getValue();
                if (Objects.equals(selectedOption, "за наказом")) {
                    ordersT.clear();
                    order.getItems().clear();
                    List<_Order> orders = getValidOrders();
                    for(_Order d  : orders) {
                        ordersT.put(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()), d.getIdOrder());
                        order.getItems().add(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()));
                    }
                    order.setDisable(false);
                    datePickerStart.setDisable(true);
                    datePickerEnd.setDisable(true);
                    routeField.setDisable(true);
                    goalField.setDisable(true);
                    worker.setDisable(true);
                }
                else {
                    workersT.clear();
                    worker.getItems().clear();
                    List<_Worker> workers = dbManager.getValidWorkers();
                    for(_Worker d  : workers) {
                        workersT.put(d.getNameN(), d.getId());
                        worker.getItems().add(d.getNameN());
                    }
                    order.setDisable(true);
                    order.getItems().clear();
                    datePickerStart.setDisable(false);
                    datePickerEnd.setDisable(false);
                    routeField.setDisable(false);
                    goalField.setDisable(false);
                    worker.setDisable(false);
                }
            });

            worker.setOnAction(e -> {
                String selectedWorker = worker.getValue();
                if (selectedWorker != null) {
                    positionField.setText(dbManager.getWorkerPosition(true, workersT.get(selectedWorker)));
                }
            });

            order.setOnAction(e -> {
                String selectedOrder = order.getValue();
                if (selectedOrder != null) {
                    datePickerStart.setValue(dbManager.getStartOrderDate(ordersT.get(selectedOrder)));
                    datePickerEnd.setValue(dbManager.getEndOrderDate(ordersT.get(selectedOrder)));
                    routeField.setText(dbManager.getOrderRoute(ordersT.get(selectedOrder)));
                    goalField.setText(dbManager.getOrderGoal(ordersT.get(selectedOrder)));
                    worker.getItems().clear();
                    workersT.clear();
                    workersT.put(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)), dbManager.getOrderIdWorker(ordersT.get(selectedOrder)) );
                    worker.getItems().add(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)));
                    worker.setValue(dbManager.getOrderWorkerName(ordersT.get(selectedOrder)));
                }
            });

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

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( carField.getValue() == null || ListType.getValue() == null || (ListType.getValue() == "за наказом" && order.getValue() == null) ||
                        datePickerStart.getValue() == null || datePickerEnd.getValue() == null || isEmptyOrWhitespace(routeField.getText()) ||
                        isEmptyOrWhitespace(goalField.getText()) || worker.getValue() == null ) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _List newList = new _List(
                                carsT.get(carField.getValue()),
                                (Objects.equals(ListType.getValue(), "за наказом"))?ordersT.get(order.getValue()): -1,
                                workersT.get(worker.getValue()),
                                datePickerStart.getValue(),
                                datePickerEnd.getValue(),
                                routeField.getText(),
                                goalField.getText()
                        );

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Додати лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.addList(newList)) {
                                    mainPage.closeInternalWindow(windowTitle);
                                }
                                controller.updateValues();
                            }
                        });


                        carsT.clear();
                        carField.getItems().clear();
                        List<_Car> cars = getValidCars();
                        for(_Car d  : cars) {
                            carsT.put(d.getNumber() + " " +d.getModel(), d.getIdCar());
                            carField.getItems().add(d.getNumber() + " " +d.getModel());
                        }

                        workersT.clear();
                        worker.getItems().clear();
                        List<_Worker> workers = getValidWorkers();
                        for(_Worker d  : workers) {
                            workersT.put(d.getNameN(), d.getId());
                            worker.getItems().add(d.getNameN());
                        }

                        ordersT.clear();
                        order.getItems().clear();
                        List<_Order> orders = getValidOrders();
                        for(_Order d  : orders) {
                            ordersT.put(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()), d.getIdOrder());
                            order.getItems().add(d.getOrderNumber() + " "+ dbManager.getWorkerName(true, d.getIdWorker()));
                        }

                    } catch (NumberFormatException ex) {
                        Alert alert = Alerts.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }
}
