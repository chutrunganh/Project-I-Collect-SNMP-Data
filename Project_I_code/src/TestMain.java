import Model.MIBTreeStructure.JsonTreeConverter;
import Model.MIBTreeStructure.JsonTreeItem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;


public class TestMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        File jsonFile = new File("Project_I_code/MIB Databases/HOST-RESOURCES-MIB.json"); // Replace with your file path

        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<JsonNode> jsonNode = new AtomicReference<>(objectMapper.readTree(jsonFile));

        TreeItem<String> rootItem = JsonTreeConverter.convertJsonToTree(jsonNode.get(), "root");
        TreeView<String> treeView = new TreeView<>(rootItem);

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isLeaf()){
                JsonTreeItem item = (JsonTreeItem) newValue;
                jsonNode.set(item.getJsonNode());

                jsonNode.get().fieldNames().forEachRemaining(fieldName -> {
                    if (fieldName.equals("name") || fieldName.equals("maxaccess") || fieldName.equals("description")) {
                        JsonNode childNode = jsonNode.get().get(fieldName);
                        System.out.println(fieldName + ": " + childNode.asText());
                    }
                });

                if (jsonNode.get().has("syntax") && jsonNode.get().get("syntax").has("type")) {
                    String type = jsonNode.get().get("syntax").get("type").asText();
                    System.out.println("Type: " + type);
                }
            }
        });

        Scene scene = new Scene(treeView, 400, 600);
        primaryStage.setTitle("JSON Tree Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}