package orhestra.createVmTab.config;


import org.ini4j.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VM {
    private String prefix;
    private String platformId;
    private int core;
    // в гигабайтах (2, 4, ..)
    private int memory;
    // в гигабайтах
    private int diskSize;
    private String sshKeyPublicPath;
    private String sshKeyPrivatePath;
    private String imageStandard;
    private String imageId;
    private String imageFamily;
    private String subnetId;
    private String userName;
    private String commandsFilePath;
    private Boolean assignPublicIp;

    public VM(Profile.Section section) {
        setPrefix(section.get("prefix"));
        setPlatformId(section.get("platform_id"));
        setCore(Integer.parseInt(section.get("core")));
        setMemory(Integer.parseInt(section.get("memory")));
        setDiskSize(Integer.parseInt(section.get("disk_size")));
        setSubnetId(section.get("subnet_id"));
        setImageStandard(section.get("image_standard"));
        setImageId(section.get("image_id"));
        setImageFamily(section.get("image_family"));
        setUserName(section.get("user_name"));
        setSshKeyPublicPath(section.get("path_public_ssh"));
        setSshKeyPrivatePath(section.get("path_private_ssh"));
        setCommandsFilePath(section.get("commands_file_path"));
        setAssignPublicIp(true);
    }

    public String getPrefix() {
        return prefix;
    }

    public int getDiskSize() {
        return diskSize;
    }

    protected void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPlatformId() {
        return platformId;
    }

    protected void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public int getCore() {
        return core;
    }

    protected void setCore(int core) {
        this.core = core;
    }

    public int getMemory() {
        return memory;
    }

    protected void setMemory(int memory) {
        this.memory = memory;
    }

    protected void setDiskSize(int diskSize) {
        this.diskSize = diskSize;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getImageStandard() {
        return imageStandard;
    }

    private void setImageStandard(String imageStandard) {
        this.imageStandard = imageStandard;
    }

    public String getUserName() {
        return userName;
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSshPath() {
        return this.sshKeyPublicPath;
    }

    private void setSshKeyPublicPath(String sshKeyPublicPath) {
        this.sshKeyPublicPath = sshKeyPublicPath;
    }

    public String getSshPublicKey() {
        Path path = Paths.get(getSshPath());
        try {
            return Files.readString(path);
        } catch (IOException e) {
            System.err.println("[ERROR] Error reading public SSH key: " + e.getMessage());
        }
        return null;
    }

    public String getImageFamily() {
        return imageFamily;
    }

    private void setImageFamily(String imageFamily) {
        this.imageFamily = imageFamily;
    }

    public String getCommandsFilePath() {
        return commandsFilePath;
    }

    private void setCommandsFilePath(String commandsFilePath) {
        this.commandsFilePath = commandsFilePath;
    }

    public Boolean getAssignPublicIp() {
        return assignPublicIp;
    }

    public void setAssignPublicIp(Boolean assignPublicIp) {
        this.assignPublicIp = assignPublicIp;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getSshKeyPrivatePath() {
        return sshKeyPrivatePath;
    }

    public void setSshKeyPrivatePath(String sshKeyPrivatePath) {
        this.sshKeyPrivatePath = sshKeyPrivatePath;
    }
}
