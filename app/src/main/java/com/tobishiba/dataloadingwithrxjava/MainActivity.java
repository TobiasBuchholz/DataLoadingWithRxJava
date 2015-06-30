package com.tobishiba.dataloadingwithrxjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int RESPONSE_OK = 0;
    private TextView mTextView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.activity_main_text_view);
    }

    public void onClickLoadData(final View view) {
        clearTextView();
        handleLoadData();
    }

    private void clearTextView() {
        mTextView.setText("");
    }

    private void handleLoadData() {
        Observable.concat(getCacheObservable(), getNetworkObservable())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleShowData);
    }

    private Observable<List<String>> getCacheObservable() {
        return Observable.create(subscriber -> {
            subscriber.onNext(getDataFromDb());
            subscriber.onCompleted();
        });
    }

    private List<String> getDataFromDb() {
        return getData("from_db_");
    }

    private List<String> getData(final String dataFrom) {
        final List<String> data = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            data.add(dataFrom + i);
        }
        return data;
    }

    private Observable<List<String>> getNetworkObservable() {
        return Observable.create(subscriber -> {
            subscriber.onNext(getDataFromNetwork());
            subscriber.onCompleted();
        });
    }

    private List<String> getDataFromNetwork() {
        final int responseCode = makeLongRunningRequest();
        if(responseCode == RESPONSE_OK) {
            final List<String> dataFromNetwork = getData("from_network_");
            persistDataFromNetwork(dataFromNetwork);
            return dataFromNetwork;
        } else {
            return new ArrayList<>();
        }
    }

    private int makeLongRunningRequest() {
        runOnUiThread(() -> mTextView.append("Long running request in background...\n\n"));
        sleep(3000);
        return new Random().nextInt(2);
    }

    private void sleep(final int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    private void persistDataFromNetwork(final List<String> dataFromNetwork) {
        for(String data : dataFromNetwork) {
            runOnUiThread(() -> mTextView.append("Persist data in background: " + data + "\n"));
        }
    }

    private void handleShowData(final List<String> data) {
        if(data.isEmpty()) {
            mTextView.append("\nNothing did change at server.\n\n");
        } else {
            mTextView.append("\nShow data:\n" + data + "\n\n");
        }
    }
}
