/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.experimentalsa.base.r;

import tck.demetra.data.Data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class LocalPolynomialFiltersTest {

    public LocalPolynomialFiltersTest() {
    }

    @Test
    public void testHenderson() {
//        System.out.println(DoubleSeq.of(Data.NILE));
        double[] rslt = LocalPolynomialFilters.filter(Data.NILE, 9, 3, "Henderson", "LC", .5, 0, 0);
        assertTrue(rslt != null);
//       System.out.println(DoubleSeq.of(rslt));
        rslt = LocalPolynomialFilters.filter(Data.NILE, 9, 3, "Henderson", "CC", .5, 10, Math.PI / 8);
        assertTrue(rslt != null);
//        System.out.println(DoubleSeq.of(rslt));
        rslt = LocalPolynomialFilters.filter(Data.NILE, 9, 3, "Henderson", "DAF", .5, 0,0);
        assertTrue(rslt != null);
//        System.out.println(DoubleSeq.of(rslt));
    }

}
