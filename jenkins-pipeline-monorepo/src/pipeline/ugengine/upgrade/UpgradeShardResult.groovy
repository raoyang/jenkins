package pipeline.ugengine.upgrade

class UpgradeShardResult {
    UpgradeShardResult() {
    }

    String getOrigin_image_url() {
        return origin_image_url
    }

    void setOrigin_image_url(String origin_image_url) {
        this.origin_image_url = origin_image_url
    }
    String origin_image_url

    String getShard_key() {
        return shard_key
    }

    void setShard_key(String shard_key) {
        this.shard_key = shard_key
    }
    String shard_key

    String getRelease_result() {
        return release_result
    }

    void setRelease_result(String release_result) {
        this.release_result = release_result
    }
    String release_result

}
