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

    // Top left panel: this is where we display the MIB tree
    @FXML
    private AnchorPane standardMIBTreePanel;

    //Bottom left panel: this is where we display the information of the selected node
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

    // Top right panel: this is we input, display the IP address, community string, OID
    @FXML
    private PasswordField tfCommunityString;
    @FXML
    private TextField tfOID;
    @FXML
    private TextField tfTargetIPAddress;

    // Query table to display the result of the SNMP GET and SNMP WALK
    @FXML
    private TableView<ARowInQueryTable> queryTable;
    @FXML
    private TableColumn<ARowInQueryTable, String> nameColumn;
    @FXML
    private TableColumn<ARowInQueryTable, String> dataTypeColumn;
    @FXML
    private TableColumn<ARowInQueryTable, String> valueColumn;

    // Second Tab top left pane for vendor sspecific MIBs
    @FXML
    private ComboBox<String> cbChooseVendors;
    @FXML
    private FlowPane vendorMIBs;


    //Default value if user does not input anything in the targetAddress, communityString text field
    public String targetAddressString = "udp:127.0.0.1/161";
    public String communityString = "password";

    //Some attributes to use cross multiple methods
    public String dataType;
    public String name;
    public String oid;

    SNMPValueConverter converter;  //Class contains methods to convert the raw response to human-readable format

    @FXML
    public void initialize() throws MibLoaderException, IOException {

        // Load some default standard MIB files
        Mib mibRFC1213 = loader.load("RFC1213-MIB");
        loader.load("RFC1155-SMI");
        loader.load("RFC-1212");
        loader.load("SNMPv2-SMI");
        loader.load("SNMPv2-TC");
        loader.load("SNMPv2-CONF");
        loader.load("HOST-RESOURCES-MIB");

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
        dataTypeColumn.setCellValueFactory(new PropertyValueFactory<>("dataType"));


        converter = new SNMPValueConverter(); // Create a new instance of the SNMPValueConverter class to use its methods to parse the raw response

    }


    /**
     * Add a listener to the TreeView's selection model to display the information of the selected node
     * in the bottom left panel. When we interact with the tree by clicking on a node, the information of that node
     * will be displayed in the bottom left panel.
     *
     * @param tree The TreeView to add the listener to
     */
    private void addSelectionListener(TreeView<MibNode> tree) {
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                MibNode selectedNode = newValue.getValue();

                // Update the labels and text area with the selected node's information
                lbName.setText(selectedNode.name);
                tfOID.setText(selectedNode.oid);
                name = selectedNode.name;
                oid = selectedNode.oid;

                //Get the data type of the OID by calling the method in the SNMPValueConverter class
                // I tried to use getType() in Mibble API to get the data type of the selected node (the datatype which assign to each node when they are added
                // to the constructing MIB tree), but it just returns the primitive type instead of the Textual Convention.
                dataType = converter.getDataTypeAndName(selectedNode.oid).getValue().getKey();
                lbType.setText(dataType);

                taDescription.setText(selectedNode.description);

                //Check if access, status are null, if it is, set it to "Not Defined" (non leaf node does not have access, status so we want to check this
                // , otherwise, it will throw null pointer exception when we click on non leaf node)
                lbAccess.setText(selectedNode.access == null ? "" : selectedNode.access.toString());
                lbStatus.setText(selectedNode.status == null ? "" : selectedNode.status.toString());
            }
        });
    }


    /**
     * SNMP Get button clicked
     */
    @FXML
    void SNMPGetButtonClicked(MouseEvent event) {

        oid = tfOID.getText(); // Get the OID from the text field

        // Get the target IP address from the text field, change it to UdpAddress format
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

            // Perform SNMP GET
            try {
                SNMPGet snmpGet = new SNMPGet(udpTargetAddress, communityString, oid);
                VariableBinding vb = snmpGet.getVariableBinding(); //Get the response from the SNMP request

                //Get the data type of the OID
                Pair<String, HashMap<Integer, String>> dataTypeAndDataTypeDetail = converter.getDataTypeAndName(oid).getValue(); //EX: "INTEGER", "1: up(1)"
                String humanReadableValue = converter.convertToHumanReadable(vb.getVariable(), dataTypeAndDataTypeDetail);


                // Add the new value to the query table
                ARowInQueryTable row = new ARowInQueryTable(name, humanReadableValue, dataTypeAndDataTypeDetail.getKey()); //EX: "INTEGER"
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


    /**
     * SNMP Walk button clicked
     */
    //Still meet issue when clicked egp groups. Seem like the OID of this groud is not valid in my machine (I checked in the MIB browser, it can not find this OID), so
    // we can not start the SNMP Walk operation from this OID
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
                    // EX of result: <ifAdminStatus, <INTEGER, {1 : up(1), 2 : down(2), 3 : testing(3)}>>
                    String nameInWalk = result.getKey();
                    Pair<String, HashMap<Integer, String>> dataTypeAndDataTypeDetail = result.getValue();

                    String humanReadableValue = converter.convertToHumanReadable(varBinding.getVariable(), dataTypeAndDataTypeDetail);

                    // Create a new row in the query table for each VariableBinding
                    ARowInQueryTable row = new ARowInQueryTable(nameInWalk, humanReadableValue, dataTypeAndDataTypeDetail.getKey());

                    // Add the new row to the query table
                    queryTable.getItems().add(row);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid IP address format.");
        }
    }


    /**
     * Clear the query table
     */
    @FXML
    public void clearTableClicked(MouseEvent event) {
        queryTable.getItems().clear();
    }


    /**
     ** Choose Vendors Combo Box clicked
     */
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

    /**
     * Add a listener to the ComboBox to display the MIB tree of the selected vendor
     *
     * @param choiceBox The ComboBox to add the listener to
     */
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

}