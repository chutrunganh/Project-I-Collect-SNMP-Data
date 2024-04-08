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

