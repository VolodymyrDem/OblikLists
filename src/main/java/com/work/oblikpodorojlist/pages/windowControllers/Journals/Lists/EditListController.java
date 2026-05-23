package com.work.oblikpodorojlist.pages.windowControllers.Journals.Lists;

import com.work.oblikpodorojlist.util.AlertsUtil;
import com.work.oblikpodorojlist.util.DBUtil;
import com.work.oblikpodorojlist.model._Car;
import com.work.oblikpodorojlist.model._List;
import com.work.oblikpodorojlist.model._Order;
import com.work.oblikpodorojlist.model._Worker;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditListController extends WindowController {
    private MainPage mainPage ;
    private DBUtil dbUtil;

    public EditListController(){}



    private List<_Order> getValidOrders() {
        List<_Order> validOrders = dbUtil.getFreeOrders();
        return validOrders;
    }
    private List<_Worker> getValidWorkers() {
        List<_Worker> validWorkers = dbUtil.getFreeWorkers();
        return validWorkers;
    }
    private List<_Car> getValidCars() {
        List<_Car> validWorkers = dbUtil.getFreeCars();
        return validWorkers;
    }

    public void openWindow(_List selectedList)  {
        String windowTitle = "Редагувати лист";
        mainPage = MainPage.getInstance();
        if(mainPage.openWindows.containsKey(windowTitle)) {
            mainPage.openWindows.get(windowTitle).toFront();
            if(!mainPage.openWindows.get(windowTitle).isVisible()){
                mainPage.openWindows.get(windowTitle).setVisible(true);
            }
        }
        else {
            dbUtil = DBUtil.getInstance();
            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            List<_Worker> validWorkers = dbUtil.getFreeWorkers();
            Map<String, Integer> workersT = new HashMap<>();

            List<_Car> validCars = dbUtil.getFreeCars();
            validCars.add(dbUtil.getCar(selectedList.getIdCar()));
            Map<String, Integer> carsT = new HashMap<>();

            Map<String, Integer> ordersT = new HashMap<>();


            DatePicker datePickerStart = new DatePicker(selectedList.getStartDate());
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
            DatePicker datePickerEnd = new DatePicker(selectedList.getEndDate());
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
            TextArea routeField = new TextArea(selectedList.getRoute());
            routeField.setWrapText(true); // перенос слів
            routeField.setPrefRowCount(4);
            TextField goalField = new TextField(selectedList.getGoal());
            ComboBox<String> carField = new ComboBox<>();
            ComboBox<String> worker = new ComboBox<>();
            TextField positionField = new TextField();
            ComboBox<String> ListType = new ComboBox<>();
            ComboBox<String> order = new ComboBox<>();
            TextField endMileageField = new TextField(String.valueOf(selectedList.getEndMileage()));
            TextField endFuelField = new TextField(String.valueOf(selectedList.getEndFuel()));
            TextField refuelField = new TextField(String.valueOf(selectedList.getRefuel()));
            TextField startFuelField = new TextField(String.valueOf(selectedList.getStartFuel()));
            TextField startMileageField = new TextField(String.valueOf(selectedList.getStartMileage()));
            TextField numberField = new TextField(String.valueOf(selectedList.getNumber()));

            startFuelField.setDisable(!dbUtil.getUsername().equals("root"));
            endFuelField.setDisable(!dbUtil.getUsername().equals("root"));
            startMileageField.setDisable(!dbUtil.getUsername().equals("root"));
            refuelField.setDisable(!dbUtil.getUsername().equals("root"));
            endMileageField.setDisable(!dbUtil.getUsername().equals("root"));

            ordersT.clear();
            order.getItems().clear();
            List<_Order> ordersL = getValidOrders();
            for(_Order d  : ordersL) {
                ordersT.put(d.getOrderNumber() + " - "+ dbUtil.getWorkerName(true, d.getIdWorker()), d.getId());
                order.getItems().add(d.getOrderNumber() + " - "+ dbUtil.getWorkerName(true, d.getIdWorker()));
            }


            for(_Worker d  : validWorkers) {
                workersT.put(d.getNameN(), d.getId());
                worker.getItems().add(d.getNameN());
            }
            workersT.put(dbUtil.getWorker(selectedList.getIdWorker()).getNameN(), selectedList.getIdWorker());
            worker.getItems().add(dbUtil.getWorker(selectedList.getIdWorker()).getNameN());
            positionField.setDisable(true);
            worker.setValue(dbUtil.getWorkerName(true, selectedList.getIdWorker()));
            positionField.setText(dbUtil.getWorkerPosition(true, selectedList.getIdWorker()));

            for(_Car d  : validCars) {
                carsT.put(d.getNumber() + " " +d.getModel(), d.getId());
                carField.getItems().add(d.getNumber() + " " +d.getModel());
            }
            carsT.put(dbUtil.getCar(selectedList.getIdCar()).getNumber() + " " + dbUtil.getCar(selectedList.getIdCar()).getModel(), selectedList.getIdCar());
            carField.getItems().add(dbUtil.getCar(selectedList.getIdCar()).getNumber() + " " + dbUtil.getCar(selectedList.getIdCar()).getModel());
            carField.setValue(dbUtil.getCar(selectedList.getIdCar()).getNumber() + " " + dbUtil.getCar(selectedList.getIdCar()).getModel());

            ListType.getItems().add("по місту");
            ListType.getItems().add("за наказом");
            if(selectedList.getIdOrder() != -1) {
                order.setValue(dbUtil.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbUtil.getWorkerName(true, selectedList.getIdWorker()));
            }

            if (selectedList.getIdOrder() != -1) {
                ListType.setValue("за наказом");
                order.setDisable(false);
                datePickerStart.setDisable(true);
                datePickerEnd.setDisable(true);
                routeField.setDisable(true);
                goalField.setDisable(true);
                worker.setDisable(true);
                ordersT.put(dbUtil.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbUtil.getWorkerName(true, selectedList.getIdWorker()), selectedList.getIdOrder());
                order.getItems().add(dbUtil.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbUtil.getWorkerName(true, selectedList.getIdWorker()));
            } else {
                ListType.setValue("\"по місту\"");
                order.setDisable(true);
                order.getItems().clear();
                datePickerStart.setDisable(false);
                datePickerEnd.setDisable(false);
                routeField.setDisable(false);
                goalField.setDisable(false);
                worker.setDisable(false);
            }


            ListType.setOnAction(e -> {
                String selectedOption = ListType.getValue();
                if (Objects.equals(selectedOption, "за наказом")) {
                    ordersT.clear();
                    order.getItems().clear();
                    List<_Order> orders = getValidOrders();
                    for(_Order d  : orders) {
                        ordersT.put(d.getOrderNumber() + " - "+ dbUtil.getWorkerName(true, d.getIdWorker()), d.getId());
                        order.getItems().add(d.getOrderNumber() + " - "+ dbUtil.getWorkerName(true, d.getIdWorker()));
                    }
                    if(selectedList.getIdOrder() != -1) {
                        ordersT.put(dbUtil.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbUtil.getWorkerName(true, selectedList.getIdWorker()), selectedList.getIdOrder());
                        order.getItems().add(dbUtil.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbUtil.getWorkerName(true, selectedList.getIdWorker()));
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
                    List<_Worker> workers = getValidWorkers();
                    for(_Worker d  : workers) {
                        workersT.put(d.getNameN(), d.getId());
                        worker.getItems().add(d.getNameN());
                    }
                    if(selectedList.getIdOrder() == -1) {
                        workersT.put(dbUtil.getWorker(selectedList.getIdWorker()).getNameN(), selectedList.getIdWorker());
                        worker.getItems().add(dbUtil.getWorker(selectedList.getIdWorker()).getNameN());
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
                    positionField.setText(dbUtil.getWorkerPosition(true, workersT.get(selectedWorker)));
                }
            });

            order.setOnAction(e -> {
                String selectedOrder = order.getValue();
                if (selectedOrder != null) {
                    datePickerStart.setValue(dbUtil.getStartOrderDate(ordersT.get(selectedOrder)));
                    datePickerEnd.setValue(dbUtil.getEndOrderDate(ordersT.get(selectedOrder)));
                    routeField.setText(dbUtil.getOrderRoute(ordersT.get(selectedOrder)));
                    goalField.setText(dbUtil.getOrderGoal(ordersT.get(selectedOrder)));
                    worker.getItems().clear();
                    workersT.clear();
                    workersT.put(dbUtil.getOrderWorkerName(ordersT.get(selectedOrder)), dbUtil.getOrderIdWorker(ordersT.get(selectedOrder)) );
                    worker.getItems().add(dbUtil.getOrderWorkerName(ordersT.get(selectedOrder)));
                    worker.setValue(dbUtil.getOrderWorkerName(ordersT.get(selectedOrder)));
                }
            });
            int i = 0;
            if(dbUtil.getUsername().equals("root")){
                grid.add(new Label("№ листа"), 0, 0);
                grid.add(numberField, 1, 0);
                i++;
            }

            grid.add(new Label("Авто:"), 0, 0+i);
            grid.add(carField, 1, 0+i);
            grid.add(new Label("Тип листа:"), 0, 1+i);
            grid.add(ListType, 1, 1+i);
            grid.add(new Label("Наказ:"), 0, 2+i);
            grid.add(order, 1, 2+i);
            grid.add(new Label("Виїзд: дата:"), 0, 3+i);
            grid.add(datePickerStart, 1, 3+i);
            grid.add(new Label("Поверення: дата:"), 0, 4+i);
            grid.add(datePickerEnd, 1, 4+i);
            grid.add(new Label("Маршрут:"), 0, 5+i);
            grid.add(routeField, 1, 5+i);
            grid.add(new Label("Мета:"), 0, 6+i);
            grid.add(goalField, 1, 6+i);
            grid.add(new Label("Працівник:"), 0, 7+i);
            grid.add(worker, 1, 7+i);
            grid.add(new Label("Посада:"), 0, 8+i);
            grid.add(positionField, 1, 8+i);
            grid.add(new Label("Виїзд: пробіг:"), 0, 9+i);
            grid.add(startMileageField, 1, 9+i);
            grid.add(new Label("Виїзд: паливо:"), 0, 10+i);
            grid.add(startFuelField, 1, 10+i);
            grid.add(new Label("Повернення: пробіг:"), 0, 11+i);
            grid.add(endMileageField, 1, 11+i);
            grid.add(new Label("Повернення: паливо:"), 0, 12+i);
            grid.add(endFuelField, 1, 12+i);
            grid.add(new Label("Заправка:"), 0, 13+i);
            grid.add(refuelField, 1, 13+i);

            Button saveButton = new Button("Зберегти");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( carField.getValue() == null || ListType.getValue() == null || (ListType.getValue() == "за наказом" && order.getValue() == null) ||
                        datePickerStart.getValue() == null || datePickerEnd.getValue() == null || isEmptyOrWhitespace(routeField.getText()) ||
                        isEmptyOrWhitespace(goalField.getText()) || worker.getValue() == null || isEmptyOrWhitespace(numberField.getText()) ) {
                    Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        _List newList = new _List(
                                selectedList.getId(),
                                carsT.get(carField.getValue()),
                                (Objects.equals(ListType.getValue(), "за наказом"))?ordersT.get(order.getValue()): -1,
                                workersT.get(worker.getValue()),
                                datePickerStart.getValue(),
                                datePickerEnd.getValue(),
                                routeField.getText(),
                                goalField.getText()
                        );
                        newList.setDone(selectedList.isDone());
                        newList.setNumber(Integer.parseInt(numberField.getText()));

                        if(dbUtil.getUsername().equals("root")) {
                            newList.setEndMileage(Double.parseDouble(endMileageField.getText()));
                            newList.setEndFuel(Double.parseDouble(endFuelField.getText()));
                            newList.setRefuel(Double.parseDouble(refuelField.getText()));
                            newList.setStartFuel(Double.parseDouble(startFuelField.getText()));
                            newList.setStartMileage(Double.parseDouble(startMileageField.getText()));
                        } else {
                            newList.setEndFuel(selectedList.getEndFuel());
                            newList.setEndMileage(selectedList.getEndMileage());
                            newList.setRefuel(selectedList.getRefuel());
                            newList.setStartFuel(selectedList.getStartFuel());
                            newList.setStartMileage(selectedList.getStartMileage());
                        }

                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Редагувати лист");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbUtil.updateList(newList)) {
                                    mainPage.closeInternalWindow(windowTitle);

                                }
                            }
                        });


                        carsT.clear();
                        carField.getItems().clear();
                        List<_Car> cars = getValidCars();
                        for(_Car d  : cars) {
                            carsT.put(d.getNumber() + " " +d.getModel(), d.getId());
                            carField.getItems().add(d.getNumber() + " " +d.getModel());
                        }
                        carsT.put(dbUtil.getCar(selectedList.getIdCar()).getNumber() + " " + dbUtil.getCar(selectedList.getIdCar()).getModel(), selectedList.getIdCar());
                        carField.getItems().add(dbUtil.getCar(selectedList.getIdCar()).getNumber() + " " + dbUtil.getCar(selectedList.getIdCar()).getModel());


                        workersT.clear();
                        worker.getItems().clear();
                        List<_Worker> workers = getValidWorkers();
                        for(_Worker d  : workers) {
                            workersT.put(d.getNameN(), d.getId());
                            worker.getItems().add(d.getNameN());
                        }
                        workersT.put(dbUtil.getWorker(selectedList.getIdWorker()).getNameN(), selectedList.getIdWorker());
                        worker.getItems().add(dbUtil.getWorker(selectedList.getIdWorker()).getNameN());

                        ordersT.clear();
                        order.getItems().clear();
                        List<_Order> orders = getValidOrders();
                        for(_Order d  : orders) {
                            ordersT.put(d.getOrderNumber() + " "+ dbUtil.getWorkerName(true, d.getIdWorker()), d.getId());
                            order.getItems().add(d.getOrderNumber() + " "+ dbUtil.getWorkerName(true, d.getIdWorker()));
                        }
                        if(selectedList.getIdOrder() != -1) {
                            ordersT.put(dbUtil.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbUtil.getWorkerName(true, selectedList.getIdWorker()), selectedList.getIdOrder());
                            order.getItems().add(dbUtil.getOrderNumber(selectedList.getIdOrder()) + " - "+ dbUtil.getWorkerName(true, selectedList.getIdWorker()));
                        }
                    } catch (NumberFormatException ex) {
                        Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }
}
