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

package cc.redberry.groovy

import cc.redberry.core.tensor.Expression
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.TransformationCollection
import cc.redberry.physics.feyncalc.*
import cc.redberry.physics.oneloopdiv.OneLoopCounterterms
import cc.redberry.physics.oneloopdiv.OneLoopInput

import static cc.redberry.core.tensor.Tensors.parse

/**
 * Groovy facade for transformations and utility methods from redberry-physics.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class RedberryPhysics {

    /**
     * Returns mandelstam and mass shell substitutions following from the provided map
     * of "momentum - mass of particle".
     *
     * @param momentumMasses "momentum - mass of particle"
     * @return resulting substitutions
     */
    public static Transformation setMandelstam(Map<String, String> momentumMasses) {
        if (momentumMasses.size() != 4)
            throw new IllegalArgumentException();
        Tensor[][] result = new Tensor[4][2];
        int i = 0;
        momentumMasses.each { a, b -> result[i][0] = parse(a); result[i++][1] = parse(b); }
        return new TransformationCollection(FeynCalcUtils.setMandelstam(result));
    }

    /**
     * Returns mandelstam and mass shell substitutions following from the provided map
     * of "momentum - mass of particle" and notation of Mandelstam variables.
     *
     * @param momentumMasses "momentum - mass of particle"
     * @param s notation for s
     * @param t notation for t
     * @param u notation for u
     * @return resulting substitutions
     */
    public static Transformation setMandelstam(Map<String, String> momentumMasses, Object s, Object t, Object u) {
        if (momentumMasses.size() != 4)
            throw new IllegalArgumentException();
        Tensor[][] result = new Tensor[4][2];
        int i = 0;
        momentumMasses.each { a, b -> result[i][0] = parse(a); result[i++][1] = parse(b); }
        return new TransformationCollection(
                FeynCalcUtils.setMandelstam(result, parse0(s), parse0(t), parse0(u)));
    }

    /**
     * Returns generalized mandelstam for 2->3 reaction and mass shell substitutions following from the provided map
     * of "momentum - mass of particle" and notation of Mandelstam variables.
     *
     * @param momentumMasses "momentum - mass of particle"
     * @return resulting substitutions
     */
    public static Transformation setMandelstam5(Map<String, String> momentumMasses) {
        if (momentumMasses.size() != 5)
            throw new IllegalArgumentException();
        Tensor[][] result = new Tensor[5][2];
        int i = 0;
        momentumMasses.each { a, b -> result[i][0] = parse(a); result[i++][1] = parse(b); }
        return new TransformationCollection(FeynCalcUtils.setMandelstam5(result));
    }

    /**
     * Returns generalized mandelstam for 2->3 reaction and mass shell substitutions following from the provided map
     * of "momentum - mass of particle" and notation of Mandelstam variables.
     *
     * @param momentumMasses "momentum - mass of particle"
     * @param s notation for s = (k1 + k2)^2
     * @param t1 notation for t1 = (k1 - k3)^2
     * @param t2 notation for t2 = (k1 - k4)^2
     * @param u1 notation for u1 = (k2 - k3)^2
     * @param u2 notation for u2 = (k2 - k4)^2
     * @return resulting substitutions
     */
    public static Transformation setMandelstam5(Map<String, String> momentumMasses,
                                                Object s, Object t1, Object t2, Object u1, Object u2) {
        if (momentumMasses.size() != 5)
            throw new IllegalArgumentException();
        Tensor[][] result = new Tensor[5][2];
        int i = 0;
        momentumMasses.each { a, b -> result[i][0] = parse(a); result[i++][1] = parse(b); }
        return new TransformationCollection(
                FeynCalcUtils.setMandelstam5(result, parse0(s), parse0(t1), parse0(t2), parse0(u1), parse0(u2)));
    }

    private static Tensor parse0(Object o) {
        if (o instanceof String || o instanceof GString)
            return parse(o.toString());
        return (Tensor) o;
    }

    /**
     * Calculates trace of Dirac matrices in d dimensions (default 4).
     * @see DiracTraceTransformation
     */
    public static final DSLTransformationInst DiracTrace = new DSLTransformationInst(DiracTraceTransformation);

    /**
     * Simplifies products of gamma matrices
     * @see DiracSimplifyTransformation
     */
    public static final DSLTransformationInst DiracSimplify = new DSLTransformationInst(DiracSimplifyTransformation);

    /**
     * Simplifies expressions involving gamma5 atrices
     * @see SimplifyGamma5Transformation
     */
    public static final DSLTransformationInst DiracSimplify5 = new DSLTransformationInst(SimplifyGamma5Transformation);

    /**
     * Puts products of gammas in canonical order
     * @see DiracOrderTransformation
     */
    public static final DSLTransformationInst DiracOrder = new DSLTransformationInst(DiracOrderTransformation);

    /**
     * Simplifies spinors using Dirac equation
     * @see SpinorsSimplifyTransformation
     */
    public static
    final DSLTransformation SpinorsSimplify = new DSLTransformation(SpinorsSimplifyTransformation);

    /**
     * Calculates trace of unitary matrices
     * @see UnitaryTraceTransformation
     */
    public static final DSLTransformationInst UnitaryTrace = new DSLTransformationInst(UnitaryTraceTransformation);

    /**
     * Simplifies combinations of unitary matrices
     * @see UnitarySimplifyTransformation
     */
    public static
    final DSLTransformationInst UnitarySimplify = new DSLTransformationInst(UnitarySimplifyTransformation);

    /**
     * Simplifies combinations of Levi-Civita tensors.
     * @see LeviCivitaSimplifyTransformation
     */
    public static final GLeviCivita LeviCivitaSimplify = new GLeviCivita();

    private final static DSLTransformationInst LeviCivitaSimplify_minkowski = new DSLTransformationInst(
            new LeviCivitaSimplifyTransformation(new LeviCivitaSimplifyOptions(true)))

    private final static DSLTransformationInst LeviCivitaSimplify_euclidean = new DSLTransformationInst(
            new LeviCivitaSimplifyTransformation(new LeviCivitaSimplifyOptions(false)))

    private static final class GLeviCivita {
        /**
         * Simplifies in Minkowski space
         * @return transformation in Minkowski space
         */
        DSLTransformationInst getMinkowski() {
            return LeviCivitaSimplify_minkowski;
        }
        /**
         * Simplifies in Euclidean space
         * @return transformation in Euclidean space
         */
        DSLTransformationInst getEuclidean() {
            return LeviCivitaSimplify_euclidean;
        }
    }

    /*
     * One-loop calculations
     */

    /**
     * Calculates one-loop counterterms of second order operator.
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv2(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression F) {
        OneLoopInput input = new OneLoopInput(2, KInv, K, S, W, null, null, F)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * Calculates one-loop counterterms of second order operator.
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @param transformation additional background conditions, such as anti de Sitter etc.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv2(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression F,
                                                  Transformation transformation) {
        OneLoopInput input = new OneLoopInput(2, KInv, K, S, W, null, null, F, transformation)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * Calculates one-loop countertemrs of the fourth order operator
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param N tensor {@code N}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code N^{...}_{...} = 0}. <b>Note:</b> if
     * {@code operatorOrder = 2} this param should be {@code null}.
     * @param M tensor {@code M} in the form {@code M^{...}_{...} = ....}.
     *                      <b>Note:</b> if {@code operatorOrder = 2} this param
     *                      should be {@code null}                                    .
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv4(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression N,
                                                  Expression M,
                                                  Expression F) {
        OneLoopInput input = new OneLoopInput(4, KInv, K, S, W, N, M, F)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * Calculates one-loop countertemrs of the fourth order operator
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param N tensor {@code N}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code N^{...}_{...} = 0}. <b>Note:</b> if
     * {@code operatorOrder = 2} this param should be {@code null}.
     * @param M tensor {@code M} in the form {@code M^{...}_{...} = ....}.
     *                      <b>Note:</b> if {@code operatorOrder = 2} this param
     *                      should be {@code null}                                    .
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @param transformation additional background conditions, such as anti de Sitter etc.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv4(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression N,
                                                  Expression M,
                                                  Expression F,
                                                  Transformation transformation) {
        OneLoopInput input = new OneLoopInput(4, KInv, K, S, W, N, M, F, transformation)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * Generates a substitution for tensor integral reduction via Passarino-Veltman method. Note: the performance is
     * limited for a large order or large number of external momentums.
     *
     * @param order power of loop momentum (q_i - 1, q_i*q_j - 2 etc.)
     * @param loopMomentum loop momentum
     * @param externalMomentums list of external momentums
     * @param simplifications additional simplification rules (e.g. Mandelstam substitutions for products of external momentum)
     * @return substitution in the form like {@code q_i*q_j = p1_i * p2_j * C1 + ... }
     */
    public static Expression PassarinoVeltman(int order, def loopMomentum,
                                              def externalMomentums,
                                              Transformation simplifications = Transformation.IDENTITY) {
        use(Redberry) {
            return PassarinoVeltman.generateSubstitution(order,
                    loopMomentum.t,
                    externalMomentums.t as SimpleTensor[],
                    simplifications);
        }
    }
}
