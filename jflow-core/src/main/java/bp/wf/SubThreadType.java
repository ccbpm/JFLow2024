package bp.wf;

/** 
 子线程类型
*/
public enum SubThreadType
{
	/** 
	 同表单
	*/
	SameSheet,
	/** 
	 异表单
	*/
	UnSameSheet;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static SubThreadType forValue(int value) throws Exception
	{
		return values()[value];
	}
}