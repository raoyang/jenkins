# 单仓流水线构建分支
---
## buildType-构建类型
    docker: docker镜像，推送到harbor仓库
    jar: jar包，推送到maven仓库
    NuGet: NuGet包，推送到NuGet仓库
    source: 源码包，流水线会跳过该类型的推送阶段

## imageVersionPrefix-镜像版本前缀可自定义，默认: branch值, 格式: ${imageVersionPrefix}+时间(到分钟)+buildNum

## configValidate
    checkType-检查的配置数据类型
        REGEX
            功能 : 正则表达式检查
            规则 : 正则表达式内容 如 "^[0-9]{1,2}$"
            TODO: 提供正则官方文档，正则库选型，选定java默认正则库
        NOT_BLANK:
            功能 : 不为空字符
            规则 : 无需设置
    checkRule-检查规则
        REGEX: 正则表达式内容
        NOT_BLANK: 无
    接口
        globalConfig 全局配置，绝对路径
        virtualEnvConfig 带@/后台替换成/，其他会带虚拟环境/vfs/xxx
        cmdbConfig 服务名，key-value：带@后台替换成/cmdb


