import Model.MIBTreeStructure.MibLoader;
import Model.MIBTreeStructure.Node;

import java.io.IOException;

public class LookUpForOIDTest {

    public static void main(String[] args) {
        // Initialize MibLoader
        MibLoader mibLoader = new MibLoader();
        mibLoader.loadMibsFromFolder("Project_I_code/MIB Databases");

        // Example OID to lookup
        //String oidToLookup = "1.3.6.1.2.1.1.1"; // Example OID

        String oidToLookup = "1.3.6.1.2.1.2.2.1.8";

        // Perform lookup
        Node node = mibLoader.lookupNode(oidToLookup);

        // Print node details if found
        if (node != null) {
            System.out.println("Node found successfully!");
            System.out.println("Name: " + node.name);
            System.out.println("OID: " + node.oid);
            System.out.println("Type: " + node.type);
            System.out.println("Access: " + node.access);
            System.out.println("Status: " + node.status);
            System.out.println("Description: " + node.description);
            System.out.println("Constraints: " + node.constraints);
        } else {
            System.out.println("No node found for OID: " + oidToLookup);
        }
    }
}
