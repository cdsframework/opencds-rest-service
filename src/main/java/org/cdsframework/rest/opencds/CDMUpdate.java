/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdsframework.rest.opencds;

/**
 *
 * @author sdn
 */
public class CDMUpdate {
    
    private byte[] cdm;

    private String code;

    private String oid;

    /**
     * Get the value of oid
     *
     * @return the value of oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * Set the value of oid
     *
     * @param oid new value of oid
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * Get the value of code
     *
     * @return the value of code
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the value of code
     *
     * @param code new value of code
     */
    public void setCode(String code) {
        this.code = code;
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
