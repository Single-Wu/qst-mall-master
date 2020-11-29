package wxd.qst.mall.controller.mall;

import wxd.qst.mall.common.Constants;
import wxd.qst.mall.common.IndexConfigTypeEnum;
import wxd.qst.mall.controller.vo.QstMallIndexCarouselVO;
import wxd.qst.mall.controller.vo.QstMallIndexCategoryVO;
import wxd.qst.mall.controller.vo.QstMallIndexConfigGoodsVO;
import wxd.qst.mall.service.QstMallCarouselService;
import wxd.qst.mall.service.QstMallCategoryService;
import wxd.qst.mall.service.QstMallIndexConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {

    @Resource
    private QstMallCarouselService qstMallCarouselService;

    @Resource
    private QstMallIndexConfigService qstMallIndexConfigService;

    @Resource
    private QstMallCategoryService qstMallCategoryService;

    @GetMapping({"/index", "/", "/index.html"})
    public String indexPage(HttpServletRequest request) {
        List<QstMallIndexCategoryVO> categories = qstMallCategoryService.getCategoriesForIndex();
        if (CollectionUtils.isEmpty(categories)) {
            return "error/error_5xx";
        }
        List<QstMallIndexCarouselVO> carousels = qstMallCarouselService.getCarouselsForIndex(Constants.INDEX_CAROUSEL_NUMBER);
        List<QstMallIndexConfigGoodsVO> hotGoodses = qstMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_HOT.getType(), Constants.INDEX_GOODS_HOT_NUMBER);
        List<QstMallIndexConfigGoodsVO> newGoodses = qstMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_NEW.getType(), Constants.INDEX_GOODS_NEW_NUMBER);
        List<QstMallIndexConfigGoodsVO> recommendGoodses = qstMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_RECOMMOND.getType(), Constants.INDEX_GOODS_RECOMMOND_NUMBER);
        request.setAttribute("categories", categories);//分类数据
        request.setAttribute("carousels", carousels);//轮播图
        request.setAttribute("hotGoodses", hotGoodses);//热销商品
        request.setAttribute("newGoodses", newGoodses);//新品
        request.setAttribute("recommendGoodses", recommendGoodses);//推荐商品
        return "mall/index";
    }
}
