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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.number.parser.NumberParserTest;
import cc.redberry.core.parser.ParseTokenSimpleTensor;
import cc.redberry.core.parser.ParserTest;
import cc.redberry.core.parser.preprocessor.IndicesInsertion;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.utils.Indicator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TestParserGlobally {

    @Before
    public void beforeMethod() {
        CC.setDefaultOutputFormat(OutputFormat.Redberry);
    }

    @After
    public void tearDown() throws Exception {
        CC.setParserAllowsSameVariance(false);
    }

    private static final String path = "src/test";

    @Test
    public void testAllExpressionsInTestDirectory() {
        runTests(false, false);
    }

    @Test
    public void testAllExpressionsInTestDirectory_allowSameInvariance() {
        runTests(true, false);
    }

    @Test
    public void testAllExpressionsInTestDirectory_withSameInvariance() {
        runTests(true, true);
    }

    public void runTests(boolean allowSameVariance, boolean raiseOrLowerAll) {
        CC.setParserAllowsSameVariance(allowSameVariance);
        File testDirectory = new File(path);
        Counter c = new Counter(), m = new Counter();
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        testParseRecurrently(testDirectory, c, m, statistics, raiseOrLowerAll);
        System.out.println("Total number of lines containing parse(..): " + c.counter);
        System.out.println("Total number of matched and parsed lines: " + m.counter);
        System.out.println("Strings statistics: \n\t" + statistics.toString().replace("\n", "\n\t"));
        //Assert.assertTrue((c.counter - m.counter) < 2);
    }

    private static final Pattern pattern = Pattern.compile(
            "(parse|parseExpression|parseSimple)" +
                    "\\([\\s]*\"([a-zA-Z0-9=\\s\\*\\\\:+\\/\\-\\.\\,\\\\_\\^\\}\\{\\]\\[\\)\\(\"\n\"]*)\"\\)[\\s]*" +
                    "(\\;|\\,|[\\)]*[\\;]*|\\.)");

    private static final class Counter {
        int counter = 0;

        void increase() {
            ++counter;
        }
    }

    private static void testParseRecurrently(File file,
                                             Counter containsParseLinesCounter,
                                             Counter matchedLinesCounter,
                                             DescriptiveStatistics statistics,
                                             boolean raiseOrLowerAll) {
        if (file.isFile()) {
            if (file.getName().equals(TestParserGlobally.class.getSimpleName() + ".java"))
                return;
            if (file.getName().equals(ParserTest.class.getSimpleName() + ".java"))
                return;
            if (file.getName().equals(NumberParserTest.class.getSimpleName() + ".java"))
                return;

            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException();
            }
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            String string;
            try {
                boolean containsParse;
                boolean matchedParse;
                int lineNumber = -1;
                String bufferedString = null;
                while ((string = bufferedReader.readLine()) != null) {
                    ++lineNumber;
                    if (bufferedString != null) {
                        string = bufferedString + "\n" + string;
                        bufferedString = null;
                    }
                    if (string.contains("expected = InconsistentIndicesException.class")) {
                        bufferedString = null;
                        //skip next line
                        bufferedReader.readLine();
                        ++lineNumber;

                        while ((string = bufferedReader.readLine()) != null) {
                            ++lineNumber;
                            if (string.contains("@Test"))
                                break;
                        }
                        continue;
                    }

                    matchedParse = false;
                    if (string.contains("IndexMappingTestUtils.parse") || string.contains("ParserIndices.parse"))
                        continue;
                    containsParse = (string.contains("parse(") || string.contains("parseExpression(") || string.contains("parseSimple(")) && string.contains("\"");
                    string = string.trim();
                    if (string.length() > 0) {
                        char c = string.charAt(string.length() - 1);
                        if (c == '\"' || c == '+' || c == '(') {
                            bufferedString = string;
                            continue;
                        }
                    }

                    string = string.replaceAll("\n", "");
                    string = string.replaceAll("\"[\\s]*\\+[\\+]*[\\s]*\"", "");
                    Matcher matcher = pattern.matcher(string);
                    String tensorString;
                    Tensor tensor;
                    while (matcher.find()) {
                        matchedParse = true;
                        tensorString = matcher.group(2);
                        //if (tensorString.length() > 100)
                        //    System.out.println("\"" + tensorString + "\",");
                        tensorString = tensorString.replace("\\\\", "\\");
                        if (tensorString.contains("\"")) {
                            tensorString = tensorString.split("\"")[0];
                        }

                        if (tensorString.contains("Tr["))
                            continue;

                        try {
                            statistics.addValue(tensorString.length());
                            tensor = Tensors.parse(tensorString);
                            checkTensor(tensor);

                            if (raiseOrLowerAll) {
                                Tensor tensor1 = Tensors.parse(tensorString.replace("^", "_"));
                                checkTensor(tensor1);
                                if (tensorString.length() < 500)
                                    TAssert.assertEquals(lower(tensor), lower(tensor1));
                            }
                        } catch (AssertionError | RuntimeException e) {

                            System.out.println(e.getClass().getSimpleName() + ":");
                            System.out.println(tensorString);
                            System.out.println(file + "  line: " + lineNumber);
                            System.out.println();
                            throw new RuntimeException(e);
                        }

                    }

                    if (containsParse && !matchedParse && bufferedString == null) {
//                        System.out.println("Parse but not matched:");
//                        System.out.println(string);
//                        System.out.println(file + "  line: " + lineNumber);
//                        System.out.println();
                    }
                    if (containsParse && bufferedString == null)
                        containsParseLinesCounter.increase();
                    if (matchedParse)
                        matchedLinesCounter.increase();
                }
                bufferedReader.close();
                dataInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }

        } else if (file.isDirectory()) {
            File[] listOfFiles = file.listFiles();
            if (listOfFiles != null) {
                for (int i = 0; i < listOfFiles.length; i++)
                    testParseRecurrently(listOfFiles[i], containsParseLinesCounter, matchedLinesCounter, statistics, raiseOrLowerAll);
            }
        } else
            throw new RuntimeException(file.toString());
    }

    private static Tensor lower(Tensor t) {
        int[] ints = t.getIndices().getFree().toArray();
        int lower[] = new int[ints.length];
        for (int i = 0; i < ints.length; i++)
            lower[i] = IndicesUtils.lower(ints[i]);
        return ApplyIndexMapping.applyIndexMapping(t, new Mapping(ints, lower));
    }

    private static void checkTensor(Tensor t) {
        TAssert.assertIndicesConsistency(t);
        TAssert.assertEqualsExactly(t, Tensors.parse(t.toString()));
        //t = inverseIndices(t);
        //TAssert.assertEqualsExactly(t, Tensors.parse(t.toString()));
    }

    private static Tensor inverseIndices(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (c instanceof SimpleTensor) {
                SimpleTensor st = (SimpleTensor) c;
                iterator.set(Tensors.simpleTensor(st.getName(), st.getIndices().getInverted()));
            }
            if (c instanceof TensorField) {
                TensorField f = (TensorField) c;
                iterator.set(Tensors.field(f.getName(), f.getIndices().getInverted(), f.argIndices, f.args));
            }
        }
        return iterator.result();
    }

    private static final String Flat_ = "Flat=(1/4)*HATS*HATS*HATS*HATS-HATW*HATS*HATS+(1/2)*HATW*HATW+HATS*HATN-HATM+(L-2)*NABLAS_\\mu*HATW^\\mu-L*NABLAS_\\mu*HATW*HATK^\\mu+(1/3)*((L-1)*NABLAS_\\mu^\\mu*HATS*HATS-L*NABLAS_\\mu*HATK^\\mu*HATS*HATS-(L-1)*NABLAS_\\mu*HATS*HATS^\\mu+L*NABLAS_\\mu*HATS*HATS*HATK^\\mu)-(1/2)*NABLAS_\\mu*NABLAS_\\nu*DELTA^{\\mu\\nu}-(1/4)*(L-1)*(L-2)*NABLAS_\\mu*NABLAS_\\nu^{\\mu\\nu}+(1/2)*L*(L-1)*(1/2)*(NABLAS_\\mu*NABLAS_{\\nu }^{\\nu}+NABLAS_{\\nu }*NABLAS_{\\mu }^{\\nu})*HATK^\\mu";
    private static final String WR_ = "WR=-(1/2)*Power[L,2]*HATW*HATF_{\\mu\\nu}*Kn^\\mu*HATK^\\nu+(1/3)*L*HATW*HATK^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}+(1/3)*Power[L,2]*(L-1)*HATW*HATK^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}-(1/6)*(L-2)*(L-3)*HATW^{\\mu\\nu}*R_{\\mu\\nu}";
    private static final String SR_ = "SR=-(1/6)*Power[L,2]*(L-1)*HATS*NABLAF_{\\mu\\alpha\\nu}*Kn^{\\mu\\nu}*HATK^\\alpha+(2/3)*L*HATS*NABLAF_{\\mu\\nu\\alpha}*Kn^\\alpha*DELTA^{\\mu\\nu}-(1/12)*(L-1)*(L-2)*(L-3)*HATS^{\\alpha\\mu\\nu}*NABLAR_{\\alpha\\mu\\nu}-(1/12)*Power[L,2]*(L-1)*(L-2)*HATS*HATK^{\\mu\\nu\\alpha}*HATK^\\beta*n_\\sigma*NABLAR_\\alpha^\\sigma_{\\mu\\beta\\nu}+L*(L-1)*HATS*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta}*n_\\sigma*((5/12)*NABLAR_\\alpha^\\sigma_{\\nu\\beta\\mu}-(1/12)*NABLAR_{\\mu}^\\sigma_{\\alpha\\nu\\beta})-(1/2)*L*HATS*HATK^\\beta*DELTA^{\\mu\\nu\\alpha}*n_\\sigma*NABLAR_{\\alpha}^{\\sigma}_{\\mu\\beta\\nu}";
    private static final String SSR_ = "SSR=-(1/2)*L*(L-1)*HATS*HATS^\\mu*HATF_{\\mu\\nu}*HATK^{\\nu}+(1/2)*Power[L,2]*HATS*HATS*HATF_{\\mu\\nu}*Kn^{\\mu}*HATK^\\nu+(1/12)*(L-1)*(L-2)*HATS*HATS^{\\mu\\nu}*R_{\\mu\\nu}+(1/3)*L*(L-1)*HATS*HATS^\\mu*HATK^\\nu*R_{\\mu\\nu}+(1/6)*HATS*HATS*DELTA^{\\mu\\nu}*R_{\\mu\\nu}-(1/6)*L*(L-1)*(L-2)*HATS*HATS^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}+(1/3)*(L-1)*HATS*HATS^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}-(1/3)*Power[L,2]*(L-1)*HATS*HATS*HATK^{\\mu\\nu}*HATK^\\alpha*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}-(1/3)*L*HATS*HATS*HATK^\\alpha*DELTA^{\\mu\\nu}*n_\\sigma*R^\\sigma_{\\mu\\alpha\\nu}";
    private static final String FF_ = "FF=-(1/24)*L*L*(L-1)*(L-1)*HATK^{\\mu\\nu}*F_{\\mu\\alpha}*HATK^{\\alpha\\beta}*F_{\\nu\\beta}+(1/24)*L*L*HATK^\\mu*F_{\\beta\\nu}*DELTA^{\\alpha\\beta}*HATK^\\nu*F_{\\alpha\\mu}-(5/24)*L*L*HATK^\\mu*F_{\\beta\\mu}*DELTA^{\\alpha\\beta}*HATK^\\nu*F_{\\alpha\\nu}-(1/48)*L*L*(L-1)*HATK^\\mu*F_{\\beta\\nu}*DELTA^\\nu*HATK^{\\alpha\\beta}*F_{\\alpha\\mu}-(1/48)*L*L*(L-1)*HATK^\\mu*F_{\\beta\\mu}*DELTA^\\nu*HATK^{\\alpha\\beta}*F_{\\alpha\\nu}";
    private static final String FR_ = "FR=(1/40)*Power[L,2]*(L-1)*(L-2)*DELTA^\\mu*HATK^\\nu*HATK^{\\alpha\\beta\\gamma}*F_{\\mu\\alpha}*n_\\sigma*R^\\sigma_{\\gamma\\beta\\nu}-Power[L,2]*(L-1)*(L-2)*DELTA^\\nu*HATK^{\\alpha\\beta\\gamma}*HATK^\\mu*n_\\sigma*((1/60)*R^\\sigma_{\\beta\\gamma\\mu}*F_{\\alpha\\nu}+(1/12)*R^\\sigma_{\\beta\\gamma\\nu}*F_{\\alpha\\mu})+Power[L,2]*Power[(L-1),2]*DELTA^\\alpha*HATK^{\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\sigma*((1/60)*R^\\sigma_{\\beta\\mu\\gamma}*F_{\\alpha\\nu}+(1/20)*R^\\sigma_{\\alpha\\mu\\gamma}*F_{\\nu\\beta}+(1/15)*R^\\sigma_{\\gamma\\mu\\alpha}*F_{\\nu\\beta}+(1/60)*R^\\sigma_{\\mu\\nu\\gamma}*F_{\\alpha\\beta})+Power[L,2]*(L-1)*DELTA^{\\alpha\\beta}*HATK^{\\gamma\\delta}*HATK^{\\mu}*n_\\sigma*((4/15)*R^\\sigma_{\\delta\\beta\\gamma}*F_{\\alpha\\mu}-(1/30)*R^\\sigma_{\\beta\\delta\\alpha}*F_{\\gamma\\mu}-(1/15)*R^\\sigma_{\\alpha\\gamma\\mu}*F_{\\beta\\delta}-(1/30)*R^\\sigma_{\\gamma\\alpha\\mu}*F_{\\beta\\delta})+Power[L,2]*(L-1)*DELTA^{\\alpha\\beta}*HATK^\\gamma*HATK^{\\mu\\nu}*n_\\sigma*((7/60)*R^\\sigma_{\\alpha\\beta\\mu}*F_{\\gamma\\nu}-(11/60)*R^\\sigma_{\\beta\\mu\\gamma}*F_{\\alpha\\nu}+(1/5)*R^\\sigma_{\\mu\\alpha\\gamma}*F_{\\beta\\nu}+(1/60)*R^\\sigma_{\\mu\\alpha\\nu}*F_{\\gamma\\beta})+Power[L,2]*DELTA^{\\mu\\alpha\\beta}*HATK^\\gamma*HATK^\\nu*n_\\sigma*((7/20)*R^\\sigma_{\\alpha\\gamma\\beta}*F_{\\nu\\mu}+(1/10)*R^\\sigma_{\\alpha\\beta\\nu}*F_{\\gamma\\mu})";
    private static final String RR_ = "RR=(1/10)*Power[L,2]*HATK^\\delta*DELTA^{\\mu\\nu\\alpha\\beta}*HATK^\\gamma*n_\\sigma*n_\\rho*R^\\sigma_{\\alpha\\beta\\gamma}*R^\\rho_{\\mu\\nu\\delta}+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{\\beta\\gamma\\delta}*DELTA^\\alpha*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*((2/45)*R^\\rho_{\\alpha\\delta\\nu}*R^\\sigma_{\\beta\\mu\\gamma}-(1/120)*R^\\rho_{\\delta\\alpha\\nu}*R^\\sigma_{\\beta\\mu\\gamma})+Power[L,2]*(L-1)*HATK^\\delta*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*((-1/10)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\beta}+(1/15)*R^\\rho_{\\delta\\alpha\\nu}*R^\\sigma_{\\beta\\mu\\gamma}+(1/60)*R^\\rho_{\\beta\\delta\\nu}*R^\\sigma_{\\gamma\\mu\\alpha})+Power[L,2]*Power[(L-1),2]*HATK^{\\gamma\\delta}*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*(-(1/20)*R^\\rho_{\\mu\\beta\\nu}*R^\\sigma_{\\delta\\alpha\\gamma}+(1/180)*R^\\rho_{\\alpha\\nu\\beta}*R^\\sigma_{\\gamma\\delta\\mu}-(7/360)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\beta}-(1/240)*R^\\rho_{\\delta\\beta\\nu}*R^\\sigma_{\\gamma\\alpha\\mu}-(1/120)*R^\\rho_{\\beta\\gamma\\nu}*R^\\sigma_{\\alpha\\delta\\mu}-(1/30)*R^\\rho_{\\delta\\beta\\nu}*R^\\sigma_{\\alpha\\gamma\\mu})+Power[L,2]*(L-1)*(L-2)*HATK^\\delta*DELTA^{\\mu\\nu}*HATK^{\\alpha\\beta\\gamma}*n_\\sigma*n_\\rho*((-1/30)*R^\\rho_{\\gamma\\nu\\beta}*R^\\sigma_{\\alpha\\delta\\mu}-(1/180)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}+(1/180)*R^\\rho_{\\mu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\nu})+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{\\mu\\nu}*DELTA^{\\delta}*HATK^{\\alpha\\beta\\gamma}*n_\\sigma*n_\\rho*((1/45)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}-(1/80)*R^\\rho_{\\beta\\nu\\gamma}*R^\\sigma_{\\mu\\alpha\\delta}+(1/90)*R^\\rho_{\\beta\\nu\\gamma}*R^\\sigma_{\\delta\\alpha\\mu})+Power[L,2]*(L-1)*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta\\gamma}*HATK^\\delta*n_\\sigma*n_\\rho*((7/120)*R^\\rho_{\\beta\\gamma\\nu}*R^\\sigma_{\\mu\\alpha\\delta}-(3/40)*R^\\rho_{\\beta\\gamma\\delta}*R^\\sigma_{\\mu\\alpha\\nu}+(1/120)*R^\\rho_{\\delta\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\mu})+Power[L,2]*(L-1)*(L-2)*HATK^{\\alpha\\beta\\gamma}*DELTA^{\\mu\\nu}*HATK^\\delta*n_\\sigma*n_\\rho*(-(1/24)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}-(1/180)*R^\\rho_{\\nu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\mu}-(1/360)*R^\\rho_{\\delta\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\mu})-(1/120)*Power[L,2]*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}*DELTA^{\\delta}*HATK^\\gamma*n_\\sigma*n_\\rho*R^\\rho_{\\alpha\\beta\\gamma}*R^\\sigma_{\\mu\\nu\\delta}-(1/80)*Power[L,2]*Power[(L-1),2]*(L-2)*(L-3)*HATK^{\\alpha\\beta\\gamma\\delta}*HATK^{\\mu\\nu}*n_\\sigma*n_\\rho*R^\\rho_{\\beta\\gamma\\mu}*R^\\sigma_{\\alpha\\delta\\nu}+Power[L,2]*HATK^\\mu*DELTA^{\\alpha\\beta\\gamma}*HATK^\\nu*n_\\rho*(-(1/8)*R_{\\beta\\gamma}*R^\\rho_{\\nu\\alpha\\mu}+(3/20)*R_{\\beta\\gamma}*R^\\rho_{\\mu\\alpha\\nu}+(3/40)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}+(1/40)*R^\\sigma_{\\beta\\gamma\\mu}*R^\\rho_{\\nu\\alpha\\sigma}-(3/20)*R^\\sigma_{\\alpha\\beta\\mu}*R^\\rho_{\\gamma\\nu\\sigma}+(1/10)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\gamma\\mu\\sigma})+Power[L,2]*(L-1)*HATK^\\gamma*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_\\rho*((1/20)*R_{\\alpha\\nu}*R^\\rho_{\\gamma\\beta\\mu}+(1/20)*R_{\\alpha\\gamma}*R^\\rho_{\\mu\\beta\\nu}+(1/10)*R_{\\alpha\\beta}*R^\\rho_{\\mu\\gamma\\nu}+(1/20)*R^\\sigma_{\\alpha\\nu\\gamma}*R^\\rho_{\\sigma\\beta\\mu}-(1/60)*R^\\sigma_{\\mu\\alpha\\nu}*R^\\rho_{\\beta\\sigma\\gamma}+(1/10)*R^\\sigma_{\\alpha\\beta\\gamma}*R^\\rho_{\\mu\\sigma\\nu}-(1/12)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\mu\\sigma\\gamma})+Power[L,2]*Power[(L-1),2]*HATK^{\\alpha\\beta}*DELTA^{\\gamma}*HATK^{\\mu\\nu}*n_\\rho*((1/60)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\nu\\gamma}-(1/20)*R_{\\alpha\\mu}*R^\\rho_{\\gamma\\nu\\beta}+(1/120)*R_{\\alpha\\beta}*R^\\rho_{\\mu\\nu\\gamma}+(3/40)*R_{\\alpha\\gamma}*R^\\rho_{\\nu\\beta\\mu}+(1/20)*R^\\sigma_{\\gamma\\mu\\alpha}*R^\\rho_{\\nu\\sigma\\beta}+(1/120)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\beta\\nu\\sigma}-(1/40)*R^\\sigma_{\\alpha\\mu\\gamma}*R^\\rho_{\\sigma\\nu\\beta}+(1/40)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\sigma\\nu\\gamma}-(1/20)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\gamma\\nu\\sigma}-(1/40)*R^\\sigma_{\\mu\\beta\\nu}*R^\\rho_{\\gamma\\sigma\\alpha})+Power[L,2]*(L-1)*HATK^{\\alpha\\beta}*DELTA^{\\mu\\nu}*HATK^{\\gamma}*n_\\rho*((1/20)*R^\\sigma_{\\mu\\nu\\beta}*R^\\rho_{\\gamma\\sigma\\alpha}-(7/60)*R^\\sigma_{\\beta\\mu\\alpha}*R^\\rho_{\\gamma\\nu\\sigma}+(1/20)*R^\\sigma_{\\beta\\mu\\alpha}*R^\\rho_{\\sigma\\nu\\gamma}+(1/10)*R^\\sigma_{\\mu\\beta\\gamma}*R^\\rho_{\\nu\\alpha\\sigma}+(1/60)*R^\\sigma_{\\beta\\mu\\gamma}*R^\\rho_{\\alpha\\nu\\sigma}+(7/120)*R_{\\alpha\\beta}*R^\\rho_{\\nu\\gamma\\mu}+(11/60)*R_{\\beta\\mu}*R^\\rho_{\\nu\\alpha\\gamma})+Power[L,2]*(L-1)*(L-2)*HATK^{\\alpha\\beta\\gamma}*DELTA^{\\mu}*HATK^{\\nu}*n_\\rho*((7/240)*R_{\\alpha\\beta}*R^\\rho_{\\gamma\\mu\\nu}+(7/240)*R_{\\alpha\\nu}*R^\\rho_{\\beta\\gamma\\mu}-(1/60)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}-(1/24)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\sigma\\gamma\\mu}+(1/15)*R^\\sigma_{\\alpha\\beta\\nu}*R^\\rho_{\\mu\\gamma\\sigma}+(1/40)*R^\\sigma_{\\alpha\\beta\\mu}*R^\\rho_{\\sigma\\gamma\\nu}+(1/40)*R_{\\beta\\gamma}*R^\\rho_{\\nu\\mu\\alpha}+(1/48)*R^\\sigma_{\\beta\\gamma\\mu}*R^\\rho_{\\nu\\alpha\\sigma})+Power[L,2]*Power[(L-1),2]*(L-2)*HATK^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_\\rho*((-7/240)*R_{\\alpha\\mu}*R^\\rho_{\\beta\\gamma\\nu}+(1/240)*R_{\\beta\\gamma}*R^\\rho_{\\mu\\alpha\\nu}-(1/40)*R^\\sigma_{\\alpha\\mu\\beta}*R^\\rho_{\\nu\\gamma\\sigma})+L*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}*((1/180)*R_{\\mu\\nu}*R_{\\alpha\\beta}+(7/720)*R^\\sigma_{\\alpha\\beta\\rho}*R^\\rho_{\\mu\\nu\\sigma})";
    private static final String DELTA_1_ = "DELTA^\\mu=-L*HATK^\\mu";
    private static final String DELTA_2_ = "DELTA^{\\mu\\nu}=-(1/2)*L*(L-1)*HATK^{\\mu\\nu}+Power[L,2]*(1/2)*(HATK^{\\mu }*HATK^{\\nu }+HATK^{\\nu }*HATK^{\\mu })";
    private static final String DELTA_3_ = "DELTA^{\\mu\\nu\\alpha}=-(1/6)*L*(L-1)*(L-2)*HATK^{\\mu\\nu\\alpha}+(1/2)*Power[L,2]*(L-1)*(1/3)*(HATK^{\\mu \\nu }*HATK^{\\alpha }+HATK^{\\alpha \\nu }*HATK^{\\mu }+HATK^{\\mu \\alpha }*HATK^{\\nu })+1/2*Power[L,2]*(L-1)*(1/3)*(HATK^{\\alpha }*HATK^{\\mu \\nu }+HATK^{\\mu }*HATK^{\\alpha \\nu }+HATK^{\\nu }*HATK^{\\alpha \\mu })-Power[L,3]*(1/6)*(HATK^{\\mu }*HATK^{\\nu }*HATK^{\\alpha }+HATK^{\\mu }*HATK^{\\alpha }*HATK^{\\nu }+HATK^{\\nu }*HATK^{\\alpha }*HATK^{\\mu }+HATK^{\\nu }*HATK^{\\mu }*HATK^{\\alpha }+HATK^{\\alpha }*HATK^{\\mu }*HATK^{\\nu }+HATK^{\\alpha }*HATK^{\\nu }*HATK^{\\mu })";
    private static final String DELTA_4_ = "DELTA^{\\mu\\nu\\alpha\\beta}=-(1/24)*L*(L-1)*(L-2)*(L-3)*HATK^{\\mu\\nu\\alpha\\beta}+(1/6)*Power[L,2]*(L-1)*(L-2)*(1/4)*(HATK^{\\mu \\nu \\alpha }*HATK^{\\beta }+HATK^{\\mu \\nu \\beta }*HATK^{\\alpha }+HATK^{\\beta \\mu \\alpha }*HATK^{\\nu }+HATK^{\\nu \\beta \\alpha }*HATK^{\\mu })+(1/6)*Power[L,2]*(L-1)*(L-2)*(1/4)*(HATK^{\\beta }*HATK^{\\mu \\nu \\alpha }+HATK^{\\alpha }*HATK^{\\mu \\nu \\beta }+HATK^{\\mu }*HATK^{\\beta \\nu \\alpha }+HATK^{\\nu }*HATK^{\\beta \\mu \\alpha })+(1/4)*Power[L,2]*Power[(L-1),2]*(1/6)*(HATK^{\\mu\\nu}*HATK^{\\alpha\\beta}+HATK^{\\mu\\beta}*HATK^{\\alpha\\nu}+HATK^{\\mu\\alpha}*HATK^{\\nu\\beta}+HATK^{\\alpha\\nu}*HATK^{\\mu\\beta}+HATK^{\\beta\\nu}*HATK^{\\alpha\\mu}+HATK^{\\alpha\\beta}*HATK^{\\mu\\nu})-(1/2)*Power[L,3]*(L-1)*(1/12)*(HATK^{\\mu\\nu}*HATK^\\alpha*HATK^\\beta+HATK^{\\mu\\nu}*HATK^\\beta*HATK^\\alpha+HATK^{\\mu\\beta}*HATK^\\alpha*HATK^\\nu+HATK^{\\mu\\beta}*HATK^\\nu*HATK^\\alpha+HATK^{\\mu\\alpha}*HATK^\\nu*HATK^\\beta+HATK^{\\mu\\alpha}*HATK^\\beta*HATK^\\nu+HATK^{\\nu\\alpha}*HATK^\\mu*HATK^\\beta+HATK^{\\nu\\alpha}*HATK^\\beta*HATK^\\mu+HATK^{\\nu\\beta}*HATK^\\alpha*HATK^\\mu+HATK^{\\nu\\beta}*HATK^\\mu*HATK^\\alpha+HATK^{\\alpha\\beta}*HATK^\\mu*HATK^\\nu+HATK^{\\alpha\\beta}*HATK^\\nu*HATK^\\mu)-(1/2)*Power[L,3]*(L-1)*(1/12)*(HATK^\\alpha*HATK^{\\mu\\nu}*HATK^\\beta+HATK^\\beta*HATK^{\\mu\\nu}*HATK^\\alpha+HATK^\\alpha*HATK^{\\mu\\beta}*HATK^\\nu+HATK^\\nu*HATK^{\\mu\\beta}*HATK^\\alpha+HATK^\\nu*HATK^{\\mu\\alpha}*HATK^\\beta+HATK^\\beta*HATK^{\\mu\\alpha}*HATK^\\nu+HATK^\\mu*HATK^{\\nu\\alpha}*HATK^\\beta+HATK^\\beta*HATK^{\\nu\\alpha}*HATK^\\mu+HATK^\\alpha*HATK^{\\nu\\beta}*HATK^\\mu+HATK^\\mu*HATK^{\\nu\\beta}*HATK^\\alpha+HATK^\\mu*HATK^{\\alpha\\beta}*HATK^\\nu+HATK^\\nu*HATK^{\\alpha\\beta}*HATK^\\mu)-(1/2)*Power[L,3]*(L-1)*(1/12)*(HATK^\\alpha*HATK^\\beta*HATK^{\\mu\\nu}+HATK^\\beta*HATK^\\alpha*HATK^{\\mu\\nu}+HATK^\\alpha*HATK^\\nu*HATK^{\\mu\\beta}+HATK^\\nu*HATK^\\alpha*HATK^{\\mu\\beta}+HATK^\\nu*HATK^\\beta*HATK^{\\mu\\alpha}+HATK^\\beta*HATK^\\nu*HATK^{\\mu\\alpha}+HATK^\\mu*HATK^\\beta*HATK^{\\nu\\alpha}+HATK^\\beta*HATK^\\mu*HATK^{\\nu\\alpha}+HATK^\\alpha*HATK^\\mu*HATK^{\\nu\\beta}+HATK^\\mu*HATK^\\alpha*HATK^{\\nu\\beta}+HATK^\\mu*HATK^\\nu*HATK^{\\alpha\\beta}+HATK^\\nu*HATK^\\mu*HATK^{\\alpha\\beta})+(1/24)*Power[L,4]*(HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\beta}+HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\beta}+HATK^{\\beta}*HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu}+HATK^{\\nu}*HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu}+HATK^{\\beta}*HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu}+HATK^{\\mu}*HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\nu}+HATK^{\\mu}*HATK^{\\nu}*HATK^{\\beta}*HATK^{\\alpha}+HATK^{\\nu}*HATK^{\\mu}*HATK^{\\beta}*HATK^{\\alpha}+HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\beta}*HATK^{\\mu}+HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\mu}+HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\beta}*HATK^{\\nu}+HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\nu}+HATK^{\\beta}*HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha}+HATK^{\\nu}*HATK^{\\beta}*HATK^{\\mu}*HATK^{\\alpha}+HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\mu}*HATK^{\\beta}+HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\beta}+HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\mu}*HATK^{\\nu}+HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu}+HATK^{\\beta}*HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha}+HATK^{\\mu}*HATK^{\\beta}*HATK^{\\nu}*HATK^{\\alpha}+HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu}*HATK^{\\beta}+HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\beta}+HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\nu}*HATK^{\\mu}+HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\mu})";
    private static final String ACTION_ = "counterterms = Flat + WR + SR + SSR + FF + FR + RR";
    private static final String[] terms = {Flat_, WR_, SR_, SSR_, RR_, FF_, FR_, ACTION_};
    private static final String[] deltas = {DELTA_1_, DELTA_2_, DELTA_3_, DELTA_4_};

    @Test
    public void testTestStrings1() {
        final String[] matrices = new String[]{
                "KINV", "HATK", "HATW", "HATS", "NABLAS",
                "HATN", "HATF", "NABLAF", "HATM", "DELTA",
                "Flat", "FF", "WR", "SR", "SSR", "FR", "RR", "Kn"};

        //F_{\\mu\\nu} type structure
        final StructureOfIndices F_TYPE_STRUCTURE = StructureOfIndices.create(IndexType.GreekLower.getType(), 2);
        //matrices indicator for parse preprocessor
        final Indicator<ParseTokenSimpleTensor> matricesIndicator = new Indicator<ParseTokenSimpleTensor>() {

            @Override
            public boolean is(ParseTokenSimpleTensor object) {
                String name = object.name;
                for (String matrix : matrices)
                    if (name.equals(matrix))
                        return true;
                if (name.equals("F") && object.indices.getStructureOfIndices().equals(F_TYPE_STRUCTURE))
                    return true;
                return false;
            }
        };

        int i, matrixIndicesCount = 8;
        //indices to insert
        int upper[] = new int[matrixIndicesCount / 2], lower[] = upper.clone();
        for (i = 0; i < matrixIndicesCount / 2; ++i) {
            upper[i] = IndicesUtils.createIndex(99930 + i, IndexType.GreekLower, true);//30
            lower[i] = IndicesUtils.createIndex(99930 + i + matrixIndicesCount / 2, IndexType.GreekLower, false);
        }

        //preprocessor for Flat, WR, SR, SSR, FF, FR, RR, counterterms
        IndicesInsertion termIndicesInsertion = new IndicesInsertion(
                IndicesFactory.createSimple(null, upper),
                IndicesFactory.createSimple(null, IndicesUtils.getIndicesNames(upper)),
                matricesIndicator);

        Tensor tensor;
        for (String str : terms) {
            tensor = Tensors.parse(str, termIndicesInsertion);
            TAssert.assertTrue(simpleTensorCount(tensor) > 6);
            TAssert.assertEqualsExactly(tensor, Tensors.parse(tensor.toString()));
        }
        //preprocessor for DELTA_1,2,3,4
        IndicesInsertion deltaIndicesInsertion = new IndicesInsertion(
                IndicesFactory.createSimple(null, upper),
                IndicesFactory.createSimple(null, lower),
                matricesIndicator);

        for (String str : deltas) {
            tensor = Tensors.parse(str, deltaIndicesInsertion);
            TAssert.assertTrue(simpleTensorCount(tensor) >= 3);
            checkTensor(tensor);
        }
    }

    private static final String[] testStrings3 = {
            "d_p^a*d_q^b*d_r^c+6*(-1/2+l*b**2)*g_pq*g^ab*d_r^c+3*(-1+l)*n_p*n^a*d_q^b*d_r^c+6*(1/2+l*b)*(n_p*n_q*g^ab*d_r^c+n^a*n^b*g_pq*d_r^c)+6*(-1/4+l*b**2)*n_p*g_qr*n^a*g^bc",
            "KINV^{pqr}_{ijk} = -1/4*(1+b)**(-1)*(3*l-24*b**2-36*b-14+12*l*b**2+12*l*b)**(-1)*l**(-1)*(-14*l+32*b**3+80*b**2+64*b+16+24*l**2*b**3+36*l**2*b**2+18*l**2*b+3*l**2-32*l*b**3-72*l*b**2-56*l*b)*(g_{jk}*n_{i}*n^{p}*n^{q}*n^{r}+g_{ij}*n_{k}*n^{p}*n^{q}*n^{r}+g_{ik}*n_{j}*n^{p}*n^{q}*n^{r})-1/12*(g_{ij}*g^{pr}*d_{k}^{q}+g_{ik}*d_{j}^{q}*g^{pr}+g_{ij}*d_{k}^{p}*g^{qr}+d_{i}^{p}*g^{qr}*g_{jk}+g_{ik}*g^{pq}*d_{j}^{r}+d_{i}^{r}*g_{jk}*g^{pq}+g_{ik}*d_{j}^{p}*g^{qr}+d_{i}^{q}*g_{jk}*g^{pr}+g_{ij}*d_{k}^{r}*g^{pq})-1/6*l**(-1)*(-1+l)*(d_{i}^{q}*d_{k}^{p}*n_{j}*n^{r}+d_{j}^{p}*d_{k}^{r}*n_{i}*n^{q}+d_{i}^{q}*d_{j}^{p}*n_{k}*n^{r}+d_{i}^{p}*d_{k}^{q}*n_{j}*n^{r}+d_{k}^{r}*d_{j}^{q}*n_{i}*n^{p}+d_{k}^{p}*d_{j}^{q}*n_{i}*n^{r}+d_{i}^{p}*d_{j}^{q}*n_{k}*n^{r}+d_{j}^{p}*d_{k}^{q}*n_{i}*n^{r}+d_{k}^{p}*d_{j}^{r}*n_{i}*n^{q}+d_{i}^{q}*d_{j}^{r}*n_{k}*n^{p}+d_{i}^{p}*d_{j}^{r}*n_{k}*n^{q}+d_{k}^{q}*d_{j}^{r}*n_{i}*n^{p}+d_{i}^{r}*d_{k}^{p}*n_{j}*n^{q}+d_{i}^{r}*d_{k}^{q}*n_{j}*n^{p}+d_{i}^{r}*d_{j}^{p}*n_{k}*n^{q}+d_{i}^{q}*d_{k}^{r}*n_{j}*n^{p}+d_{i}^{p}*d_{k}^{r}*n_{j}*n^{q}+d_{i}^{r}*d_{j}^{q}*n_{k}*n^{p})-1/4*l**(-1)*(3*l-24*b**3-60*b**2-50*b-14+12*l*b**3+24*l*b**2+15*l*b)**(-1)*(-14*l+32*b**3+80*b**2+64*b+16+24*l**2*b**3+36*l**2*b**2+18*l**2*b+3*l**2-32*l*b**3-72*l*b**2-56*l*b)*(g^{pq}*n_{i}*n_{j}*n_{k}*n^{r}+g^{qr}*n_{i}*n_{j}*n_{k}*n^{p}+g^{pr}*n_{i}*n_{j}*n_{k}*n^{q})+1/6*(d_{i}^{q}*d_{k}^{p}*d_{j}^{r}+d_{i}^{p}*d_{k}^{q}*d_{j}^{r}+d_{i}^{r}*d_{k}^{p}*d_{j}^{q}+d_{i}^{r}*d_{j}^{p}*d_{k}^{q}+d_{i}^{q}*d_{j}^{p}*d_{k}^{r}+d_{i}^{p}*d_{k}^{r}*d_{j}^{q})+1/12*(1+b)**(-1)*(2*b+1)*(g_{ij}*d_{k}^{q}*n^{p}*n^{r}+g_{ik}*d_{j}^{p}*n^{q}*n^{r}+g_{ik}*d_{j}^{q}*n^{p}*n^{r}+g_{ik}*d_{j}^{r}*n^{p}*n^{q}+d_{i}^{r}*g_{jk}*n^{p}*n^{q}+g_{ij}*d_{k}^{r}*n^{p}*n^{q}+g_{ij}*d_{k}^{p}*n^{q}*n^{r}+d_{i}^{q}*g_{jk}*n^{p}*n^{r}+d_{i}^{p}*g_{jk}*n^{q}*n^{r})+1/12*(1+b)**(-1)*(2*b+1)*(g^{pq}*d_{j}^{r}*n_{i}*n_{k}+d_{k}^{p}*g^{qr}*n_{i}*n_{j}+d_{i}^{p}*g^{qr}*n_{j}*n_{k}+d_{i}^{r}*g^{pq}*n_{j}*n_{k}+d_{j}^{p}*g^{qr}*n_{i}*n_{k}+d_{i}^{q}*g^{pr}*n_{j}*n_{k}+d_{k}^{r}*g^{pq}*n_{i}*n_{j}+g^{pr}*d_{k}^{q}*n_{i}*n_{j}+d_{j}^{q}*g^{pr}*n_{i}*n_{k})-1/12*(1+b)**(-2)*l**(-1)*(-3*l+8*b**2+16*b+6+4*l*b**2-4*l*b)*(d_{i}^{r}*n_{j}*n_{k}*n^{p}*n^{q}+d_{k}^{r}*n_{i}*n_{j}*n^{p}*n^{q}+d_{k}^{p}*n_{i}*n_{j}*n^{q}*n^{r}+d_{i}^{q}*n_{j}*n_{k}*n^{p}*n^{r}+d_{i}^{p}*n_{j}*n_{k}*n^{q}*n^{r}+d_{k}^{q}*n_{i}*n_{j}*n^{p}*n^{r}+d_{j}^{p}*n_{i}*n_{k}*n^{q}*n^{r}+d_{j}^{q}*n_{i}*n_{k}*n^{p}*n^{r}+d_{j}^{r}*n_{i}*n_{k}*n^{p}*n^{q})+1/12*(3*l-24*b**2-36*b-14+12*l*b**2+12*l*b)**(-1)*l**(-1)*(-14*l+32*b**2+48*b+18+12*l**2*b**2+12*l**2*b+3*l**2-24*l*b**2-36*l*b)*(g^{qr}*g_{jk}*n_{i}*n^{p}+g_{ij}*g^{qr}*n_{k}*n^{p}+g_{ik}*g^{qr}*n_{j}*n^{p}+g_{jk}*g^{pr}*n_{i}*n^{q}+g_{ij}*g^{pr}*n_{k}*n^{q}+g_{ik}*g^{pr}*n_{j}*n^{q}+g_{jk}*g^{pq}*n_{i}*n^{r}+g_{ij}*g^{pq}*n_{k}*n^{r}+g_{ik}*g^{pq}*n_{j}*n^{r})+3/4*(2*b+1+b**2)**(-1)*(3*l-24*b**2-36*b-14+12*l*b**2+12*l*b)**(-1)*(12*l-64*b**4-224*b**3-256*b**2-120*b-l**2-20+32*l**2*b**2+80*l**2*b**4+96*l**2*b**3-16*l*b**3-64*l*b**4+80*l*b**2+60*l*b)*l**(-1)*n_{i}*n_{j}*n_{k}*n^{p}*n^{q}*n^{r}",
            "D^{j}_{k} = -m*(m**2-p_{\\mu }*p^{\\mu })**(-1)*d^{j}_{k}-(m**2-p_{\\mu }*p^{\\mu })**(-1)*G^{j}_{k}^{\\mu }*p_{\\mu }",
            "D^{j}_{k} = -I*m*(m**2-p_{\\mu }*p^{\\mu })**(-1)*d^{j}_{k}-I*(m**2-p_{\\mu }*p^{\\mu })**(-1)*G^{j}_{k}^{\\mu }*p_{\\mu }",
            "K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)",
            "7/60*Power[R, 2]+-4/15*R^{\\mu \\nu }*R_{\\mu \\nu }+1/2*P^{\\gamma }_{\\alpha }*P^{\\alpha }_{\\gamma }+1/6*P*R",
            "K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)",
            "-5/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+47/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+1789/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+929/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-19/120*gamma*Power[gamma+1, -1]*Power[R, 2]+167/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+-5/72*R*Power[gamma+1, -1]*P*Power[gamma, 4]+-337/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+7/60*Power[R, 2]+-1/24*R*Power[gamma+1, -1]*P*Power[gamma, 2]+1/12*gamma*R*P+-109/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-37/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+1453/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+-1409/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/72*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 6]+(6851/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+11/20*Power[gamma+1, -1]*Power[gamma, 5]+-39/80*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-199/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-107/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+1333/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-23/120*Power[gamma, 4]+-49/60*Power[gamma, 2]+-67/120*Power[gamma, 3]+1259/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-133/144*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+11/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+31/64*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-41/60*gamma+29/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+3869/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+23/30*gamma*Power[gamma+1, -1]+811/360*Power[gamma+1, -1]*Power[gamma, 3]+329/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-4/15+-6631/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+97/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+1277/720*Power[gamma+1, -1]*Power[gamma, 2]+-2489/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1319/720*Power[gamma+1, -1]*Power[gamma, 4]+-2627/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/120*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-3253/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-965/576*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-9/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+17/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 10]+-1511/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-341/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+737/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-11/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3])*R^{\\mu \\nu }*R_{\\mu \\nu }+1/48*Power[P, 2]*Power[gamma, 2]+(-5/36*Power[gamma+1, -1]*Power[gamma, 4]+-7/12*Power[gamma+1, -1]*Power[gamma, 2]+-37/72*Power[gamma+1, -1]*Power[gamma, 3]+1/6*Power[gamma, 2]+1/18*Power[gamma, 3]+1/6*gamma+1/6*gamma*Power[gamma+1, -1]+73/72*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/9*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+2/3*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+11/24*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6])*P^{\\mu \\alpha }*R_{\\mu \\alpha }+1/6*R*P+-1391/1440*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+319/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-203/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/18*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+29/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+49/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+-271/480*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+29/120*gamma*Power[R, 2]+1/12*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+19/288*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+-1/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+1/20*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+9/80*Power[R, 2]*Power[gamma, 4]+17/40*Power[R, 2]*Power[gamma, 2]+2761/11520*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/12*R*P*Power[gamma, 2]+-37/120*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/36*R*P*Power[gamma, 3]+-497/1152*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+4669/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+83/240*Power[R, 2]*Power[gamma, 3]+-43/40*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+53/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+-403/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+(1/24*Power[gamma, 2]+1/4*gamma+1/2)*P^{\\alpha }_{\\rho_5 }*P^{\\rho_5 }_{\\alpha }+1/480*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+-13/144*R*Power[gamma+1, -1]*P*Power[gamma, 3]+-19/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 10]",
            "(1/24*Power[gamma,2]+1/4*gamma+1/2)*P_\\mu\\nu*P^\\mu\\nu",
            "K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-ga/(2*(1+ga))*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)",
            "-5/144*R*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*P*Power[ga, 5]+47/180*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 3]+1789/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 7]+929/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 6]+-19/120*ga*Power[ga+1, -1]*Power[R, 2]+167/3840*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 4]+1/36*R*Power[ga+1, -1]*Power[ga+1, -1]*P*Power[ga, 3]+-5/72*R*Power[ga+1, -1]*P*Power[ga, 4]+-337/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 9]+7/60*Power[R, 2]+-1/24*R*Power[ga+1, -1]*P*Power[ga, 2]+1/12*ga*R*P+-109/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 6]+1439/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 8]+829/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 6]+-37/240*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 8]+1453/1920*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 5]+-1409/2880*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 7]+-1/72*R*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*P*Power[ga, 6]+(6851/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 4]+11/20*Power[ga+1, -1]*Power[ga, 5]+-39/80*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 6]+-199/1440*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 7]+-107/720*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 2]+1333/960*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 6]+-23/120*Power[ga, 4]+-49/60*Power[ga, 2]+-67/120*Power[ga, 3]+1259/2880*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 6]+-133/144*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 3]+11/40*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 8]+31/64*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 8]+-41/60*ga+29/320*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 6]+3869/1440*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 5]+23/30*ga*Power[ga+1, -1]+811/360*Power[ga+1, -1]*Power[ga, 3]+329/960*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 7]+-4/15+-6631/2880*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 5]+97/320*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 9]+1277/720*Power[ga+1, -1]*Power[ga, 2]+-2489/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 4]+1319/720*Power[ga+1, -1]*Power[ga, 4]+-2627/960*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 4]+1/120*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 7]+-3253/1920*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 5]+-965/576*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 6]+-9/40*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 9]+17/240*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 10]+-1511/2880*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 8]+-341/2880*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 7]+737/2880*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 5]+-11/180*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 3])*R^{\\mu \\nu }*R_{\\mu \\nu }+1/48*Power[P, 2]*Power[ga, 2]+(-5/36*Power[ga+1, -1]*Power[ga, 4]+-7/12*Power[ga+1, -1]*Power[ga, 2]+-37/72*Power[ga+1, -1]*Power[ga, 3]+1/6*Power[ga, 2]+1/18*Power[ga, 3]+1/6*ga+1/6*ga*Power[ga+1, -1]+73/72*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 3]+1/9*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 5]+2/3*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 2]+11/24*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 4]+1/36*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 3]+1/36*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 4]+-1/36*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 5]+-1/36*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga, 6])*P^{\\mu \\alpha }*R_{\\mu \\alpha }+1/6*R*P+-1391/1440*Power[ga+1, -1]*Power[R, 2]*Power[ga, 4]+319/1440*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 6]+-203/3840*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 5]+1/18*R*Power[ga+1, -1]*Power[ga+1, -1]*P*Power[ga, 5]+29/1920*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 5]+49/720*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 9]+-271/480*Power[ga+1, -1]*Power[R, 2]*Power[ga, 2]+29/120*ga*Power[R, 2]+1/12*R*Power[ga+1, -1]*Power[ga+1, -1]*P*Power[ga, 4]+19/288*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 3]+-1/144*R*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*P*Power[ga, 3]+1/20*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 5]+9/80*Power[R, 2]*Power[ga, 4]+17/40*Power[R, 2]*Power[ga, 2]+2761/11520*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 4]+1/12*R*P*Power[ga, 2]+-37/120*Power[ga+1, -1]*Power[R, 2]*Power[ga, 5]+1/36*R*P*Power[ga, 3]+-497/1152*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 6]+4669/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 4]+83/240*Power[R, 2]*Power[ga, 3]+-43/40*Power[ga+1, -1]*Power[R, 2]*Power[ga, 3]+53/720*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 7]+-1/36*R*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*P*Power[ga, 4]+-403/5760*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 7]+-37/384*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 8]+(1/24*Power[ga, 2]+1/4*ga+1/2)*P^{\\alpha }_{\\rho_5 }*P^{\\rho_5 }_{\\alpha }+1/480*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 2]+-13/144*R*Power[ga+1, -1]*P*Power[ga, 3]+-19/1440*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[ga+1, -1]*Power[R, 2]*Power[ga, 10]",
            "(1+ga)**(-6)*(13/4*ga+1/2*ga**7+1/24*ga**8+14*ga**3+21/8*ga**6+91/12*ga**5+105/8*ga**4+217/24*ga**2+1/2)",
            "K^{\\mu\\nu\\gamma\\delta}_\\alpha^{\\beta}=d_\\alpha^\\beta*1/3*(g^{\\mu\\nu}*g^{\\gamma\\delta}+ g^{\\mu\\gamma}*g^{\\nu\\delta}+ g^{\\mu\\delta}*g^{\\nu\\gamma})+1/12*(-2*\\lambda+Power[\\lambda,2])*(g^{\\mu\\nu}*d_\\alpha^\\gamma*g^{\\beta\\delta}+g^{\\mu\\nu}*d_\\alpha^\\delta*g^{\\beta\\gamma}+g^{\\mu\\gamma}*d_\\alpha^\\nu*g^{\\beta\\delta}+g^{\\mu\\gamma}*d_\\alpha^\\delta*g^{\\beta\\nu}+g^{\\mu\\delta}*d_\\alpha^\\nu*g^{\\beta\\gamma}+g^{\\mu\\delta}*d_\\alpha^\\gamma*g^{\\beta\\nu}+g^{\\nu\\gamma}*d_\\alpha^\\mu*g^{\\beta\\delta}+g^{\\nu\\gamma}*d_\\alpha^\\delta*g^{\\beta\\mu}+g^{\\nu\\delta}*d_\\alpha^\\mu*g^{\\beta\\gamma}+g^{\\nu\\delta}*d_\\alpha^\\gamma*g^{\\beta\\mu}+g^{\\gamma\\delta}*d_\\alpha^\\mu*g^{\\beta\\nu}+g^{\\gamma\\delta}*d_\\alpha^\\nu*g^{\\beta\\mu})",
            "W^{\\mu\\nu}_\\alpha^\\beta=2*P_{\\alpha}^{\\beta}*g^{\\mu\\nu}-2/3*R^\\mu\\nu*d_\\alpha^\\beta-\\lambda/2*P_\\alpha^\\mu*g^\\nu\\beta-\\lambda/2*P_\\alpha^\\nu*g^\\mu\\beta-\\lambda/2*P^\\beta\\mu*d^\\nu_\\alpha-\\lambda/2*P^\\beta\\nu*d^\\mu_\\alpha+1/6*(\\lambda-2*Power[\\lambda,2])*(R_\\alpha^\\mu*g^\\nu\\beta+R_\\alpha^\\nu*g^\\mu\\beta+R^\\beta\\mu*d^\\nu_\\alpha+R^\\beta\\nu*d^\\mu_\\alpha)+1/6*(2*\\lambda-Power[\\lambda,2])*(R_\\alpha^\\mu\\beta\\nu+R_\\alpha^\\nu\\beta\\mu)+1/2*(2*\\lambda-Power[\\lambda,2])*g^\\mu\\nu*R_\\alpha^\\beta",
            "M_\\alpha^\\beta = P_\\alpha\\mu*P^\\mu\\beta-1/2*R_\\mu\\nu\\gamma\\alpha*R^\\mu\\nu\\gamma\\beta+\\lambda/2*P_\\alpha\\mu*R^\\mu\\beta+\\lambda/2*P_\\mu\\nu*R^\\mu_\\alpha^\\nu\\beta+1/6*(\\lambda-2*Power[\\lambda,2])*R_\\alpha\\mu*R^\\mu\\beta+1/12*(4*\\lambda+7*Power[\\lambda,2])*R_\\mu\\alpha\\nu^\\beta*R^\\mu\\nu+1/4*(2*\\lambda-Power[\\lambda,2])*R_\\alpha\\mu\\nu\\gamma*R^\\gamma\\mu\\nu\\beta",
            "299/60*(gamma+1)**(-12)*gamma**12*R**2+1/12*(gamma+1)**(-12)*R*P*gamma**14+1/3*(gamma+1)**(-12)*R*P+14729/40*(gamma+1)**(-12)*gamma**6*R**2+2189/6*(gamma+1)**(-12)*R*P*gamma**5+7/30*(gamma+1)**(-12)*R**2+55/6*(gamma+1)**(-12)*gamma**5*P**2+55/6*(gamma+1)**(-12)*gamma**11*P**2+47/6*(gamma+1)**(-12)*R*P*gamma**12+(gamma+1)**(-12)*(2/3*gamma+1/6*gamma**14+8/3*gamma**13+19*gamma**12+46*gamma**3+49/6*gamma**2+242/3*gamma**11+462*gamma**9+473/3*gamma**4+682*gamma**8+748*gamma**7+1100/3*gamma**5+1221/2*gamma**6+1375/6*gamma**10)*P^{\\mu \\nu }*R_{\\mu \\nu }+7/6*(gamma+1)**(-12)*R*P*gamma**13+(gamma+1)**(-12)*(25/2*gamma+1+1/12*gamma**14+3/2*gamma**13+25/2*gamma**12+190/3*gamma**11+254*gamma**3+865/12*gamma**2+869/4*gamma**10+968*gamma**8+1067/2*gamma**9+1221/2*gamma**4+1320*gamma**7+5445/4*gamma**6+6347/6*gamma**5)*P^{\\rho_5 }_{\\alpha }*P^{\\alpha }_{\\rho_5 }+256/3*(gamma+1)**(-12)*R*P*gamma**3+1331/6*(gamma+1)**(-12)*R*P*gamma**9+1925/4*(gamma+1)**(-12)*R*P*gamma**6+1243/6*(gamma+1)**(-12)*R*P*gamma**4+374*(gamma+1)**(-12)*R*P*gamma**8+1/24*(gamma+1)**(-12)*gamma**14*R**2+2/3*(gamma+1)**(-12)*gamma**13*R**2+4147/15*(gamma+1)**(-12)*gamma**5*R**2+77/2*(gamma+1)**(-12)*gamma**8*P**2+89/30*(gamma+1)**(-12)*gamma*R**2+1001/6*(gamma+1)**(-12)*gamma**9*R**2+165/8*(gamma+1)**(-12)*gamma**6*P**2+165/8*(gamma+1)**(-12)*gamma**10*P**2+1/2*(gamma+1)**(-12)*gamma**3*P**2+1/2*(gamma+1)**(-12)*gamma**13*P**2+1/24*(gamma+1)**(-12)*gamma**2*P**2+1/24*(gamma+1)**(-12)*gamma**14*P**2+484*(gamma+1)**(-12)*R*P*gamma**7+(-187/30*gamma-8/15+1/12*gamma**14+7/6*gamma**13-55*gamma**8+187/6*gamma**9+209/30*gamma**12-316/3*gamma**3+344/15*gamma**11-1012/5*gamma**7-1331/6*gamma**4-1987/60*gamma**2+2563/60*gamma**10-6391/20*gamma**6-9647/30*gamma**5)*(gamma+1)**(-12)*R_{\\gamma \\sigma }*R^{\\gamma \\sigma }+11/4*(gamma+1)**(-12)*gamma**4*P**2+11/4*(gamma+1)**(-12)*gamma**12*P**2+8723/120*(gamma+1)**(-12)*gamma**10*R**2+2093/120*(gamma+1)**(-12)*gamma**2*R**2+689/30*(gamma+1)**(-12)*gamma**11*R**2+289/12*(gamma+1)**(-12)*R*P*gamma**2+1859/5*(gamma+1)**(-12)*gamma**7*R**2+1859/12*(gamma+1)**(-12)*gamma**4*R**2+25/6*(gamma+1)**(-12)*R*P*gamma+286*(gamma+1)**(-12)*gamma**8*R**2+33*(gamma+1)**(-12)*gamma**7*P**2+33*(gamma+1)**(-12)*gamma**9*P**2+377/6*(gamma+1)**(-12)*gamma**3*R**2+100/3*(gamma+1)**(-12)*R*P*gamma**11+1199/12*(gamma+1)**(-12)*R*P*gamma**10",
            "(1/12*Power[gamma,2]+1/2*gamma+1)*P_\\mu\\nu*P^\\mu\\nu",
            "K^{\\mu\\nu}_\\alpha^{\\beta}=d_\\alpha^\\beta*g^\\mu\\nu-1/2*beta*(d_\\alpha^\\mu*g^\\nu\\beta+d_\\alpha^\\nu*g^\\mu\\beta)",
            "K^{\\mu\\nu}_\\alpha^{\\beta}=d_\\alpha^\\beta*g^\\mu\\nu-1/2*beta*(d_\\alpha^\\mu*g^\\nu\\beta+d_\\alpha^\\nu*g^\\mu\\beta)",
            "17/60*Power[R, 2]+1789/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-203/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+-337/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+61/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-109/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-497/480*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+-497/1152*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-97/160*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+-19/120*Power[1+gamma, -1]*Power[R, 2]*gamma+2441/11520*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+(17/320*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-67/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+71/60*gamma+7/120*Power[gamma, 4]+161/120*Power[gamma, 2]+233/360*Power[gamma, 3]+-1*(29/20*gamma+1/4*Power[gamma, 4]+23/20*Power[gamma, 3]+39/20*Power[gamma, 2]+83/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-43/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+3/5*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+667/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-121/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+99/160*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-7/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+7/48*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-13/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+-181/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-33/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-29/20*Power[1+gamma, -1]*gamma+-7/10*Power[1+gamma, -1]*Power[gamma, 5]+103/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-117/40*Power[1+gamma, -1]*Power[gamma, 2]+8/5+-173/40*Power[1+gamma, -1]*Power[gamma, 3]+-37/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-341/120*Power[1+gamma, -1]*Power[gamma, 4]+293/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+281/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-29/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+11/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+37/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-13/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-14/15*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-139/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+317/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3])+481/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1009/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+899/288*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+13/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-1231/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-31/60*Power[1+gamma, -1]*gamma+-3/20*Power[1+gamma, -1]*Power[gamma, 5]+35/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-4661/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+1/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+11/6+127/90*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+3509/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+3919/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+1877/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1/24*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+49/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+-827/720*Power[1+gamma, -1]*Power[gamma, 4]+-931/360*Power[1+gamma, -1]*Power[gamma, 3]+-1559/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+59/144*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+1441/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+5/64*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-1249/720*Power[1+gamma, -1]*Power[gamma, 2]+-1/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+731/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5])*R^{\\gamma }_{\\mu }*R^{\\mu }_{\\gamma }+1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+13/40*Power[R, 2]*gamma+-839/720*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+9/80*Power[R, 2]*Power[gamma, 4]+127/240*Power[R, 2]*Power[gamma, 2]+29/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+269/720*Power[R, 2]*Power[gamma, 3]+49/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+-37/120*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+3/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-403/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+167/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+5149/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+53/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+283/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-19/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 10]+-37/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+-1409/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+11/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+4679/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+319/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]",
            "KINV_\\alpha\\beta^\\gamma\\delta = (d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2+la/2*(d_\\alpha^\\gamma*n_\\beta*n^\\delta+d_\\alpha^\\delta*n_\\beta*n^\\gamma+d_\\beta^\\gamma*n_\\alpha*n^\\delta+d_\\beta^\\delta*n_\\alpha*n^\\gamma)-la*g^\\gamma\\delta*n_\\alpha*n_\\beta",
            "K^\\mu\\nu_\\alpha\\beta^\\gamma\\delta = g^\\mu\\nu*(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2-la/(4*(1+la))*(d_\\alpha^\\gamma*d_\\beta^\\mu*g^\\delta\\nu+d_\\alpha^\\gamma*d_\\beta^\\nu*g^\\delta\\mu+d_\\alpha^\\delta*d_\\beta^\\mu*g^\\gamma\\nu+d_\\alpha^\\delta*d_\\beta^\\nu*g^\\gamma\\mu+d_\\beta^\\gamma*d_\\alpha^\\mu*g^\\delta\\nu+d_\\beta^\\gamma*d_\\alpha^\\nu*g^\\delta\\mu+d_\\beta^\\delta*d_\\alpha^\\mu*g^\\gamma\\nu+d_\\beta^\\delta*d_\\alpha^\\nu*g^\\gamma\\mu)+la/(2*(1+la))*g^\\gamma\\delta*(d_\\alpha^\\mu*d_\\beta^\\nu+d_\\alpha^\\nu*d_\\beta^\\mu)",
            "W_{\\alpha\\beta}^{\\gamma\\delta}=P_\\alpha\\beta^\\gamma\\delta-la/(2*(1+la))*(R_\\alpha^\\gamma_\\beta^\\delta+R_\\alpha^\\delta_\\beta^\\gamma)+la/(4*(1+la))*(d_\\alpha^\\gamma*R_\\beta^\\delta+d_\\alpha^\\delta*R_\\beta^\\gamma+d_\\beta^\\gamma*R_\\alpha^\\delta+d_\\beta^\\delta*R_\\alpha^\\gamma)",
            "P_\\gamma\\delta^\\mu\\nu = R_\\gamma^\\mu_\\delta^\\nu+R_\\gamma^\\nu_\\delta^\\mu+1/2*(d_\\gamma^\\mu*R_\\delta^\\nu+d_\\gamma^\\nu*R_\\delta^\\mu+d_\\delta^\\mu*R_\\gamma^\\nu+d_\\delta^\\nu*R_\\gamma^\\mu)-g^\\mu\\nu*R_\\gamma\\delta-R^\\mu\\nu*g_\\gamma\\delta+(-d_\\gamma^\\mu*d_\\delta^\\nu-d_\\gamma^\\nu*d_\\delta^\\mu+g^\\mu\\nu*g_\\gamma\\delta)*R/2",
            "F_\\mu\\nu^\\lambda\\delta_\\rho\\tau = R^\\lambda_\\rho\\mu\\nu*d^\\delta_\\tau+R^\\delta_\\tau\\mu\\nu*d^\\lambda_\\rho",
            "-43/960*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+31751/2880*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)-161/960*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3311/1920*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3833/5760*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-281/60*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-59/12*R**2*la**2*(1+la)**(-1)+34979/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)-7651/1440*R**2*la**4*(1+la)**(-1)+1627/2880*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+(7/45*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-107/30*la+1631/720*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-6841/160*la**4*(1+la)**(-1)*(1+la)**(-1)-4619/5760*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+101/96*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3211/360*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+1729/80*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-18517/960*la**5*(1+la)**(-1)*(1+la)**(-1)-3697/2880*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-179/720*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3109/5760*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+953/1440*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2533/720*la**6*(1+la)**(-1)*(1+la)**(-1)+79/30*la**5*(1+la)**(-1)-2551/2880*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+127/30*la*(1+la)**(-1)+7/6+10387/1152*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-25/48*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-5477/2880*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2825/72*la**3*(1+la)**(-1)*(1+la)**(-1)+881/36*la**2*(1+la)**(-1)-95/9*la**2*(1+la)**(-1)*(1+la)**(-1)+6197/180*la**3*(1+la)**(-1)-301/480*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-23/30*la**4-299/60*la**3-541/60*la**2+1067/1440*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4003/240*la**4*(1+la)**(-1)-803/1440*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+281/1440*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+155/8*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-571/240*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1))*R^{\\mu \\nu }*R_{\\mu \\nu }-3223/360*R**2*la**3*(1+la)**(-1)-667/360*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-1109/288*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-91/60*R**2*la**5*(1+la)**(-1)-1/30*R**2*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+9/20*R**2*la**4-7349/11520*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+157/60*R**2*la**2+181/120*R**2*la**3+103/320*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-13/10*R**2*la*(1+la)**(-1)+859/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+7/12*R**2-20419/11520*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3181/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-533/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-15/64*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+601/72*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)+13/10*R**2*la+25/96*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-4955/2304*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+919/480*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)-139/960*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+17/480*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4/3*R**2*la**2*(1+la)**(-1)*(1+la)**(-1)",
            "KINV_\\alpha\\beta^\\gamma\\delta = (d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2-la/2*(d_\\alpha^\\gamma*n_\\beta*n^\\delta+d_\\alpha^\\delta*n_\\beta*n^\\gamma+d_\\beta^\\gamma*n_\\alpha*n^\\delta+d_\\beta^\\delta*n_\\alpha*n^\\gamma)-ga*(g_\\alpha\\beta*n^\\gamma*n^\\delta+g^\\gamma\\delta*n_\\alpha*n_\\beta)-1/2*g_\\alpha\\beta*g^\\gamma\\delta+2*ga*(ga*la-2*ga+2*la)*n_\\alpha*n_\\beta*n^\\gamma*n^\\delta",
            "K^\\mu\\nu_\\alpha\\beta^\\gamma\\delta = g^\\mu\\nu*(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2-la/(4*(1+la))*(d_\\alpha^\\gamma*d_\\beta^\\mu*g^\\delta\\nu+d_\\alpha^\\gamma*d_\\beta^\\nu*g^\\delta\\mu+d_\\alpha^\\delta*d_\\beta^\\mu*g^\\gamma\\nu+d_\\alpha^\\delta*d_\\beta^\\nu*g^\\gamma\\mu+d_\\beta^\\gamma*d_\\alpha^\\mu*g^\\delta\\nu+d_\\beta^\\gamma*d_\\alpha^\\nu*g^\\delta\\mu+d_\\beta^\\delta*d_\\alpha^\\mu*g^\\gamma\\nu+d_\\beta^\\delta*d_\\alpha^\\nu*g^\\gamma\\mu)+(la-be)/(2*(1+la))*(g^\\gamma\\delta*(d_\\alpha^\\mu*d_\\beta^\\nu+d_\\alpha^\\nu*d_\\beta^\\mu)+g_\\alpha\\beta*(g^\\gamma\\mu*g^\\delta\\nu+g^\\gamma\\nu*g^\\delta\\mu))+g^\\mu\\nu*g_\\alpha\\beta*g^\\gamma\\delta*(-1+(1+be)**2/(2*(1+la)))",
            "W_{\\alpha\\beta}^{\\gamma\\delta}=P_\\alpha\\beta^\\gamma\\delta-la/(2*(1+la))*(R_\\alpha^\\gamma_\\beta^\\delta+R_\\alpha^\\delta_\\beta^\\gamma)+la/(4*(1+la))*(d_\\alpha^\\gamma*R_\\beta^\\delta+d_\\alpha^\\delta*R_\\beta^\\gamma+d_\\beta^\\gamma*R_\\alpha^\\delta+d_\\beta^\\delta*R_\\alpha^\\gamma)",
            "P_\\alpha\\beta^\\mu\\nu =1/4*(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\alpha^\\delta*d_\\beta^\\gamma-g_\\alpha\\beta*g^\\gamma\\delta)*(R_\\gamma^\\mu_\\delta^\\nu+R_\\gamma^\\nu_\\delta^\\mu-g^\\mu\\nu*R_\\gamma\\delta-g_\\gamma\\delta*R^\\mu\\nu+1/2*(d^\\mu_\\gamma*R^\\nu_\\delta+d^\\nu_\\gamma*R_\\delta^\\mu+d^\\mu_\\delta*R^\\nu_\\gamma+d^\\nu_\\delta*R^\\mu_\\gamma)-1/2*(d^\\mu_\\gamma*d^\\nu_\\delta+d^\\nu_\\gamma*d^\\mu_\\delta)*(R-2*LA)+1/2*g_\\gamma\\delta*g^\\mu\\nu*R)",
            "F_\\mu\\nu^\\lambda\\delta_\\rho\\tau = R^\\lambda_\\rho\\mu\\nu*d^\\delta_\\tau+R^\\delta_\\tau\\mu\\nu*d^\\lambda_\\rho",
            "-43/960*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+31751/2880*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)-161/960*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3311/1920*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3833/5760*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-281/60*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-59/12*R**2*la**2*(1+la)**(-1)+34979/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)-7651/1440*R**2*la**4*(1+la)**(-1)+1627/2880*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+(7/45*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-107/30*la+1631/720*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-6841/160*la**4*(1+la)**(-1)*(1+la)**(-1)-4619/5760*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+101/96*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3211/360*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+1729/80*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-18517/960*la**5*(1+la)**(-1)*(1+la)**(-1)-3697/2880*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-179/720*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3109/5760*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+953/1440*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2533/720*la**6*(1+la)**(-1)*(1+la)**(-1)+79/30*la**5*(1+la)**(-1)-2551/2880*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+127/30*la*(1+la)**(-1)+7/6+10387/1152*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-25/48*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-5477/2880*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2825/72*la**3*(1+la)**(-1)*(1+la)**(-1)+881/36*la**2*(1+la)**(-1)-95/9*la**2*(1+la)**(-1)*(1+la)**(-1)+6197/180*la**3*(1+la)**(-1)-301/480*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-23/30*la**4-299/60*la**3-541/60*la**2+1067/1440*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4003/240*la**4*(1+la)**(-1)-803/1440*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+281/1440*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+155/8*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-571/240*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1))*R^{\\mu \\nu }*R_{\\mu \\nu }-3223/360*R**2*la**3*(1+la)**(-1)-667/360*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-1109/288*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-91/60*R**2*la**5*(1+la)**(-1)-1/30*R**2*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+9/20*R**2*la**4-7349/11520*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+157/60*R**2*la**2+181/120*R**2*la**3+103/320*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-13/10*R**2*la*(1+la)**(-1)+859/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+7/12*R**2-20419/11520*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3181/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-533/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-15/64*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+601/72*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)+13/10*R**2*la+25/96*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-4955/2304*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+919/480*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)-139/960*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+17/480*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4/3*R**2*la**2*(1+la)**(-1)*(1+la)**(-1)",
            "c1 = la**2*(2*ga**4+8*ga**3+12*ga**2+8*ga+8)+la*(-8*ga**4-16*ga**3-4*ga**2+8*ga+8)+(8*ga**4-8*ga**2+14)",
            "c3 = la**2*(-12*ga**4-48*ga**3-72*ga**2-48*ga-48)+la*(48*ga**4+72*ga**3-8*ga**2-40*ga-56)+(-48*ga**4+48*ga**3+40*ga**2+8*ga-104)",
            "c4 = la**2*(23*ga**4+96*ga**3+144*ga**2+96*ga+96)+la*(-96*ga**4-96*ga**3+144*ga**2+192*ga+192)+(96*ga**4-192*ga**3-144*ga**2+96*ga+240)",
            "1/30*Power[R, 2]+1/12*F_{\\nu \\beta }^{\\epsilon }_{\\rho_5 }*F^{\\nu \\beta \\rho_5 }_{\\epsilon }+1/15*R_{\\delta \\nu }*R^{\\delta \\nu }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/6*R*W^{\\beta }_{\\beta }",
            "1/12*F_{\\nu \\beta }^{\\epsilon }_{\\rho_5 }*F^{\\nu \\beta \\rho_5 }_{\\epsilon }+1/2*W^{\\rho_5 }_{\\alpha }*W^{\\alpha }_{\\rho_5 }+-1/45*Power[R, 2]+1/15*R^{\\mu \\nu }*R_{\\mu \\nu }",
            "K^{\\mu\\nu\\gamma\\delta}_\\alpha^{\\beta}=d_\\alpha^\\beta*1/3*(g^{\\mu\\nu}*g^{\\gamma\\delta}+ g^{\\mu\\gamma}*g^{\\nu\\delta}+ g^{\\mu\\delta}*g^{\\nu\\gamma})",
            "R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\alpha\\nu\\beta}=(1/2)*R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\nu\\alpha\\beta}",
            "Kn^\\alpha\\beta\\gamma=1/3*(n^\\alpha*g^\\beta\\gamma+n^\\beta*g^\\alpha\\gamma+n^\\gamma*g^\\alpha\\beta)",
            "DELTA^{\\mu\\nu\\alpha}=-(1/6)*L*(L-1)*(L-2)*Kn^{\\mu\\nu\\alpha}+Power[L,2]*(L-1)*(1/3)*(Kn^{\\mu \\nu }*Kn^{\\alpha }+Kn^{\\alpha \\nu }*Kn^{\\mu }+Kn^{\\mu \\alpha }*Kn^{\\nu })-Power[L,3]*Kn^{\\mu }*Kn^{\\nu }*Kn^{\\alpha }",
            "KINV^{\\alpha\\beta}_{\\mu\\nu} = P^{\\alpha\\beta}_{\\mu\\nu}-1/4*c*g_{\\mu\\nu}*g^{\\alpha\\beta}+(1/4)*b*(n_{\\mu}*n^{\\alpha}*d^{\\beta}_{\\nu}+n_{\\mu}*n^{\\beta}*d^{\\alpha}_{\\nu}+n_{\\nu}*n^{\\alpha}*d^{\\beta}_{\\mu}+n_{\\nu}*n^{\\beta}*d^{\\alpha}_{\\mu})+c*(n_{\\mu}*n_{\\nu}*g^{\\alpha\\beta}+n^{\\alpha}*n^{\\beta}*g_{\\mu\\nu})-c*b*n_{\\mu}*n_{\\nu}*n^{\\alpha}*n^{\\beta}",
            "K^{\\mu\\nu}^{\\alpha\\beta}_{\\gamma\\delta} = g^{\\mu\\nu}*P^{\\alpha\\beta}_{\\gamma\\delta}+(1+2*beta)*((1/4)*(d^{\\mu}_{\\gamma}*g^{\\alpha \\nu}*d^{\\beta}_{\\delta} + d^{\\mu}_{\\delta}*g^{\\alpha \\nu}*d^{\\beta}_{\\gamma}+d^{\\mu}_{\\gamma}*g^{\\beta \\nu}*d^{\\alpha}_{\\delta}+ d^{\\mu}_{\\delta}*g^{\\beta \\nu}*d^{\\alpha}_{\\gamma})+(1/4)*(d^{\\nu}_{\\gamma}*g^{\\alpha \\mu}*d^{\\beta}_{\\delta} + d^{\\nu}_{\\delta}*g^{\\alpha \\mu}*d^{\\beta}_{\\gamma}+d^{\\nu}_{\\gamma}*g^{\\beta \\mu}*d^{\\alpha}_{\\delta}+ d^{\\nu}_{\\delta}*g^{\\beta \\mu}*d^{\\alpha}_{\\gamma}) -(1/4)*(g_{\\gamma\\delta}*g^{\\mu \\alpha}*g^{\\nu \\beta}+g_{\\gamma\\delta}*g^{\\mu \\beta}*g^{\\nu \\alpha})-(1/4)*(g^{\\alpha\\beta}*d^{\\mu}_{\\gamma}*d^{\\nu}_{\\delta}+g^{\\alpha\\beta}*d^{\\mu}_{\\delta}*d^{\\nu}_{\\gamma})+(1/8)*g^{\\mu\\nu}*g_{\\gamma\\delta}*g^{\\alpha\\beta})",
            "P^{\\alpha\\beta}_{\\mu\\nu} = (1/2)*(d^{\\alpha}_{\\mu}*d^{\\beta}_{\\nu}+d^{\\alpha}_{\\nu}*d^{\\beta}_{\\mu})-(1/4)*g_{\\mu\\nu}*g^{\\alpha\\beta}",
            "55585/96*beta**3*b**3*LAMBDA**2-169/1440*beta**3*b**6*LAMBDA**2+1553833/2880*beta**2*b**3*LAMBDA**2-185763/640*beta**2*c**3*b**4*LAMBDA**2+154689/2560*beta**2*c**4*b**4*LAMBDA**2-17/180*beta*c*b**6*LAMBDA**2+2297/128*beta**4*c**4*b**5*LAMBDA**2-371/45*beta*c*b**5*LAMBDA**2+1441129/5760*beta*c**2*b**4*LAMBDA**2-20099/128*beta*c**3*b**4*LAMBDA**2-659/80*beta**4*c**3*b**6*LAMBDA**2+1837/30*beta**6*c**2*b**4*LAMBDA**2+60169/240*beta**5*c**2*b**4*LAMBDA**2+97939/640*beta**3*b**4*LAMBDA**2+709/36*beta**6*b**4*LAMBDA**2+220697/360*beta*b*LAMBDA**2-6973/120*c*beta**3*b**5*LAMBDA**2+40247/90*beta**2*LAMBDA**2+65/256*beta*c**4*b**6*LAMBDA**2-47/768*c**3*b**6*LAMBDA**2+103/72*beta**6*c**2*b**6*LAMBDA**2-221813/180*c*beta**3*b**2*LAMBDA**2-2055559/5760*c*beta**2*b**4*LAMBDA**2+9487/45*beta**4*LAMBDA**2-22117/72*beta*c*b*LAMBDA**2-10841/90*c*beta**6*b**3*LAMBDA**2+24097/92160*b**5*LAMBDA**2-239/11520*beta*b**6*LAMBDA**2+35123/720*LAMBDA**2-23071/640*beta**2*c**3*b**5*LAMBDA**2-45563/720*c*b*LAMBDA**2+2909/5760*beta*c**2*b**6*LAMBDA**2-1627/30*c*beta**6*b**4*LAMBDA**2+28622/45*beta**4*b*LAMBDA**2-1829/40*beta**5*c**3*b**5*LAMBDA**2-227/180*c*beta**3*b**6*LAMBDA**2+170959/480*beta*c**2*b**2*LAMBDA**2+10313/80*b*LAMBDA**2+178177/320*beta**3*c**2*b**4*LAMBDA**2-229/2880*beta**4*b**6*LAMBDA**2-1688857/46080*c*b**4*LAMBDA**2+2999503/2880*beta**2*c**2*b**3*LAMBDA**2-10987/11520*c*b**5*LAMBDA**2-2271/20*beta**5*c**3*b**4*LAMBDA**2-71477/384*beta*c**3*b**3*LAMBDA**2+69137/46080*c**2*b**5*LAMBDA**2-22625/128*beta*c*b**4*LAMBDA**2+95291/1440*beta**5*c**2*b**5*LAMBDA**2-2687/960*beta**2*c**3*b**6*LAMBDA**2+277117/360*beta**4*b**2*LAMBDA**2+2144/45*beta**6*LAMBDA**2+9/4*beta**6*c**4*b**4*LAMBDA**2+9/4*beta**6*c**4*b**5*LAMBDA**2-829/11520*beta**2*b**6*LAMBDA**2+35987/1280*beta**4*c**4*b**4*LAMBDA**2+56911/960*beta*b**4*LAMBDA**2-193/30*beta**3*c**3*b**6*LAMBDA**2-107/46080*b**6*LAMBDA**2-88805/576*c*b**2*LAMBDA**2-43871/90*c*beta**3*b*LAMBDA**2-6233/80*beta**4*c**3*b**5*LAMBDA**2-103/160*beta*c**3*b**6*LAMBDA**2+105127/960*beta**4*c**2*b**5*LAMBDA**2-83/180*c*beta**6*b**6*LAMBDA**2-2504/15*c*beta**5*b*LAMBDA**2-104795/3072*c**3*b**4*LAMBDA**2-1077641/2880*c*beta**4*b**4*LAMBDA**2+33/16*beta**5*c**4*b**6*LAMBDA**2-199/10*beta**6*c**3*b**3*LAMBDA**2+6049/360*beta**6*c**2*b**5*LAMBDA**2+48407/640*c**2*b**2*LAMBDA**2+9/16*beta**6*c**4*b**6*LAMBDA**2-186*beta**4*c**3*b**3*LAMBDA**2+1627247/30720*c**2*b**4*LAMBDA**2+2014/45*beta**6*c**2*b**2*LAMBDA**2-447/40*beta**6*c**3*b**5*LAMBDA**2-23369/360*c*beta**4*b**5*LAMBDA**2+2567/1024*beta*c**4*b**5*LAMBDA**2+10941/640*beta**3*c**4*b**5*LAMBDA**2+751939/5760*b**2*LAMBDA**2-13/9*c*beta**5*b**6*LAMBDA**2+4597/128*beta*c**4*b**4*LAMBDA**2+102257/7680*beta*c**2*b**5*LAMBDA**2-1339/720*c*beta**4*b**6*LAMBDA**2-3557/90*c*beta**5*b**5*LAMBDA**2+1781/360*beta**5*c**2*b**6*LAMBDA**2+240619/3840*b**3*LAMBDA**2-20714/45*c*beta**5*b**2*LAMBDA**2-21166/45*c*beta**5*b**3*LAMBDA**2-145183/480*beta**3*c**3*b**4*LAMBDA**2+16381/1152*beta**3*b**5*LAMBDA**2-1369/2880*c*beta**2*b**6*LAMBDA**2+1956/5*beta**5*b*LAMBDA**2-1249/128*beta*c**3*b**5*LAMBDA**2+88553/5760*beta**4*b**5*LAMBDA**2+8017/90*beta**6*c**2*b**3*LAMBDA**2-261/10*beta**6*c**3*b**4*LAMBDA**2-5476/45*c*beta**6*b**2*LAMBDA**2+28987/30*beta**3*b*LAMBDA**2-143707/480*beta**3*c**3*b**3*LAMBDA**2+3847/720*beta**3*c**2*b**6*LAMBDA**2-621281/1440*c*beta**3*b**4*LAMBDA**2+5726/45*beta**6*b**2*LAMBDA**2-167/30*beta**5*c**3*b**6*LAMBDA**2+3986/15*beta**5*b**3*LAMBDA**2+1851/160*beta**5*c**4*b**4*LAMBDA**2+129737/360*beta**4*c**2*b**2*LAMBDA**2+100487/46080*beta*b**5*LAMBDA**2-20919/64*beta**2*c**3*b**3*LAMBDA**2-13442/45*c*beta**4*b*LAMBDA**2-1467893/11520*c*b**3*LAMBDA**2-31/20*beta**6*c**3*b**6*LAMBDA**2+6599/128*beta**3*c**4*b**4*LAMBDA**2-71069/320*beta**4*c**3*b**4*LAMBDA**2+16547/45*beta**5*c**2*b**3*LAMBDA**2-232/5*c*beta**6*b*LAMBDA**2-5659/80*beta**3*c**3*b**5*LAMBDA**2+79/32*beta**3*c**4*b**6*LAMBDA**2-1331/15*beta**5*c**3*b**3*LAMBDA**2+112063/1440*beta**5*b**4*LAMBDA**2-149279/180*c*beta**4*b**2*LAMBDA**2+33335/4096*c**4*b**4*LAMBDA**2-35/32*c**3*b**5*LAMBDA**2+151133/12288*b**4*LAMBDA**2+279/256*beta**2*c**4*b**6*LAMBDA**2-3435461/5760*beta*c*b**3*LAMBDA**2-1621111/1440*c*beta**3*b**3*LAMBDA**2+96849/160*beta*b**2*LAMBDA**2+1/720*beta**5*b**6*LAMBDA**2+938203/7680*beta**2*b**4*LAMBDA**2+6008/45*beta**5*LAMBDA**2+1434631/11520*c**2*b**3*LAMBDA**2-89/11520*c*b**6*LAMBDA**2-236093/180*c*beta**2*b**2*LAMBDA**2+3323219/5760*beta*c**2*b**3*LAMBDA**2+379547/360*beta**3*b**2*LAMBDA**2-737/72*c*beta**6*b**5*LAMBDA**2+3171/320*beta**5*c**4*b**5*LAMBDA**2+1079/23040*c**2*b**6*LAMBDA**2+126683/180*beta**4*c**2*b**3*LAMBDA**2+1816/15*beta**6*b*LAMBDA**2+280889/2880*beta**3*c**2*b**5*LAMBDA**2-171649/5760*c*beta**2*b**5*LAMBDA**2+10177/1440*beta**4*c**2*b**6*LAMBDA**2+29107/3840*beta**2*b**5*LAMBDA**2-525161/720*beta*c*b**2*LAMBDA**2+16556/45*beta**3*LAMBDA**2+20506/45*beta**5*b**2*LAMBDA**2-633377/576*c*beta**2*b**3*LAMBDA**2+70903/1440*beta**2*c**2*b**5*LAMBDA**2+199/64*beta**4*c**4*b**6*LAMBDA**2+6247/90*beta**6*b**3*LAMBDA**2+313015/2304*beta**4*b**4*LAMBDA**2+488123/480*beta**3*c**2*b**3*LAMBDA**2+13/720*beta**6*b**6*LAMBDA**2-75797/90*c*beta**4*b**3*LAMBDA**2+64913/60*beta**2*b*LAMBDA**2+69409/120*beta**3*c**2*b**2*LAMBDA**2+25/1024*c**4*b**6*LAMBDA**2+22489/90*beta*LAMBDA**2-2201/4*c*beta**2*b*LAMBDA**2+8797/960*beta**5*b**5*LAMBDA**2+1667909/5760*beta*b**3*LAMBDA**2+12997/5760*beta**2*c**2*b**6*LAMBDA**2+283/120*beta**6*b**5*LAMBDA**2+11559/1280*beta**2*c**4*b**5*LAMBDA**2+8228/45*beta**5*c**2*b**2*LAMBDA**2+228367/360*beta**2*c**2*b**2*LAMBDA**2-10415/256*c**3*b**3*LAMBDA**2+1870429/3840*beta**2*c**2*b**4*LAMBDA**2+585/2048*c**4*b**5*LAMBDA**2+390509/360*beta**2*b**2*LAMBDA**2-6427/30*c*beta**5*b**4*LAMBDA**2+16621/36*beta**4*b**3*LAMBDA**2+2624573/5760*beta**4*c**2*b**4*LAMBDA**2",
            "F^\\alpha\\beta*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}+V^\\beta\\alpha*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}",
            "1/24*V^{\\gamma }_{\\nu }+1/24*V_{\\nu }^{\\gamma }+1/24*V_{\\alpha }^{\\alpha }*d^{\\gamma }_{\\nu }+1/24*d^{\\gamma }_{\\nu }*F_{\\beta }^{\\beta }+1/24*F_{\\nu }^{\\gamma }+1/24*F^{\\gamma }_{\\nu }",
            "FF=(-1/6)*F^{\\nu \\beta \\epsilon }_{\\zeta }*F_{\\nu \\beta }^{\\zeta }_{\\epsilon }+n^{\\mu }*F^{\\alpha }_{\\nu }^{\\epsilon }_{\\lambda }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }+(-8/3)*n^{\\mu }*F_{\\beta \\nu }^{\\epsilon }_{\\lambda }*n^{\\alpha }*n^{\\beta }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }",
            "1/24*(d^{\\alpha }_{\\nu }*d^{\\beta }_{\\mu }+d^{\\alpha }_{\\mu }*d^{\\beta }_{\\nu }+g^{\\alpha \\beta }*g_{\\mu \\nu })",
    };


    @Test
    public void testTestStrings2() {
        Tensor tensor;
        for (String str : testStrings3) {
            tensor = Tensors.parse(str);
            checkTensor(tensor);
        }
    }

    private static int simpleTensorCount(Tensor t) {
        Counter counter = new Counter();
        simpleTensorCount(t, counter);
        return counter.counter;
    }

    private static void simpleTensorCount(Tensor t, Counter counter) {
        for (Tensor c : t) {
            if (c.getClass() == SimpleTensor.class)
                counter.increase();
            else
                simpleTensorCount(c, counter);
        }
    }

    @Test
    public void test12() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("(A^ab - A^ba)*K_ajp*K^jcpm = F^bcm");
            TAssert.assertEquals(t, Tensors.parse(t.toString()));
        }
    }
}
