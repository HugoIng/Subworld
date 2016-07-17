package com.deepred.subworld.views;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.R;
import com.deepred.subworld.engine.GameService;
import com.deepred.subworld.model.User;
import com.deepred.subworld.utils.ICallbacks;
import com.deepred.subworld.utils.MyUserManager;

/**
 *
 */
public class CharactersSelectionActivity extends AppCompatActivity implements ICallbacks.IUserCallbacks {

    private final static String TAG = "SW VIEWS ChrSelectAct  ";
    int chr_selected = ICommon.CHRS_NOT_SET;
    ImageButton arch;
    ImageButton fort;
    ImageButton spy ;
    ImageButton thief;
    TextView select1;
    TextView select2;
    AlphaAnimation txtAnim = null;
    EditText txt;
    Button butt;
    String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        MyUserManager.getInstance().register4UserNotifications(this);

        setContentView(R.layout.activity_characters_selection);

        arch = (ImageButton) findViewById(R.id.imageButton3);
        fort = (ImageButton) findViewById(R.id.imageButton2);
        spy = (ImageButton) findViewById(R.id.imageButton4);
        thief = (ImageButton) findViewById(R.id.imageButton5);

        select1 = (TextView) findViewById(R.id.textView8);
        select2 = (TextView) findViewById(R.id.textView9);

        txt = (EditText) findViewById(R.id.user_name);
        butt = (Button) findViewById(R.id.ok_butt);

        arch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(ICommon.CHRS_ARCHEOLOGIST);
            }
        });
        fort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(ICommon.CHRS_FORT_TELLER);
            }
        });
        spy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(ICommon.CHRS_SPY);
            }
        });
        thief.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(ICommon.CHRS_THIEF);
            }
        });


        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = txt.getText().toString();
                hideKeyboard();
                //Check chr selected and name
                if (chr_selected != ICommon.CHRS_NOT_SET && !name.isEmpty()) {
                    butt.setEnabled(false);

                    //Comprobar que el nombre no esta en uso en la BBDD
                    // Request background login with the service
                    Intent mServiceIntent = new Intent(CharactersSelectionActivity.this, GameService.class);
                    mServiceIntent.setData(Uri.parse(ICommon.CHECK_NAME));
                    mServiceIntent.putExtra(ICommon.NAME, name);
                    mServiceIntent.putExtra(ICommon.CHR_TYPE, chr_selected);
                    mServiceIntent.putExtra(ICommon.RESULT_RECEIVER, new ResultReceiver(null) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            if (resultCode == RESULT_OK) {
                                Intent intent = new Intent(getApplicationContext(), MapActivityImpl.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                CharactersSelectionActivity.this.startActivity(intent);
                            } else {
                                butt.setEnabled(true);
                                Toast.makeText(getApplicationContext(), "El nombre ya existe o no se ha podido guardar. Elija otro o pruebe de nuevo.", Toast.LENGTH_LONG).show();
                                // set focus en el texto
                                txt.requestFocus();
                                applyAnim(select2);
                            }
                        }
                    });
                    startService(mServiceIntent); // Starts the IntentService

                } else {
                    if (chr_selected == ICommon.CHRS_NOT_SET) {
                        Toast.makeText(getApplicationContext(), "Seleccione un personaje", Toast.LENGTH_LONG).show();
                        applyAnim(select1);

                    } else {
                        Toast.makeText(getApplicationContext(), "Escriba el nombre que desea usar", Toast.LENGTH_LONG).show();
                        // set focus en el texto
                        txt.requestFocus();
                        applyAnim(select2);
                    }
                }
            }
        });

        applyAnim(select1);
    }

    @Override
    protected void onStop(){
        super.onStop();
        MyUserManager.getInstance().unregister4UserNotifications(this);
    }

    public void applyAnim(TextView t) {
        if(txtAnim != null) {
            txtAnim.cancel();
        }

        txtAnim = new AlphaAnimation(0.3f, 0.9f);
        txtAnim.setDuration(800);
        txtAnim.setStartOffset(100);
        txtAnim.setFillAfter(true);
        txtAnim.setRepeatMode(Animation.REVERSE);
        txtAnim.setRepeatCount(Animation.INFINITE);
        t.startAnimation(txtAnim);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void select(int chrType) {
        chr_selected = chrType;
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 70);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        if (chr_selected == ICommon.CHRS_ARCHEOLOGIST) {
            arch.setBackgroundColor(Color.parseColor("#33ffffff"));
        } else {
            arch.setBackgroundColor(Color.TRANSPARENT);
        }

        if (chr_selected == ICommon.CHRS_FORT_TELLER) {
            fort.setBackgroundColor(Color.parseColor("#33ffffff"));
        } else {
            fort.setBackgroundColor(Color.TRANSPARENT);
        }

        if (chr_selected == ICommon.CHRS_SPY) {
            spy.setBackgroundColor(Color.parseColor("#33ffffff"));
        } else {
            spy.setBackgroundColor(Color.TRANSPARENT);
        }

        if (chr_selected == ICommon.CHRS_THIEF) {
            thief.setBackgroundColor(Color.parseColor("#33ffffff"));
        } else {
            thief.setBackgroundColor(Color.TRANSPARENT);
        }

        applyAnim(select2);
    }

    @Override
    public void onUserChange(User user) {
        Log.d("CHR CREATION SCR", "user changed");
    }

}
