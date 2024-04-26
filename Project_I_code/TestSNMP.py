from pysnmp.hlapi import *


def get(target_ip, oids, credentials, port=161):
    errorIndication, errorStatus, errorIndex, varBinds = next(
        getCmd(SnmpEngine(),
               credentials,
               UdpTransportTarget((target_ip, port)),
               ContextData(),
               *[ObjectType(ObjectIdentity(oid)) for oid in oids]
            )
        )
    
    if errorIndication:
        print(f"Error: {errorIndication}")
    elif errorStatus:
        print(f"Error: {errorStatus} at {errorIndex}")
    else:
        #for varBind in varBinds:
            #print(f"{varBind.prettyPrint()}")
            # The output by the above line is in the format of "OID = Value"
            # For example: SNMPv2-SMI::enterprises.2021.4.5.0 = 4005396

            # We only want to print the value, so we split the string by "="
            result = []
            for varBind in varBinds:
                result.append(varBind.prettyPrint().split('=')[-1].strip())
            return result
            
        

# Define SNMPv3 credentials
credentials = UsmUserData(
    'ctav3',         # SNMPv3 username (configured on SNMP agent)
    'chutrunganh',   # Authentication password
    'chutrunganh',   # Privacy password
    authProtocol=usmHMACMD5AuthProtocol,   # Authentication protocol (MD5)
    privProtocol=usmDESPrivProtocol        # Privacy protocol (DES)
)

# Specify target IP address and OID
target_ip = "192.168.122.102"


# Print out some system information
# Define a dictionary with OIDs and their corresponding messages
# oid_dict = {
#     "1.3.6.1.2.1.1.1.0": "System Information",
#     ".1.3.6.1.4.1.2021.4.5.0": "Total RAM",
#     "1.3.6.1.4.1.2021.4.6.0" :"RAM usage OID",
#     ".1.3.6.1.2.1.1.3.0": "Up Time" ,  #Up time will need futher processing to become date time format
#     ".1.3.6.1.4.1.2021.10.1.3.1": "Percentage of user CPU time",
#     #"1.3.6.1.2.1.2.2.1.10":"Network incoming traffic OID",
#     #"1.3.6.1.2.1.2.2.1.16" :"Network outgoing traffic OID",
    
# }
oid_dict = {
    "1.3.6.1.2.1.1.1.0": "System Description",  # Includes OS, specific version, hardware details
    "1.3.6.1.2.1.1.5.0": "System Name",
    "1.3.6.1.2.1.25.3.2.1.3.1": "Processor Description",  # CPU details
    "1.3.6.1.4.1.2021.4.5.0": "Total RAM",
    "1.3.6.1.2.1.25.2.3.1.5.1": "Storage Size",  # Total size of the first storage device
    "1.3.6.1.2.1.25.3.3.1.2.1": "CPU Load",  # Percentage of CPU time
    # There's no standard OID for GPU details, it depends on the specific GPU and its driver
}

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
