package Project_I_Code.Model.SNMPRequest;

import javafx.util.Pair;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * This class contains methods to convert SNMP response values (in VariableBlinding to human-readable format.
 */
public class SNMPValueConverter {

    // In order to Parse the values of the SNMP response, we need to load some necessary MIB files.
    // These files will be loaded in the MainController class and passed to this class.
    //private final MibLoader mibLoader;

    public SNMPValueConverter() {
        //this.mibLoader = mibLoader;
    }

    //Getter for the MibLoader object
//    public MibLoader getMibLoader() {
//        return mibLoader;
//    }


    /**
     * Locates a symbol by its OID in the loaded MIB files.
     * @param loader The MibLoader instance to use for locating the symbol.
     * @param oid The OID of the symbol to locate.
     * @return The located MibValueSymbol, or null if no symbol was found.
     */
//    public MibValueSymbol locateSymbolByOid(MibLoader loader, String oid) {
//        ObjectIdentifierValue iso = loader.getRootOid();
//        ObjectIdentifierValue match = iso.find(oid);
//        return (match == null) ? null : match.getSymbol();
//    }

    /**
     * Converts an SNMP value to a human-readable format.
     *
     * @param variable The SNMP value to convert.
     * @param oid      The OID of the SNMP value.
     * @return A Pair containing the converted value and the name of the symbol.
     * Since the SNMP Walk only retrieves the OID and the value, we need to find the data type and also the name
     * corresponding to the OID in the MIB file to display to the query table.
     * <p>
     * Example by Mibble API Doc: https://www.mibble.org/doc/faq-java-api.html in Q4
     */

    public MibValueSymbol locateSymbolByOid(MibLoader loader, String oid) {
        ObjectIdentifierValue iso = loader.getRootOid();
        ObjectIdentifierValue match = iso.find(oid);
        return (match == null) ? null : match.getSymbol();
    }



    /**
    * This function is used to convert the SNMP value to human-readable format based on the data type of the OID.
     * This function is called to process the value after we found the data type of the OID in the MIB file.
     * Or this function can be used by some request which we know the data type of the OID in advance like SNMP GET,...
    * */
    public String convertToHumanReadable(Variable variable, Pair<String, HashMap<Integer, String>> dataType) {
        String humanReadableString;

        // Ensure that we can retrieve from the response, the response return nothing, we return null
        // Also, in case the node do not have the data type, like most of non-leaf nodes, we return null
        if (variable == null || dataType.getKey() == null) {return "Can not resolve this OID";}

        switch (dataType.getKey().toLowerCase()) {
            case "integer":
                HashMap<Integer, String> dataTypeDetail = dataType.getValue();
                // If it is an integer, we will need to use data type detail to convert it to right status
                humanReadableString = dataTypeDetail.get(variable.toInt());
                break;
            case "octetString":
                // Found solution here: https://stackoverflow.com/questions/10326900/how-to-convert-an-octet-string-to-readable-string
                OctetString octetString = (OctetString) variable;
                humanReadableString= octetString.toString();
                break;
            default:
                humanReadableString = variable.toString();
                break;
        }

        return humanReadableString;
    }

    /**
     * This function is used to get the data type of the OID by using the snmptranslate command.
     * This function is used to get the data type of the OID when we do not have the MIB file.
     * */
    public Pair<String, Pair<String, HashMap<Integer, String>>> getDataTypeAndName(String oid) {
        String name = "";
        String dataType = "";
        HashMap<Integer, String> dataTypeDetail = new HashMap<>();

        try {
            Process process = new ProcessBuilder("snmptranslate", "-Td", oid).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                //System.out.println("Command output first line: " + line);

                if (isFirstLine) {
                    if (line.contains("::")) {
                        name = line.split("::")[1].trim();
                    }
                    isFirstLine = false;
                }

                // Or if line start with TEXTUAL-CONVENTION, we can get the data type from here, this is more priority than the result
                // from SYNTAX
                if (line.contains("TEXTUAL CONVENTION ")) {
                    //System.out.println("Command output: " + line); //Ex: Command output:   -- TEXTUAL CONVENTION DisplayString
                    dataType = line.substring(line.indexOf("TEXTUAL CONVENTION") + 18).trim(); //EX: DisplayString
                    //Start from the end of TEXTUAL CONVENTION to the end of the line, + 18 since TEXTUAL CONVENTION has 18 characters include space
                    return new Pair<>(name, new Pair<>(dataType, new HashMap<>()));
                }



                if (line.trim().contains("SYNTAX")) {
                    //System.out.println("Command output: " + line); //EX: SYNTAX	INTEGER {up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6), lowerLayerDown(7)}

                    // Check if we are dealing with INTEGER data type, then we need to extract the detail of the data type
                    if (line.contains("INTEGER")) {
                        HashMap<Integer, String> dataDetail = new HashMap<>(); //Store the detail of the data type like this {1 : up(1), 2 : down(2),...}

                        // Split the line at the first, second space
                        String[] parts = line.split("\\s+", 3); //Now we have 3 parts: SYNTAX, INTEGER, {up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6), lowerLayerDown(7)}

                        //System.out.println("DataType: " + parts[1]); //EX: INTEGER

                        try {  //Not all Syntax has the part { }, some still have the detail about the data type, but it is ( ) which is not useful
                            //Like this: SYNTAX  INTEGER (0..2147483647)
                            if (parts[2].contains("{")) {
                                //System.out.println("Detail about data type: " + parts[2]); //EX {up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6), lowerLayerDown(7)}
                                String tmp = parts[2].substring(parts[2].indexOf("{") + 1, parts[2].indexOf("}")); //Remove the { }
                                String[] statusList = tmp.split(","); //Split the detail by comma
                                for (String status : statusList) { //EX of a status: up(1)
                                    dataDetail.put(Integer.parseInt(status.substring(status.length() - 2, status.length() - 1)), status.trim()); //Ex: 1 : up(1)
                                }
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            //System.out.println("No detail about data type");
                        }

                        //Print the data detail to console
                        for (Integer key : dataDetail.keySet()) {
                            //System.out.println("Key: " + key + " Value: " + dataDetail.get(key));
                        }

                        return new Pair<>(name, new Pair<>(parts[1], dataDetail));
                    }

                    else {
                        return new Pair<>(name, new Pair<>(line.substring(line.indexOf("SYNTAX") + 7).trim(), new HashMap<>()));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error executing snmptranslate command or reading its output");
            e.printStackTrace();
        }

        return new Pair<>(name, new Pair<>(dataType, dataTypeDetail));
    }

    /**
     *  SOME OTHER DATA TYPES THAT CAN BE USED IN SNMP
     *  timeTicks : usually used to represent the time in hundredths of a second since the device was started but in snmp4j,
     *  it is represented in time format already, like this: 3:53:39.34
     *
     * */

    //https://igss.schneider-electric.com/Files/Doc-Help/Webhelp/V14/Interface/SNMP/About_SNMP_Textual_Convention.htm
//Many node has type OCTET STRING, but their Textual Convention is different, so we need to check the Textual Convention to know how to convert it

    /*
    * The hrSystemDate object in the HOST-RESOURCES-MIB is defined with the DateAndTime dataType. DateAndTime is a textual convention
    * that represents a date and time as an OCTET STRING of 8 or 11 bytes. The
    * bytes are arranged in a specific order to represent the year, month, day,
    * hour, minute, second, deci-second, and optionally the timezone.
    *  However, when you retrieve the value of hrSystemDate using an SNMP
    * library like SNMP4J, it's returned as an OCTET STRING because that's
    * the underlying data type. To convert this OCTET STRING to a human-readable
    *  date and time, you need to interpret the bytes in the OCTET STRING
    * according to the DateAndTime format.
    *
    * If you're using Mibble to get the data type of hrSystemDate and it's
    *  returning OCTET STRING, it's likely because DateAndTime is a textual
    * convention, not a data type. Textual conventions are used in SNMP MIBs
    *  to provide a semantic definition of a data type, but they don't change
    *  the underlying data type. The underlying data type of DateAndTime
    * is OCTET STRING.
    * */


}