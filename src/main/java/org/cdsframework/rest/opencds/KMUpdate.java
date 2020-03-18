package org.cdsframework.rest.opencds;

import org.omg.dss.common.EntityIdentifier;

/**
 *
 * @author sdn
 */
public class KMUpdate {
    
    private EntityIdentifier entityIdentifier;
    private byte[] kmPackage;

    /**
     * Get the value of entityIdentifier
     *
     * @return the value of entityIdentifier
     */
    public EntityIdentifier getEntityIdentifier() {
        return entityIdentifier;
    }

    /**
     * Set the value of entityIdentifier
     *
     * @param entityIdentifier new value of entityIdentifier
     */
    public void setEntityIdentifier(EntityIdentifier entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
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
