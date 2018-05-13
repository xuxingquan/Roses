package com.stylefeng.roses.account.modular.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.roses.account.modular.consumer.MessageServiceConsumer;
import com.stylefeng.roses.account.modular.mapper.FlowRecordMapper;
import com.stylefeng.roses.account.modular.service.IFlowRecordService;
import com.stylefeng.roses.api.account.model.FlowRecord;
import com.stylefeng.roses.api.common.exception.CoreExceptionEnum;
import com.stylefeng.roses.api.common.exception.ServiceException;
import com.stylefeng.roses.api.order.GoodsFlowParam;
import com.stylefeng.roses.core.util.ToolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 流水记录 服务实现类
 * </p>
 *
 * @author stylefeng123
 * @since 2018-05-05
 */
@Service
public class FlowRecordServiceImpl extends ServiceImpl<FlowRecordMapper, FlowRecord> implements IFlowRecordService {

    @Autowired
    private MessageServiceConsumer messageServiceConsumer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordFlow(GoodsFlowParam goodsFlowParam) {

        if (goodsFlowParam == null) {
            throw new ServiceException(CoreExceptionEnum.REQUEST_NULL);
        }

        if (ToolUtil.isOneEmpty(goodsFlowParam.getUserId(), goodsFlowParam.getGoodsName(), goodsFlowParam.getSum())) {
            throw new ServiceException(CoreExceptionEnum.REQUEST_NULL);
        }

        //幂等判断
        EntityWrapper<FlowRecord> wrapper = new EntityWrapper<>();
        wrapper.eq("order_id", goodsFlowParam.getId());
        List<FlowRecord> flowRecords = this.selectList(wrapper);
        if (flowRecords != null && !flowRecords.isEmpty()) {
            return;
        }

        FlowRecord flowRecord = new FlowRecord();
        flowRecord.setUserId(goodsFlowParam.getUserId());
        flowRecord.setSum(goodsFlowParam.getSum());
        flowRecord.setOrderId(goodsFlowParam.getId());
        flowRecord.setName(goodsFlowParam.getGoodsName());
        flowRecord.setCreateTime(new Date());

        this.insert(flowRecord);

        //插入成功后要删除消息
        messageServiceConsumer.deleteMessageByBizId(flowRecord.getOrderId());
    }
}
