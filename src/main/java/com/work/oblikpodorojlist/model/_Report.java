package com.work.oblikpodorojlist.model;

import java.time.LocalDate;

public class _Report {
    private int id;
    private int orderId;
    private String comments;
    private LocalDate date;

    public _Report() {
    }

    public _Report(int id, int orderId, String comments, LocalDate date) {
        this.id = id;
        this.orderId = orderId;
        this.comments = comments;
        this.date = date;
    }

    public _Report(int orderId, String comments, LocalDate date) {
        this.orderId = orderId;
        this.comments = comments;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
