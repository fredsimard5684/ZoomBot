package src;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

public class Main {

    private static String[] getInfo() {
        JSONParser jsonParser = new JSONParser();
        String info[] = new String[6];
        try {
            URL url = Main.class.getClassLoader().getResource("config.json");
            InputStream inputStream = url.openStream();
            Reader reader = new InputStreamReader(inputStream);
            Object obj = jsonParser.parse(reader);
            reader.close();

            String email = ((JSONObject) obj).get("mail").toString();
            String pass = ((JSONObject) obj).get("pass").toString();
            String emailHost = ((JSONObject) obj).get("emailHost").toString();
            String port = ((JSONObject) obj).get("port").toString();
            String pathOBS = ((JSONObject) obj).get("pathOBS").toString();
            String pathRecording = OSValidator.isWindows() ? ((JSONObject) obj).get("pathRecordingWin").toString() : ((JSONObject) obj).get("pathRecordingLin").toString();

            info[0] = email;
            info[1] = pass;
            info[2] = emailHost;
            info[3] = port;
            info[4] = pathOBS;
            info[5] = pathRecording;

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return info;
    }

    private static void openingApplications(String messageURL, String pathOBS) throws URISyntaxException, IOException {
        Runtime rt = Runtime.getRuntime();
        if (OSValidator.isLinux()) rt.exec("firefox " + messageURL);
        else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(messageURL));
        }

        //Open up a cmd command
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String command = OSValidator.isWindows() ? "cmd /c start cmd.exe /K \"cd /d " + pathOBS + " && start obs64.exe && exit" : "obs";
        rt.exec(command);
    }

    private static String gatheringCorrectLink(String[] enseignant, String[] config) {
        final String username = config[0];
        final String pass = config[1];
        final String host = config[2];
        final String port = config[3];
        String messageURL = "";

        //Login to imap protocol
        FetchMail fetchMail = new FetchMail(host, username, pass, port);
        messageURL = fetchMail.fetch(messageURL, enseignant);

        System.out.println(messageURL);


        //Remove the &amp; after pwd
        if (messageURL.contains("amp;")) {
            messageURL = messageURL.replace("amp;", "");
        }
        System.out.println(messageURL);
        return messageURL;
    }

    public static void main(String[] args) throws URISyntaxException, IOException, ParseException {
        final String[] config = getInfo();
        final String[] enseignant = FetchMail.teacherInfo();
        final String messageURL = gatheringCorrectLink(enseignant, config);

        //Config[4] = pathOBS
        openingApplications(messageURL, config[4]);

        //Commands that close up the correct apps after a certain time
        ExecuteTask executeTask = new ExecuteTask();

        executeTask.executeOBSTask();
        executeTask.closeAllProcess(enseignant, config[5]); //config[5] = path recording
    }
}
