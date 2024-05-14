package MIB_Browser_Sourcecode.Model.Test;

import MIB_Browser_Sourcecode.Model.SNMRequest.SNMPGet;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class SNMPGetTest {

    @Test
    public void testSNMPGet() {
        ArrayList<String> testAgent = new ArrayList<>();
        testAgent.add("127.0.0.1"); // IP address
        testAgent.add("public"); // community string
        testAgent.add("1.3.6.1.2.1.1.6"); // OID for system name

        //Uncomment the below lines to redirect the standard output to a ByteArrayOutputStream instead of
        //console, so that we can compare the printed output with the expected output using assertEquals.
        // Redirect the standard output instead of printing to the console

        //ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        //System.setOut(new PrintStream(outContent));

        // Run the method that prints the output
        try {
            new SNMPGet(testAgent.get(0), testAgent.get(1), testAgent.get(2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get the printed output
        //String printedOutput = outContent.toString().trim();

        // Compare the printed output with the expected output
        //String expectedOutput = "System Description: ThinkPad-CTA";
        //assertEquals(expectedOutput, printedOutput);
    }
}