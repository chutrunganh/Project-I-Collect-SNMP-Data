package MIB_Browser_Sourcecode.Model.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import MIB_Browser_Sourcecode.Model.MIBTreeView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TreeView;
import org.junit.Test;


public class MIBTreeViewTest {

//    @Test
//    private String jsonString = "{ \"host\": { \"name\": \"CTA_ThinkPad\", \"oid\": \"1.3.6.1.2.1.25\", \"class\": \"objectidentity\" } }";
//    public void parseJsonStringToNode() throws IOException {
//        JsonNode jsonNode = MIBTreeView.parseJsonStringToNode(jsonString);
//        assertEquals(jsonNode.get("host").get("name").asText(), "CTA_ThinkPad");
//    }


    String PATH_TO_TEST_JSON_FILE = "MIB Databases/Test-MIB.json";

//    @Test
//    public void testJsonToTreeView() {
//        // Initialize JavaFX toolkit
//        new JFXPanel();
//
//        MIBTreeView.JsonToTreeView jsonToTreeView = new MIBTreeView().new JsonToTreeView();
//        File jsonFile = new File("Project_I_code/MIB-Browser/MIB Databases/Test-MIB.json");
//
//        try {
//            TreeView<String> treeView = jsonToTreeView.jsonToTreeView(jsonFile);
//            assertNotNull(treeView);
//            assertNotNull(treeView.getRoot());
//            // Add more assertions here based on what you expect the output to be
//        } catch (IOException e) {
//            fail("Test failed due to IOException");
//        }
//    }


}