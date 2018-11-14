/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cplex;

import executables.Main;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import interfaces.CplexSolver;
import java.util.TreeSet;

/**
 * Class in charge of branch and bound.
 *
 * @author AGomez
 */
public class BranchAndBound extends CplexSolver {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------
    /**
     * Precision in numerical computations.
     */
    protected static final double INTEGER_PRECISION = 1e-5;

    /**
     * Gap tolerance.
     */
    private static final double GAP_TOLERANCE = 1e-4;
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Best bound found in branch and bound tree.
     */
    protected double upperBound;

    /**
     * Value found at the root node.
     */
    double rootRelaxation;

    /**
     * Solver for the quadratic problems.
     */
    protected CplexSolver solver;

    /**
     * Nodes to process.
     */
    private final TreeSet<NodeInfo> nodes;

    /**
     * Number of branch and bound nodes processed.
     */
    protected int iterations;

    /**
     * Number of nodes created.
     */
    private int count;

    /**
     * Time to solver the problem.
     */
    private long time;

    /**
     * Coefficient for the nonlinear term.
     */
    private double Omega;

    /**
     * Number of continuous variables. Assumes the continuous variables are the
     * first 'numContinuous' in variables.
     */
    private int numContinuous;

    /**
     * Total number of variables. Assumes the integer variables go from
     * 'numContinuous' to numVariables-1.
     */
    private int numVariables;

    /**
     * Linear coefficients.
     */
    //private double[] linearCoefficients;


    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param Omega The coefficient of the nonlinear term. <br>
     * @param linearCoefficients Coefficients of the linear part of the
     * objective. <br>
     * @throws ilog.concert.IloException
     */
    public BranchAndBound(double Omega, double[] linearCoefficients) throws IloException {
        this.upperBound = Double.POSITIVE_INFINITY;
        nodes = new TreeSet<>();
        this.Omega = Omega;
        this.linearCoefficients = linearCoefficients;
    }

    /**
     * Constructor. <br>
     *
     * @param Omega The coefficient of the nonlinear term. <br>
     * @param linearCoefficients Coefficients of the linear part of the
     * objective. <br>
     * @param quadraticCoefficients Coefficients for the separable quadratic. <br>
     * @param numContinuous Number of continuous variables. <br>
     * @param numVariables Total number of variables. <br>
     * @throws IloException
     */
    public BranchAndBound(double Omega, double[] linearCoefficients,double[] quadraticCoefficients, int numContinuous, int numVariables) throws IloException {
        this.numContinuous = numContinuous;
        this.numVariables = numVariables;
        this.upperBound = Double.POSITIVE_INFINITY;
        nodes = new TreeSet<>();
        this.Omega = Omega;
        this.linearCoefficients = linearCoefficients;
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
        return upperBound;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    public void setSolver(Main.ObjAlgorithm algorithm) {
        switch (algorithm) {
            case QUADRATIC_INTEGER:
                solver = new QuadraticSolver(Omega, linearCoefficients, cplex, x, constraints, linearPart, quadraticPart);
                break;
            case BARRIER_INTEGER:
                solver = new ConicSolver(cplex, x, constraints, linearPart, quadraticPart);
                break;
            default:
                throw new IllegalArgumentException("The algorithm confguration is not recognized: " + algorithm.name());
        }
        if (numVariables == 0) {
            numContinuous = 0;
            numVariables = x.length;
        }
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    /**
     * Processes a node. Calls the continuous solver, determines which variables
     * to branch (if any) and which children to process (if any). Updates lower
     * and upper bounds if necessary. <br>
     *
     * @return a children node to process next, or null if there are no
     * children.
     */
    private NodeInfo processNode(NodeInfo info) throws IloException {
        if (info.objectiveBound > upperBound) {
            nodes.clear();
            return null;
        }
        info = solveNode(info);

        if (info.objectiveBound > upperBound) {
            return null;
        }
        double[] sol = cplex.getValues(solver.x, numContinuous, numVariables - numContinuous);
        double mostInfeasible = 0, value;

        int infeasibleIndex = -1;
        
        for (int i = 0; i < sol.length; i++) {
            value = sol[i];
            if (Math.min(value, 1 - value) > mostInfeasible) {
                mostInfeasible = Math.min(value, 1 - value);
                infeasibleIndex = i;
            }
        }
        
        if (mostInfeasible < INTEGER_PRECISION) {
            upperBound = info.objectiveBound;
            return null;
        } else {
            NodeInfo[] infos = info.branchCopy(infeasibleIndex);
            if (sol[infeasibleIndex] < 0.5) {
                nodes.add(infos[1]);
                return infos[0];
            } else {
                nodes.add(infos[0]);
                return infos[1];
            }
        }

    }

    /**
     * Solves the continuous relaxation at the current node. <br>
     *
     * @param info Information about the current node. <br>
     * @return The information of the node after solving.
     */
    private NodeInfo solveNode(NodeInfo info) throws IloException {

        double[] lowerBounds = info.lowerBounds, upperBounds = info.upperBounds;
        updateBounds(lowerBounds, upperBounds);

        if (solver instanceof ConicSolver) {
            solver.solve();
            return new NodeInfo(0, null, null, lowerBounds, upperBounds, solver.getUpperBound());
        } else if (solver instanceof QuadraticSolver) {
            QuadraticSolver qSolver = (QuadraticSolver) solver;
            try {
                qSolver.solve(info.t, info.basisVar, info.basisRange);
            } catch (NullPointerException e) {
                qSolver.solve();
            }
            return new NodeInfo(Math.sqrt(cplex.getValue(quadraticPart)), cplex.getBasisStatuses(x), cplex.getBasisStatuses(constraints), lowerBounds, upperBounds, solver.getUpperBound());
        }
        throw new IllegalStateException("Cannot recognize the solver");

    }

    @Override
    public void solve() throws IloException {
        iterations = 1;
        double gap;
        System.out.println("Iteration \t Lower bound \t Upper bound \t Gap \t Time");
        long startingTime = System.currentTimeMillis();
        double[] lowerBounds = new double[numVariables - numContinuous], upperBounds = new double[numVariables - numContinuous];
        for (int i = 0; i < upperBounds.length; i++) {
            upperBounds[i] = 1;

        }
        NodeInfo info = new NodeInfo(1, null, null, lowerBounds, upperBounds, Double.NEGATIVE_INFINITY);

        while (!nodes.isEmpty() || info != null) {
            if (info == null) {
                info = nodes.pollFirst();
                gap = Double.isInfinite(upperBound) ? Double.POSITIVE_INFINITY : (upperBound - info.objectiveBound) / (Math.abs(info.objectiveBound) + 1e-10);
                if (gap <= GAP_TOLERANCE) {
                    break;
                }
            }
            info = processNode(info);

//            if (iterations % Integer.highestOneBit(iterations) == 0 || info == null) {
//                gap = Double.isInfinite(upperBound) ? Double.POSITIVE_INFINITY : 100 * (upperBound - getLowerBound()) / (Math.abs(upperBound) + 1e-10);
//                System.out.println(iterations + "\t" + getLowerBound() + "\t" + upperBound + "\t" + gap + "%" + "\t" + (System.currentTimeMillis() - startingTime) / 1000.0);
//            }
            if (iterations++ == 1) {
                rootRelaxation = solver.getUpperBound();
            }
            if (System.currentTimeMillis() - startingTime > 1000 * Main.TIME) {
//                System.out.println(iterations+"\t"+info.objectiveBound+"\t"+upperBound+"\t"+gap+"%");
                break;
            }
        }
        time = System.currentTimeMillis() - startingTime;
    }

    /**
     * Updates the bounds on the variables. <br>
     *
     * @param lb Lower bounds on the variables. <br>
     * @param ub Upper bounds on the variables.
     * @throws ilog.concert.IloException
     */
    private void updateBounds(double[] lb, double[] ub) throws IloException {
        for (int i = 0; i < ub.length; i++) {
            x[i + numContinuous].setLB(lb[i]);
            x[i + numContinuous].setUB(ub[i]);
        }
    }

    /**
     * Gets the lower bound of the problem. <br>
     *
     * @return lowerBound.
     */
    @Override
    public double getLowerBound() {
        return nodes.isEmpty() ? upperBound : nodes.first().objectiveBound;
    }

    @Override
    public double[] getSol() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getQPs() {
        return solver.getQPs();
    }

    @Override
    public double getRootRelaxation() {
        return rootRelaxation;
    }

    @Override
    public void setTolerance(double tolerance) {
        solver.setTolerance(tolerance);
    }

    /**
     * Node information to use in branch and bound. <br>
     *
     * @author AGomez
     */
    class NodeInfo implements Comparable<NodeInfo> {

        //--------------------------------------------------------------------------
        // Attributes
        //--------------------------------------------------------------------------
        /**
         * Id of the node.
         */
        int id;
        /**
         * Value of t.
         */
        double t;

        /**
         * Basis status in the node.
         */
        IloCplex.BasisStatus[] basisVar, basisRange;

        /**
         * Lower and upper bounds for the variables.
         */
        double[] lowerBounds, upperBounds;

        /**
         * Bound on the objective.
         */
        double objectiveBound;

        //--------------------------------------------------------------------------
        // Constructor
        //--------------------------------------------------------------------------
        /**
         * Constructor by parameters. <br>
         *
         * @param t The previous value for t. <br>
         * @param basisVar The basis status for the variables. <br>
         * @param basisRange The basis status for the constraints. <br>
         * @param lowerBounds The variable lower bounds. <br>
         * @param upperBounds The variable upper bounds. <br>
         * @param objectiveBound The bound on the objective.
         */
        public NodeInfo(double t, IloCplex.BasisStatus[] basisVar, IloCplex.BasisStatus[] basisRange, double[] lowerBounds, double[] upperBounds, double objectiveBound) {
            this.t = t;
            this.basisVar = basisVar;
            this.basisRange = basisRange;
            this.lowerBounds = lowerBounds;
            this.upperBounds = upperBounds;
            this.objectiveBound = objectiveBound;
            this.id = count++;
        }

        /**
         * Default constructor.
         */
        public NodeInfo() {
        }

        //--------------------------------------------------------------------------
        // Methods
        //--------------------------------------------------------------------------
        /**
         * Copies the node info after branching in the argument position.
         *
         * @param pos The position to branch. <br>
         * @return An array containing the two new nodes to process.
         */
        public NodeInfo[] branchCopy(int pos) {
            double[] lowerBounds1 = new double[lowerBounds.length],
                    lowerBounds2 = new double[lowerBounds.length],
                    upperBounds1 = new double[lowerBounds.length],
                    upperBounds2 = new double[lowerBounds.length];
            System.arraycopy(lowerBounds, 0, lowerBounds1, 0, lowerBounds.length);
            System.arraycopy(lowerBounds, 0, lowerBounds2, 0, lowerBounds.length);
            System.arraycopy(upperBounds, 0, upperBounds1, 0, upperBounds.length);
            System.arraycopy(upperBounds, 0, upperBounds2, 0, upperBounds.length);
            upperBounds1[pos] = 0;
            lowerBounds2[pos] = 1;
            return new NodeInfo[]{new NodeInfo(t, basisVar, basisRange, lowerBounds1, upperBounds1, objectiveBound),
                new NodeInfo(t, basisVar, basisRange, lowerBounds2, upperBounds2, objectiveBound)};

        }

        @Override
        public int compareTo(NodeInfo o) {
            if (Double.compare(objectiveBound, o.objectiveBound) != 0) {
                return Double.compare(objectiveBound, o.objectiveBound);
            }
            return Integer.compare(id, o.id);

        }

    }

}
