package com.ai.risk.analysis.modules.menustatistics.service.impl;

import com.ai.risk.analysis.modules.menustatistics.entity.po.MenuStatistics;
import com.ai.risk.analysis.modules.menustatistics.service.IMenuSV;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MenuSVImpl extends MenuStatisticsServiceImpl implements IMenuSV {
    public void insertMenuInfo(Map map)
    {
        MenuStatistics menuStatistics = new MenuStatistics();
        menuStatistics.setMenuId((String)map.get("MENU_ID"));
        menuStatistics.setOpCode((String)map.get("OP_CODE"));
        menuStatistics.setServiceName((String)map.get("SERVICE_NAME"));
        menuStatistics.setAccessNum((String)map.get("ACCESS_NUM"));
        menuStatistics.setStartTime((LocalDateTime)map.get("START_TIME"));
        menuStatistics.setSuccess((String)map.get("SUCCESS"));
        menuStatistics.setHostname((String)map.get("HOSTNAME"));
        menuStatistics.setReqBody1((String)map.get("REQ_BODY1"));
        menuStatistics.setReqBody2((String)map.get("REQ_BODY2"));
        menuStatistics.setReqBody3((String)map.get("REQ_BODY3"));
        menuStatistics.setGroupId((String)map.get("GROUP_ID"));
        menuStatistics.setPsptNr((String)map.get("PSPT_NR"));
        save(menuStatistics);
    }
}
