package cmd

import entity.ProjectItemConfig
import groovy.json.JsonOutput

/**
 * subapp 类型项目打包
 */
class FrontPolymerPackageCmd extends AbstractCmd<Void> {

    ProjectItemConfig projectItemConfig
    private String buildVersion

    FrontPolymerPackageCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        generateManifestFiles(this.projectItemConfig.buildDir, this.buildVersion)
        return this
    }

    @Override
    Void getResult() {
        return null
    }

    private String generateManifestFiles(String appsDir, String buildVersion) {
        this.context.jenkins.dir("${projectItemConfig.buildDir}/dist") {
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

    FrontPolymerPackageCmd setProjectItemConfig(ProjectItemConfig projectItemConfig) {
        this.projectItemConfig = projectItemConfig
        return this
    }

    FrontPolymerPackageCmd setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion
        return this
    }
}
