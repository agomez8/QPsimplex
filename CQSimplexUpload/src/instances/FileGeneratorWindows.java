/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Andres Gomez.
 */
public class FileGeneratorWindows {

    public static void main(String[] args) throws IOException {
        int table = Integer.parseInt(args[0]);
        String cplexPath = args.length>1?args[1]:null;

        //String instance = "/home/andresgomez/java/jdk1.8.0_65/bin/java "
        //      + "-Djava.library.path=/ILOG/CPLEX1262/cplex/bin/x86-64_linux "
        //    + "-cp ./dist/LagrangeanConic.jar executables.MainObj";
        String instance;
        if(cplexPath!=null)
        {
            instance = "java "
                + "-Djava.library.path=\"" + cplexPath
                + "\" -cp ./dist/LagrangeanConic.jar executables.Main";
        }
        else
        {
             instance = "java "
                + " -cp ./dist/LagrangeanConic.jar executables.Main";
        }
        String printer = "java -cp ./dist/LagrangeanConic.jar results.Printer";

//        int[] sizes = {200,400,600,800,1000,1200,1400,1600,1800,2000,2200,2400,
//            2600,2800,3000};
        int[] sizes, ranks, instances, methods;
        double[] betas, diagonals, densities, tolerances;
        long[] seeds;
        String[] abs = {"false"};

        switch (table) {
            case 1:
                sizes = new int[]{30};
                betas = new double[]{1.0};
                ranks = new int[]{200};
                diagonals = new double[]{1.0};
                densities = new double[]{0.1};
                seeds = new long[]{12345};
                instances = new int[]{1};
                methods = new int[]{-3, -4};
                tolerances = new double[]{1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7, 1e-8, 1e-9, 1e-10, 1e-11, 1e-12};
                break;
            case 2:
                sizes = new int[]{1000};
                betas = new double[]{1.0, 2.0, 3.0};
                ranks = new int[]{100, 200};
                diagonals = new double[]{1.0};
                densities = new double[]{0.1, 0.5};
                seeds = new long[]{12345, 23451, 34512, 45123, 51234};
                instances = new int[]{2};
                methods = new int[]{-2, -3, -4};
                tolerances = new double[]{0};
                break;
            case 3:
                sizes = new int[]{30};
                betas = new double[]{1.0, 2.0, 3.0};
                ranks = new int[]{100, 200};
                diagonals = new double[]{1.0};
                densities = new double[]{0.1, 0.5};
                seeds = new long[]{12345, 23451, 34512, 45123, 51234};
                instances = new int[]{1};
                methods = new int[]{-2, -3, -4};
                tolerances = new double[]{0};
                break;
            case 4:
                sizes = new int[]{200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 3200};
                betas = new double[]{2.0};
                ranks = new int[]{200};
                diagonals = new double[]{1.0};
                densities = new double[]{0.1};
                seeds = new long[]{12345, 23451, 34512, 45123, 51234};
                instances = new int[]{2};
                methods = new int[]{-2, -3, -4};
                tolerances = new double[]{0};
                break;
            case 5:
                sizes = new int[]{200};
                betas = new double[]{1.0, 2.0, 3.0};
                ranks = new int[]{100, 200};
                diagonals = new double[]{1.0};
                densities = new double[]{0.1, 0.5};
                seeds = new long[]{12345, 23451, 34512, 45123, 51234};
                instances = new int[]{2};
                methods = new int[]{0, 1, 2, 3, 4, 5};
                tolerances = new double[]{0};
                break;
            case 6:
                sizes = new int[]{30};
                betas = new double[]{1.0, 2.0, 3.0};
                ranks = new int[]{100, 200};
                diagonals = new double[]{1.0};
                densities = new double[]{0.1, 0.5};
                seeds = new long[]{12345, 23451, 34512, 45123, 51234};
                instances = new int[]{1};
                methods = new int[]{0, 1, 2, 3, 4, 5};
                tolerances = new double[]{0};
                break;
            default:
                sizes = null;
                betas = null;
                ranks = null;
                diagonals = null;
                densities = null;
                seeds = null;
                instances = null;
                methods = null;
                tolerances = null;
                break;
        }

        try (FileWriter out = new FileWriter(new File("./run.bat"))) {
            for (int size : sizes) {
                for (double beta : betas) {
                    for (int rank : ranks) {
                        for (double diagonal : diagonals) {
                            for (double density : densities) {
                                for (String ab : abs) {
                                    for (long seed : seeds) {
                                        for (int inst : instances) {
                                            for (int method : methods) {
                                                for (double tolerance : tolerances) //                                            out.write(sContinuous + " " + size + " " + beta + " "+rank+" "+diagonal+" "+density+ " " + ab+" " + seed   + " " + method + "\n");
                                                {
                                                    out.write(instance + " " + size + " " + beta + " " + rank + " " + diagonal + " " + density + " " + ab + " " + seed + " " + inst + " " + method + " " + tolerance + "\n");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            out.write(printer+"\n");
        }

    }
}
