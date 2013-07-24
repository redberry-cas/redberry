/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.IntCombinationsGenerator;
import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static cc.redberry.core.tensor.ApplyIndexMapping.applyIndexMappingAutomatically;
import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ApplyIndexMappingTest {


    @Test
    public void testSimple1() {
        Tensor from = parse("A_m^n");
        Tensor to = parse("A_a^a");
        Mapping imb = IndexMappings.getFirst(from, to);

        Tensor target = parse("A_m^n");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, new int[0]);

        Tensor standard = parse("A_a^a");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testSimple2() {
        Tensor from = parse("g_ab");
        Tensor to = parse("g^mn");
        Mapping imb = IndexMappings.getFirst(from, to);

        Tensor target = parse("g_ab");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, new int[0]);
        System.out.println(target);
        Tensor standard = parse("g^mn");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testSimple3() {
        Tensor from = parse("A_mnpqrs");
        Tensor to = parse("A_a^a_b^b_c^c");
        Mapping imb = IndexMappings.getFirst(from, to);

        Tensor target = parse("A_mnpqrs");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, new int[0]);

        Tensor standard = parse("A_a^a_b^b_c^c");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testSum1() {
        Tensor from = parse("A_mn");
        Tensor to = parse("A_cd");
        Mapping imb = IndexMappings.getFirst(from, to);


        Tensor target = parse("C_mn+D_nm");
        target = ApplyIndexMapping.applyIndexMapping(target, imb);

        Tensor standard = parse("C_cd+D_dc");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testSum2() {
        Tensor from = parse("A_mn");
        Tensor to = parse("A_cd");
        Mapping imb = IndexMappings.getFirst(from, to);


        Tensor target = parse("(C_ms+D_ms)*F^s_n");
        target = ApplyIndexMapping.applyIndexMapping(target, imb);

        Tensor standard = parse("(C_cs+D_cs)*F^s_d");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testSum3() {
        Tensor from = parse("A_mn");
        Tensor to = parse("A_cd");
        Mapping imb = IndexMappings.getFirst(from, to);
        Tensor target = parse("(C_md+D_md)*F^d_n");
        target = ApplyIndexMapping.applyIndexMapping(target, imb);
        Tensor standard = parse("(C_{ca}+D_{ca})*F^{a}_{d}");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testSum4() {
        Tensor from = parse("A_abmn");
        Tensor to = parse("A_acdx");
        Mapping imb = IndexMappings.getFirst(from, to);
        Tensor target = parse("(C_mdb+D_mdb)*F^d_na");
        target = ApplyIndexMapping.applyIndexMapping(target, imb);
        Tensor standard = parse("(C_dbc+D_dbc)*F^b_xa");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testSum5() {
        //todo fix after Dima review of new ApplyIndexMappingConcept
        Tensor from = parse("A_abcd");
        Tensor to = parse("A_wxyz");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_mn").getIndices().getAllIndices().copy();
        Tensor target = parse("(A_mn*B^mn_ab+C_ab)*C^dc");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Tensor standard = parse("(A_{ab}*B^{ab}_{wx}+C_{wx})*C^{zy}");
        Assert.assertTrue(TensorUtils.equals(target, standard));
        TAssert.assertIndicesConsistency(target);
    }

    @Test
    public void testSum6() {
        Tensor from = parse("A_abcd");
        Tensor to = parse("A^wxyz");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_an").getIndices().getAllIndices().copy();
        Tensor target = parse("A_{ab jxk}*B^{jxk}_dc+A_{bd ujxk}*B^{ujxk}_ac");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Tensor standard = parse("A^{xz}_{ujbk}*B^{ujbkwy}+A^{wx}_{jbk}*B^{jbkzy}");
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Test
    public void testProduct1() {
        Tensor from = parse("A^ab_cd");
        Tensor to = parse("A^wx_yz");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_mn").getIndices().getAllIndices().copy();

        Tensor target = parse("A_{a txk}*B^{d txk}_w*A^w_{sqz}*B^{bc sqz}");
        Tensor standard = parse("A_{w tak}*B^{z tak}_u*A^u_{sqd}*B^{xy sqd}");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Test
    public void testProduct2() {
        Tensor from = parse("A_abcd");
        Tensor to = parse("A_wxyz");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_ab").getIndices().getAllIndices().copy();
        Tensor target = parse("A_{a qw}^{q d}*B_{er}^{c ty}*D_{b ty}^{er ui}*E_{ui}*a*J^{w}*b");

        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Tensor standard = parse("A_{w qv}^{q z}*B_{er}^{y tl}*D_{x tl}^{er ui}*E_{ui}*a*J^{v}*b");
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Ignore
    @Test
    public void testProduct3() {
        Tensor from = parse("A_abcd");
        Tensor to = parse("A_wxyz");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_abcd").getIndices().getAllIndices().copy();
        Tensor target = parse("A_{a qw}^{qd}*B_{er}^{c ty}*D_{b ty}^{er ui}*E_{ui}*a*J^{w}*b");

        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Tensor standard = parse("A_{wfexhk}*B^{xhk}_{z}*A^{z}_{sql}*B^{sql}_{yg}");
//        System.out.println(target);
//        //Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testFraction1() {
        Tensor from = parse("A_ab");
        Tensor to = parse("A_xy");
        Mapping imb = IndexMappings.getFirst(from, to);

        Tensor target = parse("(a*b*g_ab)/(A_x*A^x+B_y*B^y)");
        target = ApplyIndexMapping.applyIndexMapping(target, imb);
        Tensor standard = parse("(a*b*g_xy)/(A_{a}*A^{a}+B_{b}*B^{b})");
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Test
    public void testFraction2() {
        Tensor from = parse("A_ab");
        Tensor to = parse("A_xy");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_wxyzabcdmn").getIndices().getAllIndices().copy();

        Tensor target = parse("(a*b*g_xm*g^abxm)/(A_xwz*A^xwz+B_y*B^y/(k_max*H^amx))");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Tensor standard = parse("(a*b*g_fe*g^xyfe)/(A_xwz*A^xwz+B_y*B^y/(k_max*H^amx))");
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Test
    public void testField1() {
        Tensor from = parse("A_ab");
        Tensor to = parse("A_xy");
        Mapping imb = IndexMappings.getFirst(from, to);

        Tensor target = parse("F_ab[g_qw]");
        target = ApplyIndexMapping.applyIndexMapping(target, imb);
        Tensor standard = parse("F_xy[g_qx]");
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Test
    public void testField2() {
        Tensor from = parse("A_ab");
        Tensor to = parse("A_xy");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_wxyzabcdmn").getIndices().getAllIndices().copy();

        Tensor target = parse("F_ab[g_ab*f[h_wxyzabcdmn]]");
        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Tensor standard = parse("F_xy[g_ab*f[h_wxyzabcdmn]]");
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Test
    public void testEmpty() {
        Tensor tensor = parse("A");
        Mapping mapping = new Mapping(new int[0], new int[0], true);
        TAssert.assertEquals(ApplyIndexMapping.applyIndexMapping(tensor, mapping), "-A");
    }

    @Test
    public void cloneSensitiveTest1() {
        Tensor from = parse("A_ab");
        Tensor to = parse("A_xy");
        Mapping imb = IndexMappings.getFirst(from, to);
        int[] usedIndices = parse("B_md").getIndices().getAllIndices().copy();

        Tensor target = parse("A_mb^am+B_bd^kd*C_k^a");

        target = ApplyIndexMapping.applyIndexMapping(target, imb, usedIndices);
        Tensor standard = parse("A_{sy}^{xs}+B_yd^kd*C_k^x");
        Assert.assertTrue(TensorUtils.equals(target, standard));
    }

    @Test
    public void emptyMapping1() {
        Tensor target = parse("A_mn*(B_m^m+C)*U^mn");
        target = ApplyIndexMapping.applyIndexMapping(target, Mapping.EMPTY);
        Tensor standard = parse("A_mn*(B_m^m+C)*U^mn");
        Assert.assertTrue(TensorUtils.equalsExactly(target, standard));
    }

    @Test
    public void testManyMappings() {
        Tensor riman1 = parse("g_ax*(d_c*G^x_bd-d_d*G^x_bc+G^x_yc*G^y_bd-G^x_yd*G^y_bc)");
        //                        g_px*(d_r*G^x_qs-d_s*G^x_qr+G^x_yr*G^y_qs-G^x_ys*G^y_qr)
        //                        g_px*(d_s*G^x_qr-d_r*G^x_qs+G^x_ys*G^y_qr-G^x_yr*G^y_qs)

        Tensor riman2 = parse("g_px*(d_r*G^x_qs-d_s*G^x_qr+G^x_yr*G^y_qs-G^x_ys*G^y_qr)");

        addSymmetry("G^a_bc", IndexType.LatinLower, false, 0, 2, 1);
        addSymmetry("g_ab", IndexType.LatinLower, false, 1, 0);

        Set<Mapping> buffers = IndexMappings.getAllMappings(riman1, riman2);
        Tensor[] targets = new Tensor[buffers.size()];
        int i = 0;
        for (Mapping buffer : buffers)
            targets[i++] = ApplyIndexMapping.applyIndexMapping(riman1, buffer);

        Tensor[] standarts = new Tensor[buffers.size()];
        standarts[0] = parse("g_px*(d_r*G^x_qs-d_s*G^x_qr+G^x_yr*G^y_qs-G^x_ys*G^y_qr)");
        standarts[1] = parse("g_px*(d_s*G^x_qr-d_r*G^x_qs+G^x_ys*G^y_qr-G^x_yr*G^y_qs)");
        Arrays.sort(targets);
        Arrays.sort(standarts);
        for (i = i - 1; i >= 0; --i)
            Assert.assertTrue(IndexMappings.createPort(targets[i], standarts[i]).take() != null);

    }

    @Test
    public void testRecursive1() {
        Tensor t1 = parse("A_mn");
        Tensor t2 = parse("A_ab");

        int[] usedStates = parse("A_pqrs").getIndices().getAllIndices().copy();

        //target = R_abcd*R_pqrs
        Tensor target = parse("B_mn+D_nm");
        addSymmetry("A_bc", IndexType.LatinLower, false, 1, 0);

        Set<Mapping> buffers = IndexMappings.getAllMappings(t1, t2);
        Tensor[] targets = new Tensor[buffers.size()];
        int i = -1;
        for (Mapping buffer : buffers)
            targets[++i] = ApplyIndexMapping.applyIndexMapping(target, buffer, usedStates);
        Tensor[] standarts = new Tensor[buffers.size()];
        standarts[0] = parse("B_ab+D_ba");
        standarts[1] = parse("B_ba+D_ab");
        for (; i >= 0; --i)
            Assert.assertTrue(IndexMappings.createPort(targets[i], standarts[i]).take() != null);
    }

    /*
    * Performance tests
    */

    @Test
    @Ignore
    public void performanceRenameDummy() {

        Tensor init, temp;
        init = parse("(a + b_m*(k^m + p^m + b_a*(t^am + v^abc*(t^m_bc + v^m_bc))))" +
                "*(a + b_n*(k^n + p^n + b_d*(t^dn + v^def*(t^n_ef + v^n_ef))))" +
                "*(a*(f_qwertyuioplkjhgfdsazxcvbnm^qwertyuioplkjhgfdsazxcvbnm)**2344 + b_i*(k^i + p^i + b_gxy*(t^gixy + v^gqr*(t^xyi_qr + v_qr*(f^xyi*(t_qwtu*o^qwtu)**2 + d^xyi*(t_qwtu*o^qwtu)**2 + x*((t_qwthu*o^qhwtu)**2*d^xyi + k^xyi*(t_qwtus*o^sqwtu)**2))))))");


        long start, time = 0;
        int indicesSize = TensorUtils.getAllIndicesNamesT(init).size();

        //cold start
        IntArrayList forbidden = new IntArrayList();
        int[] forbiddenArray;
        int count = 0;
        temp = init;
        for (int i = 0; i < 1000; ++i) {
            for (int j = 0; j < indicesSize; ++j)
                forbidden.add(count++);
            forbiddenArray = forbidden.toArray();
            start = System.currentTimeMillis();
            temp = ApplyIndexMapping.renameDummy(temp, forbiddenArray);
            time += (System.currentTimeMillis() - start);
        }
        System.out.println("1000 invocations on cold JVM: " + time + " ms");
        Assert.assertTrue(time < 1800);

        //warm up JVM
        burnJVMonRenameDummy();

        temp = init;
        forbidden = new IntArrayList();
        count = 0;
        time = 0;
        for (int i = 0; i < 1000; ++i) {
            for (int j = 0; j < indicesSize; ++j)
                forbidden.add(count++);
            forbiddenArray = forbidden.toArray();
            start = System.currentTimeMillis();
            temp = ApplyIndexMapping.renameDummy(temp, forbiddenArray);
            time += (System.currentTimeMillis() - start);
        }
        System.out.println("1000 invocations on hot JVM: " + time + " ms");
        Assert.assertTrue(time < 900);
    }

    private static void burnJVMonRenameDummy() {
        Tensor t;
        t = parse("(a + b_m*(k^m + p^m + b_a*(t^am + v^abc*(t^m_bc + v^m_bc))))" +
                "*(a + b_n*(k^n + p^n + b_d*(t^dn + v^def*(t^n_ef + v^n_ef))))" +
                "*(a*(f_qwertyuioplkjhgfdsazxcvbnm^qwertyuioplkjhgfdsazxcvbnm)**2344 + b_i*(k^i + p^i + b_gxy*(t^gixy + v^gqr*(t^xyi_qr + v_qr*(f^xyi*(t_qwtu*o^qwtu)**2 + d^xyi*(t_qwtu*o^qwtu)**2 + x*((t_qwthu*o^qhwtu)**2*d^xyi + k^xyi*(t_qwtus*o^sqwtu)**2))))))");
        TIntSet fobidden = TensorUtils.getAllIndicesNamesT(t);
        for (int i = 0; i < 1000; ++i) {
            t = ApplyIndexMapping.renameDummy(t, fobidden.toArray());
            fobidden.addAll(TensorUtils.getAllIndicesNamesT(t));
        }
    }

    @Test
    public void testRenameFieldArgs() {
        Tensor t;
        TIntHashSet forbidden;

//        t = parseSimple("f[x_a, x_a, x_c]");
//        forbidden = new TIntHashSet();
//        forbidden.add(0);
//        System.out.println(ApplyIndexMapping.renameIndicesOfFieldsArguments(t, forbidden));
//
//        forbidden = new TIntHashSet();
//        forbidden.add(0);
//        t = parseSimple("f[x_b, x_a, x_c]");
//        System.out.println(ApplyIndexMapping.renameIndicesOfFieldsArguments(t, forbidden));
//
//        forbidden = new TIntHashSet();
//        forbidden.add(0);
//        t = parse("f[x_b, x_a, x_c]*f[x_b*x^b, x_c*x^c, x_c]");
//        System.out.println(ApplyIndexMapping.renameIndicesOfFieldsArguments(t, forbidden));

        forbidden = new TIntHashSet();
        forbidden.add(0);
        forbidden.add(1);
        t = parse("f[x_ab]");
        System.out.println(ApplyIndexMapping.renameIndicesOfFieldsArguments(t, forbidden));
    }

    @Test
    public void testApplyAutomatic1() {
        int[] from = {0, 1, 2, 3, 4},
                to = {12, 13, 14, 15, 16};
        Tensor t = parse("T_abcde");

        automaticTestApplyAutomatic(t, from, to, from.clone(), to.clone());
    }

    @Test
    public void testApplyAutomatic2() {
        int[] from = {0, 1, 2, 3, 4 /**/, 12, 13, 14},
                to = {12, 13, 14, 15 /**/, 16, 0, 1, 2};

        int[] ffrom = {0, 1, 2, 3, 4},
                fto = {12, 13, 14, 15, 16};
        Tensor t = parse("T_abcde");

        automaticTestApplyAutomatic(t, from, to, ffrom, fto);
    }


    @Test
    public void testApplyAutomatic2a() {
        int[] from = {0, 1, 2, 4, 5, 3, 6, 7, 8, 9},
                to = {12, 13, 14, 16, 0, 15, 1, 2, 3, 4};

        int[] ffrom = {0, 1, 2, 3, 4},
                fto = {12, 13, 14, 15, 16};
        Tensor t = parse("T_abcde");

        TAssert.assertEquals(
                applyIndexMappingAutomatically(t, new Mapping(from, to)),
                ApplyIndexMapping.applyIndexMapping(t, new Mapping(ffrom, fto), new int[0]));
    }

    @Test
    public void testApplyAutomatic3() {
        int[] from = {0, 1, 2, 3,  /**/  4, 13, 5},
                to = {12, 13, 14, 15, /**/   7, 0, 5};


        int[] ffrom = {0, 1, 2, 3}, //_abcde
                fto = {12, 13, 14, 15}; //mnopqr
        //   _ab                 _c
        Tensor t = parse("(T_m^m_ab + C_n^n_ab)*F_c*(T_e^e + B_o^o*F_fg^fg)*K_d");

        automaticTestApplyAutomatic(t, from, to, ffrom, fto);
    }

    private static void automaticTestApplyAutomatic(Tensor t, int[] from, int[] to, int[] freeFrom, int[] freeTo) {
        int[] _from, _to, __to;
        int position;
        Iterable<int[]> combinations, gen;
        ArraysUtils.quickSort(freeFrom, freeTo);
        for (int i = 0; i < from.length; ++i) {
            combinations = new IntCombinationsGenerator(from.length, i);
            for (int[] combination : combinations) {
                _from = ArraysUtils.remove(from, combination);
                _to = ArraysUtils.remove(to, combination);
                __to = freeTo.clone();
                for (int ii : combination) {
                    position = Arrays.binarySearch(freeFrom, from[ii]);
                    if (position >= 0)
                        __to[position] = freeFrom[position];
                }

                gen = new IntPermutationsGenerator(_from.length);
                for (int[] p : gen) {
//                    System.out.println();
//                    System.out.println(Arrays.toString(p));
//                    System.out.println(Arrays.toString(Combinatorics.reorder(_from, p)));
//                    System.out.println(Arrays.toString(Combinatorics.reorder(_to, p)));
//                    System.out.println(Arrays.toString(freeFrom));
//                    System.out.println(Arrays.toString(__to));
                    TAssert.assertEquals(
                            applyIndexMappingAutomatically(t,
                                    new Mapping(Combinatorics.reorder(_from, p), Combinatorics.reorder(_to, p))),
                            ApplyIndexMapping.applyIndexMapping(
                                    t, new Mapping(freeFrom, __to), new int[0]));
                }
            }
        }
    }
}
