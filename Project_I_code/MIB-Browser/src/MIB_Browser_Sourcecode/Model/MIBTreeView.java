package MIB_Browser_Sourcecode.Model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

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

    ObjectMapper objectMapper = new ObjectMapper();

    public class JsonToTreeView {

        public TreeView<String> jsonToTreeView(File jsonFile) throws IOException {
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            //TreeItem<String> rootItem = createTreeItem(rootNode);
            TreeItem<String> rootItem = new TreeItem<>(jsonFile.getName());
            rootItem.getChildren().add(createTreeItem(rootNode));
            return new TreeView<>(rootItem);
        }

        private TreeItem<String> createTreeItem(JsonNode jsonNode) {
            TreeItem<String> item = new TreeItem<>(jsonNode.toString());

            if (jsonNode.isObject()) {
                jsonNode.fields().forEachRemaining(field -> {
                    TreeItem<String> child = createTreeItem(field.getValue());
                    child.setValue(field.getKey());
                    item.getChildren().add(child);
                });
            } else if (jsonNode.isArray()) {
                for (JsonNode arrayElement : jsonNode) {
                    item.getChildren().add(createTreeItem(arrayElement));
                }
            }

            return item;
        }
    }

}