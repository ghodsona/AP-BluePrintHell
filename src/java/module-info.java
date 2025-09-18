module com.BluePrintHell {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    opens com.BluePrintHell.controller to javafx.fxml;

    opens com.BluePrintHell.model;
    opens com.BluePrintHell.model.leveldata;
    opens com.BluePrintHell.model.network;
    opens com.BluePrintHell.model.packets;

    exports com.BluePrintHell;
}