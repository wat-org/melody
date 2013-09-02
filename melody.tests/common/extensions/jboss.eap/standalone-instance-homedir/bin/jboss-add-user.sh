#!/bin/sh
#
# JBoss EAP Standalone 'add-user' wrapper

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
[ -z "${JBOSS_ADD_USER}" ]            && JBOSS_ADD_USER="${JBOSS_HOME}/bin/add-user.sh"

## Compute some variables
ADD_USER_CMD="LANG=\"${LANG}\" \
              JAVA_HOME=\"${JAVA_HOME}\" \
              JAVA_OPTS=\"$JAVA_OPTS -Djboss.server.config.user.dir=$JBOSS_BASE_DIR/configuration -Djboss.domain.config.user.dir=/noway\" \
              \"${JBOSS_ADD_USER}\""

###
### validate some stuff
[ -e "${JBOSS_ADD_USER}" ] || {
  echo "File '${JBOSS_ADD_USER}' doesn't exists."
  echo "The variable \$JBOSS_ADD_USER must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss Engine Add-User script." >&2
  exit 1 
}

[ -x "${JBOSS_ADD_USER}" ] || {
  echo "File '${JBOSS_ADD_USER}' is not executable."
  echo "The variable \$JBOSS_ADD_USER must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss Engine Add-User script." >&2
  exit 1 
}

###
### main
eval "${ADD_USER_CMD}" '"$@"'
exit $?
