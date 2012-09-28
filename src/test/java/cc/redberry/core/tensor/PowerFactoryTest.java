package cc.redberry.core.tensor;

import cc.redberry.core.context.ToStringMode;
import junit.framework.Assert;
import org.junit.Test;

public class PowerFactoryTest {

    @Test
    public void rationalValuesTest() {
        Assert.assertEquals(Tensors.parse("1/4"),
                            Tensors.parse("Power[1/2,2]"));

        Assert.assertEquals(Tensors.parse("1/3"),
                            Tensors.parse("Power[1/9,1/2]"));

        Assert.assertEquals(Tensors.parse("3"),
                            Tensors.parse("Power[1/9,-1/2]"));

        Assert.assertEquals(Tensors.parse("27"),
                            Tensors.parse("Power[1/9,-3/2]"));
    }

    @Test
    public void rationalValuesNegativeTest() {
        Assert.assertEquals("1/2**1/2",
                            Tensors.parse("Power[1/2,1/2]").toString(ToStringMode.Redberry));

        Assert.assertEquals("1/2**1/3",
                            Tensors.parse("Power[1/2,1/3]").toString(ToStringMode.Redberry));
    }

    @Test
    public void testPower1() {
        Tensor expected = Tensors.parse("3+I");
        Tensor actual = Tensors.parse("(28+I*96)**(1/4)");
        Assert.assertEquals(expected, actual);
    }
}
