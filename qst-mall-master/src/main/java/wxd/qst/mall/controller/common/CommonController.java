package wxd.qst.mall.controller.common;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import wxd.qst.mall.common.Constants;
import wxd.qst.mall.common.ServiceResultEnum;
import wxd.qst.mall.util.Result;
import wxd.qst.mall.util.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;
import java.util.Random;

@Controller
public class CommonController {

    @Autowired
    private DefaultKaptcha captchaProducer;

    @GetMapping("/common/kaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) throws Exception {
        byte[] captchaOutputStream = null;
        ByteArrayOutputStream imgOutputStream = new ByteArrayOutputStream();
        try {
            //生产验证码字符串并保存到session中
            String verifyCode = captchaProducer.createText();
            httpServletRequest.getSession().setAttribute("verifyCode", verifyCode);
            BufferedImage challenge = captchaProducer.createImage(verifyCode);

            ImageIO.write(challenge, "jpg", imgOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        captchaOutputStream = imgOutputStream.toByteArray();
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaOutputStream);
        responseOutputStream.flush();
        responseOutputStream.close();
    }

    @GetMapping("/common/mall/kaptcha")
    public void mallKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        com.google.code.kaptcha.impl.DefaultKaptcha qstMallLoginKaptcha = new com.google.code.kaptcha.impl.DefaultKaptcha();
        Properties properties = new Properties();
        properties.put("kaptcha.border", "no");
        properties.put("kaptcha.textproducer.font.color", "27,174,171");
        properties.put("kaptcha.noise.color", "20,33,42");
        properties.put("kaptcha.textproducer.font.size", "30");
        properties.put("kaptcha.image.width", "110");
        properties.put("kaptcha.image.height", "40");
        properties.put("kaptcha.session.key", Constants.MALL_VERIFY_CODE_KEY);
        properties.put("kaptcha.textproducer.char.space", "2");
        properties.put("kaptcha.textproducer.char.length", "6");
        Config config = new Config(properties);
        qstMallLoginKaptcha.setConfig(config);
        byte[] captchaOutputStream = null;
        ByteArrayOutputStream imgOutputStream = new ByteArrayOutputStream();
        try {
            //生产验证码字符串并保存到session中
            String verifyCode = qstMallLoginKaptcha.createText();
            httpServletRequest.getSession().setAttribute(Constants.MALL_VERIFY_CODE_KEY, verifyCode);
            BufferedImage challenge = qstMallLoginKaptcha.createImage(verifyCode);
            ImageIO.write(challenge, "jpg", imgOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        captchaOutputStream = imgOutputStream.toByteArray();
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaOutputStream);
        responseOutputStream.flush();
        responseOutputStream.close();
    }

    @GetMapping("/getOtp")
    @ResponseBody
    public Result getOtp(@RequestParam("loginName") String loginName, HttpSession httpSession) {
        if (StringUtils.isEmpty(loginName)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_NULL.getResult());
        }
        //1.需要按照一定规则生成OTP验证码
        Random random=new Random();
        int randomInt=random.nextInt(899999);
        randomInt+=100000;
        String code=String.valueOf(randomInt);
        //2.将OTP验证码同用户的手机号关联（可以通过redis实现）
        httpSession.setAttribute(Constants.MALL_VERIFY_CODE_KEY,code);
        System.out.println(loginName+":"+code);
        return ResultGenerator.genSuccessResult();
        //3.将OTP验证码通过短信通道发给用户
       /* CommonResponse response=AliyunSmsUtils.sendSMS(telphone,code);
        JSONObject jsonObject= new JSONObject(response.getData());
        String responseCode=jsonObject.getString("Code");
        if(responseCode.equals("OK")){
            return BaseDTO.createBaseDTO(response.getData());
        }else{
            throw new BaseException(EmBaseError.UNKNOWN_ERROR,jsonObject.getString("Message"));
        }*/
        //获取成功
       /* //获取失败
        return ResultGenerator.genFailResult(registerResult);*/
    }

}
