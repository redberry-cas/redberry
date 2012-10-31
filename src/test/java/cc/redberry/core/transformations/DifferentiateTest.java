package cc.redberry.core.transformations;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.ContextManager;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.ContractIndices.contract;
import static cc.redberry.core.transformations.Differentiate.differentiate;
import static cc.redberry.core.transformations.Expand.expand;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DifferentiateTest {

    @Test
    public void test1() {
        Tensor t = parse("x*Sin[y**3]");
        SimpleTensor var = parseSimple("y");
        TAssert.assertEquals(differentiate(t, var, 1), "3*y**2*Cos[y**3]*x");
        TAssert.assertEquals(differentiate(t, var, 2), "6*y*Cos[y**3]*x-9*y**4*Sin[y**3]*x");
        TAssert.assertEquals(differentiate(t, var, 3), "-27*y**6*Cos[y**3]*x-54*y**3*Sin[y**3]*x+6*Cos[y**3]*x");
        TAssert.assertEquals(differentiate(t, var, 4), "-180*y**2*Sin[y**3]*x-324*y**5*Cos[y**3]*x+81*y**8*Sin[y**3]*x");

    }

    @Test
    public void test2() {
        Tensor t = parse("Sin[f^mn*(x_mn+x_nm)]");
        SimpleTensor var = parseSimple("x_ij");
        t = differentiate(t, var);
        t = contract(expand(t));
        TAssert.assertEquals(t, "Cos[f^mn*(x_mn+x_nm)]*f^ij+Cos[f^mn*(x_mn+x_nm)]*f^ji");
        var = parseSimple("f_i^i");
        t = differentiate(t, var);
        t = contract(expand(t));
        TAssert.assertEquals(t, "-2*Sin[(x_{nm}+x_{mn})*f^{mn}]*x^{m}_{m}*f^{ji}-2*Sin[(x_{nm}+x_{mn})*f^{mn}]*x^{m}_{m}*f^{ij}+2*Cos[(x_{nm}+x_{mn})*f^{mn}]*g^{ij}");
    }

    @Test
    public void test3() {
        Tensor t = parse("f^ma*(x_m^n+f^n_m)*f_n^b");
        SimpleTensor var1 = parseSimple("f_a^a");
        SimpleTensor var2 = parseSimple("f_mn");
        Tensor u = differentiate(t, var1, var2);
        u = contract(expand(u));
        Tensor v = differentiate(t, var2, var1);
        v = contract(expand(v));
        TAssert.assertEquals(u, v);
        TAssert.assertEquals(u, "g^{nb}*x^{am}+g^{na}*x^{mb}+f^{bm}*g^{na}+f^{na}*g^{mb}+2*f^{mb}*g^{na}+2*f^{ma}*g^{nb}");
    }

    @Test
    public void test3a() {
        for (int i = 0; i < 100; ++i) {
            ContextManager.initializeNew();
            Tensor t = parse("f^ma*(x_m^n+f^n_m)*f_n^b");
            SimpleTensor var1 = parseSimple("f_a^a");
            SimpleTensor var2 = parseSimple("f_mn");
            Tensor u = differentiate(t, var1);
            TAssert.assertEquals(u, "(x^an+f^na)*f_n^b+f^ma*f_m^b+f^ma*(x_m^b+f^b_m)");
            Tensor v = differentiate(t, var2);
            TAssert.assertEquals(v, "g^an*(x^mv+f^vm)*f_v^b+f^na*f^mb+f^ta*(x_t^m+f^m_t)*g^bn");
            u = differentiate(u, var2);
            v = differentiate(v, var1);
            u = contract(expand(u));
            v = contract(expand(v));
            TAssert.assertEquals(u, v);
            TAssert.assertEquals(u, "g^{nb}*x^{am}+g^{na}*x^{mb}+f^{bm}*g^{na}+f^{na}*g^{mb}+2*f^{mb}*g^{na}+2*f^{ma}*g^{nb}");
        }
    }

    @Test
    public void test4() {
        addSymmetry("f_mn", IndexType.LatinLower, false, 1, 0);
        Tensor t = parse("f^ma*(x_m^n+f^n_m)*f_n^b");
        SimpleTensor var1 = parseSimple("f_a^a");
        SimpleTensor var2 = parseSimple("f_mn");
        Tensor u = differentiate(t, var1, var2);
        u = contract(expand(u));
        Tensor v = differentiate(t, var2, var1);
        v = contract(expand(v));
        TAssert.assertEquals(u, v);
        TAssert.assertEquals(u, "(1/2)*g^{bm}*x^{an}+(1/2)*g^{am}*x^{nb}+(1/2)*g^{bn}*x^{am}+(1/2)*g^{an}*x^{mb}+(3/2)*f^{na}*g^{bm}+(3/2)*f^{nb}*g^{am}+(3/2)*f^{mb}*g^{an}+(3/2)*f^{ma}*g^{bn}\n");
    }

    @Test
    public void test4a() {
        addSymmetry("f_mn", IndexType.LatinLower, false, 1, 0);
        Tensor t = parse("f^mn*f_n^b*f_mb");
        SimpleTensor var1 = parseSimple("f_cd");
        Tensor u = differentiate(t, var1);
        System.out.println(u);
        u = contract(expand(u));
        System.out.println(u);

    }

    @Test
    public void test5() {
        addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, 1, 0, 2, 3);
        addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, 2, 3, 0, 1);
        addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, 1, 0);
        Tensor t = parse("(R^\\alpha\\gamma*R_\\rho^\\beta - R_\\rho^\\gamma*R^\\alpha\\beta + R*R^\\alpha\\beta_\\rho^\\gamma)*((1/60)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\nu\\gamma}-(1/20)*R_{\\alpha\\mu}*R^\\rho_{\\gamma\\nu\\beta}+(1/120)*R_{\\alpha\\beta}*R^\\rho_{\\mu\\nu\\gamma}+(3/40)*R_{\\alpha\\gamma}*R^\\rho_{\\nu\\beta\\mu}+(1/20)*R^\\sigma_{\\gamma\\mu\\alpha}*R^\\rho_{\\nu\\sigma\\beta}+(1/120)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\beta\\nu\\sigma}-(1/40)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\sigma\\nu\\beta}+(1/40)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\sigma\\nu\\gamma}-(1/20)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\gamma\\nu\\sigma}-(1/40)*R^\\sigma_{\\mu\\beta\\nu}*R^\\rho_{\\gamma\\sigma\\alpha})");
        Expression R1 = parseExpression("R = R^\\mu_\\mu");
        Expression R2 = parseExpression("R_\\alpha\\beta = R^\\mu_\\alpha\\mu\\beta");
        t = R1.transform(t);
        t = R2.transform(t);

        SimpleTensor var1 = parseSimple("R_\\mu\\alpha\\nu^\\alpha");
        SimpleTensor var2 = parseSimple("R^\\rho_\\alpha^\\tau\\alpha");

        R1 = parseExpression("R^\\mu_\\mu = R");
        R2 = parseExpression("R^\\mu_\\alpha\\mu\\beta = R_\\alpha\\beta");
        Expression d = parseExpression("d_\\mu^\\mu = 4");
        Tensor u = differentiate(t, var1, var2);
        u = contract(expand(u));
        u = d.transform(R1.transform(R2.transform(u)));
        Tensor v = differentiate(t, var2, var1);
        v = contract(expand(v));
        v = d.transform(R1.transform(R2.transform(v)));
        TAssert.assertEquals(u, v);
    }

    @Test
    public void test5a() {
        //bad seeds
        long[] badSeeds = {1023936052033412675L,
                           -9053946475308531616L,
                           2531998578876800782L,
                           -6889844122566212877L,
                           -8268423169077194235L};
        for (long seed : badSeeds) {
            CC.resetTensorNames(seed);
            addAntiSymmetry("R_mnab", 1, 0, 2, 3);
            addSymmetry("R_mnab", 2, 3, 0, 1);
            addSymmetry("R_mn", 1, 0);
            Tensor t = parse("(R^ag*R_r^b - R_r^g*R^ab)*(R^s_{gma}*R^r_{nsb}+R^s_{amg}*R^r_{snb})");
            Expression R1 = parseExpression("R = R^m_m");
            Expression R2 = parseExpression("R_ab = R^m_amb");
            t = R1.transform(t);
            t = R2.transform(t);

            SimpleTensor var1 = parseSimple("R_mxn^x");
            SimpleTensor var2 = parseSimple("R^r_y^ty");

            R1 = parseExpression("R^m_m = R");
            R2 = parseExpression("R^m_amb = R_ab");
            Expression d = parseExpression("d_m^m = 4");
            Tensor u = differentiate(t, var1, var2);
            u = contract(expand(u));
            u = d.transform(R1.transform(R2.transform(u)));
            Tensor v = differentiate(t, var2, var1);
            v = contract(expand(v));
            v = d.transform(R1.transform(R2.transform(v)));
            u = RemoveDueToSymmetry.INSANCE.transform(u);
            v = RemoveDueToSymmetry.INSANCE.transform(v);
            TAssert.assertEquals(u, v);
        }
    }

    @Test
    public void test5b() {
        //bad seeds
        long[] badSeeds = {1023936052033412675L,
                           -9053946475308531616L,
                           2531998578876800782L,
                           -6889844122566212877L,
                           -8268423169077194235L};
        for (long seed : badSeeds) {
            CC.resetTensorNames(seed);
            addAntiSymmetry("R_mnab", 1, 0, 2, 3);
            addSymmetry("R_mnab", 2, 3, 0, 1);
            addSymmetry("R_mn", 1, 0);
            Tensor t = parse("R^ag*R_r^b*(R^s_{gma}*R^r_{nsb}+R^s_{amg}*R^r_{snb})");
            Expression R1 = parseExpression("R = R^m_m");
            Expression R2 = parseExpression("R_ab = R^m_amb");
            t = R1.transform(t);
            t = R2.transform(t);

            SimpleTensor var1 = parseSimple("R_mxn^x");
            SimpleTensor var2 = parseSimple("R^r_y^ty");

            R1 = parseExpression("R^m_m = R");
            R2 = parseExpression("R^m_amb = R_ab");
            Expression d = parseExpression("d_m^m = 4");
            Tensor v = differentiate(t, var2);
            v = differentiate(v, var1);
            v = contract(expand(v));
            v = d.transform(R1.transform(R2.transform(v)));
            Tensor u = differentiate(t, var1, var2);
            u = contract(expand(u));
            u = d.transform(R1.transform(R2.transform(u)));
            u = RemoveDueToSymmetry.INSANCE.transform(u);
            v = RemoveDueToSymmetry.INSANCE.transform(v);
            TAssert.assertEquals(u, v);
        }
    }

    @Test
    public void test6() {
        Tensor t = parse("(x*y + y*Sin[x/y]*x)*(y*x**2 + 3*x**3 + x*y**2 + y**3)");
        SimpleTensor var1 = parseSimple("x");
        SimpleTensor var2 = parseSimple("y");
        Tensor u = differentiate(t, var1, var2);
        u = expand(u);
        Tensor v = differentiate(t, var2, var1);
        v = expand(v);
        TAssert.assertEquals(u, v);
    }

    @Test
    public void test7() {
        addSymmetry("R_abcd", IndexType.LatinLower, true, 1, 0, 2, 3);
        addSymmetry("R_abcd", IndexType.LatinLower, false, 2, 3, 0, 1);
        Tensor t = parse("R_abcd");
        SimpleTensor var1 = parseSimple("R_mnpq");
        Tensor u = differentiate(t, var1);
        TAssert.assertEquals(u, "(1/8)*(-d_{a}^{m}*d_{c}^{q}*d_{d}^{p}*d_{b}^{n}-d_{a}^{n}*d_{d}^{q}*d_{c}^{p}*d_{b}^{m}-d_{a}^{q}*d_{d}^{n}*d_{b}^{p}*d_{c}^{m}+d_{c}^{q}*d_{a}^{n}*d_{d}^{p}*d_{b}^{m}+d_{c}^{n}*d_{a}^{q}*d_{b}^{p}*d_{d}^{m}+d_{b}^{q}*d_{a}^{p}*d_{d}^{n}*d_{c}^{m}+d_{a}^{m}*d_{d}^{q}*d_{c}^{p}*d_{b}^{n}-d_{b}^{q}*d_{a}^{p}*d_{c}^{n}*d_{d}^{m})");
    }

    @Test
    public void test8() {
        Tensor u = differentiate(parse("g_ab"), parseSimple("g^mn"));
        TAssert.assertEquals(u, "1/2*(g_am*g_bn+g_bm*g_an)");
    }

    @Ignore
    @Test
    public void test9() {
        //performance
        Tensor t = parse("(x**2 + y*x)**2*(Sin[x - y] + x**y)");
        for (int i = 0; i < 10000; ++i) {
            long start = System.currentTimeMillis();
            Tensor u = differentiate(t, parseSimple("x"), 123);
            Together.together(Expand.expand(u));
            System.out.println(System.currentTimeMillis() - start);
        }
    }

    @Test
    public void test10() {
        setSymmetric(parseSimple("g_abc"), IndexType.LatinLower);
        Tensor u = differentiate(parse("g_abc"), parseSimple("g^mnp"));
        TAssert.assertEquals(u, "(1/6)*(g_{am}*g_{cn}*g_{bp}+g_{ap}*g_{cm}*g_{bn}+g_{am}*g_{cp}*g_{bn}+g_{an}*g_{cm}*g_{bp}+g_{ap}*g_{bm}*g_{cn}+g_{an}*g_{bm}*g_{cp})");
    }

    @Test
    public void test12() {
        Tensor t = parse("1/(f_m*(f^m+a^m*f_i*f^i)*Cos[f_i*f^i])");
        t = differentiate(t, parseSimple("f_l"), parseSimple("f^l"));
        Expression s = parseExpression("f_m*f^m = m**2");
        t = s.transform(t);
        t = parseExpression("a_j = 0").transform(t);
        t = ContractIndices.contract(t);
        t = s.transform(t);
        t = parseExpression("d_m^m = 4").transform(t);
        t = Together.together(t);
        TAssert.assertEquals(t, "(m**4*(8*Sin[m**2]**2+4*Cos[m**2]**2)+(-8*m**2*Sin[m**2]+8*Cos[m**2])*Cos[m**2]+4*(2*m**2*Sin[m**2]-2*Cos[m**2])*Cos[m**2])*Cos[m**2]**(-3)*m**(-4)");
    }

    @Test
    public void test13() {
        addAntiSymmetry("R_abcd", 1, 0, 2, 3);
        addSymmetry("R_abcd", 2, 3, 0, 1);
        System.out.println(CC.getNameManager().getSeed());
        Tensor tensor = parse("R_mnab*R^pqnm*Sin[1/la**2*R_abcd*R^cdab]");
        SimpleTensor var1 = parseSimple("R_abmn");
        SimpleTensor var2 = parseSimple("R^pqmn");
        tensor = differentiate(tensor,true, var1, var2);
//        tensor = differentiate(tensor, true, var2);
        System.out.println(tensor);
        tensor = ContractIndices.contract(tensor);
        tensor = parseExpression("R_mnab = 1/3*(g_mb*g_na - g_ma*g_nb)*la").transform(tensor);
        tensor = expand(tensor);
        tensor = contract(tensor);
        tensor = parseExpression("d_m^m = 4").transform(tensor);
        tensor = expand(tensor);
        System.out.println(tensor);

    }
}
