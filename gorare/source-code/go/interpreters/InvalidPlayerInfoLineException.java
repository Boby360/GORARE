package go.interpreters;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author Charles
 */
public class InvalidPlayerInfoLineException extends Exception{

    public InvalidPlayerInfoLineException(String line,String reason) {
        super("Invalid playerinfo line! Reason : "+reason+" Line : " + line);
    }
    
}
