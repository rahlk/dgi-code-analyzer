# DGI Code Analyzer

Native WALA implementation of source code analysis tool for Enterprise Java Applications.

```
                                                                                                                                                                                                                                        
                                                  dddddddd                                                                                                                                                                              
        CCCCCCCCCCCCC                             d::::::d                                 AAA                                                 lllllll                                                                                  
     CCC::::::::::::C                             d::::::d                                A:::A                                                l:::::l                                                                                  
   CC:::::::::::::::C                             d::::::d                               A:::::A                                               l:::::l                                                                                  
  C:::::CCCCCCCC::::C                             d:::::d                               A:::::::A                                              l:::::l                                                                                  
 C:::::C       CCCCCC   ooooooooooo       ddddddddd:::::d     eeeeeeeeeeee             A:::::::::A         nnnn  nnnnnnnn      aaaaaaaaaaaaa    l::::lyyyyyyy           yyyyyyyzzzzzzzzzzzzzzzzz    eeeeeeeeeeee    rrrrr   rrrrrrrrr   
C:::::C               oo:::::::::::oo   dd::::::::::::::d   ee::::::::::::ee          A:::::A:::::A        n:::nn::::::::nn    a::::::::::::a   l::::l y:::::y         y:::::y z:::::::::::::::z  ee::::::::::::ee  r::::rrr:::::::::r  
C:::::C              o:::::::::::::::o d::::::::::::::::d  e::::::eeeee:::::ee       A:::::A A:::::A       n::::::::::::::nn   aaaaaaaaa:::::a  l::::l  y:::::y       y:::::y  z::::::::::::::z  e::::::eeeee:::::eer:::::::::::::::::r 
C:::::C              o:::::ooooo:::::od:::::::ddddd:::::d e::::::e     e:::::e      A:::::A   A:::::A      nn:::::::::::::::n           a::::a  l::::l   y:::::y     y:::::y   zzzzzzzz::::::z  e::::::e     e:::::err::::::rrrrr::::::r
C:::::C              o::::o     o::::od::::::d    d:::::d e:::::::eeeee::::::e     A:::::A     A:::::A       n:::::nnnn:::::n    aaaaaaa:::::a  l::::l    y:::::y   y:::::y          z::::::z   e:::::::eeeee::::::e r:::::r     r:::::r
C:::::C              o::::o     o::::od:::::d     d:::::d e:::::::::::::::::e     A:::::AAAAAAAAA:::::A      n::::n    n::::n  aa::::::::::::a  l::::l     y:::::y y:::::y          z::::::z    e:::::::::::::::::e  r:::::r     rrrrrrr
C:::::C              o::::o     o::::od:::::d     d:::::d e::::::eeeeeeeeeee     A:::::::::::::::::::::A     n::::n    n::::n a::::aaaa::::::a  l::::l      y:::::y:::::y          z::::::z     e::::::eeeeeeeeeee   r:::::r            
 C:::::C       CCCCCCo::::o     o::::od:::::d     d:::::d e:::::::e             A:::::AAAAAAAAAAAAA:::::A    n::::n    n::::na::::a    a:::::a  l::::l       y:::::::::y          z::::::z      e:::::::e            r:::::r            
  C:::::CCCCCCCC::::Co:::::ooooo:::::od::::::ddddd::::::dde::::::::e           A:::::A             A:::::A   n::::n    n::::na::::a    a:::::a l::::::l       y:::::::y          z::::::zzzzzzzze::::::::e           r:::::r            
   CC:::::::::::::::Co:::::::::::::::o d:::::::::::::::::d e::::::::eeeeeeee  A:::::A               A:::::A  n::::n    n::::na:::::aaaa::::::a l::::::l        y:::::y          z::::::::::::::z e::::::::eeeeeeee   r:::::r            
     CCC::::::::::::C oo:::::::::::oo   d:::::::::ddd::::d  ee:::::::::::::e A:::::A                 A:::::A n::::n    n::::n a::::::::::aa:::al::::::l       y:::::y          z:::::::::::::::z  ee:::::::::::::e   r:::::r            
        CCCCCCCCCCCCC   ooooooooooo      ddddddddd   ddddd    eeeeeeeeeeeeeeAAAAAAA                   AAAAAAAnnnnnn    nnnnnn  aaaaaaaaaa  aaaallllllll      y:::::y           zzzzzzzzzzzzzzzzz    eeeeeeeeeeeeee   rrrrrrr            
                                                                                                                                                            y:::::y                                                                     
                                                                                                                                                           y:::::y                                                                      
                                                                                                                                                          y:::::y                                                                       
                                                                                                                                                         y:::::y                                                                        
                                                                                                                                                        yyyyyyy                                                                         
                                                                                                                                                                                                                                        
```                                                                                                                                                                                                                                        

## Code of Conduct
Refer to Konveyor's Code of Conduct [here](https://github.com/konveyor/community/blob/main/CODE_OF_CONDUCT.md).


## Prequisits 

- Java 11 (For usage as a standalone tool)
- Docker (For usage as a container)

## Usage

```help                                                  
usage: ./codeanalyzer [-h] [-i <arg>] [-e <arg>] [-o <arg>] [-q]

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

### 1. As a standalone tool

There are some sample binaries in github.com/rahlk/dgi-sample-applications. As an example usage, we'll use daytrader8 as shown below:

   ```sh
   ./codeanalyzer  -i /path/to/dgi-sample-applications/daytrader8/binaries -e /path/to/dgi-sample-applications/daytrader8/libs -o /path/to/dgi-sample-applications/daytrader8/output
   ```
   ```
   2023-06-15T02:08:35.375035      [INFO]  Create analysis scope.
   2023-06-15T02:08:35.478729      [INFO]  Add exclusions to scope.
   2023-06-15T02:08:35.480074      [INFO]  Loading Java SE standard libs.
   2023-06-15T02:08:35.553166      [INFO]  Loading user specified extra libs.
   2023-06-15T02:08:35.563673      [INFO]  -> Adding dependency activation-1.1.jar to analysis scope.
   2023-06-15T02:08:35.574235      [INFO]  -> Adding dependency derby-10.14.2.0.jar to analysis scope.
   2023-06-15T02:08:35.586332      [INFO]  -> Adding dependency javaee-api-8.0.jar to analysis scope.
   2023-06-15T02:08:35.597902      [INFO]  -> Adding dependency javax.mail-1.6.0.jar to analysis scope.
   2023-06-15T02:08:35.607116      [INFO]  -> Adding dependency jaxb-api-2.3.0.jar to analysis scope.
   2023-06-15T02:08:35.613697      [INFO]  -> Adding dependency standard-1.1.1.jar to analysis scope.
   2023-06-15T02:08:35.636437      [INFO]  Loading application jar(s).
   2023-06-15T02:08:35.639916      [INFO]  -> Adding application daytrader8.jar to analysis scope.
   2023-06-15T02:08:35.647837      [INFO]  Make class hierarchy.
   2023-06-15T02:08:37.823779      [DONE]  There were a total of 16016 classes of which 155 are application classes.
   2023-06-15T02:08:37.904373      [INFO]  Registered 1244 entrypoints.
   2023-06-15T02:08:37.923757      [INFO]  Building call graph.
   2023-06-15T02:08:47.558413      [DONE]  Finished construction of call graph. Took 10.0 seconds.
   2023-06-15T02:08:47.559886      [INFO]  Building System Dependency Graph.
   2023-06-15T02:08:47.574035      [INFO]  Pruning SDG to keep only Application classes.
   2023-06-15T02:08:50.358856      [DONE]  SDG built and pruned. It has 32120 nodes.
   2023-06-15T02:08:52.383651      [DONE]  SDG saved at /output
   ```

This will produce a JSON file called `sdg.json` at `/path/to/dgi-sample-applications/daytrader8/output`.

### 2. For usage as a docker container

A containerized version of code-analyzer is available at quay.io/rkrsn/code-analyzer. As an example usage, we'll 
again use daytrader8 as shown below:

   ```sh
   docker run --rm \
      -v /path/to/dgi-sample-applications/daytrader8/daytrader8/binaries:/binaries \
      -v /path/to/dgi-sample-applications/daytrader8/daytrader8/libs/:/dependencies \
      -v /path/to/dgi-sample-applications/daytrader8/daytrader8/output/:/output \
      quay.io/rkrsn/code-analyzer:latest 
   ```
   ```
   2023-06-15T02:08:35.375035      [INFO]  Create analysis scope.
   2023-06-15T02:08:35.478729      [INFO]  Add exclusions to scope.
   2023-06-15T02:08:35.480074      [INFO]  Loading Java SE standard libs.
   2023-06-15T02:08:35.553166      [INFO]  Loading user specified extra libs.
   2023-06-15T02:08:35.563673      [INFO]  -> Adding dependency activation-1.1.jar to analysis scope.
   2023-06-15T02:08:35.574235      [INFO]  -> Adding dependency derby-10.14.2.0.jar to analysis scope.
   2023-06-15T02:08:35.586332      [INFO]  -> Adding dependency javaee-api-8.0.jar to analysis scope.
   2023-06-15T02:08:35.597902      [INFO]  -> Adding dependency javax.mail-1.6.0.jar to analysis scope.
   2023-06-15T02:08:35.607116      [INFO]  -> Adding dependency jaxb-api-2.3.0.jar to analysis scope.
   2023-06-15T02:08:35.613697      [INFO]  -> Adding dependency standard-1.1.1.jar to analysis scope.
   2023-06-15T02:08:35.636437      [INFO]  Loading application jar(s).
   2023-06-15T02:08:35.639916      [INFO]  -> Adding application daytrader8.jar to analysis scope.
   2023-06-15T02:08:35.647837      [INFO]  Make class hierarchy.
   2023-06-15T02:08:37.823779      [DONE]  There were a total of 16016 classes of which 155 are application classes.
   2023-06-15T02:08:37.904373      [INFO]  Registered 1244 entrypoints.
   2023-06-15T02:08:37.923757      [INFO]  Building call graph.
   2023-06-15T02:08:47.558413      [DONE]  Finished construction of call graph. Took 10.0 seconds.
   2023-06-15T02:08:47.559886      [INFO]  Building System Dependency Graph.
   2023-06-15T02:08:47.574035      [INFO]  Pruning SDG to keep only Application classes.
   2023-06-15T02:08:50.358856      [DONE]  SDG built and pruned. It has 32120 nodes.
   2023-06-15T02:08:52.383651      [DONE]  SDG saved at /output
   ```

## LICENSE

```
Copyright IBM Corporation 2023

Licensed under the Apache Public License 2.0, Version 2.0 (the "License");
you may not use this file except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
