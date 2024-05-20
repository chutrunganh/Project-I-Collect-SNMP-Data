package Project_I_Code.Model;

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

public class SNMPWalk {

    private static final int RETRIES = 2;
    private static final int TIMEOUT = 1500;
    private static final int VERSION = SnmpConstants.version2c;

    private Snmp snmp = null;
    private UdpAddress address = null;
    private String communityString = null;

    public SNMPWalk(UdpAddress address, String communityString) {
        this.address = address;
        this.communityString = communityString;
    }

    public void start() throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    public List<VariableBinding> performSNMPWalk(String targetOid) throws IOException {
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(createCommunityTarget(), new OID(targetOid));

        if (events == null || events.isEmpty()) {
            System.out.println("No result returned.");
            return Collections.emptyList();
        }

        return extractVariableBindingsFromEvents(targetOid, events);
    }

    private CommunityTarget<UdpAddress> createCommunityTarget() {
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(communityString));
        target.setAddress(address);
        target.setRetries(RETRIES);
        target.setTimeout(TIMEOUT);
        target.setVersion(VERSION);
        return target;
    }

    private List<VariableBinding> extractVariableBindingsFromEvents(String targetOid, List<TreeEvent> events) {
        List<VariableBinding> varBindingsList = new ArrayList<>();

        for (TreeEvent event : events) {
            if (event != null) {
                if (event.isError()) {
                    System.err.println("oid [" + targetOid + "] " + event.getErrorMessage());
                }

                VariableBinding[] varBindings = event.getVariableBindings();
                if (varBindings != null) {
                    varBindingsList.addAll(Arrays.asList(varBindings));
                }
            }
        }
        return varBindingsList;
    }
}