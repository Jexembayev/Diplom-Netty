package orhestra.createVmTab.deployer;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import orhestra.createVmTab.config.Configuration;
import orhestra.createVmTab.config.General;
import orhestra.createVmTab.config.Image;
import orhestra.createVmTab.creator.InstanceRequestBuilder;
import orhestra.createVmTab.service.AuthService;
import orhestra.createVmTab.util.VmInfo;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass;
import yandex.cloud.api.operation.OperationOuterClass;
import yandex.cloud.sdk.utils.OperationUtils;

import java.time.Duration;

public class ImageDeployer {

    private final AuthService authService;
    private final ObservableList<VmInfo> vmData;
    private final TextArea logArea;
    private final String configName;

    public ImageDeployer(AuthService authService, ObservableList<VmInfo> vmData, TextArea logArea, String configName) {
        this.authService = authService;
        this.vmData = vmData;
        this.logArea = logArea;
        this.configName = configName;
    }

    public void deployFromImage(Configuration config) {
        General general = config.getGeneralConf();
        Image image = config.getImageConf();

        if (general == null || image == null) {
            log("[ERROR] Конфигурация general или image не задана.");
            return;
        }

        String sshKey = image.getSshKeyPublic();
        if (sshKey.isEmpty()) {
            log("[ERROR] SSH ключ не загружен. Проверьте путь: " + image.getSshKeyPublic());
            return;
        }

        log("[INFO] SSH ключ загружен: " + sshKey.substring(0, Math.min(60, sshKey.length())) + "...");

        for (int i = 0; i < general.getVmCount(); i++) {
            var request = InstanceRequestBuilder.buildFromParams(
                    image.getVmName(),
                    general.getFolderId(),
                    general.getZoneId(),
                    image.getPlatformId(),
                    image.getCpu(),
                    image.getRamGb(),
                    image.getDiskGb(),
                    image.getImageId(),
                    image.getSubnetId(),
                    true,
                    image.getUserName(),
                    sshKey
            );

            String instanceName = request.getName();

            VmInfo info = new VmInfo(
                    instanceName,
                    "Создаётся...",
                    image.getImageId(),
                    general.getZoneId(),
                    image.getCpu(),
                    image.getRamGb(),
                    image.getDiskGb(),
                    "Конфиг: " + configName
            );

            Platform.runLater(() -> vmData.add(info));
            log("[INFO] ▶ Создание ВМ: " + instanceName);

            try {
                log("[DEBUG] Добавляем метаданные:");
                log("[DEBUG] userName = " + image.getUserName());
                log("[DEBUG] sshKey = " + sshKey.substring(0, 60) + "...");
                log("[DEBUG] ssh-keys = " + image.getUserName() + ":" + sshKey.substring(0, 60) + "...");

                log("[INFO] ⏳ Отправляем запрос создания ВМ...");

                OperationOuterClass.Operation operation = authService.getInstanceService().create(request);
                OperationUtils.wait(authService.getOperationService(), operation, Duration.ofMinutes(5));

                String vmId = operation.getMetadata()
                        .unpack(InstanceServiceOuterClass.CreateInstanceMetadata.class)
                        .getInstanceId();

                log("[INFO] ✅ ВМ создана: " + instanceName + " (ID: " + vmId + ")");

                try {
                    var instance = authService.getInstanceService()
                            .get(InstanceServiceOuterClass.GetInstanceRequest.newBuilder()
                                    .setInstanceId(vmId)
                                    .build());

                    String publicIp = instance.getNetworkInterfaces(0)
                            .getPrimaryV4Address()
                            .getOneToOneNat()
                            .getAddress();

                    log("[INFO] 🌐 IP для " + instanceName + ": " + publicIp);

                    Platform.runLater(() -> {
                        info.setStatus("✅ Успешно");
                        info.setIp(publicIp);
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        info.setStatus("⚠️ Без IP");
                        info.setIp("Ошибка IP");
                    });
                    log("[WARN] Не удалось получить IP: " + e.getMessage());
                }

            } catch (Exception e) {
                Platform.runLater(() -> info.setStatus("❌ Ошибка"));
                log("[ERROR] ❌ Ошибка при создании ВМ " + instanceName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void log(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }
}






