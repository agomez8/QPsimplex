/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package executables;

import ilog.concert.IloException;
import instances.Instances.Instance;
import interfaces.CplexInstance;
import interfaces.CplexSolver;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import javax.xml.bind.JAXBException;
import results.ResultsXml;

/**
 *
 * @author AGomez
 */
public class Main {

    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Time limit.
     */
    public static final long TIME = 7200;

    /**
     * Enumeration that encodes the different algorithms that can be used.
     */
    public enum ObjAlgorithm {

        DEFAULT_CPLEX, LP_APPROX_EXTENDED, LP_APPROX_NAIVE, QCP_APPROX,
        QUADRATIC_INTEGER, BARRIER_INTEGER,
        QUADRATIC_CONTINUOUS_COORDINATE, QUADRATIC_CONTINUOUS_COORDINATE_LP,
        QUADRATIC_CONTINUOUS_BISECTION, QUADRATIC_CONTINUOUS_BISECTION_BASIC,
        BARRIER_CONTINUOUS,
        NULL;

        public static ObjAlgorithm toAlgorithm(int i) {
            switch (i) {
                case 0:
                    return DEFAULT_CPLEX;
                case 1:
                    return LP_APPROX_EXTENDED;
                case 2:
                    return QCP_APPROX;
                case 3:
                    return QUADRATIC_INTEGER;
                case 4:
                    return BARRIER_INTEGER;
                case 5:
                    return LP_APPROX_NAIVE;
                case -1:
                    return QUADRATIC_CONTINUOUS_COORDINATE;
                case -2:
                    return QUADRATIC_CONTINUOUS_BISECTION;
                case -3:
                    return BARRIER_CONTINUOUS;
                case -4:
                    return QUADRATIC_CONTINUOUS_COORDINATE_LP;
                case -5:
                    return QUADRATIC_CONTINUOUS_BISECTION_BASIC;
            }
            return NULL;
        }
    }

    /**
     * Main method. Solves conic programs with matrix of the form
     * F*Sigma*F'+D. <br>
     *
     * @param args the command line arguments. <br>
     * 0: Size. <br>
     * 1: Beta. <br>
     * 2: Rank of Sigma. <br>
     * 3: Diagonal coefficient. <br>
     * 4: Density of F. 5: Positive. <br>
     * 6: Seed. <br>
     * 7: Instance. <br>
     * 8: Method:<br>
     * 9: Precision (Optional). <br>
     * @throws ilog.concert.IloException
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     */
    public static void main(String[] args) throws IloException, IOException, JAXBException {
        int size = Integer.parseInt(args[0]), instanceNo = Integer.parseInt(args[7]),
                method = Integer.parseInt(args[8]), rank = Integer.parseInt(args[2]);
        boolean positive = Boolean.parseBoolean(args[5]);
        double beta = Double.parseDouble(args[1]), diagonal = Double.parseDouble(args[3]),
                density = Double.parseDouble(args[4]), tolerance=args.length>=10?Double.parseDouble(args[9]):0;
        long seed = Long.parseLong(args[6]);
        Random r = new Random(seed);
        System.out.println("Building model...");
        ObjAlgorithm algorithm = ObjAlgorithm.toAlgorithm(method);
        CplexInstance instance = null;
        Instance instanceClass = instances.Instances.Instance.toInstance(instanceNo);
//        CplexInstance instance=instances.Instances.getPortfolioInstance(size, rank, beta, diagonal, density, positive, r);

        // Checks whether the instance already exists
        ResultsXml results = ResultsXml.Import(ResultsXml.PATH_RESULTS);
        if (results == null) {
            results = new ResultsXml();
        }
        if (results.exists(size, beta, rank, diagonal, density, positive, instanceClass.name(), algorithm.name(), seed,tolerance)) {
            System.out.println("Instance already exists");
            return;
        }
        switch (instanceClass) {
            case PATH_DAG:
                instance = instances.Instances.getDagInstance(size, rank, beta, diagonal, density, positive, r);
                break;
            case UNIFORM_MATROID:
                instance = instances.Instances.getCardinalityInstance(size, (int) (0.2 * size), rank, beta, diagonal, density, positive, r);
                break;
        }
        //CplexInstance instance=instances.Instances.getDagInstance(size, rank, beta, diagonal, density, positive, r);
        CplexSolver solver = instance.build(algorithm);
        solver.setConfiguration(algorithm);
        if(tolerance>1e-13)
        {
            solver.setTolerance(tolerance);
        }
        System.out.println("Model built. Starting to solve...");
        solver.solve();

        int iterations = solver.getIterations();
        int QPs = solver.getQPs();
        double time = (double) solver.getTime() / 1000.0;
        double sol = solver.getUpperBound(), lowerBound = solver.getLowerBound();

        DecimalFormat df = new DecimalFormat("#.00000");

//      
        System.out.println(algorithm.name());
        System.out.println("Solution (ub,lb,gap,time,iterations,qps):");
        System.out.println(sol + " \t" + lowerBound + "\t" + ((sol - lowerBound) / Math.abs(lowerBound) + 1e-10) + "\t" + df.format(time) + "\t" + iterations + "\t" + QPs);

        results.add(size, beta, rank, diagonal, density, positive, instanceClass.name(),tolerance,
                algorithm.name(), seed, solver.getTime(), solver.getIterations(), solver.getQPs(),
                solver.getUpperBound(), solver.getLowerBound(), solver.getRootRelaxation());
        results.Export(ResultsXml.PATH_RESULTS);

    }

}
