/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.experimentalsa.base.core.rkhs;

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.stats.Kernel;
import jdplus.toolkit.base.core.stats.Kernels;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class HighOrderKernels {

    /**
     * Returns the Hankel matrix of order k, built on the moments q,q+1... of
     * the given kernel
     *
     * @param kernel
     * @param q
     * @param k
     * @return
     */
    public FastMatrix hankel(Kernel kernel, int q, int k) {
        FastMatrix H = FastMatrix.square(k);
        H.set((i, j) -> kernel.moment(q + i + j));
        return H;
    }

    /**
     * Returns the Hankel matrix of order k, built on the moments 0,1... of
     * the given kernel
     *
     * @param kernel
     * @param k
     * @return
     */
    public FastMatrix hankel(Kernel kernel, int k) {
        FastMatrix H = FastMatrix.square(k);
        H.set((i, j) -> kernel.moment(i + j));
        return H;
    }

    /**
     * 
     * @param kernel
     * @param r
     * @return 
     */
    public DoubleUnaryOperator kernel(Kernel kernel, int r) {
        FastMatrix Hk1 = hankel(kernel, 0, r + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        DoubleUnaryOperator f0 = kernel.asFunction();
        DataBlock row = Hk1.row(0);
        row.set(0, 1);
        return x -> {

            double cur = 1;
            for (int j = 1; j <= r; ++j) {
                cur *= x;
                row.set(j, cur);
            }
            double detHx = FastMatrix.determinant(Hk1);
            return (detHx / detHk1) * f0.applyAsDouble(x);
        };
    }

    private void suppress(final int row, final int column, FastMatrix all, FastMatrix t) {
        int k = all.getColumnsCount();
        for (int c = 0, tc = 0; c < k; ++c) {
            if (c != column) {
                DataBlock cur = all.column(c);
                DataBlock tcur = t.column(tc++);
                DoubleSeqCursor cursor = cur.cursor();
                DoubleSeqCursor.OnMutable tcursor = tcur.cursor();
                for (int r = 0; r < k; ++r) {
                    if (r != row) {
                        tcursor.setAndNext(cursor.getAndNext());
                    } else {
                        cursor.skip(1);
                    }
                }
            }
        }
    }

    @Deprecated
    public Polynomial oldP(Kernel kernel, int r) {
        Polynomial q = Polynomial.ONE;
        for (int i = 1; i <= r; ++i) {
            Polynomial pcur = pk(kernel, i);
            double p0 = pcur.evaluateAt(0);
            if (p0 != 0) {
                q = q.plus(pcur.times(p0));
            }
        }
        return q;
    }

    public Polynomial p(Kernel kernel, int r) {
        if (r == 0)
            return Polynomial.ONE;
        FastMatrix Hk1 = hankel(kernel, 0, r + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        boolean pos = true;
        double[] c = new double[r + 1];
        FastMatrix m = FastMatrix.square(r);
        for (int i = 0; i <= r; ++i) {
            suppress(0, i, Hk1, m);
            double cur = FastMatrix.determinant(m) / detHk1;
            c[i] = pos ? cur : -cur;
            pos = !pos;
        }
        return Polynomial.ofInternal(c);
    }

    public Polynomial pk(Kernel kernel, int r) {
        FastMatrix Hk0 = hankel(kernel, 0, r);
        double detHk0 = SymmetricMatrix.determinant(Hk0);
        FastMatrix Hk1 = hankel(kernel, 0, r + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        double q = Math.sqrt(detHk0 * detHk1);
        double[] c = new double[r + 1];
        FastMatrix m = FastMatrix.square(r);
        boolean pos = r % 2 == 0;
        for (int i = 0; i <= r; ++i) {
            suppress(r, i, Hk1, m);
            double cur = FastMatrix.determinant(m) / q;
            c[i] = pos ? cur : -cur;
            pos = !pos;
        }
        return Polynomial.ofInternal(c);
    }

    /**
     * k-Kernel
     *
     * @param k
     * @return
     */
    public Polynomial biweightKernel(int k) {
        Polynomial pk = p(Kernels.BIWEIGHT, k - 1);
        return pk.times(Kernels.biWeightAsPolynomial());
    }

    public Polynomial truncatedBiweightKernel(int k, double q) {
        Polynomial K = biweightKernel(k);
        double w = K.integrate(-1, q);
        return K.divide(w);
    }

    public Polynomial triweightKernel(int k) {
        Polynomial pk = p(Kernels.TRIWEIGHT, k - 1);
        return pk.times(Kernels.triWeightAsPolynomial());
    }

    public Polynomial truncatedTriweightKernel(int k, double q) {
        Polynomial K = triweightKernel(k);
        double w = K.integrate(-1, q);
        return K.divide(w);
    }

    /**
     * 
     * @param k
     * @param m Length of the Henderson filter (from -m to +m)
     * @return 
     */
    public Polynomial hendersonKernel(int k, int m) {
        Polynomial pk = p(Kernels.henderson(m), k - 1);
        return pk.times(Kernels.hendersonAsPolynomial(m));
    }

    public Polynomial truncatedHendersonKernel(int k, int m, double q) {
        Polynomial K = hendersonKernel(k, m);
        double w = K.integrate(-1, q);
        return K.divide(w);
    }
}
