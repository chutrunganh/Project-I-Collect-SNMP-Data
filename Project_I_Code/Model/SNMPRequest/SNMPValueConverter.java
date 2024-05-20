package Project_I_Code.Model.SNMPRequest;

import javafx.util.Pair;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;
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
     * Since the SNMP Walk only retrieves the OID and the value, we need to find the data type and name
     * corresponding to the OID in the MIB file to display to the query table.
     * <p>
     * Example by Mibble API Doc: https://www.mibble.org/doc/faq-java-api.html in Q4
     */
    public Pair<String, String> convertToHumanReadable(Variable variable, String oid) {
        MibValueSymbol symbol = locateSymbolByOid(mibLoader, oid);
        if (symbol != null) {
            MibType type = symbol.getType();
            String name = symbol.getName();
            String value;
            switch (type.getName()) {
                case "Integer32":
                    value = String.valueOf(variable.toInt());
                    break;
                case "OctetString":
                    //value = new String(variable.toOctets());
                    // break;
                default:
                    value = variable.toString();
            }
            return new Pair<>(value, name);
        } else {
            // Consider logging this case for debugging purposes
            return new Pair<>(variable.toString(), "Not Defined");
        }
    }
}