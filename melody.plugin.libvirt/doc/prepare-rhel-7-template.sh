#!/bin/sh

[ -z "${RHSM_USERNAME}" ] && {
  echo "assign your RHSM username in a variable RHSM_USERNAME, export it and re-run the script."
  exit 1
}

[ -z "${RHSM_PASSWORD}" ] && {
  echo "assign your RHSM password in a variable RHSM_PASSWORD, export it and re-run the script."
  exit 1
}

subscription-manager register --auto-attach --force --username="${RHSM_USERNAME}" --password="${RHSM_PASSWORD}"

# need to disbale rhn classic
sed -i -E 's/^enabled\s?=.*$/enabled = 0/g' /etc/yum/pluginconf.d/rhnplugin.conf
# and to enable rhsm
sed -i -E 's/^enabled\s?=.*$/enabled = 1/g' /etc/yum/pluginconf.d/subscription-manager.conf

# need to install iptables.service
yum install -y iptables-services

# remove rhn because we are using rhsm (will also remove rhn-check, rhn-setup, yum-rhn-plugin)
yum remove -y rhnsd

# update everything
yum update -y

# copy lvcd inside
chkconfig --add lvcd
chkconfig lvcd on
systemctl enable lvcd

# sshd must start after lvcd
# vi /lib/systemd/system/sshd.service
# add lvcd.service at the end of the line 'After' 

#reload systemd
systemctl daemon-reload

# unsubscribe
subscription-manager unregister

sed -i -e /^HOSTNAME=/d /etc/sysconfig/network
sed -i -e /^HWADDR=/d /etc/sysconfig/network-scripts/ifcfg-eth0
rm -f /etc/hostname

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
