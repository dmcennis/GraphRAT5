/*

 * PathFactory.java

 *

 * Created on 2 July 2007, 20:05

 *

 * Copyright Daniel McEnnis, published under Aferro GPL (see license.txt)

 */



package nz.ac.waikato.mcennis.rat.graph.path;



import java.util.Properties;



/**

 *


 * Class for Creating Path Objects

 * 

 * @author Daniel McEnnis
 */

public class PathFactory {

    private static PathFactory instance = null;

    

    /**

     * Creates a reference to a singelton PathFactory object

     * 

     * @return 

     */

    public static PathFactory newInstance(){

        if(instance == null){

            instance = new PathFactory();

        }

        return instance;

    }

    

    /** Creates a new instance of PathFactory */

    private PathFactory() {

    }

    

    /**

     * Create a new Path.  Currently ignores parameters and only returns

     * an uninitialized BasicPath object.

     * @param props ignored

     * @return new Path object

     */

    public Path create(Properties props){

        return new BasicPath();

    }

}
