package BP.WF.Template;

/** 
 节点属性
 
*/
public class NodeAttr
{

		///#region 新属性
	/** 
	 审核组件状态
	 
	*/
	public static final String FWCSta = "FWCSta";
	/** 
	 阻塞模式
	 
	*/
	public static final String BlockModel = "BlockModel";
	/** 
	 阻塞的表达式
	 
	*/
	public static final String BlockExp = "BlockExp";
	/** 
	 被阻塞时提示信息
	 
	*/
	public static final String BlockAlert = "BlockAlert";
	/** 
	 待办处理模式
	 
	*/
	public static final String TodolistModel = "TodolistModel";
	/**
	 * 组长确认规则
	 */
    public static final String TeamLeaderConfirmRole = "TeamLeaderConfirmRole";
    /**
     * 组长确认内容
     */
    public static final String TeamLeaderConfirmDoc = "TeamLeaderConfirmDoc";
	/** 
	 当没有找到处理人时处理方式
	 
	*/
	public static final String WhenNoWorker = "WhenNoWorker";
	/** 
	 子线程类型
	 
	*/
	public static final String SubThreadType = "SubThreadType";
	/** 
	 是否可以隐性退回
	 
	*/
	public static final String IsCanHidReturn = "IsCanHidReturn";
	/** 
	 通过率
	 
	*/
	public static final String PassRate = "PassRate";
	/** 
	 是否可以设置流程完成
	 
	*/
	public static final String IsCanOver = "IsCanOver";
	/** 
	 是否是保密步骤
	 
	*/
	public static final String IsSecret = "IsSecret";
 //   public const string IsCCNode = "IsCCNode";
	public static final String IsCCFlow = "IsCCFlow";
	public static final String HisStas = "HisStas";
	public static final String HisToNDs = "HisToNDs";
	public static final String HisBillIDs = "HisBillIDs";
	public static final String NodePosType = "NodePosType";
	public static final String HisDeptStrs = "HisDeptStrs";
	public static final String HisEmps = "HisEmps";
	public static final String GroupStaNDs = "GroupStaNDs";
	public static final String IsHandOver = "IsHandOver";
	public static final String IsCanDelFlow = "IsCanDelFlow";
	/** 
	 是否可以原路返回
	 
	*/
	public static final String IsBackTracking = "IsBackTracking";
	/** 
	 退回原因(多个原因使用@符号分开.)
	 
	*/
	public static final String ReturnReasonsItems = "ReturnReasonsItems";
	
	/**
	 * 单节点退回规则
	 */
	public static final String ReturnOneNodeRole="ReturnOneNodeRole";
	
	/** 
	 退回提示
	*/
	public static final String ReturnAlert = "ReturnAlert";
	/** 
	 是否启用投递路径自动记忆功能?
	 
	*/
	public static final String IsRM = "IsRM";
	public static final String FormType = "FormType";
	public static final String FormUrl = "FormUrl";
	 
	/** 
	 发送之前的信息提示
	 
	*/
	public static final String BeforeSendAlert = "BeforeSendAlert";
	/** 
	 是否可以强制删除线程
	 
	*/
	public static final String ThreadKillRole = "ThreadKillRole";
	/** 
	 接受人sql
	 
	*/
	public static final String DeliveryParas = "DeliveryParas";
	/** 
	 退回规则
	 
	*/
	public static final String ReturnRole = "ReturnRole";
	/** 
	 可触发子流程
	 
	*/
	public static final String SFActiveFlows = "SFActiveFlows";
	/** 
	 转向处理
	 
	*/
	public static final String TurnToDeal = "TurnToDeal";
	/** 
	 考核方式
	 
	*/
	public static final String CHWay = "CHWay";
	/** 
	 工作量
	 
	*/
	public static final String WorkloadDel = "Workload";
	/** 
	 公文左边词语
	 
	*/
	public static final String DocLeftWord = "DocLeftWord";
	/** 
	 公文右边词语
	 
	*/
	public static final String DocRightWord = "DocRightWord";

		///#endregion


		
	/** 
	 OID
	 
	*/
	public static final String NodeID = "NodeID";
	/** 
	 节点的流程
	 
	*/
	public static final String FK_Flow = "FK_Flow";
	/** 
	 操作提示
	 
	*/
	public static final String Tip = "Tip";
	/** 
	 FK_FlowSort
	 
	*/
	public static final String FK_FlowSort = "FK_FlowSort";
	/** 
	 FK_FlowSortT
	 
	*/
	public static final String FK_FlowSortT = "FK_FlowSortT";
	/** 
	 流程名
	 
	*/
	public static final String FlowName = "FlowName";
	/** 
	 是否分配工作
	 
	*/
	public static final String IsTask = "IsTask";
	/** 
	 是否是业务单元？
	 
	*/
	public static final String IsBUnit = "IsBUnit";
	/** 
	 节点工作类型
	 
	*/
	public static final String NodeWorkType = "NodeWorkType";
	/** 
	 节点的描述
	 
	*/
	public static final String Name = "Name";
	/** 
	 x
	 
	*/
	public static final String X = "X";
	/** 
	 y
	 
	*/
	public static final String Y = "Y";
	/** 
	 限期小时
	 
	*/
	public static final String TSpanHour = "TSpanHour";
	/** 
	 限期天
	 
	*/
	public static final String TimeLimit = "TimeLimit";
	/** 
	 时间计算方式
	 
	*/
	public static final String TWay = "TWay";
	/** 
	 逾期提示规则
	 
	*/
	public static final String TAlertRole = "TAlertRole";
	/** 
	 逾期提示方式
	 
	*/
	public static final String TAlertWay = "TAlertWay";
	/** 
	 预警小时
	 
	*/
	public static final String WarningDay = "WarningDay";
	/** 
	 小时
	 
	*/
	public static final String WarningHour = "WarningHour";
	/** 
	 预警提示规则
	 
	*/
	public static final String WAlertRole = "WAlertRole";
	/** 
	 预警提示方式
	 
	*/
	public static final String WAlertWay = "WAlertWay";
	/** 
	 扣分
	 
	*/
	public static final String TCent = "TCent";
	/** 
	 流程步骤
	 
	*/
	public static final String Step = "Step";
	/** 
	 工作内容
	 
	*/
	public static final String Doc = "Doc";
	/** 
	  物理表名
	 
	*/
	public static final String PTable = "PTable";
	/** 
	 签字类型
	 
	*/
	public static final String SignType = "SignType";
	/** 
	 显示的表单
	 
	*/
	public static final String ShowSheets = "ShowSheets";
	/** 
	 运行模式
	 
	*/
	public static final String RunModel = "RunModel";
	/** 
	 谁执行它？
	 
	*/
	public static final String WhoExeIt = "WhoExeIt";
	/** 
	 IsSubFlow
	 
	*/
	public static final String HisSubFlows = "HisSubFlows";
	/** 
	 超时处理内容
	 
	*/
	public static final String DoOutTime = "DoOutTime";
	/** 
	 超时处理内容
	 
	*/
	public static final String OutTimeDeal = "OutTimeDeal";
	/** 
	 执行超时的条件
	 
	*/
	public static final String DoOutTimeCond = "DoOutTimeCond";
	/** 
	 是否允许子线程接受人员重复？
	 
	*/
	public static final String IsAllowRepeatEmps = "IsAllowRepeatEmps";
	/** 
	 是否启动自动运行？
	 
	*/
	public static final String AutoRunEnable = "AutoRunEnable";
	/** 
	 自动运行参数
	 
	*/
	public static final String AutoRunParas = "AutoRunParas";
	/** 
	 属性
	 
	*/
	public static final String FrmAttr = "FrmAttr";
	/** 
	 个性化发送信息
	 
	*/
	public static final String TurnToDealDoc = "TurnToDealDoc";
	/** 
	 访问规则
	 
	*/
	public static final String DeliveryWay = "DeliveryWay";
	/** 
	 本节点接收人不允许包含上一步发送人
	 
	*/
	public static final String IsExpSender = "IsExpSender";
	/** 
	 焦点字段
	 
	*/
	public static final String FocusField = "FocusField";
	/** 
	 节点表单ID
	 
	*/
	public static final String NodeFrmID = "NodeFrmID";
	/** 
	 跳转规则
	 
	*/
	public static final String JumpWay = "JumpWay";
	/** 
	 可跳转的节点
	 
	*/
	public static final String JumpToNodes = "JumpToNodes";
	/** 
	 已读回执
	 
	*/
	public static final String ReadReceipts = "ReadReceipts";
	/** 
	 操送规则
	 
	*/
	public static final String CCRole = "CCRole";
	/** 
	 保存模式
	 
	*/
	public static final String SaveModel = "SaveModel";
	/** 
	 方向条件控制规则
	 
	*/
	public static final String CondModel = "CondModel";
	/** 
	 子流程启动方式
	 
	*/
	public static final String SubFlowStartWay = "SubFlowStartWay";
	/** 
	 子流程启动参数
	 
	*/
	public static final String SubFlowStartParas = "SubFlowStartParas";
	/** 
	 是否工作质量考核点
	 
	*/
	public static final String IsEval = "IsEval";
	/** 
	 撤销规则
	 
	*/
	public static final String CancelRole = "CancelRole";
	
	public static final String CancelDisWhenRead = "CancelDisWhenRead";	
	/** 
	 抄送数据写入规则
	 
	*/
	public static final String CCWriteTo = "CCWriteTo";
	/** 
	 批处理
	 
	*/
	public static final String BatchRole = "BatchRole";
	/** 
	 批处理参数
	 
	*/
	public static final String BatchParas = "BatchParas";
	/** 
	 批处理总数
	 
	*/
	public static final String BatchListCount = "BatchListCount";
	/** 
	 自动跳转规则-1
	 
	*/
	public static final String AutoJumpRole0 = "AutoJumpRole0";
	/** 
	 自动跳转规则-2
	 
	*/
	public static final String AutoJumpRole1 = "AutoJumpRole1";
	/** 
	 自动跳转规则-3
	 
	*/
	public static final String AutoJumpRole2 = "AutoJumpRole2";
	/** 
	 是否是客户执行节点?
	 
	*/
	public static final String IsGuestNode = "IsGuestNode";
	/** 
	 打印单据方式
	 
	*/
	public static final String PrintDocEnable = "PrintDocEnable";
	/** 
	 icon头像
	 
	*/
	public static final String ICON = "ICON";
	/** 
	 自定义参数字段
	 
	*/
	public static final String SelfParas = "SelfParas";

		///#endregion


		///#region 父子流程
	/** 
	 (当前节点为启动子流程节点时)是否检查所有子流程结束后,该节点才能向下发送?
	 
	*/
	public static final String IsCheckSubFlowOver_del = "IsCheckSubFlowOver_del";

		///#endregion


		///#region 移动设置.
	/** 
	 手机工作模式
	 
	*/
	public static final String MPhone_WorkModel = "MPhone_WorkModel";
	/** 
	 手机屏幕模式
	 
	*/
	public static final String MPhone_SrcModel = "MPhone_SrcModel";
	/** 
	 pad工作模式
	 
	*/
	public static final String MPad_WorkModel = "MPad_WorkModel";
	/** 
	 pad屏幕模式
	 
	*/
	public static final String MPad_SrcModel = "MPad_SrcModel";

		///#endregion 移动设置.



		///#region 未来处理人.
	/** 
	 是否计算未来处理人
	 
	*/
	public static final String IsFullSA = "IsFullSA";
	/** 
	 是否计算未来处理人的处理时间.
	 
	*/
	public static final String IsFullSATime = "IsFullSATime";
	/** 
	 是否接受未来处理人的消息提醒.
	 
	*/
	public static final String IsFullSAAlert = "IsFullSAAlert";
	
	public static final String RefOneFrmTreeType = "RefOneFrmTreeType";

		///#endregion 未来处理人.

}