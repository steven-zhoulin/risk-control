package com.ai.risk.analysis.modules.warning.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.ai.risk.analysis.framework.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 
 * </p>
 *
 * @author Steven
 * @since 2019-06-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("WARNING")
public class Warning extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("NAME")
    private String name;

    @TableField("CNT")
    private Long cnt;

    @TableField("IS_WARNING")
    private String isWarning;

    @TableField("CREATE_DATE")
    private LocalDateTime createDate;

    @TableField("WARNING_TYPE")
    private String warningType;

}
