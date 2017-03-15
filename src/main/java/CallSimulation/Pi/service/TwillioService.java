package CallSimulation.Pi.service;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.twilio.http.HttpMethod;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;


/**
 * Calls twilio API to request an outgoing call to the passed in number.
 * @author Anu.Madan
 *
 */
@Component
public class TwillioService {
	
	 @Value("${callbackServer}")
	 private String callbackServer;

    // Find your Account Sid and Auth Token at twilio.com/console
	// Replace the place holders with your sid and Auth Token
    public static final String ACCOUNT_SID = "<ACCOUNT_SID>";
    public static final String AUTH_TOKEN = "AUTH_TOKEN";

    public String callBack(String toNumber) {
    	TwilioRestClient client = new TwilioRestClient.Builder(ACCOUNT_SID, AUTH_TOKEN).build();
    	String callbackURL = "http://" + callbackServer + "/call.simulation/temperature/callback";
        
    	PhoneNumber to = new PhoneNumber(toNumber); // TLDN passed
        PhoneNumber from = new PhoneNumber("<Twilio_Number>"); // Replace with a Twilio number for your account
        
        URI uri = URI.create("http://demo.twilio.com/welcome/voice/");
        List<String> callbackEvents = Arrays.asList("initiated", "ringing", "answered", "completed");
        
        // Make the call and configure callback URL
        Call call = Call.creator(to, from, uri).setMethod(HttpMethod.GET).setStatusCallback(callbackURL)
                .setStatusCallbackMethod(HttpMethod.POST).setStatusCallbackEvent(callbackEvents).create(client);
        
        // Print the call SID (a 32 digit hex like CA123..)
        System.out.println("Call SID is: " + call.getSid());
        return call.getSid();
    }
}
