/*
 * NullGraph.java
 *
 * Created on 3 June 2007, 14:49
 *
 * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt)
 */
/*
 *   This file is part of GraphRAT.
 *
 *   GraphRAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   GraphRAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GraphRAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mcennis.graphrat.graph;

import java.util.*;

import org.dynamicfactory.descriptors.Properties;
import org.mcennis.graphrat.link.Link;

import org.mcennis.graphrat.actor.Actor;

import org.dynamicfactory.model.ModelShell;

import org.mcennis.graphrat.path.PathSet;
import org.dynamicfactory.descriptors.*;
import org.dynamicfactory.property.Property;

/**
 * Class that retains no information and implements null operations on every node.
 * This is useful when one wishes to parse a document without creating the graph.
 *
 * @author Daniel McEnnis
 * 
 */
public class NullGraph extends ModelShell implements Graph {

    public static final long serialVersionUID = 2;
    
    Properties properties = PropertiesFactory.newInstance().create();

    /** Creates a new instance of NullGraph */
    public NullGraph() {
        init(null);
    }

    @Override
    public SortedSet<Actor> getActor() {

        return new TreeSet<Actor>();

    }

    @Override
    public void add(Actor u) {

    }

    @Override
    public SortedSet<Actor> getActor(String type) {

        return new TreeSet<Actor>();

    }

    @Override
    public Actor getActor(String type, String ID) {

        return null;

    }

    @Override
    public void add(Link link) {

    }

    @Override
    public SortedSet<Link> getLink() {

        return new TreeSet<Link>();

    }

    @Override
    public SortedSet<Link> getLink(String type) {

        return new TreeSet<Link>();

    }

    @Override
    public void remove(Actor u) {

    }

    @Override
    public void remove(Link ul) {

    }

    @Override
    public void add(Property prop) {

    }

    @Override
    public List<Property> getProperty() {

        return new LinkedList<Property>();

    }

    @Override
    public Property getProperty(String type) {

        return null;

    }

    @Override
    public List<PathSet> getPathSet() {

        return new LinkedList<PathSet>();

    }

    @Override
    public PathSet getPathSet(String id) {

        return null;

    }

    @Override
    public void add(PathSet pathSet) {

    }

    @Override
    public SortedSet<String> getLinkTypes() {

        return new TreeSet<String>();

    }

    @Override
    public SortedSet<Link> getLinkBySource(String type, Actor sourceActor) {

        return new TreeSet<Link>();

    }

    @Override
    public SortedSet<Link> getLinkByDestination(String type, Actor destActor) {

        return new TreeSet<Link>();

    }

    @Override
    public SortedSet<Link> getLink(String type, Actor sourceActor, Actor destActor) {

        return new TreeSet<Link>();

    }

    @Override
    public SortedSet<String> getActorTypes() {

        return new TreeSet<String>();

    }

    @Override
    public Iterator<Actor> getActorIterator(String type) {

        return new TreeSet<Actor>().iterator();

    }

    @Override
    public void setID(String id) {

    }

    @Override
    public String getID() {

        return "Null";

    }

    @Override
    public void commit() {

    }

    @Override
    public void add(Graph g) {

    }

    @Override
    public void close() {

    }

    @Override
    public void anonymize() {

    }

    @Override
    public Graph getParent() {

        return null;

    }

    @Override
    public SortedSet<Graph> getChildren() {

        return new TreeSet<Graph>();

    }

    @Override
    public Graph getChildren(String id) {

        return null;

    }

    @Override
    public void addChild(Graph g) {

    }

    @Override
    public Properties getParameter() {
        return properties;
    }
    
    @Override
    public void init(Properties props) {
        ;
    }

    @Override
    public int getActorCount(String mode) {
        return 0;
    }

    public int compareTo(Object o) {
        return GraphCompare.compareTo(this,o);
    }

    public Iterator<Actor> getActorIterator() {
       return new TreeSet<Actor>().iterator();
    }

    public Iterator<Link> getLinkIterator() {
       return new TreeSet<Link>().iterator();
    }

    public Iterator<Link> getLinkIterator(String type) {
       return new TreeSet<Link>().iterator();
    }

    public Iterator<Link> getLinkBySourceIterator(String type, Actor sourceActor) {
       return new TreeSet<Link>().iterator();
    }

    public Iterator<Link> getLinkByDesinationIterator(String type, Actor destActor) {
       return new TreeSet<Link>().iterator();
    }

    public Iterator<Link> getLinkIterator(String type, Actor sourceActor, Actor destActor) {
       return new TreeSet<Link>().iterator();
    }

    public Iterator<Property> getPropertyIterator() {
       return new LinkedList<Property>().iterator();
    }

    public Iterator<PathSet> getPathSetIterator() {
       return new LinkedList<PathSet>().iterator();
    }

    public Iterator<Graph> getChildrenIterator() {
       return new TreeSet<Graph>().iterator();
    }

    public Parameter getParameter(String name) {
        return null;
    }

    public Graph prototype() {
        return new NullGraph();
    }

    public void removeProperty(String ID) {
        
    }

}

