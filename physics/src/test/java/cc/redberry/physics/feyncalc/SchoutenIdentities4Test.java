/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * Created by poslavsky on 03/01/16.
 */
@Ignore
public class SchoutenIdentities4Test {
    @Test
    public void test1() throws Exception {
        setAntiSymmetric("e_abcd");
        SchoutenIdentities4 tr = new SchoutenIdentities4(parseSimple("e_abcd"));
        Tensor t;
        t = parse("-2*g_{ad}*e_{bcef}+2*g_{ac}*e_{bdef}-2*g_{ab}*e_{cdef}-2*g_{af}*e_{bcde}+2*g_{ae}*e_{bcdf} + f_aebcdf");
        TAssert.assertEquals("f_aebcdf", tr.transform(t));

        t = parse("-2*g_{ad}*e_{bcef}+2*g_{ac}*e_{bdef}-2*g_{ab}*e_{cdef}-2*g_{af}*e_{bcde}+g_{ae}*e_{bcdf} + f_aebcdf");
        TAssert.assertTrue(t == tr.transform(t));

        t = parse("2*g_{ad}*e_{bcef}+2*g_{ac}*e_{bdef}-2*g_{ab}*e_{cdef}-2*g_{af}*e_{bcde}+2*g_{ae}*e_{bcdf} + f_aebcdf");
        TAssert.assertTrue(t == tr.transform(t));

        t = parse("2*g_{ad}*e_{bcef}-2*g_{ac}*e_{bdef}+2*g_{ab}*e_{cdef}+2*g_{af}*e_{bcde}+2*g_{ae}*e_{cbdf} + f_aebcdf");
        TAssert.assertEquals("f_aebcdf", tr.transform(t));
    }

    @Test
    public void test2() throws Exception {
        setAntiSymmetric("e_abcd");
        SchoutenIdentities4 tr = new SchoutenIdentities4(parseSimple("e_abcd"));
        Tensor a = parse("(-4*I)*g_{cf}*e_{abde}+(-4*I)*g_{af}*e_{bcde}+(-4*I)*g_{ab}*e_{cdef}+(-4*I)*g_{be}*e_{acdf}+(4*I)*g_{bf}*e_{acde}+(4*I)*g_{ac}*e_{bdef}+(4*I)*g_{ce}*e_{abdf}+(-4*I)*g_{ef}*e_{abcd}+(4*I)*g_{ae}*e_{bcdf}+(-4*I)*g_{bc}*e_{adef}");
        Tensor b = parse("(-4*I)*g_{fd}*e_{ecba}+(4*I)*g_{cf}*e_{deba}+(-4*I)*g_{bd}*e_{efca}+(4*I)*g_{be}*e_{dfca}+(4*I)*g_{bf}*e_{ceda}+(-4*I)*g_{cd}*e_{bfea}+(-4*I)*g_{ce}*e_{dfba}+(-4*I)*g_{ef}*e_{bdca}+(4*I)*g_{bc}*e_{defa}+(4*I)*g_{ed}*e_{bfca}");
        Tensor c = subtract(a,b);
        c = tr.transform(c);
        System.out.println(c);


        System.out.println(a);
        System.out.println(b);
//        System.out.println(TensorUtils.equals(a, b));
        a = tr.transform(a);
        System.out.println(a);
        b = tr.transform(b);
        System.out.println(b);


        System.out.println(TensorUtils.equals(a, b));
    }
}