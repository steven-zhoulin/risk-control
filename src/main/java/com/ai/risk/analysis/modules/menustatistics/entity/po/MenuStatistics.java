package com.ai.risk.analysis.modules.menustatistics.entity.po;

import com.ai.risk.analysis.framework.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.beans.ConstructorProperties;
import java.time.LocalDateTime;


@TableName("MENU_STATISTICS")
public class MenuStatistics extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField("MENU_ID")
    private String menuId;

    @TableField("OP_CODE")
    private String opCode;

    @TableField("SERVICE_NAME")
    private String serviceName;

    @TableField("ACCESS_NUM")
    private String accessNum;

    @TableField("START_TIME")
    private LocalDateTime startTime;

    @TableField("SUCCESS")
    private String success;

    @TableField("HOSTNAME")
    private String hostname;

    @TableField("REQ_BODY1")
    private String reqBody1;

    @TableField("REQ_BODY2")
    private String reqBody2;

    @TableField("REQ_BODY3")
    private String reqBody3;

    @TableField("GROUP_ID")
    private String groupId;

    @TableField("PSPT_NR")
    private String psptNr;

    public String getMenuId()
    {
        return this.menuId;
    }
    public String getOpCode() {
        return this.opCode;
    }
    public String getServiceName() {
        return this.serviceName;
    }
    public String getAccessNum() {
        return this.accessNum;
    }
    public LocalDateTime getStartTime() {
        return this.startTime;
    }
    public String getSuccess() {
        return this.success;
    }
    public String getHostname() {
        return this.hostname;
    }
    public String getReqBody1() {
        return this.reqBody1;
    }
    public String getReqBody2() {
        return this.reqBody2;
    }
    public String getReqBody3() {
        return this.reqBody3;
    }
    public String getGroupId() {
        return this.groupId;
    }
    public String getPsptNr() {
        return this.psptNr;
    }

    public MenuStatistics setMenuId(String menuId)
    {
        this.menuId = menuId; return this; }
    public MenuStatistics setOpCode(String opCode) { this.opCode = opCode; return this; }
    public MenuStatistics setServiceName(String serviceName) { this.serviceName = serviceName; return this; }
    public MenuStatistics setAccessNum(String accessNum) { this.accessNum = accessNum; return this; }
    public MenuStatistics setStartTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
    public MenuStatistics setSuccess(String success) { this.success = success; return this; }
    public MenuStatistics setHostname(String hostname) { this.hostname = hostname; return this; }
    public MenuStatistics setReqBody1(String reqBody1) { this.reqBody1 = reqBody1; return this; }
    public MenuStatistics setReqBody2(String reqBody2) { this.reqBody2 = reqBody2; return this; }
    public MenuStatistics setReqBody3(String reqBody3) { this.reqBody3 = reqBody3; return this; }
    public MenuStatistics setGroupId(String groupId) { this.groupId = groupId; return this; }
    public MenuStatistics setPsptNr(String psptNr) { this.psptNr = psptNr; return this; }
    public String toString() { return "MenuStatistics(menuId=" + getMenuId() + ", opCode=" + getOpCode() + ", serviceName=" + getServiceName() + ", accessNum=" + getAccessNum() + ", startTime=" + getStartTime() + ", success=" + getSuccess() + ", hostname=" + getHostname() + ", reqBody1=" + getReqBody1() + ", reqBody2=" + getReqBody2() + ", reqBody3=" + getReqBody3() + ", groupId=" + getGroupId() + ", psptNr=" + getPsptNr() + ")"; }
    public boolean equals(Object o) { if (o == this) return true; if (!(o instanceof MenuStatistics)) return false; MenuStatistics other = (MenuStatistics)o; if (!other.canEqual(this)) return false; if (!super.equals(o)) return false; Object this$menuId = getMenuId(); Object other$menuId = other.getMenuId(); if (this$menuId == null ? other$menuId != null : !this$menuId.equals(other$menuId)) return false; Object this$opCode = getOpCode(); Object other$opCode = other.getOpCode(); if (this$opCode == null ? other$opCode != null : !this$opCode.equals(other$opCode)) return false; Object this$serviceName = getServiceName(); Object other$serviceName = other.getServiceName(); if (this$serviceName == null ? other$serviceName != null : !this$serviceName.equals(other$serviceName)) return false; Object this$accessNum = getAccessNum(); Object other$accessNum = other.getAccessNum(); if (this$accessNum == null ? other$accessNum != null : !this$accessNum.equals(other$accessNum)) return false; Object this$startTime = getStartTime(); Object other$startTime = other.getStartTime(); if (this$startTime == null ? other$startTime != null : !this$startTime.equals(other$startTime)) return false; Object this$success = getSuccess(); Object other$success = other.getSuccess(); if (this$success == null ? other$success != null : !this$success.equals(other$success)) return false; Object this$hostname = getHostname(); Object other$hostname = other.getHostname(); if (this$hostname == null ? other$hostname != null : !this$hostname.equals(other$hostname)) return false; Object this$reqBody1 = getReqBody1(); Object other$reqBody1 = other.getReqBody1(); if (this$reqBody1 == null ? other$reqBody1 != null : !this$reqBody1.equals(other$reqBody1)) return false; Object this$reqBody2 = getReqBody2(); Object other$reqBody2 = other.getReqBody2(); if (this$reqBody2 == null ? other$reqBody2 != null : !this$reqBody2.equals(other$reqBody2)) return false; Object this$reqBody3 = getReqBody3(); Object other$reqBody3 = other.getReqBody3(); if (this$reqBody3 == null ? other$reqBody3 != null : !this$reqBody3.equals(other$reqBody3)) return false; Object this$groupId = getGroupId(); Object other$groupId = other.getGroupId(); if (this$groupId == null ? other$groupId != null : !this$groupId.equals(other$groupId)) return false; Object this$psptNr = getPsptNr(); Object other$psptNr = other.getPsptNr(); return this$psptNr == null ? other$psptNr == null : this$psptNr.equals(other$psptNr); }
    protected boolean canEqual(Object other) { return other instanceof MenuStatistics; }
    public int hashCode() { int PRIME = 59; int result = 1; result = result * 59 + super.hashCode(); Object $menuId = getMenuId(); result = result * 59 + ($menuId == null ? 0 : $menuId.hashCode()); Object $opCode = getOpCode(); result = result * 59 + ($opCode == null ? 0 : $opCode.hashCode()); Object $serviceName = getServiceName(); result = result * 59 + ($serviceName == null ? 0 : $serviceName.hashCode()); Object $accessNum = getAccessNum(); result = result * 59 + ($accessNum == null ? 0 : $accessNum.hashCode()); Object $startTime = getStartTime(); result = result * 59 + ($startTime == null ? 0 : $startTime.hashCode()); Object $success = getSuccess(); result = result * 59 + ($success == null ? 0 : $success.hashCode()); Object $hostname = getHostname(); result = result * 59 + ($hostname == null ? 0 : $hostname.hashCode()); Object $reqBody1 = getReqBody1(); result = result * 59 + ($reqBody1 == null ? 0 : $reqBody1.hashCode()); Object $reqBody2 = getReqBody2(); result = result * 59 + ($reqBody2 == null ? 0 : $reqBody2.hashCode()); Object $reqBody3 = getReqBody3(); result = result * 59 + ($reqBody3 == null ? 0 : $reqBody3.hashCode()); Object $groupId = getGroupId(); result = result * 59 + ($groupId == null ? 0 : $groupId.hashCode()); Object $psptNr = getPsptNr(); result = result * 59 + ($psptNr == null ? 0 : $psptNr.hashCode()); return result; }
    public MenuStatistics() {  }
    @ConstructorProperties({"menuId", "opCode", "serviceName", "accessNum", "startTime", "success", "hostname", "reqBody1", "reqBody2", "reqBody3", "groupId", "psptNr"})
    public MenuStatistics(String menuId, String opCode, String serviceName, String accessNum, LocalDateTime startTime, String success, String hostname, String reqBody1, String reqBody2, String reqBody3, String groupId, String psptNr) { this.menuId = menuId; this.opCode = opCode; this.serviceName = serviceName; this.accessNum = accessNum; this.startTime = startTime; this.success = success; this.hostname = hostname; this.reqBody1 = reqBody1; this.reqBody2 = reqBody2; this.reqBody3 = reqBody3; this.groupId = groupId; this.psptNr = psptNr;
    }
}
