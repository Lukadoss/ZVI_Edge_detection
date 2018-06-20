import config.MainConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
        primaryStage.setTitle("Detekce hran ve snímku - Petr Lukašík - ZVI");
        primaryStage.setScene(new Scene(root, MainConfig.WIDTH, MainConfig.HEIGHT));
        primaryStage.setMinWidth(MainConfig.minWIDTH);
        primaryStage.setMinHeight(MainConfig.minHEIGHT);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
