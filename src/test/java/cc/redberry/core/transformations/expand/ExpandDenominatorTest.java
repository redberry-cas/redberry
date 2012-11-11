package cc.redberry.core.transformations.expand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.transformations.expand.ExpandDenominator.expandDenominator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandDenominatorTest {
    @Test
    public void test1() {
        Tensor t = parse("(a+b)/(c+d)");
        TAssert.assertTrue(t == expandDenominator(t));
    }

    @Test
    public void test2() {
        Tensor a = parse("(a+b)**2/(c+d)");
        Tensor e = expandDenominator(a);
        TAssert.assertTrue(a == e);
    }

    @Test
    public void test3() {
        Tensor a = expandDenominator(parse("(a+b)**2/(c+d)**2"));
        Tensor e = parse("(a+b)**2/(c**2+2*c*d+d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test4() {
        Tensor a = expandDenominator(parse("(x+(a+b)**2)/(c+d)**2"));
        Tensor e = parse("(x+(a+b)**2)/(c**2+2*c*d+d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test5() {
        Tensor a = expandDenominator(parse("f*(x+(a+b)**2)/(c+d)**2"));
        Tensor e = parse("f*(x+(a+b)**2)/(c**2+2*c*d+d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test6() {
        Tensor a = expandDenominator(parse("f*(x+(a+b)**2)/((c+d)**2*k)"));
        Tensor e = parse("f*(x+(a+b)**2)/(k*c**2+2*k*c*d+k*d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test7() {
        Tensor a = expandDenominator(parse("f*(x+(a+b)**2)/((c+d)**2*k*i)"));
        Tensor e = parse("f*(x+(a+b)**2)/(k*c**2*i+2*k*c*d*i+k*d**2*i)");
        TAssert.assertEquals(a, e);
    }
    //todo more tests
}
