package wxd.qst.mall.service;

import wxd.qst.mall.controller.vo.QstMallIndexCarouselVO;
import wxd.qst.mall.entity.Carousel;
import wxd.qst.mall.util.PageQueryUtil;
import wxd.qst.mall.util.PageResult;

import java.util.List;

public interface QstMallCarouselService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getCarouselPage(PageQueryUtil pageUtil);

    String saveCarousel(Carousel carousel,Integer user);

    String updateCarousel(Carousel carousel,Integer user);

    Carousel getCarouselById(Integer id);

    Boolean deleteBatch(Integer[] ids);

    /**
     * 返回固定数量的轮播图对象(首页调用)
     *
     * @param number
     * @return
     */
    List<QstMallIndexCarouselVO> getCarouselsForIndex(int number);
}
