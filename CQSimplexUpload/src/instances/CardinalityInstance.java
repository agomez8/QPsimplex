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
import java.util.Random;

/**
 * A uniform matroid. <br>
 *
 * @author Andres Gomez.
 */
public class CardinalityInstance implements CplexInstance {

    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Number of items and cardinality.
     */
    int items, cardinality;

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
     * @param size Number of items. <br>
     * @param cardinality Maximum cardinality. <br>
     * @param rank Rank of the objective. <br>
     * @param Omega Coefficient of the nonlinear term. <br>
     * @param diagonal Coefficient of the diagonal matrix. <br>
     * @param density Density of F. <br>
     * @param abs Whether to use only positive values or not. <br>
     * @param r The random number generator to use.
     */
    CardinalityInstance(int size, int cardinality, int rank, double Omega, double diagonal, double density, boolean abs, Random r) {
        items = size;
        this.cardinality = cardinality;
        this.Omega = Omega;

        linCosts = new double[items];

        pdMatrix = new PSDObjective(items, rank, diagonal, density, abs, r);
        double varAverage = 0;
        for (int i = 0; i < linCosts.length; i++) {
            varAverage += Math.sqrt(pdMatrix.M(i, i));
        }
        varAverage /= (double) linCosts.length;
        for (int i = 0; i < linCosts.length; i++) {
//            linCosts[i] = 2 * r.nextDouble() * varAverage * varAverage / Math.sqrt(pdMatrix.M(i, i));
            linCosts[i] = -2 * r.nextDouble() * Math.sqrt(pdMatrix.M(i, i));
        }

        linObjective = new double[items];
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

                model.x = new IloNumVar[items];
                for (int i = 0; i < model.x.length; i++) {
                    model.x[i] = model.cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "x" + i);
                }
                break;
            case DEFAULT_CPLEX:
            case LP_APPROX_EXTENDED:
            case LP_APPROX_NAIVE:
            case QCP_APPROX:
                model = new ConicSolver();

                model.x = new IloNumVar[items];
                for (int i = 0; i < model.x.length; i++) {
                    model.x[i] = model.cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Bool, "x" + i);
                }

                break;

        }
        // Normal formulation
        addCardinalityConstraint(model);
        /* Linear part*/
        for (int i = 0; i < items; i++) {
            model.linearPart.addTerm(model.x[i], linCosts[i]); // Updates also the linear objective.

        }

        /* Quadratic part */
        for (int i = 0; i < items; i++) {
            model.quadraticPart.addTerm(pdMatrix.M(i, i), model.x[i], model.x[i]);
            for (int j = i + 1; j < items; j++) {
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
            case QUADRATIC_INTEGER:
            case QUADRATIC_CONTINUOUS_COORDINATE_LP:
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
    private void addCardinalityConstraint(CplexSolver model) throws IloException {
        /*Flow conservation*/
        IloLinearNumExpr cardinalityExpr = model.cplex.linearNumExpr(-cardinality);

        model.linearPart = model.cplex.linearNumExpr();
        model.quadraticPart = model.cplex.quadNumExpr();

        for (int i = 0; i < items; i++) {
            cardinalityExpr.addTerm(model.x[i], 1);
        }

        /* Adds the constraints */
        model.constraints = new IloRange[1];

        model.constraints[0] = model.cplex.addEq(cardinalityExpr, 0, "Cardinality constraint");
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
