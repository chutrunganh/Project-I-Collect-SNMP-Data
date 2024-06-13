package Model.MIBTreeStructure;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.TreeItem;

public class JsonTreeConverter {

    public static TreeItem<String> convertJsonToTree(JsonNode node, String name) {
        String value = "";
//        if (node.isValueNode()) {
//            value = node.asText();
//        }

        JsonTreeItem root = new JsonTreeItem(name, value, node);

        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode element = node.get(i);
                root.getChildren().add(convertJsonToTree(element, String.valueOf(i)));
            }
        } else if (node.isObject()) {
            node.fieldNames().forEachRemaining(fieldName -> {
                JsonNode childNode = node.get(fieldName);
                root.getChildren().add(convertJsonToTree(childNode, fieldName));
            });
        }

        return root;
    }
}