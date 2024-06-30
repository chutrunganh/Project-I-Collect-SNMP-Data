#!/bin/bash

pip install pysnmp-pysmi

# Function to list all files in a directory and its subdirectories
list_files() {
    for file in "$1"/*
    do
        if [ -d "$file" ]; then
            list_files "$file"
        elif [ -f "$file" ]; then
            echo "$(basename "$file")"
        fi
    done
}

# Directory containing MIB files, we want to compile these files to JSON format
MIB_DIR="/home/chutrunganh/Downloads/ireasoning/mibbrowser/mibs"

# Directory to save converted MIB files
DEST_DIR="/home/chutrunganh/Project/Project_I_Collect_SNMP_Data/out/artifacts/SNMP_Browser/MIB Databases"

# List all MIB files and store them in a variable
MIB_FILES=$(list_files "$MIB_DIR")

# Array to store MIBs that failed to convert
FAILED_MIBS=()

# Convert each MIB file individually

# --mib-source=file:///home/chutrunganh/Desktop to add RFC1212
for mib in $MIB_FILES
do
    echo "Processing $mib..."
    mibdump.py --destination-format=json \
               --destination-directory="$DEST_DIR" \
               --mib-source=file:///home/chutrunganh/Desktop \
               --mib-source=file:///usr/share/snmp \
               --mib-source=http://mibs.snmplabs.com/asn1/@mib@ \
               --no-dependencies\
               --mib-stub=RFC-1212,RFC-1215,RFC1065-SMI,RFC1155-SMI,RFC1158-MIB,RFC1213-MIB,SNMP-FRAMEWORK-MIB,SNMP-TARGET-MIB,SNMPv2-CONF,SNMPv2-SMI,SNMPv2-TC,SNMPv2-TM,TRANSPORT-ADDRESS-MIB \
               "$mib"
    if [ $? -eq 0 ]; then
        echo "Successfully converted $mib"
    else
        echo "Failed to convert $mib, continuing..."
        FAILED_MIBS+=("$mib")
    fi
done

# Print out the MIBs that failed to convert
if [ ${#FAILED_MIBS[@]} -ne 0 ]; then
    echo "The following MIBs failed to convert:"
    for failed_mib in "${FAILED_MIBS[@]}"
    do
        echo "$failed_mib"
    done
else
    echo "All MIBs converted successfully."
fi
