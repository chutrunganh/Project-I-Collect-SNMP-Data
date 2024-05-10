package MIB_Browser_Sourcecode.GUI;

import MIB_Browser_Sourcecode.Model.MIBTreeView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;


public class MainController {

    //Attributes
    private MIBTreeView mibTreeView = new MIBTreeView();
    @FXML
    private ScrollPane A_MIBTreeScrollpane;  //Use to display list of MIBs which are loaded/opened by users + "Loaded MIBs" label
    @FXML
    private FlowPane MIBsloaded; //Use to display list of MIBs which are loaded/opened by users

    /*
     * Load some default MIBs when the program starts
     */
    @FXML
    public void initialize() {
        //Load the default MIBs
        File defaultMIB1 = new File("Project_I_code/MIB-Browser/MIB Databases/Test-MIB.json");
        showMIBsLoaded(defaultMIB1);
    }


    @FXML
        /*
         * Only open the MIB without saving it to the MIB Databases directory
         * Then show the MIB in the TreeView
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
         * Currently, I get a NullPointerException when I try to use relative paths, so I'm using absolute paths for now
         */
    void importMIBClicked(ActionEvent event) {

        //System.out.println("Import MIB Clicked");

        //Ask user to select a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        System.out.println("Opening: " + file.getName() + ".");

        try { //Save the file to the MIB Database folder also

            //Get absolute path of the current directory solution from: https://stackoverflow.com/questions/4210239/how-to-use-relative-path-instead-of-absolute-path 
            //String path = String.format("%s/%s", System.getProperty("user.dir"), this.getClass().getPackage().getName().replace(".", "/"));
            //System.out.println(path);
            //Go up two directories
            //path = path.substring(0, path.lastIndexOf("/"));
            //path = path.substring(0, path.lastIndexOf("/"));
            //System.out.println(path);
            //For example: when this MainController.java file is runed, the path will be: /home/chutrunganh/Desktop/MIB-Browser/MIB_Browser_Sourcecode/GUI
            // Then, I will need to go up two directories to access the MIB Databases directory to save the file there.

            //Create a new file in the MIB Databases folder and write the contents of the selected file to it
            Files.copy(file.toPath(), new File(  "Project_I_code/MIB-Browser/MIB Databases/" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File Saved Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Show the chosen file in loaded section
        showMIBsLoaded(file);
    }


    /*
     * Function to display the TreeView of the chosen MIB. The TreeView is proceeded by the JsonToTreeView class in the MIBTreeView class
     * to transform the JSON file to a TreeView
     * */
    public void DisplayTreeViewOfChosenMIB(File jsonFile) throws IOException {
        //Create a new TreeView and set its root item
        MIBTreeView.JsonToTreeView jsonToTreeView = mibTreeView.new JsonToTreeView();
        TreeView<String> treeView = jsonToTreeView.jsonToTreeView(jsonFile);

        // Set a custom cell factory to limit the length of the text since the first node contains the entire JSON string is
        // too long to display in the TreeView
        treeView.setCellFactory(tv -> new TreeCell<String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.length() > 30 ? item.substring(0, 30) + "..." : item);
                }
            }
        });

        // Bind the TreeView's prefWidthProperty to the ScrollPane's widthProperty
        treeView.prefWidthProperty().bind(A_MIBTreeScrollpane.widthProperty());
        //Blind the TreeView's prefHeightProperty to the ScrollPane's heightProperty
        treeView.prefHeightProperty().bind(A_MIBTreeScrollpane.heightProperty());
        //Add the TreeView to the GUI
        A_MIBTreeScrollpane.setContent(treeView);
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
                    DisplayTreeViewOfChosenMIB(file);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

        }
    }
}
