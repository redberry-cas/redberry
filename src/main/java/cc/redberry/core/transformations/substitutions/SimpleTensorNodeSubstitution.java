package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.ApplyIndexMapping;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class SimpleTensorNodeSubstitution extends TreeNodeSubstitution {
    SimpleTensorNodeSubstitution(Tensor from, Tensor to) {
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
            newTo = to;
        else
            newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbiddenIndices);

        if (buffer.getSignum())
            newTo = Tensors.negate(newTo);
        return newTo;
    }
}
