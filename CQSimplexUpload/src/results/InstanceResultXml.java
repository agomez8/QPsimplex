/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package results;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the results of a specific instance. <br>
 * @author AGomez.
 */
@XmlRootElement
public class InstanceResultXml {
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Seed corresponding to the instance.
     */
    @XmlAttribute
    public long seed;
    
    /**
     * Time used to solve the problem.
     */
    @XmlElement
    public long time;
    
    /**
     * Iterations used to solve the problem.
     */
    @XmlElement
    public int iterations;
    
    /**
     * QPs required to solve the problem. <br>
     */
    @XmlElement
    public int QPs;
    /**
     * Upper bound of the problem.
     */
    @XmlElement
    public double upperBound;
    
    /**
     * Lower bound of the problem.
     */
    @XmlElement
    public double lowerBound;
    
    @XmlElement
    public double rootRelaxation;
    


    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public InstanceResultXml() {
    }

    /**
     * Constructor by parameters. <br>
     * @param seed The seed of the random number generator. <br>
     * @param time The time used to solve the problem. <br>
     * @param iterations The number of iterations used to solve the problem. <br>
     * @param QPs The number of QPs used to solve the problem. <br>
     * @param upperBound The upper bound on the solution. <br>
     * @param lowerBound The lower bound on the solution.
     * @param rootRelaxation The root relaxation.
     */
    protected InstanceResultXml(long seed, long time, int iterations,int QPs, double upperBound,
            double lowerBound,double rootRelaxation) {
        this.seed = seed;
        this.time = time;
        this.iterations = iterations;
        this.QPs=QPs;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.rootRelaxation = rootRelaxation;
    }
    
    
    
    

    
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @Override
    public int hashCode() {
        int hash = (int)seed;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InstanceResultXml other = (InstanceResultXml) obj;
        return this.seed == other.seed;
    }
    
    
}
