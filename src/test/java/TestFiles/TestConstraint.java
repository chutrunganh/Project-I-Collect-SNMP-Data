package TestFiles;

import Model.MIBTreeStructure.MibLoader;
import Model.MIBTreeStructure.Node;

import Model.SNMRequest.SnmpResponseFormatter;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

/** @author: Chu Trung Anh 20225564
 * Test for MibLoader class and SnmpResponseFormatter class
 * This class is used to test if we are successfully finding the Node in MIB files based on the OID we specified, then process the Enumeration constraints
 * The OID and return value can be simulated by the user.
 */
public class TestConstraint {

    public static void main(String[] args) {
        MibLoader mibLoader = new MibLoader();
        mibLoader.loadMibsFromFolder("MIB Databases/");

        // Example OID to lookup
        String oidToLookup = "1.3.6.1.2.1.2.2.1.8"; // Example OID

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

            // Simulate SNMP response
            VariableBinding vb = new VariableBinding();
            vb.setOid(new OID(oidToLookup));
            vb.setVariable(new Integer32(2));

            // Format SNMP response
            String formattedResponse = SnmpResponseFormatter.format(vb.getVariable(), node.type, node.constraints);
            System.out.println("Formatted Response: " + formattedResponse);
        } else {
            System.out.println("No node found for OID: " + oidToLookup);
        }
    }
}
