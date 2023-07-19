powershell -noprofile -command "& {[system.threading.thread]::sleep(5000)}"
$BACKUP
del /F /Q $CURRENT_CONTENT
xcopy "$NEW_VERSION"\*.* .\ /E /Y /Q
rmdir /S /Q "$NEW_VERSION"
cmd /c start /B "$EXECUTABLE_TO_START"
del /F /Q simpleUpdater.bat