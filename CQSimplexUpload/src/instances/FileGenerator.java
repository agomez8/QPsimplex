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
public class FileGenerator {

    public static void main(String[] args) throws IOException {

        String instance = "java -cp ./dist/LagrangeanConic.jar executables.MainObj";

//        int[] sizes={5,10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200};
//        int[] sizes={10,30,50};
        int[] sizes = {20,30,40};
        double[] betas = {1, 2, 3};
        int[] ranks = {100, 200};
        double[] diagonals = {1.0};
        double[] densities = {0.1, 0.5};
        String[] abs = {"false"};
        long[] seeds = {12345, 23451, 34512,45123,51234};
        int[] instances = {1};
//        int[] methods = {-1, -2,-3, 0, 1, 2, 3, 4};
        int[] methods = {-1};
//        int[] methods = { 0, 1, 3, 4};
//        int[] methods = {-1,-2,-3};
        try (FileWriter out = new FileWriter(new File("./runsBB.bat"))) {
            for (int size : sizes) {
                for (double beta : betas) {
                    for (int rank : ranks) {
                        for (double diagonal : diagonals) {
                            for (double density : densities) {
                                for (String ab : abs) {
                                    for (long seed : seeds) {
                                        for (int inst : instances) {
                                            for (int method : methods) {
//                                            out.write(sContinuous + " " + size + " " + beta + " "+rank+" "+diagonal+" "+density+ " " + ab+" " + seed   + " " + method + "\n");
                                                out.write(instance + " " + size + " " + beta + " " + rank + " " + diagonal + " " + density + " " + ab + " " + seed + " "+inst+" " + method + "\n");
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
    }
}
