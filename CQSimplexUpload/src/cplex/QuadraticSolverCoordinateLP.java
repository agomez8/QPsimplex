/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cplex;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloQuadNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

/**
 * Quadratic simplex solver for a conic program. <br>
 * The first LP in the sequence is an LP.
 * @author AGomez. 
 */
public class QuadraticSolverCoordinateLP extends QuadraticSolver{
    //--------------------------------------------------------------------------
    // Tolerance
    //--------------------------------------------------------------------------
     
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Constructor by parameters. <br>
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param linearObjective Coefficients of the linear part of the objective. <br>
     * @throws ilog.concert.IloException If an exception occurs.
     */
    public QuadraticSolverCoordinateLP(double Omega,double[] linearObjective) throws IloException {
        super(Omega,linearObjective);
        
    }

    /**
     * Constructor by parameters. <br>
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param linearObjective Coefficients of the linear part of the objective. <br>
     * @param cplex The CPLEX object. <br>
     * @param x The variables. <br>
     * @param constraints The constraints. <br>
     * @param linearPart The linear part. <br>
     * @param quadraticPart The quadratic part.
     */
    public QuadraticSolverCoordinateLP(double Omega,double[] linearObjective, IloCplex cplex, IloNumVar[] x, IloRange[] constraints, IloLinearNumExpr linearPart, IloQuadNumExpr quadraticPart) {
        super(Omega,linearObjective,cplex,x,constraints,linearPart,quadraticPart);
    }
    
    

    
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------
    
    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @Override
    public void solve() throws IloException{
        long temp=System.currentTimeMillis();
        double t=Double.POSITIVE_INFINITY, newT=solveLP();
        while(Math.abs(t-newT)/newT>tTolerance)
        {
            t=newT;
            solvePrimal(t);            
            newT=Math.sqrt(cplex.getValue(quadraticPart));
        }
        time=System.currentTimeMillis()-temp;
        objValue=cplex.getValue(linearPart)+Omega*Math.sqrt(cplex.getValue(quadraticPart));
    }
    
    
    
    
   
    
    
    

    

   
}
