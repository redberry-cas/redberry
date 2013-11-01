package cc.redberry.core.parser;

import cc.redberry.core.context.Context;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseTokenExpression extends ParseToken {

    public ParseTokenExpression(boolean preprocessing, ParseToken lhs, ParseToken rhs) {
        super(preprocessing ? TokenType.PreprocessingExpression : TokenType.Expression, lhs, rhs);
    }

    @Override
    public Indices getIndices() {
        return content[0].getIndices().getFree();
    }

    @Override
    public Tensor toTensor() {
        Expression expression = Tensors.expression(content[0].toTensor(), content[1].toTensor());
        if (tokenType == TokenType.PreprocessingExpression)
            Context.get().getParseManager().defaultTensorPreprocessors.add(expression);
        return expression;
    }
}
