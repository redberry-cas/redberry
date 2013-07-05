package cc.redberry.core.utils;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntBackedBitArrayTest {
    @Test
    public void test1() throws Exception {
        IntBackedBitArray ba = new IntBackedBitArray(10);

        assertEquals("0000000000", ba.toString());

        ba.set(3);
        ba.set(5);
        ba.set(8);
        ba.set(5, false);

        assertEquals("0001000010", ba.toString());

        assertEquals(2, ba.bitCount());

        ba.not();
        assertEquals("1110111101", ba.toString());

        assertEquals(8, ba.bitCount());

        ba.and(ba);

        assertEquals("1110111101", ba.toString());

        assertEquals(8, ba.bitCount());

        ba.or(ba);

        assertEquals("1110111101", ba.toString());

        assertEquals(8, ba.bitCount());

        ba.xor(ba);

        assertTrue(ba.isEmpty());

        ba.not();

        assertTrue(ba.isFull());
    }

    @Test
    public void testSetValueFrom1() throws Exception {
        IntBackedBitArray ba = new IntBackedBitArray(32);
        ba.set(3);
        ba.set(5);
        ba.set(8);
        ba.set(21);
        ba.set(28);

        int d = ba.data[0];
        String str = ba.toString();

        IntBackedBitArray arr = new IntBackedBitArray(128);
        for (int i = 0; i < 64; ++i) {
            arr.loadValueFrom(d, i, 32);
            String expected = chars(i, '0') + str + chars(128 - i - 32, '0');
            assertEquals("On " + i, expected, arr.toString());
        }
    }

    @Test
    public void testSetValueFrom2() throws Exception {
        RandomGenerator rg = new Well19937c(2031);

        for (int k = 0; k < 100; ++k) {
            IntBackedBitArray ba = new IntBackedBitArray(32);
            ba.data[0] = rg.nextInt();
            int d = ba.data[0];
            String str = ba.toString();

            IntBackedBitArray arr = new IntBackedBitArray(128);
            for (int i = 0; i < 64; ++i) {
                arr.loadValueFrom(d, i, 32);
                String expected = chars(i, ((d & 1) == 1) ? '1' : '0') + str + chars(128 - i - 32, '0');
                assertEquals("On " + i, expected, arr.toString());
            }
        }
    }

    @Test
    public void testSetValueFrom2a() throws Exception {
        RandomGenerator rg = new Well19937c(2031);

        for (int k = 0; k < 100; ++k) {
            IntBackedBitArray ba = new IntBackedBitArray(32);
            ba.data[0] = rg.nextInt();
            int d = ba.data[0];
            String str = ba.toString();

            int size = rg.nextInt(512);
            boolean[] arr1 = new boolean[size];
            for (int i = 0; i < size; ++i)
                arr1[i] = rg.nextBoolean();

            IntBackedBitArray arr = new IntBackedBitArray(arr1);
            String initial = arr.toString();

            for (int i = 0; i < size - 32; ++i) {
                arr.loadValueFrom(d, i, 32);
                String expected = chars(i, ((d & 1) == 1) ? '1' : '0') + str + initial.substring(32 + i);
                assertEquals("On " + i, expected, arr.toString());
            }
        }
    }

    @Test
    public void testSetValueFrom2b() throws Exception {
        RandomGenerator rg = new Well19937c(2031);

        for (int k = 0; k < 100; ++k) {
            IntBackedBitArray ba = new IntBackedBitArray(32);
            ba.data[0] = rg.nextInt();
            int d = ba.data[0];
            String str = ba.toString().substring(0, 16);

            int size = rg.nextInt(512);
            boolean[] arr1 = new boolean[size];
            for (int i = 0; i < size; ++i)
                arr1[i] = rg.nextBoolean();

            IntBackedBitArray arr = new IntBackedBitArray(arr1);
            String initial = arr.toString();

            for (int i = 0; i < size - 32; ++i) {
                arr.loadValueFrom(d, i, 16);
                String expected = chars(i, ((d & 1) == 1) ? '1' : '0') + str + initial.substring(16 + i);
                assertEquals("On " + i, expected, arr.toString());
            }
        }
    }

    @Test
    public void testSetValueFrom2c() throws Exception {
        RandomGenerator rg = new Well19937c(2031);

        for (int k = 0; k < 100; ++k) {
            IntBackedBitArray ba = new IntBackedBitArray(32);
            ba.data[0] = rg.nextInt();
            int d = ba.data[0];
            String str = ba.toString();

            int size = rg.nextInt(512);
            boolean[] arr1 = new boolean[size];
            for (int i = 0; i < size; ++i)
                arr1[i] = rg.nextBoolean();

            IntBackedBitArray arr = new IntBackedBitArray(arr1);
            String initial = arr.toString();

            for (int i = 0; i < size - 32; ++i) {
                arr.loadValueFrom(d, i, 0);
                String expected = initial;
                assertEquals("On " + i, expected, arr.toString());
            }
        }
    }

    @Test
    public void testSetValueFrom3() throws Exception {
        RandomGenerator rg = new Well19937c(2031);

        int offset1, offset2, length;

        for (int k = 0; k < 100; ++k) {
            int size = rg.nextInt(512);

            boolean[] arr1 = new boolean[size], arr2 = new boolean[size];
            for (int i = 0; i < size; ++i)
                arr1[i] = rg.nextBoolean();
            for (int i = 0; i < size; ++i)
                arr2[i] = rg.nextBoolean();

            IntBackedBitArray ba1 = new IntBackedBitArray(arr1),
                    ba2 = new IntBackedBitArray(arr2);


            for (int i = 0; i < 100; ++i) {
                offset1 = rg.nextInt(size);
                offset2 = rg.nextInt(size);
                length = rg.nextInt(size - Math.max(offset1, offset2));
                System.arraycopy(arr1, offset1, arr2, offset2, length);
                ba2.loadValueFrom(ba1, offset1, offset2, length);

                assertTrue(testNormal(ba2));
                assertEquals("On :" + i + ", " + k, new IntBackedBitArray(arr2), ba2);
            }
        }
    }

    @Test
    public void testCopyOfRange1() throws Exception {
        RandomGenerator rg = new Well19937c(203);

        int offset1, length;

        for (int k = 0; k < 100; ++k) {
            int size = rg.nextInt(512);

            boolean[] arr1 = new boolean[size];
            for (int i = 0; i < size; ++i)
                arr1[i] = rg.nextBoolean();
            IntBackedBitArray ba1 = new IntBackedBitArray(arr1), ba2;


            for (int i = 0; i < 100; ++i) {
                offset1 = rg.nextInt(size);
                length = rg.nextInt(size - offset1);
                ba2 = ba1.copyOfRange(offset1, offset1 + length);
                assertTrue(testNormal(ba2));
                assertEquals("On :" + i + ", " + k, new IntBackedBitArray(Arrays.copyOfRange(arr1, offset1, offset1 + length)), ba2);
            }
        }
    }

    private boolean testNormal(IntBackedBitArray ba) {
        if (ba.size == 0)
            return true;
        return (ba.data[ba.data.length - 1] & (~(ba.lastElementMask()))) == 0;
    }

    private String chars(int count, char c) {
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
