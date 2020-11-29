package wxd.qst.mall.controller.mall;

import wxd.qst.mall.common.Constants;
import wxd.qst.mall.common.QstMallOrderStatusEnum;
import wxd.qst.mall.common.ServiceResultEnum;
import wxd.qst.mall.controller.vo.QstMallOrderDetailVO;
import wxd.qst.mall.controller.vo.QstMallShoppingCartItemVO;
import wxd.qst.mall.controller.vo.QstMallUserVO;
import wxd.qst.mall.entity.QstMallOrder;
import wxd.qst.mall.service.QstMallOrderService;
import wxd.qst.mall.service.QstMallShoppingCartService;
import wxd.qst.mall.util.PageQueryUtil;
import wxd.qst.mall.util.Result;
import wxd.qst.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @Resource
    private QstMallShoppingCartService qstMallShoppingCartService;
    @Resource
    private QstMallOrderService qstMallOrderService;

    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        QstMallOrderDetailVO orderDetailVO = qstMallOrderService.getOrderDetailByOrderNo(orderNo, user.getUserId());
        if (orderDetailVO == null) {
            System.out.println("orders");
            return "error/error_5xx";
        }
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    @GetMapping("/orders")
    public String orderListPage(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        params.put("userId", user.getUserId());
        if (StringUtils.isEmpty(params.get("page"))) {
            params.put("page", 1);
        }
        params.put("limit", Constants.ORDER_SEARCH_PAGE_LIMIT);
        //封装我的订单数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        request.setAttribute("orderPageResult", qstMallOrderService.getMyOrders(pageUtil));
        request.setAttribute("path", "orders");
        return "mall/my-orders";
    }

    @GetMapping("/saveOrder")
    public String saveOrder(@RequestParam("cartItemIds") String itemIds, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<Long> cartItemIds= Arrays.asList(itemIds.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        List<QstMallShoppingCartItemVO> mySettleShoppingCartItems = qstMallShoppingCartService.getMySettleShoppingCartItems(cartItemIds);
        if (StringUtils.isEmpty(user.getAddress().trim())) {
            //无收货地址
            return "error/error_5xx";
        }
        if (CollectionUtils.isEmpty(mySettleShoppingCartItems)) {
            //购物车中无数据则跳转至错误页
            return "error/error_5xx";
        } else {
            //保存订单并返回订单号
            String saveOrderResult = qstMallOrderService.saveOrder(user, mySettleShoppingCartItems);
            System.out.println(saveOrderResult);
            if (ServiceResultEnum.ORDER_PRICE_ERROR.getResult().equals(saveOrderResult)
                    || ServiceResultEnum.DB_ERROR.getResult().equals(saveOrderResult)
                    || ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult().equals(saveOrderResult)
                    || ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult().equals(saveOrderResult)) {
                //订单生成失败
                System.out.println("生成失败");
                return "error/error_5xx";
            }
            //跳转到订单详情页
            return "redirect:/orders/" + saveOrderResult;
        }
    }

    @PutMapping("/orders/{orderNo}/cancel")
    @ResponseBody
    public Result cancelOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String cancelOrderResult = qstMallOrderService.cancelOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }

    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public Result finishOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String finishOrderResult = qstMallOrderService.finishOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }

    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        QstMallOrder qstMallOrder = qstMallOrderService.getQstMallOrderByOrderNo(orderNo);
        //判断订单userId
        if (qstMallOrder.getUserId().longValue()!=user.getUserId().longValue()){
            return "error/error_5xx";
        }
        //判断订单状态
        if (QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()!= qstMallOrder.getOrderStatus()){
            return "error/error_5xx";
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", qstMallOrder.getTotalPrice());
        return "mall/pay-select";
    }

    @GetMapping("/payPage")
    public String payOrder(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession, @RequestParam("payType") int payType) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        QstMallOrder qstMallOrder = qstMallOrderService.getQstMallOrderByOrderNo(orderNo);
        //判断订单userId
        if (qstMallOrder.getUserId().longValue()!=user.getUserId().longValue()){
            return "error/error_5xx";
        }
        //判断订单状态
        if (QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()!= qstMallOrder.getOrderStatus()){
            return "error/error_5xx";
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", qstMallOrder.getTotalPrice());
        if (payType == 1) {
            return "mall/alipay";
        } else {
            return "mall/wxpay";
        }
    }

    @GetMapping("/paySuccess")
    @ResponseBody
    public Result paySuccess(@RequestParam("orderNo") String orderNo, @RequestParam("payType") int payType) {
        String payResult = qstMallOrderService.paySuccess(orderNo, payType);
        if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(payResult);
        }
    }

}
