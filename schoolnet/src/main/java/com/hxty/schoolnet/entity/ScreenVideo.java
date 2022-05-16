package com.hxty.schoolnet.entity;


/**
 * Created by chen on 2017/3/27.
 */

public class ScreenVideo {

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
