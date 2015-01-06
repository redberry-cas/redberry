/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.combinatorics.IntCombinationsGenerator;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.InconsistentIndicesException;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.utils.TensorUtils;
import org.apache.commons.math3.random.Well1024a;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProductTest {

    @Test
    public void testHashCode() {
        Assert.assertEquals(Tensors.parse("(-1)*D*S").hashCode(),
                Tensors.parse("D*S").hashCode());
    }

    @Test
    public void testHashCode1() {
        Assert.assertEquals(Tensors.parse("(-2)*D*S").hashCode(),
                Tensors.parse("2*D*S").hashCode());
    }

    @Test
    public void testHashCode3() {
        Assert.assertEquals(Tensors.parse("(-2)*4*D*S").hashCode(),
                Tensors.parse("2*2*2*D*S").hashCode());
    }

    @Test
    public void testHashCode4() {
        Assert.assertFalse(Tensors.parse("(-2)*D").hashCode()
                == Tensors.parse("D").hashCode());
    }

    @Test
    public void testHashCode5() {
        Assert.assertTrue(Tensors.parse("(-1)*a*D_mn").hashCode()
                == Tensors.parse("a*D_mn").hashCode());
    }

    @Test
    public void testHashCode2() {
        Assert.assertEquals(Tensors.parse("(-1)*D").hashCode(),
                Tensors.parse("D").hashCode());
    }

    @Test
    public void testHashCode6() {
        SimpleTensor r = parseSimple("R_abcd");
        addSymmetry(r, IndexType.LatinLower, false, 2, 3, 0, 1);
        addSymmetry(r, IndexType.LatinLower, true, 1, 0, 2, 3);

        Product t1 = (Product) parse("R^abcd*R_abcd");
        Product t2 = (Product) parse("R^abcd*R_abdc");

        Assert.assertEquals(t1.getContent().getStructureOfContractionsHashed(), t2.getContent().getStructureOfContractionsHashed());
        Assert.assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void contentTest0() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Product p = (Product) Tensors.parse("a*b*c*A^ij*A_i*A_j*b_u");
            Assert.assertTrue(TensorUtils.equalsExactly(Tensors.parse("b_u"), p.getContent().getNonScalar()));
            ProductBuilder pb = new ProductBuilder();
            for (Tensor t : p.getAllScalars())
                pb.put(t);
            pb.put(Tensors.parse("b_u"));
            Assert.assertTrue(TensorUtils.equalsExactly(pb.build(), p));
        }
    }

    @Test
    public void testRebuild() {
        Tensor t = Tensors.parse("a*b*c*A^ij*A_i*A_j*b_u");
        TensorBuilder builder = t.getBuilder();
        for (Tensor c : t)
            builder.put(c);
        Assert.assertTrue(TensorUtils.equalsExactly(t, builder.build()));
    }

    @Test
    public void testBuilder() {
        Tensor t1 = Tensors.parse("p_m*p^m");
        Tensor t2 = Tensors.parse("Power[p_m*p^m,2]");
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts() {
        Tensor t1 = Tensors.parse("p_a");
        Tensor t2 = Tensors.parse("(a_i+b_a^a_i)");
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts2() {
        Tensor t1 = Tensors.parse("(p_a+d_a)");
        Tensor t2 = Tensors.parse("(a_i+b_a^a_i)");
//        System.out.println(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts3() {
        Tensor t1 = Tensors.parse("A_a^a*A_b^b*A_c^c_m^n+A_d^e*A_e^d*A_f^f_m^n");
//        System.out.println(t1);
        Tensor t2 = Tensors.parse("A_a^a*A_b^b*A_c^c^m_n+A_d^e*A_e^d*A_f^f^m_n");
//        System.out.println(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts4() {
        Tensor t = Tensors.parse("(1/12*gamma+1/1440*Power[gamma, 3]*g^{\\delta \\zeta }*g_{\\delta \\zeta }+1/2880*Power[gamma, 3]*d^{\\delta }_{\\delta }*d^{\\zeta }_{\\zeta })*d^{\\beta }_{\\gamma }*d^{\\alpha }_{\\sigma }*P^{\\gamma }_{\\beta }*R_{\\alpha }^{\\sigma }");
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void testRenameConflicts5() {
        Tensor t = Tensors.parse("k_a*(f_m^m+g_m^m)*(k_b*d_m^m+k_b*h_m^m*(d_m^m+f_m^m)*(k_m^m+f_m^m))");
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void testGetRange1() {
        Product product = (Product) Tensors.parse("A*B");
        Tensor[] tensors = product.getRange(0, 1);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange2() {
        Product product = (Product) Tensors.parse("A*B*C_i*N_j*T_r*a*b");
        Tensor[] tensors = product.getRange(4, 6);
        int i = 4;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange3() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(0, 10);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange4() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(0, 1);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange5() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(0, 3);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange6() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(4, 8);
        int i = 4;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange7() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(1, 3);
        int i = 1;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange8() {
        Product product = (Product) Tensors.parse("2*a*b*g_mn");
        Tensor[] expected = {product.get(2)};
        Tensor[] actual = product.getRange(2, 3);
        Assert.assertTrue(expected.length == actual.length);
        for (int i = 0; i < expected.length; ++i)
            Assert.assertTrue(TensorUtils.equalsExactly(expected[i], actual[i]));
    }

    @Test
    public void testGetRange9() {
        RandomTensor random;
        int minProductSize, from, to;
        for (int count = 0; count < 1000; ++count) {
            CC.resetTensorNames();
            random = new RandomTensor(1, 100, new int[]{0, 0, 0, 0}, new int[]{2, 0, 0, 0}, true, true);
            minProductSize = 2 + random.nextInt(200);
            Tensor tensor = random.nextProduct(minProductSize);
            if (!(tensor instanceof Product))
                continue;
            Product p = (Product) tensor;
            to = random.nextInt(p.size());
            from = random.nextInt(to);
            assertArraysEquals(getRange(p, from, to), p.getRange(from, to));
        }
    }

    private Tensor[] getRange(Tensor tensor, int from, int to) {
        Tensor[] r = new Tensor[to - from];
        for (int i = from; i < to; ++i)
            r[i - from] = tensor.get(i);
        return r;
    }

    private static void assertArraysEquals(Tensor[] expected, Tensor[] actual) {
        Assert.assertTrue(expected.length == actual.length);
        for (int i = 0; i < expected.length; ++i)
            Assert.assertTrue(TensorUtils.equalsExactly(expected[i], actual[i]));
    }

    @Test
    public void testBuilder1() {
        Tensor x = Tensors.parse("Power[Power[pT,2] - s, 4]*Power[s, 4]");
        ProductBuilder builder = new ProductBuilder();
        builder.put(Complex.ONE);
        builder.put(x);
        Tensor t = builder.build();
        Tensor e = Tensors.parse("Power[Power[pT,2] - s, 4]*Power[s, 4]");
        Assert.assertTrue(TensorUtils.equalsExactly(t, e));
    }

    @Test
    public void toString1() {
        Tensor t = Tensors.parse("-a*b");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString2() {
        Tensor t = Tensors.parse("-a*b*g_mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString3() {
        Tensor t = Tensors.parse("-a*b*g_mn*g^mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString4() {
        Tensor t = Tensors.parse("a*b");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString5() {
        Tensor t = Tensors.parse("a*b*g_mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString6() {
        Tensor t = Tensors.parse("a*b*g_mn*g^mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString7() {
        Tensor t = Tensors.parse("(2-i)*a*b*g_mn*g^mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void testNonScalar1() {
        Product p1 = (Product) Tensors.parse("c1*k_{b}*k^{c}");
        Product p2 = (Product) Tensors.parse("(c0-c0*a**(-1))*k_{i}*k^{i}*k_{b}*k^{c}");
        TAssert.assertEquals(p1.getContent().getNonScalar(), p2.getContent().getNonScalar());
    }

    @Test
    public void testSet1() {
        Tensor t = parse("2*a*b*g_mn*t^mn*f_ab");
        TAssert.assertTrue(t.set(0, parse("2")) == t);
        TAssert.assertEquals(t.set(0, parse("3")), parse("3*a*b*g_mn*t^mn*f_ab"));
        TAssert.assertEquals(t.set(0, parse("0")), parse("0"));

        int i;
        for (Tensor m : t) {
            i = indexOf(m, t);
            TAssert.assertTrue(t.set(i, m) == t);
        }

        for (Tensor m : t) {
            i = indexOf(m, t);
            TAssert.assertTrue(t.set(i, parse(m.toString())) == t);
        }

        Tensor nn = Tensors.negate(t);
        for (Tensor m : t) {
            i = indexOf(m, t);
            m = Tensors.negate(m);
            TAssert.assertEquals(t.set(i, m), nn);
        }

        nn = Tensors.multiply(Complex.TWO, t);
        for (Tensor m : t) {
            i = indexOf(m, t);
            m = Tensors.multiply(Complex.TWO, m);
            TAssert.assertEquals(t.set(i, m), nn);
        }

        i = indexOf(parse("a"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("4*b*g_mn*t^mn*f_ab"));
        TAssert.assertEquals(t.set(i, Complex.ZERO), parse("0"));
        i = indexOf(parse("b"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("4*a*g_mn*t^mn*f_ab"));
        i = indexOf(parse("g_mn"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("4*a*b*t^mn*f_ab"));
        i = indexOf(parse("t^mn"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("4*a*b*g_mn*f_ab"));
        i = indexOf(parse("f_ab"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("4*a*b*g_mn*t^mn"));
    }

    @Test
    public void testSet2() {
        Tensor t = parse("a*b*g_mn*t^mn*f_ab");
        TAssert.assertEquals(t.set(0, parse("0")), parse("0"));

        int i;
        for (Tensor m : t) {
            i = indexOf(m, t);
            TAssert.assertTrue(t.set(i, m) == t);
        }

        for (Tensor m : t) {
            i = indexOf(m, t);
            TAssert.assertTrue(t.set(i, parse(m.toString())) == t);
        }

        Tensor nn = Tensors.negate(t);
        for (Tensor m : t) {
            i = indexOf(m, t);
            m = Tensors.negate(m);
            TAssert.assertEquals(t.set(i, m), nn);
        }

        nn = Tensors.multiply(Complex.TWO, t);
        for (Tensor m : t) {
            i = indexOf(m, t);
            m = Tensors.multiply(Complex.TWO, m);
            TAssert.assertEquals(t.set(i, m), nn);
        }

        i = indexOf(parse("a"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("2*b*g_mn*t^mn*f_ab"));
        TAssert.assertEquals(t.set(i, Complex.ZERO), parse("0"));
        i = indexOf(parse("b"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("2*a*g_mn*t^mn*f_ab"));
        i = indexOf(parse("g_mn"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("2*a*b*t^mn*f_ab"));
        i = indexOf(parse("t^mn"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("2*a*b*g_mn*f_ab"));
        i = indexOf(parse("f_ab"), t);
        TAssert.assertEquals(t.set(i, Complex.TWO), parse("2*a*b*g_mn*t^mn"));
    }

    @Test
    public void testSet3() {
        Tensor tensor = parse("f^{ta}*(f^{v}_{t}+x_{t}^{v})*f_{v}^{b}");
        Tensor t;
        int index = indexOf(parse("f^{v}_{t}+x_{t}^{v}"), tensor);
        t = tensor.set(index, parse("g^vm*d_t^n"));
        TAssert.assertEquals(EliminateMetricsTransformation.eliminate(t), "f^{na}*f^{mb}");
    }

    private static int indexOf(Tensor t, Tensor product) {
        for (int i = 0; i < product.size(); ++i)
            if (TensorUtils.equals(t, product.get(i)))
                return i;
        throw new RuntimeException();
    }

    @Test
    public void testRemove1() {
        Tensor r;
        Tensor[] tensors = {parse("a*b"), parse("2*a"), parse("2*g_mn"), parse("a*g_mn"), parse("f_mn*g^mn")};
        for (Tensor t : tensors)
            for (int i = 0; i < 2; ++i) {
                r = ((Product) t).remove(i);
                TAssert.assertTrue(r instanceof SimpleTensor || r instanceof Complex);
                TAssert.assertEqualsExactly(r, t.get(1 - i));
            }
    }

    @Test
    public void testRemove2() {
        Tensor r;
        Tensor[] tensors = {parse("2*a*b"), parse("2*a*g_mn"), parse("3*g_mn*f^mn")};
        for (Tensor t : tensors)
            for (int i = 0; i < 3; ++i) {
                r = ((Product) t).remove(i);
                if (i == 0)
                    TAssert.assertEqualsExactly(r, ((Product) t).getSubProductWithoutFactor());
                TAssert.assertTrue(r instanceof Product);
            }
    }

    @Test
    public void testRemove4() {
        Tensor r;
        Tensor[] tensors = {parse("2*a*b"), parse("2*a*g_mn"), parse("2*g_mn*f^mn")};
        for (Tensor t : tensors)
            for (int i = 1; i < 3; ++i) {
                r = t.set(i, parse("1/2"));
                TAssert.assertTrue(r instanceof SimpleTensor || r instanceof Complex);
                TAssert.assertEqualsExactly(r, t.get(3 - i));
            }
    }


    @Test
    public void testRemove5() {
        RandomTensor rnd = new RandomTensor(5, 10, new int[]{0, 0, 0, 0}, new int[]{3, 3, 3, 3}, false, true, new Well1024a(1L));
        Product pr = (Product) rnd.nextProduct(8);
        int size = pr.size();
        IntCombinationsGenerator gen;
        for (int r = 0; r < size; ++r) {
            gen = new IntCombinationsGenerator(size, r);
            for (int[] set : gen) {
                TAssert.assertEquals(Tensors.multiply(pr.remove(set), pr.select(set)), pr);
            }
        }
    }

    @Test
    public void testRemove6() {
        Product pr = (Product) parse("a*b*c*G^g_d*G_gz*G^dz*G_ae*J^e_b");
        int size = pr.size();
        IntCombinationsGenerator gen;
        for (int r = 0; r < size; ++r) {
            gen = new IntCombinationsGenerator(size, r);
            for (int[] set : gen)
                TAssert.assertEquals(Tensors.multiply(pr.remove(set), pr.select(set)), pr);
        }
    }

    @Test
    public void testRemove7() {
        Product pr = (Product) parse("12*a*b*c*G^g_d*G_gz*G^dz*G_ae*J^e_b");
        int size = pr.size();
        IntCombinationsGenerator gen;
        for (int r = 0; r < size; ++r) {
            gen = new IntCombinationsGenerator(size, r);
            for (int[] set : gen)
                TAssert.assertEquals(Tensors.multiply(pr.remove(set), pr.select(set)), pr);
        }
    }

    @Test(expected = InconsistentIndicesException.class)
    public void testInconsistentIndices1() {
        CC.current().getParseManager().getParser().parse("s_a*f_ac").getIndices();
    }
}
