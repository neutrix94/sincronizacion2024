
package sincronizacionsistema;

/**
 *
 * @author oscarmendoza
 */
public class ApisClient {
    private String base_path;
    public ApisClient(String Base_Path){
        this.base_path = Base_Path;
    }
    
    public String getSynchronizationConfig(){
        
        return "ok";
    }
    
    
    
}
