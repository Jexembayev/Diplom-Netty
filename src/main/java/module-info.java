module orhestra {
    requires javafx.controls;
    requires javafx.fxml;
    requires jsch;
    requires com.hierynomus.sshj;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    opens orhestra.app to javafx.fxml;
    opens orhestra.controller to javafx.fxml;

    exports orhestra.app;
    opens orhestra.service to javafx.fxml;
}
