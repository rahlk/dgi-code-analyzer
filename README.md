![logo](./docs/assets/logo.png)

Native WALA implementation of source code analysis tool for Enterprise Java Applications.

## Prerequisites

Before you begin, ensure you have met the following requirements:

* You have a Linux/MacOS/WSL machine.
* You have installed the latest version of [SDKMan!](sdkman.io/)

## Installing `codeanalyzer`

To install `codeanalyzer`, follow these steps:

#### Install SDKMAN! and GraalVM

1. Install SDKMAN!
   Open your terminal and enter the following command:

```bash
curl -s "https://get.sdkman.io" | bash
```

Follow the on-screen instructions to complete the installation.

2. Open a new terminal or source the SDKMAN! scripts:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

#### Install GraalVM using SDKMAN

1. You can list all available GraalVM versions with:

```bash
sdk list java | grep graalvm
```

2. Install GraalVM 17 (replace 17.x.x with the specific version you want):

```bash
sdk install java 17.x.x-grl
```

3. Set GraalVM 17 as the current Java version (replace 17.x.x with the specific version you want):

```bash
sdk use java 17.x.x-grl
```

#### Build the Project

1. Clone the repository (if you haven't already)

```
git clone git@github.ibm.com:northstar/codeanalyzer
cd codeanalyzer
```

2. Run the Gradle wrapper script to build the project. This will compile the project using GraalVM native image
   compilation:

```bash
./gradlew nativeCompile -PbinDir=$HOME/.local/bin
```

**Note: `-PbinDir` is optional. If not provided, this command places the binaries in  `build/bin`.**

## Using `codeanalyzer`

Assuming the path you provided in `-PbinDir` (in my case `$HOME/.local/bin`) is in your `$PATH`, after installation, you can use `codeanalyzer` by following the below format:

```help
usage: codeanalyzer [-h] [-i <arg>] [-e <arg>] [-o <arg>] [-q]

Convert java binary (*.jar, *.ear, *.war) to its equivalent system
dependency graph.

 -h,--help               Print this help message.
 -i,--input <arg>        Path to the input jar(s). For multiple JARs,
                         separate them with ':'. E.g.,
                         file1.jar:file2.jar, etc.
 -e,--extra-libs <arg>   Path to the extra libraries to consider when
                         processing jar(s). This arg will the the path the
                         to directory.
 -o,--output <arg>       Destination (directory) to save the output
                         graphs.
 -q,--quiet              Don't print logs to console.
```

There are some sample binaries in <https://github.com/rahlk/dgi-sample-applications>. As an example usage, we'll use daytrader8 as shown below:

   ```sh
   ./codeanalyzer  -i /path/to/dgi-sample-applications/daytrader8/binaries -e /path/to/dgi-sample-applications/daytrader8/libs -o /path/to/dgi-sample-applications/daytrader8/output
   ```

This will produce a JSON file called `sdg.json` at `/path/to/dgi-sample-applications/daytrader8/output`.

## LICENSE

```
Copyright IBM Corporation 2023, 2024

Licensed under the Apache Public License 2.0, Version 2.0 (the "License");
you may not use this file except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
