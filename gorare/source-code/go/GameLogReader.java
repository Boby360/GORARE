/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import sys.util.Settings;

public class GameLogReader extends Thread {
    private static GameLogReader _this;
    private GameLogReader(){}
    private BufferedReader reader = null;
    private File currentFile;
    private String logdir = Settings.getInstance().getSetting("logdir");
    public static GameLogReader getInstance()
    {
        if (_this == null)
            _this = new GameLogReader();
        return _this;
    }
    private File getLatestFile(String baseDir)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dayString = sdf.format(Calendar.getInstance().getTime());
        File fDir = new File(baseDir);
        ArrayList<String> lstStrFiles = new ArrayList<String>();
        while (lstStrFiles.size()  == 0)
        {
            lstStrFiles.clear();
            for (File child : fDir.listFiles())
            {
                if (child.getName().startsWith(dayString))
                {
                    lstStrFiles.add(child.getAbsolutePath());
                }
            }
            if (lstStrFiles.size() == 0)
            {
                Logger.getLogger("cvg").warning("No log files found in log dir!");
                dayString = sdf.format(Calendar.getInstance().getTime());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger("cvg").severe(ex.getMessage());
                }
            }
        }

        String[] children = new String[lstStrFiles.size()];
        children = lstStrFiles.toArray(children);
        Arrays.sort(children,String.CASE_INSENSITIVE_ORDER);        
        File retFile =new File(children[children.length-1]);
        return retFile;

    }
    private void syncReader() throws FileNotFoundException, IOException
    {
        //File latFile = getLatestFile("E:\\DDL Downloads\\Go\\logs");
        File latFile = getLatestFile(logdir);
        if (currentFile == null || !currentFile.getName().equals(latFile.getName()))
        {
            Logger.getLogger("cvg").info("Detected a new log file.");
            // We need to update our reader
            if (reader != null)
                reader.close(); 
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(latFile)));
            currentFile = latFile;
            Logger.getLogger("cvg").info("Log file " + latFile.getName() + " is now in use.");
        }
    }
    @Override
    public void run()
    {
        if (!(new File(logdir).exists()))
        {
            Logger.getLogger("cvg").severe("Log dir does not exist!");
            return;
        }
        
        try {
            read();
        } catch (IOException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
    private void read() throws IOException, InterruptedException
    {     
        String line = null;
        int counter = 0;
        while (true) {
            if (reader == null)
                syncReader();

            line = reader.readLine();                   

            if (line == null)
            {
                counter++;
                if (counter == 300)
                {
                    syncReader();
                    counter = 0;
                }
                sleepOneMillisec();
            }
            else
                onMessage(line);
        }
    }
    private void sleepOneMillisec()
    {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameLogReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void onMessage(String message)
    {
        ArrayList<GameClient> lstClientsShallowCopy = (ArrayList<GameClient>) Dispatcher.getInstance().getLstClients().clone();
        
        for (GameClient gc : lstClientsShallowCopy)
            gc.onGossipMsg(message);
    }   
}
