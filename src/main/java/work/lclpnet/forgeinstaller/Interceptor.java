
package work.lclpnet.forgeinstaller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Interceptor extends PrintStream{

    protected ProgressCallbackClient pgClient;

    public Interceptor(OutputStream out, ProgressCallbackClient pgClient) {
        super(out, true);
        this.pgClient = pgClient;
    }

    @Override
    public void print(String s) {
        super.print(s);
        if(pgClient != null) pgClient.send(s);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        if(pgClient != null) pgClient.send(new String(b));
    }

}