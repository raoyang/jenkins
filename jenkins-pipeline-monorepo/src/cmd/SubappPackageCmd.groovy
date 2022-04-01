package cmd

import entity.ProjectItemConfig
import groovy.json.JsonOutput

/**
 * subapp 类型项目打包
 */
class SubappPackageCmd extends AbstractCmd<Void> {

    private List<ProjectItemConfig> subappList
    private String appsName
    private String appsDir
    private String credentialId
    private String ossEndpoint
    private String resourcesUpload
    private String resourcePath

    SubappPackageCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("subappList", this.subappList)
        checkEmpty("appsName", this.appsName)
        checkEmpty("appsDir", this.appsDir)
        checkEmpty("credentialId", this.credentialId)
        checkEmpty("ossEndpoint", this.ossEndpoint)
        checkEmpty("resourcesUpload", this.resourcesUpload)
        checkEmpty("resourcePath", this.resourcePath)

        for (ProjectItemConfig subApp : this.subappList) {
            String assetsAppName = buildSubapp(subApp)
            copyDist(assetsAppName, subApp)
        }
        String buildVersion = createVersion()
        generateManifestFiles(this.appsDir, buildVersion)
        uploadDist(this.appsDir, buildVersion, this.credentialId, this.ossEndpoint, this.resourcesUpload, this.resourcePath)
        return this
    }

    @Override
    Void getResult() {
        return null
    }

    private String buildSubapp(ProjectItemConfig subApp) {
        String buildDir = subApp.getBuildDir()
        String assetsAppName
        this.context.jenkins.dir("${buildDir}") {
            this.context.jenkins.sh """
                if [ -f "build/env-init.sh" ];then
                    echo "-->Source has env-init.sh, running it..."
                    chmod a+x ./build/env-init.sh
                    . ./build/env-init.sh
                fi
                pnpm run install:packages
                pnpm run install:lcos-kernel
                pnpm run build
            """
            try {
                def json = this.context.jenkins.readJSON file: "package.json"
                String appName = json["eros"]["appName"]
                if (assetsAppName) {
                    assetsAppName = appName
                } else {
                    assetsAppName = json["eros"]["assetsPublicPath"]
                }
            } catch (Exception e) {
                this.context.jenkins.error "eros['assetsPublicPath'] not defined"
            }
        }
        return "${assetsAppName}"
    }

    private void copyDist(String assetsAppName, ProjectItemConfig subApp) {
        String buildDir = subApp.getBuildDir()
        this.context.jenkins.echo "${assetsAppName}"
        this.context.jenkins.dir("${buildDir}") {
            this.context.jenkins.sh """
                cd ..
                mkdir -p dist/${assetsAppName} 
            """
        }
        this.context.jenkins.dir("${buildDir}") {
            this.context.jenkins.sh """
                cp -rf dist/* ../dist/${assetsAppName}
            """
        }
    }

    private String generateManifestFiles(String appsDir, String buildVersion) {
        this.context.jenkins.dir("${appsDir}/dist") {
            def manifestJson = generateManifestJson()
            //生成main-manifest文件
            def manifestString = JsonOutput.toJson(manifestJson)
            def manifest = "window.__UG_POLYMER_VERSION__ = \"v${buildVersion}\";window.__UG_SUBAPPS_MANIFEST__ = ${manifestString};"
            this.context.jenkins.writeFile encoding: 'UTF-8', file: 'main/manifest.js', text: "${manifest}"
            // 多写一份到根目录（？）
            this.context.jenkins.writeFile encoding: 'UTF-8', file: './manifest.js', text: "${manifest}"
            //修改index.html文件
            String fixedManifest = manifest.replace("/", "\\/")
            this.context.jenkins.sh "sed -i 's/{{__SUBAPPS_MANIFEST__}}/${fixedManifest}/g' main/index.html"
        }
    }

    private def generateManifestJson() {
        //读配置文件
        def manifestJson = [:]
        def files = this.context.jenkins.findFiles(glob: '**/subapp_manifest.json')
        for (def file : files) {
            if (!"${file.path}".contains("main")) {
                //生成subapp-manifest
                def subJson = [:]
                String key = "${file.path}".replace("/subapp_manifest.json", "")
                subJson["name"] = "${key}"
                subJson["entry"] = "#${key}"
                subJson["manifest"] = this.context.jenkins.readJSON file: "${file.path}"
                manifestJson["${key}"] = subJson
            }
        }
        return manifestJson
    }

    private String uploadDist(String appsDir, String buildVersion, String credentialId, String ossEndpoint, String resourcesUpload, String resourcePath) {
        String uploadDir = "v${buildVersion}"
        String webStaticDir = "${resourcePath}/${uploadDir}"
        this.context.jenkins.dir("${appsDir}") {
            this.context.jenkins.sh """
                mkdir ${uploadDir}
                cp -rf dist/* ${uploadDir}
            """
            new OssUploadCmd(this.context).setCredentialId(credentialId)
                    .setOssEndpoint(ossEndpoint)
                    .setResourcesUpload(resourcesUpload)
                    .setFileDir(uploadDir)
                    .setCloudDir(webStaticDir)
                    .setIsRecursive(true)
                    .execute()
        }
        this.context.jenkins.dir("${appsDir}/${uploadDir}") {
            this.context.jenkins.deleteDir()
        }
    }

    private String createVersion(){
        String gitCommit = this.context.jenkins.env.GIT_COMMIT
        String shortHash = this.context.jenkins.sh(script: "git rev-parse --short ${gitCommit}", returnStdout: true).trim()
        String buildVersion = "${shortHash}-" + new Date().format('yyMMddHHmm') + "-${this.context.jenkins.env.BUILD_ID}"
        return buildVersion
    }

    List<ProjectItemConfig> getSubappList() {
        return subappList
    }

    SubappPackageCmd setSubappList(List<ProjectItemConfig> subappList) {
        this.subappList = subappList
        return this
    }

    String getAppsName() {
        return appsName
    }

    SubappPackageCmd setAppsName(String appsName) {
        this.appsName = appsName
        return this
    }

    String getAppsDir() {
        return appsDir
    }

    SubappPackageCmd setAppsDir(String appsDir) {
        this.appsDir = appsDir
        return this
    }

    String getCredentialId() {
        return credentialId
    }

    SubappPackageCmd setCredentialId(String credentialId) {
        this.credentialId = credentialId
        return this
    }

    String getOssEndpoint() {
        return ossEndpoint
    }

    SubappPackageCmd setOssEndpoint(String ossEndpoint) {
        this.ossEndpoint = ossEndpoint
        return this
    }

    String getResourcesUpload() {
        return resourcesUpload
    }

    SubappPackageCmd setResourcesUpload(String resourcesUpload) {
        this.resourcesUpload = resourcesUpload
        return this
    }

    String getResourcePath() {
        return resourcePath
    }

    SubappPackageCmd setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath
        return this
    }
}
