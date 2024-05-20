package Project_I_Code.Model;

import net.percederberg.mibble.MibType;
import net.percederberg.mibble.snmp.SnmpAccess;
import net.percederberg.mibble.snmp.SnmpStatus;

import java.util.ArrayList;
import java.util.List;

public class MibNode {
    private final List<MibNode> children;
    public String name;
    public MibType syntax;
    public String oid;
    public SnmpAccess access;
    public SnmpStatus status;
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

    public List<MibNode> getChildren() {
        return children;
    }

    public void addChild(MibNode child) {
        children.add(child);
    }

    @Override
    public String toString() {  // this method is used by the TreeView to display the name of the node instead of the hashcode
        return name;
    }

}