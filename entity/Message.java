package com.magicbox.api.prescription.utils.entity;

import java.util.ArrayList;
import java.util.Date;

public class Message {



	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public Date getCreatTime() {
		return creatTime;
	}

	public void setCreatTime(Date creatTime) {
		this.creatTime = creatTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public ArrayList<String> getReaded() {
		return readed;
	}

	public void setReaded(ArrayList<String> readed) {
		this.readed = readed;
	}

	String fromUserId;
	String toUserId;
	String fromUserName;
	String toUserName;
	Date creatTime;
	String content;
	String roomId;
	ArrayList<String> readed;// 保存用户id
}
