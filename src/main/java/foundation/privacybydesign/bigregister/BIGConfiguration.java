package foundation.privacybydesign.bigregister;

import io.jsonwebtoken.SignatureAlgorithm;
import org.irmacard.api.common.util.GsonUtil;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by ayke on 9-5-17.
 */
public class BIGConfiguration {
    static BIGConfiguration instance;
    static final String filename = "config.json";

    // JSON configuration properties (with default values)
    String private_key = "";
    String api_server_public_key = "";
    String scheme_manager = "";
    String big_issuer = "";
    String big_credential = "";
    String server_name = "";
    String human_name = "";
    String family_name_attribute = "";
    String initials_attribute = "";
    String gender_attribute = "";
    String date_of_birth_attribute = "";
    int disclosure_jwt_max_age = 60 * 1000; // 60s

    // Our configuration
    PrivateKey privateKey;
    PublicKey apiServerPublicKey;


    public static BIGConfiguration getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        try {
            String json = new String(getResource(filename));
            instance = GsonUtil.getGson().fromJson(json, BIGConfiguration.class);
        } catch (IOException e) {
            System.out.println("could not load configuration");
            instance = new BIGConfiguration();
        }
    }

	private static byte[] getResource(String filename) throws IOException {
		File file = new File("/home/ayke/src/bigregister/" + filename);
		return convertSteamToByteArray(new FileInputStream(file), 2048);
	}

    public static byte[] convertSteamToByteArray(InputStream stream, int size) throws IOException {
		byte[] buffer = new byte[size];
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		int line;
		while ((line = stream.read(buffer)) != -1) {
			os.write(buffer, 0, line);
		}
		stream.close();

		os.flush();
		os.close();
		return os.toByteArray();
	}

	public PrivateKey getPrivateKey() throws KeyManagementException {
        if (privateKey == null) {
            privateKey = loadPrivateKey(private_key);
        }
        return privateKey;
    }

    private PrivateKey loadPrivateKey(String filename) throws KeyManagementException {
        try {
            return decodePrivateKey(BIGConfiguration.getResource(filename));
        } catch (IOException e) {
            throw new KeyManagementException(e);
        }
    }

	private PrivateKey decodePrivateKey(byte[] rawKey) throws KeyManagementException {
        try {
			if (rawKey == null || rawKey.length == 0)
				throw new KeyManagementException("Could not read private key");

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(rawKey);
			return KeyFactory.getInstance("RSA").generatePrivate(spec);
		} catch (NoSuchAlgorithmException|InvalidKeySpecException e) {
			throw new KeyManagementException(e);
		}
    }

    private PublicKey loadPublicKey(String filename) throws KeyManagementException {
        try {
            return decodePublicKey(BIGConfiguration.getResource(filename));
        } catch (IOException e) {
            throw new KeyManagementException(e);
        }
    }

    private PublicKey decodePublicKey(byte[] rawKey) throws KeyManagementException {
        try {
            if (rawKey == null || rawKey.length == 0)
                throw new KeyManagementException("Could not read public key");

            X509EncodedKeySpec spec = new X509EncodedKeySpec(rawKey);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (NoSuchAlgorithmException|InvalidKeySpecException e) {
            throw new KeyManagementException(e);
        }
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
