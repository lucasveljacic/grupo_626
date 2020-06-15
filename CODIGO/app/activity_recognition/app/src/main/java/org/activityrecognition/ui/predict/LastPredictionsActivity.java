package org.activityrecognition.ui.predict;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.activityrecognition.BaseActivity;
import org.activityrecognition.R;

import java.util.ArrayList;
import java.util.List;

public class LastPredictionsActivity extends BaseActivity {
    private ListView predictionsListView;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_predictions_list);

        List<String> list = new ArrayList<>();
        list.add("prediction 1");
        list.add("prediction 2");

        List<String> lastPredictions = session.getLastPredictions();

        predictionsListView = findViewById(R.id.last_predictions);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lastPredictions);

        predictionsListView.setAdapter(adapter);

        session.checkLogin();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    protected void disableControls() {}

    @Override
    protected void updateView() {
    }
}
