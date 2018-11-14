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
import ilog.cplex.IloCplex.BasisStatus;
import interfaces.CplexSolver;

/**
 * Quadratic simplex solver for a conic program. <br>
 * @author AGomez. 
 */
public class QuadraticSolver extends CplexSolver{
    //--------------------------------------------------------------------------
    // Tolerance
    //--------------------------------------------------------------------------
    
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Nonlinear term.
     */
    protected double Omega;
    
    
    /**
     * Time used to solve the problem.
     */
    long time;
    
    /**
     * The objective value.
     */
    double objValue;
    
    /**
     * Cplex numberIterations used, QPs solved.
     */
    private int numberIterations, numberQPs;
    
        /**
     * Tolerance in the stopping criterion.
     */
    double tTolerance;

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Constructor by parameters. <br>
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param linearObjective Coefficients of the linear part of the objective. <br>
     * @throws ilog.concert.IloException If an exception occurs.
     */
    public QuadraticSolver(double Omega,double[] linearObjective) throws IloException {
        super();
        this.Omega=Omega;
        this.linearCoefficients=linearObjective;
        this.numberIterations=0;
        this.numberQPs=0;
        tTolerance=1e-5;
        
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
    public QuadraticSolver(double Omega,double[] linearObjective, IloCplex cplex, IloNumVar[] x, IloRange[] constraints, IloLinearNumExpr linearPart, IloQuadNumExpr quadraticPart) {
        super(cplex, x, constraints, linearPart, quadraticPart);
        this.Omega = Omega;
        this.linearCoefficients=linearObjective;
        tTolerance=1e-5;
    }
    
     /**
     * Constructor by parameters. <br>
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param linearObjective Coefficients of the linear part of the objective. <br>
     * @param quadraticCoefficients Coefficients for the separable quadratic part of the objective. <br>
     * @param cplex The CPLEX object. <br>
     * @param x The variables. <br>
     * @param constraints The constraints. <br>
     * @param linearPart The linear part. <br>
     * @param quadraticPart The quadratic part.
     */
    public QuadraticSolver(double Omega,double[] linearObjective,double[] quadraticCoefficients, IloCplex cplex, IloNumVar[] x, IloRange[] constraints, IloLinearNumExpr linearPart, IloQuadNumExpr quadraticPart) {
        super(cplex, x, constraints, linearPart, quadraticPart);
        this.Omega = Omega;
        this.linearCoefficients=linearObjective;
        this.quadraticCoefficients=quadraticCoefficients;
    }
    
    

    
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------
     @Override
    public long getTime() {
        return time;
    }

    @Override
    public double getUpperBound() {
        return objValue;
    }
    
    @Override
    public double getLowerBound() {
        return getUpperBound();
    }

    @Override
    public int getIterations() {
        return numberIterations;
    }
    
    /**
     * Gets the number of QPs solved. <br>
     * @return numberQPs.
     */
    @Override
    public int getQPs()
    {
        return numberQPs;
    }
    
    @Override
    public double[] getSol() {
        try {
            return cplex.getValues(x);
        } catch (IloException ex) {
            return null;
        }
    }
    
    @Override
    public double getRootRelaxation() {
        return getUpperBound();
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @Override
    public void solve() throws IloException{
        long temp=System.currentTimeMillis();
        start();
        double t=Double.POSITIVE_INFINITY, newT=Math.sqrt(cplex.getValue(quadraticPart));
        while(Math.abs(t-newT)/newT>tTolerance)
        {
            t=newT;
            solvePrimal(t);
            newT=Math.sqrt(cplex.getValue(quadraticPart));
        }
        time=System.currentTimeMillis()-temp;
        objValue=cplex.getValue(linearPart)+Omega*Math.sqrt(cplex.getValue(quadraticPart));
    }
    
    
    
    
    /**
     * Solves the problem using warm start information. <br>
     * Uses dual simplex to solve the first quadratic problem. <br>
     * @param t The value of t to use. <br>
     * @param varStatus The basis status of the variables. <br>
     * @param conStatus The basis status of the constraints. <br>
     * @throws ilog.concert.IloException
     */
    public void solve(double t, BasisStatus[] varStatus,BasisStatus[] conStatus) throws IloException{
        long temp=System.currentTimeMillis();
        cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
        warmStart(t, varStatus, conStatus);
        cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
        double newT=Math.sqrt(cplex.getValue(quadraticPart));
        while(Math.abs(t-newT)/newT>tTolerance)
        {
            t=newT;
            solvePrimal(t);
            newT=Math.sqrt(cplex.getValue(quadraticPart));
        }
        time=System.currentTimeMillis()-temp;
        objValue=cplex.getValue(linearPart)+Omega*Math.sqrt(cplex.getValue(quadraticPart));
    }
    
    /**
     * Solves the problem for a new value of t, using a primal approach. <br>
     * Assumes there is a solution. <br>
     * @param t The new value to solve.
     */
    protected void solvePrimal(double t) throws IloException
    {
        changeObjective(Omega/(2*t));
        cplex.solve();
        numberIterations+=cplex.getNiterations();
        numberQPs++;
    }
    
    /**
     * Iteration of quadratic simplex with no initial information.
     */
    protected void start() throws IloException
    {
        cplex.solve();
        numberIterations+=cplex.getNiterations();
        numberQPs++;
        
    }
    
    /**
     * Iteration of quadratic simplex using warm start information. <br>
     * @param t The value of t to use. <br>
     * @param varStatus The basis status of the variables. <br>
     * @param conStatus The basis status of the constraints. <br>
     * @throws ilog.concert.IloException
     */
    protected void warmStart(double t, BasisStatus[] varStatus,BasisStatus[] conStatus) throws IloException
    {
        changeObjective(Omega/(2*t), varStatus, conStatus);
        cplex.solve();
        numberIterations+=cplex.getNiterations();
        numberQPs++;
    }
    
    /**
     * Changes the objective. <br>
     *
     * @param beta The new coefficient for the quadratic part. <br>
     * @param varStatus Status of the variables. <br>
     * @param conStatus Status of the constraints. <br>
     * @throws ilog.concert.IloException
     */
    protected void changeObjective(double beta,BasisStatus[] varStatus,BasisStatus[] conStatus) throws IloException {
       double[] c=new double[linearCoefficients.length];
        for (int i = 0; i < c.length; i++) {
            c[i]=linearCoefficients[i]/beta;
            
        }
        cplex.setLinearCoefs(cplex.getObjective(), x, c);
        cplex.setBasisStatuses(x, varStatus, constraints, conStatus);
        
    }
    
    /**
     * Changes the objective. <br>
     *
     * @param beta The new coefficient for the quadratic part. <br>
     * @throws ilog.concert.IloException
     */
    protected void changeObjective(double beta) throws IloException {
        double[] c=new double[linearCoefficients.length];
        for (int i = 0; i < c.length; i++) {
            c[i]=linearCoefficients[i]/beta;   
        }
        cplex.setLinearCoefs(cplex.getObjective(), x, c);

        
    }
    
    /**
     * Solves the LP problem. <br>
     * Stores the basis information. The objective is stored with a nonlinear coefficient of 1. <br>
     * @return The quadratic term value. <br>
     * @throws IloException 
     */
    protected double solveLP() throws IloException {

        cplex.getObjective().setExpr(linearPart);
        cplex.solve();
        numberIterations += cplex.getNiterations();
        numberQPs++;
        BasisStatus[] varStatus = cplex.getBasisStatuses(x),conStatus = cplex.getBasisStatuses(constraints);
        double t=cplex.getValue(quadraticPart);
        cplex.getObjective().setExpr(cplex.sum(linearPart, cplex.prod(quadraticPart, 1)));
        cplex.setBasisStatuses(x, varStatus, constraints, conStatus);
        return t;
    }

    @Override
    public void setTolerance(double tolerance) {
        tTolerance=tolerance;
    }
    
   
}
