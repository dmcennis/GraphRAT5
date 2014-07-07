/* * AddClosenessCentrality.java * * Created on 27 June 2007, 19:00 * * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt) */package org.mcennis.graphrat.prestige;import java.util.Properties;import java.util.logging.Level;import java.util.logging.Logger;import org.mcennis.graphrat.algorithm.Algorithm;import org.mcennis.graphrat.graph.Graph;import org.mcennis.graphrat.actor.Actor;import org.dynamicfactory.descriptors.InputDescriptor;import org.dynamicfactory.descriptors.OutputDescriptor;import org.dynamicfactory.descriptors.DescriptorFactory;import org.dynamicfactory.descriptors.SettableParameter;import org.dynamicfactory.model.ModelShell;import org.mcennis.graphrat.path.PathSet;import org.mcennis.graphrat.scheduler.Scheduler;/** * Algorithm that implements closeness as in Freeman79. Utilizes a PathSet of  * geodesic paths. *  * <br> * Freeman, L. "Centrality in social networks: I. Conceptual clarification." * <i>Social Networks</i>. 1:215--239. * * @author Daniel McEnnis *  */public class AddClosenessCentrality extends ModelShell implements Algorithm {    public static final long serialVersionUID = 2;    private ParameterInternal[] parameter = new ParameterInternal[4];    private InputDescriptor[] input = new InputDescriptor[1];    private OutputDescriptor[] output = new OutputDescriptor[6];    private double maxCentrality;    private double maxPrestige;    private double[] centralityValue;    private double[] prestigeValue;    /** Creates a new instance of AddClosenessCentrality */    public AddClosenessCentrality() {        init(null);    }    @Override    public void execute(Graph g) {        fireChange(Scheduler.SET_ALGORITHM_COUNT,g.getActorCount((String) parameter[2].getValue()));        calculatePrestige(g);        calculateCentrality(g);        calculateGraphPrestige(g);        calculateGraphPrestigeSD(g);        calculateGraphCentrality(g);        calculateGraphCentralitySD(g);        centralityValue = null;        prestigeValue = null;    }    /**     * Calculates (MinSum of distances in a  graph)/(Sum of shortest paths to this node)     * for every actor of the given type (mode).  If the value is Infinite (because     * of unconnected graphs) the prestige is 0.0.     * @param g graph to be modified     */    public void calculatePrestige(Graph g) {        try {            Actor[] userList = g.getActor((String) parameter[2].getValue());            maxPrestige = Double.NEGATIVE_INFINITY;            prestigeValue = new double[userList.length];            PathSet pathSet = g.getPathSet("Directional Geodesic by " + (String) parameter[1].getValue());            for (int i = 0; i < userList.length; ++i) {                fireChange(Scheduler.SET_ALGORITHM_PROGRESS,i);                prestigeValue[i] = 0.0;                for (int j = 0; j < userList.length; ++j) {                    if (j != i) {                        if (!Double.isInfinite(pathSet.getGeodesicLength(userList[j].getID(), userList[i].getID()))) {                            prestigeValue[i] += pathSet.getGeodesicLength(userList[j].getID(), userList[i].getID());                        }                    }                }                if (prestigeValue[i] != 0) {                    prestigeValue[i] = 1 / prestigeValue[i];                    if (prestigeValue[i] > maxPrestige) {                        maxPrestige = prestigeValue[i];                    }                } else {                    prestigeValue[i] = 0.0;                }            }            if (((Boolean) parameter[3].getValue()).booleanValue()) {                double norm = 0.0;                for (int i = 0; i < prestigeValue.length; ++i) {                    norm += prestigeValue[i] * prestigeValue[i];                }                for (int i = 0; i < prestigeValue.length; ++i) {                    prestigeValue[i] /= Math.sqrt(norm);                }            }            for (int i = 0; i < userList.length; ++i) {                java.util.Properties props = new java.util.Properties();                props.setProperty("PropertyType", "Basic");                props.setProperty("PropertyClass", "java.lang.Double");                props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessPrestige");                Property prestige = PropertyFactory.newInstance().create(props);                prestige.add(new Double(prestigeValue[i]));                userList[i].add(prestige);            }        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddClosenessCentrality.class.getName()).log(Level.SEVERE, null, ex);        }    }    /**     * Calculates (MinSum of distances in a  graph)/(Sum of shortest paths from this node)     * for every actor of the given type (mode).If the value is Infinite (because     * of unconnected graphs) the centrality is 0.0.     *      * @param g graph to be modified     */    public void calculateCentrality(Graph g) {        try {            Actor[] userList = g.getActor((String) parameter[2].getValue());            centralityValue = new double[userList.length];            maxCentrality = Double.NEGATIVE_INFINITY;            PathSet pathSet = g.getPathSet("Directional Geodesic by " + (String) parameter[1].getValue());            for (int i = 0; i < userList.length; ++i) {                fireChange(Scheduler.SET_ALGORITHM_PROGRESS,userList.length+i);                for (int j = 0; j < userList.length; ++j) {                    if (j != i) {                        if (!Double.isInfinite(pathSet.getGeodesicLength(userList[i].getID(), userList[j].getID()))) {                            centralityValue[i] += pathSet.getGeodesicLength(userList[i].getID(), userList[j].getID());                        }                    }                }                if (centralityValue[i] != 0) {                    centralityValue[i] = 1 / centralityValue[i];                    if (centralityValue[i] > maxCentrality) {                        maxCentrality = centralityValue[i];                    }                } else {                    centralityValue[i] = 0.0;                }            }            if (((Boolean) parameter[3].getValue()).booleanValue()) {                double norm = 0.0;                for (int i = 0; i < centralityValue.length; ++i) {                    norm += centralityValue[i] * centralityValue[i];                }                for (int i = 0; i < centralityValue.length; ++i) {                    centralityValue[i] /= Math.sqrt(norm);                }            }            for (int i = 0; i < userList.length; ++i) {                java.util.Properties props = new java.util.Properties();                props.setProperty("PropertyType", "Basic");                props.setProperty("PropertyClass", "java.lang.Double");                props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessCentrality");                Property centrality = PropertyFactory.newInstance().create(props);                centrality.add(new Double(centralityValue[i]));                userList[i].add(centrality);            }        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddClosenessCentrality.class.getName()).log(Level.SEVERE, "Property class of property "+(String) parameter[1].getValue()+" ClosenessCentrality does match java.lang.Double", ex);        }    }    /**     * Calculate the mean value for actor centrality.     *      * @param g graph to be modified     */    public void calculateGraphCentrality(Graph g) {        try {            double centrality = 0.0;            for (int i = 0; i < centralityValue.length; ++i) {                centrality += maxCentrality - centralityValue[i];            }            centrality *= 2;            centrality /= (centralityValue.length - 1) * (centralityValue.length - 1) * (centralityValue.length - 2);            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");            props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessCentrality");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(centrality));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddClosenessCentrality.class.getName()).log(Level.SEVERE, "Property class of property "+(String) parameter[1].getValue()+" ClosenessCentrality does match java.lang.Double", ex);        }    }    /**     * Calculate the mean value for actor prestige.     *      * @param g graph to be modified     */    public void calculateGraphPrestige(Graph g) {        try {            double prestige = 0.0;            for (int i = 0; i < prestigeValue.length; ++i) {                prestige += maxPrestige - prestigeValue[i];            }            prestige *= 2;            prestige /= (prestigeValue.length - 1) * (prestigeValue.length - 1) * (prestigeValue.length - 2);            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");            props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessPrestige");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(prestige));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddClosenessCentrality.class.getName()).log(Level.SEVERE, "Property class of property "+(String) parameter[1].getValue()+" ClosenessPrestige does match java.lang.Double", ex);        }    }    /**     * Calculate Standard Deviation for actor centrality.     *      * @param g graph to be modified     */    public void calculateGraphCentralitySD(Graph g) {        try {            double sd = 0.0;            double centralitySquare = 0.0;            double centralitySum = 0.0;            for (int i = 0; i < centralityValue.length; ++i) {                centralitySquare += centralityValue[i] * centralityValue[i];                centralitySum += centralityValue[i];            }            sd = centralityValue.length * centralitySquare - centralitySum * centralitySum;            sd /= centralityValue.length;            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");            props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessCentralitySD");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(sd));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddClosenessCentrality.class.getName()).log(Level.SEVERE, "Property class of property "+(String) parameter[1].getValue()+" ClosenessCentralitySD does match java.lang.Double", ex);        }    }    /**     * Calculate Standard Deviation for actor prestige.     *      * @param g graph to be modified     */    public void calculateGraphPrestigeSD(Graph g) {        try {            double sd = 0.0;            double prestigeSquare = 0.0;            double prestigeSum = 0.0;            for (int i = 0; i < centralityValue.length; ++i) {                prestigeSquare += prestigeValue[i] * prestigeValue[i];                prestigeSum += prestigeValue[i];            }            double squareSum = prestigeSum * prestigeSum;            sd = (prestigeValue.length * prestigeSquare) - squareSum;            sd /= prestigeValue.length;            java.util.Properties props = new java.util.Properties();            props.setProperty("PropertyType", "Basic");            props.setProperty("PropertyClass", "java.lang.Double");            props.setProperty("PropertyID", (String) parameter[1].getValue() + " ClosenessPrestigeSD");            Property prop = PropertyFactory.newInstance().create(props);            prop.add(new Double(sd));            g.add(prop);        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(AddClosenessCentrality.class.getName()).log(Level.SEVERE, "Property class of property "+(String) parameter[1].getValue()+" ClosenessPrestigeSD does match java.lang.Double", ex);        }    }    @Override    public InputDescriptor[] getInputType() {        return input;    }    @Override    public OutputDescriptor[] getOutputType() {        return output;    }    @Override    public Parameter[] getParameter() {        return parameter;    }    @Override    public Parameter getParameter(String param) {        for (int i = 0; i < parameter.length; ++i) {            if (parameter[i].getName().contentEquals(param)) {                return parameter[i];            }        }        return null;    }    @Override    public SettableParameter[] getSettableParameter() {        return null;    }    @Override    public SettableParameter getSettableParameter(String param) {        return null;    }    /**     * Parameters for initializing AddClosenessCentrality     *      * <ol>     * <li>'name' - name of this isntance. Default is 'Basic Closeness'.     * <li>'relation' - type (relation) of link to use.  Default is 'Knows'.     * <li>'actorType' - type (mode) of actor to use. Default is 'User'.     * <li>'normalize' - should the sum of squares of all entries be normalized to 1. Default is 'false'.     * </ol>     * <br>     * <br>Input 1 - Path     * <br>Output 1 - ActorProperty     * <br>Output 2 - ActorProperty     * <br>Output 3 - GraphProperty     * <br>Output 4 - GraphProperty     * <br>Output 5 - GraphProperty     * <br>Output 6 - GraphProperty     */    public void init(Properties map) {        Properties props = new Properties();        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "name");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[0] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("name") != null)) {            parameter[0].setValue(map.getProperty("name"));        } else {            parameter[0].setValue("Basic Closeness");        }        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "relation");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[1] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("relation") != null)) {            parameter[1].setValue(map.getProperty("relation"));        } else {            parameter[1].setValue("Knows");        }        // Parameter 2 - actor type        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "actorType");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[2] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("actorType") != null)) {            parameter[2].setValue(map.getProperty("actorType"));        } else {            parameter[2].setValue("User");        }        // Parameter 3 - normalize        props.setProperty("Type", "java.lang.Boolean");        props.setProperty("Name", "normalize");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[3] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("normalize") != null)) {            parameter[3].setValue(new Boolean(Boolean.parseBoolean(map.getProperty("normalize"))));        } else {            parameter[3].setValue(new Boolean(false));        }        // Construct input descriptors        props.setProperty("Type", "Path");        props.setProperty("Relation", "Directional Geodesic By " + (String) parameter[1].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.remove("Property");        input[0] = DescriptorFactory.newInstance().createInputDescriptor(props);        // Construct Output Descriptors        props.setProperty("Type", "ActorProperty");        props.setProperty("Relation", (String) parameter[2].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessPrestige");        output[0] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "ActorProperty");        props.setProperty("Relation", (String) parameter[2].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessCentrality");        output[1] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessPrestige");        output[2] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessPrestigeSD");        output[3] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessCentrality");        output[4] = DescriptorFactory.newInstance().createOutputDescriptor(props);        props.setProperty("Type", "GraphProperty");        props.remove("Relation");        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[1].getValue() + " ClosenessCentralitySD");        output[5] = DescriptorFactory.newInstance().createOutputDescriptor(props);    }}