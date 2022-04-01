package pipeline.ugbasic.entity

import entity.ProjectItemConfig

/**
 * @author lijq* @date 2022/3/25
 */
class ProjectItemDeployConfig {

    private ProjectItemConfig itemConfig

    private String deployEnv

    private String deployNamespace

    private String image

    ProjectItemDeployConfig(ProjectItemConfig itemConfig, String deployEnv, String deployNamespace) {
        this.itemConfig = itemConfig
        this.deployEnv = deployEnv
        this.deployNamespace = deployNamespace
    }

    ProjectItemConfig getItemConfig() {
        return itemConfig
    }

    String getDeployEnv() {
        return deployEnv
    }

    String getDeployNamespace() {
        return deployNamespace
    }

    String getImage() {
        return image
    }

    void setImage(String image) {
        this.image = image
    }
}
