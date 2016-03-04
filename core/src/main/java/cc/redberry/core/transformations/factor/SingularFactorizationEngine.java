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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.utils.TensorUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Not functional yet.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SingularFactorizationEngine
        implements TransformationToStringAble, AutoCloseable, Closeable {

    public static final String command = "" +
            "short = 0;" +
            "poly p = POLYNOMIAL;" +
            "list f = factorize(p);" +
            "for(int i=1; i<=size(f[1]); i++) { " +
            "    print(\"@(\" + string(f[1][i]) + \")^\" + string(f[2][i])); " +
            "}\n" +
            "print(\"DONE\");";

    private final Process process;
    private final BufferedReader reader;
    private final PrintStream writer;


    public SingularFactorizationEngine(String singularBin) throws IOException {
        this.process = new ProcessBuilder().command(singularBin).start();
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.writer = new PrintStream(process.getOutputStream());
    }

    @Override
    public Tensor transform(Tensor t) {
        writer.println(createRing(TensorUtils.getAllDiffSimpleTensors(t).toArray(new SimpleTensor[1])));
        writer.flush();
        writer.println(command.replace("POLYNOMIAL", t.toString(OutputFormat.WolframMathematica)));
        writer.flush();
        List<Tensor> factors = new ArrayList<>();
        String s;
        try {
            while ((s = reader.readLine()) != null) {
                if (s.startsWith("//"))
                    continue;
                if (s.equals("DONE"))
                    break;
                if (s.startsWith("@"))
                    factors.add(Tensors.parse(s.substring(1).replace("^", "**")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Tensors.multiply(factors);
    }

    private static String createRing(Tensor... vars) {
        StringBuilder sb = new StringBuilder();
        sb.append("ring r = 0,(");
        for (int i = 0; ; i++) {
            sb.append(vars[i]);
            if (i == vars.length - 1)
                break;
            sb.append(',');
        }
        sb.append("),dp;");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "SingularFactorization";
    }

    @Override
    public void close() throws IOException {
        process.destroy();
        writer.close();
        reader.close();
    }
}
