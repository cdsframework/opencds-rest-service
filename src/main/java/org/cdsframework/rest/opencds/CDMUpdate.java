package org.cdsframework.rest.opencds;

import org.opencds.config.api.model.impl.CDMIdImpl;

/**
 *
 * @author sdn
 */
public class CDMUpdate {

    private byte[] cdm;

    private CDMIdImpl cdmId;

    /**
     * Get the value of cdmId
     *
     * @return the value of cdmId
     */
    public CDMIdImpl getCdmId() {
        return cdmId;
    }

    /**
     * Set the value of cdmId
     *
     * @param cdmId new value of cdmId
     */
    public void setCdmId(CDMIdImpl cdmId) {
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
    public void setCdm(byte[] cdm) {
        this.cdm = cdm;
    }
}
