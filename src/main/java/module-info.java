module ru.itis.semestrovka22 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;

    opens ru.itis.artists.controller to javafx.fxml;
    opens ru.itis.artists;
    exports ru.itis.artists.controller;
    exports ru.itis.artists.application;
    exports ru.itis.artists.protocol to com.fasterxml.jackson.databind;
}