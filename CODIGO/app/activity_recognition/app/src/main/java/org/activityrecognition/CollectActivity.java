package org.activityrecognition;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.activityrecognition.client.model.MeasureRequest;
import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelEvent;
import org.activityrecognition.client.model.ModelState;
import org.activityrecognition.measure.PacketListenerTrain;
import org.activityrecognition.measure.SensorCollectorForTrain;
import org.activityrecognition.user.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectActivity extends BaseActivity implements PacketListenerTrain {
    private final String TAG = "ACTREC_TRAIN";

    private int COLLECTION_MAX_PACKS = 60;
    private int sentDataPackets = 0;
    private TextView txtDataPackets;
    private SessionManager session;
    private ModelClient modelClient;
    private String userId;
    private SensorCollectorForTrain sensorPacketCollector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        txtDataPackets = findViewById(R.id.txt_data_packets);

        COLLECTION_MAX_PACKS = getIntent().getIntExtra("COLLECTION_MAX_PACKS", 60);
        userId = getIntent().getStringExtra("USER_ID");

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorPacketCollector = new SensorCollectorForTrain(sensorManager);
    }

    @Override
    protected void disableActions() {

    }

    @Override
    protected void updateView() {

    }

    private void showExplanationDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(CollectActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(getString(R.string.text_instructions_collect));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ACEPTAR",
                (dialog, which) -> {
                    sensorPacketCollector.registerListener(this);
                    sensorPacketCollector.start();
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    private void updateDataPackets() {
        txtDataPackets.setText(getString(R.string.data_packets, sentDataPackets));
    }

    @Override
    public void onPackageComplete(List<String> packet) {
        if (isOffline()) {
            interruptCollection();
        }

        if (sentDataPackets >= COLLECTION_MAX_PACKS) {
            endOfCollection();
        }

        // launch a thread with the http call to the external service
        MeasureRequest request = new MeasureRequest(packet);
        Call<Void> call = getModelClient().pushMeasures(session.getModelName(), userId, request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "packet pushed successfully!");
                    sentDataPackets++;

                    updateDataPackets();
                } else {
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to API. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void endOfCollection() {
        sensorPacketCollector.stop();
        sensorPacketCollector.unregisterListener();
        session.setSentDataPackets(0);
        if (userId.equals("1")) {
            sendModelTransition(ModelEvent.END_COLLECT_1);
        } else {
            sendModelTransition(ModelEvent.END_COLLECT_2);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(CollectActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage("Fin de la captura de datos");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ACEPTAR",
                (dialog, which) -> {
                    CollectActivity.this.finish();
                });
        alertDialog.show();
    }

    private void interruptCollection() {
        sensorPacketCollector.unregisterListener();
        sensorPacketCollector.stop();
        AlertDialog alertDialog = new AlertDialog.Builder(CollectActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage("Error de conexión a Internet. Intente continuar más tarde.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ACEPTAR",
                (dialog, which) -> {
                    CollectActivity.this.finish();
                });
        alertDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorPacketCollector.unregisterListener();
        sensorPacketCollector.stop();
        session.setSentDataPackets(sentDataPackets);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (session.getModelState() != ModelState.COLLECTING_1
                && session.getModelState() != ModelState.COLLECTING_2) {
            if (userId.equals("1")) {
                session.setModelState(ModelState.COLLECTING_1);
            } else {
                session.setModelState(ModelState.COLLECTING_2);
            }
            showExplanationDialog();
        } else {
            sentDataPackets = session.getSentDataPackets(0);
            sensorPacketCollector.registerListener(this);
            sensorPacketCollector.start();
        }

        updateDataPackets();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }
}
