package Model.MIBTreeStructure;

import java.util.HashMap;
import java.util.Map;

/** @author: Chu Trung Anh 20225564
 * This class represents the Node in the MIB Tree. Each node has the following attributes:
 *  @attribute:
 * - name: as the name self-explained
 * - oid: represents the OID of the object in the MIB file, used to identify the object and also build the tree structure
 * - nodeType: this attribute helps to identify an object is a scalar object or not, this will directly affect to the "actual" OID of an object
 * - type: the type of the object, for example: INTEGER, STRING, etc. contains under the "syntax" field in the MIB file
 * - access: as the name self-explained
 * - status: as the name self-explained
 * - description: as the name self-explained
 * - constraints: defined the constraints of the object, for example: the range of the INTEGER type, the enumeration of the INTEGER type, etc.
 *                this filed will be use together with the "type" field to parse the return value of the object from SNMP response
 * - children: the children of the current node, used recursively to build the tree structure
 * <p>
 * The structure of this Node class is based on a JSON object that we get from the MIB file. For example:
 *   "hrDeviceStatus": {
 *     "name": "hrDeviceStatus",
 *     "oid": "1.3.6.1.2.1.25.3.2.1.5",
 *     "nodetype": "column",
 *     "class": "objecttype",
 *     "syntax": {
 *       "type": "INTEGER",
 *       "class": "type",
 *       "constraints": {
 *         "enumeration": {
 *           "unknown": 1,
 *           "running": 2,
 *           "warning": 3,
 *           "testing": 4,
 *           "down": 5
 *         }
 *       }
 *     }
 */
public class Node {
    public String name;
    public String oid;
    public String nodeType;
    public String type;
    public String access;
    public String status;
    public String description;

    public Map<String, Object> constraints; // Constraints, include the name of the constraints and the actual constraints, since we do not
    // know in advance the type of the constraints, we use the Object type to store the constraints object.

    Map<String, Node> children = new HashMap<>();

    Node(String name, String oid, String nodeType, String type, String access, String status, String description, Map<String, Object> constraints) {
        this.name = name;
        this.oid = oid;
        this.nodeType = nodeType;
        this.type = type;
        this.access = access;
        this.status = status;
        this.description = description;
        this.constraints = constraints;
    }

    /**
     * When display the TreeView to the UI, each node has the Node type as we defined in this class. We just want
     * to display the Node name, not the whole Node object represents in string format. Override the toString method from TreeItem class
     *
     * @return Node name in the UI
     */
    @Override
    public String toString() {
        return name;
    }
}