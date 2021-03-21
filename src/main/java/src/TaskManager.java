package src;

import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class TaskManager {
    private OBSRemote obsRemote;
    private boolean featureEnable;

    public TaskManager (boolean featureEnable) {
        this.featureEnable = featureEnable;
    }

    public void openingApplications(String messageURL, String pathOBS) throws URISyntaxException, IOException {
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

    public void createOBSRemote() {
        obsRemote = new OBSRemote();
    }

    public void executeOBSTask() {
        //Delay the connection to obs to make sure that the program is open
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        obsRemote.runStream();
                    }
                }, 5000
        );
    }

    public void closeAllProcess(Teachers teachers, String pathRecording) {
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
                        moveFileToCorrectLocation(teachers, pathRecording);
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

    private void moveFileToCorrectLocation(Teachers teachers, String pathRecording) {
        //The timer is to make sure that the .mkv file has been save correctly before moving it arround
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Runtime rt = Runtime.getRuntime();
                        try {
                            String command = "";
                            //enseigant[2] = folder
                            if (OSValidator.isWindows()) {
                                command = "cmd /c start cmd.exe /K \"cd /d " + pathRecording
                                        + " && move *.mkv ./" + teachers.getFolder() + " && exit";
                                rt.exec(command);
                            } else {
                                String[] shCommand = {"/bin/sh", "-c", "mv " + pathRecording + "/*.mp4 " + pathRecording + "/" + teachers.getFolder()};

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
                        try {
                            //This allow the folder to get updated correctly before doing the operation
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (featureEnable)
                            moveToGoogleDrive(pathRecording, teachers.getFolder(), teachers.getDriveFolderID());
                    }
                }, 30000
        );
    }

    private void moveToGoogleDrive(String pathRecording, String folder, String driveFolder) {
        GoogleDriveUploader googleDriveUploader = new GoogleDriveUploader();
        File file = googleDriveUploader.getFile(pathRecording, folder);
        try {
            googleDriveUploader.upload(file, driveFolder);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
