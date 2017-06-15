package at.jinga.remotevolume;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MixerActivity extends AppCompatActivity {
    private Socket server;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receiveThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        server = ConnectionManager.server;

        try {
            out = new PrintWriter(server.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(server.getInputStream()));

            receiveThread = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String message = in.readLine();
                            Log.d("abc", message);

                            ((TextView) findViewById(R.id.message)).setText(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        disconnect(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_settings:
                break;

            case R.id.toolbar_refresh:
                break;

            case R.id.toolbar_disconnect:
                disconnect(null);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void send(View view) {
    }

    public void disconnect(View view) {
        if (server != null && server.isConnected()) {
            out.println("disconnect");
            out.close();

            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server = null;

            startActivity(new Intent(MixerActivity.this, MainActivity.class));
        }
    }
}
