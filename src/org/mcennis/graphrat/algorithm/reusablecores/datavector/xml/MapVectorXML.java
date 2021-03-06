/**
 * MaVectorXML
 * Created Jan 16, 2009 - 11:26:40 PM
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
package org.mcennis.graphrat.algorithm.reusablecores.datavector.xml;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mcennis.graphrat.algorithm.reusablecores.datavector.DataVector;
import org.mcennis.graphrat.algorithm.reusablecores.datavector.MapDataVector;
import org.dynamicfactory.property.xml.PropertyValueXML;
import org.dynamicfactory.property.PropertyValueXMLFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.dynamicfactory.propertyQuery.Query.State;

/**
 *
 * TODO: complete map XML parsing
 * 
 * @author Daniel McEnnis
 */
public class MapVectorXML implements DataVectorXML{
    
    MapDataVector ret = null;
    
    HashMap<Object,Double> data = new HashMap<Object,Double>();

    enum InternalState {START,SIZE,ITEM,KEY,VALUE};
    
    InternalState internalState = InternalState.START;
    
    Object key = null;
    
    int size = 0;
    
    PropertyValueXML keyXML = null;
    
    State state = State.UNINITIALIZED;
    
    double value = Double.NaN;
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        state = State.LOADING;
        if(keyXML!=null){
            keyXML.startElement(uri, localName, qName, attributes);
        }else if(internalState == InternalState.KEY){
                        String name = "";
            if((localName != null)&&(!localName.contentEquals(""))){
                name = localName;
            }else{
                name = qName;
            }
            Class type;
            try {
                type = Class.forName(name);
            } catch (ClassNotFoundException ex) {
                type = String.class;
                Logger.getLogger(MapVectorXML.class.getName()).log(Level.SEVERE, "Property Type '"+name+"' does not exist, assuming java.lang.String", ex);
            }
            keyXML = PropertyValueXMLFactory.newInstance().create(type);
            keyXML.startElement(uri, localName, qName, attributes);
        }else if(localName.equalsIgnoreCase("Key")||qName.equalsIgnoreCase("Key")){
            internalState = InternalState.KEY;
        }else if(localName.equalsIgnoreCase("Value")||qName.equalsIgnoreCase("Value")){
            internalState = InternalState.VALUE;
        }else if(localName.equalsIgnoreCase("Item")||qName.equalsIgnoreCase("Item")){
            internalState = InternalState.VALUE;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
      if(keyXML != null ){
            keyXML.characters(ch, start, length);
        }else if(internalState == InternalState.VALUE){
            // TODO: error checking on double value
            value = Double.parseDouble(new String(ch,start,length));
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(keyXML != null){
            if(keyXML.buildingStatus()== State.READY){
                key = keyXML.getProperty();
            }
            keyXML = null;
        }else if(internalState == InternalState.KEY){
            internalState = InternalState.ITEM;
        }else if(internalState == InternalState.VALUE){
            internalState = InternalState.ITEM;
        }else if(internalState == InternalState.ITEM){
            data.put(key,new Double(value));
        }else if(internalState == InternalState.START){
            state = State.READY;
        }
    }

    public DataVector getDataVector() {
        return ret;
    }

    public State buildingStatus(){
        return state;
    }

    public MapVectorXML newCopy() {
        return new MapVectorXML();
    }
}
