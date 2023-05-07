package go;

import go.responses.PlayerInfoResponse;
import go.responses.ServerInfoResponse;
import java.util.Calendar;
import java.util.logging.Logger;
import sys.util.Settings;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class GameCache extends Filter{
    private static GameCache _this;
    private ServerInfoResponse cachedSIResponse;
    private PlayerInfoResponse cachedPIResponse;
    private long siStamp;
    private long piStamp;
    private int maxLife = 600;
    private boolean siRequestedButOutdated = false;
    private boolean piRequestedButOutdated = false;
    public static GameCache getInstance()
    {
        if (_this == null)
            _this = new GameCache();
        return _this;
    }
    private boolean isSIOutdated()
    {
        if (cachedSIResponse == null)
            return true;
        
        long time = Calendar.getInstance().getTimeInMillis();
        time -= maxLife;        
        if (time > siStamp)
            return true;

        return false;
    }
    private boolean isPIOutdated()
    {
        if (cachedPIResponse == null)
            return true;

        long time = Calendar.getInstance().getTimeInMillis();
        time -= maxLife;
        if (time > piStamp)
            return true;

        return false;
    }
    private void sleepOneMillisec() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
    @Override
    public void run() {        
        try {
            maxLife = Integer.parseInt(Settings.getInstance().getSetting("maxcachetime"));
        } catch (Exception ex) {
            Logger.getLogger("cvg").warning("Invalid setting maxcachetime. Using default value :" + maxLife);
        }
        GameRequest piReq = new GameRequest("playerinfo");
        GameRequest siReq = new GameRequest("serverinfo");
        while (true)
        {
            if (siRequestedButOutdated)
            {
                cachedSIResponse = (ServerInfoResponse) doUncachedRequest(siReq);
                siRequestedButOutdated = false;
                siStamp = Calendar.getInstance().getTimeInMillis();
            }
            if (piRequestedButOutdated)
            {
                cachedPIResponse = (PlayerInfoResponse) doUncachedRequest(piReq);
                piRequestedButOutdated = false;
                piStamp = Calendar.getInstance().getTimeInMillis();
            }
            sleepOneMillisec();
        }
        /**
         * Start loop
         * refresh data if outdated = true
         * set outdated = false
         * End loop
         */
    }
    /**
     * Returns the cached response, this response can never be outdated (older than max life)
     * If it is outdated, this method will block until a fresh response has been retrieved
     * @param request The request that needs an answer
     * @return The GameResponse to this request
     */
    public GameResponse getCachedResponse(GameRequest request)
    {
        /**
         * if the response is outdated, set outdated to true and block
         * until fresh data has arrived
         *
         * finally return the response to this request
         */
        if (request.getRawRequest().equals("serverinfo"))
        {
            if (isSIOutdated() && !siRequestedButOutdated)
            {
                Logger.getLogger("cvg").info("Serverinfo is outdated. Requesting fresh data.");
                siRequestedButOutdated = true;                
            }

            while (isSIOutdated())
            {                
                sleepOneMillisec();
            }
            return cachedSIResponse;
        } else if (request.getRawRequest().equals("playerinfo"))
        {
            if (isPIOutdated() && !piRequestedButOutdated)
            {
                Logger.getLogger("cvg").info("Playerinfo is outdated. Requesting fresh data.");
                piRequestedButOutdated = true;                
            }

            while (isPIOutdated())
            {
                sleepOneMillisec();
            }
            return cachedPIResponse;
        } else {
            Logger.getLogger("cvg").severe("This request should not be served to the cache! ["+request.getRawRequest()+"]");
            return null;
        }        
    }

    @Override
    public void onGossipMessage(String message) {
        // do nothing.
    }
    
}
