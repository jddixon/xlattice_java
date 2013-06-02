#!/bin/sh

export PID_FILE=./stun.server.pid
echo $$>$PID_FILE

# Take care: j2sdk and XLattice component version numbers wired in.
LCP=.:$JAVA_HOME/lib/tools.jar:$LCP
LCP=$JAVA_HOME/jre/lib/jsse.jar:$LCP
LCP=$XLATTICE_HOME/lib/xlattice/util-0.3.10.jar:$LCP
LCP=$XLATTICE_HOME/lib/xlattice/transport-0.2.0.jar:$LCP
LCP=$XLATTICE_HOME/lib/xlattice/protocol-0.2.0.jar:$LCP

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`/usr/bin/which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
else
  JAVA=$JAVA_HOME/bin/java
fi
echo "JAVA=$JAVA"

CMD="$JAVA $OPTS -classpath $LCP org.xlattice.protocol.stun.Server $@" 
echo $CMD
$CMD
