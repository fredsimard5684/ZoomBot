package src;

import net.twasi.obsremotejava.callbacks.Callback;
import net.twasi.obsremotejava.OBSRemoteController;
import net.twasi.obsremotejava.requests.GetVersion.GetVersionResponse;
import net.twasi.obsremotejava.requests.ResponseBase;


public class OBSRemote {
    private final String obsAddress = "ws://localhost:4444";
    private final String obsPassword = null;

    public void test() {
        final OBSRemoteController controller = new OBSRemoteController(obsAddress, false, obsPassword);

        controller.registerDisconnectCallback(new Callback() {
            @Override
            public void run(ResponseBase response) {
                System.out.println("Disconnected");
            }
        });
        controller.registerConnectCallback(new Callback() {
            @Override
            public void run(ResponseBase response) {
                GetVersionResponse version = (GetVersionResponse) response;
                System.out.println("Connected!");
                System.out.println(version.getObsStudioVersion());
            }
        });
            controller.startStreaming(new Callback() {
                    @Override
                    public void run(ResponseBase response) {
                        System.out.println("Streaming started: " + response.getStatus());
                    }
                });
    }
}
