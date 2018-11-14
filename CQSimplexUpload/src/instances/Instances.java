/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package instances;

import instances.GridDagInstance;
import interfaces.CplexInstance;
import java.util.Random;

/**
 * Class for creating instances. <br>
 * @author AGomez.
 */
public final class Instances {
    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------
    public enum Instance{PATH_DAG,UNIFORM_MATROID,
     NULL;
    public static Instance toInstance(int i)
    {
        switch(i){
            case 1:return PATH_DAG;
            case 2: return UNIFORM_MATROID;
            
        }
        return NULL;
    }}
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------
    

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    /**
     * Gets a new instance of a DAG generated using the argument seed. <br>
     * @param size Size of the grid. <br>
     * @param rank Rank of the objective. <br>
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param diagonal Coefficient of the diagonal matrix. <br>
     * @param density Density of F. <br>
     * @param abs Whether to use only positive values or not. <br>
     * @param r The random number generator. <br>
     * @return The DAG with the specified characteristics.
     */
    public static CplexInstance getDagInstance(int size, int rank, double Omega,double diagonal, double density, boolean abs, Random r)
    {
        return new GridDagInstance(size, rank,Omega,diagonal,density,abs,r);
    }
    
    public static CplexInstance getCardinalityInstance(int size,int cardinality, int rank, double Omega,double diagonal, double density, boolean abs, Random r)
    {
        return new CardinalityInstance(size,cardinality, rank,Omega,diagonal,density,abs,r);
    }
    
}
