package com.work.oblikpodorojlist.model;

import java.time.LocalDate;

public class _Worker {
    private int id;
    private String nameN;
    private String nameR;
    private String nameD;
    private String positionN;
    private String positionR;
    private int positionId;
    private String drivingLicense;
    private LocalDate startDate;
    private String startOrderNumber;
    private LocalDate endDate;
    private String endOrderNumber;
    private boolean valid;

    public _Worker() {
    }
    public _Worker(String nameN, String nameR, String nameD, int positionId, String drivingLicense, LocalDate startDate, String startOrderNumber) {
        this.nameN = nameN;
        this.nameR = nameR;
        this.nameD = nameD;
        this.positionId = positionId;
        this.drivingLicense = drivingLicense;
        this.startDate = startDate;
        this.startOrderNumber = startOrderNumber;
        this.valid = true;
        this.endDate = null;
        this.endOrderNumber = null;
    }

    public _Worker(int id, String nameN, String nameR,String nameD, int positionId, String drivingLicense, LocalDate startDate, String startOrderNumber) {
        this.id = id;
        this.nameN = nameN;
        this.nameR = nameR;
        this.nameD = nameD;
        this.positionId = positionId;
        this.drivingLicense = drivingLicense;
        this.startDate = startDate;
        this.startOrderNumber = startOrderNumber;
        this.valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getPositionN() {
        return positionN;
    }

    public void setPositionN(String positionN) {
        this.positionN = positionN;
    }

    public String getPositionR() {
        return positionR;
    }

    public void setPositionR(String positionR) {
        this.positionR = positionR;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameN() {
        return nameN;
    }

    public void setNameN(String nameN) {
        this.nameN = nameN;
    }

    public String getNameR() {
        return nameR;
    }

    public String getNameD() {
        return nameD;
    }

    public void setNameD(String nameD) {
        this.nameD = nameD;
    }

    public void setNameR(String nameR) {
        this.nameR = nameR;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
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
}

