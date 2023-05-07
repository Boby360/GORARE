package go;


import go.gossip.GossipEvent;
import go.gossip.GossipParser;
import go.objects.GORAREResponse;
import gorare.go.ClientSocket;
import go.objects.GameResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import gorare.sys.util.LogHandler;
import gorare.sys.util.Settings;
import java.util.ArrayList;
import java.util.LinkedList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class GameInterface {
    private static GameInterface _this;
    private GameInterface(){}
    private final ArrayList<GossipEventListener> lstGossipListeners = new ArrayList<GossipEventListener>();
    private String password = null;
    private final LinkedList<String> lstGossipCommands = new LinkedList<String>();
    private Thread gossipDispatcher;
    private final Object gossipDispatcherLock = new Object();
    private boolean gossipEnabled;
    public static GameInterface getInstance()
    {
        if (_this == null)
            _this = new GameInterface();
        return _this;
    }
    public void connect()
    {
        System.out.println("GORARE Plugin library v0.1 By Artkayek Starting...");
        try {
            doConnect();
        } catch (UnknownHostException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }

    public void enableGossip()
    {
        gossipEnabled = true;
        synchronized  (gossipDispatcherLock)
        {
            if (gossipDispatcher == null)
            {
                gossipDispatcher = new Thread("GossipDispatcher")
                {
                    @Override
                    public void run()
                    {
                        String command = null;
                        GossipEvent evt = null;
                        while (true)
                        {
                            if (gossipEnabled)
                            {
                                synchronized (lstGossipCommands)
                                {
                                    if (lstGossipCommands.size() != 0)
                                    {
                                        command = lstGossipCommands.removeFirst();
                                    }
                                }
                                if (command != null)
                                    evt = GossipParser.getInstance().parseMessage(command.substring(25));
                                if (evt != null)
                                {
                                    GossipEventListener.preProcessGossipEvent(evt);
                                    dispatchGossipEvent(evt);
                                }
                            } else {
                                synchronized (lstGossipCommands)
                                {
                                    if (lstGossipCommands.size() > 0)
                                        lstGossipCommands.clear();
                                }                                
                            }
                            evt = null;
                            command = null;
                            sleepOneMillisec();
                        }
                    }
                    private void sleepOneMillisec()
                    {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GameInterface.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                };
                gossipDispatcher.start();
            }
        }
        ClientSocket.getInstance().enableGossip();
    }
    public void disableGossip()
    {
        gossipEnabled = false;
        ClientSocket.getInstance().disableGossip();
    }
    private void doConnect() throws UnknownHostException, IOException
    {

        LogHandler.getInstance().register();
        password = Settings.getInstance().getSetting("password");


        int adminPort = 1234;
        try {
            adminPort = Integer.parseInt(Settings.getInstance().getSetting("gorareport"));
        } catch (Exception ex) {
            Logger.getLogger("cvg").warning("Invalid setting gorareport. Using default value :" + adminPort);
        }

        String gameIP = "127.0.0.1";
        try {
            gameIP = Settings.getInstance().getSetting("gorareip");
            if (gameIP == null) {
                throw new IllegalArgumentException();
            }
        } catch (Exception ex) {
            gameIP = "127.0.0.1";
            Logger.getLogger("cvg").warning("Invalid setting gorareip. Using default value :" + gameIP);
        }
        ClientSocket.getInstance().connect(gameIP,adminPort);
    }
    public int getGORARECacheTime()
    {
        GORAREResponse response = (GORAREResponse) ClientSocket.getInstance().doRequest("GORARE|cachetime");
        return Integer.parseInt(response.toRawResponse().substring(7));
    }
    public GameResponse doRequest(String request)
    {
        return ClientSocket.getInstance().doRequest(request);
    }

    public void onGossipMessage(String message) {
        
        synchronized (lstGossipCommands)
        {
            lstGossipCommands.addLast(message);
        }
    }
    protected void registerGossipListener(GossipEventListener listener)
    {
        enableGossip();
        synchronized (lstGossipListeners)
        {
            lstGossipListeners.add(listener);
        }
    }
    public void dispatchGossipEvent(GossipEvent evt) {
        synchronized (lstGossipListeners) {
            for (GossipEventListener listener : lstGossipListeners) {
                listener.onGossipEvent(evt);
            }
        }
    }

}
