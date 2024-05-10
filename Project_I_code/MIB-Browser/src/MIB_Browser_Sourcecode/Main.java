package MIB_Browser_Sourcecode;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("GUI/MainUI.fxml"));
        primaryStage.setTitle("MIB Browser Tool");
        primaryStage.setScene(new Scene(root, 900, 620));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}