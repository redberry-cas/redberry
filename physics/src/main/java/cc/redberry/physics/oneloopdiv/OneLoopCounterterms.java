/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

package cc.redberry.physics.oneloopdiv;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.parser.ParseTokenSimpleTensor;
import cc.redberry.core.parser.preprocessor.IndicesInsertion;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.Transformer;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;

/**
 * This class is a container of the calculated one-loop counterterms.
 * It has no constructors and can be created using the static
 * method {@link #calculateOneLoopCounterterms(OneLoopInput)}, which
 * performs the whole calculation of the one-loop counterterms.
 * <p>Here is the example of the calculation of one-loop counterterms
 * of vector field: </p>
 * <pre>
 *      //setting symmetries to tensor P
 *      Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);
 *
 *      //input expressions
 *      Expression iK = Tensors.parseExpression("iK_a^b=d_a^b+ga*n_a*n^b");
 *      Expression K = Tensors.parseExpression("K^{lm}_a^{b}=g^{lm}*d_{a}^{b}-ga/(2*(1+ga))*(g^{lb}*d_a^m+g^{mb}*d_a^l)");
 *      Expression S = Tensors.parseExpression("S^p^l_m=0");
 *      Expression W = Tensors.parseExpression("W^{a}_{b}=P^{a}_{b}+ga/(2*(1+ga))*R^a_b");
 *      //F is equal to Riemann for vector field
 *      Expression F = Tensors.parseExpression("F_lmab=R_lmab");
 *
 *      //tensors M and N are null, since operator order is 2
 *      OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);
 *
 *      //performing the main calculation
 *      OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
 *      Tensor counterterms = action.counterterms();
 *      //here some transformations can be performed to simplify counterterms
 *      ...
 *      System.out.println(counterterms);
 * </pre>
 * The above code will produce the counterterms, which after some
 * simplifications can be written in form
 * <pre>
 *     (1/24*ga**2+1/4*ga+1/2)*P_\mu\nu*P^\mu\nu + 1/48*ga**2*P**2 + (1/12*ga**2+1/3*ga)*R_\mu\nu*P^\mu\nu +
 *     +(1/24*ga**2+1/12*ga+1/6)*R*P + (1/24*ga**2+1/12*ga-4/15)*R_\mu\nu*R^\mu\nu + (1/48*ga**2+1/12*ga+7/60)*R**2
 * </pre>
 * The divergent part of the one-loop effective action can be obtained by
 * multiplying the resulting counterterms on factor 1/(16*\pi**2*(d-4)) and
 * integrating over the space volume.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class OneLoopCounterterms {

    private static final String Flat_ =
            "Flat="
                    + "(1/4)*HATS*HATS*HATS*HATS-HATW*HATS*HATS+(1/2)*HATW*HATW+HATS*HATN-HATM+(L-2)*NABLAS_l*HATW^l"
                    + "-L*NABLAS_l*HATW*HATK^l+(1/3)*((L-1)*NABLAS_l^l*HATS*HATS-L*NABLAS_l*HATK^l*HATS*HATS"
                    + "-(L-1)*NABLAS_l*HATS*HATS^l+L*NABLAS_l*HATS*HATS*HATK^l)-(1/2)*NABLAS_l*NABLAS_m*DELTA^{lm}"
                    + "-(1/4)*(L-1)*(L-2)*NABLAS_l*NABLAS_m^{lm}+(1/2)*L*(L-1)*(1/2)*(NABLAS_l*NABLAS_{m }^{m}"
                    + "+NABLAS_{m }*NABLAS_{l }^{m})*HATK^l";
    private static final String WR_ =
            "WR="
                    + "-(1/2)*Power[L,2]*HATW*HATF_{lm}*Kn^l*HATK^m"
                    + "+(1/3)*L*HATW*HATK^a*DELTA^{lm}*n_q*R^q_{lam}"
                    + "+(1/3)*Power[L,2]*(L-1)*HATW*HATK^{lm}*HATK^a*n_q*R^q_{lam}"
                    + "-(1/6)*(L-2)*(L-3)*HATW^{lm}*R_{lm}";
    private static final String SR_ = "SR=-(1/6)*Power[L,2]*(L-1)*HATS*NABLAF_{lam}*Kn^{lm}*HATK^a"
            + "+(2/3)*L*HATS*NABLAF_{lma}*Kn^a*DELTA^{lm}"
            + "-(1/12)*(L-1)*(L-2)*(L-3)*HATS^{alm}*NABLAR_{alm}"
            + "-(1/12)*Power[L,2]*(L-1)*(L-2)*HATS*HATK^{lma}*HATK^b*n_q*NABLAR_a^q_{lbm}"
            + "+L*(L-1)*HATS*HATK^{lm}*DELTA^{ab}*n_q*((5/12)*NABLAR_a^q_{mbl}"
            + "-(1/12)*NABLAR_{l}^q_{amb})"
            + "-(1/2)*L*HATS*HATK^b*DELTA^{lma}*n_q*NABLAR_{a}^{q}_{lbm}";
    private static final String SSR_ = "SSR=-(1/2)*L*(L-1)*HATS*HATS^l*HATF_{lm}*HATK^{m}+(1/2)*Power[L,2]*HATS*HATS*HATF_{lm}*Kn^{l}*HATK^m"
            + "+(1/12)*(L-1)*(L-2)*HATS*HATS^{lm}*R_{lm}+(1/3)*L*(L-1)*HATS*HATS^l*HATK^m*R_{lm}"
            + "+(1/6)*HATS*HATS*DELTA^{lm}*R_{lm}-(1/6)*L*(L-1)*(L-2)*HATS*HATS^{lm}*HATK^a*n_q*R^q_{lam}"
            + "+(1/3)*(L-1)*HATS*HATS^a*DELTA^{lm}*n_q*R^q_{lam}"
            + "-(1/3)*Power[L,2]*(L-1)*HATS*HATS*HATK^{lm}*HATK^a*n_q*R^q_{lam}"
            + "-(1/3)*L*HATS*HATS*HATK^a*DELTA^{lm}*n_q*R^q_{lam}";
    //    public static final String FF_ =
//            "FF="
//            + "-(1/24)*L*L*(L-1)*(L-1)*HATK^{lm}*F_{la}*HATK^{ab}*F_{mb}"
//            + "+(1/24)*L*L*HATK^l*F_{bm}*DELTA^{ab}*HATK^m*F_{al}"
//            + "+(5/24)*L*L*HATK^l*F_{bl}*DELTA^{ab}*HATK^m*F_{am}"
//            + "-(1/48)*L*L*(L-1)*HATK^l*F_{bm}*DELTA^m*HATK^{ab}*F_{al}"
//            + "-(1/48)*L*L*(L-1)*HATK^l*F_{bl}*DELTA^m*HATK^{ab}*F_{am}";
    private static final String FF_ =
            "FF="
                    + "-(1/24)*L*L*(L-1)*(L-1)*HATK^{lm}*F_{la}*HATK^{ab}*F_{mb}"
                    + "+(1/24)*L*L*HATK^l*F_{bm}*DELTA^{ab}*HATK^m*F_{al}"
                    + "-(5/24)*L*L*HATK^l*F_{bl}*DELTA^{ab}*HATK^m*F_{am}"
                    + "-(1/48)*L*L*(L-1)*HATK^l*F_{bm}*DELTA^m*HATK^{ab}*F_{al}"
                    + "-(1/48)*L*L*(L-1)*HATK^l*F_{bl}*DELTA^m*HATK^{ab}*F_{am}";
    private static final String FR_ =
            "FR="
                    + "(1/40)*Power[L,2]*(L-1)*(L-2)*DELTA^l*HATK^m*HATK^{abc}*F_{la}*n_q*R^q_{cbm}"
                    + "-Power[L,2]*(L-1)*(L-2)*DELTA^m*HATK^{abc}*HATK^l*n_q*((1/60)*R^q_{bcl}*F_{am}"
                    + "+(1/12)*R^q_{bcm}*F_{al})"
                    + "+Power[L,2]*Power[(L-1),2]*DELTA^a*HATK^{bc}*HATK^{lm}*n_q*((1/60)*R^q_{blc}*F_{am}"
                    + "+(1/20)*R^q_{alc}*F_{mb}+(1/15)*R^q_{cla}*F_{mb}"
                    + "+(1/60)*R^q_{lmc}*F_{ab})+Power[L,2]*(L-1)*DELTA^{ab}*HATK^{cd}*HATK^{l}"
                    + "*n_q*((4/15)*R^q_{dbc}*F_{al}-(1/30)*R^q_{bda}*F_{cl}"
                    + "-(1/15)*R^q_{acl}*F_{bd}-(1/30)*R^q_{cal}*F_{bd})"
                    + "+Power[L,2]*(L-1)*DELTA^{ab}*HATK^c*HATK^{lm}*n_q*((7/60)*R^q_{abl}*F_{cm}"
                    + "-(11/60)*R^q_{blc}*F_{am}+(1/5)*R^q_{lac}*F_{bm}"
                    + "+(1/60)*R^q_{lam}*F_{cb})"
                    + "+Power[L,2]*DELTA^{lab}*HATK^c*HATK^m*n_q"
                    + "*((7/20)*R^q_{acb}*F_{ml}+(1/10)*R^q_{abm}*F_{cl})";
    //    public static final String RR_ =
//            "RR="
//          + "+Power[L,2]*(L-1)*HATK^d*DELTA^{abc}*HATK^{lm}*n_q*n_p*"
//            + "((-1/10)*R^p_{lcm}*R^q_{adb}+(1/15)*R^p_{dam}*R^q_{blc}+(1/60)*R^p_{bdm}*R^q_{cla})"
//           
//            ;
    private static final String RR_ =
            "RR="
                    + "(1/10)*Power[L,2]*HATK^d*DELTA^{lmab}*HATK^c*n_q*n_p*R^q_{abc}*R^p_{lmd}"
                    + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{bcd}*DELTA^a*HATK^{lm}*n_q*n_p*"
                    + "((2/45)*R^p_{adm}*R^q_{blc}-(1/120)*R^p_{dam}*R^q_{blc})"
                    + "+Power[L,2]*(L-1)*HATK^d*DELTA^{abc}*HATK^{lm}*n_q*n_p*"
                    + "((-1/10)*R^p_{lcm}*R^q_{adb}+(1/15)*R^p_{dam}*R^q_{blc}+(1/60)*R^p_{bdm}*R^q_{cla})"
                    + "+Power[L,2]*Power[(L-1),2]*HATK^{cd}*DELTA^{ab}*HATK^{lm}*n_q*n_p*"
                    + "(-(1/20)*R^p_{lbm}*R^q_{dac}+(1/180)*R^p_{amb}*R^q_{cdl}-(7/360)*R^p_{lcm}*R^q_{adb}-(1/240)*R^p_{dbm}*R^q_{cal}-(1/120)*R^p_{bcm}*R^q_{adl}-(1/30)*R^p_{dbm}*R^q_{acl})"
                    + "+Power[L,2]*(L-1)*(L-2)*HATK^d*DELTA^{lm}*HATK^{abc}*n_q*n_p*"
                    + "((-1/30)*R^p_{cmb}*R^q_{adl}-(1/180)*R^p_{lcm}*R^q_{abd}+(1/180)*R^p_{lcd}*R^q_{abm})"
                    + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{lm}*DELTA^{d}*HATK^{abc}*n_q*n_p*"
                    + "((1/45)*R^p_{lcm}*R^q_{abd}-(1/80)*R^p_{bmc}*R^q_{lad}+(1/90)*R^p_{bmc}*R^q_{dal})"
                    + "+Power[L,2]*(L-1)*HATK^{lm}*DELTA^{abc}*HATK^d*n_q*n_p*"
                    + "((7/120)*R^p_{bcm}*R^q_{lad}-(3/40)*R^p_{bcd}*R^q_{lam}+(1/120)*R^p_{dcm}*R^q_{abl})"
                    + "+Power[L,2]*(L-1)*(L-2)*HATK^{abc}*DELTA^{lm}*HATK^d*n_q*n_p*"
                    + "(-(1/24)*R^p_{lcm}*R^q_{abd}-(1/180)*R^p_{mcd}*R^q_{abl}-(1/360)*R^p_{dcm}*R^q_{abl})"
                    + "-(1/120)*Power[L,2]*(L-1)*(L-2)*(L-3)*HATK^{lmab}*DELTA^{d}*HATK^c*n_q*n_p*R^p_{abc}*R^q_{lmd}"
                    + "-(1/80)*Power[L,2]*Power[(L-1),2]*(L-2)*(L-3)*HATK^{abcd}*HATK^{lm}*n_q*n_p*R^p_{bcl}*R^q_{adm}"
                    + "+Power[L,2]*HATK^l*DELTA^{abc}*HATK^m*n_p*(-(1/8)*R_{bc}*R^p_{mal}+(3/20)*R_{bc}*R^p_{lam}+(3/40)*R_{al}*R^p_{bcm}+(1/40)*R^q_{bcl}*R^p_{maq}-(3/20)*R^q_{abl}*R^p_{cmq}+(1/10)*R^q_{abm}*R^p_{clq})"
                    + "+Power[L,2]*(L-1)*HATK^c*DELTA^{ab}*HATK^{lm}*n_p*"
                    + "((1/20)*R_{am}*R^p_{cbl}+(1/20)*R_{ac}*R^p_{lbm}+(1/10)*R_{ab}*R^p_{lcm}+(1/20)*R^q_{amc}*R^p_{qbl}-(1/60)*R^q_{lam}*R^p_{bqc}+(1/10)*R^q_{abc}*R^p_{lqm}-(1/12)*R^q_{abm}*R^p_{lqc})"
                    + "+Power[L,2]*Power[(L-1),2]*HATK^{ab}*DELTA^{c}*HATK^{lm}*n_p*"
                    + "((1/60)*R_{al}*R^p_{bmc}-(1/20)*R_{al}*R^p_{cmb}+(1/120)*R_{ab}*R^p_{lmc}+(3/40)*R_{ac}*R^p_{mbl}+(1/20)*R^q_{cla}*R^p_{mqb}+(1/120)*R^q_{alc}*R^p_{bmq}-(1/40)*R^q_{alc}*R^p_{qmb}+(1/40)*R^q_{alb}*R^p_{qmc}-(1/20)*R^q_{alb}*R^p_{cmq}-(1/40)*R^q_{lbm}*R^p_{cqa})"
                    + "+Power[L,2]*(L-1)*HATK^{ab}*DELTA^{lm}*HATK^{c}*n_p*"
                    + "((1/20)*R^q_{lmb}*R^p_{cqa}-(7/60)*R^q_{bla}*R^p_{cmq}+(1/20)*R^q_{bla}*R^p_{qmc}+(1/10)*R^q_{lbc}*R^p_{maq}+(1/60)*R^q_{blc}*R^p_{amq}+(7/120)*R_{ab}*R^p_{mcl}+(11/60)*R_{bl}*R^p_{mac})"
                    + "+Power[L,2]*(L-1)*(L-2)*HATK^{abc}*DELTA^{l}*HATK^{m}*n_p*"
                    + "((7/240)*R_{ab}*R^p_{clm}+(7/240)*R_{am}*R^p_{bcl}-(1/60)*R_{al}*R^p_{bcm}-(1/24)*R^q_{abm}*R^p_{qcl}+(1/15)*R^q_{abm}*R^p_{lcq}+(1/40)*R^q_{abl}*R^p_{qcm}+(1/40)*R_{bc}*R^p_{mla}+(1/48)*R^q_{bcl}*R^p_{maq})"
                    + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{abc}*HATK^{lm}*n_p*"
                    + "((-7/240)*R_{al}*R^p_{bcm}+(1/240)*R_{bc}*R^p_{lam}-(1/40)*R^q_{alb}*R^p_{mcq})"
                    + "+L*(L-1)*(L-2)*(L-3)*HATK^{lmab}*"
                    + "((1/180)*R_{lm}*R_{ab}+(7/720)*R^q_{abp}*R^p_{lmq})";
    //    public static final String RR_ =
//            "RR="
//            + "(1/10)*Power[L,2]*HATK^d*DELTA^{lmab}*HATK^c*n_q*n_p*R^q_{abc}*R^p_{lmd}"
//            /*
//             * (L-2)
//             */ + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{bcd}*DELTA^a*HATK^{lm}*n_q*n_p*"
//            + "((2/45)*R^p_{adm}*R^q_{blc}-(1/120)*R^p_{dam}*R^q_{blc})"
//            + "+Power[L,2]*(L-1)*HATK^d*DELTA^{abc}*HATK^{lm}*n_q*n_p*"
//            + "((-1/10)*R^p_{lcm}*R^q_{adb}+(1/15)*R^p_{dam}*R^q_{blc}+(1/60)*R^p_{bdm}*R^q_{cla})"
//            + "+Power[L,2]*Power[(L-1),2]*HATK^{cd}*DELTA^{ab}*HATK^{lm}*n_q*n_p*"
//            + "(-(1/20)*R^p_{lbm}*R^q_{dac}+(1/180)*R^p_{amb}*R^q_{cdl}-(7/360)*R^p_{lcm}*R^q_{adb}-(1/240)*R^p_{dbm}*R^q_{cal}-(1/120)*R^p_{bcm}*R^q_{adl}-(1/30)*R^p_{dbm}*R^q_{acl})"
//            /*
//             * (L-2)
//             */ + "+Power[L,2]*(L-1)*(L-2)*HATK^d*DELTA^{lm}*HATK^{abc}*n_q*n_p*"
//            + "((-1/30)*R^p_{cmb}*R^q_{adl}-(1/180)*R^p_{lcm}*R^q_{abd}+(1/180)*R^p_{lcd}*R^q_{abm})"
//            /*
//             * (L-2)
//             */ + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{lm}*DELTA^{d}*HATK^{abc}*n_q*n_p*"
//            + "((1/45)*R^p_{lcm}*R^q_{abd}-(1/80)*R^p_{bmc}*R^q_{lad}+(1/90)*R^p_{bmc}*R^q_{dal})"
//            + "+Power[L,2]*(L-1)*HATK^{lm}*DELTA^{abc}*HATK^d*n_q*n_p*"
//            + "((7/120)*R^p_{bcm}*R^q_{lad}-(3/40)*R^p_{bcd}*R^q_{lam}+(1/120)*R^p_{dcm}*R^q_{abl})"
//            /*
//             * (L-2)
//             */ + "+Power[L,2]*(L-1)*(L-2)*HATK^{abc}*DELTA^{lm}*HATK^d*n_q*n_p*"
//            + "(-(1/24)*R^p_{lcm}*R^q_{abd}-(1/180)*R^p_{mcd}*R^q_{abl}-(1/360)*R^p_{dcm}*R^q_{abl})"
//            /*
//             * (L-2)
//             *//*
//             * kv reduce
//             */ + "-(1/120)*Power[L,2]*(L-1)*(L-2)*(L-3)*HATK^d*DELTA^{c}*HATK^{lmab}*n_q*n_p*R^p_{abc}*R^q_{lmd}"
//            ///*(L-2)*//*kv paper*/    + "-(1/120)*Power[L,2]*(L-1)*(L-2)*(L-3)*HATK^{lmab}*DELTA^{d}*HATK^c*n_q*n_p*R^p_{abc}*R^q_{lmd}"
//
//            /*
//             * (L-2)
//             *//*
//             * kv reduce
//             */ + "-(1/80)*Power[L,2]*Power[(L-1),2]*(L-2)*(L-3)*HATK^{abcd}*HATK^{lm}*n_q*n_p*R^p_{cbl}*R^q_{adm}"
//            ///*(L-2)*//*kv paper*/    + "-(1/80)*Power[L,2]*Power[(L-1),2]*(L-2)*(L-3)*HATK^{abcd}*HATK^{lm}*n_q*n_p*R^p_{bcl}*R^q_{adm}"
//
//            + "+Power[L,2]*HATK^l*DELTA^{abc}*HATK^m*n_p*(-(1/8)*R_{bc}*R^p_{mal}+(3/20)*R_{bc}*R^p_{lam}+(3/40)*R_{al}*R^p_{bcm}+(1/40)*R^q_{bcl}*R^p_{maq}-(3/20)*R^q_{abl}*R^p_{cmq}+(1/10)*R^q_{abm}*R^p_{clq})"
//            + "+Power[L,2]*(L-1)*HATK^c*DELTA^{ab}*HATK^{lm}*n_p*"
//            + "((1/20)*R_{am}*R^p_{cbl}+(1/20)*R_{ac}*R^p_{lbm}+(1/10)*R_{ab}*R^p_{lcm}+(1/20)*R^q_{amc}*R^p_{qbl}-(1/60)*R^q_{lam}*R^p_{bqc}+(1/10)*R^q_{abc}*R^p_{lqm}-(1/12)*R^q_{abm}*R^p_{lqc})"
//            + "+Power[L,2]*Power[(L-1),2]*HATK^{ab}*DELTA^{c}*HATK^{lm}*n_p*"
//            + "((1/60)*R_{al}*R^p_{bmc}-(1/20)*R_{al}*R^p_{cmb}+(1/120)*R_{ab}*R^p_{lmc}+(3/40)*R_{ac}*R^p_{mbl}+(1/20)*R^q_{cla}*R^p_{mqb}+(1/120)*R^q_{alc}*R^p_{bmq}-(1/40)*R^q_{alc}*R^p_{qmb}+(1/40)*R^q_{alb}*R^p_{qmc}-(1/20)*R^q_{alb}*R^p_{cmq}-(1/40)*R^q_{lbm}*R^p_{cqa})"
//            + "+Power[L,2]*(L-1)*HATK^{ab}*DELTA^{lm}*HATK^{c}*n_p*"
//            + "((1/20)*R^q_{lmb}*R^p_{cqa}-(7/60)*R^q_{bla}*R^p_{cmq}+(1/20)*R^q_{bla}*R^p_{qmc}+(1/10)*R^q_{lbc}*R^p_{maq}+(1/60)*R^q_{blc}*R^p_{amq}+(7/120)*R_{ab}*R^p_{mcl}+(11/60)*R_{bl}*R^p_{mac})"
//            /*
//             * (L-2)
//             */ + "+Power[L,2]*(L-1)*(L-2)*HATK^{abc}*DELTA^{l}*HATK^{m}*n_p*"
//            + "((7/240)*R_{ab}*R^p_{clm}+(7/240)*R_{am}*R^p_{bcl}-(1/60)*R_{al}*R^p_{bcm}-(1/24)*R^q_{abm}*R^p_{qcl}+(1/15)*R^q_{abm}*R^p_{lcq}+(1/40)*R^q_{abl}*R^p_{qcm}+(1/40)*R_{bc}*R^p_{mla}+(1/48)*R^q_{bcl}*R^p_{maq})"
//            /*
//             * (L-2)
//             *//*
//             * kv reduce
//             */ + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{lm}*HATK^{abc}*n_p*"
//            ///*(L-2)*//*kv paper*/   + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{abc}*HATK^{lm}*n_p*"
//            + "((-7/240)*R_{al}*R^p_{bcm}+(1/240)*R_{bc}*R^p_{lam}-(1/40)*R^q_{alb}*R^p_{mcq})"
//            /*
//             * (L-2)
//             */ + "+L*(L-1)*(L-2)*(L-3)*HATK^{lmab}*"
//            + "((1/180)*R_{lm}*R_{ab}+(7/720)*R^q_{abp}*R^p_{lmq})";
    //From KV
    //public static final String RR_ = "RR=L**2/10 *(R^{q}_{a b c }*R^{p}_{l m d } *n_{q}*n_{p})*HATK^{d  }*DELTA^{l m a b  }*HATK^{c  } + L**2*(L-1)**2*(L-2)*n_{q}*n_{p} *(2/45*R^{p}_{a d m }*R^{q}_{b l c }-1/120*R^{p}_{d a m }*R^{q}_{b l c }) *HATK^{b c d  }*DELTA^{a  }*HATK^{l m  } + L**2*(L-1)*n_{p}*n_{q} *(-1/10*R^{q}_{l c m }*R^{p}_{a d b }+1/15*R^{q}_{d a m }*R^{p}_{b l c } +1/60*R^{q}_{b d m }*R^{p}_{c l a }) *HATK^{d  }*DELTA^{a b c  }*HATK^{l m  } + L**2*(L-1)**2*n_{q}*n_{p} *(-1/20*R^{p}_{l b m }*R^{q}_{d a c }+1/180*R^{p}_{a m b }*R^{q}_{c d l } -7/360*R^{p}_{l c m }*R^{q}_{a d b }-1/240*R^{p}_{d b m }*R^{q}_{c a l } -1/120*R^{p}_{b c m }*R^{q}_{a d l }-1/30*R^{p}_{d b m }*R^{q}_{a c l }) *HATK^{c d  }*DELTA^{a b  }*HATK^{l m  } + L**2*(L-1)*(L-2)*n_{q}*n_{p} *(-1/30*R^{q}_{c m b }*R^{p}_{a d l }-1/180*R^{q}_{l c m }*R^{p}_{a b d } +1/180*R^{q}_{l c d }*R^{p}_{a b m }) *HATK^{d  }*DELTA^{l m  }*HATK^{a b c  } + L**2*(L-1)**2*(L-2)*n_{q}*n_{p} *(1/45*R^{p}_{l c m }*R^{q}_{a b d }-1/80*R^{p}_{b m c }*R^{q}_{l a d } +1/90*R^{p}_{b m c }*R^{q}_{d a l }) *HATK^{l m  }*DELTA^{d  }*HATK^{a b c  } + L**2*(L-1)*n_{q}*n_{p} *(7/120*R^{p}_{b c m }*R^{q}_{l a d }-3/40*R^{p}_{b c d }*R^{q}_{l a m } +1/120*R^{p}_{d c m }*R^{q}_{a b l }) *HATK^{l m  }*DELTA^{a b c  }*HATK^{d  } + L**2*(L-1)*(L-2)*n_{q}*n_{p} *(-1/24*R^{p}_{l c m }*R^{q}_{a b d }-1/180*R^{p}_{m c d }*R^{q}_{a b l } -1/360*R^{p}_{d c m }*R^{q}_{a b l }) *HATK^{a b c  }*DELTA^{l m  }*HATK^{d  } - L**2*(L-1)*(L-2)*(L-3)*(n_{q}*n_{p} *R^{q}_{a b c }*R^{p}_{l m d }) *HATK^{d  }*DELTA^{c  }*HATK^{l m a b  } /120 - L**2*(L-1)**2*(L-2)*(L-3)*(n_{q}*n_{p} *R^{p}_{c b l }*R^{q}_{a d m }) *HATK^{a b c d  }*HATK^{l m  } /80 + L**2*n_{p} *(-1/8*R_{b c}*R^{p}_{m a l }+3/20*R_{b c}*R^{p}_{l a m } +3/40*R_{a l}*R^{p}_{b c m }+1/40*R^{q}_{b c l }*R^{p}_{m a q } -3/20*R^{q}_{a b l }*R^{p}_{c m q }+1/10*R^{q}_{a b m }*R^{p}_{c l q }) *HATK^{l  }*DELTA^{a b c  }*HATK^{m  } + L**2*(L-1)*n_{p} *(1/20*R_{a m}*R^{p}_{c b l } +1/20*R_{a c}*R^{p}_{l b m }+1/10*R_{a b}*R^{p}_{l c m } +1/20*R^{q}_{a m c }*R^{p}_{q b l }-1/60*R^{q}_{l a m }*R^{p}_{b q c } +1/10*R^{q}_{a b c }*R^{p}_{l q m }-1/12*R^{q}_{a b m }*R^{p}_{l q c }) *HATK^{c  }*DELTA^{a b  }*HATK^{l m  } + L**2*(L-1)**2*n_{p} *(1/60*R_{a l}*R^{p}_{b m c }-1/20*R_{a l}*R^{p}_{c m b } +1/120*R_{a b}*R^{p}_{l m c }+3/40*R_{a c}*R^{p}_{m b l } +1/20*R^{q}_{c l a }*R^{p}_{m q b }+1/120*R^{q}_{a l c }*R^{p}_{b m q } -1/40*R^{q}_{a l c }*R^{p}_{q m b }+1/40*R^{q}_{a l b }*R^{p}_{q m c } -1/20*R^{q}_{a l b }*R^{p}_{c m q }-1/40*R^{q}_{l b m }*R^{p}_{c q a }) *HATK^{a b  }*DELTA^{c  }*HATK^{l m  } + L**2*(L-1)*n_{p} *(1/20*R^{q}_{l m b }*R^{p}_{c q a }-7/60*R^{q}_{b l a }*R^{p}_{c m q } +1/20*R^{q}_{b l a }*R^{p}_{q m c }+1/10*R^{q}_{l b c }*R^{p}_{m a q } +1/60*R^{q}_{b l c }*R^{p}_{a m q }+7/120*R_{a b}*R^{p}_{m c l } +11/60*R_{b l}*R^{p}_{m a c }) *HATK^{a b  }*DELTA^{l m  }*HATK^{c  } + L**2*(L-1)*(L-2)*n_{p} *(7/240*R_{a b}*R^{p}_{c l m }+7/240*R_{a m}*R^{p}_{b c l } -1/60*R_{a l}*R^{p}_{b c m }-1/24*R^{q}_{a b m }*R^{p}_{q c l } +1/15*R^{q}_{a b m }*R^{p}_{l c q }+1/40*R^{q}_{a b l }*R^{p}_{q c m } +1/40*R_{b c}*R^{p}_{m l a }+1/48*R^{q}_{b c l }*R^{p}_{m a q }) *HATK^{a b c  }*DELTA^{l  }*HATK^{m  } + L**2*(L-1)**2*(L-2) *n_{p}*(-7/240*R^{p}_{b c m }*R_{l a}+1/240*R^{p}_{l a m }*R_{b c} -1/40*R^{p}_{m c q }*R^{q}_{a l b }) *HATK^{l m  }*HATK^{a b c  } + L*(L-1)*(L-2)*(L-3) *(1/180*R_{l m}*R_{a b}+7/720*R^{q}_{a b p }*R^{p}_{l m q }) *HATK^{l m a b  }";
    private static final String DELTA_1_ = "DELTA^l=-L*HATK^l";
    private static final String DELTA_2_ = "DELTA^{lm}=-(1/2)*L*(L-1)*HATK^{lm}+Power[L,2]*(1/2)*(HATK^{l }*HATK^{m }+HATK^{m }*HATK^{l })";
    private static final String DELTA_3_ =
            "DELTA^{lma}="
                    + "-(1/6)*L*(L-1)*(L-2)*HATK^{lma}"
                    + "+(1/2)*Power[L,2]*(L-1)*(1/3)*("
                    + "HATK^{l m }*HATK^{a }+"
                    + "HATK^{a m }*HATK^{l }+"
                    + "HATK^{l a }*HATK^{m })"
                    + "+1/2*Power[L,2]*(L-1)*(1/3)*("
                    + "HATK^{a }*HATK^{l m }+"
                    + "HATK^{l }*HATK^{a m }+"
                    + "HATK^{m }*HATK^{a l })"
                    + "-Power[L,3]*(1/6)*("
                    + "HATK^{l }*HATK^{m }*HATK^{a }+"
                    + "HATK^{l }*HATK^{a }*HATK^{m }+"
                    + "HATK^{m }*HATK^{a }*HATK^{l }+"
                    + "HATK^{m }*HATK^{l }*HATK^{a }+"
                    + "HATK^{a }*HATK^{l }*HATK^{m }+"
                    + "HATK^{a }*HATK^{m }*HATK^{l })";
    private static final String DELTA_4_ =
            "DELTA^{lmab}="
                    + "-(1/24)*L*(L-1)*(L-2)*(L-3)*HATK^{lmab}"
                    + "+(1/6)*Power[L,2]*(L-1)*(L-2)*(1/4)*("
                    + "HATK^{l m a }*HATK^{b }+"
                    + "HATK^{l m b }*HATK^{a }+"
                    + "HATK^{b l a }*HATK^{m }+"
                    + "HATK^{m b a }*HATK^{l })"
                    + "+(1/6)*Power[L,2]*(L-1)*(L-2)*(1/4)*("
                    + "HATK^{b }*HATK^{l m a }+"
                    + "HATK^{a }*HATK^{l m b }+"
                    + "HATK^{l }*HATK^{b m a }+"
                    + "HATK^{m }*HATK^{b l a })"
                    + "+(1/4)*Power[L,2]*Power[(L-1),2]*(1/6)*("
                    + "HATK^{lm}*HATK^{ab}+"
                    + "HATK^{lb}*HATK^{am}+"
                    + "HATK^{la}*HATK^{mb}+"
                    + "HATK^{am}*HATK^{lb}+"
                    + "HATK^{bm}*HATK^{al}+"
                    + "HATK^{ab}*HATK^{lm})"
                    + "-(1/2)*Power[L,3]*(L-1)*(1/12)*("
                    + "HATK^{lm}*HATK^a*HATK^b+"
                    + "HATK^{lm}*HATK^b*HATK^a+"
                    + "HATK^{lb}*HATK^a*HATK^m+"
                    + "HATK^{lb}*HATK^m*HATK^a+"
                    + "HATK^{la}*HATK^m*HATK^b+"
                    + "HATK^{la}*HATK^b*HATK^m+"
                    + "HATK^{ma}*HATK^l*HATK^b+"
                    + "HATK^{ma}*HATK^b*HATK^l+"
                    + "HATK^{mb}*HATK^a*HATK^l+"
                    + "HATK^{mb}*HATK^l*HATK^a+"
                    + "HATK^{ab}*HATK^l*HATK^m+"
                    + "HATK^{ab}*HATK^m*HATK^l)"
                    + "-(1/2)*Power[L,3]*(L-1)*(1/12)*("
                    + "HATK^a*HATK^{lm}*HATK^b+"
                    + "HATK^b*HATK^{lm}*HATK^a+"
                    + "HATK^a*HATK^{lb}*HATK^m+"
                    + "HATK^m*HATK^{lb}*HATK^a+"
                    + "HATK^m*HATK^{la}*HATK^b+"
                    + "HATK^b*HATK^{la}*HATK^m+"
                    + "HATK^l*HATK^{ma}*HATK^b+"
                    + "HATK^b*HATK^{ma}*HATK^l+"
                    + "HATK^a*HATK^{mb}*HATK^l+"
                    + "HATK^l*HATK^{mb}*HATK^a+"
                    + "HATK^l*HATK^{ab}*HATK^m+"
                    + "HATK^m*HATK^{ab}*HATK^l)"
                    + "-(1/2)*Power[L,3]*(L-1)*(1/12)*("
                    + "HATK^a*HATK^b*HATK^{lm}+"
                    + "HATK^b*HATK^a*HATK^{lm}+"
                    + "HATK^a*HATK^m*HATK^{lb}+"
                    + "HATK^m*HATK^a*HATK^{lb}+"
                    + "HATK^m*HATK^b*HATK^{la}+"
                    + "HATK^b*HATK^m*HATK^{la}+"
                    + "HATK^l*HATK^b*HATK^{ma}+"
                    + "HATK^b*HATK^l*HATK^{ma}+"
                    + "HATK^a*HATK^l*HATK^{mb}+"
                    + "HATK^l*HATK^a*HATK^{mb}+"
                    + "HATK^l*HATK^m*HATK^{ab}+"
                    + "HATK^m*HATK^l*HATK^{ab})"
                    + "+(1/24)*Power[L,4]*("
                    + "HATK^{l}*HATK^{m}*HATK^{a}*HATK^{b}+"
                    + "HATK^{m}*HATK^{l}*HATK^{a}*HATK^{b}+"
                    + "HATK^{b}*HATK^{m}*HATK^{a}*HATK^{l}+"
                    + "HATK^{m}*HATK^{b}*HATK^{a}*HATK^{l}+"
                    + "HATK^{b}*HATK^{l}*HATK^{a}*HATK^{m}+"
                    + "HATK^{l}*HATK^{b}*HATK^{a}*HATK^{m}+"
                    + "HATK^{l}*HATK^{m}*HATK^{b}*HATK^{a}+"
                    + "HATK^{m}*HATK^{l}*HATK^{b}*HATK^{a}+"
                    + "HATK^{a}*HATK^{m}*HATK^{b}*HATK^{l}+"
                    + "HATK^{m}*HATK^{a}*HATK^{b}*HATK^{l}+"
                    + "HATK^{a}*HATK^{l}*HATK^{b}*HATK^{m}+"
                    + "HATK^{l}*HATK^{a}*HATK^{b}*HATK^{m}+"
                    + "HATK^{b}*HATK^{m}*HATK^{l}*HATK^{a}+"
                    + "HATK^{m}*HATK^{b}*HATK^{l}*HATK^{a}+"
                    + "HATK^{a}*HATK^{m}*HATK^{l}*HATK^{b}+"
                    + "HATK^{m}*HATK^{a}*HATK^{l}*HATK^{b}+"
                    + "HATK^{a}*HATK^{b}*HATK^{l}*HATK^{m}+"
                    + "HATK^{b}*HATK^{a}*HATK^{l}*HATK^{m}+"
                    + "HATK^{b}*HATK^{l}*HATK^{m}*HATK^{a}+"
                    + "HATK^{l}*HATK^{b}*HATK^{m}*HATK^{a}+"
                    + "HATK^{a}*HATK^{l}*HATK^{m}*HATK^{b}+"
                    + "HATK^{l}*HATK^{a}*HATK^{m}*HATK^{b}+"
                    + "HATK^{a}*HATK^{b}*HATK^{m}*HATK^{l}+"
                    + "HATK^{b}*HATK^{a}*HATK^{m}*HATK^{l})";
    private static final String ACTION_ = "counterterms = Flat + WR + SR + SSR + FF + FR + RR";
    private final Expression Flat, WR, SR, SSR, FF, FR, RR, DELTA_1, DELTA_2, DELTA_3, DELTA_4, ACTION;

    private OneLoopCounterterms(Expression Flat, Expression WR, Expression SR, Expression SSR, Expression FF, Expression FR, Expression RR, Expression DELTA_1, Expression DELTA_2, Expression DELTA_3, Expression DELTA_4, Expression ACTION) {
        this.Flat = Flat;
        this.WR = WR;
        this.SR = SR;
        this.SSR = SSR;
        this.FF = FF;
        this.FR = FR;
        this.RR = RR;
        this.DELTA_1 = DELTA_1;
        this.DELTA_2 = DELTA_2;
        this.DELTA_3 = DELTA_3;
        this.DELTA_4 = DELTA_4;
        this.ACTION = ACTION;
    }

    /**
     * Returns the Flat counterterms part
     *
     * @return Flat counterterms part
     */
    public Expression Flat() { return Flat; }

    /**
     * Returns the WR counterterms part
     *
     * @return WR counterterms part
     */
    public Expression WR() { return WR; }

    /**
     * Returns the SR counterterms part
     *
     * @return SR counterterms part
     */
    public Expression SR() { return SR; }

    /**
     * Returns the SSR counterterms part
     *
     * @return SSR counterterms part
     */
    public Expression SSR() { return SSR; }

    /**
     * Returns the FF counterterms part
     *
     * @return FF counterterms part
     */
    public Expression FF() { return FF; }

    /**
     * Returns the FR counterterms part
     *
     * @return FR counterterms part
     */
    public Expression FR() { return FR; }

    /**
     * Returns the RR counterterms part
     *
     * @return RR counterterms part
     */
    public Expression RR() { return RR; }

    /**
     * Return resulting counterterms, i.e. the Flat + WR + SR + SSR + FF + FR + RR.
     * In order to obtain the divergent part of the one loop effective action, one should
     * integrate counterterms over space volume and multiply on 1/(16*\pi^2*(d-4)) factor.
     *
     * @return resulting counterterms
     */
    public Expression getCounterterms() { return ACTION; }

    /**
     * Returns \Delta^{\mu ...} tensor, where dots mean 'matrix' indices.
     *
     * @return \Delta^{\mu ...} tensor, where dots mean 'matrix' indices.
     */
    public Expression DELTA_1() { return DELTA_1; }

    /**
     * Returns \Delta^{\mu\nu ...} tensor, where dots mean 'matrix' indices.
     *
     * @return \Delta^{\mu\nu ...} tensor, where dots mean 'matrix' indices.
     */
    public Expression DELTA_2() { return DELTA_2; }

    /**
     * Returns \Delta^{\mu\nu\alpha ...} tensor, where dots mean 'matrix' indices.
     *
     * @return \Delta^{\mu\nu\alpha ...} tensor, where dots mean 'matrix' indices.
     */
    public Expression DELTA_3() { return DELTA_3; }

    /**
     * Returns \Delta^{\mu\nu\alpha\beta ...} tensor, where dots mean 'matrix' indices.
     *
     * @return \Delta^{\mu\nu\alpha\beta ...} tensor, where dots mean 'matrix' indices.
     */
    public Expression DELTA_4() { return DELTA_4; }

    /**
     * This method performs the calculation of the one-loop counterterms.
     * It also prints the interim results to standard output, during the calculation.
     *
     * @param input input parameters container.
     * @return resulting counterterms container.
     */
    public static OneLoopCounterterms calculateOneLoopCounterterms(OneLoopInput input) {
        //Parsing input strings

        //matrices names
        final String[] matrices = new String[]{
                "iK", "HATK", "HATW", "HATS", "NABLAS",
                "HATN", "HATF", "NABLAF", "HATM", "DELTA",
                "Flat", "FF", "WR", "SR", "SSR", "FR", "RR", "Kn"};

        //F_{lm} type structure
        final StructureOfIndices F_TYPE_STRUCTURE = new StructureOfIndices(IndexType.LatinLower.getType(), 2);
        //matrices indicator for parse preprocessor
        final Indicator<ParseTokenSimpleTensor> matricesIndicator = new Indicator<ParseTokenSimpleTensor>() {

            @Override
            public boolean is(ParseTokenSimpleTensor object) {
                String name = object.name;
                for (String matrix : matrices)
                    if (name.equals(matrix))
                        return true;
                if (name.equals("F") && object.indices.getStructureOfIndices().equals(F_TYPE_STRUCTURE))
                    return true;
                return false;
            }
        };

        int i, matrixIndicesCount = input.getMatrixIndicesCount(), operatorOrder = input.getOperatorOrder();

        //indices to insert
        int upper[] = new int[matrixIndicesCount / 2], lower[] = upper.clone();
        for (i = 0; i < matrixIndicesCount / 2; ++i) {
            upper[i] = IndicesUtils.createIndex(130 + i, IndexType.LatinLower, true);//30 
            lower[i] = IndicesUtils.createIndex(130 + i + matrixIndicesCount / 2, IndexType.LatinLower, false);
        }

        Expression Flat, WR, SR, SSR, FF, FR, RR, DELTA_1, DELTA_2, DELTA_3, DELTA_4, ACTION;

        //preprocessor for Flat, WR, SR, SSR, FF, FR, RR, counterterms
        IndicesInsertion termIndicesInsertion = new IndicesInsertion(
                IndicesFactory.createSimple(null, upper),
                IndicesFactory.createSimple(null, IndicesUtils.getIndicesNames(upper)),
                matricesIndicator);

        Flat = (Expression) Tensors.parse(Flat_, termIndicesInsertion);
        WR = (Expression) Tensors.parse(WR_, termIndicesInsertion);
        SR = (Expression) Tensors.parse(SR_, termIndicesInsertion);
        SSR = (Expression) Tensors.parse(SSR_, termIndicesInsertion);
        FF = (Expression) Tensors.parse(FF_, termIndicesInsertion);
        FR = (Expression) Tensors.parse(FR_, termIndicesInsertion);
        RR = (Expression) Tensors.parse(RR_, termIndicesInsertion);
        ACTION = (Expression) Tensors.parse(ACTION_, termIndicesInsertion);
        Expression[] terms = new Expression[]{Flat, WR, SR, SSR, FF, FR, RR};

        //preprocessor for DELTA_1,2,3,4
        IndicesInsertion deltaIndicesInsertion = new IndicesInsertion(
                IndicesFactory.createSimple(null, upper),
                IndicesFactory.createSimple(null, lower),
                matricesIndicator);

        DELTA_1 = (Expression) Tensors.parse(DELTA_1_, deltaIndicesInsertion);
        DELTA_2 = (Expression) Tensors.parse(DELTA_2_, deltaIndicesInsertion);
        DELTA_3 = (Expression) Tensors.parse(DELTA_3_, deltaIndicesInsertion);
        DELTA_4 = (Expression) Tensors.parse(DELTA_4_, deltaIndicesInsertion);
        Expression[] deltaExpressions = new Expression[]{DELTA_1, DELTA_2, DELTA_3, DELTA_4};

        Expression FSubstitution = input.getF();
        for (Transformation background : input.getRiemannBackground())
            FSubstitution = (Expression) background.transform(FSubstitution);

        //Calculations        
        Expression[] riemansSubstitutions = new Expression[]{
                FSubstitution,
                Tensors.parseExpression("R_{l m}^{l}_{a} = R_{ma}"),
                Tensors.parseExpression("R_{lm}^{a}_{a}=0"),
                Tensors.parseExpression("F_{l}^{l}^{a}_{b}=0"),
                Tensors.parseExpression("R_{lmab}*R^{lamb}=(1/2)*R_{lmab}*R^{lmab}"),
                Tensors.parseExpression("R_{lmab}*R^{lmab}=4*R_{lm}*R^{lm}-R*R"),
                Tensors.parseExpression("R_{l}^{l}= R")
        };


        Expression kronecker = (Expression) Tensors.parse("d_{l}^{l}=4");
        Transformation n2 = new SqrSubs(Tensors.parseSimple("n_l")), n2Transformer = new Transformer(TraverseState.Leaving, new Transformation[]{n2});
        Transformation[] common = new Transformation[]{EliminateMetricsTransformation.ELIMINATE_METRICS, n2Transformer, kronecker};
        Transformation[] all = ArraysUtils.addAll(common, riemansSubstitutions);
        Tensor temp;

        //Calculating Delta- tensors
        System.out.println("Evaluating \\Delta- tensors.");

        //DELTA_1,2
        for (i = 0; i < 2; ++i) {
            temp = deltaExpressions[i];
            temp = input.getL().transform(temp);

            for (Expression hatK : input.getHatQuantities(0))
                temp = hatK.transform(temp);
            temp = ExpandTransformation.expand(temp, common);
            for (Transformation tr : common)
                temp = tr.transform(temp);

            deltaExpressions[i] = (Expression) temp;
            System.out.println("delta" + i + " done");
        }
        Tensor[] combinations;
        Expression[] calculatedCombinations;
        //DELTA_3 //todo for particular values of L some combinations can be neglected
        combinations = new Tensor[]{
                Tensors.parse("HATK^{lma}", deltaIndicesInsertion),
                Tensors.parse("HATK^{lm}*HATK^{a}", deltaIndicesInsertion),
                Tensors.parse("HATK^{a}*HATK^{lm}", deltaIndicesInsertion),
                Tensors.parse("HATK^{l}*HATK^{m}*HATK^{a}", deltaIndicesInsertion)
        };
        calculatedCombinations = new Expression[combinations.length];
        System.out.println("Delta3:");
        for (i = 0; i < combinations.length; ++i) {
            temp = combinations[i];
//            System.out.println("Delta3: subs" + i);
            for (Expression hatK : input.getHatQuantities(0))
                temp = hatK.transform(temp);
//            System.out.println("Delta3: expand" + i);
            temp = ExpandTransformation.expand(temp, common);
            for (Transformation tr : common)
                temp = tr.transform(temp);
            calculatedCombinations[i] = Tensors.expression(combinations[i], temp);

        }
        temp = DELTA_3;
        temp = input.getL().transform(temp);
        for (Expression t : calculatedCombinations)
            temp = new NaiveSubstitution(t.get(0), t.get(1)).transform(temp);//t.transform(temp);
//        System.out.println("Delta3:expand");
        temp = ExpandTransformation.expand(temp, common);
//        System.out.println("Delta3:subs");
        for (Transformation tr : common)
            temp = tr.transform(temp);
        System.out.println("Delta3:done");
        deltaExpressions[2] = (Expression) temp;
        //DELTA_4  //todo for different L values some combinations can be neglected
        combinations = new Tensor[]{
                Tensors.parse("HATK^{lmab}", deltaIndicesInsertion),
                Tensors.parse("HATK^{lma}*HATK^{b}", deltaIndicesInsertion),
                Tensors.parse("HATK^{b}*HATK^{lma }", deltaIndicesInsertion),
                Tensors.parse("HATK^{ab}*HATK^{lm}", deltaIndicesInsertion),
                Tensors.parse("HATK^{l}*HATK^{m}*HATK^{ab}", deltaIndicesInsertion),
                Tensors.parse("HATK^{l}*HATK^{ab}*HATK^{m}", deltaIndicesInsertion),
                Tensors.parse("HATK^{ab}*HATK^{l}*HATK^{m}", deltaIndicesInsertion),
                Tensors.parse("HATK^{b}*HATK^{a}*HATK^{l}*HATK^{m}", deltaIndicesInsertion)};
        calculatedCombinations = new Expression[combinations.length];
        System.out.println("Delta4:");
        for (i = 0; i < combinations.length; ++i) {
            temp = combinations[i];
//            System.out.println("Delta4: subs " + i);
            for (Expression hatK : input.getHatQuantities(0))
                temp = hatK.transform(temp);
//            System.out.println("Delta4: expand " + i);
            temp = ExpandTransformation.expand(temp, common);
//            System.out.println("Delta4: tr" + i);
            for (Transformation tr : common)
                temp = tr.transform(temp);
            calculatedCombinations[i] = Tensors.expression(combinations[i], temp);
        }
        temp = DELTA_4;
        temp = input.getL().transform(temp);
        for (Expression t : calculatedCombinations)
            temp = new NaiveSubstitution(t.get(0), t.get(1)).transform(temp);//t.transform(temp);
//        System.out.println("Delta4: expand");
        temp = ExpandTransformation.expand(temp, common);
        System.out.println("Delta4: tr");
        for (Transformation tr : common)
            temp = tr.transform(temp);
        deltaExpressions[3] = (Expression) temp;

        System.out.println("Evaluating \\Delta- tensors done. Evaluating action terms.");

        for (i = 0; i < terms.length; ++i) {
            temp = terms[i];
//            System.out.println(temp.get(0));
            temp = input.getL().transform(temp);

            temp = input.getF().transform(temp);
            temp = input.getHatF().transform(temp);
            for (Transformation riemannBackround : input.getRiemannBackground())
                temp = riemannBackround.transform(temp);

//            temp = ExpandTransformation.expand(temp, all);//TODO may be redundant
            for (Transformation tr : all)
                temp = tr.transform(temp);

            for (Expression nabla : input.getNablaS())
                temp = nabla.transform(temp);

            temp = input.getF().transform(temp);
            temp = input.getHatF().transform(temp);

            for (Expression kn : input.getKnQuantities())
                temp = kn.transform(temp);
//            System.out.println("kn " + temp.get(0));

            for (Expression[] hatQuantities : input.getHatQuantities())
                for (Expression hatQ : hatQuantities)
                    temp = hatQ.transform(temp);

//            System.out.println("k " + temp.get(0));
            for (Expression delta : deltaExpressions)
                temp = delta.transform(temp);

//            System.out.println("delta " + temp.get(0));
            temp = ExpandTransformation.expand(temp, all);
            for (Transformation tr : all)
                temp = tr.transform(temp);

//            System.out.println("expand " + temp.get(0));

            //todo remove this line after fixing Redberry #42
//            temp = ExpandTransformation.expand(temp);

            temp = new Averaging(Tensors.parseSimple("n_l")).transform(temp);

            temp = ExpandTransformation.expand(temp, all);
            for (Transformation tr : all)
                temp = tr.transform(temp);
//            System.out.println("expand " + temp.get(0));

            temp = ExpandTransformation.expand(temp, all);

//            System.out.println("expand " + temp.get(0));

            terms[i] = (Expression) temp;
            System.out.println(temp);
        }


        for (Expression term : terms)
            ACTION = (Expression) term.transform(ACTION);

        System.out.println(ACTION);

        return new OneLoopCounterterms(Flat, WR, SR, SSR, FF, FR, RR, deltaExpressions[0], deltaExpressions[1], deltaExpressions[2], deltaExpressions[3], ACTION);
    }
}
