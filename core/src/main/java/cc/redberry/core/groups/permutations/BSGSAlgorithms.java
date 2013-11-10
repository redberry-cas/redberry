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
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.groups.permutations.RandomPermutation.*;

/**
 * Factory methods to create base and strong generating set.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see BSGSCandidateElement
 */
public class BSGSAlgorithms {

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
     * Creates a raw BSGS candidate represented as list. This method simply takes all distinct points that can be
     * mapped onto another points under any of generators.
     *
     * @param generators group generators
     * @return raw BSGS candidate
     * @throws IllegalArgumentException if not all permutations have same length
     */
    public static List<BSGSCandidateElement> createRawBSGSCandidate(final Permutation... generators) {
        if (generators.length == 0)
            return Collections.EMPTY_LIST;
        checkGenerators(generators);

        final int length = generators[0].length();

        //first let's find a "proto-base" - a set of points that cannot be fixed by any of specified generators
        //and a "proto-BSGS" corresponding to this base

        //at the moment our "proto-base" will contain only one point
        int firstBasePoint = -1;

        //we try to find such a point that is not fixed at least by one of the generators
        for (Permutation permutation : generators)
            for (int i = 0; i < length; ++i)
                if (permutation.newIndexOf(i) != i) {
                    firstBasePoint = i;
                    break;
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
        BSGS.add(new BSGSCandidateElement(firstBasePoint, new ArrayList<>(Arrays.asList(generators)), new int[length]));

        //make use all unused generators
        makeUseOfAllGenerators(BSGS);
        return BSGS;
    }

    /**
     * Creates a raw BSGS candidate represented as list. This method simply adds to {@code knownBase} all distinct
     * points that can be mapped onto another points under any of generators. Those points in {@code knownBase} that are
     * fixed by all generators will not be taken into account.
     *
     * @param knownBase  some proposed base points
     * @param generators group generators
     * @return raw BSGS candidate
     * @throws IllegalArgumentException if not all permutations have same length
     */
    public static List<BSGSCandidateElement> createRawBSGSCandidate(final int[] knownBase, final Permutation... generators) {
        if (generators.length == 0)
            return Collections.EMPTY_LIST;
        checkGenerators(generators);

        final int length = generators[0].length();

        // first, lets remove unnecessary base points, i.e. such points, that are fixed by all generators

        IntArrayList base = new IntArrayList(knownBase.clone());

        //we try to find such a point that is not fixed at least by one of the generators
        for (int i = base.size() - 1; i >= 0; --i) {
            for (Permutation permutation : generators)
                if (permutation.newIndexOf(i) != i)
                    continue;
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
                BSGS.add(new BSGSCandidateElement(base.get(i), new ArrayList<>(Arrays.asList(generators)), new int[length]));
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
     * <p/>
     * <p>
     * The underlying code organized as follows:
     * <pre><code>
     * List&lt;BSGSCandidateElement&gt; BSGSCandidate = createRawBSGSCandidate(generators);
     * if (BSGSCandidate.isEmpty())
     *    return Collections.EMPTY_LIST;
     * SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
     * return asBSGSList(BSGSCandidate);
     * </code>
     * </pre>
     * </p>
     *
     * @param generators a set of group generators
     * @return BSGS represented as array of its element
     * @throws InconsistentGeneratorsException
     *                                  if permutations are inconsistent
     * @throws IllegalArgumentException if not all permutations have same length
     * @see #SchreierSimsAlgorithm(java.util.ArrayList)
     */
    public static List<BSGSElement> createBSGSList(final Permutation... generators) {
        List<BSGSCandidateElement> BSGSCandidate = createRawBSGSCandidate(generators);
        if (BSGSCandidate.isEmpty())
            return Collections.EMPTY_LIST;
        SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
        return asBSGSList(BSGSCandidate);
    }

    /**
     * Creates BSGS using Schreier-Sims algorithm. Specified base will be extended if necessary.
     * <p/>
     * <p>
     * The underlying code organized as follows:
     * <pre><code>
     * List&lt;BSGSCandidateElement&gt; BSGSCandidate = createRawBSGSCandidate(knownBase, generators);
     * if (BSGSCandidate.isEmpty())
     *    return Collections.EMPTY_LIST;
     * SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
     * return asBSGSList(BSGSCandidate);
     * </code>
     * </pre>
     * </p>
     *
     * @param generators a set of group generators
     * @param knownBase  proposed base points
     * @return BSGS represented as array of its element
     * @throws InconsistentGeneratorsException
     *                                  if permutations are inconsistent
     * @throws IllegalArgumentException if not all permutations have same length
     * @see #SchreierSimsAlgorithm(java.util.ArrayList)
     */
    public static List<BSGSElement> createBSGSList(final int[] knownBase, final Permutation... generators) {
        List<BSGSCandidateElement> BSGSCandidate = createRawBSGSCandidate(knownBase, generators);
        if (BSGSCandidate.isEmpty())
            return Collections.EMPTY_LIST;
        SchreierSimsAlgorithm((ArrayList) BSGSCandidate);
        return asBSGSList(BSGSCandidate);
    }

    /**
     * If some of generators fixes all base points, then, this method will find a new point that is not fixed by this
     * generator and add this point to specified BSGS candidate.
     *
     * @param BSGSCandidate a BSGS candidate
     */
    public static void makeUseOfAllGenerators(List<BSGSCandidateElement> BSGSCandidate) {
        //all group generators
        List<Permutation> generators = BSGSCandidate.get(0).stabilizerGenerators;
        if (generators.isEmpty())
            return;
        final int length = generators.get(0).length();
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
     * Applies Schreier-Sims algorithm for BSGS candidate and complete it if necessary, so, as a result the specified
     * BSGS candidate is guarantied to be BSGS. The algorithm described as SCHREIERSIMS in Sec. 4.4.1 of <b>[Holt05]</b>.
     *
     * @param BSGSCandidate BSGS candidate
     */
    public static void SchreierSimsAlgorithm(ArrayList<BSGSCandidateElement> BSGSCandidate) {
        if (BSGSCandidate.isEmpty())
            return;
        final int length = BSGSCandidate.get(0).stabilizerGenerators.get(0).length();
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
                        Permutation SchreierGenerator = transversalOfBeta.composition(stabilizer)
                                .composition(transversalOfBetaX.inverse());

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
     * Applies random Schreier-Sims algorithm for BSGS candidate. Algorithm ddo not complete candidate if necessary,
     * so, in order to verify that specified BSGS candidate became a BSGS one should apply
     * {@link #SchreierSimsAlgorithm(java.util.ArrayList)} on the result.
     * The algorithm described as RANDOMSCHREIER in Sec. 4.4.5 of <b>[Holt05]</b>.
     *
     * @param BSGSCandidate   BSGS candidate
     * @param confidenceLevel confidence level (0 < confidence level < 1)
     * @param randomGenerator random generator
     */
    public static void RandomSchreierSimsAlgorithm(ArrayList<BSGSCandidateElement> BSGSCandidate,
                                                   double confidenceLevel, RandomGenerator randomGenerator) {
        if (confidenceLevel > 1 || confidenceLevel < 0)
            throw new IllegalArgumentException("Confidence level must be between 0 and 1.");

        final int length = BSGSCandidate.get(0).stabilizerGenerators.get(0).length();

        //source of randomness
        List<Permutation> source = BSGSCandidate.get(0).stabilizerGenerators;
        randomness(source, DEFAULT_RANDOMNESS_EXTEND_TO_SIZE, DEFAULT_NUMBER_OF_RANDOM_REFINEMENTS, randomGenerator);
        source = new ArrayList<>(source);
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
     * Applies random Schreier-Sims algorithm for BSGS candidate if resulting group order is known
     * and complete it if necessary, so, as a result the specified BSGS candidate is guarantied to be BSGS.
     * {@link #SchreierSimsAlgorithm(java.util.ArrayList)} on the result.
     * The algorithm described as RANDOMSCHREIER in Sec. 4.4.5 of <b>[Holt05]</b>.
     *
     * @param BSGSCandidate   BSGS candidate
     * @param groupOrder      order of a group
     * @param randomGenerator random generator
     */
    public static void RandomSchreierSimsAlgorithmForKnownOrder(ArrayList<BSGSCandidateElement> BSGSCandidate,
                                                                BigInteger groupOrder, RandomGenerator randomGenerator) {
        final int length = BSGSCandidate.get(0).stabilizerGenerators.get(0).length();

        //source of randomness
        List<Permutation> source = BSGSCandidate.get(0).stabilizerGenerators;
        randomness(source, DEFAULT_RANDOMNESS_EXTEND_TO_SIZE, DEFAULT_NUMBER_OF_RANDOM_REFINEMENTS, randomGenerator);
        source = new ArrayList<>(source);
        //recalculate BSGSCandidate
        for (BSGSCandidateElement element : BSGSCandidate)
            element.recalculateOrbitAndSchreierVector();
        makeUseOfAllGenerators(BSGSCandidate);

        //main loop
        Permutation randomElement;
        elements:
        while (!groupOrder.equals(getOrder(BSGSCandidate))) {
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

    static final BigInteger getOrder(List<? extends BSGSElement> BSGSList) {
        BigInteger order = BigInteger.ONE;
        for (BSGSElement element : BSGSList)
            order = order.multiply(BigInteger.valueOf(element.orbitSize()));
        return order;
    }

    /**
     * Removes redundant elements from BSGS candidate (actually removes those elements which are easy to determine
     * that they are redundant in this BSGS candidate). For more info see REMOVEGENS in Sec. 4.4.4 in [Holt05]
     *
     * @param BSGSCandidate BSGS candidate
     */
    public static void removeRedundantGenerators(ArrayList<BSGSCandidateElement> BSGSCandidate) {
        if (BSGSCandidate.size() == 1)
            return;

        for (int i = BSGSCandidate.size() - 2; i >= 0; --i) {
            BSGSCandidateElement element = BSGSCandidate.get(i);
            //iterator over stabilizer generators
            ListIterator<Permutation> iterator = element.stabilizerGenerators.listIterator();
            //temp list of stabilizer with removed redundant elements
            List<Permutation> newStabilizers = null;
            boolean removed = false;
            //current stabilizer element
            Permutation current;
            while (iterator.hasNext()) {
                current = iterator.next();
                // if current belongs to next stabilizers, i.e. it fixes beta_i & belongs to next BSGS element,
                // then it cannot be removed; note that second condition is necessary,
                // while first is redundant (but rids from obviously unnecessary checks)!
                if (current.newIndexOf(element.basePoint) == element.basePoint
                        && BSGSCandidate.get(i + 1).stabilizerGenerators.contains(element))
                    continue;
                //<-so generator does not fix base point and do not belongs to next element
                //let's check whether it is redundant
                if (newStabilizers == null) {
                    //make a copy of current stabilizers
                    newStabilizers = new ArrayList<>(element.stabilizerGenerators);
                } else
                    newStabilizers = new ArrayList<>(newStabilizers);

                newStabilizers.remove(current);

                //if new stabilizers produces same orbit => then current generator is redundant
                if (Combinatorics.getOrbitSize(newStabilizers, element.basePoint) == element.orbitSize()) {
                    iterator.remove();
                    removed = true;
                }
            }
            //if something was removed, then we need to recalculate Schreier vector
            if (removed)
                element.recalculateOrbitAndSchreierVector();
        }
    }


    /**
     * Returns true if specified BSGS candidate is a real BSGS. Method uses a restricted version of Schreier-Sims
     * algorithm.
     *
     * @param BSGSCandidate BSGS candidate
     * @return true if specified BSGS candidate is a real BSGS
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
                        Permutation SchreierGenerator = transversalOfBeta.composition(stabilizer)
                                .composition(transversalOfBetaX.inverse());
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

    //------------------------------ FACTORIES --------------------------------------------//

    /**
     * Creates immutable container of base and strong generating set. This method is simply returns
     * {@code new BSGS(createBSGSList(generators))}.
     *
     * @param generators
     * @return BSGS container
     */
    public static BSGS createBSGS(final Permutation... generators) {
        List<BSGSElement> BSGSList = createBSGSList(generators);
        if (BSGSList.isEmpty())
            return BSGS.EMPTY;
        return new BSGS(BSGSList);
    }

    /**
     * Makes a mutable copy of BSGS.
     *
     * @param BSGS BSGS
     * @return mutable copy of BSGS
     */
    public static List<BSGSCandidateElement> asBSGSCandidatesList(List<BSGSElement> BSGS) {
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
    public static List<BSGSElement> asBSGSList(List<BSGSCandidateElement> BSGSCandidate) {
        ArrayList<BSGSElement> BSGS = new ArrayList<>(BSGSCandidate.size());
        for (BSGSCandidateElement element : BSGSCandidate)
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
        int[] base = new int[BSGS.size()];
        for (int i = 0, size = BSGS.size(); i < size; ++i)
            base[i] = BSGS.get(i).basePoint;
        return base;
    }

    /**
     * Makes BSGS immutable
     *
     * @param BSGS BSGS
     */
    public static void makeImmutable(final List<BSGSElement> BSGS) {
        ListIterator<BSGSElement> iterator = BSGS.listIterator();
        while (iterator.hasNext())
            iterator.set(iterator.next().asBSGSElement());
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
     * Checks whether all permutations have same size and returns the result.
     *
     * @param generators permutations
     * @return true if all permutations have same length, false otherwise
     */
    public static boolean checkGeneratorsBoolean(final Permutation... generators) {
        for (int i = 1; i < generators.length; ++i)
            if (generators[i - 1].length() != generators[i].length())
                return false;
        return true;
    }
}
