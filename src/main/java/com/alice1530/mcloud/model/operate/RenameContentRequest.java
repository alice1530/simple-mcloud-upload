
package com.alice1530.mcloud.model.operate;

import com.alice1530.mcloud.model.CommonAccountInfo;

public class RenameContentRequest {


    private String ContentID;

    private String ContentName;

    private CommonAccountInfo commonAccountInfo;

    public String getContentID() {
        return ContentID;
    }

    public void setContentID(String contentID) {
        ContentID = contentID;
    }

    public String getContentName() {
        return ContentName;
    }

    public void setContentName(String contentName) {
        ContentName = contentName;
    }

    public CommonAccountInfo getCommonAccountInfo() {
        return commonAccountInfo;
    }

    public void setCommonAccountInfo(CommonAccountInfo commonAccountInfo) {
        this.commonAccountInfo = commonAccountInfo;
    }

}
