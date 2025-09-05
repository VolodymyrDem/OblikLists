package com.work.oblikpodorojlist.pages;

import com.work.oblikpodorojlist.utils.AlertsUtil;
import com.work.oblikpodorojlist.utils.DBUtil;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class AccountPage {
    public void StartSelectAccountPage(Stage accountStage) {
        DBUtil dbUtil = DBUtil.getInstance();
        String selectedCompany = dbUtil.getCompany();

        accountStage.setTitle("Оберіть користувача: " + selectedCompany);

        List<String> users = dbUtil.getUsers();
        users.add("Адміністратор");
        ListView<String> accountListView = new ListView<>();
        accountListView.getItems().addAll(users);

        accountListView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                String selectedUsername = accountListView.getSelectionModel().getSelectedItem();
                if(selectedUsername.equals("Адміністратор")) {
                    selectedUsername="root";
                }
                dbUtil.setUsername(selectedUsername);
                LoginPage nextPage = new LoginPage();
                nextPage.StartLoginPage(accountStage);
            }
        });

        accountListView.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                String selectedUsername = accountListView.getSelectionModel().getSelectedItem();
                if(selectedUsername != null) {
                    if(selectedUsername.equals("Адміністратор")) {
                        selectedUsername="root";
                    }
                    dbUtil.setUsername(selectedUsername);
                    LoginPage nextPage = new LoginPage();
                    nextPage.StartLoginPage(accountStage);
                }
            }
        });

        VBox buttons = new VBox(10);

        Button selectAccountButton = new Button("Обрати");
        selectAccountButton.setOnAction(e -> {
            String selectedUsername = accountListView.getSelectionModel().getSelectedItem();
            if (selectedUsername != null) {

                if(selectedUsername.equals("Адміністратор")) {
                    selectedUsername="root";
                }
                dbUtil.setUsername(selectedUsername);
                Stage stage = (Stage) selectAccountButton.getScene().getWindow();
                LoginPage nextPage = new LoginPage();
                nextPage.StartLoginPage(stage);

            } else {
                Alert a = AlertsUtil.ErrorAlert("Не обраний користувач", "Оберіть користувача, щоб продовжити");
                a.showAndWait();
            }
        });

        HBox buttonContainer = new HBox(selectAccountButton);
        buttonContainer.setSpacing(10);
        buttonContainer.setStyle("-fx-alignment: center;");

        VBox mainLayout = new VBox(15, accountListView, buttonContainer);
        mainLayout.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 20px;");
        mainLayout.setPrefSize(400, 350);

        Scene scene = new Scene(mainLayout, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/css/styleWhite.css").toExternalForm());

        accountStage.setScene(scene);
        accountStage.show();
    }
}

/*
Button createAccountButton = new Button("Створити користувача");
        createAccountButton.setOnAction(e -> {
            buttons.getChildren().clear();

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            TextField nameField = new TextField();
            grid.add(new Label("ПІБ:"), 0, 0);
            grid.add(nameField, 1, 0);

            Button saveButton = new Button("Зберегти");
            saveButton.setOnAction(event -> {
                dbManager.createUser(selectedCompany, nameField.getText());
                Stage stage = (Stage) saveButton.getScene().getWindow();
                selectAccount(selectedCompany, stage);
            });

            buttons.getChildren().addAll(grid, saveButton);
        });
 */
