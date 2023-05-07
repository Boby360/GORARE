package go;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;
import sys.util.PerformanceTimer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class Dispatcher extends Thread{
    private static Dispatcher _this;
    private Dispatcher(){}
    public static Dispatcher getInstance()
    {
        if (_this == null)
            _this = new Dispatcher();
        return _this;
    }
    private final LinkedList<GameRequest> lstRequests = new LinkedList<GameRequest>();
    private final LinkedList<GameResponse> lstResponses = new LinkedList<GameResponse>();
    private final ArrayList<GameClient> lstClients = new ArrayList<GameClient>();

    public ArrayList<GameClient> getLstClients() {
        return lstClients;
    }
    private int currentClientIndex = 0;
    /**
     * Adds a request to the request buffer. Used internally when dealing with clients.
     * The dispatcher thread uses this method to add the request that some client wants to issue
     * @param request The request that the current client wants to issue
     */
    private void addRequest(GameRequest request)
    {
        synchronized (lstRequests)
        {

            if (lstRequests.size() > 50)
            {
                if (lstRequests.size() % 10 == 0)
                    Logger.getLogger("cvg").warning("Congestion : dispatcher's request buffer now counts " + lstRequests.size() + " requests!");
            }

            if (!lstRequests.contains(request))
            {                
                lstRequests.addLast(request);
            }
        }
    }
    /**
     * Used by watchdog thread, returns the oldest request in the buffer
     * @return The oldest GameRequest we have, or null if there are none
     */
    public GameRequest getRequest()
    {
        synchronized (lstRequests)
        {
            if (lstRequests.size() > 0)
            {
                return lstRequests.removeFirst();
            }
        }
        return null;
    }
    /**
     * Used by the watchdog thread, adds a response to the response buffer
     * @param response The response that needs to be added to the list
     */
    public void addResponse(GameResponse response)
    {
        synchronized (lstResponses)
        {
            lstResponses.addLast(response);
        }
    }
    private GameResponse getResponse()
    {
        synchronized (lstResponses)
        {
            if (lstResponses.size() > 0)
                return lstResponses.removeFirst();            
        }
        return null;
    }
    /**
     * We try to keep the synchronized access to the lstClients as short as possible
     * in order to leave it available as much as possible for other threads
     * @return The next game client in the list, or null if the list is empty
     */
    private GameClient getNextClient()
    {
        GameClient ret = null;
        synchronized (lstClients)
        {
            if (lstClients.size() == 0)
                return null;

            if (currentClientIndex >= lstClients.size())
                currentClientIndex = 0;

            ret = lstClients.get(currentClientIndex);
            currentClientIndex++;
            
            return ret;
        }        
    }
    @Override
    public void run()
    {
        setName("Dispatcher");
        GameRequest clientRequest = null;
        GameResponse currentResponse = null;
        while (true)
        {
            synchronized (lstClients)
            {
                if (currentClientIndex == lstClients.size())
                currentResponse = getResponse();
            }                

            GameClient client = getNextClient();

            if (client != null)
            {
                if (!client.isWaitingForResponse()) {
                    clientRequest = client.getRequest();
                    if (clientRequest != null) {
                        client.setWaitingForResponse(true);
                        addRequest(clientRequest);
                    }
                }

                if (currentResponse != null) {
                    client.offerResponse(currentResponse);
                }
            }
            sleepOneMillisec();
        }
        /**
         * Start loop
         *   for each client
         *      if client wants to send , add its request to request buffer (can be serverinfo/playerinfo), unless it is waiting for a response
         *      if client wants the current response (and its not null), serve it
         *      take next response (can be null)
         *   end for
         * End loop
         */
    }
    public void registerGameClient(GameClient client)
    {
        synchronized (lstClients)
        {
            lstClients.add(client);
        }
    }
    public void unregisterGameClient(GameClient client)
    {
        synchronized (lstClients)
        {
            int index = lstClients.indexOf(client);
            if (currentClientIndex >= index)
                currentClientIndex--;
            lstClients.remove(client);
        }
    }
    private void sleepOneMillisec() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
}
