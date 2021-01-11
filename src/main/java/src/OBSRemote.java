package src;

import net.twasi.obsremotejava.OBSRemoteController;


public class OBSRemote {
    private final OBSRemoteController controller;

    public OBSRemote() {
        controller = new OBSRemoteController("ws://localhost:4444", false, null);
        if (controller.isFailed()) { // Awaits response from OBS
            System.out.println("[ERROR] : Cannot connect to OBS");
        }
    }

    public void runStream() {
        controller.registerConnectCallback(response -> {
            System.out.println("[INFO]: Connected!");
            System.out.println(response.getObsStudioVersion());
        });

        controller.startRecording(response -> System.out.println("[INFO]: Recording started: " + response.getStatus()));
    }

    public void stopRecording() {
        controller.stopRecording(response -> System.out.println("[INFO]: Recording stopped: " + response.getStatus()));
        controller.disconnect();
        System.out.println("[INFO]: Disconnected");
    }
}
