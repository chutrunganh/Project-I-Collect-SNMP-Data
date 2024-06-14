package Project_I_Code;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.Objects;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("GUI/MainUI.fxml")));
        primaryStage.setTitle("SNMP Browser Tool");
        primaryStage.setScene(new Scene(root, 993, 618));
        // Set the application icon
        primaryStage.getIcons().add(new Image("Image/AppIcon.png"));
        primaryStage.show();
    }
}