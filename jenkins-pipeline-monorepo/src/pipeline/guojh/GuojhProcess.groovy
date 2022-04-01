package pipeline.guojh

import cmd.FileUploadCmd
import entity.Context
import pipeline.BaseProcess

/**
 *  个人测试流水线
 */
class GuojhProcess extends BaseProcess {

    @Override
    void execute() {

//        this.context.jenkins.stage("钉钉通知") {
//
//            String dingTalkAccessToken = "051cea08360170d56a2d5c4290f62ca92c0b8e675bfe12233fa995db573773fd"
//            String secret = "SEC74776fcc8649de09f8a833567cc6a54dd2602a3d5b49f2ed3f8914a354483d79"
//            List<String> phoneList = ["+86-18512830417"]
//            Map notifyMsg = ["自定义通知": "测试构建通知", "构建版本": "00001"]
//            String title = "构建通知标题"
//            Map notifyUrl = ["自定义url": "https://www.baidu.com/"]
//            new BuildNotifyCmd(this.context).setDingTalkAccessToken(dingTalkAccessToken)
//                    .setTitle(title)
//                    .setNotifyMsg(notifyMsg)
//                    .setPhoneList(phoneList)
//                    .setButtonUrl(notifyUrl)
//                    .setSecret(secret)
//                    .execute()
//        }


//        this.context.jenkins.stage("其他测试") {
//            List res = new GetAllBranchesCmd(this.context).execute().getResult()
//            this.context.jenkins.echo "${res}"
//
//            new MergeCodeCmd(this.context).setSourceBranch("main").setTargetBranch("dev-sdk").execute().getResult()
//            String tokenId = this.context.jenkins.params.secretToken
//            String secretToken = new GetGlobalTokenCmd(this.context).setTokenId(tokenId).execute().getResult()
//            this.context.jenkins.echo "token是：${secretToken}"
//
//            List<GitProjectMsg> resl = new GetGroupProjCmd(this.context).setGitUrl("https://gitlab.h3yun.net").setGroupId(289).setAccessToken("2wsaHcLA3yMbXSFYxxJM").execute().getResult()
//            this.context.jenkins.echo "${resl.size()}"
//        }


//        this.context.jenkins.stage("git webhook测试") {
//            List<GitHook> gitHookList = new GetAllGitHookCmd(this.context).setGitUrl("https://gitlab.h3yun.net")
//                    .setProjectPath("ug-global/lateinos-monorepo")
//                    .setToken(secretToken).execute().getResult()
//
//            Integer id
//            for (GitHook gitHook : gitHookList) {
//                if (gitHook.getPushEventsBranchFilter() == "target") {
//                    id = gitHook.getId()
//                    break
//                }
//            }
//            new DeleteGitHookCmd(this.context).setHookId(id).setGitUrl("https://gitlab.h3yun.net")
//                    .setProjectPath("ug-global/lateinos-monorepo")
//                    .setToken(secretToken).setHookId(id).execute().getResult()
//
//
//            GitHook gitHook = new GitHook().setUrl("http://jenkins.puboa.h3yun.net/project/monorepo-pipline/lateinos-monorepo/lateinos-monorepo-multibranch/dev")
//                    .setPushEventsBranchFilter("dev")
//            new AddGitHookCmd(this.context).setGitUrl("https://gitlab.h3yun.net")
//                    .setProjectPath("ug-global/lateinos-monorepo")
//                    .setAccessToken(token)
//                    .setSecretToken("621fc137939559c9b0cb0d442c5d622c")
//                    .setGitHook(gitHook).execute().getResult()
//
//            LatestCommitMsg committer = new GetLatestCommitMsg(this.context).execute().getResult()
//            this.context.jenkins.echo "作者${committer.getAuthorName()} 提交人${committer.getCommitterName()}"
//        }


//        this.context.jenkins.stage("dotnet与java代码扫描") {
//            ProjectItemConfig javaConfig = new ProjectItemConfig()
//            javaConfig.setProjectName('lateinos-runtime-server-test')
//            javaConfig.setCodeDir('backend/runtime-server/lateinos-runtime-server')
//            javaConfig.setCodeType('java')
//
//            ProjectItemConfig dotnetConfig = new ProjectItemConfig()
//            dotnetConfig.setProjectName('lateinos-dotnet-demo')
//            dotnetConfig.setCodeDir('backend/lateinos-dotnet-demo')
//            dotnetConfig.setCodeType('dotnet')
//
//            SonarCheckMsg javaMsg = new SonarCheckCmd(this.context).setProjectItemConfig(javaConfig)
//                    .setGitCommitter('guojh').execute().getResult()
//            this.context.jenkins.echo "${javaMsg.toString()}"
//
//            SonarCheckMsg dotnetMsg = new SonarCheckCmd(this.context).setProjectItemConfig(dotnetConfig)
//                    .setGitCommitter('guojh').execute().getResult()
//            this.context.jenkins.echo "${dotnetMsg.toString()}"
//        }


//        this.context.jenkins.stage("webapp打包") {
//            String result = new BuildAppImageCmd(this.context)
//                    .setCodeType('webapp')
//                    .setBuildDir('frontend/webapp/lateinos-webadmin-webapp')
//                    .setProjectName('lateinos-webadmin-webapp')
//                    .setRegistry(context.globalConfig.defaultRegistry)
//                    .setImagePath(context.globalConfig.defaultImagePath)
//                    .setBaseImage('nginx')
//                    .execute()
//                    .getResult()
//            this.context.jenkins.echo "${result}"
//        }
//
//        this.context.jenkins.stage("前端扫描测试") {
//            ProjectItemConfig webappConfig = new ProjectItemConfig()
//            webappConfig.setProjectName('lateinos-webadmin-webapp')
//            webappConfig.setCodeDir('frontend/webapp/lateinos-webadmin-webapp')
//            webappConfig.setCodeType('webapp')
//
//            SonarCheckMsg javaMsg = new SonarCheckCmd(this.context).setProjectItemConfig(webappConfig)
//                    .setGitCommitter('guojh').execute().getResult()
//            this.context.jenkins.echo "${javaMsg.toString()}"
//        }


//        this.context.jenkins.stage("ug-pc-apps") {
//            List<ProjectItemConfig> subappList = new ArrayList<>()
//            ProjectItemConfig subapp5 = new ProjectItemConfig()
//            subapp5.setProjectName('ug-formview-subapp')
//            subapp5.setBuildDir('ug-pc-apps/ug-formview-subapp')
//            subapp5.setCodeType('subapp')
//            subappList.add(subapp5)
//            new SubappPackageCmd(this.context).setSubappList(subappList)
//                    .setAppsDir('ug-pc-apps')
//                    .setAppsName('ug-pc-apps')
//                    .setOssEndpoint("oss-cn-hangzhou.aliyuncs.com")
//                    .setCredentialId(this.context.jenkins.params.ossCredentialId)
//                    .setResourcesUpload("oss://youge-webstatic")
//                    .setResourcePath("web-static-dev/ug-pc-app-monorepo-test")
//                    .execute()
//        }

        this.context.jenkins.stage("自动化测试oss上传") {
            String Url = new FileUploadCmd(this.context)
                    .setFilePath('./tools/lcos-install.js')
                    .setProjectName('test')
                    .setDeliveryGroup('test')
                    .execute()
                    .getResult()
            this.context.jenkins.echo "${Url}"
        }


//        this.context.jenkins.stage("ug-mobile-apps") {
//            ProjectItemConfig subapp8 = new ProjectItemConfig()
//            subapp8.setBuildDir('frontend/ug-mobile-apps/ug-mobile-main')
//            subapp8.setCodeType('subapp')
//            ProjectItemConfig subapp9 = new ProjectItemConfig()
//            subapp9.setBuildDir('frontend/ug-mobile-apps/ugm-gantt-subapp')
//            subapp9.setCodeType('subapp')
//            ProjectItemConfig subapp10 = new ProjectItemConfig()
//            subapp10.setBuildDir('frontend/ug-mobile-apps/ugm-tableview-subapp')
//            subapp10.setCodeType('subapp')
//            subappList.add(subapp8)
//            subappList.add(subapp9)
//            subappList.add(subapp10)
//
//
//            new SubappPackageCmd(this.context).setSubappList(subappList)
//                    .setAppsDir('frontend/ug-mobile-apps')
//                    .setAppsName('ug-mobile-apps')
//                    .setOssEndpoint("oss-cn-shenzhen-internal.aliyuncs.com")
//                    .setCredentialId(this.context.jenkins.params.ossCredentialId)
//                    .setResourcesUpload("oss://lateinos-polymer-oss")
//                    .setResourcePath("web-static-dev/ug-mobile-app-test")
//                    .execute()
//        }

//        this.context.jenkins.stage("动态创建并行stage测试") {
//            def subappStageList = null
//            List<String> subappList = ["ug-pc-main", "ug-membermanage-subapp", "ug-workflow-subapp"]
//
//            subappStageList = subappList.collectEntries {
//                ["打包${it}": myfuction(it)]
//            }
//            this.context.jenkins.stage("subapp打包") {
//                this.context.jenkins.parallel subappStageList
//            }
//            this.context.jenkins.stage("polymer上传") {
//
//            }
//        }

    }

    private myfuction(it){
        return {
            this.context.jenkins.echo "执行任务"
        }

    }
    GuojhProcess(Context context) {
        super(context)
    }

    @Override
    def careProjects() {
        return null
    }
}
