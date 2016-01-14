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
package cc.redberry.physics

import cc.redberry.core.context.CC
import cc.redberry.core.context.OutputFormat
import cc.redberry.core.tensor.*
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.TransformationToStringAble
import cc.redberry.core.transformations.expand.ExpandPort
import cc.redberry.core.transformations.options.Creator
import cc.redberry.core.transformations.options.Options
import cc.redberry.core.utils.TensorUtils
import cc.redberry.groovy.Redberry

import java.util.concurrent.atomic.AtomicInteger

import static cc.redberry.groovy.RedberryStatic.ExpandTensorsAndEliminate
import static cc.redberry.groovy.RedberryStatic.PowerUnfold

/**
 * @author Stanislav Poslavsky
 */
final class LagrangeFourierTransformation implements TransformationToStringAble {
    public static final LagrangeFourierTransformation INSTANCE = new LagrangeFourierTransformation()

    private def $momentum = 'p'
    private Transformation $expandAndEliminate = ExpandTensorsAndEliminate

    LagrangeFourierTransformation() {
    }

    @Creator
    LagrangeFourierTransformation(@Options LagrangeFourierOptions options) {
        this.$momentum = options.momentum;
        this.$expandAndEliminate = options.expandAndEliminate;
    }

    @Override
    Tensor transform(Tensor expr) {
        use(Redberry) {
            def port = ExpandPort.createPort(expr, true)
            def result = new SumBuilder()
            Tensor t
            while ((t = port.take()) != null) {
                //expand all brackets and unfold powers of scalar tensors
                t <<= PowerUnfold
                if (t.class == Product || t.class == Power)
                    result << transformProduct(t);
            }
            return result.build()
        }
    }

    private Tensor applyToElement(Tensor term, List momentums, AtomicInteger i) {
        //transform those terms that are functions
        int power = 1
        TensorField field = null
        if (term.class == TensorField)
            field = term
        else if (TensorUtils.isPositiveIntegerPower(term) && term[0].class == TensorField) {
            field = term[0]
            power = term[1].intValue()
        }
        if (field != null) {
            def r = 1.t
            while (power-- > 0) {
                //generate next momentum
                def momentum = "${$momentum}${i.getAndIncrement()}".t
                momentums << momentum
                //replace function argument with momentum
                // (e.g. f~(2)_{a bc}[x_a] -> f~(2)_{a bc}[p_a])
                field = "${field[0]} = $momentum${field[0].indices}".t >> field
                //in case of derivative we need
                // to replace partials with momentums
                if (field.isDerivative()) {
                    //indices of differentiating variables
                    def dIndices = field.partitionOfIndices[1]
                    //extract just parent field from derivative
                    // (e.g. f~(2)_{a bc}[p_a] -> f_a[p_a])
                    field = field.parentField
                    //multiply by momentums
                    // (e.g. f~(2)_{a bc}[p_a] -> I*p_b*I*p_c*f_a[p_a])
                    dIndices.each { indices ->
                        r *= "I * $momentum $indices".t
                    }
                }
                r *= field
            }
            term = r
        }
        //put transformed term to new product
        return term
    }

    private Tensor transformProduct(Tensor product) {
        use(Redberry) {
            //list of generated momentums
            def momentums = []
            //the result
            def result = new ProductBuilder()
            //counter of momentums
            def i = new AtomicInteger(0)
            //let's transform each term in product
            if (product.class == Product)
                for (def term in product)
                    result << applyToElement(term, momentums, i)
            else
                result << applyToElement(product, momentums, i)
            if (momentums.isEmpty())
                return 0.t
            //result
            def r = result.build()
            //we must replace the last momentum with -(sum of other momentums)
            def rhs = '0'.t
            //sum generated momentums except last one
            momentums.eachWithIndex { momentum, c ->
                if (c != momentums.size() - 1)
                    rhs -= "${momentum}_a".t
            }
            //replace last momentum with sum of others and return
            return "${momentums[momentums.size() - 1]}_a = $rhs".t >> r
        }
    }

    @Override
    String toString() {
        return toString(CC.defaultOutputFormat)
    }

    @Override
    String toString(OutputFormat outputFormat) {
        return 'LagrangeFourier'
    }
}
