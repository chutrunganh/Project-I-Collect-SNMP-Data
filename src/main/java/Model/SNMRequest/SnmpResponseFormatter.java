package Model.SNMRequest;

import java.util.Calendar;
import java.util.Map;

// We tried to use the Mibble API to get the data type of the given OID (getType(), getTag(), getReferenceTo(),...), but it just returns the primitive type instead of the Textual Convention.
// for example, it just returns OCTET STRING instead of DisplayString or DataAndTime,...

/**
 * This class contains a set of static methods to format the response from an SNMP agent based on the data type, constraint of the Node.
 * @input: the response from the SNMP agent, the data type, constraints of the Node
 * @return: the formatted response in human-readable format (String0
 */
public class SnmpResponseFormatter {

    public static String format(Object variable, String dataType, Map<String, Object> constraints) {
        //System.out.println("Formatting variable: " + variable + " of type: " + dataType);
        if (dataType == null) {
            return variable.toString();
        }

        switch (dataType.trim().toLowerCase()) {
            case "integer32":
            case "integer":
                return formatInteger(variable, constraints);

            case "dateandtime":
                return formatDataAbdTime(variable, constraints);

            default:
                return variable.toString();
        }
    }

    /**
     * With integer data type, we can have some constraints like enumeration, size,...
     * To test for the Enumeration constraints, use the TestConstraint class in the Test package to simulate the SNMP response
     */
    private static String formatInteger(Object variable, Map<String, Object> constraints) {
        //System.out.println("Formatting Integer with constraints: " + constraints);
        if (constraints != null && constraints.containsKey("enumeration")) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> enumeration = (Map<String, Integer>) constraints.get("enumeration");
            System.out.println("Enumeration: " + enumeration);
            for (Map.Entry<String, Integer> entry : enumeration.entrySet()) {
                System.out.println("Checking if " + entry.getValue() + " equals " + variable);
                if (entry.getValue().equals(Integer.parseInt(variable.toString()))) {
                    return "(" + variable + ") " + entry.getKey();
                }
            }
        }
        return variable.toString();
    }



    /** DateAndTime
     * Raw response looks like: 07:e8:05:10:08:34:12:00:2b:07:00
     * Seems like DateAndTime is a child of OCTET STRING, as: http://www.ireasoning.com/javadocs/com/ireasoning/protocol/snmp/SnmpDateAndTime.html
     * and https://docs.lextudio.com/blog/snmp-pro-dateandtime-syntax-support-e99abc51bc2f
     * Some ways to convert it to human-readable format: https://stackoverflow.com/questions/9700037/snmp-eventtime-in-human-readable-format-in-java
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

    public static String formatDataAbdTime(Object variable, Map<String, Object> constraints) {

        String responseHasDataAndTimeDataType = variable.toString();
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
