#!/bin/bash
#
# chkconfig: 345 99 05
# description: Java deamon script
#
# A non-SUSE Linux start/stop script for Java daemons.
#
# Derived from -
# Home page: http://www.source-code.biz
# License:   GNU/LGPL (http://www.gnu.org/licenses/lgpl.html)
# Copyright 2006 Christian d'Heureuse, Inventec Informatik AG, Switzerland.

set -o nounset

# Set this to your Java installation
JAVA_HOME=/usr/lib/jre

serviceNameLo="safebrowsing"                               #service name with the first letter in lowercase
serviceVersion="0.1.0"
serviceJar="${serviceNameLo}2.finagle_2.9.1-${serviceVersion}.jar"
servicePort=8082
serviceAdminPort=9990

serviceName="Safebrowsing2"                                 # service name
serviceUser="safebrowsing"                                  # OS user name for the service
serviceGroup="safebrowsing"                                 # OS group name for the service

applDir="/opt/$serviceNameLo/current"                       # home directory of the service application
serviceLogFile="/var/log/$serviceNameLo/$serviceNameLo.out" # log file for StdOut/StdErr
maxStartupTime=20                                           # maximum number of seconts to wait for daemon to startup
maxShutdownTime=15                                          # maximum number of seconds to wait for the daemon to terminate normally
pidFile="/var/run/$serviceNameLo.pid"                       # name of PID file (PID = process ID number)
javaCommand=java                                            # name of the Java launcher without the path
javaExe="$JAVA_HOME/bin/$javaCommand"                       # file name of the Java application launcher executable
javaArgs="-jar $applDir/$serviceJar"                        # arguments for Java launcher

STAGE=production
EXTRA_JAVA_OPTS="-Dstage=$STAGE"

HEAP_OPTS="-Xmx1024m -Xms1024m -XX:NewSize=192m"
GC_OPTS="-XX:+UseParallelOldGC -XX:+UseAdaptiveSizePolicy -XX:MaxGCPauseMillis=1000 -XX:GCTimeRatio=99"
GC_LOGGING_OPTS="-verbosegc -Xloggc:/var/log/$serviceNameLo/gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC"
javaOpts="-server -XX:+DisableExplicitGC -XX:+UseNUMA $GC_OPTS $GC_LOGGING_OPTS $HEAP_OPTS $EXTRA_JAVA_OPTS"

javaCommandLine="$javaExe ${javaOpts} $javaArgs"            # command line to start the Java service application
javaCommandLineKeyword="$serviceJar"                        # a keyword that occurs on the commandline, used to detect an already running service process and to distinguish it from others

# Makes the file $1 writable by the group $serviceGroup.
function makeFileWritable {
   local filename="$1"
   touch $filename || return 1
   chgrp $serviceGroup $filename || return 1
   chmod g+w $filename || return 1
   return 0; }

# Returns 0 if the process with PID $1 is running.
function checkProcessIsRunning {
   local pid="$1"
   if [ -z "$pid" -o "$pid" == " " ]; then return 1; fi
   if [ ! -e /proc/$pid ]; then return 1; fi
   return 0; }

# Returns 0 if the process with PID $1 is our Java service process.
function checkProcessIsOurService {
   local pid="$1"
   if [ "$(ps -p $pid --no-headers -o comm)" != "$javaCommand" ]; then return 1; fi
   grep -q --binary -F "$javaCommandLineKeyword" /proc/$pid/cmdline
   if [ $? -ne 0 ]; then return 1; fi
   return 0; }

# Returns 0 when the service is running and sets the variable $pid to the PID.
function getServicePID {
   if [ ! -f $pidFile ]; then return 1; fi
   pid="$(<$pidFile)"
   checkProcessIsRunning $pid || return 1
   checkProcessIsOurService $pid || return 1
   return 0; }

function checkPingStatus {
  local port="$1"
  curl -m 5 -s "http://localhost:$port/ping.txt" > /dev/null 2> /dev/null; }

function waitForStartup {
   for ((i=0; i<$maxStartupTime; i++)); do
      echo -n "."
      checkPingStatus $serviceAdminPort
      if [ $? -eq 0 ]; then
         return 0
         fi
      sleep 1
      done
   echo -e "\n$serviceName failed to start within $maxStartupTime seconds!"
   return 1; }

function startServiceProcess {
   cd $applDir || return 1
   rm -f $pidFile
   makeFileWritable $pidFile || return 1
   makeFileWritable $serviceLogFile || return 1
   cmd="nohup $javaCommandLine >>$serviceLogFile 2>&1 & echo \$! >$pidFile"
   su -m $serviceUser -s $SHELL -c "$cmd" || return 1
   sleep 0.1
   pid="$(<$pidFile)"
   if checkProcessIsRunning $pid; then :; else
      echo -ne "\n$serviceName start failed, see logfile."
      return 1
   fi
   return 0; }

function stopServiceProcess {
   kill $pid || return 1
   for ((i=0; i<maxShutdownTime*10; i++)); do
      checkProcessIsRunning $pid
      if [ $? -ne 0 ]; then
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   echo -e "\n$serviceName did not terminate within $maxShutdownTime seconds, sending SIGKILL..."
   kill -s KILL $pid || return 1
   local killWaitTime=15
   for ((i=0; i<killWaitTime*10; i++)); do
      checkProcessIsRunning $pid
      if [ $? -ne 0 ]; then
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   echo "Error: $serviceName could not be stopped within $maxShutdownTime+$killWaitTime seconds!"
   return 1; }

function startService {
   getServicePID
   if [ $? -eq 0 ]; then echo "$serviceName is already running"; RETVAL=0; return 0; fi
   echo -n "Starting $serviceName   "
   startServiceProcess
   if [ $? -ne 0 ]; then RETVAL=1; return 1; fi
   waitForStartup
   if [ $? -ne 0 ]; then RETVAL=1; return 1; fi
   echo "started PID=$pid"
   RETVAL=0
   return 0; }

function stopService {
   getServicePID
   if [ $? -ne 0 ]; then echo "$serviceName is not running"; RETVAL=0; echo ""; return 0; fi
   echo -n "Stopping $serviceName   "
   stopServiceProcess
   if [ $? -ne 0 ]; then RETVAL=1; echo "failed"; return 1; fi
   echo "stopped PID=$pid"
   RETVAL=0
   return 0; }

function checkServiceStatus {
   echo -n "Checking for $serviceName:   "
   if getServicePID; then
    echo "running PID=$pid"
    RETVAL=0
   else
    echo "stopped"
    RETVAL=3
   fi
   return 0; }

function checkServicePingStatus {
   echo -n "$serviceName:$serviceAdminPort "
   checkPingStatus $serviceAdminPort
   if [ $? -eq 0 ]; then echo "success"; else echo "fail"; fi
   
   echo -n "$serviceName:$servicePort "
   checkPingStatus $servicePort
   if [ $? -eq 0 ]; then echo "success"; else echo "fail"; fi }

function main {
   RETVAL=0
   case "$1" in
      start)                                               # starts the Java program as a Linux service
         startService
         ;;
      stop)                                                # stops the Java program service
         stopService
         ;;
      restart)                                             # stops and restarts the service
         stopService && startService
         ;;
      status)                                              # displays the service status
         checkServiceStatus
         ;;
      ping)                                                # displays the results of pinging the service
         checkServicePingStatus
         ;;
      *)
         echo "Usage: $0 {start|stop|restart|status|ping}"
         exit 1
         ;;
      esac
   exit $RETVAL
}

main $1