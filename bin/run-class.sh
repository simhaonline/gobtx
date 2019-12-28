#!/bin/bash
# templates for start the JAVA instance in the tophold java application

source "$(dirname $0)"/include/functions.sh

checkJava

GC_LOG_ENABLED="false"
OME_DUM_ENABLE="false"
DAEMON_MODE="false"
REBOOT="false"
CONSOLE_ONLY="false"
INS_CNT=$(date +%s)_"$RANDOM"

MEM_POLICY="lg"
FIND_STRATEGY="jdbc"
METRICS_TYPE="log"
STATELESS=""
RPC_PORT="0"
GRPC_PORT="0"
SVC_PORT="0"
SKT_PORT="0"

export MAIN_CLASS="org.springframework.boot.loader.JarLauncher"

WHOAMI=$(whoami)

LOCAL_ADDRESS="$(hostname -i)"
IS_MULTI_IP=$(echo $LOCAL_ADDRESS | grep -P '\S')
if [ "${IS_MULTI_IP}X" != X ]; then
  TARR=(${IS_MULTI_IP})
  LOCAL_ADDRESS=${TARR[0]}
fi
DPARAM=""

DEBUGGER=""
EXCHANGE=""

WMSize="4"
WMIndex="0"
LOCAL_PORT="47500"
ADV_NETTY_DETECT=""

while [ $# -gt 0 ]; do
  COMMAND=$1
  case $COMMAND in
  -instance)
    INSTANCE=$2
    shift 2
    ;;
  -loggc)
    GC_LOG_ENABLED="true"
    shift
    ;;
  -stateless)
    STATELESS="-9"
    shift
    ;;
  -nettyadv)
    ADV_NETTY_DETECT=" -Dio.netty.leakDetection.level=advanced "
    shift
    ;;
  -ome)
    OME_DUM_ENABLE="true"
    shift
    ;;
  -daemon)
    DAEMON_MODE="true"
    shift
    ;;
  -reboot)
    REBOOT="true"
    shift
    ;;
  -console)
    CONSOLE_ONLY="true"
    REBOOT="false"
    shift
    ;;
  -env)
    ENV=$2
    shift 2
    ;;
  -metric)
    METRICS_TYPE=$2
    shift 2
    ;;
  -insc)
    INS_CNT=$2
    shift 2
    ;;
  -memp)
    MEM_POLICY=$2
    shift 2
    ;;
  -finder)
    FIND_STRATEGY=$2
    shift 2
    ;;
  -address)
    LOCAL_ADDRESS=$2
    shift 2
    ;;
  -rpc)
    RPC_PORT=$2
    shift 2
    ;;
  -grpc)
    GRPC_PORT=$2
    shift 2
    ;;
  -port)
    SVC_PORT=$2
    shift 2
    ;;
  -sport)
    SKT_PORT=$2
    shift 2
    ;;
  -main)
    MAIN_CLASS=$2
    shift 2
    ;;
  -app)
    APP=$2
    shift 2
    ;;
  -ws)
    WMSize=$2
    shift 2
    ;;
  -wi)
    WMIndex=$2
    shift 2
    ;;
  -iport)
    LOCAL_PORT=$2
    shift 2
    ;;
  -ex)
    EXCHANGE=$2
    shift 2
    ;;
  -debug)
    DEBUGGER=" -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=$2,suspend=n "
    shift 2
    ;;
  -D*)
    DPARAM=$DPARAM" $COMMAND"
    shift
    ;;
  *)
    break
    ;;
  esac
done

if [ -z "$ENV" ]; then
  echo "-env is must !"
  usage
fi

if [ -z "$INSTANCE" ]; then
  echo "-instance is must!"
  usage
fi

if [ "$ENV" = "qa" ]; then
  export USR="xtx_qa"
elif [ "$ENV" = "prod" ]; then
  export USR="xtx_prod"
  if [ "$WHOAMI" != "xtx_prod" ]; then
    echo "$WHOAMI is wrong, Must be th_prod in production environment"
    exit 1
  fi
elif [ "$ENV" = "dev" ]; then
  export USR="xtx_dev"
elif [ "$ENV" = "par" ]; then
  export USR="xtx_par"
else
  echo "Illegal environment argument"
  usage
fi

if [ "$MEM_POLICY" != 'mini' ] && [ "$MEM_POLICY" != 'x6l' ] && [ "$MEM_POLICY" != 'x5l' ] && [ "$MEM_POLICY" != 'x1.5l' ] && [ "$MEM_POLICY" != 'lg' ] && [ "$MEM_POLICY" != 'xl' ] && [ "$MEM_POLICY" != 'md' ] && [ "$MEM_POLICY" != 'xxl' ] && [ "$MEM_POLICY" != 'xxxl' ]; then
  echo "Illegal mem policy either mini or lg or xl,xxl"
  usage
fi

APP_DIR=$INSTANCE

if [ -n "$APP" ]; then
  APP_DIR="${APP_DIR}/${APP}"
fi

export $MEM_POLICY

export TMP_LOG_DIR="/var/tmp/$ENV/$APP_DIR/$INS_CNT"

PID_FILE=$TMP_LOG_DIR/run/running.pid
LOK_FILE=$TMP_LOG_DIR/run/locking

export instance_name="${INS_CNT}"

double_kill() {
  EXIST_ONE=$(ps -aef | grep "\-Dinstance.name=${INS_CNT}" | grep "\-Dapp.instance=${INSTANCE}" | wc -l)
  echo "Try do Kill through the ps grep $EXIST_ONE"
  ROUND=0
  while [[ "$EXIST_ONE" == "1" && $ROUND -lt 10 ]]; do
    echo "-Dinstance.name=${INS_CNT} -Dapp.instance=${INSTANCE} Still Live, PID gone, so Grep to Kill"
    for pid in $(ps -aef | grep "\-Dinstance.name=${INS_CNT}" | grep "\-Dapp.instance=${INSTANCE}" | awk '{print $2}'); do kill $STATELESS $pid; done
    sleep 10
    EXIST_ONE=$(ps -aef | grep "\-Dinstance.name=${INS_CNT}" | grep "\-Dapp.instance=${INSTANCE}" | wc -l)
    ROUND=$((ROUND + 1))
    echo "Round $ROUND KILL Status : $EXIST_ONE"
  done
  echo "Still exist after $ROUND try?  $EXIST_ONE"
}

kill_exist_instance() {

  [ ! -f $PID_FILE ] && {
    echo "$PID_FILE not exist, then do the manually kill"
    double_kill
    return 0
  }

  PID=$(cat $PID_FILE)

  [ -z "$PID" ] && {
    echo "$PID_FILE is empty, then do the manually kill"
    double_kill
    return 0
  }

  echo "Find PID from $PID_FILE : $PID "

  ps -p "$PID" >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    kill $PID >/dev/null 2>&1

    EXIST_ONE=$(ps -aef | grep "\-Dinstance.name=${INS_CNT}" | grep "\-Dapp.instance=${INSTANCE}" | wc -l)
    ROUND=0
    echo "Direct Kill $PID, $EXIST_ONE"
    while [[ "$EXIST_ONE" == "1" && $ROUND -lt 10 ]]; do
      kill $STATELESS $PID >/dev/null 2>&1
      sleep 10
      EXIST_ONE=$(ps -aef | grep "\-Dinstance.name=${INS_CNT}" | grep "\-Dapp.instance=${INSTANCE}" | wc -l)
      ROUND=$((ROUND + 1))
      echo "Round $ROUND KILL Status : $EXIST_ONE"
    done
  else
    echo "$PID not exist, then do the manually kill"
    double_kill
  fi
}
if [ "$CONSOLE_ONLY" = "true" ]; then
  echo "Running in Console model just print the command not kick off the instance!"
  REBOOT="false"
fi

if [ "$REBOOT" = "true" ]; then
  echo "Reboot Flag set try to kill exist previous instance "
  kill_exist_instance
fi

if [ "$CONSOLE_ONLY" != "true" ]; then
  EXIST_ONE=$(ps -aef | grep "\-Dinstance.name=${INS_CNT}" | grep "\-Dapp.instance=${INSTANCE}" | wc -l)

  if [ "$EXIST_ONE" = "1" ]; then
    echo "-Dinstance.name=${INS_CNT} -Dapp.instance=${INSTANCE} already exist;  choice another -insc XXX"
    for pid in $(ps -aef | grep "\-Dinstance.name=${INS_CNT}" | grep "\-Dapp.instance=${INSTANCE}" | awk '{print $2}'); do kill -9 $pid; done
    #usage
  fi
fi

if [[ -d $DIR ]]; then
  echo "temp dir $TMP_LOG_DIR already exist carefully"

  echo "Purge the log 9 days ago ----------------- "
  find /var/tmp/$ENV/$APP_DIR/$INS_CNT/logs -mtime +2 -exec rm {} \;
  find /var/tmp/$ENV/$APP_DIR/$INS_CNT/logs/ignite -mtime +2 -exec rm {} \;
  find /var/tmp/$ENV/$APP_DIR/$INS_CNT/logs/app -mtime +2 -exec rm {} \;
  echo "Done the purge"

else
  mkdir -p $TMP_LOG_DIR
  mkdir -p "$TMP_LOG_DIR/datas"
  mkdir -p "$TMP_LOG_DIR/logs"
  mkdir -p "$TMP_LOG_DIR/work"
  mkdir -p "$TMP_LOG_DIR/swap"
  mkdir -p "$TMP_LOG_DIR/run"
fi

export FIND_STRATEGY=$FIND_STRATEGY

export CONSOLE_OUTPUT_FILE="/var/tmp/$ENV/$APP_DIR/$INS_CNT/logs/$INSTANCE.log"

rm -rf "/var/tmp/$ENV/$APP_DIR/$INS_CNT/logs/$INSTANCE.log"

# CYGINW == 1 if Cygwin is detected, else 0.
if [[ $(uname -a) =~ "CYGWIN" ]]; then
  CYGWIN=1
else
  CYGWIN=0
fi

# Exclude jars not necessary for running commands.
regex="(-(test|src|scaladoc|javadoc)\.jar|jar.asc)$"

should_include_file() {
  file=$1
  if [ -z "$(echo "$file" | egrep "$regex")" ]; then
    return 0
  else
    return 1
  fi
}

base_dir=$(dirname $0)/..

shopt -s nullglob

SPRING_BOOT_JAR=""

echo "scan jar under the  ${base_dir}/${INSTANCE}/build/libs"

for file in "$base_dir"/$INSTANCE/build/libs/*.jar; do
  if should_include_file "$file"; then
    CLASSPATH="$CLASSPATH":"$file"
    SPRING_BOOT_JAR="$file"
    echo "find qualify jar: $file"
  fi
done

if [ -z "$SPRING_BOOT_JAR" ]; then

  if [ "$ENV" = "prod" ]; then
    #example:
    #/var/www/trade_engine/current/fee/build/libs
    echo "In production double SCAN the jar under /var/www/arkenstone/current/${INSTANCE}/build/libs "

    for file in /var/www/arkenstone/current/${INSTANCE}/build/libs/*.jar; do
      if should_include_file "$file"; then
        echo "find qualify jar2: $file"
        CLASSPATH="$CLASSPATH":"$file"
        SPRING_BOOT_JAR="$file"
      fi
    done
  fi

  if [ -z "$SPRING_BOOT_JAR" ]; then
    echo "Spring boot application suppose have a fat jar"
    exit 1
  fi
fi

#maybe other path
shopt -u nullglob

# Which java to use
if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi

#-XX:MaxDirectMemorySize=60M

if [ "$MEM_POLICY" == 'lg' ]; then
  JVM_OPTS="-Xms2g -Xmx2g  -XX:NewSize=512m -XX:MaxDirectMemorySize=256M "
elif [ "$MEM_POLICY" == 'xl' ]; then
  JVM_OPTS="-Xms4g -Xmx4g  -XX:NewSize=512m -XX:MaxDirectMemorySize=512M "
elif [ "$MEM_POLICY" == 'x1.5l' ]; then
  JVM_OPTS="-Xms1536m -Xmx1536m  -XX:NewSize=384m -XX:MaxDirectMemorySize=256M "
elif [ "$MEM_POLICY" == 'x6l' ]; then
  JVM_OPTS="-Xms6g -Xmx6g  -XX:NewSize=1g -XX:MaxDirectMemorySize=1G "
elif [ "$MEM_POLICY" == 'x5l' ]; then
  JVM_OPTS="-Xms5g -Xmx5g  -XX:NewSize=1g -XX:MaxDirectMemorySize=1G "
elif [ "$MEM_POLICY" == 'md' ]; then
  JVM_OPTS="-Xms1g -Xmx1g  -XX:NewSize=256m -XX:MaxDirectMemorySize=256M "
elif [ "$MEM_POLICY" == 'xxl' ]; then
  JVM_OPTS="-Xms8g -Xmx8g -XX:NewSize=3g -XX:MaxNewSize=5g -XX:MaxDirectMemorySize=2G "
elif [ "$MEM_POLICY" == 'xxxl' ]; then
  JVM_OPTS="-Xms10g -Xmx10g -XX:NewSize=3g -XX:MaxNewSize=5g -XX:MaxDirectMemorySize=2G "
else
  JVM_OPTS="-Xms512m -Xmx512m -XX:MaxDirectMemorySize=128M "
fi

if [ -z "$EXCHANGE" ]; then
  EXCHANGER_OP=""
else
  EXCHANGER_OP="-Dexchange=$EXCHANGE"
  export exchange="${EXCHANGE}"
fi

ConcGCThreads=$(grep 'processor' /proc/cpuinfo | sort -u | wc -l)
if [ $ConcGCThreads -gt 8 ]; then
  ConcGCThreads=8
elif [ $ConcGCThreads -eq 0 ]; then
  ConcGCThreads=4
fi

if [[ $("$JAVA" -version 2>&1 | egrep "10\.[0-9]+\.") ]]; then
  JVM_OPTS="${JVM_OPTS} --add-exports java.base/jdk.internal.misc=ALL-UNNAMED --add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-exports java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED --add-exports jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED --add-modules java.xml.bind "
fi

if [[ $("$JAVA" -version 2>&1 | egrep "1\.[7]\.") ]]; then
  JVM_OPTS="${JVM_OPTS} -server -XX:NewSize=512m  -XX:+AggressiveOpts -XX:MaxPermSize=256m"
else
  #-XX:MaxGCPauseMillis=1000
  JVM_OPTS="${JVM_OPTS} -server -XX:+CrashOnOutOfMemoryError -XX:+AlwaysPreTouch -XX:-UseBiasedLocking -XX:MaxTenuringThreshold=15 -Xss256k -XX:SurvivorRatio=6 -XX:+UseTLAB -XX:GCTimeRatio=4 -XX:+ScavengeBeforeFullGC -XX:G1HeapRegionSize=8M -XX:ConcGCThreads=${ConcGCThreads} -XX:G1HeapWastePercent=10 -XX:+AggressiveOpts -XX:MaxMetaspaceSize=256m -XX:AutoBoxCacheMax=20000 -XX:+UseG1GC  -XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC"
fi

JVM_OTHERS=" ${ADV_NETTY_DETECT} ${EXCHANGER_OP} -Djava.awt.headless=true -DLOCAL_PORT=${LOCAL_PORT} -DFIND_STRATEGY=${FIND_STRATEGY} -Dnetwork.ws.port=${SKT_PORT} -DWMIndex=${WMIndex}  -DWMSize=${WMSize} -Dio.netty.allocator.type=pooled -Dio.netty.leakDetection.level=advanced -Dnetwork.rpc.port=${RPC_PORT} -Dnetwork.grpc.port=${GRPC_PORT} -Dserver.port=${SVC_PORT}  -DMETRICS_TYPE=${METRICS_TYPE} -DIGNITE_EXCHANGE_COMPATIBILITY_VER_1=true -DIGNITE_QUIET=false -DIGNITE_THREAD_DUMP_ON_EXCHANGE_TIMEOUT=true -Duser.timezone=UTC -Djava.net.preferIPv4Stack=true -Dinstance.name=${INS_CNT} -Dapp.instance=${INSTANCE} -Dapp.env=$ENV -Dapp.user=$USR -Dtemp.dir=${TMP_LOG_DIR} $DPARAM -DMEM_POLICY=${MEM_POLICY}"

#
# Save terminal setting. Used to restore terminal on finish.
#
SAVED_STTY=$(stty -g 2>/dev/null)

#
# Restores terminal.
#
function restoreSttySettings() {
  stty ${SAVED_STTY}
}

#
# Trap that restores terminal in case script execution is interrupted.
#
trap restoreSttySettings INT

# GC options
GC_FILE_SUFFIX='-gc.log'
GC_LOG_FILE_NAME=''
if [ "$GC_LOG_ENABLED" == "true" ]; then
  GC_LOG_FILE_NAME=$INSTANCE$GC_FILE_SUFFIX
  TH_GC_LOG_OPTS="-Xloggc:$TMP_LOG_DIR/$GC_LOG_FILE_NAME -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M "
fi

# If Cygwin is detected, classpath is converted to Windows format.
((CYGWIN)) && CLASSPATH=$(cygpath --path --mixed "${CLASSPATH}")

EXTRA_SPRING_PROPERTIES="--spring.config.location=classpath:/common.properties,classpath:/common-${ENV}.properties,classpath:/${INSTANCE}/common.properties,classpath:/${INSTANCE}/${ENV}.properties"
EXTRA_SPRING_PROPERTIES=""

#LOCAL_ADDRESS  if not null then passed as properties

if [ -z "$LOCAL_ADDRESS" ]; then
  ADD_OP=""
else
  ADD_OP="-DLOCAL_ADDRESS=$LOCAL_ADDRESS -Drpc.host=$LOCAL_ADDRESS"
fi

echo_success() {
  echo "[OK]"
  return 0
}

echo_failure() {
  echo "[FAILED]"
  return 1
}

#OME
OME_DUM_FILE='ome.log'
OME_OPTION=''
if [ "$OME_DUM_ENABLE" == "true" ]; then
  OME_OPTION="-XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=$TMP_LOG_DIR/$OME_DUM_FILE  -XX:ErrorFile=$TMP_LOG_DIR/java_error%p.log "
fi

if [ "$CONSOLE_ONLY" != "true" ]; then
  [ -f $LOK_FILE -a -f $PID_FILE ] && {

    if [ "$EXIST_ONE" != "1" ]; then
      echo "WTF the PID corrupt, going process"
      rm -f $PID_FILE # Remove control files
      rm -f $LOK_FILE
    else
      echo_success #already running
      return 0
    fi
  }
fi

# Launch mode
if [ "$DAEMON_MODE" == "true" ]; then
  echo "$JAVA $JVM_OPTS $DEBUGGER $TH_GC_LOG_OPTS $OME_OPTION $ADD_OP $EXCHANGER_OP $JVM_OTHERS  -jar ${SPRING_BOOT_JAR} $EXTRA_SPRING_PROPERTIES "$@" "
  echo "run as daemon log output to the $CONSOLE_OUTPUT_FILE"

  if [ "$CONSOLE_ONLY" = "true" ]; then
    echo "In Console model so EXIT !"
    exit 0
  else
    echo "Kicking off instance...."
    nohup $JAVA $JVM_OPTS $DEBUGGER $TH_GC_LOG_OPTS $OME_OPTION $ADD_OP $EXCHANGER_OP $JVM_OTHERS -jar ${SPRING_BOOT_JAR} $EXTRA_SPRING_PROPERTIES "$@" >"$CONSOLE_OUTPUT_FILE" 2>&1 </dev/null &
    RETVAL=$?
    PID=$!
    echo $PID >$PID_FILE
    if [ $RETVAL -eq 0 ]; then
      touch $LOK_FILE
      echo_success
    else
      echo_failure
    fi
  fi
else
  echo "$JAVA $JVM_OPTS $DEBUGGER $TH_GC_LOG_OPTS $OME_OPTION $ADD_OP $EXCHANGER_OP $JVM_OTHERS  -jar ${SPRING_BOOT_JAR} $EXTRA_SPRING_PROPERTIES "$@" "

  if [ "$CONSOLE_ONLY" = "true" ]; then
    echo "In Console model so EXIT !"
    exit 0
  else
    echo "Kicking off instance...."
    exec $JAVA $JVM_OPTS $DEBUGGER $TH_GC_LOG_OPTS $OME_OPTION $ADD_OP $EXCHANGER_OP $JVM_OTHERS -jar ${SPRING_BOOT_JAR} $EXTRA_SPRING_PROPERTIES "$@"
  fi
fi
