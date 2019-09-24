package BP.Sys;
import BP.DA.*;
import BP.Difference.ContextHolderUtils;
import BP.En.*;
import BP.Tools.AesEncodeUtil;
import BP.Tools.Cryptos;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import java.math.*;

/** 
 公用的静态方法.
*/
public class Glo
{
	/** 
	 获得真实的数据类型
	 
	 @param attrs 属性集合
	 @param key key
	 @param val 值
	 @return 返回val真实的数据类型.
	*/
	public static Object GenerRealType(Attrs attrs, String key, Object val)
	{
		Attr attr = attrs.GetAttrByKey(key);
		switch (attr.getMyDataType())
		{
			case DataType.AppString:
			case DataType.AppDateTime:
			case DataType.AppDate:
				val = val.toString();
				break;
			case DataType.AppInt:
			case DataType.AppBoolean:
				val = Integer.parseInt(val.toString());
				break;
			case DataType.AppFloat:
				val = Float.parseFloat(val.toString());
				break;
			case DataType.AppDouble:

				val = Integer.parseInt(val.toString());
				break;
			case DataType.AppMoney:
				val = BigDecimal.valueOf(Double.parseDouble(val.toString()));
				break;
			default:
				throw new RuntimeException();
		}
		return val;
	}

     ///#region 业务单元.
	private static Hashtable Htable_BuessUnit = null;
	/** 
	 获得节点事件实体
	 
	 @param enName 实例名称
	 @return 获得节点事件实体,如果没有就返回为空.
	*/
	public static BuessUnitBase GetBuessUnitEntityByEnName(String enName)
	{
		if (Htable_BuessUnit == null || Htable_BuessUnit.isEmpty())
		{
			Htable_BuessUnit = new Hashtable();
			ArrayList<BuessUnitBase> al = BP.En.ClassFactory.GetObjects("BP.Sys.BuessUnitBase");
			for (BuessUnitBase en : al)
			{
				Htable_BuessUnit.put(en.toString(), en);
			}
		}

		BuessUnitBase myen = Htable_BuessUnit.get(enName) instanceof BuessUnitBase ? (BuessUnitBase)Htable_BuessUnit.get(enName) : null;
		if (myen == null)
		{
			BP.DA.Log.DefaultLogWriteLineError("@根据类名称获取业务单元实例出现错误:" + enName + ",没有找到该类的实体.");
			return null;
		}
		return myen;
	}
	/** 
	 获得事件实体String，根据编号或者流程标记
	 
	 @param flowMark 流程标记
	 @param flowNo 流程编号
	 @return null, 或者流程实体.
	*/
	public static String GetBuessUnitEntityStringByFlowMark(String flowMark, String flowNo)
	{
		BuessUnitBase en = GetBuessUnitEntityByFlowMark(flowMark, flowNo);
		if (en == null)
		{
			return "";
		}
		return en.toString();
	}
	/** 
	 获得业务单元.
	 
	 @param flowMark 流程标记
	 @param flowNo 流程编号
	 @return null, 或者流程实体.
	*/
	public static BuessUnitBase GetBuessUnitEntityByFlowMark(String flowMark, String flowNo)
	{

		if (Htable_BuessUnit == null || Htable_BuessUnit.isEmpty())
		{
			Htable_BuessUnit = new Hashtable();
			ArrayList<BuessUnitBase> al = BP.En.ClassFactory.GetObjects("BP.Sys.BuessUnitBase");
			Htable_BuessUnit.clear();
			for (BuessUnitBase en : al)
			{
				Htable_BuessUnit.put(en.toString(), en);
			}
		}

		for (Object key : Htable_BuessUnit.keySet())
		{
			BuessUnitBase fee = Htable_BuessUnit.get(key) instanceof BuessUnitBase ? (BuessUnitBase)Htable_BuessUnit.get(key) : null;
			if (fee.toString().equals(flowMark) || fee.toString().contains("," + flowNo + ",") == true)
			{
				return fee;
			}
		}
		return null;
	}
	///#endregion 业务单元.

    ///#region 与 表单 事件实体相关.
	private static Hashtable Htable_FormFEE = null;
	/** 
	 获得节点事件实体
	 
	 @param enName 实例名称
	 @return 获得节点事件实体,如果没有就返回为空.
	*/
	public static FormEventBase GetFormEventBaseByEnName(String enName)
	{
		if (Htable_FormFEE == null)
		{
			Htable_FormFEE = new Hashtable();

			ArrayList<FormEventBase> al = BP.En.ClassFactory.GetObjects("BP.Sys.FormEventBase");
			Htable_FormFEE.clear();

			for (FormEventBase en : al)
			{
				Htable_FormFEE.put(en.getFormMark(), en);
			}
		}

		for (Object keyObj : Htable_FormFEE.keySet())
		{
			FormEventBase fee = Htable_FormFEE.get(keyObj) instanceof FormEventBase ? (FormEventBase)Htable_FormFEE.get(keyObj) : null;

			if (keyObj!=null){
				String key = keyObj.toString();
				if(key.contains(","))
				{
					if (key.indexOf(enName + ",") >= 0 || key.length() == key.indexOf("," + enName) + enName.length() + 1)
					{
						return fee;
					}
				}
				
				if (enName.equals(key))
				{
					return fee;
				}
			}
		}
		return null;
	}
	///#endregion 与 表单 事件实体相关.

	///#region 与 表单从表 事件实体相关.
	private static Hashtable Htable_FormFEEDtl = null;
	/** 
	 获得节点事件实体
	 
	 @param enName 实例名称
	 @return 获得节点事件实体,如果没有就返回为空.
	*/
	public static FormEventBaseDtl GetFormDtlEventBaseByEnName(String dtlEnName)
	{
		if (Htable_FormFEEDtl == null || Htable_FormFEEDtl.isEmpty())
		{
			Htable_FormFEEDtl = new Hashtable();
			ArrayList<FormEventBaseDtl> al = BP.En.ClassFactory.GetObjects("BP.Sys.FormEventBaseDtl");
			Htable_FormFEEDtl.clear();
			for (FormEventBaseDtl en : al)
			{
				Htable_FormFEEDtl.put(en.getFormDtlMark(), en);
			}
		}

		for (Object key : Htable_FormFEEDtl.keySet())
		{
			FormEventBaseDtl fee = Htable_FormFEEDtl.get(key) instanceof FormEventBaseDtl ? (FormEventBaseDtl)Htable_FormFEEDtl.get(key) : null;
			if (fee.getFormDtlMark().indexOf(dtlEnName) >= 0 || fee.getFormDtlMark().equals(dtlEnName))
			{
				return fee;
			}
		}
		return null;
	}
	///#endregion 与 表单 事件实体相关.

	///#region 公共变量.
	public static String Plant = "CCFlow";
	/** 
	 部门版本号
	 * @throws Exception 
	*/
	public static String getDeptsVersion() throws Exception
	{
		GloVar en = new GloVar();
		en.setNo("DeptsVersion");
		if (en.RetrieveFromDBSources() == 0)
		{
			en.setName("部门版本号");
			en.setVal(BP.DA.DataType.getCurrentDataTime());
			en.setGroupKey("Glo");
			en.Insert();
		}
		return en.getVal();
	}
	/** 
	 部门数量 - 用于显示ccim的下载进度.
	*/
	public static int getDeptsCount()
	{
		return BP.DA.DBAccess.RunSQLReturnValInt("SELECT COUNT(No) as Num FROM Port_Dept");
	}
	/** 
	 人员数量 - 用于显示ccim的下载进度.
	*/
	public static int getEmpsCount()
	{
		return BP.DA.DBAccess.RunSQLReturnValInt("SELECT COUNT(a.No) as Num FROM Port_Emp a, Port_Dept b WHERE A.FK_Dept=B.No AND A.No NOT IN ('admin','Guest')");
	}
	/** 
	 人员版本号
	 * @throws Exception 
	*/
	public static String getUsersVersion() throws Exception
	{
		GloVar en = new GloVar();
		en.setNo("UsersVersion");
		if (en.RetrieveFromDBSources() == 0)
		{
			en.setName("人员版本号");
			en.setVal(BP.DA.DataType.getCurrentDataTime());
			en.setGroupKey("Glo");
			en.Insert();
		}
		return en.getVal();
	}
	///#endregion 公共变量.

	///#region 写入系统日志(写入的文件:\DataUser\Log\*.*)
	/** 
	 写入一条消息
	 
	 @param msg 消息
	*/
	public static void WriteLineInfo(String msg)
	{
		BP.DA.Log.DefaultLogWriteLineInfo(msg);
	}
	/** 
	 写入一条警告
	 
	 @param msg 消息
	*/
	public static void WriteLineWarning(String msg)
	{
		BP.DA.Log.DefaultLogWriteLineWarning(msg);
	}
	/** 
	 写入一条错误
	 
	 @param msg 消息
	*/
	public static void WriteLineError(String msg)
	{
		BP.DA.Log.DefaultLogWriteLineError(msg);
	}
	///#endregion 写入系统日志

	///#region 写入用户日志(写入的用户表:Sys_UserLog).
	/** 
	 写入用户日志
	 
	 @param logType 类型
	 @param empNo 操作员编号
	 @param msg 信息
	 @param ip IP
	 * @throws Exception 
	*/
	public static void WriteUserLog(String logType, String empNo, String msg, String ip) throws Exception
	{
		UserLog ul = new UserLog();
		ul.setMyPK(DBAccess.GenerGUID());
		ul.setFK_Emp(empNo);
		ul.setLogFlag(logType);
		ul.setDocs(msg);
		ul.setIP(ip);
		ul.setRDT(DataType.getCurrentDataTime());
		ul.Insert();
	}
	/** 
	 写入用户日志
	 
	 @param logType 日志类型
	 @param empNo 操作员编号
	 @param msg 消息
	*/
	public static void WriteUserLog(String logType, String empNo, String msg)
	{
		return;

	}
	///#endregion 写入用户日志.

	

	public static HttpServletRequest getRequest()
	{
		return ContextHolderUtils.getRequest();
	}


	/** 
	 产生消息,senderEmpNo是为了保证写入消息的唯一性，receiveid才是真正的接收者.
	 如果插入失败.
	 
	 @param fromEmpNo 发送人
	 @param now 发送时间
	 @param msg 消息内容
	 @param sendToEmpNo 接受人
	*/
	public static void SendMessageToCCIM(String fromEmpNo, String sendToEmpNo, String msg, String now)
	{
		//暂停对ccim消息提醒的支持.
		return;
	}
	/** 
	 处理生成提示信息,不友好的提示.
	 
	 @param alertInfo 从系统里抛出的错误信息.
	 @return 返回的友好提示信息.
	*/
	public static String GenerFriendlyAlertHtmlInfo(String alertInfo)
	{
		// 格式为: err@错误中文提示信息. tech@info 数据库错误,查询sqL为.
		return alertInfo;
	}

	///#region 加密解密文件.

	
	/**
	 * 文件上传加密
	 * @param fileFullPath上传的文件
	 * @param toFileFullPath 加密的文件
	 * @throws Exception 
	 */
	 ///#region 加密解密文件.
     public static void File_JiaMi(String fileFullPath,String toFileFullPath) throws Exception{
        	 AesEncodeUtil.encryptFile(fileFullPath,toFileFullPath);
         
     }
     
     /**
      * 文件下载解密
      * @param fileFullPath 下载的文件
      * @param toFileFullPath解密的文件
      */
     public static void File_JieMi(String fileFullPath,String toFileFullPath){
        	 AesEncodeUtil.decryptFile(fileFullPath,toFileFullPath);
     }
     /// <summary>
     /// 字符串的解密
     /// </summary>
     /// <param name="str">加密的字符串</param>
     /// <returns>返回解密后的字符串</returns>
     public static String String_JieMi(String str) throws Exception
     {
         //南京宝旺达.
         if (SystemConfig.getCustomerNo().equals("BWDA"))         
             return Cryptos.aesDecrypt(str);
          
         return str;
     }
     public static String String_JieMi_FTP(String str) throws Exception
     {
 
        //南京宝旺达.
       if (SystemConfig.getCustomerNo().equals( "BWDA"))
        	 return Cryptos.aesDecrypt(str);  

         return str;
     }

	///#endregion 加密解密文件.

}