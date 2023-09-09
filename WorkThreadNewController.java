package com.tydic.sms.dispatch.feign.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tydic.sms.dispatch.sms.misc.SMSConfig;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.tydic.sms.common.vo.ActionVO;
import com.tydic.sms.common.vo.CommonVO;
import com.tydic.sms.common.vo.MTBizVO;
import com.tydic.sms.dispatch.cache.CTGCacheManager;
import com.tydic.sms.dispatch.common.dc.DataReloadService;
import com.tydic.sms.dispatch.common.dc.SMSActionDC;
import com.tydic.sms.dispatch.controller.DynamicFeignClient;
import com.tydic.sms.dispatch.controller.RemoteApi;
import com.tydic.sms.dispatch.service.TbWssActionAppConfigService;
import com.tydic.sms.dispatch.vo.TbWssActionAppConfig;

import io.netty.handler.codec.AsciiHeadersEncoder.NewlineType;

@RequestMapping("sms-consumer-dispatch-work")
@RestController
public class WorkThreadNewController extends HttpServlet{

	// 日志对象
	public static Logger logger = Logger.getLogger(WorkThreadNewController.class);
	
	@Autowired
    DynamicFeignClient<RemoteApi> client;
	/*@Autowired
	TbWssActionAppConfigService appConfigService;*/
	@Autowired
	private DataReloadService dataReloadService;
	@RequestMapping("/smsIn")
	private void run(HttpServletRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub

    //根据指令ID查询配置表，确定哪个service,确定哪个方法
	/*
	 * String appName = "smsQuery"; String methodName = "feeQuery";
	 * if("smsQuery".equals(appName)) { if("feeQuery".equals(methodName)) {
	 * smsQueryService.feeQuery(null); }else
	 * if("getBalanceQuery".equals(methodName)) { smsQueryService.feeQuery(null); }
	 * //..... }else if("smsOrder".equals(appName)) {
	 * 
	 * }
	 */
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"UTF-8"));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String reqBody = sb.toString();
			JSONObject request = JSONObject.parseObject(reqBody);
			String actionId = request.getString("actionId");
			String accNbr =request.getString("accNbr");
			String latnId =request.getString("latnId");
			String msg = request.getString("msg");
			String actionName = request.getString("actionName");
			//定位数据的服务群组
	        //TbWssActionAppConfig appConfig = appConfigService.findAppConfig(actionId);
			TbWssActionAppConfig appConfig = dataReloadService.findAppConfig(actionId);
	        String service_location = appConfig.getServiceId();
	        String appName = appConfig.getClassMapping();
	        String ablitilyName = appConfig.getMethodName();
	        RemoteApi api = client.GetFeignClient(RemoteApi.class, service_location);
	        CommonVO commonVo = new CommonVO();
	        MTBizVO mtbizVo = new MTBizVO();
	        ActionVO actionVO = SMSActionDC.getInstance().getActionVO(actionId);
	        actionVO.setUserNumber(accNbr);
	        actionVO.setLatnID(latnId);
	        commonVo.setActionVo(actionVO);
	        String[] str = msg.split("#");
	        commonVo.setMsgContents(str);
	        commonVo.setMtbizVo(mtbizVo);
	      //上行流水 msgId
	        commonVo.setMsgId("2345543654364366");
	        String result = api.getContent(appName,ablitilyName,commonVo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
