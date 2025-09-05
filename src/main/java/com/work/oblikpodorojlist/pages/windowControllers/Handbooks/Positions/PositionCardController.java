package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Positions;

import com.work.oblikpodorojlist.model._Car;
import com.work.oblikpodorojlist.utils.AlertsUtil;
import com.work.oblikpodorojlist.utils.DBUtil;
import com.work.oblikpodorojlist.pages.MainPage;
import com.work.oblikpodorojlist.pages.windowControllers.WindowController;
import com.work.oblikpodorojlist.model._Position;
import com.work.oblikpodorojlist.utils.LoggerUtil;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PositionCardController extends WindowController {
    private MainPage mainPage;
    private DBUtil dbUtil;
    private boolean isEditing;
    private PositionsHandbookController controller;
    private GridPane grid;
    private String windowTitle;
    private _Position position;
    private TextField nameNField;
    private TextField nameRField;
    private static final Logger logger = LoggerUtil.getLogger();

    public PositionCardController() {}

    public void openWindow( _Position selectedPosition,
                           PositionsHandbookController controller) {
        isEditing = (selectedPosition != null);
        windowTitle = isEditing ? "Редагувати посаду" : "Додати посаду";
        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;

        this.controller = controller;
        grid = new GridPane();
        dbUtil = DBUtil.getInstance();

        nameNField = new TextField();
        nameRField = new TextField();

        position = selectedPosition;

        if (isEditing) {
            nameNField.setText(selectedPosition.getNameN());
            nameRField.setText(selectedPosition.getNameR());
        }

        grid = buildGridDouble(
                new Label("Назва (називний відмінок):"), nameNField,
                new Label("Назва (родовий відмінок):"), nameRField
        );

        Button btn = new Button("Зберегти");
        VBox vbox = new VBox(grid, btn);

        mainPage.openInternalWindow(vbox, windowTitle, false);

        btn.setOnAction(e -> {
            handleAction(isEditing);
        });
    }

    private void handleAction(boolean isEditing) {
        List<String> errors = validateInput();
        if (!errors.isEmpty()) {
            String msg = String.join("\n", errors);

            Alert alert = AlertsUtil.ErrorAlert("Error", msg);
            alert.showAndWait();

            logger.warning((isEditing ? "While editing car " : "While adding car ")
                    + (position != null ? position.getId() + " " + position.getNameN() : "")
                    + " Errors:\n" + msg);
            return;
        }

        String nameN = nameNField.getText();
        String nameR = nameRField.getText();

        _Position tempPos;
        if(isEditing)
            tempPos = new _Position(position.getId(), nameN, nameR);
        else
            tempPos =  new _Position(nameN, nameR);


        Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing ? "Редагувати посаду" : "Додати посаду");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (isEditing)
                    dbUtil.changePosition(tempPos);
                else
                    dbUtil.addPosition(tempPos);

                makeLog(isEditing, tempPos);

                mainPage.closeInternalWindow(windowTitle);
                controller.updateValues();
            }

        });
    }

    private List<String> validateInput() {
        List<String> errors = new ArrayList<>();

        if (nameRField.getText().isBlank())
            errors.add("Поле «Назва Р.В.» не може бути порожнім");

        if (nameNField.getText().isBlank())
            errors.add("Поле «Назва Н.В.» не може бути порожнім");

        return errors;
    }

    private void makeLog(boolean isEditing, _Position pos) {
        if(isEditing) {
            Map<String,String[]> diffs = position.diff(pos);
            if (diffs.isEmpty()) {
                logger.info("Edited position " + position.getId() + " " + position.getNameN() + " — no changes");
            } else {
                String changes = diffs.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue()[0] + "→" + e.getValue()[1])
                        .collect(Collectors.joining(", "));
                logger.info("Edited position " + position.getId() + " " + position.getNameN() + " — changes: " + changes);
            }
        }
        else {
            logger.info("Added position: " + pos.toSingleLine());
        }
    }

}
