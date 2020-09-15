package src;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class Main {

    private static String[] getMailInfo() {
        JSONParser jsonParser = new JSONParser();
        String info[] = new String[2];
        try {
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.json");
            Reader reader = new InputStreamReader(inputStream);
            Object obj = jsonParser.parse(reader);
            reader.close();

            JSONObject jsonObject = (JSONObject) obj;

            String email = ((JSONObject) obj).get("mail").toString();
            String pass = ((JSONObject) obj).get("pass").toString();

            info[0] = email;
            info[1] = pass;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;
    }

    private static void openingApplications(String messageURL) throws URISyntaxException, IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(messageURL));
        }

        //Open up a cmd command
        Runtime rt = Runtime.getRuntime();
        rt.exec("cmd /c start cmd.exe /K \"cd /d C:\\Program Files\\obs-studio\\bin\\64bit && start obs64.exe && exit");
    }

    private static String gatheringCorrectLink(int day) {
        final String[] emailCredentials = getMailInfo();
        final String host = "outlook.office365.com";
        final String username = emailCredentials[0];
        final String pass = emailCredentials[1];
        String messageURL = "";

        //Login to imap protocol
        FetchMail fetchMail = new FetchMail(host, username, pass);
        messageURL = fetchMail.fetch(messageURL, day);

        System.out.println(messageURL);


        //Remove the &amp; after pwd
        if (messageURL.contains("amp;")) {
            messageURL = messageURL.replace("amp;", "");
        }
        System.out.println(messageURL);
        return messageURL;
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.getTime().toString());

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        final String messageURL = gatheringCorrectLink(day);

        openingApplications(messageURL);

        //Commands that close up the correct apps after a certain time
        ExecuteTask executeTask = new ExecuteTask();

        executeTask.executeOBSTask();
        executeTask.closeAllProcess(day);
    }
}
