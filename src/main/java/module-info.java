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
    requires java.sql;
    requires org.jdbi.v3.core;
    requires org.jdbi.v3.sqlobject;

    // MQTT & JSON Parsing
    requires com.hivemq.client.mqtt;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.imaging;
    requires javafx.media;
    requires org.apache.poi.ooxml;
    requires org.eclipse.jetty.ee10.websocket.jakarta.server;
    requires org.eclipse.jetty.servlet;

    // ✅ Open packages for JavaFX & JDBI Reflection
    opens be.uhasselt.dwi_application to javafx.fxml;
    opens be.uhasselt.dwi_application.utility.modules to javafx.fxml;
    opens be.uhasselt.dwi_application.utility.network.WebSocket to javafx.fxml;

    opens be.uhasselt.dwi_application.controller to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.Part to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.Assembly to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.Manager to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.WorkInstruction.LocationPicker to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.Settings to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick to javafx.fxml;
    opens be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients to javafx.fxml;

    opens be.uhasselt.dwi_application.model.Jackson.StripLedConfig to com.fasterxml.jackson.databind;

    opens be.uhasselt.dwi_application.model.basic to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.model.workInstruction to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.model.workInstruction.picking to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.utility.database.repository.assembly to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.utility.database.repository.instruction to org.jdbi.v3.core;
    opens be.uhasselt.dwi_application.utility.database.repository.settings to org.jdbi.v3.core;

    exports be.uhasselt.dwi_application;
    exports be.uhasselt.dwi_application.controller;
    exports be.uhasselt.dwi_application.controller.WorkInstruction.Part;
    exports be.uhasselt.dwi_application.controller.WorkInstruction.Manager to javafx.fxml;
    exports be.uhasselt.dwi_application.controller.WorkInstruction.Assembly to javafx.fxml;
    exports be.uhasselt.dwi_application.controller.Settings to javafx.fxml;
    exports be.uhasselt.dwi_application.utility.handTracking;

    exports be.uhasselt.dwi_application.model.workInstruction;
    exports be.uhasselt.dwi_application.model.Jackson.Commands.Websocket to com.fasterxml.jackson.databind;
    exports be.uhasselt.dwi_application.model.Jackson.hands to com.fasterxml.jackson.databind;
    exports be.uhasselt.dwi_application.model.basic to com.fasterxml.jackson.databind;
    exports be.uhasselt.dwi_application.model.Jackson to com.fasterxml.jackson.databind;
    exports be.uhasselt.dwi_application.utility.database.repository.assembly;
    exports be.uhasselt.dwi_application.utility.database.repository.instruction;
    exports be.uhasselt.dwi_application.utility.database.repository.settings;
    exports be.uhasselt.dwi_application.model.workInstruction.picking;
    exports be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients;
    exports be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip;
}
