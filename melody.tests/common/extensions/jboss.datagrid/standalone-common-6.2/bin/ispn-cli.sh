#!/bin/sh
#
# JBoss DataGrid ISPN CLI wrapper

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
[ -z "${ISPN_CLI}" ]                  && ISPN_CLI="${JBOSS_HOME}/bin/ispn-cli.sh"
[ -z "${MGMT_IP}" ]                   && MGMT_IP="127.0.0.1"
[ -z "${MGMT_NATIVE_PORT}" ]          && MGMT_NATIVE_PORT="9999"
[ -z "${PORT_OFFSET}" ]               && PORT_OFFSET="0"

## Compute some variables
MGMT_PORT="$((MGMT_NATIVE_PORT+PORT_OFFSET))"
CLI_CMD="LANG=\"${LANG}\" \
         JAVA_HOME=\"${JAVA_HOME}\" \
         JAVA_OPTS=\"${JAVA_OPTS} -Djava.io.tmpdir=${JBOSS_BASE_DIR}/tmp/\" \
         JBOSS_MODULEPATH=\"${JBOSS_MODULEPATH}\" \
         \"${ISPN_CLI}\" \
         -c \"${MGMT_IP}\" \"${MGMT_PORT}\""

###
### validate some stuff
[ -e "${ISPN_CLI}" ] || {
  echo "File '${ISPN_CLI}' doesn't exists."
  echo "The variable ISPN_CLI must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid ISPN CLI script." >&2
  exit 1
}

[ -x "${ISPN_CLI}" ] || {
  echo "File '${ISPN_CLI}' is not executable."
  echo "The variable ISPN_CLI must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid Engine ISPN CLI script." >&2
  exit 1
}

###
### main
eval "${CLI_CMD}" '"$@"'
exit $?
