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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndexType;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ContextSettings {
    //bindings
    private String imageOne;
    private ToStringMode defaultToStringMode;
    private String kronecker;
    //optional
    private String metricName = null;
    private EnumSet<IndexType> merticTypes = EnumSet.noneOf(IndexType.class);
    private Long nameManagerSeed;

    public ContextSettings(String imageOne, ToStringMode defaultToStringMode, String kronecker) {
        this.imageOne = imageOne;
        this.defaultToStringMode = defaultToStringMode;
        this.kronecker = kronecker;
    }

    byte[] getMetricTypes() {
        byte[] mTypes = new byte[merticTypes.size()];
        int i = 0;
        for (IndexType type : merticTypes)
            mTypes[i++] = type.getType();
        Arrays.sort(mTypes);
        return mTypes;
    }

    public void addMetricIndexType(IndexType type) {
        if (metricName == null)
            throw new IllegalStateException("Metric name not set.");
        merticTypes.add(type);
    }

    public ToStringMode getDefaultToStringMode() {
        return defaultToStringMode;
    }

    public void setDefaultToStringMode(ToStringMode defaultToStringMode) {
        this.defaultToStringMode = defaultToStringMode;
    }

    public String getImageOne() {
        return imageOne;
    }

    public void setImageOne(String imageOne) {
        this.imageOne = imageOne;
    }

    public String getKronecker() {
        return kronecker;
    }

    public void setKronecker(String kronecker) {
        this.kronecker = kronecker;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Long getNameManagerSeed() {
        return nameManagerSeed;
    }

    public void setNameManagerSeed(Long nameManagerSeed) {
        this.nameManagerSeed = nameManagerSeed;
    }

    public static ContextSettings createDefault() {
        ContextSettings defaultSettings = new ContextSettings("I", ToStringMode.REDBERRY, "d");
        defaultSettings.setMetricName("g");

        defaultSettings.addMetricIndexType(IndexType.LatinLower);
        defaultSettings.addMetricIndexType(IndexType.GreekLower);
        defaultSettings.addMetricIndexType(IndexType.LatinUpper);
        defaultSettings.addMetricIndexType(IndexType.GreekUpper);

        //Reading seed from property if exists
        if (System.getProperty("redberry.nmseed") != null)
            defaultSettings.setNameManagerSeed(Long.parseLong(System.getProperty("redberry.nmseed"), 10));

        return defaultSettings;
    }
}
