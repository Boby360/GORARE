/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.filters;

import go.Filter;
import go.GameRequest;
import go.GameResponse;
import go.server.RemoteClient;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import sys.util.PerformanceTimer;
import sys.util.Settings;


/**
 *
 * @author Charles
 */
public class GRProtocolFilter extends Filter{
    private BufferedInputStream in;
    private PrintWriter out;
    private String leftOverChunk;
    private RemoteClient client;
    private boolean gossipEnabled = false;
    
    public GRProtocolFilter(RemoteClient client,BufferedInputStream in, PrintWriter out,String leftOverChunk) {
        this.in = in;
        this.out = out;
        this.leftOverChunk = leftOverChunk;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            doRun();
        } catch (IOException ex) {
            if (ex instanceof SocketException)
            {
                if (ex.getMessage().contains("Connection reset"))
                {
                    client.drop();
                    return;
                }
            }
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
    private void writeToClient(String message)
    {
        Logger.getLogger("cvg").info("Written [" + message + "] to some GR-Protocol based client.");
        out.write(message);
        out.flush();
    }
    private void doRun() throws IOException
    {
            byte[] buff = new byte[512];

            StringBuilder sb = new StringBuilder(leftOverChunk);

            int bytesRead = 0;

            while ((bytesRead = in.read(buff)) != -1) {
                sb.append(new String(buff, 0, bytesRead));
                int indexOfDelimiter = sb.toString().indexOf((char) 0);
                if (indexOfDelimiter != -1)
                {
                    String rawRequest = sb.toString().substring(0,indexOfDelimiter);
                    sb.delete(0, indexOfDelimiter+1);
                    runRawRequest(rawRequest);
                }
            }
            client.drop();
    }
    private void runRawRequest(String rawRequest)
    {
        if (rawRequest.startsWith("GOSSIP|"))
        {
            if (rawRequest.equals("GOSSIP|start"))
            {
                Logger.getLogger("cvg").info("Some client is now reading live logs.");
                gossipEnabled = true;
            } else if (rawRequest.equals("GOSSIP|stop"))
            {
                Logger.getLogger("cvg").info("Some client stopped reading live logs.");
                gossipEnabled = false;
            }
            return;
        }
        if (rawRequest.startsWith("GORARE|"))
        {
            if (rawRequest.equals("GORARE|cachetime"))
            {
                writeToClient("GORARE|"+Settings.getInstance().getSetting("maxcachetime") + (char)0);
            }
            return;
        }

        Logger.getLogger("cvg").info("Client says ["+rawRequest+"]");
        GameRequest request = new GameRequest(rawRequest);
        GameResponse response = super.doRequest(request);        
        writeToClient(response.toGRResponse());
    }

    @Override
    public void onGossipMessage(String message) {
        if (gossipEnabled)
        {
            writeToClient("GOSSIP|"+message + (char) 0);
        }
    }

}
