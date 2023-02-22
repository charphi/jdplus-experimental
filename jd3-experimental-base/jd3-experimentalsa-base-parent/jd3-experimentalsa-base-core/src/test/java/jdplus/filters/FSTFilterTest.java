/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.DoubleSeq;
import java.util.Arrays;
import jdplus.filters.FSTFilter.SmoothnessCriterion;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRoots;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class FSTFilterTest {

    public FSTFilterTest() {
    }

    @Test
    public void testSymmetric() {
        for (int i = 0; i <= 10; ++i) {
            FSTFilter ff = FSTFilter.builder()
                    .nlags(4)
                    .nleads(4)
                    .build();
            FSTFilter.Results rslt = ff.make(.1 * i, 0, false);
            System.out.println(DoubleSeq.of(rslt.getFilter().weightsToArray()));
        }
    }

    @Test
    public void testASymmetric() {
        for (int i = 0; i <= 5; ++i) {
            FSTFilter ff = FSTFilter.builder()
                    .nlags(10-i)
                    .nleads(i)
                    .build();
            FSTFilter.Results rslt = ff.make(1, 0, true);
            System.out.print(rslt.getS());
            System.out.print('\t');
            System.out.print(rslt.getF());
            System.out.print('\t');
            System.out.println(DoubleSeq.of(rslt.getFilter().weightsToArray()));
        }
    }

    public static void main(String[] args) {
        daf(11);
    }

    public static void daf(int h) {
        for (int i = 0; i <= h; ++i) {
            FSTFilter ff = FSTFilter.builder()
                    .nlags(h)
                    .nleads(i)
                    .polynomialPreservation(2)
                    .build();
            FSTFilter.Results rslt = ff.make(1, 0, false);
            FiniteFilter filter = rslt.getFilter();
//            System.out.println(DoubleSeq.of(filter.weightsToArray()));
        }

    }

    @Test
    public void testSmoothness() {
        for (int degree = 1; degree < 4; ++degree) {
            Polynomial D = UnitRoots.D(1, degree);
            SymmetricFilter S = SymmetricFilter.convolutionOf(D, 1);
            double[] s = S.coefficientsAsPolynomial().toArray();
            assertTrue(Arrays.equals(s, SmoothnessCriterion.weights(degree)));
        }
    }

//    @Test
//    public void testMatrix() {
//        Polynomial D = UnitRoots.D(1, 3);
//        FastMatrix M=FastMatrix.square(13);
//        for (int i=0; i<4; ++i){
//            M.subDiagonal(-i).set(D.get(i));
//        }
//        FastMatrix S1=SymmetricMatrix.XtX(M);
//        System.out.println(S1);
//        FastMatrix S2=SymmetricMatrix.XtX(M.extract(3, 10, 0, 13));
//        System.out.println(S2);
//    }
}
