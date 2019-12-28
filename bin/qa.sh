#!/bin/bash
# A basic templates example to run the JAVA instance

base_dir=$(dirname $0)

$base_dir/xchange.sh -env qa -daemon -finder jdbc -memp md -ome -insc XCHANGE-HUB1 -metric local -reboot

sleep 20

$base_dir/ws_server.sh -env qa -daemon -finder jdbc -memp md -ome -insc FRONTEND-K1 -metric local -reboot
