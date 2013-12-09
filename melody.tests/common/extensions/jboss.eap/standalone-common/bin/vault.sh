#!/bin/sh
#
# JBoss EAP Standalone Vault wrapper

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

if [ -z "${JBOSS_BASE_DIR}" ]; then
  echo "Variable JBOSS_BASE_DIR is not defined or empty. It should contain the JBoss EAP Standalone instance's base dir." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

## Set defaults.
# no need for default value for ${JBOSS_MODULEPATH}
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-eap-6.0"
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
         JAVA_OPTS=\"-Djava.io.tmpdir=${JBOSS_BASE_DIR}/tmp/\" \
         \"${JBOSS_VAULT}\" \
         --enc-dir \"${VAULT_ENC_DIR}\" \
         --keystore \"${VAULT_KEYSTORE}\" \
         --keystore-password \"${VAULT_KEYSTORE_PASSWORD}\" \
         --alias \"${VAULT_KEYSTORE_ALIAS}\" \
         --iteration \"${VAULT_ITERATION_COUNT}\" \
         --salt \"${VAULT_SALT}\""

###
### validate some stuff
[ -e "${JBOSS_VAULT}" ] || {
  echo "File '${JBOSS_VAULT}' doesn't exists."
  echo "The variable JBOSS_VAULT must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss Engine Vault script." >&2
  exit 1
}

[ -x "${JBOSS_VAULT}" ] || {
  echo "File '${JBOSS_VAULT}' is not executable."
  echo "The variable JBOSS_VAULT must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss Engine Vault script." >&2
  exit 1
}

###
### main
eval "${VAULT_CMD}" '"$@"'
exit $?