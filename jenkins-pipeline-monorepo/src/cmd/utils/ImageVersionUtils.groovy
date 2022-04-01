package cmd.utils

/**
 * 处理镜像版本号相关工具
 */
class ImageVersionUtils {

    /**
     * 镜像名称获取版本号
     *
     * @param image
     * @return
     */
    static String imageToTag(String image) {
        def strs = image.split("\\.")
        String version = "${strs[strs.length - 3]}.${strs[strs.length - 2]}.${strs[strs.length - 1]}"

        return version
    }
}
