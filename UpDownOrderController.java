package com.showhan.frontend.controller.game;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.showhan.activity.helper.vo.UpDownOrderVo;
import com.showhan.activity.manager.InvestOrderManager;
import com.showhan.activity.manager.UpDownOrderManager;
import com.showhan.activity.model.InvestOrder;
import com.showhan.activity.model.Quotation;
import com.showhan.activity.model.UpDownOrder;
import com.showhan.base.manager.AuxTableManager;
import com.showhan.base.model.AuxTable;
import com.showhan.base.model.SystemConfig;
import com.showhan.blockchain.WSConstants;
import com.showhan.core.Constants;
import com.showhan.core.controller.BaseController;
import com.showhan.core.helper.ConfigHelper;
import com.showhan.core.search.Search;
import com.showhan.core.utils.DateUtil;
import com.showhan.core.utils.I18nUtil;
import com.showhan.core.utils.NumberUtil;
import com.showhan.core.utils.StringUtil;
import com.showhan.core.web.QuotationApplication;
import com.showhan.core.web.TickerApplication;
import com.showhan.customer.manager.CustomerManager;
import com.showhan.customer.manager.WithdrawManager;
import com.showhan.customer.model.Customer;
import com.showhan.customer.model.Withdraw;
import com.showhan.drp.DrpConstants;
import com.showhan.drp.manager.DrpConfigManager;
import com.showhan.drp.model.DrpConfig;
import com.showhan.frontend.ws.WsClient;



@Controller
@RequestMapping("/game/upDownOrder/*")
public class UpDownOrderController extends BaseController {
	
	private static final long serialVersionUID = -1L;
	@Autowired private UpDownOrderManager upDownOrderManager=null;
	@Autowired private CustomerManager customerManager=null;
	@Autowired private DrpConfigManager drpConfigManager=null;
	@Autowired private InvestOrderManager investOrderManager=null;
	@Autowired private WithdrawManager withdrawManager=null;
	@Autowired private AuxTableManager auxTableManager=null;
	
	


	@RequestMapping(value = "/join", method = RequestMethod.POST,produces = "text/html;charset=UTF-8")
	public @ResponseBody  String join(HttpServletRequest request,
			HttpServletResponse response,HttpSession session, Model model,	
			@RequestParam(required = true) String account,	
			@RequestParam(required = true) BigDecimal amount,			
			@RequestParam(required = true) Short region,
			@RequestParam(required = true) Short direction,
			@RequestParam(required = false) String coinType
			)  { 	
		Customer customer=this.customerManager.getByAccount(account);
		if(customer==null){
			return I18nUtil.getMessage("msg.account.isNotExist");
		}
		if(amount.compareTo(BigDecimal.ZERO)!=1){
			return I18nUtil.getMessage("msg.amount.isLessZero");
		}
	
		List<UpDownOrder> upDownOrders=this.upDownOrderManager.findByAccountAndIsOpen(account, Constants.NO);
		if(upDownOrders!=null&&upDownOrders.size()>0){
			return I18nUtil.getMessage("upDownOrder.existUnOpenOrder");
		}
		coinType=StringUtil.isNotEmpty(coinType)?coinType:WSConstants.COIN_TYPE_ETH;
		DrpConfig drpConfig=this.drpConfigManager.getByPolicyCode(DrpConstants.GAME_OPTION);	
		if(UpDownOrder.REGION_FIXED.equals(region)){
			BigDecimal restJoinAmount=customer.getRestJoinAmount()!=null?customer.getRestJoinAmount():BigDecimal.ZERO;
			if(amount.compareTo(restJoinAmount)==1){	
				return I18nUtil.getMessage("msg.balanceIsLess");
			}	
			String minAmt=drpConfig.getP10();
	    	if(StringUtil.isNotEmpty(minAmt)){
	    		if(amount.compareTo(new BigDecimal(minAmt))==-1){
	    			return I18nUtil.getMessage("msg.minAmt.hint",new Object[]{minAmt});
	    		}
	    	}
	    	String maxAmt=drpConfig.getP11();
	    	if(StringUtil.isNotEmpty(maxAmt)){
	    		if(amount.compareTo(new BigDecimal(maxAmt))==1){
	    			return I18nUtil.getMessage("msg.mxAmt.hint",new Object[]{maxAmt});
	    		}
	    	}
	    	String joinTimes=drpConfig.getP12();
	    	if(StringUtil.isNotEmpty(joinTimes)){
	    		BigDecimal[] remainder=amount.divideAndRemainder(new BigDecimal(joinTimes));
	    		if(remainder[1].compareTo(BigDecimal.ZERO)!=0){
	    			return I18nUtil.getMessage("msg.times.hint",new Object[]{joinTimes});
	    		}
	    	}
			
		
			String day_times=drpConfig.getP9();
			if(StringUtil.isNotEmpty(day_times)){
				BigDecimal fixedAmount=customer.getFixedAmount()!=null?customer.getFixedAmount():BigDecimal.ZERO;
				BigDecimal dayMount=fixedAmount.divide(new BigDecimal(day_times),2,BigDecimal.ROUND_DOWN);
				if(amount.compareTo(dayMount)==1){
					return I18nUtil.getMessage("upDownOrder.amount.hint",new Object[]{dayMount});
				}
			}
		}else if(UpDownOrder.REGION_FREE.equals(region)){
			BigDecimal bw=customer.getBw()!=null?customer.getBw():BigDecimal.ZERO;
			if(amount.compareTo(bw)==1){
				return I18nUtil.getMessage("msg.balanceIsLess");
			}
			String minAmt=drpConfig.getP14();
	    	if(StringUtil.isNotEmpty(minAmt)){
	    		if(amount.compareTo(new BigDecimal(minAmt))==-1){
	    			return I18nUtil.getMessage("msg.minAmt.hint",new Object[]{minAmt});
	    		}
	    	}
	    	String maxAmt=drpConfig.getP15();
	    	if(StringUtil.isNotEmpty(maxAmt)){
	    		if(amount.compareTo(new BigDecimal(maxAmt))==1){
	    			return I18nUtil.getMessage("msg.mxAmt.hint",new Object[]{maxAmt});
	    		}
	    	}
	    	String joinTimes=drpConfig.getP16();
	    	if(StringUtil.isNotEmpty(joinTimes)){	
	    		BigDecimal[] remainder=amount.divideAndRemainder(new BigDecimal(joinTimes));
	    		if(remainder[1].compareTo(BigDecimal.ZERO)!=0){
	    			return I18nUtil.getMessage("msg.times.hint",new Object[]{joinTimes});
	    		}
	    	}
		}
		
		try {
			UpDownOrder upDownOrder=this.upDownOrderManager.saveUpDownOrder(account, amount, region, direction,coinType);
			//sendTickerServer(upDownOrder);
		
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}         
		return "";		
	}
	
	
	  private void sendTickerServer(UpDownOrder upDownOrder){
	    	try {
	    		String websockUrl=ConfigHelper.getConfig(SystemConfig.WEBSOCKET_URL, "ws://localhost/ws/ticker");
	    		if(!websockUrl.endsWith("/")){
	    			websockUrl=websockUrl+"/";
	    		}
	    		websockUrl=websockUrl+upDownOrder.getCoinType().toLowerCase();
	    		WsClient client=new WsClient(new URI(websockUrl));
			    client.connect();			    
	            while (!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
	            	
	            }
	            client.send(upDownOrder.toJsonString());	         
	            client.closeBlocking();
			} catch (Exception e) {			
				e.printStackTrace();
			}
	    	
		}
	
	@RequestMapping(value = "/findMyUpOrDownOrders", method = RequestMethod.POST,produces = "text/html;charset=UTF-8")
	public  String findMyUpOrDownOrders(HttpServletRequest request,
			HttpServletResponse response,HttpSession session, Model model,
			@RequestParam(required = true) String account,
			@RequestParam(required = true) Short region
			)  { 
			List<UpDownOrder> upDownOrders=this.upDownOrderManager.findByCondition(account,region, 0, 100);
			model.addAttribute("upDownOrders", upDownOrders);
			model.addAttribute("region", region);
		    return "game/updown/upDownOrders";		
	}
	
	
	
}
