package Project_I_Code.Model.SNMPRequest;

import javafx.util.Pair;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

/**
 * This class contains methods to convert SNMP response values (in VariableBlinding to human-readable format.
 */
public class SNMPValueConverter {

    // In order to Parse the values of the SNMP response, we need to load some necessary MIB files.
    // These files will be loaded in the MainController class and passed to this class.
    private final MibLoader mibLoader;

    public SNMPValueConverter(MibLoader mibLoader) {
        this.mibLoader = mibLoader;
    }

    //Getter for the MibLoader object
    public MibLoader getMibLoader() {
        return mibLoader;
    }


    /**
     * Locates a symbol by its OID in the loaded MIB files.
     * @param loader The MibLoader instance to use for locating the symbol.
     * @param oid The OID of the symbol to locate.
     * @return The located MibValueSymbol, or null if no symbol was found.
     */
    public MibValueSymbol locateSymbolByOid(MibLoader loader, String oid) {
        ObjectIdentifierValue iso = loader.getRootOid();
        ObjectIdentifierValue match = iso.find(oid);
        return (match == null) ? null : match.getSymbol();
    }

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

    public Pair<String, String> convertToHumanReadableWithoutKnowTheDataType(Variable variable, String oid) {
        MibValueSymbol symbol = locateSymbolByOid(mibLoader, oid);  // Locate the symbol by its OID
        if (symbol != null) {
            MibType type = symbol.getType();

            // Print the tag to the console
            //https://www.mibble.org/doc/faq-java-api.html#Q6
            System.out.println("Tag: " + type.getTag());

            //After locating the symbol, we pass the value and the data type that will process the value based on the data type to method
            // convertToHumanReadableWithKnownDataType
            return new Pair<>(convertToHumanReadableWithKnownDataType(variable, type.getName()), symbol.getName());
            //return Pair contains the value in human-readable and the name of the symbol of corresponding OID
        } else {
            // Consider logging this case for debugging purposes
            return new Pair<>(variable.toString(), "Not Defined");
        }
    }

    /**
    * This function is used to convert the SNMP value to human-readable format based on the data type of the OID.
     * This function is called to process the value after we found the data type of the OID in the MIB file.
     * Or this function can be used by some request which we know the data type of the OID in advance like SNMP GET,...
    * */
    public String convertToHumanReadableWithKnownDataType(Variable variable, String dataType) {
        String humanReadableString;

        // Ensure that we can retrieve from the response, the response return nothing, we return null
        // Also, in case the node do not have the data type, like most of non-leaf nodes, we return null
        if (variable == null || dataType == null) {return "Can not resolve this OID";}

        switch (dataType.toLowerCase()) {
            case "integer32":
                humanReadableString = String.valueOf(variable.toInt());
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
     *  SOME OTHER DATA TYPES THAT CAN BE USED IN SNMP
     *  timeTicks : usually used to represent the time in hundredths of a second since the device was started but in snmp4j,
     *  it is represented in time format already, like this: 3:53:39.34
     *
     * */

    //https://igss.schneider-electric.com/Files/Doc-Help/Webhelp/V14/Interface/SNMP/About_SNMP_Textual_Convention.htm
//Many node has type OCTET STRING, but their Textual Convention is different, so we need to check the Textual Convention to know how to convert it

    /*
    * The hrSystemDate object in the HOST-RESOURCES-MIB is defined with the DateAndTime syntax. DateAndTime is a textual convention
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