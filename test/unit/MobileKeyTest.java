import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: fle13g
 * Date: 20/01/12
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MobileKeyTest extends TestCase {

    @Test
    public void testGetAndCheckKey() throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://localhost:8090/mobileauth/mobileKey/generateKey");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("userName", "chris.flemming.ala@gmail.com"));
        nvps.add(new BasicNameValuePair("password", "Purbrick7"));
        post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        HttpResponse response = httpClient.execute(post);
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();
        String responseContent = IOUtils.toString(entity.getContent());
        System.out.println(response.getFirstHeader("Content-Type"));
        System.out.println(responseContent);

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(responseContent);

        HttpPost post2 = new HttpPost("http://localhost:8090/mobileauth/mobileKey/checkKey");

        List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();
        nvps2.add(new BasicNameValuePair("userName", "chris.flemming.ala@gmail.com"));
        nvps2.add(new BasicNameValuePair("authKey", (String) jsonObj.get("authKey")));
        post2.setEntity(new UrlEncodedFormEntity(nvps2, HTTP.UTF_8));

        HttpResponse response2 = httpClient.execute(post2);
        assertEquals(200, response2.getStatusLine().getStatusCode());
    }

    @Test
    public void testCheckIncorrectKey() throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://localhost:8090/mobileauth/mobileKey/checkKey");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("userName", "chris.flemming.ala@gmail.com"));
        nvps.add(new BasicNameValuePair("authKey", "blah blah blah"));
        post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        HttpResponse response2 = httpClient.execute(post);
        assertEquals(403, response2.getStatusLine().getStatusCode());
    }
}
