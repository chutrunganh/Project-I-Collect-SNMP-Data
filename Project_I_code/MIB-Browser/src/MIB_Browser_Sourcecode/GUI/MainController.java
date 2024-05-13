package MIB_Browser_Sourcecode.GUI;

import MIB_Browser_Sourcecode.Model.MIBNode;
import MIB_Browser_Sourcecode.Model.MIBTreeView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;


public class MainController {

    //Attributes
    private final MIBTreeView mibTreeView = new MIBTreeView();
    @FXML
    private ScrollPane A_MIBTreeScrollpane;  //Use to display list of MIBs which are loaded/opened by users + "Loaded MIBs" label
    @FXML
    private FlowPane MIBsloaded; //Use to display list of MIBs which are loaded/opened by users
    @FXML
    private TextField tfOID;

    String oidValue = null;


    /*
     * Load some default MIBs when the program starts
     */
    @FXML
    public void initialize() {
        //Load the default MIBs to the MIBsloaded FlowPane
        File defaultMIB1 = new File("Project_I_code/MIB-Browser/MIB Databases/Test-MIB.json");
        showMIBsLoaded(defaultMIB1);
    }


    @FXML
    /*
     * Only open the MIB without saving it to the MIB Databases directory
     * Then show the MIB in the MIBsloaded FlowPane
     */
    void openMIBClicked(ActionEvent event) throws IOException {
        //System.out.println("Open MIB Clicked");

        //Ask user to select a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        System.out.println("Opening: " + file.getName() + ".");

        //Show the chosen file in loaded section
        showMIBsLoaded(file);
    }


    @FXML
    /*
     * This method is called when the Load MIB button is clicked
     * It opens a file chooser dialog and allows the user to select a file, then saves the file to the MIB Databases directory also
     * Then show the MIB in the MIBsloaded FlowPane
     */
    void importMIBClicked(ActionEvent event) {
        //System.out.println("Import MIB Clicked");

        //Ask user to select a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        System.out.println("Opening: " + file.getName() + ".");

        try { //Save the file to the MIB Database directory

            //Create a new file in the MIB Databases folder and write the contents of the selected file to it
            Files.copy(file.toPath(), new File("Project_I_code/MIB-Browser/MIB Databases/" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File Saved Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Show the chosen file in loaded section
        showMIBsLoaded(file);
    }


    /*
     * Function to show the MIBs loaded/opened by the user in the MIBsloaded FlowPane
     * */
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
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

        }
    }


    /*
     * Function to display the TreeView of the chosen MIB from MIBsloaded FlowPane. The TreeView is proceeded by the JsonToTreeView class in
     * the MIBTreeView class
     * to transform the JSON file to a TreeView
     * */
    public void displayTreeFromChosenMIB(File file) throws IOException {
        //Create a TreeView from the JSON file
        TreeView<MIBNode> treeView = mibTreeView.jsonToTreeView(file);

        //Custom the appearance, event handlers of a Tree cell in the TreeView
        setCellFactory(treeView);

        // Bind the TreeView's prefWidthProperty to the ScrollPane's widthProperty
        treeView.prefWidthProperty().bind(A_MIBTreeScrollpane.widthProperty());
        // Bind the TreeView's prefHeightProperty to the ScrollPane's heightProperty
        treeView.prefHeightProperty().bind(A_MIBTreeScrollpane.heightProperty());
        // Add the TreeView to the ScrollPane
        A_MIBTreeScrollpane.setContent(treeView);
    }

    /*
     *  Menu when right-click on a tree cell
     * */
    private ContextMenu createContextMenu() {
        // Create a ContextMenu
        ContextMenu contextMenu = new ContextMenu();

        // Create MenuItems
        MenuItem getItem = new MenuItem("Get");
        MenuItem getNextItem = new MenuItem("GetNext");

        // Add MenuItems to ContextMenu
        contextMenu.getItems().addAll(getItem, getNextItem);

        // Set an onAction event handler for the MenuItems
        getItem.setOnAction(event -> {
            // Perform the "Get" action
            System.out.println("Get action performed");
        });

        getNextItem.setOnAction(event -> {
            // Perform the "GetNext" action
            System.out.println("GetNext action performed");
        });

        return contextMenu;
    }


    /*
     * Function to handle the  click action on a tree cell /node. In the context of a TreeView, you can think of a
     * TreeItem as your data, the TreeCell as the visual representation of that data
     *
     * Left-click to choose that cell (mean we will get the value of that cell)
     * Right-click to show the context menu
     */
    private void handleTreeCellClick(TreeCell<MIBNode> treeCell, MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            // Get the TreeItem associated with the clicked TreeCell
            TreeItem<MIBNode> treeItem = treeCell.getTreeItem();


            // Traverse the TreeItem to find the 'oid' key
            oidValue = MIBTreeView.findOid(treeItem);

            // If the 'oid' key was found, print its value
            if (oidValue != null) {
                String pirntOut_oidValue = oidValue.replace("\"", "");
                //Remove the " " when printing the oid value
                tfOID.setText(pirntOut_oidValue);
            }



        } else if (event.getButton() == MouseButton.SECONDARY) {
            //Show the context menu
            ContextMenu contextMenu = createContextMenu();
            treeCell.setContextMenu(contextMenu);
        }
    }


    /*
     * Function to set the cell factory look (limit cell length) and add the context menu when right-clicked on a node
     * */
    private void setCellFactory(TreeView<MIBNode> treeView) {
        // Set a custom cell factory to show the context menu
        treeView.setCellFactory(tv -> new TreeCell<MIBNode>() {
            @Override
            public void updateItem(MIBNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    String displayText = item.getKey();
                    setText(displayText);
                    setOnMouseClicked(event -> handleTreeCellClick(this, event));
                }
            }
        });
    }

}
