#!/bin/sh

ret=`curl 35.238.138.26:8080`

if [[ -z ${ret} ]]; then
    exit -1
fi

exit 0
