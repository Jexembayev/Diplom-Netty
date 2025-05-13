package orhestra.createVmTab.util;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.common.IOUtils;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class SshExecutor {

    public static String runCommand(String host, String user, String privateKeyPath, String command) throws Exception {
        try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host);
            ssh.authPublickey(user, privateKeyPath);

            try (Session session = ssh.startSession()) {
                System.out.println("🚀 Выполняем команду: " + command);
                try (Session.Command cmd = session.exec(command)) {
                    InputStream stdoutStream = cmd.getInputStream();
                    InputStream stderrStream = cmd.getErrorStream();

                    cmd.join(30, TimeUnit.SECONDS);

                    String stdout = IOUtils.readFully(stdoutStream).toString().trim();
                    String stderr = IOUtils.readFully(stderrStream).toString().trim();

                    System.out.println("📗 STDOUT:\n" + stdout);
                    if (!stderr.isEmpty()) {
                        System.out.println("📕 STDERR:\n" + stderr);
                    }

                    return "[STDOUT]\n" + stdout + "\n[STDERR]\n" + stderr;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка SSH-команды: " + e);
            throw e;
        }
    }

    public static void restartNettyServer(String host, String user, String privateKeyPath, String jarName) throws Exception {
        // Сначала убиваем старый сервер
        runCommand(host, user, privateKeyPath, "pkill -f " + jarName + " || true");

        // Запускаем новый сервер в отдельной сессии, с nohup и в фоне
        runCommand(host, user, privateKeyPath, "nohup java -jar " + jarName + " > netty.log 2>&1 &");
    }
}




