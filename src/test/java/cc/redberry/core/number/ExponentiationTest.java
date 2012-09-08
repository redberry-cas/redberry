package cc.redberry.core.number;

import cc.redberry.core.number.parser.NumberParser;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigInteger;

public class ExponentiationTest extends TestCase {
    @Test
    public void testIntegerRoots() {
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(28), BigInteger.valueOf(3)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(22), BigInteger.valueOf(3)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(27), BigInteger.valueOf(3)),
                BigInteger.valueOf(3));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(49), BigInteger.valueOf(2)),
                BigInteger.valueOf(7));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(129140163), BigInteger.valueOf(17)),
                BigInteger.valueOf(3));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(129140162), BigInteger.valueOf(17)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(129140164), BigInteger.valueOf(17)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(19073486328125L), BigInteger.valueOf(19)),
                BigInteger.valueOf(5));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(19073486328123L), BigInteger.valueOf(19)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(19073486328128L), BigInteger.valueOf(19)),
                null);
    }

    @Test
    public void testExponentiateIfPossible() {
        Assert.assertEquals(NumberParser.REAL_PARSER.parse("12/5"),
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("25/144"),
                        NumberParser.REAL_PARSER.parse("-1/2")));

        Assert.assertEquals(NumberParser.REAL_PARSER.parse("9/49"),
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("27/343"),
                        NumberParser.REAL_PARSER.parse("2/3")));

        Assert.assertEquals(null,
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("27/343"),
                        NumberParser.REAL_PARSER.parse("2/4")));

        Assert.assertEquals(NumberParser.REAL_PARSER.parse("0.28056585887484736"),
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("27/343"),
                        NumberParser.REAL_PARSER.parse("0.5")));
    }
}
