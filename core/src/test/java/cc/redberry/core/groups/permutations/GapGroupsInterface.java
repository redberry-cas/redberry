/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.context.CC;

import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An interface to GAP system (http://www.gap-system.org/).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class GapGroupsInterface {
    private final Process gapProcess;
    private final Thread readThread;
    private final PrintStream gapCmd;
    private final GapOutputReader gapReader;

    /**
     * Creates GAP instance using absolute path to GAP executable.
     *
     * @param gapExecutable
     * @throws IOException
     */
    public GapGroupsInterface(String gapExecutable) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(gapExecutable, "-b");
        pb.redirectErrorStream(true);
        this.gapProcess = pb.start();
        this.gapCmd = new PrintStream(gapProcess.getOutputStream());
        this.readThread = new Thread(this.gapReader = new GapOutputReader(this.gapProcess.getInputStream()));
        this.readThread.setDaemon(true);
        this.readThread.start();
        //reset GAP seed
        evaluate("Reset(GlobalMersenneTwister, " + CC.getNameManager().getSeed() + ");");
    }

    public String evaluate(String command) {
        gapCmd.println(stringToGapCommand(command) + "Print(\"\\nEOF\");");
        gapCmd.flush();

//        //reading errors
//        boolean error;
//        try {
//            error = gapErrorReader.ready();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        if (error) {
//            String err = readGap(charBuff, gapErrorReader);
//            assert err.trim().substring(0, 5).equals("Error");
//            throw new RuntimeException(err);
//        }

        //reading GAP output
        try {
            String result = gapReader.buffer.poll(30, TimeUnit.SECONDS);
            if (result == null)
                throw new RuntimeException("Timeout.");
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public String evaluateRedberryGroup(String var, List<Permutation> generators) {
        StringBuilder sb = new StringBuilder();
        sb.append(var.trim()).append(":= Group(");

        for (Iterator<Permutation> it = generators.iterator(); ; ) {
            Permutation p = it.next();
            sb.append("PermList(").append(convertToGapList(p.oneLine())).append(")");
            if (!it.hasNext())
                break;
            sb.append(", ");
        }
        sb.append(");");
        return evaluate(sb.toString());
    }

    public PermutationGroup evaluateToPermutationGroup(String command) {
        return PermutationGroup.createPermutationGroup(evaluateToGenerators(stringToGapCommand(command)));
    }

    public boolean evaluateToBoolean(String command) {
        return Boolean.valueOf(evaluate(stringToGapCommand(command))).booleanValue();
    }

    public int evaluateToInteger(String command) {
        return Integer.valueOf(evaluate(stringToGapCommand(command))).intValue();
    }

    public BigInteger evaluateToBigInteger(String command) {
        return new BigInteger(evaluate(stringToGapCommand(command)));
    }

    public Permutation[] evaluateToGenerators(String command) {
        command = stringFromGapCommand(command);
        if (!evaluateToBoolean("IsPermGroup(" + command + ");"))
            throw new IllegalArgumentException("Specified string does not denote any GAP permutation group.");

        String _command = " g:= " + command + ";;" +
                " degree:= Length(MovedPoints(g));;" +
                " generators:= GeneratorsOfGroup(g);;" +
                " index:= 1;;" +
                " while(index <= Length(generators)) do " +
                "     Print(ListPerm(generators[index], degree));" +
                "     if index < Length(generators) then" +
                "         Print(\", \");" +
                "     fi;;" +
                "     index:= index + 1;;" +
                " od;;";

        String generatorsString = evaluate(_command);
        generatorsString = generatorsString.replace("\n", "").replace(" ", "");

        final String[] gens = generatorsString.split("\\]\\,\\[");
        Permutation[] generators = new Permutation[gens.length];
        for (int i = 0; i < gens.length; ++i) {
            String genString = gens[i];
            if (genString.charAt(0) == '[')
                genString = genString.substring(1);
            if (genString.charAt(genString.length() - 1) == ']')
                genString = genString.substring(0, genString.length() - 1);
            final String[] ints = genString.split(",");
            int[] p = new int[ints.length];
            for (int j = 0; j < ints.length; ++j)
                p[j] = Integer.valueOf(ints[j]) - 1;
            generators[i] = Permutations.createPermutation(p);
        }

        return generators;
    }

    public int nrPrimitiveGroups(int degree) {
        return evaluateToInteger("NrPrimitiveGroups(" + degree + ");");
    }

    public Permutation[] primitiveGenerators(int degree, int nr) {
        return evaluateToGenerators("PrimitiveGroup(" + degree + "," + (nr + 1) + ");");
    }

    private int local = 0;

    private String nextVar() {
        if (local < 0)
            local = 0;
        return "var" + local++;
    }

    public PermutationGroup primitiveGroup(int degree, int nr) {
        String g = nextVar();
        evaluate(g + ":= PrimitiveGroup(" + degree + "," + (nr + 1) + ");");
        if (evaluateToBoolean("IsNaturalSymmetricGroup( " + g + ");"))
            return PermutationGroup.symmetricGroup(degree);
        if (evaluateToBoolean("IsNaturalAlternatingGroup( " + g + ");"))
            return PermutationGroup.alternatingGroup(degree);
        return evaluateToPermutationGroup(g);
    }

    public void close() {
        gapCmd.close();
        try {
            gapProcess.waitFor();
            readThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertToGapList(int[] array) {
        StringBuilder sb = new StringBuilder().append("[");
        for (int i = 0; ; ++i) {
            sb.append(array[i] + 1);
            if (i == array.length - 1)
                return sb.append("]").toString();
            sb.append(", ");
        }
    }

    private static String stringToGapCommand(String string) {
        string = string.trim();
        if (string.charAt(string.length() - 1) != ';')
            string += ";";
        return string;
    }

    private static String stringFromGapCommand(String string) {
        string = string.trim();
        if (string.charAt(string.length() - 1) == ';')
            string = string.substring(0, string.length() - 1);
        return string;
    }

    private static final class GapOutputReader implements Runnable {
        final ArrayBlockingQueue<String> buffer = new ArrayBlockingQueue<>(128);
        final BufferedReader reader;


        private GapOutputReader(InputStream inputStream) {
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public void run() {
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    line = line.replace("gap>", "");
                    line = line.trim();
                    if (!line.isEmpty() && line.charAt(line.length() - 1) == '\\')
                        line = line.substring(0, line.length() - 1);
                    if (line.equals("EOF")) {
                        try {
                            buffer.put(builder.toString());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        builder = new StringBuilder();
                    } else
                        builder.append(line);
                }
                System.out.println("GAP closed.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
