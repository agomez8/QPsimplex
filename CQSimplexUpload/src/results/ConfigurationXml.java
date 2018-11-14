/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package results;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The description of an instance. <br>
 * The objective is of the form c'x +sqrt{x' (FSimga F' x+dD)x}. <br>
 * @author AGomez.
 */
@XmlRootElement
public class ConfigurationXml implements Comparable<ConfigurationXml>{
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * Size of the instance.
     */
    @XmlAttribute
    public  int size;
    
    /**
     * Coefficient of the nonlinear term.
     */
    @XmlAttribute
    public double omega;
    
    /**
     * Rank of the matrix Sigma.
     */
    @XmlAttribute
    public int rank;
    
    /**
     * Weight of the diagonal term.
     */
    @XmlAttribute
    public double diagonal;
    
    /**
     * Density of F.
     */
    @XmlAttribute
    public double density;
    
    /**
     * Whether the matrix has only positive terms.
     */
    @XmlAttribute
    public boolean positive;
    
    /**
     * Instance class.
     */
    @XmlAttribute
    public String instanceClass;
    
    /**
     * Tightness of the constraint.
     */
    @XmlAttribute
    public double tightness;
    
    /**
     * Tolerance of the solver.
     */
    @XmlAttribute
    public double tolerance;
    
    /**
     * Results of each algorithm.
     */
    @XmlElementWrapper(name="algorithms")
    @XmlElement(name="algorithm")
    public TreeSet<AlgorithmXml> algorithmResults;
    
    
    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public ConfigurationXml() {
        algorithmResults=new TreeSet<>();
    }

    /**
     * Constructor by parameters. <br>
     * @param size The size of the instance. <br>
     * @param omega The nonlinear coefficient. <br>
     * @param rank The rank of the positive definite matrix. <br> 
     * @param diagonal The diagonal weight. <br>
     * @param density The density of F. <br>
     * @param positive Whether the matrix has only positive entries. <br>
     * @param instanceClass The instance class. <br>
     * @param tolerance Tolerance of the solver. <br>
     */
    protected ConfigurationXml(int size, double omega, int rank, double diagonal,
            double density, boolean positive, String instanceClass,double tolerance) {
        this.size = size;
        this.omega = omega;
        this.rank = rank;
        this.diagonal = diagonal;
        this.density = density;
        this.positive = positive;
        this.instanceClass=instanceClass;
        this.tolerance = tolerance;
        algorithmResults=new TreeSet<>();
    }
        
    
    
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------
    /**
     * Adds a new result. <br>
     * @param algorithm The algorithm used. <br>
     * @param seed The seed. <br>
     * @param time The time. <br>
     * @param iterations The number of iterations. <br>
     * @param QPs The QPs solved.
     * @param upperBound The upper bound. <br>
     * @param lowerBound The lower bound. <br>
     * @param rootRelaxation The value of the root relaxation. 
     */
    protected void add(String algorithm,long seed,long time, int iterations,int QPs,
            double upperBound, double lowerBound, double rootRelaxation)
    {
        AlgorithmXml original=new AlgorithmXml(algorithm);
        AlgorithmXml algo=algorithmResults.ceiling(original);
        if(algo==null || algo.compareTo(original)!=0)
        {
            algorithmResults.add(algo=original);
        }
        algo.addResult(seed, time, iterations,QPs, upperBound, lowerBound, rootRelaxation);
        
        
      
    }
    
    /**
     * Checks whether a give instance already exists in the file. <br>
     * @param algorithm The algorithm used. <br>
     * @param seed The seed. <br>
     * @return true if the instance exists.
     */
    protected boolean exists(String algorithm,long seed)
    {
        AlgorithmXml original=new AlgorithmXml(algorithm);
        AlgorithmXml algo=algorithmResults.ceiling(original);
        if(algo==null || algo.compareTo(original)!=0)
        {
            return false;
        }
        return algo.exists(seed);
    }
    


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @Override
    public int compareTo(ConfigurationXml o) {
        int c;
        if((c=Integer.compare(size, o.size))!=0)
            return c;
        else if ((c=Integer.compare(rank, o.rank))!=0)
            return c;
        else if ((c=Double.compare(diagonal, o.diagonal))!=0)
            return c;
        else if ((c=Double.compare(density, o.density))!=0)
            return c;
        else if ((c=Double.compare(omega, o.omega))!=0)
            return c;
        else if ((c=Boolean.compare(positive, o.positive))!=0)
            return c;
        else if((c=instanceClass.compareTo(o.instanceClass))!=0)
            return c;
        else if ((c=Double.compare(tolerance, o.tolerance))!=0)
            return c;
        else
            return Double.compare(tightness, o.tightness);  
    }
    
    

   
}
