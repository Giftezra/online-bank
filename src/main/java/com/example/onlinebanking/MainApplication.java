package com.example.onlinebanking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        String css = Objects.requireNonNull(getClass().getResource("/style/register.css")).toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Enigma Mobile Bank");
        stage.setScene(scene);
        stage.show();

        // Create another stage
        Stage secondaryStage = new Stage();
        FXMLLoader secondaryFxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
        Scene secondaryScene = new Scene(secondaryFxmlLoader.load());
        String secondaryCss = Objects.requireNonNull(getClass().getResource("/style/register.css")).toExternalForm();
        secondaryScene.getStylesheets().add(secondaryCss);
        secondaryStage.setTitle("Another Enigma Mobile Bank");
        secondaryStage.setScene(secondaryScene);

        stage.show();
        secondaryStage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}