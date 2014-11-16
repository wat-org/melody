#!/bin/sh
#
# JBoss DataGrid Vault wrapper

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
  echo "Variable \$JBOSS_BASE_DIR is not defined or empty. It should contain the JBoss DataGrid instance's base dir." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

if [ -z "${JDG_USER}" ]; then
  echo "Variable \$JDG_USER is not defined or empty. It should contain the JBoss DataGrid instance's user owner." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

## Set defaults.
# no need for default value for ${JBOSS_MODULEPATH}
[ -z "${UMASK}" ]                     && UMASK="0077"
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-datagrid-server-6"
[ -z "${JBOSS_VAULT}" ]               && JBOSS_VAULT="${JBOSS_HOME}/bin/vault.sh"
[ -z "${VAULT_ENC_DIR}" ]             && VAULT_ENC_DIR="${JBOSS_BASE_DIR}/configuration/vault/secret/"
[ -z "${VAULT_KEYSTORE}" ]            && VAULT_KEYSTORE="${JBOSS_BASE_DIR}/configuration/vault/vault.keystore"
[ -z "${VAULT_KEYSTORE_PASSWORD}" ]   && VAULT_KEYSTORE_PASSWORD="changeit"
[ -z "${VAULT_KEYSTORE_ALIAS}" ]      && VAULT_KEYSTORE_ALIAS="vault"
[ -z "${VAULT_ITERATION_COUNT}" ]     && VAULT_ITERATION_COUNT="69"
[ -z "${VAULT_SALT}" ]                && VAULT_SALT="supasalt"

## Compute some variables
VAULT_CMD="LANG=\"${LANG}\" \
          JAVA_HOME=\"${JAVA_HOME}\" \
          JAVA_OPTS=\"${JAVA_OPTS} -Djava.io.tmpdir=${JBOSS_BASE_DIR}/tmp/\" \
          JBOSS_MODULEPATH=\"${JBOSS_MODULEPATH}\" \
          \"${JBOSS_VAULT}\" \
          --enc-dir \"${VAULT_ENC_DIR}\" \
          --keystore \"${VAULT_KEYSTORE}\" \
          --keystore-password \"${VAULT_KEYSTORE_PASSWORD}\" \
          --alias \"${VAULT_KEYSTORE_ALIAS}\" \
          --iteration \"${VAULT_ITERATION_COUNT}\" \
          --salt \"${VAULT_SALT}\""

## command wrapper
CMD_PREFIX="eval"
[ "$(id -g)" = "0" ] && CMD_PREFIX="su - ${JDG_USER} -c"

###
### validate some stuff
[ -e "${JBOSS_VAULT}" ] || {
  echo "File '${JBOSS_VAULT}' doesn't exists."
  echo "The variable \$JBOSS_VAULT must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid Engine Vault script." >&2
  exit 1
}

[ -x "${JBOSS_VAULT}" ] || {
  echo "File '${JBOSS_VAULT}' is not executable."
  echo "The variable \$JBOSS_VAULT must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid Engine Vault script." >&2
  exit 1
}

###
### main
PARAM=$@
${CMD_PREFIX} " umask ${UMASK}; ${VAULT_CMD} $PARAM "
exit $?
