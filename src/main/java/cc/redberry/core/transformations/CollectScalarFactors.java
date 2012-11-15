package cc.redberry.core.transformations;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ScalarsBackedProductBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class CollectScalarFactors implements Transformation {
    public static final CollectScalarFactors COLLECT_SCALAR_FACTORS = new CollectScalarFactors();
    private final TraverseGuide traverseGuide;

    private CollectScalarFactors() {
        this.traverseGuide = TraverseGuide.ALL;
    }

    public CollectScalarFactors(TraverseGuide traverseGuide) {
        this.traverseGuide = traverseGuide;
    }

    @Override
    public Tensor transform(Tensor t) {
        return collectScalarFactors(t, traverseGuide);
    }

    public static Tensor collectScalarFactors(Tensor tensor) {
        return collectScalarFactors(tensor, TraverseGuide.ALL);
    }

    public static Tensor collectScalarFactors(Tensor tensor, TraverseGuide traverseGuide) {
        TensorLastIterator iterator = new TensorLastIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.set(collectScalarFactorsInProduct((Product) current));
        }
        return iterator.result();
    }

    public static Tensor collectScalarFactorsInProduct(Product product) {
        if (TensorUtils.isSymbolic(product))
            return product;
        ScalarsBackedProductBuilder builder = new ScalarsBackedProductBuilder(product.size(), 1, product.getIndices().getFree().size());
        builder.put(product);
        return builder.build();
    }
}
