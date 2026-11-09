package javax.microedition.media.control;

import javax.microedition.media.Control;
import javax.microedition.media.MediaException;
import java.io.IOException;
import java.io.OutputStream;

public interface RecordControl extends Control {
    void setRecordStream(OutputStream stream);
    void setRecordLocation(String locator) throws IOException, MediaException;
    void startRecord();
    void stopRecord();
    void commit() throws IOException;
    void reset() throws IOException;
    void setRecordSizeLimit(int size) throws MediaException;
    int getRecordSizeLimit();
}
