package cmd.middleware

import cmd.AbstractCmd
import entity.middleware.BaseValidateResult

/**
 * 执行中间件检查命令接口
 */
abstract class AbstractMiddlewareValidateCmd extends AbstractCmd<BaseValidateResult> {

    protected String clusterName
    protected String virtualEnv
    protected def config

    protected BaseValidateResult resultImpl

    AbstractMiddlewareValidateCmd(context) {
        super(context)
    }

    abstract AbstractMiddlewareValidateCmd execute()

    @Override
    BaseValidateResult getResult() {
        return this.resultImpl
    }

    String getClusterName() {
        return clusterName
    }

    AbstractMiddlewareValidateCmd setClusterName(String clusterName) {
        this.clusterName = clusterName
        return this
    }

    String getVirtualEnv() {
        return virtualEnv
    }

    AbstractMiddlewareValidateCmd setVirtualEnv(String virtualEnv) {
        this.virtualEnv = virtualEnv
        return this
    }

    def getConfig() {
        return config
    }

    AbstractMiddlewareValidateCmd setConfig(config) {
        this.config = config
        return this
    }
}