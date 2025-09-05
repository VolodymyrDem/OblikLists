package com.work.oblikpodorojlist.pages.windowControllers.Journals.Orders;

import com.work.oblikpodorojlist.utils.AlertsUtil;
import com.work.oblikpodorojlist.utils.DBUtil;
import com.work.oblikpodorojlist.model._Company;
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

public class AddOrderController extends WindowController {
    private MainPage mainPage ;
    private DBUtil dbUtil;

    public AddOrderController(){}

    public void openWindow(_Order selectedOrder, OrdersJournalController controller) {
        String windowTitle = "Додати наказ";
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

            DatePicker datePickerOrderDate = new DatePicker();
            datePickerOrderDate.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(dateFormatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                }
            });
            datePickerOrderDate.setValue(LocalDate.now());
            TextField orderNumberField = new TextField();
            orderNumberField.setText(dbUtil.getNextOrderNumber(datePickerOrderDate.getValue()));

            datePickerOrderDate.valueProperty().addListener((obs, oldV, newV) -> {
                if(newV != null) {
                    orderNumberField.setText(dbUtil.getNextOrderNumber(newV));
                }
            });

            List<_Worker> validWorkers = dbUtil.getValidWorkers();

            ComboBox<String> worker = new ComboBox<>();
            Map<String, Integer> workersT = new HashMap<>();

            for(_Worker d  : validWorkers) {
                workersT.put(d.getNameN(), d.getId());
                worker.getItems().add(d.getNameN());
            }


            TextField positionField = new TextField();
            positionField.setDisable(true);

            worker.setOnAction(e -> {
                String selectedWorker = worker.getValue();
                if (selectedWorker != null) {
                    positionField.setText(dbUtil.getWorkerPosition(true, workersT.get(selectedWorker)));
                }
            });


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
            TextArea routeField = new TextArea();
            routeField.setWrapText(true); // перенос слів
            routeField.setPrefRowCount(4);
                        TextField moneyField = new TextField();
            TextField goalField = new TextField();

            ComboBox<String> headType = new ComboBox<>();
            headType.getItems().add("директор");
            headType.getItems().add("тимчасово виконуючий обов'язки");

            TextField headField = new TextField();

            headType.setOnAction(e -> {
                String selectedOption = headType.getValue();
                if (selectedOption == "директор") {
                    _Company _company = dbUtil.getCompanyInfo();
                    headField.setText(_company.getCeo());
                    headField.setDisable(true);
                }
                else {
                    headField.clear();
                    headField.setDisable(false);
                }
            });

            if(selectedOrder != null) {
                _Company _company = dbUtil.getCompanyInfo();
                datePickerOrderDate.setValue(selectedOrder.getOrderDate());
                datePickerStart.setValue(selectedOrder.getStartDate());
                datePickerEnd.setValue(selectedOrder.getEndDate());
                routeField.setText(selectedOrder.getRoute());
                goalField.setText(selectedOrder.getGoal());
                moneyField.setText(String.valueOf(selectedOrder.getMoney()));
                headType.setValue((_company.getCeo().equals(selectedOrder.getHead()))?"директор":"тимчасово виконуючий обов'язки");
                headField.setText(selectedOrder.getHead());
            }


            grid.add(new Label("Дата наказу:"), 0, 0);
            grid.add(datePickerOrderDate, 1, 0);
            grid.add(new Label("№ наказу:"), 0, 1);
            grid.add(orderNumberField, 1, 1);
            grid.add(new Label("Працівник:"), 0, 2);
            grid.add(worker, 1, 2);
            grid.add(new Label("Посада:"), 0, 3);
            grid.add(positionField, 1, 3);
            grid.add(new Label("Виїзд: дата:"), 0, 4);
            grid.add(datePickerStart, 1, 4);
            grid.add(new Label("Повернення: дата:"), 0, 5);
            grid.add(datePickerEnd, 1, 5);
            grid.add(new Label("Маршрут:"), 0, 6);
            grid.add(routeField, 1, 6);
            grid.add(new Label("Гроші/доба:"), 0, 7);
            grid.add(moneyField, 1, 7);
            grid.add(new Label("Мета"), 0, 8);
            grid.add(goalField, 1, 8);
            grid.add(new Label("Керівник"), 0, 9);
            grid.add(headType, 1, 9);
            grid.add(new Label("ПІБ керівника"), 0, 10);
            grid.add(headField, 1, 10);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( datePickerOrderDate.getValue() == null || isEmptyOrWhitespace(orderNumberField.getText()) ||
                        worker.getValue() == null || datePickerStart.getValue() == null ||
                        datePickerEnd.getValue() == null || isEmptyOrWhitespace(moneyField.getText()) || isEmptyOrWhitespace(routeField.getText()) ||
                        isEmptyOrWhitespace(goalField.getText())) {
                    Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                        double money = Double.parseDouble(moneyField.getText().replace(',', '.'));
                        _Order newOrder = new _Order(
                                datePickerOrderDate.getValue(),
                                orderNumberField.getText(),
                                workersT.get(worker.getValue()),
                                datePickerStart.getValue(),
                                datePickerEnd.getValue(),
                                routeField.getText(),
                                money,
                                goalField.getText(),
                                headField.getText()
                        );

                        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Додати наказ");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbUtil.addOrder(newOrder)) {
                                    mainPage.closeInternalWindow(windowTitle);
                                }
                                controller.updateValues();
                            }
                        });
                    } catch (NumberFormatException ex) {
                        Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані");
                        alert.showAndWait();
                    }
                }
            });
        }
    }
}
