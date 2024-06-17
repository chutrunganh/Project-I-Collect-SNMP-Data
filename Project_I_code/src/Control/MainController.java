package Control;

import Model.MIBTreeStructure.BuildTreeFromJson;
import Model.MIBTreeStructure.MibLoader;
import Model.MIBTreeStructure.Node;
import Model.SNMRequest.SNMPGet;
import Model.SNMRequest.SNMPWalk;
import Model.SNMRequest.SnmpResponseFormatter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static Model.SNMRequest.SnmpResponseFormatter.format;

/** @author Chu Trung Anh 20225564
 * MainController class is the controller class for the main.fxml file. It contains all the methods to handle the user's actions
 * in the SNMP Browser application.
 */
public class MainController {

    // Left side of the application
    @FXML private AnchorPane MIBTreeDisplay;  //Use to display list of MIBs which are loaded/opened by users + "Loaded MIBs" label
    @FXML private FlowPane MIBsloaded; //Use to display list of MIBs which are loaded/opened by users
    @FXML private TextField tfOID;

    // Bottom right side of the application
    @FXML private Label lbAccess;
    @FXML private Label lbName;
    @FXML private Label lbStatus;
    @FXML private Label lbType;
    @FXML private TextArea taDescription;
    @FXML private PasswordField tfCommunityString;
    @FXML private TextField tfTargetIP;

    // Query Table
    @FXML private TableView<ARowInQueryTable> queryTable;
    @FXML private TableColumn<ARowInQueryTable, String> nameColumn;
    @FXML private TableColumn<ARowInQueryTable, String> valueColumn;
    @FXML private TableColumn<ARowInQueryTable, String> typeColumn;


    private List<JsonNode> mibTree = new ArrayList<>();


    //Some default values for attributes used across the application
    String oidValue = null;
    String ip = "127.0.0.1"; //Localhost by default
    String community = "password"; //Community string by default
    String nodeType = null; //Node type of the selected node, help to distinguish between scalar
    // and non-scalar nodes. This is crucial to determine the OID to be used in SNMP Get
    Map<String, Object> constraints = new HashMap<>();


    /*
     * Load some default MIBs when the program starts
     */
    @FXML
    public void initialize() throws IOException {
        //Load the default MIBs to the MIBsloaded FlowPane
//        File defaultMIB1 = new File("Project_I_code/MIB Databases/RFC1213-MIB.json");
//        File defaultMIB2 = new File("Project_I_code/MIB Databases/HOST-RESOURCES-MIB.json");
//        showMIBsLoaded(defaultMIB1);
//        showMIBsLoaded(defaultMIB2);


        //loadMIBs();

        BuildTreeFromJson treeBuilder = new BuildTreeFromJson(); //Call the BuildTreeFromJson class to build the tree structure from the MIB files
        List<String> mibFilePaths = Arrays.asList(
                "Project_I_code/MIB Databases/SNMPv2-SMI.json",
                "Project_I_code/MIB Databases/RFC1213-MIB.json",
                "Project_I_code/MIB Databases/HOST-RESOURCES-MIB.json",
                "Project_I_code/MIB Databases/SNMPv2-MIB.json",
                "Project_I_code/MIB Databases/IF-MIB.json"
        );
        try {
            treeBuilder.buildTreeFromMultipleMIBs(mibFilePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TreeItem<Node> rootItem = treeBuilder.convertNodeToTreeItem(treeBuilder.getRoot());
        TreeView<Node> treeView = new TreeView<>(rootItem); //Create a TreeView object to display to UI from the rootItem

        //Set the action for each tree node, when clicked, display the information of the node in the bottom right pane
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Node>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Node>> observable, TreeItem<Node> oldValue, TreeItem<Node> newValue) {
                if (newValue != null) {
                    Node selectedNode = newValue.getValue();
                    //printNodeAttributes(selectedNode);
                    //Set the value to label
                    tfOID.setText(selectedNode.oid);
                    lbName.setText(selectedNode.name);
                    nodeType = selectedNode.nodeType; //No need to display this value
                    constraints = (HashMap<String, Object>) selectedNode.constraints; //No need to display this value
                    lbType.setText(selectedNode.type);
                    lbAccess.setText(selectedNode.access);
                    lbStatus.setText(selectedNode.status);
                    taDescription.setText(selectedNode.description);
                }
            }
        });


        //Expand the treeview to fit the Anchor Pane width and height
        treeView.prefWidthProperty().bind(MIBTreeDisplay.widthProperty());
        treeView.prefHeightProperty().bind(MIBTreeDisplay.heightProperty());
        MIBTreeDisplay.getChildren().add(treeView);


        //Initialize the query table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Double-click on a tree node to perform SNMP Get on that
        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<Node> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Node selectedNode = selectedItem.getValue();
                    //System.out.println("Double-clicked node: " + selectedNode.name); // Debug print

                    if (selectedNode.type != null && !selectedNode.type.isEmpty()) {
                        //System.out.println("Node has a type: " + selectedNode.type); // Debug print

                        // Set the necessary fields for SNMPGetClicked
                        tfOID.setText(selectedNode.oid);
                        lbName.setText(selectedNode.name);
                        lbType.setText(selectedNode.type);
                        nodeType = selectedNode.nodeType;  // Ensure nodeType is set
                        constraints = selectedNode.constraints;  // Ensure constraints is set

                        //System.out.println("Fields set. Calling SNMPGetClicked."); // Debug print

                        // Call SNMPGetClicked
                        try {
                            SNMPGetClicked(null); //Passing null as the event parameter, perform the same action as clicking the GET button
                        } catch (Exception e) {
                            System.out.println("Error calling SNMPGetClicked.");
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("Node type is null."); // Debug print
                    }
                } else {
                    System.out.println("No tree item selected."); // Debug print
                }
            }
        });



        // Add an event listener to the TableView. Double-lick on a row to highlight the corresponding node in the TreeView
        // From the name cell of the row, find the corresponding node in the TreeView with corresponding name and highlight/move to it.
        queryTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ARowInQueryTable selectedRow = queryTable.getSelectionModel().getSelectedItem();
                if (selectedRow != null) {

                    //If the node name contains postfix ".any_number", remove it before highlighting
                    //(e.g. "hrDeviceStatus.25", "hrDeviceStatus.10", "hrDeviceStatus.2004", etc.)
                    // Rows by SNMP walk will always have a postfix, SNMP Get rows will not if the node is scalar object
                    int lastDotPosition = selectedRow.getName().lastIndexOf(".");

                    if (lastDotPosition != -1) { //Check if the node name contains a postfix, then remove it and only search for the node name
                        String nodeName = selectedRow.getName().substring(0, lastDotPosition);
                        highlightNodeInTreeView(treeView, nodeName);
                    } else { //If the node name does not contain a postfix, search for the node name as it is
                        highlightNodeInTreeView(treeView, selectedRow.getName());
                    }
                }
            }
        });
    }


    public void loadMIBs() {
        File folder = new File("Project_I_code/MIB Databases");
        File[] listOfFiles = folder.listFiles();

        System.out.println("Loading MIBs from the MIB Databases directory...");
        System.out.println("Number of MIBs found: " + listOfFiles.length);

        ObjectMapper objectMapper = new ObjectMapper();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    JsonNode mib = objectMapper.readTree(file);
                    mibTree.add(mib);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("MIBs loaded successfully.");
        //print out the mibTree for testing
        for (JsonNode node : mibTree) {
            System.out.println(node);
        }

    }


    @FXML
    /**
     * Only open the MIB without saving it to the MIB Databases directory
     * Then show the MIB in the MIBsloaded FlowPane
     */
    void openMIBClicked(ActionEvent event) throws IOException {
//        //System.out.println("Open MIB Clicked");
//
//        //Ask user to select a file
//        FileChooser fileChooser = new FileChooser();
//        File file = fileChooser.showOpenDialog(null);
//        System.out.println("Opening: " + file.getName() + ".");
//
//        //Show the chosen file in loaded section
//        showMIBsLoaded(file);
    }


    @FXML
    /**
     * This method is called when the Load MIB button is clicked
     * It opens a file chooser dialog and allows the user to select a file, then saves the file to the MIB Databases directory also
     * Then show the MIB in the MIBsloaded FlowPane
     */
    void importMIBClicked(ActionEvent event) {
//        //System.out.println("Import MIB Clicked");
//
//        //Ask user to select a file
//        FileChooser fileChooser = new FileChooser();
//        File file = fileChooser.showOpenDialog(null);
//        System.out.println("Opening: " + file.getName() + ".");
//
//        try { //Save the file to the MIB Database directory
//
//            //Create a new file in the MIB Databases folder and write the contents of the selected file to it
//            Files.copy(file.toPath(), new File("Project_I_code/MIB-Browser/MIB Databases/" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
//            System.out.println("File Saved Successfully");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //Show the chosen file in loaded section
//        showMIBsLoaded(file);
    }


    /**
    * Function to show the MIBs loaded/opened by the user in the MIBsloaded FlowPane
    */
    public void showMIBsLoaded(File file) {
        if (file != null) {
            Label fileLabel = new Label(file.getName());
            //Set fileLabel width to the width of the FlowPane so that new labels are added horizontally
            fileLabel.prefWidthProperty().bind(MIBsloaded.widthProperty());

            MIBsloaded.getChildren().add(fileLabel);

            // Event handler for the file label. If use clicks on the label, display the TreeView of the chosen MIB
            fileLabel.setOnMouseClicked(e -> {
                try {
                    displayTreeFromChosenMIB(file);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

        }
    }


    /**
     * Function to display the TreeView of the chosen MIB from MIBsloaded FlowPane. The TreeView is proceeded by the JsonToTreeView class in
     * the MIBTreeView class
     * to transform the JSON file to a TreeView
     * */
    public void displayTreeFromChosenMIB(File jsonFile) throws IOException {
        //Clear the MIBTreeDisplay AnchorPane before displaying the new MIB
        MIBTreeDisplay.getChildren().clear();

        BuildTreeFromJson treeBuilder = new BuildTreeFromJson();

        List<String> mibFilePaths = Arrays.asList(
                "Project_I_code/MIB Databases/RFC1213-MIB.json",
                "Project_I_code/MIB Databases/HOST-RESOURCES-MIB.json"
        );
        try {
            treeBuilder.buildTreeFromMultipleMIBs(mibFilePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TreeItem<Node> rootItem = treeBuilder.convertNodeToTreeItem(treeBuilder.getRoot());
        TreeView<Node> treeView = new TreeView<>(rootItem);

    }



    /**
     * Method to handle the SNMP Get button click event. This method performs an SNMP Get operation on the selected OID.
     * If the selected node is a scalar object, the method appends ".0" to the OID before performing the SNMP Get.
     * If the selected node is a non-scalar object, the method try to append ".1" to ".1000" to the OID and performs
     * SNMP Get on each OID until it get a `noSuchInstance` response.
     */
    @FXML
    void SNMPGetClicked(MouseEvent event) {
        //System.out.println("Perfroming SNMP Get.......");

        oidValue = tfOID.getText(); // Get the OID from the text field
        if (oidValue.isEmpty()) {
            System.out.println("Error: OID field is empty");
            return;
        }

        // Get the target IP address from the text field, change it to UdpAddress format
        // In case user let this field empty, use the default IP address
        if (!tfTargetIP.getText().isEmpty()) {
            ip = "udp:" + tfTargetIP.getText() + "/161";
        } else {
            ip = "udp:127.0.0.1/161";
        }
        Address targetAddress = GenericAddress.parse(ip);

        //System.out.println("Still Working on this part 1");

        if (targetAddress instanceof UdpAddress udpTargetAddress) {
            // Get the community string from the password field
            if (!tfCommunityString.getText().isEmpty()) {
                community = tfCommunityString.getText();
            }

            //System.out.println("Still Working on this part 2");
            //System.out.println("Node Type: " + nodeType);

            if (nodeType.equals("scalar")) {  // If the nodeType is scalar, append ".0" to the OID
                oidValue = oidValue + ".0";
                try {

                    System.out.println("Still Working on this part 3");
                    SNMPGet snmpGet = new SNMPGet((UdpAddress) targetAddress, community, oidValue);
                    VariableBinding vb = snmpGet.getVariableBinding(); // Get the response from the SNMP request

                    if (vb != null) {
                        // Print raw response to console
                        //System.out.println("Get: " + oidValue + " : (raw value)  " + vb.getVariable().toString());

                        // Get the data type and constraints, then using SNMPResponseFormatter to format the response to human-readable format
                        String dataType = lbType.getText();
                        //constraint already defined and assign to the constraints variable when click on the tree node

                        String humanReadableValue = format(vb.getVariable(), dataType, constraints);
                        //System.out.println("Human Readable Value: " + humanReadableValue);

                        //Add the result to the query table
                        queryTable.getItems().add(new ARowInQueryTable(lbName.getText(), humanReadableValue, lbType.getText()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { // If the nodeType is not scalar, append from ".1" to ".1000" and perform SNMP Get until a `noSuchInstance` response is received
                //System.out.println("Still Working on this part 4 (handling non-scalar nodes)");
                for (int i = 1; i <= 1000; i++) {
                    String oidToTry = oidValue + "." + i;
                    try {
                        //System.out.println("Still Working on this part 5 (handling non-scalar nodes)");
                        SNMPGet snmpGet = new SNMPGet((UdpAddress) targetAddress, community, oidToTry);
                        VariableBinding vb = snmpGet.getVariableBinding(); // Get the response from the SNMP request

                        if (vb != null) {
                            // Print raw response to console
                            String response = vb.getVariable().toString();

                            // If the response is not "noSuchInstance", print it
                            if (!response.equals("noSuchInstance")) {
                                //System.out.println("Get: " + oidToTry + " : (raw value)  " + response);

                                // Get the data type and constraints, then using SNMPResponseFormatter to format the response to human-readable format
                                String dataType = lbType.getText();
                                //constraint already defined and assign to the constraints variable when click on the tree node
                                String humanReadableValue = format(vb.getVariable(), dataType, constraints);
                                //System.out.println("Human Readable Value: " + humanReadableValue);

                                //Add the result to the query table
                                queryTable.getItems().add(new ARowInQueryTable(lbName.getText() + "." + i, humanReadableValue, lbType.getText()));
                                // Display name as "name.i" to indicate the instance number
                            }


                            // If the response is "noSuchInstance", break the loop
                            if (response.equals("noSuchInstance")) {
                                if (i == 1) { // If the first response is "noSuchInstance", mean that it is not exist in the device
                                    System.out.println("Error: Failed to get response for OID: " + oidToTry);
                                    queryTable.getItems().add(new ARowInQueryTable(lbName.getText(), "noSuchInstance", lbType.getText()));
                                    break;
                                } else { // If the response is "noSuchInstance" after some iteration, mean that it exits, we have reached the end of the table, break the loop
                                    break;
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Error: Invalid target address");
        }
    }


    /**
     * Method to handle the SNMP Walk button click event. This method performs an SNMP Walk operation on from the selected OID.
     * @param event
     */
    @FXML
    void SNMPWalkClicked(MouseEvent event) {

        //System.out.println("Performing SNMP Walk.......");
        // Get the OID from the text field
        oidValue = tfOID.getText();

        // Get the target IP address from the text field, change it to UdpAddress format
        // In case user leaves this field empty, use the default IP address
        if (!tfTargetIP.getText().isEmpty()) {
            ip = "udp:" + tfTargetIP.getText() + "/161";
        } else {
            ip = "udp:127.0.0.1/161";
        }

        if (!tfCommunityString.getText().isEmpty()) {
            community = tfCommunityString.getText();
        }
        Address targetAddress = GenericAddress.parse(ip);

        if (targetAddress instanceof UdpAddress udpTargetAddress) {

            try {
                // Initialize SNMPWalk with target address, community string, and MIB folder path
                SNMPWalk snmpWalk = new SNMPWalk((UdpAddress) targetAddress, community);
                snmpWalk.start(); // Start the SNMP session
                List<VariableBinding> varBindings = snmpWalk.performSNMPWalk(oidValue);

                // Create MibLoader instance to load MIB files and resolve OIDs
                MibLoader mibLoader = new MibLoader();
                mibLoader.loadMibsFromFolder("Project_I_code/MIB Databases/");

                // Handle the results of the SNMP walk
                // For each response from SNMP Walk, extract the OID and the value
                // With OID, look up the corresponding node in the MIB files and extract the name, data type, and constraints
                // With value, format it to human-readable format based on finding data type and constraints
                for (VariableBinding varBinding : varBindings) {
                    // OID retrieved from the SNMP Walk response always contains the instance number (postfix) so we must remove from last dot till the end to get the
                    // base OID, then we can look up for this base OID in the MIB files.
                    String oid = varBinding.getOid().toString();
                    // Find the position of the last dot
                    int lastDotPosition = oid.lastIndexOf('.');
                    // Remove everything after the last dot
                    String oidWithoutLastPart = oid.substring(0, lastDotPosition);
                    // Lookup the node using the modified OID
                    Node node = mibLoader.lookupNode(oidWithoutLastPart);
                    //System.out.println("Node: " + node);

                    if (node != null) {
                        //System.out.println("Look up by OID Fucking work!");
                        String name = node.name;
                        String dataType = node.type;
                        Map<String, Object> constraint = node.constraints;

                        //System.out.println("OID: " + oid + ", Name: " + name + ", DataType: " + dataType + ", Constraints: " + constraint);
                        // Convert the variable to a human-readable format
                        String humanReadableValue = format(varBinding.getVariable(), dataType, constraints);
                        //System.out.println("Human Readable Value: " + humanReadableValue);
                        //Add the result to the query table
                        queryTable.getItems().add(new ARowInQueryTable(name + oid.substring(lastDotPosition), humanReadableValue, dataType));
                    } else { //In case we can find that node base on OID, return the OID and raw inout
                        //System.out.println("Can not find OID: " + oid + ": return raw value: " + varBinding.getVariable().toString());
                        //Add the result to the query table
                        queryTable.getItems().add(new ARowInQueryTable(oid, varBinding.getVariable().toString() + " (raw value)", "None defined"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: Invalid target address");
        }
    }


    /**
     * Method to handle the Clear Table button click event. This method clears the query table.
     * used for the red "X" button in the UI, beside the query table
     * @param event
     */
    @FXML
    public void clearTableClicked(MouseEvent event) {
        // Clear the query table
        queryTable.getItems().clear();
    }


    /**
    * Method to highlight a node in the TreeView, this method is called when user double-click on a row in the query table, it will
     * highlight the corresponding node in the TreeView (we map to the corresponding node by the name of the node)
     */
    // Method to highlight a node in the TreeView
    private void highlightNodeInTreeView(TreeView<Node> treeView, String nodeName) {
        TreeItem<Node> root = treeView.getRoot();
        TreeItem<Node> targetNode = findNode(root, nodeName);

        if (targetNode != null) {
            treeView.getSelectionModel().select(targetNode);
            treeView.scrollTo(treeView.getRow(targetNode));
        }
    }

    // Recursive method to find a node by name in the TreeView, the "name" has been normalized to remove the postfix ".any_number"
    // if needed by the method calling this method
    private TreeItem<Node> findNode(TreeItem<Node> currentNode, String nodeName) {
        if (currentNode.getValue().name.equals(nodeName)) {
            return currentNode;
        }
        for (TreeItem<Node> child : currentNode.getChildren()) {
            TreeItem<Node> result = findNode(child, nodeName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }


}
