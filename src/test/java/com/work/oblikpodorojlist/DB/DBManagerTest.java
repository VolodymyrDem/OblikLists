package com.work.oblikpodorojlist.DB;

import com.work.oblikpodorojlist.managers.DBManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DBManagerTest {

    @Test
    void createAndRemoveUser() {
        DBManager dbManager = new DBManager();
        dbManager.setCompany("company1");
        dbManager.setUsername("root");
        dbManager.setPassword("0303");

        dbManager.createCompany("test", "Kyivyt", "089960", "Viktor Step", "Lada Typ", "TOV", "TOVARISTVO");


    }
}