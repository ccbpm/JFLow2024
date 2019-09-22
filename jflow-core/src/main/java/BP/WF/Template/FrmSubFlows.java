package BP.WF.Template;

import BP.DA.*;
import BP.En.*;
import BP.WF.Template.*;
import BP.WF.*;
import BP.Sys.*;
import BP.WF.*;
import java.util.*;

/**
 * 父子流程s
 */
public class FrmSubFlows extends Entities {
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #region 构造
	/**
	 * 父子流程s
	 */
	public FrmSubFlows() {
	}

	/**
	 * 父子流程s
	 * 
	 * @param fk_mapdata
	 *            s
	 * @throws Exception 
	 */
	public FrmSubFlows(String fk_mapdata) throws Exception {

		this.RetrieveFromCash("No", fk_mapdata);

	}

	/**
	 * 得到它的 Entity
	 */
	@Override
	public Entity getNewEntity() {
		return new FrmSubFlow();
	}
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #endregion

	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #region 为了适应自动翻译成java的需要,把实体转换成List
	/**
	 * 转化成 java list,C#不能调用.
	 * 
	 * @return List
	 */
	public final List<FrmSubFlow> ToJavaList() {
		return (List<FrmSubFlow>)(Object)this;
	}

	/**
	 * 转化成list
	 * 
	 * @return List
	 */
	public final ArrayList<FrmSubFlow> Tolist() {
		ArrayList<FrmSubFlow> list = new ArrayList<FrmSubFlow>();
		for (int i = 0; i < this.size(); i++) {
			list.add((FrmSubFlow) this.get(i));
		}
		return list;
	}
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #endregion 为了适应自动翻译成java的需要,把实体转换成List.
}