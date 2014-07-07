/* * HITSPrestige.java * * Created on 19 October 2007, 13:46 * * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt) */package org.mcennis.graphrat.prestige;import cern.colt.matrix.DoubleFactory1D;import cern.colt.matrix.DoubleFactory2D;import cern.colt.matrix.DoubleMatrix1D;import cern.colt.matrix.DoubleMatrix2D;import java.util.Properties;import java.util.logging.Level;import java.util.logging.Logger;import org.mcennis.graphrat.algorithm.Algorithm;import org.mcennis.graphrat.graph.Graph;import org.mcennis.graphrat.actor.Actor;import org.dynamicfactory.descriptors.DescriptorFactory;import org.dynamicfactory.descriptors.InputDescriptor;import org.dynamicfactory.descriptors.InputDescriptorInternal;import org.dynamicfactory.descriptors.OutputDescriptor;import org.dynamicfactory.descriptors.OutputDescriptorInternal;import org.dynamicfactory.descriptors.SettableParameter;import org.mcennis.graphrat.link.Link;import org.dynamicfactory.model.ModelShell;import org.mcennis.graphrat.scheduler.Scheduler;/** * Class Implementing the HITS algoerithm by a variation of the power metric as  * defined in Kleinberg99. * <br> * <br>Kleinberg, J. 1999. "Authoritative sources in a hyperlinked environment."  * <i>Journal of the ACM</i>. 46(5):604--32. *  * * @author Daniel McEnnis *  */public class ScalableHitsPrestige extends ModelShell implements Algorithm {    ParameterInternal[] parameter = new ParameterInternal[5];    InputDescriptorInternal[] input = new InputDescriptorInternal[1];    OutputDescriptorInternal[] output = new OutputDescriptorInternal[2];    /** Creates a new instance of HITSPrestige */    public ScalableHitsPrestige() {    }    /**     * Implements the following algorithm:     * <br><code>     * Iterate(G,k)     *  G: a collection of n linked pages     *  k: a natural number     *  Let z denote the vector (1, 1, 1, . . . , 1) Rn .     *  Set x 0 : z.     *  Set y 0 : z.     *  For i    1, 2, . . . , k     *     Apply the operation to (x i 1 , y i 1 ), obtaining new x-weights x i .     *     Apply the operation to (x i , y i 1 ), obtaining new y-weights y i .     *     Normalize x i , obtaining x i .     *     Normalize y i , obtaining y i .     *  End     * Return (x k , y k ).</code>     */    public void execute(Graph g) {        try {            fireChange(Scheduler.SET_ALGORITHM_COUNT,3);            Actor[] a = g.getActor((String) parameter[2].getValue());            java.util.Arrays.sort(a);            DoubleMatrix2D links = DoubleFactory2D.sparse.make(a.length, a.length);            links.assign(0.0);//        Link[] l = g.getLink((String)parameter[2].getValue());//        for(int i=0;i<l.length;++i){//            int source = java.util.Arrays.binarySearch(a,l[i].getSource());//            links.set(source,dest,0.15);//        }            for (int i = 0; i < a.length; ++i) {                Link[] l = g.getLinkBySource((String) parameter[1].getValue(), a[i]);                if (l != null) {                    for (int j = 0; j < l.length; ++j) {                        int dest = java.util.Arrays.binarySearch(a, l[j].getDestination());                        links.set(i, dest, (1.0));                    }                } else {                    Logger.getLogger(ScalableHitsPrestige.class.getName()).log(Level.WARNING, "User "+a[i].getID()+" has no outgoing links");                }            }            cern.colt.matrix.linalg.Algebra base = new cern.colt.matrix.linalg.Algebra();            DoubleMatrix2D inverseLinks = base.transpose(links);            DoubleMatrix1D hubVector = DoubleFactory1D.dense.make(a.length);            DoubleMatrix1D oldHubVector = DoubleFactory1D.dense.make(a.length);            hubVector.assign(Math.sqrt(1.0 / ((double) a.length)));            oldHubVector.assign(Math.sqrt(1.0 / ((double) a.length)));            DoubleMatrix1D authorityVector = DoubleFactory1D.dense.make(a.length);            DoubleMatrix1D oldAuthorityVector = DoubleFactory1D.dense.make(a.length);            authorityVector.assign(Math.sqrt(1.0 / ((double) a.length)));            oldAuthorityVector.assign(Math.sqrt(1.0 / ((double) a.length)));            DoubleMatrix1D temp = null;            double tolerance = ((Double) parameter[4].getValue()).doubleValue();            // links from authority vector to hub vector            inverseLinks.zMult(authorityVector, hubVector);            // links from hub vector to authority vector            links.zMult(hubVector, authorityVector);            // normalize hub vector            double norm = base.norm2(hubVector);            for (int i = 0; i < hubVector.size(); ++i) {                hubVector.set(i, hubVector.get(i) / Math.sqrt(norm));            }            // normalize hub vector            norm = base.norm2(authorityVector);            for (int i = 0; i < authorityVector.size(); ++i) {                authorityVector.set(i, authorityVector.get(i) / Math.sqrt(norm));            }            fireChange(Scheduler.SET_ALGORITHM_PROGRESS,1);            while ((error(oldHubVector, hubVector) > tolerance) || (error(oldAuthorityVector, authorityVector) > tolerance)) {                //swap old and new vectors                temp = hubVector;                hubVector = oldHubVector;                oldHubVector = temp;                temp = authorityVector;                authorityVector = oldAuthorityVector;                oldAuthorityVector = temp;                // links from authority vector to hub vector                inverseLinks.zMult(authorityVector, hubVector);                // links from hub vector to authority vector                links.zMult(hubVector, authorityVector);                // normalize hub vector                norm = base.norm2(hubVector);                for (int i = 0; i < hubVector.size(); ++i) {                    hubVector.set(i, hubVector.get(i) / Math.sqrt(norm));                }                // normalize hub vector                norm = base.norm2(authorityVector);                for (int i = 0; i < authorityVector.size(); ++i) {                    authorityVector.set(i, authorityVector.get(i) / Math.sqrt(norm));                }            }            fireChange(Scheduler.SET_ALGORITHM_PROGRESS,2);            Properties props = new Properties();            props.setProperty("PropertyClass", "java.lang.Double");            for (int i = 0; i < a.length; ++i) {                props.setProperty("PropertyID", output[0].getProperty());                Property property = PropertyFactory.newInstance().create(props);                property.add(new Double(hubVector.get(i)));                a[i].add(property);                props.setProperty("PropertyID", output[1].getProperty());                property = PropertyFactory.newInstance().create(props);                property.add(new Double(authorityVector.get(i)));                a[i].add(property);            }        } catch (InvalidObjectTypeException ex) {            Logger.getLogger(ScalableHitsPrestige.class.getName()).log(Level.SEVERE, "Property class does not match java.lang.Double", ex);        }    }    @Override    public InputDescriptor[] getInputType() {        return input;    }    @Override    public OutputDescriptor[] getOutputType() {        return output;    }    @Override    public Parameter[] getParameter() {        return parameter;    }    @Override    public Parameter getParameter(String param) {        for (int i = 0; i < parameter.length; ++i) {            if (parameter[i].getName().contentEquals(param)) {                return parameter[i];            }        }        return null;    }    @Override    public SettableParameter[] getSettableParameter() {        return new SettableParameter[]{parameter[4]};    }    @Override    public SettableParameter getSettableParameter(String param) {        if (parameter[4].getName().equals(param)) {            return parameter[4];        } else {            return null;        }    }    /**     *      * Parameters to be initialized:     * <ol>     * <li>'name' - name of this instance of the algorithm. Default 'HITS Centrality'.     * <li>'relation' - type (relation) of link to calculate over. Default 'Knows'.     * <li>'actorSourceType' - type (mode) of actor to calculate over. Default 'User'.     * <li>'propertyNamePrefix' - prefix for output property names. Deafult 'Knows HITS'.     * <li>'tolerance' - Double value to indicate the difference between      * iterations indicating the final version is discovered.     * </ol>     * <br>     * <br>Input 0 - Link     * <br>Output 0 - Actor Property     * <br>Output 1 - Actor Property     */    public void init(Properties map) {        Properties props = new Properties();        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "name");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[0] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("name") != null)) {            parameter[0].setValue(map.getProperty("name"));        } else {            parameter[0].setValue("HITS Centrality");        }        // Parameter 1 - relation        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "relation");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[1] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("relation") != null)) {            parameter[1].setValue(map.getProperty("relation"));        } else {            parameter[1].setValue("Knows");        }        // Parameter 2 - actor source type        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "actorSourceType");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[2] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("actorSourceType") != null)) {            parameter[2].setValue(map.getProperty("actorSourceType"));        } else {            parameter[2].setValue("User");        }        // Parameter 3 - actor relation        props.setProperty("Type", "java.lang.String");        props.setProperty("Name", "propertyNamePrefix");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[3] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("propertyNamePrefix") != null)) {            parameter[3].setValue(map.getProperty("propertyNamePrefix"));        } else {            parameter[3].setValue("Knows HITS");        }        // Parameter 4 - tolerance factor        props.setProperty("Type", "java.lang.Double");        props.setProperty("Name", "tolerance");        props.setProperty("Class", "Basic");        props.setProperty("Structural", "true");        parameter[4] = DescriptorFactory.newInstance().createParameter(props);        if ((map != null) && (map.getProperty("tolerance") != null)) {            parameter[4].setValue(new Double(Double.parseDouble(map.getProperty("tolerance"))));        } else {            parameter[4].setValue(new Double(1e-50));        }        // input 0        props.setProperty("Type", "Link");        props.setProperty("Relation", (String) parameter[1].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.remove("Property");        input[0] = DescriptorFactory.newInstance().createInputDescriptor(props);        // output 0        props.setProperty("Type", "ActorProperty");        props.setProperty("Relation", (String) parameter[2].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[3].getValue() + " Hub");        output[0] = DescriptorFactory.newInstance().createOutputDescriptor(props);        // output 1        props.setProperty("Type", "ActorProperty");        props.setProperty("Relation", (String) parameter[2].getValue());        props.setProperty("AlgorithmName", (String) parameter[0].getValue());        props.setProperty("Property", (String) parameter[3].getValue() + " Authority");        output[1] = DescriptorFactory.newInstance().createOutputDescriptor(props);    }    protected double error(DoubleMatrix1D newVector, DoubleMatrix1D oldVector) {        double ret = 0.0;        for (int i = 0; i < newVector.size(); ++i) {            ret += Math.abs(newVector.get(i) - oldVector.get(i));        }        return ret;    }}