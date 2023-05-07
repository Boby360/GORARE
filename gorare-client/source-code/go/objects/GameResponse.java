package go.objects;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Charles
 */
public abstract class GameResponse {
    /**
     * @return Returns the raw representation of this response, in GO protocol format
     */
    public abstract String toRawResponse();

}
