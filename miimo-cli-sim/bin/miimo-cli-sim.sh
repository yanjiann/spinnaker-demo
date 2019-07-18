#!/bin/sh

SUB_HOME="/opt/hpe/miimo-cli-sim"
EXCUTABLE=miimo-cli-sim.jar

if [ $# -lt 2 ]; then
echo "Usage: $0 [start] [clientId prefix] [startNum] [tps]"
echo "       $0 [stop|status] [clientId prefix]"
elif [ "$1" = "start" ]; then
   nohup java -classpath ${SUB_HOME}/conf/:$SUB_HOME/lib/$EXCUTABLE org.springframework.boot.loader.JarLauncher $2 --multi.thread.num.start=$3 --multi.thread.tps=$4 >/dev/null 2>&1 &
elif [ "$1" = "stop" ]; then
  kill `ps -ef | grep $EXCUTABLE  | grep $2 | grep -v grep | awk '{print $2}'`
elif [ "$1" = "status" ]; then
  pid=`ps -ef | grep $EXCUTABLE | grep $2 | grep -v grep | awk '{print $2}'`
  if [ -n "$pid" ]; then
      echo "miimo-cli-sim is running (pid $pid)."
  else
      echo "miimo-cli-sim is not running."
  fi
else
      echo "Usage: $0 [start] [clientId prefix] [startNum] [tps]"
      echo "       $0 [stop|status] [clientId prefix]"
fi

exit 0
