package src;

import net.twasi.obsremotejava.OBSRemoteController;


public class OBSRemote {
    private final String obsAddress = "ws://localhost:4444";
    private final String obsPassword = null;

    public void runStream() {
        final OBSRemoteController controller = new OBSRemoteController(obsAddress, false, obsPassword);
        if (controller.isFailed()) { // Awaits response from OBS
            System.out.println("[ERROR] : Cannot connect to OBS");
        }

        controller.registerDisconnectCallback(() -> System.out.println("Disconnected"));

        controller.registerConnectCallback(response -> {
            System.out.println("[INFO]: Connected!");
            System.out.println(response.getObsStudioVersion());
        });
        controller.startStreaming(response -> System.out.println("Streaming started: " + response.getStatus()));
    }
}
