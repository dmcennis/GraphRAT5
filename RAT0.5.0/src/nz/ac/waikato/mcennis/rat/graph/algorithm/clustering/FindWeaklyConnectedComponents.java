/*

 * Copyright Daniel McEnnis, see license.txt

 */



package nz.ac.waikato.mcennis.rat.graph.algorithm.clustering;



import java.util.HashSet;

import java.util.Iterator;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;

import nz.ac.waikato.mcennis.rat.graph.Graph;

import nz.ac.waikato.mcennis.rat.graph.GraphFactory;
import nz.ac.waikato.mcennis.rat.graph.actor.Actor;

import nz.ac.waikato.mcennis.rat.graph.algorithm.Algorithm;
import nz.ac.waikato.mcennis.rat.graph.algorithm.AlgorithmMacros;
import nz.ac.waikato.mcennis.rat.graph.descriptors.IODescriptor;

import nz.ac.waikato.mcennis.rat.graph.descriptors.IODescriptor.Type;
import nz.ac.waikato.mcennis.rat.graph.descriptors.IODescriptorFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.Parameter;

import nz.ac.waikato.mcennis.rat.graph.descriptors.ParameterFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.ParameterInternal;
import nz.ac.waikato.mcennis.rat.graph.descriptors.Properties;
import nz.ac.waikato.mcennis.rat.graph.descriptors.PropertiesFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.PropertiesInternal;
import nz.ac.waikato.mcennis.rat.graph.descriptors.SyntaxCheckerFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.SyntaxObject;
import nz.ac.waikato.mcennis.rat.graph.link.Link;

import nz.ac.waikato.mcennis.rat.graph.model.ModelShell;

import nz.ac.waikato.mcennis.rat.graph.query.ActorQuery;
import nz.ac.waikato.mcennis.rat.graph.query.ActorQueryFactory;
import nz.ac.waikato.mcennis.rat.graph.query.LinkQuery;
import nz.ac.waikato.mcennis.rat.graph.query.LinkQueryFactory;
import nz.ac.waikato.mcennis.rat.graph.query.actor.ActorByMode;
import nz.ac.waikato.mcennis.rat.graph.query.link.LinkByRelation;
import nz.ac.waikato.mcennis.rat.scheduler.Scheduler;



/**

 * Create subgraphs from all weakly connected components.  A weakly connected 

 * component is one where there exists a path between every pair of actors if all edges

 * are considered undirectional.  Subgraphs are disjoint and spanning.

 * 

 * @author Daniel McEnnis

 */

public class FindWeaklyConnectedComponents extends ModelShell implements Algorithm{



    PropertiesInternal parameter = PropertiesFactory.newInstance().create();
    LinkedList<IODescriptor> input = new LinkedList<IODescriptor>();
    LinkedList<IODescriptor> output = new LinkedList<IODescriptor>();

    LinkByRelation relation;

    int graphCount=0;



    HashSet<Actor> component = new HashSet<Actor>();

    HashSet<Actor> seen = new HashSet<Actor>();

    LinkedList<Graph> children = new LinkedList<Graph>();

    

    public FindWeaklyConnectedComponents(){
        ParameterInternal name = ParameterFactory.newInstance().create("AlgorithmClass", String.class);
        SyntaxObject syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("Find Weakly Connected Components");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("Name", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, Integer.MAX_VALUE, null, String.class);
        name.setRestrictions(syntax);
        name.add("Find Weakly Connected Components");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("Category", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("Clustering");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("LinkFilter", LinkQuery.class);
        syntax = SyntaxCheckerFactory.newInstance().create(0, 1, null, LinkQuery.class);
        name.setRestrictions(syntax);
        parameter.add(name);

        name = ParameterFactory.newInstance().create("ActorFilter", ActorQuery.class);
        syntax = SyntaxCheckerFactory.newInstance().create(0, 1, null, ActorQuery.class);
        name.setRestrictions(syntax);
        parameter.add(name);

        name = ParameterFactory.newInstance().create("LinkQuery", LinkQuery.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, LinkQuery.class);
        name.setRestrictions(syntax);
        parameter.add(name);

        name = ParameterFactory.newInstance().create("SourceAppendGraphID", Boolean.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, Boolean.class);
        name.setRestrictions(syntax);
        name.add(false);
        parameter.add(name);

        name = ParameterFactory.newInstance().create("Mode", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("tag");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("Relation", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("tag");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("SourceProperty", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("Property");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("DestinationProperty", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("Property");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("GraphIDPrefix", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("Property");
        parameter.add(name);

        name = ParameterFactory.newInstance().create("AddContext", Boolean.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, Boolean.class);
        name.setRestrictions(syntax);
        name.add(false);
        parameter.add(name);
    }

    

    @Override

    public void execute(Graph g) {
        ActorByMode mode = (ActorByMode)ActorQueryFactory.newInstance().create("ActorByMode");
        mode.buildQuery((String)parameter.get("Mode").get(),".*",false);

        Iterator<Actor> actor = AlgorithmMacros.filterActor(parameter, g, mode, null, null);

        relation = (LinkByRelation)LinkQueryFactory.newInstance().create("LinkByRelation");
        relation.buildQuery((String)parameter.get("Relation").get(), false);

        if(actor.hasNext()){

            fireChange(Scheduler.SET_ALGORITHM_COUNT,g.getActorCount((String)parameter.get("Mode").get()));
            int i=0;
            while(actor.hasNext()){
                Actor a =actor.next();
                if(!seen.contains(a)){

                    getComponent(a,g);

                    getGraph(g);

                }

                fireChange(Scheduler.SET_ALGORITHM_PROGRESS,i++);

            }

        }

        if(children.size()>1){

            Iterator<Graph> child = children.iterator();

            while(child.hasNext()){

                g.addChild(child.next());

            }

        }

    }

    

    protected void getComponent(Actor seed,Graph g){

        component.add(seed);

        seen.add(seed);

        LinkedList<Actor> actor = new LinkedList<Actor>();
        actor.add(seed);

        Iterator<Link> links = AlgorithmMacros.filterLink(parameter, g, relation, actor, null, null);

        if(links.hasNext()){

            while(links.hasNext()){
                Link l = links.next();
                if(!seen.contains(l.getDestination())){
                    getComponent(l.getDestination(),g);
                }
            }
        }

        links = AlgorithmMacros.filterLink(parameter, g, relation, null, actor, null);

        if(links.hasNext()){

            while(links.hasNext()){
                Link l= links.next();
                if(!seen.contains(l.getSource())){

                    getComponent(l.getSource(),g);

                }

            }

        }

    }

    

    protected void getGraph(Graph g){

        try {
                Graph graph = GraphFactory.newInstance().create((String)parameter.get("GraphIDPrefix").get()+graphCount,parameter);
                Iterator<Actor> it = component.iterator();
                while(it.hasNext()){
                    graph.add(it.next());
                }
                LinkQuery query = (LinkQuery) parameter.get("LinkQuery").get();
                Iterator<Link> link = query.executeIterator(g, component, component, null);
                while (link.hasNext()) {
                    graph.add(link.next());
                }
                if ((Boolean) parameter.get("AddContext").get()) {
                    HashSet<Actor> actorSet = new HashSet<Actor>();
                    actorSet.addAll(graph.getActor());
                    link = query.executeIterator(g, actorSet, null, null);
                    while (link.hasNext()) {
                        Link l = link.next();
                        Actor d = l.getDestination();
                        if (graph.getActor(d.getMode(), d.getID()) == null) {
                            graph.add(d);
                        }
                        if (graph.getLink(l.getRelation(), l.getSource(), l.getDestination()) == null) {
                            graph.add(l);
                        }
                    }

                    link = query.executeIterator(g, null, actorSet, null);
                    while (link.hasNext()) {
                        Link l = link.next();
                        Actor d = l.getSource();
                        if (graph.getActor(d.getMode(), d.getID()) == null) {
                            graph.add(d);
                        }
                        if (graph.getLink(l.getRelation(), l.getSource(), l.getDestination()) == null) {
                            graph.add(l);
                        }
                    }
                }
            graphCount++;

            children.add(graph);

            component.clear();

        } catch (Exception ex) {

            Logger.getLogger(FindWeaklyConnectedComponents.class.getName()).log(Level.SEVERE, null, ex);

        }

    }



    @Override

    public List<IODescriptor> getInputType() {

        return input;

    }



    @Override

    public List<IODescriptor> getOutputType() {

        return output;

    }



    @Override

    public Properties getParameter() {

        return parameter;

    }



    @Override

    public Parameter getParameter(String param) {
        return parameter.get(param);

    }

     /**

     * Parameters to be initialized.  Subclasses should override if they provide

     * any additional parameters or require additional inputs.

     * 

     * <ol>

     * <li>'name' - Name of this instance of the algorithm.  Default is ''.

     * <li>'relation' - type (relation) of link to calculate over. Default 'Knows'.

     * <li>'actorType' - type (mode) of actor to calculate over. Deafult 'User'.

     * <li>'graphClass' - graph class used to create subgraphs

     * <li>'graphIDprefix' - prefix used for graphIDs.

     * </ol>

     * <br>

     * <br>Input 0 - Link

     * <br>Output 0 - Graph

     */

   public void init(Properties map) {
        if(parameter.check(map)){
            parameter.merge(map);

            IODescriptor desc = IODescriptorFactory.newInstance().create(
                    Type.ACTOR,
                    (String)parameter.get("Name").get(),
                    (String)parameter.get("Mode").get(),
                    null,
                    null,
                    "",
                    (Boolean)parameter.get("SourceAppendGraphID").get());
            input.add(desc);

            desc = IODescriptorFactory.newInstance().create(
                    Type.LINK,
                    (String)parameter.get("Name").get(),
                    (String)parameter.get("Relation").get(),
                    null,
                    null,
                    "",
                    false);
            input.add(desc);

            desc = IODescriptorFactory.newInstance().create(
                    Type.GRAPH,
                    (String)parameter.get("Name").get(),
                    (String)parameter.get("GraphIDPrefix").get(),
                    null,
                    null,
                    "",
                    true);
            output.add(desc);
        }
   }

   public FindWeaklyConnectedComponents prototype(){
       return new FindWeaklyConnectedComponents();
   }

}
