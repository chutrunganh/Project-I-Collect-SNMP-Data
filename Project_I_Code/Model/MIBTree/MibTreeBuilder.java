package Project_I_Code.Model.MIBTree;

import javafx.scene.control.TreeItem;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpAccess;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpStatus;

/*
 * The MibTreeBuilder class is used to build the MIB tree from the MIB file. See the example
 * from the Mibble API doc: https://www.mibble.org/doc/faq-java-api.html .
 * */
public class MibTreeBuilder {

    /*
     * This method takes a MibValueSymbol as an argument, which represents a symbol in the MIB file.
     * It creates a MibNode for the symbol and then recursively builds MibNode instances for all of
     * its children. The method will start to build from the root node as we will pass the root node to
     * this method in the MainController class.
     */

    /*
     * MibValueSymbol: In the Mibble API, a MibValueSymbol represents a symbol in the MIB that has an
     * associated value. This includes all types of MIB objects. Each MibValueSymbol has a name, a type,
     * and a value. The value of a MibValueSymbol is typically an Object Identifier (OID).
     */
    public static MibNode buildMibTree(MibValueSymbol symbol) {
        String oid = symbol.getValue().toString();
        SnmpAccess access = null;
        SnmpStatus status = null;
        String description = null;

        /*
         * The MibValueSymbol class does not have getAccess(), getStatus(), and
         * getDescription() methods. These methods are specific to the
         * SnmpObjectType class. That is why we need to check if the type of the
         * symbol is an instance of SnmpObjectType and then cast it to S
         * nmpObjectType to call these methods.
         */

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


    /*
     * This method converts a MibNode and its descendants into a TreeItem<MibNode>
     * structure that can be displayed in a JavaFX TreeView. The method works by creating a
     * TreeItem for the MibNode and then recursively converting each of its children into a TreeItem
     */
    public static TreeItem<MibNode> convertToTreeItem(MibNode node) {
        TreeItem<MibNode> item = new TreeItem<>(node);

        for (MibNode childNode : node.getChildren()) {
            item.getChildren().add(convertToTreeItem(childNode));
        }

        return item;
    }
}


/*
 * The structure of a TreeView object in JavaFX:
 * - TreeView: Think of TreeView as the entire window or container where your tree structure will be displayed.
 * It's the main visual component that the user interacts with. It holds the root TreeItem. From this root, the
 * entire hierarchical structure unfolds.
 *
 * - TreeItem: Each node in your tree is represented by a TreeItem. A TreeItem can hold some data (like a string,
 * an object, etc.) and can also have children (which are also TreeItems). (TreeItem is considered as The Building Blocks)
 *
 * - TreeCell: A TreeCell is responsible for how each individual node (TreeItem) is displayed within the TreeView. It determines
 * the text, icons, and overall appearance of a single node in the tree. Since the TreeCell relates to the visual representation, I
 * put it in the GUI package in MainController.java.
 */