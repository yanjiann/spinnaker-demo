#!/bin/sh

SUB_HOME="/opt/hpe/mqtt-adapter"
EXCUTABLE=iot-core-nip-adapter-mqtt.jar

if [ $# -ne 1 ]; then
    echo "Usage: $0 [start|stop|status]"
elif [ "$1" = "start" ]; then
   nohup java -classpath ${SUB_HOME}/conf/:$SUB_HOME/lib/$EXCUTABLE org.springframework.boot.loader.JarLauncher >/dev/null 2>&1 &
elif [ "$1" = "stop" ]; then
  kill `ps -ef | grep $EXCUTABLE | grep -v grep | awk '{print $2}'`
elif [ "$1" = "status" ]; then
  pid=`ps -ef | grep $EXCUTABLE | grep -v grep | awk '{print $2}'`
  if [ -n "$pid" ]; then
      echo "iot-core-nip-adapter-mqtt is running (pid $pid)."
  else
      echo "iot-core-nip-adapter-mqtt is not running."
  fi
else
      echo "Usage: $0 [start|stop|status]"
fi

exit 0
