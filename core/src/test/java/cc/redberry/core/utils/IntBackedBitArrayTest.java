package cc.redberry.core.utils;

import org.junit.Test;

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
    public void testTrailingBit1() throws Exception {
        System.out.println(Integer.numberOfTrailingZeros(0));
    }
}
