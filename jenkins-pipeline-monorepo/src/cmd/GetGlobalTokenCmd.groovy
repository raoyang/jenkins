package cmd

import entity.Context

class GetGlobalTokenCmd extends AbstractCmd<String> {

    private String tokenId
    private String token

    GetGlobalTokenCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("tokenId", tokenId)

        this.context.jenkins.withCredentials([this.context.jenkins.string(credentialsId: "${this.tokenId}", variable: 'TOKEN')]) {
            this.token = this.context.jenkins.env.TOKEN
        }
        return this
    }

    String getTokenId() {
        return tokenId
    }

    GetGlobalTokenCmd setTokenId(String tokenId) {
        this.tokenId = tokenId
        return this
    }

    @Override
    String getResult() {
        return this.token
    }
}

