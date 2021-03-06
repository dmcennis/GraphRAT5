/*

 * Created 21/05/2008-15:45:23

 * Copyright Daniel McEnnis, see license.txt

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


package org.mcennis.graphrat.algorithm.reusablecores.datavector;



import weka.core.Instance;



/**

 *  Data vector backed by a weka Instance object

 * 

 * @author Daniel McEnnis

 */

public class InstanceDataVector implements DataVector{



    Instance source;

    

    int index=-1;

    

    int size;

    

    /**

     * Creates a data vector backed by a weka instance object stored by reference.

     * @param s Instance object to be stored

     */

    public InstanceDataVector(Instance s){

        source = s;

        if(source.classIndex() < 0){

            size = source.numAttributes();

        }else{

            size = source.numAttributes()-1;

        }

    }

    

    @Override

    public int size() {

        return size;

    }

    

    @Override

    public void setSize(int s){

        size = s;

    }



    @Override

    public double getValue(Comparable index) {

        if(index.getClass().getName().contentEquals("java.lang.Integer")){

            return source.value(((Integer)index).intValue());

        }

        else{

            return Double.NaN;

        }

    }



    @Override

    public void reset() {

        index = -1;

    }



    @Override

    public double getCurrentValue() {

        return source.value(index);

    }



    @Override

    public Comparable getCurrentIndex() {

        return index;

    }



    @Override

    public void next() {

        index++;

    }



    @Override

    public boolean hasNext() {

        if(index < size-1){

            return true;

        }else{

            return false;

        }

    }



    public int compareTo(Object o) {

        if(this.getClass().getName().compareTo(o.getClass().getName())==0){

            InstanceDataVector right = (InstanceDataVector)o;

            if(size != right.size ){

                return size - right.size;

            }

            if(source.numValues() != right.source.numValues()){

                return source.numValues() - right.source.numValues();

            }

            for(int i=0;i<source.numValues();++i){

                if(source.value(i) > right.source.value(i)){

                   return -1;

                }else if(source.value(i) < right.source.value(i)){

                    return 1;

                }              

            }

            return 0;     

        }else{

            return this.getClass().getName().compareTo(o.getClass().getName());

        }

    }



}

