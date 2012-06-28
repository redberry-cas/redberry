/*
 * Copyright (C) 2012 stas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.performance;

import cc.redberry.core.utils.ArraysUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.random.BitsStreamGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author stas
 */
public class StableSort {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            //burn JVM
            BitsStreamGenerator bitsStreamGenerator = new Well19937c();

            for (int i = 0; i < 1000; ++i)
                nextArray(1000, bitsStreamGenerator);

            System.out.println("Поехали!");
            BufferedWriter timMeanOut = new BufferedWriter(new FileWriter("/home/stas/Projects/stableSort/timMean.dat"));
            BufferedWriter insertionMeanOut = new BufferedWriter(new FileWriter("/home/stas/Projects/stableSort/insertionMean.dat"));

            BufferedWriter timMaxOut = new BufferedWriter(new FileWriter("/home/stas/Projects/stableSort/timMax.dat"));
            BufferedWriter insertionMaxOut = new BufferedWriter(new FileWriter("/home/stas/Projects/stableSort/insertionMax.dat"));

            BufferedWriter timSigOut = new BufferedWriter(new FileWriter("/home/stas/Projects/stableSort/timSig.dat"));
            BufferedWriter insertionSigOut = new BufferedWriter(new FileWriter("/home/stas/Projects/stableSort/insertionSig.dat"));



            DescriptiveStatistics timSort;
            DescriptiveStatistics insertionSort;

            int tryies = 200;
            int arrayLength = 0;
            for (; arrayLength < 1000; ++arrayLength) {

                int[] coSort = nextArray(arrayLength, bitsStreamGenerator);

                timSort = new DescriptiveStatistics();
                insertionSort = new DescriptiveStatistics();
                for (int i = 0; i < tryies; ++i) {
                    int[] t1 = nextArray(arrayLength, bitsStreamGenerator);
                    int[] t2 = t1.clone();

                    long start = System.currentTimeMillis();
                    ArraysUtils.timSort(t1, coSort);
                    long stop = System.currentTimeMillis();
                    timSort.addValue(stop - start);

                    start = System.currentTimeMillis();
                    ArraysUtils.insertionSort(t2, coSort);
                    stop = System.currentTimeMillis();
                    insertionSort.addValue(stop - start);
                }
                timMeanOut.write(arrayLength + "\t" + timSort.getMean() + "\n");
                insertionMeanOut.write(arrayLength + "\t" + insertionSort.getMean() + "\n");

                timMaxOut.write(arrayLength + "\t" + timSort.getMax() + "\n");
                insertionMaxOut.write(arrayLength + "\t" + insertionSort.getMax() + "\n");

                timSigOut.write(arrayLength + "\t" + timSort.getStandardDeviation() + "\n");
                insertionSigOut.write(arrayLength + "\t" + insertionSort.getStandardDeviation() + "\n");
            }
            timMeanOut.close();
            insertionMeanOut.close();
            timMaxOut.close();
            insertionMaxOut.close();
            timSigOut.close();
            insertionSigOut.close();
        } catch (IOException ex) {
            Logger.getLogger(StableSort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int[] nextArray(int length, BitsStreamGenerator bsg) {
        int[] a = new int[length];
        for (int i = 0; i < length; ++i)
            a[i] = bsg.nextInt();
        return a;
    }
}
