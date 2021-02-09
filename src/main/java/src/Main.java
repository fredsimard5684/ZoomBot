package src;

import org.json.simple.parser.*;
import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException, ParseException {
        AtomicBoolean hasAlreadyStarted = new AtomicBoolean(false);
        Runnable runnable = () -> {
            try {
                LoginInformation config = new LoginInformation();
                Teachers teachers = new Teachers();

                if (teachers.getName().equals("")) {
                    System.out.println("[INFO] " + LocalTime.now() + ": No course right now can be found");
                    hasAlreadyStarted.set(false);
                }
                else {
                    if (!hasAlreadyStarted.get()) {
                        System.out.println("[INFO] " + LocalTime.now() + ": The following teacher has been found -> " + teachers.getName());
                        hasAlreadyStarted.set(true);

                        MailFetcher mailFetcher = new MailFetcher(config, teachers);
                        final String messageURL = mailFetcher.gatheringCorrectLink();

                        TaskManager taskManager = new TaskManager(true);

                        taskManager.openingApplications(messageURL, config.getPathOBS());
                        taskManager.createOBSRemote();
                        taskManager.executeOBSTask();
                        taskManager.closeAllProcess(teachers, config.getPathRecording());
                    }
                    else {
                        hasAlreadyStarted.set(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.MINUTES);


    }
}
