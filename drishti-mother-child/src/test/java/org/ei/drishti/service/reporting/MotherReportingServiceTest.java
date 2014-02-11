package org.ei.drishti.service.reporting;

import org.ei.drishti.common.domain.Indicator;
import org.ei.drishti.common.domain.ReportMonth;
import org.ei.drishti.common.domain.ReportingData;
import org.ei.drishti.domain.EligibleCouple;
import org.ei.drishti.domain.Location;
import org.ei.drishti.domain.Mother;
import org.ei.drishti.repository.AllEligibleCouples;
import org.ei.drishti.repository.AllMothers;
import org.ei.drishti.util.SafeMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.testing.utils.BaseUnitTest;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.ei.drishti.common.domain.Indicator.*;
import static org.ei.drishti.common.domain.ReportingData.anmReportData;
import static org.ei.drishti.common.domain.ReportingData.serviceProvidedData;
import static org.ei.drishti.common.util.EasyMap.create;
import static org.ei.drishti.common.util.EasyMap.mapOf;
import static org.joda.time.LocalDate.parse;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class MotherReportingServiceTest extends BaseUnitTest {
    @Mock
    private ReportingService reportingService;
    @Mock
    private AllMothers allMothers;
    @Mock
    private AllEligibleCouples allEligibleCouples;
    @Mock
    private ReportMonth reportMonth;

    private MotherReportingService service;

    private final Mother MOTHER = new Mother("CASE-1", "EC-CASE-1", "TC 1")
            .withAnm("ANM X")
            .withLMP(parse("2012-01-01"));

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        service = new MotherReportingService(reportingService, allMothers, allEligibleCouples, reportMonth);
    }

    @Test
    public void shouldReportANCRegistrationWhichHasBeenDoneWithinTwelveWeeksOfLMP() throws Exception {
        assertThatIndicatorIsSetBasedOnLMP("2011-10-11", ANC_BEFORE_12_WEEKS);
        assertThatIndicatorIsSetBasedOnLMP("2011-10-11", ANC);
    }

    @Test
    public void shouldReportANCRegistrationWhichHasBeenDoneAfterExactlyTwelveWeeksAfterLMPAsLessThanTwelve() throws Exception {
        assertThatIndicatorIsSetBasedOnLMP("2011-10-09", ANC_BEFORE_12_WEEKS);
        assertThatIndicatorIsSetBasedOnLMP("2011-10-09", ANC);
    }

    @Test
    public void shouldReportANCRegistrationWhichHasBeenDoneAfterTwelveWeeksLMPAsLateRegistration() throws Exception {
        SafeMap reportData = new SafeMap();
        reportData.put("referenceDate", "2011-10-08");
        reportData.put("motherId", "CASE-1");
        reportData.put("registrationDate", "2012-01-01");
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.registerANC(reportData);

        verifyNoReportingCalls(ANC_BEFORE_12_WEEKS, "2012-01-01");
        verifyBothReportingCalls(ANC_AFTER_12_WEEKS, "2012-01-01");
    }

    @Test
    public void shouldReportANCRegistrationWhichHasBeenDoneBeforeTwelveWeeksLMPAsEarlyRegistration() throws Exception {
        SafeMap reportData = new SafeMap();
        reportData.put("referenceDate", "2011-10-08");
        reportData.put("motherId", "CASE-1");
        reportData.put("registrationDate", "2011-12-15");
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.registerANC(reportData);

        verifyBothReportingCalls(ANC_BEFORE_12_WEEKS, "2011-12-15");
        verifyNoReportingCalls(ANC_AFTER_12_WEEKS, "2011-12-15");
    }

    @Test
    public void shouldReportMTPIndicatorBasedOnMTPTime() throws Exception {
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.closeANC(reportDataForANCClose("greater_12wks", "spontaneous_abortion", null));

        verifyBothReportingCalls(MTP_GREATER_THAN_12_WEEKS, "2012-12-12");
    }

    @Test
    public void shouldReportMaternalDeath() throws Exception {
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.closeANC(reportDataForANCClose("", "death_of_woman", "yes"));

        verifyBothReportingCalls(MMA, "2012-12-12");
        verifyBothReportingCalls(MOTHER_MORTALITY, "2012-12-12");
    }

    @Test
    public void shouldNotReportMaternalDeathIfItIsNot() throws Exception {
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.closeANC(reportDataForANCClose("", "death_of_woman", "no"));

        verifyNoReportingCalls(MMA, "2012-12-12");
    }

    @Test
    public void shouldNotReportIfReasonIsNotDeath() throws Exception {
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.closeANC(reportDataForANCClose("greater_12wks", null, null));

        verifyNoReportingCalls(MOTHER_MORTALITY, "2012-01-01");
    }

    @Test
    public void shouldReportIfReasonIsSpontaneousAbortion() throws Exception {
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.closeANC(reportDataForANCClose("", "spontaneous_abortion", null));

        verifyBothReportingCalls(SPONTANEOUS_ABORTION, "2012-12-12");
    }

    @Test
    public void shouldReportTT1ProvidedIfTTDoseIsTT1() {
        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ttDose", "tt1");
        reportData.put("ttDate", "2012-01-23");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ttProvided(reportData);

        verifyBothReportingCalls(TT1, "2012-01-23");
    }

    @Test
    public void shouldReportTT2AndSUBTTProvidedIfTTDoseIsTT2() {
        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ttDose", "tt2");
        reportData.put("ttDate", "2012-01-23");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ttProvided(reportData);

        verifyBothReportingCalls(TT2, "2012-01-23");
        verifyBothReportingCalls(SUB_TT, "2012-01-23");
    }

    @Test
    public void shouldReportTTBAndSUBTTProvidedIfTTDoseIsTTBooster() {
        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ttDose", "ttbooster");
        reportData.put("ttDate", "2012-01-23");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ttProvided(reportData);

        verifyBothReportingCalls(TTB, "2012-01-23");
        verifyBothReportingCalls(SUB_TT, "2012-01-23");
    }

    @Test
    public void shouldReportANC4ProvidedWhenANCVisitIsAfter36WeeksAndAlready3ANCVisitsHappened() {
        String visitDate = parse("2012-01-01").plusWeeks(36).toString();
        Map<String, String> ancVisit1 = create("ancVisitDate", "2012-03-04")
                .put("ancVisitNumber", "1")
                .map();
        Map<String, String> ancVisit2 = create("ancVisitDate", "2012-05-04")
                .put("ancVisitNumber", "2")
                .map();
        Map<String, String> ancVisit3 = create("ancVisitDate", "2012-07-04")
                .put("ancVisitNumber", "3")
                .map();
        Map<String, String> ancVisit4 = create("ancVisitDate", visitDate)
                .put("ancVisitNumber", "4")
                .map();
        List<Map<String, String>> ancVisits = asList(ancVisit1, ancVisit2, ancVisit4, ancVisit3);
        Mother mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withLMP(parse("2012-01-01"))
                .withANCVisits(ancVisits);

        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ancVisitDate", visitDate);
        reportData.put("ancVisitNumber", "4");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ancVisit(reportData);

        verifyBothReportingCalls(ANC4, visitDate);
    }

    @Test
    public void shouldNotReportANC4WhenANCVisitHappenedIs4ButDoesNotHave3ANCVisitsAlready() {
        String visitDate = parse("2012-01-01").plusWeeks(36).toString();

        Map<String, String> ancVisit4 = create("ancVisitDate", visitDate)
                .put("ancVisitNumber", "4")
                .map();
        List<Map<String, String>> ancVisits = asList(ancVisit4);
        Mother mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withLMP(parse("2012-01-01"))
                .withANCVisits(ancVisits);
        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ancVisitDate", visitDate);
        reportData.put("ancVisitNumber", "4");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ancVisit(reportData);

        verifyNoReportingCalls(ANC4, visitDate);

        Map<String, String> ancVisit1 = create("ancVisitDate", "2012-03-04")
                .put("ancVisitNumber", "1")
                .map();
        Map<String, String> ancVisit2 = create("ancVisitDate", "2012-05-04")
                .put("ancVisitNumber", "2")
                .map();
        ancVisits = asList(ancVisit1, ancVisit2, ancVisit4);

        mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withLMP(parse("2012-01-01"))
                .withANCVisits(ancVisits);

        reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ancVisitDate", visitDate);
        reportData.put("ancVisitNumber", "4");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ancVisit(reportData);

        verifyNoReportingCalls(ANC4, visitDate);
    }

    @Test
    public void shouldNotReportANC4WhenANCVisitIsNotFourthVisit() {
        Map<String, String> ancVisit1 = create("ancVisitDate", "2012-03-04")
                .put("ancVisitNumber", "1")
                .map();
        Map<String, String> ancVisit2 = create("ancVisitDate", "2012-05-04")
                .put("ancVisitNumber", "2")
                .map();
        Map<String, String> ancVisit3 = create("ancVisitDate", "2012-07-04")
                .put("ancVisitNumber", "3")
                .map();
        List<Map<String, String>> ancVisits = asList(ancVisit1, ancVisit2, ancVisit3);

        Mother mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withLMP(parse("2012-01-01"))
                .withANCVisits(ancVisits);

        String visitDate = parse("2012-01-01").plusWeeks(36).toString();
        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ancVisitDate", visitDate);
        reportData.put("ancVisitNumber", "3");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ancVisit(reportData);

        verifyNoReportingCalls(ANC4, visitDate);
    }

    @Test
    public void shouldNotReportANC4WhenANCVisitIsBefore36Weeks() {
        String visitDate = parse("2012-01-01").plusWeeks(36).minusDays(1).toString();
        Map<String, String> ancVisit1 = create("ancVisitDate", "2012-03-04")
                .put("ancVisitNumber", "1")
                .map();
        Map<String, String> ancVisit2 = create("ancVisitDate", "2012-05-04")
                .put("ancVisitNumber", "2")
                .map();
        Map<String, String> ancVisit3 = create("ancVisitDate", "2012-07-04")
                .put("ancVisitNumber", "3")
                .map();
        Map<String, String> ancVisit4 = create("ancVisitDate", visitDate)
                .put("ancVisitNumber", "4")
                .map();
        List<Map<String, String>> ancVisits = asList(ancVisit1, ancVisit2, ancVisit3, ancVisit4);

        Mother mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withLMP(parse("2012-01-01"))
                .withANCVisits(ancVisits);

        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ancVisitDate", visitDate);
        reportData.put("ancVisitNumber", "4");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ancVisit(reportData);

        verifyNoReportingCalls(ANC4, visitDate);
    }

    @Test
    public void shouldNotReportANC4WhenVisitNumberIsInvalid() {
        String visitDate = parse("2012-01-01").plusWeeks(36).toString();
        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("ancVisitDate", visitDate);
        reportData.put("ancVisitNumber", "");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.ancVisit(reportData);

        verifyNoReportingCalls(ANC4, visitDate);
    }

    @Test
    public void shouldReportDeliveryWhenDeliveryOutcomeIsUpdatedWithOutcome() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "yes")
                .put("didMotherSurvive", "yes")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "home")
                .put("deliveryType", "normal")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(DELIVERY, "2012-01-01");
        verifyBothReportingCalls(LIVE_BIRTH, "2012-01-01");
        verifyBothReportingCalls(NRHM_LIVE_BIRTH, "2012-01-01");
    }

    @Test
    public void shouldReportInstitutionalDeliveryAndNotReportLiveBirthWhenPlaceOfDeliveryIsNotHome() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "yes")
                .put("didMotherSurvive", "yes")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "phc")
                .put("deliveryType", "normal")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(INSTITUTIONAL_DELIVERY, "2012-01-01");
        verifyBothReportingCalls(LIVE_BIRTH, "2012-01-01");
        verifyNoReportingCalls(NRHM_LIVE_BIRTH, "2012-01-01");
    }

    @Test
    public void shouldNotReportInstitutionalDeliveryAndReportLiveBirthAndNRHMLiveBirthWhenPlaceOfDeliveryIsHome() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "yes")
                .put("deliveryOutcome", "live_birth")
                .put("didMotherSurvive", "yes")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "home")
                .put("deliveryType", "normal")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyNoReportingCalls(INSTITUTIONAL_DELIVERY, "2012-01-01");
        verifyBothReportingCalls(LIVE_BIRTH, "2012-01-01");
        verifyBothReportingCalls(NRHM_LIVE_BIRTH, "2012-01-01");
    }

    @Test
    public void shouldReportLiveBirthAndNRHMLiveBirthWhenDeliveryOutcomeIsUpdatedWithOutcomeAsLiveBirthAndDeliveryPlaceIsSubCenter() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "yes")
                .put("didMotherSurvive", "yes")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "subcenter")
                .put("deliveryType", "normal")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(LIVE_BIRTH, "2012-01-01");
        verifyBothReportingCalls(NRHM_LIVE_BIRTH, "2012-01-01");
    }

    @Test
    public void shouldReportStillBirthAndNRHMStillBirthWhenDeliveryOutcomeIsUpdatedWithOutcomeAsStillBirthAndDeliveryPlaceIsSubCenter() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "yes")
                .put("didMotherSurvive", "yes")
                .put("deliveryOutcome", "still_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "subcenter")
                .put("deliveryType", "normal")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(STILL_BIRTH, "2012-01-01");
        verifyBothReportingCalls(NRHM_STILL_BIRTH, "2012-01-01");
    }

    @Test
    public void shouldReportMotherDeathDuringPregnancyOutcome() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didMotherSurvive", "no")
                .put("didWomanSurvive", "")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "home")
                .put("deliveryType", "normal")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(MMD, "2012-01-01");
        verifyBothReportingCalls(MOTHER_MORTALITY, "2012-01-01");
    }

    @Test
    public void shouldReportWomanDeathDuringPregnancyOutcome() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "no")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "home")
                .put("deliveryType", "normal")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(MMD, "2012-01-01");
        verifyBothReportingCalls(MOTHER_MORTALITY, "2012-01-01");
    }

    @Test
    public void shouldReportCesareanWhenDeliveryOutcomeIsHandledAndDeliveryTypeIsCesarean() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "no")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "home")
                .put("deliveryType", "cesarean").map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(CESAREAN, "2012-01-01");
    }

    @Test
    public void shouldReportCesareanPrivateFacilityWhenDeliveryOutcomeIsHandledAndDeliveryTypeIsCesareanAndDeliveryPlaceIsPrivateFacility() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "yes")
                .put("didMotherSurvive", "yes")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "private_facility")
                .put("deliveryType", "cesarean").map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(CESAREAN_PRIVATE_FACILITY, "2012-01-01");
    }

    @Test
    public void shouldReportCesareanGovernmentFacilityWhenDeliveryOutcomeIsHandledAndDeliveryTypeIsCesareanAndDeliveryPlaceIsNotPrivateFacility() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("didWomanSurvive", "yes")
                .put("didMotherSurvive", "yes")
                .put("deliveryOutcome", "live_birth")
                .put("referenceDate", "2012-01-01")
                .put("deliveryPlace", "phc")
                .put("deliveryType", "cesarean")
                .map();
        service.deliveryOutcome(new SafeMap(reportData));

        verifyBothReportingCalls(CESAREAN_GOVERNMENT_FACILITY, "2012-01-01");
    }

    @Test
    public void shouldReportCesareanWhenOAPNCIsRegisteredAndDeliveryTypeIsCesarean() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("motherId", "entity id 1")
                .put("referenceDate", "2012-01-01")
                .put("deliveryType", "cesarean")
                .put("deliveryPlace", "phc")
                .put("deliveryOutcome", "live_birth")
                .map();
        service.pncRegistrationOA(new SafeMap(reportData));

        verifyBothReportingCalls(CESAREAN, "2012-01-01");
        verifyBothReportingCalls(DELIVERY, "2012-01-01");
    }

    @Test
    public void shouldReportCesareanPrivateFacilityWhenOAPNCIsRegisteredAndDeliveryTypeIsCesareanAndDeliveryPlaceIsPrivateFacility() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("motherId", "entity id 1")
                .put("referenceDate", "2012-01-01")
                .put("deliveryType", "cesarean")
                .put("deliveryPlace", "private_facility")
                .put("deliveryOutcome", "live_birth")
                .map();
        service.pncRegistrationOA(new SafeMap(reportData));

        verifyBothReportingCalls(CESAREAN_PRIVATE_FACILITY, "2012-01-01");
    }

    @Test
    public void shouldReportCesareanGovernmentFacilityWhenOAPNCIsRegisteredAndDeliveryTypeIsCesareanAndPlaceNotPrivateFacility() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("motherId", "entity id 1")
                .put("referenceDate", "2012-01-01")
                .put("deliveryType", "cesarean")
                .put("deliveryPlace", "phc")
                .put("deliveryOutcome", "live_birth")
                .map();
        service.pncRegistrationOA(new SafeMap(reportData));

        verifyBothReportingCalls(CESAREAN_GOVERNMENT_FACILITY, "2012-01-01");
    }

    @Test
    public void shouldReportLiveBirthAndNRHMLiveBirthWhenOAPNCIsRegisteredWithDeliveryOutcomeAsLiveBirth() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("motherId", "entity id 1")
                .put("referenceDate", "2012-01-01")
                .put("deliveryOutcome", "live_birth")
                .put("deliveryPlace", "home")
                .put("deliveryType", "normal")
                .map();
        service.pncRegistrationOA(new SafeMap(reportData));

        verifyBothReportingCalls(LIVE_BIRTH, "2012-01-01");
        verifyBothReportingCalls(NRHM_LIVE_BIRTH, "2012-01-01");
    }

    @Test
    public void shouldReportPlaceOfDeliveryDuringPregnancyOutcome() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        testPlaceOfDeliveryIsReported("home", D_HOM);
        testPlaceOfDeliveryIsReported("subcenter", D_SC);
        testPlaceOfDeliveryIsReported("phc", D_PHC);
        testPlaceOfDeliveryIsReported("chc", D_CHC);
        testPlaceOfDeliveryIsReported("sdh", D_SDH);
        testPlaceOfDeliveryIsReported("dh", D_DH);
        testPlaceOfDeliveryIsReported("private_facility", D_PRI);
    }

    @Test
    public void shouldReportMotherDeathDuringPNCClose() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER.withDeliveryOutCome("2012-01-01"));
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("closeReason", "death_of_mother")
                .put("isMaternalDeath", "yes")
                .put("deathDate", "2012-02-01")
                .map();
        service.closePNC(new SafeMap(reportData));

        verifyBothReportingCalls(MMP, "2012-02-01");
        verifyBothReportingCalls(MOTHER_MORTALITY, "2012-02-01");
    }

    @Test
    public void shouldNotReportMotherDeathDuringPNCCloseIfCloseReasonIsNotDeath() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER.withDeliveryOutCome("2012-01-01"));
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("closeReason", "")
                .put("isMaternalDeath", "")
                .put("deathDate", "2012-02-01")
                .map();
        service.closePNC(new SafeMap(reportData));

        verifyNoReportingCalls(MMP, "2012-02-01");
        verifyNoReportingCalls(MOTHER_MORTALITY, "2012-02-01");
    }

    @Test
    public void shouldNotReportMotherDeathDuringPNCCloseIfNotMaternalDeath() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER.withDeliveryOutCome("2012-01-01"));
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("closeReason", "death_of_mother")
                .put("isMaternalDeath", "")
                .put("deathDate", "2012-02-01")
                .map();
        service.closePNC(new SafeMap(reportData));

        verifyNoReportingCalls(MMP, "2012-02-01");
        verifyNoReportingCalls(MOTHER_MORTALITY, "2012-02-01");
    }

    @Test
    public void shouldNotReportMotherDeathDuringPNCCloseIfNotWithin42DaysOfDeliveryOutcome() {
        when(allMothers.findByCaseId("entity id 1")).thenReturn(MOTHER.withDeliveryOutCome("2012-01-01"));
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        Map<String, String> reportData = create("id", "entity id 1")
                .put("closeReason", "death_of_mother")
                .put("isMaternalDeath", "yes")
                .put("deathDate", "2012-02-12")
                .map();
        service.closePNC(new SafeMap(reportData));

        verifyNoReportingCalls(MMP, "2012-02-01");
        verifyNoReportingCalls(MOTHER_MORTALITY, "2012-02-01");
    }

    @Test
    public void shouldReportPNCVisit3DuringThirdPNCVisit() throws Exception {
        Mother mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withDetails(mapOf("pncVisitDates", "2011-12-25 2011-12-28 2012-01-01"))
                .withLMP(parse("2012-01-01"));

        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("pncVisitDates", "2011-12-25 2011-12-28 2012-01-01");
        reportData.put("pncVisitDate", "2012-01-01");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.pncVisitHappened(reportData);

        verifyBothReportingCalls(PNC3, "2012-01-01");
    }

    @Test
    public void shouldNotReportPNCVisit3IfItsNotThirdPNCVisit() throws Exception {
        Mother mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withDetails(mapOf("pncVisitDates", "2011-12-25 2011-12-26 2011-12-28 2012-01-01"))
                .withLMP(parse("2012-01-01"));

        SafeMap reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("pncVisitDates", "2011-12-25 2011-12-26 2011-12-28 2012-01-01");
        reportData.put("pncVisitDate", "2012-01-01");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.pncVisitHappened(reportData);

        verifyNoReportingCalls(PNC3, "2012-01-01");

        mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withDetails(mapOf("pncVisitDates", "2011-12-28 2012-01-01"))
                .withLMP(parse("2012-01-01"));

        reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("pncVisitDates", "2011-12-28 2012-01-01");
        reportData.put("pncVisitDate", "2012-01-01");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.pncVisitHappened(reportData);

        mother = new Mother("CASE-1", "EC-CASE-1", "TC 1")
                .withAnm("ANM X")
                .withDetails(mapOf("pncVisitDates", "2012-01-01"))
                .withLMP(parse("2012-01-01"));

        verifyNoReportingCalls(PNC3, "2012-01-01");
        reportData = new SafeMap();
        reportData.put("id", "entity id 1");
        reportData.put("pncVisitDates", "2012-01-01");
        reportData.put("pncVisitDate", "2012-01-01");
        when(allMothers.findByCaseId("entity id 1")).thenReturn(mother);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        service.pncVisitHappened(reportData);

        verifyNoReportingCalls(PNC3, "2012-01-01");

    }

    private void verifyBothReportingCalls(Indicator indicator, String date) {
        verify(reportingService).sendReportData(serviceProvided(indicator, date));
        verify(reportingService).sendReportData(anmReport(indicator, date));
    }

    private void verifyNoReportingCalls(Indicator indicator, String date) {
        verify(reportingService, times(0)).sendReportData(serviceProvided(indicator, date));
        verify(reportingService, times(0)).sendReportData(anmReport(indicator, date));
    }

    private void testPlaceOfDeliveryIsReported(String placeOfDelivery, Indicator expectedIndicator) {
        ReportingService reportingService = mock(ReportingService.class);
        MotherReportingService service = new MotherReportingService(reportingService, allMothers, allEligibleCouples, reportMonth);

        service.deliveryOutcome(reportDataForPlaceOfDelivery(placeOfDelivery));

        verify(reportingService).sendReportData(serviceProvided(expectedIndicator, "2012-05-01"));
        verify(reportingService).sendReportData(anmReport(expectedIndicator, "2012-05-01"));
    }

    private void assertThatIndicatorIsSetBasedOnLMP(String lmp, Indicator indicator) {
        SafeMap reportData = new SafeMap();
        reportData.put("referenceDate", lmp);
        reportData.put("motherId", "CASE-1");
        reportData.put("registrationDate", "2012-01-01");
        when(allMothers.findByCaseId("CASE-1")).thenReturn(MOTHER);
        when(allEligibleCouples.findByCaseId("EC-CASE-1")).thenReturn(new EligibleCouple().withLocation("bherya", "Sub Center", "PHC X"));

        ReportingService fakeReportingService = mock(ReportingService.class);
        MotherReportingService motherReportingService = new MotherReportingService(fakeReportingService, allMothers, allEligibleCouples, reportMonth);

        motherReportingService.registerANC(reportData);

        verify(fakeReportingService).sendReportData(serviceProvided(indicator, "2012-01-01"));
        verify(fakeReportingService).sendReportData(anmReport(indicator, "2012-01-01"));
    }

    private ReportingData anmReport(Indicator indicator, String date) {
        return anmReportData("ANM X", "CASE-1", indicator, date);
    }

    private ReportingData serviceProvided(Indicator indicator, String date) {
        return serviceProvidedData("ANM X", "TC 1", indicator, date, new Location("bherya", "Sub Center", "PHC X"));
    }

    private SafeMap reportDataForPlaceOfDelivery(String placeOfDelivery) {
        return new SafeMap(create("id", "entity id 1")
                .put("didWomanSurvive", "no")
                .put("deliveryOutcome", "still_birth")
                .put("referenceDate", "2012-05-01")
                .put("deliveryPlace", placeOfDelivery)
                .put("deliveryType", "normal")
                .map());
    }

    private SafeMap reportDataForANCClose(String mtpTime, String closeReason, String isMaternalDeath) {
        SafeMap reportData = new SafeMap();
        reportData.put("id", "CASE-1");
        reportData.put("timeOfInducedAbortion", mtpTime);
        reportData.put("closeReason", closeReason);
        reportData.put("dateOfSpontaneousAbortion", "2012-12-12");
        reportData.put("dateOfInducedAbortion", "2012-12-12");
        reportData.put("isMaternalDeath", isMaternalDeath);
        reportData.put("maternalDeathDate", "2012-12-12");
        return reportData;
    }
}
