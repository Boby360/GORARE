package go.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketLobby {
    private static SocketLobby _this;
    private SocketLobby(){}
    private ServerSocket socket;
    public static SocketLobby getInstance()
    {
        if (_this == null)
            _this = new SocketLobby();
        return _this;
    }
    public void listen(final int port)
    {
        Thread t = new Thread("ServerLobby")
        {
            @Override
            public void run()
            {
                try {
                    doRun(port);
                } catch (IOException ex) {
                    if (ex.getMessage().contains("JVM_Bind"))
                    {
                        Logger.getLogger("cvg").severe("Port " + port + " already in use. Close all instances of this application before running.");
                        Logger.getLogger("cvg").severe("Terminating...");
                        System.exit(1);
                    } else {
                        Logger.getLogger("cvg").severe(ex.getMessage());
                    }
                }
            }
        };
        t.start();
    }
    private void doRun(int port) throws IOException
    {
        socket = new ServerSocket(port);
        Socket s = null;
        while (true)
        {
            s = socket.accept();
            RemoteClient rc = new RemoteClient(s);
            rc.start();
        }
    }
}
