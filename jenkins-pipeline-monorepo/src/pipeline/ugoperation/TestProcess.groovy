package pipeline.ugoperation

import entity.Context
import pipeline.BaseProcess

class TestProcess extends BaseProcess{


    TestProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return null
    }

    @Override
    protected void execute() {

    }
}
