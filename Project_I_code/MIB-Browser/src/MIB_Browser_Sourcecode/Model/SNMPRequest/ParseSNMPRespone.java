package MIB_Browser_Sourcecode.Model.SNMRequest;

import java.util.Calendar;

public class ParseSNMPRespone {

    public String response;
    public String dataType;

    public ParseSNMPRespone(String response, String dataType) {
        this.response = response;
        this.dataType = dataType;
    }


    /*
     * Based on the data type of the response, this function will return the response in a human-readable format.
     * Take the repose as a String and convert it to the appropriate data type.
     * */
    public String returnValueBasedOnDataType(String response, String dataType) {
        dataType = dataType.trim().toLowerCase();

        if (dataType.equals("integer")) {
            return processIntegerDataType();
        } else if (dataType.contains("displaystring")) { //Some MIB uses DisplayString, InternationalDisplayString, etc.
            return processStringDataType();
        } else if (dataType.equals("timeticks")) {
            return timeTicksDataType();
        } else if (dataType.equals("dateandtime")) {
            return dateAndTimeDataType();
        } else {
            return "Data type not supported";
        }
    }

    //SOME COMMON DATA TYPES IN SNMP:

    //String
    public String processStringDataType() {
        return response;
    }

    //Integer
    public String processIntegerDataType() {
        return response;
    }

    //TimeTicks
    //Some SNMP agents return time ticks in the format of "days:hours:minutes:seconds" but some
    // may return in thousandths of seconds format.
    public String timeTicksDataType() {
        if (response.contains(":") || response.contains(".")) {
            return response;
        } else {
            int timeTicks = Integer.parseInt(response);
            int totalSeconds = timeTicks / 100;
            int days = totalSeconds / (24 * 60 * 60);
            totalSeconds = totalSeconds % (24 * 60 * 60);
            int hours = totalSeconds / (60 * 60);
            totalSeconds = totalSeconds % (60 * 60);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
        }
    }

    //DateAndTime
    /*
     * Raw response looks like: 07:e8:05:10:08:34:12:00:2b:07:00
     * Seems like DateAndTime is a child of OCTET STRING, as: http://www.ireasoning.com/javadocs/com/ireasoning/protocol/snmp/SnmpDateAndTime.html
     * and https://docs.lextudio.com/blog/snmp-pro-dateandtime-syntax-support-e99abc51bc2f
     * Some ways to convert it to human-readable format: https://stackoverflow.com/questions/9700037/snmp-eventtime-in-human-readable-format-in-java
     */
    public String dateAndTimeDataType() {

        // Convert into array of bytes
        int[] bytes;
        bytes = octetStringToBytes(response);

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

}
