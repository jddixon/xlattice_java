#!/bin/sh
#
# Usage:
#    . classpath.sh [build|run] [set] [quiet]
#
# This script sets CLASSPATH and optionally echoes LOCALCLASSPATH 
# to the caller.
#
# The current directory and JAVA_HOME/lib/tools are always on the local path.
LCP=.:$JAVA_HOME/lib/tools.jar:$LCP
#
# If the first argument is 'build', JUnit and Ant are added to the local
# path, which will become CLASSPATH if the second argument is 'set'.
# unless there is a 'quiet' argument, the local class path is echoed.
if [ "$1" = "build" ] ; then 
    LCP=../lib/junit/junit-4.1.jar:$LCP
    #LCP=`echo ../lib/ant/*.jar | tr ' ' ':'`:$LCP
    LCP=../lib/ant/ant-1.5.4.jar:../lib/ant/optional-1.5.4.jar:$LCP
    if [ "$2" = "set" ] ; then
        CLASSPATH=$LCP
        if [ ! "$3" = "quiet" ] ; then
            echo $LCP
        fi
    elif [ ! "$2" = "quiet" ] ; then
        echo $LCP
    fi
else 
    LCP=target/classes:target/test-classes:$LCP
    if [ "$1" = "run" ] ; then
        if [ "$2" = "set" ] ; then
            CLASSPATH=$LCP
            if [ ! "$3" = "quiet" ] ; then
                echo $LCP
            fi
        elif [ ! "$2" = "quiet" ] ; then
            echo $LCP
        fi
    else 
        CLASSPATH=$LCP
        if [ ! "$1" = "quiet" ] ; then
            echo $CLASSPATH
        fi
    fi
fi
export CLASSPATH

