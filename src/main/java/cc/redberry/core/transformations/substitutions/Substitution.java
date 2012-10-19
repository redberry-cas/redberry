package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Substitution implements Transformation {
    private TreeNodeSubstitution[] treeNodeSubstitutions;
    private final boolean transformUntilComplete;

    public Substitution(Tensor from, Tensor to) {
        treeNodeSubstitutions = new TreeNodeSubstitution[1];
        treeNodeSubstitutions[0] = createTreeNodeSubstitution(from, to);
        transformUntilComplete = false;
    }

    public Substitution(Tensor[] from, Tensor[] to) {
        this(from, to, false);
    }

    public Substitution(Tensor[] from, Tensor[] to, boolean transformUntilComplete) {
        if (from.length != to.length)
            throw new IllegalArgumentException();
        treeNodeSubstitutions = new TreeNodeSubstitution[from.length];
        for (int i = 0; i < from.length; ++i)
            treeNodeSubstitutions[i] = createTreeNodeSubstitution(from[i], to[i]);
        this.transformUntilComplete = transformUntilComplete;
    }

    private static TreeNodeSubstitution createTreeNodeSubstitution(Tensor from, Tensor to) {
        if (from.getClass() == SimpleTensor.class)
            return new SimpleTensorNodeSubstitution(from, to);
        if (from.getClass() == TensorField.class)
            return new TensorFieldNodeSubstitution(from, to);
        if (from.getClass() == Product.class)
            return new ProductNodeSubstituiton(from, to);
        if (from.getClass() == Sum.class)
            return new SumNodeSubstitution(from, to);
        return new SimpleTensorNodeSubstitution(from, to);
    }

    @Override
    public Tensor transform(Tensor t) {
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor currentNode;
        while ((currentNode = iterator.next()) != null) {
            Tensor old, oldOld;
            out:
            do {
                oldOld = old = currentNode;
                for (TreeNodeSubstitution nodeSubstitution : treeNodeSubstitutions) {
                    currentNode = nodeSubstitution.newTo(old, iterator.getForbidden());
                    if (currentNode != old && !transformUntilComplete)
                        break out;
                    old = currentNode;
                }
                if (!transformUntilComplete)
                    break;
            } while (oldOld != currentNode);
            iterator.set(currentNode);
        }
        return iterator.result();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        TreeNodeSubstitution tr;
        for (int i = 0; ; ++i) {
            tr = treeNodeSubstitutions[i];
            builder.append(tr.from).append(" -> ").append(tr.to);
            if (i == treeNodeSubstitutions.length - 1)
                break;
            builder.append(',');
        }
        return builder.append('}').toString();
    }
}
