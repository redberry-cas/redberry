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
package cc.redberry.core.utils;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProgressReporter {
    private final String prefix;
    private final long limit;
    private final double percentStep;
    private final int cLength;
    private DecimalFormat decimalFormat;
    private final AtomicLong progress = new AtomicLong(0);

    public ProgressReporter(String prefix, long limit, double percentStep) {
        this.prefix = prefix;
        this.limit = limit;
        this.percentStep = percentStep / 100.0;
        this.decimalFormat = new DecimalFormat("0.00");
        this.cLength = 100;
    }

    private final Object lock = new Object();
    private volatile double previousShown = 0;

    public boolean next() {
        long c = progress.incrementAndGet();
        if (c > limit)
            return false;
        double pr = round(1.0 * c / limit);
        double prev = round(previousShown + percentStep);
        if (pr >= prev)
            synchronized (lock) {
                if (pr >= prev) {
                    previousShown = pr;
                    print();
                    return true;
                }
            }
        return false;
    }

    static double round(double v) {
        return Math.round(10000.0 * v) / 10000.0;
    }

    private void print() {
        final String pc = "(" + decimalFormat.format(100.0 * previousShown) + "%)";
        System.out.print(prefix);
        System.out.print(pc);
        System.out.print('[');
        int l = (int) (previousShown * cLength);
        int i = 0;
        for (; i < l; ++i)
            System.out.print('=');
        System.out.print('>');
        if (pc.length() < cLength - l) {
            System.out.print(pc);
            i += pc.length();
        }
        for (; i < cLength; ++i)
            System.out.print(' ');
        System.out.print(']');
        System.out.print('\n');
    }
}
