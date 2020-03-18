package org.cdsframework.rest.opencds;

import org.opencds.config.api.model.impl.KMIdImpl;

/**
 *
 * @author sdn
 */
public class KMUpdate {
    
    private KMIdImpl kmId;
    private byte[] kmPackage;

    /**
     * Get the value of kmId
     *
     * @return the value of kmId
     */
    public KMIdImpl getKmId() {
        return kmId;
    }

    /**
     * Set the value of kmId
     *
     * @param kmId new value of kmId
     */
    public void setKmId(KMIdImpl kmId) {
        this.kmId = kmId;
    }

    /**
     * Get the value of kmPackage
     *
     * @return the value of kmPackage
     */
    public byte[] getKmPackage() {
        return kmPackage;
    }

    /**
     * Set the value of kmPackage
     *
     * @param kmPackage new value of kmPackage
     */
    public void setKmPackage(byte[] kmPackage) {
        this.kmPackage = kmPackage;
    }

}
