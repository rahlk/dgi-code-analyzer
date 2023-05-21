# dgi-code-analyzer
Native WALA implementation of source code analysis tool for Enterprise Java Applications.


## Code of Conduct
Refer to Konveyor's Code of Conduct [here](https://github.com/konveyor/community/blob/main/CODE_OF_CONDUCT.md).


## Prequisits 

- Java 11

## Usage

```man
   _____            _        ___    _____                     _     
  / ____|          | |      |__ \  / ____|                   | |    
 | |      ___    __| |  ___    ) || |  __  _ __  __ _  _ __  | |__  
 | |     / _ \  / _` | / _ \  / / | | |_ || '__|/ _` || '_ \ | '_ \ 
 | |____| (_) || (_| ||  __/ / /_ | |__| || |  | (_| || |_) || | | |
  \_____|\___/  \__,_| \___||____| \_____||_|   \__,_|| .__/ |_| |_|
                                                      | |           
                                                      |_|           
                                                      
usage: ./code2graph [-h] [-i <arg>] [-e <arg>] [-o <arg>] [-q]

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

### Examples

There are some sample binaries in `etc/demo`. An example usage is shown below:

1. Process EAR files
   ```sh
   ./code2graph -i etc/demo/daytrader7/binary/daytrader-ee7.ear -e etc/demo/daytrader7/dependencies/ -o etc/demo/daytrader7/output

   2023-05-21T00:16:33.475721        [INFO]  Create analysis scope.
   2023-05-21T00:16:33.546258        [INFO]  Add exclusions to scope.
   2023-05-21T00:16:33.547106        [INFO]  Loading Java SE standard libs.
   2023-05-21T00:16:33.582578        [INFO]  Loading user specified extra libs.
   2023-05-21T00:16:33.589225        [INFO]  ↪ Adding etc/demo/daytrader7/dependencies/activation-1.1.jar to scope.
   2023-05-21T00:16:33.589612        [INFO]  ↪ Adding etc/demo/daytrader7/dependencies/javax.annotation-api-1.3.2.jar to scope.
   2023-05-21T00:16:33.589863        [INFO]  ↪ Adding etc/demo/daytrader7/dependencies/javax.mail-1.5.0.jar to scope.
   2023-05-21T00:16:33.590170        [INFO]  ↪ Adding etc/demo/daytrader7/dependencies/javaee-api-7.0.jar to scope.
   2023-05-21T00:16:33.805806        [INFO]  Make class hierarchy.
   2023-05-21T00:16:35.229173        [DONE]  Done class hierarchy: 15921 classes
   2023-05-21T00:16:35.257127        [INFO]  Registered 1000 entrypoints.
   2023-05-21T00:16:35.266491        [INFO]  Building call graph.
   2023-05-21T00:16:39.708361        [DONE]  Finished construction of call graph. Took 5.0 seconds.
   2023-05-21T00:16:39.708481        [INFO]  Building System Dependency Graph.
   2023-05-21T00:16:39.722452        [INFO]  Pruning SDG to keep only Application classes.
   2023-05-21T00:16:41.328015        [DONE]  SDG built and pruned. It has 31321 nodes.
   2023-05-21T00:16:42.334647        [DONE]  SDG saved at etc/demo/daytrader7/output
   ```

This will produce a JSON file called `sdg.json` at `etc/demo/daytrader7/output`.