/*

 * Created 1-5-08

 * 

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

package org.mcennis.graphrat.dataAquisition;




import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;

import java.io.LineNumberReader;

import java.io.FileReader;

import java.io.UnsupportedEncodingException;

import java.util.LinkedList;
import org.dynamicfactory.descriptors.*;

import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;

import org.mcennis.graphrat.crawler.Authenticator;

import org.mcennis.graphrat.crawler.CrawlerBase;

import org.mcennis.graphrat.graph.Graph;

import org.mcennis.graphrat.descriptors.IODescriptorFactory;


import org.mcennis.graphrat.descriptors.IODescriptor;

import org.mcennis.graphrat.descriptors.IODescriptor.Type;

import org.dynamicfactory.model.ModelShell;

import org.mcennis.graphrat.parser.Parser;

import org.mcennis.graphrat.parser.ParserFactory;



/**

 * Single threaded crawler for parsing a set of predetermined artists from LastFM.

 * For every artist as listed in a text file, the set of all tags for that artist

 * is downloaded from LastFM using the LastFMArtistTag XML parser.<br/>

 * <br/>

 * See the output descriptors for a description of the graph created.

 * 

 * @author Daniel McEnnis

 */

public class GetArtistTags extends ModelShell implements DataAquisition {



    Graph graph = null;

    PropertiesInternal properties = PropertiesFactory.newInstance().create();

    LinkedList<IODescriptor> input = new LinkedList<IODescriptor>();
    LinkedList<IODescriptor> output = new LinkedList<IODescriptor>();



    public GetArtistTags() {
        ParameterInternal name = ParameterFactory.newInstance().create("AlgorithmClass",String.class);
        SyntaxObject syntax = SyntaxCheckerFactory.newInstance().create(1,1,null,String.class);
        name.setRestrictions(syntax);
        name.add("File Reader 2 Pass");
        properties.add(name);

        name = ParameterFactory.newInstance().create("Name",String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1,Integer.MAX_VALUE,null,String.class);
        name.setRestrictions(syntax);
        name.add("File Reader 2 Pass");
        properties.add(name);

        name = ParameterFactory.newInstance().create("Category",String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1,1,null,String.class);
        name.setRestrictions(syntax);
        name.add("File Crawler");
        properties.add(name);

        name = ParameterFactory.newInstance().create("UseProxy",Boolean.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1,1,null,Boolean.class);
        name.setRestrictions(syntax);
        name.add(false);
        properties.add(name);

        name = ParameterFactory.newInstance().create("ProxyUser",String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(0,1,null,String.class);
        name.setRestrictions(syntax);
        name.add("dm75");
        properties.add(name);

        name = ParameterFactory.newInstance().create("ProxyPassword",String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(0,1,null,String.class);
        name.setRestrictions(syntax);
        properties.add(name);

        name = ParameterFactory.newInstance().create("ProxyLocation",String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(0,1,null,String.class);
        name.setRestrictions(syntax);
        name.add("proxy.cs.waikato.ac.nz");
        properties.add(name);

        name = ParameterFactory.newInstance().create("ToFileDirectory",File.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1,1,null,File.class);
        name.setRestrictions(syntax);
        properties.add(name);

        name = ParameterFactory.newInstance().create("ArtistFile",File.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1,1,null,File.class);
        name.setRestrictions(syntax);
        properties.add(name);
}



    public void start() {

        CrawlerBase crawler = new CrawlerBase();

        if((Boolean)properties.get("Proxy").get()){
            crawler.setProxy(true);
            crawler.setProxyHost((String)properties.get("ProxyLocation").get());
            crawler.setProxyPort(((Integer)properties.get("ProxyPort").get()).toString());
            crawler.setProxyType(((Integer)properties.get("ProxyType").get()).toString());
            Authenticator auth = new Authenticator();
            Authenticator.setUser((String)properties.get("ProxyUser").get());
            Authenticator.setPassword((String)properties.get("ProxyPassword").get());
            java.net.Authenticator.setDefault(auth);
        }else{
            crawler.setProxy(false);
        }

        properties.add("ParserClass","File");
        Parser[] parser = new Parser[]{ParserFactory.newInstance().create(properties)};
        parser[0].set(graph);
        crawler.set(parser);



        LineNumberReader reader = null;

        try {

            int count = 0;

            reader = new LineNumberReader(new FileReader((File)properties.get("ArtistFile").get()));

            String line = reader.readLine();

            while (line != null) {

                if (!line.contentEquals("")) {

                    Logger.getLogger(GetArtistTags.class.getName()).log(Level.FINE,count + ": '" + line + "'");

                    try {

                        crawler.crawl(buildURL(line));

                        Thread.sleep(500);

                    } catch (Exception ex) {

                        Logger.getLogger(GetArtistTags.class.getName()).log(Level.SEVERE, null, ex);

                    }

                }

                line = reader.readLine();

                count++;

            }

        } catch (FileNotFoundException ex) {

            Logger.getLogger(GetArtistTags.class.getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex) {

            Logger.getLogger(GetArtistTags.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            try {

                reader.close();

            } catch (IOException ex) {

                Logger.getLogger(GetArtistTags.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }



    protected String buildURL(String line) {

        if (line.matches(".*?&.*")) {

            line = line.split("\\s*&")[0];

        }

        if (line.matches(".*?Feat..*")) {

            line = line.split("\\s*Feat.")[0];

        }

        if (line.matches(".*?,.*")) {

            line = line.split("\\s*,")[0];

        }

        if (line.matches(".*?/.*")) {

            line = line.split("\\s*/")[0];

        }

        try {

            line = java.net.URLEncoder.encode(line, "UTF-8");

        } catch (UnsupportedEncodingException ex) {

            Logger.getLogger(GetArtistTags.class.getName()).log(Level.SEVERE, null, ex);

        }

        line = "http://ws.audioscrobbler.com/1.0/artist/"+line+"/toptags.xml";

        return line;

    }



    public void set(Graph g) {

        graph = g;

    }



    public Graph get() {

        return graph;

    }



    public void cancel() {

        throw new UnsupportedOperationException("Not supported yet.");

    }



    public List<IODescriptor> getInputType() {

        return input;

    }



    public List<IODescriptor> getOutputType() {

        return output;

    }



    public Properties getParameter() {
        return properties;
    }



    public Parameter getParameter(String param) {
        return properties.get(param);
    }

    /**

     * Parameters of this module:

     * <ul>

     * <li/><b>name</b>: name of this algorithm.

     * </ul>

     * @param map properties to load - null is permitted.

     */

    public void init(Properties map) {

        if(properties.check(map)){
            properties.merge(map);

            IODescriptor desc = IODescriptorFactory.newInstance().create(
                    Type.ACTOR,
                    (String)properties.get("Name").get(),
                    "artist",
                    null,
                    null,"");
            output.add(desc);

            desc = IODescriptorFactory.newInstance().create(
                    Type.ACTOR,
                    (String)properties.get("Name").get(),
                    "tag",
                    null,
                    null,"");
            output.add(desc);

            desc = IODescriptorFactory.newInstance().create(
                    Type.ACTOR_PROPERTY,
                    (String)properties.get("Name").get(),
                    "tag",
                    null,
                    "URL","");
            output.add(desc);

            desc = IODescriptorFactory.newInstance().create(
                    Type.LINK,
                    (String)properties.get("Name").get(),
                    "ArtistTag",
                    null,
                    null,"");
            output.add(desc);
        }
    }

    public GetArtistTags prototype(){
        return new GetArtistTags();
    }

}

