package com.ai.risk.analysis.modules.menustatistics.controller;

import com.ai.risk.analysis.modules.menustatistics.service.IMenuStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/menustatistics/menu-statistics"})
public class MenuStatisticsController {
    private static final Logger log = LoggerFactory.getLogger(MenuStatisticsController.class);

    @Autowired
    private IMenuStatisticsService menuStatisticsServiceImpl;
}
