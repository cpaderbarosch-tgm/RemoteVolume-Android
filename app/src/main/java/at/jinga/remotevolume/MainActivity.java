package at.jinga.remotevolume;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void connect(View view) {
        connect(((EditText) findViewById(R.id.ip)).getText().toString());
    }

    protected void connect(final String ip) {
        new Thread() {
            @Override
            public void run() {
                try {
                    ConnectionManager.server = new Socket(ip, 3131);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startActivity(new Intent(MainActivity.this, MixerActivity.class));
            }
        }.start();
    }
}
