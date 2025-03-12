package com.work.oblikpodorojlist.model;

public class _Company {
    private int id;
    private String name;
    private String address;
    private int code;
    private String ceo;
    private String accountant;
    private String typeFull;
    private String typeShort;

    private static _Company instance;

    public _Company(String name, String address, int code, String ceo, String accountant, String typeFull, String typeShort) {
        this.name = name;
        this.address = address;
        this.code = code;
        this.ceo = ceo;
        this.accountant = accountant;
        this.typeFull = typeFull;
        this.typeShort = typeShort;
    }

    public _Company() {
    }

    public static _Company getInstance() {
        if (instance == null) {
            instance = new _Company();
        }
        return instance;
    }

    public String getTypeFull() {
        return typeFull;
    }

    public void setTypeFull(String typeFull) {
        this.typeFull = typeFull;
    }

    public String getTypeShort() {
        return typeShort;
    }

    public void setTypeShort(String typeShort) {
        this.typeShort = typeShort;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getCeo() {
        return ceo;
    }

    public void setCeo(String ceo) {
        this.ceo = ceo;
    }

    public String getAccountant() {
        return accountant;
    }

    public void setAccountant(String accountant) {
        this.accountant = accountant;
    }

    @Override
    public String toString() {
        return "company{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", code=" + code +
                ", ceo='" + ceo + '\'' +
                ", accountant='" + accountant + '\'' +
                '}';
    }
}
