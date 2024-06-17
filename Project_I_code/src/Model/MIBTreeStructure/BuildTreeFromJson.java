package Model.MIBTreeStructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author: Chu Trung Anh 20225564
 * This class contains all necessary methods to build a tree structure from a directory of MIB files. To test if the tree can be build successfully,
 *  we only need this class and the Node class, then using the TestBuildTree class in the Test package to run the test, the resul will be displayed in a TreeView object on javaFX.
 * @return: the TreeView object in javafx
 */

public class BuildTreeFromJson {
    private final Node root = new Node("root", "", "", "", "", "", "", null); // Root node of the tree
    // Using the same root node for all trees created by this class, allowing to concatenate/merge multiple MIB files into a single tree

    /**
     * Build a tree structure from a single JSON file. With each node in the JSON file, extract the following information: name, oid, nodeType, type, access, status, description, constraints
     * then assign them to the corresponding attributes of the Node object. Recursively build the tree structure based on the OID of each node.
     * @param jsonFilePath: the path to the JSON file containing the MIB data.
     */
    public void buildTreeFromJson(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(new File(jsonFilePath));
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();

            // Extract information from the JSON object
            String name = Optional.ofNullable(field.getValue().get("name")).map(JsonNode::asText).orElse(null);
            String oid = Optional.ofNullable(field.getValue().get("oid")).map(JsonNode::asText).orElse(null);
            String nodeType = Optional.ofNullable(field.getValue().get("nodetype")).map(JsonNode::asText).orElse(null);
            JsonNode syntaxNode = field.getValue().get("syntax");
            String type = null;
            Map<String, Object> constraints = new HashMap<>();
            if (syntaxNode != null) {
                type = Optional.ofNullable(syntaxNode.get("type")).map(JsonNode::asText).orElse(null);
                JsonNode constraintsNode = syntaxNode.get("constraints");
                if (constraintsNode != null) {
                    constraints = objectMapper.convertValue(constraintsNode, Map.class);
                }
            }
            String access = field.getValue().has("maxaccess") ? field.getValue().get("maxaccess").asText() : null;
            String status = field.getValue().has("status") ? field.getValue().get("status").asText() : null;
            String description = Optional.ofNullable(field.getValue().get("description")).map(JsonNode::asText).orElse(null);

            //After extracting the information, add the node to the tree structure
            addNode(name, oid, nodeType, type, access, status, description, constraints);
        }
    }

    /**
     * Build a tree structure from multiple JSON files. For each JSON file, call the buildTreeFromJson method to build the tree structure.
     * @param mibFilePaths: a list of paths to the JSON files containing the MIB data.
     */
    public void buildTreeFromMultipleMIBs(List<String> mibFilePaths) throws IOException {
        for (String mibFilePath : mibFilePaths) {
            buildTreeFromJson(mibFilePath);
        }
    }

    /**
     * Add a node to the tree structure based on its OID. The node is identified by its OID. If a node with the same OID already exists, update its information.
     */
    private void addNode(String name, String oid, String nodeType, String type, String access, String status, String description, Map<String, Object> constraints) {
        if (oid != null && name != null) {
            String[] parts = oid.split("\\.");
            Node current = root;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (i == parts.length - 1) {
                    // Final part of the OID
                    if (current.children.containsKey(part)) {
                        // Node with this OID already exists, update its information
                        Node existingNode = current.children.get(part);
                        existingNode.name = name;
                        existingNode.oid = oid;
                        existingNode.nodeType = nodeType;
                        existingNode.type = type;
                        existingNode.access = access;
                        existingNode.status = status;
                        existingNode.constraints = constraints; // Update constraints
                        existingNode.description = description;
                    } else {
                        // Create new node
                        current.children.put(part, new Node(name, oid, nodeType, type, access, status, description, constraints));
                    }
                } else {
                    // Intermediate parts of the OID
                    current.children.putIfAbsent(part, new Node(part, null, null, null, null, null, null, null));
                    current = current.children.get(part);
                }
            }
        }
    }

    /**
     * Convert the tree structure to a TreeItem object for display in a javafx TreeView.
     */
    public TreeItem<Node> convertNodeToTreeItem(Node node) {
        TreeItem<Node> treeItem = new TreeItem<>(node);
        for (Node child : node.children.values()) {
            treeItem.getChildren().add(convertNodeToTreeItem(child));
        }
        return treeItem;
    }

    // Getter for the root node
    public Node getRoot() {
        return root;
    }
}
