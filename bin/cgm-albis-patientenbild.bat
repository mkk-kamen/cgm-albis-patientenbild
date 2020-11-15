@echo off
@setlocal
set JAVA_HOME="C:\Program Files\Java\jre1.8.0_231"
set INSTDIR=C:\CGM\ALBIS

%JAVA_HOME%\bin\javaw.exe -Xms256m -Xmx512m -cp %INSTDIR%;%INSTDIR%\config;%INSTDIR%\libs\* de.mkkkamen.cgm.CgmAlbisPatientenbild

pause
:running
@endlocal
