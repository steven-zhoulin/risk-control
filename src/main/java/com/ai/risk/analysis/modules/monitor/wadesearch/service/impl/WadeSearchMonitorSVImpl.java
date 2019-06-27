package com.ai.risk.analysis.modules.monitor.wadesearch.service.impl;

import com.ai.risk.analysis.modules.monitor.wadesearch.entity.SearchStat;
import com.ai.risk.analysis.modules.monitor.wadesearch.entity.Single;
import com.ai.risk.analysis.modules.monitor.wadesearch.service.IWadeSearchMonitorSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 */
@Service
@Slf4j
public class WadeSearchMonitorSVImpl implements IWadeSearchMonitorSV {

    @Autowired
    private InfluxDB influxDB;

    @Value("#{'${hubble.wadesearch.address}'.split(',')}")
    private List<String> address;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void sinkToInflux() {

        long currTime = System.currentTimeMillis();

        for (String addr : address) {
            String url = "http://" + addr;
            // log.info("采集搜索引擎数据 {} ", url);

	        ResponseEntity<String> responseEntity = null;

            try {
	            responseEntity = restTemplate.getForEntity(url, String.class);
            } catch (Exception e) {
            	log.info("实例: {} IS DOWN", addr);
	            writeToTrace("", addr, 0, 0, "", "DOWN", currTime);
	            continue;
            }

            String httpBodyContent = responseEntity.getBody();
            SearchStat searchStat = parse(httpBodyContent);

            String bootTime = searchStat.getBootTime();
            List<Single> infos = searchStat.getSearchinfo();
            for (Single info : infos) {
            	writeToTrace(info.getSearchCode(), addr, info.getCnt(), info.getTtc(), bootTime, "UP", currTime);
            }

        }

        log.info("采集搜索监控数据，耗时(ms): {}", (System.currentTimeMillis() - currTime));

    }

    private SearchStat parse(String httpBodyContent) {

    	if (StringUtils.isBlank(httpBodyContent)) {
    		// 实例访问不到的情况
		    SearchStat searchStat = new SearchStat();
		    return searchStat;
	    }

        Map<String, String> data = new HashMap();
        for (String line : StringUtils.split(httpBodyContent, '^')) {
            String[] part = StringUtils.split(line, "=");
            data.put(part[0], part[1]);
        }

        SearchStat searchStat = new SearchStat();
        searchStat.setBootTime(data.get("BOOT_TIME"));
        data.remove("BOOT_TIME");

        List<Single> searchinfo = new ArrayList<>();

        for (String key : data.keySet()) {

            String[] part = StringUtils.split(data.get(key), ',');
            long cnt = Long.parseLong(part[0]);
            long ttc = Long.parseLong(part[1]);
            double avg = ((long) (ttc * 1.0 / cnt * 1000)) * 1.0 / 1000;

            Single single = new Single();
            single.setSearchCode(key);
            single.setCnt(cnt);
            single.setTtc(ttc);
            single.setAvg(avg);

            searchinfo.add(single);

        }

        searchStat.setSearchinfo(searchinfo);
        return searchStat;
    }

    /**
     *
     * @param searchCode  搜索编码
     * @param searchCount 搜索次数
     * @param totalCost 搜索总耗时 (ms)
     * @param currentTimeMillis
     */
    private void writeToTrace(String searchCode, String instance, long searchCount, long totalCost, String bootTime, String status, long currentTimeMillis) {
        Point point = Point.measurement("wadesearch")
                .tag("search_code", searchCode)
	            .tag("instance", instance)
                .addField("search_count", searchCount)
                .addField("search_total_cost", totalCost)
                .addField("boot_time", bootTime)
	            .addField("status", status)
                .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                .build();

        influxDB.write("hubble", "autogen", point);
    }

}
