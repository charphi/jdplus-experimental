/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.businesscycle.base.r;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.businesscycle.base.core.HodrickPrescottFilter;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class HodrickPrescott {
    
    public Matrix filter(double[] data, double lambda, double cycleLength){
       HodrickPrescottFilter filter=new HodrickPrescottFilter(
               cycleLength <=0 ?lambda : HodrickPrescottFilter.lambda(cycleLength));
        DoubleSeq[] rslt = filter.process(DoubleSeq.of(data));
        int n=data.length;
        double[] all=new double[n*2];
        rslt[0].copyTo(all, 0);
        rslt[1].copyTo(all, n);
        return Matrix.of(all, n, 2);
    }
    
}
