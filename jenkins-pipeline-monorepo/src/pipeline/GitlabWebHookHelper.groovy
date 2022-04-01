package pipeline

import cmd.AddGitHookCmd
import cmd.GetAllGitHookCmd
import cmd.GetGlobalTokenCmd
import entity.Context
import entity.GitHook

class GitlabWebHookHelper {
    static def handleMultibranchWebHookForBranch(Context context) {
        boolean forceCheckWebHookSetting = context.jenkins.params.forceCheckGitWebHookSetting

        // 流水线首次添加, 或者命令行强制要求检测
        if (forceCheckWebHookSetting || context.getFromVersion() == null || context.getFromVersion().isEmpty()) {
            // 非多分支流水线，不处理
            if (context.jenkins.env.BRANCH_NAME == null || context.jenkins.env.BRANCH_NAME.isEmpty()) {
                context.jenkins.echo "非多分支流水线，handleMultibranchWebHookForBranch不执行"
                return
            }

            String token = new GetGlobalTokenCmd(context)
                    .setTokenId(context.globalConfig.gitlabApiTokenId)
                    .execute()
                    .getResult()
            List<GitHook> gitHookList = new GetAllGitHookCmd(context)
                    .setGitUrl(context.globalConfig.gitlabUrl)
                    .setProjectPath(context.globalConfig.gitlabProjectPath)
                    .setToken(token)
                    .execute().getResult()

            String gitHookUrl = "${context.globalConfig.gitlabJenkinsMultibranchHookBaseUrl}/${context.branchName}"
            for (GitHook gitHook: gitHookList) {
                if (gitHookUrl.equalsIgnoreCase(gitHook.getUrl())) {
                    context.jenkins.echo "${gitHookUrl}已经在GITLAB中存在"
                    return
                }
            }

            GitHook gitHook = new GitHook()
                    .setUrl(gitHookUrl)
                    .setPushEvents(true)
                    .setPushEventsBranchFilter(context.branchName)
            new AddGitHookCmd(context).setGitUrl(context.globalConfig.gitlabUrl)
                    .setProjectPath(context.globalConfig.gitlabProjectPath)
                    .setAccessToken(token)
                    .setSecretToken(context.globalConfig.gitlabJenkinsMultibranchHookSecret)
                    .setGitHook(gitHook)
                    .execute()
                    .getResult()
            context.jenkins.echo "${gitHookUrl}添加至GITLAB成功"
        }
    }

}
