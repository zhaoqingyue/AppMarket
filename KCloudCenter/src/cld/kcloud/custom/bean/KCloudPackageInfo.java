package cld.kcloud.custom.bean;

public class KCloudPackageInfo {
	private int combo_code;			// �ײͱ���
	private String combo_name;		// �ײ�����
	private String combo_icon;		// �ײ�ͼ��URL��ַ
	private String combo_desc;		// �ײ�����
	private int charges;			// �۸�
	private int pay_times;			// �������
	private int flow;				// �ײ�����
	private long endtime;			// ����ʱ�� 
	private int status;				// 0��δ����; 1��������; 2���ѹ���; 3���ѽ��ã���̨���ã�
	private int number;             // ��ͬ�ײ͸���
	
	public KCloudPackageInfo() {
		this.combo_code = 0;
		this.combo_name = "";
		this.combo_icon = "";
		this.combo_desc = "";
		this.charges = 0;
		this.pay_times = 0;
		this.flow = 0;
		this.endtime = 0;
		this.status = 0;
		this.number = 1;
	}
	
	public KCloudPackageInfo(KCloudPackageInfo packageInfo) {
		this.combo_code = packageInfo.combo_code;
		this.combo_name = packageInfo.combo_name;
		this.combo_icon = packageInfo.combo_icon;
		this.combo_desc = packageInfo.combo_desc;
		this.charges = packageInfo.charges;
		this.pay_times = packageInfo.pay_times;
		this.flow = packageInfo.flow;
		this.endtime = packageInfo.endtime;
		this.status = packageInfo.status;
		this.number = packageInfo.number;
	}
	
	public KCloudPackageInfo(int combo_code, String combo_name, String combo_icon, 
			String combo_desc, int charges, int pay_times, int flow, long endtime, 
			int status, int number) {
		this.combo_code = combo_code;
		this.combo_name = combo_name;
		this.combo_icon = combo_icon;
		this.combo_desc = combo_desc;
		this.charges = charges;
		this.pay_times = pay_times;
		this.flow = flow;
		this.endtime = endtime;
		this.status = status;
		this.number = number;
	}
	
	public void setComboCode(int combo_code){
		this.combo_code = combo_code;
	}
	
	public int getComboCode(){
		return this.combo_code;
	}
	
	public void setComboName(String combo_name){
		this.combo_name = combo_name;
	}
	
	public String getComboName(){
		return this.combo_name;
	}
	
	public void setComboIcon(String combo_icon){
		this.combo_icon = combo_icon;
	}
	
	public String getComboIcon(){
		return this.combo_icon;
	}
	
	public void setComboDesc(String combo_desc){
		this.combo_desc = combo_desc;
	}
	
	public String getComboDesc(){
		return this.combo_desc;
	}
	
	public void setCharges(int charges){
		this.charges = charges;
	}
	
	public int getCharges(){
		return this.charges;
	}
	
	public void setPayTimes(int pay_times){
		this.pay_times = pay_times;
	}
	
	public int getPayTimes(){
		return this.pay_times;
	}
	
	public void setFlow(int flow){
		this.flow = flow;
	}
	
	public int getFlow(){
		return this.flow;
	}
	
	public void setEndtime(long endtime) {
		this.endtime = endtime;
	}
	
	public long getEndtime() {
		return this.endtime;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return this.number;
	}
}