package BP.WF.CloudWS;

import BP.WF.*;

//------------------------------------------------------------------------------
// <auto-generated>
//     此代码由工具生成。
//     运行时版本:4.0.30319.42000
//
//     对此文件的更改可能会导致不正确的行为，并且如果
//     重新生成代码，这些更改将会丢失。
// </auto-generated>
//------------------------------------------------------------------------------


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Diagnostics.DebuggerStepThroughAttribute()][System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")][System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)][System.ServiceModel.MessageContractAttribute(WrapperName="GetFlowXML", WrapperNamespace="http://tempuri.org/", IsWrapped=true)] public partial class GetFlowXMLRequest
public class GetFlowXMLRequest
{

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.MessageBodyMemberAttribute(Namespace="http://tempuri.org/", Order=0)] public bool isPublic;
	public boolean isPublic;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.ServiceModel.MessageBodyMemberAttribute(Namespace="http://tempuri.org/", Order=1)] public string guid;
	public String guid;

	public GetFlowXMLRequest()
	{
	}

	public GetFlowXMLRequest(boolean isPublic, String guid)
	{
		this.isPublic = isPublic;
		this.guid = guid;
	}
}