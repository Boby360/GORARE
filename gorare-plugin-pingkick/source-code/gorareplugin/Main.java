/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gorareplugin;

import go.GameInterface;

/**
 *
 * @author Charles
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GameInterface.getInstance().connect();
        PingKicker.getInstance().start();
    }

}
