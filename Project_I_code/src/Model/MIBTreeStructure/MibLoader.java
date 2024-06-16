package Model.MIBTreeStructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class is responsible for loading MIB files from a folder and looking up nodes based on a provided OID, primarily used for SNMP Walk operations.
 * It loads all the files in the directory into memory, then for each incoming OID, it looks up the corresponding node in the loaded MIB files.
 * @return: the Node object corresponding to the OID, if found.
 */
public class MibLoader {
    private final Map<String, List<JsonNode>> mibFilesByRootOid = new HashMap<>();
    private final Map<String, List<String>> predefinedRootOids = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MibLoader() {
        // Predefined root OID to file name map
        predefinedRootOids.put("1.3.6.1.6", Arrays.asList("SNMPv2-TM.json"));
        predefinedRootOids.put("1.3.6.1.2.1.16", Arrays.asList("RMON2-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.12", Arrays.asList("RFC1229-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.33", Arrays.asList("RFC1317-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.15", Arrays.asList("FDDI-SMT73-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1", Arrays.asList("IP-MIB.json", "RFC1213-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.2021.14", Arrays.asList("UCD-DEMO-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10", Arrays.asList("RFC1382-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.31", Arrays.asList("RFC1304-MIB.json"));
        predefinedRootOids.put("1.3.6.1", Arrays.asList("RFC1155-SMI.json", "SNMPv2-MIB.json", "IF-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.24", Arrays.asList("RFC1414-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.19", Arrays.asList("RFC1316-MIB.json"));
        predefinedRootOids.put("1.3.6.1.6.3.10", Arrays.asList("SNMP-FRAMEWORK-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1", Arrays.asList("RFC1156-MIB.json", "RFC1271-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.8072.2", Arrays.asList("NET-SNMP-EXAMPLES-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.23", Arrays.asList("TCPIPX-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.34", Arrays.asList("SNA-NAU-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.33", Arrays.asList("UPS-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.43", Arrays.asList("Printer-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.62", Arrays.asList("APPLICATION-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.9", Arrays.asList("RFC1231-MIB.json", "TOKENRING-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.34", Arrays.asList("RFC1318-MIB.json", "PARALLEL-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.18", Arrays.asList("DECNET-PHIV-MIB.json", "RFC1289-phivMIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.16", Arrays.asList("RMON-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.18", Arrays.asList("RFC1406-MIB.json", "RFC1232-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.37", Arrays.asList("ATM-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.14", Arrays.asList("RFC1253-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.30", Arrays.asList("IANAifType-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.29", Arrays.asList("DSA-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2", Arrays.asList("RFC1389-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.8072.3", Arrays.asList("NET-SNMP-TC.json"));
        predefinedRootOids.put("1.3.6.1.4.1.2021", Arrays.asList("UCD-SNMP-MIB.json"));
        predefinedRootOids.put("1.3.6.1.6.3.16", Arrays.asList("SNMP-VIEW-BASED-ACM-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.30", Arrays.asList("RFC1407-MIB.json", "RFC1233-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.19", Arrays.asList("CHARACTER-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.47", Arrays.asList("ENTITY-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.2021.13.15", Arrays.asList("UCD-DISKIO-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.2021.13.1", Arrays.asList("UCD-IPFWACC-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.7", Arrays.asList("RFC1398-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.76", Arrays.asList("INET-ADDRESS-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.8072", Arrays.asList("NET-SNMP-AGENT-MIB.json", "NET-SNMP-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.25", Arrays.asList("HOST-RESOURCES-MIB.json"));
        predefinedRootOids.put("1.3.6.1.2.1.10.16", Arrays.asList("RFC1381-MIB.json"));
        predefinedRootOids.put("1.3.6.1.4.1.2021.13.14", Arrays.asList("UCD-DLMOD-MIB.json"));
        predefinedRootOids.put("1", Arrays.asList("Root.json"));
    }

    /**
     * Load MIB files from a folder. For each file in the directory, load the MIB file into memory.
     * @param folderPath: the path to the folder containing the MIB files.
     */
    public void loadMibsFromFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        System.out.println("Loading file: " + file.getName());
                        JsonNode fileNode = objectMapper.readTree(file);
                        String rootOid = getRootOidFromFile(file.getName());
                        if (rootOid != null) {
                            if (!mibFilesByRootOid.containsKey(rootOid)) {
                                mibFilesByRootOid.put(rootOid, new ArrayList<>());
                            }
                            mibFilesByRootOid.get(rootOid).add(fileNode);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Helper method to extract root OID from file name based on predefinedRootOids map.
     * @param fileName: the name of the MIB file.
     * @return: the root OID associated with the file name.
     */
    private String getRootOidFromFile(String fileName) {
        for (Map.Entry<String, List<String>> entry : predefinedRootOids.entrySet()) {
            if (entry.getValue().contains(fileName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Look up a node based on the provided OID. Determine which MIB file to search based on the root OID.
     * @param oid: the OID to look up.
     * @return: the Node object corresponding to the OID, if found.
     */
    public Node lookupNode(String oid) {
        //System.out.println("Looking up OID: " + oid);
        for (Map.Entry<String, List<JsonNode>> entry : mibFilesByRootOid.entrySet()) {
            String rootOid = entry.getKey();
            if (oid.startsWith(rootOid)) {
                List<JsonNode> mibFiles = entry.getValue();
                for (JsonNode mibFile : mibFiles) {
                    Node node = findNodeWithOid(mibFile, oid);
                    if (node != null) {
                        return node;
                    }
                }
            }
        }
        return null;
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
                constraints = objectMapper.convertValue(constraintsNode, Map.class);
            }
        }
        String access = jsonNode.has("maxaccess") ? jsonNode.get("maxaccess").asText() : null;
        String status = jsonNode.has("status") ? jsonNode.get("status").asText() : null;
        String description = Optional.ofNullable(jsonNode.get("description")).map(JsonNode::asText).orElse(null);
        return new Node(name, oid, nodeType, type, access, status, description, constraints);
    }
}

