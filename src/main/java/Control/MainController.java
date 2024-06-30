package Control;

import Model.MIBTreeStructure.BuildTreeFromJson;
import Model.MIBTreeStructure.MibLoader;
import Model.MIBTreeStructure.Node;
import Model.SNMRequest.SNMPGet;
import Model.SNMRequest.SNMPGetNext;
import Model.SNMRequest.SNMPWalk;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static Model.SNMRequest.SnmpResponseFormatter.format;


/** @author Chu Trung Anh 20225564
 * MainController class is the controller class for the main.fxml file. It contains all the methods to handle the user's actions
 * in the SNMP Browser application.
 */
public class MainController {

    // Left side of the application
    @FXML private AnchorPane MIBTreeDisplay;  //This Pane is used to display the TreeView structure of the MIBs
    @FXML private FlowPane MIBsLoaded; //This Pane is used to display the MIBs loaded by the user (when the user clicks on the Open/Load MIB
    // button or MIBs in the vendor tab)
    @FXML private Label ShowingMIBTreeName; //Label to show the name of the MIB  that are being displayed in the MIBTreeDisplay Pane
    @FXML private TextField tfOID; //Text field to display the OID of the selected node

    //Vendor Tab
    @FXML private ComboBox<String> chooseVendor;  //A choice box to allow the user to select a vendor
    List<String> vendorMibs = new ArrayList<>(); //A variable to store some common MIBs of the selected vendor


    // Bottom right side of the application, the place where the user can see the extracted  information of the selected node
    @FXML private Label lbAccess;
    @FXML private Label lbName;
    @FXML private Label lbStatus;
    @FXML private Label lbType;
    @FXML private TextArea taDescription;
    @FXML private PasswordField tfCommunityString;
    @FXML private TextField tfTargetIP;

    // Query Table : show the result of the SNMP Get, SNMP Get Next, SNMP Walk operations
    @FXML private TableView<ARowInQueryTable> queryTable;
    @FXML private TableColumn<ARowInQueryTable, String> nameColumn;
    @FXML private TableColumn<ARowInQueryTable, String> valueColumn;
    @FXML private TableColumn<ARowInQueryTable, String> typeColumn;
    @FXML private ImageView searchImage;
    @FXML private ImageView saveImage;

    //TreeView to display the MIB tree
    TreeView<Node> treeView;



    /** Method to get base directory dynamically
    * The MIB Databases folder is located in the same directory as the jar file.
     * We need to dynamic get the base directory of the application to locate the MIB Databases folder since the
     * path when run for the jar file in users' computer may be different from the path when run in the IDE.
     * User just need to ENSURE the jar file and the MIB Databases are both located in the same level in directory
     * named "SNMP_Browser"
     */
    private String getBaseDirectory() {
        String basePath = System.getProperty("user.dir");
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        File jarFile = new File(path);
        if (path.endsWith(".jar")) { //When we run the app by using the jar file
            basePath = jarFile.getParent();
            //System.out.println("Base Path of jar: " + basePath);
        }
        else {  //When we run the app by using the source code in the IDE
            basePath = basePath + "/out/artifacts/SNMP_Browser";
            //System.out.println("Base Path of IDE: " + basePath);
        }
        return basePath;
    }
    String BASE_DIR = getBaseDirectory() + "/MIB Databases";
    //@FXML TextArea testing;





    //Some default values for attributes used across the application
    String oidValue = null;
    String ip = "127.0.0.1";
    String community = "password";
    String nodeType = null; //Node type of the selected node, help to distinguish between scalar
    // and non-scalar nodes. This is crucial to determine the OID to be used in SNMP Get
    Map<String, Object> constraints = new HashMap<>(); //Constraints of the selected node, String is the
    // constraint name, also we do not in advance what kind of constraints can have, so use Object type. This info will not be displayed to the UI


    /**
     * Load some default MIBs when the program starts
     */
    @FXML
    public void initialize() throws IOException {

        //testing.setText(BASE_DIR);


        //Set the image for the search and save button
        searchImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/search.png"))));
        saveImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/save.png"))));


        //Build the tree view from the default MIB files
        treeView = new TreeView<>();
        returnToDefaultClicked(null);



        //Initialize the query table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));


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
                        //System.out.println("Node name to search: " + nodeName);
                        highlightNodeInTreeView(treeView, nodeName);
                    } else { //If the node name does not contain a postfix, search for the node name as it is
                        highlightNodeInTreeView(treeView, selectedRow.getName());
                    }
                }
            }
        });


        //Initialize the choice box with some vendors
        ObservableList<String> vendors = FXCollections.observableArrayList("Cisco", "Asus", "Huawei");
        chooseVendor.setItems(vendors);  //Extend the item to the choice box height and width

        //Add event listener to the choice box. When click on a vendor, show a list of MIBs of that vendor in the MIBsLoaded Pane
        chooseVendor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //System.out.println("Selected vendor: " + newValue);
                vendorMibs.clear();


                if (newValue.equals("Cisco")) {
                    //testing.setText(BASE_DIR + "/CISCO-PRODUCTS-MIB.json");
                    vendorMibs.add(BASE_DIR + "/CISCO-CVP-MIB.json");
                    vendorMibs.add(BASE_DIR + "/CISCO-ENVMON-MIB.json");
                    vendorMibs.add(BASE_DIR + "/CISCO-CDP-MIB.json");
                } else if (newValue.equals("Asus")) {
                    vendorMibs.add(BASE_DIR + "/PEGASUS-MIB.json");
                    vendorMibs.add(BASE_DIR + "/RT-AC68U-MIB.json");
                    vendorMibs.add(BASE_DIR + "/SWITCHING-MIB.json");
                } else if (newValue.equals("Huawei")) {
                    vendorMibs.add(BASE_DIR + "/A3COM-HUAWEI-IPV6-ADDRESS-MIB.json");
                    vendorMibs.add(BASE_DIR + "/HUAWEI-LINE-COMMON-MIB.json");
                    vendorMibs.add(BASE_DIR + "/HUAWEI-HTTP-MIB.json");
                }
                //System.out.println("Vendor MIBs: " + vendorMibs);
                for (String mib : vendorMibs) {
                    showMIBFileInLoadPane(new File(mib));
                }
            }
        });


    }



    /**-------------------------------------File button in the menu bar-------------------------------------**/
    @FXML
    /**
     * Only open the MIB without saving it to the MIB Databases directory
     * Then show the MIB in the MIBsLoaded FlowPane
     */
    void openMIBClicked(ActionEvent event) throws IOException {
        //System.out.println("Open MIB Clicked");

        //Ask user to select a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        //System.out.println("Opening: " + file.getName() + ".");

        //Show the chosen file in loaded section
        showMIBFileInLoadPane(file);
    }

    @FXML
    /**
     * This method is called when the Load MIB button is clicked
     * It opens a file chooser dialog and allows the user to select a file, then saves the file to the MIB Databases directory also
     * Then show the MIB in the MIBsLoaded FlowPane
     */
    void importMIBClicked(ActionEvent event) {
        //System.out.println("Import MIB Clicked");

        //Ask user to select a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        //System.out.println("Opening: " + file.getName() + ".");

        try { //Save the file to the MIB Database directory

            //Create a new file in the MIB Databases folder and write the contents of the selected file to it
            // Overwrite the file if it already exists
            Files.copy(file.toPath(), new File(BASE_DIR + "/" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            //System.out.println("File Saved Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }

        showMIBFileInLoadPane(file);
    }

    /**
     * Method to handle the Unload All MIBs button click event. This method
     * clears the MIBsLoaded FlowPane and the MIBTreeDisplay AnchorPane.
     */
    @FXML
    void unloadAllMIBsClicked(ActionEvent event) {
        MIBsLoaded.getChildren().clear();
        MIBTreeDisplay.getChildren().clear();
        ShowingMIBTreeName.setText("Showing MIB Tree: Default MIBs");
    }

    /**---------------------------------------Edit button in the menu bar--------------------------------------**/
    private Scene mainScene;

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }

    @FXML
    public void darkModeClicked() {
        mainScene.getStylesheets().clear();
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    @FXML
    public void lightModeClicked() {
        mainScene.getStylesheets().clear();
    }




    /**--------------------------------------Help button in the menu bar--------------------------------------**/
    @FXML
    void aboutUsClicked(ActionEvent event) {
        // Create an Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Us");
        alert.setHeaderText("SNMP Browser");

        // Create a TextFlow to include a hyperlink
        Label label = new Label("Acknowledgments:\n" +
                "This application is developed by Chu Trung Anh (20225564) \nfor the Project I,Semester " +
                "20232, Hanoi University of Science \nand Technology." +
                "\nVisit our website at: ");
        Hyperlink link = new Hyperlink("""
                https://github.com/chutrunganh/Project-I-Collect-SNMP-Data.git""");


        TextFlow textFlow = new TextFlow(label, link);
        alert.getDialogPane().setContent(textFlow);

        // Set preferred width and height
        alert.getDialogPane().setPrefSize(450, 250);

        // Show the Alert
        alert.showAndWait();
    }


    /**---------------------------------------------------MIBsLoaded and MIBTreeDisplay Pane------------------------------------------------------**/


    /**
    * Function to show the MIBs loaded/opened or when choose a vendor in vendor tab by the user in the MIBsLoaded FlowPane
     * @param file: the file that the user has chosen to open or the file that is in the vendor MIBs list
    */
    public void showMIBFileInLoadPane(File file) {
        if (file != null) {
            Label fileLabel = new Label(file.getName());
            //Set fileLabel width to the width of the FlowPane so that new labels are added horizontally
            fileLabel.prefWidthProperty().bind(MIBsLoaded.widthProperty());

            MIBsLoaded.getChildren().add(fileLabel);

            // Event handler for the file label. If user clicks on the label, display the TreeView of the chosen MIB
            fileLabel.setOnMouseClicked(e -> {
                try {
                    MIBTreeDisplay.getChildren().clear();
                    TreeView<Node> treeView = displayTreeFromFiles(List.of(BASE_DIR + "/" + file.getName()));
                    //System.out.println("path: " + BASE_DIR + "/" + file.getName());
                    MIBTreeDisplay.getChildren().clear();
                    //Expand the treeview to fit the Anchor Pane width and height
                    treeView.prefWidthProperty().bind(MIBTreeDisplay.widthProperty());
                    treeView.prefHeightProperty().bind(MIBTreeDisplay.heightProperty());
                    MIBTreeDisplay.getChildren().add(treeView);

                    //System.out.println("Displaying MIB: " + file.getName());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

        }
    }


    /**
     * Method to display the MIB tree from the selected MIB files in the MIBTreeDisplay AnchorPane.
     * When user click on a MIB file label in the MIBsLoaded FlowPane, this method is called to display the MIB tree in the MIBTreeDisplay AnchorPane.
     * @param mibFilePaths: a list of paths to the JSON files
     * @return a TreeView object to display the MIB tree in the UI
     */


    public TreeView<Node> displayTreeFromFiles(List<String> mibFilePaths) throws IOException {

        //Extract the MIB name from the file path
        String mibName = mibFilePaths.get(0).substring(mibFilePaths.get(0).lastIndexOf("/") + 1, mibFilePaths.get(0).lastIndexOf("."));
        ShowingMIBTreeName.setText("Showing MIB Tree: " + mibName);
        BuildTreeFromJson treeBuilder = new BuildTreeFromJson();
        try {
            treeBuilder.buildTreeFromMultipleMIBs(mibFilePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TreeItem<Node> rootItem = treeBuilder.convertNodeToTreeItem(treeBuilder.getRoot());
        treeView = new TreeView<>(rootItem); //Create a TreeView object to display to UI from the rootItem

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

                    resetCurrentOid(); //Used by SNMP Get Next to reset the current OID when clicking on a new node
                }
            }
        });

        // Double-click on a tree node to perform SNMP Get on that node
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
                            //System.out.println("Error calling SNMPGetClicked.");
                            e.printStackTrace();
                        }

                    } else {
                        //System.out.println("Node type is null."); // Debug print
                    }
                } else {
                    //System.out.println("No tree item selected."); // Debug print
                }
            }
        });

        return treeView;
    }


    /**
     * When click on the Return to Default MIBs button in the Standard MIBs tab, clear all and display the default MIBs as the first time the application starts
     */
    @FXML
    void returnToDefaultClicked(MouseEvent event) throws IOException {
        // Build the tree view from the default MIB files
        List<String> mibFilePaths = Arrays.asList(
                BASE_DIR + "/SNMPv2-SMI.json",
                BASE_DIR + "/RFC1213-MIB.json",
                BASE_DIR + "/HOST-RESOURCES-MIB.json",
                BASE_DIR + "/SNMPv2-MIB.json",
                BASE_DIR + "/IF-MIB.json"
        );


        treeView = displayTreeFromFiles(mibFilePaths);

        //This one is specifically for the default MIBs, it's using multiple MIBs to build the tree, reset the label
        ShowingMIBTreeName.setText("Showing MIB Tree: Default MIBs");

        MIBTreeDisplay.getChildren().clear();
        //Expand the treeview to fit the Anchor Pane width and height
        treeView.prefWidthProperty().bind(MIBTreeDisplay.widthProperty());
        treeView.prefHeightProperty().bind(MIBTreeDisplay.heightProperty());
        MIBTreeDisplay.getChildren().add(treeView);
    }


    /**---------------------------------------------------SNMP Operations------------------------------------------------------**/

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
            //System.out.println("Error: OID field is empty");
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

                    //System.out.println("Still Working on this part 3");
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
                            if (!response.equals("noSuchInstance")  && !response.equals("noSuchObject")) {
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
                            if (response.equals("noSuchInstance") || response.equals("noSuchObject")) {
                                if (i == 1) { // If the first response is "noSuchInstance", mean that it is not exist in the device
                                    //System.out.println("Error: Failed to get response for OID: " + oidToTry);
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
     * Method to handle the SNMP Get Next button click event. This method performs an SNMP Get Next operation on the selected OID.
     * I use the currentOid variable to track the current OID, so that from a selected node, when user click on the Get Next multi times, it
     * will perform Get Next on the next OID from the original node, then the next OID from the previous Get Next result, and so on (incremental)
     * Only reset the currentOid when user click on a new node in the TreeView or click on the Clear Table button
     * @param event
     */
    private String currentOid; // To track the current OID
    // Method to perform SNMP GET-NEXT request
    @FXML
    void SNMPGetNextClicked(MouseEvent event) throws IOException {
        //System.out.println("Performing SNMP Get Next.......");

        // Get the OID from the text field if it's the first click
        if (currentOid == null) {
            currentOid = tfOID.getText();
        }

        if (currentOid.isEmpty()) {
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

        if (targetAddress instanceof UdpAddress udpTargetAddress) {
            // Get the community string from the password field
            if (!tfCommunityString.getText().isEmpty()) {
                community = tfCommunityString.getText();
            }

            SNMPGetNext snmpGetNext = new SNMPGetNext(udpTargetAddress, community, currentOid);
            VariableBinding vb = snmpGetNext.getVariableBinding(); // Get the response from the SNMP request

            if (vb != null) {
                //System.out.println("OID that GetNext performed: " + vb.getOid());
                //System.out.println("Return value: " + vb.getVariable());



                // Update currentOid with the OID from the response
                currentOid = vb.getOid().toString();

                // Process the retrieved OID and its value
                String oidForGetNext = vb.getOid().toString();
                int lastDotPosition = oidForGetNext.lastIndexOf('.');
                String oidWithoutLastPart = oidForGetNext.substring(0, lastDotPosition);

                // Load the MIB files and look up the node
                MibLoader mibLoader = new MibLoader();
                mibLoader.loadMibsFromFolder(BASE_DIR + "/");
                Node node = mibLoader.lookupNode(oidWithoutLastPart);

                if (node != null) {
                    // Format the return value to human-readable format
                    String dataType = node.type;
                    Map<String, Object> constraints = node.constraints;
                    String humanReadableValue = format(vb.getVariable(), dataType, constraints);
                    // Add the result to the query table
                    queryTable.getItems().add(new ARowInQueryTable(node.name + oidForGetNext.substring(lastDotPosition), humanReadableValue, dataType));
                } else {
                    // If we can't find the node in the MIB files, return the OID and raw input
                    queryTable.getItems().add(new ARowInQueryTable(oidForGetNext, vb.getVariable().toString() + " (raw value)", "None defined"));
                }
            } else {
                //System.out.println("Error: No response received.");
            }
        } else {
            //System.out.println("Error: Invalid target address.");
        }
    }

    // Method to reset the current OID when a new node is selected
    public void resetCurrentOid() {
        currentOid = null;
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
                mibLoader.loadMibsFromFolder(BASE_DIR + "/");

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
            //System.out.println("Error: Invalid target address");
        }
    }


    /**---------------------------------------------------Query Table------------------------------------------------------**/

    /**
     * Method to handle the Clear Table button click event. This method clears the query table.
     * used for the red "X" button in the UI, beside the query table
     * @param event
     */
    @FXML
    public void clearTableClicked(MouseEvent event) {
        // Clear the query table
        queryTable.getItems().clear();

        // Also reset the current OID of SNMP Get Next
        resetCurrentOid();
    }


    /**
    * Method to highlight a node in the TreeView, this method is called when user double-click on a row in the query table, it will
     * highlight the corresponding node in the TreeView (we map to the corresponding node by the name of the node)
     */
    // Method to highlight a node in the TreeView
    private void highlightNodeInTreeView(TreeView<Node> treeView, String nodeName) {
        TreeItem<Node> root = treeView.getRoot();
        TreeItem<Node> targetNode = findNode(root, nodeName);
        //System.out.println("Target node: " + targetNode);

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

    /**
     * Method to handle the Search button click event. This method prompts the user to enter a name to search for a
     * row with matching name in the query table. If a row with the matching name is found, the row is highlighted and moved to.
     */
    @FXML
    void searchButtonClicked(MouseEvent event) {
        // Create a TextInputDialog to prompt the user for the name to search
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search");
        dialog.setHeaderText("Enter the name to search:");
        Optional<String> result = dialog.showAndWait();

        // If the user entered a name, search for it in the queryTable
        if (result.isPresent()) {
            String nameToSearch = result.get();

            // Iterate over the items in the queryTable
            for (ARowInQueryTable row : queryTable.getItems()) {
                // If the name of the row matches the name to search, select the row
                if (row.getName().toLowerCase().contains(nameToSearch.toLowerCase())) {
                    queryTable.getSelectionModel().select(row);
                    queryTable.scrollTo(row);
                    break;
                }
            }
        }
    }

    /**
     * Method to handle the Save button click event. This method prompts the user to select a file to save the query table to.
     * The query table is saved as a CSV file with the columns "Name", "Type", and "Value".
     */
    @FXML
    void saveButtonClicked(MouseEvent event) {
        // Create a FileChooser to prompt the user for the file to save
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Query Table");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        // If the user selected a file, save the queryTable to it
        if (file != null) {
            try {
                // Create a FileWriter to write the queryTable to the file
                FileWriter writer = new FileWriter(file);

                // Write the header to the CSV file
                writer.append("Name,Type,Value\n");

                // Iterate over the items in the queryTable
                for (ARowInQueryTable row : queryTable.getItems()) {
                    // Write each row to the CSV file
                    writer.append(row.getName());
                    writer.append(",");
                    writer.append(row.getType());
                    writer.append(",");
                    writer.append(row.getValue());
                    writer.append("\n");
                }

                // Close the FileWriter
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}