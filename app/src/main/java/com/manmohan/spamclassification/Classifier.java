package com.manmohan.spamclassification;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Classifier {

    private Context context;
    private String filename;
    private DataCallback callback;
    private int maxlen;
    private HashMap<String , Integer> vocabData;

    public Classifier(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    public void loadData () {
        LoadVocabularyTask loadVocabularyTask = new LoadVocabularyTask(callback);
        loadVocabularyTask.execute(loadJSONFromAsset( filename));
    }

    private String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open(filename);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public ArrayList<Integer> tokenize(String message){
        List<String> parts = Arrays.asList(message.split(" "));
        ArrayList<Integer> tokenizedMessage = new ArrayList<Integer>();

        for (String part : parts) {
            if (!part.trim().equals("")){
                Integer index = Integer.valueOf(0);
                if (vocabData.get(part) == null) {
                    index = Integer.valueOf(0);
                }
                else{
                    index = vocabData.get(part);
                    Log.e("tokenize", part+ " " +String.valueOf(index));
                }
                tokenizedMessage.add(index);
            }
        }
        return tokenizedMessage;
    }


    public List<Integer> padSequence (List sequence) {

        if (sequence.size() > maxlen) {
            List<Integer> slicedArray = sequence.subList(0, maxlen);
            return slicedArray;
        }
        else if (sequence.size() < maxlen ) {
            for (int i = sequence.size(); i < maxlen; i++){
                sequence.add(Integer.valueOf(0));
            }
            return sequence;
        }
        else{
            return sequence;
        }
    }

    public void setMaxlen(int maxlen) {
        this.maxlen = maxlen;
    }

    public void setVocabData(HashMap<String, Integer> vocabData) {
        this.vocabData = vocabData;
    }

    public void setCallback(DataCallback callback) {
        this.callback = callback;
    }

    interface DataCallback {
        void onDataProcessed(HashMap<String, Integer> result);
    }

    public class LoadVocabularyTask extends AsyncTask<String, Void, HashMap<String, Integer>>{

        private DataCallback callback;

        public LoadVocabularyTask(DataCallback callback) {
            this.callback = callback;
        }

        @Override
        protected HashMap<String, Integer> doInBackground(String... strings) {
            HashMap<String, Integer> data = null;
            try {
                JSONObject jsonObject = new JSONObject(strings[0]);
                Iterator<String> iterator = jsonObject.keys();

                data = new HashMap<String , Integer>();

                while (iterator.hasNext()) {
                    String key = iterator.next();
                    data.put( key , new Integer(jsonObject.getInt(key)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(HashMap<String, Integer> stringIntegerHashMap) {
            super.onPostExecute(stringIntegerHashMap);

            callback.onDataProcessed(stringIntegerHashMap);
        }
    }
}
