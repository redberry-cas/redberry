package cc.redberry.core.transformations;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.TensorUtils;

import static cc.redberry.core.transformations.ExpandUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandNumerator implements Transformation {
    @Override
    public Tensor transform(Tensor t) {
        return null;
    }

//    public static Tensor expand(Tensor tensor, TraverseGuide traverseGuide, Transformation... transformations) {
//        SubstitutionIterator iterator = new SubstitutionIterator(tensor, traverseGuide);
//        Tensor current;
//        while ((current = iterator.next()) != null) {
//            if (current instanceof Product)
//                iterator.unsafeSet(expandProductOfSums((Product) current, transformations));
//            else if (isPositiveIntegerPower(current)) {
//                Sum sum = (Sum) current.get(0);
//                int exponent = ((Complex) current.get(1)).getReal().intValue();
//                if (TensorUtils.isSymbolic(sum))
//                    iterator.unsafeSet(expandSymbolicPower(sum, exponent, transformations));
//                else
//                    iterator.set(expandPower(sum, exponent, iterator.getForbidden(), transformations));
//            }
//
//        }
//        return iterator.result();
//    }
}
