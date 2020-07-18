package org.cdsframework.rest.opencds.pojos;

import org.opencds.config.api.model.CDMId;

/**
 *
 * @author sdn
 */
public class CDMUpdate {

    private byte[] cdm;
    private CDMId cdmId;
    private String error;

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Get the value of cdmId
     *
     * @return the value of cdmId
     */
    public CDMId getCdmId() {
        return cdmId;
    }

    /**
     * Set the value of cdmId
     *
     * @param cdmId new value of cdmId
     */
    public void setCdmId(final CDMId cdmId) {
        this.cdmId = cdmId;
    }

    /**
     * Get the value of cdm
     *
     * @return the value of cdm
     */
    public byte[] getCdm() {
        return cdm;
    }

    /**
     * Set the value of cdm
     *
     * @param cdm new value of cdm
     */
    public void setCdm(final byte[] cdm) {
        this.cdm = cdm;
    }
}
