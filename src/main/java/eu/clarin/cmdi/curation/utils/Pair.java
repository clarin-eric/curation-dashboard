/**
 * 
 */
package eu.clarin.cmdi.curation.utils;

/**
 * @author dostojic
 *
 */
public class Pair<X, Y> {
    
    X x = null;
    Y y = null;
    
    public Pair(){}
    
    public Pair(X x, Y y) {
	super();
	this.x = x;
	this.y = y;
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

}
