package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ExecuteTask {
    final OBSRemote obsRemote;

    public ExecuteTask() {
        obsRemote = new OBSRemote();
    }

    public void executeOBSTask() {
        //Delay the connection to obs
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        obsRemote.runStream();
                    }
                }, 5000
        );
    }

    public void closeAllProcess(String[] enseignant, String pathRecording) {
        final long TIMEINMS = 11100 * 1000;
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Runtime rt = Runtime.getRuntime();
                        String taskList = "";
                        try {
                            if (OSValidator.isWindows()) {

                                rt.exec("taskkill /F /IM obs64.exe");
                                rt.exec("taskkill /F /IM Zoom.exe");

                                //Get the list of task that is currently running
                                taskList = chekTaskList();

                                //Make sure that obs is not running anymore
                                while (taskList.contains("obs64.exe")) {
                                    rt.exec("taskkill /F /IM obs64.exe");
                                    taskList = chekTaskList();
                                }

                            } else {
                                rt.exec("killall -9 obs");
                                rt.exec("killall -9 zoom");
                                taskList = chekTaskList();
                                while (taskList.contains("obs")) {
                                    rt.exec("killall -9 obs");
                                    taskList = chekTaskList();
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        moveFileToCorrectLocation(enseignant, pathRecording);
                    }
                }, TIMEINMS
        );
    }

    private String chekTaskList() {
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Process process = OSValidator.isWindows() ? Runtime.getRuntime().exec("tasklist") : Runtime.getRuntime().exec("top | grep zoom");
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

    private void moveFileToCorrectLocation(String[] enseignant, String pathRecording) {
        //The timer is to make sure that the .mkv file has been save correctly before moving it arround
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Runtime rt = Runtime.getRuntime();
                        try {
                            String command = "";
                            if (OSValidator.isWindows()) {
                                command = "cmd /c start cmd.exe /K \"cd /d " + pathRecording
                                        + " && move *.mkv ./" + enseignant[2] + " && exit";
                                rt.exec(command);
                            } else {
                                String[] shCommand = {"/bin/sh", "-c", "mv " + pathRecording + "/*.mkv " + pathRecording + "/" + enseignant[2]};

                                Process prcs = null;
                                try {
                                    prcs = rt.exec(shCommand);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //This is the last thing that will get executed so we can shutdown the program
                        System.exit(0);
                    }
                }, 30000
        );
    }
}
