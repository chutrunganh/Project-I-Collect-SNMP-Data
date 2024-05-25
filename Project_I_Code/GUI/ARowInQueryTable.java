package Project_I_Code.GUI;


/*
 * This class is used to define a row in the query table.
 */
public class ARowInQueryTable {
    private final String name;
    private final String value;
    private final String dataType;

    public ARowInQueryTable(String name, String value, String dataType) {
        this.name = name;
        this.value = value;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDataType() {
        return dataType;
    }

    //the table API will call getXXX so do not mark attributes as public and remove getters
}