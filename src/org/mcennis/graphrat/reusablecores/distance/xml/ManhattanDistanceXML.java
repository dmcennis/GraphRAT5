/**
 * ManhattanDistanceXML
 * Created Jan 17, 2009 - 8:38:24 PM
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
package org.mcennis.graphrat.reusablecores.distance.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.io.Writer;
import java.io.IOException;
import org.mcennis.graphrat.reusablecores.distance.ManhattanDistance;
import org.dynamicfactory.propertyQuery.Query.State;

/**
 *
 * @author Daniel McEnnis
 */
public class ManhattanDistanceXML implements DistanceXML{
    ManhattanDistance distance = null;
    
    State state = State.UNINITIALIZED;
    
    public ManhattanDistance getDistanceMeasure() {
        return distance;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        state = State.LOADING;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        distance = new ManhattanDistance();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        state = State.READY;
    }

    public State buildingStatus() {
        return state;
    }

    public ManhattanDistanceXML newCopy() {
        return new ManhattanDistanceXML();
    }

    public void exportAsXML(Writer writer) throws IOException{
        writer.append("<ManhattanDistance/>\n");
    }
}
