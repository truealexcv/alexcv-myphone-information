package com.alexcv.myphoneinformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver batteryReceiver;
    private TemperatureAdapter temperatureAdapter;
    private ArrayList<String> temperatureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView rv_temperatures = findViewById(R.id.rv_temperatures);
        rv_temperatures.setLayoutManager(new LinearLayoutManager(this));
        temperatureList = new ArrayList<>();
        temperatureAdapter = new TemperatureAdapter(temperatureList);
        rv_temperatures.setAdapter(temperatureAdapter);

        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                addTemperature(temp);

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                BatteryManager mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                long amperes = -1;
                long chargeCounter = -1;
                int currentPercentage = -1;
                if (mBatteryManager != null) {
                    amperes = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                    chargeCounter = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                    currentPercentage = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                }
                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                addBatteryInfo(level, scale, status, health, voltage, amperes, chargeCounter, currentPercentage, chargePlug);
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }

    private void addTemperature(int temp) {
        float celsius = temp / 10f;
        int maxItems = 5;
        if (temperatureList.size() == maxItems) {
            temperatureList.remove(0);
            temperatureAdapter.notifyItemRemoved(0);
        }
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        temperatureList.add(celsius + " °C (" + time + ")");
        temperatureAdapter.notifyItemInserted(temperatureList.size() - 1);
    }

    private void addBatteryInfo(int level, int scale, int status, int health, int voltage,
                                   long amperes, long chargeCounter, int currentPercentage, int chargePlug) {
        int percentageString = (int) ((level / (float) scale) * 100);

        String statusString = "Desconocido";
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            statusString = "Cargando";
        } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            statusString = "Descargando";
        } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
            statusString = "Carga completa";
        } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            statusString = "Sin cargar";
        }

        String healthString = "Desconocido";
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthString = "Buena";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthString = "Sobrecalentada";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthString = "Defectuosa";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthString = "Sobrevoltaje";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthString = "Fallo";
                break;
        }

        float voltageV = voltage / 1000f;
        float amperesA = amperes / 1_000_000f;
        float watiosW = voltageV * amperesA;

        float chargeCountermAH = chargeCounter / 1000f;
        float chargeFullmAH = (chargeCountermAH/currentPercentage) * 100;

        String chargePlugString = "Desconocida";
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
            chargePlugString = "USB";
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
            chargePlugString = "Cargador AC";
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            chargePlugString = "Carga inalámbrica";
        }

        String info = "Nivel: " + percentageString + " %"
                + "\nEstado: " + statusString
                + "\nSalud: " + healthString
                + "\nVoltaje(V): " + voltageV + " V"
                + "\nAmperios(A): " + amperesA + " A"
                + "\nVatios(W): " + watiosW + " W"
                + "\nCapacidad actual: " + (chargeCountermAH > 0 ? chargeCountermAH + " mAh" : "No disponible")
                + "\nCapacidad total: " + (chargeFullmAH > 0 ? chargeFullmAH + " mAh" : "No disponible")
                + "\nFuente: " + chargePlugString;

        TextView tvBatteryInfo = findViewById(R.id.tv_battery_info);
        tvBatteryInfo.setText(info);
    }

    static class TemperatureAdapter extends RecyclerView.Adapter<TemperatureViewHolder> {
        private final ArrayList<String> data;


        public TemperatureAdapter(ArrayList<String> data) {
            this.data = data;
        }


        @NonNull
        @Override
        public TemperatureViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new TemperatureViewHolder(view);
        }


        @Override
        public void onBindViewHolder(TemperatureViewHolder holder, int position) {
            holder.bind(data.get(position));
        }


        @Override
        public int getItemCount() {
            return data.size();
        }
    }


    static class TemperatureViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;


        public TemperatureViewHolder(android.view.View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }


        public void bind(String value) {
            textView.setText(value);
        }
    }
}