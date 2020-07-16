package org.cdsframework.rest.opencds.pojos;

import java.util.List;

/**
 *
 * @author sdn
 */
public class UpdateResponse {

    private List<KMUpdate> kmUpdates;
    private CDMUpdate cdmUpdate;

    /**
     * Get the value of kmUpdates
     *
     * @return the value of kmUpdates
     */
    public List<KMUpdate> getKmUpdates() {
        return kmUpdates;
    }

    /**
     * Set the value of kmUpdates
     *
     * @param kmUpdates new value of kmUpdates
     */
    public void setKmUpdates(final List<KMUpdate> kmUpdates) {
        this.kmUpdates = kmUpdates;
    }

    /**
     * Get the value of cdmUpdate
     *
     * @return the value of cdmUpdate
     */
    public CDMUpdate getCdmUpdate() {
        return cdmUpdate;
    }

    /**
     * Set the value of cdmUpdate
     *
     * @param cdmUpdate new value of cdmUpdate
     */
    public void setCdmUpdate(final CDMUpdate cdmUpdate) {
        this.cdmUpdate = cdmUpdate;
    }

}
