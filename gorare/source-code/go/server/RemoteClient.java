/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.server;

import go.Filter;
import go.filters.GOProtocolFilter;
import go.filters.GRProtocolFilter;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import sys.util.AsciiPrinter;
import sys.util.Settings;

/**
 *
 * @author Charles
 */
public class RemoteClient extends Thread {
    private Socket socket;
    private boolean isAuthenticated = false;
    private Filter filter = null;
    private String password;
    public RemoteClient(Socket s) {
        this.socket = s;
    }
    /**
     * Start reading the inputstream, find out what type of client it is,
     * if the client hasn't tried to authenticate within 5 seconds, drop it.
     * After the client type has been determined and authenticated, start the
     * appropriate filter that will handle this client.
     */
    @Override
    public void run()
    {
        observe();

        password = Settings.getInstance().getSetting("password");
        if (password == null)
        {
            Logger.getLogger("cvg").severe("Setting password not specified in settings.cfg. Exiting...");
            System.exit(1);
        }
        
        try {
            doRun();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(RemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            if (ex instanceof SocketException)
            {
                if (ex.getMessage().contains("Connection reset"))
                {
                    drop();
                    return;
                }
            }
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
    private void doRun() throws UnsupportedEncodingException, IOException
    {
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")));

            byte[] buff = new byte[512];

            StringBuilder sb = new StringBuilder();
            
            int bytesRead = 0;

            while ((bytesRead = in.read(buff)) != -1) {
                sb.append(new String(buff, 0, bytesRead));
                if (sb.toString().indexOf(((char) 0 )) != -1)
                    break;
            }
            String contents = sb.toString();
            int indexOfDelimiter = contents.indexOf(((char) 0 ));

            attemptAuthentication(contents.substring(0,indexOfDelimiter+1), in, out,  contents.substring(indexOfDelimiter+1));
    }
    /**
     * Attempts to recognize and authenticate this client
     */
    private void attemptAuthentication(String firstLine,BufferedInputStream in, PrintWriter out,String leftOverChunk)
    {        
        if (firstLine.substring(1).startsWith(password))
        {
            Logger.getLogger("cvg").info("A new GO-Protocol client has authenticated.");
            isAuthenticated = true;
            out.print("COMMAND hello,connect executed on the server!" + (char)0);
            out.flush();
            filter = new GOProtocolFilter(this, in, out, leftOverChunk);
            filter.start();
        } else if (firstLine.startsWith("gorare") && firstLine.endsWith(password + (char) 0))
        {
                Logger.getLogger("cvg").info("A new GR-Protocol client has authenticated.");
                isAuthenticated = true;
                out.print("OK" + (char)0);
                out.flush();
                filter = new GRProtocolFilter(this, in, out, leftOverChunk);
                filter.start();
        } else {
            Logger.getLogger("cvg").info("Client authentication failed. ["+firstLine+"]");
            drop();
        }
        //AsciiPrinter.getInstance().printAscii(firstLine);
    }
    /**
     * 
     */
    private void observe()
    {
        new Thread("RemoteClient-observer")
        {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RemoteClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!isAuthenticated)
                {
                    drop();
                    Logger.getLogger("cvg").info("Dropped unresponsive RemoteClient socket " + socket);
                }
            }
        };

    }
    /**
     * Drops this RemoteClient
     * The filter will be unregistered from the dispatcher and
     * the socket associated with this client will be closed
     * (which in turn shuts down the streams)
     */
    public void drop() {
        try {
            if (filter != null)
            filter.unRegisterFromDispatcher();
            socket.close();
            Logger.getLogger("cvg").info("Dropped a client");
        } catch (IOException ex) {
            Logger.getLogger("cvg").severe("Unable to shut down unresponsive RemoteClient socket : " + socket);
        }
    }
}

