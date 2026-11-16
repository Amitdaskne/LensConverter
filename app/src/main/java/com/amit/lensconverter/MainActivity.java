package com.amit.lensconverter;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etSph, etCyl, etAxis;
    private View resultCard;
    private TextView tvResult, tvWarning;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        etSph = findViewById(R.id.etSph);
        etCyl = findViewById(R.id.etCyl);
        etAxis = findViewById(R.id.etAxis);
        resultCard = findViewById(R.id.resultCard);
        tvResult = findViewById(R.id.tvResult);
        tvWarning = findViewById(R.id.tvWarning);

        findViewById(R.id.btnConvert).setOnClickListener(v -> calculateConversion());
    }

    private void calculateConversion() {
        resultCard.setVisibility(View.GONE);
        tvWarning.setVisibility(View.GONE);

        String sphStr = etSph.getText().toString().trim();
        String cylStr = etCyl.getText().toString().trim();
        String axisStr = etAxis.getText().toString().trim();

        if (sphStr.isEmpty()) {
            Toast.makeText(this, "Please enter Sphere (SPH)", Toast.LENGTH_SHORT).show();
            return;
        }

        double sph;
        try {
            sph = Double.parseDouble(sphStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid SPH format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sph < -20.00 || sph > 20.00) {
            showWarning("SPH out of range (-20.00 to +20.00)");
            return;
        }

        double cyl = 0.0;
        int axis = 0;

        if (!cylStr.isEmpty()) {
            try { cyl = Double.parseDouble(cylStr); } 
            catch (NumberFormatException e) { return; }
            
            if (cyl < -10.00 || cyl > 0) {
                showWarning("CYL usually ranges from -10.00 to 0.00");
                return;
            }

            if (axisStr.isEmpty()) {
                showWarning("Axis is required when CYL is entered.");
                return;
            }
            
            try { axis = Integer.parseInt(axisStr); } 
            catch (NumberFormatException e) { return; }

            if (axis < 1 || axis > 180) {
                showWarning("Axis must be between 1 and 180.");
                return;
            }
        }

        double contactSph;
        if (Math.abs(sph) < 4.00) {
            contactSph = sph;
        } else {
            double d = 0.012;
            contactSph = sph / (1 - (d * sph));
        }

        contactSph = Math.round(contactSph * 4) / 4.0;
        
        if (cyl < -2.75) {
            showWarning("High Cylinder: Toric contact lenses required. Consult an optometrist.");
        } else if (cyl == 0) {
             tvWarning.setVisibility(View.GONE);
        }

        String resultText = formatPower(contactSph);
        if (cyl != 0) {
            resultText += " / " + formatPower(cyl) + " x " + axis;
        }

        resultCard.setVisibility(View.VISIBLE);
        tvResult.setText(resultText);
    }

    private String formatPower(double power) {
        if (power == 0) return "Plano (0.00)";
        String sign = power > 0 ? "+" : "";
        return sign + String.format("%.2f", power);
    }

    private void showWarning(String msg) {
        resultCard.setVisibility(View.VISIBLE);
        tvResult.setText("--");
        tvWarning.setVisibility(View.VISIBLE);
        tvWarning.setText(msg);
    }
}
