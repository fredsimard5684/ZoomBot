package src;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

public class LoginInformation {
    private String email;
    private String pass;
    private String emailHost;
    private String port;
    private String pathOBS;
    private String pathRecording;

    public LoginInformation() throws IOException, ParseException {
        configJson();
    }

    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public String getPort() {
        return port;
    }

    public String getPathOBS() {
        return pathOBS;
    }

    public String getPathRecording() {
        return pathRecording;
    }

    private void configJson() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        URL url = Main.class.getClassLoader().getResource("config.json");
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream);
        Object obj = jsonParser.parse(reader);
        reader.close();

        email = ((JSONObject) obj).get("mail").toString();
        pass = ((JSONObject) obj).get("pass").toString();
        emailHost = ((JSONObject) obj).get("emailHost").toString();
        port = ((JSONObject) obj).get("port").toString();
        pathOBS = ((JSONObject) obj).get("pathOBS").toString();
        pathRecording = OSValidator.isWindows() ? ((JSONObject) obj).get("pathRecordingWin").toString() : ((JSONObject) obj).get("pathRecordingLin").toString();
    }
}
