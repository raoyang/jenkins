package cmd.k8s

import cmd.AbstractCmd
import entity.PodStatus

/**
 * 根据label查询Pod状态列表
 *
 * 对应k8s命令: kubectl get pod -l ${label}=${value} -m ${namespace}*/
class PodStatusByImageCmd extends AbstractCmd<List<PodStatus>> {

    private String contextName
    private String namespace
    private Map<String, String> labels

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    private List<PodStatus> resultImpl

    PodStatusByImageCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        /*String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(this.context.jenkins.currentBuild.startTimeInMillis))
        String result = this.context.jenkins.sh(script: """
            flock -x /tmp/filelock/PodStatusByImageCmd -c "echo '${time}' > /tmp/filelock/PodStatusByImageCmd && kubectl config use-context ${this.contextName} && kubectl get pod  -n ${this.namespace} -l ${this.label}=${this.value}"
        """, returnStdout: true).trim()*/

        checkEmpty("labels", this.labels)

        String label = "";
        boolean isFirst = true
        for (item in this.labels) {
            if (isFirst) {
                isFirst = false
            } else {
                label += ","
            }
            label += item.key + "=" + item.value
        }

        String result = null
        this.context.jenkins.lock(resource: "/tmp/lock/${this.context.jenkins.env.NODE_NAME}") {
            if (this.retryTimes > 0) {
                this.context.jenkins.retry(this.retryTimes + 1) {
                    result = this.context.jenkins.sh(script: """
                            kubectl config use-context ${this.contextName} && kubectl get pod  -n ${
                        this.namespace
                    } -l ${label}
                        """, returnStdout: true).trim()
                }
            } else {
                result = this.context.jenkins.sh(script: """
                        kubectl config use-context ${this.contextName} && kubectl get pod  -n ${
                    this.namespace
                } -l ${label}
                    """, returnStdout: true).trim()
            }
        }
        this.context.jenkins.echo """
                    Pod状态检查结果：
                    ${result}
                """

        this.resultImpl = new ArrayList<>()
        if (result != null && result.size() > 0) {
            String[] lines = result.split("\n")
            if (lines != null && lines.size() > 2) {
                for (int i = 2; i < lines.size(); i++) {
                    String line = lines[i]
                    String[] lineStrs = line.split(" +")
                    if (lineStrs != null && lineStrs.size() > 0) {
                        String[] containers = lineStrs[1].split("/")
                        int totalContainers = Integer.valueOf(containers[0])
                        int readyContainers = Integer.valueOf(containers[1])

                        PodStatus podStatus = new PodStatus()
                        podStatus.setName(lineStrs[0])
                        podStatus.setStatus(lineStrs[2])
                        podStatus.setTotalContainers(totalContainers)
                        podStatus.setReadyContainers(readyContainers)
                        this.resultImpl.add(podStatus)

                        this.context.jenkins.echo "Pod状态检查结果解析: ${podStatus.name}, ${podStatus.status}, ${podStatus.readyContainers}, ${podStatus.totalContainers}"
                    }
                }
            }
        }

        return this
    }

    @Override
    List<PodStatus> getResult() {
        return resultImpl
    }

    String getContextName() {
        return contextName
    }

    PodStatusByImageCmd setContextName(String contextName) {
        this.contextName = contextName
        return this
    }

    String getNamespace() {
        return namespace
    }

    PodStatusByImageCmd setNamespace(String namespace) {
        this.namespace = namespace
        return this
    }

    Map<String, String> getLabels() {
        return labels
    }

    PodStatusByImageCmd setLabels(Map<String, String> labels) {
        this.labels = labels
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    PodStatusByImageCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
