package wxd.qst.mall.dao;

import wxd.qst.mall.entity.QstMallOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QstMallOrderItemMapper {
    int deleteByPrimaryKey(Long orderItemId);

    int insert(QstMallOrderItem record);

    int insertSelective(QstMallOrderItem record);

    QstMallOrderItem selectByPrimaryKey(Long orderItemId);

    /**
     * 根据订单id获取订单项列表
     *
     * @param orderId
     * @return
     */
    List<QstMallOrderItem> selectByOrderId(Long orderId);

    /**
     * 根据订单ids获取订单项列表
     *
     * @param orderIds
     * @return
     */
    List<QstMallOrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 批量insert订单项数据
     *
     * @param orderItems
     * @return
     */
    int insertBatch(@Param("orderItems") List<QstMallOrderItem> orderItems);

    int updateByPrimaryKeySelective(QstMallOrderItem record);

    int updateByPrimaryKey(QstMallOrderItem record);
}