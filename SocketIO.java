package com.magicbox.api.prescription.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Conventions;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.magicbox.api.prescription.utils.entity.Message;
import com.magicbox.api.prescription.utils.entity.User;

//@Component("SocketIO")
public class SocketIO implements ApplicationListener<ContextRefreshedEvent> {

	// 记录当前在线的用户
	ArrayList<User> onlineUsers = new ArrayList<User>();
	// 记录当前已连接设备，已连接设备>当前在线用户
	ArrayList<String> connectings = new ArrayList<String>();

	// 记录当前连接实例
	ArrayList<SocketIOClient> connectedClients = new ArrayList<SocketIOClient>();

	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		// 疑问有时候等于可以，有时候不等于可以
		if (arg0.getApplicationContext().getParent() == null) {// root application context 有parent，他是儿子时候在执行.
			// 需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
			new Thread(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					socketStart();
				}
			}).start();
		}

	}

	@SuppressWarnings("rawtypes")
	private void socketStart() {
		System.out.println("in socketio");

		Configuration config = new Configuration();
		config.setHostname("172.17.30.221");
		config.setPort(7777);

		SocketConfig sockConfig = new SocketConfig();
		// 地址服用，这时候再启动不报错
		sockConfig.setReuseAddress(true);

		// 设置使用的协议和轮询方式
		config.setTransports(Transport.WEBSOCKET, Transport.POLLING);
		// 设置允许源
		config.setOrigin(":*:");

		config.setSocketConfig(sockConfig);
		// 允许最大帧长度
		config.setMaxFramePayloadLength(1024 * 1024);
		// 余下最大内容
		config.setMaxHttpContentLength(1024 * 1024);
		SocketIOServer server = new SocketIOServer(config);

		server.addConnectListener(new ConnectListener() {
			public void onConnect(SocketIOClient client) {
				// TODO Auto-generated method stub
				// 根据session获取当前连接设备
				connectings.add(client.getSessionId().toString());

				// 添加到当前连接实例中
				connectedClients.add(client);

				String clientInfo = client.getRemoteAddress().toString();
				String clientIp = clientInfo.substring(1, clientInfo.indexOf(":"));// 获取ip
				System.out.println("建立客户端连接ip" + clientIp);
				client.sendEvent("connected", "ip: " + clientIp);
			}
		});

		server.addDisconnectListener(new DisconnectListener() {
			public void onDisconnect(SocketIOClient client) {

				for (int i = onlineUsers.size() - 1; i >= 0; i--) {
					if (onlineUsers.get(i).getSession().equalsIgnoreCase(client.getSessionId().toString())) {
						System.out.println(i);
						onlineUsers.remove(i);
					}
				}
				server.getBroadcastOperations().sendEvent("OnlineList", onlineUsers);
				// 设备下线
				connectings.remove(client.getSessionId().toString());

				// 实例下线
				connectedClients.remove(client);

				String clientInfo = client.getRemoteAddress().toString();
				String clientIp = clientInfo.substring(1, clientInfo.indexOf(":"));// 获取ip
				System.out.println("断开客户端连接ip" + clientIp);
				client.sendEvent("disconned", "ip: " + clientIp);

			}
		});

		server.addEventListener("msginfo", String.class, new DataListener<String>() {

			public void onData(SocketIOClient client, String data, AckRequest arg2) throws Exception {
				// TODO Auto-generated method stub

//				Collection<SocketIOClient> clients= server.getRoomOperations(data).getClients();
//				for( SocketIOClient roomClient:clients) {
//					if(roomClient.equals(client)) {
//						continue;
//					}
//					roomClient.sendEvent("mes", "发送给除自己以外的该房间内的所有客户端");
//				}

//				client.leaveRoom(room);
				String clientInfo = client.getRemoteAddress().toString();
				String clientIp = clientInfo.substring(1, clientInfo.indexOf(":"));
				System.out.println(clientIp + "：客户端：************" + data);

				client.sendEvent("msginfo", "服务端返回信息!");
			}
		});
		// 返回在线用户
		server.addEventListener("getOnlineList", User.class, new DataListener<User>() {
			@Override
			public void onData(SocketIOClient client, User user, AckRequest ackSender) throws Exception {
				// TODO Auto-generated method stub
				/**
				 * 获取的时候先把这个人设置上线
				 */
				user.setSession(client.getSessionId().toString());
				boolean isContain = false;
				for (int i = 0; i < onlineUsers.size(); i++) {
					if (onlineUsers.get(i).getSession().equalsIgnoreCase(client.getSessionId().toString())) {
						isContain = true;
						break;
					}
				}
				if (connectings.contains(client.getSessionId().toString()) && !isContain) {
					onlineUsers.add(user);
				}
				server.getBroadcastOperations().sendEvent("OnlineList", onlineUsers);
			}
		});

		// 返回用户最近聊天列表
		server.addEventListener("getConversation", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map data, AckRequest ackSender) throws Exception {
				// TODO Auto-generated method stub
				// 获取连接用户的对话列表
				User user = new User();
				String userId = data.get("userId") + "";
				for (User u : onlineUsers) {
					if (u.getUserId() == userId) {
						user = u;
						break;
					}
				}
				client.sendEvent("getConversation", user.getConversation());

			}
		});
		// 加入房间
		server.addEventListener("joinRoom", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map room, AckRequest ackSender) throws Exception {
				// TODO Auto-generated method stub
				String roomId = room.get("id") + "";
				client.joinRoom(roomId);
			}
		});
		// 更新用户的对话列表
		server.addEventListener("updateUserConversation", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map data, AckRequest ackSender) throws Exception {
				// TODO Auto-generated method stub
				User user = null;
				String userId = data.get("userId") + "";
				for (User u : onlineUsers) {
					if (u.getUserId() == userId) {
						user = u;
						break;
					}
				}
				if(user!=null) {
					ArrayList<Map> conversation=(ArrayList<Map>) data.get("conversation");
					user.setConversation(conversation);
				}
				System.out.println(onlineUsers);
			}
		});
		// 聊天，发送消息
		server.addEventListener("chat", Message.class, new DataListener<Message>() {

			@Override
			public void onData(SocketIOClient client, Message data, AckRequest ackSender) throws Exception {
				// TODO Auto-generated method stub
				data.setCreatTime(new Date());
				// 找到目标用户,并检查对话是否存在目标用户的对话列表，不存在添加到对话列表
				User toUser = null;

				for (int i = 0; i < onlineUsers.size(); i++) {
					if (onlineUsers.get(i).getUserId().equals(data.getToUserId())) {
						toUser = onlineUsers.get(i);
					}
				}
				boolean isInConversation = false;
				ArrayList<Map> userConversation = toUser.getConversation();
				System.out.println(userConversation);
				if (userConversation != null) {
					for (int i = 0; i < userConversation.size(); i++) {
						if (userConversation.get(i).get("roomId").toString().equals(data.getRoomId())) {
							isInConversation = true;
							break;
						}
					}
				}
				System.out.println(!isInConversation && toUser != null);
				if (!isInConversation && toUser != null) {
					isInConversation = true;
					Map newConversation = new HashMap<String, Object>();
					newConversation.put("roomId", data.getRoomId());
					newConversation.put("name", data.getFromUserName());
					newConversation.put("id", data.getFromUserId());
					if (toUser.getConversation() == null) {
						ArrayList<Map> concersationadd = new ArrayList<Map>();
						concersationadd.add(newConversation);
						toUser.setConversation(concersationadd);
					} else
						toUser.getConversation().add(newConversation);

					// 通知对方更新列表
					for (SocketIOClient c : connectedClients) {
						if (c.getSessionId().toString().equalsIgnoreCase(toUser.getSession())) {
							c.joinRoom(data.getRoomId());
							c.sendEvent("getConversation", toUser.getConversation());
							break;
						}
					}
				}

				System.out.println("共人" + server.getRoomOperations(data.getRoomId()).getClients().size());
//				server.getRoomOperations(data.getRoomId()).sendEvent("chat", data);

				// 发送消息到房间
				Collection<SocketIOClient> clients = server.getRoomOperations(data.getRoomId()).getClients();
				for (SocketIOClient roomClient : clients) {
					if (roomClient.equals(client)) {
						continue;
					}
					roomClient.sendEvent("chat", data);
				}

			}
		});

		server.start();
		try {
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.stop();
	}
}
