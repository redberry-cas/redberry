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
        gii.addInsertionRule(Tensors.parseSimple("K^A'_B'"), IndexType.LatinUpper1);
        gii.addInsertionRule(Tensors.parseSimple("V^a'"), IndexType.LatinLower1);
        gii.addInsertionRule(Tensors.parseSimple("cV_b'"), IndexType.LatinLower1);
        //Tensor t = Tensors.parse("cV*(S*S+M*N)*V+K", gii);
        Tensor t = Tensors.parse("K+S*S*K+1", gii);
        System.out.println(t);
    }

    @Test
    public void test1() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(Tensors.parseSimple("S^a'_b'"), IndexType.LatinLower1);
        gii.addInsertionRule(Tensors.parseSimple("K^A'_B'"), IndexType.LatinUpper1);
        gii.addInsertionRule(Tensors.parseSimple("V^a'"), IndexType.LatinLower1);
        gii.addInsertionRule(Tensors.parseSimple("cV_b'"), IndexType.LatinLower1);
        //Tensor t = Tensors.parse("cV*(S*S+M*N)*V+K", gii);
        Tensor t = Tensors.parse("K*S=S*S+K+3", gii);
        System.out.println(t);
    }
}
