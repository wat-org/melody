#!/bin/sh
#
# Twiddle for JBoss EAP Standalone
#
# If ran as root or the owner, no need to pass credential
# In any other case, need to pass credentials in the command line (-u user / -p pass)

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
[ "${JAVA_HOME}x" != "x" ]            && JAVA="${JAVA_HOME}/bin/java"             || JAVA="java"
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-eap-6.0"
[ -z "${MGMT_IP}" ]                   && MGMT_IP="127.0.0.1"
[ -z "${MGMT_NATIVE_PORT}" ]          && MGMT_NATIVE_PORT="9999"
[ -z "${PORT_OFFSET}" ]               && PORT_OFFSET="0"

## Compute some variables
# compute the management address
MGMT_ADDR="${MGMT_IP}:$((MGMT_NATIVE_PORT+PORT_OFFSET))"

# move into this script parent's basedir
cd "$(dirname "$(readlink -f "$0")")/.."

# compute the java classpath
CLASSPATH="bin/lib/twiddle/*"

MODULES="org/jboss/remoting-jmx org/jboss/remoting3 org/jboss/logging org/jboss/xnio org/jboss/xnio/nio org/jboss/sasl org/jboss/marshalling org/jboss/marshalling/river org/jboss/as/cli org/jboss/staxmapper org/jboss/as/protocol org/jboss/dmr org/jboss/as/controller-client org/jboss/threads"
for MODULE in ${MODULES}; do
  CLASSPATH="${CLASSPATH}:${JBOSS_HOME}/modules/system/layers/base/${MODULE}/main/*"
done

# export the classpath, so that the below java command will use it
export CLASSPATH

# the main class
declare mainClass="org.jboss.console.twiddle.Twiddle"

# Java Options
JAVA_OPTS="${JAVA_OPTS} -Dprogram.name=\"twiddle[${SERVER_NAME}]\""

# Execute the JVM
exec "${JAVA}" ${JAVA_OPTS} \
  ${mainClass} -s service:jmx:remoting-jmx://${MGMT_ADDR} "$@"