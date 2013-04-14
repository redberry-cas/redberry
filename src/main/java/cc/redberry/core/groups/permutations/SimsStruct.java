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
import java.util.Collections;
import java.util.List;

import static cc.redberry.core.groups.permutations.PermutationGroup.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SimsStruct {
    public final int beta;
    public final List<int[]> S;
    public final int[] schreierVector;
    public final IntArrayList orbit;

    public SimsStruct(int beta, List<int[]> S, int dimension) {
        this.beta = beta;
        this.S = S;
        this.schreierVector = new int[dimension];
        this.orbit = new IntArrayList();
        orbit.add(beta);
        reCalculateSchreierVector();
    }

    void reCalculateSchreierVector() {
        orbit.removeAfter(1);
        Arrays.fill(schreierVector, -2);
        schreierVector[beta] = -1;
        int image;
        for (int i = 0; i < orbit.size(); ++i) {
            for (int r = 0; r < S.size(); ++r) {
                int[] generator = S.get(r);
                image = generator[orbit.get(i)];
                if (schreierVector[image] == -2) {
                    orbit.add(image);
                    schreierVector[image] = r;
                }
            }
        }
    }

    boolean belongsToOrbit(int point) {
        return schreierVector[point] != -2;
    }

    int[] getTransversalOf(int point) {
        int[] transversal = getIdentity(schreierVector.length);

        while (schreierVector[transversal[point]] != -1) {
            transversal = composition(transversal, inverse(S.get(schreierVector[transversal[point]])));
        }


        transversal = inverse(transversal);
        assert transversal[beta] == point;
        return transversal;
    }

    List<int[]> getBetaStabilizerGenerators() {
        List<int[]> rr = new ArrayList<>();
        for (int[] gen : S) {
            if (gen[beta] == beta)
                rr.add(gen);
        }
        return rr;
    }


    public static StripResult strip(int[] permutation, List<SimsStruct> gs) {

        int[] temp = permutation;
        int i;
        for (i = 0; i < gs.size(); ++i) {
            int beta = temp[gs.get(i).beta];
            if (!gs.get(i).belongsToOrbit(beta))
                return new StripResult(temp, i);
            temp = composition(temp, inverse(gs.get(i).getTransversalOf(beta)));
        }
        return new StripResult(temp, i);
    }

    public static class StripResult {
        final int stopPoint;
        final int[] remainderPermutation;

        public StripResult(int[] remainderPermutation, int stopPoint) {
            this.stopPoint = stopPoint;
            this.remainderPermutation = remainderPermutation;
        }

        @Override
        public String toString() {
            return Arrays.toString(remainderPermutation) + ", (" + stopPoint + ")";
        }
    }

    public static List<SimsStruct> createSGS(int[][] generators) {
        int firstBase = -1;
        for (int i = 0; i < generators.length; ++i) {
            for (int j = 0; j < generators[i].length; ++j)
                if (generators[i][j] != j) {
                    firstBase = j;
                    break;
                }
        }
        if (firstBase == -1)
            return Collections.EMPTY_LIST;

        IntArrayList base = new IntArrayList();
        base.add(firstBase);

        List<SimsStruct> structs = new ArrayList<>();
        structs.add(new SimsStruct(firstBase, new ArrayList<>(Arrays.asList(generators)), generators[0].length));

        applySchreierSimsAlgorithmToListOfSimsStructs(structs);

        return structs;
    }

    public static boolean isIdentity(int[] pppp) {
        for (int u = 0; u < pppp.length; ++u)
            if (u != pppp[u])
                return false;

        return true;
    }

    public static void applySchreierSimsAlgorithmToListOfSimsStructs(List<SimsStruct> structs) {
        boolean y;
        int dimension = structs.get(0).schreierVector.length;
        for (int[] gen : structs.get(0).S) {
            y = false;
            for (SimsStruct str : structs) {
                if (gen[str.beta] != str.beta) {
                    y = true;
                    break;
                }
            }
            if (!y) {
                for (int i = 0; i < dimension; ++i) {
                    if (gen[i] != i)
                        structs.add(new SimsStruct(i, structs.get(structs.size() - 1).getBetaStabilizerGenerators(), dimension));
                }
            }
        }

        int i = structs.size() - 1;
        out:
        while (i >= 0) {
            SimsStruct cur = structs.get(i);
            for (int jj = 0; jj < cur.orbit.size(); ++jj) {
                int beta = cur.orbit.get(jj);
                for (int[] x : cur.S) {

                    if (!Arrays.equals(composition(cur.getTransversalOf(beta), x), cur.getTransversalOf(x[beta]))) {
                        y = true;
                        StripResult str = strip(composition(composition(cur.getTransversalOf(beta), x), inverse(cur.getTransversalOf(x[beta]))), structs);
                        if (str.stopPoint < structs.size()) {
                            y = false;
                        } else if (!isIdentity(str.remainderPermutation)) {
                            y = false;

                            for (int a = 0; a < dimension; ++a)
                                if (str.remainderPermutation[a] != a) {
                                    structs.add(new SimsStruct(a, new ArrayList<int[]>(), dimension));
                                    break;
                                }
                        }
                        if (!y) {
                            for (int l = i + 1; l <= str.stopPoint; ++l) {
                                structs.get(l).S.add(str.remainderPermutation);
                                structs.get(l).reCalculateSchreierVector();

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
