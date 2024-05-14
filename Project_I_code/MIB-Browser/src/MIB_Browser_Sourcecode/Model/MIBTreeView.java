package MIB_Browser_Sourcecode.Model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;

public class MIBTreeView {

    /*
     * JSON stands for JavaScript Object Notation, it is attribute-value pairs separated by commas, and enclosed in curly braces. There are many
     * APIs to do this, like Jackson, GSON, etc.
     * Here I will try to build a JSON parser to parse a JSON file into a TreeView using Jackson:  https://github.com/FasterXML/jackson .
     * See how to install this library for Intellij in the Install.md.
     * */


    /*
     * First, let's understand the structure of a TreeView object in JavaFX:
     * - TreeView: Think of TreeView as the entire window or container where your tree structure will be displayed.
     * It's the main visual component that the user interacts with. It holds the root TreeItem. From this root, the
     * entire hierarchical structure unfolds.
     *
     * - TreeItem: Each node in your tree is represented by a TreeItem. A TreeItem can hold some data (like a string,
     * an object, etc.) and can also have children (which are also TreeItems). (TreeItem is considered as The Building Blocks)
     *
     * - TreeCell: A TreeCell is responsible for how each individual node (TreeItem) is displayed within the TreeView. It determines
     * the text, icons, and overall appearance of a single node in the tree. Since the TreeCell relates to the visual representation, I
     * put it in the GUI package in MainController.java.
     */


    /*
     * This function is a recursive function that builds the tree structure from the JSON Node.
     * It takes two arguments: a JsonNode and a TreeItem<MIBNode>.
     * This convert a passed in JSON Node to a TreeItem<MIBNode> object then add that TreeItem to
     * the parentItem's children recursively.
     * */
    private void buildTree(JsonNode jsonNode, TreeItem<MIBNode> parentItem) {
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(field -> {
                MIBNode mibNode = new MIBNode(field.getKey(), field.getValue().toString());
                TreeItem<MIBNode> child = new TreeItem<>(mibNode);
                parentItem.getChildren().add(child);
                buildTree(field.getValue(), child); //Recursive call
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
     * Some explanation about the buildTree function:
     * The function takes two arguments: a JsonNode and a TreeItem<MIBNode>. It then  checks
     * if the JSON Node is:
     * - an object (i.e., it contains key-value pairs), like this:
     *   {
     *    "name": "John Doe",
     *    "age": 30,
     *    "city": "New York"
     *   }
     *  it iterates over each field in the object. For each field, it does the following: Creates a new MIBNode object with
     * the field's key and value -> Creates a new TreeItem<MIBNode> object (child) with the MIBNode object -> Adds the child to the
     * parent item's children -> Makes a recursive call to buildTree with the field's value and the new child as arguments.
     * This allows it to process nested objects in the JSON structure.
     *
     * - an array, like this:
     *  ["apple", "banana", "cherry"]
     * it iterates over each element in the array. For each element, it does the following: Creates a new MIBNode object with the
     * element's value -> Creates a new TreeItem<MIBNode> object (child) with the MIBNode object -> Adds the child to the parent
     * item's children -> Makes a recursive call to buildTree with the element and the new child as arguments.
     * This allows it to process nested arrays in the JSON structure.
     * */


    /*
     * From JSON file to TreeView. As written in previous comments, the TreeView is the main visual component that the user interacts
     * with. It holds the root TreeItem, so this function is just simply pass the rootNode to the buildTree function to build the tree from there .
     * */

    //ObjectMapper is the main class to use for reading and writing JSON content
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TreeView<MIBNode> jsonToTreeView(File jsonFile) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        TreeItem<MIBNode> rootItem = new TreeItem<>(new MIBNode(jsonFile.getName(), ""));
        //Use the name of the file as the root node instead of the actual root content
        buildTree(rootNode, rootItem);
        return new TreeView<>(rootItem);
    }


    /*
     * Try to find the oid in the children of the current item. Here I only process ONE LEVEL of children of
     * the current item.  If the 'oid' key is found in the children, return its value. If the 'oid' key is not found,
     * return an empty string. As I implemented this function, it only checks the immediate children of the current item.
     * Multiple levels of children can be processed by making recursive calls to this function as I commented above.
     */


    /*
     * Function to travel the TreeView  to try to get the oid (recursively)
     * */
//    public static String findOid(TreeItem<MIBNode> item) {
//        // If the current item is the 'oid' key, return its value
//        if (item.getValue().getKey().equals("oid")) {
//            return item.getValue().getValue();
//        }
//
//        // If the current item is not the 'oid' key, check its children
//        for (TreeItem<MIBNode> child : item.getChildren()) {
//            String oidValue = findOid(child);
//            if (oidValue != null) {
//                return oidValue;
//            }
//        }
//
//        // If the 'oid' key was not found in the current item or its children, return null
//        return null;
//    }

    // This is the modified version of the findOid function that only checks the immediate children of the current item
    public static String findOid(TreeItem<MIBNode> item) {
        // Check the children of the current item
        for (TreeItem<MIBNode> child : item.getChildren()) {
            // If the child item is the 'oid' key, return its value
            if (child.getValue().getKey().equals("oid")) {
                return child.getValue().getValue();
            }
        }

        // If the 'oid' key was not found in the children of the current item, return an empty string
        return "";
    }

}
