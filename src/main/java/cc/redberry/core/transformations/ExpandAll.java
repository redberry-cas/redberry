package cc.redberry.core.transformations;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseGuide;

import static cc.redberry.core.tensor.Tensors.reciprocal;
import static cc.redberry.core.transformations.ExpandUtils.*;
import static cc.redberry.core.utils.TensorUtils.isPositiveIntegerPower;

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
        NumDen numDen = getNumDen(product);
        Tensor denominator = numDen.denominator;

//        assert !isPositiveIntegerPower(denominator);
        if (denominator instanceof Product)
            denominator = expandProductOfSums((Product) numDen.denominator, transformations);
        boolean denExpanded = denominator != numDen.denominator;
        denominator = reciprocal(denominator);

        Tensor numerator = numDen.numerator;
        Tensor res = Tensors.multiply(denominator, numerator), temp = res;
        if (res instanceof Product)
            res = expandProductOfSums((Product) temp, transformations);
        if (denExpanded || res != temp)
            return res;
        return product;
    }
}
