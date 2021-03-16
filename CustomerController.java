package com.showhan.frontend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.showhan.core.Constants;
import com.showhan.core.controller.BaseController;
import com.showhan.core.utils.I18nUtil;
import com.showhan.core.utils.StringUtil;
import com.showhan.customer.manager.CustomerManager;
import com.showhan.customer.manager.CustomerRecommendManager;
import com.showhan.customer.manager.MembershipManager;
import com.showhan.customer.model.Customer;
import com.showhan.drp.DrpConstants;
import com.showhan.drp.manager.DrpConfigManager;
import com.showhan.drp.model.DrpConfig;

@Controller
@RequestMapping("/customer/*")
public class CustomerController extends BaseController {
	@Autowired private DrpConfigManager drpConfigManager=null;
	@Autowired private CustomerRecommendManager customerRecommendManager=null;
	@Autowired private CustomerManager customerManager=null;
	@Autowired private MembershipManager membershipManager=null;
	
	@RequestMapping(value = "/getAccountInfo", method = RequestMethod.POST,produces = "text/html;charset=UTF-8")
	public @ResponseBody  Map<String,Object> getAccountInfo(HttpServletRequest request,
			HttpServletResponse response,HttpSession session, Model model,	
			@RequestParam(required = true) String account
			)  { 		
		DrpConfig drpConfig=this.drpConfigManager.getByPolicyCode(DrpConstants.GAME_OPTION);
		Integer maxTimes=StringUtil.isNotEmpty(drpConfig.getP13())?new Integer(drpConfig.getP13()):10;
		Customer customer=this.customerManager.getByAccount(account);
		Map<String,Object> results=new HashMap<String,Object>();
		if(customer!=null){
			results.put("membership", customer.getDistributorMembership()!=null?customer.getDistributorMembership().getMembershipName():I18nUtil.getMessage("msg.none"));
			results.put("bw", customer.getBw()!=null?customer.getBw():BigDecimal.ZERO);
			results.put("totalInput", customer.getTotalInput()!=null?customer.getTotalInput():BigDecimal.ZERO);		
			results.put("totalJoinAmount", customer.getTotalJoinAmount()!=null?customer.getTotalJoinAmount():BigDecimal.ZERO);
			results.put("totalJoinAmountFree", customer.getTotalJoinAmountFree()!=null?customer.getTotalJoinAmountFree():BigDecimal.ZERO);
			results.put("totalJoinAmountFixed", customer.getTotalJoinAmountFixed()!=null?customer.getTotalJoinAmountFixed():BigDecimal.ZERO);
			
			results.put("teamJoinAmount", customer.getTeamJoinAmount()!=null?customer.getTeamJoinAmount():BigDecimal.ZERO);	
			results.put("teamJoinAmountFree", customer.getTeamJoinAmountFree()!=null?customer.getTeamJoinAmountFree():BigDecimal.ZERO);	
			results.put("teamJoinAmountFixed", customer.getTeamJoinAmountFixed()!=null?customer.getTeamJoinAmountFixed():BigDecimal.ZERO);	
			
			results.put("totalStaticBonus", customer.getTotalStaticIncome()!=null?customer.getTotalStaticIncome():BigDecimal.ZERO);
			results.put("totalIncome", customer.getTotalIncome()!=null?customer.getTotalIncome():BigDecimal.ZERO);
			
			results.put("realseBonus", customer.getRealseBonus()!=null?customer.getRealseBonus():BigDecimal.ZERO);	
			results.put("restRealseBonus", customer.getRestRealseBonus()!=null?customer.getRestRealseBonus():BigDecimal.ZERO);	
			results.put("dynamicBonus", customer.getTotalDynamicIncome()!=null?customer.getTotalDynamicIncome():BigDecimal.ZERO);
			results.put("rw", customer.getRw()!=null?customer.getRw():BigDecimal.ZERO);	
			results.put("balance", customer.getBalance()!=null?customer.getBalance():BigDecimal.ZERO);
			results.put("dw", customer.getDw()!=null?customer.getDw():BigDecimal.ZERO);	
			
			BigDecimal fixedAmount= customer.getFixedAmount()!=null?customer.getFixedAmount():BigDecimal.ZERO;
			BigDecimal maxAmtPerTimes=fixedAmount.divide(new BigDecimal(maxTimes),2,BigDecimal.ROUND_HALF_UP);
			results.put("maxAmtPerTimes", maxAmtPerTimes);
				
			results.put("subordinateQty", customer.getSubordinateQty()!=null?customer.getSubordinateQty():0);
			
			results.put("restJoinAmount", customer.getRestJoinAmount()!=null?customer.getRestJoinAmount():BigDecimal.ZERO);		
			results.put("restJoinTimes", maxTimes-(customer.getCurrJoinTimes()!=null?customer.getCurrJoinTimes():0));
			
			
			results.put("pledgeAmount", customer.getPledgeAmount()!=null?customer.getPledgeAmount():BigDecimal.ZERO);
			
			results.put("teamInput", customer.getTeamInput()!=null?customer.getTeamInput():BigDecimal.ZERO);
			
			BigDecimal restEnergy= customer.getRestEnergy()!=null?customer.getRestEnergy():BigDecimal.ZERO;
			BigDecimal totalEnergy=customer.getTotalEnergy()!=null?customer.getTotalEnergy():BigDecimal.ZERO;
			BigDecimal releasedEnergy=totalEnergy.subtract(restEnergy);
			results.put("restEnergy",restEnergy);
			results.put("releasedEnergy",releasedEnergy);
			results.put("totalEnergy",totalEnergy);
			
			results.put("isActiveAuto", customer.getIsActiveAuto()!=null?customer.getIsActiveAuto():0);
			
		}else{
			results.put("membership", I18nUtil.getMessage("msg.none"));			
			results.put("bw", BigDecimal.ZERO);
			results.put("totalInput", BigDecimal.ZERO);
			results.put("totalJoinAmount", BigDecimal.ZERO);			
			results.put("totalJoinAmountFree",BigDecimal.ZERO);
			results.put("totalJoinAmountFixed", BigDecimal.ZERO);
			
			results.put("teamJoinAmount", BigDecimal.ZERO);	
			results.put("teamJoinAmountFree", BigDecimal.ZERO);	
			results.put("teamJoinAmountFixed", BigDecimal.ZERO);	
			
			results.put("totalStaticBonus", BigDecimal.ZERO);
			results.put("totalIncome", BigDecimal.ZERO);			
			
			results.put("realseBonus", BigDecimal.ZERO);
			results.put("restRealseBonus", BigDecimal.ZERO);
			results.put("dynamicBonus", BigDecimal.ZERO);
			results.put("ew", BigDecimal.ZERO);
			results.put("rw", BigDecimal.ZERO);	
			results.put("balance", BigDecimal.ZERO);
			
			results.put("maxAmtPerTimes", BigDecimal.ONE);
			
			results.put("subordinateQty", 0);
			
			results.put("restJoinAmount",  BigDecimal.ZERO);
			results.put("restJoinTimes", 0);
			
			results.put("pledgeAmount", BigDecimal.ZERO);
			
			
			results.put("teamInput",  BigDecimal.ZERO);
			
		
		
			results.put("restEnergy", BigDecimal.ZERO);
			results.put("releasedEnergy", BigDecimal.ZERO);
			results.put("totalEnergy", BigDecimal.ZERO);
			results.put("dw", 0);
			results.put("isActiveAuto", 0);
		}
		
		return results;
	}
	@RequestMapping(value = "/getTeamInfo", method = RequestMethod.POST,produces = "text/html;charset=UTF-8")
	public @ResponseBody  Map<String,Object> getTeamInfo(HttpServletRequest request,
			HttpServletResponse response,HttpSession session, Model model,	
			@RequestParam(required = true) String account
			)  { 
		Customer customer=this.customerManager.getByAccount(account);
		Map<String,Object> results=new HashMap<String,Object>();
		if(customer!=null){
			results.put("subordinateQty", customer.getSubordinateQty()!=null?customer.getSubordinateQty():0);
			List<Customer> list=this.customerRecommendManager.findDescend(customer.getCustomerId());
			results.put("teamQty", list!=null?list.size():0);
		}else{			
			results.put("subordinateQty", 0);
			results.put("teamQty", 0);
		}
		return results;
	}
	
	@RequestMapping(value = "/joinAi", method = RequestMethod.POST,produces = "text/html;charset=UTF-8")
	public @ResponseBody  String joinAi(HttpServletRequest request,
			HttpServletResponse response,HttpSession session, Model model,	
			@RequestParam(required = true) String account
			)  { 
		Customer customer=this.customerManager.getByAccount(account);
		if(customer==null){
			return I18nUtil.getMessage("msg.account.isNotExist");
		}
		Short isActiveAuto=customer.getIsActiveAuto();
		if(Constants.YES.equals(isActiveAuto)){
			return I18nUtil.getMessage("msg.isActiveAuto.isActived");
		}
		try{
			this.customerManager.doJoinAi(account);
		}catch(Exception e){
			return e.getMessage();
		}
		
		return "";
	}
	

}
