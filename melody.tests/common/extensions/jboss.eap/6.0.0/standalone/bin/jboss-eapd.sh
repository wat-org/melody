#!/bin/sh
#
# JBoss EAP Standalone control script
#
# chkconfig: - 80 20
# description: JBoss EAP Standalone

## Source function library.
. /etc/init.d/functions

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

## Validate some stuff.
if [ -z "${JBOSS_BASE_DIR}" ]; then
  echo "Variable \$JBOSS_BASE_DIR is not defined or empty. It should contain the JBoss EAP Standalone instance's base dir." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

if [ -z "${JBOSS_USER}" ]; then
  echo "Variable \$JBOSS_USER is not defined or empty. It should contain the JBoss EAP Standalone instance's user owner." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

if [ "$(id -un)" != "${JBOSS_USER}" -a "$(id -g)" != "0" ]; then
  echo "Should be run as 'root' or '${JBOSS_USER}'." >&2
  exit 1
fi

if [ $# -gt 2 ]; then
  echo "Usage: $0 { [re]start[-async[-tail]] [--admin-only] | stop | status | tdump | hdump }"
  exit 1
fi

if [[ $# = 2 && "$1" =~ ^(re)?start(-async(-tail)?)?$ && "$2" != "--admin-only" ]]; then
  echo "Usage: $0 { [re]start[-async[-tail]] [--admin-only] | stop | status | tdump | hdump }"
  exit 1
fi

if [[ $# = 2 && ! "$1" =~ ^(re)?start(-async(-tail)?)?$ ]]; then
  echo "Usage: $0 { [re]start[-async[-tail]] [--admin-only] | stop | status | tdump | hdump }"
  exit 1
fi


## Set defaults.
# no need for default value for ${JBOSS_MODULEPATH}
[ "${JAVA_HOME}x" != "x" ]            && JAVA="${JAVA_HOME}/bin/java"             || JAVA="java"
[ -x "/usr/sbin/ss" ]                 && TSCMD="/usr/sbin/ss -punta"              || TSCMD="/bin/netstat -punta"
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-eap-6.0"
[ -z "${JBOSS_CONSOLE_LOG}" ]         && JBOSS_CONSOLE_LOG="/var/log/jboss-eap/console.log"
[ -z "${STARTUP_WAIT}" ]              && STARTUP_WAIT=30
[ -z "${SHUTDOWN_WAIT}" ]             && SHUTDOWN_WAIT=30
[ -z "${JBOSS_START_ADMIN_ONLY}" ]    && JBOSS_START_ADMIN_ONLY="false"
[ -z "${PURGE_TMP_DIR_AT_STARTUP}" ]  && PURGE_TMP_DIR_AT_STARTUP="true"
[ -z "${PURGE_DATA_DIR_AT_STARTUP}" ] && PURGE_DATA_DIR_AT_STARTUP="false"
[ -z "${RUN_CONF}" ]                  && RUN_CONF="${JBOSS_BASE_DIR}/configuration/standalone.conf"
[ -z "${JBOSS_CONFIG}" ]              && JBOSS_CONFIG="standalone.xml"
[ -z "${JBOSS_CLI_WRAPPER}" ]         && JBOSS_CLI_WRAPPER="${JBOSS_BASE_DIR}/bin/jboss-cli.sh"
[ -z "${JBOSS_HEAP_DUMP_PATH}" ]      && JBOSS_HEAP_DUMP_PATH="${JBOSS_BASE_DIR}/log"
[ -z "${LISTEN_IP}" ]                 && LISTEN_IP="127.0.0.1"
[ -z "${MGMT_IP}" ]                   && MGMT_IP="127.0.0.1"
[ -z "${MGMT_NATIVE_PORT}" ]          && MGMT_NATIVE_PORT="9999"
[ -z "${REMOTE_DEBUG_PORT}" ]         && REMOTE_DEBUG_PORT="8787"
[ -z "${PORT_OFFSET}" ]               && PORT_OFFSET="0"
[ -z "${SERVER_NAME}" ]               && SERVER_NAME="default-server-name"
[ -z "${SERVICE_NAME}" ]              && SERVICE_NAME="jboss-eap[${SERVER_NAME}]"

## Compute some variables
MGMT_ADDR="${MGMT_IP}:$((MGMT_NATIVE_PORT+PORT_OFFSET))"
[ "$2" = "--admin-only" ] && ADMIN_ONLY_FLAG="--admin-only"
[ "${JBOSS_START_ADMIN_ONLY}" = "true" ] && ADMIN_ONLY_FLAG="--admin-only"
JBOSS_SCRIPT="LANG=\"${LANG}\" \
              JAVA_HOME=\"${JAVA_HOME}\" \
              JBOSS_HOME=\"${JBOSS_HOME}\" \
              JBOSS_BASE_DIR=\"${JBOSS_BASE_DIR}\" \
              JBOSS_MODULEPATH=\"${JBOSS_MODULEPATH}\" \
              LISTEN_IP=\"${LISTEN_IP}\" \
              REMOTE_DEBUG_PORT=\"$((REMOTE_DEBUG_PORT+PORT_OFFSET))\" \
              RUN_CONF=\"${RUN_CONF}\" \
              \"${JBOSS_HOME}/bin/standalone.sh\" \
              -b ${LISTEN_IP} \
              -bmanagement ${MGMT_IP} \
              -Djboss.server.name=${SERVER_NAME} \
              -Djboss.socket.binding.port-offset=${PORT_OFFSET} \
              -Djboss.management.native.port=${MGMT_NATIVE_PORT} \
              -c \"${JBOSS_CONFIG}\" \
              ${ADMIN_ONLY_FLAG} \
              </dev/null >\"${JBOSS_CONSOLE_LOG}\" 2>&1 \
              &"
              # doing this, this background call uses its own stdin, stdout and stderr,
              # which are not the same the caller uses.

## command wrapper
CMD_PREFIX="eval"
[ "$(id -g)" = "0" ] && CMD_PREFIX="su - ${JBOSS_USER} -c"


###
### will sleep more if the machine is highly loaded
__sleep() {
  local seconds=$1
  for (( ; seconds>-1 ; seconds--)); do
    dd if=/dev/urandom of=/dev/null bs=10M count=1 1>/dev/null 2>&1
    "${JAVA}" -version 1>/dev/null 2>&1
  done
}


###
### get the pid of the jboss server
get_pid() {
  echo $(\pgrep -u ${JBOSS_USER} -f "(jboss.server.base.dir=${JBOSS_BASE_DIR//[+*.^$\[\]\{\}]/.}\s).*(jboss.server.name=${SERVER_NAME//[+*.^$\[\]\{\}]/.}\s)|\$2.*\$1")
}


###
### diplay error log
display_error_log() {
  echo "An unexpected problem may have cause this situation. Please read log bellow to find more detail about the problem."
  cat "${JBOSS_CONSOLE_LOG}"
}


###
### validate the process exists
validate_process() {
  # is the process running ?
  [ "x$(get_pid)" != "x" ] || {
    failure
    echo >&2
    echo "------------------------------------" >&2
    echo "Process ${SERVICE_NAME} is not running ..." >&2
    echo "------------------------------------" >&2
    echo >&2
    display_error_log >&2
    return 1
  }

  return 0
}

###
### validate the server state
validate_server_state() {
  # is the management native port listening ?
  local starttime=$(date "+%s")
  local listening=0
  while [ ${listening} = 0 -a $((STARTUP_WAIT-$(($(date "+%s")-starttime)))) -gt 0 ]; do
    ${TSCMD} | \grep -E " ${MGMT_ADDR//[+*.^$\[\]\{\}]/.} " 1>/dev/null && { listening=1; break; }
    sleep 1
    # maybe the process is no more active, which means there was an error
    validate_process || return $?
  done
  [ ${listening} = 1 ] || {
    warning
    echo
    echo "------------------------------------"
    echo "${SERVICE_NAME} CLI is still not listening after ${STARTUP_WAIT}s ..."
    echo
    echo "TIP :"
    echo "  The startup timeout may be too short or the CLI configuration contains error(s)."
    echo "  You should take a look at the server log."
    echo "  If the server log show some problems, you should correct them and restart the server."
    echo "  If the server log don't show any problem, you should increase the startup timeout."
    echo "------------------------------------"
    echo
    return 255 # WARN : timeout may be to short
  }

  # is the connection to the cli possible ?
  local operational=0
  while [ ${operational} = 0 -a $((STARTUP_WAIT-$(($(date "+%s")-starttime)))) -gt 0 ]; do
    [ -w "${JBOSS_BASE_DIR}/tmp/auth/" ] && "${JBOSS_CLI_WRAPPER}" --command="read-attribute server-state" 1>/dev/null 2>&1 && { operational=1; break; }
    sleep 1
    # maybe the process is no more active, which means there was an error
    validate_process || return $?
  done
  [ ${operational} = 1 ] || {
    failure
    echo >&2
    echo "------------------------------------" >&2
    echo "${SERVICE_NAME} CLI is still not operational after ${STARTUP_WAIT}s ..." >&2
    echo >&2
    echo "WHAT TO DO :" >&2
    echo "  The startup timeout may be too short or the CLI configuration contains error(s)." >&2
    echo "  You should take a look at the server log." >&2
    echo "  If the server log show some problems, you should correct them and restart the server." >&2
    echo "  If the server log don't show any problem, you should increase the startup timeout." >&2
    echo "------------------------------------" >&2
    echo >&2
    display_error_log >&2
    return 1
  }

  # loop while the server-state equal is equal to starting
  local started=0
  while [ ${started} = 0 -a $((STARTUP_WAIT-$(($(date "+%s")-starttime)))) -gt 0 ]; do
    res=$("${JBOSS_CLI_WRAPPER}" --command="read-attribute server-state" 2>&1)
    # if the cli command succeed 
    [ $? = 0 ] && {
      # if status is different than 'starting' => break the loop
      echo $res | \grep starting 1>/dev/null || { started=1; break; }
    }
    sleep 1
    # maybe the process is no more active, which means there was an error
    validate_process || return $?
  done
  [ ${started} = 1 ] || {
    warning
    echo
    echo "------------------------------------"
    echo "${SERVICE_NAME} is still starting after ${STARTUP_WAIT}s ..."
    echo
    echo "TIP :"
    echo "  The startup timeout may be too short or the server encounters problems which slow down its startup sequence."
    echo "  You should take a look at the server log."
    echo "  If the server log show some problems, you should correct them and restart the server."
    echo "  If the server log don't show any problem, you should increase the startup timeout."
    echo "------------------------------------"
    echo
    return 255 # WARN : timeout may be to short
  }

  # does the log contains error ?
  # JBREM000200: Remote connection failed": discarded because it should be a WARN
  # JBREM000201: Received invalid message on Remoting connection": discarded because it should be a WARN
  # JBAS014612: discard CLI + osgi related errors: 'Operation ("add") failed ... osgi is already registered': discarded because it is a bug in startup sequence, which lead to duplicate osgi loading
  # JBAS014784: discard osgi related errors: 'Failed executing subsystem osgi boot operations': discarded for the same reason
  \cat "${JBOSS_CONSOLE_LOG}"   | \
  \grep -v "ERROR.*JBREM000200" | \
  \grep -v "ERROR.*JBREM000201" | \
  \grep -v "ERROR.*JBAS014612.*osgi.*already.*registered" |  \
  \grep -v "ERROR.*JBAS014784" |  \
  \grep -iE "[ \[](ERROR|FATAL)" 1>/dev/null && {
    warning
    echo
    echo "------------------------------------"
    echo "${SERVICE_NAME} started with error ..."
    echo
    echo "WHAT TO DO :"
    echo "  Take a look at the log bellow. Some errors may be important, some less... Just do what you have to do."
    echo "  Once the problem solved, restart the sever."
    echo "------------------------------------"
    echo
    display_error_log
    return 255 # WARN : error encountered during startup sequence
  }

  return 0
}

###
### start the jboss server if not running
### return 1 if the server is already started
ensure_started() {
  echo -n "Starting ${SERVICE_NAME}"
  if [ "x$(get_pid)" != "x" ]; then
    echo
    echo -n "already running"
    success
    echo
    return 1
  fi

  [ "${PURGE_TMP_DIR_AT_STARTUP}" = "true" -a -d "${JBOSS_BASE_DIR}/tmp/" ] && rm -rf "${JBOSS_BASE_DIR}/tmp/"* 1>/dev/null
  [ "${PURGE_DATA_DIR_AT_STARTUP}" = "true" -a -d "${JBOSS_BASE_DIR}/data/" ] && rm -rf "${JBOSS_BASE_DIR}/data/"* 1>/dev/null

  ${CMD_PREFIX} "mkdir -p \"$(dirname "${JBOSS_CONSOLE_LOG}")\""
  ${CMD_PREFIX} "${JBOSS_SCRIPT}"

  # sleep a little
  __sleep 3

  return 0
}


###
### start the JBoss server and wait til it reach the started state
start() {
  ensure_started || return 0
  validate_process || return $?
  validate_server_state || return $?
  success
  echo
  return 0
}

###
### start the JBoss server and exit immediately
start_async() {
  ensure_started || return 0
  validate_process || return $?
  success
  echo
  return 0
}

###
### start the JBoss server and tail server log
start_async_tail() {
  ensure_started || return 0
  validate_process || return $?
  success
  echo
  tail -F "$(dirname "${JBOSS_CONSOLE_LOG}")/server.log"
  return 0
}

###
### stop the jboss server
stop() {
  echo -n "Stopping ${SERVICE_NAME}"

  local jboss_pid=$(get_pid)
  if [ "x${jboss_pid}" = "x" ]; then
    echo
    echo -n "not running"
    success
    echo
    return 0
  fi

  local timeout=${SHUTDOWN_WAIT}
  # Try issuing SIGTERM
  kill -15 ${jboss_pid}
  while [ "x$(get_pid)" != "x" -a ${timeout} -gt 0 ]; do
    timeout=$((timeout-1))
    sleep 1
  done

  # Send kill -9
  if [ ${timeout} = 0 ]; then
    echo
    echo "Soft kill timeout (${SHUTDOWN_WAIT}s) elapsed."
    echo -n "Send kill -9."
    kill -9 ${jboss_pid} 2>/dev/null
  fi

  success
  echo
  return 0
}

###
### display the state of the jboss server
status() {
  local jboss_pid=$(get_pid)
  if [ "x${jboss_pid}" != "x" ]; then
    echo "${SERVICE_NAME} is running (pid ${jboss_pid})."
    return 0
  fi
  echo "${SERVICE_NAME} is not running."
  return 3
}

###
### generate a thread dump in "${JBOSS_CONSOLE_LOG}"
thread_dump() {
  local jboss_pid=$(get_pid)
  if [ "x${jboss_pid}" != "x" ]; then
    kill -3 ${jboss_pid}
    echo "A thread dump for ${SERVICE_NAME} have been generated in '${JBOSS_CONSOLE_LOG}'."
    return 0
  fi
  echo "Cannot generate a thread dump for ${SERVICE_NAME} because it is not running."
  return 3
}

###
### generate a thread dump in "${JBOSS_BASE_DIR}/log/heap_dump.hprof"
heap_dump() {
  local jboss_pid=$(get_pid)
  if [ "x${jboss_pid}" != "x" ]; then
    local HEAP_DUMP_PATH="${JBOSS_HEAP_DUMP_PATH}/heap_dump.$(date "+%Y%m%d_%H%M%S_%3N").hprof"
    echo "Generating a heap dump for ${SERVICE_NAME}. Please wait, this can take a long time..."
    ${JBOSS_BASE_DIR}/bin/twiddle.sh invoke "com.sun.management:type=HotSpotDiagnostic" dumpHeap "${HEAP_DUMP_PATH}" true 1>/dev/null
    local res=$?
    if [ "$res" = "0" ]; then
      echo "A heap dump for ${SERVICE_NAME} have been generated in '${HEAP_DUMP_PATH}'."
      return 0
    else
      echo "Fail to generate a heap dump for ${SERVICE_NAME} in '${HEAP_DUMP_PATH}'."
      return 1
    fi
  fi
  echo "Cannot generate a heap dump for ${SERVICE_NAME} because it is not running."
  return 3
}

###
### main
case "$1" in
  start)
      start
      exit $?
      ;;
  stop)
      stop
      exit $?
      ;;
  restart)
      stop || exit $?
      __sleep 2
      start
      exit $?
      ;;
  start-async)
      start_async
      exit $?
      ;;
  restart-async)
      stop || exit $?
      __sleep 2
      start_async
      exit $?
      ;;
  start-async-tail)
      start_async_tail
      exit $?
      ;;
  restart-async-tail)
      stop || exit $?
      __sleep 2
      start_async_tail
      exit $?
      ;;
  status)
      status
      exit $?
      ;;
  tdump)
      thread_dump
      exit $?
      ;;
  hdump)
      heap_dump
      exit $?
      ;;
  *)
      ## If no parameters are given, print which are available.
      echo "Usage: $0 { [re]start[-async[-tail]] [--admin-only] | stop | status | tdump | hdump }"
      exit 1
      ;;
esac

