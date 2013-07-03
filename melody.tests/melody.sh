#!/bin/sh

######## license / author
# license : GPL
# author : Guillaume Cornet

######## variables definition
# Get the basedir of the called script
#
declare homedir="$(dirname $0)"

# Folder where are stored JARs. All JAR will be included
export CLASSPATH="${homedir}/common/lib/*"

# Melody's Main class
declare melodyMainClass="com.wat.melody.cli.Launcher"

# Default Global Configuration file path
declare defaultGlobalConfigurationFilePath="${homedir}/config/melody.properties"

# Melody's Java Options
declare JAVA_OPTS="-Dmelody.default.global.configuration.file=${defaultGlobalConfigurationFilePath}"

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
  which java 1>/dev/null 2>&1 || {
    local res=$?
    myecho -e "'java' not found in path (code '$res'). Exiting" "${ERROR}"
    return $res
  }
  
  # Execute Melody
  java ${JAVA_OPTS} "${melodyMainClass}" "$@" || {
    local res=$?
    [ $res = 130 ] && myecho -e "Melody was interrupted (code '$res'). Exiting" "${WARNING}" || myecho -e "Melody return an error (code '$res'). Exiting" "${ERROR}"
    return $res
  }

  # No error, return 0
  return 0
}

#################
# fire !
main "$@"
exit $?
