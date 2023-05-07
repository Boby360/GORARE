/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gorarepluginrps;

import go.GameInterface;

/**
 *
 * @author Charles
 */
public class Main {
    public static void main(String[] args)
    {
        GameInterface.getInstance().connect();
        new myPlugin();
    }

}
