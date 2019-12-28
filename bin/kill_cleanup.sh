#!/bin/bash
# templates for start the JAVA instance in the tophold java application

source "$(dirname $0)"/include/functions.sh


while [ $# -gt 0 ]; do
  COMMAND=$1
  case $COMMAND in
    -instance)
      INSTANCE=$2
      shift 2
      ;;
    -env)
      ENV=$2
      shift 2
      ;;
    -insc)
      INS_CNT=$2
      shift 2
      ;;
    -app)
      APP=$2
      shift 2
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

if [ -z "$INS_CNT" ]; then
  echo "-insc is must!"
  usage
fi


APP_DIR=$INSTANCE

if [ -n "$APP" ]; then
  APP_DIR="${APP_DIR}/${APP}"
fi

export TMP_LOG_DIR="/var/tmp/$ENV/$APP_DIR/$INS_CNT"


echo_success() {
    echo "[OK]"
    return 0
}

echo_failure() {
    echo "[FAILED]"
    return 1
}


PID_FILE=$TMP_LOG_DIR/run/running.pid
LOK_FILE=$TMP_LOG_DIR/run/locking


double_kill(){
    EXIST_ONE=`ps -aef | grep "\-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE}" | wc -l`
    echo "Try do Kill through the ps grep $EXIST_ONE"
    ROUND=0
    while [[ "$EXIST_ONE" = "1" && $ROUND -lt 7 ]]; do
        echo "-Dapp.instance.cnt=${INS_CNT} -Dapp.instance=${INSTANCE} Still Live, PID gone, so Grep to Kill"
        for pid in $(ps -aef | grep "\-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE}" | awk '{print $2}'); do kill -9 $pid; done
        sleep 10
        EXIST_ONE=`ps -aef | grep "\-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE}" | wc -l`
        ROUND=$((ROUND+1))
        echo "Round $ROUND KILL Status : $EXIST_ONE"
    done
    echo "Still exist after $ROUND try?  $EXIST_ONE"
}

kill_exist_instance(){

    [ ! -f $PID_FILE ] && {
        echo "$PID_FILE not exist, then do the manually kill"
        double_kill
        return 0
    }

    PID=`cat $PID_FILE`

    echo "Find PID from $PID_FILE : $PID "
    [ -z "$PID" ] && {
        echo "$PID_FILE is empty, then do the manually kill"
        double_kill
        return 0
    }
    ps -p "$PID" >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        kill $PID >/dev/null 2>&1

        EXIST_ONE=`ps -aef | grep "\-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE}" | wc -l`
        ROUND=0
        echo "Direct Kill $PID, $EXIST_ONE"
        while [[ "$EXIST_ONE" = "1" && $ROUND -lt 7 ]]; do
            kill -9 $PID >/dev/null 2>&1
            sleep 10
            EXIST_ONE=`ps -aef | grep "\-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE}" | wc -l`
            ROUND=$((ROUND+1))
            echo "Round $ROUND KILL Status : $EXIST_ONE"
        done
    else
        echo "$PID not exist, then do the manually kill"
        double_kill
    fi
}

kill_exist_instance


rm -rf /var/tmp/$ENV/$APP_DIR/$INS_CNT/work
rm -rf /var/tmp/$ENV/$APP_DIR/$INS_CNT/datas
rm -rf /var/tmp/$ENV/$APP_DIR/$INS_CNT/swap
rm -rf /var/tmp/$ENV/$APP_DIR/$INS_CNT/run


detect(){
    EXIST_ONE=`ps -aef | grep "\-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE}" | wc -l`
    for pid in $(ps -aef | grep "app.instance" | awk '{print $2}'); do kill -9 $pid; done
    if [ "$EXIST_ONE" = "1" ]; then
        echo "STILL exist force kill -9 \-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE} "
        for pid in $(ps -aef | grep "\-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE}" | awk '{print $2}'); do kill -9 $pid; done
        return 1
    else
        echo "NO \-Dapp.instance.cnt=${INS_CNT} \-Dapp.instance=${INSTANCE} exist"
        return 0
    fi
}

detect