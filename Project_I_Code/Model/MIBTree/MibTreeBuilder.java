package Project_I_Code.Model;

import javafx.scene.control.TreeItem;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpAccess;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpStatus;

//https://www.mibble.org/doc/faq-java-api.html
public class MibTreeBuilder {

    public static MibNode buildMibTree(MibValueSymbol symbol) {
        String oid = symbol.getValue().toString();
        SnmpAccess access = null;
        SnmpStatus status = null;
        String description = null;

        /*
         * The MibValueSymbol class does not have getAccess(), getStatus(), and
         * getDescription() methods. These methods are specific to the
         * SnmpObjectType class. That's why you need to check if the type of the
         * symbol is an instance of SnmpObjectType and then cast it to S
         * nmpObjectType to call these methods.
         *
         * */

        if (symbol.getType() instanceof SnmpObjectType snmpType) {
            access = snmpType.getAccess();
            status = snmpType.getStatus();
            description = snmpType.getDescription();
        }

        MibNode node = new MibNode(symbol.getName(), symbol.getType(), oid, access, status, description);

        for (MibValueSymbol child : symbol.getChildren()) {
            node.addChild(buildMibTree(child));
        }

        return node;
    }

    public static TreeItem<MibNode> convertToTreeItem(MibNode node) {
        TreeItem<MibNode> item = new TreeItem<>(node);

        for (MibNode childNode : node.getChildren()) {
            item.getChildren().add(convertToTreeItem(childNode));
        }

        return item;
    }

}