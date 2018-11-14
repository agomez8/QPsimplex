/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instances;

import interfaces.PositiveSDefiniteMatrix;
import java.util.Random;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Models the matrix in the objective. <br>
 *
 * @author AGomez.
 */
class PSDObjective implements PositiveSDefiniteMatrix {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------
    /**
     * Small number to add to the diagonal (for numerical stability)
     */
    private static final double epsilon = 0;//1e-5;

    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * The rank of the problem.
     */
    protected int rank;

    /**
     * The factor matrix.
     */
    protected double[][] Sigma;

    /**
     * The incidence matrix.
     */
    protected double[][] F;

    /**
     * The matrix
     */
    protected double[][] Q;

    /**
     * The diagonal matrix.
     */
    protected double[] D;

    /**
     * The square root of the matrix.
     */
    protected double[][] sqrtMatrix;

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Constructor by parameters. <br>
     *
     * @param n The dimension of the problem. <br>
     * @param rank The rank of Sigma. <br>
     * @param d The weight of the diagonal matrix. <br>
     * @param rho The density of F. <br>
     * @param positive Whether to use only nonnegative terms. <br>
     * @param random Random number generator.
     */
    public PSDObjective(int n, int rank, double d, double rho, boolean positive, Random random) {
        this.rank = rank;

        double[][] temp = new double[rank][rank];

        // Initializes Sigma
        for (double[] temp1 : temp) {
            for (int j = 0; j < temp1.length; j++) {
                temp1[j] = random.nextDouble() * 2 - 1;
                if (positive) {
                    temp1[j] = Math.abs(temp1[j]);
                }
            }
        }
        double[][] Sigma = new double[rank][rank];
        for (int i = 0; i < Sigma.length; i++) {
            for (int j = 0; j < Sigma.length; j++) {
                Sigma[i][j] = 0;
                for (double[] t : temp) {
                    Sigma[i][j] += t[i] * t[j];
                }
            }
        }

        // Initializes D and c
        D = new double[n];
        for (int i = 0; i < D.length; i++) {
            D[i] = random.nextDouble() * d;
        }

        // Initializes F.
        double[][] F = new double[n][rank];
        for (double[] F1 : F) {
            for (int j = 0; j < F1.length; j++) {
                if (random.nextDouble() < rho) {
                    F1[j] = random.nextDouble() * 2 - 1;
                    if (positive) {
                        F1[j] = Math.abs(F1[j]);
                    }
                }
            }
        }

        Q = new double[n][n];
        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q[i].length; j++) {
                Q[i][j] = (i == j) ? D[i] + epsilon : 0;
                for (int k = 0; k < rank; k++) {
                    for (int l = 0; l < rank; l++) {
                        Q[i][j] += F[i][l] * Sigma[l][k] * F[j][k];
                    }
                }

            }

        }
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    @Override
    public double M(int i, int j) {
        return Q[i][j];
//        double resp = (i == j) ? D[i] + epsilon : 0;
//        for (int k = 0; k < rank; k++) {
//            for (int l = 0; l < rank; l++) {
//                resp += F[i][l] * Sigma[l][k] * F[j][k];
//            }
//        }
//        return resp;
    }

    @Override
    public double D(int pos) {
        return D[pos];
    }

    @Override
    public double sqrtM(int row, int column) {

        if (sqrtMatrix == null) {
            double[][] m = new double[D.length][D.length];
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m.length; j++) {
                    m[i][j] = M(i, j);

                }

            }
            RealMatrix matrix = new Array2DRowRealMatrix(m);
            EigenDecomposition decomp = new EigenDecomposition(matrix);
            sqrtMatrix = decomp.getSquareRoot().getData();
        }
        return sqrtMatrix[row][column];
    }

}
