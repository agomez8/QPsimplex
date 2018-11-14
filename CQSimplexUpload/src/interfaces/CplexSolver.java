/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import executables.Main;
import executables.Main.ObjAlgorithm;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloQuadNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

/**
 * Interface to a cplex model. <br>
 *
 * @author AGomez
 */
public abstract class CplexSolver {
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------

    /**
     * Global instance of CPLEX.
     */
    private static IloCplex cplexGeneral;

    /**
     * Pointer to cplexGeneral.
     */
    public IloCplex cplex;

    /**
     * Variables.
     */
    public IloNumVar[] x;

    /**
     * Constraints.
     */
    public IloRange[] constraints;

    /**
     * Linear part of the conic quadratic term.
     */
    public IloLinearNumExpr linearPart;

    /**
     * Quadratic part.
     */
    public IloQuadNumExpr quadraticPart;
    
    /**
     * Coefficients of the linear part of the objective, separable quadratic part.
     */
    protected double[] linearCoefficients,quadraticCoefficients;

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Constructor by parameters. <br>
     *
     * @throws ilog.concert.IloException
     */
    public CplexSolver() throws IloException {
        cplex = cplexGeneral = (cplexGeneral == null ? new IloCplex() : cplexGeneral);

        cplex.clearModel();

        linearPart = cplex.linearNumExpr();
        quadraticPart = cplex.quadNumExpr();
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
    public CplexSolver(IloCplex cplex, IloNumVar[] x, IloRange[] constraints, IloLinearNumExpr linearPart, IloQuadNumExpr quadraticPart) {
        this.cplex = cplex;
        this.x = x;
        this.constraints = constraints;
        this.linearPart = linearPart;
        this.quadraticPart = quadraticPart;
    }

    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------
    /**
     * Gets the time used to solve the problem. <br>
     *
     * @return time.
     */
    public abstract long getTime();

    /**
     * Gets the objective value of the best feasible solution. <br>
     *
     * @return upperBound.
     */
    public abstract double getUpperBound();

    /**
     * Gets the best lower bound for the objective value. <br>
     *
     * @return lowerBound.
     */
    public abstract double getLowerBound();
    
    /**
     * Gets the value at the root node of branch and bound. <br>
     * @return rootRelaxation.
     */
    public abstract double getRootRelaxation();

    /**
     * Gets the number of iterations/nodes used to solve the problem. <br>
     *
     * @return iterations.
     */
    public abstract int getIterations();

    /**
     * Gets the number of QPs solved. <br>
     *
     * @return QPs.
     */
    public abstract int getQPs();

    /**
     * Gets the optimal solution for the problem. <br>
     *
     * @return solution.
     */
    public abstract double[] getSol();
    
    /**
     * Sets the tolerance for the solver. <br>
     * @param tolerance The tolerance to set.
     */
    public abstract void setTolerance(double tolerance);

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    /**
     * Solves the problem. <br>
     *
     * @throws ilog.concert.IloException
     */
    public abstract void solve() throws IloException;

    /**
     * Sets the configuration of the CPLEX solver according to the algorithm.
     * <br>
     *
     * @param algorithm The algorithm. <br>
     * @throws ilog.concert.IloException
     */
    public void setConfiguration(ObjAlgorithm algorithm) throws IloException {
        cplex.setParam(IloCplex.IntParam.Threads, 1);
        cplex.setParam(IloCplex.DoubleParam.TiLim, Main.TIME);
        cplex.setOut(null);
//        cplex.setOut(null);
        switch (algorithm) {
            case DEFAULT_CPLEX:
                cplex.setParam(IloCplex.IntParam.MIQCPStrat, 2);
                break;
            case QCP_APPROX:
                cplex.setParam(IloCplex.IntParam.MIQCPStrat, 1);
                turnOffFeatures();
                break;
            case LP_APPROX_EXTENDED:
                cplex.setParam(IloCplex.IntParam.MIQCPStrat, 2);
                turnOffFeatures();
                break;
            case LP_APPROX_NAIVE:
                cplex.setParam(IloCplex.IntParam.MIQCPStrat, 2);
                turnOffFeatures();
                 {
                    cplex.setParam(IloCplex.IntParam.Symmetry, 0);
                    cplex.setParam(IloCplex.IntParam.AggFill, 0);
                    cplex.setParam(IloCplex.IntParam.AggInd, 0);
                    cplex.setParam(IloCplex.IntParam.BndStrenInd, 0);
                    cplex.setParam(IloCplex.IntParam.CoeRedInd, 0);
                    cplex.setParam(IloCplex.IntParam.DepInd, 0);
                    cplex.setParam(IloCplex.IntParam.PreDual, -1);
                    cplex.setParam(IloCplex.BooleanParam.PreInd, false);
                    cplex.setParam(IloCplex.IntParam.PreLinear, 0);
                    cplex.setParam(IloCplex.IntParam.PrePass, 0);
                    cplex.setParam(IloCplex.IntParam.PreslvNd, -1); // No presolve at nodes 
                    cplex.setParam(IloCplex.IntParam.RelaxPreInd, 0);
                    cplex.setParam(IloCplex.IntParam.RepeatPresolve, 0);
                    cplex.setParam(IloCplex.IntParam.Reduce, 0);
                }
                break;
            case QUADRATIC_CONTINUOUS_COORDINATE:
            case QUADRATIC_CONTINUOUS_BISECTION:
            case QUADRATIC_CONTINUOUS_BISECTION_BASIC:
            case QUADRATIC_CONTINUOUS_COORDINATE_LP: 
            case QUADRATIC_INTEGER:
                cplex.setOut(null);
                cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
                break;
            case BARRIER_INTEGER:
                cplex.setOut(null);
                cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Barrier);
                break;
            case BARRIER_CONTINUOUS:
                cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Barrier);
                break;
        }
    }

   

    /**
     * Turns off different features of CPLEX
     *
     * @throws IloException
     */
    private void turnOffFeatures() throws IloException {
        // Turns off presolve
//        cplex.setParam(IloCplex.IntParam.Symmetry, 0);
//        cplex.setParam(IloCplex.IntParam.AggFill, 0);
//        cplex.setParam(IloCplex.IntParam.AggInd, 0);
//        cplex.setParam(IloCplex.IntParam.BndStrenInd, 0);
//        cplex.setParam(IloCplex.IntParam.CoeRedInd, 0);
//        cplex.setParam(IloCplex.IntParam.DepInd, 0);
//        cplex.setParam(IloCplex.IntParam.PreDual, -1);
//        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
//        cplex.setParam(IloCplex.IntParam.PreLinear, 0);
//        cplex.setParam(IloCplex.IntParam.PrePass, 0);
//        cplex.setParam(IloCplex.IntParam.PreslvNd, -1); // No presolve at nodes 
//        cplex.setParam(IloCplex.IntParam.RelaxPreInd, 0);
//        cplex.setParam(IloCplex.IntParam.RepeatPresolve, 0);
//        cplex.setParam(IloCplex.IntParam.Reduce, 0);

        // Turning off cuts
        cplex.setParam(IloCplex.IntParam.EachCutLim, 0);
        cplex.setParam(IloCplex.IntParam.CutPass, -1);
        cplex.setParam(IloCplex.DoubleParam.CutsFactor, 1.0);
        cplex.setParam(IloCplex.IntParam.AggCutLim, 0);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MCFCuts, -1);// Multi commodity cuts.
        cplex.setParam(IloCplex.IntParam.LiftProjCuts, -1); // Lift and project cuts.

        // Turning off heursitics
        cplex.setParam(IloCplex.IntParam.RINSHeur, -1); // No heuristics for improving feasible solutions
        cplex.setParam(IloCplex.IntParam.HeurFreq, -1); // No periodic heuristics

        // Branch and node selection
        cplex.setParam(IloCplex.IntParam.NodeSel, 1);
        cplex.setParam(IloCplex.IntParam.VarSel, 1);
//        cplex.setParam(IloCplex.IntParam.BrDir, -1);//Processes down branch first
        cplex.setParam(IloCplex.IntParam.DiveType, 1);//Does not dive
        cplex.setParam(IloCplex.IntParam.Probe, -1); // No probing
        cplex.setParam(IloCplex.IntParam.FPHeur, -1); // No feasibility pump

    }
    
}
