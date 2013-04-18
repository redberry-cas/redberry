package cc.redberry.core.groups.permutations;

import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationGroup {


    public static int[] calculateSchreierVector(final int[][] generators,
                                                int point) {

        final int dimension = generators[0].length;
        int[] schreierVector = new int[dimension];
        Arrays.fill(schreierVector, -2);

        schreierVector[point] = -1;
        IntArrayList orbit = new IntArrayList();
        orbit.add(point);
        int image;
        for (int i = 0; i < orbit.size(); ++i) {
            for (int r = 0; r < generators.length; ++r) {
                int[] generator = generators[r];
                image = generator[orbit.get(i)];
                if (schreierVector[image] == -2) {
                    orbit.add(image);
                    schreierVector[image] = r;
                }
            }
        }

        return schreierVector;
    }


    public static int[] decomposeSchreierVectorSequence(final int[][] generators, int[] schreierVector, int point) {
        IntArrayList list = new IntArrayList();
        while (schreierVector[point] != -1) {
            list.add(schreierVector[point]);
            point = getInverse(generators[schreierVector[point]], point);
        }
        return list.toArray();
    }


    public static int[] decomposeSchreierVector(final int[][] generators, int[] schreierVector, int point) {
        int[] decompositonSequence = decomposeSchreierVectorSequence(generators, schreierVector, point);

        int[] permutstion = new int[generators[0].length];
        for (int i = 0; i < permutstion.length; ++i)
            permutstion[i] = i;

        for (int i = decompositonSequence.length - 1; i >= 0; --i)
            composition1(permutstion, generators[decompositonSequence[i]]);

        return permutstion;
    }

    public static int getInverse(int[] permutation, int point) {
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] == point) return i;
        throw new RuntimeException();
    }

    public static int[] composition(int[] left, int[] right) {
        int[] r = new int[left.length];

        for (int i = 0; i < left.length; ++i)
            r[i] = right[left[i]];

        return r;
    }

    public static void composition1(int[] left, int[] right) {
        for (int i = 0; i < left.length; ++i)
            left[i] = right[left[i]];
    }

    public static int[] inverse(int[] permutation) {
        int[] result = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            result[permutation[i]] = i;
        return result;
    }

    public static class OrbitStabilizer {
        final int[] schreierVector;
        final int[][] stabilizerGenerators;

        public OrbitStabilizer(int[] schreierVector, int[][] stabilizerGenerators) {
            this.schreierVector = schreierVector;
            this.stabilizerGenerators = stabilizerGenerators;
        }
    }

    public static int[] getIdentity(int n) {
        int[] result = new int[n];
        for (int i = 0; i < n; ++i)
            result[i] = i;
        return result;
    }


    public static OrbitStabilizer calculateOrbitStabilizer(final int[][] generators,
                                                           int point) {

        final int dimension = generators[0].length;
        int[] schreierVector = new int[dimension];
        Arrays.fill(schreierVector, -2);
        schreierVector[point] = -1;

        List<int[]> stabilizerGenerators = new ArrayList<>();
        int[][] transversals = new int[dimension][];
        transversals[point] = getIdentity(dimension);

        IntArrayList orbit = new IntArrayList();
        orbit.add(point);
        int image;
        for (int i = 0; i < orbit.size(); ++i) {
            for (int r = 0; r < generators.length; ++r) {
                int[] generator = generators[r];
                image = generator[orbit.get(i)];
                if (schreierVector[image] == -2) {
                    orbit.add(image);
                    schreierVector[image] = r;
                    transversals[image] = composition(transversals[orbit.get(i)], generator);
                } else {
                    stabilizerGenerators.add(composition(transversals[orbit.get(i)],
                            composition(generator, inverse(transversals[image]))));
                }
            }
        }

        return new OrbitStabilizer(schreierVector, stabilizerGenerators.toArray(new int[stabilizerGenerators.size()][]));
    }
}
