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


*Or you can try to run an SNMP agent on a Virtual machine for better demonstrayion. Here I tested on Linux Mint Xfce 21.3 using KVM. Install instruction in this: https://phoenixnap.com/kb/ubuntu-install-kvm.*.
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



### With SNMP version v1 and v2c
Pay attention to these lines:
```plaintext
# Read-only access to everyone to the systemonly view
rocommunity  public default -V systemonly
rocommunity6 public default -V systemonly
```
These are the community strings used for SNMP v1 and v2. The default value is `public` as shown.

Here's a breakdown of each part:

- `rocommunity`: This keyword specifies that you are defining a read-only SNMP community. `rocommunity6` is uesd for IPv6 (maybe)

- `public`: This is the community string, which is like a password. In this case, the community string is `public`.

- `default`: This is the source list. It defines where SNMP requests can come from. The `default` keyword means that requests can come from any source.

- `-V systemonly`: It limits the community named `public` to only the SNMP view called `systemonly`. In this case, the community has access to the `systemonly` view. A view in SNMP is a subset of the MIB (Management Information Base) that is available for management operations.

The view is defined in these lines above:
```plaintext
#  system + hrSystem groups only
view   systemonly  included   .1.3.6.1.2.1.1
view   systemonly  included   .1.3.6.1.2.1.25.1
```
The lines you've posted are from an SNMP (Simple Network Management Protocol) configuration file. They define a view in SNMP, which is a subset of the MIB (Management Information Base) that is available for management operations. 

Here's what each line means:

- `view systemonly included .1.3.6.1.2.1.1`: This line is defining a view named `systemonly` that includes the MIB subtree `.1.3.6.1.2.1.1`. This subtree corresponds to the `system` group in the SNMP MIB-2, which contains system-level information like the system's description, location, and uptime.

- `view systemonly included .1.3.6.1.2.1.25.1`: This line is adding the MIB subtree `.1.3.6.1.2.1.25.1` to the `systemonly` view. This subtree corresponds to the `hrSystem` group in the HOST-RESOURCES-MIB, which contains information about the host's system, like the number of users and the system's date and time.


Now, we will configure our own community string as follows:
```plaintext
rwcommunity password default
```
This line indicates that we are setting up a read-write community string. Here, `password` is the community string, which functions like a password. You should replace `password` with your actual desired password. The `default` keyword allows SNMP requests from any source. There is no restriction on view so this configuration grants access to the entire MIB.

Remember to restart snmpd to takes effect:
```sudo systemctl restart snmpd```
S

### With SNMP version v3

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
Write more on this: https://hiendef.wordpress.com/wp-content/uploads/2013/07/th5-nm.pdf


# References

- [How To Install and Configure an SNMP Daemon and Client on Ubuntu 18.04](https://www.digitalocean.com/community/tutorials/how-to-install-and-configure-an-snmp-daemon-and-client-on-ubuntu-18-04)
- [Configuring SNMP on Linux](https://www.site24x7.com/help/admin/adding-a-monitor/configuring-snmp-linux.html)
- [SNMP Configuration Video](https://www.youtube.com/watch?v=D5uifMiVdbY&t=54s)