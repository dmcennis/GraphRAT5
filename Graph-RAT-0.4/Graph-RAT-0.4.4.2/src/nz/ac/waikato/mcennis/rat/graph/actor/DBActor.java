/* * DerbyActor.java * * Created on 8 October 2007, 11:49 * * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt) */package org.mcennis.graphrat.actor;import java.sql.Connection;import java.sql.PreparedStatement;import java.sql.ResultSet;import java.sql.SQLException;import org.dynamicfactory.model.Listener;import org.dynamicfactory.model.Model;import org.dynamicfactory.model.ModelShell;import nz.ac.waikato.mcennis.rat.graph.page.Page;/** * *  * Class that implemnts an actor backed by a DerbyDB database.  Fixes problems * of inconsistencies between the database and the in-memory data when using * BasicUser class. * @author Daniel McEnnis */public class DBActor extends ModelShell implements Actor, Listener{        int id = -1;        boolean nameSet=false;        boolean typeSet=false;        String name = "";        String type = "";        static String directory="/tmp/";    static String database="LiveJournal";        static Connection connection = null;        static PreparedStatement statSetID = null;    static PreparedStatement statGetPropertyArray = null;    static PreparedStatement statRemoveProperty = null;    static PreparedStatement statAddProperty = null;    static PreparedStatement statGetProperty = null;    static PreparedStatement statAlterProperty = null;    static PreparedStatement statGetPageArray = null;    static PreparedStatement statGetPage = null;    static PreparedStatement statAddPage = null;    static PreparedStatement statSetType = null;    static PreparedStatement statGetNumID = null;    static PreparedStatement statSetNumID = null;        /** Creates a new instance of DerbyActor */    public DBActor() {    }    /**     * static initialization method that sets up the global connection objects.  This     * static connection should ultimately be replaced by a database connection pool.     */    public static void init(){        try {            System.setProperty("derby.system.home",directory);            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");            connection = java.sql.DriverManager.getConnection("jdbc:derby:"+database);            connection.setAutoCommit(false);            statSetID = connection.prepareStatement("UPDATE Actor SET name=? WHERE id=?");            statGetPropertyArray = connection.prepareStatement("SELECT type, value FROM ActorProperties WHERE id=?");            statRemoveProperty = connection.prepareStatement("DELETE FROM actorProperties WHERE id=? AND type=?");            statAlterProperty = connection.prepareStatement("UPDATE ActorProperties SET value=? WHERE id=? AND type=?");            statAddProperty = connection.prepareStatement("INSERT  INTO ActorProperties (id,type,value) VALUES (?,?,?)");            statGetProperty = connection.prepareStatement("SELECT value FROM ActorProperties WHERE id=? AND type=?");            //            statGetPage = connection.prepareStatement("SELECT * FROM Page WHERE actor=?");            //            statAddPage = connection.prepareStatement("INSERT INTO Page VALUES (?)");            statSetType = connection.prepareStatement("UPDATE Actor SET type=? WHERE id=?");            statGetNumID = connection.prepareStatement("SELECT id FROM Actor WHERE name=? AND type=?");            statSetNumID = connection.prepareStatement("INSERT INTO Actor (name,type) VALUES (?,?)");        } catch (ClassNotFoundException ex) {            ex.printStackTrace();        } catch (SQLException ex) {            ex.printStackTrace();        }    }        /**     * Has this object been Initialized yet.     * @return is initialized or not     */    public static boolean isInitialized(){        return !(connection == null);    }        @Override    public String getID() {        return name;    }        @Override    public void setID(String id) {        if(this.id > 0){            //            synchronized(connection){            try {                statSetID.clearParameters();                statSetID.setString(1,id);                statSetID.setInt(2,this.id);                statSetID.executeUpdate();                name=id;            } catch (SQLException ex) {                ex.printStackTrace();            }            //            }        }else{            name = id;            nameSet = true;            if(nameSet&&typeSet){                getNumericalID();            }        }    }        @Override    public Property[] getProperty() {        java.util.HashMap<String,Property> ret = new java.util.HashMap<String,Property>();        try {            //            synchronized(connection){            statGetPropertyArray.setInt(1,id);            ResultSet rs = statGetPropertyArray.executeQuery();            while(rs.next()){                String type = rs.getString("type");                if(!ret.containsKey(type)){                    java.util.Properties properties = new java.util.Properties();                    properties.setProperty("PropertyID",type);                    ret.put(type,PropertyFactory.newInstance().create(properties));                }                String value = rs.getString("value");                if(!value.contentEquals("")){                    ret.get(type).add(rs.getString("value"));                }            }            java.util.Iterator<Property> it = ret.values().iterator();            while(it.hasNext()){                it.next().addListener(this);            }            //            }        } catch (java.sql.SQLException ex) {            ex.printStackTrace();        } catch (InvalidObjectTypeException ex) {            ex.printStackTrace();        }        if(ret.size()>0){            return ret.values().toArray(new Property[]{});        }else{            return null;        }    }        @Override    public Property getProperty(String ID) {        Property ret = null;        try {            //            synchronized(connection){            statGetProperty.setInt(1,id);            statGetProperty.setString(2,ID);            ResultSet rs = statGetProperty.executeQuery();            if(rs.next()){                                java.util.Properties properties = new java.util.Properties();                properties.setProperty("PropertyID",ID);                ret = PropertyFactory.newInstance().create(properties);                String value = rs.getString("value");                if(!value.contentEquals("")){                    addValue(ret,value);                }                                while(rs.next()){                    addValue(ret,value);                }                ret.addListener(this);            }            //            }        } catch (SQLException ex) {            ex.printStackTrace();        }        return ret;    }        @Override    public void removeProperty(String ID) {        try {            //            synchronized(connection){            statRemoveProperty.clearParameters();            statRemoveProperty.setInt(1,id);            statRemoveProperty.setString(2,ID);            statRemoveProperty.executeUpdate();            //            }        } catch (SQLException ex) {            ex.printStackTrace();        }    }        @Override    public void add(Property prop) {        try {            //            synchronized(connection){            prop.addListener(this);            statAddProperty.clearParameters();            statAddProperty.setInt(1,id);            statAddProperty.setString(2,prop.getType());            Object[] value = prop.getValue();            if(value == null){                statAddProperty.setString(3,"");                statAddProperty.executeUpdate();            }else{                for(int i=0;i<value.length;++i){                    statAddProperty.setString(3,value[i].toString());                    statAddProperty.executeUpdate();                }            }            //            }        } catch (SQLException ex) {            ex.printStackTrace();        }    }        /**     * FIXME: Not implemented yet.     */    public Page[] getPage() {        return null;    }        /**     * FIXME: Not implemented yet.     */    public Page getPage(String ID) {        return null;    }        /**     * FIXME: Not implemented yet.     */    public void add(Page page) {    }        @Override    public void setType(String type) {        if(id>0){            try {                statSetType.clearParameters();                statSetType.setString(1,type);                statSetType.setInt(2,id);                statSetType.executeUpdate();                this.type = type;            } catch (SQLException ex) {                ex.printStackTrace();            }        }else{            this.type=type;            typeSet = true;            if(nameSet && typeSet){                getNumericalID();            }        }    }        @Override    public String getType() {        return type;    }        @Override    public Actor duplicate() {        DBActor ret = new DBActor();        ret.name = name;        ret.type = type;        ret.id = id;        return ret;    }        /**     * Throws ClassCastException when the parameter is not an Actor.  Comparisons     * are:     * <br>     * <ol><li>String compareTo on type (mode)     * <li>String compareTo on ID     * <li>Property comparison: return compareTo on the first pair of properties      * that do not return 0     * <li>Page comparison: return compareTo on the first pair of pages that do not     * return 0     * <li>return 0     * </ol>     */    public int compareTo(Object o) {        Actor a =(Actor)o;        if(type.compareTo(a.getType())==0){            if(name.compareTo(a.getID())==0){                if(compareProperties(a)==0){                    return comparePages(a);                }else{                    return compareProperties(a);                }            }else{                return name.compareTo(a.getID());            }        }else{            return type.compareTo(a.getType());        }    }        /**     * Compare pages of this actor with the given actor.  Sort the page arrays     * and compare sequentially, returning the first non-zero value or returning      * zero if all pages are equal.     *      * @param right actor to be compared against     * @return compareTo over all pages     */    protected int comparePages(Actor right){        Page[] l = this.getPage();        Page[] r = right.getPage();        if((l==null)&&(r==null)){            return 0;        }else if(l==null){            return -1;        }else if(r==null){            return 1;        }        java.util.Arrays.sort(l);        java.util.Arrays.sort(r);        int i=0;        int ret = 0;        while((i<l.length)&&(i<r.length)){            ret = l[i].compareTo(r[i]);            if(ret != 0){                return ret;            }else{                ++i;            }        }        if(i<l.length){            return 1;        }else if(i<r.length){            return -1;        }else{            return 0;        }    }        /**     * Compare properties of this actor with the given actor.  Sort the property     * arrays and compare sequentially, returning the first non-zero value or      * returning zero if all properties are equal.     *      * @param right actor to be compared against     * @return compareTo over all properties     */    protected int compareProperties(Actor right){        Property[] l = this.getProperty();        Property[] r = right.getProperty();        if((l==null)&&(r==null)){            return 0;        }else if(l==null){            return -1;        }else if(r==null){            return 1;        }        java.util.Arrays.sort(l);        java.util.Arrays.sort(r);        int i=0;        int ret = 0;        while((i<l.length)&&(i<r.length)){            ret = l[i].compareTo(r[i]);            if(ret != 0){                return ret;            }else{                ++i;            }        }        if(i<l.length){            return 1;        }else if(i<r.length){            return -1;        }else{            return 0;        }    }        /**     * Get thye numerical ID that uniquely describes this Actor in the database     */    protected void getNumericalID(){        try {            statGetNumID.clearParameters();            statGetNumID.setString(1,name);            statGetNumID.setString(2,type);            ResultSet rs = statGetNumID.executeQuery();            if(rs.next()){                id = rs.getInt("id");            }else{                statSetNumID.clearParameters();                statSetNumID.setString(1,name);                statSetNumID.setString(2,type);                statSetNumID.executeUpdate();                statGetNumID.clearParameters();                statGetNumID.setString(1,name);                statGetNumID.setString(2,type);                ResultSet rs2 = statGetNumID.executeQuery();                if(rs2.next()){                    id= rs2.getInt("id");                }else{                    id = -2;                }                rs2.close();            }            rs.close();        } catch (SQLException ex) {            ex.printStackTrace();        }    }        /**     * Set the location of the Derby database directory      * @param dir directory where thye datbases are stored     */    public static void setDirectory(String dir){        directory = dir;    }        /**     * Set which database to access     * @param db name of the datbase to open     */    public static void setDatabase(String db){        database = db;    }        /**     * Saves to database the changes in a property.     * @param m Property that changed     * @param type currently only 0 (Changed) is fired     */    public void publishChange(Model m, int type, int argument) {        if(m instanceof Property){            try {                Property p = (Property)m;                statGetProperty.clearParameters();                statGetProperty.setInt(1,id);                statGetProperty.setString(2,p.getType());                ResultSet rs = statGetProperty.executeQuery();                if(rs.next()){                    if(rs.getString("value").contentEquals("")){                        statAlterProperty.clearParameters();                        statAlterProperty.setInt(2,id);                        statAlterProperty.setString(3,p.getType());                        statAlterProperty.setString(1,p.getValue()[0].toString());                        statAlterProperty.executeUpdate();                    }else{                        statAddProperty.clearParameters();                        statAddProperty.setInt(1,id);                        statAddProperty.setString(2,p.getType());                        statAddProperty.setString(3,p.getValue()[p.getValue().length-1].toString());                        statAddProperty.executeUpdate();                    }                }else{                    statAddProperty.clearParameters();                    statAddProperty.setInt(1,id);                    statAddProperty.setString(2,p.getType());                    statAddProperty.setString(3,p.getValue()[p.getValue().length-1].toString());                    statAddProperty.executeUpdate();                }                rs.close();            } catch (SQLException ex) {                ex.printStackTrace();            }        }    }    private void addValue(Property property, String string) {        try {            if (property.getPropertyClass().getName().contentEquals("java.lang.Double")) {                property.add(Double.valueOf(string));            } else if (property.getPropertyClass().getName().contentEquals("java.lang.String")) {                property.add(string);            }        } catch (InvalidObjectTypeException ex) {            ex.printStackTrace();        }    }}