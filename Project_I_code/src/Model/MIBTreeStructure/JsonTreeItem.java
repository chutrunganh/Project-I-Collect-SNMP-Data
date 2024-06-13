package Model.MIBTreeStructure;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.TreeItem;

public class JsonTreeItem extends TreeItem<String> {
    private String key;
    private String value;
    private JsonNode jsonNode;

    public JsonTreeItem(String key, String value, JsonNode jsonNode) {
        super(key + (value.isEmpty() ? "" : ": " + value));
        this.key = key;
        this.value = value;
        this.jsonNode = jsonNode;
    }

    public String getKey() {
        return key;
    }


    public JsonNode getJsonNode() {
        return jsonNode;
    }
}