package cmd

import entity.Context

/**
 * 执行命令接口
 */
abstract class AbstractCmd<T> implements Serializable {

    protected Context context


    AbstractCmd(context) {
        this.context = context
    }

    AbstractCmd() {
    }

    abstract AbstractCmd execute()

    Context getContext() {
        return context
    }

    void setContext(Context context) {
        this.context = context
    }

    abstract T getResult()

    /**
     * 检测空的函数
     * @param variableName
     * @param variable
     */
    protected static void checkEmpty(String variableName, def variable) {
        if (variable == null) {
            throw new IllegalArgumentException("${variableName} is not set or ${variableName} is empty")
        }

        if (variable instanceof String && variable.isEmpty()) {
            throw new IllegalArgumentException("${variableName} is not set or ${variableName} is empty")
        }

        if (variable instanceof List && variable.size() <= 0) {
            throw new IllegalArgumentException("${variableName} is not set or ${variableName} is empty")
        }

        if (variable instanceof Map && variable.size() <= 0) {
            throw new IllegalArgumentException("${variableName} is not set or ${variableName} is empty")
        }
    }

}