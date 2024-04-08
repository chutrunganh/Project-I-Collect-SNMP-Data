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
