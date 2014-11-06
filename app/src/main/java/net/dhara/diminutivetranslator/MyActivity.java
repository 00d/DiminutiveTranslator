package net.dhara.diminutivetranslator;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MyActivity extends Activity {

    //EditText translationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickTranslate(View view) {
        EditText translateEditText = (EditText) findViewById(R.id.editText);

        if (!isEmpty(translateEditText)) {
            Toast.makeText(this, "Translating...", Toast.LENGTH_LONG).show();   //user knows app is working
            new SaveTheFeed().execute();
        } else {
            //no text was entered
            Toast.makeText(this, "Enter text to translate", Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean isEmpty(EditText edit) {
        return edit.getText().toString().trim().length() == 0;
    }

    //get background data without locking down interface
    class SaveTheFeed extends AsyncTask<Void, Void, Void> {
        String jsonString = "";
        String result = "";

        @Override
        protected Void doInBackground(Void... params) {
            EditText translateEditText = (EditText) findViewById(R.id.editText);
            String originalText = translateEditText.getText().toString();

            originalText = originalText.replace(" ","+");   //replace spaces between words with +

            DefaultHttpClient httpCl = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost =
                    new HttpPost("http://newjustin.com/translateit.php?action=translations&english_words=" + originalText);
            httpPost.setHeader("Content-type", "application/json");

            InputStream inputStream = null; //input bytes from url

            try {
                HttpResponse response = httpCl.execute(httpPost);

                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();

                //using UTF-8 characters with this web service
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder build = new StringBuilder();
                String textline = null;

                while ((textline = reader.readLine()) != null) {
                    build.append(textline + "\n");
                }

                jsonString = build.toString();
                JSONObject jsonObj = new JSONObject(jsonString);
                JSONArray jsonArr = jsonObj.getJSONArray("translations");

                outputTranslations(jsonArr);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {  //sends to user interface
            TextView translations = (TextView) findViewById(R.id.translationTextView);
            translations.setText(result);
        }

        protected void outputTranslations(JSONArray jsonArr) {
            //specific languages used in web service
            String[] languages = {"arabic", "chinese", "danish", "dutch", "french", "german",
                    "italian", "portuguese", "russian", "spanish"};

            //languages are key name
            try {
                for (int i=0; i<jsonArr.length(); i++) {
                    //each piece in json array is an object
                    JSONObject translationObj = jsonArr.getJSONObject(i);
                    //languages[] array index is already in order, above
                    result = result + languages[i] + ": " + translationObj.getString(languages[i]) + "\n";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
