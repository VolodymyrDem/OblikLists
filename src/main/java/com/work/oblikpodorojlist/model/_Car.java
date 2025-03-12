package com.work.oblikpodorojlist.model;

import java.time.LocalDate;

public class _Car {
    private int idCar;
    private String number;
    private String model;
    private String fuelType;
    private double fuelUsage;
    private double engineVolume;
    private LocalDate startDate;
    private String startOrderNumber;
    private LocalDate endDate;
    private String endOrderNumber;
    private boolean valid;
    private double startFuel;
    private double startMileage;

    public _Car() {
    }

    public _Car(int idCar, String number, String model, String fuelType, double fuelUsage, double engineVolume, LocalDate startDate, String startOrderNumber, LocalDate endDate, String endOrderNumber, boolean valid) {
        this.idCar = idCar;
        this.number = number;
        this.model = model;
        this.fuelType = fuelType;
        this.fuelUsage = fuelUsage;
        this.engineVolume = engineVolume;
        this.startDate = startDate;
        this.startOrderNumber = startOrderNumber;
        this.endDate = endDate;
        this.endOrderNumber = endOrderNumber;
        this.valid = valid;
    }

    public _Car(int idCar, String number, String model, String fuelType, double fuelUsage, double engineVolume, LocalDate startDate, String startOrderNumber, LocalDate endDate, String endOrderNumber, boolean valid, double startFuel, double startMileage) {
        this.idCar = idCar;
        this.number = number;
        this.model = model;
        this.fuelType = fuelType;
        this.fuelUsage = fuelUsage;
        this.engineVolume = engineVolume;
        this.startDate = startDate;
        this.startOrderNumber = startOrderNumber;
        this.endDate = endDate;
        this.endOrderNumber = endOrderNumber;
        this.valid = valid;
        this.startFuel = startFuel;
        this.startMileage = startMileage;
    }

    public _Car(String number, String model, String fuelType, double fuelUsage, double engineVolume, LocalDate startDate, String startOrderNumber, double startFuel, double startMileage) {
        this.number = number;
        this.model = model;
        this.fuelType = fuelType;
        this.fuelUsage = fuelUsage;
        this.engineVolume = engineVolume;
        this.startDate = startDate;
        this.startOrderNumber = startOrderNumber;
        this.startFuel = startFuel;
        this.startMileage = startMileage;
    }

    public _Car(String number, String model, String fuelType, double fuelUsage, double engineVolume, LocalDate startDate, String startOrderNumber) {
        this.number = number;
        this.model = model;
        this.fuelType = fuelType;
        this.fuelUsage = fuelUsage;
        this.engineVolume = engineVolume;
        this.startDate = startDate;
        this.startOrderNumber = startOrderNumber;
    }

    public _Car(String number, String model, String fuelType, double fuelUsage, double engineVolume, LocalDate startDate, String startOrderNumber, LocalDate endDate, String endOrderNumber, boolean valid) {
        this.number = number;
        this.model = model;
        this.fuelType = fuelType;
        this.fuelUsage = fuelUsage;
        this.engineVolume = engineVolume;
        this.startDate = startDate;
        this.startOrderNumber = startOrderNumber;
        this.endDate = endDate;
        this.endOrderNumber = endOrderNumber;
        this.valid = valid;
    }

    public double getStartFuel() {
        return startFuel;
    }

    public void setStartFuel(double startFuel) {
        this.startFuel = startFuel;
    }

    public double getStartMileage() {
        return startMileage;
    }

    public void setStartMileage(double startMileage) {
        this.startMileage = startMileage;
    }

    public int getIdCar() {
        return idCar;
    }

    public void setIdCar(int idCar) {
        this.idCar = idCar;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public double getFuelUsage() {
        return fuelUsage;
    }

    public void setFuelUsage(double fuelUsage) {
        this.fuelUsage = fuelUsage;
    }

    public double getEngineVolume() {
        return engineVolume;
    }

    public void setEngineVolume(double engineVolume) {
        this.engineVolume = engineVolume;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getStartOrderNumber() {
        return startOrderNumber;
    }

    public void setStartOrderNumber(String startOrderNumber) {
        this.startOrderNumber = startOrderNumber;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getEndOrderNumber() {
        return endOrderNumber;
    }

    public void setEndOrderNumber(String endOrderNumber) {
        this.endOrderNumber = endOrderNumber;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return "номер=" + number + '\n' +
                "модель='" + model + '\n' +
                "тип палива='" + fuelType + '\n' +
                "використання=" + fuelUsage + '\n' +
                "об'єм двигуна=" + engineVolume + '\n' +
                "дата початку=" + startDate + '\n' +
                "номер наказу початку='" + startOrderNumber + '\n' +
                "дата закінчення=" + endDate + '\n' +
                "номер наказу закінчення='" + endOrderNumber + '\n' +
                "актуальність=" + valid;
    }
}
