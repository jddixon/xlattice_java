#!/bin/sh

# Take care: XLattice component version numbers wired in.
LCP=.:$JAVA_HOME/lib/tools.jar:$LCP
LCP=.:$JAVA_HOME/jre/lib/jsse.jar:$LCP
LCP=$XLATTICE_HOME/lib/antlr/antlr-2.7.4.jar:$LCP
LCP=$XLATTICE_HOME/lib/xpp3/xpp3-1.1.3.4.C.jar:$LCP
LCP=$XLATTICE_HOME/lib/xlattice/util-0.3.11.jar:$LCP
LCP=$XLATTICE_HOME/lib/xlattice/corexml-0.3.8.jar:$LCP
LCP=$XLATTICE_HOME/lib/xlattice/projmgr-0.4.1.jar:$LCP

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

CMD="$JAVA $OPTS -classpath $LCP org.xlattice.projmgr.ProjMgr $@" 
echo $CMD
$CMD
