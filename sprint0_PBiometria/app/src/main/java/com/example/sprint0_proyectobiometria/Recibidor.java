package com.example.sprint0_proyectobiometria;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class Recibidor extends ActivityCompat {

    private static final String ETIQUETA_LOG = ">>>>";
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;


    // Variables para calcualar la media en función de los beacons recibidos en cada escaneado
    private List<Float> valoresO3 = new ArrayList<>();
    private Handler handlerLimpiar = new Handler();
    private Handler handlerMedia = new Handler();

    float valorMostrar = 0;

    private Publicador publicador = new Publicador();


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission") // Evitamos que revise los permisos, porque ya lo hemos hecho
    public void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        // Define como se deberá comportar mi aplicación cuando encuentre un dispositivo BTLE
        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                // Para manejar un resultado individual
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                // Mostramos los resultados que encuentre de los dispositivos escaneados
                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                // Para manejar un conjunto de resultados
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                // Para manejar cualquier error que pueda surgir
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        // Inicia el escaneo sin ningún filtro; queremos ver todos los resultados
        this.elEscanner.startScan(this.callbackDelEscaneo);

    } // ()

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // mostrarInfomracionDispositivosBTLE() muestra en el logCat la información de los datos recibidos
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // resultado: ScanResult -> mostrarInformacionDispositivosBTLE()
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    public void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ************ DISPOSITIVO DETECTADO BTLE *********** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].getUuid());
           // Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // La función datosRecibidos() coge el dato del beacon que recibe y lo almacena en la lista
    // valoresO3 para hacer la media
    // ---------------------------------------------------------------------------------------------
    public void datosRecibidos(ScanResult resultado) {
        byte[] bytes = resultado.getScanRecord().getBytes();
        TramaIBeacon tib = new TramaIBeacon(bytes);

        float valorO3 = Utilidades.bytesToInt(tib.getMajor());

        // Añadir el valor a la lista
        valoresO3.add(valorO3);
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // La función calcularMedia() coge el dato del beacon que recibe y lo almacena en la lista
    // valoresO3 para hacer la media
    // ---------------------------------------------------------------------------------------------
    // calcularMedia() -> media: R
    // ---------------------------------------------------------------------------------------------

    private float calcularMedia() {
        float suma = 0;
        for (float valor : valoresO3) {
            suma += valor;
        }
        float media = suma / valoresO3.size();

        // Limpiar la lista después de calcular la media
        valoresO3.clear();

        return media;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // dispositivoBuscado: String -> buscarEsteDispositivoBTLE()
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    // Suprime la advertencia sobre permisos faltantes
    public void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        // Registrar en log el inicio de la búsqueda del dispositivo
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        // Registrar en log la instalación del callback de escaneo
        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        // Inicializar el callback de escaneo BLE
        this.callbackDelEscaneo = new ScanCallback() {
            // Este método se llama cuando se encuentra un dispositivo durante el escaneo
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                // Llama al método onScanResult de la clase padre
                super.onScanResult(callbackType, resultado);
                // Registrar en log que se ha encontrado un dispositivo
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");
                // Mostrar la información del dispositivo encontrado
                mostrarInformacionDispositivoBTLE(resultado);
                datosRecibidos(resultado);
                detenerEscaneo();  // Detener el escaneo después de encontrar un dispositivo
            }

            // Este método se llama cuando se acumulan varios resultados de escaneo
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                // Llama al método onBatchScanResults de la clase padre
                super.onBatchScanResults(results);
                // Registrar en log que se han encontrado varios dispositivos
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");
            }

            // Este método se llama si el escaneo falla
            @Override
            public void onScanFailed(int errorCode) {
                // Llama al método onScanFailed de la clase padre
                super.onScanFailed(errorCode);
                // Registrar en log que el escaneo ha fallado
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");
            }
        };

        // Configurar el filtro de escaneo para buscar el dispositivo por nombre.
        ScanFilter sf = new ScanFilter.Builder().setDeviceName(dispositivoBuscado).build();

        // Configurar las opciones de escaneo, como el modo de latencia baja.
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        // Crear una lista de filtros y añadir el filtro definido.
        // Podríamos añadir más filtros si quisieramos una búsqueda más definida, pero solo tenemos
        // que buscar por nombre de momento.
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(sf);

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);

        // Iniciar el escaneo con los filtros y ajustes especificados
        this.elEscanner.startScan(filters, settings, this.callbackDelEscaneo);

        // Configura un temporizador para detener el escaneo y calcular/enviar la media
        handlerMedia.postDelayed(new Runnable() {
            @Override
            public void run() {
                detenerEscaneo();  // Detiene el escaneo
                float media = calcularMedia();
                Log.d(ETIQUETA_LOG, "Envio a la bbdd");
                Log.d(ETIQUETA_LOG, "Valor media: " + media);
                valorMostrar = media;

                // Datos que me invento
                int idUsuario = 1;
                double latitud = 40.4168;
                double longitud = -3.7038;

                Publicador.SendPostRequest publicador = new Publicador().new SendPostRequest(media, idUsuario, latitud, longitud);
                publicador.execute(); // Envía la media calculada

                valoresO3.clear();  // Limpiamos la lista después de calcular la media
            }
        }, 10000);  // 10 segundos

    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // mostrarValor() obtiene la variable privada de la clase y la devuelve para usarla en el Main
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    public float mostrarValor() {
        return valorMostrar;
    }


    @SuppressLint("MissingPermission")
    private void detenerEscaneo() {
        this.elEscanner.stopScan(this.callbackDelEscaneo);
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    public void detenerBusquedaDispositivosBTLE() {

        // Comprueba primero que haya un escaneo; si no lo hay no puede parar nada
        if (this.callbackDelEscaneo == null) {
            return;
        }

        // Detiene el escaneo en caso de que lo haya
        this.elEscanner.stopScan(this.callbackDelEscaneo);

        // Una vez ha detenido el escaneo, lo marca como null para indicar que no está escaneando
        this.callbackDelEscaneo = null;

    } // ()

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // Antes de inicial el escaneo de los dispositivos, tenemos que inicializar el bluetooth.
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    public void inicializarBlueTooth(Context context) {
        Log.d(ETIQUETA_LOG, "inicializarBlueTooth(): obtenemos adaptador BT");

        // Obtener el adaptador Bluetooth del dispositivo
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        // Comprobar si el adaptador Bluetooth está presente en el dispositivo
        if (bta == null) {
            Log.d(ETIQUETA_LOG, "inicializarBlueTooth(): No hay soporte de Bluetooth en este dispositivo");
            return; // Si no hay soporte, termina la función porque no se va a poder conectar
        }

        Log.d(ETIQUETA_LOG, "inicializarBlueTooth(): habilitamos adaptador BT");

        // Comprobar si Bluetooth está habilitado.
        // Comprueba también los permisos y si no está habilitado y tiene permisos, lo habilita
        if (!bta.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                bta.enable();
            } else {
                Log.d(ETIQUETA_LOG, "inicializarBlueTooth(): No tenemos permisos para habilitar Bluetooth");
            }
        }

        Log.d(ETIQUETA_LOG, "inicializarBlueTooth(): habilitado = " + bta.isEnabled());
        Log.d(ETIQUETA_LOG, "inicializarBlueTooth(): estado = " + bta.getState());

        // Obtenemos el escaner del dispositivo
        this.elEscanner = bta.getBluetoothLeScanner();

        if (this.elEscanner == null) {
            Log.d(ETIQUETA_LOG, "inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle");
        }
    }

    // ()

}
