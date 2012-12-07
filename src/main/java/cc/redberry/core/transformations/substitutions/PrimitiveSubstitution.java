package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class PrimitiveSubstitution {
    final Tensor from, to;
    final boolean toIsSymbolic;

    PrimitiveSubstitution(Tensor from, Tensor to) {
        this.from = from;
        this.to = to;
        this.toIsSymbolic = TensorUtils.isSymbolic(to);
    }

    Tensor newTo(Tensor current, SubstitutionIterator iterator) {
        if (current.getClass() != from.getClass())
            return current;
        return newTo_(current, iterator);
    }

    abstract Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator);
}
