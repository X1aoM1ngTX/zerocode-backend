package com.xm.vexorbackend.core.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReactProjectBuilder {

    /**
     * 异步构建项目（不阻塞主流程）
     *
     * @param projectPath 项目路径
     */
    public void buildProjectAsync(String projectPath) {
        // 在单独的线程中执行构建，避免阻塞主流程
        Thread.ofVirtual().name("react-builder-" + System.currentTimeMillis()).start(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("异步构建 React 项目时发生异常: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 构建 React 项目
     *
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectPath);
            return false;
        }
        // 检查 package.json 是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在: {}", projectPath);
            return false;
        }
        try {
            log.info("等待 React 项目构建许可: {}", projectPath);
            NpmBuildConcurrencyLimiter.SEMAPHORE.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("等待 React 项目构建许可被中断: {}", projectPath);
            return false;
        }
        try {
            log.info("开始构建 React 项目: {}", projectPath);
            // 执行 npm install
            if (!executeNpmInstall(projectDir)) {
                log.error("npm install 执行失败：{}", projectPath);
                return false;
            }
            // 执行 npm run build
            if (!executeNpmBuild(projectDir)) {
                log.error("npm run build 执行失败：{}", projectPath);
                return false;
            }
            // 验证 dist 目录是否生成（Vite默认生成dist目录）
            File distDir = new File(projectDir, "dist");
            if (!distDir.exists() || !distDir.isDirectory()) {
                log.error("构建完成但 dist 目录未生成：{}", projectPath);
                return false;
            }
            log.info("React 项目构建成功，dist 目录：{}", projectPath);
            return true;
        } finally {
            NpmBuildConcurrencyLimiter.SEMAPHORE.release();
        }
    }

    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install --no-audit --no-fund --prefer-offline "
                        + "--fetch-timeout=60000 --fetch-retries=2 "
                        + "--fetch-retry-mintimeout=10000 --fetch-retry-maxtimeout=60000",
                buildCommand("npm"));
        return executeCommand(projectDir, command, 600, null); // 10分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 600, "--max-old-space-size=1024"); // 10分钟超时
    }

    /**
     * 判断是否为 Windows 系统
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 构建命令，根据操作系统添加 .cmd 后缀
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @param nodeOptions    Node.js 参数，为 null 时不设置
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds, String nodeOptions) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            configureNpmEnvironment(pb, workingDir);
            if (nodeOptions != null) {
                pb.environment().put("NODE_OPTIONS", nodeOptions);
            }
            Process process = pb.start();
            Thread outputThread = Thread.ofVirtual()
                    .name("react-builder-output-" + System.currentTimeMillis())
                    .start(() -> logProcessOutput(process, command));
            Thread monitorThread = Thread.ofVirtual()
                    .name("react-builder-monitor-" + System.currentTimeMillis())
                    .start(() -> logProcessHeartbeat(process, command, timeoutSeconds));
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                destroyProcessTree(process);
                process.waitFor(5, TimeUnit.SECONDS);
                outputThread.join(5000);
                monitorThread.join(5000);
                return false;
            }
            outputThread.join(5000);
            monitorThread.join(5000);
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }

    /**
     * 固定 npm registry、cache 和日志行为，减少线上环境差异。
     */
    private void configureNpmEnvironment(ProcessBuilder pb, File workingDir) {
        String registry = System.getProperty("vexor.npm.registry", "https://registry.npmmirror.com");
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), "vexor-npm-cache");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            log.warn("npm cache 目录创建失败: {}", cacheDir.getAbsolutePath());
        }
        pb.environment().put("NPM_CONFIG_REGISTRY", registry);
        pb.environment().put("NPM_CONFIG_CACHE", cacheDir.getAbsolutePath());
        pb.environment().put("NPM_CONFIG_PROGRESS", "false");
        pb.environment().put("NPM_CONFIG_FUND", "false");
        pb.environment().put("NPM_CONFIG_AUDIT", "false");
        pb.environment().putIfAbsent("HOME", workingDir.getAbsolutePath());
    }

    /**
     * 定时输出心跳，方便判断 npm 是无输出运行还是 JVM/宿主机调度停顿。
     */
    private void logProcessHeartbeat(Process process, String command, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        try {
            while (process.isAlive()) {
                TimeUnit.SECONDS.sleep(30);
                if (process.isAlive()) {
                    long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                    log.info("命令仍在执行 [{}]，已运行 {} 秒，超时限制 {} 秒", command, elapsedSeconds, timeoutSeconds);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 同时终止 npm 及其子进程，避免超时后残留 node 进程继续占用资源。
     */
    private void destroyProcessTree(Process process) {
        process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    /**
     * 持续读取子进程输出，避免 stdout/stderr 管道写满导致 npm 进程阻塞。
     */
    private void logProcessOutput(Process process, String command) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("命令输出 [{}]: {}", command, line);
            }
        } catch (Exception e) {
            log.warn("读取命令输出失败: {}, 错误信息: {}", command, e.getMessage());
        }
    }

}
