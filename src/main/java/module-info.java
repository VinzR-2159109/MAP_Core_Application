module be.uhasselt.dwi_application {
    // JavaFX dependencies
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    // UI Dependencies
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    // Database Dependencies
    requires java.naming;
    requires java.sql;
    requires org.jdbi.v3.core;
    requires org.jdbi.v3.sqlobject;

    // MQTT & JSON Parsing
    requires com.hivemq.client.mqtt;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.imaging;

    // ✅ Open packages for JavaFX & JDBI Reflection
    opens be.uhasselt.dwi_application to javafx.fxml;
    opens be.uhasselt.dwi_application.controller to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.BinManager to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.Part to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.Assembly to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.Manager to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.LocationPicker to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick to javafx.fxml;

    opens be.uhasselt.dwi_application.model.Jackson.StripLedConfig to com.fasterxml.jackson.databind;

    opens be.uhasselt.dwi_application.model.basic to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.model.workInstruction to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.model.picking to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.utility.database.repository.assembly to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.utility.database.repository.instruction to org.jdbi.v3.core;

    // ✅ Export required packages
    exports be.uhasselt.dwi_application;
    exports be.uhasselt.dwi_application.controller;
    exports be.uhasselt.dwi_application.controller.BinManager;
    exports be.uhasselt.dwi_application.controller.WorkInstruction.Part;
    exports be.uhasselt.dwi_application.controller.WorkInstruction.Manager to javafx.fxml;
    exports be.uhasselt.dwi_application.controller.WorkInstruction.Assembly to javafx.fxml;

    exports be.uhasselt.dwi_application.model.workInstruction;
    exports be.uhasselt.dwi_application.model.hands to com.fasterxml.jackson.databind;
    exports be.uhasselt.dwi_application.model.basic to com.fasterxml.jackson.databind;
    exports be.uhasselt.dwi_application.model.Jackson to com.fasterxml.jackson.databind;
    exports be.uhasselt.dwi_application.utility.database.repository.assembly;
    exports be.uhasselt.dwi_application.utility.database.repository.instruction;

}
