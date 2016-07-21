#!/bin/sh

profile=$1
function=$2
binary=$3
handler=$4

aws lambda update-function-code \
    --profile "$profile" \
    --function-name "$function" \
    --zip-file "fileb://$binary"

aws lambda update-function-configuration \
    --function-name "$function" \
    --handler "$handler"