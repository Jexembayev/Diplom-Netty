package orhestra.createVmTab.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import orhestra.createVmTab.config.Configuration;
import orhestra.createVmTab.deployer.ImageDeployer;
import orhestra.createVmTab.util.IniLoader;
import orhestra.createVmTab.util.VmInfo;
import yandex.cloud.api.compute.v1.InstanceOuterClass;
import yandex.cloud.api.compute.v1.InstanceServiceGrpc;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass;
import yandex.cloud.api.compute.v1.InstanceOuterClass.Instance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VmCreationService {

    private final AuthService authService;

    public VmCreationService(AuthService authService) {
        this.authService = authService;
    }

    public void createVmsFromIniFile(File iniFile, ObservableList<VmInfo> vmData, TextArea logArea) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> logArea.appendText("📄 Загружаем конфиг: " + iniFile.getName() + "\n"));

                IniLoader loader = new IniLoader(iniFile.getAbsolutePath());
                Configuration config = loader.getConfiguration();

                Platform.runLater(() -> logArea.appendText("✅ Конфиг загружен. Запуск деплоя...\n"));

                String configName = iniFile.getName();
                ImageDeployer deployer = new ImageDeployer(authService, vmData, logArea, configName);
                deployer.deployFromImage(config);

                Platform.runLater(() -> logArea.appendText("🎉 Деплой завершён.\n"));
            } catch (IOException e) {
                Platform.runLater(() -> logArea.appendText("❌ Ошибка чтения ini-файла: " + e.getMessage() + "\n"));
            } catch (Exception e) {
                Platform.runLater(() -> logArea.appendText("❌ Ошибка при создании ВМ: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    public List<VmInfo> listExistingVms(String folderId) {
        List<VmInfo> vms = new ArrayList<>();
        try {
            InstanceServiceGrpc.InstanceServiceBlockingStub stub = authService.getInstanceService();
            InstanceServiceOuterClass.ListInstancesRequest request = InstanceServiceOuterClass.ListInstancesRequest.newBuilder()
                    .setFolderId(folderId)
                    .build();

            for (Instance instance : stub.list(request).getInstancesList()) {
                String imageId = "-";
                long diskGb = 0;

                if (instance.hasBootDisk()) {
                    var bootDisk = instance.getBootDisk();
                    imageId = "-";
                    diskGb = -1;
                }


                VmInfo info = new VmInfo(
                        instance.getName(),
                        instance.getStatus().name(),
                        imageId,
                        instance.getZoneId(),
                        (int) instance.getResources().getCores(),  // ← приведено к int
                        (int) (instance.getResources().getMemory() / (1024 * 1024 * 1024)),
                        (int) diskGb,
                        "Импортировано"
                );



                if (instance.getNetworkInterfacesCount() > 0 &&
                        instance.getNetworkInterfaces(0).hasPrimaryV4Address() &&
                        instance.getNetworkInterfaces(0).getPrimaryV4Address().hasOneToOneNat()) {
                    info.setIp(instance.getNetworkInterfaces(0).getPrimaryV4Address().getOneToOneNat().getAddress());
                } else {
                    info.setIp("—");
                }

                info.setJavaVersion("?");
                info.setServerRunning("?");
                info.setDiskGb((int) diskGb);
                vms.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vms;
    }
}






