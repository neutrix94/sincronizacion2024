/*
    Version 2024.2 ( Depuración de registros de saincronizacion )
*/
package sincronizacionsistema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class tickets {
   public tickets() throws IOException {
   }

   public String descarga_archivos(String ruta_origen, String ruta_destino, String nombre) throws IOException {
      ruta_origen = ruta_origen + nombre;
      File dir = new File(ruta_destino);
      if (!dir.exists() && !dir.mkdir()) {
         return "No se encontró el archivo!!!" + ruta_origen + nombre;
      } else {
         File file = new File(ruta_destino + nombre);
         URLConnection conn = (new URL(ruta_origen)).openConnection();
         conn.connect();
         System.out.println("\nempezando descarga: \n");
         System.out.println(">> URL: " + ruta_origen);
         System.out.println(">> Nombre: " + nombre);
         System.out.println(">> tamaño: " + conn.getContentLength() + " bytes");
         InputStream in = conn.getInputStream();
         OutputStream out = new FileOutputStream(file);
         int b = 0;

         while(b != -1) {
            b = in.read();
            if (b != -1) {
               out.write(b);
            }
         }

         out.close();
         in.close();
         return "ok";
      }
   }
}
