package com.work.oblikpodorojlist.pages;

import com.work.oblikpodorojlist.util.AlertsUtil;
import com.work.oblikpodorojlist.util.DBUtil;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class CompanyPage extends Application {
    DBUtil dbUtil;
    ObservableList<String> databases = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new javafx.scene.image.Image("/icon.png"));
        dbUtil = DBUtil.getInstance();
        primaryStage.setTitle("Компанія");
        TableView<String> tableView = new TableView<>();
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("Немає даних"));
        Button changeHost = new Button("Змінити адресу підключення");
        Button RootLogin = new Button("Адмін вхід");
        Button save = new Button("Зберегти");
        TextField hostField = new TextField();



        TableColumn<String, String> dbColumn = new TableColumn<>("Назва компанії");
        dbColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()));
        tableView.getColumns().add(dbColumn);
        getCompanies();
        tableView.getItems().addAll(databases);

        tableView.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                String selectedCompany = tableView.getSelectionModel().getSelectedItem();
                if (selectedCompany != null) {
                    dbUtil.setCompany(selectedCompany.split("\\s+")[0]);
                    AccountPage nextPage = new AccountPage();
                    nextPage.StartSelectAccountPage(primaryStage);
                } else {
                    Alert a = AlertsUtil.ErrorAlert("Не обрана компанія", "Оберіть компанію, щоб продовжити");
                    a.showAndWait();
                }
            }
        });


        tableView.setRowFactory(tv -> {
            TableRow<String> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String selectedCompany = row.getItem();
                    if (selectedCompany != null) {
                        dbUtil.setCompany(selectedCompany.split("\\s+")[0]);
                        AccountPage nextPage = new AccountPage();
                        nextPage.StartSelectAccountPage(primaryStage);
                    } else {
                        Alert a = AlertsUtil.ErrorAlert("Не обрана компанія", "Оберіть компанію, щоб продовжити");
                        a.showAndWait();
                    }
                }
            });

            return row;
        });

        Button selectDatabaseButton = new Button("Обрати");
        selectDatabaseButton.setOnAction(e -> {
            String selectedCompany = tableView.getSelectionModel().getSelectedItem();
            if (selectedCompany != null) {
                dbUtil.setCompany(selectedCompany.split("\\s+")[0]);
                Stage stage = (Stage) selectDatabaseButton.getScene().getWindow();
                AccountPage nextPage = new AccountPage();
                nextPage.StartSelectAccountPage(stage);
            } else {
                Alert a = AlertsUtil.ErrorAlert("Не обрана компанія", "Оберіть компанію, щоб продовжити");
                a.showAndWait();
            }
        });

        RootLogin.setOnAction(e->{
            String selectedUsername = "root";
            dbUtil.setUsername(selectedUsername);
            LoginPage nextPage = new LoginPage();
            nextPage.StartLoginPage(primaryStage);
        });

        HBox buttonContainer = new HBox(selectDatabaseButton, changeHost, RootLogin);
        buttonContainer.setSpacing(10);
        buttonContainer.setStyle("-fx-alignment: center;");

        VBox vbox = new VBox(10, tableView, buttonContainer);
        vbox.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 20px;");
        vbox.setPrefSize(400, 300);

        BorderPane layout = new BorderPane();
        layout.setCenter(vbox);

        changeHost.setOnAction(event -> {
            vbox.getChildren().addAll(hostField, save);
            hostField.setText(dbUtil.getHost());
        });

        save.setOnAction(event -> {
            dbUtil.setHost(hostField.getText());
            vbox.getChildren().removeAll(hostField, save);
            databases.clear();
            getCompanies();
            tableView.getItems().clear();
            tableView.getItems().addAll(databases);
        });

        Scene scene = new Scene(layout, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/css/styleWhite.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.show();
    }

    private void getCompanies() {
        try {
            databases.addAll(dbUtil.getCompanies());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
