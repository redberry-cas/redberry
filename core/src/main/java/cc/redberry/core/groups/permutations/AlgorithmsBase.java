/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.context.CC;
import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.MathUtils;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.groups.permutations.RandomPermutation.*;

/**
 * Algorithms for constructing, modifying and manipulating base and strong generating set (BSGS) of permutation group
 * including Schreier-Sims algorithm and its randomized versions, algorithms for changing base of BSGS, algorithms for
 * creating BSGS of symmetric and alternating groups and many other utility methods.
 * <p><b>BSGS data structure</b>
 * The data structure used for representing BSGS is an array list of
 * BSGS elements (see {@link cc.redberry.core.groups.permutations.BSGSElement}); <i>i-th</i> item in this list
 * contains <i>i-th</i> point of base and <i>i-th</i> basic stabilizer in stabilizers chain (pointwise stabilizer of all
 * points before <i>i-th</i> point, exclusive), represented by its generators.
 * </p>
 * <p> The BSGS structure appears in two forms:
 * <i>mutable</i> --- {@code ArrayList<BSGSCandidateElement>} (see
 * {@link cc.redberry.core.groups.permutations.BSGSCandidateElement}) and <i>immutable</i> --- {@code List<BSGSElement>}
 * (unmodifiable). The first form is used as a candidate BSGS of permutation group, while the second everywhere
 * considered as a valid BSGS. For illustration, consider the following code:
 * <pre style="background:#f1f1f1;color:#000"> 1:  <span style="color:#a08000">Permutation</span> perm1 <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">1</span>, <span style="color:#0080a0">2</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">0</span>);
 * 2:  <span style="color:#a08000">Permutation</span> perm2 <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">1</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">2</span>);
 * 3:  <span style="color:#406040">//create a candidate BSGS</span>
 * 4:  <span style="color:#a08000">ArrayList&lt;<span style="color:#a08000">BSGSCandidateElement</span>></span> candidate <span style="color:#2060a0">=</span> (<span style="color:#a08000">ArrayList</span>) <span style="color:#a08000">AlgorithmsBase</span><span style="color:#2060a0">.</span>createRawBSGSCandidate(perm1, perm2);
 * 5:  <span style="color:#406040">//apply randomized Schreier-Sims algorithm to candidate BSGS (add missing base points and basic stabilizers)</span>
 * 6:  <span style="color:#a08000">AlgorithmsBase</span><span style="color:#2060a0">.</span>RandomSchreierSimsAlgorithm(candidate, <span style="color:#0080a0">0.9999</span>, <span style="color:#2060a0">new</span> <span style="color:#a08000">Well1024a</span>());
 * 7:  <span style="color:#406040">//if our random Schreier-Sims was not enough</span>
 * 8:  <span style="color:#2060a0">if</span> (<span style="color:#2060a0">!</span><span style="color:#a08000">AlgorithmsBase</span><span style="color:#2060a0">.</span>isBSGS(candidate))
 * 9:  <span style="color:#a08000">    AlgorithmsBase</span><span style="color:#2060a0">.</span>SchreierSimsAlgorithm(candidate);
 * 10: <span style="color:#a08000">List&lt;<span style="color:#a08000">BSGSElement</span>></span> bsgs <span style="color:#2060a0">=</span> <span style="color:#a08000">AlgorithmsBase</span><span style="color:#2060a0">.</span>asBSGSList(candidate);
 * </pre>
 * In this example we construct a very raw candidate BSGS in the line 4 and then apply randomized Schreier-Sims
 * algorithm which modifies it. Still after, there is a very small ~0.01% probability that this candidate is not a real
 * BSGS; we check this in the line 8 and apply deterministic algorithm if necessary. After all, we can convert
 * candidate BSGS to a list with immutable elements in line 10.
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.groups.permutations.AlgorithmsBacktrack
 * @since 1.1.6
 */
public final class AlgorithmsBase {
    private AlgorithmsBase() {
    }
    //------------------------------ ALGORITHMS --------------------------------------------//

    /**
     * The result of {@link #strip(java.util.List, Permutation)}
     *
     * @see #strip(java.util.List, Permutation)
     */
    public static final class StripContainer {
        public final int terminationLevel;
        public final Permutation remainder;

        public StripContainer(int terminationLevel, Permutation remainder) {
            this.terminationLevel = terminationLevel;
            this.remainder = remainder;
        }
    }

    /**
     * Calculates representation of specified permutation in terms of specified BSGS. If specified permutation can be
     * represented in terms of specified BSGS, then the produced remainder will be identity and {@code terminationLevel}
     * equals to BSGS size. If produced {@code remainder} is not identity then it fixes all base points in specified
     * BSGS (hence permutation does not belong to group). If {@code terminationLevel} is less then BSGS size, then
     * specified permutation does not  belong to group and produced remainder is a unique generator that should be
     * placed at {@code terminationLevel} in specified BSGS in order to extend group such that it will contain
     * specified permutation.
     * <p/>
     * The algorithm is a straightforward implementation of STRIP described in Sec. 4.4.1 of <b>[Holt05]</b>.
     *
     * @param BSGS
     * @param permutation
     * @return terminationLevel and remainder
     */
    public static StripContainer strip(final List<? extends BSGSElement> BSGS, Permutation permutation) {
        //loop over all base points
        for (int i = 0, size = BSGS.size(); i < size; ++i) {
            //image of current base point under the permutation
            int beta = permutation.newIndexOf(BSGS.get(i).basePoint);
            //test whether this image belongs to the orbit
            if (!BSGS.get(i).belongsToOrbit(beta)) {
                //if not, then permutation cannot be represented in terms of specified BSGS
                return new StripContainer(i, permutation);
            }
            //strip
            permutation = permutation.composition(BSGS.get(i).getInverseTransversalOf(beta));
        }
        return new StripContainer(BSGS.size(), permutation);
    }

    /**
     * Returns whether specified permutation belongs to permutation group defined by specified base and strong
     * generating set.
     *
     * @param BSGS        base and strong generating set
     * @param permutation permutation
     * @return true if specified permutation belongs to permutation group defined by specified base and strong
     * generating set and false otherwise
     */
    public static boolean membershipTest(final List<? extends BSGSElement> BSGS, Permutation permutation) {
        StripContainer sc = strip(BSGS, permutation);
        return sc.terminationLevel == BSGS.size() && sc.remainder.isIdentity();
    }

    /**
     * Creates a raw BSGS candidate represented as list. This method simply takes all distinct points that can be
     * mapped onto another points under any of generators and adjoins these points to a base. If generating set is
     * empty, or it fixes all points, then this method returns {@code Collections.EMPTY_LIST}, otherwise it returns an
     * {@code ArrayList} which can be further used in Schreier-Sims algorithm.
     *
     * @param generators group generators
     * @return raw BSGS candidate
     * @throws IllegalArgumentException if not all permutations have same length
     */
    public static List<BSGSCandidateElement> createRawBSGSCandidate(final Permutation... generators) {
        return createRawBSGSCandidate(Arrays.asList(generators));
    }

    /**
     * Creates a raw BSGS candidate represented as list. This method simply takes all distinct points that can be
     * mapped onto another points under any of generators and adjoins these points to a base. If generating set is
     * empty, or it fixes all points, then this method returns {@code Collections.EMPTY_LIST}, otherwise it returns an
     * {@code ArrayList} which can be further used in Schreier-Sims algorithm.
     *
     * @param generators group generators
     * @return raw BSGS candidate
     * @throws IllegalArgumentException if not all permutations have same length
     */
    public static List<BSGSCandidateElement> createRawBSGSCandidate(final List<Permutation> generators) {
        if (generators.isEmpty())
            return Collections.EMPTY_LIST;
        checkGenerators(generators);

        final int length = generators.get(0).degree();

        //first let's find a "proto-base" - a set of points that cannot be fixed by any of specified generators
        //and a "proto-BSGS" corresponding to this base

        //at the moment our "proto-base" will contain only one point
        int firstBasePoint = -1;

        //we try to find such a point that is not fixed at least by one of the generators
        out:
        for (Permutation permutation : generators)
            for (int i = 0; i < length; ++i)
                if (permutation.newIndexOf(i) != i) {
                    firstBasePoint = i;
                    break out;
                }

        if (firstBasePoint == -1) {
            //there is no any point that is not fixed by all generators, i.e. all generators are identities
            return Collections.EMPTY_LIST;
        }

        // our "proto-base" with only one point
        IntArrayList base = new IntArrayList();
        base.add(firstBasePoint);
        // our "proto-BSGS" with only one element
        // corresponding G^(i) is G^(0) = G, so its stabilizer generators (stabilizes zero points)
        // are just generators of group
        ArrayList<BSGSCandidateElement> BSGS = new ArrayList<>();
        BSGS.add(new BSGSCandidateElement(firstBasePoint, new ArrayList<>(generators), new int[length]));

        //make use all unused generators
        makeUseOfAllGenerators(BSGS);
        return BSGS;
    }

    /**
     * Creates a raw BSGS candidate represented as list. This method simply adds to {@code knownBase} all distinct
     * points that can be mapped onto another points under any of generators. Those points in {@code knownBase} that are
     * fixed by all generators will not be taken into account.  If generating set is empty, or it fixes all points,
     * then this method returns {@code Collections.EMPTY_LIST}, otherwise it returns an {@code ArrayList} which can be
     * further used in Schreier-Sims algorithm.
     *
     * @param knownBase  some proposed base points
     * @param generators group generators
     * @return raw BSGS candidate
     * @throws IllegalArgumentException if not all permutations have same length
     */
    public static List<BSGSCandidateElement> createRawBSGSCandidate(final int[] knownBase, final List<Permutation> generators) {
        if (generators.isEmpty())
            return Collections.EMPTY_LIST;
        checkGenerators(generators);

        final int length = generators.get(0).degree();

        // first, lets remove unnecessary base points, i.e. such points, that are fixed by all generators

        IntArrayList base = new IntArrayList(knownBase.clone());

        //we try to find such a point that is not fixed at least by one of the generators
        out:
        for (int i = base.size() - 1; i >= 0; --i) {
            for (Permutation permutation : generators)
                if (permutation.newIndexOf(base.get(i)) != base.get(i))
                    continue out;
            base.remove(i);
        }

        if (base.isEmpty()) {
            //there is no any point that is not fixed by all generators, i.e. all generators fixes all points in the specified base
            //all that's left is to try to find a base
            return createRawBSGSCandidate(generators);
        }


        ArrayList<BSGSCandidateElement> BSGS = new ArrayList<>(knownBase.length);
        for (int i = 0, size = base.size(); i < size; ++i) {
            if (i == 0) {
                // corresponding G^(i) is G^(0) = G, so its stabilizer generators (stabilizes zero points)
                // are just generators of group
                BSGS.add(new BSGSCandidateElement(base.get(i), new ArrayList<>(generators), new int[length]));
                continue;
            }
            //lets find generators that fixes all points before current point
            ArrayList<Permutation> stabilizerGenerators = new ArrayList<>();
            allgenerators:
            for (Permutation stabilizerGenerator : generators) {
                for (int j = 0; j < i; ++j)
                    if (stabilizerGenerator.newIndexOf(base.get(j)) != base.get(j))
                        continue allgenerators;
                stabilizerGenerators.add(stabilizerGenerator);
            }
            BSGS.add(new BSGSCandidateElement(base.get(i), stabilizerGenerators, new int[length]));
        }

        //make use all unused generators
        makeUseOfAllGenerators(BSGS);

        return BSGS;
    }

    /**
     * Creates BSGS using Schreier-Sims algorithm.
     * <p>
     * The underlying code organized as follows:
     * <pre><code>
     * List&lt;BSGSCandidateElement&gt; BSGSCandidate = createRawBSGSCandidate(generators);
     * if (BSGSCandidate.isEmpty())
     *    return Collections.EMPTY_LIST;
     * SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
     * return asBSGSList(BSGSCandidate);
     * </code></pre>
     * </p>
     *
     * @param generators a set of group generators
     * @return BSGS represented as array of its element
     * @throws cc.redberry.core.groups.permutations.InconsistentGeneratorsException if algorithm detects that specified
     *                                                                              generators are inconsistent (due to antisymmetries)
     * @throws IllegalArgumentException                                             if not all permutations have same length
     * @see #createRawBSGSCandidate(java.util.List)
     * @see #SchreierSimsAlgorithm(java.util.ArrayList)
     */
    public static List<BSGSElement> createBSGSList(final List<Permutation> generators) {
        List<BSGSCandidateElement> BSGSCandidate = createRawBSGSCandidate(generators);
        if (BSGSCandidate.isEmpty())
            return Collections.EMPTY_LIST;
        SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
        removeRedundantBaseRemnant((ArrayList) BSGSCandidate);
        return asBSGSList(BSGSCandidate);
    }

    /**
     * Creates BSGS using Schreier-Sims algorithm. Specified base will be extended if necessary.
     * <p>
     * The underlying code organized as follows:
     * <pre><code>
     * List&lt;BSGSCandidateElement&gt; BSGSCandidate = createRawBSGSCandidate(knownBase, generators);
     * if (BSGSCandidate.isEmpty())
     *    return Collections.EMPTY_LIST;
     * SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
     * return asBSGSList(BSGSCandidate);
     * </code></pre>
     * </p>
     *
     * @param generators a set of group generators
     * @param knownBase  proposed base points
     * @return BSGS represented as array of its element
     * @throws cc.redberry.core.groups.permutations.InconsistentGeneratorsException if algorithm detects that specified
     *                                                                              generators are inconsistent (due to antisymmetries)
     * @throws IllegalArgumentException                                             if not all permutations have same length
     * @see #createRawBSGSCandidate(int[], java.util.List)
     * @see #SchreierSimsAlgorithm(java.util.ArrayList)
     */
    public static List<BSGSElement> createBSGSList(final int[] knownBase, final List<Permutation> generators) {
        List<BSGSCandidateElement> BSGSCandidate = createRawBSGSCandidate(knownBase, generators);
        if (BSGSCandidate.isEmpty())
            return Collections.EMPTY_LIST;
        SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
        removeRedundantBaseRemnant((ArrayList) BSGSCandidate);
        return asBSGSList(BSGSCandidate);
    }

    /**
     * If some of generators fixes all base points, then, this method will find a new point that is not fixed by this
     * generator and add this point to specified BSGS candidate.
     *
     * @param BSGSCandidate BSGS candidate
     */
    public static void makeUseOfAllGenerators(List<BSGSCandidateElement> BSGSCandidate) {
        //all group generators
        List<Permutation> generators = BSGSCandidate.get(0).stabilizerGenerators;
        if (generators.isEmpty())
            return;
        final int length = generators.get(0).degree();
        //iterate over all generators find each one that fixes all base points
        for (Permutation generator : generators) {
            boolean fixesBase = true;
            //iterating over all base points
            for (BSGSCandidateElement element : BSGSCandidate)
                if (generator.newIndexOf(element.basePoint) != element.basePoint) {
                    //this generator does not fix at least one base point => this generator is used
                    fixesBase = false;
                    break;
                }

            if (fixesBase) {
                //current generator fixes all points in base
                //in order to make it in use let's find any point that is not fixed under current generator
                for (int point = 0; point < length; ++point)
                    if (generator.newIndexOf(point) != point) {
                        //this point is not fixed by current generator
                        //let's add this point to base
                        BSGSCandidate.add(new BSGSCandidateElement(point,
                                BSGSCandidate.get(BSGSCandidate.size() - 1).getStabilizersOfThisBasePoint(),
                                new int[length]));
                    }
            }
        }
    }

    /**
     * Applies Schreier-Sims algorithm to specified BSGS candidate and complete it if necessary; as result, specified
     * BSGS candidate will be guaranteed BSGS. The algorithm described as SCHREIERSIMS in Sec. 4.4.1 of <b>[Holt05]</b>.
     *
     * @param BSGSCandidate BSGS candidate
     * @throws cc.redberry.core.groups.permutations.InconsistentGeneratorsException if algorithm detects that specified
     *                                                                              generators are inconsistent (due to antisymmetries)
     */
    public static void SchreierSimsAlgorithm(ArrayList<BSGSCandidateElement> BSGSCandidate) {
        if (BSGSCandidate.isEmpty())
            return;
        final int length = BSGSCandidate.get(0).stabilizerGenerators.get(0).degree();
        //main loop
        BSGSCandidateElement currentElement;
        int index = BSGSCandidate.size() - 1;
        elements:
        while (index >= 0) {
            currentElement = BSGSCandidate.get(index);

            //we need to test that H^i_{beta_i} = H^{(i+1)}
            //for this purpose we shall test that each generator of H^i_{beta_i} belongs to H^{(i+1)}
            //(we already know that H^i_{beta_i} >= H^{(i+1)}, so we shall test that H^i_{beta_i} =< H^{(i+1)})

            // we need to enumerate all generators of H^i_{beta_i}
            // this can be done by enumerating all nontrivial combinations u_{beta}*x*u_{beta^x}^{(-1)},
            // where x - are generators of current group and u_{beta} maps beta_i to beta
            // (see e.g. ORBITSTABILIZER in Sec. 4.1 of [Holt05])
            // enumerating all betas, i.e. current orbit elements
            for (int indexInOrbit = 0, sizeOfOrbit = currentElement.orbitList.size();
                 indexInOrbit < sizeOfOrbit; ++indexInOrbit) {
                //current point in orbit
                int beta = currentElement.orbitList.get(indexInOrbit);
                //obtain u_{beta} - element that maps beta_i onto beta
                Permutation transversalOfBeta = currentElement.getTransversalOf(beta);

                //enumerating through all generators of current element
                for (Permutation stabilizer : currentElement.stabilizerGenerators) {
                    //obtain u_{beta^x} - element that maps beta_i onto beta^x
                    Permutation transversalOfBetaX =
                            currentElement.getTransversalOf(stabilizer.newIndexOf(beta));

                    //so, let's construct nontrivial u_{beta}*x*u_{beta^x}^{(-1)}
                    if (!transversalOfBeta.composition(stabilizer).equals(transversalOfBetaX)) {
                        //this is a nontrivial generator of H^i_{beta_i}
                        Permutation SchreierGenerator = transversalOfBeta.composition(stabilizer, transversalOfBetaX.inverse());

                        // in order to test whether this generator contained in H^(i+1), let's apply STRIP
                        // we can use STRIP since main condition H^i_{beta_i} = H^{(i+1)} is already verified for
                        // all larger values of index (i.e. if we take a part of BSGS starting from index, then it is
                        // real a BSGS for a subgroup fixing all points before index)
                        StripContainer strip = strip(BSGSCandidate, SchreierGenerator);

                        //this signals, whether we shall add new generator to our BSGS (not necessary a new point)
                        boolean toAddNewGenerator = false;
                        if (strip.terminationLevel < BSGSCandidate.size()) {
                            // strip terminated earlier then complete:
                            // this means, that corresponding SchreierGenerator extends an orbit of
                            // BSGS[terminationLevel] element, i.e. there is a permutation (actually it is remainder)
                            // that acts on base point of BSGS[terminationLevel] and maps it out of its orbit.
                            // =>so we shall add a new generator at terminationLevel and recalculate its orbit
                            toAddNewGenerator = true;
                        } else if (!strip.remainder.isIdentity()) {
                            //in this case, nontrivial remainder fixes all base points
                            toAddNewGenerator = true;
                            //so, we need also to extend our base with a new point

                            //let's find some point that is not fixed by remainder
                            for (int i = 0; i < length; ++i)
                                if (strip.remainder.newIndexOf(i) != i) {
                                    // adding this point to BSGS (with empty stabilizers set, since it is a last point)
                                    BSGSCandidate.add(new BSGSCandidateElement(i, new ArrayList<Permutation>(), new int[length]));
                                    //here we can proceed, but we break
                                    break;
                                }
                        }

                        if (toAddNewGenerator) {
                            // we need to add a new generator
                            // (note, that it can fix all old base points, but not necessary)

                            for (int i = index + 1; i <= strip.terminationLevel; ++i) {
                                //add new generator
                                BSGSCandidate.get(i).stabilizerGenerators.add(strip.remainder);
                                //recalculate content
                                BSGSCandidate.get(i).recalculateOrbitAndSchreierVector();
                            }

                            //revert
                            index = strip.terminationLevel;
                            continue elements;
                        }
                    }
                }
            }
            --index;
        }
    }

    /**
     * Applies randomized version of Schreier-Sims algorithm to specified BSGS candidate and complete it if necessary.
     * The probability that after applying this algorithm the BSGS candidate will be guaranteed BSGS is equal to
     * specified confidence level. The algorithm described as RANDOMSCHREIER in Sec. 4.4.5 of <b>[Holt05]</b>.
     *
     * @param BSGSCandidate   BSGS candidate
     * @param confidenceLevel confidence level (0 < confidence level < 1)
     * @param randomGenerator random generator
     * @throws cc.redberry.core.groups.permutations.InconsistentGeneratorsException if algorithm detects that specified
     *                                                                              generators are inconsistent (due to antisymmetries)
     */
    public static void RandomSchreierSimsAlgorithm(ArrayList<BSGSCandidateElement> BSGSCandidate,
                                                   double confidenceLevel, RandomGenerator randomGenerator) {
        if (confidenceLevel > 1 || confidenceLevel < 0)
            throw new IllegalArgumentException("Confidence level must be between 0 and 1.");

        final int length = BSGSCandidate.get(0).stabilizerGenerators.get(0).degree();

        //source of randomness
        List<Permutation> source = new ArrayList<>(BSGSCandidate.get(0).stabilizerGenerators);
        randomness(source, DEFAULT_RANDOMNESS_EXTEND_TO_SIZE, DEFAULT_NUMBER_OF_RANDOM_REFINEMENTS, randomGenerator);
        //recalculate BSGSCandidate
        for (BSGSCandidateElement element : BSGSCandidate)
            element.recalculateOrbitAndSchreierVector();
        makeUseOfAllGenerators(BSGSCandidate);

        //counts the random elements sifted without change to BSGS
        int sifted = 0;
        int CL = (int) (-FastMath.log(2, 1 - confidenceLevel));
        assert CL > 0;

        //main loop
        Permutation randomElement;
        elements:
        while (sifted < CL) {
            //random element
            randomElement = random(source, randomGenerator);

            //let's try to represent it via our BSGS candidate
            StripContainer strip = strip(BSGSCandidate, randomElement);

            //this signals, whether we shall add new generator to our BSGS (not necessary a new point)
            boolean toAddNewGenerator = false;
            if (strip.terminationLevel < BSGSCandidate.size()) {
                // strip terminated earlier then complete:
                // this means, that corresponding randomElement extends an orbit of
                // BSGS[terminationLevel] element, i.e. there is a permutation (actually it is remainder)
                // that acts on base point of BSGS[terminationLevel] and maps it out of its orbit.
                // =>so we shall add a new generator at terminationLevel and recalculate its orbit
                toAddNewGenerator = true;
            } else if (!strip.remainder.isIdentity()) {
                //in this case, nontrivial remainder fixes all base points
                toAddNewGenerator = true;
                //so, we need also to extend our base with a new point

                //let's find some point that is not fixed by remainder
                for (int i = 0; i < length; ++i)
                    if (strip.remainder.newIndexOf(i) != i) {
                        // adding this point to BSGS (with empty stabilizers set, since it is a last point)
                        BSGSCandidate.add(new BSGSCandidateElement(i, new ArrayList<Permutation>(), new int[length]));
                        //here we can proceed, but we break
                        break;
                    }
            }

            if (toAddNewGenerator) {
                // we need to add a new generator
                // (note, that it can fix all old base points, but not necessary)

                //we do not know the index, so we shall add it to all elements (c'est la vie)
                for (int i = 1; i <= strip.terminationLevel; ++i) {
                    //add new generator
                    BSGSCandidate.get(i).stabilizerGenerators.add(strip.remainder);
                    //recalculate content
                    BSGSCandidate.get(i).recalculateOrbitAndSchreierVector();
                }

                //revert
                sifted = 0;
            } else {
                //our random element is already in BSGS
                //this increases the probability that our candidate is a real BSGS!
                ++sifted;
            }
        }
    }

    /**
     * Applies randomized version of Schreier-Sims algorithm to specified BSGS until the group order calculated
     * using this candidate is not equals to order specified; as result, specified BSGS candidate will be guarantied
     * BSGS. If specified order greater then the order of permutation group generated by specified BSGS candidate,
     * then the algorithm will fall in infinite loop.
     *
     * @param BSGSCandidate   BSGS candidate
     * @param groupOrder      order of a group
     * @param randomGenerator random generator
     * @throws cc.redberry.core.groups.permutations.InconsistentGeneratorsException if algorithm detects that specified
     *                                                                              generators are inconsistent (due to antisymmetries)
     * @see #RandomSchreierSimsAlgorithm(java.util.ArrayList, double, org.apache.commons.math3.random.RandomGenerator)
     */
    public static void RandomSchreierSimsAlgorithmForKnownOrder(ArrayList<BSGSCandidateElement> BSGSCandidate,
                                                                BigInteger groupOrder, RandomGenerator randomGenerator) {
        final int length = BSGSCandidate.get(0).stabilizerGenerators.get(0).degree();

        //source of randomness
        List<Permutation> source = new ArrayList<>(BSGSCandidate.get(0).stabilizerGenerators);
        randomness(source, DEFAULT_RANDOMNESS_EXTEND_TO_SIZE, DEFAULT_NUMBER_OF_RANDOM_REFINEMENTS, randomGenerator);
        //recalculate BSGSCandidate
        for (BSGSCandidateElement element : BSGSCandidate)
            element.recalculateOrbitAndSchreierVector();
        makeUseOfAllGenerators(BSGSCandidate);

        //main loop
        Permutation randomElement;
        elements:
        while (!groupOrder.equals(calculateOrder(BSGSCandidate))) {
            //random element
            randomElement = random(source, randomGenerator);

            //let's try to represent it via our BSGS candidate
            StripContainer strip = strip(BSGSCandidate, randomElement);

            //this signals, whether we shall add new generator to our BSGS (not necessary a new point)
            boolean toAddNewGenerator = false;
            if (strip.terminationLevel < BSGSCandidate.size()) {
                // strip terminated earlier then complete:
                // this means, that corresponding randomElement extends an orbit of
                // BSGS[terminationLevel] element, i.e. there is a permutation (actually it is remainder)
                // that acts on base point of BSGS[terminationLevel] and maps it out of its orbit.
                // =>so we shall add a new generator at terminationLevel and recalculate its orbit
                toAddNewGenerator = true;
            } else if (!strip.remainder.isIdentity()) {
                //in this case, nontrivial remainder fixes all base points
                toAddNewGenerator = true;
                //so, we need also to extend our base with a new point

                //let's find some point that is not fixed by remainder
                for (int i = 0; i < length; ++i)
                    if (strip.remainder.newIndexOf(i) != i) {
                        // adding this point to BSGS (with empty stabilizers set, since it is a last point)
                        BSGSCandidate.add(new BSGSCandidateElement(i, new ArrayList<Permutation>(), new int[length]));
                        //here we can proceed, but we break
                        break;
                    }
            }

            if (toAddNewGenerator) {
                // we need to add a new generator
                // (note, that it can fix all old base points, but not necessary)

                //we do not know the index, so we shall add it to all elements (c'est la vie)
                for (int i = 1; i <= strip.terminationLevel; ++i) {
                    //add new generator
                    BSGSCandidate.get(i).stabilizerGenerators.add(strip.remainder);
                    //recalculate content
                    BSGSCandidate.get(i).recalculateOrbitAndSchreierVector();
                }
            }
        }
    }

    /**
     * Calculates order of permutation group represented by specified BSGS.
     *
     * @param BSGSList BSGS
     * @return order of permutation group represented by specified BSGS
     */
    public static final BigInteger calculateOrder(List<? extends BSGSElement> BSGSList) {
        return calculateOrder(BSGSList, 0);
    }

    static final BigInteger calculateOrder(List<? extends BSGSElement> BSGSList, int from) {
        BigInteger order = BigInteger.ONE;
        final int size = BSGSList.size();
        for (int i = from; i < size; ++i)
            order = order.multiply(BigInteger.valueOf(BSGSList.get(i).orbitSize()));
        return order;
    }


    /**
     * Removes redundant elements from BSGS candidate. The algorithm have O(degree^5) complexity in the worst case.
     *
     * @param BSGSCandidate BSGS candidate
     */
    public static void removeRedundantGenerators(ArrayList<BSGSCandidateElement> BSGSCandidate) {
        if (BSGSCandidate.size() == 1)
            return;

        /* REMOVEGENS in Sec. 4.4.4 in [Holt05] IS WRONG!!! */

        //the following is correct
        for (int i = BSGSCandidate.size() - 2; i > 0; --i) {
            BSGSCandidateElement element = BSGSCandidate.get(i);
            //iterator over stabilizer generators
            ListIterator<Permutation> iterator = element.stabilizerGenerators.listIterator();
            //temp list of stabilizer with removed redundant elements
            ArrayList<Permutation> tempStabilizers = null;
            boolean removed = false;
            //current stabilizer element
            Permutation current;
            out:
            while (iterator.hasNext()) {
                current = iterator.next();
                if (current.isIdentity()) {
                    iterator.remove();
                    removed = true;
                    continue;
                }
                // if current belongs to next stabilizers, i.e. it fixes beta_i & belongs to next BSGS element,
                // then it cannot be removed; note that second condition is necessary,
                // while first is redundant (but rids from obviously unnecessary checks)!
                if (current.newIndexOf(element.basePoint) == element.basePoint
                        && BSGSCandidate.get(i + 1).stabilizerGenerators.contains(element))
                    continue;
                //<-so generator does not fix base point and do not belongs to next element
                //let's check whether it is redundant
                if (tempStabilizers == null) {
                    //make a copy of current stabilizers
                    tempStabilizers = new ArrayList<>(element.stabilizerGenerators);
                }
                tempStabilizers.remove(current);

                //if new stabilizers generate same group => then current generator is redundant
                if (Permutations.getOrbitSize(tempStabilizers, element.basePoint) == element.orbitSize()) {
                    //<!!! we must ensure that next stabilizer in chain is a subgroup of temp !!! >//
                    int[] subBase = getBaseAsArray(BSGSCandidate, i);
                    List<BSGSCandidateElement> _subBSGS = createRawBSGSCandidate(subBase, tempStabilizers);
                    if (_subBSGS.isEmpty()) {
                        assert calculateOrder(BSGSCandidate, i).intValue() != 1;
                        continue;
                    }
                    ArrayList<BSGSCandidateElement> subBSGS = (ArrayList) _subBSGS;
                    SchreierSimsAlgorithm(subBSGS);
                    if (!calculateOrder(BSGSCandidate, i).equals(calculateOrder(subBSGS)))
                        continue out;
                    for (Permutation stabGen : BSGSCandidate.get(i + 1).stabilizerGenerators)
                        if (!membershipTest(subBSGS, stabGen))
                            continue out;

                    iterator.remove();
                    removed = true;
                } else {
                    //if not, we need revert
                    tempStabilizers.add(current);
                }
            }
            //if something was removed, then we need to recalculate Schreier vector
            if (removed)
                element.recalculateOrbitAndSchreierVector();
        }
    }

    /**
     * Removes redundant base points from the ending of specified BSGS.
     *
     * @param BSGS BSGS
     */
    public static void removeRedundantBaseRemnant(ArrayList<BSGSCandidateElement> BSGS) {
        for (int i = BSGS.size() - 1; i >= 0; --i)
            if (BSGS.get(i).stabilizerGenerators.isEmpty())
                BSGS.remove(i);
            else  //we can break since this guaranties that all other points are not redundant
                break;

    }

    /**
     * Returns true if specified BSGS candidate is a real BSGS. Method uses a restricted version of Schreier-Sims
     * algorithm.
     *
     * @param BSGSCandidate BSGS candidate
     * @return true if specified BSGS candidate is a real BSGS and false otherwise
     */
    public static boolean isBSGS(List<? extends BSGSElement> BSGSCandidate) {
        if (BSGSCandidate.isEmpty())
            return true;
        //main loop
        BSGSElement currentElement;
        int index = BSGSCandidate.size() - 1;
        while (index >= 0) {
            currentElement = BSGSCandidate.get(index);
            //testing that H^i_{beta_i} = H^{(i+1)}


            // enumerating all generators of H^i_{beta_i} (see ORBITSTABILIZER)
            //    enumerating all betas, i.e. current orbit elements
            for (int indexInOrbit = 0, sizeOfOrbit = currentElement.orbitList.size();
                 indexInOrbit < sizeOfOrbit; ++indexInOrbit) {
                //current point in orbit
                int beta = currentElement.orbitList.get(indexInOrbit);
                //obtain u_{beta} - element that maps beta_i onto beta
                Permutation transversalOfBeta = currentElement.getTransversalOf(beta);

                //enumerating through all generators of current element
                for (Permutation stabilizer : currentElement.stabilizerGenerators) {
                    //obtain u_{beta^x} - element that maps beta_i onto beta^x
                    Permutation transversalOfBetaX =
                            currentElement.getTransversalOf(stabilizer.newIndexOf(beta));

                    //so, let's construct nontrivial u_{beta}*x*u_{beta^x}^{(-1)}
                    if (!transversalOfBeta.composition(stabilizer).equals(transversalOfBetaX)) {
                        //this is a nontrivial generator of H^i_{beta_i}
                        Permutation SchreierGenerator = transversalOfBeta.composition(stabilizer, transversalOfBetaX.inverse());
                        // in order to test whether this generator contained in H^(i+1), let's apply STRIP
                        StripContainer strip = strip(BSGSCandidate, SchreierGenerator);
                        //if STRIP gives a nontrivial result, then this is not a BSGS
                        if (strip.terminationLevel < BSGSCandidate.size() || !strip.remainder.isIdentity())
                            return false;
                    }
                }
            }
            --index;
        }
        return true;
    }

    /**
     * Returns true if specified BSGS candidate is a real BSGS with specified confidence level. Method uses a restricted
     * version of randomized Schreier-Sims algorithm.
     *
     * @param BSGSCandidate   BSGS candidate
     * @param confidenceLevel confidence level (0 < confidence level < 1)
     * @param randomGenerator random generator
     * @return true if specified BSGS candidate is a real BSGS and false otherwise
     */
    public static boolean isBSGS(List<? extends BSGSElement> BSGSCandidate, double confidenceLevel, RandomGenerator randomGenerator) {
        if (confidenceLevel > 1 || confidenceLevel < 0)
            throw new IllegalArgumentException("Confidence level must be between 0 and 1.");

        //source of randomness
        List<Permutation> source = new ArrayList<>(BSGSCandidate.get(0).stabilizerGenerators);
        randomness(source, DEFAULT_RANDOMNESS_EXTEND_TO_SIZE, DEFAULT_NUMBER_OF_RANDOM_REFINEMENTS, randomGenerator);
        source = new ArrayList<>(source);

        //counts the random elements sifted without change to BSGS
        int sifted = 0;
        int CL = (int) (-FastMath.log(2, 1 - confidenceLevel));
        assert CL > 0;

        //main loop
        Permutation randomElement;
        elements:
        while (sifted < CL) {
            //random element
            randomElement = random(source, randomGenerator);

            //let's try to represent it via our BSGS candidate
            StripContainer strip = strip(BSGSCandidate, randomElement);

            //this signals, whether we shall add new generator to our BSGS (not necessary a new point)
            boolean toAddNewGenerator = false;
            if (strip.terminationLevel < BSGSCandidate.size() || !strip.remainder.isIdentity()) {
                return false;
            }
            //our random element is already in BSGS
            //this increases the probability that our candidate is a real BSGS!
            ++sifted;

        }
        return true;
    }

    /**
     * Returns the number of elements in specified strong generating set.
     *
     * @param BSGS strong generating set
     * @return number of elements in specified strong generating set
     */
    public static long numberOfStrongGenerators(List<? extends BSGSElement> BSGS) {
        /* Since expected maximum number of generators
           in BSGS is about n*(n-1)/2, then, in order to avoid integer overflow, we use long, since for
           n ~ Integer.MAX_VALUE the corresponding number of elements an be up to ~ Long.MAX_VALUE / 2. */
        long num = 0;
        for (BSGSElement el : BSGS)
            num += el.stabilizerGenerators.size();
        return num;
    }

    /**
     * Swaps <i>i-th</i> and <i>(i+1)-th</i> points of specified BSGS. The details of the implementation can be
     * found in Sec. 4.4.7 of <b>[Holt05]</b> (see BASESWAP algorithm).
     *
     * @param BSGS BSGS
     * @param i    position of base point to swap with next point
     */
    public static void swapAdjacentBasePoints(ArrayList<BSGSCandidateElement> BSGS, int i) {
        if (i > BSGS.size() - 2)
            throw new IndexOutOfBoundsException();

        ArrayList<Permutation> newStabilizers;


        //i-th and (i+1)-th base points
        int ithBeta = BSGS.get(i).basePoint, jthBeta = BSGS.get(i + 1).basePoint;

        //computing size of orbit of beta_{i+1} under G^(i)
        int d = Permutations.getOrbitSize(BSGS.get(i).stabilizerGenerators, BSGS.get(i + 1).basePoint);
        //as we know |H| = s |G^(i+2)|, where s
        int s = (int) ((((long) BSGS.get(i).orbitSize()) * BSGS.get(i + 1).orbitSize()) / ((long) d));//avoid integer overflow

        //new stabilizers of G^(i+1)'
        //these stabilizers should fix beta_1, beta_2, ..., beta_(i-1), beta_(i+1)
        if (i == BSGS.size() - 2)
            newStabilizers = new ArrayList<>();
        else
            newStabilizers = new ArrayList<>(BSGS.get(i + 2).stabilizerGenerators);

        //allowed points
        BitArray allowedPoints = new BitArray(BSGS.get(0).degree());
        allowedPoints.setAll(BSGS.get(i).orbitList, true);
        allowedPoints.set(ithBeta, false);
        allowedPoints.set(jthBeta, false);

        //we shall store the orbit of ithBeta under new stabilizers in BSGSCandidateElement
        BSGSCandidateElement newOrbitStabilizer =
                new BSGSCandidateElement(ithBeta, newStabilizers, new int[BSGS.get(0).degree()]);

        //main loop
        main:
        while (newOrbitStabilizer.orbitSize() != s) {
            //this loop is redundant but helps to avoid unnecessary calculations of orbits in the main loop condition
            int nextBasePoint = -1;
            while ((nextBasePoint = allowedPoints.nextBit(++nextBasePoint)) != -1) {
                //transversal
                Permutation transversal = BSGS.get(i).getTransversalOf(nextBasePoint);
                int newIndexUnderInverse = transversal.newIndexOfUnderInverse(jthBeta);
                //check whether beta_{i+1}^(inverse transversal) belongs to orbit of G^{i+1}
                if (!BSGS.get(i + 1).belongsToOrbit(newIndexUnderInverse)) {
                    //then this transversal is bad and we can skip the orbit of this point under new stabilizers
                    IntArrayList toRemove = Permutations.getOrbitList(newStabilizers, nextBasePoint);
                    allowedPoints.setAll(toRemove, false);
                } else {
                    //<-ok this transversal is good
                    //we need an element in G^(4) that beta_{i+1}^element = beta_{i+1}^{inverse transversal}
                    //so that beta_{i+1} is fixed under product of element * transversal
                    //todo unnecessary composition can be carried out!
                    Permutation newStabilizer =
                            BSGS.get(i + 1).getTransversalOf(newIndexUnderInverse).composition(transversal);
                    //if this element was not yet seen
                    if (!newOrbitStabilizer.belongsToOrbit(newStabilizer.newIndexOf(ithBeta))) {
                        //newOrbitStabilizer have same reference!
                        newStabilizers.add(newStabilizer);
                        newOrbitStabilizer.recalculateOrbitAndSchreierVector();

                        IntArrayList toRemove = Permutations.getOrbitList(newStabilizers, nextBasePoint);
                        allowedPoints.setAll(toRemove, false);

                        continue main;
                    }
                }
            }
        }

        //swap base points (orbits and and Schreier vectors will be recalculated in constructors)
        BSGSCandidateElement ith = new BSGSCandidateElement(BSGS.get(i + 1).basePoint,
                BSGS.get(i).stabilizerGenerators, BSGS.get(i).SchreierVector);
        BSGSCandidateElement jth = new BSGSCandidateElement(BSGS.get(i).basePoint,
                newStabilizers, BSGS.get(i + 1).SchreierVector);
        BSGS.set(i, ith);
        BSGS.set(i + 1, jth);
    }


    /**
     * Changes the base of specified BSGS to specified new base using an algorithm with transpositions. The
     * algorithm guaranties that if initial base is [b1, b2, b3, ..., bk] and specified base is [a1, a2, a3, ..., al],
     * then the resulting base will look like  [a1, a2, a3, ...., al, b4, b7, ..., b19] with no any redundant base
     * points at the end (redundant point is point which corresponding stabilizer generators are empty) - this
     * achieves by invocation of {@link #removeRedundantBaseRemnant(java.util.ArrayList)} at the end of procedure.
     *
     * @param BSGS    BSGS
     * @param newBase new base
     */
    public static void rebaseWithTranspositions(ArrayList<BSGSCandidateElement> BSGS, int[] newBase) {
        for (int i = 0; i < newBase.length && i < BSGS.size(); ++i) {
            int newBasePoint = newBase[i];
            if (BSGS.get(i).basePoint != newBasePoint)
                changeBasePointWithTranspositions(BSGS, i, newBasePoint);
        }
        removeRedundantBaseRemnant(BSGS);
    }

    /**
     * Changes base of specified BSGS to specified new base using an algorithm with conjugations and transpositions.
     * The algorithm guaranties that if initial base is [b1, b2, b3, ..., bk] and specified base is [a1, a2, a3, ..., al],
     * then the resulting base will look like  [a1, a2, a3, ...., al, b4, b7, ..., b19] with no any redundant base
     * points at the end (redundant point is point which corresponding stabilizer generators are empty) - this
     * achieves by invocation of {@link #removeRedundantBaseRemnant(java.util.ArrayList)} at the end of procedure.
     *
     * @param BSGS    BSGS
     * @param newBase new base
     */
    public static void rebaseWithConjugationAndTranspositions(ArrayList<BSGSCandidateElement> BSGS, int[] newBase) {
        final int degree = BSGS.get(0).degree();
        //conjugating permutation
        Permutation conjugation = BSGS.get(0).stabilizerGenerators.get(0).getIdentity();

        int positionOfFirstChanged = -1;
        //first, lets proceed by swapping
        for (int i = 0; i < newBase.length && i < BSGS.size(); ++i) {
            //new base point image under conjugation
            int newBasePoint = conjugation.newIndexOfUnderInverse(newBase[i]);
            //early check
            if (BSGS.get(i).basePoint == newBasePoint)
                continue;

            if (positionOfFirstChanged == -1)
                positionOfFirstChanged = i;

            //check, whether new base point belongs to current orbit, i.e. there is some permutation in G
            //that maps current point onto new point
            if (BSGS.get(i).belongsToOrbit(newBasePoint)) {
                //we can simply conjugate this base element
                Permutation transversal = BSGS.get(i).getTransversalOf(newBasePoint);
                conjugation = transversal.composition(conjugation);
                continue;
            }

            //<- else, if new base point does not belong to current orbit we'll proceed as usual
            changeBasePointWithTranspositions(BSGS, i, newBasePoint);
        }

        //removing redundant now for performance
        removeRedundantBaseRemnant(BSGS);
        if (BSGS.size() <= positionOfFirstChanged)
            return;

        //conjugating base and strong generating set
        if (!conjugation.isIdentity()) {
            //inverse conjugation
            Permutation inverse = conjugation.inverse();
            ListIterator<BSGSCandidateElement> elementsIterator = BSGS.listIterator(positionOfFirstChanged);
            while (elementsIterator.hasNext()) {
                BSGSCandidateElement element = elementsIterator.next();
                //conjugating stabilizers
                ArrayList<Permutation> newStabilizers = new ArrayList<>(element.stabilizerGenerators.size());
                for (Permutation oldStabilizer : element.stabilizerGenerators)
                    newStabilizers.add(inverse.composition(oldStabilizer, conjugation));

                //conjugating base point
                int newBasePoint = conjugation.newIndexOf(element.basePoint);
                elementsIterator.set(
                        new BSGSCandidateElement(newBasePoint, newStabilizers, new int[degree]));
            }
        }
        removeRedundantBaseRemnant(BSGS);
    }

    /**
     * Changes <i>i-th</i> base point with a new value, by insertion redundant point and swapping.
     *
     * @param BSGS                 BSGS
     * @param oldBasePointPosition position of base point to change
     * @param newBasePoint         new base point
     */
    static void changeBasePointWithTranspositions(
            ArrayList<BSGSCandidateElement> BSGS, int oldBasePointPosition, int newBasePoint) {
        assert BSGS.get(oldBasePointPosition).basePoint != newBasePoint;
        final int degree = BSGS.get(0).degree();
        int insertionPosition = oldBasePointPosition + 1;
        insertion_points:
        for (; insertionPosition < BSGS.size(); ++insertionPosition) {
            for (Permutation permutation : BSGS.get(insertionPosition).stabilizerGenerators)
                if (permutation.newIndexOf(newBasePoint) != newBasePoint)
                    continue insertion_points;
            break;
        }

        if (insertionPosition == BSGS.size()) {
            //<- no element that fixes new base point
            BSGS.add(new BSGSCandidateElement(newBasePoint, new ArrayList<Permutation>(),
                    new int[degree]));
        } else if (BSGS.get(insertionPosition).basePoint != newBasePoint) {
            //<- we've found an element (call it pivot) that stabilizes all
            // points before pivot and also a new base point
            // we can insert a new base point before pivot, and still pivot will fix all points before pivot

            //stabilizers of new base point inserted are same to stabilizers of pivot
            BSGS.add(insertionPosition,
                    new BSGSCandidateElement(newBasePoint,
                            new ArrayList<>(BSGS.get(insertionPosition).stabilizerGenerators),
                            new int[degree]));
        }
        //then just swap
        //note, that if insertionPosition <= i then no any swap needed
        while (insertionPosition > oldBasePointPosition)
            swapAdjacentBasePoints(BSGS, --insertionPosition);
    }

    /**
     * Changes base of specified BSGS to specified new base by construction of a new BSGS with known base using
     * randomized Schreier-Sims algorithm {@link #RandomSchreierSimsAlgorithmForKnownOrder(java.util.ArrayList, java.math.BigInteger, org.apache.commons.math3.random.RandomGenerator)}.
     * The algorithm guaranties that if initial base is [b1, b2, b3, ..., bk] and specified base is [a1, a2, a3, ..., al],
     * then the resulting base will look like  [a1, a2, a3, ...., al, x, y, ..., z] with no any redundant base
     * points at the end (redundant point is point which corresponding stabilizer generators are empty) but with some
     * additional points introduced if specified new base was not anought.
     *
     * @param BSGS    BSGS
     * @param newBase new base
     */
    public static void rebaseFromScratch(ArrayList<BSGSCandidateElement> BSGS, int[] newBase) {
        List<BSGSCandidateElement> newBSGS = createRawBSGSCandidate(newBase, BSGS.get(0).stabilizerGenerators);
        if (newBSGS.isEmpty())//todo add new base points here!!!!
            return; //new base is fixed by all group generators; nothing to do
        BigInteger order = calculateOrder(BSGS);
        RandomSchreierSimsAlgorithmForKnownOrder((ArrayList) newBSGS, order, CC.getRandomGenerator());
        int i = 0;
        for (; i < newBSGS.size() && i < BSGS.size(); ++i)
            BSGS.set(i, newBSGS.get(i));
        if (i < newBSGS.size())
            for (; i < newBSGS.size(); ++i)
                BSGS.add(newBSGS.get(i));
        if (i < BSGS.size())
            for (int j = BSGS.size() - 1; j >= i; --j)
                BSGS.remove(j);
    }

    /**
     * Changes base of specified BSGS to the specified base. The algorithm heuristically choose the algorithm of base
     * change.
     *
     * @param BSGS    BSGS
     * @param newBase new base
     * @see #rebaseWithTranspositions(java.util.ArrayList, int[])
     * @see #rebaseWithConjugationAndTranspositions(java.util.ArrayList, int[])
     * @see #rebaseFromScratch(java.util.ArrayList, int[])
     */
    public static void rebase(ArrayList<BSGSCandidateElement> BSGS, int[] newBase) {
        rebaseWithConjugationAndTranspositions(BSGS, newBase);
    }

    //------------------------------ FACTORIES --------------------------------------------//


    /**
     * Returns direct product of two groups given by their BSGS. This product is organized as follows:
     * the initial segment of each permutation is equal to permutation taken from first group, while the rest is taken
     * from the second.
     *
     * @param bsgs1 BSGS of first group
     * @param bsgs1 BSGS of second group
     * @return direct product first group × second group
     */
    public static ArrayList<BSGSElement> directProduct(List<? extends BSGSElement> bsgs1, List<? extends BSGSElement> bsgs2) {
        int degree1 = bsgs1.get(0).degree(), degree2 = bsgs2.get(0).degree();
        int deg = degree1 + degree2;

        //adjust bsgs of group
        ArrayList<BSGSElement> groupBsgsExtended = new ArrayList<>(bsgs2.size());
        for (BSGSElement element : bsgs2) {
            ArrayList<Permutation> stabilizers = new ArrayList<>(element.stabilizerGenerators.size());
            for (Permutation p : element.stabilizerGenerators)
                stabilizers.add(p.extendBefore(deg));

            int[] SchreierVector = new int[deg];
            Arrays.fill(SchreierVector, 0, degree1, -2);
            System.arraycopy(element.SchreierVector, 0, SchreierVector, degree1, degree2);
            IntArrayList orbit = new IntArrayList(element.orbitList.size());
            for (int i = element.orbitList.size() - 1; i >= 0; --i)
                orbit.add(element.orbitList.get(i) + degree1);
            groupBsgsExtended.add(new BSGSElement(element.basePoint + degree1, stabilizers, SchreierVector, orbit));
        }

        ArrayList<BSGSElement> bsgs = new ArrayList<>(bsgs1.size() + bsgs2.size());
        //adjust bsgs of this
        for (BSGSElement element : bsgs1) {
            ArrayList<Permutation> stabilizers = new ArrayList<>(element.stabilizerGenerators.size());
            for (Permutation p : element.stabilizerGenerators)
                stabilizers.add(p.extendAfter(deg));
            stabilizers.addAll(groupBsgsExtended.get(0).stabilizerGenerators);
            int[] SchreierVector = new int[deg];
            System.arraycopy(element.SchreierVector, 0, SchreierVector, 0, degree1);
            Arrays.fill(SchreierVector, degree1, deg, -2);
            bsgs.add(new BSGSElement(element.basePoint, stabilizers, SchreierVector, element.orbitList));
        }
        bsgs.addAll(groupBsgsExtended);

        return bsgs;
    }

    /**
     * Calculates a union of specified groups.
     *
     * @param bsgs1 base and strong generating set of first group
     * @param bsgs2 base and strong generating set of second group
     * @return base and strong generating set of the union
     */
    public static ArrayList<? extends BSGSElement> union(ArrayList<? extends BSGSElement> bsgs1,
                                                         ArrayList<? extends BSGSElement> bsgs2) {
        if (bsgs2.isEmpty())
            return bsgs1;
        if (bsgs1.isEmpty())
            return bsgs2;

        int[] base1 = getBaseAsArray(bsgs1),
                base2 = getBaseAsArray(bsgs2);
        int[] base = MathUtils.intSetUnion(base1, base2);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.addAll(bsgs1.get(0).stabilizerGenerators);
        generators.addAll(bsgs1.get(0).stabilizerGenerators);

        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(base, generators);

        SchreierSimsAlgorithm(bsgs);
        return bsgs;
    }

    /**
     * Creates an empty BSGS structure with single zero base point and one identity stabilizer.
     *
     * @param degree group degree
     * @return empty BSGS structure with single zero base point and one identity stabilizer
     */
    public static ArrayList<BSGSElement> createEmptyBSGS(int degree) {
        ArrayList<BSGSElement> bsgs = new ArrayList<>();
        ArrayList<Permutation> gens = new ArrayList<>();
        gens.add(Permutations.createIdentityPermutation(degree));
        bsgs.add(new BSGSCandidateElement(0, gens, new int[degree]).asBSGSElement());
        return bsgs;
    }

    /**
     * This value is an upper bound of degrees, which we consider as "small".
     */
    public static final int SMALL_DEGREE_THRESHOLD = 100;
    /**
     * Cached BSGS structures for symmetric groups.
     */
    private static final ArrayList<BSGSElement>[] CACHED_SYMMETRIC_GROUPS = new ArrayList[SMALL_DEGREE_THRESHOLD];
    /**
     * Cached BSGS structures for antisymmetric groups.
     */
    private static final ArrayList<BSGSElement>[] CACHED_ANTISYMMETRIC_GROUPS = new ArrayList[SMALL_DEGREE_THRESHOLD];
    /**
     * Cached BSGS structures for alternating groups.
     */
    private static final ArrayList<BSGSElement>[] CACHED_ALTERNATING_GROUPS = new ArrayList[SMALL_DEGREE_THRESHOLD];

    /**
     * Creates base and strong generating set of alternating group of specified degree. Alternating group of degree
     * smaller then {@link #SMALL_DEGREE_THRESHOLD} will provide zero-time access to all transversals in
     * each stabilizer; group with larger degree will provide <i>log(size of orbit)</i> access. Additionally, small degree
     * group with fixed degree will be constructed once (at the first invocation of this method with specified
     * degree) and then cached, so second invocation of this method with same degree will return same reference.
     *
     * @param degree group degree
     * @return base and strong generating set of symmetric group
     */
    public static ArrayList<BSGSElement> createAlternatingGroupBSGS(final int degree) {
        if (degree == 0)
            throw new IllegalArgumentException("Degree = 0.");

        /* For alternating group we can construct BSGS explicitly without call of Schreier-Sims algorithm */

        //For small degree groups we'll construct all BSGS elements with a "quick" access to all transversals
        if (degree <= SMALL_DEGREE_THRESHOLD) {
            ArrayList<BSGSElement> bsgs = CACHED_ALTERNATING_GROUPS[degree - 1];
            if (bsgs == null) {
                bsgs = createAlternatingGroupBSGSForSmallDegree(degree);
                CACHED_ALTERNATING_GROUPS[degree - 1] = bsgs;
            }
            return bsgs;
        }

        //For groups with large degree we'll construct all BSGS elements with a log(degree) access to all transversals
        return createAlternatingGroupBSGSForLargeDegree(degree);
    }

    static ArrayList<BSGSElement> createAlternatingGroupBSGSForSmallDegree(final int degree) {
        if (degree < 3)
            return createEmptyBSGS(degree);

        //For small groups we'll construct all BSGS elements with a "quick" access to all transversals, i.e.
        // each stabilizer in chain will contain all required transversals. This involves to store
        // ~(degree - 1)*degree/2 stabilizers


        // Alt(n) generated by two elements:
        // if degree is odd: p1 = (012) and (0,1,2,...,degree) (in cycle notation)
        // if degree is even: p1 = (012) and (1,2,...,degree) (in cycle notation)

        ArrayList<BSGSElement> bsgs = new ArrayList<>();
        ArrayList<Permutation> stabilizers = new ArrayList<>(degree);
        for (int i = 0; i < degree - 2; ++i) {
            IntArrayList orbit = new IntArrayList(degree - i);
            for (int j = i; j < degree; ++j)
                orbit.add(j);

            int[] SchreierVector = new int[degree];
            Arrays.fill(SchreierVector, -2);
            SchreierVector[i] = -1;

            //creating (012)
            int[] perm = new int[degree];
            for (int j = 1; j < i; ++j)
                perm[j] = j;

            for (int j = i + 3; j < degree; ++j)
                perm[j] = j;

            perm[i] = i + 1;
            perm[i + 1] = i + 2;
            perm[i + 2] = i;

            //filling Schreier vector for (012)
            SchreierVector[i + 1] = stabilizers.size();
            stabilizers.add(new PermutationOneLine(perm));
            SchreierVector[i + 2] = stabilizers.size();
            stabilizers.add(stabilizers.get(0).pow(2));

            int inverseParity = 1 - ((degree - i) % 2);
            Permutation base = inverseParity == 1 ? stabilizers.get(0) : stabilizers.get(0).getIdentity();
            for (int k = 3; k < degree - i; ++k) {
                perm = new int[degree];
                for (int j = 1; j <= i; ++j)
                    perm[j] = j;

                int j;
                for (j = i + inverseParity; j < degree - k + inverseParity; ++j)
                    perm[j] = j + k - inverseParity;

                for (int t = 0; j < degree; ++j, ++t)
                    perm[j] = i + inverseParity + t;

                SchreierVector[i + k] = stabilizers.size();
                stabilizers.add(base.composition(new PermutationOneLine(perm)));
            }
            bsgs.add(new BSGSElement(i, new ArrayList<>(stabilizers), SchreierVector, orbit));
            stabilizers.clear();
        }
        return bsgs;
    }

    static ArrayList<BSGSElement> createAlternatingGroupBSGSForLargeDegree(final int degree) {
        if (degree < 3)
            return createEmptyBSGS(degree);

        //For groups with large degree we'll construct all BSGS elements with a log(degree) access to all transversals

        // Alt(n) generated by two elements:
        // if degree is odd: p1 = (012) and (0,1,2,...,degree) (in cycle notation)
        // if degree is even: p1 = (012) and (1,2,...,degree) (in cycle notation)

        ArrayList<BSGSElement> bsgs = new ArrayList<>();
        ArrayList<Permutation> stabilizers = new ArrayList<>(degree);
        for (int i = 0; i < degree - 2; ++i) {

            //creating (012)
            int[] perm = new int[degree];
            for (int j = 1; j < i; ++j)
                perm[j] = j;

            for (int j = i + 3; j < degree; ++j)
                perm[j] = j;

            perm[i] = i + 1;
            perm[i + 1] = i + 2;
            perm[i + 2] = i;

            //filling Schreier vector for (012)
            stabilizers.add(new PermutationOneLine(perm));
            stabilizers.add(stabilizers.get(0).pow(2));

            int inverseParity = 1 - ((degree - i) % 2);
            Permutation base = inverseParity == 1 ? stabilizers.get(0) : stabilizers.get(0).getIdentity();
            int k;
            //for (int k = 3; k < degree - i; ++k) {
            for (int r = degree - i - 3; r > 0; r /= 2) {
                k = r + 2;
                perm = new int[degree];
                for (int j = 1; j <= i; ++j)
                    perm[j] = j;

                int j;
                for (j = i + inverseParity; j < degree - k + inverseParity; ++j)
                    perm[j] = j + k - inverseParity;

                for (int t = 0; j < degree; ++j, ++t)
                    perm[j] = i + inverseParity + t;

                stabilizers.add(base.composition(new PermutationOneLine(perm)));
            }
            bsgs.add(new BSGSCandidateElement(i, new ArrayList<>(stabilizers), new int[degree]).asBSGSElement());
            stabilizers.clear();
        }
        return bsgs;
    }

    /**
     * Creates base and strong generating set of symmetric group of specified degree. Symmetric group of degree
     * smaller then {@link #SMALL_DEGREE_THRESHOLD} will provide zero-time access to all transversals in
     * each stabilizer; group with larger degree will provide <i>log(size of orbit)</i> access. Additionally, small degree
     * group with fixed degree will be constructed once (at the first invocation of this method with specified
     * degree) and then cached, so second invocation of this method with same degree will return same reference.
     *
     * @param degree group degree
     * @return base and strong generating set of symmetric group
     */
    public static ArrayList<BSGSElement> createSymmetricGroupBSGS(final int degree) {
        if (degree == 0)
            throw new IllegalArgumentException("Degree = 0.");

        /* For symmetric group we can construct BSGS explicitly without call of Schreier-Sims algorithm */

        //For small degree groups we'll construct all BSGS elements with a "quick" access to all transversals
        if (degree <= SMALL_DEGREE_THRESHOLD) {
            ArrayList<BSGSElement> bsgs = CACHED_SYMMETRIC_GROUPS[degree - 1];
            if (bsgs == null) {
                bsgs = createSymmetricGroupBSGSForSmallDegree(degree);
                CACHED_SYMMETRIC_GROUPS[degree - 1] = bsgs;
            }
            return bsgs;
        }

        //For groups with large degree we'll construct all BSGS elements with a log(degree) access to all transversals
        return createSymmetricGroupBSGSForLargeDegree(degree);
    }

    static ArrayList<BSGSElement> createSymmetricGroupBSGSForSmallDegree(final int degree) {
        //For small groups we'll construct all BSGS elements with a "quick" access to all transversals, i.e.
        // each stabilizer in chain will contain all required transversals. This involves to store
        // (degree - 1)*degree/2 stabilizers

        ArrayList<BSGSElement> bsgs = new ArrayList<>(degree - 1);
        for (int i = 0; i < degree - 1; ++i) {

            //calculating orbit of i-th base point
            IntArrayList orbit = new IntArrayList(degree - i);
            int j = i;
            for (; j < degree; ++j)
                orbit.add(j);

            //calculating stabilizers
            final Permutation[] stabilizers = new Permutation[degree - i - 1];


            int[] SchreierVector = new int[degree];
            Arrays.fill(SchreierVector, -2);
            SchreierVector[i] = -1;

            int c = 0, permutation[], k;
            for (j = i + 1; j < degree; ++j) {
                permutation = new int[degree];
                for (k = 1; k < degree; ++k)
                    permutation[k] = k;
                permutation[j] = i;
                permutation[i] = j;
                stabilizers[c] = new PermutationOneLine(permutation);
                SchreierVector[j] = c++;
            }

            BSGSElement element = new BSGSElement(i, Arrays.asList(stabilizers), SchreierVector, orbit);
            bsgs.add(element);
        }
        return bsgs;
    }

    static ArrayList<BSGSElement> createSymmetricGroupBSGSForLargeDegree(final int degree) {
        //For groups with large degree we'll construct all BSGS elements with a log(degree) access to all transversals

        ArrayList<BSGSElement> bsgs = new ArrayList<>(degree - 1);
        for (int i = 0; i < degree - 1; ++i) {

            //calculating orbit of i-th base point
            IntArrayList orbit = new IntArrayList(degree - i);
            int j = i;
            for (; j < degree; ++j)
                orbit.add(j);

            //calculating stabilizers
            final ArrayList<Permutation> stabilizers = new ArrayList<>((int) (FastMath.log(degree - i) / FastMath.log(2)));

            //first stabilizer is transposition
            int[] permutation = new int[degree];
            for (j = 1; j < degree; ++j)
                permutation[j] = j;
            permutation[i] = i + 1;
            permutation[i + 1] = i;
            stabilizers.add(new PermutationOneLine(permutation));

            int image, k, l;
            //provide log(size of orbit) access
            for (j = degree - i - 1; j > 0; j /= 2) {
                //for each element in orbit
                image = i + j;
                permutation = new int[degree];
                //all points before base point are fixed
                for (k = 0; k < i; ++k)
                    permutation[k] = k;
                //the rest of permutation should map i to i + j; we'll do this using cycles
                l = 0;
                for (; l < degree - image; ++k, ++l)
                    permutation[k] = image + l;
                l = 0;
                for (; k < degree; ++k)
                    permutation[k] = i + (l++);

                stabilizers.add(new PermutationOneLine(permutation));
            }

            //Collections.reverse(stabilizers);

            BSGSElement element = new BSGSCandidateElement(i, stabilizers, new int[degree]).asBSGSElement();
            bsgs.add(element);
        }
        return bsgs;
    }

    /**
     * Creates base and strong generating set of symmetric group of specified degree, where all odd permutations are
     * antisymmetries. Symmetric group of degree smaller then {@link #SMALL_DEGREE_THRESHOLD} will provide zero-time
     * access to all transversals in each stabilizer; group with larger degree will provide <i>log(size of orbit)</i>
     * access. Additionally, small degree group with fixed degree will be constructed once (at the first invocation of
     * this method with specified degree) and then cached, so second invocation of this method with same degree will
     * return same reference.
     *
     * @param degree group degree
     * @return base and strong generating set of symmetric group
     */
    public static ArrayList<BSGSElement> createAntisymmetricGroupBSGS(final int degree) {
        if (degree == 0)
            throw new IllegalArgumentException("Degree = 0.");

        /* For symmetric group we can construct BSGS explicitly without call of Schreier-Sims algorithm */

        //For small degree groups we'll construct all BSGS elements with a "quick" access to all transversals
        if (degree <= SMALL_DEGREE_THRESHOLD) {
            ArrayList<BSGSElement> bsgs = CACHED_ANTISYMMETRIC_GROUPS[degree - 1];
            if (bsgs == null) {
                bsgs = convertToAntisymmetric(createSymmetricGroupBSGS(degree));
                CACHED_ANTISYMMETRIC_GROUPS[degree - 1] = bsgs;
            }
            return bsgs;
        }

        //For groups with large degree we'll construct all BSGS elements with a log(degree) access to all transversals
        return convertToAntisymmetric(createSymmetricGroupBSGS(degree));
    }

    private static ArrayList<BSGSElement> convertToAntisymmetric(ArrayList<BSGSElement> symmetricGroup) {
        ArrayList<BSGSCandidateElement> bsgs = asBSGSCandidatesList(symmetricGroup);
        for (BSGSCandidateElement c : bsgs) {
            ListIterator<Permutation> stabs = c.getStabilizerGeneratorsReference().listIterator();
            while (stabs.hasNext()) {
                Permutation p = stabs.next();
                if (p.parity() == 1)
                    stabs.set(new PermutationOneLine(true, p.oneLine()));
            }
        }
        return asBSGSList(bsgs);
    }

    /**
     * Makes a mutable copy of BSGS.
     *
     * @param BSGS BSGS
     * @return mutable copy of BSGS
     */
    public static ArrayList<BSGSCandidateElement> asBSGSCandidatesList(List<? extends BSGSElement> BSGS) {
        ArrayList<BSGSCandidateElement> BSGSCandidates = new ArrayList<>(BSGS.size());
        for (BSGSElement element : BSGS)
            BSGSCandidates.add(element.asBSGSCandidateElement());
        return BSGSCandidates;
    }

    /**
     * Makes an immutable copy of BSGS.
     *
     * @param BSGSCandidate BSGS
     * @return immutable copy of BSGS
     */
    public static ArrayList<BSGSElement> asBSGSList(List<? extends BSGSElement> BSGSCandidate) {
        ArrayList<BSGSElement> BSGS = new ArrayList<>(BSGSCandidate.size());
        for (BSGSElement element : BSGSCandidate)
            BSGS.add(element.asBSGSElement());
        return BSGS;
    }

    /**
     * Returns base represented as array
     *
     * @param BSGS BSGS
     * @return base represented as array
     */
    public static int[] getBaseAsArray(final List<? extends BSGSElement> BSGS) {
        return getBaseAsArray(BSGS, 0);
    }

    static int[] getBaseAsArray(final List<? extends BSGSElement> BSGS, int from) {
        int[] base = new int[BSGS.size()];
        for (int i = from, size = BSGS.size(); i < size; ++i)
            base[i] = BSGS.get(i).basePoint;
        return base;
    }

    /**
     * Returns a deep copy of specified list
     *
     * @param BSGSCandidate
     * @return deep copy of specified list
     */
    public static ArrayList<BSGSCandidateElement> clone(List<BSGSCandidateElement> BSGSCandidate) {
        ArrayList<BSGSCandidateElement> copy = new ArrayList<>(BSGSCandidate);
        ListIterator<BSGSCandidateElement> it = copy.listIterator();
        while (it.hasNext())
            it.set(it.next().clone());
        return copy;
    }

    //------------------------------ UTIL ROUTINES --------------------------------------------//

    /**
     * Checks whether all permutations have same size and throws exception if not all permutations have same size.
     *
     * @param generators permutations
     * @throws IllegalArgumentException if not all permutations have same length
     */
    public static void checkGenerators(final Permutation... generators) {
        if (!checkGeneratorsBoolean(generators))
            throw new IllegalArgumentException("Generators of different sizes.");
    }

    /**
     * Checks whether all permutations have same size and throws exception if not all permutations have same size.
     *
     * @param generators permutations
     * @throws IllegalArgumentException if not all permutations have same length
     */
    public static void checkGenerators(final List<Permutation> generators) {
        if (!checkGeneratorsBoolean(generators))
            throw new IllegalArgumentException("Generators of different degrees.");
    }

    /**
     * Checks whether all permutations have same size and returns the result.
     *
     * @param generators permutations
     * @return true if all permutations have same length, false otherwise
     */
    public static boolean checkGeneratorsBoolean(final Permutation... generators) {
        return checkGeneratorsBoolean(Arrays.asList(generators));
    }

    /**
     * Checks whether all permutations have same size and returns the result.
     *
     * @param generators permutations
     * @return true if all permutations have same length, false otherwise
     */
    public static boolean checkGeneratorsBoolean(final List<Permutation> generators) {
        for (int i = 1, size = generators.size(); i < size; ++i)
            if (generators.get(i - 1).degree() != generators.get(i).degree())
                return false;
        return true;
    }
}
