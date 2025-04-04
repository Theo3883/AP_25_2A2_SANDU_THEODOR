module org.example.compulsory {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.fasterxml.jackson.annotation;
    requires static lombok;
    requires com.fasterxml.jackson.databind;

    opens org.example.compulsory to javafx.fxml;
    exports org.example.compulsory;
}