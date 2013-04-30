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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class SchreierSimsAlgorithm {
    private SchreierSimsAlgorithm() {
    }

    static StripResult strip(Permutation permutation,
                             ArrayList<SGSIntermediateElement> intermediateGeneratingSet) {

        Permutation temp = permutation;
        int i;
        for (i = 0; i < intermediateGeneratingSet.size(); ++i) {
            int beta = temp.newIndexOf(intermediateGeneratingSet.get(i).basePoint);
            if (!intermediateGeneratingSet.get(i).belongsToOrbit(beta))
                return new StripResult(temp, i);
            temp = temp.composition(intermediateGeneratingSet.get(i).getInverseTransversalOf(beta));
        }
        return new StripResult(temp, i);
    }

    static class StripResult {
        final int stopPoint;
        final Permutation remainderPermutation;

        public StripResult(Permutation remainderPermutation, int stopPoint) {
            this.stopPoint = stopPoint;
            this.remainderPermutation = remainderPermutation;
        }

        @Override
        public String toString() {
            return remainderPermutation + ", (" + stopPoint + ")";
        }
    }

    static SGSElement[] createSGS(Permutation[] generators) {
        if (generators.length == 0)
            return new SGSElement[0];

        final int length = generators[0].length();
        int firstBase = -1;
        for (int i = generators.length - 1; i >= 0; --i) {
            for (int j = length - 1; j >= 0; --j)
                if (generators[i].newIndexOf(j) != j) {
                    firstBase = j;
                    break;
                }
        }

        if (firstBase == -1)
            return new SGSElement[0];

        IntArrayList base = new IntArrayList();
        base.add(firstBase);

        ArrayList<SGSIntermediateElement> intermediateSGSElements = new ArrayList<>();
        intermediateSGSElements.add(
                new SGSIntermediateElement(firstBase,
                        new ArrayList<>(Arrays.asList(generators)),
                        length));

        schreierSimsAlgorithm(intermediateSGSElements);

        SGSElement[] sgs = new SGSElement[intermediateSGSElements.size()];
        for (int i = sgs.length - 1; i >= 0; --i) {
            intermediateSGSElements.get(i).trimToSize();
            sgs[i] = new SGSElement(intermediateSGSElements.get(i));
        }

        return sgs;
    }

    static void schreierSimsAlgorithm(ArrayList<SGSIntermediateElement> intermediateSGSElements) {
        boolean y;
        int dimension = intermediateSGSElements.get(0).schreierVector.length;
        for (Permutation generator : intermediateSGSElements.get(0).stabilizerGenerators) {
            y = false;
            for (SGSIntermediateElement sgsElement : intermediateSGSElements) {
                if (generator.newIndexOf(sgsElement.basePoint) != sgsElement.basePoint) {
                    y = true;
                    break;
                }
            }
            if (!y)
                for (int i = 0; i < dimension; ++i)
                    if (generator.newIndexOf(i) != i)
                        intermediateSGSElements.add(
                                new SGSIntermediateElement(i,
                                        intermediateSGSElements.get(intermediateSGSElements.size() - 1).getBasePointStabilizerGenerators(),
                                        dimension));
        }

        int i = intermediateSGSElements.size() - 1;
        SGSIntermediateElement currentElement;
        int orbitIndex, beta;
        out:
        while (i >= 0) {
            currentElement = intermediateSGSElements.get(i);
            //enumerating orbit elements
            for (orbitIndex = 0; orbitIndex < currentElement.orbit.size(); ++orbitIndex) {
                beta = currentElement.orbit.get(orbitIndex);
                //enumerating stabilizers from current sgsElement
                for (Permutation stabilizerGenerator : currentElement.stabilizerGenerators) {
                    if (!currentElement.getTransversalOf(beta).composition(stabilizerGenerator)
                            .equals(currentElement.getTransversalOf(stabilizerGenerator.newIndexOf(beta)))) {
                        y = true;
                        StripResult str = strip(
                                currentElement.getTransversalOf(beta).composition(
                                        stabilizerGenerator).composition(
                                        currentElement.getInverseTransversalOf(stabilizerGenerator.newIndexOf(beta))),
                                intermediateSGSElements);

                        if (str.stopPoint < intermediateSGSElements.size()) {
                            y = false;
                        } else if (!str.remainderPermutation.isIdentity()) {
                            y = false;
                            for (int a = 0; a < dimension; ++a)
                                if (str.remainderPermutation.newIndexOf(a) != a) {
                                    intermediateSGSElements.add(new SGSIntermediateElement(a, new ArrayList<Permutation>(), dimension));
                                    break;
                                }
                        }
                        if (!y) {
                            for (int l = i + 1; l <= str.stopPoint; ++l) {
                                intermediateSGSElements.get(l).stabilizerGenerators.add(str.remainderPermutation);
                                intermediateSGSElements.get(l).reCalculateSchreierVector();
                            }
                            i = str.stopPoint;
                            continue out;
                        }
                    }
                }
            }
            --i;
        }
    }

}
