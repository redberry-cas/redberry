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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.MathUtils;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static cc.redberry.core.tensor.Tensors.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumTest {

    @Test
    public void toString1() {
        Tensor t = Tensors.parse("Power[M, 14] - 135*s*Power[M, 14] + 27*Power[M, 16] + 45*Power[M, 20] - 211*s*Power[M, 12]*Power[pT, 2] + 38*Power[M, 14]*Power[pT, 2] + 2*Power[M, 12]*(-3*s + Power[pT, 2]) + 45*Power[M, 18]*(-2*s + Power[pT, 2]) + 60*Power[M, 18]*(-3*s + 2*Power[pT, 2]) - 374*s*Power[M, 10]*Power[pT, 4] + 38*Power[M, 12]*Power[pT, 4] - 92*s*Power[M, 8]*Power[pT, 6] + 27*Power[M, 10]*Power[pT, 6] - 54*s*Power[M, 6]*Power[pT, 8] + 324*Power[M, 12]*Power[s, 2] + 616*Power[M, 10]*Power[pT, 2]*Power[s, 2] + 882*Power[M, 8]*Power[pT, 4]*Power[s, 2] + 698*Power[M, 6]*Power[pT, 6]*Power[s, 2] + 135*Power[M, 4]*Power[pT, 8]*Power[s, 2] + 27*Power[M, 2]*Power[pT, 10]*Power[s, 2] + Power[M, 10]*(-11*s*Power[pT, 2] + 3*Power[pT, 4] + 16*Power[s, 2]) + 12*Power[M, 16]*(-24*s*Power[pT, 2] + 6*Power[pT, 4] + 17*Power[s, 2]) + 3*Power[M, 16]*(-41*s*Power[pT, 2] + 8*Power[pT, 4] + 34*Power[s, 2]) + 3*Power[M, 16]*(-59*s*Power[pT, 2] + 12*Power[pT, 4] + 51*Power[s, 2]) + 6*Power[M, 16]*(-70*s*Power[pT, 2] + 18*Power[pT, 4] + 51*Power[s, 2]) + Power[M, 14]*(-456*s*Power[pT, 4] + 48*Power[pT, 6] + 771*Power[pT, 2]*Power[s, 2] - 360*Power[s, 3]) + 3*Power[M, 14]*(-55*s*Power[pT, 4] + 6*Power[pT, 6] + 118*Power[pT, 2]*Power[s, 2] - 60*Power[s, 3]) + 12*Power[M, 14]*(-54*s*Power[pT, 4] + 6*Power[pT, 6] + 98*Power[pT, 2]*Power[s, 2] - 45*Power[s, 3]) + 9*Power[M, 14]*(-27*s*Power[pT, 4] + 3*Power[pT, 6] + 59*Power[pT, 2]*Power[s, 2] - 30*Power[s, 3]) + 2*Power[M, 8]*(-5*s*Power[pT, 4] + Power[pT, 6] + 15*Power[pT, 2]*Power[s, 2] - 12*Power[s, 3]) - 486*Power[M, 10]*Power[s, 3] - 1091*Power[M, 8]*Power[pT, 2]*Power[s, 3] - 1557*Power[M, 6]*Power[pT, 4]*Power[s, 3] - 1087*Power[M, 4]*Power[pT, 6]*Power[s, 3] - 346*Power[M, 2]*Power[pT, 8]*Power[s, 3] - 27*Power[pT, 10]*Power[s, 3] + s*Power[M, 10]*(616*s*Power[pT, 6] - 87*Power[pT, 8] - 1365*Power[pT, 4]*Power[s, 2] + 1089*Power[pT, 2]*Power[s, 3] - 270*Power[s, 4]) + s*Power[M, 10]*(310*s*Power[pT, 6] - 63*Power[pT, 8] - 696*Power[pT, 4]*Power[s, 2] + 651*Power[pT, 2]*Power[s, 3] - 180*Power[s, 4]) + s*Power[M, 4]*(13*s*Power[pT, 6] - 3*Power[pT, 8] - 33*Power[pT, 4]*Power[s, 2] + 39*Power[pT, 2]*Power[s, 3] - 10*Power[s, 4]) + 486*Power[M, 8]*Power[s, 4] + 1134*Power[M, 6]*Power[pT, 2]*Power[s, 4] + 1340*Power[M, 4]*Power[pT, 4]*Power[s, 4] + 665*Power[M, 2]*Power[pT, 6]*Power[s, 4] - 27*Power[pT, 8]*Power[s, 4] + Power[M, 6]*(-6*s*Power[pT, 6] + Power[pT, 8] + 22*Power[pT, 4]*Power[s, 2] - 47*Power[pT, 2]*Power[s, 3] + 21*Power[s, 4]) - 4*s*Power[M, 10]*(-377*s*Power[pT, 6] + 51*Power[pT, 8] + 753*Power[pT, 4]*Power[s, 2] - 561*Power[pT, 2]*Power[s, 3] + 135*Power[s, 4]) - 2*s*Power[M, 10]*(-391*s*Power[pT, 6] + 72*Power[pT, 8] + 747*Power[pT, 4]*Power[s, 2] - 651*Power[pT, 2]*Power[s, 3] + 180*Power[s, 4]) + 2*Power[M, 12]*(-180*s*Power[pT, 6] + 6*Power[pT, 8] + 557*Power[pT, 4]*Power[s, 2] - 612*Power[pT, 2]*Power[s, 3] + 216*Power[s, 4]) + Power[M, 12]*(-129*s*Power[pT, 6] + 6*Power[pT, 8] + 463*Power[pT, 4]*Power[s, 2] - 591*Power[pT, 2]*Power[s, 3] + 216*Power[s, 4]) + 2*Power[M, 12]*(-252*s*Power[pT, 6] + 9*Power[pT, 8] + 920*Power[pT, 4]*Power[s, 2] - 1008*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 12]*(-189*s*Power[pT, 6] + 9*Power[pT, 8] + 769*Power[pT, 4]*Power[s, 2] - 951*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 6]*Power[s, 2]*(-218*s*Power[pT, 8] + 75*Power[pT, 10] + 513*Power[pT, 6]*Power[s, 2] - 842*Power[pT, 4]*Power[s, 3] + 483*Power[pT, 2]*Power[s, 4] - 72*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-439*s*Power[pT, 8] + 81*Power[pT, 10] + 898*Power[pT, 6]*Power[s, 2] - 844*Power[pT, 4]*Power[s, 3] + 357*Power[pT, 2]*Power[s, 4] - 54*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-115*s*Power[pT, 8] + 39*Power[pT, 10] + 271*Power[pT, 6]*Power[s, 2] - 433*Power[pT, 4]*Power[s, 3] + 243*Power[pT, 2]*Power[s, 4] - 36*Power[s, 5]) + 4*Power[M, 6]*Power[s, 2]*(-227*s*Power[pT, 8] + 42*Power[pT, 10] + 456*Power[pT, 6]*Power[s, 2] - 425*Power[pT, 4]*Power[s, 3] + 180*Power[pT, 2]*Power[s, 4] - 27*Power[s, 5]) - 324*Power[M, 6]*Power[s, 5] - 702*Power[M, 4]*Power[pT, 2]*Power[s, 5] - 697*Power[M, 2]*Power[pT, 4]*Power[s, 5] - 27*Power[pT, 6]*Power[s, 5] + s*Power[M, 8]*(133*s*Power[pT, 8] - 12*Power[pT, 10] - 381*Power[pT, 6]*Power[s, 2] + 665*Power[pT, 4]*Power[s, 3] - 492*Power[pT, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(149*s*Power[pT, 8] - 12*Power[pT, 10] - 407*Power[pT, 6]*Power[s, 2] + 663*Power[pT, 4]*Power[s, 3] - 486*Power[pT, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(344*s*Power[pT, 8] - 18*Power[pT, 10] - 1142*Power[pT, 6]*Power[s, 2] + 1476*Power[pT, 4]*Power[s, 3] - 810*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + s*Power[M, 8]*(301*s*Power[pT, 8] - 18*Power[pT, 10] - 1041*Power[pT, 6]*Power[s, 2] + 1415*Power[pT, 4]*Power[s, 3] - 798*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + 135*Power[M, 4]*Power[s, 6] + 243*Power[M, 2]*Power[pT, 2]*Power[s, 6] + 108*Power[pT, 4]*Power[s, 6] + 2*Power[M, 4]*Power[s, 2]*(-23*s*Power[pT, 10] + 6*Power[pT, 12] + 32*Power[pT, 8]*Power[s, 2] - 122*Power[pT, 6]*Power[s, 3] + 173*Power[pT, 4]*Power[s, 4] - 66*Power[pT, 2]*Power[s, 5] + 6*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-25*s*Power[pT, 10] + 6*Power[pT, 12] + 42*Power[pT, 8]*Power[s, 2] - 127*Power[pT, 6]*Power[s, 3] + 175*Power[pT, 4]*Power[s, 4] - 66*Power[pT, 2]*Power[s, 5] + 6*Power[s, 6]) + 2*Power[M, 4]*Power[s, 2]*(-86*s*Power[pT, 10] + 9*Power[pT, 12] + 269*Power[pT, 8]*Power[s, 2] - 374*Power[pT, 6]*Power[s, 3] + 263*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-88*s*Power[pT, 10] + 9*Power[pT, 12] + 276*Power[pT, 8]*Power[s, 2] - 382*Power[pT, 6]*Power[s, 3] + 265*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) - 27*Power[M, 2]*Power[s, 7] - 27*Power[pT, 2]*Power[s, 7] - s*Power[M, 2]*(-(s*Power[pT, 4]) + 2*Power[pT, 6] + 10*Power[pT, 2]*Power[s, 2] - 2*Power[s, 3])*Power[-s + Power[pT, 2], 2] + Power[pT, 2]*Power[s, 2]*Power[-s + Power[pT, 2], 4] + 6*Power[pT, 4]*Power[s, 4]*Power[-s + Power[pT, 2], 4] - Power[M, 2]*(17*s*Power[pT, 4] + 12*Power[pT, 6] + 44*Power[pT, 2]*Power[s, 2] - 12*Power[s, 3])*Power[s, 3]*Power[-(pT*s) + Power[pT, 3], 2] - Power[M, 2]*(9*s*Power[pT, 4] + 6*Power[pT, 6] + 22*Power[pT, 2]*Power[s, 2] - 6*Power[s, 3])*Power[s, 3]*Power[-(pT*s) + Power[pT, 3], 2] + 4*Power[M, 2]*Power[s, 3]*(10*s*Power[pT, 4] - 3*Power[pT, 6] - 11*Power[pT, 2]*Power[s, 2] + 3*Power[s, 3])* Power[-(pT*s) + Power[pT, 3], 2] + Power[M, 2]*Power[s, 3]*(21*s*Power[pT, 4] - 6*Power[pT, 6] - 22*Power[pT, 2]*Power[s, 2] + 6*Power[s, 3])*Power[-(pT*s) + Power[pT, 3], 2]");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString2() {
        Tensor t = Tensors.parse("-a**4*b-a**2 - c*(a + b)**2*d*n**131*h**(-a) - a");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString3() {
        Tensor t = Tensors.parse("-1*a**4*b-2*a**2 - 3*c*(a + 2*b)**2*d*n**131*h**(-6*a) - 1*a");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString4() {
        Tensor t = Tensors.parse("2*(-3*s + Power[M, 2])*Power[M, 12] + Power[M, 14] - 866*s*Power[M, 14] + 130*Power[M, 16] + 45*(-2*s + Power[M, 2])*Power[M, 18] + 60*(-3*s + 2*Power[M, 2])*Power[M, 18] + 45*Power[M, 20] + 2682*Power[M, 12]*Power[s, 2] + Power[M, 10]*(-11*s*Power[M, 2] + 3*Power[M, 4] + 16*Power[s, 2]) + 12*Power[M, 16]*(-24*s*Power[M, 2] + 6*Power[M, 4] + 17*Power[s, 2]) + 3*Power[M, 16]*(-41*s*Power[M, 2] + 8*Power[M, 4] + 34*Power[s, 2]) + 3*Power[M, 16]*(-59*s*Power[M, 2] + 12*Power[M, 4] + 51*Power[s, 2]) + 6*Power[M, 16]*(-70*s*Power[M, 2] + 18*Power[M, 4] + 51*Power[s, 2]) + Power[M, 14]*(-456*s*Power[M, 4] + 48*Power[M, 6] + 771*Power[M, 2]*Power[s, 2] - 360*Power[s, 3]) + 3*Power[M, 14]*(-55*s*Power[M, 4] + 6*Power[M, 6] + 118*Power[M, 2]*Power[s, 2] - 60*Power[s, 3]) + 12*Power[M, 14]*(-54*s*Power[M, 4] + 6*Power[M, 6] + 98*Power[M, 2]*Power[s, 2] - 45*Power[s, 3]) + 9*Power[M, 14]*(-27*s*Power[M, 4] + 3*Power[M, 6] + 59*Power[M, 2]*Power[s, 2] - 30*Power[s, 3]) + 2*Power[M, 8]*(-5*s*Power[M, 4] + Power[M, 6] + 15*Power[M, 2]*Power[s, 2] - 12*Power[s, 3]) - 4594*Power[M, 10]*Power[s, 3] + s*Power[M, 10]*(616*s*Power[M, 6] - 87*Power[M, 8] - 1365*Power[M, 4]*Power[s, 2] + 1089*Power[M, 2]*Power[s, 3] - 270*Power[s, 4]) + s*Power[M, 10]*(310*s*Power[M, 6] - 63*Power[M, 8] - 696*Power[M, 4]*Power[s, 2] + 651*Power[M, 2]*Power[s, 3] - 180*Power[s, 4]) + s*Power[M, 4]*(13*s*Power[M, 6] - 3*Power[M, 8] - 33*Power[M, 4]*Power[s, 2] + 39*Power[M, 2]*Power[s, 3] - 10*Power[s, 4]) + 3598*Power[M, 8]*Power[s, 4] + Power[M, 6]*(-6*s*Power[M, 6] + Power[M, 8] + 22*Power[M, 4]*Power[s, 2] - 47*Power[M, 2]*Power[s, 3] + 21*Power[s, 4]) - 4*s*Power[M, 10]*(-377*s*Power[M, 6] + 51*Power[M, 8] + 753*Power[M, 4]*Power[s, 2] - 561*Power[M, 2]*Power[s, 3] + 135*Power[s, 4]) - 2*s*Power[M, 10]*(-391*s*Power[M, 6] + 72*Power[M, 8] + 747*Power[M, 4]*Power[s, 2] - 651*Power[M, 2]*Power[s, 3] + 180*Power[s, 4]) + 2*Power[M, 12]*(-180*s*Power[M, 6] + 6*Power[M, 8] + 557*Power[M, 4]*Power[s, 2] - 612*Power[M, 2]*Power[s, 3] + 216*Power[s, 4]) + Power[M, 12]*(-129*s*Power[M, 6] + 6*Power[M, 8] + 463*Power[M, 4]*Power[s, 2] - 591*Power[M, 2]*Power[s, 3] + 216*Power[s, 4]) + 2*Power[M, 12]*(-252*s*Power[M, 6] + 9*Power[M, 8] + 920*Power[M, 4]*Power[s, 2] - 1008*Power[M, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 12]*(-189*s*Power[M, 6] + 9*Power[M, 8] + 769*Power[M, 4]*Power[s, 2] - 951*Power[M, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 6]*Power[s, 2]*(-218*s*Power[M, 8] + 75*Power[M, 10] + 513*Power[M, 6]*Power[s, 2] - 842*Power[M, 4]*Power[s, 3] + 483*Power[M, 2]*Power[s, 4] - 72*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-439*s*Power[M, 8] + 81*Power[M, 10] + 898*Power[M, 6]*Power[s, 2] - 844*Power[M, 4]*Power[s, 3] + 357*Power[M, 2]*Power[s, 4] - 54*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-115*s*Power[M, 8] + 39*Power[M, 10] + 271*Power[M, 6]*Power[s, 2] - 433*Power[M, 4]*Power[s, 3] + 243*Power[M, 2]*Power[s, 4] - 36*Power[s, 5]) + 4*Power[M, 6]*Power[s, 2]*(-227*s*Power[M, 8] + 42*Power[M, 10] + 456*Power[M, 6]*Power[s, 2] - 425*Power[M, 4]*Power[s, 3] + 180*Power[M, 2]*Power[s, 4] - 27*Power[s, 5]) - 1750*Power[M, 6]*Power[s, 5] + s*Power[M, 8]*(133*s*Power[M, 8] - 12*Power[M, 10] - 381*Power[M, 6]*Power[s, 2] + 665*Power[M, 4]*Power[s, 3] - 492*Power[M, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(149*s*Power[M, 8] - 12*Power[M, 10] - 407*Power[M, 6]*Power[s, 2] + 663*Power[M, 4]*Power[s, 3] - 486*Power[M, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(344*s*Power[M, 8] - 18*Power[M, 10] - 1142*Power[M, 6]*Power[s, 2] + 1476*Power[M, 4]*Power[s, 3] - 810*Power[M, 2]*Power[s, 4] + 153*Power[s, 5]) + s*Power[M, 8]*(301*s*Power[M, 8] - 18*Power[M, 10] - 1041*Power[M, 6]*Power[s, 2] + 1415*Power[M, 4]*Power[s, 3] - 798*Power[M, 2]*Power[s, 4] + 153*Power[s, 5]) + 486*Power[M, 4]*Power[s, 6] + 2*Power[M, 4]*Power[s, 2]*(-23*s*Power[M, 10] + 6*Power[M, 12] + 32*Power[M, 8]*Power[s, 2] - 122*Power[M, 6]*Power[s, 3] + 173*Power[M, 4]*Power[s, 4] - 66*Power[M, 2]*Power[s, 5] + 6*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-25*s*Power[M, 10] + 6*Power[M, 12] + 42*Power[M, 8]*Power[s, 2] - 127*Power[M, 6]*Power[s, 3] + 175*Power[M, 4]*Power[s, 4] - 66*Power[M, 2]*Power[s, 5] + 6*Power[s, 6]) + 2*Power[M, 4]*Power[s, 2]*(-86*s*Power[M, 10] + 9*Power[M, 12] + 269*Power[M, 8]*Power[s, 2] - 374*Power[M, 6]*Power[s, 3] + 263*Power[M, 4]*Power[s, 4] - 84*Power[M, 2]*Power[s, 5] + 9*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-88*s*Power[M, 10] + 9*Power[M, 12] + 276*Power[M, 8]*Power[s, 2] - 382*Power[M, 6]*Power[s, 3] + 265*Power[M, 4]*Power[s, 4] - 84*Power[M, 2]*Power[s, 5] + 9*Power[s, 6]) - 54*Power[M, 2]*Power[s, 7] - s*Power[M, 2]*(-(s*Power[M, 4]) + 2*Power[M, 6] + 10*Power[M, 2]*Power[s, 2] - 2*Power[s, 3])*Power[-s + Power[M, 2], 2] + Power[M, 2]*Power[s, 2]*Power[-s + Power[M, 2], 4] + 6*Power[M, 4]*Power[s, 4]*Power[-s + Power[M, 2], 4] - Power[M, 2]*(17*s*Power[M, 4] + 12*Power[M, 6] + 44*Power[M, 2]*Power[s, 2] - 12*Power[s, 3])*Power[s, 3]*Power[-(M*s) + Power[M, 3], 2] - Power[M, 2]*(9*s*Power[M, 4] + 6*Power[M, 6] + 22*Power[M, 2]*Power[s, 2] - 6*Power[s, 3])*Power[s, 3]* Power[-(M*s) + Power[M, 3], 2] + 4*Power[M, 2]*Power[s, 3]*(10*s*Power[M, 4] - 3*Power[M, 6] - 11*Power[M, 2]*Power[s, 2] + 3*Power[s, 3])*Power[-(M*s) + Power[M, 3], 2] + Power[M, 2]*Power[s, 3]*(21*s*Power[M, 4] - 6*Power[M, 6] - 22*Power[M, 2]*Power[s, 2] + 6*Power[s, 3])*Power[-(M*s) + Power[M, 3], 2]");
        Assert.assertTrue(TensorUtils.equalsExactly(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString5() {
        Tensor t = Tensors.parse("Power[M, 14] + 45*Power[M, 20] + 2*Power[M, 12]*(-3*Power[M, 2] + Power[pT, 2]) + 45*Power[M, 18]*(-2*Power[M, 2] + Power[pT, 2]) + 60*Power[M, 18]*(-3*Power[M, 2] + 2*Power[pT, 2]) - 260*Power[M, 12]*Power[pT, 4] + Power[M, 10]*(16*Power[M, 4] - 11*Power[M, 2]*Power[pT, 2] + 3*Power[pT, 4]) + 12*Power[M, 16]*(17*Power[M, 4] - 24*Power[M, 2]*Power[pT, 2] + 6*Power[pT, 4]) + 3*Power[M, 16]*(34*Power[M, 4] - 41*Power[M, 2]*Power[pT, 2] + 8*Power[pT, 4]) + 3*Power[M, 16]*(51*Power[M, 4] - 59*Power[M, 2]*Power[pT, 2] + 12*Power[pT, 4]) + 6*Power[M, 16]*(51*Power[M, 4] - 70*Power[M, 2]*Power[pT, 2] + 18*Power[pT, 4]) + 184*Power[M, 10]*Power[pT, 6] + 2*Power[M, 8]*(-12*Power[M, 6] + 15*Power[M, 4]*Power[pT, 2] - 5*Power[M, 2]*Power[pT, 4] + Power[pT, 6]) + 9*Power[M, 14]*(-30*Power[M, 6] + 59*Power[M, 4]*Power[pT, 2] - 27*Power[M, 2]*Power[pT, 4] + 3*Power[pT, 6]) + 3*Power[M, 14]*(-60*Power[M, 6] + 118*Power[M, 4]*Power[pT, 2] - 55*Power[M, 2]*Power[pT, 4] + 6*Power[pT, 6]) + 12*Power[M, 14]*(-45*Power[M, 6] + 98*Power[M, 4]*Power[pT, 2] - 54*Power[M, 2]*Power[pT, 4] + 6*Power[pT, 6]) + Power[M, 14]*(-360*Power[M, 6] + 771*Power[M, 4]*Power[pT, 2] - 456*Power[M, 2]*Power[pT, 4] + 48*Power[pT, 6]) + Power[M, 12]*(-270*Power[M, 8] + 1089*Power[M, 6]*Power[pT, 2] - 1365*Power[M, 4]*Power[pT, 4] + 616*Power[M, 2]*Power[pT, 6] - 87*Power[pT, 8]) + Power[M, 12]*(-180*Power[M, 8] + 651*Power[M, 6]*Power[pT, 2] - 696*Power[M, 4]*Power[pT, 4] + 310*Power[M, 2]*Power[pT, 6] - 63*Power[pT, 8]) + Power[M, 6]*(-10*Power[M, 8] + 39*Power[M, 6]*Power[pT, 2] - 33*Power[M, 4]*Power[pT, 4] + 13*Power[M, 2]*Power[pT, 6] - 3*Power[pT, 8]) - 292*Power[M, 8]*Power[pT, 8] + Power[M, 6]*(21*Power[M, 8] - 47*Power[M, 6]*Power[pT, 2] + 22*Power[M, 4]*Power[pT, 4] - 6*Power[M, 2]*Power[pT, 6] + Power[pT, 8]) + 2*Power[M, 12]*(216*Power[M, 8] - 612*Power[M, 6]*Power[pT, 2] + 557*Power[M, 4]*Power[pT, 4] - 180*Power[M, 2]*Power[pT, 6] + 6*Power[pT, 8]) + Power[M, 12]*(216*Power[M, 8] - 591*Power[M, 6]*Power[pT, 2] + 463*Power[M, 4]*Power[pT, 4] - 129*Power[M, 2]*Power[pT, 6] + 6*Power[pT, 8]) + 2*Power[M, 12]*(324*Power[M, 8] - 1008*Power[M, 6]*Power[pT, 2] + 920*Power[M, 4]*Power[pT, 4] - 252*Power[M, 2]*Power[pT, 6] + 9*Power[pT, 8]) + Power[M, 12]*(324*Power[M, 8] - 951*Power[M, 6]*Power[pT, 2] + 769*Power[M, 4]*Power[pT, 4] - 189*Power[M, 2]*Power[pT, 6] + 9*Power[pT, 8]) - 4*Power[M, 12]*(135*Power[M, 8] - 561*Power[M, 6]*Power[pT, 2] + 753*Power[M, 4]*Power[pT, 4] - 377*Power[M, 2]*Power[pT, 6] + 51*Power[pT, 8]) - 2*Power[M, 12]*(180*Power[M, 8] - 651*Power[M, 6]*Power[pT, 2] + 747*Power[M, 4]*Power[pT, 4] - 391*Power[M, 2]*Power[pT, 6] + 72*Power[pT, 8]) + Power[M, 10]*(153*Power[M, 10] - 798*Power[M, 8]*Power[pT, 2] + 1415*Power[M, 6]*Power[pT, 4] - 1041*Power[M, 4]*Power[pT, 6] + 301*Power[M, 2]*Power[pT, 8] - 18*Power[pT, 10]) + 2*Power[M, 10]*(153*Power[M, 10] - 810*Power[M, 8]*Power[pT, 2] + 1476*Power[M, 6]*Power[pT, 4] - 1142*Power[M, 4]*Power[pT, 6] + 344*Power[M, 2]*Power[pT, 8] - 18*Power[pT, 10]) + Power[M, 10]*(102*Power[M, 10] - 492*Power[M, 8]*Power[pT, 2] + 665*Power[M, 6]*Power[pT, 4] - 381*Power[M, 4]*Power[pT, 6] + 133*Power[M, 2]*Power[pT, 8] - 12*Power[pT, 10]) + 2*Power[M, 10]*(102*Power[M, 10] - 486*Power[M, 8]*Power[pT, 2] + 663*Power[M, 6]*Power[pT, 4] - 407*Power[M, 4]*Power[pT, 6] + 149*Power[M, 2]*Power[pT, 8] - 12*Power[pT, 10]) + Power[M, 10]*(-36*Power[M, 10] + 243*Power[M, 8]*Power[pT, 2] - 433*Power[M, 6]*Power[pT, 4] + 271*Power[M, 4]*Power[pT, 6] - 115*Power[M, 2]*Power[pT, 8] + 39*Power[pT, 10]) + 4*Power[M, 10]*(-27*Power[M, 10] + 180*Power[M, 8]*Power[pT, 2] - 425*Power[M, 6]*Power[pT, 4] + 456*Power[M, 4]*Power[pT, 6] - 227*Power[M, 2]*Power[pT, 8] + 42*Power[pT, 10]) + Power[M, 10]*(-72*Power[M, 10] + 483*Power[M, 8]*Power[pT, 2] - 842*Power[M, 6]*Power[pT, 4] + 513*Power[M, 4]*Power[pT, 6] - 218*Power[M, 2]*Power[pT, 8] + 75*Power[pT, 10]) + Power[M, 10]*(-54*Power[M, 10] + 357*Power[M, 8]*Power[pT, 2] - 844*Power[M, 6]*Power[pT, 4] + 898*Power[M, 4]*Power[pT, 6] - 439*Power[M, 2]*Power[pT, 8] + 81*Power[pT, 10]) + Power[M, 8]*(6*Power[M, 12] - 66*Power[M, 10]*Power[pT, 2] + 175*Power[M, 8]*Power[pT, 4] - 127*Power[M, 6]*Power[pT, 6] + 42*Power[M, 4]*Power[pT, 8] - 25*Power[M, 2]*Power[pT, 10] + 6*Power[pT, 12]) + 2*Power[M, 8]*(6*Power[M, 12] - 66*Power[M, 10]*Power[pT, 2] + 173*Power[M, 8]*Power[pT, 4] - 122*Power[M, 6]*Power[pT, 6] + 32*Power[M, 4]*Power[pT, 8] - 23*Power[M, 2]*Power[pT, 10] + 6*Power[pT, 12]) + Power[M, 8]*(9*Power[M, 12] - 84*Power[M, 10]*Power[pT, 2] + 265*Power[M, 8]*Power[pT, 4] - 382*Power[M, 6]*Power[pT, 6] + 276*Power[M, 4]*Power[pT, 8] - 88*Power[M, 2]*Power[pT, 10] + 9*Power[pT, 12]) + 2*Power[M, 8]*(9*Power[M, 12] - 84*Power[M, 10]*Power[pT, 2] + 263*Power[M, 8]*Power[pT, 4] - 374*Power[M, 6]*Power[pT, 6] + 269*Power[M, 4]*Power[pT, 8] - 86*Power[M, 2]*Power[pT, 10] + 9*Power[pT, 12]) - Power[M, 4]*(-2*Power[M, 6] + 10*Power[M, 4]*Power[pT, 2] - Power[M, 2]*Power[pT, 4] + 2*Power[pT, 6])*Power[-Power[M, 2] + Power[pT, 2], 2] + Power[M, 4]*Power[pT, 2]*Power[-Power[M, 2] + Power[pT, 2], 4] + 6*Power[M, 8]*Power[pT, 4]*Power[-Power[M, 2] + Power[pT, 2], 4] + Power[M, 8]*(6*Power[M, 6] - 22*Power[M, 4]*Power[pT, 2] + 21*Power[M, 2]*Power[pT, 4] - 6*Power[pT, 6])*Power[-(pT*Power[M, 2]) + Power[pT, 3], 2] + 4*Power[M, 8]*(3*Power[M, 6] - 11*Power[M, 4]*Power[pT, 2] + 10*Power[M, 2]*Power[pT, 4] - 3*Power[pT, 6])*Power[-(pT*Power[M, 2]) + Power[pT, 3], 2] - Power[M, 8]*(-6*Power[M, 6] + 22*Power[M, 4]*Power[pT, 2] + 9*Power[M, 2]*Power[pT, 4] + 6*Power[pT, 6])*Power[-(pT*Power[M, 2]) + Power[pT, 3], 2] - Power[M, 8]*(-12*Power[M, 6] + 44*Power[M, 4]*Power[pT, 2] + 17*Power[M, 2]*Power[pT, 4] + 12*Power[pT, 6])*Power[-(pT*Power[M, 2]) + Power[pT, 3], 2]");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void testHash1() {
        Tensor a = Tensors.parse("f_mn + h_mn");
        Tensor b = Tensors.parse("f_nm + h_mn");
        Tensor c = Tensors.parse("f_nm + h_nm");
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertEquals(a.hashCode(), c.hashCode());
    }

    @Test
    public void testHash2() {
        Tensor a = Tensors.parse("a_mn + b_mn - c_m*d_n");
        Tensor b = Tensors.parse("-(a_mn - b_mn - c_m*d_n)");
//        Tensor c = Tensors.parse("f_nm + h_nm");
        Assert.assertEquals(a.hashCode(), b.hashCode());
//        Assert.assertEquals(a.hashCode(), c.hashCode());
    }


    @Test
    public void testHash3() throws Exception {
        int a = parse("a + 2*b - c").hashCode();
        int b = parse("-a - 2*b + c").hashCode();
        int c = parse("a - 2*b - c").hashCode();

        assertEquals(a, b);
        assertFalse(a == c);
    }

    @Test
    public void testHash4() throws Exception {
        int a = parse("a + 2*b - c").hashCode();
        int b = parse("-a - 2*b + c").hashCode();
        int c = parse("a - 2*b - c").hashCode();

        assertEquals(a, b);
        assertFalse(a == c);
    }


    @Test
    public void testHash5() throws Exception {
        int a = parse("a + b - c").hashCode();
        int b = parse("-a - b + c").hashCode();
        int c = parse("a - b - c").hashCode();

        assertEquals(a, b);
        assertFalse(a == c);
    }


    @Test
    public void testHash6() throws Exception {
        int a = parse("x**2 - 2*x*y + y**2").hashCode();
        int b = parse("-x**2 + 2*x*y - y**2").hashCode();
        int c = parse("-x**2 - 2*x*y - y**2").hashCode();

        assertEquals(a, b);
        assertFalse(a == c);
    }

    @Test
    public void testHash7() throws Exception {
        int a = parse("a_i + b_i - c_i").hashCode();
        int b = parse("-a_i - b_i + c_i").hashCode();
        int c = parse("a_i - b_i - c_i").hashCode();

        assertEquals(a, b);
        assertFalse(a == c);
    }

    @Test
    public void testHash8() throws Exception {
        int a = parse("a_ij + b_ij - c_ij").hashCode();
        int b = parse("-a_ij - b_ij + c_ij").hashCode();
        int c = parse("a_ij - b_ij - c_ij").hashCode();

        assertEquals(a, b);
        assertEquals(a, c);
    }


    @Test
    public void testHash9() throws Exception {
        int a = parse("1-x").hashCode();
        int b = parse("x-1").hashCode();

        assertEquals(a, b);
    }

    @Test
    public void testHash10() throws Exception {
        Tensor t, d;
        t = parse("1/(1 - x)**2");
        d = parse("2*(-x+1)**(-3)");
        System.out.println(d);
        TAssert.assertEquals(d, "-2/(x-1)**3");
    }

    @Test
    public void testRemove1() {
        Sum t = (Sum) Tensors.parse("a+b+c");
        Assert.assertEquals(t.remove(0).size(), 2);
        Assert.assertEquals(t.remove(1).size(), 2);
        Assert.assertEquals(t.remove(2).size(), 2);
    }

    @Test
    public void testRemove2() {
        Sum t = (Sum) Tensors.parse("a + b + c + d + e + f + g + h + q + w + e + t + o + i");
        int size = t.size();
        Random rnd = new Random();
        int l, j, positions[];
        for (int i = 0; i < 1000; ++i) {
            positions = new int[l = 3 * (rnd.nextInt(size) + 1)];
            for (j = 0; j < l; ++j)
                positions[j] = rnd.nextInt(size);
            System.out.println(Arrays.toString(MathUtils.getSortedDistinct(positions)));
            assertRemoveArray(t, positions);
        }
    }

    private static void assertRemoveArray(Sum sum, int[] positions) {
        Tensor a = sum.remove(positions);
        Tensor b = sum;
        positions = MathUtils.getSortedDistinct(positions);
        for (int i = positions.length - 1; i >= 0; --i) {
            if (b instanceof Sum)
                b = ((Sum) b).remove(positions[i]);
            else b = Complex.ZERO;
        }
        TAssert.assertEquals(a, b);
    }
}
