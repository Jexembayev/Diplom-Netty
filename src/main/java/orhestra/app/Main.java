package orhestra.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String MAIN_VIEW_PATH = "/orhestra/main.fxml";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;
    private static final String APP_TITLE = "VM Orchestrator";

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = loadMainView();
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Failed to load the main view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Parent loadMainView() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_VIEW_PATH));
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

