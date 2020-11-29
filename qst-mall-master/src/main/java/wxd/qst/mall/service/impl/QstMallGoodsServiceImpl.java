package wxd.qst.mall.service.impl;

import wxd.qst.mall.common.Constants;
import wxd.qst.mall.common.ServiceResultEnum;
import wxd.qst.mall.controller.vo.QstMallSearchGoodsVO;
import wxd.qst.mall.dao.QstMallGoodsMapper;
import wxd.qst.mall.entity.QstMallGoods;
import wxd.qst.mall.entity.Promotion;
import wxd.qst.mall.service.QstMallGoodsService;
import wxd.qst.mall.service.QstMallPromotionService;
import wxd.qst.mall.util.BeanUtil;
import wxd.qst.mall.util.PageQueryUtil;
import wxd.qst.mall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class QstMallGoodsServiceImpl implements QstMallGoodsService {

    @Autowired
    private QstMallGoodsMapper goodsMapper;
    @Autowired
    QstMallPromotionService qstMallPromotionService;

    @Override
    public PageResult getQstMallGoodsPage(PageQueryUtil pageUtil) {
        List<QstMallGoods> goodsList = goodsMapper.findQstMallGoodsList(pageUtil);
        int total = goodsMapper.getTotalQstMallGoods(pageUtil);
        PageResult pageResult = new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String saveQstMallGoods(QstMallGoods goods) {
        if (goodsMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveQstMallGoods(List<QstMallGoods> qstMallGoodsList) {
        if (!CollectionUtils.isEmpty(qstMallGoodsList)) {
            goodsMapper.batchInsert(qstMallGoodsList);
        }
    }

    @Override
    public String updateQstMallGoods(QstMallGoods goods) {
        QstMallGoods temp = goodsMapper.selectByPrimaryKey(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        goods.setUpdateTime(new Date());
        if (goodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public QstMallGoods getQstMallGoodsById(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }
    
    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return goodsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    @Override
    public PageResult searchQstMallGoods(PageQueryUtil pageUtil) {
        List<QstMallGoods> goodsList = goodsMapper.findQstMallGoodsListBySearch(pageUtil);

        int total = goodsMapper.getTotalQstMallGoodsBySearch(pageUtil);
        List<QstMallSearchGoodsVO> qstMallSearchGoodsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            qstMallSearchGoodsVOS = BeanUtil.copyList(goodsList, QstMallSearchGoodsVO.class);
            for (QstMallSearchGoodsVO qstMallSearchGoodsVO : qstMallSearchGoodsVOS) {
                Promotion promotion= qstMallPromotionService.getActivatedPromotion(qstMallSearchGoodsVO.getGoodsId());
                if(promotion!=null){
                    qstMallSearchGoodsVO.setSellingPrice(promotion.getPromotionPrice());
                    qstMallSearchGoodsVO.setPromotionStatus(Constants.PROMOTION_STATUS_STARTED);
                }
                String goodsName = qstMallSearchGoodsVO.getGoodsName();
                String goodsIntro = qstMallSearchGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    qstMallSearchGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    qstMallSearchGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        PageResult pageResult = new PageResult(qstMallSearchGoodsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }
}
