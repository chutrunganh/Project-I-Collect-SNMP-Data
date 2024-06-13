package Model.MIBTreeStructure;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class MIBReader {

    public void findNodeByOIDInAllFiles(String directoryPath, String targetOID) {
        ObjectMapper objectMapper = new ObjectMapper();

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(file -> {
                        try {
                            System.out.println("Reading file: " + file.getName()); // Add this line
                            JsonNode mib = objectMapper.readTree(file);
                            JsonNode foundNode = findNodeByOIDRecursive(mib, targetOID);
                            if (foundNode != null) {
                                String name = extractNodeName(foundNode);
                                String datatype = extractNodeDatatype(foundNode);
                                System.out.println("Node found in file " + file.getName() + ": " + name + " (Datatype: " + datatype + ")");
                            } else {
                                System.out.println("Node not found in file: " + file.getName()); // Add this line
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonNode findNodeByOIDRecursive(JsonNode node, String targetOID) {
        if (node == null) {
            return null;
        }
        if (node.has("oid") && node.get("oid").asText().equals(targetOID)) {
            return node; // Found the node
        }

        // Recurse into all fields, not just objects or arrays
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode foundNode = findNodeByOIDRecursive(field.getValue(), targetOID);
            if (foundNode != null) {
                return foundNode;
            }
        }

        return null; // Not found in this subtree
    }

    private String extractNodeName(JsonNode node) {
        if (node.has("name")) {
            return node.get("name").asText();
        }
        return "Unknown";
    }

    private String extractNodeDatatype(JsonNode node) {
        if (node.has("syntax") && node.get("syntax").has("type")) {
            return node.get("syntax").get("type").asText();
        }
        return "Unknown";
    }
}