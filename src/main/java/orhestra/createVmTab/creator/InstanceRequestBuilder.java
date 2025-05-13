package orhestra.createVmTab.creator;

import yandex.cloud.api.compute.v1.InstanceOuterClass;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass;

import java.util.UUID;

public class InstanceRequestBuilder {

    public static InstanceServiceOuterClass.CreateInstanceRequest buildFromParams(
            String namePrefix,
            String folderId,
            String zoneId,
            String platformId,
            int cores,
            int memoryGb,
            int diskGb,
            String imageId,
            String subnetId,
            boolean assignPublicIp,
            String userName,
            String sshPublicKey
    ) {
        String instanceName = namePrefix + UUID.randomUUID();

        InstanceServiceOuterClass.NetworkInterfaceSpec.Builder networkInterfaceBuilder =
                InstanceServiceOuterClass.NetworkInterfaceSpec.newBuilder()
                        .setSubnetId(subnetId);

        if (assignPublicIp) {
            networkInterfaceBuilder.setPrimaryV4AddressSpec(
                    InstanceServiceOuterClass.PrimaryAddressSpec.newBuilder()
                            .setOneToOneNatSpec(
                                    InstanceServiceOuterClass.OneToOneNatSpec.newBuilder()
                                            .setIpVersion(InstanceOuterClass.IpVersion.IPV4)
                            )
            );
        }

        String cloudInit = String.format(
                "#cloud-config\n" +
                        "users:\n" +
                        "  - name: %s\n" +
                        "    sudo: ['ALL=(ALL) NOPASSWD:ALL']\n" +
                        "    shell: /bin/bash\n" +
                        "    ssh-authorized-keys:\n" +
                        "      - %s",
                userName, sshPublicKey
        );

        return InstanceServiceOuterClass.CreateInstanceRequest.newBuilder()
                .setFolderId(folderId)
                .setName(instanceName)
                .setZoneId(zoneId)
                .setPlatformId(platformId)
                .setResourcesSpec(
                        InstanceServiceOuterClass.ResourcesSpec.newBuilder()
                                .setCores(cores)
                                .setMemory(memoryGb * 1024L * 1024L * 1024L)
                )
                .setBootDiskSpec(
                        InstanceServiceOuterClass.AttachedDiskSpec.newBuilder()
                                .setDiskSpec(
                                        InstanceServiceOuterClass.AttachedDiskSpec.DiskSpec.newBuilder()
                                                .setImageId(imageId)
                                                .setSize(diskGb * 1024L * 1024L * 1024L)
                                )
                )
                .addNetworkInterfaceSpecs(networkInterfaceBuilder)
                .putMetadata("user-data", cloudInit)
                .build();
    }
}



