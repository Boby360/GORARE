/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import go.Dispatcher;
import go.GameCache;
import go.GameIO;
import go.GameLogReader;
import go.WatchDog;
import go.server.SocketLobby;
import java.util.logging.Logger;
import sys.util.LogHandler;
import sys.util.Settings;

/**
 *
 * @author Charles
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogHandler.getInstance().register();
        System.out.println("Remote Admin RElay v0.4b by Artkayek starting...");
        int localPort = 1234;
        try {
             localPort = Integer.parseInt(Settings.getInstance().getSetting("port"));
        } catch (Exception ex)
        {
            Logger.getLogger("cvg").warning("Invalid setting port. Using default value :" + localPort);
        }
        GameLogReader.getInstance().start();
        GameIO.getInstance().start();
        WatchDog.getInstance().start();
        Dispatcher.getInstance().start();
        GameCache.getInstance().start();
        SocketLobby.getInstance().listen(localPort);
    }

}
