package com.work.oblikpodorojlist.model;

public class _Position {
    private int id;
    private String NameN;
    private String NameR;

    public _Position() {
    }

    public _Position(int id, String nameN, String nameR) {
        this.id = id;
        NameN = nameN;
        NameR = nameR;
    }

    public _Position(String nameN, String nameR) {
        NameN = nameN;
        NameR = nameR;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameN() {
        return NameN;
    }

    public void setNameN(String nameN) {
        NameN = nameN;
    }

    public String getNameR() {
        return NameR;
    }

    public void setNameR(String nameR) {
        NameR = nameR;
    }
}
