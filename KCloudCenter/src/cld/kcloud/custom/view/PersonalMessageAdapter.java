package cld.kcloud.custom.view;

import java.util.List;
import com.cld.log.CldLog;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.center.R;
import cld.kcloud.fragment.PersonalMessageFragment;
import cld.kcloud.utils.KCloudCommonUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PersonalMessageAdapter extends BaseAdapter{

	private List<PersonalMessage> data;
	private LayoutInflater mInflater = null;
	final private PersonalMessageFragment mfragment;
	
	public PersonalMessageAdapter(Context context, PersonalMessageFragment fragment, List<PersonalMessage> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mfragment = fragment;
		this.data = data;
	}
	
	class ViewHolder {
		public ImageView imgFlag;
		public TextView tvMsgType;
		public TextView tvTime;
		public TextView tvMsgContent;
		public TextView tvClickToWatch;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return data.get(position).getMsgId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.layout_user_message, null);
			holder.imgFlag = (ImageView)convertView.findViewById(R.id.usermessage_img_flag);
			holder.tvMsgType = (TextView)convertView.findViewById(R.id.usermessage_text_msg_type);
			holder.tvTime = (TextView)convertView.findViewById(R.id.usermessage_text_time);
			holder.tvMsgContent = (TextView)convertView.findViewById(R.id.usermessage_text_msg_content);
			holder.tvClickToWatch = (TextView)convertView.findViewById(R.id.usermessage_text_click_towatch);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		// 消息类型
		if (data.get(position).getMsgType() == 1) {
			holder.tvMsgType.setText(KCloudCommonUtil.getString(R.string.msg_type_message));
		} else if (data.get(position).getMsgType() == 2) {
			holder.tvMsgType.setText(KCloudCommonUtil.getString(R.string.msg_type_upgrade));
		}
		
		if (data.get(position).getMsgMark() != 3) {
			holder.imgFlag.setImageResource(R.drawable.img_message_state_true);
			holder.tvMsgContent.setTextColor(
					KCloudCommonUtil.getColor(R.color.text_hightlight_color));
			holder.tvClickToWatch.setTextColor(
					KCloudCommonUtil.getColor(R.color.text_green_color));
		} else {
			holder.imgFlag.setImageResource(R.drawable.img_message_state_false);
			holder.tvMsgContent.setTextColor(
					KCloudCommonUtil.getColor(R.color.text_normal_color));
			holder.tvClickToWatch.setTextColor(
					KCloudCommonUtil.getColor(R.color.text_gray_color_7a));
		}
		
		// 选项item
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				data.get(position).setMsgMark(3);
				mfragment.gotoMessageDetail(position);
			}
		});
		
		// 时间 
		holder.tvTime.setText(getDisplayTime(data.get(position).getTime()));

		// 内容
		holder.tvMsgContent.setText(data.get(position).getMsgContent());
		
		//"点击查看"的点击事件
		holder.tvClickToWatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				data.get(position).setMsgMark(3);
				mfragment.gotoMessageDetail(position);
			}
		});
		
		return convertView;
	}
	
	public boolean find(long id) {
		if (data.isEmpty()) {
			return false;
		}
		
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getMsgId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressLint("DefaultLocale") 
	private String getDisplayTime(long downloadTime) {
		String dispalyTime = "";
		
		long diff = System.currentTimeMillis() - downloadTime*1000L;
		long day = diff / (1000*60*60*24);
		long hour = (diff - day*1000*60*60*24) / (1000*60*60);
		long minute = (diff - day*1000*60*60*24 - hour*1000*60*60) / (1000*60); 
		
		if (day == 0 && hour == 0) {
			if (minute > 1) {
				dispalyTime = String.format(KCloudCommonUtil.getString(R.string.msg_updated_minutes_ago), minute);
			} else {
				dispalyTime = String.format(KCloudCommonUtil.getString(R.string.msg_updated_just));
			}
		} else if (day != 0) {
			if (day <= 30) {
				dispalyTime = String.format(KCloudCommonUtil.getString(R.string.msg_updated_days_ago), day);
			} else {
				dispalyTime = String.format(KCloudCommonUtil.getString(R.string.msg_updated_months_ago), day/30);
			}
		} else if (hour != 0) {
			dispalyTime = String.format(KCloudCommonUtil.getString(R.string.msg_updated_hours_ago), hour);
		}
		
		CldLog.i("PersonalMessageAdapter", "downloadTime = " + downloadTime);
		CldLog.i("PersonalMessageAdapter", "dispalyTime = " + day + ", " + hour + ", " + minute + ", " + dispalyTime);
		return dispalyTime;
	}
}
