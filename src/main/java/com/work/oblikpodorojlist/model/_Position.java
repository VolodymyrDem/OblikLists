package com.work.oblikpodorojlist.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class _Position {
    private int id;
    private String NameN;
    private String NameR;

    public _Position() {}
    public _Position(int id, String nameN, String nameR) {
        this.id = id;
        this.NameN = nameN;
        this.NameR = nameR;
    }
    public _Position(String nameN, String nameR) {
        this.NameN = nameN;
        this.NameR = nameR;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNameN() { return NameN; }
    public void setNameN(String nameN) { this.NameN = nameN; }
    public String getNameR() { return NameR; }
    public void setNameR(String nameR) { this.NameR = nameR; }

    @Override
    public String toString() {
        return String.format("id=%d, NameN='%s', NameR='%s'", id, NameN, NameR);
    }

    public String toSingleLine() {
        return String.format("id=%d, NameN=%s, NameR=%s", id, NameN, NameR);
    }

    public Map<String, String[]> diff(_Position other) {
        Map<String, String[]> changes = new LinkedHashMap<>();
        if (!Objects.equals(NameN, other.NameN)) {
            changes.put("NameN", new String[]{NameN, other.NameN});
        }
        if (!Objects.equals(NameR, other.NameR)) {
            changes.put("NameR", new String[]{NameR, other.NameR});
        }
        return changes;
    }
}

