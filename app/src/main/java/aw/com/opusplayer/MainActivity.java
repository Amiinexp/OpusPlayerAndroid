package aw.com.opusplayer;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

import aw.com.events.OpusMessageEvent;
import aw.com.utils.Converters;
import aw.com.utils.FileUtilities;
import top.oply.opuslib.OpusEvent;
import top.oply.opuslib.OpusPlayer;

public class MainActivity extends AppCompatActivity {

    ImageButton playPauseButton;
    ProgressBar progressBar;
    TextView durationText;
    TextView currentPositionText;
    ListView playList;

    OpusPlayer opusPlayer;
    OpusPlayerState playerState;

    public enum OpusPlayerState {
        NONE,
        PLAYING,
        PAUSED,
        FINISHED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerState = OpusPlayerState.NONE;

        copySampleFiles();
        initOpusPlayer();
        InitUI();
        EventBus.getDefault().register(this);
    }

    private void InitUI() {
        durationText = (TextView) findViewById(R.id.durationText);
        currentPositionText = (TextView) findViewById(R.id.currentPositionText);

        playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (playerState) {
                    case NONE:
                        opusPlayer.play(Environment.getExternalStorageDirectory() + "/OpusPlayer/sample2.opus");
                        break;
                    case PLAYING:
                        opusPlayer.pause();
                        break;
                    case PAUSED:
                        opusPlayer.resume();
                        break;
                    case FINISHED:
                        break;
                }
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.playerProgressBar);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Jump on the specific duration is not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });

        playList = (ListView) findViewById(R.id.playList);
    }

    private void initOpusPlayer() {
        opusPlayer = OpusPlayer.getInstance();
        opusPlayer.setEventSender(new OpusEvent(this));
        OpusReceiver receiver = new OpusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OpusEvent.ACTION_OPUS_UI_RECEIVER);
        registerReceiver(receiver, filter);
    }

    private void copySampleFiles() {
        // we have 2 sample files here, we will copy those sample into local, because that's currently the only way for OpusLib to play it
        String sample1Path = Environment.getExternalStorageDirectory() + "/OpusPlayer/sample1.opus";
        File file1 = new File(sample1Path);
        if (!file1.exists()) {
            try {
                FileUtilities.copyRAWtoSDCard(this, R.raw.sample1, sample1Path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String sample2Path = Environment.getExternalStorageDirectory() + "/OpusPlayer/sample2.opus";
        File file2 = new File(sample2Path);
        if (!file2.exists()) {
            try {
                FileUtilities.copyRAWtoSDCard(this, R.raw.sample2, sample2Path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnOpusEvent(OpusMessageEvent event) {
        switch (event.getOpusEventCode()) {
            case OpusEvent.PLAYING_STARTED:
                playPauseButton.setImageResource(R.drawable.pause);
                playerState = OpusPlayerState.PLAYING;
                durationText.setText(Converters.convertNumberToTimeDisplay((int) opusPlayer.getDuration()));
                break;
            case OpusEvent.PLAYING_FINISHED:
                playPauseButton.setImageResource(R.drawable.play);
                playerState = OpusPlayerState.FINISHED;
                break;
            case OpusEvent.PLAYING_PAUSED:
                playPauseButton.setImageResource(R.drawable.play);
                playerState = OpusPlayerState.PAUSED;
                break;
            case OpusEvent.PLAY_PROGRESS_UPDATE:
                double progress = (double) opusPlayer.getPosition() / opusPlayer.getDuration() * 100;
                progressBar.setProgress((int) progress);
                currentPositionText.setText(Converters.convertNumberToTimeDisplay((int) opusPlayer.getPosition()));
        }
    }


}
