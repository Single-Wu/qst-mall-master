package wxd.qst.mall.controller.mall;

import wxd.qst.mall.common.Constants;
import wxd.qst.mall.common.ServiceResultEnum;
import wxd.qst.mall.controller.vo.QstMallShoppingCartItemVO;
import wxd.qst.mall.controller.vo.QstMallUserVO;
import wxd.qst.mall.entity.QstMallShoppingCartItem;
import wxd.qst.mall.service.QstMallShoppingCartService;
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
import java.util.stream.Collectors;

@Controller
public class ShoppingCartController {

    @Resource
    private QstMallShoppingCartService qstMallShoppingCartService;

    @GetMapping("/shop-cart")
    public String cartListPage(HttpServletRequest request,HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        int itemsTotal = 0;
        int priceTotal = 0;
        List<QstMallShoppingCartItemVO> myShoppingCartItems = qstMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        /* if (!CollectionUtils.isEmpty(myShoppingCartItems)) {
            //订单项总数
            itemsTotal = myShoppingCartItems.stream().mapToInt(NewBeeMallShoppingCartItemVO::getGoodsCount).sum();
            if (itemsTotal < 1) {
                return "error/error_5xx";
            }
            //总价
            for (NewBeeMallShoppingCartItemVO newBeeMallShoppingCartItemVO : myShoppingCartItems) {
                priceTotal += newBeeMallShoppingCartItemVO.getGoodsCount() * newBeeMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                return "error/error_5xx";
            }
        }
        request.setAttribute("itemsTotal", itemsTotal);
        request.setAttribute("priceTotal", priceTotal);*/
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/cart";
    }

    @PostMapping("/shop-cart")
    @ResponseBody
    public Result saveQstMallShoppingCartItem(@RequestBody QstMallShoppingCartItem qstMallShoppingCartItem,
                                                 HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        qstMallShoppingCartItem.setUserId(user.getUserId());
        //todo 判断数量
        String saveResult = qstMallShoppingCartService.saveQstMallCartItem(qstMallShoppingCartItem);
        //添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult(saveResult);
    }

    @PutMapping("/shop-cart")
    @ResponseBody
    public Result updateQstMallShoppingCartItem(@RequestBody QstMallShoppingCartItem qstMallShoppingCartItem,
                                                   HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        qstMallShoppingCartItem.setUserId(user.getUserId());
        //todo 判断数量
        String saveResult = qstMallShoppingCartService.updateQstMallCartItem(qstMallShoppingCartItem);
        //修改成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult(saveResult);
    }

    @DeleteMapping("/shop-cart/{qstMallShoppingCartItemId}")
    @ResponseBody
    public Result updateQstMallShoppingCartItem(@PathVariable("qstMallShoppingCartItemId") Long qstMallShoppingCartItemId) {
        Boolean deleteResult = qstMallShoppingCartService.deleteById(qstMallShoppingCartItemId);
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    @DeleteMapping("/shop-cart")
    @ResponseBody
    public Result updateQstMallShoppingCartItem(HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Boolean deleteResult = qstMallShoppingCartService.deleteByUserId(user.getUserId());
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    /*@GetMapping("/shop-cart/settle")
    public String settlePage(HttpServletRequest request,HttpSession httpSession) {
        int priceTotal = 0;
        NewBeeMallUserVO user = (NewBeeMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<NewBeeMallShoppingCartItemVO> myShoppingCartItems = newBeeMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        if (CollectionUtils.isEmpty(myShoppingCartItems)) {
            //无数据则不跳转至结算页
            return "/shop-cart";
        } else {
            //总价
            for (NewBeeMallShoppingCartItemVO newBeeMallShoppingCartItemVO : myShoppingCartItems) {
                priceTotal += newBeeMallShoppingCartItemVO.getGoodsCount() * newBeeMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                return "error/error_5xx";
            }
        }
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/order-settle";
    }*/

    @GetMapping("/shop-cart/settle")
    public String settlePage(@RequestParam("cartItemIds")String itemIds,  HttpServletRequest request) {
        int itemsTotal = 0;
        int priceTotal = 0;
        if (StringUtils.isEmpty(itemIds)) {
            //无数据则不跳转至结算页
            return "redirect:/shop-cart";
        }
        List<Long> cartItemIds= Arrays.asList(itemIds.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        List<QstMallShoppingCartItemVO> mySettleShoppingCartItems = qstMallShoppingCartService.getMySettleShoppingCartItems(cartItemIds);
        if (!CollectionUtils.isEmpty(mySettleShoppingCartItems)) {
            //订单项总数
            itemsTotal = mySettleShoppingCartItems.stream().mapToInt(QstMallShoppingCartItemVO::getGoodsCount).sum();
            if (itemsTotal < 1) {
                System.out.println("shop-carts");
                return "error/error_5xx";
            }
            //总价
            for (QstMallShoppingCartItemVO qstMallShoppingCartItemVO : mySettleShoppingCartItems) {

                if(qstMallShoppingCartItemVO.getPromotionStatus()==null){
                    priceTotal += qstMallShoppingCartItemVO.getGoodsCount() * qstMallShoppingCartItemVO.getOriginalPrice();
                }else if(qstMallShoppingCartItemVO.getPromotionStatus()==Constants.PROMOTION_STATUS_STARTED){
                    priceTotal += qstMallShoppingCartItemVO.getGoodsCount() * qstMallShoppingCartItemVO.getSellingPrice();
                }
            }
            if (priceTotal < 1) {
                return "error/error_5xx";
            }
        }else{
            return "redirect:/shop-cart";
        }
        request.setAttribute("itemsTotal",itemsTotal);
        request.setAttribute("priceTotal",priceTotal);
        request.setAttribute("mySettleShoppingCartItems", mySettleShoppingCartItems);
        return "mall/order-settle";
    }
}
