package mimo.krokodil.stringadvanced;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    
    private static Button[] buttons;
    private static int rightButton;
    private Random random;
    private ImageView imageView;
    private Bitmap image;
    private static String text;
    private String url;
    private static ArrayList<Integer> arrayList;
    private String block;
    private String imageURL;
    private String rightButtonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }


        image = null;

        buttons = new Button[4];
        buttons[0] = findViewById(R.id.button1);
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);

        imageView = findViewById(R.id.imageView);


        reload();
    }

    public void onClick(View view) {
        Button button = (Button) view;
        if(button.equals(buttons[rightButton])) {
            Toast.makeText(getApplicationContext(), "You are right!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "You are wrong... Try again!", Toast.LENGTH_SHORT).show();
        }
        reload();
    }


    private void reload() {
        random = new Random();
        clearViews();


        //initializing core variables
        url = "https://www.listchallenges.com/200-most-famous-people-of-all-time";

        //Set for managing unique buttons numbers
        Set<Integer> setNumbers = new HashSet<>();
        while (setNumbers.size() < 4) {
            try {
                int temp = random.nextInt(39) + 1;
                setNumbers.add(temp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i("info", String.valueOf(setNumbers));
        arrayList = new ArrayList<>(setNumbers);
        rightButton = random.nextInt(3);

        //initializing async tasks
        DownloadTask downloadTask = new DownloadTask();
        DownloadImageTask downloadImageTask = new DownloadImageTask();
        MatcherTask matcherTask = new MatcherTask();

        //getting TEXT variable with page source code
        String block = "";
        try {
            text = downloadTask.execute(url).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String[] strings = matcherTask.execute().get();
            imageURL = strings[0];
            rightButtonText = strings[1];
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            image = downloadImageTask.execute("https://www.listchallenges.com/" + imageURL).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(image);
    }

    private void clearViews() {
        for (int i = 0; i < 4; i++) {
            buttons[i].setText("");
        }
        imageView.setImageResource(R.drawable.unnamed);
    }

    @SuppressWarnings("deprecation")
    private static class MatcherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            String imageURL = "";
            String rightButtonText = "";
            String buttonText = "";
            String block = null;

            for(int i = 0; i < 4; i++) {
                String blockRegex = "<div class=\"item-rank\">%d</div>(.*)<div class=\"item-name\">";
                blockRegex = String.format(Locale.getDefault(), blockRegex, arrayList.get(i));
                Pattern blockPattern = Pattern.compile(blockRegex);
                Matcher blockMatcher = blockPattern.matcher(text);
                if(blockMatcher.find()) {
                    block = blockMatcher.group(1);
                    Pattern textPattern = Pattern.compile("alt=\"(.*?)\"");
                    Matcher textMatcher = textPattern.matcher(block);
                    if(textMatcher.find()) {
                        buttonText = textMatcher.group(1);
                        buttons[i].setText(buttonText);
                    }
                    if(i == rightButton) {
                        Pattern imagePattern = Pattern.compile("src=\"(.*?)\"");
                        Matcher imageMatcher = imagePattern.matcher(block);
                        if(imageMatcher.find()) {
                            imageURL = imageMatcher.group(1);
                            rightButtonText = buttonText;
                        }
                    }

                }
            }
            return new String[]{imageURL, rightButtonText};
        }
    }

    @SuppressWarnings("deprecation")
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            Bitmap bitmap = null;

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch(IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    @SuppressWarnings("deprecation")
    private static class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            BufferedReader bufferedReader;
            try{
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);


                String line = bufferedReader.readLine();
                while(line != null) {
                    //Log.i("Debug", line);
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                httpURLConnection.disconnect();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }
    }
}