package orhestra.createVmTab.service;

import yandex.cloud.api.compute.v1.ImageServiceGrpc;
import yandex.cloud.api.compute.v1.InstanceServiceGrpc;
import yandex.cloud.api.operation.OperationServiceGrpc;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;

import java.time.Duration;

public class AuthService {
    private final ServiceFactory factory;
    private final OperationServiceGrpc.OperationServiceBlockingStub operationService;
    private InstanceServiceGrpc.InstanceServiceBlockingStub instanceService;
    private ImageServiceGrpc.ImageServiceBlockingStub imageService;

    public AuthService() {
        factory = Auth();
        operationService = factory.create(OperationServiceGrpc.OperationServiceBlockingStub.class, OperationServiceGrpc::newBlockingStub);
        instanceService = factory.create(InstanceServiceGrpc.InstanceServiceBlockingStub.class, InstanceServiceGrpc::newBlockingStub);
        imageService = factory.create(ImageServiceGrpc.ImageServiceBlockingStub.class, ImageServiceGrpc::newBlockingStub);
    }

    private static ServiceFactory Auth() {
        return ServiceFactory.builder()
                .credentialProvider(Auth.oauthTokenBuilder().fromEnv("OAUTH_TOKEN"))
                .requestTimeout(Duration.ofMinutes(1))
                .build();
    }

    public ServiceFactory getFactory() {
        return factory;
    }

    public OperationServiceGrpc.OperationServiceBlockingStub getOperationService() {
        return operationService;
    }

    public InstanceServiceGrpc.InstanceServiceBlockingStub getInstanceService() {
        return instanceService;
    }

    public ImageServiceGrpc.ImageServiceBlockingStub getImageService() {
        return imageService;
    }

}
