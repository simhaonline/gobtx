#!/bin/bash
# A basic templates example to run the JAVA instance

base_dir=$(dirname $0)

#Main class let the spring boot to handle

EXTRA_ARGS=${EXTRA_ARGS-'-instance ws_server'}

exec $base_dir/run-class.sh $EXTRA_ARGS "$@"
