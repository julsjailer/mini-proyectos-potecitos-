
package javax.microedition.media.control;

import javax.microedition.media.Control;

public interface VolumeControl extends Control {

    int setLevel(int level);

    int getLevel();

    boolean isMuted();

    void setMute(boolean mute);
}
