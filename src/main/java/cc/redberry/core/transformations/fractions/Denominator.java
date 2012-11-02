package cc.redberry.core.transformations.fractions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Denominator implements Transformation {
    public static final Denominator DENOMINATOR = new Denominator();

    private Denominator() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return NumeratorDenominator.getNumeratorAndDenominator(t).denominator;
    }
}
