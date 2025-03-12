package com.work.oblikpodorojlist.model;

import java.time.LocalDate;

public class _List {
    private int id;
    private int number;
    private int idCar;
    private int idOrder;
    private int idWorker;
    private double startMileage;
    private double startFuel;
    private double endMileage;
    private double endFuel;
    private double refuel;
    private boolean done;
    private LocalDate startDate;
    private LocalDate endDate;
    private String route;
    private String goal;

    public _List() {
    }

    public _List(int idCar, int idOrder, int idWorker, LocalDate startDate, LocalDate endDate, String route, String goal) {
        this.idCar = idCar;
        this.idOrder = idOrder;
        this.idWorker = idWorker;
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.goal = goal;
    }

    public _List(int id, int idCar, int idOrder, int idWorker, LocalDate startDate, LocalDate endDate, String route, String goal) {
        this.id = id;
        this.idCar = idCar;
        this.idOrder = idOrder;
        this.idWorker = idWorker;
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.goal = goal;
    }

    public _List(int number, int idOrder, int idCar, double startMileage, double startFuel, boolean done) {
        this.number = number;
        this.idOrder = idOrder;
        this.idCar = idCar;
        this.startMileage = startMileage;
        this.startFuel = startFuel;
        this.done = done;

    }

    public _List(int number, int idCar, int idWorker, double startMileage, double startFuel, boolean done, LocalDate startDate, LocalDate endDate, String route, String goal) {
        this.number = number;
        this.idCar = idCar;
        this.idWorker = idWorker;
        this.startMileage = startMileage;
        this.startFuel = startFuel;
        this.done = done;
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.goal = goal;
    }

    public _List(int id, int number, int idOrder, int idCar, double startMileage, double startFuel,  boolean done) {
        this.id = id;
        this.number = number;
        this.idOrder = idOrder;
        this.idCar = idCar;
        this.startMileage = startMileage;
        this.startFuel = startFuel;
        this.done = done;

    }

    public _List(int id, int number, int idCar, int idWorker, double startMileage, double startFuel, boolean done, LocalDate startDate, LocalDate endDate, String route, String goal) {
        this.id = id;
        this.number = number;
        this.idCar = idCar;
        this.idWorker = idWorker;
        this.startMileage = startMileage;
        this.startFuel = startFuel;
        this.done = done;
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.goal = goal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getIdCar() {
        return idCar;
    }

    public void setIdCar(int idCar) {
        this.idCar = idCar;
    }

    public int getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public int getIdWorker() {
        return idWorker;
    }

    public void setIdWorker(int idWorker) {
        this.idWorker = idWorker;
    }

    public double getStartMileage() {
        return startMileage;
    }

    public void setStartMileage(double startMileage) {
        this.startMileage = startMileage;
    }

    public double getStartFuel() {
        return startFuel;
    }

    public void setStartFuel(double startFuel) {
        this.startFuel = startFuel;
    }

    public double getEndMileage() {
        return endMileage;
    }

    public void setEndMileage(double endMileage) {
        this.endMileage = endMileage;
    }

    public double getEndFuel() {
        return endFuel;
    }

    public void setEndFuel(double endFuel) {
        this.endFuel = endFuel;
    }

    public double getRefuel() {
        return refuel;
    }

    public void setRefuel(double refuel) {
        this.refuel = refuel;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
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

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    @Override
    public String toString() {
        return "_List{" +
                "id=" + id +
                ", number=" + number +
                ", idCar=" + idCar +
                ", idOrder=" + idOrder +
                ", idWorker=" + idWorker +
                ", startMileage=" + startMileage +
                ", startFuel=" + startFuel +
                ", endMileage=" + endMileage +
                ", endFuel=" + endFuel +
                ", refuel=" + refuel +
                ", done=" + done +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", route='" + route + '\'' +
                ", goal='" + goal + '\'' +
                '}';
    }
}
