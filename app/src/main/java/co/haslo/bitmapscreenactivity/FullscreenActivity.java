package co.haslo.bitmapscreenactivity;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import co.haslo.bitmapscreenactivity.util.Dlog;

import static android.graphics.ColorSpace.match;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    public Dlog mDlog = new Dlog(this);
    public FullscreenController mFullscreenController;

    TextView timerView;
    Button setTimerButton;
    Button timerControlButton;
    Button timerClearButton;
    Button setImageButton;
    ImageView bitmapImage;

    Timer timer;
    TimerTask timerTask;

    Thread timeThread = null;
    Boolean isRunning = true;


    Bitmap bmp;

    int colorAccent;
    int colorWhite;
    int colorWhiteDark;
    int viewData = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        if(mDlog != null){
            boolean isDebuggable = Dlog.isDebuggable();
            Dlog.d("Debugging Status: "+isDebuggable);
        }
        colorAccent = ContextCompat.getColor(this, R.color.colorAccent);
        colorWhite = ContextCompat.getColor(this, R.color.colorWhite);
        colorWhiteDark = ContextCompat.getColor(this, R.color.colorWhiteDark);

        mFullscreenController = new FullscreenController(this);
        mFullscreenController.initialize();

        timerView = findViewById(R.id.timer_view);
        setTimerButton = findViewById(R.id.button_set_timer);
        timerControlButton = findViewById(R.id.button_timer_control);
        timerClearButton = findViewById(R.id.button_timer_clear);
        setImageButton = findViewById(R.id.button_set_image);
        bitmapImage = findViewById(R.id.bitmap_view);

        setTimerButton.setOnClickListener(new Button.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);

                if(!isRunning) isRunning = true;
                timeThread = new Thread(new timeThread());
                timeThread.start();
                setTimerButton.setEnabled(false);
                setTimerButton.setTextColor(colorWhiteDark);
                timerControlButton.setText("PAUSE");
                timerControlButton.setEnabled(true);
                timerControlButton.setTextColor(colorWhite);
                timerClearButton.setEnabled(true);
                timerClearButton.setTextColor(colorWhite);
            }
        });

        timerControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = !isRunning;
                if (isRunning) {
                    timerControlButton.setText("PAUSE");
                } else {
                    timerControlButton.setText("START");
                }
            }
        });

        timerClearButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                bitmapImage.setImageBitmap(null);

                setTimerButton.setEnabled(true);
                setTimerButton.setTextColor(colorAccent);
                timerControlButton.setEnabled(false);
                timerControlButton.setTextColor(colorWhiteDark);
                timerClearButton.setEnabled(false);
                timerClearButton.setTextColor(colorWhiteDark);
                timeThread.interrupt();
                timerView.setText("");
                timerView.setText("00:00:00:0");
            }
        });

        setImageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmapImage.setImageBitmap(bmp);
            }
        });

    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int mSec = msg.arg1 % 10;
            int sec = (msg.arg1 / 10) % 60;
            int min = (msg.arg1 / 10) / 60;
            int hour = (msg.arg1 / 10) / 360;
            //1초 세기
//            int sec = msg.arg1 % 60;
//            int min = (msg.arg1/60) % 60;
//            int hour = (msg.arg1) / 3600;
            //1000이 1초 1000*60 은 1분 1000*60*10은 10분 1000*60*60은 한시간

//            @SuppressLint("DefaultLocale") final String result = String.format("%02d:%02d:%02d", hour, min, sec);
            @SuppressLint("DefaultLocale") final String result = String.format("%02d:%02d:%02d:%01d", hour, min, sec, mSec);
//            if (result.equals("00:01:15:00")) {
//                Toast.makeText(FullscreenActivity.this, "1분 15초가 지났습니다.", Toast.LENGTH_SHORT).show();
//            }

            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    timerView.setText(result);
                    bitmapImage.setImageBitmap(bmp);
                }
            });
        }
    };

    public class timeThread implements Runnable {
        @Override
        public void run() {
            int i = 0;

            while (true) {
                while (isRunning) { //일시정지를 누르면 멈춤
                    Message msg = new Message();
                    msg.arg1 = i++;
                    handler.sendMessage(msg);
                    setBitmapData();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Toast.makeText(FullscreenActivity.this, "NEW TIMER HAS BEEN LAUNCHED", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return; // 인터럽트 받을 경우 return
                    }
                }
            }
        }
    }



    void setBitmapData() {
        int col = 640;
        int row = 480;

        for(int x = 0; x < col; x++){
            for(int y = 0; y < row; y++){
                bmp.setPixel(x, y, packRGB(viewData, viewData, viewData));
            }
        }
        if(viewData>255){
            viewData = 0;
        } else {
            viewData++;
        }

    }

    private static int packRGB(int r, int g, int b) {
        return 0xff000000 | r << 16 | g << 8 | b;
    }



}
