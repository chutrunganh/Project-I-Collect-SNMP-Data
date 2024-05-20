package Project_I_Code.Model.MIBTree;

import net.percederberg.mibble.MibType;
import net.percederberg.mibble.snmp.SnmpAccess;
import net.percederberg.mibble.snmp.SnmpStatus;

import java.util.ArrayList;
import java.util.List;

/*
 * The MibNode class represents a node in the MIBtree. Each MibNode object holds information
 * about a specific MIB node, including its name, syntax, OID, access level, status, and description.
 */
public class MibNode {

    private final List<MibNode> children;
    public String oid;
    public String name;
    public SnmpAccess access;
    public SnmpStatus status;
    public MibType syntax;
    public String description;

    public MibNode(String name, MibType syntax, String oid, SnmpAccess access, SnmpStatus status, String description) {
        this.name = name;
        this.syntax = syntax;
        this.oid = oid;
        this.access = access;
        this.status = status;
        this.description = description;
        this.children = new ArrayList<>();
    }

    /*
     * This method is used to get the children of the node, this
     * is used buildMibTree method in MibTreeBuilder class to recursively
     * build the tree
     */
    public List<MibNode> getChildren() {
        return children;
    }

    public void addChild(MibNode child) {
        children.add(child);
    }

    @Override
    /*
     * The TreeView in JavaFX uses the toString method to display the nodes in the tree, so
     * we may want to override this toString method to return the name of the node instead of return the
     * string representation of the MibNode objects.
     */
    public String toString() {
        return name;
    }

}