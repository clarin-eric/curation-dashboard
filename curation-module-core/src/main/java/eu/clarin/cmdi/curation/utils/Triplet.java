/**
 * 
 */
package eu.clarin.cmdi.curation.utils;

/**
 * @author dostojic
 *
 */
public class Triplet<X, Y, Z> {
    
    X x = null;
    Y y = null;
    Z z = null;
    
    
    public Triplet(){}
    
    public Triplet(X x, Y y, Z z) {
	super();
	this.x = x;
	this.y = y;
	this.z = z;
    }

    public X getX() {
        return x;
    }


    public void setX(X x) {
        this.x = x;
    }


    public Y getY() {
        return y;
    }


    public void setY(Y y) {
        this.y = y;
    }


    public Z getZ() {
        return z;
    }


    public void setZ(Z z) {
        this.z = z;
    }
}
