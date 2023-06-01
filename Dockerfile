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
RUN mkdir /binaries /dependencies /output

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
ENTRYPOINT ./gradlew --stacktrace --quiet clean codeanalyzer -Pargs="--input=/binaries --extra-libs=/dependencies --output=/output"
