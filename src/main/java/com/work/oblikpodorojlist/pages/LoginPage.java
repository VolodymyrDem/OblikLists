package com.work.oblikpodorojlist.pages;

import com.work.oblikpodorojlist.managers.Alerts;
import com.work.oblikpodorojlist.managers.DBManager;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginPage {
    private String selectedCompany;
    private String selectedUsername;
    public void StartLoginPage(DBManager dbManager, Stage loginStage) {
        selectedCompany = dbManager.getCompany();
        selectedUsername = dbManager.getUsername();

        loginStage.setTitle(selectedCompany+": Введіть пароль до користувача " + selectedUsername);

        PasswordField passwordField = new PasswordField();
        Label passwordL = new Label("Пароль:");

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                dbManager.setPassword(passwordField.getText());
                if(dbManager.tryConnection()) {
                    MainPage mainPage = new MainPage();
                    mainPage.StartMainPage(dbManager, loginStage);
                }
                else {
                    StartLoginPage(dbManager, loginStage);
                }
            }
        });

        Button loginButton = new Button("Увійти");
        loginButton.setOnAction(event -> {
            dbManager.setPassword(passwordField.getText());
            if(dbManager.tryConnection()) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                MainPage mainPage = new MainPage();
                mainPage.StartMainPage(dbManager, stage);
            }
            else {
                Alerts.ErrorAlert("Невірний пароль", "Перевірте правильність введеня даних").showAndWait();
                StartLoginPage(dbManager, loginStage);
            }
        });

        HBox buttonContainer = new HBox(passwordL, passwordField);
        buttonContainer.setSpacing(10);
        buttonContainer.setStyle("-fx-alignment: center;");
        VBox vBox = new VBox(10, buttonContainer, loginButton);
        vBox.setStyle("-fx-background-color: #F8F9FA;-fx-alignment: center;");

        Scene scene = new Scene(vBox,500, 400);
        scene.getStylesheets().add(getClass().getResource("/css/styleWhite.css").toExternalForm());
        loginStage.setScene(scene);
        loginStage.show();
    }
}
