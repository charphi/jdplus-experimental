/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.calendars.base.core;

import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.HolidayPattern;
import jdplus.toolkit.base.api.timeseries.regression.MovingHolidayVariable;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class ChineseMovingHolidaysTest {
    
    public ChineseMovingHolidaysTest() {
    }

    @Test
    public void testVariable() {
        MovingHolidayVariable ny = new MovingHolidayVariable("chinese.newyear", HolidayPattern.of(-10, HolidayPattern.Shape.LinearUp, 11).normalize());
        MovingHolidayVariable ge = new MovingHolidayVariable("gregorian.easter", HolidayPattern.of(-15, HolidayPattern.Shape.LinearUp, 12, 
                HolidayPattern.Shape.Constant,5, 
                HolidayPattern.Shape.LinearDown, 3).normalize());
        TsDomain domain = TsDomain.of(TsPeriod.monthly(1980, 1), 1000);
        FastMatrix matrix = Regression.matrix(domain, ny, ge);
//        System.out.println(matrix);
    }
    
}
