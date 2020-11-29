package wxd.qst.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import wxd.qst.mall.common.PayStatusEnum;
import wxd.qst.mall.common.PayTypeEnum;
import wxd.qst.mall.common.QstMallOrderStatusEnum;
import wxd.qst.mall.common.ServiceResultEnum;
import wxd.qst.mall.controller.vo.*;
import wxd.qst.mall.dao.QstMallGoodsMapper;
import wxd.qst.mall.dao.QstMallOrderItemMapper;
import wxd.qst.mall.dao.QstMallOrderMapper;
import wxd.qst.mall.dao.QstMallShoppingCartItemMapper;
import wxd.qst.mall.entity.QstMallOrder;
import wxd.qst.mall.entity.QstMallOrderItem;
import wxd.qst.mall.service.QstMallOrderService;
import wxd.qst.mall.service.QstMallPromotionService;
import wxd.qst.mall.util.BeanUtil;
import wxd.qst.mall.util.PageQueryUtil;
import wxd.qst.mall.util.PageResult;

import javax.validation.constraints.AssertFalse;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static wxd.qst.mall.util.NumberUtil.genOrderNo;

@Service
public class QstMallOrderServiceImpl implements QstMallOrderService {

    @Autowired
    private QstMallOrderMapper qstMallOrderMapper;
    @Autowired
    private QstMallOrderItemMapper qstMallOrderItemMapper;
    @Autowired
    private QstMallShoppingCartItemMapper qstMallShoppingCartItemMapper;
    @Autowired
    private QstMallGoodsMapper qstMallGoodsMapper;
    @Autowired
    private QstMallPromotionService qstMallPromotionService;

    @Override
    public PageResult getQstMallOrdersPage(PageQueryUtil pageUtil) {
        List<QstMallOrder> qstMallOrders = qstMallOrderMapper.findQstMallOrderList(pageUtil);
        int total = qstMallOrderMapper.getTotalQstMallOrders(pageUtil);
        PageResult pageResult = new PageResult(qstMallOrders, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    @Transactional
    public String updateOrderInfo(QstMallOrder qstMallOrder) {
        QstMallOrder temp = qstMallOrderMapper.selectByPrimaryKey(qstMallOrder.getOrderId());
        //不为空且orderStatus>=0且状态为出库之前可以修改部分信息
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(qstMallOrder.getTotalPrice());
            temp.setUserAddress(qstMallOrder.getUserAddress());
            temp.setUpdateTime(new Date());
            if (qstMallOrderMapper.updateByPrimaryKeySelective(temp) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<QstMallOrder> orders = qstMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (QstMallOrder qstMallOrder : orders) {
                if (qstMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                    continue;
                }
                if (qstMallOrder.getOrderStatus() != 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (qstMallOrderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<QstMallOrder> orders = qstMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (QstMallOrder qstMallOrder : orders) {
                if (qstMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                    continue;
                }
                if (qstMallOrder.getOrderStatus() != 1 && qstMallOrder.getOrderStatus() != 2) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (qstMallOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<QstMallOrder> orders = qstMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (QstMallOrder qstMallOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (qstMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                    continue;
                }
                //已关闭或者已完成无法关闭订单
                if (qstMallOrder.getOrderStatus() == 4 || qstMallOrder.getOrderStatus() < 0) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                if (qstMallOrderMapper.closeOrder(Arrays.asList(ids), QstMallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String saveOrder(QstMallUserVO user, List<QstMallShoppingCartItemVO> mySettleShoppingCartItems) {
        System.out.println(user);
        QstMallOrder record = new QstMallOrder();
        Integer totalPrice = 0;
        for(int item=0;item<mySettleShoppingCartItems.size();item++){
            QstMallShoppingCartItemVO good =mySettleShoppingCartItems.get(item);
            QstMallOrderItem recordItem = new QstMallOrderItem();
            if(good.getSellingPrice() != null) {
                totalPrice += good.getSellingPrice();
                recordItem.setSellingPrice(good.getSellingPrice());
            }
            else {
                totalPrice += good.getOriginalPrice();
                recordItem.setSellingPrice(good.getOriginalPrice());
            }
            Long id = new Long(15);
            recordItem.setOrderId(id);
            recordItem.setGoodsId(good.getGoodsId());
            recordItem.setGoodsName(good.getGoodsName());
            recordItem.setGoodsCoverImg(good.getGoodsCoverImg());
            recordItem.setGoodsCount(good.getGoodsCount());
            recordItem.setCreateTime(new Date());
            qstMallOrderItemMapper.insert(recordItem);
            System.out.println(good);
        }
        record.setUserId(user.getUserId());
        record.setUserAddress(user.getAddress());
        record.setTotalPrice(totalPrice);
        String CurrentOrderNO = genOrderNo();
        record.setOrderNo(CurrentOrderNO);
        record.setCreateTime(new Date());
        record.setPayStatus((byte)0);
        record.setOrderStatus((byte)0);
        record.setPayType((byte)0);
        record.setExtraInfo("");
        record.setIsDeleted((byte)0);
        record.setUpdateTime(new Date());
        qstMallOrderMapper.insert(record);
        return CurrentOrderNO;
    }

    @Override
    public QstMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        System.out.println(orderNo+"     "+String.valueOf(userId));
        if (qstMallOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (userId.longValue()!= qstMallOrder.getUserId().longValue()){
                return null;
            }
            System.out.println(qstMallOrder.getOrderId());
            List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderId(qstMallOrder.getOrderId());
            //获取订单项数据
            System.out.println(orderItems);
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<QstMallOrderItemVO> qstMallOrderItemVOS = BeanUtil.copyList(orderItems, QstMallOrderItemVO.class);
                QstMallOrderDetailVO qstMallOrderDetailVO = new QstMallOrderDetailVO();
                BeanUtil.copyProperties(qstMallOrder, qstMallOrderDetailVO);
                qstMallOrderDetailVO.setOrderStatusString(QstMallOrderStatusEnum.getQstMallOrderStatusEnumByStatus(qstMallOrderDetailVO.getOrderStatus()).getName());
                qstMallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(qstMallOrderDetailVO.getPayType()).getName());
                qstMallOrderDetailVO.setQstMallOrderItemVOS(qstMallOrderItemVOS);
                return qstMallOrderDetailVO;
            }
        }
        return null;
    }

    @Override
    public QstMallOrder getQstMallOrderByOrderNo(String orderNo) {
        return qstMallOrderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = qstMallOrderMapper.getTotalQstMallOrders(pageUtil);
        List<QstMallOrder> qstMallOrders = qstMallOrderMapper.findQstMallOrderList(pageUtil);
        List<QstMallOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            //数据转换 将实体类转成vo
            orderListVOS = BeanUtil.copyList(qstMallOrders, QstMallOrderListVO.class);
            //设置订单状态中文显示值
            for (QstMallOrderListVO qstMallOrderListVO : orderListVOS) {
                qstMallOrderListVO.setOrderStatusString(QstMallOrderStatusEnum.getQstMallOrderStatusEnumByStatus(qstMallOrderListVO.getOrderStatus()).getName());
                qstMallOrderListVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(qstMallOrderListVO.getPayType()).getName());
            }
            List<Long> orderIds = qstMallOrders.stream().map(QstMallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<QstMallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(QstMallOrderItem::getOrderId));
                for (QstMallOrderListVO qstMallOrderListVO : orderListVOS) {
                    //封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(qstMallOrderListVO.getOrderId())) {
                        List<QstMallOrderItem> orderItemListTemp = itemByOrderIdMap.get(qstMallOrderListVO.getOrderId());
                        //将QstMallOrderItem对象列表转换成QstMallOrderItemVO对象列表
                        List<QstMallOrderItemVO> qstMallOrderItemVOS = BeanUtil.copyList(orderItemListTemp, QstMallOrderItemVO.class);
                        qstMallOrderListVO.setQstMallOrderItemVOS(qstMallOrderItemVOS);
                    }
                }
            }
        }
        PageResult pageResult = new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (userId.longValue()!= qstMallOrder.getUserId().longValue()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            //订单状态判断
            if (qstMallOrder.getOrderStatus()<(byte) QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                    || qstMallOrder.getOrderStatus()>(byte) QstMallOrderStatusEnum.OREDER_EXPRESS.getOrderStatus()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (qstMallOrderMapper.closeOrder(Collections.singletonList(qstMallOrder.getOrderId()), QstMallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //todo 验证是否是当前userId下的订单，否则报错
            //todo 订单状态判断
            qstMallOrder.setOrderStatus((byte) QstMallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            qstMallOrder.setUpdateTime(new Date());
            if (qstMallOrderMapper.updateByPrimaryKeySelective(qstMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //订单状态判断 非待支付状态下不进行修改操作
            if((byte) QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()!= qstMallOrder.getOrderStatus()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            qstMallOrder.setOrderStatus((byte) QstMallOrderStatusEnum.OREDER_PAID.getOrderStatus());
            qstMallOrder.setPayType((byte) payType);
            qstMallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            qstMallOrder.setPayTime(new Date());
            qstMallOrder.setUpdateTime(new Date());
            if (qstMallOrderMapper.updateByPrimaryKeySelective(qstMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public List<QstMallOrderItemVO> getOrderItems(Long id) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByPrimaryKey(id);
        if (qstMallOrder != null) {
            List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderId(qstMallOrder.getOrderId());
            //获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<QstMallOrderItemVO> qstMallOrderItemVOS = BeanUtil.copyList(orderItems, QstMallOrderItemVO.class);
                return qstMallOrderItemVOS;
            }
        }
        return null;
    }
}
