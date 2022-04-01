package cmd

import entity.Context
import groovy.json.JsonOutput

/**
 *  API调用命令
 */
class LateinosFrameworkApiCmd extends AbstractCmd<Object> {

    /**
     *  需要调用的集群名称
     */
    private String clusterName
    /**
     *  调用的虚拟环节
     */
    private String virtualEnv
    /**
     *  远程服务的名称
     */
    private String remoteServiceName
    /**
     *  路由Key
     */
    private String routeKey

    /**
     * 请求方法
     */
    private String apiMethod
    /**
     *  api的路径
     */
    private String apiPath
    /**
     *  api查询的请求参数列表
     */
    private Map apiQueryParams
    /**
     * 请求的body对象
     */
    private def apiBody
    /**
     *  api请求额外的头部
     */
    private Map apiExtraHeaders

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    private def resultImpl

    LateinosFrameworkApiCmd(Context context) {
        super(context)
    }

    private Map constructHeaders() {
        String token = new GetGlobalTokenCmd(this.context)
                .setTokenId("LateinosControllerAgentToken")
                .execute()
                .getResult()

        // 处理头部
        Map headers = [
                "X-Request-Cluster"   : clusterName,
                "X-Remote-ServiceName": remoteServiceName,
                "X-Request-AuthToken" : token
        ]
        if (virtualEnv != null && !virtualEnv.isEmpty()) {
            headers["X-Request-VirtualEnv"] = virtualEnv
        }
        if (apiExtraHeaders != null) {
            for (extraHeader in apiExtraHeaders) {
                headers[extraHeader.key.toString()] = extraHeader.value.toString()
            }
        }
        return headers
    }

    private String constructRequestURI() {
        // 处理URI
        String encodeParams = ""
        if (apiQueryParams != null) {
            for (queryParam in apiQueryParams) {
                encodeParams = encodeParams + "&${queryParam.key}="
                +URLEncoder.encode("${queryParam.value}", "UTF-8")
            }
        }

        String requestPathAndParams = apiPath
        if (encodeParams.length() > 0) {
            requestPathAndParams = "${apiPath}?${encodeParams}"
        }

        return "http://lateinos-controller-agent-svc.lateinos.svc:7000${requestPathAndParams}"
    }

    private String constructBody() {
        if (this.apiBody != null) {
            return JsonOutput.toJson(this.apiBody)
        }
        return null
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("clusterName", clusterName)
        checkEmpty("remoteServiceName", remoteServiceName)
        checkEmpty("apiMethod", apiMethod)
        checkEmpty("apiPath", apiPath)

        if (!apiPath.startsWith("/")) {
            throw new IllegalArgumentException("apiPath should start with /")
        }

        Map headers = constructHeaders()
        String requestURI = constructRequestURI()
        /*for (def item in this.apiBody.validateOptions) {
            this.context.jenkins.echo "item - key=${item.key}, validateType=${item.validateType}, validateRule=${item.validateRule}"
        }*/
        String body = constructBody()
        /*this.context.jenkins.echo "body=${body}"*/

        def httpResult = {}
        if (retryTimes > 0) {
            this.context.jenkins.retry(retryTimes + 1) {
                httpResult = handleRequest(requestURI, headers, body)
            }
        } else {
            httpResult = handleRequest(requestURI, headers, body)
        }
        if (httpResult["responseCode"] != 200) {
            this.context.jenkins.error "request failed, responseCode=${httpResult["responseCode"]}}, content=${httpResult["responseContent"]}"
        }
        this.resultImpl = this.context.jenkins.readJSON text: httpResult["responseContent"]
        return this
    }

    private def handleRequest(String requestURI, Map headers, String body) {
        URL urlRequest = new URL(requestURI)
        HttpURLConnection urlConnection = (HttpURLConnection) urlRequest.openConnection()
        urlConnection.setRequestMethod(this.apiMethod)
        for (header in headers) {
            urlConnection.setRequestProperty(header.key.toString(), header.value.toString())
        }
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( jenkins ) ")
        urlConnection.setRequestProperty("Accept", "*/*")
        urlConnection.setConnectTimeout(30000)
        urlConnection.setReadTimeout(30000)
        urlConnection.setUseCaches(false)
        urlConnection.setDoInput(true)

        if (body != null) {
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setDoOutput(true)
            urlConnection.getOutputStream().write(body.getBytes("UTF-8"))
            urlConnection.getOutputStream().flush()
        }

        urlConnection.connect()

        int responseCode = urlConnection.getResponseCode()
        String responseContent = urlConnection.getInputStream().getText("UTF-8")

        return [responseCode: responseCode, responseContent: responseContent]
    }

    @Override
    def getResult() {
        return this.resultImpl
    }

    String getClusterName() {
        return clusterName
    }

    LateinosFrameworkApiCmd setClusterName(String clusterName) {
        this.clusterName = clusterName
        return this
    }

    String getVirtualEnv() {
        return virtualEnv
    }

    LateinosFrameworkApiCmd setVirtualEnv(String virtualEnv) {
        this.virtualEnv = virtualEnv
        return this
    }

    String getRemoteServiceName() {
        return remoteServiceName
    }

    LateinosFrameworkApiCmd setRemoteServiceName(String remoteServiceName) {
        this.remoteServiceName = remoteServiceName
        return this
    }

    String getRouteKey() {
        return routeKey
    }

    LateinosFrameworkApiCmd setRouteKey(String routeKey) {
        this.routeKey = routeKey
        return this
    }

    String getApiPath() {
        return apiPath
    }

    LateinosFrameworkApiCmd setApiPath(String apiPath) {
        this.apiPath = apiPath
        return this
    }

    Map getApiQueryParams() {
        return apiQueryParams
    }

    LateinosFrameworkApiCmd setApiQueryParams(Map apiQueryParams) {
        this.apiQueryParams = apiQueryParams
        return this
    }

    def getApiBody() {
        return apiBody
    }

    LateinosFrameworkApiCmd setApiBody(apiBody) {
        this.apiBody = apiBody
        return this
    }

    Map getApiExtraHeaders() {
        return apiExtraHeaders
    }

    LateinosFrameworkApiCmd setApiExtraHeaders(Map apiExtraHeaders) {
        this.apiExtraHeaders = apiExtraHeaders
        return this
    }

    String getApiMethod() {
        return apiMethod
    }

    LateinosFrameworkApiCmd setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod
        return this
    }
}
