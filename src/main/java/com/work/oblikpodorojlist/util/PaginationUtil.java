package com.work.oblikpodorojlist.util;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PaginationUtil {

    private final Pagination pagination;
    private final TextField pageField;
    private final Label pageInfo;

    public PaginationUtil(Pagination pagination) {
        this.pagination = pagination;
        this.pageField = new TextField();
        this.pageInfo = new Label();
        setupPagination();
    }

    private void setupPagination() {
        // Налаштування текстового поля для введення номера сторінки
        pageField.setPrefWidth(60);
        pageField.setPromptText("№");
        pageField.setAlignment(Pos.CENTER);

        // Дозволити тільки цифри
        pageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pageField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Обробка Enter для переходу на сторінку
        pageField.setOnAction(event -> goToPage());

        // Оновлення інформації про сторінки
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updatePageInfo();
        });

        pagination.pageCountProperty().addListener((obs, oldVal, newVal) -> {
            updatePageInfo();
        });

        updatePageInfo();
    }

    private void goToPage() {
        try {
            String text = pageField.getText().trim();
            if (text.isEmpty()) {
                return;
            }

            int pageNumber = Integer.parseInt(text);
            int maxPages = pagination.getPageCount();

            // Перевірка меж
            if (pageNumber < 1) {
                pageNumber = 1;
                pageField.setText("1");
            } else if (pageNumber > maxPages) {
                pageNumber = maxPages;
                pageField.setText(String.valueOf(maxPages));
            }

            // Встановлення сторінки (індекс з 0)
            pagination.setCurrentPageIndex(pageNumber - 1);

        } catch (NumberFormatException e) {
            pageField.setText("");
        }
    }

    private void updatePageInfo() {
        int currentPage = pagination.getCurrentPageIndex() + 1;
        int totalPages = pagination.getPageCount();

        if (totalPages == 0) {
            pageInfo.setText("Немає сторінок");
            pageField.setDisable(true);
        } else {
            pageInfo.setText("з " + totalPages);
            pageField.setDisable(false);

            // Автоматично оновити поле, якщо воно порожнє
            if (pageField.getText().isEmpty()) {
                pageField.setText(String.valueOf(currentPage));
            }
        }
    }

    /**
     * Створює панель управління пагінацією з кнопками навігації та полем введення
     * @return HBox з елементами управління
     */
    public HBox createPaginationControls() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        // Кнопка "На початок"
        Button firstButton = new Button("<<");
        firstButton.setTooltip(new Tooltip("Перша сторінка"));
        firstButton.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            pageField.setText("1");
        });

        // Кнопка "Назад"
        Button prevButton = new Button("<");
        prevButton.setTooltip(new Tooltip("Попередня сторінка"));
        prevButton.setOnAction(e -> {
            int current = pagination.getCurrentPageIndex();
            if (current > 0) {
                pagination.setCurrentPageIndex(current - 1);
                pageField.setText(String.valueOf(current));
            }
        });

        // Кнопка "Вперед"
        Button nextButton = new Button(">");
        nextButton.setTooltip(new Tooltip("Наступна сторінка"));
        nextButton.setOnAction(e -> {
            int current = pagination.getCurrentPageIndex();
            if (current < pagination.getPageCount() - 1) {
                pagination.setCurrentPageIndex(current + 1);
                pageField.setText(String.valueOf(current + 2));
            }
        });

        // Кнопка "В кінець"
        Button lastButton = new Button(">>");
        lastButton.setTooltip(new Tooltip("Остання сторінка"));
        lastButton.setOnAction(e -> {
            int lastPage = pagination.getPageCount() - 1;
            pagination.setCurrentPageIndex(lastPage);
            pageField.setText(String.valueOf(pagination.getPageCount()));
        });

        // Кнопка "Перейти"
        Button goButton = new Button("→");
        goButton.setTooltip(new Tooltip("Перейти на сторінку"));
        goButton.setOnAction(e -> goToPage());

        // Додати стилі для кнопок
        firstButton.getStyleClass().add("pagination-button");
        prevButton.getStyleClass().add("pagination-button");
        nextButton.getStyleClass().add("pagination-button");
        lastButton.getStyleClass().add("pagination-button");
        goButton.getStyleClass().add("pagination-button");
        pageField.getStyleClass().add("pagination-field");
        pageInfo.getStyleClass().add("pagination-info");

        // Вимкнути кнопки при необхідності
        pagination.currentPageIndexProperty().addListener((obs, old, newVal) -> {
            int current = newVal.intValue();
            int max = pagination.getPageCount() - 1;

            firstButton.setDisable(current == 0);
            prevButton.setDisable(current == 0);
            nextButton.setDisable(current == max);
            lastButton.setDisable(current == max);
        });

        Label pageLabel = new Label("Сторінка:");

        controls.getChildren().addAll(
            firstButton,
            prevButton,
            pageLabel,
            pageField,
            pageInfo,
            goButton,
            nextButton,
            lastButton
        );

        return controls;
    }

    /**
     * Створює повний контейнер з пагінацією та елементами управління
     * @return VBox з пагінацією та панеллю управління
     */
    public VBox createPaginationContainer() {
        VBox container = new VBox(10);
        VBox.setVgrow(pagination, Priority.ALWAYS);

        HBox controls = createPaginationControls();

        // Приховати стандартні кнопки пагінації, якщо потрібно
        // pagination.setVisible(false);

        container.getChildren().addAll(pagination, controls);
        return container;
    }

    /**
     * Оновити поле номера сторінки відповідно до поточної сторінки
     */
    public void updatePageField() {
        int currentPage = pagination.getCurrentPageIndex() + 1;
        pageField.setText(String.valueOf(currentPage));
    }

    /**
     * Отримати текстове поле для номера сторінки
     */
    public TextField getPageField() {
        return pageField;
    }

    /**
     * Отримати мітку з інформацією про сторінки
     */
    public Label getPageInfo() {
        return pageInfo;
    }
}

