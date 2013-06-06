@echo off
if "%JAVA_HOME%" == "" goto javahomeerror

REM echo JAVA_HOME=%JAVA_HOME%

set LCP=%JAVA_HOME%\lib\tools.jar
set LCP=%JAVA_HOME%\jre\lib\jsse.jar;%LCP%
set LCP=..\lib\xlattice\util-0.3.8.jar;%LCP%
set LCP=..\lib\xlattice\transport-0.1.2.jar;%LCP%
set LCP=..\lib\xlattice\protocol-0.1.7.jar;%LCP%

set CMD=%JAVA_HOME%\bin\java.exe -classpath %LCP% org.xlattice.protocol.stun.GUIClient %1 %2 %3 %4 %5 %6 %7 %8 %9

REM echo %CMD%
%CMD%

goto end

REM ERROR EXIT ######################################################
:javahomeerror
echo "You need to set JAVA_HOME. Click on "
echo "  [Control Panel] [System] [Advanced] [Environmental Variable]"
echo "and add a value for JAVA_HOME such as C:\JDK1.5.0_06"

:end
