package jdplus.experimentalsa.base.core.dfa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.experimentalsa.base.core.filters.ISymmetricFiltering;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;

public class DFAFilterFactory {
	
	private static final Map<DFAFilterSpec, ISymmetricFiltering> dictionary = new HashMap<>();

    public static ISymmetricFiltering of(DFAFilterSpec spec) {
        synchronized (dictionary) {
            ISymmetricFiltering filtering = dictionary.get(spec);
            if (filtering == null) {
                filtering = new Filter(spec);
                dictionary.put(spec, filtering);
            }
            return filtering;
        }
    }
	private static class Filter implements ISymmetricFiltering {

		private Filter(DFAFilterSpec spec) {
            int len = spec.getLags();
            symmetricFilter = spec.getTarget();
            asymmetricFilters = new FiniteFilter[len+1];
            DoubleUnaryOperator density = spec.getDensity().asFunction();
            
            DFAFilter.Builder builder = DFAFilter.builder()
                    .polynomialPreservation(spec.getPolynomialPreservationDegree())
                    .nlags(spec.getLags())
                    .timelinessLimits(spec.getW0(), spec.getW1())
                    .density(density)
                    .symetricFilter(symmetricFilter);
            DFAFilter.Results rslt;
            for (int i = 0, j=len-1; i < len; ++i, --j) {
            	rslt = builder.nleads(j).build().make(spec.getAccuracyWeight(), spec.getAccuracyWeight(), spec.getTimelinessWeight());
                asymmetricFilters[i] = rslt.getFilter();
            }
            
        }
		private final SymmetricFilter symmetricFilter;
        private final FiniteFilter[] asymmetricFilters;
        @Override
        public DoubleSeq process(DoubleSeq in) {
            return FilterUtility.filter(in, symmetricFilter, asymmetricFilters);
        }
        
        @Override
        public SymmetricFilter symmetricFilter(){
            return symmetricFilter;
        }
        
        @Override
        public IFiniteFilter[] endPointsFilters(){
            return asymmetricFilters;
        }
	}
}
