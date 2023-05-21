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
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.ipa.modref.AstJavaModRef;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cfg.InterproceduralCFG;
import com.ibm.wala.ipa.slicer.*;

import com.ibm.wala.ssa.*;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import org.apache.commons.cli.*;
import org.konveyor.dgi.code2graph.utils.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

public class Code2Graph {

  private static String outDir;

  /**
   * Convert java binary (*.jar, *.ear, *.war) to a neo4j graph.
   *
   * <p>
   * usage: ./code2graph [-h | --help] [-q | --quite] [-i | --input <arg> input jar]
   * [-d | --outdir <arg>] [-o | --output]
   *
   */
  public static void main(String... args) {
    // Set Log Level
    Options options = new Options();
    options.addOption("i", "input", true,
        "Path to the input jar(s). For multiple JARs, separate them with ':'. E.g., file1.jar:file2.jar, etc.");
    options.addOption("e", "extra-libs", true,
            "Path to the extra libraries to consider when processing jar(s). This arg will the the path the to directory.");
    options.addOption("o", "output", true, "Destination (directory) to save the output graphs.");
    options.addOption("q", "quiet", false, "Don't print logs to console.");
    options.addOption("h", "help", false, "Print this help message.");
    CommandLineParser parser = new DefaultParser();

    CommandLine cmd = null;

    String header = "Convert java binary (*.jar, *.ear, *.war) to its equivalent system dependency graph.\n\n";
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
      if (!cmd.hasOption("output")) {
        throw new RuntimeException(
            "[Runtime Exception] Need to provide an output path to save the generated files.\n\n");
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
    String outDir = cmd.getOptionValue("output");
    String extraLibs = cmd.getOptionValue("extra-libs");
    AnalysisScope scope = ScopeUtils.createScope(input, extraLibs);

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

      // Build call graph
      Log.info("Building call graph.");
      long start_time = System.currentTimeMillis();
      PropagationCallGraphBuilder builder = new ZeroOneCFABuilderFactory().make(options, cache, cha);
      CallGraph callGraph = builder.makeCallGraph(options, null);
      long end_time = System.currentTimeMillis();
      Log.done("Finished construction of call graph. Took " + (end_time - start_time)+ " milliseconds.");

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

      // Build IPCFG
      InterproceduralCFG ipcfg_full = new InterproceduralCFG(callGraph,
              n -> n.getMethod().getReference().getDeclaringClass().getClassLoader() == JavaSourceAnalysisScope.SOURCE
                      || n == callGraph.getFakeRootNode() || n == callGraph.getFakeWorldClinitNode());

      // Save SDG, IPCFG, and Call graph as JSON
      Log.info("Saving the system dependency graph");
      Graph2JSON.convert(sdg, callGraph, ipcfg_full, outDir);
      Log.done("SDG saved at " + outDir);

    } catch (ClassHierarchyException | IllegalArgumentException | NullPointerException che) {
      che.printStackTrace();
      System.exit(-1);
    }
  }
}
