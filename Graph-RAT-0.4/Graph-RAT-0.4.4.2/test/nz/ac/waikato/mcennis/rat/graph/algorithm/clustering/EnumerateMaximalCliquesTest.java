/*
 * EnumerateMaximalCliquesTest.java
 * JUnit based test
 *
 * Created on 15 October 2007, 14:59
 *
 * Copyright Daniel McEnnis, all rights reserved
 */

package nz.ac.waikato.mcennis.rat.graph.algorithm.clustering;

import nz.ac.waikato.mcennis.rat.graph.algorithm.clustering.EnumerateMaximalCliques;
import junit.framework.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import nz.ac.waikato.mcennis.rat.graph.Clique;
import nz.ac.waikato.mcennis.rat.graph.Graph;
import nz.ac.waikato.mcennis.rat.graph.MemGraph;
import nz.ac.waikato.mcennis.rat.graph.actor.Actor;
import nz.ac.waikato.mcennis.rat.graph.actor.ActorFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.DescriptorFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.InputDescriptor;
import nz.ac.waikato.mcennis.rat.graph.descriptors.InputDescriptorInternal;
import nz.ac.waikato.mcennis.rat.graph.descriptors.OutputDescriptor;
import nz.ac.waikato.mcennis.rat.graph.descriptors.OutputDescriptorInternal;
import nz.ac.waikato.mcennis.rat.graph.descriptors.Parameter;
import nz.ac.waikato.mcennis.rat.graph.descriptors.ParameterInternal;
import nz.ac.waikato.mcennis.rat.graph.descriptors.SettableParameter;
import nz.ac.waikato.mcennis.rat.graph.link.Link;
import nz.ac.waikato.mcennis.rat.graph.link.LinkFactory;
import nz.ac.waikato.mcennis.rat.graph.model.ModelShell;
import nz.ac.waikato.mcennis.rat.graph.property.Property;
import nz.ac.waikato.mcennis.rat.graph.property.PropertyFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.InputDescriptor;

/**
 *
 * @author Daniel McEnnis
 */
public class EnumerateMaximalCliquesTest extends TestCase {

    Actor a;
    Actor b;
    Actor c;
    Actor d;
    Actor e;
    Actor art1;
    Actor art3;
    Actor art5;
    Actor art7;
    Link ab;
    Link ba;
    Link bc;
    Link cb;
    Link ac;
    Link ca;
    Link ae;
    Link bd;
    Link dc;
    Link a1;
    Link b1;
    Link b3;
    Link c3;
    Link c5;
    Link d7;
    MemGraph test;

    public EnumerateMaximalCliquesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        test = new MemGraph();
         java.util.Properties props = new java.util.Properties();
        props.setProperty("ActorType","User");
        props.setProperty("ActorID","A");
        props.setProperty("PropertyType","Basic");
        props.setProperty("PropertyID","interest");
        props.setProperty("LinkType","Interest");
        props.setProperty("LinkClass","Basic");
        a = ActorFactory.newInstance().create(props);
        Property ia = PropertyFactory.newInstance().create(props);
        ia.add("1");
        ia.add("2");
        a.add(ia);
        test.add(a);
        props.setProperty("ActorType","User");
        props.setProperty("ActorID","B");
        b = ActorFactory.newInstance().create(props);
        Property ib = PropertyFactory.newInstance().create(props);
        ib.add("1");
        ib.add("2");
        ib.add("3");
        ib.add("4");
        b.add(ib);
        test.add(b);
        props.setProperty("ActorID","C");
        c = ActorFactory.newInstance().create(props);
        Property ic = PropertyFactory.newInstance().create(props);
        ic.add("3");
        ic.add("4");
        ic.add("5");
        ic.add("6");
        c.add(ic);
        test.add(c);
        props.setProperty("ActorID","D");
        d = ActorFactory.newInstance().create(props);
        Property id = PropertyFactory.newInstance().create(props);
        id.add("7");
        id.add("8");
        d.add(id);
        test.add(d);
        props.setProperty("ActorID","E");
        e = ActorFactory.newInstance().create(props);
        test.add(e);
        props.setProperty("LinkType","Knows");
        ab = LinkFactory.newInstance().create(props);
        ab.set(a,1.0,b);
        test.add(ab);
        ba = LinkFactory.newInstance().create(props);
        ba.set(b,1.0,a);
        test.add(ba);
        cb = LinkFactory.newInstance().create(props);
        cb.set(c,1.0,b);
        test.add(cb);
        bc = LinkFactory.newInstance().create(props);
        bc.set(b,1.0,c);
        test.add(bc);
        ca = LinkFactory.newInstance().create(props);
        ca.set(c,1.0,a);
        test.add(ca);
        ac = LinkFactory.newInstance().create(props);
        ac.set(a,1.0,c);
        test.add(ac);
        ae = LinkFactory.newInstance().create(props);
        ae.set(a,1.0,e);
        test.add(ae);
        bd = LinkFactory.newInstance().create(props);
        bd.set(b,1.0,d);
        test.add(bd);
        dc = LinkFactory.newInstance().create(props);
        dc.set(d,1.0,b);
        test.add(dc);
        props.setProperty("ActorType","Artist");
        props.setProperty("ActorID","1");
        art1 = ActorFactory.newInstance().create(props);
        test.add(art1);
        props.setProperty("ActorID","3");
        art3 = ActorFactory.newInstance().create(props);
        test.add(art3);
        props.setProperty("ActorID","5");
        art5 = ActorFactory.newInstance().create(props);
        test.add(art5);
        props.setProperty("ActorID","7");
        art7 = ActorFactory.newInstance().create(props);
        test.add(art7);
        props.setProperty("LinkType","Given");
        a1 = LinkFactory.newInstance().create(props);
        a1.set(a,1.79769E+308,art1);
        test.add(a1);
        b1 = LinkFactory.newInstance().create(props);
        b1.set(b,1.79769E+308,art1);
        test.add(b1);
        b3 = LinkFactory.newInstance().create(props);
        b3.set(b,1.79769E+308,art3);
        test.add(b3);
        c3 = LinkFactory.newInstance().create(props);
        c3.set(c,1.79769E+308,art3);
        test.add(c3);
        c5 = LinkFactory.newInstance().create(props);
        c5.set(c,1.79769E+308,art5);
        test.add(c5);
        d7 = LinkFactory.newInstance().create(props);
        d7.set(d,1.79769E+308,art7);
        test.add(d7);
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of execute method, of class nz.ac.waikato.mcennis.rat.graph.algorithm.EnumerateMaximalCliques.
     */
    public void testExecute() {
        System.out.println("execute");
        
        
        EnumerateMaximalCliques instance = new EnumerateMaximalCliques();
        
        instance.execute(test);
        
        Graph[] children = test.getChildren();
        assertNotNull(children);
        assertEquals(2,children.length);
        boolean isFirst = false;
        boolean isSecond = false;
        for(int i=0;i<children.length;++i){
            assertTrue(children[i] instanceof Clique);
            Clique q = (Clique)children[i];
            if(isFirstTrue(q)){
                isFirst = true;
            }else if(isSecondTrue(q)){
                isSecond = true;
            }else{
                fail("Unexpected clique detected");
            }
        }
        if(!(isFirst && isSecond)){
            fail("Only one instance of clique detected");
        }
    }

    
    protected boolean isFirstTrue(Clique q){
        Actor[] actors = q.getActor();
        boolean ret = true;
        if(actors.length != 3){
            return false;
        }
        for(int i=0;i<actors.length;++i){
            if(actors[i].getID().contentEquals("A")){
                
            }else if(actors[i].getID().contentEquals("B")){
                
            }else if(actors[i].getID().contentEquals("C")){
                
            }else{
                return false;
            }
        }
        return true;
    }
    
    protected boolean isSecondTrue(Clique q){
        Actor[] actors = q.getActor();
        boolean ret = true;
        if(actors.length != 2){
            return false;
        }
        for(int i=0;i<actors.length;++i){
            if(actors[i].getID().contentEquals("B")){
                
            }else if(actors[i].getID().contentEquals("D")){
                
            }else{
                return false;
            }
        }
        return true;
    }

    /**
     * Test of getInputType method, of class nz.ac.waikato.mcennis.rat.graph.algorithm.EnumerateMaximalCliques.
     */
    public void testGetInputType() {
        System.out.println("getInputType");
        
        EnumerateMaximalCliques instance = new EnumerateMaximalCliques();
        
        InputDescriptor[] result = instance.getInputType();
        assertNotNull(result);
        assertEquals(1,result.length);
        assertEquals(InputDescriptor.Type.LINK,result[0].getClassType());
        assertEquals("Knows",result[0].getRelation());
        assertEquals("Enumerate Maximal Cliques",result[0].getAlgorithmName());
        assertNull(result[0].getProperty());
        
    }

    /**
     * Test of getOutputType method, of class nz.ac.waikato.mcennis.rat.graph.algorithm.EnumerateMaximalCliques.
     */
    public void testGetOutputType() {
        System.out.println("getOutputType");
        
        EnumerateMaximalCliques instance = new EnumerateMaximalCliques();
        
        OutputDescriptor[] result = instance.getOutputType();

        assertNotNull(result);
        assertEquals(1,result.length);
        assertEquals(OutputDescriptor.Type.GRAPH,result[0].getClassType());
        assertNull(result[0].getRelation());
        assertEquals("Enumerate Maximal Cliques",result[0].getAlgorithmName());
        assertNull(result[0].getProperty());
    }

    /**
     * Test of getParameter method, of class nz.ac.waikato.mcennis.rat.graph.algorithm.EnumerateMaximalCliques.
     */
    public void testGetParameter() {
        System.out.println("getParameter");
        
        EnumerateMaximalCliques instance = new EnumerateMaximalCliques();
        
        Parameter[] result = instance.getParameter();
        assertNotNull(result);
        assertEquals(4,result.length);
        
    }

    /**
     * Test of getSettableParameter method, of class nz.ac.waikato.mcennis.rat.graph.algorithm.EnumerateMaximalCliques.
     */
    public void testGetSettableParameter() {
        System.out.println("getSettableParameter");
        
        EnumerateMaximalCliques instance = new EnumerateMaximalCliques();
        
        SettableParameter[] result = instance.getSettableParameter();
        assertNull(result);
        
    }

    /**
     * Test of init method, of class nz.ac.waikato.mcennis.rat.graph.algorithm.EnumerateMaximalCliques.
     */
    public void testInit() {
        System.out.println("init");
        
        Properties map = null;
        EnumerateMaximalCliques instance = new EnumerateMaximalCliques();
        
        instance.init(map);
        
    }
    
}