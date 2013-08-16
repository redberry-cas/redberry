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
package cc.redberry.core.solver;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.utils.THashMap;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExternalSolver {


    public static Expression[] solveSystemWithMaple(ReducedSystem reducedSystem,
                                                    boolean keepFreeParameters,
                                                    String mapleBinDir,
                                                    String path)
            throws IOException, InterruptedException {
        return solveSystemWithExternalProgram(MapleScriptCreator.INSTANCE, reducedSystem, keepFreeParameters, mapleBinDir, path);
    }


    public static Expression[] solveSystemWithMathematica(ReducedSystem reducedSystem,
                                                          boolean keepFreeParameters,
                                                          String mathematicaBinDir,
                                                          String path)
            throws IOException, InterruptedException {
        return solveSystemWithExternalProgram(MathematicaScriptCreator.INSTANCE, reducedSystem, keepFreeParameters, mathematicaBinDir, path);
    }

    public static Expression[] solveSystemWithExternalProgram(ExternalScriptCreator scriptCreator,
                                                              ReducedSystem reducedSystem,
                                                              boolean keepFreeParameters,
                                                              String programBinDir,
                                                              String path)
            throws IOException, InterruptedException {
        //create the general form of the inverse and system of linear equations
        final Expression[] equations = reducedSystem.equations.clone();

        /*in order to process equations with Maple we must to replace all tensors
        with indices (they are found only in scalar combinations) with some symbols*/

        //scalar tensor <-> symbol
        THashMap<Tensor, Tensor> tensorSubstitutions = new THashMap<>();
        //all symbols will have names scalar1,scalar2, etc.

        //processing equations
        int i;
        for (i = 0; i < equations.length; ++i) {
            Expression eq = equations[i];
            //iterating over the whole equation
            FromChildToParentIterator iterator = new FromChildToParentIterator(eq);
            Tensor t;
            while ((t = iterator.next()) != null) {
                if (!(t instanceof Product) || t.getIndices().size() == 0)
                    continue;
                //scalars content
                Tensor[] scalars = ((Product) t).getContent().getScalars();
                for (Tensor scalar : scalars) {
                    if (!tensorSubstitutions.containsKey(scalar)) {
                        //map does not contains rule for current scalar (e.g. k_{i}*k^{i})
                        //adding new rule for the scalar, e.g. k_{i}*k^{i} = scalar2
                        tensorSubstitutions.put(scalar, CC.generateNewSymbol());
                    }
                }
            }
        }

        final Expression[] scalarSubs = new Expression[tensorSubstitutions.size()];
        i = -1;
        for (Map.Entry<Tensor, Tensor> entry : tensorSubstitutions.entrySet())
            scalarSubs[++i] = Tensors.expression(entry.getKey(), entry.getValue());
        SubstitutionTransformation fullSub = new SubstitutionTransformation(scalarSubs, true);
        for (i = 0; i < equations.length; ++i)
            equations[i] = (Expression) fullSub.transform(equations[i]);


        /*if (scalarSubs.length != 0) {
            StringBuilder scalarsString = new StringBuilder().append('[');
            for (Expression sub : scalarSubs)
                scalarsString.append(sub).append(", ");
            scalarsString.deleteCharAt(scalarsString.length() - 1).deleteCharAt(scalarsString.length() - 1).append(']');
         System.out.println(scalarsString.toString());
        }*/

        /*System.out.println("Reduced solution: " + Arrays.toString(reducedSystem.generalSolutions));
        System.out.println();*/

        scriptCreator.createScript(equations, reducedSystem, path);

        //cleaning previous output
        new File(path + "/equations." + scriptCreator.getScriptExtension() + "Out").delete();

        //running external program
        try {
            Process p = Runtime.getRuntime().exec(programBinDir + "/" + scriptCreator.getScriptExecutionCommand() + " " + path + "/equations." + scriptCreator.getScriptExtension());
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = bri.readLine()) != null)
                System.out.println(line);
            bri.close();
            while ((line = bre.readLine()) != null)
                System.out.println(line);
            bre.close();
            p.waitFor();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        //reading the produced output with the solution
        //allocating resulting coefficients array
        Expression[] coefficientsResults = new Expression[reducedSystem.unknownCoefficients.length];
        //no solutions
        if (!new File(path + "/equations." + scriptCreator.getScriptExtension() + "Out").exists()) {
            return new Expression[0];
        }
        FileInputStream fstream = new FileInputStream(path + "/equations." + scriptCreator.getScriptExtension() + "Out");
        if (fstream.available() == 0)
            return null;
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        i = -1;
        //reading resulting solutions from file
        while ((strLine = br.readLine()) != null)
            coefficientsResults[++i] = Tensors.parseExpression(strLine);
        Expression[] solution = reducedSystem.generalSolutions.clone();

        //substituting coefficients into general inverse form
        List<Transformation> zeroSubs = new ArrayList<>();
        for (Expression coef : coefficientsResults)
            if (coef.isIdentity())//if current coefficient is free parameter
            {
                zeroSubs.add(Tensors.expression(coef.get(0), Complex.ZERO));
            } else {
                for (int si = solution.length - 1; si >= 0; --si)
                    solution[si] = (Expression) coef.transform(solution[si]);
            }
        if (!keepFreeParameters)
            for (int si = solution.length - 1; si >= 0; --si)
                solution[si] = (Expression) new TransformationCollection(zeroSubs).transform(solution[si]);


        //substituting the renamed tensors combinations
        for (Expression sub : scalarSubs)
            for (int si = solution.length - 1; si >= 0; --si)
                solution[si] = (Expression) sub.transpose().transform(solution[si]);
        in.close();
        return solution;
    }

    public static interface ExternalScriptCreator {
        void createScript(Expression[] equations, ReducedSystem reducedSystem, String path) throws IOException;

        String getScriptExecutionCommand();

        String getScriptExtension();
    }

    public static final class MathematicaScriptCreator implements ExternalScriptCreator {
        public static final MathematicaScriptCreator INSTANCE = new MathematicaScriptCreator();

        private MathematicaScriptCreator() {
        }

        @Override
        public void createScript(Expression[] equations, ReducedSystem reducedSystem, String path) throws IOException {
            //creating file with Mathematica code to solve the system of equations
            FileOutputStream output = new FileOutputStream(path + "/equations.mathematica");
            PrintStream file = new PrintStream(output);

            file.append("$equations = {\n");
            int i;
            for (i = 0; ; i++) {
                file.append(equations[i].toString(OutputFormat.WolframMathematica).replace("=", "=="));
                if (i == equations.length - 1)
                    break;
                file.append(",\n");
            }
            file.append("\n};\n");

            file.append("$coefficients = {");
            for (i = 0; ; i++) {
                file.append(reducedSystem.unknownCoefficients[i].toString(OutputFormat.WolframMathematica));
                if (i == reducedSystem.unknownCoefficients.length - 1)
                    break;
                file.append(',');
            }
            file.append(" };\n");

            file.append("$result = Solve[$equations,$coefficients];\n");
            file.append("If[Length[$result] != 0, ");
            file.append("$result = Simplify[$result][[1]];\n");
            file.append("$found = $result[[All, 1]];\n");
            file.append("For[$i = 1, $i <= Length[$coefficients], ++$i,If[!MemberQ[$found, $coefficients[[$i]]], $result = $result/.{$coefficients[[$i]] -> 0}; AppendTo[$result, $coefficients[[$i]] -> 0];]];\n");
            file.append("$result = Simplify[$result];\n");

//            file.append("left = Array[Function[x,Coefficient[equations[[x, 1]], #] & /@ coefficients], Length[equations]];\n");
//            file.append("right = equations[[All, 2]];\n");
//            file.append("result = LinearSolve[left, right]\n");

            file.append("$stream = OpenWrite[\"" + path + "/equations.mathematicaOut\"];\n");
//            file.append("For[i = 1, i <= Length[coefficients], ++i, WriteString[stream, StringReplace[ToString[coefficients[[i]] == result[[i]] // InputForm], {\"==\" -> \"=\", \"^\" -> \"**\"}] <> If[i != Length[coefficients], \"\\n\", \"\"]]];\n");
            file.append("For[$i = 1, $i <= Length[$coefficients], ++$i, WriteString[$stream, StringReplace[ToString[$result[[$i]] // InputForm], {\"->\" -> \"=\", \"^\" -> \"**\"}] <> If[$i != Length[$coefficients], \"\\n\", \"\"]]];\n");
            file.append("Close[$stream];");
            file.append("];");
            output.close();
            file.close();
        }

        @Override
        public String getScriptExecutionCommand() {
            return "MathematicaScript -script";
        }

        @Override
        public String getScriptExtension() {
            return "mathematica";
        }
    }

    public static final class MapleScriptCreator implements ExternalScriptCreator {
        public static final MapleScriptCreator INSTANCE = new MapleScriptCreator();

        private MapleScriptCreator() {
        }

        @Override
        public void createScript(Expression[] equations, ReducedSystem reducedSystem, String path) throws IOException {
            //creating file with Maple code to solve the system of equations
            FileOutputStream output = new FileOutputStream(path + "/equations.maple");
            PrintStream file = new PrintStream(output);
            file.append("with(StringTools):\n");
            file.append("ans:=array([");
            int i;
            for (i = 0; i < reducedSystem.unknownCoefficients.length; ++i)
                if (i == reducedSystem.unknownCoefficients.length - 1)
                    file.append(reducedSystem.unknownCoefficients[i].toString());
                else
                    file.append(reducedSystem.unknownCoefficients[i] + ",");
            file.append("]):\n");

            file.println("eq:=array(1.." + equations.length + "):");
            for (i = 0; i < equations.length; i++)
                file.println("eq[" + (i + 1) + "]:=" + equations[i] + ":");

            file.print("Result := solve(simplify({seq(eq[i],i=1.." + equations.length + ")}),[");
            for (i = 0; i < reducedSystem.unknownCoefficients.length; ++i)
                if (i == reducedSystem.unknownCoefficients.length - 1)
                    file.append(reducedSystem.unknownCoefficients[i].toString());
                else
                    file.append(reducedSystem.unknownCoefficients[i] + ",");
            file.append("]):\n");
            file.append("if nops(Result) <> 0 then\n");
            file.append("Result:= factor(Result);\n");
            file.println("file:=fopen(\"" + path + "/equations.mapleOut\",WRITE):");
            file.append("for temp_var_3x66y66i3 from 1 to " + reducedSystem.unknownCoefficients.length + " do\n");
            file.append("temp1 := SubstituteAll(convert(lhs(Result[1][temp_var_3x66y66i3]), string), \"^\", \"**\");\n");
            file.append("temp2 := SubstituteAll(convert(rhs(Result[1][temp_var_3x66y66i3]), string), \"^\", \"**\");\n");
            file.append("fprintf(file,\"%s=%s\\n\",temp1,temp2);\n");
            file.append("od:\n");
            file.append("end if;\n");
            file.append("fclose(file):");
            output.close();
            file.close();
        }

        @Override
        public String getScriptExecutionCommand() {
            return "maple";
        }

        @Override
        public String getScriptExtension() {
            return "maple";
        }
    }
}
