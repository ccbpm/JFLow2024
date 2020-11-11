package bp.da;

import bp.*;

/** 
 排序方式
*/
public enum OrderWay
{
	/** 
	 升序
	*/
	OrderByUp,
	/** 
	 降序
	*/
	OrderByDown;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static OrderWay forValue(int value) throws Exception
	{
		return values()[value];
	}
}