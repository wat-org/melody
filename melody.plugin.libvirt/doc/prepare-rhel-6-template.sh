#!/bin/sh

[ -z "${ACTIVATIONKEY}" ] && {
  echo "assign your RHN activation key in a variable ACTIVATIONKEY, export it and re-run the script."
  exit 1
}

rhnreg_ks --activationkey="${ACTIVATIONKEY}"

# need to enable rhn classic
sed -i -E 's/^enabled\s?=.*$/enabled = 1/g' /etc/yum/pluginconf.d/rhnplugin.conf
# and to disable rhsm
sed -i -E 's/^enabled\s?=.*$/enabled = 0/g' /etc/yum/pluginconf.d/subscription-manager.conf

yum update -y
yum install -y acpid

sed -i -e /^HOSTNAME=/d /etc/sysconfig/network
sed -i -e /^HWADDR=/d /etc/sysconfig/network-scripts/ifcfg-eth0
sed -i -e /^UUID=/d /etc/sysconfig/network-scripts/ifcfg-eth0
sed -i -e s/^ONBOOT=.*$/ONBOOT=yes/ /etc/sysconfig/network-scripts/ifcfg-eth0

rm -f /etc/sysconfig/rhn/systemid 
rm -f /etc/udev/rules.d/70-persistent-*
rm -f /etc/ssh/ssh_host_*

service lvcd stop
rm -vrf /root/.ssh/

> /var/log/messages
> /var/log/cron
> /var/log/audit/audit.log 

find /var/log/  -regex "/var/log/.*-[0-9]+" -exec rm -f {} \;
rm -f /var/log/dmesg.old

sync
> .bash_history
history -c
sync

sleep 10
poweroff; history -c
