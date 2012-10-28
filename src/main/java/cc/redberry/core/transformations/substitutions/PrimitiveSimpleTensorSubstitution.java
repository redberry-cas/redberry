package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class PrimitiveSimpleTensorSubstitution extends PrimitiveSubstitution {
    PrimitiveSimpleTensorSubstitution(Tensor from, Tensor to) {
        super(from, to);
    }

    @Override
    Tensor newTo_(Tensor currentNode, int[] forbiddenIndices) {
        IndexMappingBuffer buffer =
                IndexMappings.getFirst(from, currentNode);
        if (buffer == null)
            return currentNode;
        Tensor newTo;
        if (toIsSymbolic)
            newTo = buffer.getSignum() ? Tensors.negate(to) : to;
        else
            newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbiddenIndices);

        return newTo;
    }
}
