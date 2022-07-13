
package com.alice1530.mcloud.model.filelist.result;

import com.alice1530.mcloud.model.Result;

public class FileListData {

     
    private GetDiskResult getDiskResult;
     
    private Result result;

    public GetDiskResult getGetDiskResult() {
        return getDiskResult;
    }

    public void setGetDiskResult(GetDiskResult getDiskResult) {
        this.getDiskResult = getDiskResult;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

}
