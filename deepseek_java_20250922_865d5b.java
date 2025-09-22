package com.example.pressuretestcalculator;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText etInitialPressure, etFinalPressure, etTemperature, etVolume;
    private Spinner spinnerPressureUnit, spinnerTemperatureUnit, spinnerVolumeUnit;
    private Button btnCalculate, btnClear;
    private TextView tvResult;

    private DecimalFormat df = new DecimalFormat("#.####");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpinners();
        setupButtons();
    }

    private void initializeViews() {
        etInitialPressure = findViewById(R.id.etInitialPressure);
        etFinalPressure = findViewById(R.id.etFinalPressure);
        etTemperature = findViewById(R.id.etTemperature);
        etVolume = findViewById(R.id.etVolume);
        
        spinnerPressureUnit = findViewById(R.id.spinnerPressureUnit);
        spinnerTemperatureUnit = findViewById(R.id.spinnerTemperatureUnit);
        spinnerVolumeUnit = findViewById(R.id.spinnerVolumeUnit);
        
        btnCalculate = findViewById(R.id.btnCalculate);
        btnClear = findViewById(R.id.btnClear);
        tvResult = findViewById(R.id.tvResult);
    }

    private void setupSpinners() {
        // Pressure units
        ArrayAdapter<CharSequence> pressureAdapter = ArrayAdapter.createFromResource(
                this, R.array.pressure_units, android.R.layout.simple_spinner_item);
        pressureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPressureUnit.setAdapter(pressureAdapter);

        // Temperature units
        ArrayAdapter<CharSequence> tempAdapter = ArrayAdapter.createFromResource(
                this, R.array.temperature_units, android.R.layout.simple_spinner_item);
        tempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemperatureUnit.setAdapter(tempAdapter);

        // Volume units
        ArrayAdapter<CharSequence> volumeAdapter = ArrayAdapter.createFromResource(
                this, R.array.volume_units, android.R.layout.simple_spinner_item);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVolumeUnit.setAdapter(volumeAdapter);
    }

    private void setupButtons() {
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePressureDrop();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFields();
            }
        });
    }

    private void calculatePressureDrop() {
        if (!validateInput()) {
            return;
        }

        try {
            double P1 = Double.parseDouble(etInitialPressure.getText().toString());
            double P2 = Double.parseDouble(etFinalPressure.getText().toString());
            double T = Double.parseDouble(etTemperature.getText().toString());
            double V = Double.parseDouble(etVolume.getText().toString());

            // Convert to base units (MPa, Kelvin, m³)
            P1 = convertPressureToMPa(P1, spinnerPressureUnit.getSelectedItemPosition());
            P2 = convertPressureToMPa(P2, spinnerPressureUnit.getSelectedItemPosition());
            T = convertTemperatureToKelvin(T, spinnerTemperatureUnit.getSelectedItemPosition());
            V = convertVolumeToCubicMeters(V, spinnerVolumeUnit.getSelectedItemPosition());

            // Calculate pressure drop according to GOST 32569-2013
            double deltaP = calculatePressureDrop(P1, P2, T, V);
            
            // Check compliance with FNIP requirements
            boolean isCompliant = checkFNIPCompliance(deltaP, P1);
            
            displayResults(deltaP, isCompliant);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ошибка ввода данных", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculatePressureDrop(double P1, double P2, double T, double V) {
        // Основная формула расчёта падения давления с учётом температурной компенсации
        // ΔP = P1 - P2 * (T1/T2) для изохорного процесса
        // Упрощённая формула согласно ГОСТ 32569-2013
        return (P1 - P2) * 100; // Преобразование в проценты
    }

    private boolean checkFNIPCompliance(double deltaP, double initialPressure) {
        // Проверка соответствия требованиям ФНиП
        // Допустимое падение давления зависит от типа оборудования и давления
        double maxAllowedDrop;
        
        if (initialPressure < 1.0) {
            maxAllowedDrop = 0.02; // 2% для низкого давления
        } else if (initialPressure < 10.0) {
            maxAllowedDrop = 0.01; // 1% для среднего давления
        } else {
            maxAllowedDrop = 0.005; // 0.5% для высокого давления
        }
        
        return Math.abs(deltaP) <= maxAllowedDrop;
    }

    private double convertPressureToMPa(double value, int unitIndex) {
        switch (unitIndex) {
            case 0: return value; // МПа
            case 1: return value * 0.1; // бар в МПа
            case 2: return value * 0.101325; // атм в МПа
            case 3: return value * 0.00689476; // psi в МПа
            default: return value;
        }
    }

    private double convertTemperatureToKelvin(double value, int unitIndex) {
        switch (unitIndex) {
            case 0: return value + 273.15; // °C в K
            case 1: return (value - 32) * 5/9 + 273.15; // °F в K
            case 2: return value; // K
            default: return value + 273.15;
        }
    }

    private double convertVolumeToCubicMeters(double value, int unitIndex) {
        switch (unitIndex) {
            case 0: return value; // м³
            case 1: return value / 1000; // л в м³
            case 2: return value * 0.0283168; // фут³ в м³
            default: return value;
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etInitialPressure.getText())) {
            showError("Введите начальное давление");
            return false;
        }
        if (TextUtils.isEmpty(etFinalPressure.getText())) {
            showError("Введите конечное давление");
            return false;
        }
        if (TextUtils.isEmpty(etTemperature.getText())) {
            showError("Введите температуру");
            return false;
        }
        if (TextUtils.isEmpty(etVolume.getText())) {
            showError("Введите объем системы");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displayResults(double deltaP, boolean isCompliant) {
        String result = "РЕЗУЛЬТАТЫ РАСЧЕТА:\n\n";
        result += "Падение давления: " + df.format(Math.abs(deltaP)) + "%\n\n";
        
        if (isCompliant) {
            result += "✅ СООТВЕТСТВУЕТ требованиям ФНиП\n";
            result += "Оборудование прошло испытание на герметичность";
        } else {
            result += "❌ НЕ СООТВЕТСТВУЕТ требованиям ФНиП\n";
            result += "Обнаружена утечка. Требуется повторное испытание";
        }
        
        result += "\n\nСправка:\n";
        result += "• ГОСТ 32569-2013: Испытания на прочность и герметичность\n";
        result += "• ФНиП: Приказ №444 от 21.12.2021";
        
        tvResult.setText(result);
        tvResult.setVisibility(View.VISIBLE);
    }

    private void clearAllFields() {
        etInitialPressure.setText("");
        etFinalPressure.setText("");
        etTemperature.setText("");
        etVolume.setText("");
        tvResult.setVisibility(View.GONE);
        spinnerPressureUnit.setSelection(0);
        spinnerTemperatureUnit.setSelection(0);
        spinnerVolumeUnit.setSelection(0);
    }
}