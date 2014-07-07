/* * AddGeodesicPaths.java * * Created on 5 July 2007, 19:25 * * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt) */package org.mcennis.graphrat.prestige;import java.util.Properties;import org.mcennis.graphrat.algorithm.Algorithm;import org.mcennis.graphrat.graph.Graph;import org.dynamicfactory.descriptors.DescriptorFactory;import org.dynamicfactory.descriptors.InputDescriptor;import org.dynamicfactory.descriptors.OutputDescriptor;import org.dynamicfactory.descriptors.SettableParameter;import org.mcennis.graphrat.path.Path;import org.mcennis.graphrat.path.PathFactory;import org.mcennis.graphrat.path.PathSet;import org.mcennis.graphrat.path.PathSetFactory;import org.mcennis.graphrat.actor.Actor;import org.mcennis.graphrat.link.Link;import java.util.HashSet;import java.util.HashMap;import java.util.Vector;import java.util.Iterator;import java.util.logging.Level;import java.util.logging.Logger;import org.dynamicfactory.model.ModelShell;import org.mcennis.graphrat.scheduler.Scheduler;/** * Optimized version of Closeness as defined by Freeman79.  This version integrates * path calculations into the loop.  While still O(n2) in time, it is now O(n) in space. * <br> * Freeman, L. "Centrality in social networks: I. Conceptual clarification." * <i>Social Networks</i>. 1:215--239. * * @author Daniel McEnnis *  */public class AddCombinedCloseness extends ModelShell implements Algorithm {    public static final long serialVersionUID = 2;    HashSet<String> usedCount;    HashMap<String, Integer> map;    HashMap<String, Vector<String>> nodeExpansion;    Actor[] userList;    PathSet lastPath;    PathSet nextPath;    HashSet<String> seedNodes;    double[] prestigeValue = null;    double[] centralityValue = null;    double maxPrestige = 0.0;    double maxCentrality = 0.0;    ParameterInternal[] parameter = new ParameterInternal[4];    InputDescriptor[] input = new InputDescriptor[1];    OutputDescriptor[] output = new OutputDescriptor[6];    /** Creates a new instance of AddGeodesicPaths */    public AddCombinedCloseness() {        init(null);    }    /**     * Uses Djikstra's algorithm for spanning trees. After the spanning tree for     * each user, calcualate closeness centrality and prestige for the given node.     * This reduces space requirements to O(n) from O(n2).     */    public void execute(Graph g) {        try {            java.util.Properties props = new java.util.Properties();            props.setProperty("PathSetType", "Basic");            seedNodes = new HashSet<String>();            usedCount = new HashSet<String>();            nodeExpansion = new HashMap<String, Vector<String>>();            userList = g.getActor((String) parameter[2].getValue());            java.util.Arrays.sort(userList);            fireChange(Scheduler.SET_ALGORITHM_COUNT,userList.length);            int count = 0;            prestigeValue = new double[userList.length];            java.util.Arrays.fill(prestigeValue, 0.0);            centralityValue = new double[userList.length];            java.util.Arrays.fill(centralityValue, 0.0);            //            // For every user, calculate all geodesics.            //            // NOTE: iff path p is a geodesic of length n+1 iff the first n nodes            // form a geodesic and node n+1 does not occur in any shorter path.            //            // NOTE: this method enumerates all geodesics in order of length.  It            // ceases searching after consecutive expansions with no new nodes -            // sign of a disconnected graph.            //            // For every user            //    For each length geodesics            //            for (int i = 0; i < userList.length; ++i) {                Logger.getLogger(AddCombinedCloseness.class.getName()).log(Level.FINE, "Geodesic set " + i + " of " + userList.length);                seedNodes.clear();                seedNodes.add(userList[i].getID());                usedCount.clear();                usedCount.add(userList[i].getID());                nodeExpansion.clear();                java.util.Properties properties = new java.util.Properties();                properties.setProperty("PathType", "Basic");                properties.setProperty("PathID", "Base");                properties.setProperty("PathSetType", "Basic");                properties.setProperty("PathSetID", "Base");                Path base = PathFactory.newInstance().create(properties);                lastPath = PathSetFactory.newInstance().create(properties);                base.setPath(new Actor[]{userList[i]}, 0.0, "Base");                lastPath.addPath(base);                // While there exists nodes to be found or new nodes are added                while ((usedCount.size() < userList.length) && (lastPath.size() > 0)) {                    // For every last node in list                    props.setProperty("PathSetID", "Directional Geodesic by " + parameter[1].getValue());                    nextPath = PathSetFactory.newInstance().create(properties);                    nextStepNodes(g);                    processVectors(g, i);                    lastPath = nextPath;                    count++;                }                fireChange(Scheduler.SET_ALGORITHM_PROGRESS,i);            }// for every user            // Calculate Closeness centrality vector            props = new Properties();            for (int i = 0; i < prestigeValue.length; ++i) {                if (prestigeValue[i] != 0) {                    prestigeValue[i] = 1 / prestigeValue[i];                    if (prestigeValue[i] > maxPrestige) {                        maxPrestige = prestigeValue[i];                    }                } else {                    prestigeValue[i] = 0.0;                }                if (centralityValue[i] != 0) {                    centralityValue[i] = 1 / centralityValue[i];                    if (centralityValue[i] > maxCentrality) {                        maxCentrality = centralityValue[i];                    }                } else {                    centralityValue[i] = 0.0;                }                if (maxPrestige < prestigeValue[i]) {                    maxPrestige = prestigeValue[i];                }                if (maxCentrality < centralityValue[i]) {                    maxCentrality = centralityValue[i];                }            }            // Normalize if necessary            if (((Boolean) parameter[3].getValue()).booleanValue()) {                double norm = 0.0;                for (int i = 0; i < centralityValue.length; ++i) {                    norm += centralityValue[i] * centralityValue[i];                }                for (int i = 0; i < centralityValue.length; ++i) {                    centralityValue[i] /= Math.sqrt(norm);                }            }            if (((Boolean) parameter[3].getValue()).booleanValue()) {                double norm = 0.0;                for (int i = 0; i < prestigeValue.length; ++i) {                    norm += prestigeValue[i] * prestigeValue[i];                }                for (int i = 0; i < prestigeValue.length; ++i) {                    prestigeValue[i] /= Math.sqrt(norm);                }            }            //create and set the properties defined            for (int i = 0; i < prestigeValue.length; ++i) {                props.setProperty("PropertyType", "Basic");                props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessCentrality");                Property prop = PropertyFactory.newInstance().create(props);                if (centralityValue[i] != 0.0) {                    prop.add(Double.toString(centralityValue[i]));                } else {                    prop.add(Double.toString(Double.NaN));                }                userList[i].add(prop);                props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");                props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessPrestige");                prop = PropertyFactory.newInstance().create(props);                if (prestigeValue[i] != 0.0) {                    prop.add(new Double(prestigeValue[i]));                } else {                    prop.add(Double.toString(Double.NaN));                }                userList[i].add(prop);            }        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddCombinedCloseness.class.getName()).log(Level.SEVERE, "Property "+(String) parameter[1].getValue()+" ClosenessPrestige does not match class java.lang.Double", ex);        }        calculateGraphCentrality(g);        calculateGraphCentralitySD(g);        calculateGraphPrestige(g);        calculateGraphPrestigeSD(g);    }    /**     * Place node expansion into node expansion map.  This must be done every     * user becuase this filters out all nodes that would not create a new     * geodesic.     *     * @param g graph containing the nodes to be parsed.     */    protected void nextStepNodes(Graph g) {        nodeExpansion.clear();        Iterator<String> seed = seedNodes.iterator();        HashSet<String> nextSeed = new HashSet<String>();        while (seed.hasNext()) {            String key = seed.next();            Link[] linkedNodes = g.getLinkBySource((String) parameter[1].getValue(), g.getActor((String) parameter[2].getValue(), key));            if (linkedNodes != null) {                nodeExpansion.put(key, new Vector<String>());                for (int i = 0; i < linkedNodes.length; ++i) {                    String dest = linkedNodes[i].getDestination().getID();                    if (!usedCount.contains(dest)) {                        nodeExpansion.get(key).add(dest);                        nextSeed.add(dest);                    }                }            }        }        usedCount.addAll(nextSeed);        seedNodes = nextSeed;    }    /**     * for each vector in the list     *   process end node for each geodesic     *     * @param g graph for betweeness calculations     * @param userNumber which user is being calculated.     */    protected void processVectors(Graph g, int userNumber) {        Iterator<String> seed = nodeExpansion.keySet().iterator();        while (seed.hasNext()) {            String seedString = seed.next();            Path[] base = lastPath.getPathByDestination(seedString);            Vector<String> mapping = nodeExpansion.get(seedString);            if ((base != null) && (mapping != null) && (mapping.size() > 0)) {                for (int i = 0; i < base.length; ++i) {                    for (int j = 0; j < mapping.size(); ++j) {                        Actor dest = g.getActor((String) parameter[2].getValue(), mapping.get(j));                        Path item = base[i].addActor(dest, 1.0);                        nextPath.addPath(item);                        centralityValue[userNumber] += item.getCost();                        prestigeValue[java.util.Arrays.binarySearch(userList, dest)] += item.getCost();//                        pathSet.addPath(item);                    }                }            }            usedCount.add(seedString);        }        lastPath = nextPath;    }    @Override    public InputDescriptor[] getInputType() {        return input;    }    @Override    public OutputDescriptor[] getOutputType() {        return output;    }    @Override    public Parameter[] getParameter() {        return parameter;    }    @Override    public Parameter getParameter(String param) {        for (int i = 0; i < parameter.length; ++i) {            if (parameter[i].getName().contentEquals(param)) {                return parameter[i];            }        }        return null;    }    @Override    public SettableParameter[] getSettableParameter() {        return null;    }    @Override    public SettableParameter getSettableParameter(String param) {        return null;    }    /**     * Parameters for initialization     * <br>     * <ol>     * <li>'name' - name of this isntance. Default is 'Closeness'.     * <li>'relation' - type (relation) of link to use.  Default is 'Knows'.     * <li>'actorType' - type (mode) of actor to use. Default is 'User'.     * <li>'normalize' - should the sum of squares of all entries be normalized to 1. Default is 'false'.     * </ol>     * <br>     * <br>Input 1 - Path     * <br>Output 1 - ActorProperty     * <br>Output 2 - ActorProperty     * <br>Output 3 - GraphProperty     * <br>Output 4 - GraphProperty     * <br>Output 5 - GraphProperty     * <br>Output 6 - GraphProperty     */    public void init(Properties map) {        Properties props = new Properties();        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "name");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[0] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("name") != null)) {            parameter[0].setValue(map.getProperty("name"));        } else {            parameter[0].setValue("Closeness");        }        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "relation");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[1] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("relation") != null)) {            parameter[1].setValue(map.getProperty("relation"));        } else {            parameter[1].setValue("Knows");        }        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "actorType");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[2] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("actorType") != null)) {            parameter[2].setValue(map.getProperty("actorType"));        } else {            parameter[2].setValue("User");        }        // Parameter 3 - normalize        props.setProperty("Type", "java.lang.Boolean");        props.setProperty("Name", "normalize");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[3] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("normalize") != null)) {            parameter[3].setValue(new Boolean(Boolean.parseBoolean(map.getProperty("normalize"))));        } else {            parameter[3].setValue(new Boolean(false));        }        // Create Input Descriptors        // Construct input descriptors        props.setProperty("Type", "Link");        props.setProperty("Relation", (String) parameter[1].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.remove("Property");        input[0] = DescriptorFactory.newInstance().createInputDescriptor(props);        // Construct Output Descriptors        // Construct Output Descriptors        props.setProperty("Type", "ActorProperty");        props.setProperty("Relation", (String) parameter[2].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessPrestige");        output[0] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "ActorProperty");        props.setProperty("Relation", (String) parameter[2].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessCentrality");        output[1] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessPrestige");        output[2] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessPrestigeSD");        output[3] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessCentrality");        output[4] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessCentralitySD");        output[5] = DescriptorFactory.newInstance().createOutputDescriptor(props);    }    /**     * Calculate the mean value for actor centrality.     *      * @param g graph to be modified     */    public void calculateGraphCentrality(Graph g) {        try {            double centrality = 0.0;            for (int i = 0; i < centralityValue.length; ++i) {                centrality += maxCentrality - centralityValue[i];            }            centrality *= 2;            centrality /= (centralityValue.length - 1) * (centralityValue.length - 1) * (centralityValue.length - 2);            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");            props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessCentrality");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(centrality));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddCombinedCloseness.class.getName()).log(Level.SEVERE, "Property class of "+(String) parameter[1].getValue()+" ClosenessCentrality does not match java.lang.Double", ex);                    }    }    /**     * Calculate the mean value for actor prestige.     *      * @param g graph to be modified     */    public void calculateGraphPrestige(Graph g) {        try {            double prestige = 0.0;            for (int i = 0; i < prestigeValue.length; ++i) {                prestige += maxPrestige - prestigeValue[i];            }            prestige *= 2;            prestige /= (prestigeValue.length - 1) * (prestigeValue.length - 1) * (prestigeValue.length - 2);            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");            props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessPrestige");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(prestige));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddCombinedCloseness.class.getName()).log(Level.SEVERE, "Property class of "+(String) parameter[1].getValue()+" ClosenessPrestige does not match java.lang.Double", ex);        }    }    /**     * Calculate Standard Deviation for actor centrality.     *      * @param g graph to be modified     */    public void calculateGraphCentralitySD(Graph g) {        try {            double sd = 0.0;            double centralitySquare = 0.0;            double centralitySum = 0.0;            for (int i = 0; i < centralityValue.length; ++i) {                centralitySquare += centralityValue[i] * centralityValue[i];                centralitySum += centralityValue[i];            }            sd = centralityValue.length * centralitySquare - centralitySum * centralitySum;            sd /= centralityValue.length;            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");           props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessCentralitySD");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(sd));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddCombinedCloseness.class.getName()).log(Level.SEVERE, "Property class of "+(String) parameter[1].getValue()+" ClosenessCentralitySD does not match java.lang.Double", ex);        }    }    /**     * Calculate Standard Deviation for actor prestige.     *      * @param g graph to be modified     */    public void calculateGraphPrestigeSD(Graph g) {        try {            double sd = 0.0;            double prestigeSquare = 0.0;            double prestigeSum = 0.0;            for (int i = 0; i < centralityValue.length; ++i) {                prestigeSquare += prestigeValue[i] * prestigeValue[i];                prestigeSum += prestigeValue[i];            }            double squareSum = prestigeSum * prestigeSum;            sd = (prestigeValue.length * prestigeSquare) - squareSum;            sd /= prestigeValue.length;            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");            props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessPrestigeSD");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(sd));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddCombinedCloseness.class.getName()).log(Level.SEVERE, "Property class of "+(String) parameter[1].getValue()+" ClosenessPrestigeSD does not match java.lang.Double", ex);        }    }}