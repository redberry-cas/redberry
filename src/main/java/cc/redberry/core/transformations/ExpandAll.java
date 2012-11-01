package cc.redberry.core.transformations;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.TensorUtils;

import static cc.redberry.core.tensor.Tensors.reciprocal;
import static cc.redberry.core.transformations.ExpandUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandAll implements Transformation {
    public static final ExpandAll EXPAND_ALL = new ExpandAll();

    private final Transformation[] transformations;
    private final TraverseGuide traverseGuide;

    public ExpandAll() {
        this(TraverseGuide.ALL, new Transformation[0]);
    }

    public ExpandAll(Transformation[] transformations) {
        this(TraverseGuide.ALL, transformations);
    }

    public ExpandAll(TraverseGuide traverseGuide, Transformation[] transformations) {
        this.transformations = transformations;
        this.traverseGuide = traverseGuide;
    }

    @Override
    public Tensor transform(Tensor t) {
        return expandAll(t, traverseGuide, transformations);
    }

    public static Tensor expandAll(Tensor tensor) {
        return expandAll(tensor, new Transformation[0]);
    }

    public static Tensor expandAll(Tensor tensor, Transformation... transformations) {
        return expandAll(tensor, TraverseGuide.ALL, transformations);
    }

    public static Tensor expandAll(Tensor tensor, TraverseGuide traverseGuide, Transformation[] transformations) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.unsafeSet(expandAllProductOfSums((Product) current, transformations));
            else if (isIntegerPower(current)) {
                Sum sum = (Sum) current.get(0);
                int exponent = ((Complex) current.get(1)).intValue();
                boolean symbolic = TensorUtils.isSymbolic(sum),
                        reciprocal = exponent < 0;
                exponent = Math.abs(exponent);
                Tensor temp;
                if (symbolic)
                    temp = expandSymbolicPower(sum, exponent, transformations);
                else
                    temp = expandPower(sum, exponent, iterator.getForbidden(), transformations);
                if (reciprocal)
                    temp = reciprocal(temp);
                if (symbolic)
                    iterator.unsafeSet(temp);
                else
                    iterator.set(temp);
            }
        }
        return iterator.result();
    }

    private static Tensor expandAllProductOfSums(Product product, Transformation[] transformations) {
        NumDen numDen = getNumDen(product);
        Tensor denominator = numDen.denominator;

        assert !isPositiveIntegerPower(denominator);
        if (denominator instanceof Product)
            denominator = expandProductOfSums((Product) denominator, transformations);
        denominator = reciprocal(denominator);

        Tensor numerator = numDen.numerator;
        Tensor res = Tensors.multiply(denominator, numerator);
        if (res instanceof Product)
            return expandProductOfSums((Product) res, transformations);
        else
            return res;
    }
}
