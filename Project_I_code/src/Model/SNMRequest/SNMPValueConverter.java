package Model.SNMRequest;


import javafx.util.Pair;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;

/**
 * This class contains:
 * - method to get the name and data type of given OID by using the `snmptranslate command`.
 * So make sure you have 'sudo apt install snmp snmp-mibs-downloader`. Then `sudo download-mibs` to download the MIB files for snmptranslate command to work.
 * - methods to convert SNMP response values (in VariableBlinding to human-readable format.
 */
public class SNMPValueConverter {

    public SNMPValueConverter() {
    }

    /**
     * This function is used to get the name and the data type of the given OID by using the `snmptranslate command`.
     * We tried to use the Mibble API to get the data type of the given OID (getType(), getTag(), getReferenceTo(),...), but it just returns the primitive type instead of the Textual Convention.
     * for example, it just returns OCTET STRING instead of DisplayString or DataAndTime,... So temporarily, we will use the `snmptranslate command` to get the data type.
     * Some SNMP Request like GET, GETNEXT do not need to return the name since we already know the name of selected node, it will just need to retrieve the data type.
     * But SNMP Walk will need to return the name of the node also to display in the query table.
     *
     * @param oidToFind The OID we want to retrieve the name and datatype.
     * @return A Pair containing the name of the OID and a Pair containing the data type and the detail of the data type.
     * Here we just process the detail of the data type for INTEGER data type. The status corresponding to the integer value in detail data type is stored in a HashMap.
     * Fox example, if the OID represents this node:
     * ifAdminStatus OBJECT-TYPE
     * SYNTAX  INTEGER {
     *      up(1),       -- ready to pass packets
     *      down(2),
     *      testing(3)   -- in some test mode
     *      }
     * we will return c
     */
//    public Pair<String, Pair<String, HashMap<Integer, String>>> getDataTypeAndName(String oidToFind) {
//        String name = "";
//        String dataType = "";
//        HashMap<Integer, String> dataTypeDetail = new HashMap<>();  //Store the detail of the data type like this {1 : up(1), 2 : down(2),...}
//
//        try {
//            // Execute the command snmptranslate -Td OID to get the data type of the OID
//            Process process = new ProcessBuilder("snmptranslate", "-Td", "-OS", oidToFind).start();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            String line; //Store the output of the command
//
//            boolean isFirstLine = true;
//            while ((line = reader.readLine()) != null) {
//                line = line.trim();
//
//                // For debugging
//                // System.out.println("Command output: " + line);
//                // System.out.println("Location: SNMPValueConverter class, row ~ 64");
//
//                //Read the first line of the output to extract the name of the OID (Hopefully the first line contains the name of the OID :)) )
//                if (isFirstLine) {
//                    if (line.contains("::")) {
//                        name = line.split("::")[1].trim();
//                    }
//                    isFirstLine = false;
//                }
//
//                // Now we try to extract the data type of the OID, I notice that the data type is usually after the TEXTUAL-CONVENTION or SYNTAX keyword
//
//                if (line.contains("TEXTUAL CONVENTION ")) {
//                    // System.out.println("Textual convention data type: " + line); //Ex: Command output:   -- TEXTUAL CONVENTION DisplayString
//                    dataType = line.substring(line.indexOf("TEXTUAL CONVENTION") + 18).trim(); //EX: DisplayString
//                    // Start from the end of TEXTUAL CONVENTION to the end of the line, + 18 since TEXTUAL CONVENTION has 18 characters include space
//                    return new Pair<>(name, new Pair<>(dataType, new HashMap<>())); // Empty name and empty data type detail
//                }
//
//
//                if (line.trim().contains("SYNTAX")) {
//
//                    //System.out.println("Command output: " + line); //EX: SYNTAX	INTEGER {up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6), lowerLayerDown(7)}
//
//                    // Check if we are dealing with INTEGER data type, then we need to extract the detail of the data type
//                    if (line.contains("INTEGER")) { //EX: SYNTAX	INTEGER {up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6), lowerLayerDown(7)}
//
//                        // Split the line at the first, second white spaces
//                        String[] lineComponents = line.split("\\s+", 3); //Now we have 3 lineComponents: SYNTAX, INTEGER, {up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6), lowerLayerDown(7)}
//
//                        //System.out.println("DataType: " + lineComponents[1]); //EX: INTEGER
//
//                        try {
//                            // Not all Syntax has the detail about the data type in { }, some may not have this, or some just
//                            // have the range in ( ), like this: SYNTAX  INTEGER (0..2147483647)but it is not really useful.
//                            if (lineComponents[2].contains("{")) {
//                                //System.out.println("Detail about data type: " + lineComponents[2]); //EX {up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6), lowerLayerDown(7)}
//                                String tmp = lineComponents[2].substring(lineComponents[2].indexOf("{") + 1, lineComponents[2].indexOf("}")); //Remove the { }
//                                String[] statusList = tmp.split(","); //Split the detail by comma
//                                // Process the detail of the data type to a dictionary
//                                for (String status : statusList) { //EX of a status: up(1)
//                                    dataTypeDetail.put(Integer.parseInt(status.substring(status.length() - 2, status.length() - 1)), status.trim()); //Ex: 1 : up(1)
//                                }
//                            }
//                        } catch (ArrayIndexOutOfBoundsException e) {
//                            //System.out.println("No detail about data type");
//                        }
//
//                        //Print the data type  detail dictionary to console
////                        for (Integer key : dataTypeDetail.keySet()) {
////                            System.out.println("Key: " + key + " Value: " + dataTypeDetail.get(key));
////                        }
//
//                        return new Pair<>(name, new Pair<>(lineComponents[1], dataTypeDetail));
//                    } else { // If not INTEGER data type, we just return the data type and empty data type detail
//                        return new Pair<>(name, new Pair<>(line.substring(line.indexOf("SYNTAX") + 7).trim(), new HashMap<>()));
//                    }
//                }
//            }
//        } catch (IOException e) {
//            System.out.println("Error executing snmptranslate command or reading its output");
//            e.printStackTrace();
//        }
//
//        return new Pair<>(name, new Pair<>(dataType, dataTypeDetail)); // Return empty name and empty data type detail if we can not find the data type by the command
//    }


    /**
     * This function is used to convert the SNMP response value to human-readable format based on the provided data type.
     * Some data types like Integer will have the detail data type part like this: {1 : up(1), 2 : down(2), 3 : testing(3)} to process the retrieved value.
     * @param variable The SNMP response value in Variable format.
     * @param dataType A Pair containing the data type and the detail of the data type.
     * @return The human-readable string of the SNMP response value.
     */
    public String convertToHumanReadable(Variable variable, Pair<String, HashMap<Integer, String>> dataType) {
        String humanReadableString= "";

        // Ensure that we can retrieve from the response, the response return nothing, we return null
        // Also, in case the node do not have the data type, like most of non-leaf nodes, we return null
        if (variable == null || dataType.getKey() == null) {
            return "Can not resolve this OID";
        }

        String dataTypeKey = dataType.getKey().toLowerCase(); //The data type part in lower case

        if (dataTypeKey.contains("integer")) { //EX: interface > ifTable > ifEntry > ifOpenStatus

            //If it is INTEGER, we will need to use data type detail part (if have) to convert it to right status
            if (dataType.getValue().size() > 0) {
                humanReadableString = dataType.getValue().get(variable.toInt()); //Use the detail data type  dictionary to convert the value
            } else {
                humanReadableString = variable.toString();
            }
        }


        else if (dataTypeKey.contains("dateandtime")) { //EX: host > hrSystem > hrSystemDate
            humanReadableString = dateAndTimeDataType(variable.toString());
        }


        else {
            humanReadableString = variable.toString(); //If can not resolve the value, return the raw value
        }

        return humanReadableString;
    }


    /**
     * Some function to process some data types
     * 1. DateAndTime: The DateAndTime data type is a textual convention that represents a date and time as an OCTET STRING of 8 or 11 bytes.
     */


    // DataAndTime
    //DateAndTime
    /*
     * Raw response looks like: 07:e8:05:10:08:34:12:00:2b:07:00
     * Seems like DateAndTime is a child of OCTET STRING, as: http://www.ireasoning.com/javadocs/com/ireasoning/protocol/snmp/SnmpDateAndTime.html
     * and https://docs.lextudio.com/blog/snmp-pro-dateandtime-syntax-support-e99abc51bc2f
     * Some ways to convert it to human-readable format: https://stackoverflow.com/questions/9700037/snmp-eventtime-in-human-readable-format-in-java
     */
    public String dateAndTimeDataType(String responseHasDataAndTimeDataType) {

        // Convert into array of bytes
        int[] bytes;
        bytes = octetStringToBytes(responseHasDataAndTimeDataType);

        // Maybe nothing specified
        if (bytes[0] == 0)
            return (null);

        // Extract parameters
        int year, month, day, hour, minute, second, deci_sec = 0, offset = 0;

        //Parse the bytes
        year = (bytes[0] * 256) + bytes[1];
        month = bytes[2];
        day = bytes[3];
        hour = bytes[4];
        minute = bytes[5];
        second = bytes[6];
        if (bytes.length >= 8)
            deci_sec = bytes[7];
        if (bytes.length >= 10) {
            offset = bytes[9] * 60;
            if (bytes.length >= 11)
                offset += bytes[10];
            if (bytes[8] == '-')
                offset = -offset;
            offset *= 60 * 1000;
        }

        // Get current DST and time zone offset
        Calendar calendar;
        int my_dst;
        int my_zone;
        calendar = Calendar.getInstance();
        my_dst = calendar.get(Calendar.DST_OFFSET);
        my_zone = calendar.get(Calendar.ZONE_OFFSET);


        // Compose result
        // Month to be converted into 0-based
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, deci_sec * 100);

        // Reset DST
        calendar.add(Calendar.MILLISECOND, my_dst);


        // If the offset is set, we have to convert the time using the offset of our time zone
        if (offset != 0) {
            int delta;
            delta = my_zone - offset;
            calendar.add(Calendar.MILLISECOND, delta);
        }

        // Return result
        return (calendar.getTime().toString());

    } // return a human-readable date and time

    /*
     * Convert the octet string to bytes for further processing in dateAndTimeDataType()
     * @param response: the response from the SNMP agent in String format
     * @return: an array of bytes
     */
    public static int[] octetStringToBytes(String response) {
        // Split string into its parts
        String[] bytes;
        bytes = response.split("[^0-9A-Fa-f]");

        // Initialize result
        int[] result;
        result = new int[bytes.length];

        // Convert bytes
        int counter;
        for (counter = 0; counter < bytes.length; counter++)
            result[counter] = Integer.parseInt(bytes[counter], 16);

        return (result);
    } //return a byte array








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