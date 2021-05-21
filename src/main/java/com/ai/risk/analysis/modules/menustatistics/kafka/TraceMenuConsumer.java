package com.ai.risk.analysis.modules.menustatistics.kafka;

import com.ai.risk.analysis.modules.menustatistics.service.IMenuSV;
import com.ai.risk.analysis.modules.warning.util.SpanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.stereotype.Component;

@Component
public class TraceMenuConsumer {
    private static final Logger log = LoggerFactory.getLogger(TraceMenuConsumer.class);
    private static final String TOPIC = "LOG4X-TRACE-TOPIC";

    @Autowired
    private IMenuSV menuSVImpl;
    private static final String CALLTYPE_MENU = "MENU_ID";
    private static final String CALLTYPE_MENU_NEW = "&MENU_ID=";
    private static final String CALLTYPE_IDEN = "IDEN_NR";
    private static final String CALLTYPE_GROUP = "GROUP_ID";
    private ObjectMapper mapper = new ObjectMapper();

    private void accumulation(ConsumerRecord<?, String> record, int partition)
    {
        String value = (String)record.value();
        if (value.indexOf("MENU_ID") != -1)
            try {
                Map span = (Map)this.mapper.readValue(value, Map.class);
                String menuId = "";
                if (value.indexOf("&MENU_ID=") != -1) {
                    int start = value.indexOf("&MENU_ID=") + "&MENU_ID=".length();
                    int end = value.indexOf("&", start);
                    if (end == -1) {
                        end = value.indexOf("\"", start);
                    }
                    menuId = value.substring(start, end);
                } else {
                    int start = value.indexOf("MENU_ID") + "MENU_ID".length();
                    int end = value.indexOf(",", start);
                    menuId = value.substring(start, end);
                }
                if (menuId.length() > 40)
                {
                    menuId = "";
                }
                if ((menuId != null) && (menuId != "")) {
                    Map param = new HashMap();
                    menuId = resEx(menuId).trim();
                    if ((menuId != null) && (menuId != "") && (menuId.length() > 0)) {
                        if (value.indexOf("IDEN_NR") != -1) {
                            int start = value.indexOf("IDEN_NR") + "IDEN_NR".length();
                            int end = value.indexOf(",", start);
                            String idenNr = value.substring(start, end);
                            idenNr = resEx(idenNr).trim();
                            if (idenNr.length() > 100) {
                                idenNr = idenNr.substring(0, 100);
                            }
                            param.put("PSPT_NR", idenNr);
                        }

                        if (value.indexOf("GROUP_ID") != -1) {
                            int start = value.indexOf("GROUP_ID") + "GROUP_ID".length();
                            int end = value.indexOf(",", start);
                            String groupId = value.substring(start, end);
                            groupId = resEx(groupId).trim();
                            if (groupId.length() > 30) {
                                groupId = groupId.substring(0, 30);
                            }
                            param.put("GROUP_ID", groupId);
                        }

                        String serviceName = (String)span.get("serviceName");
                        String sn = (String)span.get("sn");
                        Long startTime = (Long)span.get("startTime");
                        String hostName = SpanUtil.getHostIp((String)span.get("hostName"));
                        Map rpc = (Map)span.get("rpc");
                        String reqBody = "";
                        if (rpc != null) {
                            reqBody = (String)rpc.get("reqBody");
                        }
                        boolean success = ((Boolean)span.get("success")).booleanValue();
                        String opCode = (String)span.get("opCode");
                        if (StringUtils.isEmpty(opCode)){
                            Map extMap = (Map)span.get("ext");
                            if (!Objects.isNull(extMap)){
                                opCode = (String)extMap.get("opCode");
                            }
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmdd");
                        Date date = new Date(startTime.longValue());
                        String startDate = sdf.format(date);
                        param.put("MENU_ID", menuId);
                        param.put("OP_CODE", opCode);
                        param.put("SERVICE_NAME", serviceName);
                        sn = resEx(sn);
                        if (sn.length() > 20) {
                            sn = "";
                        }
                        param.put("ACCESS_NUM", sn);
                        if ((startDate != null) && (startDate != "") && (startDate.length() >= 14)) {
                            int year = Integer.valueOf(startDate.substring(0, 4)).intValue();
                            Month month = Month.of(Integer.valueOf(startDate.substring(4, 6)).intValue());
                            int day = Integer.valueOf(startDate.substring(6, 8)).intValue();
                            int hour = Integer.valueOf(startDate.substring(8, 10)).intValue();
                            int minute = Integer.valueOf(startDate.substring(10, 12)).intValue();
                            int second = Integer.valueOf(startDate.substring(12, 14)).intValue();
                            param.put("START_TIME", LocalDateTime.of(year, month, day, hour, minute, second));
                        }

                        param.put("SUCCESS", String.valueOf(success));
                        param.put("HOSTNAME", hostName);
                        if ((reqBody != null) && (reqBody != "") && (reqBody.length() > 0)) {
                            int reqLength = reqBody.length();
                            if (reqLength > 2000) {
                                param.put("REQ_BODY1", reqBody.substring(0, 2000));
                                if (reqLength > 4000) {
                                    param.put("REQ_BODY2", reqBody.substring(2000, 4000));
                                    if (reqLength > 6000)
                                        param.put("REQ_BODY3", reqBody.substring(4000, 6000));
                                    else
                                        param.put("REQ_BODY3", reqBody.substring(4000, reqLength));
                                }
                                else {
                                    param.put("REQ_BODY2", reqBody.substring(2000, reqLength));
                                }
                            } else {
                                param.put("REQ_BODY1", reqBody);
                            }
                        }

                        this.menuSVImpl.insertMenuInfo(param);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private String resEx(String str)
    {
        String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String result = m.replaceAll("").trim();

        result = result.replace("\"", "").trim();
        result = result.replace("\\", "").trim();
        result = result.replace("rnt", "").trim();
        return result;
    }

    @KafkaListeners({@KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"0"})}), @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"1"})})})
    public void listenMenu1(ConsumerRecord<?, String> record)
    {
        accumulation(record, 1);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"2"})})
    public void listenMenu2(ConsumerRecord<?, String> record) {
        accumulation(record, 2);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"3"})})
    public void listenMenu3(ConsumerRecord<?, String> record) {
        accumulation(record, 3);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"4"})})
    public void listenMenu4(ConsumerRecord<?, String> record) {
        accumulation(record, 4);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"5"})})
    public void listenMenu5(ConsumerRecord<?, String> record) {
        accumulation(record, 5);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"6"})})
    public void listenMenu6(ConsumerRecord<?, String> record) {
        accumulation(record, 6);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"7"})})
    public void listenMenu7(ConsumerRecord<?, String> record) {
        accumulation(record, 7);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"8"})})
    public void listenMenu8(ConsumerRecord<?, String> record) {
        accumulation(record, 8);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"9"})})
    public void listenMenu9(ConsumerRecord<?, String> record) {
        accumulation(record, 9);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"10"})})
    public void listenMenu10(ConsumerRecord<?, String> record) {
        accumulation(record, 10);
    }

    @KafkaListener(topicPartitions={@org.springframework.kafka.annotation.TopicPartition(topic="LOG4X-TRACE-TOPIC", partitions={"11"})})
    public void listenMenu11(ConsumerRecord<?, String> record) {
        accumulation(record, 11);
    }
}
