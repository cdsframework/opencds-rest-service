package org.cdsframework.rest.opencds.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.rest.opencds.KMUpdate;
import org.cdsframework.rest.opencds.MarshalUtils;
import org.cdsframework.rest.opencds.UpdateResponse;
import org.opencds.config.api.ConfigurationService;
import org.opencds.config.api.model.CDMId;
import org.opencds.config.api.model.KMId;
import org.opencds.config.api.model.KMStatus;
import org.opencds.config.api.model.PluginId;
import org.opencds.config.api.model.SecondaryCDM;
import org.opencds.config.api.model.TraitId;
import org.opencds.config.api.model.impl.CDMIdImpl;
import org.opencds.config.api.model.impl.KnowledgeModuleImpl;
import org.opencds.config.api.model.impl.SSIdImpl;
import org.opencds.config.mapper.ConceptDeterminationMethodMapper;
import org.opencds.config.schema.ConceptDeterminationMethod;
import org.opencds.config.schema.ConceptDeterminationMethods;

/**
 *
 * @author sdn
 */
public class ConfigUtils {

    private static final Log log = LogFactory.getLog(ConfigUtils.class);

    /**
     * Processes a cdm and km update.
     *
     * @param updateResponse
     * @param configurationService
     * @throws JAXBException
     * @throws TransformerException
     */
    public static void update(UpdateResponse updateResponse, ConfigurationService configurationService) throws JAXBException, TransformerException {
        final String METHODNAME = "update ";
        log.debug(METHODNAME + "updateResponse.getCdmUpdate().getCode(): " + updateResponse.getCdmUpdate().getCdmId().getCode());
        log.debug(METHODNAME + "updateResponse.getCdmUpdate().getCodeSystem(): " + updateResponse.getCdmUpdate().getCdmId().getCodeSystem());
        log.debug(METHODNAME + "updateResponse.getCdmUpdate().getVersion(): " + updateResponse.getCdmUpdate().getCdmId().getVersion());
        log.debug(METHODNAME + "updateResponse.getCdmUpdate().getCdm() length: " + updateResponse.getCdmUpdate().getCdm().length);
        if (updateResponse.getCdmUpdate().getCdm() != null && updateResponse.getCdmUpdate().getCdm().length > 0) {
            ConceptDeterminationMethods cdms = MarshalUtils.unmarshal(
                    new ByteArrayInputStream(updateResponse.getCdmUpdate().getCdm()),
                    ConceptDeterminationMethods.class
            );
            CDMId cdmId = updateResponse.getCdmUpdate().getCdmId();
            if (cdms.getConceptDeterminationMethod() == null) {
                throw new IllegalStateException("cdms.getConceptDeterminationMethod() is null!");
            }
            if (cdms.getConceptDeterminationMethod().isEmpty()) {
                throw new IllegalStateException("cdms.getConceptDeterminationMethod() is empty!");
            }
            if (cdms.getConceptDeterminationMethod().size() > 1) {
                throw new IllegalStateException("cdms.getConceptDeterminationMethod() is only allow to have one cdm!");
            }
            ConceptDeterminationMethod cdm = cdms.getConceptDeterminationMethod().get(0);
            if (cdm == null) {
                throw new IllegalStateException("cdm is null!");
            }
            ConfigUtils.updateConceptDeterminationMethod(cdmId, cdm, configurationService);
        }
        for (KMUpdate kmUpdate : updateResponse.getKmUpdates()) {
            log.debug(METHODNAME + "kmUpdate.getKmId().getScopingEntityId(): " + kmUpdate.getKmId().getScopingEntityId());
            log.debug(METHODNAME + "kmUpdate.getKmId().getBusinessId(): " + kmUpdate.getKmId().getBusinessId());
            log.debug(METHODNAME + "kmUpdate.getKmId().getVersion(): " + kmUpdate.getKmId().getVersion());
            log.debug(METHODNAME + "kmUpdate.getKm() length: " + kmUpdate.getKmPackage().length);
            KMId kmId = kmUpdate.getKmId();
            InputStream kmPackageInputStream = new ByteArrayInputStream(kmUpdate.getKmPackage());
            ConfigUtils.updateKnowledgeModulePackage(kmId, kmPackageInputStream, configurationService);
        }
    }

    /**
     * Updates the supplied knowledge module package.
     *
     * @param kmId
     * @param knowledgePackage
     * @param configurationService
     */
    public static void updateKnowledgeModulePackage(KMId kmId, InputStream knowledgePackage, ConfigurationService configurationService) {
        final String METHODNAME = "updateKmp ";
        boolean created = false;
        if (configurationService.getKnowledgeRepository().getKnowledgeModuleService().find(kmId) == null) {
            created = true;
            KnowledgeModuleImpl knowledgeModule = KnowledgeModuleImpl.create(
                    kmId,
                    KMStatus.APPROVED,
                    "org.opencds.service.drools.v55.DroolsAdapter",
                    SSIdImpl.create("org.opencds.vmr", "VMR", "1.0"),
                    CDMIdImpl.create("2.16.840.1.113883.3.795.5.4.12.5.1", "RCKMS0000", "1.0"),
                    new ArrayList<SecondaryCDM>(),
                    "PKG",
                    kmId.getScopingEntityId() + "^" + kmId.getBusinessId() + "^" + kmId.getVersion() + ".pkg",
                    true,
                    "RCKMSReportingProcess",
                    new ArrayList<TraitId>(),
                    new ArrayList<PluginId>(),
                    new ArrayList<PluginId>(),
                    new Date(),
                    "system");
            configurationService.getKnowledgeRepository().getKnowledgeModuleService().persist(knowledgeModule);
        }
        configurationService.getKnowledgeRepository().getKnowledgeModuleService().persistKnowledgePackage(kmId, knowledgePackage);
        log.info(METHODNAME + (created ? "created: " : "updated: ") + kmId.getScopingEntityId() + " - " + kmId.getBusinessId() + " - " + kmId.getVersion());
    }

    /**
     * Updates the supplied concept determination method.
     *
     * @param cdmId
     * @param cdm
     * @param configurationService
     */
    public static void updateConceptDeterminationMethod(CDMId cdmId, ConceptDeterminationMethod cdm, ConfigurationService configurationService) {
        final String METHODNAME = "updateCdm ";
        org.opencds.config.api.model.ConceptDeterminationMethod cdmInternal = ConceptDeterminationMethodMapper.internal(cdm);
        if (!cdmId.equals(cdmInternal.getCDMId())) {
            throw new IllegalStateException("CDMId of request and document do not match");
        }
        boolean created = false;
        if (configurationService.getKnowledgeRepository().getConceptDeterminationMethodService().find(cdmInternal.getCDMId()) == null) {
            created = true;
        }
        configurationService.getKnowledgeRepository().getConceptDeterminationMethodService().persist(cdmInternal);
        log.info(METHODNAME + (created ? "created: " : "updated: ") + cdmId.getCodeSystem() + " - " + cdmId.getCodeSystem() + " - " + cdmId.getVersion());
    }

}
