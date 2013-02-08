package mobileauth

import org.apache.http.client.HttpClient;
import org.apache.http.NameValuePair
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.entity.UrlEncodedFormEntity;

class MobileKeyController {

    static allowedMethods = [getKey: "POST", checkKey: "POST"]

    /**
     * Create a key fo
     * this user.
     */
    def generateKey = {
        log.info("Authenticate request received for....." +params.userName)
        try {
            HttpClient http = new DefaultHttpClient()
            HttpPost post = new HttpPost("https://auth.ala.org.au/cas/v1/tickets")
            String userName = params.userName.toString().toLowerCase()
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("username", userName));
            nvps.add(new BasicNameValuePair("password", params.password));
            post.setEntity(new UrlEncodedFormEntity(nvps));

            def alaAuthResponse = http.execute(post)
            int statusCode = alaAuthResponse.getStatusLine().getStatusCode();
            //println("CAS response: " + statusCode)
          //  println(alaAuthResponse.getStatusLine().getReasonPhrase());
            if (statusCode == 201) {
                //persist the key
               // println("Sending a 201 to client.....")
                String authKey = UUID.randomUUID().toString()
                //add the user and auth key
                MobileUser user = MobileUser.findByUserName(userName)
                if (user == null) {
                    user = new MobileUser(userName: userName)
                    user.save(flush: true)
                }
                (new AuthKey([mobileUser: user, key: authKey])).save(flush: true)
                [authKey: authKey]
            } else {
                response.sendError(400)
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
        }
    }

    /**
       * Check this key combination.
       */
    def checkKey = {
        //check the user, check the auth key
        log.info("Check key request received for....." +params.userName + ", key: " + params.authKey)
        //println("Check record for...." + params.userName + " with key " + params.authKey)
        MobileUser user = MobileUser.findByUserName(params.userName)
       // println(user)

        AuthKey authKey = AuthKey.findByKeyAndMobileUser(params.authKey, user)
        if (authKey == null) {
         //   println("Unable to add an observation.")
            log.info("Sending error for check key requets. Key combination not recognised")
            response.sendError(403)
        }
    }
}