package Model.MIBTreeStructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BuildTreeFromJson {
    private Node root = new Node("root", "", "", "", "", "", "", null);

    public void buildTreeFromJson(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(new File(jsonFilePath));
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();

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
            addNode(name, oid, nodeType, type, access, status, description, constraints);
        }
    }

    public void buildTreeFromMultipleMIBs(List<String> mibFilePaths) throws IOException {
        for (String mibFilePath : mibFilePaths) {
            buildTreeFromJson(mibFilePath);
        }
    }

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
                        existingNode.description = description;
                        existingNode.constraints = constraints; // Update constraints
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

    public TreeItem<Node> convertNodeToTreeItem(Node node) {
        TreeItem<Node> treeItem = new TreeItem<>(node);
        for (Node child : node.children.values()) {
            treeItem.getChildren().add(convertNodeToTreeItem(child));
        }
        return treeItem;
    }

    public Node getRoot() {
        return root;
    }
}
