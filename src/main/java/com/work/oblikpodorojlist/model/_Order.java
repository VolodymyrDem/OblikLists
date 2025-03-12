package com.work.oblikpodorojlist.model;

import java.time.LocalDate;

public class _Order {
    private int idOrder;
    private LocalDate orderDate;
    private String orderNumber;
    private int idWorker;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private String route;
    private double money;
    private String goal;
    private String head;

    public _Order() {
    }

    public _Order(LocalDate orderDate, String orderNumber, int idWorker, LocalDate startDate, LocalDate endDate, String route, double money, String goal, String head) {
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.idWorker = idWorker;
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.money = money;
        this.goal = goal;
        this.head = head;
    }

    public _Order(int idOrder, LocalDate orderDate, String orderNumber, int idWorker, LocalDate startDate, LocalDate endDate, String route, double money, String goal, String head) {
        this.idOrder = idOrder;
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.idWorker = idWorker;
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.money = money;
        this.goal = goal;
        this.head = head;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getIdWorker() {
        return idWorker;
    }

    public void setIdWorker(int idWorker) {
        this.idWorker = idWorker;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return  "idOrder=" + idOrder + "\n" +
                "orderDate=" + orderDate + "\n" +
                "orderNumber='" + orderNumber + "\n" +
                "startDate=" + startDate + "\n" +
                "endDate=" + endDate + "\n" +
                "route='" + route + "\n" +
                "money=" + money + "\n";
    }
}
