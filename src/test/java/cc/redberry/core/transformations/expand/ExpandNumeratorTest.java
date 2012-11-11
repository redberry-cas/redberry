package cc.redberry.core.transformations.expand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.transformations.expand.ExpandNumerator.expandNumerator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandNumeratorTest {
    @Test
    public void test1() {
        Tensor t = parse("(a+b)/(c+d)");
        TAssert.assertTrue(t == expandNumerator(t));
    }

    @Test
    public void test2() {
        Tensor a = expandNumerator(parse("(a+b)**2/(c+d)"));
        Tensor e = parse("(a**2+2*a*b+b**2)/(c+d)");
        TAssert.assertEquals(a, e);
    }
    //todo more tests
}
