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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static cc.redberry.core.number.NumberUtils.isZeroOrIndeterminate;
import static cc.redberry.core.utils.TensorUtils.isScalar;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ScalarsBackedProductBuilder implements TensorBuilder {
    /* a containner of powers */
    private final PowersContainer powers;

    /* freeIndex -> connectedComponent, which contains this free index
    * each free index belongs to the unique component */
    private final TIntObjectHashMap<Component> indexToComponent;

    /* set of unique components */
    private final Set<Component> components;

    /* complex factor of resulting product */
    private Complex factor = Complex.ONE;

    public ScalarsBackedProductBuilder() {
        powers = new PowersContainer();
        indexToComponent = new TIntObjectHashMap<>();
        components = new HashSet<>();
    }

    public ScalarsBackedProductBuilder(int powersCapacity, int componentsCapacity, int indicesCapacity) {
        powers = new PowersContainer(powersCapacity);
        indexToComponent = new TIntObjectHashMap<>(indicesCapacity);
        components = new HashSet<>(componentsCapacity);
    }

    /* for clone() */
    private ScalarsBackedProductBuilder(PowersContainer powers, TIntObjectHashMap<Component> indexToComponent, Set<Component> components, Complex factor) {
        this.powers = powers;
        this.indexToComponent = indexToComponent;
        this.components = components;
        this.factor = factor;
    }

    @Override
    public void put(Tensor tensor) {
        if (tensor instanceof Complex) {
            factor = factor.multiply((Complex) tensor);
            return;
        }
        if (isZeroOrIndeterminate(factor))
            return;

        if (tensor instanceof Product) {
            Product p = (Product) tensor;
            factor = factor.multiply(p.factor);
            if (isZeroOrIndeterminate(factor))
                return;

            ProductContent pc = p.getContent();
            //todo can be improved, since a part of product factors already collected
            for (Tensor t : p.getAllScalarsWithoutFactor())
                powers.put(t);
            Tensor nonScalar = pc.getNonScalar();
            if (nonScalar == null)
                return;

            if (nonScalar instanceof Product)
                for (Tensor t : nonScalar)
                    putNonScalar(t);
            else
                putNonScalar(nonScalar);
            return;
        }
        if (isScalar(tensor)) {
            powers.put(tensor);
            return;
        } else
            putNonScalar(tensor);
    }

    private void putNonScalar(Tensor tensor) {
        Indices freeIndices = tensor.getIndices().getFree();
        TIntHashSet freeSet = new TIntHashSet(freeIndices.getAllIndices().copy());

        // Firstly we should check whether adding
        // specified tensor will merge some of the
        // existing components,
        // e.g. if component1 = F_mn*A^n
        //         component2 = H_ij
        //         tensor     = X^im
        // then two of the existing components must be
        // merged into resulting
        //         component = F_mn*A^n*H_ij*X^im

        //components that will be merged
        Set<Component> toMerge = new HashSet<>();
        Component component;
        int index;
        if (!indexToComponent.isEmpty())
            // for each free index in tensor
            for (TIntIterator iterator = freeSet.iterator(); iterator.hasNext(); ) {
                index = iterator.next();
                // this assertion case corresponds to the inconsistent
                // indices exception
                //todo discuss with Dima, may be we must to throw exception here?
                assert !indexToComponent.containsKey(index);

                index = IndicesUtils.inverseIndexState(index);
                component = indexToComponent.remove(index);
                if (component != null) {
                    // free index of tensor (e.g. ^i) is
                    // contracted with index (_i) in component
                    // as result, this index is not free now

                    //removing not free from the set of free indices of tensor
                    iterator.remove();
                    //removing not free from the set of free indices of component
                    component.freeIndices.remove(index);
                    //the component should be merged with this tensor
                    toMerge.add(component);
                }
            }

        if (toMerge.isEmpty()) {
            //no any intersections found
            //so, tensor forms the new connected component
            component = new Component(tensor, freeSet);
            //for each free index of tensor
            for (TIntIterator iterator = freeSet.iterator(); iterator.hasNext(); ) {
                index = iterator.next();
                //we should add the entry new free index <-> new component
                //to the index <-> component mapping
                indexToComponent.put(index, component);
            }
            //adding new unique component to the set of unique components
            components.add(component);
            return;
        }

        // At this point toMerge is not empty
        // we shall merge all of the components from the toMerge
        // set into the first component in the toMerge set

        Iterator<Component> iterator = toMerge.iterator();
        //taking the first component in the toMerge set
        component = iterator.next();
        //adding new tensor to this component
        component.elements.add(tensor);
        //adding all of the new free indices to the free indices of component
        component.freeIndices.addAll(freeSet);

        // We should add additional entries for the new free
        // indices into the free index <-> component mapping

        //for each new free index
        for (TIntIterator tit = freeSet.iterator(); tit.hasNext(); ) {
            index = tit.next();
            //adding additional entries for new free
            //indices in the free index <-> component mapping
            indexToComponent.put(index, component);
        }

        // Merge

        Component temp;
        //for all components except the first one from the toMerge set
        while (iterator.hasNext()) {
            temp = iterator.next();
            //for all free indices in the current component
            for (TIntIterator tit = temp.freeIndices.iterator(); tit.hasNext(); ) {
                index = tit.next();
                //replacing old component, that must be merged into
                //first component in toMerge set with this first component
                indexToComponent.put(index, component);
            }
            //merging free indices from temp
            component.freeIndices.addAll(temp.freeIndices);
            //merging tensors from temp
            component.elements.addAll(temp.elements);
            //removing temp from the set of unique components
            components.remove(temp);
        }

        //checking whether the component became a scalar after merging
        if (component.freeIndices.isEmpty()) {
            //component is scalar

            //removing it from the set of unique components
            components.remove(component);

            // we do not need to remove anything in the free index <-> component
            // mapping, since it is done automatically

            //putting scalar component
            powers.put(
                    new Product(
                            new IndicesBuilder().append(component.elements).getIndices(),
                            Complex.ONE,
                            new Tensor[0], //all tensors have indices with nonzero length
                            component.elements.toArray(new Tensor[component.elements.size()])));
        }
    }


    @Override
    public Tensor build() {
        if (isZeroOrIndeterminate(factor))
            return factor;

        if (powers.isSign())
            factor = factor.negate();

        ArrayList<Tensor> indexLess = new ArrayList<>(powers.size());
        ArrayList<Tensor> data = new ArrayList<>();
        // processing powers
        for (Tensor power : powers) {
            if (power instanceof Complex) {
                factor = factor.multiply((Complex) power);
                if (isZeroOrIndeterminate(factor))
                    return factor;
            } else if (TensorUtils.isIndexless(power))
                indexLess.add(power);
            else {
                if (power instanceof Product)
                    //case power = (x_m*x^m)**1 (= x_m*x^m)
                    for (Tensor t : power)
                        data.add(t);
                else
                    data.add(power);
            }
        }

        // processing non indexless tensors from unique components
        for (Component component : components)
            data.addAll(component.elements);

        if (indexLess.isEmpty() && data.isEmpty())
            return factor;

        if (factor.isOne()) {
            if (indexLess.size() == 1 && data.isEmpty())
                //case x -> x
                return indexLess.get(0);
            if (indexLess.isEmpty() && data.size() == 1)
                //case x_i -> x_i
                return data.get(0);
        }

        if (factor.isMinusOne()) {
            Sum s = null;
            if (indexLess.size() == 1 && data.isEmpty() && indexLess.get(0) instanceof Sum)
                //case (-1)*(a+b) -> -a-b
                s = ((Sum) indexLess.get(0));
            if (indexLess.isEmpty() && data.size() == 1 && data.get(0) instanceof Sum)
                //case (-1)*(a_i+b_i) -> -a_i-b_i
                s = ((Sum) data.get(0));
            if (s != null) {
                Tensor sumData[] = s.data.clone();
                for (int i = sumData.length - 1; i >= 0; --i)
                    sumData[i] = Tensors.negate(sumData[i]);
                return new Sum(s.indices, sumData, s.hashCode());
            }
        }

        return new Product(new IndicesBuilder().append(data).getIndices(),
                factor,
                indexLess.toArray(new Tensor[indexLess.size()]),
                data.toArray(new Tensor[data.size()]));
    }

    @Override
    public ScalarsBackedProductBuilder clone() {
        Set<Component> newComponents = new HashSet<>(components.size());
        TIntObjectHashMap<Component> nIndexToComponent = new TIntObjectHashMap<>(indexToComponent);
        Component component;
        //deep clone
        for (Component curr : components) {
            newComponents.add(component = curr.clone());
            for (TIntIterator iterator = component.freeIndices.iterator(); iterator.hasNext(); )
                nIndexToComponent.put(iterator.next(), component);
        }
        return new ScalarsBackedProductBuilder(powers.clone(), nIndexToComponent, newComponents, factor);
    }

    private static final class Component {
        private final ArrayList<Tensor> elements;
        private final TIntHashSet freeIndices;

        private Component(Tensor tensor, TIntHashSet freeIndices) {
            this.elements = new ArrayList<>();
            this.elements.add(tensor);
            this.freeIndices = freeIndices;
        }

        private Component(ArrayList<Tensor> elements, TIntHashSet freeIndices) {
            this.elements = elements;
            this.freeIndices = freeIndices;
        }

        public Component clone() {
            return new Component(new ArrayList<>(elements), new TIntHashSet(freeIndices));
        }
    }
}
