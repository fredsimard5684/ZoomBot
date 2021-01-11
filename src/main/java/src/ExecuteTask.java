package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ExecuteTask {

    public void executeOBSTask() {
        //Delay the connection to obs
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        OBSRemote obsRemote = new OBSRemote();
                        obsRemote.runStream();
                    }
                }, 5000
        );
    }

    public void closeAllProcess(String[] enseignant) {
        final long TIMEINMS = 11100 * 1000;
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Runtime rt = Runtime.getRuntime();
                        try {
                            rt.exec("taskkill /F /IM obs64.exe");
                            rt.exec("taskkill /F /IM Zoom.exe");

                            //Get the list of task that is currently running
                            String taskList = chekTaskList();

                            //Make sure that obs is not running anymore
                            while (taskList.contains("obs64.exe")) {
                                rt.exec("taskkill /F /IM obs64.exe");
                                taskList = chekTaskList();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        moveFileToCorrectLocation(enseignant);
                    }
                }, TIMEINMS
        );
    }

    private String chekTaskList() {
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("tasklist");
            process.getOutputStream().close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private void moveFileToCorrectLocation(String[] enseignant) {
        //The timer is to make sure that the .mkv file has been save correctly before moving it arround
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Runtime rt = Runtime.getRuntime();

                        try {
                            rt.exec("cmd /c start cmd.exe /K \"cd /d C:\\Users\\Fred\\Documents\\EnregistrementCoursUQTR"
                                    + " && move *.mkv ./" + enseignant[2] + " && exit");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //This is the last thing that will get executed so we can shutdown the program
                        System.exit(0);
                    }
                } , 30000
        );
    }
}
