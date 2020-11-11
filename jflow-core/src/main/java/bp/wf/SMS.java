package bp.wf;

import bp.da.*;
import bp.difference.SystemConfig;
import bp.en.*;
import bp.en.Map;
import bp.web.*;
import bp.sys.*;
import bp.tools.DateUtils;
import bp.tools.HttpClientUtil;
import bp.wf.port.*;
import java.util.*;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** 
 消息
*/
public class SMS extends EntityMyPK
{

		///新方法 2013
	/** 
	 发送消息
	 
	 @param userNo 接受人
	 @param msgTitle 标题
	 @param msgDoc 内容
	 @param msgFlag 标记
	 @param msgType 类型
	 @param paras 扩展参数
	 * @throws Exception 
	*/

	public static void SendMsg(String userNo, String msgTitle, String msgDoc, String msgFlag, String msgType, String paras, long workid, String pushModel) throws Exception
	{
		SendMsg(userNo, msgTitle, msgDoc, msgFlag, msgType, paras, workid, pushModel, null);
	}

	public static void SendMsg(String userNo, String msgTitle, String msgDoc, String msgFlag, String msgType, String paras, long workid) throws Exception
	{
		SendMsg(userNo, msgTitle, msgDoc, msgFlag, msgType, paras, workid, null, null);
	}


	public static void SendMsg(String userNo, String msgTitle, String msgDoc, String msgFlag, String msgType, String paras, long workid, String pushModel, String openUrl) throws Exception
	{

		SMS sms = new SMS();
		sms.setMyPK(DBAccess.GenerGUID());
		sms.setHisEmailSta(MsgSta.UnRun);

		sms.setSender(WebUser.getNo());
		sms.setSendToEmpNo(userNo);

		sms.setTitle(msgTitle);
		sms.setDocOfEmail(msgDoc);
		sms.setMobileInfo(msgDoc);

		sms.setSender(WebUser.getNo());
		sms.setRDT(DataType.getCurrentDataTime());

		sms.setMsgFlag(msgFlag); // 消息标志.
		sms.setMsgType(msgType); // 消息类型.'

		sms.setAtPara(paras);

		sms.setWorkID(workid);

		if (DataType.IsNullOrEmpty(openUrl) == false)
		{
			sms.SetPara("OpenUrl", openUrl);
		}
		if (DataType.IsNullOrEmpty(pushModel) == false)
		{
			sms.SetPara("PushModel", pushModel);
		}

		sms.Insert();
	}

		/// 新方法


		///手机短信属性
	/** 
	 手机号码
	 * @throws Exception 
	*/
	public final String getMobile() throws Exception
	{
		return this.GetValStringByKey(SMSAttr.Mobile);
	}
	public final void setMobile(String value) throws Exception
	{
		SetValByKey(SMSAttr.Mobile, value);
	}
	/** 
	 手机状态
	*/
	public final MsgSta getHisMobileSta() throws Exception
	{
		return MsgSta.forValue(this.GetValIntByKey(SMSAttr.MobileSta));
	}
	public final void setHisMobileSta(MsgSta value) throws Exception
	{
		SetValByKey(SMSAttr.MobileSta, value.getValue());
	}
	/** 
	 手机信息
	*/
	public final String getMobileInfo() throws Exception
	{
		return this.GetValStringByKey(SMSAttr.MobileInfo);
	}
	public final void setMobileInfo(String value) throws Exception
	{
		SetValByKey(SMSAttr.MobileInfo, value);
	}

		///


		/// 邮件属性
	/** 
	 参数
	*/
	public final String getAtPara() throws Exception
	{
		return this.GetValStrByKey("AtPara", "");
	}
	public final void setAtPara(String value) throws Exception
	{
		this.SetValByKey("AtPara", value);
	}
	/** 
	 邮件状态
	*/
	public final MsgSta getHisEmailSta() throws Exception
	{
		return MsgSta.forValue(this.GetValIntByKey(SMSAttr.EmailSta));
	}
	public final void setHisEmailSta(MsgSta value) throws Exception
	{
		this.SetValByKey(SMSAttr.EmailSta, value.getValue());
	}
	/** 
	 Email
	 * @throws Exception 
	*/
	public final String getEmail() throws Exception
	{
		return this.GetValStringByKey(SMSAttr.Email);
	}
	public final void setEmail(String value) throws Exception
	{
		SetValByKey(SMSAttr.Email, value);
	}
	/** 
	 发送给
	*/
	public final String getSendToEmpNo()throws Exception
	{
		return this.GetValStringByKey(SMSAttr.SendTo);
	}
	public final void setSendToEmpNo(String value) throws Exception
	{
		SetValByKey(SMSAttr.SendTo, value);
	}
	public final int getIsRead()throws Exception
	{
		return this.GetValIntByKey(SMSAttr.IsRead);
	}
	public final void setIsRead(int value) throws Exception
	{
		this.SetValByKey(SMSAttr.IsRead, value);
	}
	public final int getIsAlert()throws Exception
	{
		return this.GetValIntByKey(SMSAttr.IsAlert);
	}
	public final void setIsAlert(int value) throws Exception
	{
		this.SetValByKey(SMSAttr.IsAlert, value);
	}
	/** 
	 消息标记(可以用它来避免发送重复)
	 * @throws Exception 
	*/
	public final String getMsgFlag() throws Exception
	{
		return this.GetValStringByKey(SMSAttr.MsgFlag);
	}
	public final void setMsgFlag(String value) throws Exception
	{
		SetValByKey(SMSAttr.MsgFlag, value);
	}
	/** 
	 类型
	*/
	public final String getMsgType()throws Exception
	{
		return this.GetValStringByKey(SMSAttr.MsgType);
	}
	public final void setMsgType(String value) throws Exception
	{
		SetValByKey(SMSAttr.MsgType, value);
	}
	/** 
	 工作ID
	*/
	public final long getWorkID() throws Exception
	{
		return this.GetValInt64ByKey(SMSAttr.WorkID);
	}
	public final void setWorkID(long value)  throws Exception
	{
		SetValByKey(SMSAttr.WorkID, value);
	}
	/** 
	 发送人
	*/
	public final String getSender()throws Exception
	{
		return this.GetValStringByKey(SMSAttr.Sender);
	}
	public final void setSender(String value) throws Exception
	{
		SetValByKey(SMSAttr.Sender, value);
	}
	/** 
	 记录日期
	*/
	public final String getRDT() throws Exception
	{
		return this.GetValStringByKey(SMSAttr.RDT);
	}
	public final void setRDT(String value) throws Exception
	{
		this.SetValByKey(SMSAttr.RDT, value);
	}
	/** 
	 标题
	*/
	public final String getTitle()throws Exception
	{
		return this.GetValStringByKey(SMSAttr.EmailTitle);
	}
	public final void setTitle(String value) throws Exception
	{
		SetValByKey(SMSAttr.EmailTitle, value);
	}
	/** 
	 邮件内容
	*/
	public final String getDocOfEmail()throws Exception
	{
		String doc = this.GetValStringByKey(SMSAttr.EmailDoc);
		if (DataType.IsNullOrEmpty(doc))
		{
			return this.getTitle();
		}

		return doc.replace('~', '\'');
	}
	public final void setDocOfEmail(String value) throws Exception
	{
		SetValByKey(SMSAttr.EmailDoc, value);
	}
	/** 
	 邮件内容.
	*/
	public final String getDoc()throws Exception
	{
		String doc = this.GetValStringByKey(SMSAttr.EmailDoc);
		if (DataType.IsNullOrEmpty(doc))
		{
			return this.getTitle();
		}
		return doc.replace('~', '\'');

		//return this.getDocOfEmail();
	}
	public final void setDoc(String value) throws Exception
	{
		SetValByKey(SMSAttr.EmailDoc, value);
	}
	/** 
	 打开的连接
	*/
	public final String getOpenURL()throws Exception
	{
		return this.GetParaString(SMSAttr.OpenUrl);
	}
	public final void setOpenURL(String value) throws Exception
	{
		this.SetPara(SMSAttr.OpenUrl, value);
	}

		///

	public final String getPushModel()throws Exception
	{
		return this.GetParaString(SMSAttr.PushModel);
	}


		///构造函数
	/** 
	 UI界面上的访问控制
	*/
	@Override
	public UAC getHisUAC() throws Exception
	{
		UAC uac = new UAC();
		uac.OpenAll();
		return uac;
	}
	/** 
	 消息
	*/
	public SMS()
	{
	}
	/** 
	 Map
	*/
	@Override
	public Map getEnMap() throws Exception
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}

		Map map = new Map("Sys_SMS", "消息");

		map.AddMyPK();

		map.AddTBString(SMSAttr.Sender, null, "发送人(可以为空)", false, true, 0, 200, 20);
		map.AddTBString(SMSAttr.SendTo, null, "发送给(可以为空)", false, true, 0, 200, 20);
		map.AddTBDateTime(SMSAttr.RDT, "写入时间", true, false);

		map.AddTBString(SMSAttr.Mobile, null, "手机号(可以为空)", false, true, 0, 30, 20);
		map.AddTBInt(SMSAttr.MobileSta, MsgSta.UnRun.getValue(), "消息状态", true, true);
		map.AddTBString(SMSAttr.MobileInfo, null, "短信信息", false, true, 0, 1000, 20);

		map.AddTBString(SMSAttr.Email, null, "Email(可以为空)", false, true, 0, 200, 20);
		map.AddTBInt(SMSAttr.EmailSta, MsgSta.UnRun.getValue(), "EmaiSta消息状态", true, true);
		map.AddTBString(SMSAttr.EmailTitle, null, "标题", false, true, 0, 3000, 20);
		map.AddTBStringDoc(SMSAttr.EmailDoc, null, "内容", false, true);
		map.AddTBDateTime(SMSAttr.SendDT, null, "发送时间", false, false);

		map.AddTBInt(SMSAttr.IsRead, 0, "是否读取?", true, true);
		map.AddTBInt(SMSAttr.IsAlert, 0, "是否提示?", true, true);

			//用于获得数据.
		map.AddTBInt(SMSAttr.WorkID, 0, "WorkID", true, true);

			//消息主键.
		map.AddTBString(SMSAttr.MsgFlag, null, "消息标记(用于防止发送重复)", false, true, 0, 200, 20);
		map.AddTBString(SMSAttr.MsgType, null, "消息类型(CC抄送,Todolist待办,Return退回,Etc其他消息...)", false, true, 0, 200, 20);

			//其他参数.
		map.AddTBAtParas(500);

		this.set_enMap(map);
		return this.get_enMap();
	}

		///

	/** 
	 发送邮件
	 
	 @param mail
	 @param mailTitle
	 @param mailDoc
	 @return 
	*/
	public static void SendEmailNowAsync(String mail, String mailTitle, String mailDoc,String sendTo) throws Exception
	{
		bp.gpm.Emp emp=new bp.gpm.Emp(sendTo);
   		if(DataType.IsNullOrEmpty(emp.getEmail())){
   			return;
		}
		//邮件地址.return
       final String  emailAddr = SystemConfig.GetValByKey("SendEmailAddress", "ccbpmtester@tom.com");

      final String emailPassword = SystemConfig.getAppSettings().get("SendEmailPass").toString();
		// 第一步：配置javax.mail.Session对象  
		Properties props = new Properties(); 
		props.setProperty("mail.transport.protocol", "smtp"); 
		props.put("mail.smtp.host",SystemConfig.GetValByKey("SendEmailHost", "smtp.tom.com"));//发邮件地址
		props.put("mail.smtp.port",SystemConfig.GetValByKeyInt("SendEmailPort", 25)); //端口号
		props.put("mail.smtp.auth","true");//使用 STARTTLS安全连接  
		
		bp.sys.Glo.WriteUserLog("EM", "emailAddr", emailAddr);

		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication(){
			    return new PasswordAuthentication(emailAddr,emailPassword);
			 }
		};

		Session mailSession = Session.getInstance(props,auth);
		InternetAddress fromAddress = new InternetAddress(emailAddr);
       InternetAddress toAddress = new InternetAddress(emp.getEmail());
       
		bp.sys.Glo.WriteUserLog("EM", "toAddress", emp.getEmail());

		// 3. 创建一封邮件
       MimeMessage message = new MimeMessage(mailSession);
       message.setFrom(fromAddress);
       message.addRecipient(RecipientType.TO, toAddress);
       message.setSentDate(DateUtils.currentDate());
       message.setSubject(mailTitle);
       message.setText(mailDoc);
       
       //4.发送Email,
       Transport transport = mailSession.getTransport("smtp");//定义发送协议
       bp.sys.Glo.WriteUserLog("EM", "transport", "定义发送协议");
		//登录邮箱
		try{
			transport.send(message, message.getRecipients(RecipientType.TO));//发送邮件

		}catch(Exception e){
			bp.sys.Glo.WriteUserLog("EM", "send", "邮件发送失败，接收人邮箱["+emp.getEmail()+"],错误信息："+e.getMessage());
		}
		bp.sys.Glo.WriteUserLog("EM", "send", "发送成功");

	}
	/** 
	 插入之后执行的方法.
	 * @throws Exception 
	*/
	@Override
	protected void afterInsert() throws Exception
	{
		try
		{
			if (this.getHisEmailSta() != MsgSta.UnRun)
			{
				return;
			}


				///发送邮件
			if (this.getPushModel().contains("Email") == true && DataType.IsNullOrEmpty(this.getEmail()) == false)
			{
				String emailStrs = this.getEmail();
				emailStrs = emailStrs.replace(",", ";");
				emailStrs = emailStrs.replace("，", ";");

				//包含多个邮箱
				if (emailStrs.contains(";") == true)
				{
					String[] emails = emailStrs.split("[;]", -1);
					for (String email : emails)
					{
						if (DataType.IsNullOrEmpty(email) == true)
						{
							continue;
						}
						SendEmailNowAsync(email, this.getTitle(), this.getDocOfEmail(),this.getSendToEmpNo());
						
					}
				}
				else
				{ //单个邮箱
					SendEmailNowAsync(this.getEmail(), this.getTitle(), this.getDocOfEmail(),this.getSendToEmpNo());
					
				}

			}

				/// 发送邮件


				///发送短消息 调用接口
			//发送短消息的前提必须是手机号不能为空
			//if (DataType.IsNullOrEmpty(this.Mobile) == true)
			//    return;
			//throw new Exception("发送短消息时接收人的手机号不能为空,否则接受不到消息");

			String messageUrl = SystemConfig.GetValByKey("HandlerOfMessage","");
			if (DataType.IsNullOrEmpty(messageUrl) == true)
			{
				return;
			}
			String httpUrl = "";

	HashMap<String,String> msgMap = new HashMap<String,String>();


		    msgMap.put("sender",this.getSender());
		    msgMap.put("sendTo",this.getSendToEmpNo());
			
			msgMap.put("tel",this.getMobile());
			msgMap.put("title",this.getTitle());
			msgMap.put("content",this.getMobileInfo());
			msgMap.put("openUrl",this.getOpenURL());

			//soap = BP.WF.Glo.GetPortalInterfaceSoapClient();
			if (this.getPushModel().contains("CCMsg") == true)
			{
				httpUrl =messageUrl+ "?DoType=SendToCCMSG";
				HttpClientUtil.doPost(httpUrl, msgMap,null,null);
			}
			//短信
			if (this.getPushModel().contains("SMS") == true)
			{
				httpUrl = messageUrl + "?DoType=SendToWebServices";
				HttpClientUtil.doPost(httpUrl, msgMap,null,null);
			}
			//钉钉
			if (this.getPushModel().contains("DingDing") == true)
			{
				httpUrl = messageUrl + "?DoType=SendToDingDing";
				HttpClientUtil.doPost(httpUrl, msgMap,null,null);
			}
			//微信
			if (this.getPushModel().contains("WeiXin") == true)
			{
				httpUrl = messageUrl + "?DoType=SendToWeiXin";
				HttpClientUtil.doPost(httpUrl, msgMap,null,null);
			}
			//WebService
			if (this.getPushModel().contains("WS") == true)
			{
				httpUrl = messageUrl + "?DoType=SendToWebServices";
				HttpClientUtil.doPost(httpUrl, msgMap,null,null);
			}
			super.afterInsert();

				/// 发送短消息 调用接口

		}
		catch (RuntimeException ex)
		{
			bp.da.Log.DebugWriteError("@消息机制没有配置成功." + ex.getMessage());
		}
		super.afterInsert();
	}
}