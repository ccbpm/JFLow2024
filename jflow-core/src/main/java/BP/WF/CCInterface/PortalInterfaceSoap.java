package BP.WF.CCInterface;

import BP.WF.*;

//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:4.0.30319.42000
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------




//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")][System.ServiceModel.ServiceContractAttribute(ConfigurationName="CCInterface.PortalInterfaceSoap")] public interface PortalInterfaceSoap
public interface PortalInterfaceSoap
{

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/SendToWebServices", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool SendToWebServices(string msgPK, string sender, string sendToEmpNo, string tel, string msgInfo, string title, string openUrl);
	boolean SendToWebServices(String msgPK, String sender, String sendToEmpNo, String tel, String msgInfo, String title, String openUrl);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action = "http://tempuri.org/SendToCCMSG", ReplyAction = "*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults = true)] bool SendToCCMSG(string msgPK, string sender, string sendToEmpNo, string tel, string msgInfo, string title, string openUrl);
	boolean SendToCCMSG(String msgPK, String sender, String sendToEmpNo, String tel, String msgInfo, String title, String openUrl);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/SendWhen", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool SendWhen(string flowNo, int nodeID, long workid, string userNo, string userName);
	boolean SendWhen(String flowNo, int nodeID, long workid, String userNo, String userName);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/FlowOverBefore", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool FlowOverBefore(string flowNo, int nodeID, long workid, string userNo, string userName);
	boolean FlowOverBefore(String flowNo, int nodeID, long workid, String userNo, String userName);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/SendToDingDing", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool SendToDingDing(string mypk, string sender, string sendToEmpNo, string tel, string msgInfo, string title, string openUrl);
	boolean SendToDingDing(String mypk, String sender, String sendToEmpNo, String tel, String msgInfo, String title, String openUrl);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/SendToWeiXin", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool SendToWeiXin(string mypk, string sender, string sendToEmpNo, string tel, string msgInfo, string title, string openUrl);
	boolean SendToWeiXin(String mypk, String sender, String sendToEmpNo, String tel, String msgInfo, String title, String openUrl);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/SendToEmail", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool SendToEmail(string mypk, string sender, string sendToEmpNo, string email, string title, string maildoc);
	boolean SendToEmail(String mypk, String sender, String sendToEmpNo, String email, String title, String maildoc);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/SendToCCIM", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool SendToCCIM(string mypk, string sender, string sendToEmpNo, string tel, string msgInfo, string title, string openUrl);
	boolean SendToCCIM(String mypk, String sender, String sendToEmpNo, String tel, String msgInfo, String title, String openUrl);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/Print", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] void Print(string billFilePath);
	void Print(String billFilePath);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/WriteUserSID", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] bool WriteUserSID(string miyue, string userNo, string sid);
	boolean WriteUserSID(String miyue, String userNo, String sid);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/CheckUserNoPassWord", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] int CheckUserNoPassWord(string userNo, string password);
	int CheckUserNoPassWord(String userNo, String password);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetDept", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetDept(string deptNo);
	System.Data.DataTable GetDept(String deptNo);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetDepts", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetDepts();
	System.Data.DataTable GetDepts();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetDeptsByParentNo", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetDeptsByParentNo(string parentDeptNo);
	System.Data.DataTable GetDeptsByParentNo(String parentDeptNo);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetStations", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetStations();
	System.Data.DataTable GetStations();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetStation", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetStation(string stationNo);
	System.Data.DataTable GetStation(String stationNo);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetEmps", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetEmps();
	System.Data.DataTable GetEmps();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetEmpsByDeptNo", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetEmpsByDeptNo(string deptNo);
	System.Data.DataTable GetEmpsByDeptNo(String deptNo);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetEmp", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetEmp(string no);
	System.Data.DataTable GetEmp(String no);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetDeptEmp", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetDeptEmp();
	System.Data.DataTable GetDeptEmp();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetEmpHisDepts", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetEmpHisDepts(string empNo);
	System.Data.DataTable GetEmpHisDepts(String empNo);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetEmpHisStations", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetEmpHisStations(string empNo);
	System.Data.DataTable GetEmpHisStations(String empNo);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GetDeptEmpStations", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GetDeptEmpStations();
	System.Data.DataTable GetDeptEmpStations();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GenerEmpsByStations", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GenerEmpsByStations(string stationNos);
	System.Data.DataTable GenerEmpsByStations(String stationNos);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GenerEmpsByDepts", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GenerEmpsByDepts(string deptNos);
	System.Data.DataTable GenerEmpsByDepts(String deptNos);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/GenerEmpsBySpecDeptAndStats", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] System.Data.DataTable GenerEmpsBySpecDeptAndStats(string deptNo, string stations);
	System.Data.DataTable GenerEmpsBySpecDeptAndStats(String deptNo, String stations);

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/SendSuccess", ReplyAction="*")][System.ServiceModel.XmlSerializerFormatAttribute(SupportFaults=true)] string SendSuccess(string flowNo, int nodeID, long workid, string userNo, string userName);
	String SendSuccess(String flowNo, int nodeID, long workid, String userNo, String userName);
}