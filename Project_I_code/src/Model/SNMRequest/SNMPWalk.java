package Model.SNMRequest;

import Model.MIBTreeStructure.MibLoader;
import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.*;

import java.io.IOException;
import java.util.*;

public class SNMPWalk {

    private MibLoader mibLoader;

    private static final int RETRIES = 1; // Number of retries when a request fails
    private static final int TIMEOUT = 150; // Timeout in milliseconds
    private static final int VERSION = SnmpConstants.version2c; // SNMP version. Could be v1, v2c, or v3

    private Snmp snmp = null;
    private UdpAddress ipAddress = null;
    private String communityString = null;

    /**
     * Constructor for the SNMPWalk class.
     *
     * @param ipAddress         The IP address of the target SNMP agent in UdpAddress format.
     * @param communityString The community string for the SNMP agent.
     * @param mibFolderPath    The folder path containing MIB files.
     */

    public  String mibFolderPath;
    public SNMPWalk(UdpAddress ipAddress, String communityString, String mibFolderPath) {
        this.ipAddress = ipAddress;
        this.communityString = communityString;
       this.mibFolderPath = mibFolderPath;
        mibLoader = new MibLoader();
        mibLoader.loadMibsFromFolder(mibFolderPath);
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
     */
    public List<VariableBinding> performSNMPWalk(String startOid) throws IOException {
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(createCommunityTarget(), new OID(startOid));

        if (events == null || events.isEmpty()) {
            System.out.println("No result returned.");
            return Collections.emptyList();
        }

        return extractVariableBindingsFromEvents(startOid, events);
    }

    /**
     * Extracts the VariableBindings from the TreeEvents returned by the SNMP walk operation.
     *
     * @param startOid The OID to start the walk from.
     * @param events   The TreeEvents returned by the SNMP walk operation.
     * @return A list of VariableBindings representing the results of the walk.
     */
    private List<VariableBinding> extractVariableBindingsFromEvents(String startOid, List<TreeEvent> events) {
        List<VariableBinding> varBindingsList = new ArrayList<>();

        for (TreeEvent event : events) {
            if (event != null) {
                if (event.isError()) {
                    System.err.println("oid [" + startOid + "] " + event.getErrorMessage());
                }

                VariableBinding[] varBindings = event.getVariableBindings();
                if (varBindings != null) {
                    // Perform MIB lookup for each VariableBinding
                    for (VariableBinding varBinding : varBindings) {
                        String oid = varBinding.getOid().toString();
                        String name = mibLoader.lookupName(oid);
                        String dataType = mibLoader.lookupDataType(oid);

                        System.out.println("OID: " + oid + ", Name: " + name + ", DataType: " + dataType);
                        // Optionally, you can attach name and dataType to VariableBinding objects
                        // varBinding.setVariableName(new OctetString(name));
                        // varBinding.setSyntax(new OID(dataType));

                        varBindingsList.add(varBinding);
                    }
                }
            }
        }
        return varBindingsList;
    }

    public void close() throws IOException {
        if (snmp != null) {
            snmp.close();
        }
    }
}
