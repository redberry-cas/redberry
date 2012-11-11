package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

public class GeneralIndicesInsertionTest {
    @Test
    public void test0() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(Tensors.parseSimple("S^a'_b'"), IndexType.LatinLower1);
        gii.addInsertionRule(Tensors.parseSimple("V^a'"), IndexType.LatinLower1);
        gii.addInsertionRule(Tensors.parseSimple("cV_b'"), IndexType.LatinLower1);
        Tensor t = Tensors.parse("cV*S*S*V+S", gii);
        System.out.println(t);
    }
}
