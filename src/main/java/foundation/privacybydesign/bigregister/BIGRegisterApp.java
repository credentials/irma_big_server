/**
 * This is a debugging frontend for the BIG register API.
 * Use a single parameter: the name of the person you're looking for.
 * It will show all information it can gather from that.
 */

package foundation.privacybydesign.bigregister;

import nl.cibg.services.externaluser.*;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPException;
import java.util.*;

public class BIGRegisterApp {
    public static void main(String[] args) throws SOAPException {
        String name = ".";
        if (args.length >= 1) {
            name = args[0];
        }

        BIGService service = new BIGService();
        List<ListHcpApprox4> results;
        System.out.println("Requesting people...");
        try {
            results = service.doRequest(name, null, "");
        } catch (BIGRequestException e) {
            System.out.println("Could not query the BIG database: " + e.getMessage());
            return;
        }

        System.out.println("Found " + results.size() + " entries:");
        for (ListHcpApprox4 result : results) {
            System.out.println("Mailing name:  " + result.getMailingName());
            System.out.println("\tInitials:      " + result.getInitial());
            System.out.println("\tPrefix:        " + result.getPrefix());
            System.out.println("\tBirth surname: " + result.getBirthSurname());
            System.out.println("\tGender:        " + result.getGender());
            for (ArticleRegistrationExtApp group : result.getArticleRegistration().getArticleRegistrationExtApp()) {
                int groupCode;
                try {
                    groupCode = Integer.parseInt(group.getProfessionalGroupCode());
                } catch (NumberFormatException e) {
                    groupCode = 0;
                }
                System.out.println("\tGroup:         " + BIGService.GROUPS.getOrDefault(groupCode, group.getProfessionalGroupCode()));
                System.out.println("\t  BIG number:  " + group.getArticleRegistrationNumber());
                System.out.println("\t  Start date:  " + group.getArticleRegistrationStartDate());
                // End date appears to be 0001-01-01T00:00:00
                // TODO: inspect the *actual* XML output to test this.
                XMLGregorianCalendar endDate = group.getArticleRegistrationEndDate();
                if (endDate.getDay() == 1 && endDate.getMonth() == 1 && endDate.getYear() == 1) {
                    System.out.println("\t  End date:    <none>");
                } else {
                    System.out.println("\t  End date:    " + endDate);
                }
            }
            for (SpecialismExtApp1 specialism : result.getSpecialism().getSpecialismExtApp1()) {
                int specialismType;
                try {
                    specialismType = specialism.getTypeOfSpecialismId().intValueExact();
                } catch (ArithmeticException e) {
                    // Should not happen.
                    // This means the BigInteger does not fit in a regular int, which is very unlikely for the
                    // specified list of integers.
                    specialismType = 0;
                }
                System.out.println("\tSpecialism:    " + BIGService.SPECIALISMS.getOrDefault(specialismType, specialism.getTypeOfSpecialismId().toString()));
                System.out.println("\t  BIG number:  " + specialism.getArticleRegistrationNumber());
                System.out.println("\t  ID:          " + specialism.getSpecialismId());
            }
            for (MentionExtApp mention : result.getMention().getMentionExtApp()) {
                System.out.println("\tMentionId:     " + mention.getMentionId());
                System.out.println("\t  BIG number:  " + mention.getArticleRegistrationNumber());
                System.out.println("\t  Type:        " + mention.getTypeOfMentionId());
                System.out.println("\t  Start date:  " + mention.getStartDate());
                System.out.println("\t  End date:    " + mention.getEndDate());
            }
            for (JudgmentProvisionExtApp judgment : result.getJudgmentProvision().getJudgmentProvisionExtApp()) {
                System.out.println("\tJudgment provision: " + judgment.getId());
                System.out.println("\t  BIG number:  " + judgment.getArticleNumber());
                System.out.println("\t  Start date:  " + judgment.getStartDate());
                System.out.println("\t  End date:    " + judgment.getEndDate());
                System.out.println("\t  Description: " + judgment.getPublicDescription());
                // There is also a 'Public' property, but it is always true.
            }
            for (LimitationExtApp limitation : result.getLimitation().getLimitationExtApp()) {
                System.out.println("\tLimitation:    " + limitation.getLimitationId());
                System.out.println("\t  BIG number:  " + limitation.getArticleRegistrationNumber());
                System.out.println("\t  Type:        " + limitation.getTypeLimitationId());
                System.out.println("\t  Competence:  " + limitation.getCompetenceRegistrationId());
                System.out.println("\t  Description: " + limitation.getDescription());
                System.out.println("\t  Start date:  " + limitation.getStartDate());
                System.out.println("\t  End date:    " + limitation.getEndDate());
                System.out.println("\t  Expiration:  " + limitation.getExpirationEndDate());
                System.out.println("\t  Months valid:" + limitation.getMonthsValid());
                System.out.println("\t  Years valid: " + limitation.getYearsValid());
            }
        }
        System.out.println("finished.");
    }
}
