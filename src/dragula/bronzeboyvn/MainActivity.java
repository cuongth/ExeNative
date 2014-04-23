package dragula.bronzeboyvn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
	private static final String LOCAL =
		"/data/data/dragula.bronzeboyvn/";
	
	// Keep these three constants in sync
	private static final String COMMAND_NAME = "ffmpeg";
	private static final String COMMAND_ARGS = "-f s16le -ar 22.05k -ac 1 -i ";
	private static final String COMMAND_IN = " testsound.raw ";
	private static final String COMMAND_OUT = " output.wav";
	private static final int COMMAND_BINARY = R.raw.ffmpeg;
	
	private TextView outputText;
    private Button lsButton;
    private Handler handler = new Handler();
    private TextView commandText;
    private Button commandButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        outputText = (TextView)findViewById(R.id.output);
        lsButton = (Button)findViewById(R.id.lsButton);
        lsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String output = exec("/system/bin/ls");
            	output(output);
            }
        });
        commandText = (TextView)findViewById(R.id.helloText);
        commandText.setText("Your binary:" + COMMAND_NAME + " " + COMMAND_ARGS + COMMAND_IN + COMMAND_OUT);
        commandButton = (Button)findViewById(R.id.helloButton);
        commandButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	output("Loading...");
            	Thread thread = new Thread(new Runnable() {
            		public void run() {
            			try {
    						saveRawToFile();
    						exec("/system/bin/chmod 744 " + LOCAL + COMMAND_NAME);
    					} catch (IOException e) {
    						e.printStackTrace();
    					}
    					output("Executing...");
    					String output = exec(LOCAL + COMMAND_NAME + " " + COMMAND_ARGS + COMMAND_IN + COMMAND_OUT);
    					output(output);
            		}
            	});
            	thread.start();
            }
    	});
    }
    
    private void saveRawToFile() throws IOException {
        File file = new File(LOCAL, COMMAND_NAME);
		if (!file.exists()) {
			InputStream input = getResources().openRawResource(COMMAND_BINARY);
			OutputStream output = new FileOutputStream(file);
	
			byte[] buffer = new byte[1024 * 4];
			int a;
			while((a = input.read(buffer)) > 0)
			    output.write(buffer, 0, a);
	
			input.close();
			output.close();
		}
	}
	
	private String exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

	private void output(final String str) {
	    Log.i(TAG, str);
        handler.post(new Runnable() {
        	public void run() {
        		outputText.setText(str);
        	}
        });
    }
}