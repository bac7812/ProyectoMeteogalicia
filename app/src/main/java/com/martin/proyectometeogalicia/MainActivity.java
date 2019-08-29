package com.martin.proyectometeogalicia;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends Activity {
    String URL = "http://servizos.meteogalicia.gal/rss/observacion/rssEstacionsEstActual.action?idEst=10155";
    TextView textview;
    ImageView imageview;
    NodeList nodelist;
    ProgressDialog diagolo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        diagolo = new ProgressDialog(this);
        diagolo.setMessage("Cargando...");
        diagolo.show();
        new cargarXML().execute(URL);
    }

    public void actualizar(View view) {
        setContentView(R.layout.activity_main);
        diagolo = new ProgressDialog(this);
        diagolo.setMessage("Cargando...");
        diagolo.show();
        new cargarXML().execute(URL);
    }

    private class cargarXML extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... Url) {
            try {
                URL url = new URL(Url[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
                nodelist = doc.getElementsByTagName("item");

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            for (int i=0; i<nodelist.getLength(); i++) {

                Node item = nodelist.item(i);

                NodeList datos = item.getChildNodes();

                for (int d = 0; d < datos.getLength(); d++) {
                    Node dato = datos.item(d);
                    String etiqueta = dato.getNodeName();

                    if (etiqueta.equals("EstacionsEstActual:concello")) {
                        String concello = dato.getFirstChild().getNodeValue();
                        textview = findViewById(R.id.concelloTextView);
                        textview.setText(concello);
                    }

                    if (etiqueta.equals("EstacionsEstActual:icoCeo")) {
                        String ceo = dato.getFirstChild().getNodeValue();
                        String imagen = "http://servizos.meteogalicia.gal/datosred/infoweb/meteo/imagenes/meteorosmapa/ceo/"+ceo+".png";
                        new cargarImagenCeo().execute(imagen);
                    }

                    if (etiqueta.equals("EstacionsEstActual:valTemperatura")) {
                        String temperatura = dato.getFirstChild().getNodeValue();
                        textview = findViewById(R.id.numTemperaturaTextView);
                        textview.setText(temperatura+"º");
                    }

                    if (etiqueta.equals("EstacionsEstActual:icoTemperatura")) {
                        String tendencia = dato.getFirstChild().getNodeValue();
                        String imagen = "http://servizos.meteogalicia.gal/datosred/infoweb/meteo/imagenes/termometros/"+tendencia+".png";
                        new cargarImagenTendencia().execute(imagen);
                    }

                    if (etiqueta.equals("EstacionsEstActual:valSensTermica")) {
                        String sensacionTermica = dato.getFirstChild().getNodeValue();
                        textview = findViewById(R.id.numSensacionTermicaTextView);
                        textview.setText(sensacionTermica+"º");
                    }

                    if (etiqueta.equals("EstacionsEstActual:icoVento")) {
                        String vento = dato.getFirstChild().getNodeValue();
                        String imagen = "http://servizos.meteogalicia.gal/datosred/infoweb/meteo/imagenes/meteoros/vento/combo/"+vento+".png";
                        new cargarImagenVento().execute(imagen);
                    }

                    if (etiqueta.equals("EstacionsEstActual:dataUTC")) {
                        String data = dato.getFirstChild().getNodeValue();
                        textview = findViewById(R.id.dataTextView);
                        textview.setText("Estado actual de estación: "+data);
                    }
                }

            }
           // diagolo.dismiss();
        }
    }

    private InputStream openHttpConnection(String url) throws IOException {
        InputStream is = null;
        int responseCode;

        URLConnection connection = null;
        connection = (new URL(url)).openConnection();

        if(!(connection instanceof HttpURLConnection)) {
            throw new IOException("No conexión HTTP");
        }

        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
        httpURLConnection.setAllowUserInteraction(false);
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();
        responseCode = httpURLConnection.getResponseCode();

        if(responseCode == HttpURLConnection.HTTP_OK) {
            is = httpURLConnection.getInputStream();
        }

        return is;
    }

    private Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        InputStream is = null;

        try {
            is = openHttpConnection(url);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;
    }

    private class cargarImagenCeo extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return downloadImage(strings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageview = findViewById(R.id.ceoImageView);
            imageview.setImageBitmap(bitmap);
        }
    }

    private class cargarImagenTendencia extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return downloadImage(strings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageview = findViewById(R.id.tendenciaImageView);
            imageview.setImageBitmap(bitmap);
            diagolo.dismiss();
        }
    }

    private class cargarImagenVento extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return downloadImage(strings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageview = findViewById(R.id.ventoImageView);
            imageview.setImageBitmap(bitmap);
        }
    }
}
