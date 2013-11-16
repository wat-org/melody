@echo off

rem ######## license / author
rem # license : GPL
rem # author : Guillaume Cornet

rem ######## variables definition
rem # Get the basedir of the called script
rem #
set homedir=%~p0

rem # Folder where are stored JARs. All JAR will be included
set CLASSPATH=%homedir%\common\lib\*

rem # Melody's Main class
set melodyMainClass=com.wat.melody.cli.Launcher

rem # Default Global Configuration file path
set defaultGlobalConfigurationFilePath=%homedir%\config\melody.properties

rem # Melody's Java Options
set JAVA_OPTS=-Dmelody.default.global.configuration.file="%defaultGlobalConfigurationFilePath%"

rem # Execute Melody
rem #
java -Dfile.encoding=UTF-8 %JAVA_OPTS% %melodyMainClass% %*

rem # free variables
rem #
set JAVA_OPTS=
set defaultGlobalConfigurationFilePath=
set melodyMainClass=
set CLASSPATH=
set homedir=
