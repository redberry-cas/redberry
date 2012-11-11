package cc.redberry.core.transformations.expand;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraversePermission;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.TensorUtils;

import static cc.redberry.core.tensor.Tensors.reciprocal;
import static cc.redberry.core.utils.TensorUtils.isNegativeIntegerPower;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class ExpandAbstract implements Transformation {
    public static TraverseGuide DefaultExpandTraverseGuide = new TraverseGuide() {
        @Override
        public TraversePermission getPermission(Tensor tensor, Tensor parent, int indexInParent) {
            if (tensor instanceof ScalarFunction)
                return TraversePermission.DontShow;
            if (tensor instanceof TensorField)
                return TraversePermission.DontShow;
            if (isNegativeIntegerPower(tensor))
                return TraversePermission.DontShow;
            return TraversePermission.Enter;
        }
    };

    protected final Transformation[] transformations;
    protected final TraverseGuide traverseGuide;

    protected ExpandAbstract() {
        this(new Transformation[0], DefaultExpandTraverseGuide);
    }

    protected ExpandAbstract(Transformation[] transformations) {
        this(transformations, DefaultExpandTraverseGuide);
    }

    protected ExpandAbstract(Transformation[] transformations, TraverseGuide traverseGuide) {
        this.transformations = transformations;
        this.traverseGuide = traverseGuide;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.unsafeSet(expandProduct((Product) current, transformations));
            else if (ExpandUtils.isExpandablePower(current)) {
                Sum sum = (Sum) current.get(0);
                int exponent = ((Complex) current.get(1)).intValue();
                if (exponent == -1)
                    continue;
                boolean symbolic = TensorUtils.isSymbolic(sum),
                        reciprocal = exponent < 0;
                exponent = Math.abs(exponent);
                Tensor temp;
                if (symbolic)
                    temp = ExpandUtils.expandSymbolicPower(sum, exponent, transformations);
                else
                    temp = ExpandUtils.expandPower(sum, exponent, iterator.getForbidden(), transformations);
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

    protected abstract Tensor expandProduct(Product product, Transformation[] transformations);
}
