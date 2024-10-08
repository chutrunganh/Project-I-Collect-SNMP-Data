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


/*
 * To verify that this class work correctly, we can use SNMP command line utility in 'snmp' package
 * by 'sudo apt install snmp', then run: 'snmpget -v2c -c public localhost desired_OID', some result
 * can be different from the result of
 * this class: https://stackoverflow.com/questions/70892857/why-did-snmp4j-return-different-result-with-net-snmp
 */

/**
 * @author Chu Trung Anh 20225564
 * Perform SNMP GET request to retrieve the value of a specific OID from an SNMP agent.
 *
 * @input: ipAddress (UdpAddress data type), community string, and OID we want to scan
 * @output: the response from the SNMP agent in the form of a VariableBinding object
 */
public class SNMPGet {

    private final VariableBinding vb; // Variable binding to store the response from the SNMP agent

    /**
     * Function to create a transport mapping
     * Here, since we only work with UDP transport protocol, we set the type of generic classes
     * like TransportMapping, ResponseEvent to UdpAddress.
     */
    private TransportMapping<UdpAddress> createTransportMapping() throws IOException {
        // Create a transport mapping using UDP protocol. This is used by the Snmp class to send requests.
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen(); // Start listening for responses
        return transport;
    }

    /**
     * Function to create a target address
     */
    private CommunityTarget<UdpAddress> createTarget(UdpAddress ipAddress, String community) {
        // Create a target address. This is where the SNMP request will be sent.
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community)); // Set the community string
        target.setVersion(SnmpConstants.version2c); // Set the SNMP version. Could be v1, v2c, or v3.
        target.setAddress(ipAddress); // Set the address of the SNMP agent. The port number is 161 for SNMP Get request.
        target.setRetries(2); // Set the number of retries when a request fails.
        target.setTimeout(1000); // Set the timeout (in milliseconds).
        return target;
    }

    /**
     * Function to create a PDU
     * What is a PDU? A PDU stands for Protocol Data Unit. In the context of SNMP (Simple Network Management Protocol),
     * a PDU is a packet of data that contains SNMP commands, variables, and associated identifiers.
     * Here, we are creating a PDU to send a GET request to the SNMP agent.
     */
    private PDU createPDU(String oid) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid))); // Add an OID (Object Identifier) to the PDU. This is what you want to get from the SNMP agent.
        pdu.setType(PDU.GET); // Set the type of the PDU to GETNEXT. It could also be SET, GET, GETBULK, etc.
        return pdu;
    }

    /*
     * Function to process the response
     * Some information about the PDU returned by the SNMP agent (ResponseEvent):
     * The ResponseEvent object contains the following key components:
     * - PeerAddress: The IP address and port of the SNMP agent that sent the response.
     * - Request: The original PDU request sent to the agent.
     * - Response: The PDU response received from the agent (or null if there was an error).
     *- ErrorStatus: An integer representing the error status code (0 indicates success).
     *- ErrorIndex: An integer indicating the index of the variable binding where an error occurred (if any).
     * Example output: RESPONSE[requestID=1109679558, errorStatus=Success(0), errorIndex=0, VBS[1.3.6.1.2.1.1.5.0 = ThinkPad-CTA]]
     *
     * Some related methods:
     * - responseEvent.getResponse(): Retrieves the PDU response.
     * - responsePDU.getErrorStatus(): Gets the error status code.
     * - responsePDU.getErrorStatusText(): Gets the error status descripAddresstion.
     * - responsePDU.getVariableBindings(): Retrieves a vector of variable bindings containing the requested data (for GET and GETNEXT responses).
     * - responsePDU.get(index): Retrieves a specific variable binding at the given index
     */

    /**
     * Function to process the response from the SNMP agent
     *
     * @param response: The ResponseEvent object containing the response from the SNMP agent
     * @return The VariableBinding object containing the response data
     */
    private VariableBinding processResponse(ResponseEvent<UdpAddress> response) {
        VariableBinding vb = null;
        if (response != null) {
            PDU responsePDU = response.getResponse(); //Retrieve the response PDU

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
     * Constructor to perform the SNMP GET request
     */
    public SNMPGet(UdpAddress ipAddress, String community, String oid) throws IOException {
        // Create a transport mapping
        TransportMapping<UdpAddress> transport = createTransportMapping();

        // Create a target address
        CommunityTarget<UdpAddress> target = createTarget(ipAddress, community);

        // Create a PDU
        PDU pdu = createPDU(oid);

        // Create an SNMP instance
        Snmp snmp = new Snmp(transport);

        // Send the GET request
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