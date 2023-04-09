package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.FieldValueDataTypeEnum;
import com.hn.onelabel.adapter.api.enums.RuleRelationalOperatorEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveRuleConditionInfoRequest {
    /**
     * @see RuleRelationalOperatorEnum
     */
    private String relationalOperator;
    private String fieldName;
    /**
     * @see FieldValueDataTypeEnum
     */
    private String fieldValueDataType;
    private String fieldValue;

    public boolean isValid() {
        Objects.requireNonNull(RuleRelationalOperatorEnum.getByName(this.relationalOperator), "Error relationalOperator.");
        Objects.requireNonNull(this.fieldName, "Null fieldName.");
        Objects.requireNonNull(FieldValueDataTypeEnum.getByName(this.fieldValueDataType), "Error fieldValueDataType.");
        Objects.requireNonNull(this.fieldValue, "Null fieldValue.");
        Assert.isTrue(RuleRelationalOperatorEnum.isCollectionRelationalOperator(this.relationalOperator) == FieldValueDataTypeEnum.isCollectionDataType(this.fieldValueDataType), "Mismatch relationalOperator and fieldValueDataType.");
        return true;
    }
}
