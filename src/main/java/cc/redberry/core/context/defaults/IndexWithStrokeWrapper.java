package cc.redberry.core.context.defaults;

import cc.redberry.core.context.IndexConverterException;
import cc.redberry.core.context.IndexSymbolConverter;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexWithStrokeWrapper implements IndexSymbolConverter {
    private final IndexSymbolConverter converter;
    private final byte numberOfStrokes;
    private final String strokesString;

    public IndexWithStrokeWrapper(IndexSymbolConverter converter, byte numberOfStrokes) {
        if (numberOfStrokes + converter.getType() > Byte.MAX_VALUE)
            throw new IllegalArgumentException("Too much strokes.");
        this.converter = converter;
        this.numberOfStrokes = numberOfStrokes;
        StringBuilder sb = new StringBuilder();
        while (numberOfStrokes-- > 0)
            sb.append('\'');
        strokesString = sb.toString();
    }

    private String getStrokes(String symbol) {
        return symbol.substring(symbol.length() - numberOfStrokes);
    }

    private String getBase(String symbol) {
        return symbol.substring(0, symbol.length() - numberOfStrokes);
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        if (symbol.length() <= strokesString.length())
            return false;
        if (!strokesString.equals(getStrokes(symbol)))
            return false;
        return converter.applicableToSymbol(getBase(symbol));
    }

    @Override
    public String getSymbol(int code, OutputFormat mode) throws IndexConverterException {
        return converter.getSymbol(code, mode) + strokesString;
    }

    @Override
    public int getCode(String symbol) throws IndexConverterException {
        return converter.getCode(getBase(symbol));
    }

    @Override
    public int maxSymbolsCount() {
        return converter.maxSymbolsCount();
    }

    @Override
    public byte getType() {
        return (byte) (IndexType.ALPHABETS_COUNT + (numberOfStrokes * converter.getType()));
    }
}
