package nl.knaw.dans.lib.ocflext;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CloseNotifyingInputStream extends FilterInputStream {
    private Runnable closeCallback;

    CloseNotifyingInputStream(InputStream in, Runnable closeCallback) {
        super(in);
        this.closeCallback = closeCallback;
    }

    @Override
    public void close() throws IOException {
       super.close();
       closeCallback.run();
    }

}
