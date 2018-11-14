/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package interfaces;

/**
 * Represents a positive definite matrix. <br>
 * @author AGomez.
 */
public interface PositiveSDefiniteMatrix {  
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    /**
     * Gets the entry of the matrix specified by the arguments. <br>
     * @param row The row. <br>
     * @param column The column. <br>
     * @return The position.
     */
    public double M(int row, int column);
    
        /**
     * Gets a separable diagonal element of the matrix. <br>
     * @param pos The row/column of the diagonal element. <br>
     * @return The position.
     */
    public double D(int pos);
    
    /**
     * Gets the entry of the square root matrix specified by the arguments. <br>
     * @param row The row. <br>
     * @param column The column. <br>
     * @return The entry.
     */
    public double sqrtM(int row, int column);

}
