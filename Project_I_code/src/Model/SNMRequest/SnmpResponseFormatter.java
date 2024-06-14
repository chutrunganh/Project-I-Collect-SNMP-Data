package Model.SNMRequest;

import java.util.Map;

public class SnmpResponseFormatter {

    public static String format(Object variable, String dataType, Map<String, Object> constraints) {
        System.out.println("Formatting variable: " + variable + " of type: " + dataType);
        if (dataType == null) {
            return variable.toString();
        }

        switch (dataType) {
            case "Integer32":
            case "INTEGER":
                return formatInteger(variable, constraints);

            case "DisplayString":
                return formatDisplayString(variable, constraints);

            // Add more cases for other data types as needed

            default:
                return variable.toString();
        }
    }

    private static String formatInteger(Object variable, Map<String, Object> constraints) {
        System.out.println("Formatting Integer with constraints: " + constraints);
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

    private static String formatDisplayString(Object variable, Map<String, Object> constraints) {
        // You can add more formatting based on constraints if needed
        return variable.toString();
    }
}
