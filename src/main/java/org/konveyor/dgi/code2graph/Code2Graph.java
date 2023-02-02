/*
Copyright IBM Corporation 2022

Licensed under the Apache Public License 2.0, Version 2.0 (the "License");
you may not use this file except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.konveyor.dgi.code2graph;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.client.impl.ZeroOneCFABuilderFactory;
import com.ibm.wala.cast.java.ipa.modref.AstJavaModRef;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.slicer.*;

import com.ibm.wala.ssa.*;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import org.apache.commons.cli.*;
import org.konveyor.dgi.code2graph.utils.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Code2Graph {

  private static String outDir;

  /**
   * Convert java binary (*.jar, *.ear, *.war) to a neo4j graph.
   *
   * <p>
   * usage: ./code2graph [-h|--help] [-q|--quite] [-i|--input <arg> input jar]
   * [-d|--outdir <arg>] [-o|--output]
   * output jar]`
   */
  public static void main(String... args) {
    // Set Log Level
    Options options = new Options();
    options.addOption("i", "input", true,
        "Path to the input jar(s). For multiple JARs, separate them with ':'. E.g., file1.jar:file2.jar, etc.");
    options.addOption("t", "graph-type", true, "The type of graph to build. Valid options are \"callgraph\" and \"sdg\", by default both the graphs will be built.");
    options.addOption("d", "outdir", true, "Destination (directory) to save the output graph.");
    options.addOption("o", "outfile", true, "Destination (filename) to save the output graph (as graphml/dot/json).");
    options.addOption("q", "quiet", false, "Don't print logs to console.");
    options.addOption("h", "help", false, "Print this help message.");
    CommandLineParser parser = new DefaultParser();

    CommandLine cmd = null;

    String header = "Convert java binary (*.jar, *.ear, *.war) to a neo4j graph.\n\n";
    HelpFormatter hf = new HelpFormatter();

    try {
      cmd = parser.parse(options, args);
      if (cmd.hasOption("help")) {
        hf.printHelp("./code2graph", header, options, null, true);
        System.exit(0);
      }
      if (cmd.hasOption("quiet")) {
        Log.setVerbosity(false);
      }
      if (!cmd.hasOption("input")) {
        throw new RuntimeException(
            "[Runtime Exception] Need to provide an input JAR to process.\n\n");
      }
      if (!cmd.hasOption("outdir")) {
        throw new RuntimeException(
            "[Runtime Exception] Need to provide an output path to save the generated files.\n\n");
      }
      else {
        outDir = String.valueOf(options.getOption("outdir"));
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      hf.printHelp("./code2graph", header, options, null, true);
      return;
    }
    try {
      run(cmd);
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    System.exit(0);
  }

  /**
   * @param cmd
   * @throws Exception
   */
  private static void run(CommandLine cmd) throws Exception {

    String input = cmd.getOptionValue("input");
    AnalysisScope scope = ScopeUtils.createScope(input);

    // Make class hierarchy
    Log.info("Make class hierarchy.");
    try {
      // Create class hierarchy
      IClassHierarchy cha = ClassHierarchyFactory.make(scope, new ECJClassLoaderFactory(scope.getExclusions()));
      Log.done("Done class hierarchy: " + cha.getNumberOfClasses() + " classes");

      // Initialize analysis options
      AnalysisOptions options = new AnalysisOptions();
      Iterable<Entrypoint> entryPoints = AnalysisUtils.getEntryPoints(cha);
      options.setEntrypoints(entryPoints);
      options.getSSAOptions().setDefaultValues(SymbolTable::getDefaultValue);
      options.setReflectionOptions(ReflectionOptions.NONE);
      IAnalysisCacheView cache = new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory(), options.getSSAOptions());

      String graphType = cmd.getOptionValue("graph-type");

      /*--------------------------------------------------------------------------------------------------------------*/
      // Build the call graph
      Log.info("Building call graph.");
      long start_time = System.currentTimeMillis();
      PropagationCallGraphBuilder builder = new ZeroOneCFABuilderFactory().make(options, cache, cha);
      CallGraph callGraph = builder.makeCallGraph(options, null);
      long end_time = System.currentTimeMillis();
      Log.done(
              "Finished construction of call graph. Took "
                      + (end_time - start_time)
                      + " milliseconds."
      );

      outDir = cmd.getOptionValue("outdir");
      String outFile = cmd.getOptionValue("outfile");
      if (graphType == null || graphType.equals("callgraph")) {
        // Save call graph
        Log.info("Saving callgraph.");
        String extenstion = FilenameUtils.getExtension(outFile);
        switch (extenstion) {
          case "graphml":
            CallGraphUtil.convert2GraphML(callGraph, outDir, "call_graph_"+ outFile);
            break;
          case "json":
            CallGraphUtil.convert2JSON(callGraph, outDir, "call_graph_"+ outFile);
            break;
          case "dot":
            CallGraphUtil.convert2DOT(callGraph, outDir, "call_graph_"+ outFile);
            break;
          default:
            Log.error("Output file not provided or the extension type is unknown.");
            System.exit(1);
        }
        Log.done("Callgraph saved at: " + outDir + "call_graph_"+ outFile);
      }

      if (graphType == null || graphType.equals("sdg")) {
        /*--------------------------------------------------------------------------------------------------------------*/
        // Build SDG graph
        Log.info("Building System Dependency Graph.");
        start_time = System.currentTimeMillis();
        SDG<? extends InstanceKey> sdg = new SDG<>(
                callGraph,
                builder.getPointerAnalysis(),
                new AstJavaModRef<>(),
                Slicer.DataDependenceOptions.NO_HEAP_NO_EXCEPTIONS,
                Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);


        Log.done("Built SDG. Took " + (System.currentTimeMillis() - start_time) + " seconds.");

        // Save SDG as JSON
        Log.info("Saving the system dependency graph");
        SDG2JSON.convert2JSON(sdg, new File(outDir, "sdg_" + outFile));
        Log.done("SDG saved at " + outDir + "/sdg_" + outFile + ".");
      }
    } catch (ClassHierarchyException | IllegalArgumentException | NullPointerException che) {
      che.printStackTrace();
      System.exit(-1);
    }
  }
}
