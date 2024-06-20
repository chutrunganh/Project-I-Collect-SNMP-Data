package Model.SNMRequest;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

/**
 * Perform SNMP GET-NEXT request to retrieve the next OID value from an SNMP agent.
 *
 * @input: ipAddress (UdpAddress data type), community string, and OID we want to scan
 * @output: the response from the SNMP agent in the form of a VariableBinding object
 */
public class SNMPGetNext {

    private final VariableBinding vb; // Variable binding to store the response from the SNMP agent

    /**
     * Function to create a transport mapping
     */
    private TransportMapping<UdpAddress> createTransportMapping() throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen(); // Start listening for responses
        return transport;
    }

    /**
     * Function to create a target address
     */
    private CommunityTarget<UdpAddress> createTarget(UdpAddress ipAddress, String community) {
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community)); // Set the community string
        target.setVersion(SnmpConstants.version2c); // Set the SNMP version
        target.setAddress(ipAddress); // Set the address of the SNMP agent
        target.setRetries(2); // Set the number of retries
        target.setTimeout(1000); // Set the timeout (in milliseconds)
        return target;
    }

    /**
     * Function to create a PDU
     */
    private PDU createPDU(String oid) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid))); // Add the OID to the PDU
        pdu.setType(PDU.GETNEXT); // Set the type of the PDU to GETNEXT
        return pdu;
    }

    /**
     * Function to process the response from the SNMP agent
     *
     * @param response The ResponseEvent object containing the response from the SNMP agent
     * @return The VariableBinding object containing the response data
     */
    private VariableBinding processResponse(ResponseEvent<UdpAddress> response) {
        VariableBinding vb = null;
        if (response != null) {
            PDU responsePDU = response.getResponse(); // Retrieve the response PDU

            if (responsePDU != null) {
                int errorStatus = responsePDU.getErrorStatus();

                if (errorStatus == PDU.noError) {
                    vb = responsePDU.get(0); // Get the first VariableBinding directly
                } else {
                    System.out.println("Error: Request Failed");
                }
            } else {
                System.out.println("Error: Response PDU is null");
            }
        } else {
            System.out.println("Error: Agent Timeout...");
        }
        return vb;
    }

    /**
     * Constructor to perform the SNMP GET-NEXT request
     */
    public SNMPGetNext(UdpAddress ipAddress, String community, String oid) throws IOException {
        // Create a transport mapping
        TransportMapping<UdpAddress> transport = createTransportMapping();

        // Create a target address
        CommunityTarget<UdpAddress> target = createTarget(ipAddress, community);

        // Create a PDU
        PDU pdu = createPDU(oid);

        // Create an SNMP instance
        Snmp snmp = new Snmp(transport);

        // Send the GET-NEXT request
        ResponseEvent<UdpAddress> response = snmp.send(pdu, target);

        // Process the response
        vb = processResponse(response);

        // Close the SNMP session
        snmp.close();
    }

    /**
     * Getter
     * @return The VariableBinding object containing the response data
     */
    public VariableBinding getVariableBinding() {
        return this.vb;
    }
}
