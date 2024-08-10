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
import java.sql.SQLException;
import java.util.Base64;
import java.util.Base64.Decoder;
import javax.swing.JOptionPane;

public class carga_inicial {
   public static int puerto_sinc;

   public static void leer_ruta() throws FileNotFoundException, InterruptedException, SQLException {
      String linea = "";
      File archivo = new File("ruta.txt");
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
      File archivo = new File(ruta_config);
      File comprueba_ruta = new File(ruta_config);
      if (!comprueba_ruta.exists()) {
         JOptionPane.showMessageDialog((Component)null, "No se encontro el archivo de configuracion con la ruta: " + ruta_config);
         crea_config();
      }

      FileReader fr = new FileReader(archivo);
      BufferedReader br = new BufferedReader(fr);
      String linea = br.readLine();
      String[] arreglo = linea.split("<>");
      main.retardo_inicial = Long.parseLong(arreglo[7]);
      puerto_sinc = Integer.parseInt(arreglo[8]);
      String[] parametros64 = arreglo[0].split("~");
      main.local_system_path = decodifica(parametros64[1]);
   }

   public static String decodifica(String codificado) {
      Decoder decod = Base64.getDecoder();
      byte[] descodificado = decod.decode(codificado);
      return new String(descodificado);
   }

   public static void crea_config() {
      String ruta_sistema = JOptionPane.showInputDialog("Ingrese la ruta de configuracion del sistema: ");

      try {
         String ruta_txt = "ruta.txt";
         String contenido = "Contenido de ejemplo";
         File file = new File(ruta_txt);
         if (!file.exists()) {
            file.createNewFile();
         }

         FileWriter fw = new FileWriter(file);
         BufferedWriter bw = new BufferedWriter(fw);
         bw.write(ruta_sistema);
         bw.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      main.ruta_config = ruta_sistema;
   }
}
