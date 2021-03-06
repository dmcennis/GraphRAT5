/*
 * Copyright Daniel McEnnis, see license.txt
 */

package nz.ac.waikato.mcennis.rat.graph;

import java.util.Collection;
import java.util.LinkedList;
import org.mcennis.graphrat.actor.Actor;
import org.mcennis.graphrat.graph.MemGraph;

/**
 * Class for describing a tree.  Does not enforce acyclic condition.  Subclass
 * of MemGraph with MemGraph indexing.<br/>
 * <br/>
 * FIXME: Serialization not implemented - needs its own parser and output function.
 * @author Daniel McEnnis
 */
public class Tree extends MemGraph {
    
    LinkedList<Actor> roots = new LinkedList<Actor>();
    
    /**
     * Lists all the roots of this acyclic graph.
     * @return roots of the forest
     */
    public Collection<Actor> getRoots(){
        return roots;
    }
    
    /**
     * Add a root to the list of roots of this tree (forest)
     * @param a root actor to be added
     */
    public void addRoot(Actor a){
        roots.add(a);
    }
    
    @Override
    public void remove(Actor a){
        if(roots.contains(a)){
            roots.remove(a);
        }
        super.remove(a);
    }
}
