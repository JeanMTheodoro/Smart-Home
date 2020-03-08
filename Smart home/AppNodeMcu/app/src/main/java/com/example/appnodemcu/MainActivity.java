package com.example.appnodemcu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {

    public static final String TAG_LOG = "LOG_TTS";
    private TextToSpeech tts;

    TextView fire;
    ImageButton bntQuarto, bntRecord, bntRoom, bntLaunch, bntW;

    Handler handler = new Handler();
    boolean statusRecebidos = true;


    boolean statusBntSala = false;
    boolean statusBntQuarto = false;
    boolean statusBntCozinha = false;
    boolean statusBntWc = false;
    String capturaFala = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bntQuarto = (ImageButton) findViewById(R.id.bntQuarto);
        bntRoom = (ImageButton) findViewById(R.id.bntRoom);
        bntLaunch = (ImageButton) findViewById(R.id.bntLunch);
        bntW = (ImageButton) findViewById(R.id.bntW);
        bntRecord = (ImageButton) findViewById(R.id.bnt_Record);
        fire = (TextView) findViewById(R.id.fire);



        handler.postDelayed(refresh, 0);


        bntRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                capturaFala();
            }
        });


        bntQuarto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (statusBntQuarto) {

                    Solicita("/gpio/off");
                } else {
                    Solicita("/gpio/on");
                }

            }
        });

        bntRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (statusBntSala) {

                    Solicita("/gpio1/off");
                } else {
                    Solicita("/gpio1/on");
                }
            }
        });

        bntLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (statusBntCozinha) {

                    Solicita("/gpio2/off");
                } else {
                    Solicita("/gpio2/on");
                }

            }
        });

        bntW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statusBntWc) {

                    Solicita("/gpio3/off");
                } else {

                    Solicita("/gpio3/on");
                }


            }
        });


    }



    private Runnable refresh = new Runnable() {
        @Override
        public void run() {

            if (statusRecebidos) {
                Solicita("");
                handler.postDelayed(this, 500);

                Log.d("status", "solicitado");

            } else {

                handler.removeCallbacks(refresh);
                Log.d("Status", "finalizado");


            }

        }
    };

    @Override
    protected void onDestroy() {

        if(tts != null){
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
        statusRecebidos = false;

    }

    private void capturaFala() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "FALE NATURALMENTE...");

        try {
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Reconhecimento de voz não suportado", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 1) {

            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                 capturaFala = result.get(0);

                Log.i(TAG_LOG, "Sitentiza: " + capturaFala);

                processingMachineLearning(capturaFala);
            }
        }
    }

    private void processingMachineLearning(String capturaFala) {

        ArrayList<String> command = new ArrayList();
        command.add("acender meu quarto");
        command.add("desligar meu quarto");
        command.add("acender sala");
        command.add("Desligar sala");
        command.add("acender cozinha");
        command.add("desligar cozinha");
        command.add("acender banheiro");
        command.add("desligar banheiro");


        ArrayList<String> gpios = new ArrayList();
        gpios.add("/gpio/on");
        gpios.add("/gpio/off");
        gpios.add("/gpio1/on");
        gpios.add("/gpio1/off");
        gpios.add("/gpio2/on");
        gpios.add("/gpio2/off");
        gpios.add("/gpio3/on");
        gpios.add("/gpio3/off");


        int position = 0;



        try {
            if(command.contains(capturaFala)){
                position = command.indexOf(capturaFala);
            }
        }catch (NullPointerException e){

            Toast.makeText(this, "Não reconheci a frase que você informou, repita novamente", Toast.LENGTH_SHORT).show();
            position = 0;

        }


        Solicita(gpios.get(position));
    }


    public void Solicita(String comando) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnected()) {


            String url = "http://192.168.15.150";
            new SolicitarDados().execute(url + comando);


        } else {
            Toast.makeText(MainActivity.this, "Nenhuma conexão foi detectada", Toast.LENGTH_SHORT).show();
        }

    }


    private class SolicitarDados extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            return Conexao.getDados(url[0]);
        }

        @Override
        protected void onPostExecute(String resultado) {

            if (resultado != null) {

                //Toast.makeText(MainActivity.this,"Funcionou", Toast.LENGTH_LONG).show();

                if (resultado.contains("meuQuartoOn")) {
                    bntQuarto.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                    statusBntQuarto = true;
                } else if (resultado.contains("meuQuartoOff")) {
                    bntQuarto.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                    statusBntQuarto = false;
                }

                if (resultado.contains("salaOn")) {
                    bntRoom.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                    statusBntSala = true;
                } else if (resultado.contains("salaOff")) {
                    bntRoom.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                    statusBntSala = false;
                }

                if (resultado.contains("cozinhaOn")) {
                    bntLaunch.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                    statusBntCozinha = true;
                } else if (resultado.contains("cozinhaOf")) {
                    bntLaunch.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                    statusBntCozinha = false;
                }

                if (resultado.contains("wcOn")) {
                    bntW.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                    statusBntWc = true;
                } else if (resultado.contains("wcOff")) {
                    bntW.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                    statusBntWc = false;
                }

                String[] data_receive = resultado.split(",");
                String sensorFire = data_receive[4];
                String sensorGas = data_receive[5];

                if (sensorFire.equals("fogoOn") && sensorGas.equals("gasOn")) {


                    abrirDialog(0);

                } else if (sensorGas.equals("gasOn")) {

                    abrirDialog(1);

                } else if (sensorFire.equals("fogoOn")) {

                    abrirDialog(2);

                }


            } else {
                Toast.makeText(MainActivity.this, "Ocorreu Erro no Servidor", Toast.LENGTH_LONG).show();


            }


        }
    }



    private void abrirDialog(int mensagem) {

        String word [] = {"Risco de explosão", "Fazamento de gás", "Risco de incêndio"};

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);


        dialog.setTitle("Alerta");
        dialog.setMessage(word[mensagem]);


        dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });

        dialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(MainActivity.this, "não", Toast.LENGTH_SHORT).show();

            }
        });

        dialog.create();
        dialog.show();


    }
}
