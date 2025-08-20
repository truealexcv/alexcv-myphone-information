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
                float celsius = temp / 10f;
                addTemperature(celsius);
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }

    private void addTemperature(float value) {
        int maxItems = 5;
        if (temperatureList.size() == maxItems) {
            temperatureList.remove(0);
            temperatureAdapter.notifyItemRemoved(0);
        }
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        temperatureList.add(value + " °C (" + time + ")");
        temperatureAdapter.notifyItemInserted(temperatureList.size() - 1);
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