# Step 1: Install SNMP Daemon and Utilities

## On the Network Management System (NMS)
On your local machine, install the necessary packages for the SNMP Manager:
```bash
sudo apt update && sudo apt upgrade
sudo apt install snmp snmp-mibs-downloader
```
The `snmp` package provides a suite of command-line tools for sending SNMP requests to agents. The `snmp-mibs-downloader` package assists in installing and managing Management Information Base (MIB) files.

## On the Agent
Next, switch to the agent device we want to manage:

You can use your local machine with the host OS as an agent (i.e., your machine acts as both an SNMP manager and an SNMP agent simultaneously). This configuration will follow this approach.

Install the SNMP agent:
```bash
sudo apt install snmpd
```
This command installs the SNMP daemon.


*I attempted to run an SNMP agent on a KVM virtual machine, which I installed following the instructions at this link: https://phoenixnap.com/kb/ubuntu-install-kvm. However, most of the OIDs are not functioning as expected. Interestingly, some OIDs related to the system name and total system RAM are working. I don't know why :))*.
# Step 2: Configure SNMP

## On the NMS
Edit the `snmp.conf` file with your preferred text editor:
```bash
sudo nano /etc/snmp/snmp.conf
```
In this file, comment out the `mibs :` line to allow the manager to import the MIB files.

## On the Agent
Edit the `snmpd.conf` file with your preferred text editor:
```bash
sudo nano /etc/snmp/snmpd.conf
```
Note that it's `snmpd.conf` on the agent side compared to `snmp.conf` on the manager side, different by the 'd' letter.

First, modify the `agentAddress` directive. Currently, it only allows connections from the local computer. Comment out the current line, and add a new line as below, which allows all connections to port 161 UDP. If you only want to work with the local machine, you don't need to do this.

```plaintext
#agentaddress  127.0.0.1,[::1]
agentAddress udp:161,udp6:[::1]:161
```
This change allows all connections to port 161 of the agent device (since the Polling Request from NMS communicates with agents through port 161 UDP).

Pay attention to these lines:
```plaintext
# Read-only access to everyone to the systemonly view
rocommunity  public default -V systemonly
rocommunity6 public default -V systemonly
```
These are the community strings used for SNMP v1 and v2. The default value is `public` as shown.

If you want to use SNMP v3, you need to create a user. The following commands create a new user on the agent side:

First, stop the `snmpd` daemon to add a new user:
```bash
sudo systemctl stop snmpd
```
Then add a new user:

```bash
sudo net-snmp-create-v3-user
```

Example: 
```bash
sudo net-snmp-create-v3-user           
Enter a SNMPv3 user name to create: 
chutrunganh
Enter authentication pass-phrase: 
chutrunganh.123
Enter encryption pass-phrase: 
  [press return to reuse the authentication pass-phrase]

adding the following line to /var/lib/snmp/snmpd.conf:
   createUser chutrunganh MD5 "chutrunganh.123" DES
adding the following line to /usr/share/snmp/snmpd.conf:
   rwuser chutrunganh
```
Next, restart the `snmpd` daemon to apply the changes
```bash
sudo systemctl restart snmpd
```

Verify the user creation by checking these files:
```bash
sudo tail /usr/share/snmp/snmpd.conf              
```
Output:
```plaintext
rwuser chutrunganh
```
And

```bash
sudo nano /var/lib/snmp/snmpd.conf
```

# Step 3: Configure Firewall

Allow necessary ports by executing the following commands:

```bash
sudo ufw allow 161/udp
sudo ufw allow 162/udp
```

# Testing the Configuration
Verify if `snmpd` is running by executing:
```bash
sudo systemctl status snmpd
```
If it's not active, start it using:
```bash
sudo systemctl start snmpd
```

After completing the above steps, send a GET request from the NMS to the agent using the `snmpget` command:
```bash
snmpget -u chutrunganh -l authPriv -a MD5 -x DES -A chutrunganh.123 -X chutrunganh.123 192.168.1.8  1.3.6.1.2.1.1.1.0
```

In this command:

- `-u chutrunganh` specifies the SNMPv3 username.
- `-l authPriv` sets the security level to `authPriv`, which means authentication and privacy are required.
- `-a MD5` and `-x DES` set the authentication and privacy protocols to MD5 and DES, respectively.
- `-A chutrunganh.123` and `-X chutrunganh.123` set the authentication and privacy passphrases to `chutrunganh`.
- `192.168.1.8` is the IP address of the SNMP agent you're sending the GET request to. Use `hostname -I` to find the machine IP address.
- `1.3.6.1.2.1.1.1.0` is the OID (Object Identifier) for the system description.

Replace `chutrunganh`, `chutrunganh.123`, `192.168.1.8` with your actual values.

The output of this command should be the system description of the SNMP agent. If you get this output, your SNMP configuration is working correctly.

The output should be: 
```plaintext
SNMPv2-MIB::sysDescr.0 = STRING: Linux ThinkPad-CTA 6.5.0-1020-oem #21-Ubuntu SMP PREEMPT_DYNAMIC Wed Apr  3 14:54:32 UTC 2024 x86_64
```

# References

- [How To Install and Configure an SNMP Daemon and Client on Ubuntu 18.04](https://www.digitalocean.com/community/tutorials/how-to-install-and-configure-an-snmp-daemon-and-client-on-ubuntu-18-04)
- [Configuring SNMP on Linux](https://www.site24x7.com/help/admin/adding-a-monitor/configuring-snmp-linux.html)
- [SNMP Configuration Video](https://www.youtube.com/watch?v=D5uifMiVdbY&t=54s)