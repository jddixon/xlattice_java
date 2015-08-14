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

#if [ ! "`$JAVA -version 2>&1 | grep "\ 1\.3"`" ]; then 
#if [ ! "`$JAVA -version 2>&1 | egrep "\ 1\.[3456789].*"`" ]; then 
#    echo Required 1.3 or better version of JDK: can not use $JAVA
#    echo Current version is:
#    $JAVA -version
#    exit 1
#fi 

LOCALCLASSPATH=`/bin/sh $PWD/classpath.sh build`

CMD="$JAVA $OPTS -classpath $LOCALCLASSPATH org.apache.tools.ant.Main $@ -buildfile build.xml"
echo $CMD
$CMD
