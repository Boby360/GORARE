/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sys.util;

public class AsciiPrinter {
    private static AsciiPrinter _this;
    private AsciiPrinter(){}
    public static AsciiPrinter getInstance()
    {
        if (_this == null)
            _this = new AsciiPrinter();
        return _this;
    }
    public void printAscii(String data)
    {
        System.out.println("-----------------");
        System.out.println("Ascii representation of ["+data+"]");
        for (int i=0; i < data.length(); i++)
        {
            System.out.println("char n°" + i + "\tvalue " + ((int)data.charAt(i)) + "\tcharachter " + data.charAt(i));
        }
    }
}
