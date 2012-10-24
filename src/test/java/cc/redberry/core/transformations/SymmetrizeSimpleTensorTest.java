package cc.redberry.core.transformations;

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.combinatorics.symmetries.SymmetriesFactory.createFullSymmetries;
import static cc.redberry.core.indices.IndexType.LatinLower;
import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.transformations.SymmetrizeSimpleTensor.symmetrize;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetrizeSimpleTensorTest {
    @Test
    public void test1() {
        SimpleTensor t = parseSimple("T_abcd");
        TAssert.assertEquals(
                symmetrize(t, new int[]{0, 1, 2}, createFullSymmetries(3)),
                "1/6*(T_{acbd}+T_{abcd}+T_{cbad}+T_{cabd}+T_{bcad}+T_{bacd})");
    }

    @Test
    public void test2() {
        SimpleTensor t = parseSimple("T_abcd");
        Symmetries symmetries = SymmetriesFactory.createSymmetries(4);
        symmetries.addUnsafe(new Symmetry(new int[]{2, 3, 0, 1}, false));
        symmetries.addUnsafe(new Symmetry(new int[]{1, 0, 2, 3}, true));

        Tensor r = symmetrize(t, new int[]{0, 1, 2, 3}, symmetries);

        TAssert.assertEquals(r,
                "(1/8)*(-T_{abdc}+T_{badc}+T_{dcba}+T_{abcd}+T_{cdab}-T_{bacd}-T_{dcab}-T_{cdba})");
    }

    @Test
    public void test3() {
        SimpleTensor t = parseSimple("T_abcd");
        addSymmetry(t, LatinLower, false, new int[]{1, 0, 2, 3});

        Tensor r = symmetrize(t, new int[]{0, 1, 2, 3}, SymmetriesFactory.createFullSymmetries(4));
        TAssert.assertEquals(r,
                "(1/12)*(T_{adbc}+T_{acdb}+T_{abcd}+T_{bcda}+T_{bcad}+T_{bdac}+T_{acbd}+T_{bdca}+T_{cdab}+T_{adcb}+T_{cdba}+T_{abdc})");
    }

    @Test
    public void test4() {
        SimpleTensor t = parseSimple("T_abcd");
        addSymmetry(t, LatinLower, false, new int[]{2, 3, 0, 1});

        Symmetries symmetries = SymmetriesFactory.createSymmetries(4);
        symmetries.addUnsafe(new Symmetry(new int[]{2, 3, 0, 1}, false));
        symmetries.addUnsafe(new Symmetry(new int[]{1, 0, 2, 3}, true));

        Tensor r = symmetrize(t, new int[]{0, 1, 2, 3}, symmetries);
        System.out.println(r);

        TAssert.assertEquals(r,
                "(1/4)*(-T_{dcab}-T_{bacd}+T_{abcd}+T_{dcba})");
    }

}
