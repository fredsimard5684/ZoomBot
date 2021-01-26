package src;

import org.json.simple.parser.*;
import java.io.*;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException, ParseException {

        LoginInformation config = new LoginInformation();
        Teachers teachers = new Teachers();


        MailFetcher mailFetcher = new MailFetcher(config, teachers);
        final String messageURL = mailFetcher.gatheringCorrectLink();

        //Commands that close up the correct apps after a certain time
        TaskManager taskManager = new TaskManager();

        taskManager.openingApplications(messageURL, config.getPathOBS());
        taskManager.createOBSRemote();
        taskManager.executeOBSTask();
        taskManager.closeAllProcess(teachers, config.getPathRecording());
    }
}
