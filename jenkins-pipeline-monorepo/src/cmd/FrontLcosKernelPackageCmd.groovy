package cmd

import entity.ProjectItemConfig

/**
 * subapp 类型项目打包
 */
class FrontLcosKernelPackageCmd extends AbstractCmd<Void> {
    
    private ProjectItemConfig projectItemConfig
	
	private boolean publish;

    FrontLcosKernelPackageCmd(context) {
        super(context);
    }

    @Override
    AbstractCmd execute() {
        this.context.jenkins.dir(this.projectItemConfig.buildDir) {
			if (publish) {
				this.context.jenkins.sh """
                	yarn publish
				"""
			} else {
				this.context.jenkins.sh """
                	yarn && yarn run build && yarn run task
				"""
			}
            
        }
        return this
    }

    @Override
    Void getResult() {
        return null
    }
    
    FrontLcosKernelPackageCmd setProjectItemConfig(ProjectItemConfig projectItemConfig) {
        this.projectItemConfig = projectItemConfig
        return this
    }
	
	FrontLcosKernelPackageCmd setPublish(boolean publish) {
		this.publish = publish;
		return this;
	}

}
