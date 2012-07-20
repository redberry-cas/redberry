/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.transformations.substitutions;

import java.util.Arrays;
import static cc.redberry.core.TAssert.*;

import org.junit.Test;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.*;
import org.junit.Ignore;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//@Ignore
public class ProductsBijectionsPortTest {
    @Ignore
    @Test
    public void test0() {
        ProductContent from = ((Product) Tensors.parse("A_mn*K^n")).getContent();
        ProductContent target = ((Product) Tensors.parse("A_ab*M_d^g*K^a*S*A_cq*K^q")).getContent();

        Tensor[] fromData = from.getDataCopy();
        Tensor[] targetData = target.getDataCopy();
        ProductsBijectionsPort port = new ProductsBijectionsPort(from, target);
        int[] bijection;
        while ((bijection = port.take()) != null) {
            for (int i = 0; i < bijection.length; ++i)
                System.out.println(fromData[i] + "\t" + targetData[bijection[i]]);
            System.out.println();
        }
    }

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    @Ignore
    @Test
    public void test1() {
        for (int I = 0; I < 1000; ++I) {
            CC.resetTensorNames();
            Product from = (Product) Tensors.parse("A_{ed}^{afj}*A^{e}_{i}^{blm}*B_{f}^{d}_{mck}*C_{j}^{ih}*D_{h}^{k}_{l}");
            Tensors.addSymmetry("A_{abcdf}", IndexType.LatinLower, false, 1, 0, 2, 3, 4);
            Tensors.addSymmetry("A_{abcdf}", IndexType.LatinLower, false, 0, 1, 3, 4, 2);

            Tensors.addSymmetry("B_{abcdf}", IndexType.LatinLower, false, 1, 0, 2, 3, 4);
            Tensors.addSymmetry("B_{abcdf}", IndexType.LatinLower, false, 0, 1, 3, 4, 2);

            Tensors.addSymmetry("C_{abc}", IndexType.LatinLower, false, 1, 2, 0);

            Product target = (Product) Tensors.parse("A_{ed}^{afj}*A^{e}_{i}^{blm}*B_{f}^{d}_{mck}*C_{j}^{ih}*D_{h}^{k}_{l}");

            ProductsBijectionsPort port = new ProductsBijectionsPort(from.getContent(), target.getContent());
            Tensor[] fromData = from.getContent().getDataCopy();
            int[] bijection;
            while ((bijection = port.take()) != null) {
                System.out.println(Arrays.toString(bijection));
                if (Arrays.binarySearch(bijection, -1) >= 0) {
                    System.out.println(CC.getNameManager().getSeed());
                    System.exit(0);
                    FullContractionsStructure fcs = from.getContent().getFullContractionsStructure();
                    for (int i = 0; i < fcs.contractions.length; ++i)
                        for (int j = 0; j < fcs.contractions[i].length; ++j) {
                            long contraction = fcs.contractions[i][j];
                            Tensor from1 = fromData[i];
                            if (FullContractionsStructure.getToTensorIndex(contraction) == -1)
                                continue;
                            Tensor to1 = fromData[FullContractionsStructure.getToTensorIndex(contraction)];
                            int indexFrom = j;
                            System.out.println(from1 + "\t" + to1 + "\t" + indexFrom);
                        }
                }
            }
            System.out.println();
        }
    }

    @Ignore
    @Test
    public void test2() {
        //CC.getNameManager().reset();
        Product from = (Product) Tensors.parse("A_{ed}^{afj}*A^{e}_{i}^{blm}*B_{f}^{d}_{mck}*C_{j}^{ih}*D_{h}^{k}_{l}");
        Tensors.addSymmetry("A_{abcdf}", IndexType.LatinLower, false, 1, 0, 2, 3, 4);
        Tensors.addSymmetry("A_{abcdf}", IndexType.LatinLower, false, 0, 1, 3, 4, 2);

        Tensors.addSymmetry("B_{abcdf}", IndexType.LatinLower, false, 1, 0, 2, 3, 4);
        Tensors.addSymmetry("B_{abcdf}", IndexType.LatinLower, false, 0, 1, 3, 4, 2);

        Tensors.addSymmetry("C_{abc}", IndexType.LatinLower, false, 1, 2, 0);

        Product target = (Product) Tensors.parse("A_{ed}^{afj}*A^{e}_{i}^{blm}*B_{f}^{d}_{mck}*S*C_{j}^{ih}*D_{h}^{k}_{l}*K");

        ProductsBijectionsPort port = new ProductsBijectionsPort(from.getContent(), target.getContent());
        Tensor[] fromData = from.getContent().getDataCopy();
        Tensor[] targetData = target.getContent().getDataCopy();
        int[] bijection;
        while ((bijection = port.take()) != null) {
            System.out.println(Arrays.toString(bijection));
            for (int i = 0; i < bijection.length; ++i)
                System.out.println(fromData[i] + "\t" + targetData[bijection[i]]);
            System.out.println();
        }
    }

    @Test
    public void test3() {
        for (int i = 0; i < 1000; ++i) {
            CC.resetTensorNames();
            Product from = (Product) _("p_a*p^a");
            Product target = (Product) _("a*p_i*p^i");
            ProductsBijectionsPort port = new ProductsBijectionsPort(from.getContent(), target.getContent());
            int[] bijection;
            int count = 0;
            while ((bijection = port.take()) != null)
                ++count;
            if (count != 2)
                System.out.println(CC.getNameManager().getSeed());
            assertTrue(count == 2);
        }
    }

//    @Test
//    public void test31() {
//        CC.resetTensorNames(5281301740134105709L);
//        Product from = (Product) _("p_a*p^a");
//        Product target = (Product) _("a*p_i*p^i");
//        ContractionsGraphDrawer.drawToPngFile(from,"/home/stas/Projects/Durty/", "from");
//        ContractionsGraphDrawer.drawToPngFile(target,"/home/stas/Projects/Durty/", "target");
//        ProductsBijectionsPort port = new ProductsBijectionsPort(from.getContent(), target.getContent());
//        int[] bijection;
//        int count = 0;
//        while ((bijection = port.take()) != null){
//            System.out.println(Arrays.toString(bijection));
//            ++count;
//        }
//        assertTrue(count == 2);
//    }
//
//    @Ignore
//    @Test
//    public void testRandom0() {
//        int goodCounter = 0, badCounter = 0;
//        //CC.getNameManager().reset(1329073571830L);
//        CC.getNameManager().reset(1329075353860L);
//        System.out.println("NM Seed = " + CC.getNameManager().getSeed());
//        RandomProduct rp = new RandomProduct(8, 4,
//                new int[]{5, 0, 0, 0}, //Min Free Indices
//                new int[]{10, 0, 0, 0}, //Indices count
//                new int[]{7, 0, 0, 0}, //Max INdex Per Tensor
//                20, 2, false);
//        //rp.reset(-5989041161274082800L);
//        rp.reset(-5616896352196250341L);
//        System.out.println("Random Seed = " + rp.getSeed());
//
//        ArrayList<int[]> bijections = new ArrayList<>();
//        Product product;
//        String file;
//        for (int n = 0; n < 100; ++n) {
//            Product from = rp.next(23);
//            Product target = from.clone();
//
//            ContractionsGraphDrawer.drawToPngFile(from, file = "/home/stas/Projects/Durty/bad", Integer.toString(badCounter++));
//
//            if (n == 72)
//                System.out.println("GG");
//
//            ProductsBijectionsPort port = new ProductsBijectionsPort(from.getContent(), target.getContent());
//            Tensor[] fromData = from.getContent().getDataCopy();
//            Tensor[] targetData = target.getContent().getDataCopy();
//
//            int[] bijection;
//
//            bijections.clear();
//            boolean good = false;
//            int trys = 0;
//            OUTER:
//            while (trys++ < 200 && (bijection = port.take()) != null) {
//                bijections.add(bijection);
//                for (int i = 0; i < bijection.length; ++i)
//                    if (bijection[i] != i)
//                        continue OUTER;
//                good = true;
//                break;
//            }
//
//            System.out.println(n + "   -   " + bijections.size());
//
//            if (!good)
//                for (int[] b : bijections)
//                    System.out.println(Arrays.toString(b));
//
//            assertTrue(true);
//
//            if (!good) {
//                //ContractionsGraphDrawer.drawToPngFile(from, "/Volumes/Data/TMP/bad" + (badCounter++) + ".png");
//                product = from;
//                break;
//            } else if (bijections.size() < 100)
//                new File(file).delete();
//
//            //} else {
//            //ContractionsGraphDrawer.drawToPngFile(from, "/Volumes/Data/TMP/good" + (goodCounter++) + ".png");
//            //}
//            //int[] bijection;
//            //while ((bijection = port.take()) != null) {
//            //    System.out.println(Arrays.toString(bijection));
//            //for (int i = 0; i < bijection.length; ++i)
//            //    System.out.println(fromData[i] + "\t" + targetData[bijection[i]]);
//            //    System.out.println();
//            //}
//        }
//        //System.out.println(product);
//    }
//
//    @Ignore
//    @Test
//    public void testRandomSymmetries() {
//        int badCounter = 0;
//        CC.getNameManager().reset();
//        RandomProduct rp = new RandomProduct(80, 4,
//                new int[]{5, 0, 0, 0}, //Min Free Indices
//                new int[]{10, 0, 0, 0}, //Indices count
//                new int[]{7, 0, 0, 0}, //Max INdex Per Tensor
//                20, 2, true);
//        System.out.println("Random Seed = " + rp.getSeed());
//        int count = 0;
//        while (++count < 5000) {
//            CC.resetTensorNames();
//            Product from = rp.next(23);
//            Product target = from.clone();
//
//            ProductsBijectionsPort port = new ProductsBijectionsPort(from.getContent(), target.getContent());
//
//            int[] bijection;
//            boolean good = false;
//            int trys = 0;
//            OUTER:
//            while (trys++ < 4000 && (bijection = port.take()) != null) {
//                for (int i = 0; i < bijection.length; ++i)
//                    if (bijection[i] != i)
//                        continue OUTER;
//                good = true;
//                break;
//            }
//            System.out.println(trys);
//            if (!good)
//                ContractionsGraphDrawer.drawToPngFile(from, "/home/stas/Projects/Durty/", Integer.toString(badCounter++));
//        }
//        System.out.println(badCounter);
//    }

    public static boolean debugFlag = false;
}
