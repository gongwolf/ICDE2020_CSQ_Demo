package cs.nmsu.edu.demo.RstarTree;

import java.lang.*;
import java.lang.Comparable;

/**
 * Integer wrappwer that implements that Comparable interface
 */
public class ComparableInteger extends Number implements Comparable
{
//--------------------------------------------------------------------------
    public static void main(String argv[])
    {
        try
        {
            ComparableInteger a = new ComparableInteger(1);
            ComparableInteger b = new ComparableInteger(2);
            ComparableInteger c = new ComparableInteger(3);
            System.out.println("Compare "+a+" and "+b+" = "+a.compareTo(b));
            System.out.println("Compare "+a+" and "+c+" = "+a.compareTo(c));
            System.out.println("Compare "+b+" and "+a+" = "+b.compareTo(a));
            System.out.println("Compare "+b+" and "+c+" = "+b.compareTo(c));
            System.out.println("Compare "+c+" and "+a+" = "+c.compareTo(a));
            System.out.println("Compare "+c+" and "+b+" = "+c.compareTo(b));
        }
        catch(Exception e)
        {
            System.out.println("ERROR:"+e.getMessage());
        }
    }
//--------------------------------------------------------------------------
    /**
     * Constructor
     */
    public ComparableInteger(int anInt) { this.myValue = anInt; }
//--------------------------------------------------------------------------
    /**
     * Implement the Comparable interface
     */
    public int compareTo(Object other)
    {
        if (!(other instanceof Number))
            return 1;
        Number you  = (Number)other;
        int myInt   = this.intValue();
        int yourInt = you.intValue();
        if      (myInt > yourInt) return  1;
        else if (myInt < yourInt) return -1;
        else                      return  0;
    }
//--------------------------------------------------------------------------
    /**
     * Implement the Number interface
     */
    public int    intValue()    { return (int)this.myValue; }
    public long   longValue()   { return (long)this.myValue; }
    public float  floatValue()  { return (float)this.myValue; }
    public double doubleValue() { return (double)this.myValue; }
//--------------------------------------------------------------------------
    /**
     * Implement Object.toString()
     */
    public String toString() { return String.valueOf(this.myValue); }
//--------------------------------------------------------------------------
    private int myValue;
//--------------------------------------------------------------------------
}
