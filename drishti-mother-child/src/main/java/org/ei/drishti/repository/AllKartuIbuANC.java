package org.ei.drishti.repository;

import java.util.List;

import org.ei.drishti.common.AllConstants;
import org.ei.drishti.domain.Child;
import org.ei.drishti.domain.KartuIbuANC;
import org.ektorp.CouchDbConnector;
import org.motechproject.dao.MotechBaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class AllKartuIbuANC extends MotechBaseRepository<KartuIbuANC> {
    private static Logger logger = LoggerFactory.getLogger(AllEligibleCouples.class);
	@Autowired
    public AllKartuIbuANC(@Qualifier(AllConstants.DRISHTI_DATABASE_CONNECTOR) CouchDbConnector db) {
        super(KartuIbuANC.class, db);
    }
	public KartuIbuANC findByCaseId(String motherId) {
		 List<KartuIbuANC> kartu_ibu_ANC = queryView("by_caseId", motherId);
	        if (kartu_ibu_ANC == null || kartu_ibu_ANC.isEmpty()) {
	            return null;
	        }
	        return kartu_ibu_ANC.get(0);
	}

}
