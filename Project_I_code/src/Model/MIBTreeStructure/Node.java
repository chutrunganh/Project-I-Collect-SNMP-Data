package Model.MIBTreeStructure;

import java.util.HashMap;
import java.util.Map;

public class Node {
    public String name;
    public String oid;
    public String type;
    public String access;
    public String status;
    public String description;
    Map<String, Node> children = new HashMap<>();

    Node(String name, String oid, String type, String access, String status, String description) {
        this.name = name;
        this.oid = oid;
        this.type = type;
        this.access = access;
        this.status = status;
        this.description = description;
    }

    /**
     * When display the TreeView to the UI, each node has the Node type as we defined in this class, we just want
     * to display the node name., not th Node object represents in string format.
     * @return
     */
    @Override
    public String toString() {
        return name;
    }
}