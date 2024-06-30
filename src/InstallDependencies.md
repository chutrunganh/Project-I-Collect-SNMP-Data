- With MIB files:

Using the provided script in `GetJSONFiles.sh` to convert them to JSON files. There are over 70 MIB files already available in the `MIB Databases` 
directory files, or find them here: https://mibbrowser.online/mibdb_search.php?mib=SNMPv2-MIB with all formats

Choose to download and add the dependencies manually by below steps or using Maven pom.xml file
to install them automatically.

- Install **JavaFX**:

Download the  compatible version here  https://gluonhq.com/products/javafx/. Currently, in this project, I 
use JavaFX 21.0.3 [LTS].

- Install **Jackson**: https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.2.3

With IntelliJ IDEA: Go to File -> Project Structure -> select Modules on the left -> Go to the 
Dependencies tab.ss -> Click on the + button on the right side and select Library -> In the New Library 
dialog, select From Maven -> In the Download Library from Maven Repository 
dialog, enter com.fasterxml.jackson.core:jackson-databind:2.17.1 in the Coordinates field. Replace 2.13.1 
with the latest version if necessary -> Click OK to start downloading the library -> Once the library is downloaded, click OK in the New Library dialog
-> Click Apply and then OK in the Project Structure dialog.

Now, Jackson library should be added to your project, and you can use it in your code. IntelliJ IDEA will automatically handle the classpath settings.


- Install **snmp4j** by similar steps as above. Here I use `org.snmp4j:snmp4j:3.7.7`