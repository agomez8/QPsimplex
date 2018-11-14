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
 *
 * @author AGomez.
 */
public class QuadraticSolverBissection extends CplexSolver {

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
    private long time;

    /**
     * The objective value.
     */
    private double objValue;

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
     *
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param linearObjective Coefficients of the linear part of the objective.
     * <br>
     * @throws ilog.concert.IloException If an exception occurs.
     */
    public QuadraticSolverBissection(double Omega, double[] linearObjective) throws IloException {
        super();
        this.Omega = Omega;
        this.linearCoefficients = linearObjective;
        this.numberIterations = 0;
        this.numberQPs = 0;
        tTolerance=1e-5;
    }

    /**
     * Constructor by parameters. <br>
     *
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param linearObjective Coefficients of the linear part of the objective.
     * <br>
     * @param cplex The CPLEX object. <br>
     * @param x The variables. <br>
     * @param constraints The constraints. <br>
     * @param linearPart The linear part. <br>
     * @param quadraticPart The quadratic part.
     */
    public QuadraticSolverBissection(double Omega, double[] linearObjective, IloCplex cplex, IloNumVar[] x, IloRange[] constraints, IloLinearNumExpr linearPart, IloQuadNumExpr quadraticPart) {
        super(cplex, x, constraints, linearPart, quadraticPart);
        this.Omega = Omega;
        this.linearCoefficients = linearObjective;
        tTolerance=1e-5;
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
     *
     * @return numberQPs.
     */
    @Override
    public int getQPs() {
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
    public void solve() throws IloException {
        long temp = System.currentTimeMillis();
        double minT=0, maxT=solveLP();
        //System.out.println(System.currentTimeMillis()-temp);
        double t = Double.POSITIVE_INFINITY, newT = 0;
        while (Math.abs(t - newT) / newT > tTolerance) {
//            System.out.println(Math.abs(t-newT)/newT+"\t"+numberIterations);
            t = (minT+maxT)/2.0;
            solvePrimal(t);
//            System.out.println(Math.abs(t-newT)+"\t"+numberIterations);
            newT = Math.sqrt(cplex.getValue(quadraticPart));
//            System.out.println(minT+"\t"+t+"/"+newT+"\t"+maxT+"\t"+(System.currentTimeMillis()-temp));
            if(t<=newT)
            {
                minT=newT;
            }
            else
                maxT=newT;
        }
        time = System.currentTimeMillis() - temp;
        objValue = cplex.getValue(linearPart) + Omega * Math.sqrt(cplex.getValue(quadraticPart));
    }

    /**
     * Solves the problem for a new value of t, using a primal approach. <br>
     * Assumes there is a solution. <br>
     *
     * @param t The new value to solve.
     */
    private void solvePrimal(double t) throws IloException {
        changeObjective(Omega / (2 * t));
//        changeObjective(Omega/(2*t),cplex.getBasisStatuses(x),cplex.getBasisStatuses(constraints));
        cplex.solve();
        numberIterations += cplex.getNiterations();
        numberQPs++;
    }

    /**
     * Iteration of quadratic simplex with no initial information.
     */
    private void start() throws IloException {
        cplex.solve();
        numberIterations += cplex.getNiterations();
        numberQPs++;

    }

    /**
     * Changes the objective. <br>
     *
     * @param beta The new coefficient for the quadratic part. <br>
     * @throws ilog.concert.IloException
     */
    private void changeObjective(double beta) throws IloException {
//        double[] values=cplex.getValues(x),dj=cplex.getReducedCosts(x),slack=cplex.getSlacks(constraints),dual=cplex.getDuals(constraints);
        double[] c = new double[linearCoefficients.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = linearCoefficients[i] / beta;

        }
        cplex.setLinearCoefs(cplex.getObjective(), x, c);
//        cplex.getObjective().setExpr(cplex.sum(linearPart, cplex.prod(quadraticPart, beta)));
//        cplex.setStart(values, dj, x, slack, dual, constraints);
//        cplex.setBasisStatuses(x, varStatus, constraints, conStatus);

    }

    /**
     * Solves the LP problem. <br>
     * Stores the basis information. The objective is stored with a nonlinear coefficient of 1. <br>
     * return The quadratic term value. <br>
     * @throws IloException 
     */
    private double solveLP() throws IloException {

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
