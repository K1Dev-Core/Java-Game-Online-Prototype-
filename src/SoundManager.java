import java.io.File;
import javax.sound.sampled.*;

public class SoundManager {
    private static Clip buttonClip;
    private static Clip backgroundMusicClip;
    private static Clip teleportClip;
    private static boolean soundsEnabled = true;
    private static boolean musicEnabled = true;
    private static float musicVolume = 0.1f;
    
    static {
        try {
            AudioInputStream buttonStream = AudioSystem.getAudioInputStream(new File("assets/sfx/button.wav"));
            buttonClip = AudioSystem.getClip();
            buttonClip.open(buttonStream);
            
            AudioInputStream musicStream = AudioSystem.getAudioInputStream(new File("assets/sfx/music.wav"));
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(musicStream);
            
            AudioInputStream teleportStream = AudioSystem.getAudioInputStream(new File("assets/sfx/teleport.wav"));
            teleportClip = AudioSystem.getClip();
            teleportClip.open(teleportStream);
            
            setBackgroundMusicVolume(musicVolume);
            
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            
            if (musicEnabled) {
                backgroundMusicClip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            soundsEnabled = false;
            musicEnabled = false;
        }
    }
    
    public static void playButtonSound() {
        if (soundsEnabled && buttonClip != null) {
            buttonClip.setFramePosition(0);
            buttonClip.start();
        }
    }
    
    public static void playTeleportSound() {
        if (soundsEnabled && teleportClip != null) {
            teleportClip.setFramePosition(0);
            teleportClip.start();
        }
    }
    
    public static void setSoundsEnabled(boolean enabled) {
        soundsEnabled = enabled;
    }
    
    public static boolean isSoundsEnabled() {
        return soundsEnabled;
    }
    
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (backgroundMusicClip != null) {
            if (enabled) {
                backgroundMusicClip.start();
            } else {
                backgroundMusicClip.stop();
                backgroundMusicClip.setFramePosition(0);
            }
        }
    }
    
    public static boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public static void setBackgroundMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (backgroundMusicClip != null) {
            try {
                if (backgroundMusicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                    float db = (float) (20.0 * Math.log10(musicVolume));
                    gainControl.setValue(db);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static float getBackgroundMusicVolume() {
        return musicVolume;
    }
    
    public static void stopBackgroundMusic() {
        if (backgroundMusicClip != null) {
            backgroundMusicClip.stop();
            backgroundMusicClip.setFramePosition(0);
        }
    }
    
    public static void resumeBackgroundMusic() {
        if (backgroundMusicClip != null && musicEnabled) {
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
}
