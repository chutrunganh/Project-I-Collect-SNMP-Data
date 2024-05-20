package Project_I_Code.GUI;


/*
 * This class is used to define a row in the query table.
 */
public class ARowInQueryTable {
    private final String name;
    private final String value;
    private final String syntax;

    public ARowInQueryTable(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.syntax = type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getSyntax() {
        return syntax;
    }

    //the table API will call getXXX so do not mark attributes as public and remove getters
}