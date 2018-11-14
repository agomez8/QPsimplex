/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.bind.JAXBException;

/**
 *
 * @author AGomez
 */
public class Printer {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------
    /**
     * Path for saving the files.
     */
    private static final String PATH = "./results/resultsPrinted.csv";

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------
    public static void main(String[] args) throws JAXBException, IOException {
        ResultsXml results = ResultsXml.Import(ResultsXml.PATH_RESULTS);
        try (FileWriter out = new FileWriter(new File(PATH))) {
            out.write("Size,Rank,Omega,Diagonal,Density,Positive,Instance class,Tolerance,");
           
            out.write("Algorithm,LB,UB,Time,Iterations,QPs,Root relaxation,Instances");
            
            out.write("\n");

            for (ConfigurationXml configuration : results.configurations) {

                for (AlgorithmXml alg : configuration.algorithmResults) {
                    out.write(configuration.size + "," + configuration.rank +","+ configuration.omega
                            + "," + configuration.diagonal + "," + configuration.density + "," 
                            + configuration.positive + ","+configuration.instanceClass+","+configuration.tolerance+",");
                    out.write(alg.algorithm + ",");

                    double lb = 0, ub = 0, time = 0, iterations = 0,qps=0,rootRelaxation=0;
                    for (InstanceResultXml res : alg.results) {
                        lb+=res.lowerBound;
                        ub+=res.upperBound;
                        time+=res.time;
                        iterations+=res.iterations;
                        qps+=res.QPs;
                        rootRelaxation+=res.rootRelaxation;
                    }
                    if(alg.results.size()!=0)
                    {
                        lb/=(double)alg.results.size();
                        ub/=(double)alg.results.size();
                        time/=(double)alg.results.size();
                        time/=1000.0;
                        iterations/=(double)alg.results.size();
                        qps/=(double)alg.results.size();
                        rootRelaxation/=(double)alg.results.size();
                    }
                    out.write(lb+","+ub+","+time+","+iterations+","+qps+","+rootRelaxation+","+alg.results.size()+"\n");
                }

            }
        }

    }
}
