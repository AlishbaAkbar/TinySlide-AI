package com.tinyslide.offline;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private TinySlideEngine engine;
    private EditText inputText;
    private TextView statusText;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        loadEngine();
    }

    private void buildUi() {
        int padding = dp(16);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, padding);
        scrollView.addView(container);

        TextView title = new TextView(this);
        title.setText("TinySlide Offline");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.START);
        container.addView(title, matchWrap());

        statusText = new TextView(this);
        statusText.setText("Loading offline ONNX model...");
        statusText.setTextSize(14);
        statusText.setPadding(0, dp(8), 0, dp(12));
        container.addView(statusText, matchWrap());

        inputText = new EditText(this);
        inputText.setHint("Paste notes here...");
        inputText.setMinLines(8);
        inputText.setGravity(Gravity.TOP | Gravity.START);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        inputText.setText(sampleText());
        container.addView(inputText, matchWrap());

        Button generateButton = new Button(this);
        generateButton.setText("Generate Slides");
        generateButton.setAllCaps(false);
        generateButton.setOnClickListener(view -> generateSlides());
        container.addView(generateButton, matchWrap());

        outputText = new TextView(this);
        outputText.setTextSize(13);
        outputText.setTypeface(Typeface.MONOSPACE);
        outputText.setPadding(0, dp(12), 0, 0);
        container.addView(outputText, matchWrap());

        setContentView(scrollView);
    }

    private void loadEngine() {
        try {
            engine = new TinySlideEngine(this);
            statusText.setText("Offline model loaded: ONNX Runtime, no internet required.");
            generateSlides();
        } catch (Exception exception) {
            statusText.setText("Model load failed: " + exception.getMessage());
        }
    }

    private void generateSlides() {
        if (engine == null) {
            outputText.setText("Model is not loaded yet.");
            return;
        }

        String text = inputText.getText().toString().trim();
        if (text.isEmpty()) {
            outputText.setText("Please enter notes first.");
            return;
        }

        try {
            JSONObject result = engine.makeSlides(text);
            outputText.setText(result.toString(2));
        } catch (Exception exception) {
            outputText.setText("Generation failed: " + exception.getMessage());
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private String sampleText() {
        return "To solve this gridlock, the ultimate solution lies in transitioning from traditional, static traffic management to an intelligent system. "
                + "Our approach to building this system relies on integrating real-time Internet of Things sensors, predictive machine learning, and control dashboards. "
                + "The key features of this technology include adaptive traffic lights that change intervals based on actual vehicle density. "
                + "Implementing this requires a precise, step-by-step method: first, we map the city's worst bottlenecks; second, we deploy sensors. "
                + "Our long-term plan and strategy focus on a phased rollout, starting with a high-density downtown pilot zone before scaling. "
                + "Ultimately, by transforming chaotic streets into an intelligent, self-optimizing network, this strategy clears congestion and improves mobility.";
    }

    @Override
    protected void onDestroy() {
        if (engine != null) {
            try {
                engine.close();
            } catch (Exception ignored) {
            }
        }

        super.onDestroy();
    }
}
