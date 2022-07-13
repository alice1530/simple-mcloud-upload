
package com.alice1530.mcloud.model.download.result;

import com.alice1530.mcloud.model.Result;

public class DownloadData {

    private String downloadURL;
    private Result result;

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

}
