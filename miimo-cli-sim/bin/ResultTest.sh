#!/bin/sh

LOG_FILE="/home/develope/miimo-cli-sim/log/miimo-cli-sim.log"

if [[ -e ${LOG_FILE} ]]; then
    exit 0
fi

exit -1
