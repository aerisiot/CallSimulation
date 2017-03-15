package CallSimulation.Pi.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import CallSimulation.Pi.service.TwillioService;

/**
 * Service for fetching the Aeris TLDN and integrating with Twilio
 * @author Anu.madan
 *
 */
@Controller
@RequestMapping(value = "temperature")
public class NotificationController {
	
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private TwillioService service;
    
    @Value("${accountId}")
    private String accountId;
    
    @Value("${apiKey}")
    private String apiKey;
    
    @Value("${imsi}")
    private String imsi;
    
    private static final String URI = "https://api.aerframe.aeris.com/networkservices/v2/";
    
    /**
     * Requests Aerframe API to generate TLDN and then calls Twilio to request an outgoing voice call.
     * @param param: current temperature
     * @return success/failure
     */
    @RequestMapping(value="/v1/notify",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody Map<String,String> createPackage(@RequestBody String param) {
    	logger.info("/notify [POST]");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	Map<String,String> result = new HashMap<String,String>();
    	
		try {
			//Call TLDN API
			String tldnUri = URI + accountId + "/devices/imsi/" + imsi + "/localDialableNumber?apiKey=" + apiKey;
			
			URL url = new URL(tldnUri);
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");

			if (con.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ con.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(con.getInputStream())));

			String output;
			HashMap<String,String> resultMap = null;
			if ((output = br.readLine()) != null) {
				resultMap = mapper.readValue(output, HashMap.class);
			}

			con.disconnect();
			String toNumber = resultMap.get("dialableNumber");
			System.out.println("TLDN Num " + toNumber);

			//Call Twilio API
			service.callBack(toNumber);
			System.out.println("Call sent.");
			result.put("Result", "Success");
		} catch (Exception e) {
			System.out.println("Error while notifying" + e);
			result.put("Result", "Failure");
		}
		return result;
    }

    /**
     * A callback service provided to twilio to provide status callbacks on for a voice call.
     */
    @RequestMapping(value="/callback",method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody void getPackagesBySku() {
    	//Simply logging the message in order to simulate.
    	//Enhancement: The actual request body can be parsed to fetch the actual call even statuses.
    	System.out.println("Callback for call received from twilio");
    }
}
