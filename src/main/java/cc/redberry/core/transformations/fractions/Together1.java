package cc.redberry.core.transformations.fractions;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cc.redberry.core.transformations.CollectScalarFactors.collectScalarFactors;
import static cc.redberry.core.transformations.fractions.NumeratorDenominator.defaultDenominatorIndicator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Together1 {
    public static Tensor together(Tensor t) {
        TensorLastIterator iterator = new TensorLastIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (c instanceof Sum)
                iterator.set(togetherSum(c));
            if (c instanceof Product)
                iterator.set(collectScalarFactors(c));
        }
        return iterator.result();
    }

    private static Tensor togetherSum(Tensor tensor) {
        if (!(tensor instanceof Sum))
            return tensor;
        if (!needTogether(tensor))
            return tensor;

        int size = tensor.size(), i;

        //allocating array of numerators
        TensorBuilder[] numerator = new TensorBuilder[size];
        //numerator of the first term in sum
        TensorBuilder baseNumerator = new ScalarsBackedProductBuilder();
        numerator[0] = baseNumerator;

        //allocating resulting denominator
        TMap<Tensor, SumBuilder> denominator = new THashMap<>();

        Tensor temp = tensor.get(0);
        //adding to the resulting denominator the denominator of the first term in sum
        if (temp instanceof Power) {
            if (defaultDenominatorIndicator.is(temp)) {
                SumBuilder sb = new SumBuilder();
                sb.put(Tensors.negate(temp.get(1)));
                denominator.put(temp.get(0), sb);
            } else
                baseNumerator.put(temp);
        } else if (temp instanceof Product)
            for (Tensor factor : temp)
                if (defaultDenominatorIndicator.is(factor)) {
                    assert factor instanceof Power;
                    SumBuilder sb = new SumBuilder();
                    sb.put(Tensors.negate(factor.get(1)));
                    denominator.put(factor.get(0), sb);
                } else
                    baseNumerator.put(factor);
        else
            baseNumerator.put(temp);

        //appending all other terms in the sum to the result
        for (i = 1; i < size; ++i) {
            temp = tensor.get(i);
            numerator[i] = new ScalarsBackedProductBuilder();

            Set<Tensor> processed = new HashSet<>();
            if (temp instanceof Power) {
                if (defaultDenominatorIndicator.is(temp)) {
                    Tensor base = temp.get(0);
                    processed.add(base);
                    SumBuilder sb = denominator.get(base);
                    if (sb == null) {
                        //adding the new factor to the denominator
                        putNew(temp, denominator, baseNumerator);
                    } else {
                        Tensor exponent = Tensors.negate(temp.get(1));
                        Tensor diff = Tensors.subtract(sb.build(), exponent);
                        if (!TensorUtils.isInteger(diff)) {
                            putNew(temp, denominator, baseNumerator);

                        } else {
                            int di = ((Complex) diff).intValue();
                            if (di > 0)
                                numerator[i].put(Tensors.pow(base, di));
                            else {
                                sb.put(((Complex) diff).abs());
                                baseNumerator.put(Tensors.pow(base, -di));
                            }
                        }
                    }
                }
            } else if (temp instanceof Product) {
                for (Tensor factor : temp) {
                    if (defaultDenominatorIndicator.is(factor)) {
                        Tensor base = factor.get(0);
                        processed.add(base);
                        SumBuilder sb = denominator.get(base);
                        if (sb == null) {
                            //adding the new factor to the denominator
                            putNew(factor, denominator, baseNumerator);
                            continue;
                        }
                        Tensor exponent = Tensors.negate(factor.get(1));
                        Tensor diff = Tensors.subtract(sb.build(), exponent);
                        if (!TensorUtils.isInteger(diff)) {
                            putNew(factor, denominator, baseNumerator);
                            continue;
                        }
                        int di = ((Complex) diff).intValue();
                        if (di > 0)
                            numerator[i].put(Tensors.pow(base, di));
                        else {
                            sb.put(((Complex) diff).abs());
                            baseNumerator.put(Tensors.pow(base, -di));
                        }

                    } else
                        numerator[i].put(factor);
                }
            } else
                numerator[i].put(temp);
            for (Map.Entry<Tensor, SumBuilder> entry : denominator.entrySet()) {
                if (processed.contains(entry.getKey()))
                    continue;
                numerator[i].put(Tensors.pow(entry.getKey(), entry.getValue().build()));
            }

        }
        ProductBuilder den = new ProductBuilder(denominator.size(), 0);
        for (Map.Entry<Tensor, SumBuilder> entry : denominator.entrySet())
            den.put(Tensors.pow(entry.getKey(), entry.getValue().build()));
        SumBuilder num = new SumBuilder(numerator.length);
        for (TensorBuilder builder : numerator)
            num.put(builder.build());

        return Tensors.multiply(num.build(), Tensors.reciprocal(den.build()));
    }

    private static void putNew(Tensor t, TMap<Tensor, SumBuilder> denominator, TensorBuilder baseNumerator) {
        SumBuilder sb = new SumBuilder();
        sb.put(Tensors.negate(t.get(1)));
        denominator.put(t.get(0), sb);
        baseNumerator.put(Tensors.reciprocal(t));
    }

    private static TMap<Tensor, SumBuilder> createDenominator(Tensor tensor) {
        TMap<Tensor, SumBuilder> result = new THashMap<>();
        if (tensor instanceof Power && defaultDenominatorIndicator.is(tensor)) {
            SumBuilder sb = new SumBuilder();
            sb.put(Tensors.negate(tensor.get(1)));
            result.put(tensor.get(0), sb);
            return result;
        }
        if (tensor instanceof Product) {
            for (Tensor factor : tensor) {
                if (defaultDenominatorIndicator.is(factor)) {
                    assert factor instanceof Power;
                    SumBuilder sb = new SumBuilder();
                    sb.put(Tensors.negate(factor.get(1)));
                    result.put(factor.get(0), sb);
                }
            }
        }
        return result;
    }

    private static boolean needTogether(Tensor sum) {
        for (Tensor summand : sum)
            if (summand instanceof Product)
                for (Tensor m : summand) {
                    if (defaultDenominatorIndicator.is(m))
                        return true;
                }
            else if (summand instanceof Power)
                if (defaultDenominatorIndicator.is(summand))
                    return true;
        return false;
    }


}
