#!/bin/bash
#
# basic check
#
checkJava() {
    # Check JAVA_HOME.
    if [ "$JAVA_HOME" = "" ]; then
        JAVA=`type -p java`
        RETCODE=$?

        if [ $RETCODE -ne 0 ]; then
            echo $0", ERROR:"
            echo "JAVA_HOME environment variable is not found."
            echo "Please point JAVA_HOME variable to location of >= JDK 1.8."
            echo "You can also download latest JDK at http://java.com/download"

            exit 1
        fi

        JAVA_HOME=
    else
        JAVA=${JAVA_HOME}/bin/java
    fi

    #
    # Check JDK.
    #
    if [ ! -e "$JAVA" ]; then
        echo $0", ERROR:"
        echo "JAVA is not found in JAVA_HOME=$JAVA_HOME."
        echo "Please point JAVA_HOME variable to installation of >= JDK 1.8."
        echo "You can also download latest JDK at http://java.com/download"

        exit 1
    fi

    JAVA_VER=`"$JAVA" -version 2>&1 | egrep "1\.[8]\.|10\.[0-9]+\."`

    if [ "$JAVA_VER" == "" ]; then
        echo $0", ERROR:"
        echo "The version of JAVA installed in JAVA_HOME=$JAVA_HOME is incorrect."
        echo "Please point JAVA_HOME variable to installation of >= JDK 1.8."
        echo "You can also download latest JDK at http://java.com/download"

        exit 1
    fi
}


usage(){
    echo "USAGE: $0 -instance application_name(must) -env qa/prod/dev(must), -memp mini/lg(memory policy) JVM + Spring configuration(optional)"
    exit 1
}