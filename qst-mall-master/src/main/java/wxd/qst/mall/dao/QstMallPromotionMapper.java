package wxd.qst.mall.dao;

import wxd.qst.mall.entity.Promotion;
import wxd.qst.mall.entity.QstMallOrder;
import wxd.qst.mall.util.PageQueryUtil;

import java.util.List;

public interface QstMallPromotionMapper {

    List<Promotion> findQstMallPromotionList(PageQueryUtil pageUtil);

    int getTotalQstMallPromotions(PageQueryUtil pageUtil);

    Promotion selectByPrimaryKey(Long promotionId);

    Promotion selectByGoodsId(Long goodsId);

    int deleteByPrimaryKey(Long promotionId);

    int insertSelective (Promotion promotion);

    int updateByPrimaryKeySelective(Promotion promotion);

    List<Promotion> findPromotions();
}
