package orhestra.createVmTab.manager;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import orhestra.createVmTab.service.AuthService;
import yandex.cloud.api.compute.v1.ImageServiceOuterClass;
import yandex.cloud.api.compute.v1.InstanceOuterClass;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.GetInstanceRequest;

public class CloudVmManager {

    private final AuthService authService;
    private final VBox ipListBox;
    private final TextArea logArea;

    public CloudVmManager(AuthService authService, VBox ipListBox, TextArea logArea) {
        this.authService = authService;
        this.ipListBox = ipListBox;
        this.logArea = logArea;
    }

    private GetInstanceRequest getRequest(String id) {
        return GetInstanceRequest.newBuilder()
                .setInstanceId(id)
                .build();
    }

    private InstanceOuterClass.Instance getInstance(String id) {
        return authService.getInstanceService().get(getRequest(id));
    }

    public String getPublicIp(String id) {
        return getInstance(id).getNetworkInterfaces(0).getPrimaryV4Address().getOneToOneNat().getAddress();
    }

    public String getPrivateIp(String id) {
        return getInstance(id).getNetworkInterfaces(0).getPrimaryV4Address().getAddress();
    }

    public void monitorVmStatus(String id) {
        new Thread(() -> {
            String status = "";
            while (!status.equals("RUNNING")) {
                try {
                    InstanceOuterClass.Instance instance = getInstance(id);
                    status = instance.getStatus().toString();
                    String name = instance.getName();
                    String publicIp = getPublicIp(id);

                    String log = String.format("[INFO] ВМ: %s | Статус: %s | IP: %s\n", name, status, publicIp);
                    String finalStatus = status;
                    Platform.runLater(() -> {
                        logArea.appendText(log);
                        if (finalStatus.equals("RUNNING")) {
                            ipListBox.getChildren().add(new Label(name + ": " + publicIp));
                        }
                    });

                    Thread.sleep(3000);
                } catch (Exception e) {
                    String error = "[ERROR] Ошибка при мониторинге: " + e.getMessage() + "\n";
                    Platform.runLater(() -> logArea.appendText(error));
                    break;
                }
            }
        }).start();
    }

    public static ImageServiceOuterClass.GetImageLatestByFamilyRequest buildGetLatestByFamilyRequest(String folderId, String family) {
        return ImageServiceOuterClass.GetImageLatestByFamilyRequest.newBuilder()
                .setFolderId(folderId)
                .setFamily(family)
                .build();
    }
}
