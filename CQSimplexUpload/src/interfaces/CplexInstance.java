/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package interfaces;

import executables.Main.ObjAlgorithm;
import ilog.concert.IloException;

/**
 *
 * @author AGomez
 */
public interface CplexInstance { 
    
   

    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    /**
     * Builds a CPLEX model. <br>
     * @param config The configuration to use for building the model. <br>
     * @return the model built. <br>
     * @throws ilog.concert.IloException
     */
    public CplexSolver build( ObjAlgorithm config) throws IloException;
    
 
    
    


}

