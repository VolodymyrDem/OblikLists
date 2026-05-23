package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Workers;

import com.work.oblikpodorojlist.util.AlertsUtil;
import com.work.oblikpodorojlist.util.DBUtil;
import com.work.oblikpodorojlist.model._Position;
import com.work.oblikpodorojlist.model._Worker;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerCardController extends WindowController {
    private MainPage mainPage;
    private DBUtil dbUtil;
    private boolean isEditing;
    private GridPane grid;
    private WorkersHandbookController controller;

    public WorkerCardController() {}

    public void openWindow(boolean isRemoving,
                           _Worker selectedWorker,
                           WorkersHandbookController controller) {
        this.controller = controller;
        this.isEditing = selectedWorker != null && !isRemoving;

        String title;
        if (isRemoving) {
            title = "Звільнити працівника";
        } else {
            title = isEditing ? "Редагувати працівника" : "Додати працівника";
        }
        grid = new GridPane();
        mainPage = MainPage.getInstance();
        if (mainPage.openWindows.containsKey(title)) {
            StackPane existing = mainPage.openWindows.get(title);
            existing.toFront();
            if (!existing.isVisible()) existing.setVisible(true);
            return;
        }

        dbUtil = DBUtil.getInstance();

        TextField nameNField = new TextField();
        TextField nameRField = new TextField();
        TextField nameDField = new TextField();
        ComboBox<String> positionCombo = new ComboBox<>();
        Map<String,Integer> positionsMap = new HashMap<>();
        List<_Position> positions = dbUtil.getPositions();
        for(_Position p : positions) {
            positionsMap.put(p.getNameN(), p.getId());
            positionCombo.getItems().add(p.getNameN());
        }

        TextField licenceField = new TextField();
        DatePicker datePickerStart = new DatePicker();
        DatePicker datePickerEnd = new DatePicker();
        TextField startOrderField = new TextField();
        TextField endOrderField = new TextField();

        if (isEditing || isRemoving) {
            nameNField.setText(selectedWorker.getNameN());
            nameRField.setText(selectedWorker.getNameR());
            nameDField.setText(selectedWorker.getNameD());
            positionCombo.setValue(selectedWorker.getPositionN());
            licenceField.setText(selectedWorker.getDrivingLicense());
            datePickerStart.setValue(selectedWorker.getStartDate());
            startOrderField.setText(selectedWorker.getStartOrderNumber());
            if (selectedWorker.getEndDate() != null) {
                datePickerEnd.setValue(selectedWorker.getEndDate());
                endOrderField.setText(selectedWorker.getEndOrderNumber());
            }
        }

        if (isRemoving) {
            datePickerEnd.setValue(LocalDate.now());
            nameNField.setDisable(true);
            nameRField.setDisable(true);
            nameDField.setDisable(true);
            positionCombo.setDisable(true);
            licenceField.setDisable(true);
            datePickerStart.setDisable(true);
            startOrderField.setDisable(true);
        }

        // Побудова форми за допомогою buildGridDouble
        grid = buildGridDouble(
                new Label("ПІБ (називний):"), nameNField,
                new Label("ПІБ (родовий):"), nameRField,
                new Label("ПІБ (давальний):"), nameDField,
                new Label("Посада:"), positionCombo,
                new Label("Водійське посвідчення:"), licenceField,
                new Label("Дата найму:"), datePickerStart,
                new Label("Номер наказу найму:"), startOrderField,
                new Label("Дата звільнення:"), datePickerEnd,
                new Label("Номер наказу звільнення:"), endOrderField
        );

        Button saveBtn = new Button("Зберегти");
        VBox vbox = new VBox(grid, saveBtn);
        StackPane internal = mainPage.openInternalWindow(vbox, title, false);

        saveBtn.setOnAction(e -> {
            // Валідація
            if (!isRemoving) {
                boolean missing = isEmptyOrWhitespace(nameNField.getText()) ||
                        isEmptyOrWhitespace(nameRField.getText()) ||
                        isEmptyOrWhitespace(nameDField.getText()) ||
                        positionCombo.getValue() == null ||
                        isEmptyOrWhitespace(licenceField.getText()) ||
                        datePickerStart.getValue() == null ||
                        isEmptyOrWhitespace(startOrderField.getText());
                if (missing ||
                        (datePickerEnd.getValue() != null && isEmptyOrWhitespace(endOrderField.getText())) ||
                        (datePickerEnd.getValue() == null && !isEmptyOrWhitespace(endOrderField.getText()))) {
                    Alert alert = AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                    return;
                }
            }

            Alert confirm = AlertsUtil.ConfirmAlert(
                    "Підтвердіть операцію",
                    isRemoving ? "Звільнити працівника" : (isEditing ? "Редагувати працівника" : "Додати працівника")
            );
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    if (isRemoving) {
                        selectedWorker.setEndDate(datePickerEnd.getValue());
                        selectedWorker.setEndOrderNumber(endOrderField.getText());
                        dbUtil.removeWorker(selectedWorker);
                    } else {
                        _Worker w = isEditing
                                ? new _Worker(selectedWorker.getId(), nameNField.getText(), nameRField.getText(),
                                nameDField.getText(), positionsMap.get(positionCombo.getValue()),
                                licenceField.getText(), datePickerStart.getValue(), startOrderField.getText())
                                : new _Worker(nameNField.getText(), nameRField.getText(), nameDField.getText(),
                                positionsMap.get(positionCombo.getValue()), licenceField.getText(),
                                datePickerStart.getValue(), startOrderField.getText());
                        w.setPositionN(positionCombo.getValue());
                        w.setPositionR(dbUtil.getPositionNameR(positionsMap.get(positionCombo.getValue())));
                        if (datePickerEnd.getValue() != null) {
                            w.setEndDate(datePickerEnd.getValue());
                            w.setEndOrderNumber(endOrderField.getText());
                        }
                        if (isEditing) dbUtil.changeWorker(w);
                        else dbUtil.addWorker(w);
                    }
                    mainPage.closeInternalWindow(title);
                    controller.updateValues();
                }
            });
        });
    }
}
