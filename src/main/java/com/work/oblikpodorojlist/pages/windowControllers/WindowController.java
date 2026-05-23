package com.work.oblikpodorojlist.pages.windowControllers;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public abstract class WindowController {
    protected final int rowsPerPage = 25;
    protected DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    protected void openFolder(String path) {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                File folder = new File(path);

                if (folder.exists() && folder.isDirectory()) {
                    try {
                        Desktop.getDesktop().open(folder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Папка не існує: " + path);
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public static GridPane buildGridDouble(Node... elements) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < elements.length; i++) {
            int row = i / 2;
            int col = i % 2;
            grid.add(elements[i], col, row);
        }

        return grid;
    }


    protected boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }
    protected int extractNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        String numPart = orderNumber.replaceAll("^(\\d+).*", "$1");
        try {
            return Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
    protected void moveTableDown(TableView<?> tableView) {
        tableView.scrollTo(tableView.getItems().size());
    }
}
