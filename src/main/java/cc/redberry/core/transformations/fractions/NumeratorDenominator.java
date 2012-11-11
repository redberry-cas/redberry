package cc.redberry.core.transformations.fractions;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class NumeratorDenominator {
    public final Tensor numerator, denominator;

    private NumeratorDenominator(Tensor numerator, Tensor denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public Tensor getNumerator() {
        return numerator;
    }

    public Tensor getDenominator() {
        return denominator;
    }

    public static NumeratorDenominator getNumeratorAndDenominator(Tensor tensor) {
        return getNumeratorAndDenominator(tensor, defaultDenominatorIndicator);
    }

    public static NumeratorDenominator getNumeratorAndDenominator(Tensor tensor, Indicator<Tensor> denominatorIndicator) {
        if (denominatorIndicator.is(tensor))
            return new NumeratorDenominator(Complex.ONE, Tensors.reciprocal(tensor));

        if (tensor instanceof Power && tensor.get(1) instanceof Sum) {
            List<Tensor> powers = expandPower(tensor);
            TensorBuilder denominator = new ProductBuilder(),
                    numerator = new ProductBuilder();
            for (Tensor power : powers)
                if (denominatorIndicator.is(power))
                    denominator.put(Tensors.reciprocal(power));
                else
                    numerator.put(power);
            return new NumeratorDenominator(numerator.build(), denominator.build());
        }

        if (!(tensor instanceof Product))
            return new NumeratorDenominator(tensor, Complex.ONE);

        ProductBuilder denominators = new ProductBuilder();
        Tensor temp = tensor;
        Tensor t;
        Tensor exponent;
        for (int i = tensor.size() - 1; i >= 0; --i) {
            t = tensor.get(i);
            if (denominatorIndicator.is(t)) {
                exponent = Tensors.negate(t.get(1));
                denominators.put(Tensors.pow(t.get(0), exponent));
                if (temp instanceof Product)
                    temp = ((Product) temp).remove(i);
                else
                    temp = Complex.ONE;
            }
        }
        return new NumeratorDenominator(temp, denominators.build());
    }

    private static List<Tensor> expandPower(Tensor power) {
        List<Tensor> powers = new ArrayList<>(power.get(1).size());
        for (Tensor exponent : power.get(1))
            powers.add(Tensors.pow(power.get(0), exponent));
        return powers;
    }

    public static Indicator<Tensor> defaultDenominatorIndicator = new Indicator<Tensor>() {
        @Override
        public boolean is(Tensor tensor) {
            if (tensor instanceof Power) {
                Tensor exponent = tensor.get(1);
                if (exponent instanceof Complex)
                    return NumberUtils.isRealNegative((Complex) exponent);
                if (exponent instanceof Product)
                    return ((Product) exponent).getFactor().isMinusOne();
            }
            return false;
        }
    };

    public static Indicator<Tensor> integerDenominatorIndicator = new Indicator<Tensor>() {
        @Override
        public boolean is(Tensor tensor) {
            return TensorUtils.isNegativeIntegerPower(tensor);
        }
    };
}
