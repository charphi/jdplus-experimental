/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.businesscycle;

import demetra.data.DoubleSeq;
import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRoots;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.UcarimaModel;
import jdplus.ssf.arima.SsfUcarima;

/**
 *
 * @author Jean Palate
 */
public class HodrickPrescottFilter {

    private final double lambda;
    private final UcarimaModel ucm;
    private final CompositeSsf ssf;

    public HodrickPrescottFilter(double snRatio) {
        this.lambda = snRatio;
        Polynomial D = UnitRoots.D(1), D2 = D.times(D);
        ArimaModel i2 = new ArimaModel(BackFilter.ONE, new BackFilter(D2), BackFilter.ONE, 1);
        ArimaModel wn = new ArimaModel(BackFilter.ONE, BackFilter.ONE, BackFilter.ONE, lambda);
        ucm = UcarimaModel.builder()
                .add(i2)
                .add(wn)
                .build();
        ssf = SsfUcarima.of(ucm);
    }

    public DoubleSeq[] process(DoubleSeq x) {
        DataBlockStorage ss = DkToolkit.fastSmooth(ssf, new SsfData(x));
        DataBlock t = ss.item(0);
        DataBlock c = ss.item(2);
        return new DoubleSeq[]{DoubleSeq.of(t.toArray()), DoubleSeq.of(c.toArray())};
    }

    public static double lambda(double nperiods) {
        double w = 2 * Math.PI / nperiods;
        double x = 1 - Math.cos(w);
        return .75 / (x * x);
    }

}
