/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2013:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algebra System (JAS).
 *
 * JAS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JAS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAS. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * $Id$
 */

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Term order class for ordered polynomials. Implements the most used term
 * orders: graded, lexicographical, weight aray and block orders. For the
 * definitions see for example the articles <a
 * href="http://doi.acm.org/10.1145/43882.43887">Kredel,
 * "Admissible term orderings used in computer algebra systems"</a> and <a
 * href="http://doi.acm.org/10.1145/70936.70941">Sit,
 * "Some comments on term-ordering in Gr&oumlbner basis computations"</a>.
 * <b>Note: </b> the naming is not quite easy to understand: in case of doubt
 * use the term orders with "I" in the name, like IGRLEX (the default) or
 * INVLEX. Not all algorithms may work with all term orders, so watch your step.
 * This class does not jet implement orders by linear forms over Q[t]. Objects
 * of this class are immutable.
 *
 * @author Heinz Kredel
 */

public final class TermOrder implements Serializable {


    public static final int LEX = 1;


    public static final int INVLEX = 2;


    public static final int GRLEX = 3;


    public static final int IGRLEX = 4;


    public static final int REVLEX = 5;


    public static final int REVILEX = 6;


    public static final int REVTDEG = 7;


    public static final int REVITDG = 8;


    public final static int DEFAULT_EVORD = IGRLEX;


    //public final static int DEFAULT_EVORD = INVLEX;

    private final int evord;


    // for split termorders
    private final int evord2;


    private final int evbeg1;


    private final int evend1;


    private final int evbeg2;


    private final int evend2;


    /**
     * Defined array of weight vectors.
     */
    private final long[][] weight;


    /**
     * Defined descending order comparator. Sorts the highest terms first.
     */
    private final EVComparator horder;


    /**
     * Defined ascending order comparator. Sorts the lowest terms first.
     */
    private final EVComparator lorder;


    /**
     * Comparator for ExpVectors.
     */
    public static abstract class EVComparator implements Comparator<ExpVector>, Serializable {


        public abstract int compare(ExpVector e1, ExpVector e2);
    }


    /**
     * Constructor for default term order.
     */
    public TermOrder() {
        this(DEFAULT_EVORD);
    }


    /**
     * Constructor for given term order.
     *
     * @param evord requested term order indicator / enumerator.
     */
    public TermOrder(int evord) {
        if (evord < LEX || REVITDG < evord) {
            throw new IllegalArgumentException("invalid term order: " + evord);
        }
        this.evord = evord;
        this.evord2 = 0;
        weight = null;
        evbeg1 = 0;
        evend1 = Integer.MAX_VALUE;
        evbeg2 = evend1;
        evend2 = evend1;
        switch (evord) { // horder = new EVhorder();
            case TermOrder.LEX: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return ExpVector.EVILCP(e1, e2);
                    }
                };
                break;
            }
            case TermOrder.INVLEX: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return -ExpVector.EVILCP(e1, e2);
                    }
                };
                break;
            }
            case TermOrder.GRLEX: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return ExpVector.EVIGLC(e1, e2);
                    }
                };
                break;
            }
            case TermOrder.IGRLEX: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return -ExpVector.EVIGLC(e1, e2);
                    }
                };
                break;
            }
            case TermOrder.REVLEX: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return ExpVector.EVRILCP(e1, e2);
                    }
                };
                break;
            }
            case TermOrder.REVILEX: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return -ExpVector.EVRILCP(e1, e2);
                    }
                };
                break;
            }
            case TermOrder.REVTDEG: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return ExpVector.EVRIGLC(e1, e2);
                    }
                };
                break;
            }
            case TermOrder.REVITDG: {
                horder = new EVComparator() {


                    @Override
                    public int compare(ExpVector e1, ExpVector e2) {
                        return -ExpVector.EVRIGLC(e1, e2);
                    }
                };
                break;
            }
            default: {
                horder = null;
            }
        }
        if (horder == null) {
            throw new IllegalArgumentException("invalid term order: " + evord);
        }

        // lorder = new EVlorder();
        lorder = new EVComparator() {


            @Override
            public int compare(ExpVector e1, ExpVector e2) {
                return -horder.compare(e1, e2);
            }
        };

    }


    /**
     * Constructor for given exponent weights.
     *
     * @param w weight vector of longs.
     */
    public TermOrder(long[] w) {
        this(new long[][]{w});
    }


    /**
     * Constructor for given exponent weights.
     *
     * @param w weight array of longs.
     */
    public TermOrder(long[][] w) {
        if (w == null || w.length == 0) {
            throw new IllegalArgumentException("invalid term order weight");
        }
        weight = w;
        this.evord = 0;
        this.evord2 = 0;
        evbeg1 = 0;
        evend1 = weight[0].length;
        evbeg2 = evend1;
        evend2 = evend1;

        horder = new EVComparator() {


            @Override
            public int compare(ExpVector e1, ExpVector e2) {
                return -ExpVector.EVIWLC(weight, e1, e2);
            }
        };

        // lorder = new EVlorder();
        lorder = new EVComparator() {


            @Override
            public int compare(ExpVector e1, ExpVector e2) {
                return +ExpVector.EVIWLC(weight, e1, e2);
                // return - horder.compare( e1, e2 );
            }
        };

    }


    /**
     * Constructor for default split order.
     *
     * @param r     max number of exponents to compare.
     * @param split index.
     */
    public TermOrder(int r, int split) {
        this(DEFAULT_EVORD, DEFAULT_EVORD, r, split);
    }


    /**
     * Constructor for given split order.
     *
     * @param ev1   requested term order indicator for first block.
     * @param ev2   requested term order indicator for second block.
     * @param r     max number of exponents to compare.
     * @param split index.
     */
    public TermOrder(int ev1, int ev2, int r, int split) {
        if (ev1 < LEX || REVITDG < ev1) {
            throw new IllegalArgumentException("invalid term order: " + ev1);
        }
        if (ev2 < LEX || REVITDG < ev2) {
            throw new IllegalArgumentException("invalid term order: " + ev2);
        }
        this.evord = ev1;
        this.evord2 = ev2;
        weight = null;
        evbeg1 = 0;
        evend1 = split; // excluded
        evbeg2 = split;
        evend2 = r;
        if (evbeg2 > evend2) {
            throw new IllegalArgumentException("invalid term order split, r = " + r + ", split = " + split);
        }
        switch (evord) { // horder = new EVhorder();
            case TermOrder.LEX: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            case TermOrder.INVLEX: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVILEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVTDEG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVITDG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            case TermOrder.GRLEX: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            case TermOrder.IGRLEX: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVILEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVTDEG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVITDG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            //----- begin reversed -----------
            case TermOrder.REVLEX: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVILEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVTDEG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVITDG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            case TermOrder.REVILEX: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVILEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVTDEG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVITDG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRILCP(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            case TermOrder.REVTDEG: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVILEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVTDEG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVITDG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            case TermOrder.REVITDG: {
                switch (evord2) {
                    case TermOrder.LEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.INVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.GRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.IGRLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVLEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVILEX: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRILCP(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVTDEG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    case TermOrder.REVITDG: {
                        horder = new EVComparator() {


                            @Override
                            public int compare(ExpVector e1, ExpVector e2) {
                                int t = -ExpVector.EVRIGLC(e1, e2, evbeg1, evend1);
                                if (t != 0) {
                                    return t;
                                }
                                return -ExpVector.EVRIGLC(e1, e2, evbeg2, evend2);
                            }
                        };
                        break;
                    }
                    default: {
                        horder = null;
                    }
                }
                break;
            }
            //----- end reversed-----------
            default: {
                horder = null;
            }
        }
        if (horder == null) {
            throw new IllegalArgumentException("invalid term order: " + evord + " 2 " + evord2);
        }

        lorder = new EVComparator() {


            @Override
            public int compare(ExpVector e1, ExpVector e2) {
                return -horder.compare(e1, e2);
            }
        };


    }


    /**
     * Get the first defined order indicator.
     *
     * @return evord.
     */
    public int getEvord() {
        return evord;
    }


    /**
     * Get the second defined order indicator.
     *
     * @return evord2.
     */
    public int getEvord2() {
        return evord2;
    }


    /**
     * Get the split index.
     *
     * @return split.
     */
    public int getSplit() {
        return evend1; // = evbeg2
    }


    /**
     * Get the weight array.
     *
     * @return weight.
     */
    public long[][] getWeight() {
        return weight;
    }


    /**
     * Get the descending order comparator. Sorts the highest terms first.
     *
     * @return horder.
     */
    public EVComparator getDescendComparator() {
        return horder;
    }


    /**
     * Get the ascending order comparator. Sorts the lowest terms first.
     *
     * @return lorder.
     */
    public EVComparator getAscendComparator() {
        return lorder;
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof TermOrder)) {
            return false;
        }
        TermOrder b = (TermOrder) B;
        boolean t = evord == b.getEvord() && evord2 == b.evord2 && evbeg1 == b.evbeg1 && evend1 == b.evend1
                && evbeg2 == b.evbeg2 && evend2 == b.evend2;
        if (!t) {
            return t;
        }
        return Arrays.equals(weight, b.weight);
    }


    /**
     * Hash code.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h = evord;
        h = (h << 3) + evord2;
        h = (h << 4) + evbeg1;
        h = (h << 4) + evend1;
        h = (h << 4) + evbeg2;
        h = (h << 4) + evend2;
        if (weight == null) {
            return h;
        }
        h = h * 7 + Arrays.deepHashCode(weight);
        return h;
    }


    /**
     * String representation of TermOrder.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder erg = new StringBuilder();
        if (weight != null) {
            erg.append("W(");
            for (int j = 0; j < weight.length; j++) {
                long[] wj = weight[j];
                erg.append("(");
                for (int i = 0; i < wj.length; i++) {
                    erg.append("").append(wj[wj.length - i - 1]);
                    if (i < wj.length - 1) {
                        erg.append(",");
                    }
                }
                erg.append(")");
                if (j < weight.length - 1) {
                    erg.append(",");
                }
            }
            erg.append(")");
            if (evend1 == evend2) {
                return erg.toString();
            }
            erg.append("[").append(evbeg1).append(",").append(evend1).append("]");
            erg.append("[").append(evbeg2).append(",").append(evend2).append("]");
            return erg.toString();
        }
        switch (evord) {
            case LEX:
                erg.append("LEX");
                break;
            case INVLEX:
                erg.append("INVLEX");
                break;
            case GRLEX:
                erg.append("GRLEX");
                break;
            case IGRLEX:
                erg.append("IGRLEX");
                break;
            case REVLEX:
                erg.append("REVLEX");
                break;
            case REVILEX:
                erg.append("REVILEX");
                break;
            case REVTDEG:
                erg.append("REVTDEG");
                break;
            case REVITDG:
                erg.append("REVITDG");
                break;
            default:
                erg.append("invalid(").append(evord).append(")");
                break;
        }
        if (evord2 <= 0) {
            return erg.toString();
        }
        erg.append("[").append(evbeg1).append(",").append(evend1).append("]");
        switch (evord2) {
            case LEX:
                erg.append("LEX");
                break;
            case INVLEX:
                erg.append("INVLEX");
                break;
            case GRLEX:
                erg.append("GRLEX");
                break;
            case IGRLEX:
                erg.append("IGRLEX");
                break;
            case REVLEX:
                erg.append("REVLEX");
                break;
            case REVILEX:
                erg.append("REVILEX");
                break;
            case REVTDEG:
                erg.append("REVTDEG");
                break;
            case REVITDG:
                erg.append("REVITDG");
                break;
            default:
                erg.append("invalid(").append(evord2).append(")");
                break;
        }
        erg.append("[").append(evbeg2).append(",").append(evend2).append("]");
        return erg.toString();
    }


    /**
     * Extend variables. Used e.g. in module embedding. Extend TermOrder by k
     * elements. <b>Note:</b> todo distinguish TOP and POT orders.
     *
     * @param r current number of variables.
     * @param k number of variables to extend.
     * @return extended TermOrder.
     */
    public TermOrder extend(int r, int k) {
        if (weight != null) {
            long[][] w = new long[weight.length][];
            for (int i = 0; i < weight.length; i++) {
                long[] wi = weight[i];
                long max = 0;
                // long min = Long.MAX_VALUE;
                for (long aWi : wi) {
                    if (aWi > max)
                        max = aWi;
                    //if ( wi[j] < min ) min = wi[j];
                }
                max++;
                long[] wj = new long[wi.length + k];
                for (int j = 0; j < i; j++) {
                    wj[j] = max;
                }
                System.arraycopy(wi, 0, wj, i, wi.length);
                w[i] = wj;
            }
            return new TermOrder(w);
        }
        if (evord2 != 0) {
            return new TermOrder(evord, evord2, r + k, evend1 + k);
        }
        return new TermOrder(DEFAULT_EVORD/*evord*/, evord, r + k, k); // don't change to evord, cause REVITDG
    }

    /**
     * Contract variables. Used e.g. in module embedding. Contract TermOrder to
     * non split status.
     *
     * @param k   position of first element to be copied.
     * @param len new length.
     * @return contracted TermOrder.
     */
    public TermOrder contract(int k, int len) {
        if (weight != null) {
            long[][] w = new long[weight.length][];
            for (int i = 0; i < weight.length; i++) {
                long[] wi = weight[i];
                long[] wj = new long[len];
                System.arraycopy(wi, k, wj, 0, len);
                w[i] = wj;
            }
            return new TermOrder(w);
        }
        if (evord2 == 0) {
            return new TermOrder(evord);
        }
        if (evend1 > k) { // < IntMax since evord2 != 0
            int el = evend1 - k;
            while (el > len) {
                el -= len;
            }
            if (el == 0L) {
                return new TermOrder(evord);
            }
            if (el == len) {
                return new TermOrder(evord);
            }
            return new TermOrder(evord, evord2, len, el);
        }
        return new TermOrder(evord2);
    }


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @return TermOrder for reversed variables.
     */
    public TermOrder reverse() {
        return reverse(false);
    }


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @param partial true for partialy reversed term orders.
     * @return TermOrder for reversed variables.
     */
    public TermOrder reverse(boolean partial) {
        TermOrder t;
        if (weight != null) {
            if (partial) {
            }
            long[][] w = new long[weight.length][];
            for (int i = 0; i < weight.length; i++) {
                long[] wi = weight[i];
                long[] wj = new long[wi.length];
                for (int j = 0; j < wj.length; j++) {
                    wj[j] = wi[wj.length - 1 - j];
                }
                w[i] = wj;
            }
            t = new TermOrder(w);
            return t;
        }
        if (evord2 == 0) {
            t = new TermOrder(revert(evord));
            return t;
        }
        if (partial) {
            t = new TermOrder(revert(evord), revert(evord2), evend2, evend1);
        } else {
            t = new TermOrder(revert(evord2), revert(evord), evend2, evend2 - evbeg2);
        }
        return t;
    }


    /**
     * Revert exponent order. Used e.g. in opposite rings.
     *
     * @param evord exponent order to be reverted.
     * @return reverted exponent order.
     */
    public static int revert(int evord) {
        int i = evord;
        switch (evord) {
            case LEX:
                i = REVLEX;
                break;
            case INVLEX:
                i = REVILEX;
                break;
            case GRLEX:
                i = REVTDEG;
                break;
            case IGRLEX:
                i = REVITDG;
                break;
            case REVLEX:
                i = LEX;
                break;
            case REVILEX:
                i = INVLEX;
                break;
            case REVTDEG:
                i = GRLEX;
                break;
            case REVITDG:
                i = IGRLEX;
                break;
            default:
                break;
        }
        return i;
    }

}
