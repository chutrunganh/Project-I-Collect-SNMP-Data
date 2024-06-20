import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("View/MainUI.fxml"));
        primaryStage.setTitle("SNMP Browser Tool");
        primaryStage.getIcons().add(new Image("file:Project_I_code/src/AppImages/Icon.png"));


        primaryStage.setScene(new Scene(root, 977, 653));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}