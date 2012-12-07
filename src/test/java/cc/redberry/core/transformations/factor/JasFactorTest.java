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
 * the Free Software Foundation, either version 2 of the License, or
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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigRational;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.FactorAbstract;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.FactorFactory;
import cc.redberry.core.utils.TensorUtils;

import gnu.trove.map.TIntObjectMap;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.SortedMap;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.transformations.expand.Expand.expand;
import static cc.redberry.core.transformations.factor.JasFactor.*;

public class JasFactorTest extends TestCase {


    @Test
    public void test1() {
        Tensor t = parse("-64*m**6 - 32*m**4*s - 8*m**2*s**2 - s**3");
        t = parse("(4*m**2 + s)*(16*m**4+4*m**2*s+s**2)");
        t = expand(t);

        System.out.println(JasFactor.factor(t));
    }

    @Test
    public void test2() {

        String[] vars = {"a", "b"};


        GenPolynomialRing<BigInteger> fPolyFactory = new GenPolynomialRing<BigInteger>(BigInteger.ONE, vars);

        String pol;
        pol = "64 a^6 + 32 a^4 * b + 8 a^2 * b^2 + b^3";
        pol = "64*a**6 + 32*a**4 * b + 8*a**2 * b**2 + b**3";


        GenPolynomial<BigInteger> polynomial = tensor2Poly(parse(pol));
        System.out.println(polynomial);

        FactorAbstract<BigInteger> factorAbstract = FactorFactory.getImplementation(BigInteger.ONE);
        SortedMap<GenPolynomial<BigInteger>, Long> map = factorAbstract.factors(polynomial);
        for (SortedMap.Entry<GenPolynomial<BigInteger>, Long> mm : map.entrySet()) {
            System.out.println(mm.getKey() + "  " + mm.getValue());
        }


    }


//    @Test
//    public void test2r() {
//        Tensor t = parse("8568300000*e*(5546*Power[b, 2]*c+4550*a*b*Power[c, 2]*d*f*Power[e, 3]+497*e*d*Power[b, 2]*c*f+3968*a*d+3895*a*b*Power[e, 2]+1187*Power[c, 2])*(2149*Power[a, 2]*Power[b, 2]*c*f*Power[d, 2]+7970*a*b*c*Power[d, 2]*Power[e, 3]+6103*Power[b, 2]*c+6165*c*f+8012*a*Power[c, 3]*Power[d, 2]*Power[e, 4]+6418*Power[b, 4]*c*f*Power[e, 4]+1156*a*Power[d, 2]*Power[e, 2]+778*b*e*Power[c, 2]*d*Power[f, 3])*Power[b, 2]*Power[d, 2]*(8882*Power[a, 2]*d*f*Power[e, 2]+210*Power[a, 2]*e*Power[b, 2]*Power[d, 3]+7313*d)");
//        Tensor expand = expand(t);
//        System.out.println(t.toString(OutputFormat.WolframMathematica));
////        System.out.println(expand.toString(OutputFormat.WolframMathematica));
//        Tensor factor = factor(expand);
//        System.out.println(factor);
//        TAssert.assertEquals(expand(factor), expand);
//    }


//
//
//    @Test
//    public void test546() {
//        System.out.println("ХУЙ.");
//
//        String[] vars = new String[]{"b", "d", "c", "a", "e"};
//        IntPermutationsGenerator generator = new IntPermutationsGenerator(vars.length);
//        for (int[] prmutation : generator) {
//
//            final String[] vars1 = Combinatorics.shuffle(vars, prmutation);
//            System.out.println(Arrays.toString(vars1));
//            Thread thread = new Thread() {
//                @Override
//                public void run() {
//
//                    GenPolynomialRing<BigRational> fPolyFactory = new GenPolynomialRing<>(BigRational.ONE, vars1.length, new TermOrder(TermOrder.INVLEX), vars1);
//
//                    String pol;
//                    pol = "4959776*b**7*c**6*d**9*e**5*a+9362592*b**4*c**3*d**7*e**4*a**5+26821872*b**5*c**6*d**8*e**3*a**3+8802864*b**9*c**2*d**7*e**3*a**3+4825728*b**7*c**6*d**8*e**2*a+7050736*b**5*c**8*d**8*e**3*a**2+5078920*b*c**5*d**3*e**3*a+6453216*b**3*c**8*d**4*a**2*e+5359080*b**4*c**3*d**5*e**4+8681904*c**6*d**3*a**4*e+1681280*b**6*c**5*d**5*e**5*a+937860*b**3*c**5*d**4*e**4*a**4+23264712*b**5*c**3*d**6*e**3*a**2+4957260*b**2*c**5*d**5*e**5*a**4+4539456*b**5*c**6*d**5*e**3*a+1453683*b**3*c**5*d**7*e**2*a**6+13534992*b**2*c**3*d**3*e**2*a**2+4447440*b**4*c**2*d**5*a**3*e+7770088*b**3*c**2*d**6*e**2*a**4+7872326*b*c**5*d**6*a**3*e+6386688*b**5*c**8*d**4*e**3*a**3+3538560*b**7*c**4*d**5*e**3*a**3+10461528*c**7*d**3*e**4*a+1520820*b**6*c**3*d**5*e**2+32306688*b**7*c**2*d**5*e**4*a**5+1627920*b**6*c**3*d**4*e**4*a**2+8746632*b**4*c**3*d**8*e**2*a**3+510720*b**8*c**5*d**4*e**5*a**3+776160*b**7*c**8*d**5*e**3*a**2+3173760*b**3*c**2*d**3*e**4*a**5+2334906*b**4*c**3*d**5*a**3*e+6498030*b**2*c**4*d**5*e**2*a**4+22635936*b**5*c**3*d**5*a**2+7618380*b**2*c**5*d**4*e**2*a+6413640*b**4*d**5*e**3*a**5*c+13767552*b**4*c**3*d**4*e**7*a**3+5843376*b*c**6*d**5*a**3*e+2445240*b**8*c**2*d**6*e**3*a**2+2651880*b**6*c**5*d**5*e**4*a**3+10325664*b**2*c**4*d**3*e**5*a**2+2789504*b**6*c**5*d**7*e**5*a**5+4760640*b**4*c**2*d**4*e**3*a**5+5767488*b**3*c**3*d**5*e**2*a**4+6627428*b**3*d**7*e**2*a**7*c+13948704*b**2*c**6*d**4*e**6*a**2+11425536*b**4*c**2*d**4*e**4*a**6+14092848*b**4*c**5*d**7*a**5*e+9422784*b**9*c**2*d**6*e**5*a**5+1407504*b**9*c**6*d**9*e**3*a+1862784*b**7*c**8*d**5*e**4*a**3+2605984*b**6*c**5*d**8*e**3*a**3+739536*b**8*c**5*d**8*a**3*e+8492544*b**7*c**4*d**5*e**4*a**4+10785042*b**2*c**2*d**5*a**3+791616*b**8*c**5*d**7*e**3*a**5+766080*b**9*c**5*d**5*e**4*a**3+25418852*b**7*c**3*d**10*e**5*a**2+27208912*b**7*c**3*d**9*e**7*a**4+13909824*b**6*c**2*d**7*e**2*a**6+5736480*b**4*c**3*d**4*e**6*a**2+4984056*b*c**5*d**6*a**6*e+6052608*b**7*c**5*d**6*e**5*a**2+2826208*b**4*c**8*d**7*e**4*a**4+8974080*b**6*c**2*d**4*e**4*a**4+3656512*b**6*c**4*d**7*e**2*a**5+3585120*b**5*c**7*d**5*e**2*a**2+10178784*c**7*d**2*e*a+3649968*b**6*c**3*d**5*e**3*a+864780*b**5*c**2*d**5*e**3*a**3+9223360*b**6*c**2*d**5*e**7*a**4+12613492*b**4*c**2*d**8*e**4*a**5+14380056*b**4*d**6*e**2*a**4*c+2964960*b**3*c**2*d**4*e**2*a**3+3667860*b**9*c**2*d**7*e**2*a**2+1682184*b**5*c**3*d**6*e**3*a**4+6714631*b*c**4*d**7*a**6*e+2730756*b**6*c**3*d**8*e**5*a**5+30181248*b**7*c**2*d**6*e**2*a**3+31019616*b**7*c**2*d**7*e**5*a**3+14788232*b**4*c**3*d**7*e**4*a**2+3572720*b**3*c**3*d**4*e**5+14017080*b**5*c**5*d**6*e**5*a**3+24731856*b**7*c**3*d**9*e**2*a**2+9546768*b**7*c**5*d**6*e**4*a**4+4892880*b**4*c**2*d**5*e**6*a**5+2453760*b**7*c**5*d**5*e*a+4823280*b**2*c**5*d**4*e**2*a**4+9385632*b**2*c**4*d**3+2737476*b**4*c**4*d**4*e+27566924*b**5*c**6*d**9*e**6*a**3+9749278*b**2*c**6*d**8*e**6*a**4+2930256*b**4*c**4*d**3*e**3*a**2+1085280*b**5*c**3*d**3*e**5*a**2+477120*b**8*c**5*d**5*e**3*a+2551101*b**6*c**3*d**9*e**3*a**3+1388520*b**6*c**2*d**5*e**4*a**5+1799680*b**6*c**5*d**4*e**7*a**3+7823046*b**7*c**6*d**9*e**4*a**3+6959168*b**7*c**5*d**8*e**4*a**3+13571712*b**2*c**6*d**3*e**3*a**2+1717632*b**9*c**5*d**6*e**3*a**2+10046592*b**2*c**4*d**2*e**2*a**2+10976832*b**4*c**4*d**6*e**4*a**2+2661120*b**5*c**8*d**4*e**2*a**2+3215520*b*c**5*d**3*e**3*a**4+1378944*b**7*c**6*d**4*e**3*a**3+715680*b**9*c**5*d**6*e**2*a+4595688*b**3*c**2*d**7*a**5+18284112*b**2*c**5*d**4*e**3*a**2+3332448*b**6*c**2*d**5*e**5*a**6+6564096*b**5*c**8*d**5*e**6*a**3+1774080*b**4*c**8*d**3*e**3*a**2+11281744*b**4*c**4*d**7*e**7*a**2+13713084*c**6*d**3*e*a+18739624*b**6*d**8*e**2*a**6*c+18986198*b**4*c**4*d**8*a**5*e+5055976*b**3*c**2*d**7*e**5*a**7+8569152*b**2*c**3*d**3*e**2*a**5+13395456*b**4*c**3*d**3*e**4*a**3+5122502*b*c**5*d**7*e**4*a**6+4859136*b**5*c**6*d**4*e**5*a**3+11121264*b**2*c**7*d**6*e**3*a+10539524*b**4*c**4*d**8*e**5+2359040*b**6*c**4*d**4*e**4*a**3+5811960*b**2*c**6*d**4*e**5*a+44096976*b**5*c**4*d**6*e**3*a**4+2699520*b**7*c**5*d**5*e**6*a**3+4923072*b**3*c**9*d**4*e**4*a**2+32643216*b**5*c**2*d**5*e**2*a**4+6478848*b**7*c**5*d**5*e**7*a**4+12575520*b**7*c**2*d**6*a**2*e+4683160*b**3*c**2*d**4*e**2+11783657*b**4*c**2*d**9*e**2*a**3+12514176*b**4*c**3*d**4*e**2*a+2990946*b**6*c**4*d**8*e**3+9485784*b**2*c**6*d**7*e**3*a**4+13355668*b**6*c**2*d**9*e**3*a**4+3243702*b**4*c**7*d**7*e**4*a+6303744*b**7*c**5*d**4*e**4*a**4+26473536*b**7*c**3*d**8*e**4*a**4+11430188*b**2*c**7*d**7*e**6*a+925680*b**5*c**2*d**4*e**5*a**5+1649340*b**4*c**6*d**4*e**3*a+6191413*b**3*d**8*a**5*c+8923068*c**6*d**4*e**4*a**4+5991690*b**4*d**6*a**3*e*c+11742912*b**4*c**2*d**5*e**7*a**6+11575872*b**2*c**5*d**4*e**3*a**5+2203840*b**6*c**4*d**5*e**2*a+8227764*b**2*c**3*d**5*e**3*a**3+1406790*b**4*c**5*d**5*e**3*a**4+5233536*b**5*c**9*d**7*e**3*a**2+14982814*b**2*c**6*d**7*e**3*a+1434804*b**5*c**2*d**7*e**3*a**7+5214240*b**4*c**3*d**4*e+2535552*b**6*c**5*d**7*a**3+40660848*b**7*d**7*e**2*a**3*c+14296208*b**6*c**2*d**8*e**5*a**6+1340409*b**5*c**2*d**8*a**5*e+3304840*b*c**5*d**4*e**6*a**4+5309056*b**7*c**6*d**8*e**7*a**3+11544552*b**2*c**2*d**4*e**2*a**5+9646344*b**2*c**4*d**4*e**3+7160076*b**5*c**6*d**5*e**2*a**3+8604288*b**5*c**7*d**5*e**3*a**3+24548832*b**3*c**6*d**4*a**3*e+8616560*b**6*c**2*d**6*e**5*a**2+2968812*b**2*c**7*d**3*e**2*a+8989594*b**4*c**3*d**9*e**5*a**3+15392736*b**4*d**5*e**4*a**6*c+2714112*b**6*c**5*d**6*e**2*a**5+35665736*b**7*c**2*d**9*e**4*a**4+3874640*b*c**6*d**3*e**6*a+12090080*b**6*d**5*e**4*a**4*c+5537716*b**3*c**3*d**7*e**3*a**2+5378912*b**5*c**9*d**8*e**6*a**2+3415952*b**6*c**4*d**8*a**3+3958416*b**4*c**6*d**4*e**4*a**2+3977820*b**7*c**5*d**6*e**3*a**3+36135022*b**5*c**5*d**9*e**3*a**3+14484316*b**4*c**5*d**8*e**4*a**5+5012960*b**3*c**2*d**3*e**4*a**2+12644532*b**2*c**3*d**4+7067088*b**7*c**3*d**5*e**3*a**4+5165568*b**7*c**6*d**7*e**4*a**3+2532222*b**2*c**6*d**4*e**2*a**4+24230016*b**5*c**3*d**4*e**2*a**4+7213458*b**9*c**3*d**10*e**3*a**2+3926160*b**9*c**2*d**6*e**4*a**4+9344720*b**4*c**5*d**5*e**6*a**3+4790016*b**3*c**9*d**3*a**2*e+12861792*b**4*c**3*d**5*e**5*a+10254672*b**4*c**4*d**7*e**2+1013880*b**5*c**3*d**4*e**3+7721448*b**9*c**3*d**9*e**5*a**4+8807184*b**2*c**3*d**4*e**5*a**5+3994460*b**3*d**5*e**2*a**3*c+33072732*b**3*c**5*d**5*a**3*e+5581440*b**4*c**3*d**3*e**3*a**2+18046656*b**4*c**2*d**4*e**4*a**3+18135120*b**7*d**6*e**3*a**4*c+1751040*b**6*c**5*d**3*e**4*a**3+1526448*b**7*c**9*d**8*e**4*a**2+33640992*b**5*c**5*d**6*e**6*a**4+3305760*b**7*c**4*d**6*e*a+6602148*b**7*c**3*d**6*a**2*e+33204096*b**7*c**2*d**6*e**7*a**5+4416768*b**5*c**6*d**4*a+2617440*b**8*c**2*d**5*e**5*a**4+33319306*b**7*c**2*d**10*e**2*a**2+5950368*b**5*c**5*d**5*a+12249160*b**4*c**4*d**5*e**3*a**3+5927696*b**3*c**3*d**6*e**5*a**4+7258898*b**3*c**2*d**7*a**2+43524288*b**7*d**6*e**4*a**5*c+3201576*b**6*c**4*d**7*e**5*a**2+1288224*b**7*c**6*d**5*e*a+12924840*b**7*c**2*d**7*e**4*a**2+3907008*b**6*c**3*d**4*e**5*a**3+1823360*b**4*c**8*d**4*e**6*a**2+13461120*b**7*c**2*d**5*e**3*a**4+5654880*b**2*c**6*d**3*e**2*a+1099560*b**3*c**6*d**3*e**4*a+2499336*b**4*c**3*d**4*e**3*a**5+3376296*b**4*c**5*d**5*e**4*a**5+9092160*b**4*c**5*d**4*e**3*a**3+13835040*b**7*c**2*d**6*e**6*a**4+8005392*b**2*c**3*d**4*a**3+11696454*c**5*d**4*a**4*e+3720960*b**3*c**3*d**2*e**4*a**2+3769920*b*c**6*d**2*e**3*a+6501328*b**7*c**5*d**9*e**2*a+7933824*b**7*c**4*d**6*e**2*a**2+2521920*b**7*c**5*d**6*e**4*a+517440*b**6*c**8*d**4*e**4*a**2+1838592*b**9*c**5*d**5*e**5*a**4+4275760*b**3*d**4*e**4*a**5*c+3824320*b**3*c**3*d**3*e**7*a**2+13815322*b**4*c**3*d**8*e**2+17506754*b**6*d**9*a**4*c+7024740*b**4*c**2*d**5*e+2749824*b**4*c**8*d**6*a**4*e+16942020*b**7*d**7*a**2*e*c+1704318*b**3*c**6*d**6*e**2*a**3+4110414*b**6*c**5*d**8*e**2*a**5+3261920*b**3*c**2*d**4*e**7*a**5+4723346*b**3*c**2*d**8*e**3*a**5+24903072*b**5*c**3*d**5*e**5*a**4+15595272*b**2*c**4*d**5*e**3*a**5+6005692*b*c**6*d**6*e**4*a**3+8383680*b**6*c**2*d**5*e**2*a**2+1635840*b**6*c**5*d**4*e**2*a+12779459*b**2*c**5*d**8*e**3*a**4+1571514*b**5*c**3*d**7*a**2*e+12994704*b**6*c**2*d**8*a**4+11897424*b**2*c**5*d**5*e**6*a**5+16859376*b**4*c**2*d**5*e**2*a+10673856*b**4*c**2*d**5*e**2*a**4+3113208*b**6*c**2*d**6*e**3*a**4+4332020*b*c**4*d**4*e**3*a**4+18373740*b**5*c**4*d**6*e**2*a**3+3476160*b**3*c**3*d**3*e**2+2735040*b**5*c**8*d**5*e**5*a**2+802032*b**6*c**8*d**7*e**2*a**4+30495636*b**5*c**2*d**6*a**2+25230744*b**3*c**6*d**5*e**4*a**3+4057032*b**8*c**2*d**8*e**3*a**6+4919328*b**3*c**2*d**6*e**2*a**7+3704624*b**4*c**7*d**7*a**4*e+32731776*b**5*c**5*d**5*e**3*a**4+9622664*b**4*c**3*d**8*e**7*a**5+2766687*b**4*c**6*d**8*e**4*a**4+10970352*b**4*c**2*d**6*e**5*a**4+7519440*b**4*c**2*d**4*e**3*a**2+4727808*b**5*c**6*d**3*e**2*a**3+2626560*b**7*c**5*d**4*e**3*a**3+5388048*b**3*c**3*d**6*a**2+5889024*b**7*c**5*d**5*e**2*a**2+3790122*b**8*c**2*d**9*a**4*e+13638240*b**5*c**5*d**5*e**2*a**3+11294680*b**6*d**6*e**2*a**2*c+2390080*b**4*c**7*d**4*e**3*a**2+1297170*b**6*c**2*d**6*e**2*a**3+4570980*b**4*c**2*d**6*e**4*a**3+6369408*b**5*c**5*d**4*e**2*a**3+1397088*b**5*c**9*d**4*e**2*a**2+1506624*b**9*c**6*d**8*e**5*a**3+3047320*b**3*c**2*d**5*e**5*a**3".replace("**", "^");
//
//
//                    GenPolynomial<BigRational> polynomial = fPolyFactory.parse(pol);//"kn1^2*kn2^2*M^10*t^2*1/64");//"kn1^2*kn2^2*M^10*t^2*1/64 - kn1*kn2^2*kn3*M^10*t^2*1/32 + kn2^2*kn3^2*M^10*t^2*1/64 - kn1^2*kn2^2*M^8*t^3*1/16 + kn1*kn2^2*kn3*M^8*t^3*1/8 - kn2^2*kn3^2*M^8*t^3*1/16 + 3*kn1^2*kn2^2*M^6*t^4*1/32 - 3*kn1*kn2^2*kn3*M^6*t^4*1/16 + 3*kn2^2*kn3^2*M^6*t^4*1/32 - kn1^2*kn2^2*M^4*t^5*1/16 + kn1*kn2^2*kn3*M^4*t^5*1/8 - kn2^2*kn3^2*M^4*t^5*1/16 + kn1^2*kn2^2*M^2*t^6*1/64 - kn1*kn2^2*kn3*M^2*t^6*1/32 + kn2^2*kn3^2*M^2*t^6*1/64");
//
//                    FactorAbstract<BigRational> factorAbstract = FactorFactory.getImplementation(BigRational.ONE);
//                    for (int i = 0; i < 100; ++i) {
//                        SortedMap<GenPolynomial<BigRational>, Long> map = factorAbstract.factors(polynomial);
//                    }
//                }
//            };
//            long s = System.currentTimeMillis();
//            thread.start();
//            try {
//                thread.join(6000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//            if (!thread.isAlive())
//                thread.stop();
//            System.out.println("Time: " + (System.currentTimeMillis() - s) + "ms");
//        }
//    }
}
