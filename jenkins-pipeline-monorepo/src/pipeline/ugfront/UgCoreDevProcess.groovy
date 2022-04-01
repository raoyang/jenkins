package pipeline.ugfront

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import cmd.FrontAppPackageCmd
import cmd.FrontLcosKernelPackageCmd
import cmd.FrontPolymerPackageCmd
import cmd.OssUploadCmd
import entity.Context
import entity.ProjectItemConfig
import pipeline.BaseProcess

class UgCoreDevProcess extends UgCoreBaseProcess {

    UgCoreDevProcess(Context context) {
        super(context)
    }

    protected String getCloudDir(String module, String version) {
        String dir = "";
        switch(module) {
            case "pc":
                dir = "web-static-dev/ug-pc-app-monorepo-test";
                break;
            case "mobile":
                dir = "web-static-dev/ug-mobile-app-monorepo-test";
                break;
            case "external":
                dir = "web-static-dev/ug-external-app-monorepo-test";
                break;
            default:
                dir = "web-static-dev/ug-pc-app-monorepo-test";
        }
        return dir + "/v" + version;
    }

    protected void doVerification(){
        // without Verification
    }
}
