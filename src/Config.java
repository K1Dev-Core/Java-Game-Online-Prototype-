public class Config {
    // ขนาดหน้าจอ
    public static final int WINDOW_WIDTH = 1300;    // ความกว้างหน้าจอ
    public static final int WINDOW_HEIGHT = 820;    // ความสูงหน้าจอ
    public static final int MAP_WIDTH = 1200;       // ความกว้างแมพ
    public static final int MAP_HEIGHT = 700;       // ความสูงแมพ
    public static final int FPS = 60;               
    
  
    public static final double PLAYER_SPEED = 1.5;          
    public static final int PLAYER_SIZE = 96;               // ขนาดตัวละคร
    public static final double DASH_DISTANCE = 90.0;        // ระยะแดช
    public static final long DASH_COOLDOWN = 7000;          // คูลดาวน์แดช
    public static final long RESPAWN_TIME = 10000;          // เวลาตายก่อน

    public static final int MAX_HEALTH = 150;      
    public static final int ATTACK_DAMAGE = 3;      // ดาเมจ
    public static final double KNOCKBACK_FORCE = 15.0;  
    public static final long ATTACK_COOLDOWN = 1000;   // คูลดาวน์โจมตี
    public static final int ATTACK_RANGE = 90;         // ระยะโจมตี
    
    // แถบคูลดาวน์
    public static final int COOLDOWN_BAR_WIDTH = 200;   
    public static final int COOLDOWN_BAR_HEIGHT = 15;   
    public static final int COOLDOWN_BAR_X = 540;       
    public static final int COOLDOWN_BAR_Y = 680;       
    

    public static final int SERVER_PORT = 7777;             
    public static final String SERVER_IP = "localhost";      
    public static final int NETWORK_UPDATE_RATE = 22;        
    

    public static final double BOB_SPEED = 0.3;                 
    public static final double BOB_AMPLITUDE = 3.0;            
    public static final double MOVEMENT_THRESHOLD = 3.0;        
    public static final int PARTICLE_DURATION = 400;           
    public static final double PARTICLE_SPAWN_DISTANCE = 60;   
    public static final int PARTICLE_ANIMATION_DELAY = 80;     
    public static final int WALK_ANIMATION_DELAY = 200;        
    public static final int ATTACK_ANIMATION_DELAY = 30;      

}