package Model.MIBTreeStructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MibLoader {
    private JsonNode rootNode;

    public void loadMibsFromFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                System.out.println("Using file: " + file.getName());
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        loadMibFromFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadMibFromFile(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        if (rootNode == null) {
            rootNode = objectMapper.readTree(file);
        } else {
            JsonNode fileNode = objectMapper.readTree(file);
            // Combine rootNode and fileNode
            ((ObjectNode) rootNode).setAll((ObjectNode) fileNode);
        }
    }

    public Node lookupNode(String oid) {
        // Print OID that is being looked up
        System.out.println("Looking up node for OID: " + oid);
        Node node = findNodeWithOid(rootNode, oid);
        if (node == null) {
            System.out.println("Node not found for OID: " + oid);
        }
        return node;
    }

    private Node findNodeWithOid(JsonNode currentNode, String oid) {
        if (currentNode == null) {
            return null;
        }

        // Check if the current node has the matching OID
        if (currentNode.has("oid") && oid.equals(currentNode.get("oid").asText())) {
            return createNodeFromJson(currentNode);
        }

        // Traverse the children of the current node
        if (currentNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = currentNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();
                Node node = findNodeWithOid(value, oid);
                if (node != null) {
                    return node;
                }
            }
        } else if (currentNode.isArray()) {
            for (JsonNode value : currentNode) {
                Node node = findNodeWithOid(value, oid);
                if (node != null) {
                    return node;
                }
            }
        }

        return null;
    }

    private Node createNodeFromJson(JsonNode jsonNode) {
        String name = Optional.ofNullable(jsonNode.get("name")).map(JsonNode::asText).orElse(null);
        String oid = Optional.ofNullable(jsonNode.get("oid")).map(JsonNode::asText).orElse(null);
        String nodeType = Optional.ofNullable(jsonNode.get("nodetype")).map(JsonNode::asText).orElse(null);
        JsonNode syntaxNode = jsonNode.get("syntax");
        String type = null;
        Map<String, Object> constraints = new HashMap<>();
        if (syntaxNode != null) {
            type = Optional.ofNullable(syntaxNode.get("type")).map(JsonNode::asText).orElse(null);
            JsonNode constraintsNode = syntaxNode.get("constraints");
            if (constraintsNode != null) {
                constraints = new ObjectMapper().convertValue(constraintsNode, Map.class);
            }
        }
        String access = jsonNode.has("maxaccess") ? jsonNode.get("maxaccess").asText() : null;
        String status = jsonNode.has("status") ? jsonNode.get("status").asText() : null;
        String description = Optional.ofNullable(jsonNode.get("description")).map(JsonNode::asText).orElse(null);
        return new Node(name, oid, nodeType, type, access, status, description, constraints);
    }

    public Node getRootNode() {
        return createNodeFromJson(rootNode);
    }
}
