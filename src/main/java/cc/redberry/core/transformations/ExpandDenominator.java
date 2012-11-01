package cc.redberry.core.transformations;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraversePermission;

import static cc.redberry.core.transformations.ExpandUtils.NumDen;
import static cc.redberry.core.transformations.ExpandUtils.getNumDen;
import static cc.redberry.core.utils.TensorUtils.isPositiveIntegerPower;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandDenominator extends ExpandAbstract {
    public static TraverseGuide ExpandDenominatorTraverseGuide = new TraverseGuide() {
        @Override
        public TraversePermission getPermission(Tensor tensor, Tensor parent, int indexInParent) {
            if (tensor instanceof ScalarFunction)
                return TraversePermission.DontShow;
            if (tensor instanceof TensorField)
                return TraversePermission.DontShow;
            if (isPositiveIntegerPower(tensor))
                return TraversePermission.DontShow;
            return TraversePermission.Enter;
        }
    };
    public static final ExpandDenominator EXPAND_DENOMINATOR = new ExpandDenominator();

    private ExpandDenominator() {
        super(new Transformation[0], ExpandDenominatorTraverseGuide);
    }

    public ExpandDenominator(Transformation[] transformations) {
        super(transformations, ExpandDenominatorTraverseGuide);
    }

    public ExpandDenominator(Transformation[] transformations, TraverseGuide traverseGuide) {
        super(transformations, traverseGuide);
    }

    public static Tensor expandDenominator(Tensor tensor) {
        return EXPAND_DENOMINATOR.transform(tensor);
    }

    public static Tensor expandDenominator(Tensor tensor, Transformation... transformations) {
        return new ExpandDenominator(transformations).transform(tensor);
    }

    @Override
    protected Tensor expandProduct(Product product, Transformation[] transformations) {
        NumDen numDen = getNumDen(product);
        Tensor denominator = numDen.denominator;
        if (denominator instanceof Product)
            denominator = ExpandUtils.expandProductOfSums((Product) numDen.denominator, transformations);
        if (numDen.denominator == denominator)
            return product;
        return Tensors.multiply(numDen.numerator, Tensors.reciprocal(denominator));
    }
}
