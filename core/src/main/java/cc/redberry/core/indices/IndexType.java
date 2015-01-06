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
package cc.redberry.core.indices;

import cc.redberry.core.context.ContextSettings;
import cc.redberry.core.context.IndexSymbolConverter;
import cc.redberry.core.context.defaults.IndexWithStrokeConverter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static cc.redberry.core.context.defaults.IndexConverterExtender.*;

/**
 * This {@code enum} is a container of the information on all available index types and appropriate converters. This
 * {@code enum} is scanning at the initialization of {@link ContextSettings} and all the values are putting in the
 * Context as default indices types.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public enum IndexType {
    /**
     * Latin lower case indices ("a", "b", "c", etc.)
     */
    LatinLower(LatinLowerEx, "l"),
    /**
     * Latin upper case indices ("A", "B", "C", etc.)
     */
    LatinUpper(LatinUpperEx, "L"),
    /**
     * Greek lower case indices ("\\alpha", "\\beta", "\\gamma", etc.)
     */
    GreekLower(GreekLowerEx, "g"),
    /**
     * Greek upper case indices ("\\Gamma", "\\Delta", "\\Lambda", etc.)
     */
    GreekUpper(GreekUpperEx, "G"),
    /**
     * Latin lower case indices with strokes ("a'", "b'", "c'", etc.)
     */
    Matrix1(new IndexWithStrokeConverter(LatinLowerEx, (byte) 1), "l'"),
    /**
     * Latin upper case indices with strokes ("A'", "B'", "C'", etc.)
     */
    Matrix2(new IndexWithStrokeConverter(LatinUpperEx, (byte) 1), "L'"),
    /**
     * Greek lower case indices with strokes ("\\alpha'", "\\beta'", "\\gamma'", etc.)
     */
    Matrix3(new IndexWithStrokeConverter(GreekLowerEx, (byte) 1), "g'"),
    /**
     * Greek upper case indices with strokes ("\\Gamma'", "\\Delta'", "\\Lambda'", etc.)
     */
    Matrix4(new IndexWithStrokeConverter(GreekUpperEx, (byte) 1), "G'");

    /**
     * Corresponding index converter
     */
    private final IndexSymbolConverter converter;
    /**
     * Short name of this type.
     */
    private final String shortString;

    private IndexType(IndexSymbolConverter converter, String shortString) {
        this.shortString = shortString;
        this.converter = converter;
    }

    /**
     * Returns the appropriate string<->integer converter for this index type.
     *
     * @return the appropriate string<->integer converter for this index type
     * @see IndexSymbolConverter
     */
    public IndexSymbolConverter getSymbolConverter() {
        return converter;
    }

    /**
     * Returns short form of string representation of this type
     *
     * @return short form of string representation of this type
     */
    public String getShortString() {
        return shortString;
    }

    /**
     * Returns the types bits corresponding to this index type.
     *
     * @return types bits corresponding to this index type.
     */
    public byte getType() {
        return converter.getType();
    }

    //for fast access to types form short names
    private final static Map<String, IndexType> commonNames = new HashMap<>();
    ;
    //cached values
    private final static IndexType[] VALUES = IndexType.values();
    //all types as bytes
    private final static byte[] ALL_BYTES = new byte[VALUES.length];
    //cached converters
    private final static IndexSymbolConverter[] ALL_CONVERTERS = new IndexSymbolConverter[VALUES.length];
    //cached EnumSet
    private final static EnumSet<IndexType> ALL_TYPES = EnumSet.allOf(IndexType.class);
    //for fast access to IndexType form its byte value
    private final static IndexType[] BYTE_TO_ENUM = new IndexType[VALUES.length];

    static {
        for (int i = 0; i < VALUES.length; ++i) {
            commonNames.put(VALUES[i].getShortString(), VALUES[i]);
            BYTE_TO_ENUM[VALUES[i].getType()] = VALUES[i];
            ALL_BYTES[i] = VALUES[i].getType();
            ALL_CONVERTERS[i] = VALUES[i].getSymbolConverter();
        }
    }

    /**
     * Total number of available index types
     */
    public static final byte TYPES_COUNT = 8;//redundant

    /**
     * Total number of alphabets is 4: latin lower, latin upper, greek lower, greek upper
     */
    public static final byte ALPHABETS_COUNT = 4;//redundant

    /**
     * Returns IndexType with specified short name or {@code null} if name is invalid.
     *
     * @param shortName short name
     * @return IndexType with specified short name or {@code null} if name is invalid
     */
    public static IndexType fromShortString(String shortName) {
        return commonNames.get(shortName);
    }

    /**
     * Returns all available types represented as bytes.
     *
     * @return all available types represented as bytes
     */
    public static byte[] getBytes() {
        return ALL_BYTES.clone();
    }

    /**
     * Returns IndexType with specified byte type.
     *
     * @param type type
     * @return IndexType with specified byte type
     */
    public static IndexType getType(byte type) {
        if (type >= TYPES_COUNT || type < 0)
            throw new IllegalArgumentException("Now such type: " + type);
        return BYTE_TO_ENUM[type];
    }

    /**
     * Returns converters for all available types.
     *
     * @return converters for all available types
     */
    public static IndexSymbolConverter[] getAllConverters() {
        return ALL_CONVERTERS.clone();
    }
}
