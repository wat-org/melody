#!/bin/sh

######## license / author
# license : GPL
# author : Guillaume Cornet

## Load JBoss EAP Standalone Service configuration.
#
JBOSS_CONF="$(dirname "$(readlink -f "$0")")/../configuration/jboss-eapd.conf"
[ -r "${JBOSS_CONF}" ] || {
  echo "Cannot read configuration file '${JBOSS_CONF}'." >&2
  exit 1
}

. "${JBOSS_CONF}" || {
  echo "Failed to load configuration file '${JBOSS_CONF}'." >&2
  exit 1
}

## Verify that the script is launched by the jboss user
#
if [ -z "${JBOSS_USER}" ]; then
  echo "Variable JBOSS_USER is not defined or empty. It should contain the JBoss EAP Standalone instance's user owner." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

if [ "$(id -un)" != "${JBOSS_USER}" ]; then
  echo "Should be run as '${JBOSS_USER}'." >&2
  exit 1
fi

## Set defaults.
[ "${JAVA_HOME}x" != "x" ]            && JAVA="${JAVA_HOME}/bin/java"             || JAVA="java"
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-eap-6.0"

######## relocate (this simplifies the java program configuration)
cd "$(dirname "$(readlink -f "$0")")"

######## variables definition
# compute the java classpath
CLASSPATH="lib/update-password/*"

MODULES="org/jboss/logging org/picketbox"
for MODULE in $MODULES; do
  CLASSPATH="${CLASSPATH}:${JBOSS_HOME}/modules/system/layers/base/${MODULE}/main/*"
done

# export the classpath, so that below java command will use it
export CLASSPATH

# the main class
declare mainClass="com.wat.jboss.updatepassword.Main"

# Default Global Configuration file path
declare configurationFile="../configuration/update-password.properties"

# Java Options
JAVA_OPTS="${JAVA_OPTS} -Dcom.wat.jboss.update-password.configuration.file=${configurationFile}"

#################
# Severity of log messages
#
declare FATAL="FATAL"
declare ERROR="ERROR"
declare WARN="WARN "
declare INFO="INFO "
declare DEBUG="DEBUG"
declare TRACE="TRACE"

#################
# Display a message to the standard output, with the following format : date/time severity message
#
myecho() {
  # $1 : same as echo ; is either "-n" or "-e" or "ne" or ""
  # $2 : message
  # $3 : severity (DEBUG, INFO, WARN, ERROR, FATAL)
  echo $1 $(date "+[%F] [%T,%3N]") "[$3]" "$2"
  return 0
}

#################
# Main method
#
main() {
  # Verify java cmd accessibility
  which "${JAVA}" 1>/dev/null 2>&1 || {
    local res=$?
    myecho -e "'${JAVA}' not found (code '$res'). Exiting" "${ERROR}"
    return $res
  }

  # Execute
  "${JAVA}" ${JAVA_OPTS} "${mainClass}" "$@" || {
    local res=$?
    [ $res = 130 ] && myecho -e "Interrupted (code '$res'). Exiting" "${WARNING}" || myecho -e "Error (code '$res'). Exiting" "${ERROR}"
    return $res
  }

  # No error, return 0
  return 0
}

#################
# fire !
main "$@"
exit $?