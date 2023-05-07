package go;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import sys.util.Settings;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class WatchDog extends Thread{
    private static WatchDog _this;
    private WatchDog(){}
    private long startTime;
    private int maxTime = 1000;
    public static WatchDog getInstance()
    {
        if (_this == null)
            _this = new WatchDog();
        return _this;
    }
    @Override
    public void run()
    {
        setName("Watchdog");
        GameRequest request = null;
        GameResponse response = null;
        long now;

        try {
            maxTime = Integer.parseInt(Settings.getInstance().getSetting("waittime"));
        } catch (Exception ex) {
            Logger.getLogger("cvg").warning("Unknown value for setting waittime! Using default of " + maxTime+ "ms .");
        }

        while (true)
        {
            request = null;
            response = null;

            startTime = Calendar.getInstance().getTimeInMillis();
            request = Dispatcher.getInstance().getRequest();
            
            while (request == null)
            {
                sleepOneMillisec();
                request = Dispatcher.getInstance().getRequest();
            }            
            
            GameIO.getInstance().addRequest(request);

            response = GameIO.getInstance().getResponse();

            while (response == null)
            {
                sleepOneMillisec();
                response = GameIO.getInstance().getResponse();
            }

            Dispatcher.getInstance().addResponse(response);

            
            now = Calendar.getInstance().getTimeInMillis();

            long diff = now - (startTime + maxTime);
            if (diff < 0)
            {
                try {
                    Logger.getLogger("cvg").info("Watchdog is now sleeping for " + Math.abs(diff) + "ms.");
                    Thread.sleep(Math.abs(diff));
                } catch (InterruptedException ex) {
                    Logger.getLogger(WatchDog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        // Start loop
        // reset timer
        //      get a request from dispatcher
        //      pass it to GameIO
        //      block for response from GameIO
        //      post the response to the dispatcher
        // wait the remaining time
        // end loop
        // 
    }
    private void sleepOneMillisec() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
}
