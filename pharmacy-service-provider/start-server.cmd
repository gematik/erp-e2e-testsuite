echo "try to start pharmacy-serviceprovider-jar-with-dependencies.jar"

cd target
echo "Errorlevel"
echo %ERRORLEVEL%
echo "pid"
echo %PID%
ScriptRunner.exe -appvscript  cmd "/c" "java -jar pharmacy-serviceprovider-jar-with-dependencies.jar -timeout=10" -appvscriptrunnerparameters -timeout=3



echo start-server exit
exit
