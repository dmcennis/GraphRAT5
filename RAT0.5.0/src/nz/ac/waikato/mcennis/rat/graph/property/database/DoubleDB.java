/*
 * DoubleDB - created 14/03/2009 - 6:34:10 PM
 * Copyright Daniel McEnnis, see license.txt
 */

package nz.ac.waikato.mcennis.rat.graph.property.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel McEnnis
 */
public class DoubleDB extends AbstractPropertyDB<Double>{
    PreparedStatement get = null;
    PreparedStatement put = null;
    PreparedStatement putGet = null;
    PreparedStatement delete = null;

    public StringDB newCopy() {
        return new StringDB();
    }

    public void initializeDatabase(Connection conn) {
        if(conn != null){
            try {
                Statement stat = conn.createStatement();
                stat.executeUpdate("CREATE TABLE Double ("
                        + "id integer not null generated ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                        + "double double not null, "
                        + "primary key(id))");
            } catch (SQLException ex) {
                Logger.getLogger(URLDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setConnection(Connection con) {
        try {
            conn = con;
            get = conn.prepareStatement("SELECT double FROM Double WHERE id=?");
            put = conn.prepareStatement("INSERT INTO Double (double) VALUES (?)");
            putGet = conn.prepareStatement("SELECT MAX(id) FROM Double");
            delete = conn.prepareStatement("DELETE FROM Double WHERE id=?");
        } catch (SQLException ex) {
            Logger.getLogger(StringDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearDatabase(Connection conn) {
        try {
            conn.createStatement().executeUpdate("DELETE FROM Double");
        } catch (SQLException ex) {
            Logger.getLogger(StringDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Double get(int i) {
            Double ret=(double)0.0;
            ResultSet rs = null;
        try {
            get.clearParameters();
            get.setInt(1, i);
            rs = get.executeQuery();
            if (rs.next()) {
                ret = rs.getDouble("double");
            }
        } catch (SQLException ex) {
            Logger.getLogger(StringDB.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {}
                rs = null;
            }
        }
            return ret;
    }

    public int put(Double object) {
        int ret = -1;
        ResultSet rs = null;
        try {
            double url = object.doubleValue();
            put.clearParameters();
            putGet.clearParameters();
            put.setDouble(1, url);
            put.executeUpdate();
            rs = putGet.executeQuery();
            if(rs.next()){
                ret = rs.getInt("id");
            }else{
                Logger.getLogger(URLDB.class.getName()).log(Level.SEVERE, "Put method for Double '"+object.toString()+"' silently failed");
            }
        } catch (SQLException ex) {
            Logger.getLogger(URLDB.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {}
                rs = null;
            }
        }
        return ret;
    }

    public void remove(int id) {
        try {
            delete.clearParameters();
            delete.setInt(1, id);
            delete.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DoubleDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Class getValueClass() {
        return Double.class;
    }

}