package com.work.oblikpodorojlist.model;

import java.time.LocalDate;

public class _FuelUsage {
    private LocalDate startDate;
    private LocalDate endDate;
    private String carNumber;
    private Double mileage;
    private Double fuelFact;
    private Double fuelNorm;
    private Double overUse;
    private Double underUse;

    public _FuelUsage(LocalDate startDate, LocalDate endDate, String carNumber, Double mileage, Double fuelFact, Double fuelNorm) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.carNumber = carNumber;
        this.mileage = mileage;
        this.fuelFact = fuelFact;
        this.fuelNorm = fuelNorm;

        if(fuelFact > fuelNorm) {
            overUse = fuelFact - fuelNorm;
            underUse = null;
        } else if(fuelFact == fuelNorm) {
            underUse = null;
            overUse = null;
        } else {
            overUse = null;
            underUse = fuelNorm - fuelFact;
        }
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

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public Double getFuelFact() {
        return fuelFact;
    }

    public void setFuelFact(Double fuelFact) {
        this.fuelFact = fuelFact;
    }

    public Double getFuelNorm() {
        return fuelNorm;
    }

    public void setFuelNorm(Double fuelNorm) {
        this.fuelNorm = fuelNorm;
    }

    public Double getOverUse() {
        return overUse;
    }

    public void setOverUse(Double overUse) {
        this.overUse = overUse;
    }

    public Double getUnderUse() {
        return underUse;
    }

    public void setUnderUse(Double underUse) {
        this.underUse = underUse;
    }
}
