package go;

import go.interpreters.InvalidPlayerInfoLineException;
import go.interpreters.PlayerInfoParser;
import go.interpreters.ServerInfoParser;
import go.objects.Player;
import go.responses.ServerInfoResponse;
import go.responses.CustomResponse;
import go.responses.PlayerInfoResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import sys.util.AsciiPrinter;
import sys.util.PerformanceTimer;
import sys.util.Settings;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
public class GameIO extends Thread {

    private static GameIO _this;

    private GameIO() {
    }
    private final LinkedList<GameRequest> lstRequests = new LinkedList<GameRequest>();
    private final LinkedList<GameResponse> lstResponses = new LinkedList<GameResponse>();
    private String password = null;
    private Socket sock;
    private PrintWriter writer;

    public static GameIO getInstance() {
        if (_this == null) {
            _this = new GameIO();
        }
        return _this;
    }

    public void addRequest(GameRequest request) {
        synchronized (lstRequests) {
            lstRequests.addLast(request);
        }
    }

    /**
     * Gets the latest GameResponse
     * @return The latest GameResponse or null if there is no response yet
     */
    public GameResponse getResponse() {
        synchronized (lstResponses) {
            if (lstResponses.size() > 0) {
                return lstResponses.removeFirst();
            }
            return null;
        }
    }

    @Override
    public void run() {
        setName("GameIO");
        password = Settings.getInstance().getSetting("password");


        int adminPort = 29672;
        try {
            adminPort = Integer.parseInt(Settings.getInstance().getSetting("adminport"));
        } catch (Exception ex) {
            Logger.getLogger("cvg").warning("Invalid setting adminport. Using default value :" + adminPort);
        }

        String gameIP = "127.0.0.1";
        try {
            gameIP = Settings.getInstance().getSetting("gameip");
            if (gameIP == null) {
                throw new IllegalArgumentException();
            }
        } catch (Exception ex) {
            gameIP = "127.0.0.1";
            Logger.getLogger("cvg").warning("Invalid setting gameip. Using default value :" + gameIP);
        }
        try {
            connect(gameIP, adminPort);
            BufferedReader rdr = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // Skip the first line : COMMAND hello,connect executed on the server!
            rdr.readLine();

            String line = null;

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            
            // Start reading/writing streams, sync on lists
            while (true)
            {                                        
                GameRequest request = null;

                // Block for a new request
                while (request == null) {
                    synchronized (lstRequests) {
                        if (lstRequests.size() > 0) {
                            request = lstRequests.removeFirst();
                        }
                    }
                    sleepOneMillisec();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
                }

                while (rdr.ready())
                {
                    line = rdr.readLine();
                    Logger.getLogger("cvg").severe("Got loose response ["+line+"]");
                }
                
                sendRequest(request);

                line = rdr.readLine();
                while (line.startsWith("Server is switching"))
                {
                    Logger.getLogger("cvg").info("Server is switching map. All requests will be delayed.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    sendRequest(request);
                    line = rdr.readLine();
                }
                if (request.isCustom() && line.startsWith(" "))
                {
                    Logger.getLogger("cvg").warning("Received a playerinfo line ["+line+"] but expected response to ["+request.getRawRequest()+"]");
                    //setExpectedPlayerCount(expectedPlayerCount+1,"Increasing b/c received loose playerinfo line");
                    Logger.getLogger("cvg").warning("Re-issuing next request ["+request.getRawRequest()+"]");
                    //sendRequest(request);
                    line = rdr.readLine();
                }

                if (request.isCustom()) {
                    handleCustomResponse(request, line);
                } else {
                    // Its serverinfo or playerinfo
                    handleSPResponse(request, line, rdr);
                }
                
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
    private void sleepOneMillisec() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void handleSPResponse(GameRequest request, String line,BufferedReader rdr) throws IOException
    {           if (line.equals("NO PLAYER YET"))
                {
                    GameResponse response = new PlayerInfoResponse(new ArrayList<Player>());
                    synchronized (lstResponses) {
                        lstResponses.addLast(response);
                    }
                } else if (line.startsWith(" ")) {
                    // this is a part of a playerinfo request
                    ArrayList<Player> lstPlayers = new ArrayList<Player>();
                    Player p = null;
                    int highestNumber = 0;
                    while (true)
                    {
                        // Try to parse that line as a playerinfo line
                        try {
                            p = PlayerInfoParser.getInstance().parsePlayerInfoLine(line);

                            lstPlayers.add(p);
                        } catch (InvalidPlayerInfoLineException ex) {
                            // Oops this wasn't a playerinfo line, this should theoretically never happen!
                            Logger.getLogger("cvg").severe("This isn't a playerinfo line! ["+line+"]");
                        }
                        
                        if (!rdr.ready())
                        {
                            // Give it some time to send the next line to us
                            Logger.getLogger("cvg").info("Looks like this is the end of the playerinfo...Got " + lstPlayers.size() + " players.");
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        // Check if there is a line coming.
                        if (!rdr.ready())
                        {
                            // There's nothing coming (at least not fast enough, so break here and we'll discard any lines that come in too late)
                            break;
                        }
                        line = rdr.readLine();
                    }
                    GameResponse response = new PlayerInfoResponse(lstPlayers);
                    synchronized (lstResponses) {
                        lstResponses.addLast(response);
                    }
                    
                } else {
                    // Its a serverinfo response
                    if (line.startsWith("Host="))
                    {
                        GameResponse response = ServerInfoParser.getInstance().parseServerinfo(line);
                        ServerInfoResponse svr = (ServerInfoResponse) response;
                        synchronized (lstResponses) {
                                lstResponses.addLast(response);
                        }
                    } else {
                        Logger.getLogger("cvg").severe("This should be a serverinfo response ! Received : [" + line + "]");
                    }
                }
    }
    private void handleCustomResponse(GameRequest request, String line)
    {
        String expectedLine = "COMMAND " + request.getRawRequest() + " executed on the server!";
        if (line.equals(expectedLine)) {
            GameResponse response = new CustomResponse(expectedLine);
            synchronized (lstResponses) {
                lstResponses.addLast(response);
            }
        } else {
            GameResponse response = new CustomResponse(expectedLine);
            synchronized (lstResponses) {
                lstResponses.addLast(response);
            }
            Logger.getLogger("cvg").warning("This is not the expected custom command response command : ["+request.getRawRequest()+"] response : ["+line+"]");
        }
    }

    private void sendGlobalopsEncodedData(String data) throws IOException {
        char pw_size = (char) password.length();
        char data_size = (char) data.length();
        writer.write(pw_size + password + data_size + data + ((char) 0));
        writer.flush();
    }

    private void sendRequest(GameRequest request) {
        try {
            sendGlobalopsEncodedData(request.getRawRequest());
        } catch (IOException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }

    private void connect(String ipAddress, int port) throws UnknownHostException, IOException {
        sock = new Socket();
        InetAddress trAdr = InetAddress.getByName(ipAddress);
        try {
            InetSocketAddress isa = new InetSocketAddress(trAdr, port);
            sock.connect(isa);
        } catch (ConnectException ex) {
            Logger.getLogger("cvg").severe("Unable to connect to " + ipAddress + ":" + port);
            System.exit(1);
        }
        writer = new PrintWriter(sock.getOutputStream());
        sock.setReceiveBufferSize(70000);
        sendGlobalopsEncodedData("hello,connect");
        Logger.getLogger("cvg").info("Connected with Globalops server at " + sock.getInetAddress().getHostAddress());
    }

    public void disconnect() throws IOException {
        sock.close();
        Logger.getLogger("cvg").info("Disconnected from globalops server at " + sock.getInetAddress().getHostAddress());
    }
}
