package com.work.oblikpodorojlist.pages.windowControllers.Handbooks.Positions;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model._Position;
import com.work.oblikpodorojlist.pages.MainPage;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class EditPositionController {
    private MainPage mainPage ;
    private DBManager dbManager;
    private PositionsHandbookController controller;
    private static EditPositionController instance;

    public EditPositionController(){}

    public void openWindow(_Position selectedPosition, PositionsHandbookController controller) {
        String windowTitle = "Редагувати посаду";
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

            TextField nameNField = new TextField(selectedPosition.getNameN());
            TextField nameRField = new TextField(selectedPosition.getNameR());

            grid.add(new Label("Назва(називний відмінок):"), 0, 0);
            grid.add(nameNField, 1, 0);
            grid.add(new Label("Назва(родовий відмінок):"), 0, 1);
            grid.add(nameRField, 1, 1);

            Button saveButton = new Button("Зберегти");


            VBox vbox = new VBox();
            vbox.getChildren().addAll(grid, saveButton);

            StackPane internalWindow = mainPage.openInternalWindow(vbox, windowTitle, false);

            saveButton.setOnAction(e ->{
                if ( isEmptyOrWhitespace(nameNField.getText()) || isEmptyOrWhitespace(nameRField.getText())) {
                    Alert alert = Alerts.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані");
                    alert.showAndWait();
                } else {
                    try {
                       _Position position = new _Position(selectedPosition.getId(), nameNField.getText(), nameRField.getText());

                        Alert confirmationAlert = Alerts.ConfirmAlert("Підтвердіть операцію", "Редагувати посаду");
                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if(dbManager.changePosition(position)) {
                                    mainPage.closeInternalWindow(windowTitle);
                                }
                                controller.updateValues();
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
    private boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }
}
