package Project_I_Code.GUI;

import Project_I_Code.Model.MIBTree.MibNode;
import Project_I_Code.Model.SNMPRequest.SNMPGet;
import Project_I_Code.Model.SNMPRequest.SNMPValueConverter;
import Project_I_Code.Model.SNMPRequest.SNMPWalk;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.util.Pair;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static Project_I_Code.Model.MIBTree.MibTreeBuilder.buildMibTree;
import static Project_I_Code.Model.MIBTree.MibTreeBuilder.convertToTreeItem;


public class MainController {

    MibLoader loader = new MibLoader(); //manage all the MIB loads to the Ram

    // Top left panel
    @FXML
    private AnchorPane standardMIBTreePanel;

    //Bottom left panel
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

    // Top right panel
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

    // Second Tab top left pane
    @FXML
    private ComboBox<String> cbChooseVendors;
    @FXML
    private FlowPane vendorMIBs;


    //Default value if user does not input anything
    public String targetAddressString = "udp:127.0.0.1/161";
    public String communityString = "password";

    //Some attributes to use cross multiple methods
    public String dataType;
    public String name;
    public String oid;

    SNMPValueConverter converter;  //Convert the raw response to human readable format

    @FXML
    public void initialize() throws MibLoaderException, IOException {
        // Load the MIB files
        Mib mibRFC1213 = loader.load("RFC1213-MIB");  // replace with your MIB file name
        loader.load("RFC1155-SMI");
        loader.load("RFC-1212");
        loader.load("SNMPv2-SMI");
        loader.load("SNMPv2-TC");
        loader.load("SNMPv2-CONF");
        //loader.load("HOST-RESOURCES-MIB");

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


        converter = new SNMPValueConverter(); // Create a new instance of the SNMPValueConverter class

    }

    private void addSelectionListener(TreeView<MibNode> tree) {
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                MibNode selectedNode = newValue.getValue();


                // Update the labels and text area with the selected node's information
                lbName.setText(selectedNode.name);
                tfOID.setText(selectedNode.oid);

                dataType = converter.getDataTypeAndName(selectedNode.oid).getValue().getKey();
                lbType.setText(dataType);
                name = selectedNode.name;
                oid = selectedNode.oid;
                taDescription.setText(selectedNode.description);

                //Check if access, status are null, if it is, set it to "Not Defined"
                lbAccess.setText(selectedNode.access == null ? "" : selectedNode.access.toString());
                lbStatus.setText(selectedNode.status == null ? "" : selectedNode.status.toString());
            }
        });
    }


    //SNMP GET button clicked
    @FXML
    void SNMPGetButtonClicked(MouseEvent event) {
        // Get the OID from the text field
        oid = tfOID.getText();
        // Get the target IP address from the text field, change it to UdpAddress

        //In case use let this field empty, use the default IP address
        if (!tfTargetIPAddress.getText().isEmpty()) {
            targetAddressString = "udp:" + tfTargetIPAddress.getText() + "/161";
        }
        Address targetAddress = GenericAddress.parse(targetAddressString);
        if (targetAddress instanceof UdpAddress udpTargetAddress) {
            // Get the community string from the password field
            if (!tfCommunityString.getText().isEmpty()) {
                communityString = tfCommunityString.getText();
            }

            //Perform SNMP GET
            try {
                SNMPGet snmpGet = new SNMPGet(udpTargetAddress, communityString, oid);
                VariableBinding vb = snmpGet.getVariableBinding(); //Get the response from the SNMP request

                SNMPValueConverter converter = new SNMPValueConverter();
                //Get the data type of the OID
                //the variable dataType here is different from the dataType in the main controller, this is the data type of the OID + detail information
                Pair<String, HashMap<Integer, String>> dataType = converter.getDataTypeAndName(oid).getValue();
                String humanReadableValue = converter.convertToHumanReadable(vb.getVariable(), dataType);


                // Add the new value to the query table
                ARowInQueryTable row = new ARowInQueryTable(name, humanReadableValue, dataType.getKey());
                queryTable.getItems().add(row);

                //Print raw response to console to debug
                System.out.println("Get: " + oid + " : " + vb.getVariable().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid IP address format.");
        }
    }

    /*
     * Click on a non leave node in the MIB tree, just a directory to hold other nodes, perform SNMP GET on this OID
     * should return nothing, as I tried in other MIB Browser, it returns null
     *
     * However. in my case, it return the value of the nearest leaf node it can get the value
     *
     * Why ???
     * The behavior you're observing might be due to how the SNMP library you're
     * using handles SNMP GET requests for non-leaf nodes. In SNMP, non-leaf
     * nodes typically don't have associated data. They're usually just
     * containers for other nodes. Therefore, performing an SNMP GET
     * request on a non-leaf node's OID usually doesn't return a meaningful
     * result.  However, some SNMP libraries may handle this situation
     * differently. For example, when performing an SNMP GET request on a
     * non-leaf node's OID, they might automatically perform an SNMP GETNEXT
     * request instead. An SNMP GETNEXT request retrieves the value of the next
     * leaf node in the MIB tree. This could explain why you're able to perform
     * an SNMP GET request on a non-leaf node's OID and get a result.
     * */

    // SNMP Walk button clicked


    //Still meet issue with egp groups
    @FXML
    void SNMPWalkButtonCLicked(MouseEvent event) {
        // Get the OID from the text field if it is not empty
        oid = tfOID.getText();

        //In case use let this field empty, use the default IP address
        if (!tfTargetIPAddress.getText().isEmpty()) {
            targetAddressString = "udp:" + tfTargetIPAddress.getText() + "/161";
        }
        Address targetAddress = GenericAddress.parse(targetAddressString);
        if (targetAddress instanceof UdpAddress udpTargetAddress) {
            // Get the community string from the password field
            if (!tfCommunityString.getText().isEmpty()) {
                communityString = tfCommunityString.getText();
            }

            //Perform SNMP Walk
            try {
                SNMPWalk snmpWalk = new SNMPWalk(udpTargetAddress, communityString);
                snmpWalk.start();
                List<VariableBinding> varBindings = snmpWalk.performSNMPWalk(oid);

                SNMPValueConverter converter = new SNMPValueConverter();

                for (VariableBinding varBinding : varBindings) {
                    //Get the OID from the VariableBinding
                    String oidFromWalk = varBinding.getOid().toString();

                    Pair<String, Pair<String, HashMap<Integer, String>>> result = converter.getDataTypeAndName(oidFromWalk);
                    String nameInWalk = result.getKey();
                    Pair<String, HashMap<Integer, String>> dataType = result.getValue();

                    String humanReadableValue = converter.convertToHumanReadable(varBinding.getVariable(), dataType);

                    // Create a new row in the query table for each VariableBinding
                    ARowInQueryTable row = new ARowInQueryTable(nameInWalk, humanReadableValue, dataType.getKey());

                    // Add the new row to the query table
                    Platform.runLater(() -> queryTable.getItems().add(row));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid IP address format.");
        }
    }

    public MibValueSymbol locateSymbolByOid(MibLoader loader, String oid) {
        ObjectIdentifierValue iso = loader.getRootOid();
        ObjectIdentifierValue match = iso.find(oid);
        return (match == null) ? null : match.getSymbol();
    }


    // Clear the query table (toolbar beside the query table)
    @FXML
    public void clearTableClicked(MouseEvent event) {
        queryTable.getItems().clear();
    }


    // Second Tab of top left pane
    @FXML
    void chooseVendorsClicked(MouseEvent event) {
        // List of famous vendors
        List<String> vendors = Arrays.asList("Cisco", "Juniper", "Huawei", "Arista", "Dell", "HP", "IBM");

        // Clear the ComboBox
        cbChooseVendors.getItems().clear();

        // Add the vendors to the ComboBox
        cbChooseVendors.getItems().addAll(vendors);

        // Set the cell factory to use a ListCell with its prefWidthProperty bound to the widthProperty of the ComboBox
        cbChooseVendors.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            cell.prefWidthProperty().bind(cbChooseVendors.widthProperty());
            cell.setTextOverrun(OverrunStyle.CLIP);
            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                cell.setText(newItem);
            });
            return cell;
        });

        // Add the listener to the ComboBox
        addChoiceBoxListener(cbChooseVendors);
    }

    private void addChoiceBoxListener(ComboBox<String> choiceBox) {
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Clear the vendor MIBs FlowPane
                vendorMIBs.getChildren().clear();
                // Unload all the  current MIBs
                loader.unloadAll();
                loader.reset();

                // Get the common MIBs for the selected vendor
                List<String> commonMibs = getCommonMibsForVendor(newValue);

                // Load the root MIB file and build the MIB tree
                try {
                    // Load the root MIB file
                    Mib rootMibFile = loader.load(commonMibs.get(0));
                    for (String mib : commonMibs) {
                        loader.load(mib);
                    }

                    // Get the root MIB object
                    MibValueSymbol root = rootMibFile.getRootSymbol();

                    // Build the MIB tree
                    MibNode rootNode = buildMibTree(root);

                    // Convert the MIB tree to a TreeItem
                    TreeItem<MibNode> rootItem = convertToTreeItem(rootNode);

                    // Create a TreeView from the TreeItem
                    TreeView<MibNode> tree = new TreeView<>(rootItem);

                    // Extend the tree to fit the pane
                    tree.prefWidthProperty().bind(vendorMIBs.widthProperty());
                    tree.prefHeightProperty().bind(vendorMIBs.heightProperty());

                    // Add the TreeView to the FlowPane
                    vendorMIBs.getChildren().add(tree);
                } catch (MibLoaderException | IOException e) {
                    e.printStackTrace();
                }

                // Load the other MIB files without building their MIB trees
                for (int i = 1; i < commonMibs.size(); i++) {
                    try {
                        // Load the MIB file
                        loader.load(commonMibs.get(i));
                    } catch (MibLoaderException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private List<String> getCommonMibsForVendor(String vendor) {
        // List to store the common MIBs
        List<String> commonMibs = switch (vendor) {
            case "Cisco" -> List.of("IF-MIB");
            case "Juniper" -> List.of("RFC1213-MIB");
            // Add more cases for other vendors if needed
            default -> Collections.emptyList();
        };

        return commonMibs;
    }


    @FXML
    void stopOperationClicked(MouseEvent event) {
        //Stop the SNMP operation
        // DO later
    }


}