#!/usr/bin/env bash
set -e
gradle :server:run --console=plain --no-configuration-cache &
SERVER_PID=$!
gradle :client:run --console=plain --args="gui" --no-configuration-cache &
CLIENT_1=$!
gradle :client:run --console=plain --args="gui" --no-configuration-cache &
CLIENT_2=$!
trap "kill $SERVER_PID $CLIENT_1 $CLIENT_2"
wait
