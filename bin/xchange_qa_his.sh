#!/bin/bash
# A basic templates example to run the JAVA instance

base_dir=$(dirname $0)

#Main class let the spring boot to handle

EXTRA_ARGS=${EXTRA_ARGS-'-instance xchange -env qa -daemon -finder jdbc -memp md -ome -insc XCHANGE-HUB1 -metric local -reboot'}

export load_mode='HISTORY'

exec $base_dir/run-class.sh $EXTRA_ARGS "$@"
