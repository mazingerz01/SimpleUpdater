powershell -noprofile -command "& {[system.threading.thread]::sleep(5000)}"
del /F /S /Q $TODO_PLACEHOLDERS
xcopy "xxxnewversionxxxx"\*.* .\ /E /Y /Q
rmdir /S /Q "xxxnewversionxxxx"
cmd /c start /B "newVersExecut"