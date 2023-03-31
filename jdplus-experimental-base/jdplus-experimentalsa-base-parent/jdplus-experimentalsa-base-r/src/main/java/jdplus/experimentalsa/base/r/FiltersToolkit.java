/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.experimentalsa.base.r;

import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.math.linearfilters.IFilter;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import jdplus.experimentalsa.base.core.dfa.MSEDecomposition;
import jdplus.experimentalsa.base.core.filters.FSTFilter;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.api.information.GenericExplorable;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class FiltersToolkit {

    @lombok.Value
    @lombok.Builder
    public static class FiniteFilters implements GenericExplorable {

        private SymmetricFilter filter;
        private IFiniteFilter[] afilters;

        private static final InformationMapping<FiniteFilters> MAPPING = new InformationMapping<FiniteFilters>() {
            @Override
            public Class getSourceClass() {
                return FiniteFilters.class;
             }
        };

        public static final InformationMapping<FiniteFilters> getMapping() {
            return MAPPING;
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        static {
            MAPPING.set("svariancereduction", Double.class, source -> varianceReduction(source.filter));
            MAPPING.setArray("avariancereduction", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? varianceReduction(source.afilters[i]) : Double.NaN);
            MAPPING.set("sbias2", Double.class, source -> bias2(source.filter));
            MAPPING.setArray("abias0", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? bias0(source.afilters[i]) : Double.NaN);
            MAPPING.setArray("abias1", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? bias1(source.afilters[i]) : Double.NaN);
            MAPPING.setArray("abias2", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? bias2(source.afilters[i]) : Double.NaN);
            MAPPING.set("sweights", double[].class, source -> source.filter.weightsToArray());
            MAPPING.setArray("aweights", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? source.afilters[i].weightsToArray() : null);
            MAPPING.set("sgain", double[].class, source -> gain(source.filter));
            MAPPING.setArray("again", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? gain(source.afilters[i]) : null);
            MAPPING.setArray("aphase", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? phase(source.afilters[i]) : null);
        }
    }

    static double[] gain(IFilter filter) {
        DoubleUnaryOperator gainFunction = filter.gainFunction();
        int RES = 600;
        double[] g = new double[RES + 1];
        for (int i = 0; i <= RES; ++i) {
            g[i] = gainFunction.applyAsDouble(i * Math.PI / 600);
        }
        return g;
    }

    static double varianceReduction(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = weights.applyAsDouble(i);
            s += w * w;
        }
        return s;
    }

    static double bias0(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = weights.applyAsDouble(i);
            s += w;
        }
        return s;
    }

    static double bias1(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = i * weights.applyAsDouble(i);
            s += w;
        }
        return s;
    }

    static double bias2(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = i * i * weights.applyAsDouble(i);
            s += w;
        }
        return s;
    }

    static double[] phase(IFilter filter) {
        DoubleUnaryOperator phaseFunction = filter.phaseFunction();
        int RES = 600;
        double[] g = new double[RES + 1];
        for (int i = 0; i <= RES; ++i) {
            g[i] = phaseFunction.applyAsDouble(i * Math.PI / 600);
        }
        return g;
    }

    public double[] mseDecomposition(double[] sfilter, double[] afilter, String density, double passband) {
        SymmetricFilter sf = SymmetricFilter.ofInternal(sfilter);
        FiniteFilter af = FiniteFilter.of(afilter, -sfilter.length+1);
        DoubleUnaryOperator sd;
        switch (density) {
            case "uniform":
                sd = x -> 1;
                break;
            default:
                sd = null;
        }
        MSEDecomposition d = MSEDecomposition.of(sd, sf.frequencyResponseFunction(), af.frequencyResponseFunction(), passband);
        return new double[]{d.getAccuracy(), d.getSmoothness(), d.getTimeliness(), d.getResidual(), d.getTotal()};
    }
    
 
    public FSTResult fstfilter(int nlags, int nleads, int pdegree, double scriterion, int sdegree, double tcriterion, double bandwith, boolean antiphase) {

        FSTFilter fst = FSTFilter.builder()
                .nlags(nlags)
                .nleads(nleads)
                .degreeOfSmoothness(sdegree)
                .polynomialPreservation(pdegree)
                .timelinessAntiphaseCriterion(antiphase)
                .timelinessLimits(0, bandwith)
                .build();

        FSTFilter.Results rslt = fst.make(scriterion, tcriterion, true);
        return FSTResult.builder()
                .filter(rslt.getFilter())
                .criterions(new double[]{rslt.getF(), rslt.getS(), rslt.getT()})
                .gain(gain(rslt.getFilter()))
                .phase(phase(rslt.getFilter()))
                .build();
    }
    
    @lombok.Value
    @lombok.Builder
    public static class FSTResult{

        private FiniteFilter filter;
        private double[] gain;
        private double[] phase;
        private double[] criterions;
        
        public double[] weights(){
            return filter.weightsToArray();
        }
        
        public int lb(){
            return filter.getLowerBound();
        }

        public int ub(){
            return filter.getUpperBound();
        }
    }
    
   public FSTResult fst(double[] filter, int startPos, double passband){
        FiniteFilter ff=FiniteFilter.of(filter, startPos);
        
        double f=FSTFilter.FidelityCriterion.fidelity(ff);
        double s=FSTFilter.SmoothnessCriterion.smoothness(ff);
        double t=FSTFilter.TimelinessCriterion.timeliness(ff, passband);
        
        return FSTResult.builder()
                .filter(ff)
                .criterions(new double[]{f,s,t})   
                .gain(gain(ff))
                .phase(phase(ff))
                .build();
    }
}
