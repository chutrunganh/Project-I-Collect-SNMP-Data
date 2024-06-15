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


public class MainController {


    @FXML private AnchorPane MIBTreeDisplay;  //Use to display list of MIBs which are loaded/opened by users + "Loaded MIBs" label
    @FXML private FlowPane MIBsloaded; //Use to display list of MIBs which are loaded/opened by users
    @FXML private TextField tfOID;

    @FXML private Label lbAccess;
    @FXML private Label lbName;
    @FXML private Label lbStatus;
    @FXML private Label lbType;
    @FXML private TextArea taDescription;
    @FXML private PasswordField tfCommunityString;
    @FXML private TextField tfTargetIP;

    @FXML
    private TableView<ARowInQueryTable> queryTable;
    @FXML
    private TableColumn<ARowInQueryTable, String> nameColumn;
    @FXML
    private TableColumn<ARowInQueryTable, String> valueColumn;
    @FXML
    private TableColumn<ARowInQueryTable, String> typeColumn;

    SnmpResponseFormatter converter = new SnmpResponseFormatter();
    private List<JsonNode> mibTree = new ArrayList<>();


    String oidValue = null;
    String ip = "127.0.0.1"; //Localhost by default
    String community = "password"; //Community string by default
    String nodeType = null;
    Map<String,Object> constraints = new HashMap<>();

    TreeView<String> treeView;

    /*
     * Load some default MIBs when the program starts
     */
    @FXML
    public void initialize() throws IOException {
        //Load the default MIBs to the MIBsloaded FlowPane
        File defaultMIB1 = new File("Project_I_code/MIB Databases/RFC1213-MIB.json");
        File defaultMIB2 = new File("Project_I_code/MIB Databases/HOST-RESOURCES-MIB.json");
        showMIBsLoaded(defaultMIB1);
        showMIBsLoaded(defaultMIB2);


        //loadMIBs();

        BuildTreeFromJson treeBuilder = new BuildTreeFromJson();
//        try {
//            treeBuilder.buildTreeFromJson("Project_I_code/MIB Databases/IF-MIB.json");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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




        //Expand the treeview to fit the anchorpane width and height
        treeView.prefWidthProperty().bind(MIBTreeDisplay.widthProperty());
        treeView.prefHeightProperty().bind(MIBTreeDisplay.heightProperty());
        MIBTreeDisplay.getChildren().add(treeView);


        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<Node> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Node selectedNode = selectedItem.getValue();
                    System.out.println("Double-clicked node: " + selectedNode.name); // Debug print

                    if (selectedNode.type != null && !selectedNode.type.isEmpty()) {
                        System.out.println("Node has a type: " + selectedNode.type); // Debug print

                        // Set the necessary fields for SNMPGetClicked
                        tfOID.setText(selectedNode.oid);
                        lbName.setText(selectedNode.name);
                        lbType.setText(selectedNode.type);
                        nodeType = selectedNode.nodeType;  // Ensure nodeType is set
                        constraints = selectedNode.constraints;  // Ensure constraints is set

                        System.out.println("Fields set. Calling SNMPGetClicked."); // Debug print

                        // Call SNMPGetClicked
                        try {
                            SNMPGetClicked(null);
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
        /*
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
        /*
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


//    /*
//     * Function to show the MIBs loaded/opened by the user in the MIBsloaded FlowPane
//     * */
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


    /*
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


            /*
     * Function to handle the  click action on a tree cell /node. In the context of a TreeView, you can think of a
     * TreeItem as your data, the TreeCell as the visual representation of that data
     *
     * Left-click to choose that cell (mean we will get the value of that cell)
     * Right-click to show the context menu
     */
//    private void handleTreeCellClick(TreeCell<MIBNode> treeCell, MouseEvent event) {
//        if (event.getButton() == MouseButton.PRIMARY) {
//            // Get the TreeItem associated with the clicked TreeCell
//            TreeItem<MIBNode> treeItem = treeCell.getTreeItem();
//
//            // Traverse the TreeItem to find the 'oid' key
//            //Current implementation only checks the immediate children of the current item
//            oidValue = MIBTreeView.findOid(treeItem);
//            tfOID.setText(oidValue.replace("\"", "")); //remove the double quotes when displaying the oid value
//
//            //Load the information of the selected node to the bottom right Pane in Control
//            //Find the name, access, status, type, description of the node
//            String name = MIBTreeView.findName(treeItem);
//            lbName.setText(name);
//
//            //Type
//            String type = MIBTreeView.findType(treeItem).replace("\"", "");
//            lbType.setText(type);
//
//            //Access
//            String access = MIBTreeView.findAccess(treeItem).replace("\"", "");
//            lbAccess.setText(access);
//
//            //Status
//            String status = MIBTreeView.findStatus(treeItem).replace("\"", "");
//            lbStatus.setText(status);
//
//            //Description
//            String description = MIBTreeView.findDescription(treeItem).replace("\"", "");
//            taDescription.setText(description);
//
//
//        } else if (event.getButton() == MouseButton.SECONDARY) {
//            //Show the context menu
//            ContextMenu contextMenu = createContextMenu();
//            treeCell.setContextMenu(contextMenu);
//        }
//    }
//
//
//    /*
//     * Function to set the cell factory look (limit cell length) and add the context menu when right-clicked on a node
//     * */
//    private void setCellFactory(TreeView<MIBNode> treeView) {
//        // Set a custom cell factory to show the context menu
//        treeView.setCellFactory(tv -> new TreeCell<MIBNode>() {
//            @Override
//            public void updateItem(MIBNode item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty) {
//                    setText(null);
//                } else {
//                    String displayText = item.getKey();
//                    setText(displayText);
//                    setOnMouseClicked(event -> handleTreeCellClick(this, event));
//                }
//            }
//        });
//    }




    @FXML
    void SNMPGetClicked(MouseEvent event) {
        System.out.println("perfroming SNMP Get.......");
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


        System.out.println("Still Working on this part 1");

        if (targetAddress instanceof UdpAddress udpTargetAddress) {
            // Get the community string from the password field
            if (!tfCommunityString.getText().isEmpty()) {
                community = tfCommunityString.getText();
            }

            System.out.println("Still Working on this part 2");
            System.out.println("Node Type: " + nodeType);

            // If the nodeType is scalar, append ".0" to the OID
            if (nodeType.equals("scalar")) {
                oidValue = oidValue + ".0";
                try {

                    System.out.println("Still Working on this part 3");
                    SNMPGet snmpGet = new SNMPGet((UdpAddress) targetAddress, community, oidValue);
                    VariableBinding vb = snmpGet.getVariableBinding(); // Get the response from the SNMP request

                    if (vb != null) {
                        // Print raw response to console
                        System.out.println("Get: " + oidValue + " : (raw value)  " + vb.getVariable().toString());

                        // Get the data type and constraints
                        String dataType = lbType.getText();
                        //constraint already defined ad assign to the constraints variable

                        // Convert the variable to a human-readable format
                        String humanReadableValue = format(vb.getVariable(), dataType, constraints);
                        System.out.println("Human Readable Value: " + humanReadableValue);
                        //Add the result to the query table
                        queryTable.getItems().add(new ARowInQueryTable(lbName.getText(), humanReadableValue, lbType.getText()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // If the nodeType is not scalar, append from ".1" to ".100" and perform SNMP Get until a response is received
                for (int i = 1; i <= 100; i++) {
                    String oidToTry = oidValue + "." + i;
                    try {
                        SNMPGet snmpGet = new SNMPGet((UdpAddress) targetAddress, community, oidToTry);
                        VariableBinding vb = snmpGet.getVariableBinding(); // Get the response from the SNMP request

                        if (vb != null) {
                            // Print raw response to console
                            String response = vb.getVariable().toString();

                            // If the response is not "noSuchInstance", print it
                            if (!response.equals("noSuchInstance")) {
                                System.out.println("Get: " + oidToTry + " : (raw value)  " + response);

                                // Get the data type and constraints
                                String dataType = lbType.getText();
                                //constraint already defined ad assign to the constraints variable

                                // Convert the variable to a human-readable format
                                String humanReadableValue = format(vb.getVariable(), dataType, constraints);
                                System.out.println("Human Readable Value: " + humanReadableValue);
                                //Add the result to the query table
                                queryTable.getItems().add(new ARowInQueryTable(lbName.getText() +"."+ i, humanReadableValue, lbType.getText()));
                            }


                            // If the response is "noSuchInstance", break the loop
                            if (response.equals("noSuchInstance")) {
                                break;
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



    @FXML
    void SNMPWalkClicked(MouseEvent event) {

        System.out.println("perfroming SNMP Get.......");
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
                // For each VariableBinding, print OID, name, and data type
                for (VariableBinding varBinding : varBindings) {
                    String oid = varBinding.getOid().toString();
                    //Print raw response to console
                    System.out.println("Walk: " + oid + " : " + varBinding.getVariable().toString());
                    //Stroe the last teo chracter to add to name when print out
                    String lastTwoChar = oid.substring(oid.length()-2);
                    //remove the last two characters of the OID to get the node
                    Node node = mibLoader.lookupNode(oid.substring(0, oid.length()-2));
                    System.out.println("Node: " + node);

                    if (node != null) {
                        //System.out.println("Look up by OID Fucking work!");
                        String name = node.name;
                        String dataType = node.type;
                        Map<String, Object> constraint = node.constraints;

                        System.out.println("OID: " + oid + ", Name: " + name + ", DataType: " + dataType + ", Constraints: " + constraint);
                        // Convert the variable to a human-readable format
                        String humanReadableValue = format(varBinding.getVariable(), dataType, constraints);
                        System.out.println("Human Readable Value: " + humanReadableValue);
                        //Add the result to the query table
                        queryTable.getItems().add(new ARowInQueryTable(name + lastTwoChar, humanReadableValue, dataType));
                    }
                    else { //In case we can find that node base on OID, return the OID and raw inout
                        System.out.println("Can not find OID: " + oid + ": return raw value: " + varBinding.getVariable().toString());
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

    @FXML
    public void clearTableClicked(MouseEvent event) {
        // Clear the query table
        queryTable.getItems().clear();
    }

}
