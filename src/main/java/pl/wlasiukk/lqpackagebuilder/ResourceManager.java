//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class ResourceManager {
    private static final Logger LOGGER = Logger.getLogger(ResourceManager.class.getName());

    public ResourceManager() {
    }

    static String convertStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int length;
        while((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString("UTF-8");
    }

    public String getResourceAsString(String resource) throws IOException, URISyntaxException {
        String ret = convertStreamToString(this.getResourceAsStream(resource));
        return ret;
    }

    public InputStream getResourceAsStream(String resource) throws IOException, URISyntaxException {
        InputStream in = ResourceManager.class.getResourceAsStream(resource);
        return in;
    }
}
