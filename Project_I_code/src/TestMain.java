import Model.MIBTreeStructure.BuildTreeFromJson;
import Model.MIBTreeStructure.Node;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestMain extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        BuildTreeFromJson treeBuilder = new BuildTreeFromJson();
//        try {
//            treeBuilder.buildTreeFromJson("Project_I_code/MIB Databases/IF-MIB.json");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        List<String> mibFilePaths = Arrays.asList(
                "Project_I_code/MIB Databases/RFC1213-MIB.json",
                "Project_I_code/MIB Databases/HOST-RESOURCES-MIB.json"
        );
        try {
            treeBuilder.buildTreeFromMultipleMIBs(mibFilePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TreeItem<Node> rootItem = treeBuilder.convertNodeToTreeItem(treeBuilder.getRoot());
        TreeView<Node> treeView = new TreeView<>(rootItem);

        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Node>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Node>> observable, TreeItem<Node> oldValue, TreeItem<Node> newValue) {
                if (newValue != null) {
                    Node selectedNode = newValue.getValue();
                    printNodeAttributes(selectedNode);
                }
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(treeView);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    private void printNodeAttributes(Node node) {
        if (node != null) {
            System.out.println("Name: " + node.name);
            System.out.println("OID: " + node.oid);
            System.out.println("Type: " + node.type);
            System.out.println("Access: " + node.access);
            System.out.println("Status: " + node.status);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}