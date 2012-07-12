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
package cc.redberry.core.number;

import java.math.BigInteger;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.fraction.BigFraction;

/**
 *
 * @author Stanislav Poslavsky
 */
public interface Number<T>
        extends FieldElement<T> {
  
    int intValue();
    long longValue();
    double doubleValue();
    float floatValue();
  
    T getNumericValue();
    
    T abs();
    
    T add(double bg);
    T add(int i);
    T add(long l);
    T add(BigInteger bg);
    T add(BigFraction fraction);
    
    T subtract(double bg);
    T subtract(int i);
    T subtract(long l);
    T subtract(BigInteger bg);
    T subtract(BigFraction fraction);
    
    T divide(double d);
    T divide(int i);
    T divide(long l);
    T divide(BigInteger bg);
    T divide(BigFraction fraction);
    
    T multiply(double d);
    T multiply(long l);
    T multiply(BigInteger bg);
    T multiply(BigFraction fraction);
    
    T pow(double exponent);
    T pow(BigInteger exponent);
    T pow(long exponent);
    T pow(int exponent);
    
    boolean isInfinite();
    boolean isNaN();
    boolean isZero();
    boolean isOne();
    boolean isMinusOne();
    boolean isNumeric();
    boolean isInteger();
    boolean isNatural();
}
