/*
 * Query.java
 *
 * Created on 31 January 2007, 16:04
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

package org.mcennis.graphrat.query;

import java.io.IOException;
import org.mcennis.graphrat.graph.Graph;
import org.mcennis.graphrat.actor.Actor;
import org.mcennis.graphrat.link.Link;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;

import org.dynamicfactory.propertyQuery.Query;


/**
 * Class for general queries generating subgraphs.  Currently in pre-planning stages
 * This class is a stub to satisfy the graph methods that reference it.
 *
 * @author Daniel McEnnis
 * 
 */
public interface LinkQuery extends Query{
    
    public SortedSet<Link> execute(Graph g, SortedSet<Actor> sourceActorList, SortedSet<Actor> destinationActorList, SortedSet<Link> linkList);
    
    public Iterator<Link> executeIterator(Graph g, SortedSet<Actor> sourceActorList, SortedSet<Actor> destinationActorList, SortedSet<Link> linkList);

    public void exportQuery(java.io.Writer writer) throws IOException;
    
    public State buildingStatus();
    
    public LinkQuery prototype();
        
    public enum LinkEnd {SOURCE , DESTINATION, ALL};

    public enum SetOperation {AND, OR, XOR};
}

