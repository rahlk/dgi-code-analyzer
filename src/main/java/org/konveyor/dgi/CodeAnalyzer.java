/*
Copyright IBM Corporation 2023

Licensed under the Apache Public License 2.0, Version 2.0 (the "License");
you may not use this file except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.konveyor.dgi;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.client.impl.ZeroOneCFABuilderFactory;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.cfg.InterproceduralCFG;
import com.ibm.wala.ipa.slicer.*;

import com.ibm.wala.ssa.*;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.modref.ModRef;

import org.konveyor.dgi.utils.*;

// PicoCLI command line parser
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(name = "codeanalyzer", mixinStandardHelpOptions = true, version = "1.0", description = "Convert java binary (*.jar, *.ear, *.war) to a neo4j graph.")
public class CodeAnalyzer implements Runnable {

  @Option(names = { "-i", "--input" }, required = true, description = "Path to the input jar(s).")
  private String input;

  @Option(names = { "-d", "--app-deps" }, description = "Path to the application dependencies.")
  private String appDeps;

  @Option(names = { "-e", "--extra-libs" }, description = "Path to the extra libraries.")
  private String extraLibs;

  @Option(names = { "-o",
      "--output" }, required = true, description = "Destination directory to save the output graphs.")
  private String outDir;

  @Option(names = { "-q", "--quiet" }, description = "Don't print logs to console.")
  private boolean quiet = false;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new CodeAnalyzer()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    // Set log level based on quiet option
    Log.setVerbosity(!quiet);
    try {
      analyze(input, outDir, appDeps, extraLibs);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassHierarchyException e) {
      throw new RuntimeException(e);
    } catch (CallGraphBuilderCancelException e) {
      throw new RuntimeException(e);
    }
  }

  private static void analyze(String input, String outDir, String appDeps, String extraLibs)
      throws IOException, ClassHierarchyException, CallGraphBuilderCancelException {

    AnalysisScope scope = ScopeUtils.createScope(input, extraLibs, appDeps);

    // Make class hierarchy
    Log.info("Make class hierarchy.");
    IClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope, new ECJClassLoaderFactory(scope.getExclusions()));
    Log.done("There were a total of " + cha.getNumberOfClasses() + " classes of which "
        + AnalysisUtils.getNumberOfApplicationClasses(cha) + " are application classes.");

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
    Log.done("Finished construction of call graph. Took "
        + Math.ceil((double) (System.currentTimeMillis() - start_time) / 1000) + " seconds.");

    // Build SDG graph
    Log.info("Building System Dependency Graph.");
    SDG<? extends InstanceKey> sdg = new SDG<>(
        callGraph,
        builder.getPointerAnalysis(),
        new ModRef<>(),
        Slicer.DataDependenceOptions.NO_HEAP_NO_EXCEPTIONS,
        Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);

    // Build IPCFG
    InterproceduralCFG ipcfg_full = new InterproceduralCFG(callGraph,
        n -> n.getMethod().getReference().getDeclaringClass().getClassLoader() == JavaSourceAnalysisScope.SOURCE
            || n == callGraph.getFakeRootNode() || n == callGraph.getFakeWorldClinitNode());

    // Save SDG, IPCFG, and Call graph as JSON
    Graph2JSON.convert(sdg, callGraph, ipcfg_full, outDir);
    Log.done("SDG saved at " + outDir);
  }
}
