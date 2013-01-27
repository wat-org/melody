@echo off

rem ######## license / author
rem # license : GPL
rem # author : Guillaume Cornet

rem ######## variables definition
rem # Get the basedir of the called script
set homedir=%~p0
rem # Folder where are stored JARs. All JAR will be included recursively
set jarsLibraryPath=%homedir%/common
rem # Default Global Configuration file path
set defaultGlobalConfigurationFilePath="%homedir%/config/melody.properties"
rem # Melody's Main class
set MelodyMainClass=com.wat.melody.cli.Launcher

rem # Dynamically built the Java ClassPath
setLocal EnableDelayedExpansion
set classpath=
for /r "%jarsLibraryPath%" %%a IN (*.jar) do set classpath=!classpath!;"%%a"

rem # Execute Melody; pass the path to the default Global Configuration File
java -cp %classpath% %MelodyMainClass% %defaultGlobalConfigurationFilePath% %*

rem # free variables
set classpath=
set MelodyMainClass=
set defaultGlobalConfigurationFilePath=
set jarsLibraryPath=

