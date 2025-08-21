
package sincronizacion;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import sincronizacionsistema.carga_inicial;
import sincronizacionsistema.ventanaInicio;
import org.apache.log4j.LogManager;

/**
 *
 * @author oscarmendoza
 */
public class main {
    public static String ruta_config;//ruta de archivo de configuracion donde se guarda ruta de api
    public static int puerto, store_id, syncronization_interval;//puerto de socket para no dejar abrir mas de una vez el sistema
    public static long retardo_inicial;//retardo antes de iniciar la sincroinizacion
    public static String local_system_path, store_name;//nombre de la carpeta de sistema General en local
    private static ServerSocket SERVER_SOCKET;
    final static org.apache.log4j.Logger logger4j = LogManager.getLogger(main.class);//implemenatcion 

    public static void main(String[] args) throws SQLException, IOException, FileNotFoundException, InterruptedException {
        //carga_inicial carga = new carga_inicial();//instancia carga inicial
        File arch = new File("synchronization_config.txt");
        if (!arch.exists()) {
            carga_inicial.crea_config();
        } else {
            carga_inicial.leer_ruta();
        }

        carga_inicial.carga_inicial(ruta_config);//consulta archivo de configuracion
        try{
            SERVER_SOCKET = new ServerSocket(carga_inicial.puerto_sinc);
        }catch(IOException var4){
            logger4j.error(var4.toString());
            JOptionPane.showMessageDialog((Component)null, "El sistema de sincronizacion ya se encuentra en ejecucion");
            System.exit(0);
        }

        new ventanaInicio(ruta_config, retardo_inicial, local_system_path, store_id, syncronization_interval, store_name);//int store_id, int syncronization_interval, String store_name
    }
    
}
