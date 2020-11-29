package wxd.qst.mall.dao;

import wxd.qst.mall.entity.QstMallOrder;
import wxd.qst.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QstMallOrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(QstMallOrder record);

    int insertSelective(QstMallOrder record);

    QstMallOrder selectByPrimaryKey(Long orderId);

    QstMallOrder selectByOrderNo(String orderNo);

    int updateByPrimaryKeySelective(QstMallOrder record);

    int updateByPrimaryKey(QstMallOrder record);

    List<QstMallOrder> findQstMallOrderList(PageQueryUtil pageUtil);

    int getTotalQstMallOrders(PageQueryUtil pageUtil);

    List<QstMallOrder> selectByPrimaryKeys(@Param("orderIds") List<Long> orderIds);

    int checkOut(@Param("orderIds") List<Long> orderIds);

    int closeOrder(@Param("orderIds") List<Long> orderIds, @Param("orderStatus") int orderStatus);

    int checkDone(@Param("orderIds") List<Long> asList);
}