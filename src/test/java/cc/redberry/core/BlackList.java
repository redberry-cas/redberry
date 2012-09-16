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
package cc.redberry.core;

import cc.redberry.core.tensor.*;
import java.io.*;
import java.util.regex.*;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BlackList {

    @Test
    public void test123() {
        String orig = "L**2/10 *(R(s,al,be,gm)*R(ro,mu,nu,de) *n(s)*n(ro))*hk(de,j1,j2)*d(mu,nu,al,be,j2,j3)*hk(gm,j3,j1) + L**2*(L-1)**2*(L-2)*n(s)*n(ro) *(2/45*R(ro,al,de,nu)*R(s,be,mu,gm)-1/120*R(ro,de,al,nu)*R(s,be,mu,gm)) *hk(be,gm,de,j1,j2)*d(al,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)*n(ro)*n(s) *(-1/10*R(s,mu,gm,nu)*R(ro,al,de,be)+1/15*R(s,de,al,nu)*R(ro,be,mu,gm) +1/60*R(s,be,de,nu)*R(ro,gm,mu,al)) *hk(de,j1,j2)*d(al,be,gm,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)**2*n(s)*n(ro) *(-1/20*R(ro,mu,be,nu)*R(s,de,al,gm)+1/180*R(ro,al,nu,be)*R(s,gm,de,mu) -7/360*R(ro,mu,gm,nu)*R(s,al,de,be)-1/240*R(ro,de,be,nu)*R(s,gm,al,mu) -1/120*R(ro,be,gm,nu)*R(s,al,de,mu)-1/30*R(ro,de,be,nu)*R(s,al,gm,mu)) *hk(gm,de,j1,j2)*d(al,be,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)*(L-2)*n(s)*n(ro) *(-1/30*R(s,gm,nu,be)*R(ro,al,de,mu)-1/180*R(s,mu,gm,nu)*R(ro,al,be,de) +1/180*R(s,mu,gm,de)*R(ro,al,be,nu)) *hk(de,j1,j2)*d(mu,nu,j2,j3)*hk(al,be,gm,j3,j1) + L**2*(L-1)**2*(L-2)*n(s)*n(ro) *(1/45*R(ro,mu,gm,nu)*R(s,al,be,de)-1/80*R(ro,be,nu,gm)*R(s,mu,al,de) +1/90*R(ro,be,nu,gm)*R(s,de,al,mu)) *hk(mu,nu,j1,j2)*d(de,j2,j3)*hk(al,be,gm,j3,j1) + L**2*(L-1)*n(s)*n(ro) *(7/120*R(ro,be,gm,nu)*R(s,mu,al,de)-3/40*R(ro,be,gm,de)*R(s,mu,al,nu) +1/120*R(ro,de,gm,nu)*R(s,al,be,mu)) *hk(mu,nu,j1,j2)*d(al,be,gm,j2,j3)*hk(de,j3,j1) + L**2*(L-1)*(L-2)*n(s)*n(ro) *(-1/24*R(ro,mu,gm,nu)*R(s,al,be,de)-1/180*R(ro,nu,gm,de)*R(s,al,be,mu) -1/360*R(ro,de,gm,nu)*R(s,al,be,mu)) *hk(al,be,gm,j1,j2)*d(mu,nu,j2,j3)*hk(de,j3,j1) - L**2*(L-1)*(L-2)*(L-3)*(n(s)*n(ro) *R(s,al,be,gm)*R(ro,mu,nu,de)) *hk(de,j1,j2)*d(gm,j2,j3)*hk(mu,nu,al,be,j3,j1) /120 - L**2*(L-1)**2*(L-2)*(L-3)*(n(s)*n(ro) *R(ro,gm,be,mu)*R(s,al,de,nu)) *hk(al,be,gm,de,j1,j2)*hk(mu,nu,j2,j1) /80 + L**2*n(ro) *(-1/8*R(be,gm)*R(ro,nu,al,mu)+3/20*R(be,gm)*R(ro,mu,al,nu) +3/40*R(al,mu)*R(ro,be,gm,nu)+1/40*R(s,be,gm,mu)*R(ro,nu,al,s) -3/20*R(s,al,be,mu)*R(ro,gm,nu,s)+1/10*R(s,al,be,nu)*R(ro,gm,mu,s)) *hk(mu,j1,j2)*d(al,be,gm,j2,j3)*hk(nu,j3,j1) + L**2*(L-1)*n(ro) *(1/20*R(al,nu)*R(ro,gm,be,mu) +1/20*R(al,gm)*R(ro,mu,be,nu)+1/10*R(al,be)*R(ro,mu,gm,nu) +1/20*R(s,al,nu,gm)*R(ro,s,be,mu)-1/60*R(s,mu,al,nu)*R(ro,be,s,gm) +1/10*R(s,al,be,gm)*R(ro,mu,s,nu)-1/12*R(s,al,be,nu)*R(ro,mu,s,gm)) *hk(gm,j1,j2)*d(al,be,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)**2*n(ro) *(1/60*R(al,mu)*R(ro,be,nu,gm)-1/20*R(al,mu)*R(ro,gm,nu,be) +1/120*R(al,be)*R(ro,mu,nu,gm)+3/40*R(al,gm)*R(ro,nu,be,mu) +1/20*R(s,gm,mu,al)*R(ro,nu,s,be)+1/120*R(s,al,mu,gm)*R(ro,be,nu,s) -1/40*R(s,al,mu,gm)*R(ro,s,nu,be)+1/40*R(s,al,mu,be)*R(ro,s,nu,gm) -1/20*R(s,al,mu,be)*R(ro,gm,nu,s)-1/40*R(s,mu,be,nu)*R(ro,gm,s,al)) *hk(al,be,j1,j2)*d(gm,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)*n(ro) *(1/20*R(s,mu,nu,be)*R(ro,gm,s,al)-7/60*R(s,be,mu,al)*R(ro,gm,nu,s) +1/20*R(s,be,mu,al)*R(ro,s,nu,gm)+1/10*R(s,mu,be,gm)*R(ro,nu,al,s) +1/60*R(s,be,mu,gm)*R(ro,al,nu,s)+7/120*R(al,be)*R(ro,nu,gm,mu) +11/60*R(be,mu)*R(ro,nu,al,gm)) *hk(al,be,j1,j2)*d(mu,nu,j2,j3)*hk(gm,j3,j1) + L**2*(L-1)*(L-2)*n(ro) *(7/240*R(al,be)*R(ro,gm,mu,nu)+7/240*R(al,nu)*R(ro,be,gm,mu) -1/60*R(al,mu)*R(ro,be,gm,nu)-1/24*R(s,al,be,nu)*R(ro,s,gm,mu) +1/15*R(s,al,be,nu)*R(ro,mu,gm,s)+1/40*R(s,al,be,mu)*R(ro,s,gm,nu) +1/40*R(be,gm)*R(ro,nu,mu,al)+1/48*R(s,be,gm,mu)*R(ro,nu,al,s)) *hk(al,be,gm,j1,j2)*d(mu,j2,j3)*hk(nu,j3,j1) + L**2*(L-1)**2*(L-2) *n(ro)*(-7/240*R(ro,be,gm,nu)*R(mu,al)+1/240*R(ro,mu,al,nu)*R(be,gm) -1/40*R(ro,nu,gm,s)*R(s,al,mu,be)) *hk(mu,nu,j1,j2)*hk(al,be,gm,j2,j1) + L*(L-1)*(L-2)*(L-3) *(1/180*R(mu,nu)*R(al,be)+7/720*R(s,al,be,ro)*R(ro,mu,nu,s)) *hk(mu,nu,al,be,j1,j1)";
        Pattern pattern = Pattern.compile("(R|n|hk|d)\\(([a-zA-Z0-9,]*)\\)");
        Matcher matcher = pattern.matcher(orig);
        StringBuffer sb = new StringBuffer();
        String group, tensorName, indices;

        while (matcher.find()) {
            group = matcher.group();
            tensorName = matcher.group(1);
            indices = matcher.group(2);
            System.out.println(indices);
            if (tensorName.equals("R")) {
                String[] indicesArray = indices.split(",");
                assert indicesArray.length == 2 || indicesArray.length == 4;
                if (indicesArray.length == 4) {
                    indices = "^{" + indicesArray[0] + "}_{";
                    for (int i = 1; i < 4; ++i)
                        indices += indicesArray[i] + " ";
                    indices += "}";
                } else
                    indices = "_{" + indices.replace(',', ' ') + "}";
            } else if (tensorName.equals("n"))
                indices = "_{" + indices.replace(',', ' ') + "}";
            else
                indices = "^{" + indices.replace(',', ' ') + "}";
            group = tensorName + indices;
            group = group.replace("al", "\\\\alpha");
            group = group.replace("be", "\\\\beta");
            group = group.replace("gm", "\\\\gamma");
            group = group.replace("de", "\\\\delta");
            group = group.replace("s", "\\\\sigma");
            group = group.replace("ro", "\\\\rho");
            group = group.replace("mu", "\\\\mu");
            group = group.replace("nu", "\\\\nu");
            group = group.replace("j1", "");
            group = group.replace("j2", "");
            group = group.replace("j3", "");
            group = group.replace("j4", "");

            matcher.appendReplacement(sb, group);
        }
        matcher.appendTail(sb);
        String result = sb.toString();
        Tensors.parse(result);
        System.out.println(result);
    }

    @Test
    public void wqe() throws IOException {
        boolean x = false, y = false, z = false;
        if (x && !y || z == x)
            System.out.println("x");
    }
}
