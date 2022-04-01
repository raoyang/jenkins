package cmd.utils

import entity.Context

/**
 * 操作Git相关工具
 */
class GitUtils {

    static void pullGitRepository(Context context
            , String gitUrl
            , String gitBranch) {
        context.jenkins.sh """
            git init
            git config remote.origin.url ${gitUrl}
            git pull origin ${gitBranch}
        """
    }

    static void pushGitRepository(Context context
            , String gitBranch
            , String commitMsg) {
        if (context.jenkins.sh(script: "git status -s", returnStdout: true).trim()) {
            context.jenkins.sh """
                git add .
                git commit -m "${commitMsg}"
                git pull origin ${gitBranch}
                git push -u origin HEAD:${gitBranch}
                git status
            """
        }
    }

    static String fixPath(String path) {
        String result = path.replace('/', '%2f')
        return result
    }

}
