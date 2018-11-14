/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cplex;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjectiveSense;
import ilog.concert.IloQuadNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import interfaces.CplexSolver;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A conic solver using CPLEX. <br>
 *
 * @author AGomez
 */
public class ConicSolver extends CplexSolver {

    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Time used to solve the problem.
     */
    private long time;

    /**
     * Value at the root node.
     */
    double rootRelaxation;

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Constructor by parameters. <br>
     *
     * @throws ilog.concert.IloException If an exception occurs.
     */
    public ConicSolver() throws IloException {
        super();

    }

    /**
     * Constructor by parameters. <br>
     *
     * @param cplex The cplex object. <br>
     * @param x The variables. <br>
     * @param constraints The constraints. <br>
     * @param linearPart The linear part. <br>
     * @param quadraticPart The quadratic part.
     */
    public ConicSolver(IloCplex cplex, IloNumVar[] x, IloRange[] constraints, IloLinearNumExpr linearPart, IloQuadNumExpr quadraticPart) {
        super(cplex, x, constraints, linearPart, quadraticPart);
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
        try {
            return cplex.getObjValue();
        } catch (IloException ex) {
            return Double.POSITIVE_INFINITY;
        }
    }

    @Override
    public double getLowerBound() {
        try {
            if (cplex.isMIP()) {
                return cplex.getObjValue() - cplex.getMIPRelativeGap() * (cplex.getObjValue() + 1e-10);
            }
            return getUpperBound();
        } catch (IloException ex) {
            Logger.getLogger(ConicSolver.class.getName()).log(Level.SEVERE, null, ex);
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public int getIterations() {
        if (cplex.isMIP()) {
            return cplex.getNnodes();
        }
        return cplex.getNbarrierIterations();

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
        return rootRelaxation;
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    @Override
    public void solve() throws IloException {
        long temp = System.currentTimeMillis();
        if (cplex.isMIP()) {
            cplex.setParam(IloCplex.IntParam.NodeLim, 0); // Stop after root node.
            cplex.solve();
            rootRelaxation = cplex.getBestObjValue();
            cplex.setParam(IloCplex.IntParam.NodeLim, 210000000);

        }
        cplex.solve();

        time = System.currentTimeMillis() - temp;
    }

    /**
     * Adds a conic constraint to the problem. <br>
     * If the objective is null, creates the objective. Otherwise adds the conic
     * term to the existing objective. <br>
     *
     * @param quadraticExpression The quadratic expression for the conic
     * objective. <br>
     * @param Omega Coefficient for the conic term. <br>
     * @throws ilog.concert.IloException
     */
    public void addConicObjective(IloQuadNumExpr quadraticExpression, double Omega) throws IloException {
        IloNumVar sd = cplex.numVar(0, Double.POSITIVE_INFINITY, "z");
        IloNumExpr conic = cplex.sum(quadraticPart, cplex.prod(sd, sd, -1));
        cplex.addLe(conic, 0.0);
        if (cplex.getObjective() == null)// If no objective, adds an empty objective.
        {
            cplex.addObjective(IloObjectiveSense.Minimize);
        }
        cplex.getObjective().setExpr(cplex.sum(cplex.prod(Omega, sd), cplex.getObjective().getExpr()));
    }

    @Override
    public int getQPs() {
        return 0;
    }

    @Override
    public void setTolerance(double tolerance) {
        try {
            cplex.setParam(IloCplex.DoubleParam.BarQCPEpComp, tolerance);
        } catch (IloException ex) {
            Logger.getLogger(ConicSolver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
