package com.magicbox.api.prescription.utils.entity;

import java.util.ArrayList;
import java.util.Map;

public class User {
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Map> getConversation() {
		return conversation;
	}
	public void setConversation(ArrayList<Map> conversation) {
		this.conversation = conversation;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMassegeType() {
		return massegeType;
	}
	public void setMassegeType(String massegeType) {
		this.massegeType = massegeType;
	}
	String name;//名字
	ArrayList<Map> conversation;//map{roomId,name:目标人的名字,id:目标人id}
	String session;
	String userId;
	String massegeType;//info通知消息，mes对话消息
}
