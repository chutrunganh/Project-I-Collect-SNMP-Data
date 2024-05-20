package Project_I_Code.Model;

import javafx.util.Pair;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import org.snmp4j.smi.Variable;

public class SNMPValueConverter {

    private final MibLoader mibLoader;

    public SNMPValueConverter(MibLoader mibLoader) {
        this.mibLoader = mibLoader;
    }

    public MibLoader getMibLoader() {
        return mibLoader;
    }

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

    public MibValueSymbol locateSymbolByOid(MibLoader loader, String oid) {
        ObjectIdentifierValue iso = loader.getRootOid();
        ObjectIdentifierValue match = iso.find(oid);
        return (match == null) ? null : match.getSymbol();
    }
}