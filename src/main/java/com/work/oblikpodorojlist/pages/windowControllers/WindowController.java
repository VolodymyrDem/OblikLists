package com.work.oblikpodorojlist.pages.windowControllers;

import com.work.oblikpodorojlist.model.FuelUsage;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public abstract class WindowController {
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


    protected boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }
    protected int extractNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isEmpty()) {
            return Integer.MAX_VALUE; // Якщо порожнє значення, відправити в кінець
        }
        // Виділяємо початкове число з наказу
        String numPart = orderNumber.replaceAll("^(\\d+).*", "$1");
        try {
            return Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // Якщо не вдалося розпарсити, відправити в кінець
        }
    }

}
