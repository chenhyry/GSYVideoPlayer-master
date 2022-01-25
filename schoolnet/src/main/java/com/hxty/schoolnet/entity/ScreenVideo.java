package com.hxty.schoolnet.entity;

import java.util.List;

/**
 * Created by chen on 2017/3/27.
 */

public class ScreenVideo {

    private int total;

    private List<ScreenVideoRow> rows;

    public class ScreenVideoRow {
        private int SchoolId;
        private int KeyId;
        private int State;
        private String LinkUrl;
        private String VideoTitle;
        private String Remark;

        public int getSchoolId() {
            return SchoolId;
        }

        public void setSchoolId(int schoolId) {
            SchoolId = schoolId;
        }

        public int getKeyId() {
            return KeyId;
        }

        public void setKeyId(int keyId) {
            KeyId = keyId;
        }

        public int getState() {
            return State;
        }

        public void setState(int state) {
            State = state;
        }

        public String getLinkUrl() {
            return LinkUrl;
        }

        public void setLinkUrl(String linkUrl) {
            LinkUrl = linkUrl;
        }

        public String getVideoTitle() {
            return VideoTitle;
        }

        public void setVideoTitle(String videoTitle) {
            VideoTitle = videoTitle;
        }

        public String getRemark() {
            return Remark;
        }

        public void setRemark(String remark) {
            Remark = remark;
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ScreenVideoRow> getRows() {
        return rows;
    }

    public void setRows(List<ScreenVideoRow> rows) {
        this.rows = rows;
    }
}
