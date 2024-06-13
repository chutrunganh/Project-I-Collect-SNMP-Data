package Model.MIBTreeStructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MibLoader {
    private Map<String, String> oidToName = new HashMap<>();
    private Map<String, String> oidToDataType = new HashMap<>();

    public void loadMibsFromFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    loadMibFromFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle IOException if needed
                }
            }
        }
    }

    private void loadMibFromFile(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(file);
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();

            String oid = field.getKey();
            String name = Optional.ofNullable(field.getValue().get("name")).map(JsonNode::asText).orElse(null);
            String dataType = Optional.ofNullable(field.getValue().get("dataType")).map(JsonNode::asText).orElse(null);

            oidToName.put(oid, name);
            oidToDataType.put(oid, dataType);
        }
    }

    public String lookupName(String oid) {
        return oidToName.get(oid);
    }

    public String lookupDataType(String oid) {
        return oidToDataType.get(oid);
    }
}
