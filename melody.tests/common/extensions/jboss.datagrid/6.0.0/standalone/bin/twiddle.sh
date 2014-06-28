#!/bin/sh

######## license / author
# license : GPL
# author : Guillaume Cornet

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

## Validate some stuff.
if [ -z "${JBOSS_BASE_DIR}" ]; then
  echo "Variable JBOSS_BASE_DIR is not defined or empty. It should contain the JBoss DataGrid instance's base dir." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

if [ -z "${JDG_USER}" ]; then
  echo "Variable JDG_USER is not defined or empty. It should contain the JBoss DataGrid instance's user owner." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

if [ "$(id -un)" != "${JDG_USER}" -a "$(id -g)" != "0" ]; then
  echo "Should be run as 'root' or '${JDG_USER}'." >&2
  exit 1
fi

## Set defaults.
[ "${JAVA_HOME}x" != "x" ]            && JAVA="${JAVA_HOME}/bin/java"             || JAVA="java"
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-datagrid-server-6.0"
[ -z "${MGMT_IP}" ]                   && MGMT_IP="127.0.0.1"
[ -z "${MGMT_NATIVE_PORT}" ]          && MGMT_NATIVE_PORT="9999"
[ -z "${PORT_OFFSET}" ]               && PORT_OFFSET="0"

## Compute some variables
# compute the management address
MGMT_ADDR="${MGMT_IP}:$((MGMT_NATIVE_PORT+PORT_OFFSET))"

## command wrapper
CMD_PREFIX="eval"
[ "$(id -g)" = "0" ] && CMD_PREFIX="su - ${JDG_USER} -c"

# Java Options
JAVA_OPTS="${JAVA_OPTS} -Dprogram.name=\"twiddle[${SERVER_NAME}]\""

# Execute the JVM
PARAM=$@
${CMD_PREFIX} " \"${JAVA}\" ${JAVA_OPTS} \
  \"-Dlogging.configuration=file:${JBOSS_BASE_DIR}/configuration/twiddle-logging.properties\" \"-Djboss.twiddle.log.file=${JBOSS_BASE_DIR}/log/twiddle.log\" \
  -jar \"${JBOSS_HOME}/jboss-modules.jar\" -mp \"${JBOSS_MODULEPATH}\" com.wat.jboss.tools.twiddle \
  -s service:jmx:remoting-jmx://${MGMT_ADDR} ${PARAM} "

