package com.alice1530.mcloud.model.filelist;

import com.alice1530.mcloud.model.CommonAccountInfo;
import com.alice1530.mcloud.model.Page;

public class FileListRequest extends Page {
    private String catalogID;
    private Integer catalogSortType = 0;
    private Integer contentSortType = 0;
    private CommonAccountInfo commonAccountInfo;

    public String getCatalogID() {
        return catalogID;
    }

    public void setCatalogID(String catalogID) {
        this.catalogID = catalogID;
    }

    public Integer getCatalogSortType() {
        return catalogSortType;
    }

    public void setCatalogSortType(Integer catalogSortType) {
        this.catalogSortType = catalogSortType;
    }

    public CommonAccountInfo getCommonAccountInfo() {
        return commonAccountInfo;
    }

    public void setCommonAccountInfo(CommonAccountInfo commonAccountInfo) {
        this.commonAccountInfo = commonAccountInfo;
    }

    public Integer getContentSortType() {
        return contentSortType;
    }

    public void setContentSortType(Integer contentSortType) {
        this.contentSortType = contentSortType;
    }
}
