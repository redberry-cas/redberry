package cc.redberry.core.transformations;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TensorLastIterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ComplexConjugate implements Transformation {
    public static final ComplexConjugate CONJUGATE = new ComplexConjugate();

    private ComplexConjugate() {
    }

    @Override
    public Tensor transform(Tensor t) {
        TensorLastIterator iterator = new TensorLastIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Complex)
                iterator.set(((Complex) c).conjugate());
        return iterator.result();
    }
}
