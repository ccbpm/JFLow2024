package BP.WF;

import BP.DA.*;
import BP.En.*;
import BP.Sys.*;
import BP.Port.*;
import BP.WF.XML.*;
import BP.WF.Template.*;

import java.text.ParseException;
import java.time.*;

/**
 * WorkBase 的摘要说明。 工作
 */
public abstract class Work extends Entity {
	/**
	 * 检查MD5值是否通过
	 * 
	 * @return true/false
	 */
	public final boolean IsPassCheckMD5() {
		String md51 = this.GetValStringByKey(WorkAttr.MD5);
		String md52 = Glo.GenerMD5(this);
		if (!md51.equals(md52)) {
			return false;
		}
		return true;
	}

	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #region 基本属性(必须的属性)
	/**
	 * 主键
	 */
	@Override
	public String getPK() {
		return "OID";
	}

	/**
	 * classID
	 */
	@Override
	public String getClassID() {
		return "ND" + this.getHisNode().getNodeID();
	}

	/**
	 * 流程ID
	 */
	public long getFID() {
		if (this.getHisNode().getHisRunModel() != RunModel.SubThread) {
			return 0;
		}
		return this.GetValInt64ByKey(WorkAttr.FID);
	}

	public void setFID(long value) {
		if (this.getHisNode().getHisRunModel() != RunModel.SubThread) {
			this.SetValByKey(WorkAttr.FID, 0);
		} else {
			this.SetValByKey(WorkAttr.FID, value);
		}
	}

	/**
	 * workid,如果是空的就返回 0 .
	 */
	public long getOID() {
		return this.GetValInt64ByKey(WorkAttr.OID);
	}

	public void setOID(long value) {
		this.SetValByKey(WorkAttr.OID, value);
	}

	/**
	 * 完成时间
	 */
	public final String getCDT() {
		String str = this.GetValStringByKey(WorkAttr.CDT);
		if (str.length() < 5) {
			this.SetValByKey(WorkAttr.CDT, DataType.getCurrentDataTime());
		}

		return this.GetValStringByKey(WorkAttr.CDT);
	}

	/**
	 * 人员emps
	 */
	public final String getEmps() {
		return this.GetValStringByKey(WorkAttr.Emps);
	}

	public final void setEmps(String value) {
		this.SetValByKey(WorkAttr.Emps, value);
	}

	public final int RetrieveFID() throws Exception {
		QueryObject qo = new QueryObject(this);
		qo.AddWhereIn(WorkAttr.OID, "(" + this.getFID() + "," + this.getOID() + ")");
		int i = qo.DoQuery();
		if (i == 0) {

			this.CheckPhysicsTable();
			throw new RuntimeException("@节点[" + this.getEnDesc() + "]数据丢失：WorkID=" + this.getOID() + " FID="
					+ this.getFID() + " sql=" + qo.getSQL());

		}
		return i;
	}

	/**
	 * 记录时间
	 */
	public final String getRDT() {
		return this.GetValStringByKey(WorkAttr.RDT);
	}

	public final String getRDT_Date() {
		try {
			return DataType.ParseSysDate2DateTime(this.getRDT()).toString(DataType.SysDataFormat);
		} catch (java.lang.Exception e) {
			return DataType.getCurrentDate();
		}
	}

	public final LocalDateTime getRDT_DateTime() {
		try {
			return DataType.ParseSysDate2DateTime(this.getRDT_Date());
		} catch (java.lang.Exception e) {
			return LocalDateTime.now();
		}
	}

	public final String getRecord_FK_NY() {
		return this.getRDT().substring(0, 7);
	}

	/**
	 * 记录人
	 */
	public final String getRec() {
		String str = this.GetValStringByKey(WorkAttr.Rec);
		if (str.equals("")) {
			this.SetValByKey(WorkAttr.Rec, WebUser.getNo());
		}

		return this.GetValStringByKey(WorkAttr.Rec);
	}

	public final void setRec(String value) {
		this.SetValByKey(WorkAttr.Rec, value);
	}

	/**
	 * 工作人员
	 */
	public final Emp getRecOfEmp() {
		return new Emp(this.getRec());
	}

	/**
	 * 记录人名称
	 */
	public final String getRecText() {
		try {
			return this.getHisRec().getName();
		} catch (java.lang.Exception e) {
			return this.getRec();
		}
	}

	public final void setRecText(String value) {
		this.SetValByKey("RecText", value);
	}

	private Node _HisNode = null;

	/**
	 * 工作的节点.
	 */
	public final Node getHisNode() {
		if (this._HisNode == null) {
			this._HisNode = new Node(this.getNodeID());
		}
		return _HisNode;
	}

	public final void setHisNode(Node value) {
		_HisNode = value;
	}

	/**
	 * 从表.
	 */
	public final MapDtls getHisMapDtls() {
		return this.getHisNode().getMapData().getMapDtls();
	}

	/**
	 * 从表.
	 */
	public final FrmAttachments getHisFrmAttachments() {
		return this.getHisNode().getMapData().getFrmAttachments();
	}
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #endregion

	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #region 扩展属性
	/**
	 * 跨度天数
	 * @throws ParseException 
	 */
	public final int getSpanDays() throws ParseException {
		if (this.getCDT().equals(this.getRDT())) {
			return 0;
		}
		return DataType.SpanDays(this.getRDT(), this.getCDT());
	}

	/**
	 * 得到从工作完成到现在的日期
	 * 
	 * @return
	 * @throws ParseException 
	 */
	public final int GetCDTimeLimits(String todata) throws ParseException {
		return DataType.SpanDays(this.getCDT(), todata);
	}

	/**
	 * 他的记录人
	 */
	public final Emp getHisRec() {
		// return new Emp(this.Rec);
		Object tempVar = this.GetValByKey("HisRec" + this.getRec());
		Emp emp = tempVar instanceof Emp ? (Emp) tempVar : null;
		if (emp == null) {
			emp = new Emp(this.getRec());
			this.SetValByKey("HisRec" + this.getRec(), emp);
		}
		return emp;
	}
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #endregion

	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #region 构造函数
	/**
	 * 工作
	 */
	protected Work() {
	}

	/**
	 * 工作
	 * 
	 * @param oid
	 *            WFOID
	 * @throws Exception 
	 */
	protected Work(long oid) throws Exception {
		this.SetValByKey(EntityOIDAttr.OID, oid);
		this.Retrieve();
	}
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #endregion

	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #region 重写基类的方法。
	/**
	 * 按照指定的OID Insert.
	 */
	public final void InsertAsOID(long oid) {
		this.SetValByKey("OID", oid);
		this.RunSQL(SqlBuilder.Insert(this));
	}

	/**
	 * 按照指定的OID 保存
	 * 
	 * @param oid
	 * @throws Exception 
	 */
	public final void SaveAsOID(long oid) throws Exception {
		this.SetValByKey("OID", oid);
		if (this.RetrieveNotSetValues().Rows.size() == 0) {
			this.InsertAsOID(oid);
		}
		this.Update();
	}

	/**
	 * 保存实体信息
	 * @throws Exception 
	 */
	// C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s
	// shadowing via the 'new' keyword:
	// ORIGINAL LINE: public new int Save()
	public final int Save() throws Exception {
		if (this.getOID() <= 10) {
			throw new RuntimeException("@没有给WorkID赋值,不能保存.");
		}
		if (this.Update() == 0) {
			this.InsertAsOID(this.getOID());
			return 0;
		}
		return 1;
	}

	@Override
	public void Copy(DataRow dr) {
		for (Attr attr : this.getEnMap().getAttrs()) {
			if (WorkAttr.CDT.equals(attr.getKey()) || WorkAttr.RDT.equals(attr.getKey()) || WorkAttr.Rec.equals(attr.getKey())
					|| WorkAttr.FID.equals(attr.getKey()) || WorkAttr.OID.equals(attr.getKey()) || attr.getKey().equals("No")
					|| attr.getKey().equals("Name")) {
				continue;
			}

			try {
				this.SetValByKey(attr.getKey(), dr.get(attr.getKey()));
			} catch (java.lang.Exception e) {
			}
		}
	}

	@Override
	public void Copy(Entity fromEn) {
		if (fromEn == null) {
			return;
		}
		Attrs attrs = fromEn.getEnMap().getAttrs();
		for (Attr attr : attrs) {
			if (WorkAttr.CDT.equals(attr.getKey()) || WorkAttr.RDT.equals(attr.getKey()) || WorkAttr.Rec.equals(attr.getKey())
					|| WorkAttr.FID.equals(attr.getKey()) || WorkAttr.OID.equals(attr.getKey()) || WorkAttr.Emps.equals(attr.getKey())
					|| attr.getKey().equals("No") || attr.getKey().equals("Name")) {
				continue;
			}
			this.SetValByKey(attr.getKey(), fromEn.GetValByKey(attr.getKey()));
		}
	}

	/**
	 * 删除主表数据也要删除它的明细数据
	 * @throws Exception 
	 */
	@Override
	protected void afterDelete() throws Exception {
		// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		/// #warning 删除了明细，有可能造成其他的影响.
		// MapDtls dtls = this.HisNode.MapData.MapDtls;
		// foreach (MapDtl dtl in dtls)。
		// DBAccess.RunSQL("DELETE FROM " + dtl.getPTable() + " WHERE RefPK=" +
		// this.OID);
		super.afterDelete();
	}
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #endregion

	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #region 公共方法
	/**
	 * 更新之前
	 * 
	 * @return
	 * @throws Exception 
	 */
	@Override
	protected boolean beforeUpdate() throws Exception {
		return super.beforeUpdate();
	}

	/**
	 * 直接的保存前要做的工作
	 */
	public void BeforeSave() {
	 

		// 执行保存前的事件。
		this.getHisNode().getHisFlow().DoFlowEventEntity(EventListOfNode.SaveBefore, this.getHisNode(),
				this.getHisNode().getHisWork(), "@WorkID=" + this.getOID() + "@FID=" + this.getFID());
	}

	/**
	 * 直接的保存
	 */
	// C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s
	// shadowing via the 'new' keyword:
	// ORIGINAL LINE: public new void DirectSave()
	public final void DirectSave() {
		this.beforeUpdateInsertAction();
		if (this.DirectUpdate() == 0) {
			this.SetValByKey(WorkAttr.RDT, LocalDateTime.now().toString("yyyy-MM-dd"));
			this.DirectInsert();
		}
	}

	public String NodeFrmID = "";
	protected int _nodeID = 0;

	public final int getNodeID() {
		if (_nodeID == 0) {
			throw new RuntimeException("您没有给_Node给值。");
		}
		return this._nodeID;
	}

	public final void setNodeID(int value) {
		if (this._nodeID != value) {
			this._nodeID = value;
			this.set_enMap( null);
		}
		this._nodeID = value;
	}

	/**
	 * 已经路过的节点
	 */
	public String HisPassedFrmIDs = "";
	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	/// #endregion
}