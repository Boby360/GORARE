package go;


import java.util.LinkedList;
import java.util.logging.Logger;
import sys.util.PerformanceTimer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Charles
 */
public abstract class GameClient {
    private final LinkedList<GameRequest> lstRequests = new LinkedList<GameRequest>();
    private final LinkedList<GameResponse> lstResponses = new LinkedList<GameResponse>();
    private boolean waitingForResponse = false;

    public GameClient() {
        Dispatcher.getInstance().registerGameClient(this);
    }
    
    public boolean isWaitingForResponse() {
        return waitingForResponse;
    }

    public void setWaitingForResponse(boolean waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }
/**
 * Gets the latest request
 * @return The latest request or null if there is no request pending
 */
    public GameRequest getRequest()
    {
        synchronized (lstRequests)
        {
            if (lstRequests.size() > 0)
            {
                return lstRequests.getFirst();
            }
            return null;
        }
    }
    /**
     * Used by the dispatcher thread. Adds a response to the response buffer
     * This method will take any response and save the response only if it is the one
     * we want.
     * @param response The response received by this GameClient from the dispatcher
     */
    public void offerResponse(GameResponse response)
    {
        if (lstRequests.size() == 0)
            return;

        GameRequest request = lstRequests.getFirst();
        if (request.wantsResponse(response))
        {
            synchronized (lstRequests)
            {
                synchronized (lstResponses)
                {
                    lstRequests.removeFirst();
                    lstResponses.addLast(response);
                    setWaitingForResponse(false);
                }
            }
        }
    }
    /**
     * This method handles the requests that are performed by the filter
     * It will block until a response is received
     * @param request The request that needs to be issued
     * @return The response to this request
     */
    public GameResponse doRequest(GameRequest request)
    {
        if (request.isCustom())
        {
            return doUncachedRequest(request);
        } else {
            // Return value from cache
            return GameCache.getInstance().getCachedResponse(request);
        }
        
    }
    /**
     * This method handles the requests that are performed by the GameCache or Filter
     * It will block until a response is received
     * @param request The request that needs to be issued
     * @return The response to this request
     */
    public GameResponse doUncachedRequest(GameRequest request)
    {
        // Query the server
        synchronized (lstRequests) {
            lstRequests.add(request);
        }

        while (true) {
            synchronized (lstResponses) {
                if (lstResponses.size() > 0) {
                    return lstResponses.removeFirst();                    
                }                
            }
            sleepOneMillisec();
        }
    }

    private void sleepOneMillisec() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
    public abstract void onGossipMsg(String message);
}
