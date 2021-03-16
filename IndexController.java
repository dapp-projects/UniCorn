package com.showhan.frontend.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.web3j.utils.Convert;

import com.showhan.activity.manager.GameOrderManager;
import com.showhan.activity.manager.GameTermManager;
import com.showhan.activity.model.GameTerm;
import com.showhan.base.helper.CodeGenUtil;
import com.showhan.blockchain.helper.Web3jHelper;
import com.showhan.blockchain.manager.CompanyWalletManager;
import com.showhan.blockchain.model.CompanyWallet;
import com.showhan.core.Constants;
import com.showhan.core.controller.BaseController;
import com.showhan.core.utils.I18nUtil;
import com.showhan.core.utils.RequestUtil;
import com.showhan.core.utils.StringUtil;
import com.showhan.core.web.TickerApplication;
import com.showhan.customer.manager.CustomerManager;
import com.showhan.customer.manager.MembershipManager;
import com.showhan.customer.model.Customer;
import com.showhan.customer.model.Membership;
import com.showhan.drp.DrpConstants;
import com.showhan.drp.manager.DrpConfigManager;
import com.showhan.drp.manager.FundHistoryManager;
import com.showhan.drp.model.DrpConfig;


@Controller

public class IndexController extends BaseController {
	private static final long serialVersionUID = -1L;
	@Autowired private GameTermManager gameTermManager=null;
	@Autowired private GameOrderManager gameOrderManager=null;
	@Autowired private CustomerManager customerManager=null;
	@Autowired private DrpConfigManager drpConfigManager=null;
	@Autowired private FundHistoryManager fundHistoryManager=null;
	@Autowired private CompanyWalletManager companyWalletManager=null;
	@Autowired private MembershipManager membershipManager=null;
	
	@RequestMapping(value = "/index",method = RequestMethod.GET,produces = "text/html;charset=UTF-8")
	public String index(HttpServletRequest request,
			HttpServletResponse response,HttpSession session, Model model)  { 	
	    DrpConfig  callOptionDrpConfig=drpConfigManager.getByPolicyCode(DrpConstants.GAME_OPTION);
        model.addAttribute("callOptionDrpConfig", callOptionDrpConfig);
        
        
		 DrpConfig  drpConfig=drpConfigManager.getByPolicyCode(DrpConstants.GAME_FOMO3D);
         model.addAttribute("drpConfig", drpConfig);
         
     
         model.addAttribute("ref", RequestUtil.getParameterNullSafe(request, "ref"));
         model.addAttribute("lang", RequestUtil.getParameterNullSafe(request, "lang"));
         
      
         String token=CodeGenUtil.getInstance().getAccessToken();
         session.setAttribute(Constants.TOKEN, token);
      
      
         BigInteger gasPrice= Convert.toWei(Web3jHelper.getRealTimeGasPrice(), Convert.Unit.GWEI).toBigInteger();         
         model.addAttribute("gasPrice", gasPrice);
         
         Long diffMs=TickerApplication.getDiffMs()!=null?TickerApplication.getDiffMs():1L;      
         model.addAttribute("diffSecond", diffMs/1000);
       
         CompanyWallet companyWallet= companyWalletManager.getDefaultWallet();
         model.addAttribute("companyWallet", companyWallet);
               
        List<Membership> memberships=this.membershipManager.findMembershipsForDistributor();      
        model.addAttribute("memberships", memberships);
        
     
        Cookie cookie=RequestUtil.getCookie(request, "_account");
        if(cookie!=null){
        	String account=cookie.getValue();
        	if(StringUtil.isNotEmpty(account)){
        		Customer customer=this.customerManager.getByAccount(account);
        		if(customer!=null){
        			   Membership defaultMembership=customer.getDistributorMembership();
        			   model.addAttribute("defaultMembership", defaultMembership);
        			   
        			   Membership curMembership=null;
        			   for(Membership tmpMembership:memberships){
        				   if(tmpMembership.getMembershipLevel()>defaultMembership.getMembershipLevel()){
        					   curMembership=tmpMembership;
        					   break;
        				   }
        			   }
        			   if(curMembership!=null){
        				   model.addAttribute("curMembership", curMembership);
        			   }
        			   
        		}
        	}
        	
        }
        
        //
    	DrpConfig withdrawConfig= this.drpConfigManager.getByPolicyCode(DrpConstants.OP_WITHDRAW);
    	String feeMode=withdrawConfig.getP12();
 	  	String feeRate=withdrawConfig.getP13();
 	  	String feeFixed=StringUtil.isNotEmpty(withdrawConfig.getP14())?withdrawConfig.getP14():"0";
 	  	if("0".equals(feeMode)){
 	  	  model.addAttribute("withdrawTitle", I18nUtil.getMessage("msg.withdraw.percent.hint", new Object[]{new BigDecimal(feeRate).multiply(new BigDecimal("100"))}));
 	  	}else{
 	   	  model.addAttribute("withdrawTitle", I18nUtil.getMessage("msg.withdraw.hint", new Object[]{feeFixed}));
 	  	
 	  	}
		return "index";		
	}
	
    private void loadFomo3d(Model model){
    	 DrpConfig  drpConfig=drpConfigManager.getByPolicyCode(DrpConstants.GAME_FOMO3D);
         model.addAttribute("drpConfig", drpConfig);
         
      
      
        GameTerm currGameTerm = gameTermManager.getCurrGameTerm();
        model.addAttribute("currGameTerm", currGameTerm);
     
        
     
        GameTerm lastGameTerm= gameTermManager.getLastGameTerm();
        model.addAttribute("lastGameTerm", lastGameTerm);
       
    
        BigDecimal bounsPool=BigDecimal.ZERO;
       if(currGameTerm==null){
    	   if(lastGameTerm!=null){
    		   bounsPool=lastGameTerm.getBounsPool()!=null?lastGameTerm.getBounsPool():BigDecimal.ZERO;    		 
    		   String nextPoolRate=StringUtil.isNotEmpty(drpConfig.getP14())?drpConfig.getP14():"0";
    		   bounsPool=bounsPool.multiply(new BigDecimal(nextPoolRate));
    		   model.addAttribute("bounsPool", bounsPool);
    	   }
       }else{
    	   bounsPool=currGameTerm.getBounsPool();
    	   model.addAttribute("bounsPool", currGameTerm.getBounsPool());
       }
       
    
       String openTimeMode=drpConfig.getP16();
       if("0".equals(openTimeMode)){
    	   Long downTime=0L;
           String startPoolQty=drpConfig.getP15();
           if(StringUtil.isNotEmpty(startPoolQty)){
        	   BigDecimal b_startPoolQty=new BigDecimal(startPoolQty);
        	   if(bounsPool.compareTo(b_startPoolQty)!=-1){
        		   if(currGameTerm!=null){	
  					 downTime=currGameTerm.getDownTime().getTime();	
  				   }
        	   }
           }
           model.addAttribute("downTime",downTime);
       }else{
    	   String hours=drpConfig.getP21();
           Date nextOpeningTime=this.gameOrderManager.getNextOpeningTimeForHours(hours) ; 
           model.addAttribute("downTime", nextOpeningTime.getTime());
           model.addAttribute("nextOpeningTime", nextOpeningTime);
       }
    }


}
