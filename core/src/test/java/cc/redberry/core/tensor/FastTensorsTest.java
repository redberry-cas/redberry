package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.number.Complex;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FastTensorsTest {
    @Test
    public void test1() {
        Tensor t = parse("(1+b)/a - 1/a - b/a");
        Tensor actual = FastTensors.multiplySumElementsOnFactor((Sum) t, parse("a"));
        Tensor expected = Complex.ZERO;
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test2() {
        Tensor t = parse("I*(p2*e5+1)+(-I)*p2*e5-I");
        Tensor actual = FastTensors.multiplySumElementsOnFactor((Sum) t, Complex.IMAGINARY_UNIT);
        Tensor expected = Complex.ZERO;
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test3() {
        Tensor t = parse("(x-y)**2*f + (-x**2 + 2*x*y - y**2)*f");
        Tensor actual = FastTensors.multiplySumElementsOnFactorAndExpand((Sum) t, parse("1/(x**2 - 2*x*y + y**2)"));
        Tensor expected = Complex.ZERO;
        TAssert.assertEquals(actual, expected);
    }

}
