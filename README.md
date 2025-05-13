Необходимо ввести в терминал
OAUTH_TOKEN(из яндекса)
CLOUD_ID(из яндекса)



Есть метод, который по хоршему тоже вынести в конфиг, путь и имя

SshExecutor.restartNettyServer(
                        vm.getIp(),
                        "rus",
                        "C:/Users/rus/.ssh/id_rsa",
                        "NettyVMServer-1.0-SNAPSHOT-jar-with-dependencies.jar"
                );



[GENERAL]
folder_id=b1g6dj027hrlqa3bq3sr
zone_id=ru-central1-d
vm_count=2

[IMAGE_DEPLOY]
vm_name=test-vm
image_id=fd8iqvq163kiat6hs3qb
subnet_id=fl8krouopt55q04umdpi
platform_id=standard-v3
cpu=2
ram_gb=2
disk_gb=20
user_name = rus
ssh_key_public_path = C:/Users/rus/.ssh/id_rsa.pub
