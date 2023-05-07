/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sys.util;

import java.util.Calendar;

/**
 *
 * @author Charles
 */

public class PerformanceTimer {
    private static PerformanceTimer _this;
    private long start;
    private PerformanceTimer(){}
    public static PerformanceTimer getInstance()
    {
        if (_this == null)
            _this = new PerformanceTimer();
        return _this;
    }
    public void start()
    {
        start = Calendar.getInstance().getTimeInMillis();
    }
    public void print(String label)
    {
        System.out.println("Action : " + label);
        System.out.println("Total elapsed time " + (Calendar.getInstance().getTimeInMillis() - start) + " ms");
        start = Calendar.getInstance().getTimeInMillis();
    }
}
