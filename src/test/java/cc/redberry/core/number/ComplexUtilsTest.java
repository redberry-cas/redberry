package cc.redberry.core.number;

import cc.redberry.core.tensor.Tensors;
import org.junit.Assert;
import org.junit.Test;

public class ComplexUtilsTest {
    private static final double DELTA = 1E-10;

    @Test
    public void test() {
        atomicTest((Complex) Tensors.parse("1+2*I"), true);
        atomicTest((Complex) Tensors.parse("7*I"), false);
    }

    private static void atomicTest(Complex input, boolean exp) {
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arcsin(ComplexUtils.sin(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.sin(ComplexUtils.arcsin(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arccos(ComplexUtils.cos(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.cos(ComplexUtils.arccos(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arctan(ComplexUtils.tan(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.tan(ComplexUtils.arctan(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arccot(ComplexUtils.cot(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.cot(ComplexUtils.arccot(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.exp(ComplexUtils.log(input))).absNumeric(), DELTA);
        if (exp)
            Assert.assertEquals(0.0, input.subtract(ComplexUtils.log(ComplexUtils.exp(input))).absNumeric(), DELTA);
    }
}
