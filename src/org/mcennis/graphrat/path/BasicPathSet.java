/*

 * BasicPathSet.java

 *

 * Created on 2 July 2007, 15:24

 *

 * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt)

 */



package org.mcennis.graphrat.path;



import java.util.HashMap;

import java.util.Vector;

import org.dynamicfactory.model.ModelShell;



/**

 *


 * 

 * Class for describing a set of Paths

 * @author Daniel McEnnis
 */

public class BasicPathSet extends ModelShell implements PathSet{

    

    String type;

    

    HashMap<String,Vector<Path>> source;

    HashMap<String,Vector<Path>> dest;

    

    /** Creates a new instance of BasicPathSet */

    public BasicPathSet() {

        source = new HashMap<String,Vector<Path>>();

        dest = new HashMap<String,Vector<Path>>();

    }

    

    @Override

    public Path[] getPath() {

        if(source.size()==0){

            return null;

        }

        Vector<Path> ret = new Vector<Path>();

        java.util.Iterator<Vector<Path>> it = source.values().iterator();

        while(it.hasNext()){

            ret.addAll(it.next());

        }

        return ret.toArray(new Path[]{});

    }

    

    @Override

    public Path[] getPathBySource(String source) {

        if(this.source.get(source)==null){

            return null;

        }else{

            return this.source.get(source).toArray(new Path[]{});

        }

    }

    

    @Override

    public Path[] getPathByDestination(String destination) {

        if(dest.get(destination)==null){

            return null;

        }else{

            return dest.get(destination).toArray(new Path[]{});

        }

    }

    

    @Override

    public void addPath(Path p) throws NotConstructedError{

        if(source.get(p.getStart().getID())==null){

            source.put(p.getStart().getID(),new Vector<Path>());

        }

        if(dest.get(p.getEnd().getID())==null){

            dest.put(p.getEnd().getID(),new Vector<Path>());

        }

        source.get(p.getStart().getID()).add(p);

        dest.get(p.getEnd().getID()).add(p);

        this.fireChange(ADD_PATH,0);

    }

    

    @Override

    public Path[] getPath(String source, String destination) {

        if((dest.get(destination)==null)||(this.source.get(source)==null)){

            return null;

        }else{

            Vector<Path> ret = new Vector<Path>();

            Vector<Path> s = this.source.get(source);

            for(int i=0;i<s.size();++i){

                try {

                    if(s.get(i).getEnd().getID().contentEquals(destination)){

                        ret.add(s.get(i));

                    }

                } catch(NotConstructedError e) {

                    System.err.println("Every path should be constructed in a path set!");

                    e.printStackTrace();

                }

            }

            return ret.toArray(new Path[]{});

        }

    }

    

    @Override

    public void setType(String name){

        type = name;

        this.fireChange(SET_TYPE,0);

    }

    

    @Override

    public String getType(){

        return type;

    }

    

    @Override

    public int size(){

        int ret = 0;

        java.util.Iterator<Vector<Path>> iterator = source.values().iterator();

        while(iterator.hasNext()){

            ret += iterator.next().size();

        }

        return ret;

    }

    

    @Override

    public double getGeodesicLength(String source,String destination){

        double ret = Double.POSITIVE_INFINITY;

        Vector<Path> set = this.source.get(source);

        if(set != null){

            for(int i=0;i<set.size();++i){

                if(set.get(i).getEnd().getID().contentEquals(destination)&&(set.get(i).getCost()<ret)){

                    ret = set.get(i).getCost();

                }

            }

        }

        return ret;

    }

}

