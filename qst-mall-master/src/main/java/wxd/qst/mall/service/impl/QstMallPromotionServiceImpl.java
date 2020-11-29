package wxd.qst.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import wxd.qst.mall.common.ServiceResultEnum;
import wxd.qst.mall.controller.vo.PromotionItemVO;
import wxd.qst.mall.dao.QstMallGoodsMapper;
import wxd.qst.mall.dao.QstMallPromotionMapper;
import wxd.qst.mall.entity.Promotion;
import wxd.qst.mall.entity.QstMallGoods;
import wxd.qst.mall.entity.QstMallOrder;
import wxd.qst.mall.service.QstMallPromotionService;
import wxd.qst.mall.util.BeanUtil;
import wxd.qst.mall.util.DateUtil;
import wxd.qst.mall.util.PageQueryUtil;
import wxd.qst.mall.util.PageResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QstMallPromotionServiceImpl implements QstMallPromotionService {
    @Autowired
    QstMallPromotionMapper qstMallPromotionMapper;
    @Autowired
    private QstMallGoodsMapper qstMallGoodsMapper;

    @Override
    public PageResult getQstMallPromotionPage(PageQueryUtil pageUtil) {
        List<Promotion> promotions = qstMallPromotionMapper.findQstMallPromotionList(pageUtil);
        int total = qstMallPromotionMapper.getTotalQstMallPromotions(pageUtil);
        PageResult pageResult = new PageResult(promotions, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }


    @Override
    public List<PromotionItemVO> getActivatedPromotions(int number) {
        List<PromotionItemVO> promotionItemVOS=new ArrayList<>(number);
        //找到前20条促销活动
        List<Promotion> promotions=qstMallPromotionMapper.findPromotions();
        if(!CollectionUtils.isEmpty(promotions)){
            Date now=new Date();
            for (Promotion promotion:promotions) {
                if (now.after(promotion.getStartTime())&&now.before(promotion.getEndTime())){
                    PromotionItemVO promotionItemVO=new PromotionItemVO();
                    BeanUtil.copyProperties(promotion,promotionItemVO);
                    promotionItemVOS.add(promotionItemVO);
                    if(promotionItemVOS.size()==number){
                        break;
                    }
                }
            }
        }
        if(promotionItemVOS.size()>0){
            //获得所有促销商品ID
            List<Long> goodsIds = promotionItemVOS.stream().map(PromotionItemVO::getGoodsId).collect(Collectors.toList());
            //获得所有促销商品
            List<QstMallGoods> qstMallGoods = qstMallGoodsMapper.selectByPrimaryKeys(goodsIds);
            Map<Long, QstMallGoods> newBeeMallGoodsMap = qstMallGoods.stream().collect(Collectors.toMap(QstMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            //判断商品库存
            for (PromotionItemVO promotionItemVO : promotionItemVOS) {
                QstMallGoods tempGood= newBeeMallGoodsMap.get(promotionItemVO.getGoodsId());
                promotionItemVO.setGoodsName(tempGood.getGoodsName());
                promotionItemVO.setOriginalPrice(tempGood.getOriginalPrice());
                promotionItemVO.setGoodsCoverImg(tempGood.getGoodsCoverImg());
            }
        }
        return promotionItemVOS;
    }

    @Override
    public Promotion getPromotionsByGoodsId(Long goodsId) {
        Promotion promotion=qstMallPromotionMapper.selectByGoodsId(goodsId);
        if (promotion!=null){
            Date startTime=promotion.getStartTime();
            if(!DateUtil.isBetween24H(startTime)){
                return null;
            }
        }
        return promotion;
    }

    @Override
    public Promotion getActivatedPromotion(Long goodsId) {
        Promotion promotion=this.getPromotionsByGoodsId(goodsId);
        if(promotion!=null){
            Date now=new Date();
            if (now.after(promotion.getStartTime())&&now.before(promotion.getEndTime())){
                return promotion;
            }
        }

        return null;
    }

    @Override
    public String saveQstMallPromotion(Promotion promotion) {
        if (qstMallPromotionMapper.insertSelective(promotion) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }
}
