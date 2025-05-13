package orhestra.createVmTab.util;

import javafx.beans.property.*;

public class VmInfo {
    private final StringProperty name;
    private final StringProperty status;
    private final StringProperty imageId;
    private final StringProperty zoneId;
    private final IntegerProperty cores;
    private final IntegerProperty memoryGb;
    private final IntegerProperty diskGb;
    private final StringProperty ip;
    private final StringProperty configuration;
    private final StringProperty javaVersion;
    private final StringProperty serverStatus;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);



    public VmInfo(String name, String status, String imageId, String zoneId,
                  int cores, int memoryGb, int diskGb, String configuration) {
        this.name = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
        this.imageId = new SimpleStringProperty(imageId);
        this.zoneId = new SimpleStringProperty(zoneId);
        this.cores = new SimpleIntegerProperty(cores);
        this.memoryGb = new SimpleIntegerProperty(memoryGb);
        this.diskGb = new SimpleIntegerProperty(diskGb);
        this.ip = new SimpleStringProperty("");
        this.configuration = new SimpleStringProperty(configuration);
        this.javaVersion = new SimpleStringProperty("?");
        this.serverStatus = new SimpleStringProperty("?");
    }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public String getImageId() { return imageId.get(); }
    public StringProperty imageIdProperty() { return imageId; }

    public String getZoneId() { return zoneId.get(); }
    public StringProperty zoneIdProperty() { return zoneId; }

    public int getCores() { return cores.get(); }
    public IntegerProperty coresProperty() { return cores; }

    public int getMemoryGb() { return memoryGb.get(); }
    public IntegerProperty memoryGbProperty() { return memoryGb; }

    public int getDiskGb() { return diskGb.get(); }
    public IntegerProperty diskGbProperty() { return diskGb; }

    public String getIp() { return ip.get(); }
    public void setIp(String ip) { this.ip.set(ip); }
    public StringProperty ipProperty() { return ip; }


    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
    public BooleanProperty selectedProperty() { return selected; }

    public String getConfiguration() { return configuration.get(); }
    public StringProperty configurationProperty() { return configuration; }

    public void setDiskGb(int value) { this.diskGb.set(value); }
    public void setServerRunning(String value) { this.serverStatus.set(value); }

    public String getJavaVersion() { return javaVersion.get(); }
    public void setJavaVersion(String javaVersion) { this.javaVersion.set(javaVersion); }
    public StringProperty javaVersionProperty() { return javaVersion; }

    public String getServerStatus() { return serverStatus.get(); }
    public void setServerStatus(String status) { this.serverStatus.set(status); }
    public StringProperty serverStatusProperty() { return serverStatus; }

    @Override
    public String toString() {
        return String.format("🖥 %s | Status: %s | Image: %s | Zone: %s | CPU: %d | RAM: %dGB | Disk: %dGB | Java: %s | Server: %s",
                getName(), getStatus(), getImageId(), getZoneId(), getCores(), getMemoryGb(), getDiskGb(),
                getJavaVersion(), getServerStatus());
    }
}




