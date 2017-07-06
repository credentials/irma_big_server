package foundation.privacybydesign.bigregister;

import io.jsonwebtoken.SignatureAlgorithm;
import org.irmacard.api.common.util.GsonUtil;
import foundation.privacybydesign.common.BaseConfiguration;

import java.io.*;
import java.security.KeyManagementException;
import java.security.PublicKey;

/**
 * Created by ayke on 9-5-17.
 */
public class BIGConfiguration extends BaseConfiguration {
    private static BIGConfiguration instance;
    private static final String CONFIG_FILENAME = "config.json";

    // JSON configuration properties (with default values)
    private String api_server_public_key = "";
    private String scheme_manager = "";
    private String big_issuer = "";
    private String big_credential = "";
    private String server_name = "";
    private String human_name = "";
    private String family_name_attribute = "";
    private String initials_attribute = "";
    private String gender_attribute = "";
    private String date_of_birth_attribute = "";
    int disclosure_jwt_max_age = 60 * 1000; // 60s

    // Our configuration
    private PublicKey apiServerPublicKey;


    public static BIGConfiguration getInstance() {
        if (instance == null) {
            try {
                String json = new String(getResource(CONFIG_FILENAME));
                instance = GsonUtil.getGson().fromJson(json, BIGConfiguration.class);
            } catch (IOException e) {
                System.out.println("could not load configuration");
                instance = new BIGConfiguration();
            }
        }
        return instance;
    }

    public PublicKey getApiServerPublicKey() throws KeyManagementException {
        if (apiServerPublicKey == null) {
            apiServerPublicKey = loadPublicKey(api_server_public_key);
        }
        return apiServerPublicKey;
    }

    public SignatureAlgorithm getJwtAlgorithm() {
        return SignatureAlgorithm.RS256;
    }

    public String getSchemeManager() {
        return scheme_manager;
    }

    public String getBIGIssuer() { return big_issuer; }

    public String getBIGCredential() {
        return big_credential;
    }

    public String getInitialsAttribute() { return initials_attribute; }

    public String getFamilyNameAttribute() { return family_name_attribute; }

    public String getDateOfBirthAttribute() { return date_of_birth_attribute; }

    public String getGenderAttribute() { return gender_attribute; }

    public String getServerName() { return server_name; }

    public String getHumanReadableName() { return human_name; }

    public int getDisclosureJwtMaxAge() { return disclosure_jwt_max_age; }
}
