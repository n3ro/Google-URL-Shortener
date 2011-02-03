package com.git.nero.goorl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class GooRl extends Activity implements OnClickListener{
    /** Called when the activity is first created. */

	private static final String API_URL_SHORTEN = "https://www.googleapis.com/urlshortener/v1/url";
	private static final String API_URL_EXPAND = "https://www.googleapis.com/urlshortener/v1/url?shortUrl=";
    EditText urlEdit, parsedUrlEdit;
    TextView eMessage;
    Button goButton;
    ClipboardManager cm;
    ProgressDialog progress;
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        urlEdit = (EditText) findViewById(R.id.urlEdit);
        parsedUrlEdit = (EditText) findViewById(R.id.parsedUrlEdit);
        goButton = (Button) findViewById(R.id.goShort);
        goButton.setOnClickListener(this);
        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        checkUrlInCm();	
    }
    
    protected void onResume() {
    	super.onResume();
    	checkUrlInCm();
    }
    
    public String postData(URL url) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(API_URL_SHORTEN);
        String gourl = "";

        log(url.toString());
        try {
            // Add your data
            httppost.setEntity(new StringEntity("{\"longUrl\": \""+url.toString()+"\"}"));
            //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httppost.setHeader("Content-Type", "application/json");
            // Execute HTTP Post Request
            String response = httpclient.execute(httppost, new BasicResponseHandler());
            log(response);
            
            gourl = parseShortenJson(response);
        } catch (ClientProtocolException e){
        	log(e.getMessage());
        } catch (IOException e) {
        	log(e.getMessage());
        }finally{
        	httpclient.getConnectionManager().shutdown();
        }
        return gourl;
    }
    
    public String getData(URL url){
        HttpClient httpclient = new DefaultHttpClient();
        String gourl = "";
        try {
            HttpGet httpget = new HttpGet(API_URL_EXPAND+url.toString());
            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);

            gourl = parseExpandedJson(responseBody);
            
        } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
		
		return gourl;
    	
    }
 
    void log(String text){
    	Log.i("*****************TAG: ", text);
    }
    
    void checkUrlInCm(){
    	log(cm.getText().toString());
    	if(cm.getText().toString().startsWith("http://")){
    		urlEdit.setText(cm.getText());
    	}
    }

	public String parseShortenJson(String json){
		/*
{
 "kind": "urlshortener#url",
 "id": "http://goo.gl/fbsS",
 "longUrl": "http://www.google.com/"
}
		 */
		
		String splitter = "\"id\": \"";
		int a = json.indexOf(splitter);
		String word = json.substring(a+splitter.length());
		word = word.substring(0, word.indexOf("\","));
		return word;
	}
	
	public String parseExpandedJson(String json){
		/*
{
 "kind": "urlshortener#url",
 "id": "http://goo.gl/fbsS",
 "longUrl": "http://www.google.com/",
 "status": "OK"
}
		 */
		
		String splitter = "\"longUrl\": \"";
		int a = json.indexOf(splitter);
		String word = json.substring(a+splitter.length());
		word = word.substring(0, word.indexOf("\","));
		return word;
	}

	

	private class DwlhortenURL extends AsyncTask<URL,String,Void> {

		String goUrl = "";
	     protected void onPostExecute(Void notUsed) {
	            pVisible();
	            parsedUrlEdit.setText(goUrl);
	     }

		protected Void doInBackground(URL... urls){
			if(urls[0].toString().indexOf("http://goo.gl/") >= 0){
				goUrl = getData(urls[0]);	
			}else{
			    goUrl = postData(urls[0]);
			}
			return null;
		}
		
		protected void onPreExecute() {
			pVisible();
		}
		protected void onProgressUpdate(String... values) {

		}
	 }
	
	
	public void pVisible(){
		if(progress.isShowing()){
			progress.dismiss();
		}else{
			progress.show();
		}
	}
	
	public void onClick(View v) {
        try{
        	URL url = new URL(urlEdit.getText().toString());
        	progress = new ProgressDialog(this);
			progress.setIndeterminate(true);
			progress.setMessage("Shortening! Please wait...");
        	log(url.toString());
		    new DwlhortenURL().execute(url);
		}catch (MalformedURLException e){
			Toast.makeText(this, "Bad url!", Toast.LENGTH_SHORT).show();
		}			
	}
}