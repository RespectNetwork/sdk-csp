package net.respectnetwork.sdk.csp.performance;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import net.respectnetwork.sdk.csp.performance.model.Availability;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CheckCloudNameAvaliabilityREST extends AbstractTester {
    
    /** CloudName */
    private String cloudName;
    
    /** GCS */
    private String gcs;
    

    /**
     * @return the CloudName
     */
    public String getCloudName() {
        return cloudName;
    }

    /**
     * @param verifiedEmail the verifiedEmail to set
     */
    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }
    
    /**
     * 
     * @return
     */
    public String getGcs() {
		return gcs;
	}

    /**
     * 
     * @param gcs
     */
	public void setGcs(String gcs) {
		this.gcs = gcs;
	}

	
    public CheckCloudNameAvaliabilityREST() {
    }

	public void execute() throws TestException {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		//String serviceEndpoint = "http://cloudnameavailability.elasticbeanstalk.com/api/availability/";
		String serviceEndpoint = "http://perf-cloudname-availability-vpc.elasticbeanstalk.com/api/availability/";
		String restQuery = serviceEndpoint + gcs + "/" + cloudName;
		HttpGet httpget = new HttpGet(restQuery);

		ResponseHandler<Availability> rh = new ResponseHandler<Availability>() {

			@Override
			public Availability handleResponse(final HttpResponse response)
					throws IOException {
				StatusLine statusLine = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getReasonPhrase());
				}
				if (entity == null) {
					throw new ClientProtocolException(
							"Response contains no content");
				}
				Gson gson = new GsonBuilder().create();
				ContentType contentType = ContentType.getOrDefault(entity);
				Charset charset = contentType.getCharset();
				if ( charset == null ){
					charset = Charset.forName("UTF-8");
				}
				Reader reader = new InputStreamReader(entity.getContent(),
						charset);
				return gson.fromJson(reader, Availability.class);
			}
		};


		try {

			Availability myjson = httpclient.execute(httpget, rh);
			System.out.println("Return from checkAvailability: CloudName = "
					+ myjson.getCloudname() + " Available = "
					+ myjson.getAvailable() + "Error = " + myjson.getError());
			
			if ( !(myjson.getAvailable() == 0 )) { // && myjson.getError().equalsIgnoreCase("null"))) {
				throw new TestException("Unexpected Response");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new TestException(e.getMessage());
		} 
	}
    
    public void init() throws TestException {
        this.setCloudName("beech"); 
        this.setGcs("equals");
    }
    
}
