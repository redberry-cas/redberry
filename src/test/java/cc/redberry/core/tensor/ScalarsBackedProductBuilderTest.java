package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ScalarsBackedProductBuilderTest {
    private static Tensor parse(String s) {
        //TODO replace tests after placing new ProductBuilder as default
        Tensor t = Tensors.parse(s);
        if (!(t instanceof Product))
            return t;
        ScalarsBackedProductBuilder productBuilder1 = new ScalarsBackedProductBuilder();
        for (Tensor f : t)
            productBuilder1.put(f);
        return productBuilder1.build();
    }

    @Test
    public void test1() {
        Tensor t = parse("(a+b)*(a+b)");
        TAssert.assertEquals(t, "(a+b)**2");
    }

    @Test
    public void test2() {
        Tensor t = parse("(a+b)**2*(a+b)");
        TAssert.assertEquals(t, "(a+b)**3");
    }

    @Test
    public void test3() {
        CC.resetTensorNames(-1394473649739479577L);
        Tensor t = parse("(-a+b)**2*(a-b)");
        TAssert.assertEquals(t, "(a-b)**3");
    }

    @Test
    public void test4() {
        Tensor t = parse("p_a*p^a*p_b*p^b");
        TAssert.assertEquals(t, "(p_a*p^a)**2");
    }

    @Test
    public void test5() {
        Tensor t = parse("a*a*(a-b)*(a-b)*p_a*p^a*p_b*p^b");
        TAssert.assertEquals(t, "a**2*(a-b)**2*(p_a*p^a)**2");
    }

    @Test
    public void test6() {
        Tensor t = parse("(-a+b)**a*(a-b)");
        TAssert.assertEquals(t, "-(b-a)**(a+1)");
    }

    @Test
    public void test7() {
        Tensor t = parse("(-a+b)*(a-b)**a");
        TAssert.assertEquals(t, "-(a-b)**(a+1)");
    }

    @Test
    public void test8() {
        Tensor t = parse("p_m*A^mnpq*p_n*A_pq");
        TAssert.assertEquals(t, "p_m*A^mnpq*p_n*A_pq");
    }

    @Test
    public void test9() {
        Tensor t = parse("p_m*A^mnpq*p_n*A_pq*p_a*A^abcd*p_b*A_cd");
        TAssert.assertEquals(t, "(p_m*A^mnpq*p_n*A_pq)**2");
    }

    @Test
    public void test10() {
        Tensor t = parse("-(a+b)");
        TAssert.assertEquals(t, "-a-b");
    }

    @Test
    public void test11() {
        Tensor t = parse("-(a_m+b_m)");
        TAssert.assertEquals(t, "-a_m-b_m");
    }


    @Test
    public void test12() {
        Tensors.addSymmetry("A_mn", IndexType.LatinLower, true, 1, 0);
        Tensors.addSymmetry("S_mn", IndexType.LatinLower, true, 1, 0);
        Tensor t = parse("A_mn*S^nm*A_ab*S^ba*A_ij*S^ij");
        TAssert.assertEquals(t, "(A_mn*S^mn)**3");
    }

    @Test
    public void test13() {
        Tensors.addSymmetry("A_mn", IndexType.LatinLower, true, 1, 0);
        Tensors.addSymmetry("S_mn", IndexType.LatinLower, true, 1, 0);
        Tensor t = parse("A_nm*S^nm*A_ab*S^ba*A_ij*S^ij");
        TAssert.assertEquals(t, "-(A_mn*S^mn)**3");
    }
}
