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

import jdplus.experimentalsa.base.core.filters.LocalPolynomialFilterFactory;
import jdplus.toolkit.base.core.math.linearfilters.*;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalPolynomialFilters {

    public double[] filter(double[] data, int horizon, int degree, String kernel, String endpoints, double ic, double tw, double passband) {
        // Creates the filters
        IntToDoubleFunction weights = weights(horizon, kernel);
        SymmetricFilter filter = LocalPolynomialFilterFactory.of(horizon, degree, weights);
        IFiniteFilter[] afilters;
        if (endpoints.equals("DAF")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = LocalPolynomialFilterFactory.directAsymmetricFilter(horizon, horizon-i-1, degree, weights);
            }
        } else if (endpoints.equals("CN")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = AsymmetricFiltersFactory.cutAndNormalizeFilter(filter, horizon-i-1);
            }
        } else {
            int u = 0;
            double[] c = new double[]{ic};
            switch (endpoints) {
                case "CC":
                    c = new double[0];
                case "LC":
                    u = 0;
                    break;
                case "QL":
                    u = 1;
                    break;
                case "CQ":
                    u = 2;
                    break;
            }
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = AsymmetricFiltersFactory.mmsreFilter(filter, horizon-i-1, u, c, null, passband, tw );
            }
        }
        DoubleSeq rslt = FilterUtility.filter(DoubleSeq.of(data), filter, afilters);
        return rslt.toArray();
    }

    IntToDoubleFunction weights(int horizon, String filter) {
        switch (filter) {
            case "Uniform":
                return DiscreteKernel.uniform(horizon);
            case "Biweight":
                return DiscreteKernel.biweight(horizon);
            case "Triweight":
                return DiscreteKernel.triweight(horizon);
            case "Tricube":
                return DiscreteKernel.tricube(horizon);
            case "Triangular":
                return DiscreteKernel.triangular(horizon);
            case "Parabolic":
                return DiscreteKernel.epanechnikov(horizon);
            case "Trapezoidal":
                return DiscreteKernel.trapezoidal(horizon);
            case "Gaussian":
                return DiscreteKernel.gaussian(horizon, 0.25);
            default:
                return DiscreteKernel.henderson(horizon);
        }
    }

    public FiltersToolkit.FiniteFilters filterProperties(int horizon, int degree, String kernel, String endpoints, double ic, double tw, double passband) {
        // Creates the filters
        IntToDoubleFunction weights = weights(horizon, kernel);
        SymmetricFilter filter = LocalPolynomialFilterFactory.of(horizon, degree, weights);
        IFiniteFilter[] afilters;
        if (endpoints.equals("DAF")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = LocalPolynomialFilterFactory.directAsymmetricFilter(horizon, i, degree, weights);
            }
        } else if (endpoints.equals("CN")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = AsymmetricFiltersFactory.cutAndNormalizeFilter(filter, i);
            }
        } else {
            int u = 0;
            double[] c = new double[]{ic};
            switch (endpoints) {
                case "CC":
                    c = new double[0];
                case "LC":
                    u = 0;
                    break;
                case "QL":
                    u = 1;
                    break;
                case "CQ":
                    u = 2;
                    break;
            }
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = AsymmetricFiltersFactory.mmsreFilter(filter, i, u, c, null, passband, tw);
            }
        }
        return new FiltersToolkit.FiniteFilters(filter, afilters);
    }
    
}
