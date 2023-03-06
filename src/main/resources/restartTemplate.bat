powershell -noprofile -command "& {[system.threading.thread]::sleep(5000)}"
del /Q $CURRENT_VERSION
move /Y $NEW_VERSION $CURRENT_VERSION
cmd /c start /B $CURRENT_VERSION
