package com.amit.lensconverter;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etSph, etCyl, etAxis;
    private MaterialButton btnConvert;
    private CardView resultCard, warningCard;
    private TextView tvResult, tvWarning;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        etSph = findViewById(R.id.etSph);
        etCyl = findViewById(R.id.etCyl);
        etAxis = findViewById(R.id.etAxis);
        btnConvert = findViewById(R.id.btnConvert);
        resultCard = findViewById(R.id.resultCard);
        warningCard = findViewById(R.id.warningCard);
        tvResult = findViewById(R.id.tvResult);
        tvWarning = findViewById(R.id.tvWarning);

        btnConvert.setOnClickListener(v -> calculateConversion());
    }

    private void calculateConversion() {
        resultCard.setVisibility(View.GONE);
        warningCard.setVisibility(View.GONE);

        String sphStr = etSph.getText().toString().trim();
        String cylStr = etCyl.getText().toString().trim();
        String axisStr = etAxis.getText().toString().trim();

        // 1. Validation: SPH is required
        if (sphStr.isEmpty()) {
            Toast.makeText(this, "Sphere (SPH) is required", Toast.LENGTH_SHORT).show();
            return;
        }

        double sph;
        try {
            sph = Double.parseDouble(sphStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid SPH format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Range validation for SPH
        if (sph < -20.00 || sph > 20.00) {
            showWarning("SPH out of range. Allowed: -20.00 to +20.00");
            return;
        }

        double cyl = 0.0;
        int axis = 0;

        // 2. Validation: CYL & Axis
        if (!cylStr.isEmpty()) {
            try {
                cyl = Double.parseDouble(cylStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid CYL format", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (cyl < -10.00 || cyl > 0) { // Usually CYL is negative in prescriptions
                showWarning("CYL out of range. Usually between -10.00 and 0.00");
                return;
            }

            // If CYL is present, Axis becomes mandatory
            if (axisStr.isEmpty()) {
                showWarning("Axis is required when Cylinder (CYL) is entered.");
                return;
            }
            
            try {
                axis = Integer.parseInt(axisStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Axis format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (axis < 1 || axis > 180) {
                showWarning("Axis must be between 1 and 180 degrees.");
                return;
            }
        }

        // 3. Medical Math: Vertex Distance Formula for Contact Lens
        // Formula: Fc = Fs / (1 - d * Fs)  where d = 0.012 meters (12mm)
        // For Plus power (Hyperopia), usually no change is needed up to +4.00, but exact formula applies.
        
        double contactSph;
        
        if (Math.abs(sph) < 4.00) {
            // Under 4.00, vertex distance doesn't change the power significantly
            contactSph = sph;
        } else {
            // Apply Vertex Formula
            double d = 0.012; // 12mm vertex distance
            contactSph = sph / (1 - (d * sph));
        }

        // 4. Rounding to nearest 0.25 diopters (Standard for Contact Lenses)
        contactSph = Math.round(contactSph * 4) / 4.0;
        
        // Warning for High Cylinder
        if (cyl < -2.75) {
            showWarning("High Cylinder detected (CYL < -2.75). Toric contact lenses are required. Please consult an optometrist.");
        }

        // 5. Display Result
        String resultText = formatPower(contactSph);
        
        if (cyl != 0) {
            // In basic conversion, CYL remains the same, but high CYL needs toric lenses
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
        warningCard.setVisibility(View.VISIBLE);
        tvWarning.setText("⚠️ " + msg);
    }
}
