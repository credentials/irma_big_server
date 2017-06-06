package foundation.privacybydesign.bigregister;

import nl.cibg.services.externaluser.*;

import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Abstract away the BIG SOAP API.
 */
public class BIGService {

    // All professions and specialisms as defined by the BIG register. Source:
    // https://www.bigregister.nl/zoek-zorgverlener/documenten/publicaties/2017/03/03/handleiding-webservice-big-register
    public static final Map<Integer, String> GROUPS;
    public static final Map<Integer, String> SPECIALISMS;
    static {
        Map<Integer, String> groups = new HashMap<Integer, String>();
        groups.put(1,  "Arts");
        groups.put(2,  "Tandarts");
        groups.put(3,  "Verloskundige");
        groups.put(4,  "Fysiotherapeut");
        groups.put(16, "Psychotherapeut");
        groups.put(17, "Apotheker");
        groups.put(25, "Gz-psycholoog");
        groups.put(30, "Verpleegkundige");
        GROUPS = Collections.unmodifiableMap(groups);

        Map<Integer, String> specialisms = new HashMap<Integer, String>();
        specialisms.put(2, "Allergologie (allergoloog)");
        specialisms.put(3, "Anesthesiologie (anesthesioloog)");
        specialisms.put(4, "Huisartsgeneeskunde met apotheek (Apoth. Huisarts)");
        specialisms.put(8, "Arbeid en gezond - bedrijfsgeneeskunde");
        specialisms.put(10, "Cardiologie (cardioloog)");
        specialisms.put(11, "Cardio-thoracale chirurgie");
        specialisms.put(12, "Dermatologie en venerologie (dermatoloog)");
        specialisms.put(13, "Maag-darm-leverziekten (maag-darm-leverarts)");
        specialisms.put(14, "Heelkunde (chirurg)");
        specialisms.put(15, "Huisartsgeneeskunde (huisarts)");
        specialisms.put(16, "Interne geneeskunde (internist)");
        specialisms.put(18, "Keel-, neus- en oorheelkunde (kno-arts)");
        specialisms.put(19, "Kindergeneeskunde (kinderarts)");
        specialisms.put(20, "Klinische chemie (arts klinische chemie)");
        specialisms.put(21, "Klinische genetica (klinisch geneticus)");
        specialisms.put(22, "Klinische geriatrie (klinisch geriater)");
        specialisms.put(23, "Longziekten en tuberculose (longarts)");
        specialisms.put(24, "Medische microbiologie (arts-microbioloog)");
        specialisms.put(25, "Neurochirurgie (neurochirurg)");
        specialisms.put(26, "Neurologie (neuroloog)");
        specialisms.put(30, "Nucleaire geneeskunde (nucleair geneeskundige)");
        specialisms.put(31, "Oogheelkunde (oogarts)");
        specialisms.put(32, "Orthopedie (orthopeed)");
        specialisms.put(33, "Pathologie (patholoog)");
        specialisms.put(34, "Plastische chirurgie (plastisch chirurg)");
        specialisms.put(35, "Psychiatrie (psychiater)");
        specialisms.put(39, "Radiologie (radioloog)");
        specialisms.put(40, "Radiotherapie (radiotherapeut)");
        specialisms.put(41, "Reumatologie (reumatoloog)");
        specialisms.put(42, "Revalidatiegeneeskunde (revalidatiearts)");
        specialisms.put(43, "Maatschappij en gezondheid (beëindigd per 01-01-2007)");
        specialisms.put(45, "Urologie (uroloog)");
        specialisms.put(46, "Obstetrie en gynaecologie (gynaecoloog)");
        specialisms.put(47, "Specialisme ouderengeneeskunde");
        specialisms.put(48, "Arbeid en gezondheid - verzekeringsgeneeskunde");
        specialisms.put(50, "Zenuw- en zielsziekten (zenuwarts)");
        specialisms.put(53, "Dento-maxillaire orthopaedie (orthodontist)");
        specialisms.put(54, "Mondziekten en kaakchirurgie (kaakchirurg)");
        specialisms.put(55, "Maatschappij en gezondheid");
        specialisms.put(56, "Geneeskunde voor verstandelijk gehandicapten");
        specialisms.put(60, "Ziekenhuisfarmacie (ziekenhuisapotheker)");
        specialisms.put(61, "Klinische psychologie (klinisch psycholoog)");
        specialisms.put(62, "Interne geneeskunde-allergologie");
        specialisms.put(63, "Klinische neuropsychologie");
        specialisms.put(65, "Verpl. spec. prev. zorg bij som. aandoeningen");
        specialisms.put(66, "Verpl. spec. acute zorg bij som. aandoeningen");
        specialisms.put(67, "Verpl. spec. intensieve zorg bij som. aandoeningen");
        specialisms.put(68, "Verpl. spec. chronische zorg bij som. aandoeningen");
        specialisms.put(69, "Verpl. spec. geestelijke gezondheidszorg");
        specialisms.put(70, "Jeugdgezondheidszorg (Profiel KNMG Jeugdarts)");
        specialisms.put(71, "Spoedeisendehulp (Profiel SEH Arts KNMG)");
        specialisms.put(74, "Sportgeneeskunde");
        specialisms.put(75, "Openbaar apotheker");
        SPECIALISMS = Collections.unmodifiableMap(specialisms);
    }

    // Provide a simple method for the SOAP API.
    // It either returns the requested values, or returns an error.
    public List<ListHcpApprox4> doRequest(String name, Date dateOfBirth, String gender)
            throws BIGRequestException {
        // Create the SOAP client
        PublicV4 service = new PublicV4();
        PublicV4Soap client = service.getPublicV4Soap();

        ListHcpApproxRequest request = new ListHcpApproxRequest();
        try {
            // Prefix handling in the BIG register is pretty inconsistent.
            // Digging through the BIG database, I've found
            // * the prefix is sometimes null and sometimes an empty string ""
            // * When people do have a prefix, it is sometimes attached in front of the birth surname
            //   instead of set in the prefix field.
            // * When setting the prefix to null, it won't search for it.
            // The name that is matched on appears to be a simple string search on the mailing name.
            request.setName(name);
            if (dateOfBirth != null) {
                // BIG uses the ISO8601 format for the birth date.
                request.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").format(dateOfBirth));
            }
            if (gender.equals("male")) {
                request.setGender("M");
            } else if (gender.equals("female")) {
                request.setGender("V");
            } else {
                // gender can also be unknown or unspecified
            }

            // This must be set, and it's the only valid value.
            request.setWebSite(SourceWebSite.RIBIZ);

            // Do the request!
            ListHcpApproxResponse4 response = client.listHcpApprox4(request);

            // Return the List of results.
            return response.getListHcpApprox().getListHcpApprox4();

        } catch (SOAPFaultException ex) {
            // The API returned an error in a SOAP object. This is most likely our fault.
            // The actual SOAP exception is a bit hard to find. It's buried somewhere in the XML and the code isn't
            // easily accessible from the API. So we have to do some work to get to the actual exception.

            SOAPFault fault = ex.getFault();
            if (fault.hasDetail()) {
                // Get error message from the server
                Iterator entries = fault.getDetail().getDetailEntries();
                String title = null;
                String message = null;
                // We're parsing the XML here, in this form:
                // <detail xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                //   <Title>Exception</Title>
                //   <Description/>
                //   <Message></Message>
                //   <Exception/>
                // </detail>

                while (entries.hasNext()) {
                    DetailEntry entry = (DetailEntry) entries.next();
                    if (entry.getTagName().equals("Title")) {
                        title = entry.getValue();
                    } else if (entry.getTagName().equals("Message")) {
                        message = entry.getValue();
                    }
                }
                if (title == null) {
                    title = fault.getFaultString();
                }
                if (message == null) {
                    message = "<unknown exception>";
                }
                throw new BIGRequestException(title + ": " + message + " (actor: " + fault.getFaultActor() + ", code: " + fault.getFaultCode() + ")");
            }

            // Fallback when there is no exception detail - I haven't seen this in actual error messages.
            throw new BIGRequestException(fault.getFaultActor() + ": " + fault.getFaultCode());
        }
    }


    // Take a single ListHcpApprox4 result (one person) and convert this to a list of professions
    // (with attached BIG numbers, specialisms, etc.).
    public static Collection<BIGProfession> getProfessions(ListHcpApprox4 result) throws BIGFormatException {
        // https://www.bigregister.nl/documenten/publicaties/2017/03/03/handleiding-webservice-big-register
        // https://www.bigregister.nl/over-het-big-register/maatregelen
        // https://www.bigregister.nl/over-het-big-register/voor-zorgconsumenten
        // http://wetten.overheid.nl/BWBR0027468/2015-12-22
        // What I've found:
        //   - Some people have multiple professions (e.g. Arts and Psychotherapeut).
        //     They have different BIG number for each profession.
        //   - Sometimes a judgment provision is returned, if they currently have
        //     gotten measures or had them in the past. When there is a current measure
        //     active, there is an end date that lies in the past.
        //   - For each profession, a separate judgemental provision is returned (with
        //     associated BIG number).
        //   - A specialism is associated with a profession (see 4.5 of the specification).
        //     That also means it has no start or end date - you need to look at the
        //     main profession for that (my interpretation of the specification).
        //   - Only valid specialisms are returned - unless the profession is invalid in
        //     which case the specialism isn't relevant ayway.
        //   - Even though a judgemental provision has ended, the end date of a
        //     judgemental provision is still null.
        //   - I haven't actually been able to find end dates in the future.
        //   - Limitations appear to be added for people from foreign countries, e.g.
        //     Suriname.
        // From that I conclude:
        //   - We should just ignore the judgemental provisions and only look at the end
        //     date of the registration. Only if it lies in the past it has ended, if it
        //     lies in the future or is undefined it is still active.

        // Create a map: {BIG number: [profession1, profession2, ...]}
        HashMap<BigDecimal, BIGProfession> professions = new HashMap<>();
        for (ArticleRegistrationExtApp group : result.getArticleRegistration().getArticleRegistrationExtApp()) {
            int groupCode;
            try {
                groupCode = Integer.parseInt(group.getProfessionalGroupCode());
            } catch (NumberFormatException e) {
                throw new BIGFormatException("invalid profession number: '" + group.getProfessionalGroupCode() + "'");
            }
            String professionName = GROUPS.getOrDefault(groupCode, group.getProfessionalGroupCode());

            BigDecimal number = group.getArticleRegistrationNumber();
            if (professions.containsKey(number)) {
                // I'm not entirely sure this never happens.
                throw new BIGFormatException("two professions with the same BIG number");
            }

            // End date appears to be 0001-01-01T00:00:00
            // TODO: inspect the *actual* XML output to test this.
            GregorianCalendar startDate, endDate;
            startDate = group.getArticleRegistrationStartDate().toGregorianCalendar();
            endDate = group.getArticleRegistrationEndDate().toGregorianCalendar();
            if (endDate.get(Calendar.YEAR) == 1 && endDate.get(Calendar.MONTH) == Calendar.JANUARY && endDate.get(Calendar.DAY_OF_MONTH) == 1) {
                // No end date was specified. This is almost always the case.
                endDate = null;
            }

            professions.put(number, new BIGProfession(number, professionName, startDate, endDate));
        }

        // Once we have all the professions, get the specialisms that belong to the professions.
        // Those are put in a separate list, but linked to professions by BIG number.
        for (SpecialismExtApp1 specialism : result.getSpecialism().getSpecialismExtApp1()) {
            int specialismType;
            try {
                specialismType = specialism.getTypeOfSpecialismId().intValueExact();
            } catch (ArithmeticException e) {
                // Should not happen.
                // This means the BigInteger does not fit in a regular int, which is very unlikely for the
                // specified list of integers.
                throw new BIGFormatException("specialism ID doesn't fit in an int");
            }
            String specialismName = SPECIALISMS.getOrDefault(specialismType, specialism.getTypeOfSpecialismId().toString());
            BIGProfession profession = professions.get(specialism.getArticleRegistrationNumber());
            // This loses one field of information (the specialism ID), but that field appears largely irrelevant.
            // I don't even know what it means exactly, maybe the row ID in their database?
            profession.addSpecialism(specialismName);
        }

        // The keys are now irrelevant - only return the values (actual BIG professions).
        return professions.values();
    }
}
