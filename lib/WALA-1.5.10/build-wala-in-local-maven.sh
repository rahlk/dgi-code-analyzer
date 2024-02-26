#! /bin/bash

# exit on error
set -o errexit

build() {
    wala_root_folder="/Users/fabiofranco/Development/wala/WALA-1.5.10"
    cd $wala_root_folder
    ./gradlew assemble

    # com.ibm.wala.cast
    mvn install:install-file \
        -Dfile=${wala_root_folder}/com.ibm.wala.cast/build/libs/com.ibm.wala.cast-1.5.9.jar \
        -DgroupId=com.ibm.wala \
        -DartifactId=com.ibm.wala.cast \
        -Dversion=1.5.10 \
        -Dpackaging=jar \
        -DgeneratePom=true

    # com.ibm.wala.cast.java
    mvn install:install-file \
        -Dfile=${wala_root_folder}/com.ibm.wala.cast.java/build/libs/com.ibm.wala.cast.java-1.5.9.jar \
        -DgroupId=com.ibm.wala \
        -DartifactId=com.ibm.wala.cast.java \
        -Dversion=1.5.10 \
        -Dpackaging=jar \
        -DgeneratePom=true

    # com.ibm.wala.core
    mvn install:install-file \
        -Dfile=${wala_root_folder}/com.ibm.wala.core/build/libs/com.ibm.wala.core-1.5.9.jar \
        -DgroupId=com.ibm.wala \
        -DartifactId=com.ibm.wala.core \
        -Dversion=1.5.10 \
        -Dpackaging=jar \
        -DgeneratePom=true

    # com.ibm.wala.core.java11
    mvn install:install-file \
        -Dfile=${wala_root_folder}/com.ibm.wala.core.java11/build/libs/com.ibm.wala.core.java11-1.5.9.jar \
        -DgroupId=com.ibm.wala \
        -DartifactId=com.ibm.wala.core.java11 \
        -Dversion=1.5.10 \
        -Dpackaging=jar \
        -DgeneratePom=true

    # com.ibm.wala.cast.java.ecj
    mvn install:install-file \
        -Dfile=${wala_root_folder}/com.ibm.wala.cast.java.ecj/build/libs/com.ibm.wala.cast.java.ecj-1.5.9.jar \
        -DgroupId=com.ibm.wala \
        -DartifactId=com.ibm.wala.cast.java.ecj \
        -Dversion=1.5.10 \
        -Dpackaging=jar \
        -DgeneratePom=true

    # com.ibm.wala.util   
    mvn install:install-file \
        -Dfile=${wala_root_folder}/com.ibm.wala.util/build/libs/com.ibm.wala.util-1.5.9.jar \
        -DgroupId=com.ibm.wala \
        -DartifactId=com.ibm.wala.util \
        -Dversion=1.5.10 \
        -Dpackaging=jar \
        -DgeneratePom=true 

    # com.ibm.wala.shrike
    mvn install:install-file \
        -Dfile=${wala_root_folder}/com.ibm.wala.shrike/build/libs/com.ibm.wala.shrike-1.5.9.jar \
        -DgroupId=com.ibm.wala \
        -DartifactId=com.ibm.wala.shrike \
        -Dversion=1.5.10 \
        -Dpackaging=jar \
        -DgeneratePom=true     
}

build
echo "MAVEN: WALA DEPENDENCIES COMPLETED."