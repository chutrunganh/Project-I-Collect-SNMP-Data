package GUI;

import Model.MIBTreeStructure.JsonTreeConverter;
import Model.MIBTreeStructure.JsonTreeItem;
import Model.SNMRequest.SNMPGet;
import Model.SNMRequest.SNMPValueConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;


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
    @FXML private Button btnGo;

    SNMPValueConverter converter = new SNMPValueConverter();


    String oidValue = null;
    String ip = "127.0.0.1"; //Localhost by default
    String community = "public"; //Community string by default


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
            // Create the new tree from TestMain and replace the old tree

            ObjectMapper objectMapper = new ObjectMapper();
            AtomicReference<JsonNode> jsonNode = new AtomicReference<>(objectMapper.readTree(jsonFile));

            TreeItem<String> rootItem = JsonTreeConverter.convertJsonToTree(jsonNode.get(), "root");
            TreeView<String> treeView = new TreeView<>(rootItem);

            treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isLeaf()){
                    JsonTreeItem item = (JsonTreeItem) newValue;
                    jsonNode.set(item.getJsonNode());

                    jsonNode.get().fieldNames().forEachRemaining(fieldName -> {
                        JsonNode childNode = jsonNode.get().get(fieldName);
                        if (fieldName.equals("name")) {
                            lbName.setText(childNode.asText());
                        } else if (fieldName.equals("maxaccess")) {
                            lbAccess.setText(childNode.asText());
                        } else if (fieldName.equals("description")) {
                            taDescription.setText(childNode.asText());
                        } else if (fieldName.equals("status")) {
                            lbStatus.setText(childNode.asText());
                        } else if (fieldName.equals("oid")) {
                            tfOID.setText(childNode.asText());
                        } else if (fieldName.equals("syntax")) {
                            if (childNode.has("type")) {
                                String type = childNode.get("type").asText();
                                lbType.setText(type);
                            }
                            if (childNode.has("constraints")) {
                                JsonNode constraintsNode = childNode.get("constraints");
                                constraintsNode.fieldNames().forEachRemaining(constraintType -> {
                                    JsonNode constraintValue = constraintsNode.get(constraintType);
                                    // Handle each type of constraint here
                                    // For example, if the constraint is an enumeration:
                                    if (constraintType.equals("enumeration")) {
                                        constraintValue.fieldNames().forEachRemaining(enumFieldName -> {
                                            // Handle each field in the enumeration
                                            // For example, print the field name and value
                                            System.out.println(enumFieldName + ": " + constraintValue.get(enumFieldName).asText());
                                        });
                                    }
                                    // Add additional checks for other types of constraints as needed
                                });
                            }
                        }
                    });
                }
            });

            // Bind the TreeView's prefWidthProperty to the ScrollPane's widthProperty
            treeView.prefWidthProperty().bind(MIBTreeDisplay.widthProperty());
            // Bind the TreeView's prefHeightProperty to the ScrollPane's heightProperty
            treeView.prefHeightProperty().bind(MIBTreeDisplay.heightProperty());
            // Add the TreeView to the ScrollPane
            MIBTreeDisplay.getChildren().add(treeView);
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
//            //Load the information of the selected node to the bottom right Pane in GUI
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

        oidValue = tfOID.getText(); // Get the OID from the text field
        oidValue = oidValue + ".0"; // Replace the dots with spaces

        // Get the target IP address from the text field, change it to UdpAddress format
        //In case use let this field empty, use the default IP address
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

            // Perform SNMP GET
            try {
                SNMPGet snmpGet = new SNMPGet((UdpAddress) targetAddress, community, oidValue);
                VariableBinding vb = snmpGet.getVariableBinding(); //Get the response from the SNMP request

                // Print raw response to console
                System.out.println("Get: " + oidValue + " : " + vb.getVariable().toString());

//                Pair<String, String> dataTypeAndDataTypeDetail = new Pair<>(lbType.getText(), null);
//                String humanReadableValue = converter.convertToHumanReadable(vb.getVariable(), dataTypeAndDataTypeDetail);
//                System.out.println("Human Readable Value: " + humanReadableValue);

            // Add the new value to the query table
            //ARowInQueryTable row = new ARowInQueryTable(lbName, humanReadableValue, dataTypeAndDataTypeDetail.getKey()); //EX: "INTEGER"
            //queryTable.getItems().add(row);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            System.out.println("Error: Invalid target address");
        }
    }

    @FXML
    void SNMPWalkClicked(MouseEvent event) {
        // Get the target IP address from the text field, change it to UdpAddress format
    }

}
