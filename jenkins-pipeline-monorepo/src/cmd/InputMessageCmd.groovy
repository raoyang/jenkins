package cmd

import entity.Context
import pipeline.BaseProcess

/**
 *  输入通知消息
 */
class InputMessageCmd extends AbstractCmd<Object> {

    private List<String> notifyUserNames = new ArrayList<>()
    private String notifyMsg

    private String okBtnText = "确定"
    private String inputId
    private List<String> submitterList = new ArrayList<>()
    private BaseProcess process
    private List inputParameters = new ArrayList()

    private boolean notifyToGlobal = false

    private def resultImpl

    InputMessageCmd(Context context, BaseProcess process) {
        super(context)
        this.process = process
    }

    InputMessageCmd addNotifyUserName(String notifyUserName) {
        if (notifyUserName == null || notifyUserName.trim().isEmpty()) {
            return this
        }

        if (!this.notifyUserNames.contains(notifyUserName.trim())) {
            this.notifyUserNames.add(notifyUserName.trim())
        }
        return this
    }

    InputMessageCmd addSubmitter(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return this
        }

        if (!this.submitterList.contains(userName.trim())) {
            this.submitterList.add(userName.trim())
        }
        return this
    }

    String getNotifyMsg() {
        return notifyMsg
    }

    InputMessageCmd setNotifyMsg(String notifyMsg) {
        this.notifyMsg = notifyMsg
        return this
    }

    String getOkBtnText() {
        return okBtnText
    }

    InputMessageCmd setOkBtnText(String okBtnText) {
        this.okBtnText = okBtnText
        return this
    }

    InputMessageCmd addParameter(def parameter) {
        this.inputParameters.add(parameter)
        return this
    }

    boolean getNotifyToGlobal() {
        return notifyToGlobal
    }

    InputMessageCmd setNotifyToGlobal(boolean notifyToGlobal) {
        this.notifyToGlobal = notifyToGlobal
        return this
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("notifyMsg", this.notifyMsg)

        if (!notifyUserNames.isEmpty()) {
            BuildNotifyCmd notifyCmd = this.process.constructNotify()
            for (String userName : notifyUserNames) {
                notifyCmd.addToNotifyUserList(userName)
            }
            notifyCmd.setTodoNotify(true)
            notifyCmd.setNotifyToGlobalFlag(this.notifyToGlobal)
            notifyCmd.notifyMsg["信息"] = notifyMsg
            notifyCmd.buttonUrl["输入链接"] = "${this.context.jenkins.env.BUILD_URL}/input"
            notifyCmd.execute()
        }

        // 填坑，如果只有一个选型，最后input方法不会返回一个对象
        if (inputParameters.size() == 1) {
            inputParameters.add(
                    this.context.jenkins.choice(name: "branchName", choices: "${this.context.branchName}"))
        }

        Map inputMessage = [
                message: this.notifyMsg,
                ok: this.okBtnText,
                parameters: inputParameters
        ]
        if (this.submitterList) {
            inputMessage["submitter"] = this.submitterList.join(",")
        }

        this.resultImpl = this.context.jenkins.input(inputMessage)
        return this
    }

    @Override
    def getResult() {
        return resultImpl
    }
}
