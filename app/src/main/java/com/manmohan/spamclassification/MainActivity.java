package com.manmohan.spamclassification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Context context = this;

    Button classify_button;
    EditText message;
    TextView result_text;

    Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message_text);
        result_text = findViewById(R.id.result_text);

        classify_button = findViewById(R.id.classify_button);
        classify_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        classifier = new Classifier(context, "word_dict.json");
        classifier.setMaxlen(171);
        classifier.setCallback(new Classifier.DataCallback() {
            @Override
            public void onDataProcessed(HashMap<String, Integer> result) {
                String message_text = message.getText().toString().toLowerCase().trim();

                if (!TextUtils.isEmpty(message_text)) {
                    classifier.setVocabData(result);
                    List<Integer> tokenizedMessage = classifier.tokenize(message_text);
                    List<Integer> paddedMessage = classifier.padSequence(tokenizedMessage);
                    float[] results = classifySequence(paddedMessage);
                    float class1 = results[0];
                    float class2 = results[1];
                    result_text.setText("SPAM : "+class2+"\nNOT SPAM : "+class1);
                } else {
                    Toast.makeText(context,"Please enter a message.", Toast.LENGTH_LONG).show();
                }
            }
        });

        classifier.loadData();
    }

    private float[] classifySequence(List<Integer> paddedMessage) {
        try {
            Interpreter interpreter = new Interpreter(loadModelFile());

            Log.e("model","model loaded successfully");
            Log.e("model","model input size "+paddedMessage.size());

            float[] inputs = new float[paddedMessage.size()];

            String values = "";

            int index = 0;
            for (final Integer value: paddedMessage) {
                inputs[index++] = value;
                values += " "+value;
            }

            Log.e("model", "inputs " + values);

            float[][] outputs = new float[1][2];

            Log.e("model", "before model"+outputs[0][1]);

            interpreter.run(inputs , outputs);

            Log.e("model", "after model"+outputs[0][1]);
            return outputs[0];
        } catch (IOException e) {
            Log.e("model", "exception generated");
            e.printStackTrace();
        }
        return null;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "model.tflite";
        AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(MODEL_ASSETS_PATH) ;
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startoffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength);
    }
}
