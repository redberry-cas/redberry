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
package cc.redberry.core.performance.kv;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesTypeStructure;
import cc.redberry.core.parser.ParseNodeSimpleTensor;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.parser.preprocessor.IndicesInsertion;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.*;
import cc.redberry.core.transformations.*;
import cc.redberry.core.utils.Indicator;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class OneLoopAction {

    public static final String _Flat = "Flat=(1/4)*HATS*HATS*HATS*HATS-HATW*HATS*HATS+(1/2)*HATW*HATW+HATS*HATN-HATM+(L-2)*NABLAS_\\mu*HATW^\\mu"
            + "-L*NABLAS_\\mu*HATW*HATK^\\mu+(1/3)*((L-1)*NABLAS_\\mu^\\mu*HATS*HATS-L*NABLAS_\\mu*HATK^\\mu*HATS*HATS"
            + "-(L-1)*NABLAS_\\mu*HATS*HATS^\\mu+L*NABLAS_\\mu*HATS*HATS*HATK^\\mu)-(1/2)*NABLAS_\\mu*NABLAS_\\nu*DELTA^{\\mu\\nu}"
            + "-(1/4)*(L-1)*(L-2)*NABLAS_\\mu*NABLAS_\\nu^{\\mu\\nu}+(1/2)*L*(L-1)*(1/2)*(NABLAS_\\mu*NABLAS_{\\nu }^{\\nu}"
            + "+NABLAS_{\\nu }*NABLAS_{\\mu }^{\\nu})*HATK^\\mu";
    public static final String WR_ = "WR=-(1/2)*Power[L,2]*HATW*HATF_{\\mu\\nu}*Kn^\\mu*HATK^\\nu+(1/3)*L*HATW*HATK^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "+(1/3)*Power[L,2]*(L-1)*HATW*HATK^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}-(1/6)*(L-2)*(L-3)*HATW^{\\mu\\nu}*R_{\\mu\\nu}";
    public static final String SR_ = "SR=-(1/6)*Power[L,2]*(L-1)*HATS*NABLAF_{\\mu\\alpha\\nu}*Kn^{\\mu\\nu}*HATK^\\alpha"
            + "+(2/3)*L*HATS*NABLAF_{\\mu\\nu\\alpha}*Kn^\\alpha*DELTA^{\\mu\\nu}"
            + "-(1/12)*(L-1)*(L-2)*(L-3)*HATS^{\\alpha\\mu\\nu}*NABLAR_{\\alpha\\mu\\nu}"
            + "-(1/12)*Power[L,2]*(L-1)*(L-2)*HATS*HATK^{\\mu\\nu\\alpha}*HATK^\\beta*n_\\sigma*NABLAR_\\alpha^\\sigma_{\\mu\\beta\\nu}"
            + "+L*(L-1)*HATS*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta}*n_\\sigma*((5/12)*NABLAR_\\alpha^\\sigma_{\\nu\\beta\\mu}"
            + "-(1/12)*NABLAR_{\\mu}^\\sigma_{\\alpha\\nu\\beta})"
            + "-(1/2)*L*HATS*HATK^\\beta*DELTA^{\\mu\\nu\\alpha}*n_\\sigma*NABLAR_{\\alpha}^{\\sigma}_{\\mu\\beta\\nu}";
    public static final String SSR_ = "SSR=-(1/2)*L*(L-1)*HATS*HATS^\\mu*HATF_{\\mu\\nu}*HATK^{\\nu}+(1/2)*Power[L,2]*HATS*HATS*HATF_{\\mu\\nu}*Kn^{\\mu}*HATK^\\nu"
            + "+(1/12)*(L-1)*(L-2)*HATS*HATS^{\\mu\\nu}*R_{\\mu\\nu}+(1/3)*L*(L-1)*HATS*HATS^\\mu*HATK^\\nu*R_{\\mu\\nu}"
            + "+(1/6)*HATS*HATS*DELTA^{\\mu\\nu}*R_{\\mu\\nu}-(1/6)*L*(L-1)*(L-2)*HATS*HATS^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "+(1/3)*(L-1)*HATS*HATS^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "-(1/3)*Power[L,2]*(L-1)*HATS*HATS*HATK^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "-(1/3)*L*HATS*HATS*HATK^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}";
    public static final String FF_ = "FF=-(1/24)*L*L*(L-1)*(L-1)*HATK^{\\mu\\nu}*F_{\\mu\\alpha}*HATK^{\\alpha\\beta}*F_{\\nu\\beta}"
            + "+(1/24)*L*L*HATK^\\mu*F_{\\beta\\nu}*DELTA^{\\alpha\\beta}*HATK^\\nu*F_{\\alpha\\mu}"
            + "-(5/24)*L*L*HATK^\\mu*F_{\\beta\\mu}*DELTA^{\\alpha\\beta}*HATK^\\nu*F_{\\alpha\\nu}"
            + "-(1/48)*L*L*(L-1)*HATK^\\mu*F_{\\beta\\nu}*DELTA^\\nu*HATK^{\\alpha\\beta}*F_{\\alpha\\mu}"
            + "-(1/48)*L*L*(L-1)*HATK^\\mu*F_{\\beta\\mu}*DELTA^\\nu*HATK^{\\alpha\\beta}*F_{\\alpha\\nu}";
    public static final String FR_ = "FR=(1/40)*Power[L,2]*(L-1)*(L-2)*DELTA^\\mu*HATK^\\nu*HATK^{\\alpha\\beta\\gamma}*F_{\\mu\\alpha}*n_\\sigma*R^\\sigma_{\\gamma\\beta\\nu}"
            + "-Power[L,2]*(L-1)*(L-2)*DELTA^\\nu*HATK^{\\alpha\\beta\\gamma}*HATK^\\mu*n_\\sigma*((1/60)*R^\\sigma_{\\beta\\gamma\\mu}*F_{\\alpha\\nu}"
            + "+(1/12)*R^\\sigma_{\\beta\\gamma\\nu}*F_{\\alpha\\mu})"
            + "+Power[L,2]*Power[(L-1),2]*DELTA^\\alpha*HATK^{\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\sigma*((1/60)*R^\\sigma_{\\beta\\mu\\gamma}*F_{\\alpha\\nu}"
            + "+(1/20)*R^\\sigma_{\\alpha\\mu\\gamma}*F_{\\nu\\beta}+(1/15)*R^\\sigma_{\\gamma\\mu\\alpha}*F_{\\nu\\beta}"
            + "+(1/60)*R^\\sigma_{\\mu\\nu\\gamma}*F_{\\alpha\\beta})+Power[L,2]*(L-1)*DELTA^{\\alpha\\beta}*HATK^{\\gamma\\delta}*HATK^{\\mu}"
            + "*n_\\sigma*((4/15)*R^\\sigma_{\\delta\\beta\\gamma}*F_{\\alpha\\mu}-(1/30)*R^\\sigma_{\\beta\\delta\\alpha}*F_{\\gamma\\mu}"
            + "-(1/15)*R^\\sigma_{\\alpha\\gamma\\mu}*F_{\\beta\\delta}-(1/30)*R^\\sigma_{\\gamma\\alpha\\mu}*F_{\\beta\\delta})"
            + "+Power[L,2]*(L-1)*DELTA^{\\alpha\\beta}*HATK^\\gamma*HATK^{\\mu\\nu}*n_\\sigma*((7/60)*R^\\sigma_{\\alpha\\beta\\mu}*F_{\\gamma\\nu}"
            + "-(11/60)*R^\\sigma_{\\beta\\mu\\gamma}*F_{\\alpha\\nu}+(1/5)*R^\\sigma_{\\mu\\alpha\\gamma}*F_{\\beta\\nu}"
            + "+(1/60)*R^\\sigma_{\\mu\\alpha\\nu}*F_{\\gamma\\beta})+Power[L,2]*DELTA^{\\mu\\alpha\\beta}*HATK^\\gamma*HATK^\\nu*n_\\sigma"
            + "*((7/20)*R^\\sigma_{\\alpha\\gamma\\beta}*F_{\\nu\\mu}+(1/10)*R^\\sigma_{\\alpha\\beta\\nu}*F_{\\gamma\\mu})";
    public static final String RR_ =
            "RR=(1/10)*Power[L,2]*HATK^\\delta*DELTA^{\\mu\\nu\\alpha\\beta}*HATK^\\gamma*n_\\sigma*n_\\rho*"
            + "R^\\sigma_{\\alpha\\beta\\gamma}*R^\\rho_{\\mu\\nu\\delta}"
            + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{\\beta\\gamma\\delta}*DELTA^\\alpha*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*"
            + "((2/45)*R^\\rho_{\\alpha\\delta\\nu}*R^\\sigma_{\\beta\\mu\\gamma}-(1/120)*R^\\rho_{\\delta\\alpha\\nu}*R^\\sigma_{\\beta\\mu\\gamma})"
            + "+Power[L,2]*(L-1)*HATK^\\delta*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*"
            + "((-1/10)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\beta}+(1/15)*R^\\rho_{\\delta\\alpha\\nu}*R^\\sigma_{\\beta\\mu\\gamma}+(1/60)*R^\\rho_{\\beta\\delta\\nu}*R^\\sigma_{\\gamma\\mu\\alpha})"
            + "+Power[L,2]*Power[(L-1),2]*HATK^{\\gamma\\delta}*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*"
            + "(-(1/20)*R^\\rho_{\\mu\\beta\\nu}*R^\\sigma_{\\delta\\alpha\\gamma}+(1/180)*R^\\rho_{\\alpha\\nu\\beta}*R^\\sigma_{\\gamma\\delta\\mu}-(7/360)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\beta}-(1/240)*R^\\rho_{\\delta\\beta\\nu}*R^\\sigma_{\\gamma\\alpha\\mu}-(1/120)*R^\\rho_{\\beta\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\mu}-(1/30)*R^\\rho_{\\delta\\beta\\nu}*R^\\sigma_{\\alpha\\gamma\\mu})"
            + "+Power[L,2]*(L-1)*(L-2)*HATK^\\delta*DELTA^{\\mu\\nu}*HATK^{\\alpha\\beta\\gamma}*n_\\sigma*n_\\rho*"
            + "((-1/30)*R^\\rho_{\\gamma\\nu\\beta}*R^\\sigma_{\\alpha\\delta\\mu}-(1/180)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}+(1/180)*R^\\rho_{\\mu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\nu})"
            + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{\\mu\\nu}*DELTA^{\\delta}*HATK^{\\alpha\\beta\\gamma}*n_\\sigma*n_\\rho*"
            + "((1/45)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}-(1/80)*R^\\rho_{\\beta\\nu\\gamma}*R^\\sigma_{\\mu\\alpha\\delta}+(1/90)*R^\\rho_{\\beta\\nu\\gamma}*R^\\sigma_{\\delta\\alpha\\mu})"
            + "+Power[L,2]*(L-1)*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta\\gamma}*HATK^\\delta*n_\\sigma*n_\\rho*"
            + "((7/120)*R^\\rho_{\\beta\\gamma\\nu}*R^\\sigma_{\\mu\\alpha\\delta}-(3/40)*R^\\rho_{\\beta\\gamma\\delta}*R^\\sigma_{\\mu\\alpha\\nu}+(1/120)*R^\\rho_{\\delta\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\mu})"
            + "+Power[L,2]*(L-1)*(L-2)*HATK^{\\alpha\\beta\\gamma}*DELTA^{\\mu\\nu}*HATK^\\delta*n_\\sigma*n_\\rho*"
            + "(-(1/24)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}-(1/180)*R^\\rho_{\\nu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\mu}-(1/360)*R^\\rho_{\\delta\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\mu})"
            + "-(1/120)*Power[L,2]*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}*DELTA^{\\delta}*HATK^\\gamma*n_\\sigma*n_\\rho*R^\\rho_{\\alpha\\beta\\gamma}*R^\\sigma_{\\mu\\nu\\delta}"
            + "-(1/80)*Power[L,2]*Power[(L-1),2]*(L-2)*(L-3)*HATK^{\\alpha\\beta\\gamma\\delta}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*R^\\rho_{\\beta\\gamma\\mu}*R^\\sigma_{\\alpha\\delta\\nu}"
            + "+Power[L,2]*HATK^\\mu*DELTA^{\\alpha\\beta\\gamma}*HATK^\\nu*n_\\rho*(-(1/8)*R_{\\beta\\gamma}*R^\\rho_{\\nu\\alpha\\mu}+(3/20)*R_{\\beta\\gamma}*R^\\rho_{\\mu\\alpha\\nu}+(3/40)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}+(1/40)*R^\\sigma_{\\beta\\gamma\\mu}*R^\\rho_{\\nu\\alpha\\sigma}-(3/20)*R^\\sigma_{\\alpha\\beta\\mu}*R^\\rho_{\\gamma\\nu\\sigma}+(1/10)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\gamma\\mu\\sigma})"
            + "+Power[L,2]*(L-1)*HATK^\\gamma*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_\\rho*"
            + "((1/20)*R_{\\alpha\\nu}*R^\\rho_{\\gamma\\beta\\mu}+(1/20)*R_{\\alpha\\gamma}*R^\\rho_{\\mu\\beta\\nu}+(1/10)*R_{\\alpha\\beta}*R^\\rho_{\\mu\\gamma\\nu}+(1/20)*R^\\sigma_{\\alpha\\nu\\gamma}*R^\\rho_{\\sigma\\beta\\mu}-(1/60)*R^\\sigma_{\\mu\\alpha\\nu}*R^\\rho_{\\beta\\sigma\\gamma}+(1/10)*R^\\sigma_{\\alpha\\beta\\gamma}*R^\\rho_{\\mu\\sigma\\nu}-(1/12)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\mu\\sigma\\gamma})"
            + "+Power[L,2]*Power[(L-1),2]*HATK^{\\alpha\\beta}*DELTA^{\\gamma}*HATK^{\\mu\\nu}*n_\\rho*"
            + "((1/60)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\nu\\gamma}-(1/20)*R_{\\alpha\\mu}*R^\\rho_{\\gamma\\nu\\beta}+(1/120)*R_{\\alpha\\beta}*R^\\rho_{\\mu\\nu\\gamma}+(3/40)*R_{\\alpha\\gamma}*R^\\rho_{\\nu\\beta\\mu}+(1/20)*R^\\sigma_{\\gamma\\mu\\alpha}*R^\\rho_{\\nu\\sigma\\beta}+(1/120)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\beta\\nu\\sigma}-(1/40)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\sigma\\nu\\beta}+(1/40)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\sigma\\nu\\gamma}-(1/20)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\gamma\\nu\\sigma}-(1/40)*R^\\sigma_{\\mu\\beta\\nu}*R^\\rho_{\\gamma\\sigma\\alpha})"
            + "+Power[L,2]*(L-1)*HATK^{\\alpha\\beta}*DELTA^{\\mu\\nu}*HATK^{\\gamma}*n_\\rho*"
            + "((1/20)*R^\\sigma_{\\mu\\nu\\beta}*R^\\rho_{\\gamma\\sigma\\alpha}-(7/60)*R^\\sigma_{\\beta\\mu\\alpha}*R^\\rho_{\\gamma\\nu\\sigma}+(1/20)*R^\\sigma_{\\beta\\mu\\alpha}*R^\\rho_{\\sigma\\nu\\gamma}+(1/10)*R^\\sigma_{\\mu\\beta\\gamma}*R^\\rho_{\\nu\\alpha\\sigma}+(1/60)*R^\\sigma_{\\beta\\mu\\gamma}*R^\\rho_{\\alpha\\nu\\sigma}+(7/120)*R_{\\alpha\\beta}*R^\\rho_{\\nu\\gamma\\mu}+(11/60)*R_{\\beta\\mu}*R^\\rho_{\\nu\\alpha\\gamma})"
            + "+Power[L,2]*(L-1)*(L-2)*HATK^{\\alpha\\beta\\gamma}*DELTA^{\\mu}*HATK^{\\nu}*n_\\rho*"
            + "((7/240)*R_{\\alpha\\beta}*R^\\rho_{\\gamma\\mu\\nu}+(7/240)*R_{\\alpha\\nu}*R^\\rho_{\\beta\\gamma\\mu}-(1/60)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}-(1/24)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\sigma\\gamma\\mu}+(1/15)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\mu\\gamma\\sigma}+(1/40)*R^\\sigma_{\\alpha\\beta\\mu}*R^\\rho_{\\sigma\\gamma\\nu}+(1/40)*R_{\\beta\\gamma}*R^\\rho_{\\nu\\mu\\alpha}+(1/48)*R^\\sigma_{\\beta\\gamma\\mu}*R^\\rho_{\\nu\\alpha\\sigma})"
            + "+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\rho*"
            + "((-7/240)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}+(1/240)*R_{\\beta\\gamma}*R^\\rho_{\\mu\\alpha\\nu}-(1/40)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\nu\\gamma\\sigma})"
            + "+L*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}*"
            + "((1/180)*R_{\\mu\\nu}*R_{\\alpha\\beta}+(7/720)*R^\\sigma_{\\alpha\\beta\\rho}*R^\\rho_{\\mu\\nu\\sigma})";
    public static final String DELTA_1_ = "DELTA^\\mu=-L*HATK^\\mu";
    public static final String DELTA_2_ = "DELTA^{\\mu\\nu}=-(1/2)*L*(L-1)*HATK^{\\mu\\nu}+Power[L,2]*(1/2)*(HATK^{\\mu }*HATK^{\\nu }+HATK^{\\nu }*HATK^{\\mu })";
    public static final String DELTA_3_ = "DELTA^{\\mu\\nu\\alpha}=-(1/6)*L*(L-1)*(L-2)*HATK^{\\mu\\nu\\alpha}"
            + "+(1/2)*Power[L,2]*(L-1)*(1/3)*("
            + "HATK^{\\mu \\nu }*HATK^{\\alpha }+"
            + "HATK^{\\alpha \\nu }*HATK^{\\mu }+"
            + "HATK^{\\mu \\alpha }*HATK^{\\nu })"
            + "+1/2*Power[L,2]*(L-1)*(1/3)*("
            + "HATK^{\\alpha }*HATK^{\\mu \\nu }+"
            + "HATK^{\\mu }*HATK^{\\alpha \\nu }+"
            + "HATK^{\\nu }*HATK^{\\alpha \\mu })"
            + "-Power[L,3]*(1/6)*("
            + "HATK^{\\mu }*HATK^{\\nu }*HATK^{\\alpha }+"
            + "HATK^{\\mu }*HATK^{\\alpha }*HATK^{\\nu }+"
            + "HATK^{\\nu }*HATK^{\\alpha }*HATK^{\\mu }+"
            + "HATK^{\\nu }*HATK^{\\mu }*HATK^{\\alpha }+"
            + "HATK^{\\alpha }*HATK^{\\mu }*HATK^{\\nu }+"
            + "HATK^{\\alpha }*HATK^{\\nu }*HATK^{\\mu })";
    public static final String DELTA_4_ = "DELTA^{\\mu\\nu\\alpha\\beta}=-(1/24)*L*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}"
            + "+(1/6)*Power[L,2]*(L-1)*(L-2)*(1/4)*("
            + "HATK^{\\mu \\nu \\alpha }*HATK^{\\beta }+"
            + "HATK^{\\mu \\nu \\beta }*HATK^{\\alpha }+"
            + "HATK^{\\beta \\mu \\alpha }*HATK^{\\nu }+"
            + "HATK^{\\nu \\beta \\alpha }*HATK^{\\mu })"
            + "+(1/6)*Power[L,2]*(L-1)*(L-2)*(1/4)*("
            + "HATK^{\\beta }*HATK^{\\mu \\nu \\alpha }+"
            + "HATK^{\\alpha }*HATK^{\\mu \\nu \\beta }+"
            + "HATK^{\\mu }*HATK^{\\beta \\nu \\alpha }+"
            + "HATK^{\\nu }*HATK^{\\beta \\mu \\alpha })"
            + "+(1/4)*Power[L,2]*Power[(L-1),2]*(1/6)*("
            + "HATK^{\\mu\\nu}*HATK^{\\alpha\\beta}+"
            + "HATK^{\\mu\\beta}*HATK^{\\alpha\\nu}+"
            + "HATK^{\\mu\\alpha}*HATK^{\\nu\\beta}+"
            + "HATK^{\\alpha\\nu}*HATK^{\\mu\\beta}+"
            + "HATK^{\\beta\\nu}*HATK^{\\alpha\\mu}+"
            + "HATK^{\\alpha\\beta}*HATK^{\\mu\\nu})"
            + "-(1/2)*Power[L,3]*(L-1)*(1/12)*("
            + "HATK^{\\mu\\nu}*HATK^\\alpha*HATK^\\beta+"
            + "HATK^{\\mu\\nu}*HATK^\\beta*HATK^\\alpha+"
            + "HATK^{\\mu\\beta}*HATK^\\alpha*HATK^\\nu+"
            + "HATK^{\\mu\\beta}*HATK^\\nu*HATK^\\alpha+"
            + "HATK^{\\mu\\alpha}*HATK^\\nu*HATK^\\beta+"
            + "HATK^{\\mu\\alpha}*HATK^\\beta*HATK^\\nu+"
            + "HATK^{\\nu\\alpha}*HATK^\\mu*HATK^\\beta+"
            + "HATK^{\\nu\\alpha}*HATK^\\beta*HATK^\\mu+"
            + "HATK^{\\nu\\beta}*HATK^\\alpha*HATK^\\mu+"
            + "HATK^{\\nu\\beta}*HATK^\\mu*HATK^\\alpha+"
            + "HATK^{\\alpha\\beta}*HATK^\\mu*HATK^\\nu+"
            + "HATK^{\\alpha\\beta}*HATK^\\nu*HATK^\\mu)"
            + "-(1/2)*Power[L,3]*(L-1)*(1/12)*("
            + "HATK^\\alpha*HATK^{\\mu\\nu}*HATK^\\beta+"
            + "HATK^\\beta*HATK^{\\mu\\nu}*HATK^\\alpha+"
            + "HATK^\\alpha*HATK^{\\mu\\beta}*HATK^\\nu+"
            + "HATK^\\nu*HATK^{\\mu\\beta}*HATK^\\alpha+"
            + "HATK^\\nu*HATK^{\\mu\\alpha}*HATK^\\beta+"
            + "HATK^\\beta*HATK^{\\mu\\alpha}*HATK^\\nu+"
            + "HATK^\\mu*HATK^{\\nu\\alpha}*HATK^\\beta+"
            + "HATK^\\beta*HATK^{\\nu\\alpha}*HATK^\\mu+"
            + "HATK^\\alpha*HATK^{\\nu\\beta}*HATK^\\mu+"
            + "HATK^\\mu*HATK^{\\nu\\beta}*HATK^\\alpha+"
            + "HATK^\\mu*HATK^{\\alpha\\beta}*HATK^\\nu+"
            + "HATK^\\nu*HATK^{\\alpha\\beta}*HATK^\\mu)"
            + "-(1/2)*Power[L,3]*(L-1)*(1/12)*("
            + "HATK^\\alpha*HATK^\\beta*HATK^{\\mu\\nu}+"
            + "HATK^\\beta*HATK^\\alpha*HATK^{\\mu\\nu}+"
            + "HATK^\\alpha*HATK^\\nu*HATK^{\\mu\\beta}+"
            + "HATK^\\nu*HATK^\\alpha*HATK^{\\mu\\beta}+"
            + "HATK^\\nu*HATK^\\beta*HATK^{\\mu\\alpha}+"
            + "HATK^\\beta*HATK^\\nu*HATK^{\\mu\\alpha}+"
            + "HATK^\\mu*HATK^\\beta*HATK^{\\nu\\alpha}+"
            + "HATK^\\beta*HATK^\\mu*HATK^{\\nu\\alpha}+"
            + "HATK^\\alpha*HATK^\\mu*HATK^{\\nu\\beta}+"
            + "HATK^\\mu*HATK^\\alpha*HATK^{\\nu\\beta}+"
            + "HATK^\\mu*HATK^\\nu*HATK^{\\alpha\\beta}+"
            + "HATK^\\nu*HATK^\\mu*HATK^{\\alpha\\beta})"
            + "+(1/24)*L*L*L*L*("
            + "HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\beta}+"
            + "HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\beta}+"
            + "HATK^{\\beta}*HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu}+"
            + "HATK^{\\nu}*HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu}+"
            + "HATK^{\\beta}*HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu}+"
            + "HATK^{\\mu}*HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\nu}+"
            + "HATK^{\\mu}*HATK^{\\nu}*HATK^{\\beta}*HATK^{\\alpha}+"
            + "HATK^{\\nu}*HATK^{\\mu}*HATK^{\\beta}*HATK^{\\alpha}+"
            + "HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\beta}*HATK^{\\mu}+"
            + "HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\mu}+"
            + "HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\beta}*HATK^{\\nu}+"
            + "HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\nu}+"
            + "HATK^{\\beta}*HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha}+"
            + "HATK^{\\nu}*HATK^{\\beta}*HATK^{\\mu}*HATK^{\\alpha}+"
            + "HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\mu}*HATK^{\\beta}+"
            + "HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\beta}+"
            + "HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\mu}*HATK^{\\nu}+"
            + "HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu}+"
            + "HATK^{\\beta}*HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha}+"
            + "HATK^{\\mu}*HATK^{\\beta}*HATK^{\\nu}*HATK^{\\alpha}+"
            + "HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu}*HATK^{\\beta}+"
            + "HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\beta}+"
            + "HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\nu}*HATK^{\\mu}+"
            + "HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\mu})";
    public static final String ACTION_ = "ACTION = Flat + WR + SR + SSR + FF + FR + RR";
    private static final String[] matrices = new String[]{"ACTION", "KINV", "HATK", "HATW", "HATS", "NABLAS", "HATN", "HATF", "NABLAF", "HATM", "DELTA", "Flat", "FF", "WR", "SR", "SSR", "FR", "RR"};
    private static final IndicesTypeStructure F_TYPE_STRUCTURE = new IndicesTypeStructure(IndexType.GreekLower.getType(), 2);
    private static final Indicator<ParseNodeSimpleTensor> matricesIndicator = new Indicator<ParseNodeSimpleTensor>() {

        @Override
        public boolean is(ParseNodeSimpleTensor object) {
            String name = object.name;
            for (String matrix : matrices)
                if (name.equals(matrix))
                    return true;
            if (name.equals("F") && object.indices.getIndicesTypeStructure().equals(F_TYPE_STRUCTURE))
                return true;
            return false;
        }
    };

    private static Expression parseHATKAndDELTA(String expression) {
        IndicesInsertion indicesInsertion = new IndicesInsertion(ParserIndices.parseSimple("^{\\mu_9}"), ParserIndices.parseSimple("_{\\nu_9}"), matricesIndicator);
        return (Expression) Tensors.parse(expression, indicesInsertion);
    }

    private static Expression parseTerms(String expression) {
        IndicesInsertion indicesInsertion = new IndicesInsertion(ParserIndices.parseSimple("^{\\mu_9}"), ParserIndices.parseSimple("_{\\mu_9}"), matricesIndicator);
        return (Expression) Tensors.parse(expression, indicesInsertion);
    }

    private static Expression parseExpression(String expression) {
        return (Expression) Tensors.parse(expression);
    }
    private Expression Flat, WR, SR, SSR, FF, FR, RR, DELTA_1, DELTA_2, DELTA_3, DELTA_4, ACTION;

    public OneLoopAction(OneLoopInput input) {
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});
        Tensors.addSymmetry("F_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{1, 0, 2, 3});
        Tensors.addSymmetry("P_\\alpha\\beta", IndexType.GreekLower, false, new int[]{1, 0});

        Flat = parseTerms(_Flat);
        WR = parseTerms(WR_);
        SR = parseTerms(SR_);
        SSR = parseTerms(SSR_);
        FF = parseTerms(FF_);
        FR = parseTerms(FR_);
        RR = parseTerms(RR_);
        DELTA_1 = parseHATKAndDELTA(DELTA_1_);
        DELTA_2 = parseHATKAndDELTA(DELTA_2_);
        DELTA_3 = parseHATKAndDELTA(DELTA_3_);
        DELTA_4 = parseHATKAndDELTA(DELTA_4_);
        ACTION = parseTerms(ACTION_);

        Expression kronecker = parseExpression("d_{\\mu}^{\\mu}=4");

        Expression[] deltaExpressions = new Expression[]{DELTA_1, DELTA_2, DELTA_3, DELTA_4};
        Expression[] terms = new Expression[]{ACTION, Flat, WR, SR, SSR, FF, FR, RR};

        Transformation nn = new SqrSubs(Tensors.parseSimple("n_\\mu")), nnTransformer = new Transformer(TraverseState.Leaving, new Transformation[]{nn});


        int i, j;
        Tensor temp;
        System.out.println("Evaluating \\Delta-tensors.");
        for (i = 0; i < deltaExpressions.length; ++i) {
            temp = deltaExpressions[i];
            temp = input.getL().transform(temp);

            for (Expression hatK : input.getHatQuantities(0))
                temp = hatK.transform(temp);

            System.out.println("expand");
            temp = Expand.expand(temp, new Transformation[]{ContractIndices.CONTRACT_INDICES, nn});
            temp = ContractIndices.CONTRACT_INDICES.transform(temp);
            temp = nnTransformer.transform(temp);

            deltaExpressions[i] = (Expression) temp;
            System.out.println(temp);
        }
        System.out.println("Done.");
    }
}
