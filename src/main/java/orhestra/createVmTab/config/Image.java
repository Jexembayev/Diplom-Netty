package orhestra.createVmTab.config;

import org.ini4j.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Image {
    private final String vmId;
    private final String imageName;
    private final String imageId;
    private final String vmName;
    private final int vmCount;
    private final int cpu;
    private final int ramGb;
    private final int diskGb;
    private final boolean deleteAll;
    private final String subnetId;
    private final String platformId;
    private final String userName;
    private final String sshKeyPublic;

    public Image(Profile.Section section) {
        this.vmId = section.get("vmId");
        this.imageName = section.get("imageName");
        this.imageId = section.get("image_id");
        this.vmName = section.get("vm_name");
        this.subnetId = section.get("subnet_id");
        this.platformId = section.get("platform_id");
        this.userName = section.get("user_name");

        // Загрузка публичного ключа из файла
        String keyPath = section.get("ssh_key_public_path");
        this.sshKeyPublic = readPublicKey(keyPath);

        String countStr = section.get("vmCount");
        this.vmCount = (countStr != null) ? Integer.parseInt(countStr) : 1;

        String cpuStr = section.get("cpu");
        this.cpu = (cpuStr != null) ? Integer.parseInt(cpuStr) : 2;

        String ramStr = section.get("ram_gb");
        this.ramGb = (ramStr != null) ? Integer.parseInt(ramStr) : 2;

        String diskStr = section.get("disk_gb");
        this.diskGb = (diskStr != null) ? Integer.parseInt(diskStr) : 20;

        String deleteAllStr = section.get("deleteAll");
        this.deleteAll = deleteAllStr != null && deleteAllStr.equalsIgnoreCase("true");
    }

    private String readPublicKey(String path) {
        if (path == null) return "";
        try {
            return Files.readString(Path.of(path)).trim();
        } catch (IOException e) {
            System.err.println("[ERROR] Не удалось прочитать SSH ключ: " + path);
            return "";
        }
    }

    public String getVmId() { return vmId; }
    public String getImageName() { return imageName; }
    public String getImageId() { return imageId; }
    public String getVmName() { return vmName; }
    public int getVmCount() { return vmCount; }
    public boolean isDeleteAll() { return deleteAll; }
    public String getSubnetId() { return subnetId; }
    public String getUserName() { return userName; }
    public String getSshKeyPublic() { return sshKeyPublic; }
    public String getPlatformId() { return platformId; }
    public int getCpu() { return cpu; }
    public int getRamGb() { return ramGb; }
    public int getDiskGb() { return diskGb; }
}

