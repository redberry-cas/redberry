/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.groups.permutations.gap;

import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.groups.permutations.PermutationGroupFactory;
import cc.redberry.core.groups.permutations.PermutationOneLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GapPrimitiveGroupsReader {

    static final Pattern PRIMGRP = Pattern.compile("PRIMGRP\\[\\s*(\\d+)\\s*\\]");

    public static Permutation[][] readGeneratorsFromGap(String fileName) throws Exception {
        File file = new File(fileName);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
        BufferedReader bufferedReader = new BufferedReader(reader);

        ArrayList<Permutation[]> generators = new ArrayList<>();

        StringBuilder buffer = new StringBuilder();
        int characterInt;
        char c;
        while ((characterInt = bufferedReader.read()) != -1) {
            c = (char) characterInt;
            if (c == '\n' || c == '\t')
                continue;
            if (c == ';') {
                String allOfDegree = buffer.toString();
                buffer = new StringBuilder();
                String[] lhsrhs = allOfDegree.split(":=");
                Matcher matcher = PRIMGRP.matcher(lhsrhs[0]);
                int degree = -1;
                while (matcher.find())
                    degree = Integer.valueOf(matcher.group(1));
                if (degree == -1)
                    throw new RuntimeException();

                allOfDegree = lhsrhs[1];
                allOfDegree = allOfDegree.split(";")[0];

                try {
                    Object parsed = parseArrayList(allOfDegree);
                    ArrayList<Object> list = (ArrayList) parsed;
                    for (Object elem : list) {
                        ArrayList<Object> group = (ArrayList) elem;
                        if (group.size() >= 9) {
                            if (justInts(group.get(8)) && !((ArrayList) group.get(8)).isEmpty()) {
                                generators.add(parseGenerators(degree, (ArrayList<Object>) group.get(8)));
                            }
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            } else
                buffer.append(c);
        }
        return generators.toArray(new Permutation[generators.size()][]);
    }

    public static PermutationGroup[] readGroupsFromGap(String fileName) {
        try {
            Permutation[][] generators = readGeneratorsFromGap(fileName);
            PermutationGroup[] groups = new PermutationGroup[generators.length];
            for (int i = 0; i < generators.length; ++i)
                groups[i] = PermutationGroupFactory.create(generators[i]);
            return groups;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static PermutationGroup readGroupFromGap(String fileName, int groupNumber) {
        try {
            Permutation[][] generators = readGeneratorsFromGap(fileName);
            return PermutationGroupFactory.create(generators[groupNumber]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Permutation[] parseGenerators(int degree, ArrayList<Object> string) {
        ArrayList<Permutation> perms = new ArrayList<>();
        for (Object perm : string) {
            int[] permutation = new int[degree];
            for (int i = 0; i < degree; ++i)
                permutation[i] = i;

            String permString = (String) perm;
            String[] cycles = permString.split("\\)\\(");
            for (String cycle : cycles) {
                if (cycle.charAt(0) == '(')
                    cycle = cycle.substring(1);
                if (cycle.charAt(cycle.length() - 1) == ')')
                    cycle = cycle.substring(0, cycle.length() - 1);
//                System.out.println(cycle);

                String[] ints = cycle.split(",");
                int lastPoint = -1, firstPoint = -1;
                for (String point : ints) {
                    if (lastPoint == -1) {
                        lastPoint = firstPoint = (Integer.valueOf(point) - 1);
                        continue;
                    }
                    permutation[lastPoint] = (lastPoint = Integer.valueOf(point) - 1);
                }
                permutation[lastPoint] = firstPoint;
            }
//            System.out.println(Arrays.toString(permutation));

            perms.add(new PermutationOneLine(permutation));
        }
        return perms.toArray(new Permutation[perms.size()]);
    }

    static boolean justInts(Object obj) {
        String objString = obj.toString();
        for (int i = 0; i < objString.length(); ++i) {
            char c = objString.charAt(i);

            if (!Character.isDigit(c) && c != '(' && c != ' ' && c != ')' && c != ',' && c != '[' && c != ']')
                return false;

        }
        return true;
    }

    static Object parseArrayList(String string) {
        string = string.trim();
        try {
            Integer integer = Integer.valueOf(string);
            return integer;
        } catch (NumberFormatException ex) {
        }


        boolean isList = false;
        if (string.charAt(0) == '[') {
            if (string.charAt(string.length() - 1) != ']')
                throw new RuntimeException();
            isList = true;
        }
        if (!isList) {
            if (string.charAt(0) != '\"' || string.charAt(string.length() - 1) != '\"')
                return string;
            return string.substring(1, string.length() - 1);
        } else {
            ArrayList<Object> list = new ArrayList<>();

            StringBuilder sb = new StringBuilder();
            int level = 0, level2 = 0;
            char c;
            for (int i = 1; i < string.length() - 1; ++i) {
                c = string.charAt(i);
                if (c == '[')
                    ++level;
                if (c == ']')
                    --level;
                if (c == '(')
                    ++level2;
                if (c == ')')
                    --level2;
                if (c == ' ')
                    continue;
                else if (c == ',' && level == 0 && level2 == 0) {
                    list.add(parseArrayList(sb.toString()));
                    sb = new StringBuilder();
                } else
                    sb.append(c);
            }
            if (sb.length() != 0)
                list.add(parseArrayList(sb.toString()));

            return list;
        }
    }


}
