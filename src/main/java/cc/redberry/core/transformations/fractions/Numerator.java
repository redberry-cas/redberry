package cc.redberry.core.transformations.fractions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Numerator implements Transformation {
    public static final Numerator NUMERATOR = new Numerator();

    private Numerator() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return NumeratorDenominator.getNumeratorAndDenominator(t).numerator;
    }
}
