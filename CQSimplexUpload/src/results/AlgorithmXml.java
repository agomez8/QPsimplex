/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package results;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the result of an algorithm configuration. <br>
 * @author AGomez.
 */
@XmlRootElement
public class AlgorithmXml implements Comparable<AlgorithmXml>{
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Name of the algorithm.
     */
    @XmlAttribute
    public String algorithm;

    /**
     * Sets of results.
     */
    @XmlElementWrapper(name="results")
    @XmlElement(name="result")
    public Set<InstanceResultXml> results;
    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public AlgorithmXml() {
        results=new HashSet<>();
    }

    /**
     * Constructor by parameters. <br>
     * @param algorithm The algorithm.
     */
    protected AlgorithmXml(String algorithm) {
        this.algorithm = algorithm;
        results=new HashSet<>();
    }
    
    
    
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------
    /**
     * Adds a new result for this algorithm. <br>
     * @param seed The seed of the random number generator. <br>
     * @param time The time used to solve the instance. <br>
     * @param iterations The number of iterations used. <br>
     * @param QPs The number of QPs. <br>
     * @param upperBound The upper bound. <br>
     * @param lowerBound The lower bound. <br>
     * @param rootRelaxation The root relaxation.
     */
    protected void addResult(long seed,long time, int iterations,int QPs, double upperBound,
            double lowerBound, double rootRelaxation)
    {
        if(!results.add(new InstanceResultXml(seed, time, iterations,QPs, upperBound, lowerBound,rootRelaxation)))
            System.out.println("Instance with the same seed already present in the set.") ;
    }
    
    /**
     * Checks whether a given instance exists in the file. <br>
     * @param seed The seed of the random number generator. <br>
     * @return true if it exists.
     */
    protected boolean exists(long seed)
    {
        return results.contains(new InstanceResultXml(seed, 0, 0,0, 0, 0,0));
    }
    

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @Override
    public int compareTo(AlgorithmXml o) {
        return algorithm.compareTo(o.algorithm);
    }

    
}
