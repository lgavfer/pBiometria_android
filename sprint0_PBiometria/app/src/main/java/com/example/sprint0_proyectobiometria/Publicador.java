package com.example.sprint0_proyectobiometria;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------
// Clase Publicador -> utilizando la regla POST de la API, envia los datos obtenidos a la BBDD
// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------

public class Publicador {

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        // -----------------------------------------------------------------------------------------
        // Constructor() -> inicializa todas las variables
        // -----------------------------------------------------------------------------------------
        // Creamos todas las variables para almacenar los datos
        private float valorO3;
        private int idUsuario;
        private double latitud;
        private double longitud;
        private String currentDate;
        private String currentTime;

         public SendPostRequest(float valorO3, int idUsuario, double latitud, double longitud) {
            this.valorO3 = valorO3;
            this.idUsuario = idUsuario;
            this.latitud = latitud;
            this.longitud = longitud;

            // Inicializa la fecha y la hora actuales
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            this.currentDate = dateFormat.format(calendar.getTime());
            this.currentTime = timeFormat.format(calendar.getTime());
        }

        // -----------------------------------------------------------------------------------------
        // -----------------------------------------------------------------------------------------
        // doInBackground -> se encarga de enviar los datos
        @Override
        protected String doInBackground(String... params) { // Realiza funciones en segundo plano
            try {

                // Establecemos la conexión con el  servidor
                URL url = new URL("http://192.168.1.41:8888/src/api/mediciones"); // Utilizamos la IP del servidor local
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);

                // Creamos un JSON con todos los parámetros que queremos enviar
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("valorO3", this.valorO3);
                jsonParam.put("idUsuario", this.idUsuario);
                jsonParam.put("latitud", this.latitud);
                jsonParam.put("longitud", this.longitud);
                jsonParam.put("fecha", this.currentDate);
                jsonParam.put("hora", this.currentTime);

                // Escribimos el objeto JSON en la salida de la conexión
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();

                // Obtenemos el código de respuesta que indica el resultado de la solicitud
                int responseCode = conn.getResponseCode();

                // Leemos la respuesta -> para saber si hay conexión o no.
                // Si no hay conexión, queremos ver porque.
                BufferedReader in;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                return sb.toString();

            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        // La función onPostExecute se encarga de mostrar la respuesta HTTP una vez se ha
        // ejecutado la función anterior.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("HTTP Response", result); // Muestra en el LogCat la respuesta
        }
    }
}
