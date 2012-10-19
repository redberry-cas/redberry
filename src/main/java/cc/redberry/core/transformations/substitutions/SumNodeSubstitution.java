package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.tensor.SumBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.ApplyIndexMapping;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumNodeSubstitution extends TreeNodeSubstitution {
    public SumNodeSubstitution(Tensor from, Tensor to) {
        super(from, to);
    }

    @Override
    Tensor newTo_(Tensor currentNode, int[] forbiddenIndices) {
        BijectionContainer bc = new SumBijectionPort(from, currentNode).take();
        if (bc == null)
            return currentNode;

        IndexMappingBuffer buffer = bc.buffer;
        Tensor newTo;
        if (toIsSymbolic)
            newTo = to;
        else
            newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbiddenIndices);

        SumBuilder builder = new SumBuilder();
        int[] bijection = bc.bijection;
        Arrays.sort(bijection);
        builder.put(newTo);
        for (int i = currentNode.size() - 1; i >= 0; --i)
            if (Arrays.binarySearch(bijection, i) < 0)
                builder.put(currentNode.get(i));
        return builder.build();
    }
}
