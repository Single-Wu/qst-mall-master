package wxd.qst.mall.controller.mall;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import wxd.qst.mall.common.Constants;
import wxd.qst.mall.controller.vo.PromotionItemVO;
import wxd.qst.mall.service.QstMallPromotionService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by wuxd on 2019/12/5.
 */
@Controller
public class PromotionController {
    @Resource
    private QstMallPromotionService qstMallPromotionService;
    @GetMapping("/promotions")
    public String promotionIndex(HttpServletRequest request){
        List<PromotionItemVO> promotionItemVOS = qstMallPromotionService.getActivatedPromotions(Constants.PROMOTION_GOODS_NUMBER);
        request.setAttribute("promotionListVOS",promotionItemVOS);
        return "mall/promotions";
    }

}
