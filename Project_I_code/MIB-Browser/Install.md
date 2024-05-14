Find MIB file here: https://mibbrowser.online/mibdb_search.php?mib=SNMPv2-MIB with all formats

Install Jackson: https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.2.3
1. Open your project in IntelliJ IDEA.
2. Go to File -> Project Structure (or press Ctrl+Alt+Shift+S).
3. In the Project Structure dialog, select Modules on the left.
4. Select your module where you want to add the Jackson library. 
5. Go to the Dependencies tab.ss
6. Click on the + button on the right side and select Library....
7. In the New Library dialog, select From Maven....
8. In the Download Library from Maven Repository dialog, enter com.fasterxml.jackson.core:jackson-databind:2.13.1 in the Coordinates field. Replace 2.13.1 with the latest version if necessary.
9. Click OK to start downloading the library.
10. Once the library is downloaded, click OK in the New Library dialog.
11. Click Apply and then OK in the Project Structure dialog.
Now, Jackson library should be added to your project and you can use it in your code. IntelliJ IDEA will automatically handle the classpath settings.


Install snmp4j by similar steps as above. Here I use `org.snmp4j:snmp4j:3.7.7`