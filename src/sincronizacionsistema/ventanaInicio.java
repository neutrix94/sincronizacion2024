
package sincronizacionsistema;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import org.apache.log4j.LogManager;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;
import org.apache.log4j.LogManager;

public class ventanaInicio extends JFrame {
    conexion_doble conecta;
    procesos proc;
    //private static Connection conn_local;
    //private static Connection conn_linea;
    private int id_sucursal;
    private int tiempo_sincronizacion;
    private long retardo_inicio;
    private String local_system_path;
    public JTextField intervalo_busqueda;
    private JButton jButton1;
    private JLabel jLabel1;
    private JLabel jLabel3;
    private JScrollPane jScrollPane1;
    public JTextArea status_sinc;
    final static org.apache.log4j.Logger logger4j = LogManager.getLogger(ventanaInicio.class);//implementacion de logger4j 2024-10-23

    public ventanaInicio(String ruta_conect, long retardo, String system_path, int store_id, int syncronization_interval, String store_name) throws SQLException, IOException, InterruptedException {
        this.initComponents();
        this.retardo_inicio = retardo;
        this.setVisible(true);
        this.setLocationRelativeTo((Component)null);
        long start = System.currentTimeMillis();
        Thread.sleep(this.retardo_inicio);
//System.out.println("Sleep time in ms = " + (System.currentTimeMillis() - start));
        //this.conecta = new conexion_doble(ruta_conect);
        this.proc = new procesos(ruta_conect, system_path);
//        String[] datos_sesion = this.proc.getDatosSucursal().split("~");
        this.id_sucursal = store_id;
        if (this.id_sucursal == -1) {
           JOptionPane.showMessageDialog((Component)null, "Esta sucursal no se puede sincronizar por que la BD es del sistema en línea");
           System.exit(0);
        }
//id_sucursal,nombre,(intervalo_sinc*1000)
        this.intervalo_busqueda.setText("" + syncronization_interval);
        this.intervalo_busqueda.setEnabled(false);
        this.proc.id_sucursal = Integer.parseInt("" + this.id_sucursal);//datos_sesion[0]
        //this.proc.tiempo_buscar = Integer.parseInt(datos_sesion[2]);
        //this.proc.tiempo_buscar = Integer.parseInt(datos_sesion[2]);
        
System.out.println("Intervalo : " + syncronization_interval);
        this.proc.tiempo_buscar = (int) (syncronization_interval*1000);//Integer.parseInt(syncronization_interval);
        //conn_local = this.conecta.conecta_local();
        this.oculta();
        //this.setTitle("Sincronización de Sistema General Sucursal " + datos_sesion[1]);
        this.setTitle("Sincronización de Sistema General Sucursal " + store_name);
        this.setLocationRelativeTo((Component)null);

        try {
           this.status_sinc.setText(this.proc.ejecutaIntervalo(retardo));
        } catch (Exception var9) {
          logger4j.info( var9.toString() );//aqui no se controlaba anteriormente el error
         // this.dispose();//actualmente revisar
        }
    }

   public void oculta() {
      this.dispose();
      this.jButton1.setVisible(false);
      System.out.println("Ya ocultó");
   }

   private void initComponents() {
      this.jButton1 = new JButton();
      this.intervalo_busqueda = new JTextField();
      this.jLabel1 = new JLabel();
      this.jScrollPane1 = new JScrollPane();
      this.status_sinc = new JTextArea();
      this.jLabel3 = new JLabel();
      this.jButton1.setText("Iniciar");
      this.jButton1.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent evt) {
            ventanaInicio.this.jButton1MouseClicked(evt);
         }
      });
      this.jButton1.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            ventanaInicio.this.jButton1ActionPerformed(evt);
         }
      });
      this.intervalo_busqueda.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            ventanaInicio.this.intervalo_busquedaActionPerformed(evt);
         }
      });
      this.setDefaultCloseOperation(0);
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent evt) {
            ventanaInicio.this.formWindowClosing(evt);
         }

         public void windowClosed(WindowEvent evt) {
            ventanaInicio.this.formWindowClosed(evt);
         }
      });
      this.getContentPane().setLayout(new AbsoluteLayout());
      this.jLabel1.setFont(new Font("Times New Roman", 0, 20));
      this.jLabel1.setForeground(new Color(153, 153, 0));
      this.jLabel1.setText("Preparando sistema de Sincronización...");
      this.getContentPane().add(this.jLabel1, new AbsoluteConstraints(90, 30, 330, -1));
      this.status_sinc.setColumns(20);
      this.status_sinc.setRows(5);
      this.jScrollPane1.setViewportView(this.status_sinc);
      this.getContentPane().add(this.jScrollPane1, new AbsoluteConstraints(0, 400, 0, 10));
      //this.jLabel3.setIcon(new ImageIcon(this.getClass().getResource("/img/log2.gif")));
      //this.jLabel3.setText("jLabel3");
      this.getContentPane().add(this.jLabel3, new AbsoluteConstraints(0, 0, 500, 470));
      this.pack();
   }

   private void formWindowClosed(WindowEvent evt) {
   }

   private void formWindowClosing(WindowEvent evt) {
   }

   private void jButton1ActionPerformed(ActionEvent evt) {
   }

   private void jButton1MouseClicked(MouseEvent evt) {
      try {
         this.status_sinc.setText(this.proc.ejecutaIntervalo(this.retardo_inicio));
      } catch (InterruptedException var3) {
         Logger.getLogger(ventanaInicio.class.getName()).log(Level.SEVERE, (String)null, var3);
      } catch (Exception var4) {
         Logger.getLogger(ventanaInicio.class.getName()).log(Level.SEVERE, (String)null, var4);
      }

      this.dispose();
      this.jButton1.setVisible(false);
   }

   private void intervalo_busquedaActionPerformed(ActionEvent evt) {
   }

   public static void main(String[] args) {
      try {
         LookAndFeelInfo[] var1 = UIManager.getInstalledLookAndFeels();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            LookAndFeelInfo info = var1[var3];
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (ClassNotFoundException var5) {
         Logger.getLogger(ventanaInicio.class.getName()).log(Level.SEVERE, (String)null, var5);
      } catch (InstantiationException var6) {
         Logger.getLogger(ventanaInicio.class.getName()).log(Level.SEVERE, (String)null, var6);
      } catch (IllegalAccessException var7) {
         Logger.getLogger(ventanaInicio.class.getName()).log(Level.SEVERE, (String)null, var7);
      } catch (UnsupportedLookAndFeelException var8) {
         Logger.getLogger(ventanaInicio.class.getName()).log(Level.SEVERE, (String)null, var8);
      }

      EventQueue.invokeLater(new Runnable() {
         public void run() {
         }
      });
   }
}
