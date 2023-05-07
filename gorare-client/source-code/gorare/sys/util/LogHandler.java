/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gorare.sys.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogHandler {
    private static LogHandler _this;
    private LogHandler(){}
    private boolean errorOnly = false;

    public void setErrorOnly(boolean errorOnly) {
        this.errorOnly = errorOnly;
    }
    public static LogHandler getInstance()
    {
        if (_this == null)
        {
            _this = new LogHandler();
            
            String erronly = Settings.getInstance().getSetting("erroronly");
            if (erronly != null && (erronly.toLowerCase().equals("yes") || erronly.toLowerCase().equals("true")))
                _this.setErrorOnly(true);
            
        }
        return _this;
    }
    public void register()
    {
        Logger.getLogger("cvg").setUseParentHandlers(false);
        Logger.getLogger("cvg").addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                if (record.getLevel().intValue() < 900)
                {
                    if (!errorOnly)
                        System.out.println(sdf.format(Calendar.getInstance().getTimeInMillis()) + " " + record.getLevel().getName() + " " + record.getMessage());
                } else {
                    System.err.println(sdf.format(Calendar.getInstance().getTimeInMillis()) + " " + record.getLevel().getName() + " " + record.getMessage());
                }
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }
}
