package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class TreeNodeSubstitution {
    final Tensor from, to;
    final boolean toIsSymbolic;

    TreeNodeSubstitution(Tensor from, Tensor to) {
        this.from = from;
        this.to = to;
        this.toIsSymbolic = TensorUtils.isSymbolic(to);
    }

    Tensor newTo(Tensor currentNode, int[] forbiddenIndices) {
        if (currentNode.getClass() != from.getClass())
            return currentNode;
        return newTo_(currentNode, forbiddenIndices);
    }

    abstract Tensor newTo_(Tensor currentNode, int[] forbiddenIndices);
}
