package cc.redberry.core.indices;

import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Tensors;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesTypeStructureTest {

    @Test
    public void test1() {
        SimpleIndices si = ParserIndices.parseSimple("_ab'c'^d'");
        IndicesTypeStructure st = new IndicesTypeStructure(si);
        Assert.assertTrue(st.isStructureOf(si));
        Assert.assertFalse(st.isStructureOf(ParserIndices.parseSimple("_ab'^c'd'")));
        Assert.assertTrue(st.isStructureOf(ParserIndices.parseSimple("^a_b'c'^d'")));
    }

    @Test
    public void testDiffNames1() {
        Assert.assertTrue(Tensors.parse("v_a'").hashCode() != Tensors.parse("v^a'").hashCode());
        Assert.assertTrue(Tensors.parse("v_a").hashCode() == Tensors.parse("v^a").hashCode());
    }
}
