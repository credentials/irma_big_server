package foundation.privacybydesign.bigregister;

/**
 * Created by ayke on 18-5-17.
 */

import org.glassfish.jersey.server.ResourceConfig;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.ws.rs.*;

@ApplicationPath("/")
public class RestService extends ResourceConfig {
    //private static Logger logger = LoggerFactory.getLogger(BIGApplication.class);

    public RestService() {
        register(RestApi.class);

        //logger.info("Starting API for BIG registration");
    }
}
