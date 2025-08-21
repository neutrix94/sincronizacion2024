/*
    Version 2024.2 ( Depuración de registros de saincronizacion )
*/
package sincronizacionsistema;

import sincronizacion.main;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
//import java.util.Base64;
//import java.util.Base64.Decoder;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class carga_inicial {
    public static int puerto_sinc;

    public static void leer_ruta() throws FileNotFoundException, InterruptedException, SQLException {
System.out.println("Entra en leer_ruta");
        String linea = "";
        File archivo = new File("synchronization_config.txt");
        FileReader fr = new FileReader(archivo);
        BufferedReader br = new BufferedReader(fr);
        File comprueba_ruta;
        try {
           linea = br.readLine();
           if (linea == null) {
              archivo.delete();
              main.main((String[])null);
           }

           comprueba_ruta = new File(linea);
           if (!comprueba_ruta.exists()) {
              JOptionPane.showMessageDialog((Component)null, "No se encontro el archivo de configuracion con la ruta: " + main.ruta_config);
              crea_config();
           }
        } catch (IOException var5) {
           var5.printStackTrace();
        }

        comprueba_ruta = new File(linea);
        if (!comprueba_ruta.exists()) {
           JOptionPane.showMessageDialog((Component)null, "No se en contro el archivo de configuracion con la ruta: " + main.ruta_config);
           crea_config();
        }

        main.ruta_config = linea;
    }

    public static void carga_inicial(String ruta_config) throws IOException {
System.out.println("Entra en carga_inicial");
        /*File archivo = new File(ruta_config);
        File comprueba_ruta = new File(ruta_config);
        if (!comprueba_ruta.exists()) {
           JOptionPane.showMessageDialog((Component)null, "No se encontro el archivo de configuracion con la ruta: " + ruta_config);
           crea_config();
        }*/
    //consume api para obtener parametros
        
        //String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/sincronizacion/" + module_endpoint;
    //System.out.println("URL : " + urlParaVisitar);
        StringBuilder resultado = new StringBuilder();
        URL url = new URL(ruta_config);
        HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
        conexion.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
        
        String linea;
        while((linea = rd.readLine()) != null) {
            resultado.append(linea);
        }
        System.out.println("Resultado : " + resultado);
        rd.close();
        

        try {
            // Parsear el JSON
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(resultado.toString());

            // Acceder a los campos
            String apiPath = (String) jsonObject.get("api_path");
            long minutos_retardo_inicial_sincronizacion = (long) jsonObject.get("minutos_retardo_inicial_sincronizacion");
            //String storeName = (String) jsonObject.get("store_name");
            //int puerto = (int) jsonObject.get("puerto_sincronizacion");
            Long long_valor = (Long) jsonObject.get("puerto_sincronizacion");
            int puerto = long_valor.intValue(); // Correcto
            Long long_store_id = (Long) jsonObject.get("puerto_sincronizacion");
            int store_id = long_store_id.intValue(); // Correcto
            
            Long long_synchronization_interval = (Long) jsonObject.get("segundos_intervalo_sincronizacion");
            int synchronization_interval = long_synchronization_interval.intValue(); // Correcto
            
            String local_path = (String) jsonObject.get("local_path");
            String store_name= (String) jsonObject.get("store_name");

            /*System.out.println("API Path: " + apiPath);
            System.out.println("Store ID: " + storeId);
            System.out.println("Store Name: " + storeName);
            System.out.println("Puerto: " + puerto);
            System.out.println("Hora Referencia: " + horaReferencia);*/
            main.local_system_path = local_path;//decodifica(parametros64[1]);
            main.retardo_inicial = minutos_retardo_inicial_sincronizacion;//Long.parseLong(arreglo[7]);            
            main.store_id = store_id;//Long.parseLong(arreglo[7]);
            main.syncronization_interval = synchronization_interval;
            main.store_name = store_name;

            puerto_sinc = puerto;//Integer.parseInt(puerto);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //main.local_system_path = decodifica(parametros64[1]);
        //main.retardo_inicial = Long.parseLong(arreglo[7]);
       //puerto_sinc = Integer.parseInt(puerto);
    }

    /*public static String decodifica(String codificado) {
       Decoder decod = Base64.getDecoder();
       byte[] descodificado = decod.decode(codificado);
       return new String(descodificado);
    }*/

    public static void crea_config() {
System.out.println("Entra en crea_config");
        String api_url = JOptionPane.showInputDialog("Ingresa la url del api del sistema local : ");

        try {
           String ruta_txt = "synchronization_config.txt";
           File file = new File(ruta_txt);
           if (!file.exists()) {
              file.createNewFile();
           }

           FileWriter fw = new FileWriter(file);
           BufferedWriter bw = new BufferedWriter(fw);
           bw.write( api_url );
           bw.close();
        } catch (Exception e) {
            System.out.println("Error en crea_config : " + e);
            e.printStackTrace();
        }
        main.ruta_config = api_url;
    }
}
