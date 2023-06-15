# Copyright IBM Corporation 2023
#
# Licensed under the Apache Public License 2.0, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# 1. Use Ubuntu 22.04 as base image
FROM ubuntu:22.04

# Update the package repository and install necessary packages
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk gradle wget unzip

# Create the directories
RUN mkdir /binaries /dependencies /output /libs

WORKDIR /libs

# Download a base set of dependencies
# Java EE Libraries

# Java API for RESTful Web Services (JAX-RS)
ARG JAX_RS_VERSION
RUN wget https://repo1.maven.org/maven2/javax/ws/rs/javax.ws.rs-api/${JAX_RS_VERSION}/javax.ws.rs-api-${JAX_RS_VERSION}.jar

# Java API for JSON Processing (JSON-P)
ARG JSON_P_VERSION
RUN wget https://repo1.maven.org/maven2/javax/json/javax.json-api/${JSON_P_VERSION}/javax.json-api-${JSON_P_VERSION}.jar

# Java API for WebSocket
ARG WEBSOCKET_VERSION
RUN wget https://repo1.maven.org/maven2/javax/websocket/javax.websocket-api/${WEBSOCKET_VERSION}/javax.websocket-api-${WEBSOCKET_VERSION}.jar

# JavaMail API
ARG JAVAMAIL_VERSION
RUN wget https://repo1.maven.org/maven2/javax/mail/mail/${JAVAMAIL_VERSION}/mail-${JAVAMAIL_VERSION}.jar

# Bean Validation API
ARG VALIDATION_API_VERSION
RUN wget https://repo1.maven.org/maven2/javax/validation/validation-api/${VALIDATION_API_VERSION}/validation-api-${VALIDATION_API_VERSION}.jar

# Java Persistence API (JPA)
ARG JPA_VERSION
RUN wget https://repo1.maven.org/maven2/javax/persistence/javax.persistence-api/${JPA_VERSION}/javax.persistence-api-${JPA_VERSION}.jar

# Java Transaction API (JTA)
ARG JTA_VERSION
RUN wget https://repo1.maven.org/maven2/javax/transaction/jta/${JTA_VERSION}/jta-${JTA_VERSION}.jar

# Servlet API
ARG SERVLET_VERSION
RUN wget https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/${SERVLET_VERSION}/javax.servlet-api-${SERVLET_VERSION}.jar

# Java EE API (version 7.0)
ARG JAVAEE7_VERSION
RUN wget https://repo1.maven.org/maven2/javax/javaee-api/${JAVAEE7_VERSION}/javaee-api-${JAVAEE7_VERSION}.jar

# Java EE API (version 8.0)
ARG JAVAEE8_VERSION
RUN wget https://repo1.maven.org/maven2/javax/javaee-api/${JAVAEE8_VERSION}/javaee-api-${JAVAEE8_VERSION}.jar

# Spring Boot
ARG SPRING_BOOT_VERSION
RUN wget https://repo1.maven.org/maven2/org/springframework/boot/spring-boot/${SPRING_BOOT_VERSION}/spring-boot-${SPRING_BOOT_VERSION}.jar

# Apache Derby
ARG DERBY_VERSION
RUN wget https://repo1.maven.org/maven2/org/apache/derby/derby/${DERBY_VERSION}/derby-${DERBY_VERSION}.jar

# 2. Copy the project directory into the container
COPY . /app/

# Set the working directory to /app
WORKDIR /app

# Give execute permission for the gradlew script
RUN chmod +x ./gradlew

# Build the Gradle project
RUN ./gradlew build
# 3. Copy your bash script into the container
COPY ./codeanalyzer /

# Make your bash script executable
RUN chmod +x /codeanalyzer

# Set your bash script as the entrypoint
ENTRYPOINT ./gradlew --stacktrace --quiet clean codeanalyzer -Pargs="--input=/binaries --app-dep=/dependencies --extra-libs=/libs --output=/output"
