package org.activityrecognition;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.activityrecognition.client.model.MeasureRequest;
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
    private final String TAG = "ACTREC_COLLECT";

    private final String USER_1 = "1";
    private final String USER_2 = "2";

    private int COLLECTION_MAX_PACKS;
    private int sentDataPackets = 0;
    private TextView txtDataPackets;
    private String userId;
    private SensorCollectorForTrain sensorPacketCollector;
    private boolean actionsStopped;

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

        switch (session.getModelState()) {
            case NEW:
                session.setModelState(ModelState.COLLECTING_1);
                showExplanationDialog();
                break;
            case COLLECTED_1:
                session.setModelState(ModelState.COLLECTING_2);
                showExplanationDialog();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopActivityActions();
        session.setSentDataPackets(sentDataPackets);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopActivityActions();
        session.setSentDataPackets(sentDataPackets);
    }

    @Override
    public void onResume() {
        super.onResume();
        sentDataPackets = session.getSentDataPackets(0);
        startActivityActions();
        updateView();
    }

    @Override
    protected void updateView() {
        txtDataPackets.setText(getString(R.string.data_packets, sentDataPackets));
    }

    private void stopActivityActions() {
        sensorPacketCollector.unregisterListener();
        sensorPacketCollector.stop();
    }

    private void startActivityActions() {
        sensorPacketCollector.registerListener(this);
        sensorPacketCollector.start();
    }

    private void showExplanationDialog() {
        actionsStopped = true;
        AlertDialog alertDialog = new AlertDialog.Builder(CollectActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(getString(R.string.text_instructions_collect));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.accept_btn_msg),
                (dialog, which) -> {
                    actionsStopped = false;
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    @Override
    public void onPackageComplete(List<String> packet) {
        if (actionsStopped) {
            return;
        }

        if (isOffline()) {
            interruptCollectionOnOffline();
        }

        if (sentDataPackets >= COLLECTION_MAX_PACKS-1) {
            endOfCollection();
        }

        // launch a thread with the http call to the external service
        MeasureRequest request = new MeasureRequest(packet);
        getModelClient().pushMeasures(session.getModelName(), userId, request)
            .enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (!actionsStopped) {
                        Log.i(TAG, "packet pushed successfully!");
                        incrementSentDataPackets();
                        updateView();
                    }
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
        stopActivityActions();
        if (userId.equals(USER_1)) {
            sendModelTransition(ModelEvent.END_COLLECT_1);
        } else {
            sendModelTransition(ModelEvent.END_COLLECT_2);
        }

        AlertDialog alertDialog = buildDialog(getString(R.string.end_of_data_collection_msg));
        alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "ACEPTAR",
                (dialog, which) -> {
                    resetSentDataPackets();
                    CollectActivity.this.finish();
                });
        alertDialog.show();
    }

    private void interruptCollectionOnOffline() {
        stopActivityActions();
        AlertDialog alertDialog = buildDialog(getString(R.string.offline_error_msg));
        alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "ACEPTAR",
                (dialog, which) -> CollectActivity.this.finish());
        alertDialog.show();
    }

    private AlertDialog buildDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(CollectActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(message);
        return alertDialog;
    }

    @Override
    protected void disableControls() {}

    private void incrementSentDataPackets() {
        synchronized (this) {
            sentDataPackets++;
            session.setSentDataPackets(sentDataPackets);
        }
    }

    private void resetSentDataPackets() {
        synchronized (this) {
            sentDataPackets = 0;
            session.setSentDataPackets(sentDataPackets);
        }
    }
}
