@echo off

rem ######## license / author
rem # license : GPL
rem # author : Guillaume Cornet

rem ######## variables definition
rem # Get the basedir of the called script
rem #
set homedir=%~p0

rem # Folder where are stored JARs. All JAR will be included recursively
set jarsLibraryPath=%homedir%/common

rem # Melody's Main class
set melodyMainClass=com.wat.melody.cli.Launcher

rem # Default Global Configuration file path
set defaultGlobalConfigurationFilePath=%homedir%/config/melody.properties

rem # Melody's Java Options
set JAVA_OPTS="-Dmelody.default.global.configuration.file=%defaultGlobalConfigurationFilePath%"

rem # Dynamically built the Java ClassPath
rem #
setLocal EnableDelayedExpansion
set classpath=
for /r "%jarsLibraryPath%" %%a IN (*.jar) do set classpath=!classpath!;"%%a"

rem # Move into the Melody homedir, so that the java program can found the default configuration file
rem #
cd %homedir%

rem # Execute Melody
rem #
java -cp %classpath% %JAVA_OPTS% %melodyMainClass% %*

rem # free variables
rem #
set classpath=
set melodyMainClass=
set jarsLibraryPath=

