package com.ai.risk.analysis.modules.menustatistics.service.impl;

import com.ai.risk.analysis.modules.menustatistics.entity.po.MenuStatistics;
import com.ai.risk.analysis.modules.menustatistics.mapper.MenuStatisticsMapper;
import com.ai.risk.analysis.modules.menustatistics.service.IMenuStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MenuStatisticsServiceImpl extends ServiceImpl<MenuStatisticsMapper, MenuStatistics> implements IMenuStatisticsService {
    private static final Logger log = LoggerFactory.getLogger(MenuStatisticsServiceImpl.class);
}
