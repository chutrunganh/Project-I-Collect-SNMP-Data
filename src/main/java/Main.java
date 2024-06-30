import Control.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("View/MainUI.fxml"));
        Parent root = loader.load();


        MainController controller = loader.getController();
        Scene scene = new Scene(root, 977, 653);
        controller.setMainScene(scene);


        primaryStage.setTitle("SNMP Browser Tool");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("Image/Icon.png"))));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}