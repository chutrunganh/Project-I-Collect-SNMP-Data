package Model.MIBTreeStructure;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author: Chu Trung Anh 20225564
 * This class is used to find the root OID of MIB files. The root OID is the longest common prefix of all OIDs in the file.
 * it iterates through all the JSON files in the specified directory and finds the root OID for each file. These root OIDs help optimizing
 * the finding process of Node based on the OID in SNMP Walk request.
 */

public class MibRootOidFinder {
    public static void main(String[] args) {
        // Set the directory containing the JSON files
        String folderPath = "Project_I_code/MIB Databases";
        findRootOids(folderPath);
    }

    private static void findRootOids(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        System.out.println("Processing file: " + file.getName());
                        String rootOid = findRootOidInFile(file);
                        if (rootOid != null) {
                            System.out.println("Root OID for file " + file.getName() + ": " + rootOid);
                        } else {
                            System.out.println("No OID found for file " + file.getName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String findRootOidInFile(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode fileNode = objectMapper.readTree(file);

        // Collect all OIDs in the file
        List<String> oids = new ArrayList<>();
        collectOids(fileNode, oids);

        // Find the most common OID prefix
        return findLongestCommonPrefix(oids);
    }

    private static void collectOids(JsonNode node, List<String> oids) {
        if (node == null) {
            return;
        }

        if (node.has("oid")) {
            oids.add(node.get("oid").asText());
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                collectOids(field.getValue(), oids);
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                collectOids(element, oids);
            }
        }
    }

    private static String findLongestCommonPrefix(List<String> oids) {
        if (oids.isEmpty()) {
            return null;
        }

        // Split OIDs into parts
        List<String[]> splitOids = new ArrayList<>();
        for (String oid : oids) {
            splitOids.add(oid.split("\\."));
        }

        // Find the longest common prefix
        StringBuilder longestCommonPrefix = new StringBuilder();
        for (int i = 0; i < splitOids.get(0).length; i++) {
            String part = splitOids.get(0)[i];
            for (String[] splitOid : splitOids) {
                if (i >= splitOid.length || !splitOid[i].equals(part)) {
                    return longestCommonPrefix.toString();
                }
            }
            if (longestCommonPrefix.length() > 0) {
                longestCommonPrefix.append(".");
            }
            longestCommonPrefix.append(part);
        }

        return longestCommonPrefix.toString();
    }
}
