@echo off

set homedir=%~p0
set jarsLibraryPath=%homedir%/common
set defaultGlobalConfigurationFilePath="%homedir%/config/melody.properties"
set MelodyMainClass=com.wat.melody.cli.Launcher
setLocal EnableDelayedExpansion
set classpath=
for /r "%jarsLibraryPath%" %%a IN (*.jar) do set classpath=!classpath!;"%%a"

java -cp %classpath% %MelodyMainClass% %defaultGlobalConfigurationFilePath% %*

set classpath=
set MelodyMainClass=
set defaultGlobalConfigurationFilePath=
set jarsLibraryPath=

