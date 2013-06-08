#!/bin/sh
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

# LocalClassPath
LCP=.:$JAVA_HOME/lib/tools.jar:$LCP
LCP=../lib/xpp3/xpp3-1.1.3.4.C.jar:$LCP

LCP=../lib/xlattice/util-0.3.8.jar:$LCP
LCP=../lib/xlattice/corexml-0.3.4.jar:$LCP
LCP=../lib/xlattice/crypto-0.1.1.jar:$LCP
LCP=../lib/xlattice/node-0.1.1.jar:$LCP

LCP=../lib/junit/junit-3.8.1p.jar:target/test-classes:$LCP

CMD="$JAVA $OPTS -classpath $LCP junit.textui.TestRunner org.xlattice.node.TestConfigurer"
echo $CMD
$CMD

# following tests succeed
CMD="$JAVA $OPTS -classpath $LCP junit.textui.TestRunner org.xlattice.node.TestNodeImpl"
echo $CMD
$CMD

CMD="$JAVA $OPTS -classpath $LCP junit.textui.TestRunner org.xlattice.node.TestRSAInfo"
echo $CMD
$CMD
