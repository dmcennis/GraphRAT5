/*
 * MinXML - created 2/02/2009 - 5:46:51 PM
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

package org.mcennis.graphrat.reusablecores.aggregator.xml;

import java.io.IOException;
import java.io.Writer;
import org.mcennis.graphrat.reusablecores.aggregator.AggregatorFunction;
import org.mcennis.graphrat.reusablecores.aggregator.MinAggregator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.dynamicfactory.propertyQuery.Query.State;

/**
 *
 * @author Daniel McEnnis
 */
public class MinXML implements AggregatorXML{
    MinAggregator min = new MinAggregator();

    State state = State.UNINITIALIZED;

    public AggregatorFunction getAggregator() {
        return min;
    }

    public void export(Writer writer) throws IOException {
        writer.append("<Min/>\n");
    }

    public MinXML newCopy() {
        return new MinXML();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        state = State.LOADING;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        ;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        state = State.READY;
    }

    public State buildingStatus() {
        return state;
    }


}
