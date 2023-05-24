package org.konveyor.dgi.utils;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cfg.InterproceduralCFG;
import com.ibm.wala.ipa.slicer.MethodEntryStatement;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.traverse.DFS;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.json.JSONExporter;
import org.konveyor.dgi.utils.graph.*;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * The type Sdg 2 json.
 */
public class Graph2JSON {

    private static JSONExporter<AbstractGraphNode, AbstractGraphEdge> getGraphExporter() {
        JSONExporter<AbstractGraphNode, AbstractGraphEdge> exporter = new JSONExporter<>(v -> String.valueOf(v.getId()));
        exporter.setVertexAttributeProvider(AbstractGraphNode::getAttributes);
        exporter.setEdgeAttributeProvider(AbstractGraphEdge::getAttributes);
        return exporter;
    }

    private static org.jgrapht.Graph<AbstractGraphNode, AbstractGraphEdge> buildGraph(Supplier<Iterator<Statement>> entryPoints,
                                                                                      Graph<Statement> sdg, CallGraph callGraph,
                                                                                      BiFunction<Statement, Statement, String> edgeLabels) {

        org.jgrapht.Graph<AbstractGraphNode, AbstractGraphEdge> graph = new DefaultDirectedGraph<>(AbstractGraphEdge.class);
        // We'll use forward and backward search on the DFS to identify which CFG nodes are dominant
        // This is a forward DFS search (or exit time first search)
        int dfsNumber = 0;
        Map<Statement,Integer> dfsFinish = HashMapFactory.make();
        Iterator<Statement> search = DFS.iterateFinishTime(sdg, entryPoints.get());
        while (search.hasNext()) {
            dfsFinish.put(search.next(), dfsNumber++);
        }

        // This is a reverse DFS search (or entry time first search)
        int reverseDfsNumber = 0;
        Map<Statement,Integer> dfsStart = HashMapFactory.make();
        Iterator<Statement> reverseSearch = DFS.iterateDiscoverTime(sdg, entryPoints.get());
        while (reverseSearch.hasNext()) {
            dfsStart.put(reverseSearch.next(), reverseDfsNumber++);
        }

        // Populate graph
        sdg.stream()
                .filter(dfsFinish::containsKey)
                .sorted(Comparator.comparingInt(dfsFinish::get))
                .forEach(p -> sdg.getSuccNodes(p).forEachRemaining(s -> {
                    if (dfsFinish.containsKey(s)
                            && !((dfsStart.get(p) >= dfsStart.get(s))
                            && (dfsFinish.get(p) <= dfsFinish.get(s)))
                            && !p.getNode().getMethod().equals(s.getNode().getMethod())) {

                        // Build source and destination nodes
                        MethodNode source = new MethodNode(p.getNode().getMethod());
                        MethodNode target = new MethodNode(s.getNode().getMethod());

                        // Add the nodes to the graph as vertices
                        graph.addVertex(source);
                        graph.addVertex(target);

                        String edgeType = edgeLabels.apply(p, s);
                        SystemDepEdge graphEdge = new SystemDepEdge(p, s, edgeType);
                        SystemDepEdge cgEdge = (SystemDepEdge) graph.getEdge(source, target);
                        if (cgEdge == null || !cgEdge.equals(graphEdge)) {
                            graph.addEdge(
                                    source,
                                    target,
                                    graphEdge);
                        } else {
                            graphEdge.incrementWeight();
                        }
                    }
                }));

        callGraph.getEntrypointNodes()
                .forEach(p -> {
                    // Get call statements that may execute in a given method
                    Iterator<CallSiteReference> outGoingCalls = p.iterateCallSites();
                    outGoingCalls.forEachRemaining( n -> {
                        callGraph.getPossibleTargets(p, n).stream().filter(o -> AnalysisUtils.isApplicationClass(o.getMethod().getDeclaringClass()))
                                .forEach(o -> {
                            MethodNode source = new MethodNode(p.getMethod());
                            MethodNode target = new MethodNode(o.getMethod());
                            if (!source.equals(target)) {
                                graph.addVertex(source);
                                graph.addVertex(target);
                                // Get the edge between the source and the target
                                AbstractGraphEdge cgEdge = graph.getEdge(source, target);

                                if (cgEdge == null) {
                                    graph.addEdge(source, target, new CallEdge());
                                }
                                // If edge exists, then increment the weight
                                else {
                                    cgEdge.incrementWeight();
                                }
                            }});
                        });
                });

        return graph;
    }

    public static void convert(SDG<? extends InstanceKey> sdg, CallGraph cg, InterproceduralCFG ipcfg_full, String outDir) {
        // Prune the Graph to keep only application classes.
        Log.info("Pruning SDG to keep only Application classes.");
        Graph<Statement> prunedGraph = GraphSlicer.prune(sdg,
                statement -> (
                        statement.getNode()
                                .getMethod()
                                .getDeclaringClass()
                                .getClassLoader()
                                .getReference()
                                .equals(ClassLoaderReference.Application)
                )
        );
        Log.done("SDG built and pruned. It has " + prunedGraph.getNumberOfNodes() + " nodes.");

        CallGraph callGraph = sdg.getCallGraph();

        // A supplier to get entries
        Supplier<Iterator<Statement>> sdgEntryPointsSupplier =
                () -> callGraph.getEntrypointNodes().stream().map(n -> (Statement)new MethodEntryStatement(n)).iterator();


        org.jgrapht.Graph<AbstractGraphNode, AbstractGraphEdge> sdg_graph = buildGraph(
                sdgEntryPointsSupplier,
                prunedGraph, callGraph,
                (p, s) -> String.valueOf(sdg.getEdgeLabels(p, s).iterator().next()));

        // Save the SDG as JSON
        JSONExporter<AbstractGraphNode, AbstractGraphEdge> sdg_exporter = getGraphExporter();
        sdg_exporter.exportGraph(sdg_graph, new File(outDir, "sdg.json"));
    }
}
