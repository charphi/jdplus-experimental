/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sigex.base.core;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MatrixOperationsTest {
    
    public MatrixOperationsTest() {
    }

    @Test
    public void testGcd() {
        FastMatrix m=FastMatrix.make(10,5 );
        Random rnd=new Random();
        double[] storage = m.getStorage();
        for (int i=0; i<storage.length; ++i)
            storage[i]=rnd.nextDouble();
        FastMatrix s=SymmetricMatrix.XXt(m);
        FastMatrix[] gcd = MatrixOperations.gcd(s, 10);
//        System.out.println(gcd[0]);
//        System.out.println(gcd[1]);
    }
    
}
