package com.example.sprint0_proyectobiometria;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ServicioEscucharBeacons extends IntentService {

    private static final String ETIQUETA_LOG = ">>>>";
    private long tiempoDeEspera = 1000;
    private boolean seguir = true;

    public ServicioEscucharBeacons() {
        super("HelloIntentService");
        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.constructor: termina");
    }

    public void parar() {
        if (this.seguir == false) {
            return;
        }

        this.seguir = false;
        this.stopSelf();

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.parar() : acaba ");
    }

    public void onDestroy() {
        this.parar();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(ETIQUETA_LOG, " onHandleIntent: comienza");
        this.tiempoDeEspera = intent.getLongExtra("tiempoDeEspera", 50000);
        this.seguir = true;

        long contador = 1;

        try {
            while (this.seguir) {
                Thread.sleep(tiempoDeEspera);
                Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onHandleIntent: tras la espera:  " + contador);
                contador++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
