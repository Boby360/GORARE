package go;

import go.server.RemoteClient;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Charles
 */
public abstract class Filter extends Thread{
    private final GameClient client;
    private final Object myLock = new Object();
    public Filter()
    {
         this.client = new GameClient() {

            @Override
            public void onGossipMsg(String message) {
                onGossipMessage(message);
            }
        };
    }
    public abstract void onGossipMessage(String message);
    public void unRegisterFromDispatcher()
    {
        Dispatcher.getInstance().unregisterGameClient(client);
    }
    /**
     * The thread that will use the doRequest method and perform read/writes
     */
    @Override
    public abstract void run();

    /**
     * This method is an entry point to the game.
     * It takes a request and blocks until a response is received.
     * This method uses a blank object as a lock to make sure only one
     * thread is allowed to run a request on the GameClient at a time.
     * @param request The request that is to be sent to the game (or cache)
     * @return The response to this request
     */
    public GameResponse doRequest(GameRequest request)
    {
        synchronized (myLock)
        {
            return client.doRequest(request);
        }
    }
    /**
     * This method is an entry point to the game.
     * It takes a request and blocks until a response is received.
     * This method uses a blank object as a lock to make sure only one
     * thread is allowed to run a request on the GameClient at a time.
     * @param request The request that is to be sent to the game
     * @return The response to this request
     */
    public GameResponse doUncachedRequest(GameRequest request)
    {
        synchronized (myLock)
        {
            return client.doUncachedRequest(request);
        }
    }
}
