package Model.SNMRequest;

import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/*
 * To verify that this class work correctly, we can use SNMP command line utility in 'snmp' package
 * by 'sudo apt install snmp', then run: 'snmpwalk -v2c -c public localhost desired_OID_to_perform_scan_from', some result
 * can be different from the result of
 * this class: https://stackoverflow.com/questions/70892857/why-did-snmp4j-return-different-result-with-net-snmp
 */

/*
 * We can write SNMPWalk by using GETNEXT PDU type to get the next OID in the MIB tree like this example:
 * https://github.com/micmiu/snmp-tutorial/blob/master/snmp4j-1x-demo/src/main/java/com/micmiu/snmp4j/demo1x/SnmpWalk.java.
 * Theoretically, SNMP Walk works by this way: send a continuous GETNEXT request to
 * the SNMP agent until it covers the whole tree
 *
 * However, SNMP4J library provides a utility class called TreeUtils that can be used to perform SNMP
 * walk as suggested from: https://stackoverflow.com/questions/19025802/snmp4j-snmpwalk-with-community-string-indexing ,
 * see this example: https://examples.javacodegeeks.com/java-development/enterprise-java/snmp4j/snmp-walk-example-using-snmp4j/
 *
 * */

/** @author Chu Trung Anh 20225564
 * When performing SNMP Walk, we scan the whole tree structure (or a part of it if we specify the start OID) of the SNMP agent.
 * This tree structure is already embedded in the SNMP agent firewall. That is why we can perform SNMP Walk without providing any
 * MIB file.
 * <p>
 * But if we want to read the value retrieved from the SNMP Walk (in human-readable format), we will need to know the data type of the
 * OID we are scanning. And the data type associates with the OID is stored in the MIB files provided by vendors for their specific devices.
 * That is why we need the MIB files. These parse processing will be further implemented in the SnmpResponseFormatter class.
 *
 * @input: This SNMPWalk class takes the start OID, IP address, community string of the SNMP agent as input, then perform the SNMP Walk operation
 * @return: a list of VariableBinding objects, each object contains the OID and the value of the OID in the SNMP agent
 * */
public class SNMPWalk {

    private static final int RETRIES = 1; // Number of retries when a request fails
    private static final int TIMEOUT = 150; // Timeout in milliseconds
    private static final int VERSION = SnmpConstants.version2c; // SNMP version. Could be v1, v2c, or v3

    private Snmp snmp = null;
    private UdpAddress ipAddress = null;
    private String communityString = null;

    /**
     * Constructor for the SNMPWalk class.
     *
     * @param ipAddress         The  IP ipAddress of the target SNMP agent in UdpAddress format.
     * @param communityString The community string for the SNMP agent.
     */
    public SNMPWalk(UdpAddress ipAddress, String communityString) {
        this.ipAddress = ipAddress;
        this.communityString = communityString;
    }

    public void start() throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    /**
     * Creates a CommunityTarget for the SNMP request.
     *
     * @return The created CommunityTarget.
     */
    private CommunityTarget<UdpAddress> createCommunityTarget() {
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(communityString));
        target.setAddress(ipAddress);
        target.setRetries(RETRIES);
        target.setTimeout(TIMEOUT);
        target.setVersion(VERSION);
        return target;
    }

    /**
     * Performs an SNMP walk operation starting from the specified OID.
     *
     * @param startOid The OID to start the walk from.
     * @return A list of VariableBindings representing the results of the walk.
     * I just store the response in a list of VariableBinding objects instead of parse it to human-readable format
     * immediately, this will be done in the MainController class to remain the code readability.
     */
    public List<VariableBinding> performSNMPWalk(String startOid) throws IOException {
        //System.out.println("Performing SNMP walk for OID: " + startOid);

        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(createCommunityTarget(), new OID(startOid));

        if (events == null || events.isEmpty()) {
            System.out.println("No result returned.");
            return Collections.emptyList();
        }

        //System.out.println("SNMP walk completed. Extracting VariableBindings...");

        List<VariableBinding> varBindings = extractVariableBindingsFromEvents(startOid, events);

        //System.out.println("Extracted " + varBindings.size() + " VariableBindings");

        return varBindings;
    }

    private List<VariableBinding> extractVariableBindingsFromEvents(String startOid, List<TreeEvent> events) {
        List<VariableBinding> varBindingsList = new ArrayList<>();

        for (TreeEvent event : events) {
            if (event != null) {
                if (event.isError()) {
                    System.err.println("oid [" + startOid + "] " + event.getErrorMessage());
                }

                VariableBinding[] varBindings = event.getVariableBindings();
                if (varBindings != null) {
                    //System.out.println("Found " + varBindings.length + " VariableBindings in event");
                    varBindingsList.addAll(Arrays.asList(varBindings));
                }
            }
        }
        return varBindingsList;
    }
}