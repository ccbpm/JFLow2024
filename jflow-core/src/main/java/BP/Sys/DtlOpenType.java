package BP.Sys;

public enum DtlOpenType
{
	/** 
	 对人员开放
	*/
	ForEmp,
	/** 
	 对工作开放
	*/
	ForWorkID,
	/** 
	 对流程开放
	*/
	ForFID,
	ForPWorkID,
	ForP2WorkID,
	ForP3WorkID;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static DtlOpenType forValue(int value)
	{
		return values()[value];
	}
}