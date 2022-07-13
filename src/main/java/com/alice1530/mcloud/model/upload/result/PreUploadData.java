
package com.alice1530.mcloud.model.upload.result;

import com.alice1530.mcloud.model.Result;

public class PreUploadData {

    
    private Result result;
    
    private UploadResult uploadResult;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public UploadResult getUploadResult() {
        return uploadResult;
    }

    public void setUploadResult(UploadResult uploadResult) {
        this.uploadResult = uploadResult;
    }

}
