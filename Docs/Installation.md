## Get IP address of connected router

Use:
```bash
ip route | grep default
#default via .... proto dhcp metric 600 
```
or 
```bash
route -n
```

But my Viettel modem does not support SNMP :((
## Run NMS on Linux host and SNMP Agent on Linux virtual machine
Install KVM: https://phoenixnap.com/kb/ubuntu-install-kvm
In this, I test on Linux Mint Xfce 23.1

Get the IP address of virtual machine using:
```bash
hostname -I
#192.168.122.133 
```

Set up the SNMP on host and virtual machine:
Refer: https://www.digitalocean.com/community/tutorials/how-to-install-and-configure-an-snmp-daemon-and-client-on-ubuntu-18-04

https://www.youtube.com/watch?v=D5uifMiVdbY&t=54s

```bash
sudo apt install snmp snmpd
```
snmp is SNMP utils, snmpd is the agent, which hold database and respone to query

Then check the status of the agent to make sure it is active
```bash
systemctl status snmpd
```