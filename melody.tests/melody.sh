#!/bin/sh

######## license / author
# license : GPL
# author : Guillaume Cornet

######## variables definition
# Get the basedir of the called script
homedir="$(dirname $0)"
# Folder where are stored JARs. All JAR will be included recursively
jarsLibraryPath="${homedir}/common"
# Default Global Configuration file path
defaultGlobalConfigurationFilePath="${homedir}/config/melody.properties"
# Melody's Main class
MelodyMainClass="com.wat.melody.cli.Launcher"

#################
# Severity of log messages 
FATAL="FATAL"
ERROR="ERROR"
WARN="WARN"
INFO="INFO"
DEBUG="DEBUG"
TRACE="TRACE"

#################
# Display a message to the standard output, with the following format : date/time severity message
myecho() {
# $1 : same as echo ; is either "-n" or "-e" or "ne" or ""
# $2 : message
# $3 : severity (DEBUG, INFO, WARN, ERROR, FATAL) 
	echo $1 $(date "+%F %T.%N" | cut -b1-23) "$3" "$2"
	return 0
}

#################
# Concatenate in a single string the name of all jar files found in the given folder and sub-folders.
# The returned string is the Java ClassPath
addJarToClasspath() {
# $1 : the folder to find JAR inside
	local basedir="$1"
	local classpath=""
	
	# If the given folder doesn't exists => return an error
	[ ! -d "${basedir}" ] && return 1
	
	# For each item in the given folder
	for item in $(ls "${basedir}"); do
		local toAdd=""
		# If the item is a file, which name ended by ".jar" => add it to the classpath
		[ -f "${basedir}/${item}" ] && [ $(echo "${item}" | grep -E "[.]jar$" | wc -l) = 1 ] && toAdd="${basedir}/${item}"
		# If the item is a subfolder => recursively build classpath, based on the content of the subfolder
		[ -d "${basedir}/${item}" ] && toAdd=$(addJarToClasspath "${basedir}/${item}")
		# If no jar where found => looping
		[ -z "${toAdd}" ] && continue
		# Add jar found to the classpath
		[ -z "${classpath}" ] && classpath="${toAdd}" || classpath="${classpath}:${toAdd}"
	done
	# Display the Java ClassPath
	echo "${classpath}"
	return 0
}

#################
# Main method
main() {
	# Verify java cmd accessibility
	which java 1>/dev/null 2>&1 || {
		local res=$?
		myecho -e "'java' not found in path (code '$res'). Exiting" "${ERROR}"
		return $res
	}

	# Dynamically built the Java ClassPath
	local classpath="$(addJarToClasspath "${jarsLibraryPath}")" || {
		local res=$?
		myecho -e "Cannot dynamically build Java ClassPath (code '$res'). Exiting" "${ERROR}"
		return $res
	}

	# Execute Melody; pass the path to the default Global Configuration File
	java -cp "${classpath}" "${MelodyMainClass}" "${defaultGlobalConfigurationFilePath}" "$@" || {
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
