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
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("S^a'_b'"), IndexType.LatinLower1);
        indicesInsertion.addInsertionRule(parseSimple("K^A'_B'"), IndexType.LatinUpper1);

        TAssert.assertEquals(parse("Tr[S*S*S*Y^y+S*Y^y+K*R^y,l'] + K*Y^y"),
                "Y^{y}*K^{A'}_{B'}+Y^{y}*d^{A'}_{B'}*S^{a'}_{a'}+Y^{y}*d^{A'}_{B'}*S^{c'}_{a'}*S^{b'}_{c'}*S^{a'}_{b'}+K^{A'}_{B'}*d^{a'}_{a'}*R^{y}");

        TAssert.assertEquals(parse("Tr[S*S*S*Y^y+S*Y^y+K*R^y,L']"),
                "Y^{y}*d^{A'}_{A'}*S^{a'}_{b'}+Y^{y}*d^{A'}_{A'}*S^{d'}_{b'}*S^{c'}_{d'}*S^{a'}_{c'}+K^{A'}_{A'}*d^{a'}_{b'}*R^{y}");

        TAssert.assertEquals(parse("Tr[S*S*S*Y^y+S*Y^y+K*R^y,L'] + S*Y^y"),
                "Y^{y}*S^{a'}_{b'}+Y^{y}*d^{A'}_{A'}*S^{a'}_{b'}+Y^{y}*d^{A'}_{A'}*S^{d'}_{b'}*S^{c'}_{d'}*S^{a'}_{c'}+K^{A'}_{A'}*d^{a'}_{b'}*R^{y}");
    }

    @Test
    public void test3() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("S^a'_b'"), IndexType.LatinLower1);
        indicesInsertion.addInsertionRule(parseSimple("K^A'_B'"), IndexType.LatinUpper1);
        TAssert.assertEquals(parse("Tr[K] + Tr[S]"), "K^A'_A'+S^a'_a'");
    }

    @Test
    public void test4() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("S^a'A'_b'B'"), IndexType.LatinLower1);
        indicesInsertion.addInsertionRule(parseSimple("S^a'A'_b'B'"), IndexType.LatinUpper1);
        indicesInsertion.addInsertionRule(parseSimple("v^a'A'"), IndexType.LatinLower1);
        indicesInsertion.addInsertionRule(parseSimple("v^a'A'"), IndexType.LatinUpper1);
        indicesInsertion.addInsertionRule(parseSimple("cv_a'A'"), IndexType.LatinLower1);
        indicesInsertion.addInsertionRule(parseSimple("cv_a'A'"), IndexType.LatinUpper1);

        TAssert.assertEquals(parse("cv*v"), "v^{a'A'}*cv_{a'A'}");
        TAssert.assertEquals(parse("v*cv"), "v^{a'A'}*cv_{b'B'}");
        TAssert.assertEquals(parse("cv*S*v"), "v^{b'B'}*S^{a'}_{b'}^{A'}_{B'}*cv_{a'A'}");
    }

    @Test
    public void test5() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("f^a'A'_b'B'[x]"), IndexType.LatinLower1);
        indicesInsertion.addInsertionRule(parseSimple("f^a'A'_b'B'[x]"), IndexType.LatinUpper1);

        TAssert.assertEquals(parse("f[x] = 1 + c"), "f^{a'}_{b'}^{A'}_{B'}[x] = (c+1)*d^{a'}_{b'}*d^{A'}_{B'}");
    }

    @Test
    public void test13() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("S^a'_b'"), IndexType.LatinLower1);
        gii.addInsertionRule(parseSimple("K^A'_B'"), IndexType.LatinUpper1);
        gii.addInsertionRule(parseSimple("V^a'"), IndexType.LatinLower1);
        gii.addInsertionRule(parseSimple("cV_b'"), IndexType.LatinLower1);
        Tensor t = parse("Sin[Tr[S*S*S+S+K]]+K", gii);
        System.out.println(t);
    }
}
