package at.jinga.remotevolume;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MixerActivity extends AppCompatActivity {
    private boolean visible;
    private AppVolume[] tempApps;
    private ArrayList<Command> tempCommands = new ArrayList<>();

    private Socket server;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receiveThread;
    private Thread sendThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixer);

        visible = true;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (server == null) {
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

                                AppVolume[] apps = new Gson().fromJson(message, AppVolume[].class);

                                if (visible) {
                                    update(apps);
                                } else {
                                    tempApps = apps;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                receiveThread.start();

                sendThread = new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                if (tempCommands.size() > 0) {
                                    for (Command command : tempCommands) {
                                        send(command);
                                    }

                                    tempCommands.clear();
                                }

                                Thread.sleep(1000);
                            } catch (InterruptedException e) {}
                        }
                    }
                };
                sendThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        visible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;

        if (tempApps != null) {
            update(tempApps);
            tempApps = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        disconnect(null);
        super.onDestroy();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
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



    protected void send(Command command) {
        if (server != null && server.isConnected()) {
            out.print(new Gson().toJson(command));
            out.flush();
        }
    }

    public void disconnect(View view) {
        if (server != null && server.isConnected()) {
            receiveThread.interrupt();
            sendThread.interrupt();

            out.println("disconnect");

            out.close();
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server = null;
        }
    }

    public void update(final AppVolume[] apps) {
        MixerActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                LinearLayout mixer = (LinearLayout) findViewById(R.id.mixer);

                mixer.removeAllViews();

                mixer.addView(new PitcherView(MixerActivity.this, apps[0].Name, apps[0].Volume));

                for (int i = 1; i < apps.length; ++i) {
                    float volume = apps[0].Volume * apps[i].Volume / 100;

                    PitcherView pitcher = new PitcherView(MixerActivity.this, apps[i].Name, volume);

                    ((VerticalSeekBar) pitcher.findViewById(R.id.seekbar)).processId = apps[i].Id;

                    mixer.addView(pitcher);
                }

                for (int i = 0; i < mixer.getChildCount(); ++i) {
                    PitcherView pitcher = (PitcherView) mixer.getChildAt(i);
                    VerticalSeekBar seekbar = (VerticalSeekBar) pitcher.findViewById(R.id.seekbar);

                    final Integer pid = seekbar.processId;
                    final int i2 = i;

                    seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            LinearLayout mixer = (LinearLayout) findViewById(R.id.mixer);
                            PitcherView pitcher = (PitcherView) mixer.getChildAt(i2);

                            ((TextView) pitcher.findViewById(R.id.volume)).setText("" + progress);

                            // Check if a Command for the same App allready exists, if yes -> overwrite
                            int index = -1;
                            for (Command command : tempCommands) {
                                if (command.Id == pid) index = tempCommands.indexOf(command);
                            }

                            // Display Value to Real Value
                            float level;
                            if (pid != null) {
                                level = progress / apps[0].Volume * 100;

                                if (level > 100) {
                                    Command master_command = new Command(Action.ChangeVolume, null, false, progress);

                                    int index_master = -1;
                                    for (Command command : tempCommands) {
                                        if (command.Id == null) index_master = tempCommands.indexOf(command);
                                    }

                                    if (index_master == -1) {
                                        tempCommands.add(master_command);
                                    } else {
                                        tempCommands.set(index_master, master_command);
                                    }

                                    level = 100;
                                }
                            } else {
                                level = progress;
                            }

                            Command newCommand = new Command(Action.ChangeVolume, pid, false, level);
                            if (index == -1) {
                                tempCommands.add(newCommand);
                            } else {
                                tempCommands.set(index, newCommand);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }
            }
        });
    }
}
