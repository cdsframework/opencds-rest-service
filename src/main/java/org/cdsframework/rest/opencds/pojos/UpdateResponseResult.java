package org.cdsframework.rest.opencds.pojos;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sdn
 */
public class UpdateResponseResult {

    private List<KMUpdateResult> kms;
    private CDMUpdateResult cdm;

    /**
     * Get the value of kms
     *
     * @return the value of kms
     */
    public List<KMUpdateResult> getKms() {
        return kms;
    }

    /**
     * Set the value of kms
     *
     * @param kms new value of kms
     */
    public void setKms(List<KMUpdateResult> kms) {
        this.kms = kms;
    }

    /**
     * Get the value of cdm
     *
     * @return the value of cdm
     */
    public CDMUpdateResult getCdm() {
        return cdm;
    }

    /**
     * Set the value of cdm
     *
     * @param cdm new value of cdm
     */
    public void setCdm(CDMUpdateResult cdm) {
        this.cdm = cdm;
    }

}
