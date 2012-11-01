package cc.redberry.core.transformations;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseGuide;

import static cc.redberry.core.transformations.ExpandUtils.NumDen;
import static cc.redberry.core.transformations.ExpandUtils.getNumDen;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandNumerator extends ExpandAbstract {
    public static final ExpandNumerator EXPAND_NUMERATOR = new ExpandNumerator();

    private ExpandNumerator() {
        super();
    }

    public ExpandNumerator(Transformation[] transformations) {
        super(transformations);
    }

    public ExpandNumerator(Transformation[] transformations, TraverseGuide traverseGuide) {
        super(transformations, traverseGuide);
    }

    public static Tensor expandNumerator(Tensor tensor) {
        return EXPAND_NUMERATOR.transform(tensor);
    }

    public static Tensor expandNumerator(Tensor tensor, Transformation... transformations) {
        return new ExpandNumerator(transformations).transform(tensor);
    }

    @Override
    protected Tensor expandProduct(Product product, Transformation[] transformations) {
        NumDen numDen = getNumDen(product);
        Tensor numerator = numDen.numerator;
        if (numerator instanceof Product)
            numerator = ExpandUtils.expandProductOfSums((Product) numDen.numerator, transformations);
        if (numDen.numerator == numerator)
            return product;
        return Tensors.multiply(numerator, numDen.denominator);
    }
}
