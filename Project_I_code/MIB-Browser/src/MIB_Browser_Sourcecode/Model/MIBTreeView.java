package MIB_Browser_Sourcecode.Model;

import MIB_Browser_Sourcecode.GUI.MainController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;

public class MIBTreeView {

    /*
     * JSON stands for JavaScript Object Notation, it is attribute-value pairs separated by commas, and enclosed in curly braces. There are many
     * APIs to do this, like Jackson, GSON, etc.
     * Here I will try to build a JSON parser to parse a JSON file into a TreeView using Jackson:  https://github.com/FasterXML/jackson
     * */

    /*
     * Test function to parse a JSON string to a JsonNode
     */
//    private static ObjectMapper objectMapper = getdefaultObjectMapper();
//
//    // Initialize the ObjectMapper by using this method instead of directly initializing it, since I want to configure it initially
//    private static ObjectMapper getdefaultObjectMapper() {
//        ObjectMapper defaultObjectMapper = new ObjectMapper();
//        return defaultObjectMapper;
//    }
//
//    public static JsonNode parseJsonStringToNode(String jsonString) throws IOException {
//        return objectMapper.readTree(jsonString);
//    }



    /*
     * Convert a MIB in JSON format to a TreeView object
     */


    //Create a TreeItem from a JsonNode. TEach TreeItem is a MIBNode (self-defined class) which has a pair key-value
    private TreeItem<MIBNode> createTreeItem(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            TreeItem<MIBNode> item = new TreeItem<>();
            jsonNode.fields().forEachRemaining(field -> {
                MIBNode mibNode = new MIBNode(field.getKey(), field.getValue().toString());
                TreeItem<MIBNode> child = new TreeItem<>(mibNode);
                item.getChildren().add(child);
            });
            return item;
        } else if (jsonNode.isArray()) {
            TreeItem<MIBNode> item = new TreeItem<>();
            for (JsonNode arrayElement : jsonNode) {
                MIBNode mibNode = new MIBNode(arrayElement.asText(), arrayElement.toString());
                item.getChildren().add(new TreeItem<>(mibNode));
            }
            return item;
        } else {
            return new TreeItem<>(new MIBNode(jsonNode.asText(), jsonNode.toString()));
        }
    }


        //Re explanation of createTreeItem function:
        /*
         * Some explanation of TreeItem function:
         * This function is used to convert a JSONNode into a TreeItem.
         * 1. TreeItem<MIBNode> item = new TreeItem<>(jsonNode.toString()); - This line creates a new TreeItem<MIBNode> with
         * the string representation of the JsonNode as its value.
         * 2. if (jsonNode.isObject()) {...} - This block is executed if the JsonNode is an object. It iterates over each field in the JsonNode, recursively calls createTreeItem on the field's value to create a child TreeItem<MIBNode>, sets
         * the child's value to the field's key, and adds the child to the item.
         * 3. else if (jsonNode.isArray()) {...} - This block is executed if the JsonNode is an array. It iterates over each element in the JsonNode, recursively calls createTreeItem on the element to create a child TreeItem<MIBNode>,
         *  and adds the child to the item.
         * 4. return item; - This line returns the TreeItem<MIBNode> that was created.
         * */


    //ObjectMapper is the main class to use for reading and writing JSON content
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TreeView<MIBNode> jsonToTreeView(File jsonFile) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        TreeItem<MIBNode> rootItem = new TreeItem<>(new MIBNode(jsonFile.getName(), "")); //Use the name of the file as the root node
        buildTree(rootNode, rootItem);
        return new TreeView<>(rootItem);
    }

    private void buildTree(JsonNode jsonNode, TreeItem<MIBNode> parentItem) {
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(field -> {
                MIBNode mibNode = new MIBNode(field.getKey(), field.getValue().toString());
                TreeItem<MIBNode> child = new TreeItem<>(mibNode);
                parentItem.getChildren().add(child);
                buildTree(field.getValue(), child);
            });
        } else if (jsonNode.isArray()) {
            for (JsonNode arrayElement : jsonNode) {
                MIBNode mibNode = new MIBNode(arrayElement.asText(), arrayElement.toString());
                TreeItem<MIBNode> child = new TreeItem<>(mibNode);
                parentItem.getChildren().add(child);
                buildTree(arrayElement, child);
            }
        }
    }

        /*
         * Some explanation of jsonToTreeView function:
         * This method, jsonToTreeView, is responsible for converting a JSON file into a TreeView<MIBNode> object.
         * 1. JsonNode rootNode = objectMapper.readTree(jsonFile); - This line reads the JSON file and converts it into a JsonNode object using the ObjectMapper's readTree method. The JsonNode
         *  object represents the root node of the JSON structure.
         * 2. TreeItem<MIBNode> rootItem = new TreeItem<>(jsonFile.getName()); - This line creates a new TreeItem<MIBNode> object, which is the root item of the TreeView. The name of the TreeItem
         *  is set to the name of the JSON file.
         * 3. rootItem.getChildren().add(createTreeItem(rootNode)); - This line adds a child to the rootItem. The child is created by calling the createTreeItem method with the rootNode as the
         * argument. The createTreeItem method is responsible for converting a JsonNode into a TreeItem<MIBNode>.
         * 4. return new TreeView<>(rootItem); - This line creates a new TreeView<MIBNode> object with the rootItem as its root, and returns it.
         *
         * */

        
        /**/




}