#!/bin/sh
#
# JBoss DataGrid CLI wrapper

## Load JBoss DataGrid Service configuration.
JBOSS_CONF="$(dirname "$(readlink -f "$0")")/../configuration/jboss-jdgd.conf"
[ -r "${JBOSS_CONF}" ] || {
  echo "Cannot read configuration file '${JBOSS_CONF}'." >&2
  exit 1
}

. "${JBOSS_CONF}" || {
  echo "Failed to load configuration file '${JBOSS_CONF}'." >&2
  exit 1
}

if [ -z "${JBOSS_BASE_DIR}" ]; then
  echo "Variable JBOSS_BASE_DIR is not defined or empty. It should contain the JBoss DataGrid instance's base dir." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

## Set defaults.
# no need for default value for ${JBOSS_MODULEPATH}
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-datagrid-server-6"
[ -z "${JBOSS_CLI}" ]                 && JBOSS_CLI="${JBOSS_HOME}/bin/jboss-cli.sh"
[ -z "${MGMT_IP}" ]                   && MGMT_IP="127.0.0.1"
[ -z "${MGMT_NATIVE_PORT}" ]          && MGMT_NATIVE_PORT="9999"
[ -z "${PORT_OFFSET}" ]               && PORT_OFFSET="0"

## Compute some variables
MGMT_ADDR="${MGMT_IP}:$((MGMT_NATIVE_PORT+PORT_OFFSET))"
CLI_CMD="LANG=\"${LANG}\" \
         JAVA_HOME=\"${JAVA_HOME}\" \
         JAVA_OPTS=\"-Djava.io.tmpdir=${JBOSS_BASE_DIR}/tmp/\" \
         \"${JBOSS_CLI}\" \
         -c --controller=\"${MGMT_ADDR}\""

###
### validate some stuff
[ -e "${JBOSS_CLI}" ] || {
  echo "File '${JBOSS_CLI}' doesn't exists."
  echo "The variable JBOSS_CLI must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid Engine CLI script." >&2
  exit 1
}

[ -x "${JBOSS_CLI}" ] || {
  echo "File '${JBOSS_CLI}' is not executable."
  echo "The variable JBOSS_CLI must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid Engine CLI script." >&2
  exit 1
}

###
### main
eval "${CLI_CMD}" '"$@"'
exit $?