/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.tensor;

import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static cc.redberry.core.number.NumberUtils.isZeroOrIndeterminate;
import static cc.redberry.core.transformations.ToNumericTransformation.toNumeric;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductBuilder implements TensorBuilder {

    private Complex factor = Complex.ONE;
    private final ArrayList<Tensor> elements, indexLess;
    /* hash -> list of power nodes */
    private final PowersContainer symbolicPowers;

    public ProductBuilder(int initialCapacityIndexless, int initialCapacityData) {
        elements = new ArrayList<>(initialCapacityData);
        symbolicPowers = new PowersContainer(initialCapacityIndexless);
        indexLess = new ArrayList<>();
    }

    public ProductBuilder() {
        this(4, 3);
    }

    private ProductBuilder(Complex factor, ArrayList<Tensor> elements, ArrayList<Tensor> indexLess, PowersContainer powers) {
        this.factor = factor;
        this.elements = elements;
        this.indexLess = indexLess;
        this.symbolicPowers = powers;
    }


    private void initializeData(Complex complex, Tensor[] indexlessData, Tensor[] data) {
        this.factor = complex.multiply(this.factor);
        if (isZeroOrIndeterminate(this.factor))
            return;
        elements.addAll(Arrays.asList(data));
        Tensor base, exponent;
        for (Tensor t : indexlessData) {
            if (TensorUtils.isSymbolic(t)) {
                if (t instanceof Power) {
                    //case x^y
                    base = t.get(0);
                    exponent = t.get(1);
                } else {
                    //case x^1 (= x)
                    base = t;
                    exponent = Complex.ONE;
                }
                symbolicPowers.putNew(base, exponent);
            } else
                indexLess.add(t);
        }
    }

    private boolean isEmpty() {
        return symbolicPowers.isEmpty() && elements.isEmpty();
    }

    @Override
    public Tensor build() {
        if (isZeroOrIndeterminate(factor))
            return factor;
        final boolean isNumeric = factor.isNumeric();
        for (Tensor t : symbolicPowers) {
            assert !(t instanceof Product);

            if (isNumeric)
                t = toNumeric(t);

            if (t instanceof Complex) {
                factor = factor.multiply((Complex) t);
                if (isZeroOrIndeterminate(factor))
                    return factor;
            } else
                indexLess.add(t);
        }
        if (symbolicPowers.isSign())
            factor = factor.negate();

        //Only factor
        if (indexLess.isEmpty() && elements.isEmpty())
            return factor;

        if (isNumeric) {
            ArrayList<Tensor> nonNumbers = new ArrayList<>();
            for (Tensor t : elements) {
                t = toNumeric(t);
                if (t instanceof Complex)
                    factor = factor.multiply((Complex) t);
                else
                    nonNumbers.add(t);
            }
            //Only factor
            if (indexLess.isEmpty() && nonNumbers.isEmpty())
                return factor;
            elements.clear();
            elements.addAll(nonNumbers);
        }

        // 1 * (something)
        if (factor.isOne()) {
            if (indexLess.size() == 1 && elements.isEmpty())
                return indexLess.get(0);
            if (indexLess.isEmpty() && elements.size() == 1)
                return elements.get(0);
        }

        if (factor.isMinusOne()) {
            Sum s = null;
            if (indexLess.size() == 1 && elements.isEmpty() && indexLess.get(0) instanceof Sum)
                //case (-1)*(a+b) -> -a-b
                s = ((Sum) indexLess.get(0));
            if (indexLess.isEmpty() && elements.size() == 1 && elements.get(0) instanceof Sum)
                //case (-1)*(a_i+b_i) -> -a_i-b_i
                s = ((Sum) elements.get(0));
            if (s != null) {
                Tensor sumData[] = s.data.clone();
                for (int i = sumData.length - 1; i >= 0; --i)
                    sumData[i] = Tensors.negate(sumData[i]);
                return new Sum(s.indices, sumData, s.hashCode());
            }
        }

        //Calculating product indices
        IndicesBuilder ibs = new IndicesBuilder();
        for (Tensor m : elements)
            ibs.append(m);
        return new Product(ibs.getIndices(), factor,
                indexLess.toArray(new Tensor[indexLess.size()]),
                elements.toArray(new Tensor[elements.size()]));
    }

    @Override
    public void put(Tensor tensor) {
        if (factor.isNumeric())
            tensor = toNumeric(tensor);
        if (tensor instanceof Complex) {
            factor = factor.multiply((Complex) tensor);
            return;
        }

        if (tensor instanceof Product) {
            Product p = (Product) tensor;
            //if no any elements were added yet
            if (isEmpty()) {
                initializeData(p.factor, p.indexlessData, p.data);
            } else {
                factor = factor.multiply(p.factor);
                if (isZeroOrIndeterminate(factor))
                    return;
                for (Tensor t : p.indexlessData)
                    put(t);
                elements.addAll(Arrays.asList(p.data));
            }
            return;
        }
        if (isZeroOrIndeterminate(factor))
            return;

        if (TensorUtils.isSymbolic(tensor)) {
            symbolicPowers.put(tensor);
        } else if (tensor.getIndices().size() == 0)
            indexLess.add(tensor);
        else
            elements.add(tensor);
    }


    @Override
    public ProductBuilder clone() {
        return new ProductBuilder(factor, new ArrayList<>(elements), new ArrayList<>(indexLess), symbolicPowers.clone());
    }
}
