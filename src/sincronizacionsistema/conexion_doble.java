/*
    Version 2024.2 ( Depuración de registros de saincronizacion )
*/
package sincronizacionsistema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Base64.Decoder;

public class conexion_doble {
   String[] parametrosDecod = new String[10];
   String ruta_arch_jar = "";
   private static final String driver = "com.mysql.jdbc.Driver";
   private static Connection conn_local;
   private static String user_local;
   private static String pass_local;
   private static String url_local;
   private static Connection conn_linea;
   private static String user_linea;
   private static String pass_linea;
   private static String url_linea;
   private Connection con;
   private ResultSet rs;
   private ResultSetMetaData mtd;
   private String[] columna;
   private String consulta;
   private String error;

   public conexion_doble(String ruta) {
      this.leer_txt(ruta);
   }

   public void leer_txt(String ruta_archivo_inicial) {
      try {
         BufferedReader br = new BufferedReader(new FileReader(ruta_archivo_inicial));

         String temporal;
         String bRead;
         for(temporal = ""; (bRead = br.readLine()) != null; temporal = temporal + bRead) {
         }

         String[] conexiones = temporal.split("<>");
         int cont_conex = 0;

         for(int cont_posic = 0; cont_conex <= 1; ++cont_conex) {
            String[] parametros64 = conexiones[cont_conex].split("~");

            for(int i = 0; i < parametros64.length; ++i) {
               this.parametrosDecod[cont_posic] = decodifica(parametros64[i]);
               ++cont_posic;
               if (cont_posic == 4) {
                  this.parametrosDecod[cont_posic] = "";
                  ++cont_posic;
               }
            }
         }

         this.ruta_arch_jar = conexiones[3];
         br.close();
      } catch (IOException var10) {
         System.out.println("No se encontó el archivo deconfiguración inicial del sistema!!!\n" + var10);
      }

   }

   public static String decodifica(String codificado) {
      Decoder decod = Base64.getDecoder();
      byte[] descodificado = decod.decode(codificado);
      return new String(descodificado);
   }

   public Connection conecta_local() {
      url_local = "jdbc:mysql://" + this.parametrosDecod[0] + ":3306/" + this.parametrosDecod[2] + "?autoReconnect=false";
      user_local = this.parametrosDecod[3];
      pass_local = "";
      conn_local = null;

      try {
         Class.forName("com.mysql.jdbc.Driver");
         conn_local = DriverManager.getConnection(url_local, user_local, pass_local);
         if (conn_local != null) {
            System.out.println("Conectado en local!!!");
         }
      } catch (SQLException | ClassNotFoundException var2) {
         System.out.println("Error al conectar con servidor local " + var2);
      }

      return conn_local;
   }

   public void desconectar_local() {
      conn_local = null;
      if (conn_local == null) {
         System.out.println("Conexion local terminada!!!");
      }

   }

   public Connection conecta_linea() throws SQLException, IOException {
      url_linea = "jdbc:mysql://" + this.parametrosDecod[5] + ":3306/" + this.parametrosDecod[7] + "?autoReconnect=false";
      user_linea = this.parametrosDecod[8];
      pass_linea = this.parametrosDecod[9];
      conn_linea = null;

      try {
         Class.forName("com.mysql.jdbc.Driver");
         conn_linea = DriverManager.getConnection(url_linea, user_linea, pass_linea);
         if (conn_linea != null) {
            System.out.println("Conectado en línea!!!");
         }
      } catch (SQLException | ClassNotFoundException var2) {
         System.out.println("Error al conectar con servidor en línea " + var2);
         return null;
      }

      return conn_linea;
   }

   public void desconectar_linea() {
      conn_local = null;
      if (conn_local == null) {
         System.out.println("Conexion en linea terminada!!!");
      }

   }

   public String getError() {
      return "Error: " + this.error;
   }

   public ResultSet getResultado() {
      return this.rs;
   }

   public String cuenta_registros(String querty, Connection conexion) {
      String respuesta = "ok~";

      try {
         this.consulta = querty;
         System.out.println(this.consulta);
         Statement st = conexion.createStatement();
         ResultSet rs = st.executeQuery(this.consulta);
         this.mtd = rs.getMetaData();
         int no_cols = this.mtd.getColumnCount();
         String[] res = this.getNombresColumnas(no_cols, this.mtd);
         rs.next();

         for(int j = 0; j < res.length; ++j) {
            respuesta = respuesta + rs.getObject(res[0]);
         }

         rs.close();
         return respuesta;
      } catch (SQLException var9) {
         this.error = var9.getMessage();
         System.out.println("Error al contar registros!!!\n" + this.getError());
         return "error";
      }
   }

   public String consultar(String query, Connection conexion, String nom_tabla) {
      String respuesta = "";

      try {
         this.consulta = query;
         System.out.println(query);
         Statement st = conexion.createStatement();
         ResultSet rs = st.executeQuery(this.consulta);
         this.mtd = rs.getMetaData();
         int no_cols = this.mtd.getColumnCount();
         String[] res = this.getNombresColumnas(no_cols, this.mtd);

         while(true) {
            if (!rs.next()) {
               rs.close();
               break;
            }

            for(int j = 0; j < res.length; ++j) {
               respuesta = respuesta + rs.getObject(res[j]) + "~";
            }

            respuesta = respuesta + "°";
         }
      } catch (SQLException var10) {
         this.error = var10.getMessage();
         System.out.println("Tabla: " + nom_tabla + "\n" + this.getError() + " " + query);
         return "error";
      }

      if (respuesta == "" || respuesta == null) {
         respuesta = "0";
      }

      return respuesta;
   }

   public String[] getNombresColumnas(int numColumnas, ResultSetMetaData mt) {
      try {
         this.columna = new String[numColumnas];

         for(int i = 0; i < numColumnas; ++i) {
            this.columna[i] = mt.getColumnLabel(i + 1);
         }
      } catch (SQLException var4) {
         System.out.println("Error al consultar nombres de columnas");
      }

      return this.columna;
   }

   public String actualiza(String query, Connection conexion) throws SQLException {
      boolean var3 = false;

      int respuesta;
      try {
         PreparedStatement st = conexion.prepareStatement(query);
         respuesta = st.executeUpdate();
         st.close();
      } catch (SQLException var5) {
         return "error";
      }

      return "ok~" + respuesta;
   }

   public String inserta(String query, Connection conexion) {
      String respuesta = "ok~";

      try {
         PreparedStatement st = conexion.prepareStatement(query, 1);
         int reg_afectados = st.executeUpdate();
         if (reg_afectados == 0) {
            throw new SQLException("Error al insertar el registro!!!");
         } else {
            ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
               respuesta = respuesta + generatedKeys.getInt(1);
            }

            st.close();
            return respuesta;
         }
      } catch (SQLException var7) {
         return "error:\n" + var7 + "\n" + query;
      }
   }
}
