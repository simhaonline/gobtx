#!/bin/bash

LOCAL_ADDRESS="$(hostname -i)"
IS_MULTI_IP=$(echo $LOCAL_ADDRESS | grep -P '\S')
if [ "${IS_MULTI_IP}X" != X ]; then
  TARR=(${IS_MULTI_IP})
  LOCAL_ADDRESS=${TARR[0]}
fi

echo "kick off host:  ${LOCAL_ADDRESS}"

WHOAMI=$(whoami)
if [ "$WHOAMI" != "btx_qa" ]; then
  echo "$WHOAMI is wrong, Must be btx_qa"
  exit 1
fi

now=$(date)
echo "--------------------START $now --------------------"
sw=$(date +%s)

echo "ssh btx_qa@172.31.11.66  /var/www/gobtx/current/bin/kill_cleanup.sh -instance ws_server -env qa -insc FRONTEND-K1"
ssh btx_qa@172.31.11.66 "/var/www/gobtx/current/bin/kill_cleanup.sh -instance ws_server -env qa -insc FRONTEND-K1" >/dev/null 2>&1 &

echo "ssh btx_qa@172.31.11.65  /var/www/gobtx/current/bin/kill_cleanup.sh -instance xchange -env qa -insc XCHANGE-HUB1"
ssh btx_qa@172.31.11.65 "/var/www/gobtx/current/bin/kill_cleanup.sh -instance xchange -env qa -insc XCHANGE-HUB1" >/dev/null 2>&1 &

echo "ssh btx_qa@	172.31.11.64  /var/www/gobtx/current/bin/kill_cleanup.sh -instance xchange -env qa -insc XCHANGE-HUB2"
ssh btx_qa@ 172.31.11.64 "/var/www/gobtx/current/bin/kill_cleanup.sh -instance xchange -env qa -insc XCHANGE-HUB2" >/dev/null 2>&1 &

echo "Try to sleep 45"
sleep 45

# Kick off the jobs

echo "ssh btx_qa@172.31.11.65  /var/www/gobtx/current/bin/xchange.sh -env qa -finder local -memp xl -metric local -port 8802 -address 172.31.11.65 -ome -loggc -reboot -stateless  -daemon  -instance xchange -insc XCHANGE-HUB1"
ssh btx_qa@172.31.11.65 "/var/www/gobtx/current/bin/xchange.sh -env qa -finder local -memp xl -metric local -port 8802 -address 172.31.11.65 -ome -loggc -reboot -stateless  -daemon  -instance xchange -insc XCHANGE-HUB1" >/dev/null 2>&1 &

echo "ssh btx_qa@	172.31.11.64  /var/www/gobtx/current/bin/xchange.sh -env qa -finder local -memp xl -metric local -port 8802 -address 	172.31.11.64 -ome -loggc -reboot -stateless  -daemon  -instance xchange -insc XCHANGE-HUB2"
ssh btx_qa@ 172.31.11.64 "/var/www/gobtx/current/bin/xchange.sh -env qa -finder local -memp xl -metric local -port 8802 -address 	172.31.11.64 -ome -loggc -reboot -stateless  -daemon  -instance xchange -insc XCHANGE-HUB2" >/dev/null 2>&1 &

echo "ssh btx_qa@172.31.11.66  /var/www/gobtx/current/bin/ws_server.sh -env qa -finder local -memp xl -metric local -address 172.31.11.66 -ome -loggc -reboot -stateless  -daemon  -instance ws_server -insc FRONTEND-K1"
ssh btx_qa@172.31.11.66 "/var/www/gobtx/current/bin/ws_server.sh -env qa -finder local -memp xl -metric local -address 172.31.11.66 -ome -loggc -reboot -stateless  -daemon  -instance ws_server -insc FRONTEND-K1" >/dev/null 2>&1 &

swd=$(date +%s)
now=$(date)
swd=$(expr $swd - $sw)
swd=$(expr $swd / 60)
echo "--------------------END $now --------------------"
echo "--------------------COST $swd Minutes--------------------"
