package gov.noaa.pmel.tmap.ferret.server.dodstype;

import dods.dap.*;

/** A factory for Ferret-DODS server-side data objects 
 *   
 *  Modified from org.iges.grads.server.dap.GradsServerFactory
 *
 * @author Richard Roger, Yonghua Wei (Yonghua.Wei@noaa.gov)
 *
 */
public class FerretTypeFactory 
    extends DefaultFactory {

    /** Returns a unique instance of this class */
    public static FerretTypeFactory getFactory() {
	if (factory == null) {
	    factory = new FerretTypeFactory();
	}
	return factory;
    }

    private static FerretTypeFactory factory;

    /** 
     * Construct a new DFloat32.
     * @return the new DFloat32
     */
    public DFloat32 newDFloat32() {
	return new GenericFloat32();
    }

    /**
     * Construct a new DFloat32 with name n.
     * @param n the variable name
     * @return the new DFloat32
     */
    public DFloat32 newDFloat32(String n) {
	return new GenericFloat32(n);
    }

    /** 
     * Construct a new DFloat64.
     * @return the new DFloat64
     */
    public DFloat64 newDFloat64() {
	return new GenericFloat64();
    }

    /**
     * Construct a new DFloat64 with name n.
     * @param n the variable name
     * @return the new DFloat64
     */
    public DFloat64 newDFloat64(String n) {
	return new GenericFloat64(n);
    }


    /** 
     * Construct a new DString.
     * @return the new DString
     */
    public DString newDString() {
	return new GenericString();
    }

    /**
     * Construct a new DString with name n.
     * @param n the variable name
     * @return the new DString
     */
    public DString newDString(String n) {
	return new GenericString(n);
    }

    /** 
     * Construct a new DInt32.
     * @return the new DInt32
     */
    public DInt32 newDInt32() {
	return new GenericInt32();
    }

    /**
     * Construct a new DInt32 with name n.
     * @param n the variable name
     * @return the new DInt32
     */
    public DInt32 newDInt32(String n) {
	return new GenericInt32(n);
    }

    /** 
     * Construct a new DArray.
     * @return the new DArray
     */
    public DArray newDArray() {
	return new FerretArray();
    }

    /**
     * Construct a new DArray with name n.
     * @param n the variable name
     * @return the new DArray
     */
    public DArray newDArray(String n) {
	return new FerretArray(n);
    }

    /** 
     * Construct a new DGrid.
     * @return the new DGrid
     */
    public DGrid newDGrid() {
	return new FerretGrid();
    }

    /**
     * Construct a new DGrid with name n.
     * @param n the variable name
     * @return the new DGrid
     */
    public DGrid newDGrid(String n) {
	return new FerretGrid(n);
    }

    /** 
     * Construct a new DSequence.
     * @return the new DSequence
     */
    public DSequence newDSequence() {
	return new FerretSequence();
    }

    /**
     * Construct a new DSequence with name n.
     * @param n the variable name
     * @return the new DSequence
     */
    public DSequence newDSequence(String n) {
	return new FerretSequence(n);
    }
}

