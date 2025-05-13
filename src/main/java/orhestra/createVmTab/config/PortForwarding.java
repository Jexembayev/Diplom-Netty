package orhestra.createVmTab.config;

import org.ini4j.Profile;

import java.util.HashMap;
import java.util.Map;

public class PortForwarding {
    private Map<Integer, Integer> portMappingBase;

    public PortForwarding(Profile.Section section) {
        setPortMappingBase(section.get("port_mapping_base"));
    }

    public Map<Integer, Integer> getPortMappingBase() {
        return portMappingBase;
    }

    public void setPortMappingBase(String portMappingStr) {
        portMappingBase = new HashMap<Integer, Integer>();

        String[] pairs = portMappingStr.split(", *");
        for (String pair : pairs) {
            String[] ports = pair.split(":");
            if (ports.length == 2) {
                int internalPort = Integer.parseInt(ports[0].trim());
                int basePort = Integer.parseInt(ports[1].trim());
                portMappingBase.put(internalPort, basePort);
            }
        }
    }
}
