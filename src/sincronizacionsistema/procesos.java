/*
    Version 2024.2 ( Depuración de registros de saincronizacion )
*/
package sincronizacionsistema;

import formularios.infoSinccronizacion;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class procesos {
   conexion_doble conecta;
   infoSinccronizacion info = new infoSinccronizacion();
   private Connection conexion_local;
   private Connection conexion_linea;
   private Connection conexion_extraer;
   private Connection conexion_insertar;
   public int id_sucursal;
   public int tiempo_buscar;
   int sincronizando = 0;
   Timer tiempo;
   public static String final_local_system_path;
   String dir;
   FileWriter archivo;

   public procesos(String ruta_conexion, String system_path) throws SQLException, IOException {
      this.final_local_system_path = system_path;
      this.conecta = new conexion_doble(ruta_conexion);
      this.conexion_local = this.conecta.conecta_local();
      if (this.conexion_linea == null) {
         this.sincronizando = 0;
         //info.api_local_path = system_path;
         //this.info.notification_sync.setVisible(true);
      } else {
         this.conexion_linea = this.conecta.conecta_linea();
         this.info.url_field.setText(this.final_local_system_path);
         this.info.time_interval_field.setText("" + this.tiempo_buscar);
      }
      
   }

   public String getDatosSucursal() {
      String sql = "SELECT id_sucursal,nombre,(intervalo_sinc*1000) FROM sys_sucursales WHERE acceso=1 LIMIT 1";
      return this.conecta.consultar(sql, this.conexion_local, "sucursal");
   }

   public void InfoLog(String error) throws IOException {
      if (!(new File("log.txt")).exists()) {
         this.archivo = new FileWriter(new File("log.txt"), false);
      }

      this.archivo = new FileWriter(new File("log.txt"), true);
      Calendar fechaActual = Calendar.getInstance();
      this.archivo.write("[" + String.valueOf(fechaActual.get(5)) + "/" + (fechaActual.get(2) + 1) + "/" + fechaActual.get(1) + " " + fechaActual.get(11) + ":" + fechaActual.get(12) + ":" + fechaActual.get(13) + "][INFO] " + error + "\r\n");
      this.archivo.close();
   }

   public String ejecutaIntervalo(long tiempo_retardo_inicio) throws InterruptedException, Exception {
      this.info.setVisible(true);
    //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

      while(true) {
         if (this.sincronizando == 0) {
            this.sincronizando = 1;
            info.api_local_path = this.final_local_system_path;
            //this.info.notification_sync.setVisible(false);
            this.info.url_field.setText(this.final_local_system_path);
            this.info.time_interval_field.setText("" + this.tiempo_buscar / 1000);
            this.reset_progress_bar();
            String resp_temp = "";
            this.obtener_registros_restantes();
            System.out.println("pasa");

            try {
               this.info.synchronization_rows_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_rows_info.setText("Sincronizando...<->");
               resp_temp = this.sincroniza_registros();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var7) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var7);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var6) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var6);
                  }

                  this.info.synchronization_rows_info.setText(resp_temp);
                  this.info.synchronization_rows_bar.setValue(100);
                  this.info.synchronization_rows_bar.setBackground(Color.red);
                  this.info.synchronization_rows_bar.setForeground(Color.red);
                  this.info.synchronization_rows_end.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_rows_info.setText(resp_temp);
                  this.info.synchronization_rows_bar.setValue(100);
                  this.info.synchronization_rows_bar.setBackground(Color.green);
                  this.info.synchronization_rows_bar.setForeground(Color.green);
                  this.info.synchronization_rows_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var35) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var35);
            }

            this.obtener_registros_restantes();
             try {
               this.info.synchronization_transfer_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_transfer_info.setText("Sincronizando...<->");
               resp_temp = this.sys_sincronizacion_registros_transferencias();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var25) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var25);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var24) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var24);
                  }

                  this.info.synchronization_transfer_info.setText(resp_temp);
                  this.info.synchronization_transfer_bar.setValue(100);
                  this.info.synchronization_transfer_bar.setBackground(Color.red);
                  this.info.synchronization_transfer_bar.setForeground(Color.red);
               } else {
                  this.info.synchronization_transfer_info.setText(resp_temp);
                  this.info.synchronization_transfer_bar.setValue(100);
                  this.info.synchronization_transfer_bar.setBackground(Color.green);
                  this.info.synchronization_transfer_bar.setForeground(Color.green);
                  this.info.synchronization_transfer_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var26) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var26);
            }

            this.obtener_registros_restantes();
            
            try {
               this.info.synchronization_sales_start.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
               this.info.synchronization_sales_info.setText("Sincronizando...<->");
               resp_temp = this.sincroniza_registros_ventas();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var9) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var9);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var8) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var8);
                  }

                  this.info.synchronization_sales_info.setText(resp_temp);
                  this.info.synchronization_sales_bar.setValue(100);
                  this.info.synchronization_sales_bar.setBackground(Color.red);
                  this.info.synchronization_sales_bar.setForeground(Color.red);
                  this.info.synchronization_sales_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_sales_info.setText(resp_temp);
                  this.info.synchronization_sales_bar.setValue(100);
                  this.info.synchronization_sales_bar.setBackground(Color.green);
                  this.info.synchronization_sales_bar.setForeground(Color.green);
                  this.info.synchronization_sales_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var34) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var34);
            }

            this.obtener_registros_restantes();

            try {
               this.info.synchronization_sales_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_sales_info.setText("Sincronizando...<->");
               resp_temp = this.sincroniza_ventas();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var11) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var11);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var10) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var10);
                  }

                  this.info.synchronization_sales_info.setText(resp_temp);
                  this.info.synchronization_sales_bar_update.setValue(100);
                  this.info.synchronization_sales_bar_update.setBackground(Color.red);
                  this.info.synchronization_sales_bar_update.setForeground(Color.red);
                  this.info.synchronization_sales_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_sales_info.setText(resp_temp);
                  this.info.synchronization_sales_bar_update.setValue(100);
                  this.info.synchronization_sales_bar_update.setBackground(Color.green);
                  this.info.synchronization_sales_bar_update.setForeground(Color.green);
                  this.info.synchronization_sales_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var33) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var33);
            }

            this.obtener_registros_restantes();

            try {
               this.info.synchronization_returns_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_returns_info.setText("Sincronizando...<->");
               resp_temp = this.sincroniza_devoluciones();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var13) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var13);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var12) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var12);
                  }

                  this.info.synchronization_returns_info.setText(resp_temp);
                  this.info.synchronization_returns_bar.setValue(100);
                  this.info.synchronization_returns_bar.setBackground(Color.red);
                  this.info.synchronization_returns_bar.setForeground(Color.red);
                  this.info.synchronization_returns_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_returns_info.setText(resp_temp);
                  this.info.synchronization_returns_bar.setValue(100);
                  this.info.synchronization_returns_bar.setBackground(Color.green);
                  this.info.synchronization_returns_bar.setForeground(Color.green);
                  this.info.synchronization_returns_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var32) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var32);
            }

            this.obtener_registros_restantes();

            try {
               this.info.synchronization_movements_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_movements_info.setText("Sincronizando...<->");
               resp_temp = this.sincroniza_m_a();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var15) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var15);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var14) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var14);
                  }

                  this.info.synchronization_movements_info.setText(resp_temp);
                  this.info.synchronization_movements_bar.setValue(100);
                  this.info.synchronization_movements_bar.setBackground(Color.red);
                  this.info.synchronization_movements_bar.setForeground(Color.red);
                  this.info.synchronization_movements_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_movements_info.setText(resp_temp);
                  this.info.synchronization_movements_bar.setValue(100);
                  this.info.synchronization_movements_bar.setBackground(Color.green);
                  this.info.synchronization_movements_bar.setForeground(Color.green);
                  this.info.synchronization_movements_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var31) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var31);
            }

            this.obtener_registros_restantes();
            
            try {
               this.info.synchronization_movements_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_movements_info.setText("Sincronizando...<->");
               resp_temp = this.sys_sincronizacion_registros_movimientos_almacen();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var17) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var17);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var16) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var16);
                  }

                  this.info.synchronization_movements_info.setText(resp_temp);
                  this.info.synchronization_movements_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                  this.info.synchronization_movements_bar_update.setValue(100);
                  this.info.synchronization_movements_bar_update.setBackground(Color.red);
                  this.info.synchronization_movements_bar_update.setForeground(Color.red);
               } else {
                  this.info.synchronization_movements_info.setText(resp_temp);
                  this.info.synchronization_movements_bar_update.setValue(100);
                  this.info.synchronization_movements_bar_update.setBackground(Color.green);
                  this.info.synchronization_movements_bar_update.setForeground(Color.green);
                  this.info.synchronization_movements_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var30) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var30);
            }

            this.obtener_registros_restantes();

            try {
               this.info.synchronization_sales_validation_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_sales_validation_info.setText("Sincronizando...<->");
               resp_temp = this.sincroniza_validaciones_ventas();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var19) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var19);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var18) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var18);
                  }

                  this.info.synchronization_sales_validation_info.setText(resp_temp);
                  this.info.synchronization_sales_validation_bar.setValue(100);
                  this.info.synchronization_sales_validation_bar.setBackground(Color.red);
                  this.info.synchronization_sales_validation_bar.setForeground(Color.red);
                  this.info.synchronization_sales_validation_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_sales_validation_info.setText(resp_temp);
                  this.info.synchronization_sales_validation_bar.setValue(100);
                  this.info.synchronization_sales_validation_bar.setBackground(Color.green);
                  this.info.synchronization_sales_validation_bar.setForeground(Color.green);
                  this.info.synchronization_sales_validation_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var29) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var29);
            }

            this.obtener_registros_restantes();

            try {
               this.info.synchronization_product_provider_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_product_provider_info.setText("Sincronizando...<->");
               resp_temp = this.sincroniza_movimientos_proveedor_producto();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var21) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var21);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var20) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var20);
                  }

                  this.info.synchronization_product_provider_info.setText(resp_temp);
                  this.info.synchronization_product_provider_bar.setValue(100);
                  this.info.synchronization_product_provider_bar.setBackground(Color.red);
                  this.info.synchronization_product_provider_bar.setForeground(Color.red);
                  this.info.synchronization_product_provider_end.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_product_provider_info.setText(resp_temp);
                  this.info.synchronization_product_provider_bar.setValue(100);
                  this.info.synchronization_product_provider_bar.setBackground(Color.green);
                  this.info.synchronization_product_provider_bar.setForeground(Color.green);
                  this.info.synchronization_product_provider_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var28) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var28);
            }

            this.obtener_registros_restantes();
            
            try {
               this.info.synchronization_product_provider_start.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
               this.info.synchronization_product_provider_info.setText("Sincronizando...<->");
               resp_temp = this.sys_sincronizacion_registros_movimientos_proveedor_producto();
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var23) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var23);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var22) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var22);
                  }

                  this.info.synchronization_product_provider_info.setText(resp_temp);
                  this.info.synchronization_product_provider_bar_update.setValue(100);
                  this.info.synchronization_product_provider_bar_update.setBackground(Color.red);
                  this.info.synchronization_product_provider_bar_update.setForeground(Color.red);
                  this.info.synchronization_product_provider_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               } else {
                  this.info.synchronization_product_provider_info.setText(resp_temp);
                  this.info.synchronization_product_provider_bar_update.setValue(100);
                  this.info.synchronization_product_provider_bar_update.setBackground(Color.green);
                  this.info.synchronization_product_provider_bar_update.setForeground(Color.green);
                  this.info.synchronization_product_provider_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception var27) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var27);
            }

            this.obtener_registros_restantes();
           
            try {
               this.info.synchronization_depuration_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_depuration_info.setText("Depurando...");
               resp_temp = this.depurationProcess( false );
               if (!"ok".equals(resp_temp)) {
                  try {
                     this.InfoLog(resp_temp);
                     this.info.logArea.append(resp_temp + "\n");
                  } catch (IOException var25) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var25);
                  }

                  try {
                     this.InfoLog(resp_temp);
                  } catch (IOException var24) {
                     Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var24);
                  }

                  this.info.synchronization_depuration_info.setText(resp_temp);
                  this.info.synchronization_depuration_bar.setValue(100);
                  this.info.synchronization_depuration_bar.setBackground(Color.red);
                  this.info.synchronization_depuration_bar.setForeground(Color.red);
               } else {
                  this.info.synchronization_depuration_info.setText(resp_temp);
                  this.info.synchronization_depuration_bar.setValue(100);
                  this.info.synchronization_depuration_bar.setBackground(Color.green);
                  this.info.synchronization_depuration_bar.setForeground(Color.green);
                  this.info.synchronization_depuration_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               }
            } catch (Exception e) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, e);
            }

            this.obtener_registros_restantes();
            
            try {
               this.info.synchronization_depuration_log_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
               this.info.synchronization_depuration_log_info.setText("Depurando...");
               resp_temp = this.depurationLogProcess( false );
                if (!"ok".equals(resp_temp)) {
                    try {
                       this.InfoLog(resp_temp);
                       this.info.logArea.append(resp_temp + "\n");
                    } catch (IOException var25) {
                       Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var25);
                    }

                    try {
                       this.InfoLog(resp_temp);
                    } catch (IOException var24) {
                       Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var24);
                    }

                    this.info.synchronization_depuration_log_info.setText(resp_temp);
                    this.info.synchronization_depuration_log_bar.setValue(100);
                    this.info.synchronization_depuration_log_bar.setBackground(Color.red);
                    this.info.synchronization_depuration_log_bar.setForeground(Color.red);
                }else{
                    this.info.synchronization_depuration_log_info.setText(resp_temp);
                    this.info.synchronization_depuration_log_bar.setValue(100);
                    this.info.synchronization_depuration_log_bar.setBackground(Color.green);
                    this.info.synchronization_depuration_log_bar.setForeground(Color.green);
                    this.info.synchronization_depuration_log_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                }
            } catch (Exception e) {
               Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, e);
            }
            
            this.sincronizando = 0;
            this.info.last_sync.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
            //this.info.notification_sync.setVisible(false);
         } else {
            JOptionPane.showMessageDialog((Component)null, "Aun esta sincronizando!!!");
         }

         Thread.sleep((long)this.tiempo_buscar);
      }
   }

   public String sincroniza_registros() throws Exception {
      int ya_existe = 0;
      int id_equivalente = 0;
      int actualiza_equivalente_reg = 0;
      float fraccion = 100.0F;
      String respuesta = "";
      String sql = "";
      String sql_verifica = "";
      String sql_tmp = "";
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_registros_sincronizacion";
      System.out.println("URL : " + urlParaVisitar);
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_registros_ventas() throws Exception {
      int ya_existe = 0;
      int id_equivalente = 0;
      int actualiza_equivalente_reg = 0;
      float fraccion = 100.0F;
      String respuesta = "";
      String sql = "";
      String sql_verifica = "";
      String sql_tmp = "";
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_registros_sincronizacion_ventas";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sys_sincronizacion_registros_movimientos_almacen() throws Exception {
      int ya_existe = 0;
      int id_equivalente = 0;
      int actualiza_equivalente_reg = 0;
      float fraccion = 100.0F;
      String respuesta = "";
      String sql = "";
      String sql_verifica = "";
      String sql_tmp = "";
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_registros_sincronizacion_mov_almacen";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sys_sincronizacion_registros_movimientos_proveedor_producto() throws Exception {
      int ya_existe = 0;
      int id_equivalente = 0;
      int actualiza_equivalente_reg = 0;
      float fraccion = 100.0F;
      String respuesta = "";
      String sql = "";
      String sql_verifica = "";
      String sql_tmp = "";
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_registros_sincronizacion_mov_p_p";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sys_sincronizacion_registros_transferencias() throws Exception {
      int ya_existe = 0;
      int id_equivalente = 0;
      int actualiza_equivalente_reg = 0;
      float fraccion = 100.0F;
      String respuesta = "";
      String sql = "";
      String sql_verifica = "";
      String sql_tmp = "";
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_registros_sincronizacion_transferencias";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_ventas() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_ventas";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_devoluciones() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_devoluciones";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_m_a() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_movimientos_almacen";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_validaciones_ventas() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_validaciones_ventas";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_movimientos_proveedor_producto() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/v1/obtener_movimientos_proveedor_producto";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_archivos() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/print/";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String sincroniza_archivos_() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/print/";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

   public String verficacion_dominio() throws Exception {
      String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/netPay/domain_test";
      StringBuilder resultado = new StringBuilder();
      URL url = new URL(urlParaVisitar);
      HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
      conexion.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

      String linea;
      while((linea = rd.readLine()) != null) {
         resultado.append(linea);
      }

      rd.close();
      return resultado.toString();
   }

    public String obtener_registros_restantes() throws Exception {
       System.out.println("obtener_registros_restantes");
       String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/crones/consultar_registros_restantes";
       StringBuilder resultado = new StringBuilder();
       URL url = new URL(urlParaVisitar);
       HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
       conexion.setRequestMethod("GET");
       BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

       String linea;
       while((linea = rd.readLine()) != null) {
          resultado.append(linea);
       }

       rd.close();
       String tmp = resultado.toString();
       String[] registrosPendientes = tmp.split(",");
       System.out.println("Reg pendientes : " + registrosPendientes[0]);
       this.info.synchronization_rows_upload.setText("" + registrosPendientes[0]);
       this.info.synchronization_rows_download.setText("" + registrosPendientes[7]);
       this.info.synchronization_sales_upload.setText("" + registrosPendientes[1]);
       this.info.synchronization_sales_download.setText("" + registrosPendientes[8]);
       this.info.synchronization_returns_upload.setText("" + registrosPendientes[2]);
       this.info.synchronization_returns_download.setText("" + registrosPendientes[9]);
       this.info.synchronization_movements_upload.setText("" + registrosPendientes[3]);
       this.info.synchronization_movements_download.setText("" + registrosPendientes[10]);
       this.info.synchronization_sales_validation_upload.setText("" + registrosPendientes[4]);
       this.info.synchronization_sales_validation_download.setText("" + registrosPendientes[11]);
       this.info.synchronization_product_provider_upload.setText("" + registrosPendientes[5]);
       this.info.synchronization_product_provider_download.setText("" + registrosPendientes[12]);
       this.info.synchronization_transfer_upload.setText("" + registrosPendientes[6]);
       this.info.synchronization_transfer_download.setText("" + registrosPendientes[13]);
       this.info.url_field.setText("" + registrosPendientes[14]);
       this.info.synchronization_rows_number.setText("" + registrosPendientes[15]);
       this.info.synchronization_sales_number.setText("" + registrosPendientes[16]);
       this.info.synchronization_returns_number.setText("" + registrosPendientes[17]);
       this.info.synchronization_movements_number.setText("" + registrosPendientes[18]);
       this.info.synchronization_sales_validation_number.setText("" + registrosPendientes[19]);
       this.info.synchronization_product_provider_number.setText("" + registrosPendientes[20]);
       this.info.synchronization_transfer_number.setText("" + registrosPendientes[21]);
       return resultado.toString();
    }
   
    public void reset_progress_bar() {
       this.info.synchronization_rows_info.setText("0%");
       this.info.synchronization_rows_bar.setValue(0);
       this.info.synchronization_sales_info.setText("0%");
       this.info.synchronization_sales_bar.setValue(0);
       this.info.synchronization_sales_bar_update.setValue(0);
       this.info.synchronization_returns_info.setText("0%");
       this.info.synchronization_returns_bar.setValue(0);
       this.info.synchronization_movements_info.setText("0%");
       this.info.synchronization_movements_bar.setValue(0);
       this.info.synchronization_movements_bar_update.setValue(0);
       this.info.synchronization_sales_validation_info.setText("0%");
       this.info.synchronization_sales_validation_bar.setValue(0);
       this.info.synchronization_product_provider_info.setText("0%");
       this.info.synchronization_product_provider_bar.setValue(0);
       this.info.synchronization_product_provider_bar_update.setValue(0);
       this.info.synchronization_transfer_info.setText("0%");
       this.info.synchronization_transfer_bar.setValue(0);
       this.info.synchronization_depuration_info.setText("0%");
       this.info.synchronization_depuration_bar.setValue(0);
       this.info.synchronization_depuration_log_info.setText("0%");
       this.info.synchronization_depuration_log_bar.setValue(0);
    }

    public String depurationProcess( Boolean is_complete ) throws MalformedURLException, IOException{
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/crones/depurar_sincronizacion";
        if( is_complete ){
            urlParaVisitar += "?is_complete=1";
        }
        StringBuilder resultado = new StringBuilder();
        URL url = new URL(urlParaVisitar);
        HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
        conexion.setRequestMethod("POST");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

        String linea;
        while((linea = rd.readLine()) != null) {
           resultado.append(linea);
        }

        rd.close();
        return resultado.toString();
   }
    public String depurationLogProcess( Boolean is_complete ) throws MalformedURLException, IOException{
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest/crones/depurar_logs";
        if( is_complete ){
            urlParaVisitar += "?is_complete=1";
        }
        StringBuilder resultado = new StringBuilder();
        URL url = new URL(urlParaVisitar);
        HttpURLConnection conexion = (HttpURLConnection)url.openConnection();
        conexion.setRequestMethod("POST");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

        String linea;
        while((linea = rd.readLine()) != null) {
           resultado.append(linea);
        }

        rd.close();
        return resultado.toString();
   }
    
    public String getCurrentTime(){
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    // Definir la zona horaria
        ZoneId zoneId = ZoneId.of("America/Mexico_City");
    // Convertir LocalDateTime a ZonedDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
    // Mostrar la fecha y hora con la zona horaria
        return dtf.format(zonedDateTime );
        //System.out.println("Fecha y hora en la zona horaria especificada: " + dtf.format(zonedDateTime ));
    }
}
