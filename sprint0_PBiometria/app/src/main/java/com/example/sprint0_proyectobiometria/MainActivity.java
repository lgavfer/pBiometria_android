package com.example.sprint0_proyectobiometria;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.Manifest;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private static final String ETIQUETA_LOG = ">>>>";
    private static final int REQUEST_CODE_PERMISSIONS = 200;

    private Intent elIntentDelServicio = null;

    // Creamos una variable global que definirá el nombre del dispositivo bluetooth que queremos encontrar
    private String dispositivoBuscado = "GTI-3A-Laura";

    // Declarar una instancia de Recibidor
    private Recibidor recibidor;

    TextView textView;


    // Función para comprobar que tiene todos los permisos solicitados
    // En caso de que no tenga los permisos -> los solicita
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void verificarPermisos() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
        } else {
            Log.d(ETIQUETA_LOG, "Todos los permisos concedidos.");
        }
    }


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.recibidor.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        //this.buscarEsteDispositivoBTLE( "EPSG-GTI-PROY-3A" );
        this.recibidor.buscarEsteDispositivoBTLE(dispositivoBuscado);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado");
        this.recibidor.detenerBusquedaDispositivosBTLE();
    } // ()


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    // --------------------------------------------------------------

    public void botonArrancarServicioPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton arrancar servicio Pulsado");

        if (this.elIntentDelServicio != null) {
            return;
        }


        this.elIntentDelServicio = new Intent(this, ServicioEscucharBeacons.class);
        this.elIntentDelServicio.putExtra("tiempoDeEspera", (long) 5000);
        startService(this.elIntentDelServicio);
    }

    public void botonDetenerServicioPulsado(View v) {
        if (this.elIntentDelServicio == null) {
            return;
        }

        stopService(this.elIntentDelServicio);
        this.elIntentDelServicio = null;


        Log.d(ETIQUETA_LOG, " boton detener servicio Pulsado");
    }

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Esta función se encarga de revisar los permisos y de solicitarlos si no los tiene
        verificarPermisos();

        // Inicializamos la instancia para usar la clase Recibidor
        recibidor = new Recibidor();

        // Iniciamos el Bluetooth para que poder escanear
        recibidor.inicializarBlueTooth(this);

        textView = findViewById(R.id.textView);

        Handler handlerRepetirBusqueda = new Handler();

        Runnable actualizarRecepcion = new Runnable() {
            @Override
            public void run() {
                recibidor.buscarEsteDispositivoBTLE(dispositivoBuscado);
                handlerRepetirBusqueda.postDelayed(this, 12000);
                if (recibidor.mostrarValor() == 0.0) {
                    textView.setText("Cargando...");
                } else {
                    textView.setText(String.valueOf(recibidor.mostrarValor()) + " ppm");
                }
            }
        };

        actualizarRecepcion.run();


    }


}
