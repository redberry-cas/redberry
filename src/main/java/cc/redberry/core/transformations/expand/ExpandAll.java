package cc.redberry.core.transformations.expand;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.fractions.NumeratorDenominator;
import cc.redberry.core.transformations.Transformation;

import static cc.redberry.core.tensor.Tensors.reciprocal;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandAll extends ExpandAbstract {
    public static final ExpandAll EXPAND_ALL = new ExpandAll();

    private ExpandAll() {
        super(new Transformation[0], TraverseGuide.ALL);
    }

    public ExpandAll(Transformation[] transformations) {
        super(transformations, TraverseGuide.ALL);
    }

    public ExpandAll(Transformation[] transformations, TraverseGuide traverseGuide) {
        super(transformations, traverseGuide);
    }

    public static Tensor expandAll(Tensor tensor) {
        return EXPAND_ALL.transform(tensor);
    }

    public static Tensor expandAll(Tensor tensor, Transformation... transformations) {
        return new ExpandAll(transformations).transform(tensor);
    }

    @Override
    protected Tensor expandProduct(Product product, Transformation[] transformations) {
        NumeratorDenominator numDen = NumeratorDenominator.getNumeratorAndDenominator(product, NumeratorDenominator.integerDenominatorIndicator);
        Tensor denominator = numDen.denominator;

//        assert !isPositiveIntegerPower(denominator);
        if (denominator instanceof Product)
            denominator = ExpandUtils.expandProductOfSums((Product) numDen.denominator, transformations);
        boolean denExpanded = denominator != numDen.denominator;
        denominator = reciprocal(denominator);

        Tensor numerator = numDen.numerator;
        Tensor res = Tensors.multiply(denominator, numerator), temp = res;
        if (res instanceof Product)
            res = ExpandUtils.expandProductOfSums((Product) temp, transformations);
        if (denExpanded || res != temp)
            return res;
        return product;
    }
}
