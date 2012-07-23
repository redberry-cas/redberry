package cc.redberry.core.performance.kv;

import cc.redberry.core.indices.*;
import cc.redberry.core.parser.*;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.parser.preprocessor.IndicesInsertion;

public class MainTensors {

    public static final String _Flat = "Flat=(1/4)*HATS*HATS*HATS*HATS-HATW*HATS*HATS+(1/2)*HATW*HATW+HATS*HATN-HATM+(L-2)*NABLAS_\\mu*HATW^\\mu"
            + "-L*NABLAS_\\mu*HATW*HATK^\\mu+(1/3)*((L-1)*NABLAS_\\mu^\\mu*HATS*HATS-L*NABLAS_\\mu*HATK^\\mu*HATS*HATS"
            + "-(L-1)*NABLAS_\\mu*HATS*HATS^\\mu+L*NABLAS_\\mu*HATS*HATS*HATK^\\mu)-(1/2)*NABLAS_\\mu*NABLAS_\\nu*DELTA^{\\mu\\nu}"
            + "-(1/4)*(L-1)*(L-2)*NABLAS_\\mu*NABLAS_\\nu^{\\mu\\nu}+(1/2)*L*(L-1)*(1/2)*(NABLAS_\\mu*NABLAS_{\\nu }^{\\nu}"
            + "+NABLAS_{\\nu }*NABLAS_{\\mu }^{\\nu})*HATK^\\mu";
    public static final String WR_ = "WR=-(1/2)*Pow[L,2]*HATW*HATF_{\\mu\\nu}*Kn^\\mu*HATK^\\nu+(1/3)*L*HATW*HATK^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "+(1/3)*Pow[L,2]*(L-1)*HATW*HATK^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}-(1/6)*(L-2)*(L-3)*HATW^{\\mu\\nu}*R_{\\mu\\nu}";
    public static final String SR_ = "SR=-(1/6)*Pow[L,2]*(L-1)*HATS*NABLAF_{\\mu\\alpha\\nu}*Kn^{\\mu\\nu}*HATK^\\alpha"
            + "+(2/3)*L*HATS*NABLAF_{\\mu\\nu\\alpha}*Kn^\\alpha*DELTA^{\\mu\\nu}"
            + "-(1/12)*(L-1)*(L-2)*(L-3)*HATS^{\\alpha\\mu\\nu}*NABLAR_{\\alpha\\mu\\nu}"
            + "-(1/12)*Pow[L,2]*(L-1)*(L-2)*HATS*HATK^{\\mu\\nu\\alpha}*HATK^\\beta*n_\\sigma*NABLAR_\\alpha^\\sigma_{\\mu\\beta\\nu}"
            + "+L*(L-1)*HATS*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta}*n_\\sigma*((5/12)*NABLAR_\\alpha^\\sigma_{\\nu\\beta\\mu}"
            + "-(1/12)*NABLAR_{\\mu}^\\sigma_{\\alpha\\nu\\beta})"
            + "-(1/2)*L*HATS*HATK^\\beta*DELTA^{\\mu\\nu\\alpha}*n_\\sigma*NABLAR_{\\alpha}^{\\sigma}_{\\mu\\beta\\nu}";
    public static final String SSR_ = "SSR=-(1/2)*L*(L-1)*HATS*HATS^\\mu*HATF_{\\mu\\nu}*HATK^{\\nu}+(1/2)*Pow[L,2]*HATS*HATS*HATF_{\\mu\\nu}*Kn^{\\mu}*HATK^\\nu"
            + "+(1/12)*(L-1)*(L-2)*HATS*HATS^{\\mu\\nu}*R_{\\mu\\nu}+(1/3)*L*(L-1)*HATS*HATS^\\mu*HATK^\\nu*R_{\\mu\\nu}"
            + "+(1/6)*HATS*HATS*DELTA^{\\mu\\nu}*R_{\\mu\\nu}-(1/6)*L*(L-1)*(L-2)*HATS*HATS^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "+(1/3)*(L-1)*HATS*HATS^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "-(1/3)*Pow[L,2]*(L-1)*HATS*HATS*HATK^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}"
            + "-(1/3)*L*HATS*HATS*HATK^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}";
    public static final String FF_ = "FF=-(1/24)*L*L*(L-1)*(L-1)*HATK^{\\mu\\nu}*F_{\\mu\\alpha}*HATK^{\\alpha\\beta}*F_{\\nu\\beta}"
            + "+(1/24)*L*L*HATK^\\mu*F_{\\beta\\nu}*DELTA^{\\alpha\\beta}*HATK^\\nu*F_{\\alpha\\mu}"
            + "-(5/24)*L*L*HATK^\\mu*F_{\\beta\\mu}*DELTA^{\\alpha\\beta}*HATK^\\nu*F_{\\alpha\\nu}"
            + "-(1/48)*L*L*(L-1)*HATK^\\mu*F_{\\beta\\nu}*DELTA^\\nu*HATK^{\\alpha\\beta}*F_{\\alpha\\mu}"
            + "-(1/48)*L*L*(L-1)*HATK^\\mu*F_{\\beta\\mu}*DELTA^\\nu*HATK^{\\alpha\\beta}*F_{\\alpha\\nu}";
    public static final String FR_ = "FR=(1/40)*Pow[L,2]*(L-1)*(L-2)*DELTA^\\mu*HATK^\\nu*HATK^{\\alpha\\beta\\gamma}*F_{\\mu\\alpha}*n_\\sigma*R^\\sigma_{\\gamma\\beta\\nu}"
            + "-Pow[L,2]*(L-1)*(L-2)*DELTA^\\nu*HATK^{\\alpha\\beta\\gamma}*HATK^\\mu*n_\\sigma*((1/60)*R^\\sigma_{\\beta\\gamma\\mu}*F_{\\alpha\\nu}"
            + "+(1/12)*R^\\sigma_{\\beta\\gamma\\nu}*F_{\\alpha\\mu})"
            + "+Pow[L,2]*Pow[(L-1),2]*DELTA^\\alpha*HATK^{\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\sigma*((1/60)*R^\\sigma_{\\beta\\mu\\gamma}*F_{\\alpha\\nu}"
            + "+(1/20)*R^\\sigma_{\\alpha\\mu\\gamma}*F_{\\nu\\beta}+(1/15)*R^\\sigma_{\\gamma\\mu\\alpha}*F_{\\nu\\beta}"
            + "+(1/60)*R^\\sigma_{\\mu\\nu\\gamma}*F_{\\alpha\\beta})+Pow[L,2]*(L-1)*DELTA^{\\alpha\\beta}*HATK^{\\gamma\\delta}*HATK^{\\mu}"
            + "*n_\\sigma*((4/15)*R^\\sigma_{\\delta\\beta\\gamma}*F_{\\alpha\\mu}-(1/30)*R^\\sigma_{\\beta\\delta\\alpha}*F_{\\gamma\\mu}"
            + "-(1/15)*R^\\sigma_{\\alpha\\gamma\\mu}*F_{\\beta\\delta}-(1/30)*R^\\sigma_{\\gamma\\alpha\\mu}*F_{\\beta\\delta})"
            + "+Pow[L,2]*(L-1)*DELTA^{\\alpha\\beta}*HATK^\\gamma*HATK^{\\mu\\nu}*n_\\sigma*((7/60)*R^\\sigma_{\\alpha\\beta\\mu}*F_{\\gamma\\nu}"
            + "-(11/60)*R^\\sigma_{\\beta\\mu\\gamma}*F_{\\alpha\\nu}+(1/5)*R^\\sigma_{\\mu\\alpha\\gamma}*F_{\\beta\\nu}"
            + "+(1/60)*R^\\sigma_{\\mu\\alpha\\nu}*F_{\\gamma\\beta})+Pow[L,2]*DELTA^{\\mu\\alpha\\beta}*HATK^\\gamma*HATK^\\nu*n_\\sigma"
            + "*((7/20)*R^\\sigma_{\\alpha\\gamma\\beta}*F_{\\nu\\mu}+(1/10)*R^\\sigma_{\\alpha\\beta\\nu}*F_{\\gamma\\mu})";
    public static final String RR_ =
            "RR=(1/10)*Pow[L,2]*HATK^\\delta*DELTA^{\\mu\\nu\\alpha\\beta}*HATK^\\gamma*n_\\sigma*n_\\rho*"
            + "R^\\sigma_{\\alpha\\beta\\gamma}*R^\\rho_{\\mu\\nu\\delta}"
            + "+Pow[L,2]*Pow[(L-1),2]*(L-2)*HATK^{\\beta\\gamma\\delta}*DELTA^\\alpha*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*"
            + "((2/45)*R^\\rho_{\\alpha\\delta\\nu}*R^\\sigma_{\\beta\\mu\\gamma}-(1/120)*R^\\rho_{\\delta\\alpha\\nu}*R^\\sigma_{\\beta\\mu\\gamma})"
            + "+Pow[L,2]*(L-1)*HATK^\\delta*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*"
            + "((-1/10)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\beta}+(1/15)*R^\\rho_{\\delta\\alpha\\nu}*R^\\sigma_{\\beta\\mu\\gamma}+(1/60)*R^\\rho_{\\beta\\delta\\nu}*R^\\sigma_{\\gamma\\mu\\alpha})"
            + "+Pow[L,2]*Pow[(L-1),2]*HATK^{\\gamma\\delta}*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*"
            + "(-(1/20)*R^\\rho_{\\mu\\beta\\nu}*R^\\sigma_{\\delta\\alpha\\gamma}+(1/180)*R^\\rho_{\\alpha\\nu\\beta}*R^\\sigma_{\\gamma\\delta\\mu}-(7/360)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\beta}-(1/240)*R^\\rho_{\\delta\\beta\\nu}*R^\\sigma_{\\gamma\\alpha\\mu}-(1/120)*R^\\rho_{\\beta\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\mu}-(1/30)*R^\\rho_{\\delta\\beta\\nu}*R^\\sigma_{\\alpha\\gamma\\mu})"
            + "+Pow[L,2]*(L-1)*(L-2)*HATK^\\delta*DELTA^{\\mu\\nu}*HATK^{\\alpha\\beta\\gamma}*n_\\sigma*n_\\rho*"
            + "((-1/30)*R^\\rho_{\\gamma\\nu\\beta}*R^\\sigma_{\\alpha\\delta\\mu}-(1/180)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}+(1/180)*R^\\rho_{\\mu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\nu})"
            + "+Pow[L,2]*Pow[(L-1),2]*(L-2)*HATK^{\\mu\\nu}*DELTA^{\\delta}*HATK^{\\alpha\\beta\\gamma}*n_\\sigma*n_\\rho*"
            + "((1/45)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}-(1/80)*R^\\rho_{\\beta\\nu\\gamma}*R^\\sigma_{\\mu\\alpha\\delta}+(1/90)*R^\\rho_{\\beta\\nu\\gamma}*R^\\sigma_{\\delta\\alpha\\mu})"
            + "+Pow[L,2]*(L-1)*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta\\gamma}*HATK^\\delta*n_\\sigma*n_\\rho*"
            + "((7/120)*R^\\rho_{\\beta\\gamma\\nu}*R^\\sigma_{\\mu\\alpha\\delta}-(3/40)*R^\\rho_{\\beta\\gamma\\delta}*R^\\sigma_{\\mu\\alpha\\nu}+(1/120)*R^\\rho_{\\delta\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\mu})"
            + "+Pow[L,2]*(L-1)*(L-2)*HATK^{\\alpha\\beta\\gamma}*DELTA^{\\mu\\nu}*HATK^\\delta*n_\\sigma*n_\\rho*"
            + "(-(1/24)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}-(1/180)*R^\\rho_{\\nu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\mu}-(1/360)*R^\\rho_{\\delta\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\mu})"
            + "-(1/120)*Pow[L,2]*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}*DELTA^{\\delta}*HATK^\\gamma*n_\\sigma*n_\\rho*R^\\rho_{\\alpha\\beta\\gamma}*R^\\sigma_{\\mu\\nu\\delta}"
            + "-(1/80)*Pow[L,2]*Pow[(L-1),2]*(L-2)*(L-3)*HATK^{\\alpha\\beta\\gamma\\delta}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*R^\\rho_{\\beta\\gamma\\mu}*R^\\sigma_{\\alpha\\delta\\nu}"
            + "+Pow[L,2]*HATK^\\mu*DELTA^{\\alpha\\beta\\gamma}*HATK^\\nu*n_\\rho*(-(1/8)*R_{\\beta\\gamma}*R^\\rho_{\\nu\\alpha\\mu}+(3/20)*R_{\\beta\\gamma}*R^\\rho_{\\mu\\alpha\\nu}+(3/40)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}+(1/40)*R^\\sigma_{\\beta\\gamma\\mu}*R^\\rho_{\\nu\\alpha\\sigma}-(3/20)*R^\\sigma_{\\alpha\\beta\\mu}*R^\\rho_{\\gamma\\nu\\sigma}+(1/10)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\gamma\\mu\\sigma})"
            + "+Pow[L,2]*(L-1)*HATK^\\gamma*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_\\rho*"
            + "((1/20)*R_{\\alpha\\nu}*R^\\rho_{\\gamma\\beta\\mu}+(1/20)*R_{\\alpha\\gamma}*R^\\rho_{\\mu\\beta\\nu}+(1/10)*R_{\\alpha\\beta}*R^\\rho_{\\mu\\gamma\\nu}+(1/20)*R^\\sigma_{\\alpha\\nu\\gamma}*R^\\rho_{\\sigma\\beta\\mu}-(1/60)*R^\\sigma_{\\mu\\alpha\\nu}*R^\\rho_{\\beta\\sigma\\gamma}+(1/10)*R^\\sigma_{\\alpha\\beta\\gamma}*R^\\rho_{\\mu\\sigma\\nu}-(1/12)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\mu\\sigma\\gamma})"
            + "+Pow[L,2]*Pow[(L-1),2]*HATK^{\\alpha\\beta}*DELTA^{\\gamma}*HATK^{\\mu\\nu}*n_\\rho*"
            + "((1/60)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\nu\\gamma}-(1/20)*R_{\\alpha\\mu}*R^\\rho_{\\gamma\\nu\\beta}+(1/120)*R_{\\alpha\\beta}*R^\\rho_{\\mu\\nu\\gamma}+(3/40)*R_{\\alpha\\gamma}*R^\\rho_{\\nu\\beta\\mu}+(1/20)*R^\\sigma_{\\gamma\\mu\\alpha}*R^\\rho_{\\nu\\sigma\\beta}+(1/120)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\beta\\nu\\sigma}-(1/40)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\sigma\\nu\\beta}+(1/40)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\sigma\\nu\\gamma}-(1/20)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\gamma\\nu\\sigma}-(1/40)*R^\\sigma_{\\mu\\beta\\nu}*R^\\rho_{\\gamma\\sigma\\alpha})"
            + "+Pow[L,2]*(L-1)*HATK^{\\alpha\\beta}*DELTA^{\\mu\\nu}*HATK^{\\gamma}*n_\\rho*"
            + "((1/20)*R^\\sigma_{\\mu\\nu\\beta}*R^\\rho_{\\gamma\\sigma\\alpha}-(7/60)*R^\\sigma_{\\beta\\mu\\alpha}*R^\\rho_{\\gamma\\nu\\sigma}+(1/20)*R^\\sigma_{\\beta\\mu\\alpha}*R^\\rho_{\\sigma\\nu\\gamma}+(1/10)*R^\\sigma_{\\mu\\beta\\gamma}*R^\\rho_{\\nu\\alpha\\sigma}+(1/60)*R^\\sigma_{\\beta\\mu\\gamma}*R^\\rho_{\\alpha\\nu\\sigma}+(7/120)*R_{\\alpha\\beta}*R^\\rho_{\\nu\\gamma\\mu}+(11/60)*R_{\\beta\\mu}*R^\\rho_{\\nu\\alpha\\gamma})"
            + "+Pow[L,2]*(L-1)*(L-2)*HATK^{\\alpha\\beta\\gamma}*DELTA^{\\mu}*HATK^{\\nu}*n_\\rho*"
            + "((7/240)*R_{\\alpha\\beta}*R^\\rho_{\\gamma\\mu\\nu}+(7/240)*R_{\\alpha\\nu}*R^\\rho_{\\beta\\gamma\\mu}-(1/60)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}-(1/24)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\sigma\\gamma\\mu}+(1/15)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\mu\\gamma\\sigma}+(1/40)*R^\\sigma_{\\alpha\\beta\\mu}*R^\\rho_{\\sigma\\gamma\\nu}+(1/40)*R_{\\beta\\gamma}*R^\\rho_{\\nu\\mu\\alpha}+(1/48)*R^\\sigma_{\\beta\\gamma\\mu}*R^\\rho_{\\nu\\alpha\\sigma})"
            + "+Pow[L,2]*Pow[(L-1),2]*(L-2)*HATK^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\rho*"
            + "((-7/240)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}+(1/240)*R_{\\beta\\gamma}*R^\\rho_{\\mu\\alpha\\nu}-(1/40)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\nu\\gamma\\sigma})"
            + "+L*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}*"
            + "((1/180)*R_{\\mu\\nu}*R_{\\alpha\\beta}+(7/720)*R^\\sigma_{\\alpha\\beta\\rho}*R^\\rho_{\\mu\\nu\\sigma})";
    public static final String DELTA_1_ = "DELTA^\\mu=-L*HATK^\\mu";
    public static final String DELTA_2_ = "DELTA^{\\mu\\nu}=-(1/2)*L*(L-1)*HATK^{\\mu\\nu}+Pow[L,2]*(1/2)*(HATK^{\\mu }*HATK^{\\nu }+HATK^{\\nu }*HATK^{\\mu })";
    public static final String DELTA_3_ = "DELTA^{\\mu\\nu\\alpha}=-(1/6)*L*(L-1)*(L-2)*HATK^{\\mu\\nu\\alpha}"
            + "+(1/2)*Pow[L,2]*(L-1)*(1/3)*("
            + "HATK^{\\mu \\nu }*HATK^{\\alpha }+"
            + "HATK^{\\alpha \\nu }*HATK^{\\mu }+"
            + "HATK^{\\mu \\alpha }*HATK^{\\nu })"
            + "+1/2*Pow[L,2]*(L-1)*(1/3)*("
            + "HATK^{\\alpha }*HATK^{\\mu \\nu }+"
            + "HATK^{\\mu }*HATK^{\\alpha \\nu }+"
            + "HATK^{\\nu }*HATK^{\\alpha \\mu })"
            + "-Pow[L,3]*(1/6)*("
            + "HATK^{\\mu }*HATK^{\\nu }*HATK^{\\alpha }+"
            + "HATK^{\\mu }*HATK^{\\alpha }*HATK^{\\nu }+"
            + "HATK^{\\nu }*HATK^{\\alpha }*HATK^{\\mu }+"
            + "HATK^{\\nu }*HATK^{\\mu }*HATK^{\\alpha }+"
            + "HATK^{\\alpha }*HATK^{\\mu }*HATK^{\\nu }+"
            + "HATK^{\\alpha }*HATK^{\\nu }*HATK^{\\mu })";
    public static final String DELTA_4_ = "DELTA^{\\mu\\nu\\alpha\\beta}=-(1/24)*L*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}"
            + "+(1/6)*Pow[L,2]*(L-1)*(L-2)*(1/4)*("
            + "HATK^{\\mu \\nu \\alpha }*HATK^{\\beta }+"
            + "HATK^{\\mu \\nu \\beta }*HATK^{\\alpha }+"
            + "HATK^{\\beta \\mu \\alpha }*HATK^{\\nu }+"
            + "HATK^{\\nu \\beta \\alpha }*HATK^{\\mu })"
            + "+(1/6)*Pow[L,2]*(L-1)*(L-2)*(1/4)*("
            + "HATK^{\\beta }*HATK^{\\mu \\nu \\alpha }+"
            + "HATK^{\\alpha }*HATK^{\\mu \\nu \\beta }+"
            + "HATK^{\\mu }*HATK^{\\beta \\nu \\alpha }+"
            + "HATK^{\\nu }*HATK^{\\beta \\mu \\alpha })"
            + "+(1/4)*Pow[L,2]*Pow[(L-1),2]*(1/6)*("
            + "HATK^{\\mu\\nu}*HATK^{\\alpha\\beta}+"
            + "HATK^{\\mu\\beta}*HATK^{\\alpha\\nu}+"
            + "HATK^{\\mu\\alpha}*HATK^{\\nu\\beta}+"
            + "HATK^{\\alpha\\nu}*HATK^{\\mu\\beta}+"
            + "HATK^{\\beta\\nu}*HATK^{\\alpha\\mu}+"
            + "HATK^{\\alpha\\beta}*HATK^{\\mu\\nu})"
            + "-(1/2)*Pow[L,3]*(L-1)*(1/12)*("
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
            + "-(1/2)*Pow[L,3]*(L-1)*(1/12)*("
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
            + "-(1/2)*Pow[L,3]*(L-1)*(1/12)*("
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
    public static final String HATK_0_ = "HATK = KINV*K^{\\mu\\nu}*n_{\\nu}*n_\\mu";
    public static final String HATK_1_ = "HATK^{\\mu} = KINV*K^{\\mu\\nu}*n_{\\nu}";
    public static final String HATK_2_ = "HATK^{\\mu\\nu} = KINV*K^{\\mu\\nu}";
    public static final String HATK_3_ = "HATK^{\\mu\\nu\\alpha} = HATK^{\\mu\\nu\\alpha}";
    public static final String HATK_4_ = "HATK^{\\mu\\nu\\alpha\\beta} = HATK^{\\mu\\nu\\alpha\\beta}";
    public static final String HATF_2_ = "HATF^{\\mu\\nu} = KINV*F^{\\mu\\nu}";
    public static final String HATW_ = "HATW = KINV*W";
    private static final IndicesTypeStructure F_TYPE_STRUCTURE = new IndicesTypeStructure(IndexType.GreekLower.getType(), 2);
    private static final String[] matrices = new String[]{"ACTION", "KINV", "HATK", "HATW", "HATS", "NABLAS", "HATN", "HATF", "NABLAF", "HATM", "DELTA", "Flat", "FF", "WR", "SR", "SSR", "FR", "RR"};
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
    /*
     * parse expressions
     */
    public final Expression Flat;// = parseTerms(_Flat);
    public final Expression WR;//= parseTerms(WR_);
    public final Expression SR;// = parseTerms(SR_);
    public final Expression SSR;// = parseTerms(SSR_);
    public final Expression FF;//= parseTerms(FF_);
    public final Expression FR;//= parseTerms(FR_);
    public final Expression RR;//= parseTerms(RR_);
    public final Expression DELTA_1;// = parseHATKAndDELTA(DELTA_1_);
    public final Expression DELTA_2;//= parseHATKAndDELTA(DELTA_2_);
    public final Expression DELTA_3;//= parseHATKAndDELTA(DELTA_3_);
    public final Expression DELTA_4;//= parseHATKAndDELTA(DELTA_4_);
    public final Expression HATK_0;//= parseHATKAndDELTA(HATK_0_);
    public final Expression HATK_1;//= parseHATKAndDELTA(HATK_1_);
    public final Expression HATK_2;//= parseHATKAndDELTA(HATK_2_);
    public final Expression HATK_3;//= parseHATKAndDELTA(HATK_3_);
    public final Expression HATK_4;//= parseHATKAndDELTA(HATK_4_);
    public final Expression HATF_2;// = parseHATKAndDELTA(HATF_2_);
    public final Expression HATW;//= parseHATKAndDELTA(HATW_);
    public final Expression ACTION;//= parseTerms(ACTION_);

    /*
     * Kronecker dimension
     */
    public final Expression KRONECKER_DIMENSION;//= parseExpression("d_{\\mu}^{\\mu}=4");

    /*
     * Arrays of terms by groups.
     */
    public final Expression[] HATs;// = new Expression[]{HATK_0, HATK_1, HATK_2, HATK_3, HATK_4, HATF_2, HATW};
    public final Expression[] DELTAs;//= new Expression[]{DELTA_1, DELTA_2, DELTA_3, DELTA_4};
    public final Expression[] TERMs;//= new Expression[]{ACTION, Flat, WR, SR, SSR, FF, FR, RR};
    public final Expression[] ALL;//= new Expression[]{Flat, WR, SR, SSR, FF, FR, RR, DELTA_1, DELTA_2, DELTA_3, DELTA_4, HATK_0, HATK_1, HATK_2, HATK_3, HATK_4, HATF_2};
//    public static final Tensor[] MATRIX_INPUT = {CC.parse("K^{\\mu\\nu}"), CC.parse("W")}; //TODO: for other theory need to add terms

    public MainTensors() {
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
        HATK_0 = parseHATKAndDELTA(HATK_0_);
        HATK_1 = parseHATKAndDELTA(HATK_1_);
        HATK_2 = parseHATKAndDELTA(HATK_2_);
        HATK_3 = parseHATKAndDELTA(HATK_3_);
        HATK_4 = parseHATKAndDELTA(HATK_4_);
        HATF_2 = parseHATKAndDELTA(HATF_2_);
        HATW = parseHATKAndDELTA(HATW_);
        ACTION = parseTerms(ACTION_);

        KRONECKER_DIMENSION = parseExpression("d_{\\mu}^{\\mu}=4");

        HATs = new Expression[]{HATK_0, HATK_1, HATK_2, HATK_3, HATK_4, HATF_2, HATW};
        DELTAs = new Expression[]{DELTA_1, DELTA_2, DELTA_3, DELTA_4};
        TERMs = new Expression[]{ACTION, Flat, WR, SR, SSR, FF, FR, RR};
        ALL = new Expression[]{Flat, WR, SR, SSR, FF, FR, RR, DELTA_1, DELTA_2, DELTA_3, DELTA_4, HATK_0, HATK_1, HATK_2, HATK_3, HATK_4, HATF_2};
    }

    public static void main(String[] arg) {
        MainTensors mt = new MainTensors();
        System.out.println("Hello world!");
    }
}
