package com.work.oblikpodorojlist.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {


    public static StringConverter<LocalDate> dateConverter(DateTimeFormatter fmt) {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(fmt) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty())
                        ? LocalDate.parse(string, fmt)
                        : null;
            }
        };
    }


    public static <S> Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> dateCellFactory(DateTimeFormatter fmt) {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(fmt));
            }
        };
    }
}
