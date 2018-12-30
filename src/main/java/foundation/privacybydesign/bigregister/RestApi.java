package foundation.privacybydesign.bigregister;

import com.google.gson.reflect.TypeToken;
import io.jsonwebtoken.*;
import nl.cibg.services.externaluser.ListHcpApprox4;
import org.irmacard.api.common.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.irmacard.api.common.JwtParser;
import org.irmacard.api.common.issuing.IdentityProviderRequest;
import org.irmacard.api.common.issuing.IssuingRequest;
import org.irmacard.credentials.info.AttributeIdentifier;
import org.irmacard.credentials.info.CredentialIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The REST api to be used by the web client.
 */
@Path("")
public class RestApi {
    // We currently use the Dutch format, not the internal ISO8601 format.
    private static String IRMA_DATE_FORMAT = "dd-MM-yyyy";

    private static String NO_RESULTS         = "error:no-results";
    private static String MULTIPLE_RESULTS   = "error:multiple-results";
    private static String INVALID_JWT        = "error:invalid-jwt";
    private static String BIG_REQUEST_FAILED = "error:big-request-failed";

    private static Logger logger = LoggerFactory.getLogger(RestApi.class);

    // Get a disclosure request - to get the right credential from the user.
    @GET
    @Path("/request-search-attrs")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSearchAttributes() throws KeyManagementException {
        BIGConfiguration conf = BIGConfiguration.getInstance();

        // Request the following iDIN properties for verification: name, birth date, gender
        AttributeDisjunctionList requestAttrs = new AttributeDisjunctionList(4);
        requestAttrs.add(conf.getInitialsAttribute());
        requestAttrs.add(conf.getFamilyNameAttribute());
        requestAttrs.add(conf.getDateOfBirthAttribute());
        requestAttrs.add(conf.getGenderAttribute());
        return ApiClient.getDisclosureJWT(requestAttrs,
                conf.getServerName(),
                conf.getHumanReadableName(),
                conf.getJwtAlgorithm(),
                conf.getPrivateKey());
    }

    private String findAttributeInMap(Map<AttributeIdentifier, String> map, AttributeDisjunction disjunction) {
        for (AttributeIdentifier attr : disjunction)
            if (map.containsKey(attr))
                return map.get(attr);
        return null;
    }

    // Request the BIG credential, based on iDIN attributes.
    @POST
    @Path("/request-attrs")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getAttributes(String disclosureJWT) throws KeyManagementException {
        BIGConfiguration conf = BIGConfiguration.getInstance();

        Map<AttributeIdentifier, String> disclosureAttrs;
        // TODO: detect when the maxAge constraint doesn't hold and indicate it to the user.
        // It might just be caused by unsynchronized clocks.
        try {
            Type t = new TypeToken<Map<AttributeIdentifier, String>>() {}.getType();
            JwtParser<Map<AttributeIdentifier, String>> parser
                = new JwtParser<>(t, false, conf.getDisclosureJwtMaxAge(), "disclosure_result", "attributes");
            parser.setSigningKey(conf.getApiServerPublicKey());
            parser.parseJwt(disclosureJWT);
            disclosureAttrs = parser.getPayload();
        } catch (ExpiredJwtException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(INVALID_JWT).build();
        }

        String initials = findAttributeInMap(disclosureAttrs, conf.getInitialsAttribute());
        String familyName = findAttributeInMap(disclosureAttrs, conf.getFamilyNameAttribute());
        String gender = findAttributeInMap(disclosureAttrs, conf.getGenderAttribute());
        String dateOfBirthString = findAttributeInMap(disclosureAttrs, conf.getDateOfBirthAttribute());

        // These attributes can be set for debugging purposes - to impersonate someone else.
        // Must only be used in a test environment!
        //String initials = "";
        //String familyName = "";
        //String gender = "";
        //String dateOfBirthString = "";

        Date dateOfBirth;
        try {
            dateOfBirth = new SimpleDateFormat(IRMA_DATE_FORMAT).parse(dateOfBirthString);
        } catch (ParseException e) {
            // Should never happen.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not parse date").build();
        }

        String bigNumber;
        try {
            bigNumber = BIGWebSearch.getBIGNumber(familyName, dateOfBirth, gender);
        } catch (BIGRequestException e) {
            // This should indicate a problem on their end or with the connection, not on our side.
            logger.error("BIG request error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(BIG_REQUEST_FAILED).build();
        } catch (BIGWebNoResultsException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(NO_RESULTS).build();
        } catch (BIGWebTooManyException e) {
            // TODO: notify someone of this situation. It shouldn't happen.
            logger.error("Multiple results found in BIG query!");
            return Response.status(Response.Status.BAD_REQUEST).entity(MULTIPLE_RESULTS).build();
        }

        List<ListHcpApprox4> results;
        try {
            results = BIGService.doRequest(bigNumber);
        } catch (BIGRequestException e) {
            // This should indicate a problem on their end or with the connection, not on our side.
            logger.error("BIG request error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(BIG_REQUEST_FAILED).build();
        }

        if (results.size() < 1) {
            // This can happen when e.g. there is a mismatch (e.g. in the name)
            // or someone tries to request a property while they're not
            // registered.
            return Response.status(Response.Status.BAD_REQUEST).entity(NO_RESULTS).build();
        }

        if (results.size() > 1) {
            // TODO: notify someone of this situation. It shouldn't happen.
            logger.error("Multiple results found in BIG query!");
            return Response.status(Response.Status.BAD_REQUEST).entity(MULTIPLE_RESULTS).build();
        }

        ListHcpApprox4 result = results.get(0);
        if (!result.getBirthSurname().equals(familyName) ||
                !result.getInitial().substring(0, 1).equals(initials.substring(0, 1))) {
            // The 'familyName' is the birth family name, so we're OK here
            // even if people marry and change their last name.
            // The initial is a bit more difficult, I'm not sure people will always use the same initials.
            // So only check the first character, which should match.
            // Unfortunately, we can't match on prefix as that's not provided by iDIN.
            // See BIGService.doRequest for more details about prefix handling in the BIG register.
            // Sent a NO_RESULTS messsage, as it is essentially the same
            // problem, with the difference that we do the validation instead
            // of BIG.
            return Response.status(Response.Status.BAD_REQUEST).entity(NO_RESULTS).build();
        }

        Collection<BIGProfession> professions;
        try {
            professions = BIGService.getProfessions(result);
        } catch (BIGFormatException e) {
            // TODO: log this issue somewhere - it shouldn't happen.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not parse response").build();
        }

        if (professions.size() < 1) {
            // This should not happen, or only if the BIG has invalid data.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No professions in response").build();
        }

        ArrayList<CredentialRequest> credentials = new ArrayList<>(professions.size());

        // Work with the same time everywhere.
        Calendar now = Calendar.getInstance();

        for (BIGProfession profession : professions) {
            // Do some checks
            // TODO: Should we also check for start date?
            if (profession.getEndDate() != null && profession.getEndDate().before(now)) {
                // This registration isn't valid anymore.
                continue;
            }

            HashMap<String, String> attrs = new HashMap<>(4);
            attrs.put("profession", profession.getName());
            attrs.put("bignumber", profession.getRegistrationNumber().toString());
            if (profession.getSpecialisms().size() == 0) {
                // There is no specialism.
                // Put a space here as we have to provide *something*.
                attrs.put("specialism", " ");
            } else {
                // Create a string of the form a;b;c where a, b and c are specialisms.
                StringBuilder specialismsBuilder = new StringBuilder();
                for (String specialism : profession.getSpecialisms()) {
                    if (specialismsBuilder.length() != 0) {
                        specialismsBuilder.append(';');
                    }
                    specialismsBuilder.append(specialism);
                }
                attrs.put("specialism", specialismsBuilder.toString());
            }
            attrs.put("startdate", new SimpleDateFormat(IRMA_DATE_FORMAT).format(profession.getStartDate().getTime()));
            Calendar credentialEndDate = (Calendar)now.clone();
            // TODO: how long should this credential be valid?
            credentialEndDate.add(Calendar.YEAR, 1);
            if (profession.getEndDate() != null && profession.getEndDate().before(credentialEndDate)) {
                credentialEndDate = profession.getEndDate();
            }
            credentials.add(new CredentialRequest(
                    (int) CredentialRequest.floorValidityDate(credentialEndDate.getTimeInMillis(), true),
                    new CredentialIdentifier(
                            conf.getSchemeManager(),
                            conf.getBIGIssuer(),
                            conf.getBIGCredential()
                    ),
                    attrs
            ));
        }

        if (credentials.size() == 0) {
            // May happen when all credentials lie in the past - thus the whole
            // registration has ended.
            // TODO: maybe we want to indicate the real reason - that this
            // person is probably not registered anymore?
            return Response.status(Response.Status.BAD_REQUEST).entity(NO_RESULTS).build();
        }

        // Create an AttributeDisjunctionList to match the original request
        AttributeDisjunctionList requestAttrs = new AttributeDisjunctionList(4);
        // Initials
        AttributeDisjunction attributeInitialDisjunction = conf.getInitialsAttribute();
        putExpectedValue(attributeInitialDisjunction, initials);
        requestAttrs.add(attributeInitialDisjunction);
        // Family name
        AttributeDisjunction attributeFamilyNameDisjunction = conf.getFamilyNameAttribute();
        putExpectedValue(attributeFamilyNameDisjunction, familyName);
        requestAttrs.add(attributeFamilyNameDisjunction);
        // Date of birth
        AttributeDisjunction attributeDateOfBirthDisjunction = conf.getDateOfBirthAttribute();
        putExpectedValue(attributeDateOfBirthDisjunction, dateOfBirthString);
        requestAttrs.add(attributeDateOfBirthDisjunction);
        // Gender
        AttributeDisjunction attributeGenderDisjunction = conf.getGenderAttribute();
        putExpectedValue(attributeGenderDisjunction, gender);
        requestAttrs.add(attributeGenderDisjunction);

        // Now generate the credential issuing request!
        // The user of this API can use this to get the actual credential from the IRMA API server.
        IdentityProviderRequest ipRequest = new IdentityProviderRequest("", new IssuingRequest(null, null, credentials, requestAttrs), 120);
        return Response.ok(ApiClient.getSignedIssuingJWT(ipRequest,
                conf.getServerName(),
                conf.getHumanReadableName(),
                conf.getJwtAlgorithm(),
                conf.getPrivateKey() // throws KeyManagementException, but this should not happen with a proper configuration
        ), MediaType.TEXT_PLAIN).build();
    }

    private void putExpectedValue(AttributeDisjunction disjunction, String value) {
        for (AttributeIdentifier attr : disjunction)
            disjunction.getValues().put(attr, value);
    }
}
