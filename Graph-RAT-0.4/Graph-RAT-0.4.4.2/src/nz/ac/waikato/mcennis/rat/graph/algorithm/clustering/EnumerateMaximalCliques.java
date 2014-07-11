/* * EnumerateMaximalCliques.java * * Created on 15 October 2007, 13:25 * * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt) */package org.mcennis.graphrat.algorithm.clustering;import java.util.Arrays;import java.util.TreeSet;import java.util.Iterator;import java.util.Properties;import java.util.logging.Level;import java.util.logging.Logger;import org.mcennis.graphrat.algorithm.Algorithm;import org.mcennis.graphrat.graph.Clique;import org.mcennis.graphrat.graph.Graph;import org.mcennis.graphrat.actor.Actor;import org.dynamicfactory.descriptors.DescriptorFactory;import org.dynamicfactory.descriptors.InputDescriptor;import org.dynamicfactory.descriptors.InputDescriptorInternal;import org.dynamicfactory.descriptors.OutputDescriptor;import org.dynamicfactory.descriptors.OutputDescriptorInternal;import org.dynamicfactory.descriptors.SettableParameter;import org.dynamicfactory.model.ModelShell;import org.mcennis.graphrat.scheduler.Scheduler;/** * Maximal cliques are defined as a fully connected subgraph such that any  * additional node added to the subgraph is not fully connected. * * This problem is NP-Complete, but can be completed in O(nlog(n)) where n is the  * number of nodes in the graph and exponential over k where k is the maximum  * out degree of the graph. For many applications, including web page hyperlink  * structure and social network data, k is suffeciantly small that the  * algorithm's cost is manageable. * * This a rediscovery of the Bron-Berbosch Algorithm (1973) * C. Bron, J. Kerboach, Algorithm 457. 1973. Finding all cliques of an uncirected graph. * <i>Communications of the ACM</i>. 16(9):575-577. *  * @author Daniel McEnnis * */public class EnumerateMaximalCliques extends ModelShell implements Algorithm {    /**     * set of candidate maximal cliques including the seed node     */    TreeSet<Clique> maximal = new TreeSet<Clique>();    /**     * set of all maximal cliques confirmed to date     */    TreeSet<Clique> globalMaximal = new TreeSet<Clique>();    /**     * All current cliques generated by breadth first search of new maximal     * clique candidates from a given seed node for a given clique size     */    TreeSet<Clique> currentLevel = new TreeSet<Clique>();    /**     * all potential maximal clique candidates generated in the previous step     * all cliques in this set are of the same size.     */    TreeSet<Clique> lastLevel = new TreeSet<Clique>();    /**     * Generates a unique clique name based on the order that the clique is      * discovered.     */    int cliqueCount = 0;    /**     * Description of all parameters that modify how this algorithm interacts      * with other algorithms.  Used to automate parameter setting     */    ParameterInternal[] parameter = new ParameterInternal[4];    /**     * Formal description of all algorithmic inputs used by this algorithm. This     * algorithm requires a link type as input. (Type is specified in paramters)     */    InputDescriptorInternal[] input = new InputDescriptorInternal[1];    /**     * Formal description of all algorithmic outputs.  This algorithm generates      * one property on actors of a given relation. (Specified in parameter      * settings)     */    OutputDescriptorInternal[] output = new OutputDescriptorInternal[1];    /** Creates a new instance of EnumerateMaximalCliques */    public EnumerateMaximalCliques() {        init(null);    }    public void execute(Graph g) {        /*         * Start with every actor as a seed clqiue of one         */        Actor[] allActors = g.getActor((String) parameter[2].getValue());        /**         * Sort ther actor array, giving an actor ranking         */        java.util.Arrays.sort(allActors);        int maxClique = 0;        fireChange(Scheduler.SET_ALGORITHM_COUNT,allActors.length);        /**         * Grow cliques using each actor as a seed node         */        for (int i = 0; i < allActors.length; ++i) {            currentLevel.clear();            lastLevel.clear();            maximal.clear();            int level = 0;            Clique base = new Clique();            g.addChild(base);            base.setID((String) parameter[3].getValue() + cliqueCount);            base.setActorType((String) parameter[2].getValue());            base.setRelation((String) parameter[1].getValue());            base.add(allActors[i]);            lastLevel.add(base);            cliqueCount++;            /**             * Continue iterating over cliques of increasing size until no              * greater cliques are found.  This is bounded by the out degree of              * the seed node.  WARNING this can blow up computationally for              * seeds with massive out degrees (out degree ~= # nodes).             */            while (lastLevel.size() > 0) {                level++;                Iterator<Clique> itLastLevel = lastLevel.iterator();                while (itLastLevel.hasNext()) {                    Clique seed = itLastLevel.next();                    Actor[] intersection = seed.getIntersection();                    if (intersection != null) {                        /**                         * add all new cliques (see helper)                         */                        extend(seed, intersection);                    }                }                lastLevel = currentLevel;                currentLevel = new TreeSet<Clique>();            }            /**             * By only allowing checking of cliqies with only higher actors,             * some non-maximal cliques are included as well             */            assertMaximal(maximal);            /**             * Take all maximal cliques and add them to the set of all cliques             */            globalMaximal.addAll(maximal);            if ((level - 1) > maxClique) {                maxClique = level - 1;            }            fireChange(Scheduler.SET_ALGORITHM_PROGRESS,i);        }        Logger.getLogger(EnumerateMaximalCliques.class.getName()).log(Level.FINE, "Maximal Clique count = " + globalMaximal.size());        Logger.getLogger(EnumerateMaximalCliques.class.getName()).log(Level.FINE, "Max Clique Size " + maxClique);        Iterator<Clique> itGlobalMaximal = globalMaximal.iterator();        while (itGlobalMaximal.hasNext()) {            Clique next = itGlobalMaximal.next();            g.addChild(next);        }        maximal.clear();        allActors = null;        currentLevel.clear();        lastLevel.clear();    }    /**     * create a set of new cliques of size n+1 from a clique of size n.  This      * only need to be done for actors strictly greater than the largest actor      * in the set.  This is acceptable since every clique involving a smaller      * actor will already generate this clique in a previous iteration.       * However, this generates some spurious maximal cliques that are subsets     * of other cliques.  These are weeded out by assertMaximal algorithm     */    protected void extend(Clique seed, Actor[] intersection) {        Arrays.sort(intersection);        Actor largest = seed.getMaxActor();        for (int i = 0; i < intersection.length; ++i) {            if (intersection[i].compareTo(largest) > 0) {                Clique next = seed.expand(intersection[i]);                if (next != null) {                    next.setID((String) parameter[3].getValue() + cliqueCount);                    cliqueCount++;                    currentLevel.add(next);                    maximal.remove(seed);                    maximal.add(next);                }            }        }    }    /**     * This functions identify those cliques that are subsets of maixmal cliques     * rather than maximal cliques themselves.  These are removed from the set      * of maximal cliques before they are added to the global clique set.  The      * test is to sequentially test whether or not there exists a node in the      * intersection that creates a new clique (hence the current clique is not      * maximal).  Since this clique will be created by another clique or seed,     * this clique can be safely discarded.     */    protected void assertMaximal(TreeSet<Clique> maximal) {        Clique[] maximalArray = maximal.toArray(new Clique[]{});        for (int i = 0; i < maximalArray.length; ++i) {            Actor[] intersection = maximalArray[i].getIntersection();            if (intersection != null) {                for (int j = 0; j < intersection.length; ++j) {                    if (maximalArray[i].expand(intersection[j]) != null) {                        maximal.remove(maximalArray[i]);                        break;                    }                }            }        }    }    @Override    public InputDescriptor[] getInputType() {        return input;    }    @Override    public OutputDescriptor[] getOutputType() {        return output;    }    @Override    public Parameter[] getParameter() {        return parameter;    }    @Override    public Parameter getParameter(String param) {        for (int i = 0; i < parameter.length; ++i) {            if (parameter[i].getName().contentEquals(param)) {                return parameter[i];            }        }        return null;    }    @Override    public SettableParameter[] getSettableParameter() {        return null;    }    @Override    public SettableParameter getSettableParameter(String param) {        return null;    }    /**     * Parameters for init:     * <ol>     * <li>'name' - name for this instance of the algorithm. Default 'Enumerate Maximal Cliques'.     * <li>'relation' - name of the type (relation) of link to be used. Default     * 'Knows'.     * <li>'actorType' - naem of the actor type (mode) to be used. Default 'User'.     * <li>'graphIDPrefix' - name of the prefix for the ids  of generated graphs.     * Default 'clique'.     * </ol>     * <br>     * <br>Input  1 - Link     * <br>Output 1 - Graph     */    public void init(Properties map) {        // set all parameters        Properties props = new Properties();        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "name");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[0] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("name") != null)) {            parameter[0].setValue(map.getProperty("name"));        } else {            parameter[0].setValue("Enumerate Maximal Cliques");        }        // Parameter 1 - relation        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "relation");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[1] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("relation") != null)) {            parameter[1].setValue(map.getProperty("relation"));        } else {            parameter[1].setValue("Knows");        }        // Parameter 2 - actor type        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "actorType");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[2] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("actorType") != null)) {            parameter[2].setValue(map.get("actorType"));        } else {            parameter[2].setValue("User");        }        // Parameter 3 - graph ID prefix        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "graphIDPrefix");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[3] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("graphIDPrefix") != null)) {            parameter[3].setValue(map.get("graphIDPrefix"));        } else {            parameter[3].setValue("clique");        }        // init input 0        props.setProperty("Type", "Link");        props.setProperty("Relation", (String) parameter[1].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.remove("Property");        input[0] = DescriptorFactory.newInstance().createInputDescriptor(props);        // init output 0        // Construct Output Descriptors        props.setProperty("Type", "Graph");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.remove("Property");        output[0] = DescriptorFactory.newInstance().createOutputDescriptor(props);    }}