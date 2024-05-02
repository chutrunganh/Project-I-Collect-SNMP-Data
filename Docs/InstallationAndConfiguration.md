




# Step 1: Installing the SNMP Deamon and Utilities

## With MNS
From your local machine, install packages needed for SNMP Mangager:
```bash
sudo apt update && sudo apt upgrade
sudo apt install snmp snmp-mibs-downloader
```
The ```snmp``` package provides a collection of command line tools for issuing SNMP requests to agents. The ```snmp-mibs-downloader``` package will help to install and manage Management Information Base (MIB) files.

## With Agent
Now, conme to the agent device we want to manage:

You can use your local machine with host OS as an agent (Meaning that your machine is act as a SNMP manager and SNMP agent at the same time), I will use this approach in this configuration

*I tried to run SNMP agent on virtual machine KVM install by this way: https://phoenixnap.com/kb/ubuntu-install-kvm but most of OID do not work, some work are OID related to name, total system RAM, don know why :))*

Install SNMP agent:
```bash
sudo apt install snmpd
```
This command will install the SNMP deamon

# Step 2: Configuration file

## With MNS

Edit the ```snmp.conf``` by your favourite text editor:
```bash
sudo nano /etc/snmp/snmp.conf
```
In this file, there are a few comments and a single un-commented line. To allow the manager to import the MIB files, comment out the ```mibs :``` line

## With Agent
dit the ```snmpd.conf``` by your favourite text editor:
```bash
sudo nano /etc/snmp/snmpd.conf
```
*Note that it is snmpd.conf in the agent side compare to snmp.conf in the manager side*

First, you need to change the ```agentAddress``` directive. Currently, it is set to only allow connections originating from the local computer. You’ll need to comment out the current line, and uncomment the line underneath, which allows all connections. In case you only want to work with local machine like I said previous, there is no need to do this.

```plaintext
#agentaddress  127.0.0.1,[::1]
agentAddress udp:161,udp6:[::1]:161
```
This will allow all connection come to port 161 of the agent device (Since the Polling Request from NMS communicate with agents throught port 161 UDP)


You can also pay attendtion to these lines:
```plaintext
# Read-only access to everyone to the systemonly view
rocommunity  public default -V systemonly
rocommunity6 public default -V systemonly
```
This is the communication string use for SNMP v1 and v2. The default value of it is currently ```public``` as shown

If you want to use with SNMP v3, you will need to create a user to work with. I find add new user by this way is easier than in the article, just using this command in refer https://checkmk.com/blog/how-configure-snmp-linux. 

Use these command in the agent side:

First we need to stop ```snmpd``` deamon to add new user:
```bash
sudo systemctl stop snmpd
```
Then add new user:

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
Next, restart the ```snmpd``` deamon to see the affect
```bash
sudo systemctl restart snmpd
```


After that, verify in to of these find to see if the user exist or not :
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

# Step 3: Allow Firewall

Execute the following commands to allow necessary ports:

```bash
sudo ufw allow 161/udp
sudo ufw allow 162/udp
```

# Test everything
Everythings is ready now, we can verify if ```snmpd``` is runnig by
```bash
sudo systemctl status snmpd
```
In case it is not active, use 
```bash
sudo systemctl start snmpd
```


After do all things above, send a **GET** request from NMS side to the agent using ```snmpget``` command:
```bash
snmpget -u chutrunganh -l authPriv -a MD5 -x DES -A chutrunganh.123 -X chutrunganh.123 192.168.1.8  1.3.6.1.2.1.1.1.0
```


In this command:

- `-u chutrunganh` specifies the SNMPv3 username.
- `-l authPriv` sets the security level to `authPriv`, which means authentication and privacy are required.
- `-a MD5` and `-x DES` set the authentication and privacy protocols to MD5 and DES, respectively.
- `-A chutrunganh.123` and `-X chutrunganh.123` set the authentication and privacy passphrases to `chutrunganh`.
- `192.168.1.8` is the IP address of the SNMP agent you're sending the GET request to. Use ```hostname -I``` to find machine IP address
- `1.3.6.1.2.1.1.1.0` is the OID (Object Identifier) for the system description.

Please replace `chutrunganh`, `chutrunganh.123`, `192.168.1.8` with your actual values.

The output of this command should be the system description of the SNMP agent. If you get this output, that means your SNMP configuration is working correctly.

The output should be: 
```plaintext
SNMPv2-MIB::sysDescr.0 = STRING: Linux ThinkPad-CTA 6.5.0-1020-oem #21-Ubuntu SMP PREEMPT_DYNAMIC Wed Apr  3 14:54:32 UTC 2024 x86_64

```

# Reference

https://www.digitalocean.com/community/tutorials/how-to-install-and-configure-an-snmp-daemon-and-client-on-ubuntu-18-04

https://www.site24x7.com/help/admin/adding-a-monitor/configuring-snmp-linux.html

https://www.youtube.com/watch?v=D5uifMiVdbY&t=54s