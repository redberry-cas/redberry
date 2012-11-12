package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

public class GeneralIndicesInsertionTest {

    @Test
    public void test1() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);

        indicesInsertion.addInsertionRule(parseSimple("S^a'_b'"), IndexType.LatinLower1);
        TAssert.assertEquals(parse("S"), "S^a'_b'");
        TAssert.assertEquals(parse("S*S"), "S^a'_c'*S^c'_b'");
        TAssert.assertEquals(parse("S*S*S"), "S^a'_c'*S^c'_d'*S^d'_b'");
        TAssert.assertEquals(parse("S + S"), "2*S^a'_b'");
        TAssert.assertEquals(parse("S*A_m + S*B_m"), "S^a'_b'*A_m + S^a'_b'*B_m");
        TAssert.assertEquals(parse("S*(A_m + S*B_m)"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m)");
        TAssert.assertEquals(parse("S*(A_m + S*B_m) + S*C_m"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m) + S^a'_b'*C_m");
        TAssert.assertEquals(parse("S*(A_m + S*B_m) + S*C_m = F_m"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m) + S^a'_b'*C_m = d^a'_b'*F_m");
        TAssert.assertEquals(parse("S*(A_m + S*B_m) + S*C_m = F_m + D_m"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m) + S^a'_b'*C_m = d^a'_b'*(F_m + D_m)");
        TAssert.assertEquals(parse("S*(A_m + S*(B_m + S*S*A_m*(S + F_m^m*S))*S)"),
                "S^a'_c'*(d^c'_b'*A_m + S^c'_d'*(B_m*d^d'_e' + S^d'_f'*S^f'_g'*A_m*(S^g'_e' + F^m_m*S^g'_e'))*S^e'_b')");
    }

    @Test
    public void test2() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("S^a'_b'"), IndexType.LatinLower1);
        gii.addInsertionRule(parseSimple("K^A'_B'"), IndexType.LatinUpper1);
        gii.addInsertionRule(parseSimple("V^a'"), IndexType.LatinLower1);
        gii.addInsertionRule(parseSimple("cV_b'"), IndexType.LatinLower1);
        //Tensor t = Tensors.parse("cV*(S*S+M*N)*V+K", gii);
        Tensor t = parse("K*S=S*S+K+3", gii);
        System.out.println(t);
    }
}
