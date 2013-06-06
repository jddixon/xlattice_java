#!/bin/bash
export PID_FILE=./stun.server.pid
/bin/kill -KILL `cat $PID_FILE`
