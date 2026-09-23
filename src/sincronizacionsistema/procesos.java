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
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.log4j.LogManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class procesos {
    private static final int HTTP_CONNECT_TIMEOUT_MS = 15000;
    private static final int HTTP_READ_TIMEOUT_MS = 120000;
    final static org.apache.log4j.Logger logger4j = LogManager.getLogger(procesos.class);//implementacion de logger4j 2024-10-23 
    infoSinccronizacion info;//= new infoSinccronizacion();
    public int id_sucursal;
    public int tiempo_buscar;
    public int depuration_interval;
    int sincronizando = 0;
    public int bloqueado = 0;
    Timer tiempo;
    public static String final_local_system_path;
    public String depuration_time;
    String dir;
    FileWriter archivo;

    public procesos(String ruta_conexion, String system_path, String depuration_time, int depuration_interval) throws SQLException, IOException {
        this.final_local_system_path = system_path;
        //this.conexion_local = this.conecta.conecta_local();
        this. depuration_time = depuration_time;
        this.depuration_interval = depuration_interval;
        info = new infoSinccronizacion();//depuration_time, depuration_interval
        
        info.synchronization_depuration_start.setText(this.depuration_time);
        info.synchronization_depuration_log_start.setText(this.depuration_time);
        info.HORA_BASE = depuration_time;
        info.INTERVALO_MINUTOS = (depuration_interval <= 0 ? 30 : depuration_interval );//depuration_interval;
System.out.println("det_time : " + depuration_time + "\ndep_interval : " + depuration_interval);
        info.startTimer();
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
 /*Agregado por Oscar 2024-10-18 para loguear errores de try-catch*/
    public void errorLog(String error) throws IOException {
      if (!(new File("ErrorLog")).exists()) {
         this.archivo = new FileWriter(new File("ErrorLog"), false);
      }

      this.archivo = new FileWriter(new File("ErrorLog"), true);
      Calendar fechaActual = Calendar.getInstance();
      this.archivo.write("[" + String.valueOf(fechaActual.get(5)) + "/" + (fechaActual.get(2) + 1) + "/" + fechaActual.get(1) + " " + fechaActual.get(11) + ":" + fechaActual.get(12) + ":" + fechaActual.get(13) + "][INFO] " + error + "\r\n");
      this.archivo.close();
   }

   public String ejecutaIntervalo(long tiempo_retardo_inicio) throws InterruptedException, Exception {
      this.info.setVisible(true);
    //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    int count_resp = 0;
      while(true) {
        try {
         if (this.sincronizando == 0) {
            this.sincronizando = 1;
            info.api_local_path = this.final_local_system_path;
            //this.info.notification_sync.setVisible(false);
            this.info.url_field.setText(this.final_local_system_path);
            this.info.time_interval_field.setText("" + this.tiempo_buscar / 1000);
            this.reset_progress_bar();
            String resp_temp = "";
            this.obtener_registros_restantes();
            Map<String, JSONObject> modules = sendInitialPetition(true);//manda consumir servicio para saber que modulos si tienen que sincronizar
            //System.out.println("Modules : " + modules);
        //registros de sincronizacion
System.out.println("sys_sincronizacion_registros : " + this.bloqueado);       
            if ( modules.containsKey("sys_sincronizacion_registros") && this.bloqueado == 0 ) {//1 == 1
System.out.println("Entr en sys_sincronizacion_registros");
                try {
                    this.info.synchronization_rows_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_rows_info.setText("Sincronizando...");
//System.out.println("modules : " + modules);
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_registros");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
//System.out.println("Entra en comprobacion : comprobacion_local_registros_sincronizacion");
                        resp_temp = this.sendPetition("comprobacion_local_registros_sincronizacion");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_registros_sincronizacion");
                        resp_temp = this.sendPetition("obtener_registros_sincronizacion");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try {
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch(IOException var7){
                            logger4j.error(var7.toString());
                            this.errorLog( var7.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var7);
                        }

                        try{
                            this.InfoLog(resp_temp);
                        }catch (IOException var6) {
                            logger4j.error(var6.toString());
                            this.errorLog( var6.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var6);
                        }

                        logger4j.info(resp_temp.toString());
                        this.info.synchronization_rows_info.setText(resp_temp);
                        this.info.synchronization_rows_bar.setValue(100);
                        this.info.synchronization_rows_bar.setBackground(Color.red);
                        this.info.synchronization_rows_bar.setForeground(Color.red);
                        this.info.synchronization_rows_end.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
                    }else{
                        this.info.synchronization_rows_info.setText(resp_temp);
                        this.info.synchronization_rows_bar.setValue(100);
                        this.info.synchronization_rows_bar.setBackground(Color.green);
                        this.info.synchronization_rows_bar.setForeground(Color.green);
                        this.info.synchronization_rows_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }
                    this.obtener_registros_restantes();
                }catch(Exception var35) {
                    logger4j.error(var35.toString());
                    this.errorLog( var35.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var35);
                }
            }else{
                this.info.synchronization_rows_bar.setValue(100);
                this.info.synchronization_rows_bar.setBackground(Color.blue);
                this.info.synchronization_rows_bar.setForeground(Color.blue);
            }
        //registros de sincronizacion de transferencias
            if ( modules.containsKey("sys_sincronizacion_registros_transferencias") && this.bloqueado == 0 ) {//1 == 1
                try {
//this.obtener_registros_restantes();
                    this.info.synchronization_transfer_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_transfer_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_registros_transferencias");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_registros_sincronizacion_transferencias");
                        resp_temp = this.sendPetition("comprobacion_local_registros_sincronizacion_transferencias");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_registros_sincronizacion_transferencias");
                        resp_temp = this.sendPetition("obtener_registros_sincronizacion_transferencias");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try{
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch(IOException var25) {
                            logger4j.error(var25.toString());
                            this.errorLog( var25.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var25);
                        }

                        try{
                           this.InfoLog(resp_temp);
                        }catch (IOException var24) {
                          logger4j.error(var24.toString());
                          this.errorLog( var24.toString() );
                          Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var24);
                        }

                        logger4j.info(resp_temp.toString());
                        this.info.synchronization_transfer_info.setText(resp_temp);
                        this.info.synchronization_transfer_bar.setValue(100);
                        this.info.synchronization_transfer_bar.setBackground(Color.red);
                        this.info.synchronization_transfer_bar.setForeground(Color.red);
                    }else{
                        this.info.synchronization_transfer_info.setText(resp_temp);
                        this.info.synchronization_transfer_bar.setValue(100);
                        this.info.synchronization_transfer_bar.setBackground(Color.green);
                        this.info.synchronization_transfer_bar.setForeground(Color.green);
                        this.info.synchronization_transfer_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }
                    this.obtener_registros_restantes();
                }catch(Exception var26) {
                    logger4j.error(var26.toString());
                    this.errorLog( var26.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var26);
                }
            }else{
                this.info.synchronization_transfer_bar.setValue(100);
                this.info.synchronization_transfer_bar.setBackground(Color.blue);
                this.info.synchronization_transfer_bar.setForeground(Color.blue);
            }

        //sincronizacion de ventas
            if ( modules.containsKey("sys_sincronizacion_ventas") && this.bloqueado == 0 ) {//1 == 1
                try {
//this.obtener_registros_restantes();
                    this.info.synchronization_sales_start.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
                    this.info.synchronization_sales_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_ventas");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_ventas");
                        resp_temp = this.sendPetition("comprobacion_local_ventas");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_ventas");
                        resp_temp = this.sendPetition("obtener_ventas");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try{
                           this.InfoLog(resp_temp);
                           this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch (IOException var9) {   
                          logger4j.error(var9.toString());                 
                          this.errorLog( var9.toString() );
                          Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var9);
                        }

                        try{
                           this.InfoLog(resp_temp);
                        }catch (IOException var8) {   
                          logger4j.error(var8.toString());              
                          this.errorLog( var8.toString() );
                          Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var8);
                        }

                        logger4j.info(resp_temp.toString());
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
                    this.obtener_registros_restantes();
                }catch(Exception var34) {  
                    logger4j.error(var34.toString());               
                    this.errorLog( var34.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var34);
                }
            }else{
                this.info.synchronization_sales_bar.setValue(100);
                this.info.synchronization_sales_bar.setBackground(Color.blue);
                this.info.synchronization_sales_bar.setForeground(Color.blue);
            }

        //sincronizacion de registros de ventas
            if ( modules.containsKey("sys_sincronizacion_registros_ventas") && this.bloqueado == 0 ) {//1 == 1
                try {
//this.obtener_registros_restantes();
                    this.info.synchronization_sales_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_sales_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_registros_ventas");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_registros_sincronizacion_ventas");
                        resp_temp = this.sendPetition("comprobacion_local_registros_sincronizacion_ventas");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_registros_sincronizacion_ventas");
                        resp_temp = this.sendPetition("obtener_registros_sincronizacion_ventas");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try{
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch (IOException var11) {    
                            logger4j.error(var11.toString());             
                            this.errorLog( var11.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var11);
                        }

                        try{
                           this.InfoLog(resp_temp);
                        }catch(IOException var10) {      
                            logger4j.error(var10.toString());           
                            this.errorLog( var10.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var10);
                        }

                        logger4j.info(resp_temp.toString());
                        this.info.synchronization_sales_info.setText(resp_temp);
                        this.info.synchronization_sales_bar_update.setValue(100);
                        this.info.synchronization_sales_bar_update.setBackground(Color.red);
                        this.info.synchronization_sales_bar_update.setForeground(Color.red);
                        this.info.synchronization_sales_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }else{
                        this.info.synchronization_sales_info.setText(resp_temp);
                        this.info.synchronization_sales_bar_update.setValue(100);
                        this.info.synchronization_sales_bar_update.setBackground(Color.green);
                        this.info.synchronization_sales_bar_update.setForeground(Color.green);
                        this.info.synchronization_sales_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }
                    this.obtener_registros_restantes();
                }catch(Exception var33){     
                    logger4j.error(var33.toString());            
                    this.errorLog( var33.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var33);
                }
            }else{
                this.info.synchronization_sales_bar_update.setValue(100);
                this.info.synchronization_sales_bar_update.setBackground(Color.blue);
                this.info.synchronization_sales_bar_update.setForeground(Color.blue);
            }

        //sincronizacion de registros de ventas
            if ( modules.containsKey("sys_sincronizacion_devoluciones") && this.bloqueado == 0 ) {//1 == 1
                try {
//this.obtener_registros_restantes();
                    this.info.synchronization_returns_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_returns_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_devoluciones");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_devoluciones");
                        resp_temp = this.sendPetition("comprobacion_local_devoluciones");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_devoluciones");
                        resp_temp = this.sendPetition("obtener_devoluciones");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try{
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch (IOException var13) {   
                            logger4j.error(var13.toString());              
                            this.errorLog( var13.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var13);
                        }

                        try{
                           this.InfoLog(resp_temp);
                        }catch (IOException var12) { 
                          logger4j.error(var12.toString());                
                          this.errorLog( var12.toString() );
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
                    this.obtener_registros_restantes();
                }catch (Exception var32) {
                    logger4j.error(var32.toString());                 
                    this.errorLog( var32.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var32);
                }
            }else{
                this.info.synchronization_returns_bar.setValue(100);
                this.info.synchronization_returns_bar.setBackground(Color.blue);
                this.info.synchronization_returns_bar.setForeground(Color.blue);
            }


        //sincronizacion de movimientos almacen
            if ( modules.containsKey("sys_sincronizacion_movimientos_almacen") && this.bloqueado == 0 ) {//1 == 1
                try {
//this.obtener_registros_restantes();
                    this.info.synchronization_movements_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_movements_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_movimientos_almacen");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_movimientos_almacen");
                        resp_temp = this.sendPetition("comprobacion_local_movimientos_almacen");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_movimientos_almacen");
                        resp_temp = this.sendPetition("obtener_movimientos_almacen");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try {
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        } catch (IOException var15) {   
                            logger4j.error(var15.toString());              
                            this.errorLog( var15.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var15);
                        }

                        try {
                            this.InfoLog(resp_temp);
                        }catch (IOException var14){   
                            logger4j.error(var14.toString());              
                            this.errorLog( var14.toString() );
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
                    this.obtener_registros_restantes();
                } catch (Exception var31) {  
                    logger4j.error(var31.toString());               
                    this.errorLog( var31.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var31);
                }
            }else{
                this.info.synchronization_movements_bar.setValue(100);
                this.info.synchronization_movements_bar.setBackground(Color.blue);
                this.info.synchronization_movements_bar.setForeground(Color.blue);
            }

            
        //sincronizacion de registros de movimientos almacen
            if ( modules.containsKey("sys_sincronizacion_registros_movimientos_almacen") && this.bloqueado == 0 ) {//1 == 1
                try {
//this.obtener_registros_restantes();
                    this.info.synchronization_movements_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_movements_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_registros_movimientos_almacen");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_registros_movimientos_almacen");
                        resp_temp = this.sendPetition("comprobacion_local_registros_movimientos_almacen");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_registros_sincronizacion_mov_almacen");
                        resp_temp = this.sendPetition("obtener_registros_sincronizacion_mov_almacen");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try {
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch(IOException var17){   
                            logger4j.error(var17.toString());              
                            this.errorLog( var17.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var17);
                        }

                        try{
                           this.InfoLog(resp_temp);
                        }catch (IOException var16){  
                            logger4j.error(var16.toString());               
                            this.errorLog( var16.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var16);
                        }

                        this.info.synchronization_movements_info.setText(resp_temp);
                        this.info.synchronization_movements_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                        this.info.synchronization_movements_bar_update.setValue(100);
                        this.info.synchronization_movements_bar_update.setBackground(Color.red);
                        this.info.synchronization_movements_bar_update.setForeground(Color.red);
                    }else{
                        this.info.synchronization_movements_info.setText(resp_temp);
                        this.info.synchronization_movements_bar_update.setValue(100);
                        this.info.synchronization_movements_bar_update.setBackground(Color.green);
                        this.info.synchronization_movements_bar_update.setForeground(Color.green);
                        this.info.synchronization_movements_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }
                    this.obtener_registros_restantes();
                }catch(Exception var30) {            
                    logger4j.error(var30.toString());     
                    this.errorLog( var30.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var30);
                }
            }else{
                this.info.synchronization_movements_bar_update.setValue(100);
                this.info.synchronization_movements_bar_update.setBackground(Color.blue);
                this.info.synchronization_movements_bar_update.setForeground(Color.blue);
            }

        //sincronizacion de validaciones de ventas
            if ( modules.containsKey("sys_sincronizacion_validaciones_ventas") && this.bloqueado == 0 ) {//1 == 1
                try {
//this.obtener_registros_restantes();
                    this.info.synchronization_sales_validation_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_sales_validation_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_validaciones_ventas");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_validaciones_ventas");
                        resp_temp = this.sendPetition("comprobacion_local_validaciones_ventas");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_validaciones_ventas");
                        resp_temp = this.sendPetition("obtener_validaciones_ventas");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try{
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch(IOException var19){ 
                            logger4j.error(var19.toString());                
                            this.errorLog( var19.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var19);
                        }

                        try{
                            this.InfoLog(resp_temp);
                        }catch (IOException var18){   
                            logger4j.error(var18.toString());              
                            this.errorLog( var18.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var18);
                        }

                        this.info.synchronization_sales_validation_info.setText(resp_temp);
                        this.info.synchronization_sales_validation_bar.setValue(100);
                        this.info.synchronization_sales_validation_bar.setBackground(Color.red);
                        this.info.synchronization_sales_validation_bar.setForeground(Color.red);
                        this.info.synchronization_sales_validation_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }else{
                        this.info.synchronization_sales_validation_info.setText(resp_temp);
                        this.info.synchronization_sales_validation_bar.setValue(100);
                        this.info.synchronization_sales_validation_bar.setBackground(Color.green);
                        this.info.synchronization_sales_validation_bar.setForeground(Color.green);
                        this.info.synchronization_sales_validation_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }
                    this.obtener_registros_restantes();
                } catch (Exception var29) {         
                    logger4j.error(var29.toString());        
                    this.errorLog( var29.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var29);
                }
            }else{
                this.info.synchronization_sales_validation_bar.setValue(100);
                this.info.synchronization_sales_validation_bar.setBackground(Color.blue);
                this.info.synchronization_sales_validation_bar.setForeground(Color.blue);
            }

            
        //sincronizacion de movimientos de almacen proveedor producto
            if ( modules.containsKey("sys_sincronizacion_movimientos_proveedor_producto") && this.bloqueado == 0 ) {//1 == 1
                try {
                    this.info.synchronization_product_provider_start.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    this.info.synchronization_product_provider_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_movimientos_proveedor_producto");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_movimientos_proveedor_producto");
                        resp_temp = this.sendPetition("comprobacion_local_movimientos_proveedor_producto");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_movimientos_proveedor_producto");
                        resp_temp = this.sendPetition("obtener_movimientos_proveedor_producto");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try {
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        } catch (IOException var21) {  
                            logger4j.error(var21.toString());               
                            this.errorLog( var21.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var21);
                        }

                        try {
                            this.InfoLog(resp_temp);
                        } catch (IOException var20) { 
                            logger4j.error(var20.toString());                
                            this.errorLog( var20.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var20);
                        }

                        this.info.synchronization_product_provider_info.setText(resp_temp);
                        this.info.synchronization_product_provider_bar.setValue(100);
                        this.info.synchronization_product_provider_bar.setBackground(Color.red);
                        this.info.synchronization_product_provider_bar.setForeground(Color.red);
                        this.info.synchronization_product_provider_end.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
                    }else{
                        this.info.synchronization_product_provider_info.setText(resp_temp);
                        this.info.synchronization_product_provider_bar.setValue(100);
                        this.info.synchronization_product_provider_bar.setBackground(Color.green);
                        this.info.synchronization_product_provider_bar.setForeground(Color.green);
                        this.info.synchronization_product_provider_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }
                    this.obtener_registros_restantes();
                } catch (Exception var28){             
                    logger4j.error(var28.toString());    
                    this.errorLog( var28.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var28);
                }
            }else{
                this.info.synchronization_product_provider_bar.setValue(100);
                this.info.synchronization_product_provider_bar.setBackground(Color.blue);
                this.info.synchronization_product_provider_bar.setForeground(Color.blue);
            }

        //sincronizacion de registros de movimientos de almacen proveedor producto
            if ( modules.containsKey("sys_sincronizacion_registros_movimientos_proveedor_producto") && this.bloqueado == 0 ){//1 == 1
                try {
                    this.info.synchronization_product_provider_start.setText("" + getCurrentTime());//dtf.format(LocalDateTime.now())
                    this.info.synchronization_product_provider_info.setText("Sincronizando...");
                    
                    JSONObject modulo = (JSONObject) modules.get("sys_sincronizacion_registros_movimientos_proveedor_producto");
                    long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                    long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                    long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                    long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                    if(pendingLocalComprobation > 0 || pendingServerComprobation > 0){
                        System.out.println("Entra en comprobacion : comprobacion_local_resgistros_movimientos_proveedor_producto");
                        resp_temp = this.sendPetition("comprobacion_local_resgistros_movimientos_proveedor_producto");
                    }else if(pendingLocal > 0 || pendingServer > 0){
                        System.out.println("Entra en sincronizacion : obtener_registros_sincronizacion_mov_p_p");
                        resp_temp = this.sendPetition("obtener_registros_sincronizacion_mov_p_p");
                    }
                    
                    logger4j.info( "Iteracion  : " + count_resp + "  respuesta :  " + resp_temp );
                    if (!"ok".equals(resp_temp)) {
                        try{
                            this.InfoLog(resp_temp);
                            this.info.logArea.append(resp_temp + getCurrentTime() + "\n");
                        }catch(IOException var23) {  
                            logger4j.error(var23.toString());               
                            this.errorLog( var23.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var23);
                        }

                        try {
                            this.InfoLog(resp_temp);
                        } catch (IOException var22) { 
                            logger4j.error(var22.toString());                
                            this.errorLog( var22.toString() );
                            Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var22);
                        }

                        this.info.synchronization_product_provider_info.setText(resp_temp);
                        this.info.synchronization_product_provider_bar_update.setValue(100);
                        this.info.synchronization_product_provider_bar_update.setBackground(Color.red);
                        this.info.synchronization_product_provider_bar_update.setForeground(Color.red);
                        this.info.synchronization_product_provider_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }else{
                        this.info.synchronization_product_provider_info.setText(resp_temp);
                        this.info.synchronization_product_provider_bar_update.setValue(100);
                        this.info.synchronization_product_provider_bar_update.setBackground(Color.green);
                        this.info.synchronization_product_provider_bar_update.setForeground(Color.green);
                        this.info.synchronization_product_provider_end.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
                    }
                    this.obtener_registros_restantes();
                }catch(Exception var27){   
                    logger4j.error(var27.toString());              
                    this.errorLog( var27.toString() );
                    Logger.getLogger(procesos.class.getName()).log(Level.SEVERE, (String)null, var27);
                }
            }else{
                this.info.synchronization_product_provider_bar_update.setValue(100);
                this.info.synchronization_product_provider_bar_update.setBackground(Color.blue);
                this.info.synchronization_product_provider_bar_update.setForeground(Color.blue);
            }
            this.sincronizando = 0;
            this.info.last_sync.setText("" + getCurrentTime() );//dtf.format(LocalDateTime.now())
            //this.info.notification_sync.setVisible(false);
         } else {
            JOptionPane.showMessageDialog((Component)null, "Aun esta sincronizando!!!");
         }
        } catch (Exception ex) {
            // Ningun fallo de red, HTTP o JSON debe terminar el ciclo infinito.
            logger4j.error("Fallo en ciclo de sincronizacion; se reintentara en el siguiente intervalo", ex);
            try {
                this.errorLog(ex.toString());
            } catch (IOException logError) {
                logger4j.error("No fue posible escribir ErrorLog", logError);
            }
            this.info.logArea.append("Error temporal: " + ex.getMessage() + ". Se reintentara. " + getCurrentTime() + "\n");
            this.info.last_petition_time.setBackground(new Color(255, 0, 0));            
            this.info.last_petition_time.setForeground(Color.WHITE);   
        this.info.ultima_peticion_label.setForeground(new Color(255, 0, 0));
        } finally {
            // Evita que una excepcion deje el proceso marcado como ocupado para siempre.
            this.sincronizando = 0;
        }
         count_resp ++;
         Thread.sleep((long)this.tiempo_buscar);
        }
    }
   
    public Map<String, JSONObject> sendInitialPetition(boolean is_initial) throws Exception {
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/sincronizacion/obtener_registros_restantes_local";
        System.out.println("URL : http://localhost/" + this.final_local_system_path + "/rest_v2/sincronizacion/obtener_registros_restantes_local");

        String resultado = executeHttpRequest(urlParaVisitar, "GET");
//System.out.println("Respuesta verificacion : " + resultado.toString());

        // Parsear JSON
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(resultado);

        /*Map<String, JSONObject> modulosConPendientes = new HashMap<>();

        for (Object keyObj : jsonObject.keySet()) {
            String key = (String) keyObj;
            JSONObject modulo = (JSONObject) jsonObject.get(key);

            long pendingLocal = (long) modulo.get("pending_rows_local");
            long pendingServer = (long) modulo.get("pending_rows_server");

            if (pendingLocal > 0 || pendingServer > 0) {
                modulosConPendientes.put(key, modulo);
            }
        }*/
        Map<String, JSONObject> modulosConPendientes = new HashMap<>();

        for (Object keyObj : jsonObject.keySet()) {
//System.out.println("HERE");
            String key = (String) keyObj;
            JSONObject modulo = (JSONObject) jsonObject.get(key);
//System.out.println(pendingLocal + " > 0 || " + pendingServer + " > 0 || " + pendingLocalComprobation + " > 0 || " + pendingServerComprobation +" > 0");
            if ( is_initial == true && !key.equals("limites_local") && !key.equals("limites_linea")) {
                System.out.println("KEY : " + key);
                long pendingLocal = Long.parseLong(modulo.get("pending_rows_local").toString());
                long pendingServer = Long.parseLong(modulo.get("pending_rows_server").toString());
                long pendingLocalComprobation = Long.parseLong(modulo.get("pending_rows_client_comprobation").toString());
                long pendingServerComprobation = Long.parseLong(modulo.get("pending_rows_server_comprobation").toString());
                if((pendingLocal > 0 || pendingServer > 0 || pendingLocalComprobation > 0 || pendingServerComprobation > 0)){
                    modulosConPendientes.put(key, modulo);
                }
            }else if(is_initial == false){
                modulosConPendientes.put(key, modulo);
            }
        }
        
        this.info.last_petition_time.setText("" + getCurrentTime() );
        this.info.last_petition_time.setBackground(new Color(255, 255, 255));            
        this.info.last_petition_time.setForeground(new Color(0, 100, 0));        
        this.info.ultima_peticion_label.setForeground(new Color(0, 100, 0));

        return modulosConPendientes;
    }
   
    public String sendPetition(String module_endpoint) throws Exception {
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/sincronizacion/" + module_endpoint;
    //System.out.println("URL : " + urlParaVisitar);
        return executeHttpRequest(urlParaVisitar, "GET");
    }

    public String sincroniza_archivos() throws Exception {
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/sincronizacion/print/";
        return executeHttpRequest(urlParaVisitar, "GET");
    }

    public String sincroniza_archivos_() throws Exception {
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/sincronizacion/print/";
        return executeHttpRequest(urlParaVisitar, "GET");
    }

    public String verficacion_dominio() throws Exception {
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/sincronizacion/netPay/domain_test";
        return executeHttpRequest(urlParaVisitar, "GET");
    }

    public String obtener_registros_restantes() throws Exception {
//System.out.println("obtener_registros_restantes");
       /*String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/crones/consultar_registros_restantes";
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
        String[] registrosPendientes = tmp.split(",");*/
        Map<String, JSONObject> modules = sendInitialPetition(false);//manda consumir servicio para saber que modulos si tienen que sincronizar
        /*if( registrosPendientes.length < 22 ){
            this.info.logArea.append("El API no regreso una respuesta correcta." + getCurrentTime() + "\n");
            throw new Exception("El API no regreso una respuesta correcta.");
            //return resultado.toString();
        }else{*/
//System.out.println("Reg pendientes : " + registrosPendientes[0]);
            //String example = modules.get("sys_sincronizacion_registros").get("pending_rows_local").toString();
            this.info.synchronization_rows_upload.setText(modules.get("sys_sincronizacion_registros").get("pending_rows_local").toString());
            this.info.synchronization_rows_upload_comprobation.setText(modules.get("sys_sincronizacion_registros").get("pending_rows_client_comprobation").toString());
            this.info.synchronization_rows_download.setText(modules.get("sys_sincronizacion_registros").get("pending_rows_server").toString());
            this.info.synchronization_rows_download_comprobation.setText(modules.get("sys_sincronizacion_registros").get("pending_rows_server_comprobation").toString());
            
            this.info.synchronization_transfer_upload.setText(modules.get("sys_sincronizacion_registros_transferencias").get("pending_rows_local").toString());
            this.info.synchronization_transfer_upload_comprobation.setText(modules.get("sys_sincronizacion_registros_transferencias").get("pending_rows_client_comprobation").toString());
            this.info.synchronization_transfer_download.setText(modules.get("sys_sincronizacion_registros_transferencias").get("pending_rows_server").toString());
            this.info.synchronization_transfer_download_comprobation.setText(modules.get("sys_sincronizacion_registros_transferencias").get("pending_rows_server_comprobation").toString());
            
            this.info.synchronization_sales_upload.setText(modules.get("sys_sincronizacion_ventas").get("pending_rows_local").toString() + " | " + modules.get("sys_sincronizacion_registros_ventas").get("pending_rows_local").toString());
            this.info.synchronization_sales_upload_comprobation.setText(modules.get("sys_sincronizacion_ventas").get("pending_rows_client_comprobation").toString() + " | " + modules.get("sys_sincronizacion_registros_ventas").get("pending_rows_client_comprobation").toString());
            this.info.synchronization_sales_download.setText(modules.get("sys_sincronizacion_ventas").get("pending_rows_server").toString() + " | " + modules.get("sys_sincronizacion_registros_ventas").get("pending_rows_server").toString());
            this.info.synchronization_sales_download_comprobation.setText(modules.get("sys_sincronizacion_ventas").get("pending_rows_server_comprobation").toString() + " | " + modules.get("sys_sincronizacion_registros_ventas").get("pending_rows_server_comprobation").toString());
            
            
            this.info.synchronization_returns_upload.setText(modules.get("sys_sincronizacion_devoluciones").get("pending_rows_local").toString());
            this.info.synchronization_returns_upload_comprobation.setText(modules.get("sys_sincronizacion_devoluciones").get("pending_rows_client_comprobation").toString());
            this.info.synchronization_returns_download.setText(modules.get("sys_sincronizacion_devoluciones").get("pending_rows_server").toString());
            this.info.synchronization_returns_download_comprobation.setText(modules.get("sys_sincronizacion_devoluciones").get("pending_rows_server_comprobation").toString());
            
            this.info.synchronization_movements_upload.setText(modules.get("sys_sincronizacion_movimientos_almacen").get("pending_rows_local").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_almacen").get("pending_rows_local").toString());
            this.info.synchronization_movements_upload_comprobation.setText(modules.get("sys_sincronizacion_movimientos_almacen").get("pending_rows_client_comprobation").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_almacen").get("pending_rows_client_comprobation").toString());
            this.info.synchronization_movements_download.setText(modules.get("sys_sincronizacion_movimientos_almacen").get("pending_rows_server").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_almacen").get("pending_rows_server").toString());
            this.info.synchronization_movements_download_comprobation.setText(modules.get("sys_sincronizacion_movimientos_almacen").get("pending_rows_server_comprobation").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_almacen").get("pending_rows_server_comprobation").toString());
            
            this.info.synchronization_sales_validation_upload.setText(modules.get("sys_sincronizacion_validaciones_ventas").get("pending_rows_local").toString());
            this.info.synchronization_sales_validation_upload_comprobation.setText(modules.get("sys_sincronizacion_validaciones_ventas").get("pending_rows_client_comprobation").toString());
            this.info.synchronization_sales_validation_download.setText(modules.get("sys_sincronizacion_validaciones_ventas").get("pending_rows_server").toString());
            this.info.synchronization_sales_validation_download_comprobation.setText(modules.get("sys_sincronizacion_validaciones_ventas").get("pending_rows_server_comprobation").toString());
            
            this.info.synchronization_product_provider_upload.setText(modules.get("sys_sincronizacion_movimientos_proveedor_producto").get("pending_rows_local").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_proveedor_producto").get("pending_rows_local").toString());
            this.info.synchronization_product_provider_upload_comprobation.setText(modules.get("sys_sincronizacion_movimientos_proveedor_producto").get("pending_rows_client_comprobation").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_proveedor_producto").get("pending_rows_client_comprobation").toString());
            this.info.synchronization_product_provider_download.setText(modules.get("sys_sincronizacion_movimientos_proveedor_producto").get("pending_rows_server").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_proveedor_producto").get("pending_rows_server").toString());
            this.info.synchronization_product_provider_download_comprobation.setText(modules.get("sys_sincronizacion_movimientos_proveedor_producto").get("pending_rows_server_comprobation").toString() + " | " + modules.get("sys_sincronizacion_registros_movimientos_proveedor_producto").get("pending_rows_server_comprobation").toString());
            
            this.info.url_field.setText(modules.get("limites_local").get("url_api").toString());
            
            this.bloqueado = Integer.parseInt(modules.get("limites_linea").get("bloqueo_apis_linea").toString());
            
            this.info.synchronization_rows_number.setText("↑" + modules.get("limites_local").get("sys_sincronizacion_registros").toString() + " ↓" + modules.get("limites_linea").get("sys_sincronizacion_registros").toString());     
            this.info.synchronization_sales_number.setText("↑" + modules.get("limites_local").get("ec_pedidos").toString() + " ↓" + modules.get("limites_linea").get("ec_pedidos").toString());            
            this.info.synchronization_returns_number.setText("↑" + modules.get("limites_local").get("ec_devolucion").toString() + " ↓" + modules.get("limites_linea").get("ec_devolucion").toString());
            this.info.synchronization_movements_number.setText("↑" + modules.get("limites_local").get("ec_movimiento_almacen").toString() + " ↓" + modules.get("limites_linea").get("ec_movimiento_almacen").toString());
            this.info.synchronization_sales_validation_number.setText("↑" + modules.get("limites_local").get("ec_pedidos_validacion_usuarios").toString() + " ↓" + modules.get("limites_linea").get("ec_pedidos_validacion_usuarios").toString());
            this.info.synchronization_product_provider_number.setText("↑" + modules.get("limites_local").get("ec_movimiento_detalle_proveedor_producto").toString() + " ↓" + modules.get("limites_linea").get("ec_movimiento_detalle_proveedor_producto").toString());
            this.info.synchronization_transfer_number.setText("↑" + modules.get("limites_local").get("ec_transferencias").toString() + " ↓" + modules.get("limites_linea").get("ec_transferencias").toString());/**/
       //}
       return "";//resultado.toString();
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
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/crones/depurar_sincronizacion";
        if( is_complete ){
            urlParaVisitar += "?is_complete=1";
        }
        return executeHttpRequest(urlParaVisitar, "POST");
    }
    public String depurationLogProcess( Boolean is_complete ) throws MalformedURLException, IOException{
        String urlParaVisitar = "http://localhost/" + this.final_local_system_path + "/rest_v2/crones/depurar_logs";
        if( is_complete ){
            urlParaVisitar += "?is_complete=1";
        }
        return executeHttpRequest(urlParaVisitar, "POST");
   }

    /**
     * Ejecuta una peticion sin permitir esperas infinitas y libera siempre la conexion.
     */
    private String executeHttpRequest(String urlParaVisitar, String method) throws IOException {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) new URL(urlParaVisitar).openConnection();
            conexion.setRequestMethod(method);
            conexion.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MS);
            conexion.setReadTimeout(HTTP_READ_TIMEOUT_MS);
            conexion.setUseCaches(false);
            conexion.setRequestProperty("Connection", "close");

            int status = conexion.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP " + status + " al consultar " + urlParaVisitar);
            }

            StringBuilder resultado = new StringBuilder();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream(), "UTF-8"))) {
                String linea;
                while ((linea = rd.readLine()) != null) {
                    resultado.append(linea);
                }
            }
            return resultado.toString();
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
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
