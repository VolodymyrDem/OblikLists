module com.work.oblikpodorojlist {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires org.apache.poi.ooxml;
    requires javafx.swing;
    requires org.kordamp.ikonli.fontawesome5;

    opens com.work.oblikpodorojlist to javafx.fxml;
    exports com.work.oblikpodorojlist;
    exports com.work.oblikpodorojlist.model;
    opens com.work.oblikpodorojlist.model to javafx.fxml;
    exports com.work.oblikpodorojlist.pages to javafx.graphics;
    exports com.work.oblikpodorojlist.managers;
    opens com.work.oblikpodorojlist.managers to javafx.fxml;
}