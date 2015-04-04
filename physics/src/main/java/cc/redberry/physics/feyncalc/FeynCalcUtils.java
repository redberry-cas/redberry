/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

import static cc.redberry.core.context.OutputFormat.Redberry;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class FeynCalcUtils {

    public static Expression[] setMandelstam(String[][] momentums) {
        final Tensor[][] tt = new Tensor[momentums.length][];
        int j;
        for (int i = 0; i < tt.length; ++i) {
            tt[i] = new Tensor[momentums[i].length];
            for (j = 0; j < tt[i].length; ++j)
                tt[i][j] = parse(momentums[i][j]);
        }
        return setMandelstam(tt);
    }

    public static Expression[] setMandelstam(Tensor[][] momentums) {
        return setMandelstam(momentums, parse("s"), parse("t"), parse("u"));
    }

    public static Expression[] setMandelstam(Tensor[][] momentums, Tensor s, Tensor t, Tensor u) {
        checkMandelstamInput(momentums, 4);
        if (s.getIndices().getFree().size() != 0 || t.getIndices().getFree().size() != 0 || u.getIndices().getFree().size() != 0)
            throw new IllegalArgumentException("Mandelstam variables should be scalar.");
        Expression[] result = new Expression[10];
        int i;
        // (k1,k1) = m1^2, (k2,k2) = m2^2, (k3,k3) = m3^2, (k4,k4) = m4^2
        for (i = 0; i < 4; ++i)
            result[i] = expression(square(momentums[i][0]), pow(momentums[i][1], 2));

        //2(k1, k2) = s - k1^2 - k2^2
        //2(k3, k4) = s - k3^2 - k4^2
        result[i++] = expression(multiply(Complex.TWO, contract(momentums[0][0], momentums[1][0])),
                sum(s, negate(sum(pow(momentums[0][1], 2), pow(momentums[1][1], 2)))));
        result[i++] = expression(multiply(Complex.TWO, contract(momentums[2][0], momentums[3][0])),
                sum(s, negate(sum(pow(momentums[2][1], 2), pow(momentums[3][1], 2)))));

        //-2(k1, k3) = t - k1^2 - k3^2
        //-2(k2, k4) = t - k2^2 - k4^2
        result[i++] = expression(multiply(Complex.MINUS_TWO, contract(momentums[0][0], momentums[2][0])),
                sum(t, negate(sum(pow(momentums[0][1], 2), pow(momentums[2][1], 2)))));
        result[i++] = expression(multiply(Complex.MINUS_TWO, contract(momentums[1][0], momentums[3][0])),
                sum(t, negate(sum(pow(momentums[1][1], 2), pow(momentums[3][1], 2)))));


        //-2(k1, k4) = u - k1^2 - k4^2
        //-2(k2, k3) = u - k2^2 - k3^2
        result[i++] = expression(multiply(Complex.MINUS_TWO, contract(momentums[0][0], momentums[3][0])),
                sum(u, negate(sum(pow(momentums[0][1], 2), pow(momentums[3][1], 2)))));
        result[i++] = expression(multiply(Complex.MINUS_TWO, contract(momentums[1][0], momentums[2][0])),
                sum(u, negate(sum(pow(momentums[1][1], 2), pow(momentums[2][1], 2)))));

        return result;
    }

    public static Expression[] setMandelstam5(Tensor[][] momentums) {
        return setMandelstam5(momentums, parse("s"), parse("t1"), parse("t2"), parse("u1"), parse("u2"));
    }

    public static Expression[] setMandelstam5(Tensor[][] momentums, Tensor s, Tensor t1, Tensor t2, Tensor u1, Tensor u2) {
        checkMandelstamInput(momentums, 5);
        if (s.getIndices().getFree().size() != 0
                || t1.getIndices().getFree().size() != 0
                || t2.getIndices().getFree().size() != 0
                || u1.getIndices().getFree().size() != 0
                || u2.getIndices().getFree().size() != 0)
            throw new IllegalArgumentException("Mandelstam variables should be scalar.");

        Expression[] result = new Expression[15];
        int i;
        // (k1,k1) = m1^2, (k2,k2) = m2^2, (k3,k3) = m3^2, (k4,k4) = m4^2
        for (i = 0; i < 5; ++i)
            result[i] = expression(square(momentums[i][0]), pow(momentums[i][1], 2));

        //k1_{a}*k2^{a} = (1/2)*(-m1**2-m2**2+s)

        result[i++] = parseExpression("k1_{a}*k2^{a} = (1/2)*(-m1**2-m2**2+s)");
        result[i++] = parseExpression("k3^{a}*k1_{a} = (1/2)*(m1**2+m3**2-t1)");
        result[i++] = parseExpression("k4^{a}*k1_{a} = (1/2)*(m1**2-t2+m4**2)");
        result[i++] = parseExpression("k5^{a}*k1_{a} = (1/2)*(-m1**2-m2**2-m3**2+s+t2-m4**2+t1)");
        result[i++] = parseExpression("k3^{a}*k2_{a} = (1/2)*(-u1+m2**2+m3**2)");
        result[i++] = parseExpression("k4^{a}*k2_{a} = (1/2)*(-u2+m2**2+m4**2)");
        result[i++] = parseExpression("k5^{a}*k2_{a} = (1/2)*(u1-m1**2+u2-m2**2-m3**2+s-m4**2)");
        result[i++] = parseExpression("k3_{a}*k4^{a} = (1/2)*(-u1-u2+2*m1**2+m3**2-s-t2+m4**2+2*m2**2-t1+m5**2)");
        result[i++] = parseExpression("k3_{a}*k5^{a} = (1/2)*(-m1**2+u2-m2**2-m3**2+s+t2-m4**2-m5**2)");
        result[i] = parseExpression("k5^{a}*k4_{a} = (1/2)*(u1-m1**2-m2**2-m3**2+s-m4**2+t1-m5**2)");

        Expression[] subs = {
                parseExpression("s = " + s.toString(Redberry)),
                parseExpression("t1 = " + t1.toString(Redberry)),
                parseExpression("t2 = " + t2.toString(Redberry)),
                parseExpression("u1 = " + u1.toString(Redberry)),
                parseExpression("u2 = " + u2.toString(Redberry)),

                expression(parse("m1"), momentums[0][1]),
                expression(parse("m2"), momentums[1][1]),
                expression(parse("m3"), momentums[2][1]),
                expression(parse("m4"), momentums[3][1]),
                expression(parse("m5"), momentums[4][1]),

                compileExpr0(parse("k1_a"), momentums[0][0]),
                compileExpr0(parse("k2_a"), momentums[1][0]),
                compileExpr0(parse("k3_a"), momentums[2][0]),
                compileExpr0(parse("k4_a"), momentums[3][0]),
                compileExpr0(parse("k5_a"), momentums[4][0])
        };

        for (int k = 5; k < result.length; ++k) {
            Expression temp = result[k];
            for (Expression e : subs)
                temp = (Expression) e.transform(temp);
            result[k] = temp;
        }

        return result;
    }

    private static void checkMandelstamInput(Tensor[][] momentums, int num) {
        if (momentums.length != num)
            throw new IllegalArgumentException();
        for (int i = 0; i < 4; ++i) {
            Tensor[] pair = momentums[i];
            if (pair.length != 2)
                throw new IllegalArgumentException();
            if (pair[0].getIndices().size() != 1)
                throw new IllegalArgumentException();
            if (pair[1].getIndices().size() != 0)
                throw new IllegalArgumentException();
        }
    }

    private static Tensor mul2(Tensor a) {
        return multiply(a, Complex.TWO);
    }

    private static Tensor contract(Tensor a, Tensor b) {
        return Tensors.multiplyAndRenameConflictingDummies(a, invertIndices(b));
    }

    private static Tensor square(Tensor tensor) {
        return Tensors.multiplyAndRenameConflictingDummies(tensor, invertIndices(tensor));
    }

    private static Tensor invertIndices(Tensor t) {
        Indices free = t.getIndices().getFree();
        Mapping mapping = new Mapping(free.toArray(), free.getInverted().toArray());
        return ApplyIndexMapping.applyIndexMapping(t, mapping);
    }

    private static Expression compileExpr0(Tensor lhs, Tensor rhs) {
        return expression(lhs, ApplyIndexMapping.applyIndexMapping(rhs, new Mapping(rhs.getIndices().getFree().toArray(), lhs.getIndices().getFree().toArray())));
    }
}
