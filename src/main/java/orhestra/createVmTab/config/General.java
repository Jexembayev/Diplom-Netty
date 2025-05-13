package orhestra.createVmTab.config;

import org.ini4j.Profile;

public class General {
    private int vmCount;
    private String folderId;
    private String zoneId;

    public General(Profile.Section section) {
        setVmCount(section.get("vm_count"));
        setFolderId(section.get("folder_id"));
        setZoneId(section.get("zone_id"));
    }

    public General() {}

    public int getVmCount() {
        return vmCount;
    }

    private void setVmCount(String user_count) throws NumberFormatException {
        int count;
        try {
            count = Integer.parseInt(user_count);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("[ERROR] Invalid user vm count");
        }

        if (count > 0 && count <= 10) {
            this.vmCount = count;
        } else {
            throw new NumberFormatException("[ERROR] Count must be less than 10 or greater than 0");
        }
    }

    public String getFolderId() {
        return folderId;
    }

    private void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getZoneId() {
        return zoneId;
    }

    private void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
