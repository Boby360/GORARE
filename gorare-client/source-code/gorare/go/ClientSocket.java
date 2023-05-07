package gorare.go;


import go.GameInterface;
import go.objects.CustomResponse;
import go.objects.GORAREResponse;
import go.objects.GameResponse;
import go.objects.Player;
import go.objects.PlayerInfoResponse;
import go.objects.ServerInfoResponse;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import gorare.sys.util.AsciiPrinter;
import gorare.sys.util.PerformanceTimer;
import gorare.sys.util.Settings;
import java.net.SocketException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class ClientSocket extends Thread{
    private static ClientSocket _this;
    private ClientSocket(){}
    private Socket socket;
    private PrintWriter out;
    private BufferedInputStream in;
    private String request = null;
    private GameResponse response;
    private boolean waitingForResponse = false;
    private final Object myLock = new Object();
    private final Object myPreLock = new Object();
    private boolean authenticated = false;
    public static ClientSocket getInstance()
    {
        if (_this == null)
            _this = new ClientSocket();
        return _this;
    }
    public void connect(String ipAddress, int port) throws UnknownHostException, IOException {
        socket = new Socket();
        InetAddress trAdr = InetAddress.getByName(ipAddress);
        try {
            InetSocketAddress isa = new InetSocketAddress(trAdr, port);
            socket.connect(isa);
        } catch (ConnectException ex) {
            Logger.getLogger("cvg").severe("Unable to connect to " + ipAddress + ":" + port);
            System.exit(1);
        }
        out = new PrintWriter(socket.getOutputStream());
        in = new BufferedInputStream(socket.getInputStream());
        socket.setReceiveBufferSize(70000);
        
        Logger.getLogger("cvg").info("Connected with GORARE server at " + socket.getInetAddress().getHostAddress());
        Logger.getLogger("cvg").info("Attempting to authenticate ...");
        start();
    }
    @Override
    public void run()
    {
        if (!socket.isConnected())
        {
            Logger.getLogger("cvg").severe("Socket must be connected first!");
            System.exit(1);
        }


        
        try {
            doRun();
        } catch (IOException ex) {
            if (ex instanceof SocketException)
            {
                if (ex.getMessage().contains("Connection reset"))
                {
                    Logger.getLogger("cvg").info("Server closed the connection!");
                    System.exit(1);
                }
            }
            Logger.getLogger("cvg").severe(ex.getMessage());
        }

    }
    private void doRun() throws IOException
    {
        if (Settings.getInstance().getSetting("password") == null)
        {
            Logger.getLogger("cvg").severe("Setting password must be set in settings.cfg!");
            System.exit(1);
        }
        
        waitingForResponse = true;
        writeToServer("gorare" + Settings.getInstance().getSetting("password")  + (char) 0);
        byte[] buff = new byte[512];

            StringBuilder sb = new StringBuilder();

            int bytesRead = 0;

            while ((bytesRead = in.read(buff)) != -1) {
                sb.append(new String(buff, 0, bytesRead));
                while (sb.toString().indexOf((char) 0) != -1)
                {
                    int indexOfDelimiter = sb.toString().indexOf((char) 0);
                    if (indexOfDelimiter != -1)
                    {
                        String receivedMessage = sb.toString().substring(0,indexOfDelimiter);
                        sb.delete(0, indexOfDelimiter+1);
                        onMessageReceived(receivedMessage);
                    }
                }
                while (request == null && in.available() == 0)
                {
                    sleepOneMillisec();
                }
                if (!waitingForResponse && request != null)
                {
                    //Logger.getLogger("cvg").info("Sending request ["+request+"]");
                    waitingForResponse = true;
                    writeToServer(request + (char)0);
                }
            }
        Logger.getLogger("cvg").info("Server closed connection.");
        System.exit(0);
    }
    private void sleepOneMillisec()
    {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private GameResponse doRequestBis(String request)
    {
        synchronized (myLock)
        {
            response = null;
            this.request = request;
            while (response == null)
            {
                sleepOneMillisec();
            }

            return response;
        }
    }
    public void enableGossip()
    {
        while (!authenticated)
        {
            sleepOneMillisec();
        }
        writeToServer("GOSSIP|start" + (char) 0);
    }
    public void disableGossip()
    {
        while (!authenticated)
        {
            sleepOneMillisec();
        }
        writeToServer("GOSSIP|stop" + (char) 0);
    }
    public GameResponse doRequest(String request)
    {
        // Make sure one thread doesn't monopolize the locks
        synchronized (myPreLock)
        {
            while (waitingForResponse)
                sleepOneMillisec();            
        }
        return doRequestBis(request);
    }
    private void onMessageReceived(String response)
    {
        if (response.equals("OK"))
        {
            authenticated = true;
            Logger.getLogger("cvg").info("Authentication successful.");
            waitingForResponse = false;
            return;
        }
        if (response.startsWith("GOSSIP|"))
        {
            GameInterface.getInstance().onGossipMessage(response.substring(7));
            return;
        }
        String[] splitResponse = response.split("\n");
        if (request.equals("serverinfo"))
        {            
            if (splitResponse.length == 8)
            {
                this.response = handleServerInfo(splitResponse);
                request = null;
                waitingForResponse = false;
            }
        } else if (request.equals("playerinfo"))
        {
            this.response = handlePlayerInfo(splitResponse);
            request = null;
            waitingForResponse = false;
        } else {
            this.response = handleCustom(splitResponse);
            request = null;
            waitingForResponse = false;
        }
        //Logger.getLogger("cvg").info("Response received.");
    }
    private GameResponse handleCustom(String[] splitResponse)
    {
        if (splitResponse.length == 1)
        {
            String resp = splitResponse[0];
            if (resp.startsWith("GORARE|"))
            {
                return new GORAREResponse(resp);
            } else {
                return new CustomResponse(resp);
            }
                       
        }
        Logger.getLogger("cvg").severe("Wrong response to custom command ["+splitResponse+"]");
        return null;

    }
    private GameResponse handlePlayerInfo(String[] splitResponse)
    {
        ArrayList<Player> lstPlayers = new ArrayList<Player>();
        if (splitResponse.length == 1 && splitResponse[0].equals("NO PLAYER YET"))
            return new PlayerInfoResponse(lstPlayers);
        Player p = new Player();
        String playerLine = null;
        String dataElement = null;
        
        for (int i=0;i<splitResponse.length;i++)
        {            
            playerLine = splitResponse[i];
            String data = playerLine.substring(0,playerLine.indexOf("|"));
            String name = playerLine.substring(playerLine.indexOf("|")+1,playerLine.length());
            String[] splitData = data.split(",");
            if (splitData.length == 5)
            {
                p = new Player();
                for (int j=0;j<splitData.length;j++)
                {
                    dataElement = splitData[j];
                    switch (j)
                    {
                        case 0: p.setNumber(Integer.parseInt(dataElement));break;
                        case 1: p.setClientId(Integer.parseInt(dataElement));break;
                        case 2: p.setTeam(dataElement);break;
                        case 3: p.setSpecialty(dataElement);break;
                        case 4: p.setPing(Integer.parseInt(dataElement));break;
                    }
                }
                p.setName(name);
                lstPlayers.add(p);
            }
        }
        PlayerInfoResponse resp = new PlayerInfoResponse(lstPlayers);
        return resp;
    }
    private GameResponse handleServerInfo(String[] splitResponse)
    {
        ServerInfoResponse resp = new ServerInfoResponse();
        String element = null;
        for (int i=0;i<splitResponse.length;i++)
        {
            element = splitResponse[i];
            switch (i)
            {
                case 0:resp.setHostname(element);break;
                case 1:resp.setPort(Integer.parseInt(element));break;
                case 2:resp.setMap(element);break;
                case 3:resp.setNumplayers(Integer.parseInt(element));break;
                case 4:resp.setMaxplayers(Integer.parseInt(element));break;
                case 5:resp.setOs(Integer.parseInt(element));break;
                case 6:resp.setPassword(Integer.parseInt(element));break;
                case 7:resp.setTimeleft(element);break;
            }                    
        }
        return resp;
    }
    private void writeToServer(String message)
    {
        out.write(message);
        out.flush();
    }

    public void disconnect() throws IOException {
        socket.close();
        Logger.getLogger("cvg").info("Disconnected from GORARE server at " + socket.getInetAddress().getHostAddress());
    }
}
