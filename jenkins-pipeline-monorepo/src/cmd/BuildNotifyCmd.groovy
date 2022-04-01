package cmd

import entity.Context
import groovy.json.JsonOutput

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 *  通知执行命令
 */
class BuildNotifyCmd extends AbstractCmd<Void> {

    private String title
    private List<String> notifyUserList
    private Map notifyMsg
    private Map buttonUrl

    // 标记是否是错误提示
    private boolean errorNotify = false

    // 是否为代办提醒
    private boolean todoNotify = false

    // 是否发送到全局
    private boolean notifyToGlobalFlag = false

    // 标记是否需要发送
    private boolean needNotify = true

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    BuildNotifyCmd(Context context) {
        super(context)
    }

    private Map userListToDetails(List<String> paramUserList) {
        Map resultMap = [:]
        if (paramUserList == null) {
            return resultMap
        }

        for (String userName : paramUserList) {
            def userDetails = this.context.globalConfig.userDetails
            if (userDetails == null) {
                this.notifyMsg["无法找到联系人${userName}"] = "全局配置中的userDetails信息未配置，请联系管理员"
                this.notifyToGlobalFlag = true
                continue
            }
            def userDetailObject = userDetails[userName]
            if (userDetailObject == null) {
                this.notifyMsg["无法找到联系人${userName}"] = "全局配置中未找到${userName}的配置信息，请联系管理员"
                this.notifyToGlobalFlag = true
                continue
            }

            def telephone = userDetailObject["telephone"]
            def dingTalkAccessToken = userDetailObject["dingTalkAccessToken"]
            def dingTalkSecret = userDetailObject["dingTalkSecret"]

            if (telephone == null || dingTalkAccessToken == null || dingTalkSecret == null) {
                this.notifyMsg["${userName}配置信息不全"] = "全局配置中${userName}配置信息不全，请确认telephone，dingTalkAccessToken和dingTalkSecret"
                this.notifyToGlobalFlag = true
                continue
            }

            resultMap[userName] = userDetailObject
        }

        return resultMap
    }

    boolean getTodoNotify() {
        return todoNotify
    }

    BuildNotifyCmd setTodoNotify(boolean todoNotify) {
        this.todoNotify = todoNotify
        return this
    }

    private String constructJsonContent(List<String> paramUserList) {
        Map notifyInfo = [:]

        String text = "### ${this.title}"
        text += "\n---"

        if (this.errorNotify) {
            text += "\n<font color=#FF3300 size=5 face=\"黑体\">执行结果：失败</font>"
        } else {
            if (this.todoNotify) {
                text += "\n<font color=#FF3300 size=5 face=\"黑体\">注意：请及时执行</font>"
            } else {
                text += "\n<font color=#009900 size=5 face=\"黑体\">执行结果: 成功</font>"
            }
        }

        if (this.notifyUserList != null && !this.notifyUserList.isEmpty()) {
            text += "\n#### 通知用户列表:"
            text += this.notifyUserList.join(",")
            text += "\n---"
        }

        for (notify in this.notifyMsg) {
            text += "\n#### ${notify.key}: ${notify.value}"
        }
        text += "\n---"

        if (paramUserList != null && paramUserList.size() > 0) {
            List<String> phoneList = new ArrayList<>()
            Map userDetailObjects = userListToDetails(paramUserList)
            for (def userDetail : userDetailObjects) {
                phoneList.add(userDetail.value["telephone"].toString())
            }

            notifyInfo["at"] =  [
                    "atMobiles" : phoneList
            ]
            text += "\n####"
            for (String phone : phoneList) {
                text += " @${phone} "
            }
            text += "\n---"
        }

        for (url in this.buttonUrl) {
            text += "\n [${url.key}](${url.value}) "
        }

        notifyInfo["msgtype"] = "markdown"
        notifyInfo["markdown"] = [
                "title" : "${this.title}",
                "text" : "${text}"
        ]

        return JsonOutput.toJson(notifyInfo)
    }

    private void notify(List<String> paramUserList
                        , String dingTalkAccessToken
                        , String dingTalkSecret) {
        this.context.jenkins.retry(3) {
            String json = constructJsonContent(paramUserList)

            Long timestamp = System.currentTimeMillis()
            String sign = buildSign(timestamp, dingTalkSecret)
            if (this.retryTimes > 0) {
                this.context.jenkins.retry(this.retryTimes + 1) {
                    this.context.jenkins.sh """
                        curl 'https://oapi.dingtalk.com/robot/send?access_token=${dingTalkAccessToken}&timestamp=${timestamp}&sign=${sign}' \
                            -H 'Content-Type: application/json' -d '${json}'
                        """
                }
            } else {
                this.context.jenkins.sh """
                    curl 'https://oapi.dingtalk.com/robot/send?access_token=${dingTalkAccessToken}&timestamp=${timestamp}&sign=${sign}' \
                        -H 'Content-Type: application/json' -d '${json}'
                    """
            }
        }
    }

    private void sendToUserList() {
        Map userDetails = userListToDetails(this.notifyUserList)
        for (String userName : this.notifyUserList) {
            if (!userDetails.containsKey(userName)) {
                this.context.jenkins.echo "未找到${userName}的用户配置详情信息"
                continue
            }

            def userDetailObject = userDetails.get(userName)
            List userList = new ArrayList()
            userList.add(userName)

            this.context.jenkins.echo "notify to ${userName}..."
            notify(userList, userDetailObject["dingTalkAccessToken"].toString(), userDetailObject["dingTalkSecret"].toString())
        }
    }

    @Override
    AbstractCmd execute() {
        if (!needNotify) {
            return this
        }

        if (this.notifyUserList == null || this.notifyUserList.isEmpty()) {
            this.notifyToGlobalFlag = true
        }

        sendToUserList()
        if (this.notifyToGlobalFlag) {
            this.context.jenkins.echo "notify to global.., the group will receive the msg"
            notify(this.notifyUserList
                    , this.context.globalConfig.dingTalkAccessToken
                    , this.context.globalConfig.dingTalkSecret)
        }

        return this
    }



    @Override
    Void getResult() {
        return null
    }

    String getTitle() {
        return title
    }

    BuildNotifyCmd setTitle(String title) {
        this.title = title
        return this
    }

    List<String> getNotifyUserList() {
        return this.notifyUserList
    }

    BuildNotifyCmd setNotifyUserList(List<String> notifyUserList) {
        this.notifyUserList = notifyUserList
        return this
    }

    BuildNotifyCmd addToNotifyUserList(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return this
        }

        if (this.notifyUserList == null) {
            this.notifyUserList = new ArrayList<>()
        }
        if (this.notifyUserList.contains(userName.trim())) {
            return this
        }
        this.notifyUserList.add(userName.trim())

        return this
    }

    Map getNotifyMsg() {
        return notifyMsg
    }

    BuildNotifyCmd setNotifyMsg(Map notifyMsg) {
        this.notifyMsg = notifyMsg
        return this
    }

    Map getButtonUrl() {
        return buttonUrl
    }

    BuildNotifyCmd setButtonUrl(Map buttonUrl) {
        this.buttonUrl = buttonUrl
        return this
    }

    boolean getErrorNotify() {
        return errorNotify
    }

    BuildNotifyCmd setErrorNotify(boolean errorNotify) {
        this.errorNotify = errorNotify
        return this
    }

    boolean getNotifyToGlobalFlag() {
        return notifyToGlobalFlag
    }

    boolean getNeedNotify() {
        return needNotify
    }

    BuildNotifyCmd setNeedNotify(boolean needNotify) {
        this.needNotify = needNotify
        return this
    }

    BuildNotifyCmd setNotifyToGlobalFlag(boolean notifyToGlobalFlag) {
        this.notifyToGlobalFlag = notifyToGlobalFlag
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    BuildNotifyCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }

    private static String buildSign(Long timestamp, String secret) {
        String stringToSign = timestamp + "\n" + secret
        Mac mac = Mac.getInstance("HmacSHA256")
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"))
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"))
        String sign = URLEncoder.encode(new String(new String(Base64.getEncoder().encodeToString(signData))), "UTF-8")
        return sign
    }


}
