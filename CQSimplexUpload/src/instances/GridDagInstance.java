package instances;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import cplex.BranchAndBound;
import cplex.ConicSolver;

import cplex.QuadraticSolverBissection;
import cplex.QuadraticSolver;
import cplex.QuadraticSolverBissectionBasic;
import cplex.QuadraticSolverCoordinateLP;
import executables.Main.ObjAlgorithm;
import static executables.Main.ObjAlgorithm.BARRIER_CONTINUOUS;
import static executables.Main.ObjAlgorithm.DEFAULT_CPLEX;
import static executables.Main.ObjAlgorithm.QCP_APPROX;
import static executables.Main.ObjAlgorithm.QUADRATIC_CONTINUOUS_BISECTION;
import static executables.Main.ObjAlgorithm.QUADRATIC_INTEGER;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjectiveSense;
import ilog.concert.IloRange;
import interfaces.CplexInstance;
import interfaces.CplexSolver;
import interfaces.PositiveSDefiniteMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A sparse directed acyclic graph. <br>
 *
 * @author Andres Gomez.
 */
public class GridDagInstance implements CplexInstance {

    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Number of vertices.
     */
    int vertices;

    /**
     * List of arcs. <br>
     * arc: {head,tail}.
     */
    List<int[]> arcs;

    /**
     * Objective.
     */
    PositiveSDefiniteMatrix pdMatrix;

    /**
     * Objective costs.
     */
    double[] linCosts, linObjective;

    /**
     * Coefficient for the nonlinear term.
     */
    double Omega;

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Constructor. <br>
     *
     * @param size Size of the grid. <br>
     * @param rank Rank of the objective. <br>
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param diagonal Coefficient of the diagonal matrix. <br>
     * @param density Density of F. <br>
     * @param abs Whether to use only positive values or not. <br>
     * @param r The random number generator to use.
     */
    GridDagInstance(int size, int rank, double Omega, double diagonal, double density, boolean abs, Random r) {
        vertices = size * size;
        this.Omega = Omega;
        int[][] position = new int[size][size];
        int number = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                position[i][j] = number++;
            }
        }
        arcs = new ArrayList<>();
        // Generates arcs with costs and means.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i < size - 1) {
                    arcs.add(new int[]{position[i][j], position[i + 1][j]});
                }
                if (j < size - 1) {
                    arcs.add(new int[]{position[i][j], position[i][j + 1]});
                }
            }
        }

        linCosts = new double[arcs.size()];

        pdMatrix = new PSDObjective(arcs.size(), rank, diagonal, density, abs, r);
//        pdMatrix = new PSDObjective(arcs.size(), r);
        double varAverage = 0;
        for (int i = 0; i < linCosts.length; i++) {
            varAverage += Math.sqrt(pdMatrix.M(i, i));
        }
        varAverage /= (double) linCosts.length;
        for (int i = 0; i < linCosts.length; i++) {
//            linCosts[i] = 2 * r.nextDouble() * varAverage * varAverage / Math.sqrt(pdMatrix.M(i, i));
//            linCosts[i] = 2*r.nextDouble()*varAverage;
            linCosts[i] = -2 * r.nextDouble() * Math.sqrt(pdMatrix.M(i, i));
        }

        linObjective = new double[arcs.size()];
        for (int i = 0; i < linObjective.length; i++) {
            linObjective[i] = r.nextDouble();
        }
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    @Override
    public CplexSolver build(ObjAlgorithm algorithm) throws IloException {
        assert !algorithm.equals(ObjAlgorithm.NULL);
        CplexSolver model;
        switch (algorithm) {
            case QUADRATIC_CONTINUOUS_COORDINATE:
                model = new QuadraticSolver(Omega, linCosts);
                break;
            case QUADRATIC_CONTINUOUS_BISECTION:
                model = new QuadraticSolverBissection(Omega, linCosts);
                break;
            case QUADRATIC_CONTINUOUS_BISECTION_BASIC:
                model = new QuadraticSolverBissectionBasic(Omega, linCosts);
                break;
            case QUADRATIC_CONTINUOUS_COORDINATE_LP:
                model = new QuadraticSolverCoordinateLP(Omega, linCosts);
                break;
            case QUADRATIC_INTEGER:
            case BARRIER_INTEGER:
                model = new BranchAndBound(Omega, linCosts);
                break;
            default:
                model = new ConicSolver();
                break;
        }

        switch (algorithm) {
            case QUADRATIC_CONTINUOUS_COORDINATE:
            case QUADRATIC_CONTINUOUS_BISECTION:
            case QUADRATIC_CONTINUOUS_BISECTION_BASIC:
            case QUADRATIC_CONTINUOUS_COORDINATE_LP:
            case BARRIER_CONTINUOUS:
            case QUADRATIC_INTEGER:
            case BARRIER_INTEGER:

                model.x = new IloNumVar[arcs.size()];
                for (int i = 0; i < model.x.length; i++) {
                    model.x[i] = model.cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "x" + (int) arcs.get(i)[0] + "," + (int) arcs.get(i)[1]);
                }
                break;

            case DEFAULT_CPLEX:
            case LP_APPROX_EXTENDED:
            case LP_APPROX_NAIVE:
            case QCP_APPROX:
                model = new ConicSolver();

                model.x = new IloNumVar[arcs.size()];
                for (int i = 0; i < model.x.length; i++) {
                    model.x[i] = model.cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Bool, "x" + (int) arcs.get(i)[0] + "," + (int) arcs.get(i)[1]);
                }

                break;

        }

        addFlowConservationConstraints(model);

        /* Linear part*/
        for (int i = 0; i < arcs.size(); i++) {
            model.linearPart.addTerm(model.x[i], linCosts[i]); // Updates also the linear objective.

        }

        /* Quadratic part */
        for (int i = 0; i < arcs.size(); i++) {
            model.quadraticPart.addTerm(pdMatrix.M(i, i), model.x[i], model.x[i]);
            for (int j = i + 1; j < arcs.size(); j++) {
                if (pdMatrix.M(i, j) != 0) {
                    model.quadraticPart.addTerm(2 * pdMatrix.M(i, j), model.x[i], model.x[j]);
                }
            }

        }

        model.cplex.addMinimize(model.cplex.sum(model.linearPart, model.quadraticPart));
        switch (algorithm) {

            case QUADRATIC_CONTINUOUS_COORDINATE:
            case QUADRATIC_CONTINUOUS_BISECTION:
            case QUADRATIC_CONTINUOUS_BISECTION_BASIC:
            case QUADRATIC_CONTINUOUS_COORDINATE_LP:
            case QUADRATIC_INTEGER:
                break;
            case BARRIER_CONTINUOUS:
            case DEFAULT_CPLEX:
            case LP_APPROX_EXTENDED:
            case LP_APPROX_NAIVE:
            case QCP_APPROX:
            case BARRIER_INTEGER:
                IloNumVar sd = model.cplex.numVar(0, Double.POSITIVE_INFINITY, "z");
                IloNumExpr conic = model.cplex.sum(model.quadraticPart, model.cplex.prod(sd, sd, -1));
                model.cplex.addLe(conic, 0.0);
                if (model.cplex.getObjective() == null)// If no objective, adds an empty objective.
                {
                    model.cplex.addObjective(IloObjectiveSense.Minimize);
                }
                model.cplex.getObjective().setExpr(model.cplex.sum(model.linearPart, model.cplex.prod(Omega, sd)));

        }

        if (model instanceof BranchAndBound) {
            ((BranchAndBound) model).setSolver(algorithm);
        }

        return model;
    }

    /**
     * Adds the linear constraints to the model. <br>
     *
     * @param cplex The cplex object.
     */
    private void addFlowConservationConstraints(CplexSolver model) throws IloException {
        /*Flow conservation*/
        IloLinearNumExpr[] flowConservation = new IloLinearNumExpr[vertices];
        flowConservation[0] = model.cplex.linearNumExpr(-1);
        flowConservation[flowConservation.length - 1] = model.cplex.linearNumExpr(1);
        model.linearPart = model.cplex.linearNumExpr();
        model.quadraticPart = model.cplex.quadNumExpr();
        for (int i = 1; i < flowConservation.length - 1; i++) {
            flowConservation[i] = model.cplex.linearNumExpr();
        }
        for (int i = 0; i < arcs.size(); i++) {
            int[] arc = arcs.get(i);
            flowConservation[arc[0]].addTerm(model.x[i], 1);
            flowConservation[arc[1]].addTerm(model.x[i], -1);

        }

        /* Adds the constraints */
        model.constraints = new IloRange[flowConservation.length];
        for (int i = 0; i < flowConservation.length; i++) {

            model.constraints[i] = model.cplex.addEq(flowConservation[i], 0, "Flow conservation at " + i);
        }
    }

    /**
     * Gets the linear costs.
     *
     * @return costs.
     */
    public double[] getC() {
        return linCosts;
    }

    /**
     * Gets the Q matrix. <br>
     *
     * @return Q.
     */
    public PositiveSDefiniteMatrix getQ() {
        return pdMatrix;
    }
}
