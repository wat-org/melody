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

### copy lvcd inside
chkconfig --add lvcd
chkconfig lvcd on
systemctl enable lvcd

# sshd must start after lvcd
# vi /lib/systemd/system/sshd.service
# add lvcd.service at the end of the line 'After' 

#reload systemd
systemctl daemon-reload

### install gnome
yum groupinstall -y "GNOME Desktop"
# make GUI start at boot time
ln -sf /lib/systemd/system/graphical.target /etc/systemd/system/default.target

### poweroff button should shutdown system unconditionally
# tell gnome to do nothin when power button is pressed
gsettings set org.gnome.settings-daemon.plugins.power button-power nothing
# or
dconf write /org/gnome/settings-daemon/plugins/power/button-power nothing
# then install acpid
yum install -y acpid
# enable acpid
systemctl enable acpid
# start acpid
systemctl start acpid
# re-write your own 'power off' handler
cd /etc/acpi/actions
cp -p power.sh power.sh.orig
echo -e '#!/bin/sh\nPATH=/sbin:/bin:/usr/bin\nshutdown -h now' > power.sh
# clean audit.log
> /var/log/audit/audit.log
# try to shutdown the workstation from libvirt
virsh shutdown YOUR-WORKSTATION-NAME
# this will fail, because of SELinux. So we must create our own selinux policy
cat  /var/log/audit/audit.log | audit2allow -a -M authorize-acpi
semodule -i authorize-acpi.pp
# try again to shutdown the workstation from libvirt
virsh shutdown YOUR-WORKSTATION-NAME
# it works !!


### default screen resolution is too big. Set it to 1024x768
# edit your workstation vm definition
virsh edit YOUR-WORKSTATION-NAME
# and set to 'vga' the attribute /domain/devices/video/model/@type

# edit /etc/sysconfig/grub and add the option 'vga=792' at the end of the grub command line
sed -i -E 's/^(GRUB_CMDLINE_LINUX=.*)"$/\1 vga=792"/g' /etc/sysconfig/grub
# re-generate the grub.cfg file
grub2-mkconfig -o /boot/grub2/grub.cfg
# after rebooting the system, run 'cat /proc/cmdline' to verify that the option was correctly added

# WARNING : grub2-mkconfig can erase the default kernel to boot on.
# before 'grub2-mkconfig', run 'grub2-editenv list' and keep the result somewhere
# after 'grub2-mkconfig', run 'grub2-editenv list' and, if it has changed, run 'grub2-set-default <previously stored kernel>' (or directly edit /boot/grub2/grubenv)


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
