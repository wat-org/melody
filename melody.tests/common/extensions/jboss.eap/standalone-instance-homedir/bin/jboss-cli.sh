#!/bin/sh
#
# JBoss EAP Standalone CLI wrapper

## Load JBoss EAP Standalone Service configuration.
JBOSS_CONF="$(dirname "$(readlink -f "$0")")/../configuration/jboss-eapd.conf"
[ -r "${JBOSS_CONF}" ] || {
  echo "Cannot read configuration file '${JBOSS_CONF}'." >&2
  exit 1 
}

. "${JBOSS_CONF}" || {
  echo "Failed to load configuration file '${JBOSS_CONF}'." >&2
  exit 1 
}

## Set defaults.
# no need for default value for ${JBOSS_MODULEPATH}
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-eap-6.0"
[ -z "${JBOSS_CLI}" ]                 && JBOSS_CLI="${JBOSS_HOME}/bin/jboss-cli.sh"
[ -z "${MGMT_IP}" ]                   && MGMT_IP="127.0.0.1"
[ -z "${MGMT_NATIVE_PORT}" ]          && MGMT_NATIVE_PORT="9999"
[ -z "${PORT_OFFSET}" ]               && PORT_OFFSET="0"

## Compute some variables
MGMT_ADDR="${MGMT_IP}:$((MGMT_NATIVE_PORT+PORT_OFFSET))"
CLI_CMD="LANG=\"${LANG}\" \
         JAVA_HOME=\"${JAVA_HOME}\" \
         \"${JBOSS_CLI}\" \
         -c --controller=\"${MGMT_ADDR}\""

###
### validate some stuff
[ -e "${JBOSS_CLI}" ] || {
  echo "File '${JBOSS_CLI}' doesn't exists."
  echo "The variable JBOSS_CLI must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss Engine CLI script." >&2
  exit 1 
}

[ -x "${JBOSS_CLI}" ] || {
  echo "File '${JBOSS_CLI}' is not executable."
  echo "The variable JBOSS_CLI must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss Engine CLI script." >&2
  exit 1 
}

###
### main
eval "${CLI_CMD}" '"$@"'
exit $?