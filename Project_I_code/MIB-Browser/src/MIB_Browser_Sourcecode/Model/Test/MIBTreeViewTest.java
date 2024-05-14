package MIB_Browser_Sourcecode.Model.Test;

import MIB_Browser_Sourcecode.Model.MIBTreeView;
import MIB_Browser_Sourcecode.Model.MIBNode;
import javafx.scene.control.TreeItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MIBTreeViewTest {

    @Test
    public void testBuildTree() throws IOException {
        // Create a JsonNode with a nested node inside then convert it to a TreeItem<MIBNode>
        String json = "{\"key\": \"root\", \"children\": [{\"key\": \"child\", \"value\": \"value\"}]}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        // Create a TreeItem<MIBNode> as the root node
        TreeItem<MIBNode> rootItem = new TreeItem<>(new MIBNode("root", ""));

        // Create an instance of MIBTreeView
        MIBTreeView treeView = new MIBTreeView();

        // Call the buildTree method with the JsonNode and the root node
        treeView.buildTree(rootNode, rootItem);

        printTree(rootItem, 0);


    }

    //Show the tree structure to the console to check if the tree is built correctly
    public void printTree(TreeItem<MIBNode> node, int indentation) {
        // Print the key of the current node with the given indentation
        for (int i = 0; i < indentation; i++) {
            System.out.print("  ");
        }
        System.out.println(node.getValue().getKey());

        // Recursively call printTree for each child of the current node with an increased indentation level
        for (TreeItem<MIBNode> child : node.getChildren()) {
            printTree(child, indentation + 1);
        }
    }
}