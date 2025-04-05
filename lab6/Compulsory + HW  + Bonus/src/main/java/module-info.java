module org.example.compulsory {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.fasterxml.jackson.annotation;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires google.cloud.vertexai;
    requires java.net.http;
    requires io.grpc.netty.shaded;
    //requires proto.google.cloud.vertexai.v1;

    opens org.example.compulsory to javafx.fxml;
    exports org.example.compulsory;
}