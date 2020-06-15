package org.activityrecognition.ui.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

import org.activityrecognition.external.client.model.ModelState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SessionManager {
    private static SessionManager instance;
    private SharedPreferences pref;
    private Editor editor;
    private Context context;
    private final int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "AndroidHivePref";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    public static final String KEY_TOKEN = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_MODEL_NAME = "modelName";
    public static final String KEY_MODEL_STATE = "modelState";
    private static final String KEY_SENT_DATA_PACKETS = "sentDataPackets";
    private static final String KEY_LAST_PREDICTIONS = "lastPredictionsList";

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    private SessionManager(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String token, String email, String model){
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_MODEL_NAME, model);
        editor.commit();
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        return user;
    }

    public String getModelName() {
        String modelName = pref.getString(KEY_MODEL_NAME, null);
        if (modelName == null) {
            logoutUser();
        }
        return modelName;
    }

    public ModelState getModelState() {
        String state = pref.getString(KEY_MODEL_STATE, null);
        if (state == null) {
            return null;
        }
        return ModelState.valueOf(state);
    }

    synchronized public void setModelState(ModelState state) {
        editor.putString(KEY_MODEL_STATE, state.name());
        editor.commit();
    }

    synchronized public void setLastPredictions(List<String> lastPredictionsList) {
        Gson gson = new Gson();
        editor.putString(KEY_LAST_PREDICTIONS, gson.toJson(new LastPredictionsList(lastPredictionsList)));
        editor.commit();
    }

    synchronized public List<String> getLastPredictions() {
        String lastPredictionsJson = pref.getString(KEY_LAST_PREDICTIONS, null);
        if (lastPredictionsJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        LastPredictionsList lastPredictionsList = gson.fromJson(lastPredictionsJson, LastPredictionsList.class);
        return lastPredictionsList.getLastPredictions();
    }

    public int getSentDataPackets(int defValue) {
        return Integer.parseInt(pref.getString(KEY_SENT_DATA_PACKETS, String.valueOf(defValue)));
    }

    synchronized public void setSentDataPackets(int sentDataPackets) {
        editor.putString(KEY_SENT_DATA_PACKETS, String.valueOf(sentDataPackets));
        editor.commit();
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();

        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(i);
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    // clase utilizada para serializar la lista de Predicciones
    private class LastPredictionsList implements Serializable {
        private List<String> lastPredictions;

        public LastPredictionsList(List<String> lastPredictions) {
            this.lastPredictions = lastPredictions;
        }

        public List<String> getLastPredictions() {
            return lastPredictions;
        }

        public void setLastPredictions(List<String> lastPredictions) {
            this.lastPredictions = lastPredictions;
        }
    }
}
