package com.work.oblikpodorojlist.managers.Documents;

import com.work.oblikpodorojlist.managers.DBManager;
import com.work.oblikpodorojlist.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class DocumentsManagerTest {

    @Test
    void makeExcel() {
        DocumentsManager documentManager = new DocumentsManager();
        DBManager dbManager = new DBManager();
        dbManager.setUsername("root");
        dbManager.setPassword("0303");
        dbManager.setCompany("company1");
        List<_List> lists = dbManager.getLists();
        documentManager.createList(dbManager, lists.get(1));
    }
//    @Test
//    void documentTest() {
//        DocumentsManager documentManager = new DocumentsManager();
//        _Company comp = _Company.getInstance();
//        comp.setName("«ДІАЛІЗ МЕДИК»");
//        comp.setAddress("01015, Україна, м. Київ, вул. Лаврська, буд. 16");
//        comp.setCode(40477029);
//        comp.setAccountant("Світлана Кондрачук");
//        comp.setCeo("Генадій Ігнатенко");
//        _Worker work = new _Worker();
//        work.setName("Позняренко Вадим Олександрович");
//        documentManager.createOrder(comp, work, 1200.00, LocalDate.now(), LocalDate.of(2025, 2, 1), "Вінниця - Київ", "№ 280 – вд");
//    }
//
//    @Test
//    void ExcelTest() {
//        DocumentsManager documentManager = new DocumentsManager();
//        _Car car = new _Car();
//        car.setId(32);
//        car.setModel("Golf");
//        car.setFuelType("Diesel");
//        car.setFuelUsage(12);
//        car.setNumber("AR4545GT");
//        _User.getInstance().setUsername("test1");
//        _User.getInstance().setPassword("test1");
//        documentManager.createRegister(car);
//    }
//
//    @Test
//    void excelTest2() {
//        DocumentsManager documentManager = new DocumentsManager();
//        _Car car = new _Car();
//        car.setId(32);
//        car.setModel("Golf");
//        car.setFuelType("Diesel");
//        car.setFuelUsage(12);
//        car.setNumber("AR4545GT");
//
//        _Company comp = _Company.getInstance();
//        comp.setName("«ДІАЛІЗ МЕДИК»");
//        comp.setAddress("01015, Україна, м. Київ, вул. Лаврська, буд. 16");
//        comp.setCode(40477029);
//        comp.setAccountant("Світлана Кондрачук");
//        comp.setCeo("Генадій Ігнатенко");
//        _Worker work = new _Worker();
//        work.setName("Позняренко Вадим Олександрович");
//
//        _User.getInstance().setUsername("test1");
//        _User.getInstance().setPassword("test1");
//
//        documentManager.createList(car, work, comp, "Вінниця, Хмельницький", "перевезення медикаментів");
//
//
//    }
}