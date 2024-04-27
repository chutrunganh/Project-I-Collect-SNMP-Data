## Test SNMP in Python
Refer: https://www.javatpoint.com/snmp-module-in-python
1. Download SNMP module for python:
```bash
pip install pysnmp  
```
2. Uderstand the Get operation

The Get operation of SNMP enables us to retrieve the value of an individual object in the MIB


```python
from pysnmp.hlapi import *

# For SNMP v3
from pysnmp.hlapi import *

def get(target_ip, oids, credentials, port=161):
    errorIndication, errorStatus, errorIndex, varBinds = next(
        getCmd(SnmpEngine(),
               credentials,
               UdpTransportTarget((target_ip, port)),
               ContextData(),
               *([ObjectType(ObjectIdentity(oid)) for oid in oids])
        )
    )
    
    if errorIndication:
        print(f"Error: {errorIndication}")
    elif errorStatus:
        print(f"Error: {errorStatus} at {errorIndex}")
    else:
        for varBind in varBinds:
            print(f"{varBind.prettyPrint()}")

# Define SNMPv3 credentials
credentials = UsmUserData(
    'ctav3',         # SNMPv3 username (configured on SNMP agent)
    'chutrunganh',   # Authentication password
    'chutrunganh',   # Privacy password
    authProtocol=usmHMACMD5AuthProtocol,   # Authentication protocol (MD5)
    privProtocol=usmDESPrivProtocol        # Privacy protocol (DES)
)

# Specify target IP address and OID
target_ip = "192.168.122.133"
oid = "1.3.6.1.2.1.1.1.0"  # System description OID

# Call the get() function to retrieve SNMP data using SNMPv3
get(target_ip, [oid], credentials)

```


```python
from pysnmp.hlapi import *

# Define the get() function for SNMPv2
def get(target, oids, community_string, port=161):
    errorIndication, errorStatus, errorIndex, varBinds = next(
        getCmd(SnmpEngine(),
               CommunityData(community_string),
               UdpTransportTarget((target, port)),
               ContextData(),
               *[ObjectType(ObjectIdentity(oid)) for oid in oids])
    )
    
    if errorIndication:
        print(f"Error: {errorIndication}")
    elif errorStatus:
        print(f"Error: {errorStatus} at {errorIndex}")
    else:
        for varBind in varBinds:
            print(f"{varBind.prettyPrint()}")

# Specify target IP address
target_ip = '192.168.122.133'  # Change to the IP address of your SNMP agent

# Specify OID(s) you want to retrieve
oids = ['1.3.6.1.2.1.1.1.0']  # Example OID for system description

# Specify SNMPv2 community string
community_string = 'public'  # Change to your SNMPv2 community string

# Call the get() function to retrieve SNMP data
get(target_ip, oids, community_string)


```
#### Explain parameters
Function get() requires:
- **target** : Represents the IP address or hostname of the device from which you want to retrieve SNMP information

- **oids**: Represents the list of SNMP Object Identifiers (OIDs) that specify the information you want to retrieve.
- **credentials**: Represents the SNMP credentials (e.g. community string in SNMP version 1,2c or username/password in version 3) required to authenticate with the device.
- **port**: Represents the UDP port number (default is 161, the standard SNMP port).

- **engine**: Represents the SNMP engine used for communication (default is an instance of hlapi.SnmpEngine()).
    
    - *What this parameter "engine" use for? -> Represents the core component responsible for managing SNMP operations Like:*
        - Message Processing: Handles the encoding, decoding, and validation of SNMP messages.
        - Security Configuration: Manages SNMP security features like authentication and encryption settings.
        - Transport Layer Interaction: Communicates with transport protocols (e.g., UDP, TCP) for sending and receiving SNMP messages.
        - Session Management: Maintains the state and lifecycle of SNMP sessions and associated resources.

- **context**: (Phần này chưa hiểu lắm) The context parameter in SNMP, specifically hlapi.ContextData(), is used to define the context in which an SNMP operation is performed. In the context of the PySNMP high-level API (hlapi), ContextData() is an object that represents SNMP context information, which includes both the contextEngineId and the contextName. The contextName and contextEngineID parameters allow multiple versions of the same MIB objects to be made available by a single SNMPv3 engine, as if you have multiple agents running on the same IP address and port. These are distinguished from one another by contextName and contextEngineID, where otherwise they would have had a different address and/or port. 
Refer: https://stackoverflow.com/questions/32132365/significance-of-context-name-in-snmpv3

#### Explain the operation inside the function

- hlapi.getCmd: This function is used to create an SNMP GET command handler.
- engine: The SNMP engine object used for sending the command.
- credentials: The SNMP credentials object used for authentication.
- hlapi.UdpTransportTarget((target, port)): Creates a UDP transport target using the specified target (IP address or hostname) and port.
- context: The SNMP context data used for the operation.
- *construct_object_types(oids): This line calls a function construct_object_types() with oids as arguments to construct a list of SNMP Object Types for the SNMP GET operation.

- fetch(handler, 1): This part of the line calls a function named fetch() with two arguments:
    - handler: Represents the SNMP command handler generated by hlapi.getCmd(). This handler encapsulates the SNMP operation to retrieve information from a device.
    - 1: Indicates the maximum number of results to fetch. In this case, 1 means fetching only one result.
    
    After calling fetch(handler, 1), the [0] indexing is used to extract the first (and presumably only) result from the fetched data.

<<rewrite section Explain the operation inside the function later>>

## Get some info from server

I found some usefull OID here
refer: http://www.linux-admins.net/2012/02/linux-snmp-oids-for-cpumemory-and-disk.html

```python
oid_dict = {
    "1.3.6.1.2.1.1.1.0": "System Information",
    ".1.3.6.1.4.1.2021.4.5.0": "Total RAM",
    "1.3.6.1.4.1.2021.4.6.0" :"RAM usage OID",
    ".1.3.6.1.2.1.1.3.0": "Up Time" ,  #Up time will need futher processing to become date time format
    #"1.3.6.1.4.1.2021.11.9.0": "Percentage of user CPU time",
    #"1.3.6.1.2.1.2.2.1.10":"Network incoming traffic OID",
    #"1.3.6.1.2.1.2.2.1.16" :"Network outgoing traffic OID",
    
}
```

## Write to csv file
```python
oids = list(oid_dict.keys())
result = get(target_ip, oids, credentials)
print(result)


import csv
# Write results to a CSV file
csv_file = 'snmp_data.csv'
headers = ['CPU Load (%)', 'Total RAM (bytes)', 'Used RAM (bytes)', 'Incoming Octets', 'Outgoing Octets']


# Check if the file is empty, then we write the header and server name, otherwise we just write the data
# Open the file in read mode
with open(csv_file, 'r') as file:
    # Try to read the first character
    first_char = file.read(1)

# If we couldn't read a character, the file is empty
if not first_char:
    # Open the file in write mode and write the header
    with open(csv_file, 'w') as file:
        writer = csv.writer(file)
        writer.writerow([result[0]])
        writer.writerow(headers)
        
        


with open(csv_file, 'a', newline='') as file:
    writer = csv.writer(file)   
    writer.writerow(result[1:])

    
    #writer.writerow(result[1:])

print(f"SNMP data saved to '{csv_file}'")
```