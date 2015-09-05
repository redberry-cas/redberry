/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indexmapping.MappingsPort;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.ParseUtils;
import cc.redberry.core.parser.preprocessor.ChangeIndicesTypesAndTensorNames;
import cc.redberry.core.parser.preprocessor.TypesAndNamesTransformer;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.StructureOfContractions.getToTensorIndex;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.EliminateMetricsTransformation.ELIMINATE_METRICS;
import static cc.redberry.core.transformations.EliminateMetricsTransformation.eliminate;
import static cc.redberry.core.transformations.expand.ExpandTransformation.expand;

/**
 * Simplifies combinations of Levi-Civita tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class LeviCivitaSimplifyTransformation implements Transformation {
    private static final String defaultLeviCivitaName = "eps";

    private final int leviCivita;
    private final boolean minkowskiSpace;
    private final int numberOfIndices;
    private final IndexType typeOfLeviCivitaIndices;
    private final ChangeIndicesTypesAndTensorNames tokenTransformer;
    private final Transformation simplifications, overallSimplifications;
    /**
     * First is Levi-Civita self-contraction and second is d^a_a = numberOfIndices
     */
    private final Expression[] leviCivitaSimplifications;

    /**
     * Creates transformation, which simplifies combinations of Levi-Civita tensors in Euclidean or Minkowski space.
     *
     * @param leviCivita     tensor, which will be considered as Levi-Civita tensor
     * @param minkowskiSpace if {@code true}, then Levi-Civita tensor will be considered in Minkowski
     *                       space (so e.g. e_abcd*e^abcd = -24), otherwise in Euclidean space
     *                       (so e.g. e_abcd*e^abcd = +24)
     */
    public LeviCivitaSimplifyTransformation(SimpleTensor leviCivita, boolean minkowskiSpace) {
        this(leviCivita, minkowskiSpace, Transformation.IDENTITY, Transformation.IDENTITY);
    }


    /**
     * Creates transformation, which simplifies combinations of Levi-Civita tensors in Euclidean or Minkowski space.
     *
     * @param leviCivita      tensor, which will be considered as Levi-Civita tensor
     * @param minkowskiSpace  if {@code true}, then Levi-Civita tensor will be considered in Minkowski
     *                        space (so e.g. e_abcd*e^abcd = -24), otherwise in Euclidean space
     *                        (so e.g. e_abcd*e^abcd = +24)
     * @param simplifications additional transformations applied to each simplified combination of Levi-Civita tensors
     */
    public LeviCivitaSimplifyTransformation(SimpleTensor leviCivita, boolean minkowskiSpace, Transformation simplifications) {
        this(leviCivita, minkowskiSpace, simplifications, Transformation.IDENTITY);
    }

    /**
     * Creates transformation, which simplifies combinations of Levi-Civita tensors in Euclidean or Minkowski space.
     *
     * @param leviCivita             tensor, which will be considered as Levi-Civita tensor
     * @param minkowskiSpace         if {@code true}, then Levi-Civita tensor will be considered in Minkowski
     *                               space (so e.g. e_abcd*e^abcd = -24), otherwise in Euclidean space
     *                               (so e.g. e_abcd*e^abcd = +24)
     * @param simplifications        additional transformations applied to each simplified combination of Levi-Civita tensors
     * @param overallSimplifications additional transformations applied to each simplified product of Levi-Civita tensors
     */
    public LeviCivitaSimplifyTransformation(SimpleTensor leviCivita, boolean minkowskiSpace,
                                            Transformation simplifications, Transformation overallSimplifications) {
        checkLeviCivita(leviCivita);
        this.simplifications = simplifications;
        this.overallSimplifications = overallSimplifications;
        this.leviCivita = leviCivita.getName();
        this.minkowskiSpace = minkowskiSpace;
        this.numberOfIndices = leviCivita.getIndices().size();
        this.typeOfLeviCivitaIndices = IndicesUtils.getTypeEnum(leviCivita.getIndices().get(0));

        final String leviCivitaName = CC.getNameManager().getNameDescriptor(leviCivita.getName()).getName(null);

        this.tokenTransformer = new ChangeIndicesTypesAndTensorNames(
                new TypesAndNamesTransformer() {
                    @Override
                    public int newIndex(int oldIndex, NameAndStructureOfIndices oldDescriptor) {
                        return oldIndex;
                    }

                    @Override
                    public IndexType newType(IndexType oldType, NameAndStructureOfIndices old) {
                        return typeOfLeviCivitaIndices;
                    }

                    @Override
                    public String newName(String oldName, NameAndStructureOfIndices old) {
                        return oldName.equals(defaultLeviCivitaName) ? leviCivitaName : oldName;
                    }
                }
        );
        leviCivitaSimplifications = getLeviCivitaSubstitutions();
    }

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (c instanceof SimpleTensor
                    && ((SimpleTensor) c).getName() == leviCivita
                    && c.getIndices().size() != c.getIndices().getFree().size()) {
                iterator.set(Complex.ZERO);
            }
            if (c instanceof Product)
                iterator.set(simplifyProduct(c));
        }
        return iterator.result();
    }


    private Tensor simplifyProduct(Tensor t) {
        /*
         * Simplifying symmetries
         */

        Product product = (Product) t;
        ProductContent content = product.getContent();
        //positions of Levi-Civita tensors in product
        IntArrayList epsPositions = new IntArrayList();
        int i = 0, sizeOfComponent = content.size();
        for (; i < sizeOfComponent; ++i)
            if (isLeviCivita(content.get(i), leviCivita))
                epsPositions.add(i);

        //no Levi-Civita tensors found
        if (epsPositions.isEmpty())
            return product;

        //calculating connected components with Levi-Civita tensors
        StructureOfContractions fs = content.getStructureOfContractions();
        sizeOfComponent = epsPositions.size();

        //tensors, which are contracted with Levi-Civita ( ... eps_abcd * (...tensors...)^abpq ... )
        Set<Tensor> epsComponent = new HashSet<>(numberOfIndices);
        Tensor temp;
        int toIndex, a, b;

        for (i = 0; i < sizeOfComponent; ++i) {
            //traversing contractions and building single component
            for (long contraction : fs.contractions[epsPositions.get(i)]) {
                toIndex = getToTensorIndex(contraction);
                if (toIndex == -1)
                    continue;
                temp = content.get(toIndex);
                if (isLeviCivita(temp, leviCivita))
                    continue;
                epsComponent.add(temp);
            }
            //all eps indices are free
            if (epsComponent.isEmpty())
                continue;

            //product, which is contracted with Levi-Civita ( ... eps_abcd * (product)^ab ... )
            temp = multiply(epsComponent.toArray(new Tensor[epsComponent.size()]));
            epsComponent.clear();

            //free indices of product, which is contracted with Levi-Civita
            int[] indices = temp.getIndices().getFree().getAllIndices().copy();
            //nothing to do
            if (indices.length == 1)
                continue;
            //positions of indices of product, which are not contracted with eps
            IntArrayList nonPermutableList = new IntArrayList();
            //indices of Levi-Civita
            int[] epsIndices = content.get(epsPositions.get(i)).getIndices().getFree().getAllIndices().copy();

            boolean contract;
            for (b = 0; b < indices.length; ++b) {
                contract = false;
                for (a = 0; a < epsIndices.length; ++a)
                    if (indices[b] == inverseIndexState(epsIndices[a]))
                        contract = true;
                if (!contract)
                    nonPermutableList.add(b);
            }
            int[] nonPermutableArray = nonPermutableList.toArray();

            //symmetries of eps indices, which are contracted with other product (also totally antisymmetric)
            Map<Permutation, Boolean> symmetries = getEpsilonSymmetries(indices.length);

            //symmetries of product, which is contracted with Levi-Civita
            MappingsPort port = IndexMappings.createPort(temp, temp);
            Mapping mapping;
            Permutation sym;

            //check for two symmetric indices of product contracted with two antisymmetric indices of eps
            while ((mapping = port.take()) != null) {
                //symmetry of product indices
                sym = TensorUtils.getSymmetryFromMapping(indices, mapping);
                //if symmetry mixes indices of product, which are not contracted with eps
                if (!checkNonPermutingPositions(sym, nonPermutableArray))
                    continue;
                //bingo!
                if (sym.antisymmetry() != symmetries.get(sym.toSymmetry()))
                    return Complex.ZERO;
            }

        }

        /*
         * Simplifying Levi-Civita self-contractions
         */

        if (epsPositions.size() == 1)
            return product;

        int[] epsPoss = epsPositions.toArray();
        for (i = 0; i < epsPoss.length; ++i)
            epsPoss[i] += product.sizeOfIndexlessPart();

        Tensor epsSubProduct = product.select(epsPoss);
        Tensor remnant = product.remove(epsPoss);
        for (Expression exp : leviCivitaSimplifications)
            epsSubProduct = exp.transform(epsSubProduct);

        //todo expand only Levi-Civita sums
        epsSubProduct = simplifications.transform(eliminate(expand(epsSubProduct, ELIMINATE_METRICS, simplifications)));
        epsSubProduct = leviCivitaSimplifications[1].transform(epsSubProduct);
        return overallSimplifications.transform(multiply(epsSubProduct, remnant));
    }

    private static boolean checkNonPermutingPositions(Permutation permutation, int[] nonPermutablePositions) {
        for (int i : nonPermutablePositions)
            if (permutation.newIndexOf(i) != i)
                return false;
        return true;
    }

    private static boolean isLeviCivita(Tensor tensor, int leviCivitaName) {
        return tensor instanceof SimpleTensor && ((SimpleTensor) tensor).getName() == leviCivitaName;
    }


    private Expression getLeviCivitaSelfContraction() {
        ParseToken substitutionToken = cachedLeviCivitaSelfContractions.get(numberOfIndices);
        if (substitutionToken == null) {

            //the l.h.s. of substitution: eps_{abc...}*eps^{pqr...}
            //lower indices of eps ( eps_{...lower...} )
            int[] lower = new int[numberOfIndices];
            //upper indices of eps ( eps^{...lower...} )
            int[] upper = new int[numberOfIndices];

            for (int i = 0; i < numberOfIndices; ++i) {
                lower[i] = i;
                upper[i] = IndicesUtils.inverseIndexState(numberOfIndices + i);
            }

            //eps_{abc...}
            SimpleTensor eps1 = simpleTensor(defaultLeviCivitaName, IndicesFactory.createSimple(null, lower));
            //eps^{pqr...}
            SimpleTensor eps2 = simpleTensor(defaultLeviCivitaName, IndicesFactory.createSimple(null, upper));
            //eps_{abc..}*eps^{pqr...}
            Tensor lhs = multiply(eps1, eps2);

            //the r.h.s. of substitution: determinant of deltas
            //Determinant of Kronecker deltas
            Tensor[][] matrix = new Tensor[numberOfIndices][numberOfIndices];
            int j;
            for (int i = 0; i < numberOfIndices; ++i)
                for (j = 0; j < numberOfIndices; ++j)
                    matrix[i][j] = createKronecker(lower[i], upper[j]);
            Tensor rhs = TensorUtils.det(matrix);

            // eps_{abc..}*eps^{pqr...} = det
            Expression substitution = expression(lhs, rhs);

            substitutionToken = ParseUtils.tensor2AST(substitution);
            cachedLeviCivitaSelfContractions.put(numberOfIndices, substitutionToken);
        }

        Expression substitution = (Expression) tokenTransformer.transform(substitutionToken).toTensor();
        if (minkowskiSpace & numberOfIndices % 2 == 0)
            substitution = expression(substitution.get(0), negate(substitution.get(1)));
        return substitution;
    }

    private Expression[] getLeviCivitaSubstitutions() {
        Expression[] substitutions = new Expression[2];
        //Levi-Civita self-contraction
        substitutions[0] = getLeviCivitaSelfContraction();

        //d^a_a = numberOfIndices
        substitutions[1] = expression(createKronecker(
                        setType(typeOfLeviCivitaIndices, 0),
                        setType(typeOfLeviCivitaIndices, 0x80000000)),
                new Complex(numberOfIndices));

        return substitutions;
    }

    synchronized
    private static Map<Permutation, Boolean> getEpsilonSymmetries(int indicesSize) {
        Map<Permutation, Boolean> symmetries = cachedLeviCivitaSymmetries.get(indicesSize);
        if (symmetries != null)
            return symmetries;
        symmetries = new HashMap<>();
        PermutationGroup lc = PermutationGroup.antisymmetricGroup(indicesSize);
        for (Permutation symmetry : lc)
            symmetries.put(symmetry.toSymmetry(), symmetry.antisymmetry());
        cachedLeviCivitaSymmetries.put(indicesSize, symmetries);
        return symmetries;
    }

    private static TIntObjectHashMap<ParseToken> cachedLeviCivitaSelfContractions = new TIntObjectHashMap<>();
    private static TIntObjectHashMap<Map<Permutation, Boolean>> cachedLeviCivitaSymmetries = new TIntObjectHashMap<>();

    private static void checkLeviCivita(SimpleTensor LeviCivita) {
        SimpleIndices indices = LeviCivita.getIndices();
        if (indices.size() <= 1)
            throw new IllegalArgumentException("Levi-Civita cannot be a scalar.");
        byte type = getType(indices.get(0));
        for (int i = 1; i < indices.size(); ++i)
            if (type != getType(indices.get(i)))
                throw new IllegalArgumentException("Levi-Civita have indices with different types.");
    }
}
