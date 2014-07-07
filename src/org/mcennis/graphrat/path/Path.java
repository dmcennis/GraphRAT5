/*

 * Path.java

 *

 * Created on 30 June 2007, 19:16

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


package org.mcennis.graphrat.path;



import org.mcennis.graphrat.actor.Actor;

import org.dynamicfactory.model.Model;



/**

 *


 * 

 * Class for describing a path in a graph

 * @author Daniel McEnnis
 */

public interface Path extends Model{

    

    public static final int WEAK = 0;

    public static final int ADD_ACTOR=1;

    public static final int SET_PATH=2;

    

    /**

     * Create a path that goes through the following actors in order with a total

     * cost and given id representing this PathSet id.

     * 

     * @param participants actors in this path in order

     * @param cost total cost (strength) of this path

     * @param type PathSet id of this path

     */

    public void setPath(Actor[] participants, double cost, String type);

    

    /**

     * Returns the order array of the actors in this object.

     * 

     * @return ordered array of actors

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public Actor[] getPath() throws NotConstructedError;

    

    /**

     * Return the first actor in the path

     * 

     * @return first actor

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public Actor getStart() throws NotConstructedError;

    

    /**

     * return the last actor in this path

     *

     * @return last actor

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public Actor getEnd() throws NotConstructedError;

    

    /**

     * return all actors that are neither the start or end actors.

     * 

     * @return 

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public Actor[] getMiddle() throws NotConstructedError;

    

    /**

     * return the total cost of this path

     * 

     * @return total cost of the path

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public double getCost() throws NotConstructedError;

    

    /**

     * returns if this path ever traverses a link in the wrong direction.

     * 

     * @return traverses in the wrong direction on any link in the path.

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public boolean isWeak() throws NotConstructedError;

    

    /**

     * Set that the path traverses the wrong direction on at least one link between actors.

     * 

     * @param weak 

     */

    public void setWeak(boolean weak);

    

    /**

     * returns if the path contains the given actor

     * 

     * @param node actor to be checked

     * @return does this actor exist in this path

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public boolean contains(Actor node) throws NotConstructedError;

    

    /**

     * Returns the PathSet id of this path

     * 

     * @return PathSet id of this path.

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public String getType() throws NotConstructedError;

    

    /**

     * Create a new path that contains all the contents of this path plus an

     * additional link to the given actor with the additional cost given.  It is

     * the callers responsibility to not add an actor already present.

     * 

     * @param a actor to be added

     * @param cost cost to be added to the path cost

     * @return new path including a deep copy of the original with the new path added.

     * @throws org.mcennis.graphrat.path.NotConstructedError

     */

    public Path addActor(Actor a,double cost) throws NotConstructedError;

}

