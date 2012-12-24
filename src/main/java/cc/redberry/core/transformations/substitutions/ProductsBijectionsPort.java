/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.DistinctCombinationsPort;
import cc.redberry.core.combinatorics.IntCombinatoricGenerator;
import cc.redberry.core.graph.GraphUtils;
import cc.redberry.core.tensor.FullContractionsStructure;
import cc.redberry.core.tensor.ProductContent;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.stretces.Stretch;
import cc.redberry.core.utils.stretces.StretchIteratorS;

import java.util.Arrays;

import static cc.redberry.core.tensor.FullContractionsStructure.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductsBijectionsPort implements OutputPortUnsafe<int[]> {
    //private ProductContent targetContent;
    //private ProductContent fromContent;

    private Tensor[] fromData, targetData;
    private final int[] seeds;
    private FullContractionsStructure targetFContractions, fromFContractions;
    private long[][] fromContractions, targetContractions;
    private final SeedPlanter planter;
    private InnerPort innerPort;

    public ProductsBijectionsPort(ProductContent fromContent, ProductContent targetContent) {
        //this.targetContent = targetContent;
        //this.fromContent = fromContent;
        this.targetFContractions = targetContent.getFullContractionsStructure();
        this.fromFContractions = fromContent.getFullContractionsStructure();

        this.fromContractions = fromFContractions.contractions;
        this.targetContractions = targetFContractions.contractions;

        int[] seeds = new int[fromFContractions.componentCount];
        Arrays.fill(seeds, -1);
        for (int i = 0; i < fromFContractions.components.length; ++i)
            if (seeds[fromFContractions.components[i]] == -1)
                seeds[fromFContractions.components[i]] = i;
        this.seeds = seeds;

        this.fromData = fromContent.getRange(0, fromContent.size());
        this.targetData = targetContent.getRange(0, targetContent.size());

        this.planter = new SeedPlanter();
    }

    @Override
    public int[] take() {
        while (true) {
            if (innerPort != null) {
                int[] bijection_;
                if ((bijection_ = innerPort.take()) != null)
                    return bijection_;
                else
                    innerPort = null;
            }

            //innerPort == null

            int[] seedsInTarget = planter.next();
            if (seedsInTarget == null)
                return null;
            int[] bijection = new int[fromData.length];
            Arrays.fill(bijection, -1);
            for (int i = 0; i < seedsInTarget.length; ++i)
                bijection[seeds[i]] = seedsInTarget[i];
            innerPort = new InnerPort(bijection, seeds);
        }
    }

    private static boolean weakMatch(Tensor t0, Tensor t1) {
        if (t0.hashCode() != t1.hashCode())
            return false;
        if (t0.getClass() != t1.getClass())
            return false;
        //CHECKSTYLE
        //This test is highly assotiated with current architecture.
        //Can fail arter architecture change
        //BEWARE!
        if (t0.getIndices().getClass() != t1.getIndices().getClass())
            return false;
        return t0.getIndices().size() == t1.getIndices().size();
    }

    private static boolean alreadyContains(final int[] bijection, int value) {
        for (int i : bijection)
            if (i == value)
                return true;
        return false;
    }

    private class InnerPort implements OutputPortUnsafe<int[]> {

        boolean closed = false;
        final int[] bijection;
        final int[] seeds;
//        final List<PermutationInfo>[] permutationInfos;
        PermutationInfo lastInfo = null, firstInfo = null;
        IntArrayList addedBijections;
        InnerPort innerPort = null;

        InnerPort(int[] bijection, int[] seeds) {
            this.bijection = bijection;
            this.seeds = seeds;
//            this.permutationInfos = new List[seeds.length];
            init();
        }

        @Override
        public int[] take() {
            if (closed)
                return null;

            MAIN:
            do {
                if (innerPort != null) {
                    int[] bijection_;
                    if ((bijection_ = innerPort.take()) != null)
                        return bijection_;
                    else if (lastInfo == null) { // innerPort.take() == null && lastInfo == null
                        closed = true;
                        return null;
                    } else // innerPort.take() == null && lastInfo != null
                        innerPort = null;
                }

                if (lastInfo == null) {
                    if (addedBijections.size() == 0) { //It means that this InnerPort is terminal. (Last port in chain).
                        closed = true;
                        return bijection;
                    }
                    innerPort = new InnerPort(bijection, addedBijections.toArray());
                    continue;
                }

                if (!lastInfo.next()) {
                    closed = true;
                    return null;
                }

                int[] bijectionNew = bijection.clone();
                IntArrayList addedBijectionsNew = addedBijections.clone();

                int j, fromTensorIndex, targetTensorIndex;
                long fromContraction, targetContraction;
                PermutationInfo currentInfo = firstInfo;
                do
                    for (j = 0; j < currentInfo.fromContractions.length; ++j) {
                        //Vse tut cherez jopu mi napisali, no vrode pravil'no
                        fromContraction = currentInfo.fromContractions[j];
                        targetContraction = currentInfo.targetContractions[currentInfo.permutation[j]];

                        assert getFromIndexId(fromContraction) == getFromIndexId(targetContraction);

                        fromTensorIndex = getToTensorIndex(fromContraction);
                        targetTensorIndex = getToTensorIndex(targetContraction);

                        if (getToIndexId(fromContraction) != getToIndexId(targetContraction)) {
                            if (!currentInfo.nextAndResetRightChain()) {
                                closed = true;
                                return null;
                            }
                            continue MAIN;
                        }

//                        assert fromTensorIndex != -1;

                        if (targetTensorIndex == -1) { //Not contracted index of target (but from is contracted with some tensor)
                            if (!currentInfo.nextAndResetRightChain()) {
                                closed = true;
                                return null;
                            }
                            continue MAIN;
                        }

                        if (!weakMatch(fromData[fromTensorIndex], targetData[targetTensorIndex])) {
                            if (!currentInfo.nextAndResetRightChain()) {
                                closed = true;
                                return null;
                            }
                            continue MAIN;
                        }

                        if (bijectionNew[fromTensorIndex] == -1) {
                            //Try addAll new bijection
                            if (alreadyContains(bijectionNew, targetTensorIndex)) {
                                if (!currentInfo.nextAndResetRightChain()) {
                                    closed = true;
                                    return null;
                                }
                                continue MAIN;
                            }
                            bijectionNew[fromTensorIndex] = targetTensorIndex;
                            addedBijectionsNew.add(fromTensorIndex);
                        } else if (bijectionNew[fromTensorIndex] != targetTensorIndex) { //Testing weather new bijection is consistent with already exists
                            if (!currentInfo.nextAndResetRightChain()) {
                                closed = true;
                                return null;
                            }
                            continue MAIN;
                        }
                    }
                while ((currentInfo = currentInfo.next) != null);

                innerPort = new InnerPort(bijectionNew, addedBijectionsNew.toArray());
            } while (true);
        }

        private void init() {
            int j;

            //Used to build chain of permutators
            PermutationInfo previousInfo = null;

            //Here added bijections will be collected
            IntArrayList addedBijections = new IntArrayList();

            //Iterating through seeds
            for (int i = 0; i < seeds.length; ++i) {
                //Index of seed in from array
                int seedFromIndex = seeds[i];
                //Seed tensor (used only to get indices -> index Ids)
                Tensor seedFrom = fromData[seedFromIndex];
                //Seed index in target array (bijection for this tensor should already be provided by upper level port)
                int seedTargetIndex = bijection[seedFromIndex];

                //Diff ids of indices. Cloned because it will be sorted.
                short[] diffIds = seedFrom.getIndices().getDiffIds().clone(); // (!!!)
                //Sorting with permutation retrieval.
                //This step needed because we use the fastest (we think so)
                //method of collecting indices with the same index id [Permutable indices].
                int[] diffIdsPermutation = ArraysUtils.quickSortP(diffIds);

                //Creating array for permutation infos for current bijection.
//                permutationInfos[i] = new ArrayList<>();

                //Iterating through stretches of indices
                for (Stretch stretch : new StretchIteratorS(diffIds))
                    if (stretch.length == 1) { //Indices with unique index id (stretch.length == 1) [Non permutable indices].
                        //Corresponding contraction in from tensor
                        long fromIndexContraction = fromContractions[seedFromIndex][diffIdsPermutation[stretch.from]];
                        //Corresponding contraction in target tensor
                        long targetIndexContraction = targetContractions[seedTargetIndex][diffIdsPermutation[stretch.from]];

                        final int fromTensorIndex = getToTensorIndex(fromIndexContraction); //Index of contracting tensor in from array
                        if (fromTensorIndex == -1) //Not contracted index of from
                            continue;

                        if (getToIndexId(fromIndexContraction) != getToIndexId(targetIndexContraction)) { //Contracts with different index id
                            closed = true;
                            return;
                        }

                       final int targetTensorIndex = getToTensorIndex(targetIndexContraction); //Index of contracting tensor in target array
                        if (targetTensorIndex == -1) {//Not contracted index of target (but from is contracted with some tensor),
                            // so this bijection is impossible
                            closed = true;
                            return;
                        }

                        //Checking weak match between corresponding contracting tensors
                        if (!weakMatch(fromData[fromTensorIndex], targetData[targetTensorIndex])) {
                            //Early termination
                            closed = true;
                            return;
                        }

                        if (bijection[fromTensorIndex] == -1) { //This bijection is free
                            //Adding new bijection
                            if (alreadyContains(bijection, targetTensorIndex)) {
                                closed = true;
                                return;
                            }
                            bijection[fromTensorIndex] = targetTensorIndex;
                            addedBijections.add(fromTensorIndex);
                        } else if (bijection[fromTensorIndex] != targetTensorIndex) { //Testing weather new bijection is consistent with already existing
                            closed = true;
                            return;
                        }
                    } else { //There are several indices with the same index id.
                        //Some of indices contracts with -1 tensor (are free)
                        //Counting tensors in from array contracting with other tensor (non free)
                        int count = 0;
                        for (j = 0; j < stretch.length; ++j)
                            if (getToTensorIndex(fromContractions[seedFromIndex][diffIdsPermutation[stretch.from + j]]) != -1) //TODO addAll bijection == -1 (????)
                                ++count;
                        long[] fromContractions_ = new long[count]; //Positions of permutating indices (contractions) in from array
                        long[] targetContractions_ = new long[stretch.length];
                        count = 0; //used as pointer below (to save 4 bytes of stack memory :-D )
                        long contraction;
                        for (j = 0; j < stretch.length; ++j) {
                            if (getToTensorIndex(contraction = fromContractions[seedFromIndex][diffIdsPermutation[stretch.from + j]]) != -1)
                                fromContractions_[count++] = contraction;
                            targetContractions_[j] = targetContractions[seedTargetIndex][diffIdsPermutation[stretch.from + j]];
                        }

                        previousInfo = new PermutationInfo(previousInfo,
                                                           fromContractions_,
                                                           targetContractions_);
                        if (firstInfo == null)
                            firstInfo = previousInfo;
                    }
            }
            this.addedBijections = addedBijections;
            this.lastInfo = previousInfo;
        }
    }

    private static final class PermutationInfo {

        /**
         * Previous Permutation info in chain
         */
        final PermutationInfo previous;
        PermutationInfo next;
        /**
         * Indices in array of contractions for some (seed) tensor in from
         * array. None of this contractions points (in terms of
         * getToTensorIndex()) to -1 tensor
         */
        final long[] fromContractions;
        /**
         * Indices in array of contractions for some (seed) tensor in target
         * array.
         */
        final long[] targetContractions;
        final int[] permutation;
        /**
         * Generator
         */
        final IntCombinatoricGenerator generator;

        public PermutationInfo(PermutationInfo previous, long[] fromContractions, long[] targetContractions) {
            this.previous = previous;
            this.fromContractions = fromContractions;
            this.targetContractions = targetContractions;
            this.generator = Combinatorics.createIntGenerator(targetContractions.length, fromContractions.length);
            permutation = this.generator.getReference();
            if (previous != null) {
                previous.generator.next();
                previous.next = this;
            }
        }

        boolean next() {
            if (!generator.hasNext()) {
                generator.reset();
                generator.next();
                if (previous != null)
                    return previous.next();
                else
                    return false;
            }
            generator.next();
            return true;
        }

        boolean nextAndResetRightChain() {
            if (next == null)
                return true;
            if (!next())
                return false;
            PermutationInfo current = this;
            while ((current = current.next).next != null) {
                current.generator.reset();
                current.generator.next();
            }
            current.generator.reset();
            return true;
        }
    }

    private final class SeedPlanter {

        final DistinctCombinationsPort combinationsPort;

        public SeedPlanter() {
            int[][] hits = new int[seeds.length][];
            IntArrayList hitList = new IntArrayList();
            for (int seedIndex = 0; seedIndex < seeds.length; ++seedIndex) {
                hitList.clear();
                for (int i = 0; i < targetData.length; ++i)
                    if (weakMatch(fromData[seeds[seedIndex]], targetData[i])
                            && GraphUtils.componentSize(seeds[seedIndex], fromFContractions.components)
                            <= GraphUtils.componentSize(i, targetFContractions.components))
                        hitList.add(i);
                hits[seedIndex] = hitList.toArray();
            }
            combinationsPort = new DistinctCombinationsPort(hits);
        }

        public int[] next() {
            return combinationsPort.take();
        }
    }
}
