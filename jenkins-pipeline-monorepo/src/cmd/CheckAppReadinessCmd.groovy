package cmd

import cmd.k8s.PodStatusByImageCmd
import cmd.utils.ImageVersionUtils
import entity.AppStatus
import entity.PodStatus

/**
 * 检查应用是否就绪
 */
class CheckAppReadinessCmd extends AbstractCmd<AppStatus> {

    private static int LOOP_INTERVAL = 10

    private String projectName
    private String cluster
    private String namespace
    private String targetImage
    private int firstPodTimeoutSecond = 180
    private int timeoutSecond = 360

    private AppStatus resultImpl

    CheckAppReadinessCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("projectName", this.projectName)
        checkEmpty("cluster", this.cluster)
        checkEmpty("namespace", this.namespace)
        checkEmpty("targetImage", this.targetImage)

        AppStatus appStatus = new AppStatus()
        appStatus.setProjectName(this.projectName)

        // 循环检查，5s间隔
        String version = ImageVersionUtils.imageToTag(this.targetImage)
        def labels = [
                "app"      : this.projectName,
                "image-tag": version
        ]

        def firstPodStatus = this.checkFirstPod(labels)
        if (firstPodStatus.status) {
            def allPodStatus = this.checkAllPod(labels)
            appStatus.setIsReadiness(allPodStatus.status)
            appStatus.setPodStatuses(allPodStatus.targetPods)
            if (!allPodStatus.status) {
                this.context.jenkins.echo allPodStatus.errorMsg
            }
        } else {
            this.context.jenkins.echo firstPodStatus.errorMsg
        }

        this.resultImpl = appStatus
        return this
    }

    /**
     * 检查第一个启动的Pod是否正常
     *
     * @param loopNum
     * @param labels
     * @return
     */
    private def checkFirstPod(def labels) {
        boolean isFirstPodFaild = false
        List<PodStatus> targetPods = null
        String errorMsg = ""
        int loopNum = (int) (this.firstPodTimeoutSecond / LOOP_INTERVAL) + 1
        int i = 1
        for (; i <= loopNum; i++) {
            targetPods = new PodStatusByImageCmd(this.context)
                    .setContextName(this.cluster)
                    .setNamespace(this.namespace)
                    .setLabels(labels)
                    .execute().getResult()
            if (targetPods != null && targetPods.size() > 0) {
                PodStatus firstPod = targetPods.get(0)
                isFirstPodFaild = firstPod.status == "Failed" ||
                        firstPod.status == "Unkonwn" ||
                        firstPod.status == "InvalidImageName" ||
                        firstPod.status == "ImageInspectError" ||
                        firstPod.status == "ErrImageNeverPull" ||
                        firstPod.status == "ImagePullBackOff" ||
                        firstPod.status == "RegistryUnavailabel" ||
                        firstPod.status == "ErrImagePull" ||
                        firstPod.status == "CreateContainerConfigError" ||
                        firstPod.status == "CreateContainerError" ||
                        firstPod.status == "m.internalLifecycle.PreStartContainer" ||
                        firstPod.status == "RunContainerError" ||
                        firstPod.status == "PostStartHookError"

                if (isFirstPodFaild) {
                    errorMsg = "App就绪状态检查 - 首个Pod启动失败: firstPodStatus=${firstPod.status}"
                    break
                } else if (firstPod.status == "Running") {
                    break
                }
            } else {
                isFirstPodFaild = true
            }

            this.context.jenkins.sleep(LOOP_INTERVAL)
        }

        if (i > loopNum) {
            errorMsg = "App就绪状态检查: 部署超时"
        }

        return [status: !isFirstPodFaild, targetPods: targetPods, errorMsg: errorMsg]
    }

    /**
     * 检查所有Pod是否部署完成
     * @param labels
     */
    private def checkAllPod(def labels) {
        // 开启循环检测
        boolean isTargetPodReady = false
        List<PodStatus> targetPods = null
        String errorMsg = ""
        int loopNum = (int) (timeoutSecond / LOOP_INTERVAL) + 1
        int i = 1
        for (; i <= loopNum; i++) {
            // 查询所有Pod状态
            List<PodStatus> allPods = new PodStatusByImageCmd(this.context)
                    .setContextName(this.cluster)
                    .setNamespace(this.namespace)
                    .setLabels(["app": projectName])
                    .execute().getResult()

            // 查询本次发布Pod状态
            targetPods = new PodStatusByImageCmd(this.context)
                    .setContextName(this.cluster)
                    .setNamespace(this.namespace)
                    .setLabels(labels)
                    .execute().getResult()

            // 检查流量是否切换完毕
            int terminatingPodNum = 0
            for (PodStatus podStatus : allPods) {
                if (podStatus.status == "Terminating") {
                    terminatingPodNum++
                }
            }
            int diff = allPods.size() - targetPods.size() - terminatingPodNum
            if (diff <= 0) {
                this.context.jenkins.echo "${i} - App就绪状态检查: PodDiff = ${diff}"

                // 检查所有Pod状态是否就绪
                for (PodStatus podStatus : targetPods) {
                    isTargetPodReady = podStatus.status == "Running" &&
                            podStatus.readyContainers > 0 &&
                            podStatus.readyContainers == podStatus.totalContainers
                }

                this.context.jenkins.echo "${i} - App就绪状态检查: targetPodNume=${targetPods.size()}, isTargetPodReady=${isTargetPodReady}"

                if (isTargetPodReady) {
                    break
                }
            }

            this.context.jenkins.sleep(LOOP_INTERVAL)
        }

        if (i > loopNum) {
            this.context.jenkins.echo "App就绪状态检查: 部署超时"
        }

        return [status: isTargetPodReady, targetPods: targetPods, errorMsg: errorMsg]
    }

    @Override
    AppStatus getResult() {
        return this.resultImpl
    }

    String getProjectName() {
        return projectName
    }

    CheckAppReadinessCmd setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }

    String getCluster() {
        return cluster
    }

    CheckAppReadinessCmd setCluster(String cluster) {
        this.cluster = cluster
        return this
    }

    String getNamespace() {
        return namespace
    }

    CheckAppReadinessCmd setNamespace(String namespace) {
        this.namespace = namespace
        return this
    }

    String getTargetImage() {
        return targetImage
    }

    CheckAppReadinessCmd setTargetImage(String targetImage) {
        this.targetImage = targetImage
        return this
    }

    int getFirstPodTimeoutSecond() {
        return firstPodTimeoutSecond
    }

    CheckAppReadinessCmd setFirstPodTimeoutSecond(int firstPodTimeoutSecond) {
        this.firstPodTimeoutSecond = firstPodTimeoutSecond
        return this
    }

    int getTimeoutSecond() {
        return timeoutSecond
    }

    CheckAppReadinessCmd setTimeoutSecond(int timeoutSecond) {
        this.timeoutSecond = timeoutSecond
        return this
    }
}
