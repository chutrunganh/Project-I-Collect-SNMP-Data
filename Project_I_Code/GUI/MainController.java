package Project_I_Code.GUI;

import Project_I_Code.Model.MibNode;
import Project_I_Code.Model.SNMPValueConverter;
import Project_I_Code.Model.SNMPWalk;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static Project_I_Code.Model.MibTreeBuilder.buildMibTree;
import static Project_I_Code.Model.MibTreeBuilder.convertToTreeItem;


public class MainController {

    MibLoader loader = new MibLoader();
    @FXML
    private AnchorPane standardMIBTreePanel;
    @FXML
    private Label lbAccess;
    @FXML
    private Label lbName;
    @FXML
    private Label lbStatus;
    @FXML
    private Label lbType;
    @FXML
    private TextArea taDescription;
    @FXML
    private PasswordField tfCommunityString;
    @FXML
    private TextField tfOID;
    @FXML
    private TextField tfTargetIPAddress;
    // Query table
    @FXML
    private TableView<ARowInQueryTable> queryTable;
    @FXML
    private TableColumn<ARowInQueryTable, String> nameColumn;
    @FXML
    private TableColumn<ARowInQueryTable, String> syntaxColumn;
    @FXML
    private TableColumn<ARowInQueryTable, String> valueColumn;

    @FXML
    public void initialize() throws MibLoaderException, IOException {
        // Load the MIB files
        Mib mibRFC1213 = loader.load("RFC1213-MIB");  // replace with your MIB file name
        loader.load("IF-MIB");
        loader.load("IP-MIB");
        loader.load("TCP-MIB");
        loader.load("UDP-MIB");
        loader.load("SNMPv2-MIB");
        loader.load("HOST-RESOURCES-MIB");
        loader.load("ENTITY-MIB");
        loader.load("BRIDGE-MIB");
        // Get the root MIB objects
        MibValueSymbol rootRFC1213 = mibRFC1213.getRootSymbol();


        // Build the MIB trees
        MibNode rootNodeRFC1213 = buildMibTree(rootRFC1213);

        // Convert the MIB trees to TreeItems
        TreeItem<MibNode> rootItemRFC1213 = convertToTreeItem(rootNodeRFC1213);

        // Create the TreeViews
        TreeView<MibNode> treeRFC1213 = new TreeView<>(rootItemRFC1213);
        //Extend to fit the Pane
        // Bind the TreeView's prefWidthProperty and prefHeightProperty to the AnchorPane's widthProperty and heightProperty
        treeRFC1213.prefWidthProperty().bind(standardMIBTreePanel.widthProperty());
        treeRFC1213.prefHeightProperty().bind(standardMIBTreePanel.heightProperty());

        // Add the TreeView to the AnchorPane
        standardMIBTreePanel.getChildren().add(treeRFC1213);

        // Add listeners to the TreeViews' selection models
        addSelectionListener(treeRFC1213);


        // Set up the query table
        // Set up the TableView. The TableView requires ti initialize the columns and set the cell value factory at start
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        syntaxColumn.setCellValueFactory(new PropertyValueFactory<>("syntax"));

    }

    private void addSelectionListener(TreeView<MibNode> tree) {
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                MibNode selectedNode = newValue.getValue();

                tfOID.setText(selectedNode.oid); //gt OID first, even it is not the leaf node

                if (selectedNode.access == null) {
                    return; //if the selected node is not a leaf node, do not show anything instead
                    // of throwing error in the console
                }
                System.out.println("-------------------------------------------------");
                System.out.println("Name: " + selectedNode.name);
                System.out.println("OID: " + selectedNode.oid);
                System.out.println("Access: " + selectedNode.access);
                System.out.println("Status: " + selectedNode.status);
                if (selectedNode.syntax instanceof SnmpObjectType snmpType) {
                    System.out.println("Type: " + snmpType.getSyntax().getName());
                    lbType.setText(snmpType.getSyntax().getName());

                }
                System.out.println("Description: " + selectedNode.description);

                //Also update the labels and text area
                lbName.setText(selectedNode.name);
                lbAccess.setText(selectedNode.access.toString());
                lbStatus.setText(selectedNode.status.toString());
                taDescription.setText(selectedNode.description);


            }
        });
    }

    @FXML
    void SNMPGetButtonClicked(MouseEvent event) {
//        // Get the OID from the text field
//        String oid = tfOID.getText();
//        // Get the target IP address from the text field
//        String targetIPAddress = tfTargetIPAddress.getText();
//        // Get the community string from the password field
//        String communityString = tfCommunityString.getText();
//
//        //Perform SNMP GET
//        try {
//            SNMPGet snmpGet = new SNMPGet(targetIPAddress, communityString, oid);
//            VariableBinding vb = snmpGet.getVariableBinding(); //Get the response from the SNMP request
//            String responseToProcess = vb.getVariable().toString(); //Get the response as a string
//            ParseSNMPRespone parser = new ParseSNMPRespone(responseToProcess, type); //Create a new ParseSNMPRespone object to parse the response
//            String value = parser.returnValueBasedOnDataType(responseToProcess, type); //Get the value of the response based on the data type of the node
//            addRowToQueryTable(name, value, type); //Add the new value to the query table
//
//            //Print raw response to console to debug
//            System.out.println("Response: " + responseToProcess);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
    }


    @FXML
    void SNMPWalkButtonCLicked(MouseEvent event) {
        // Get the OID from the text field
        String oid = tfOID.getText();
        // Get the target IP address from the text field, change it to UdpAddress
        String targetAddressString = "udp:127.0.0.1/161";
        Address targetAddress = GenericAddress.parse(targetAddressString);
        if (targetAddress instanceof UdpAddress udpTargetAddress) {
            // Get the community string from the password field
            String communityString = "password";

            //Perform SNMP Walk
            try {
                SNMPWalk snmpWalk = new SNMPWalk(udpTargetAddress, communityString);
                snmpWalk.start();
                List<VariableBinding> varBindings = snmpWalk.performSNMPWalk(oid);

                SNMPValueConverter converter = new SNMPValueConverter(loader);

                // Use a Set to store the OIDs and ignore duplicates
                Set<String> oidSet = new HashSet<>();

                for (VariableBinding varBinding : varBindings) {
                    String oidFromWalk = varBinding.getOid().toString();

                    // If the OID is not in the Set, process it and add it to the Set
                    if (!oidSet.contains(oidFromWalk)) {
                        Pair<String, String> result = converter.convertToHumanReadable(varBinding.getVariable(), oidFromWalk);
                        String humanReadableValue = result.getKey();
                        String name = result.getValue();
                        System.out.println(name + " [" + oidFromWalk + "] " + " : " + humanReadableValue);

                        // Create a new row in the query table for each VariableBinding
                        ARowInQueryTable row = new ARowInQueryTable(name, humanReadableValue, varBinding.getVariable().getSyntaxString());
                        queryTable.getItems().add(row);

                        // Add the OID to the Set
                        oidSet.add(oidFromWalk);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid IP address format.");
        }
    }


    @FXML
    public void clearTableClicked(MouseEvent event) {
        queryTable.getItems().clear();
    }


}