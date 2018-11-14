/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package results;

import java.io.File;
import java.util.TreeSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The results. <br>
 * @author AGomez
 */
@XmlRootElement
public class ResultsXml {
    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------
    /**
     * Path for saving the files with objective conic term.
     */
    public static final String PATH_RESULTS="./results/resultsObj.xml";
    
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    /**
     * The different configurations attempted.
     */
    @XmlElementWrapper(name="configurations")
    @XmlElement(name="configuration")
    public TreeSet<ConfigurationXml> configurations;


    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public ResultsXml()
    {
        configurations=new TreeSet<>();
    }
    

    
    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------
    /**
     * Checks whether a given instance exists. <br>
     * @param size The size of the problem. <br>
     * @param omega The nonlinear coefficient. <br>
     * @param rank The rank of Sigma. <br>
     * @param diagonal The diagonal element. <br>
     * @param density The density. <br>
     * @param positive Whether the matrix is positive. <br>
     * @param instanceClass The instance class used. <br>
     * @param algorithm The algorithm used. <br>
     * @param seed The seed of the random number generator. <br>
     * @param tolerance Tolerance of the solver. <br>
     * @return true if the instance exists.
     */
    public boolean exists(int size, double omega, int rank, double diagonal, double density, boolean positive,
             String instanceClass,String algorithm,long seed, double tolerance)
    {
        ConfigurationXml original=new ConfigurationXml(size, omega, rank, diagonal, density, positive,instanceClass,tolerance);
        ConfigurationXml config=configurations.ceiling(original);
        if(config==null || config.compareTo(original)!=0)
        {
            return false;
        }
        return config.exists(algorithm, seed);
    }
    
    /**
     * Adds an element. <br>
     * @param size The size of the problem. <br>
     * @param omega The nonlinear coefficient. <br>
     * @param rank The rank of Sigma. <br>
     * @param diagonal The diagonal element. <br>
     * @param density The density. <br>
     * @param positive Whether the matrix is positive. <br>
     * @param instanceClass The instance class used. <br>
     * @param tolerance The tolerance of the solver. <br>
     * @param algorithm The algorithm used. <br>
     * @param seed The seed of the random number generator. <br>
     * @param time The time used to solve the instance. <br>
     * @param iterations The number of iterations. <br>
     * @param QPs The number of QPS. <br>
     * @param upperBound The upper bound. <br>
     * @param lowerBound The lower bound. <br>
     * @param rootRelaxation The value at the root node.
     */
    public void add(int size, double omega, int rank, double diagonal, double density, boolean positive,
             String instanceClass,double tolerance,String algorithm,long seed,long time, int iterations,int QPs,
             double upperBound, double lowerBound, double rootRelaxation)
    {
      
        ConfigurationXml original=new ConfigurationXml(size, omega, rank, diagonal, density, positive,instanceClass,tolerance);
        ConfigurationXml config=configurations.ceiling(original);
        if(config==null || config.compareTo(original)!=0)
        {
            configurations.add(config=original);
        }
        config.add(algorithm, seed, time, iterations,QPs, upperBound, lowerBound, rootRelaxation);
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    
    /**
     * Exports to a XML file. <br>
     *
     * @param path Path of the xml file. <br>
     * @throws javax.xml.bind.JAXBException
     */
    public void Export(String path) throws JAXBException {

        // create JAXB context and initializing Marshaller  
        JAXBContext jaxbContext = JAXBContext.newInstance(ResultsXml.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // for getting nice formatted output  
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            //specify the location and name of xml file to be created  

        new File(path.substring(0, path.lastIndexOf("/"))).mkdirs();
        File XMLfile = new File(path);

        // Writing to XML file  
        jaxbMarshaller.marshal(this, XMLfile);

    }

    /**
     * Imports from a XML file. <br>
     *
     * @param path Path of the XML file. <br>
     * @return The corresponding java object, or null if there file does not exist.
     * @throws javax.xml.bind.JAXBException
     */
    public static ResultsXml Import(String path) throws JAXBException {
       
            ResultsXml results;
            
            // create JAXB context and initializing Marshaller
            JAXBContext jaxbContext = JAXBContext.newInstance(ResultsXml.class);
            
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            
            // specify the location and name of xml file to be read
//            File XMLfile =  new File(ResultsXml.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getParentFile();
            boolean exists = new File(path).exists();
            if(!exists)
                return null;
            File XMLfile=new File(path);
            
            
            // this will create Java object - country from the XML file
            results = (ResultsXml) jaxbUnmarshaller.unmarshal(XMLfile);
            
            return results;
        
    }
    
    
}
