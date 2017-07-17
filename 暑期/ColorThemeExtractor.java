package com.huijie;

import java.awt.LinearGradientPaint;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.invoke.ConstantCallSite;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.sampled.Line;

import java.util.Set;

import org.bson.BasicBSONObject;
import org.bson.NewBSONDecoder;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBDecoder;
import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class ColorThemeExtractor {

	/*
	 * collection = downstats
	 * collection = images
	 * collection = imgfeature
	 * collection = imgwordscores
	 * collection = system.indexes
	 * collection = users
	 */
	
	// ����map��value����
	private static class ValueComparator implements Comparator<Map.Entry<String,Integer>>  
    {  
        public int compare(Map.Entry<String,Integer> m, Map.Entry<String,Integer> n)  
        {  
            return n.getValue()-m.getValue();  
        }  
    }
	
	
	//����ÿ��Ⱥ����ÿ��ʱ��Ƭ�������б���
	//����ļ�֮ǰ��maxEmotionRatioDate.txt�����������ÿ��Ⱥ��ÿ��ʱ��Ƭ�������кʹ�ʱͼƬ��Ŀ�Լ������б�����ÿ��ʱ��Ƭһ�У����ǲ�û��Ⱥ������
	//�µ�����ļ���groupsEmotionRationDate.txt����������ļ�����Ⱥ������
	public static void getEmotionRatioDate(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/Groups/groupUserImages&Date.txt")));
			BufferedReader brr = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date2.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/Groups/groupsEmotionRatioDate_min3.txt")));
			
			Map<String, Integer> imgEmotion = new HashMap<String, Integer>();
			String line = "";
			while((line = brr.readLine()) != null){
				String p[] = line.split(" ");
				imgEmotion.put(p[0], Integer.parseInt(p[1]));
			}
			
			Integer minDate = 20100805;//ʱ������Ϊ��λ
			Integer maxDate = 0;
			int num1 = 0;//the num of images from 2010-2013
			int num2 = 0;//the num of images from XXXX--2009
			System.out.println("!!!!!!!!!");
			
			//<group, <imgid, date>>
			Map<String, Map<String, Integer>> groupImgD = new HashMap<String, Map<String,Integer>>(); 
			line = "";
			while((line = br.readLine()) != null){
				String p[] = line.split(" ");
				int numuser = Integer.parseInt(p[1]);//num of user in this group p[0]
				Map<String, Integer> imgDate = new HashMap<String, Integer>();
				for(int i = 0; i < numuser; i++){
					line = br.readLine();
					String pp[] = line.split(" ");
					for(int j = 1; j < pp.length - 1; j++){
						int d = Integer.parseInt(pp[++j].substring(0, 8));
						if(d >= 20100000)
						    imgDate.put(pp[j-1], d);//img id and date
						if(d > maxDate)
							maxDate = d;
						if(d < minDate)
							minDate = d;
						if(d >= 20100000)
							num1++;
						else
							num2++;
					}
				}
				groupImgD.put(p[0], imgDate);
			}
			
			System.out.println("after read two files !");
			System.out.println("max date : " + maxDate);
			System.out.println("min date : " + minDate);
			System.out.println("num1 " + num1);
			System.out.println("num2 " + num2);
			
			List<Integer> dp = new ArrayList<Integer>();
			int date = 20100101;//����ʱ��Ƭ
			while(date < 20140000){
				System.out.println("date begin " + date);
				dp.add(date);
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
				Date formatDate = df.parse(String.valueOf(date));
				Calendar cdate = Calendar.getInstance();
				cdate.setTime(formatDate);
				cdate.set(Calendar.DATE, cdate.get(Calendar.DATE) + 14);//���ܵ�ʱ��Ƭ
				Date endDate = df.parse(df.format(cdate.getTime()));
				date = Integer.parseInt(df.format(endDate));
				//break;
			}
			System.out.println("size of time slice " + dp.size());
			
			int num = 0;
			Iterator iter = groupImgD.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next(); //for each group
			    List<List<String>> imgs = new ArrayList<List<String>>();//<slice , >
			    for(int s = 0; s < 104; s++){
			    	List<String> img = new ArrayList<String>();
			    	imgs.add(img);
			    }
			  //  commentNum += ((List<String>) entry.getValue()).size(); 
			   // bw.append(entry.getKey() + " " + ((Map)entry.getValue()).size() + "\n");//write a group and the num of user
			    Iterator it = ((Map)entry.getValue()).entrySet().iterator();
			    while(it.hasNext()){//for each img 
			    	Map.Entry entry2 = (Map.Entry) it.next();
			    	int imgd = Integer.parseInt(entry2.getValue().toString());
			    	//System.out.println("key " + entry2.getKey().toString());
			    	//System.out.println("value " + entry2.getValue().toString());
			    	for(int l = 0; l < 104; l++){
			    		if(imgd >= dp.get(l) && (imgd < dp.get(l+1))){
			    			List<String> temp = imgs.get(l);
			    			temp.add(entry2.getKey().toString());
			    			imgs.set(l, temp);
			    		}
			    	}
			    	
			    }
			    //bw.append(entry.getKey().toString() + "\n");//д��Ⱥ��id,��һ������ʡ�ԣ��Ժ�����ٸ�->����������һ��
			    
			    for(int s = 0; s < 104; s++){
			    	List<String> tem = imgs.get(s);//ÿ��ʱ��ε�ͼƬid
			    	int fre[] = {0, 0, 0, 0, 0, 0};
			    	for(int ss = 0; ss < tem.size(); ss++){
			    		//System.out.println("img " + tem.get(ss));
			    		//System.out.println("emotion " + imgEmotion.get(tem.get(ss)));
			    	
			    		fre[imgEmotion.get(tem.get(ss))]++;
			    	}
			    	int maxf = 0;//��ǿ�������Ŀ
			    	int maxE = -1;//��ǿ����б��
			    	for(int ss = 0; ss < 6; ss++){
			    		if(fre[ss] > maxf){
			    			maxf = fre[ss];
			    			maxE = ss;
			    		}
			    	}
			    	if(tem.size() > 2){//��ȥ��Ŀ���ٵ�����, 2017/5/31�޸���С��Ŀ���ƣ�֮ǰ��5
			    		bw.append(entry.getKey().toString() + " " + dp.get(s) + " " + maxE + " " + tem.size() + " " + maxf*1.0/tem.size() + "\n");
			    		num++;
			    	}
			    }
			}
			System.out.println("������Ŀ   " + num);
			br.close();
			brr.close();
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}//��� ������Ŀ 62734
	
	//wenjing 
	//�������ݿ�user���contactList�ó�ÿ���û���contactlist,û�ж�contactList����������nsid, �Լ������в����о���Χ�ڵ�contact
	//����ļ�userContactList.txt
	public static void getUserContacts(){
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			BufferedWriter bww = new BufferedWriter(new FileWriter(new File("output/getImageStatics/userContactList.txt")));
			
			DBCollection collection = db.getCollection("users");
			
			int num = 0;
			String line = "";
			while((line = br.readLine()) != null){
				DBObject query = new BasicDBObject("palias", line);
				DBObject field = new BasicDBObject();
				DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
				while(cursor.hasNext()){
					DBObject dbo = cursor.next();
					List contacts = (List) dbo.get("contactList");
					bww.append(line);//write user alias
					for(int j = 0; j < contacts.size(); j++){
						String nsid = "";
						if(contacts.get(j) instanceof String)
							nsid = contacts.get(j).toString();
						else{
							BasicBSONObject contact = (BasicBSONObject) contacts.get(j);
							nsid = contact.get("nsid").toString();
							//System.out.println("in nsid");
						}
						num++;
						if(num % 100 == 0)
							System.out.println(num);
						bww.append(" " + nsid);
					}
					bww.append("\n");
				}
			}
			System.out.println(num);
			br.close();
			bww.close();
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
    ////����ļ�groupsUser.txt [gid usernum user0 user1 ...]
	//ʹ��userAlias.txt��user Collection ������group users
	public static void getGroupUser() {
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			BufferedWriter bww = new BufferedWriter(new FileWriter(new File("output/Groups/groupsUser.txt")));
			
			DBCollection collection = db.getCollection("users");
			
			int num = 0;//for print
			Map<String, List<String>> groups = new HashMap<String, List<String>>();//for all groups[gid user0 user1 ...]
			String line = "";
			while ((line = br.readLine()) != null) {
				DBObject query = new BasicDBObject("palias", line);
				DBObject field = new BasicDBObject();
				DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
				while(cursor.hasNext()){
					DBObject dbo = cursor.next();
					List gr = (List) dbo.get("groupList");
					for(int i = 0; i < gr.size(); i++){
						String gid = gr.get(i).toString();
						if(!groups.containsKey(gid)){
							List<String> user = new ArrayList<String>();
							user.add(line);
							groups.put(gid, user);
						}else{
							if(!groups.get(gid).contains(line))
								groups.get(gid).add(line);								
						}
						num++;
						if(num %1000 == 0)
							System.out.println(num);
					}
				}
			}
			br.close();
			Iterator iter = groups.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next();  
			    bww.append(entry.getKey() + " : " + ((List)entry.getValue()).size());
			    for(int i = 0; i < ((List)entry.getValue()).size(); i++){
			    	bww.append(" " + ((List)(entry.getValue())).get(i).toString());
			    }
			    bww.append("\n");
			}
			bww.close();
			System.out.println("after bww close" + "\n");
		}catch(Exception e){
			e.printStackTrace();
		}
			
	}
	
	public static void getNsidAlias() {
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/Groups/nsidAlias.txt")));
			
			DBCollection collection = db.getCollection("users");
			
			int num = 0;//for print
			
			// userAlias���nsid-alias
			//Map <String, String> usernsidAlias = new HashMap<String, String>();
			String line = "";
			while((line = br.readLine()) != null){
				DBObject query = new BasicDBObject("palias", line);
				DBObject field = new BasicDBObject();
				DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
				while(cursor.hasNext()){
					DBObject dbo = cursor.next();
					//usernsidAlias.put(dbo.get("nsid").toString(), line);
					bw.append(dbo.get("nsid").toString() + " " + line + "\n");
				}
				num++;
				if(num % 20 == 0)
					System.out.println(num);
			}
			br.close();
			bw.close();
			System.out.println(num);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//wenjing
	//����groupUserImages&Date.txt���Ⱥ����û���Ϣ��������ЩȺ������û�֮����໥��ע�ͱ���ע���
	public static void getGroupUsersConnect(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/Groups/groupUserImages&Date.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/Groups/nsidAlias.txt")));
			BufferedReader brr = new BufferedReader(new FileReader(new File("output/Groups/groupUsersConnect.txt")));
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/Groups/groupsUsersConnect-left.txt")));
			
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			
			DBCollection collection = db.getCollection("users");
			
			List<String> doneGroups = new ArrayList<String>();
			// userAlias���nsid-alias
			Map <String, String> usernsidAlias = new HashMap<String, String>();
			
			String str = "";
			while((str = br2.readLine()) != null){
				String part[] = str.split(" ");
				usernsidAlias.put(part[0], part[1]);
			}
			System.out.println("after read userNsid&&Alias!!!!");
			str = "";
			while((str = brr.readLine()) != null){
				String p[] = str.split(" ");
				int size = p.length;
				doneGroups.add(p[0]);
				for(int i = 1; i < size; i++)
					str = brr.readLine();
			}
			
			Map<String, List<String>> groupUser = new HashMap<String, List<String>>();
			String line = "";
			
			int num  = 0;
			int max = 0;//����Ⱥ������������Ŀ
			while((line = br.readLine()) != null){
				//System.out.println(line);
				String p[] = line.split(" ");
				int n = Integer.parseInt(p[1]);
				List<String> users = new ArrayList<String>();
				for(int i = 0; i < n; i++){
					line = br.readLine();
					String pp[] = line.split(" ");
					users.add(pp[0]);
				}
				if(n < 6)//С��6���
					continue;
				if(doneGroups.contains(p[0]))
					continue;
				groupUser.put(p[0], users);
				//for each group				
				int[][] A = new int[800][800];//800 > 739
				for(int i = 0; i < 800; i++){
					for(int j = 0; j < 800; j++)
						A[i][j] = 0;
				}
				Map<String, Integer> userInt = new HashMap<String, Integer>();//user  - id in A
				for(int k = 0; k < n; k++){
					userInt.put(users.get(k), k);//[0 n-1]
				}
				for(int i = 0; i < n; i++){
					String user = users.get(i);
					DBObject query = new BasicDBObject("palias", user);
					DBObject field = new BasicDBObject();
					DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
					while(cursor.hasNext()){
						DBObject dbo = cursor.next();
						List contacts = (List) dbo.get("contactList");
						for(int j = 0; j < contacts.size(); j++){
							String cnsid = "";
							if(contacts.get(j) instanceof String)
								cnsid = contacts.get(j).toString();
							else{
								BasicBSONObject contact = (BasicBSONObject) contacts.get(j);
								cnsid = contact.get("nsid").toString();
								//System.out.println("in nsid");
							}
							String contect = usernsidAlias.get(cnsid);
							if(users.contains(contect)){//contact �� user ��ע��that's right!!!��
								A[userInt.get(contect)][userInt.get(user)] = 1;
							}
						}
						
					}
				}
				
				bw.append(p[0]);
				for(int u = 0; u < n; u++){
					bw.append(" " + users.get(u));
				}
				bw.append("\n");
				for(int c = 0; c < n; c++){
					for(int r = 0; r < n - 1; r++)
						bw.append(A[c][r] + " ");
					bw.append(A[c][n-1] + "\n");
				}					
				if(n > max)
					max = n;
				num++;
				System.out.println(num);
			}
			System.out.println("Ⱥ����Ŀ��С��6������� " + num);//9094
			System.out.println("Ⱥ���������  " + max);//739
			
			br.close();
			br2.close();
			bw.close();
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
		
	//wenjing 
	//����һ���������Ĺ��ܣ���Ϊ��PageRank�㷨׼����
	//���������������û�֮��Ĺ�ע����ע��Ϣ�����󣩣�����Ⱥ����
	public static void getUserConnect(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/Groups/nsidAlias.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getImageStatics/userContactList.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			
			//BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/Groups/usersConnect.txt")));
			PrintWriter out = new PrintWriter("output/Groups/usersConnect.txt");
			OutputStream fos = new FileOutputStream("output/Groups/usersConnect.txt");
			DataOutputStream dos = new DataOutputStream(fos);
			
			// userAlias���nsid-alias
			Map <String, String> usernsidAlias = new HashMap<String, String>();
			Map<String, List<String>> userContacts = new HashMap<String, List<String>>();//<user alias, contact alias list>
			
			String str = "";
			while((str = br.readLine()) != null){
				String part[] = str.split(" ");
				usernsidAlias.put(part[0], part[1]);
			}
			System.out.println("after read userNsid&&Alias!!!!");
			
			str = "";
			while((str=br2.readLine()) != null){
				String p[] = str.split(" ");
				List<String> contacts = new ArrayList<String>();
				for(int i = 1; i < p.length; i++){
					contacts.add(usernsidAlias.get(p[i]));
				}
				userContacts.put(p[0], contacts);
				
			}
			
			Map<String, Integer> userInt = new HashMap<String, Integer>();
			List<String> users = new ArrayList<String>();
			String[][] A = new String[2700][2700];//�����2605����
			for(int i = 0; i < 2700; i++){
				for(int j = 0; j < 2700; j++)
					A[i][j] = " 0";
			}
			int index = 0;
			while((str = br3.readLine()) != null){
				userInt.put(str, index);
				index++;
				users.add(str);
			}
			
			int num = 0;
			for(int s = 0; s < users.size(); s++){
				String user = users.get(s);//for each user 
				if(userContacts.containsKey(user)){
					List c = userContacts.get(user);//for the contacts of each user
					for(int i = 0; i < c.size(); i++){
						String cont = (String)c.get(i);
						if(users.contains(cont)){
							A[userInt.get(cont)][userInt.get(user)] = " 1";
							num++;
						}
					}
				}
			}
			System.out.println(num);//37433
			System.out.println(users.size());
			//bw.append("user contact" + "\n");
			for(int c = 0; c < users.size(); c++){
				//System.out.print(A[c][0] + " ");
				//bw.append("" + A[c][0]);
				out.print(A[c][0]);
				//dos.writeInt(A[c][0]);
				for(int r = 1; r < users.size(); r++){
					//bw.append(" " + A[c][r]);
					out.print(A[c][r]);
					//dos.writeInt(A[c][r]);
					//System.out.print(A[c][r] + " ");
					//if(r % 100 == 0)
						//System.out.println(r);
				}
				System.out.println();
				//bw.append("\n");
				out.println();
			}
			out.flush();
			br.close();
			br2.close();
			br3.close();
			out.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	} 
	//wenjing bishe
	//����Ⱥ�����ͨ��[�û�֮�����ͨ����/ȫ��ͨ����]������ͼ,�������Ⱥ�鷶Χ�����ļ�userAlias.txtȷ���ģ�2605���û�
	//����ļ�groupsConnectsDirected.txt [groupid userNumofGroup connectNum]
	public static void getGroupsConnectsDirected(){
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			BufferedReader brr = new BufferedReader(new FileReader(new File("output/Groups/groupsUser.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/Groups/nsidAlias.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/Groups/groupsConnectsDirected.txt")));
			
			DBCollection collection = db.getCollection("users");
			
			int num = 0;//for print
			
			// userAlias���nsid-alias
			Map <String, String> usernsidAlias = new HashMap<String, String>();
			
			String str = "";
			while((str = br2.readLine()) != null){
				String part[] = str.split(" ");
				usernsidAlias.put(part[0], part[1]);
			}
			System.out.println("after read userNsid&&Alias!!!!");
			
			Map<String, List<Integer>> groupsConnects = new HashMap<String, List<Integer>>();
			Map<String, List<String>> groups = new HashMap<String, List<String>>();//for all groups[gid user0 user1 ...]
			String line = "";
			while((line = brr.readLine()) != null){
				String p[] = line.split(" ");
				List<String> user = new ArrayList<String>();
				List<Integer> cnum = new ArrayList<Integer>();
				cnum.add(Integer.parseInt(p[2]));
				cnum.add(0);
				for(int k = 3; k < p.length; k++){
					user.add(p[k]);
				}
				groups.put(p[0], user);
				groupsConnects.put(p[0], cnum);
			}
			
			System.out.println("after read groupsUser!!!");
			
			while ((line = br.readLine()) != null) {
				//System.out.println(num);
				DBObject query = new BasicDBObject("palias", line);
				DBObject field = new BasicDBObject();
				DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
				while(cursor.hasNext()){
					DBObject dbo = cursor.next();
					List gList = (List) dbo.get("groupList");
					List contacts = (List) dbo.get("contactList");
					for(int i = 0; i < gList.size(); i++){
						String gid = "";
						gid = gList.get(i).toString();
						//System.out.println("gid : " + gid);
						if(groupsConnects.containsKey(gid)){
							List<Integer> temp = groupsConnects.get(gid);
							//System.out.println("begin to count connect num !!!");
							for(int j = 0; j < contacts.size(); j++){
								String cnsid = "";
								if(contacts.get(j) instanceof String)
									cnsid = contacts.get(j).toString();
								else{
									BasicBSONObject contact = (BasicBSONObject) contacts.get(j);
									cnsid = contact.get("nsid").toString();
									//System.out.println("in nsid");
								}
								//System.out.println("cid : " + cnsid);
								if(groups.get(gid).contains(usernsidAlias.get(cnsid)))
									temp.set(1, temp.get(1)+1);
							}
							//System.out.println("temp : " + temp.get(1));
							groupsConnects.put(gid, temp);
						}
						num++;
						if(num %500 == 0)
							System.out.println(num);
					}
				}
				//if(num > 1)
					//break;
			}
			br.close();
			Iterator iter = groupsConnects.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next();  
			    bw.append(entry.getKey() + " " + ((List)(entry.getValue())).get(0) + " " + ((List)(entry.getValue())).get(1) + "\n");
			}
			bw.close();
			System.out.println(num);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//wenjing
	//[groups] ���ݴ������ͼƬ����Ϣ���õ�����Ⱥ������֣�һ����һ��Ⱥ������
	public static void getGroups(){
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/dataSet_/imgwordscores.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/groups.txt")));
			
			DBCollection collection = db.getCollection("images");
			
			int n = 0;
			int imgid;
			String line = "";
			List<String> gs = new ArrayList<String>();
			while((line = br.readLine()) != null){
				String p[] = line.split(",");
				if(p.length >= 2){
					String imgId = p[0];
					DBObject query = new BasicDBObject("_id", new BigInteger(imgId, 10).longValue());//imgfreature���_id��String
					DBObject field = new BasicDBObject();
					DBCursor imgCursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
					while (imgCursor.hasNext()) {
						DBObject dbo = imgCursor.next();
						List gr = (List) dbo.get("groups");
						for(int i = 0; i < gr.size(); i++){
							if(!gs.contains(gr.get(i).toString())){
								gs.add(gr.get(i).toString());
								n++;
								bw.append(gr.get(i).toString() + "\n");
								if(n % 1000 == 0){
									System.out.println(n);
								}
							}
						}
					}
				}
			} 
			System.out.println(n);
			br.close();
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//wenjing bishe
	//ÿ��ͼƬ���ڶ��group ,group id is string
	//get the emotion and group id s,write to file 'emotion&group.txt',[imgid, emotion, group1, group2, ...]
	//file"group&num.txt" : [groupid, num0, num1, num2, num3, num4, num5]
	public static void getImgEmotionGroup(){
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/emotion&group.txt")));
			BufferedWriter bww = new BufferedWriter(new FileWriter(new File("output/group&num.txt")));
			
			DBCollection collection = db.getCollection("imgwordscores");
			DBCollection imgcollection = db.getCollection("images");
			
			DBObject query = new BasicDBObject("bestidx", new BasicDBObject("$gt", -1));
			DBObject field = new BasicDBObject("bestidx", true);
			DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			
			Map<String, List<Integer>> numOfEmotion= new HashMap<String, List<Integer>>();
			
			int num = 0;
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				String imageId = dbo.get("_id").toString();
				int emotion = Integer.parseInt(dbo.get("bestidx").toString());
				DBCursor dbCursor = imgcollection.find(new BasicDBObject("_id", new BigInteger(imageId, 10).longValue()));
				while(dbCursor.hasNext()){
					DBObject imgDbo = dbCursor.next();
					List<String> gs = new ArrayList<String>();
					gs = (List)imgDbo.get("groups");
					bw.append(imageId + "," + emotion);
					for(int j = 0; j < gs.size(); j++){
						String gid = gs.get(j);
						bw.append("," + gid);
						if(numOfEmotion.containsKey(gid)){
							int past = numOfEmotion.get(gid).get(emotion);
							numOfEmotion.get(gid).set(emotion, past+1);
						}else{
							List<Integer> nn = new ArrayList<Integer>();
							for(int i = 0; i < 6; i++){
								nn.add(0);
							}
							numOfEmotion.put(gid, nn);
							numOfEmotion.get(gid).set(emotion, 1);
						}
					}
					bw.append("\n");
					num++;
				}
				if(num % 1000 == 0)
					System.out.println(num);
			}
			
			System.out.println(num);
			Iterator entries = numOfEmotion.entrySet().iterator();
			int numG = 0;
			while(entries.hasNext()){
				Map.Entry entry = (Map.Entry)entries.next();
				String key = (String)entry.getKey();
				List<Integer> arr = new ArrayList<Integer>();
				arr = (List)entry.getValue();
				bww.append(key + "," + arr.get(0) + "," + arr.get(1) + "," + arr.get(2) + "," + arr.get(3) + "," + arr.get(4) + "," + arr.get(5) + "\n");
				numG++;
				if(numG % 1000 == 0)
					System.out.println(numG);
			}
			System.out.println(numG);
			bw.close();
			bww.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//wenjing 
	//[imgUrl.txt]�ļ���ʽ�ǣ�imgId,url
	public static void getImgUrl(){
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/imgUrl.txt")));
			
			DBCollection collection = db.getCollection("imgwordscores");
			DBCollection imgcollection = db.getCollection("images");
			DBObject query = new BasicDBObject("bestidx", new BasicDBObject("$gt", -1));
			DBObject field = new BasicDBObject("bestidx", true);
			DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			int num = 0;
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				String imageId = dbo.get("_id").toString();
				DBCursor dbCursor = imgcollection.find(new BasicDBObject("_id", new BigInteger(imageId, 10).longValue()));
				while(dbCursor.hasNext()){
					DBObject imgDbo = dbCursor.next();
					String url = imgDbo.get("url").toString();
					bw.append(imageId + "," + url + "\n");
					num++;
				}
				if(num % 1000 == 0)
					System.out.println(num);
			}	
			System.out.println(num);
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//����ÿ��Ⱥ�������з���ͼƬ��¼
	//[groups/groupsUserImages.txt] [groupid numofUser \n user image1 date image2 date .. \n user2 ...]
	public static void getGroupUserImages(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date2&group.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/Groups/groupUserImages&Date.txt")));
			
			Map<String, Map<String, List<String>>> groupUserImg = new HashMap<String, Map<String, List<String>>>();
			
			int num = 0;
			
			String line = "";
			while((line = br.readLine()) != null){
				String p[] = line.split(" ");//p[0]:imgid, p[1]emotion type p[2]user alias p[3]publish date p[4-]group id
				for(int i = 4; i < p.length; i++){
					String gid = p[i];
					if(groupUserImg.containsKey(gid)){
						Map<String, List<String>> temp = groupUserImg.get(gid);//temp -> <user <img date>>
						if(temp.containsKey(p[2])){
							List<String> imgd = temp.get(p[2]);
							imgd.add(p[0]);
							imgd.add(p[3]);	
							temp.put(p[2], imgd);
						} else{
							List<String> imgdd = new ArrayList<String>();
							imgdd.add(p[0]);
							imgdd.add(p[3]);
							temp.put(p[2], imgdd);
						}
					    groupUserImg.put(gid, temp);
					}else{
						Map<String, List<String>> newG = new HashMap<String, List<String>>();
						List<String> imgDate = new ArrayList<String>();
						imgDate.add(p[0]);
						imgDate.add(p[3]);
						newG.put(p[2], imgDate);
						groupUserImg.put(gid, newG);
					}
					num++;
					if(num%1000 == 0)
						System.out.println(num);
				}
			}
			
			System.out.println("after get groupUserImg");
			Iterator iter = groupUserImg.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next();  
			  //  commentNum += ((List<String>) entry.getValue()).size(); 
			    bw.append(entry.getKey() + " " + ((Map)entry.getValue()).size() + "\n");//write a group and the num of user
			    Iterator it = ((Map)entry.getValue()).entrySet().iterator();
			    while(it.hasNext()){
			    	Map.Entry entry2 = (Map.Entry) it.next();
			    	List imd = (List) entry2.getValue();
			    	bw.append(entry2.getKey().toString());//write user alias
			    	for(int l = 0; l < imd.size(); l++){
			    		bw.append(" " + imd.get(l));//write img and date
			    	}
			    	bw.append("\n");//for each user
			    }
			}
			
			System.out.println("num of groups " + groupUserImg.size());
			br.close();
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}//output: num of groups 53200
	// [image] �õ���������з������ڵ�ͼƬ����ʽΪͼƬid�������𣬷����û��ǳƣ�����ʱ�䣬group id һ��Ϊһ��ͼƬ
	//wenjing add some changes : add the groups id
	public static void getImageStatics() {
		try {
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date2&group.txt")));
			
			List<String> userAliases = new ArrayList<String>();
			
			// ��������е�ͼƬ
			DBCollection collection = db.getCollection("imgwordscores");
			DBCollection imgCollection = db.getCollection("images");
			DBObject query = new BasicDBObject("bestidx", new BasicDBObject("$gt", -1));
			DBObject field = new BasicDBObject("bestidx", true);
			DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			int num = 0;
			int []fre = {0, 0, 0, 0, 0, 0};
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				String imageId = dbo.get("_id").toString();
				String emotionType = dbo.get("bestidx").toString();
				// ���з���ʱ���ͼƬ��һ���з����ߣ�
				BasicDBList condList = new BasicDBList(); 
				DBObject cond1 = new BasicDBObject("_id", new BigInteger(imageId, 10).longValue());
				DBObject cond2 = new BasicDBObject("exif.datt", new BasicDBObject("$exists", true));
				condList.add(cond1);
				condList.add(cond2);
				BasicDBObject searchCond = new BasicDBObject();
				searchCond.put("$and", condList);
				DBObject imgField = new BasicDBObject();
				//imgField.put("ownid", true);
				//imgField.put("exif.datt", true);
				//note: imgField�Ƕ�find��ѯ�����ƣ�Ҳʹ�ò�ѯ�����ĿimgDbo.get()���������ȡ
				DBCursor imgCursor = imgCollection.find(searchCond, imgField);
				while (imgCursor.hasNext()) {
					DBObject imgDbo = imgCursor.next();
					String user = imgDbo.get("ownid").toString();
					String qualifiedImageId = imgDbo.get("_id").toString();
					Date date = (Date) ((DBObject)imgDbo.get("exif")).get("datt");
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
					String formatDate = df.format(date);
					List gr = (List) imgDbo.get("groups");
					if (!userAliases.contains(user)) {
						userAliases.add(user);
					}
					bw.append(qualifiedImageId + " " + emotionType + " " + user + " " + formatDate);
					//wenjing add , get group list
					for(int i = 0; i < gr.size(); i++){
						String gString = gr.get(i).toString();
						bw.append(" " + gString);
					}
					bw.append("\n");
					num ++;
					fre[Integer.parseInt(emotionType)] ++;
					if (num % 100 == 0) {
						System.out.println(num);
					}
				}
			}
			bw.close();
			db.cleanCursors(true);
			System.out.println("num: " + num);
			System.out.println("size: " + userAliases.size());
			System.out.println("fre: " + fre[0] + " " +  fre[1] + " " + fre[2] + " " + fre[3] + " " + fre[4] + " " + fre[5]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//���н��������ļ���ͬʱ�����num:218816; size:2605; fre:101189 21169 17491 11571 37791 29605
	
	// [comment] �õ�������������з������ڵ�ͼƬ���û�������е�ͼƬ���е����ۣ���ʽΪ[�����û��ǳ� ����ʱ�� ����ͼƬid ͼƬ������ �����û��ǳ� ����]��һ��Ϊһ������
	public static void getStimuli() {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getStimuli/stimuliNotSelf.txt")));
			// �����û����ϵ�id-alias
			List <String> userAliasList = new ArrayList<String>();
			String line = "";
			while ((line = br.readLine()) != null) {
				userAliasList.add(line);
			}
			br.close();
			// ��������е�ͼƬ
			DBCollection collection = db.getCollection("imgwordscores");
			DBCollection imgCollection = db.getCollection("images");
			DBObject query = new BasicDBObject("bestidx", new BasicDBObject("$gt", -1));
			DBObject field = new BasicDBObject("bestidx", true);
			DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			int num = 0;
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				String imageId = dbo.get("_id").toString();
				String emotionType = dbo.get("bestidx").toString();
				// ��������һ����������ʱ������۵�ͼƬ
				BasicDBList condList = new BasicDBList(); 
				DBObject cond1 = new BasicDBObject("_id", new BigInteger(imageId, 10).longValue());
				DBObject cond2 = new BasicDBObject("comts", new BasicDBObject("$exists", true));
				condList.add(cond1);
				condList.add(cond2);
				BasicDBObject searchCond = new BasicDBObject();
				searchCond.put("$and", condList);
				DBObject imgField = new BasicDBObject();
				imgField.put("ownid", true);
				imgField.put("comts", true);
				DBCursor imgCursor = imgCollection.find(searchCond, imgField);
				while (imgCursor.hasNext()) {
					DBObject imgDbo = imgCursor.next();
					String imageOwnerAlias = imgDbo.get("ownid").toString();
					String qualifiedImageId = imgDbo.get("_id").toString();
					// �õ������б�
					List commentList = (List) imgDbo.get("comts");
					for (int i = 0; i < commentList.size(); i ++) {
						BasicBSONObject comment = (BasicBSONObject) commentList.get(i);
						// ������� ��������������з���ʱ���ͼƬ�� �û������ۣ�����һ������
						// ��������߾��Ƿ����ߣ�����һ������
						String commentMakerAlias = comment.get("ownid").toString();
						if (!userAliasList.contains(commentMakerAlias) || commentMakerAlias.equals(imageOwnerAlias)) {
							continue;
						}
						// ʱ��
						Date date = (Date) comment.get("comtt");
						SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
						String formatDate = df.format(date);
						// ����
						String comt = comment.get("comt").toString();
						bw.append(commentMakerAlias + " " + formatDate + " " + qualifiedImageId + " " + emotionType + " " + imageOwnerAlias + " " + comt + "\n");
						bw.flush();
					}
					num ++;
				}
				if (num % 1000 == 0) {
					System.out.println(num);
				}
			}
			bw.close();
			db.cleanCursors(true);
			System.out.println("num: " + num);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// [extract comment emotion] ��ȡ�û�����ʱ�����飬��ʽΪ[�����û��ǳ� ����ʱ�� ����ͼƬid ͼƬ������ ͼƬ�����û��ǳ� �������]��һ��Ϊһ������
	public static void stimulateResult() {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			BufferedReader br1 = new BufferedReader(new FileReader(new File("wordlist/happy.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("wordlist/surprise.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("wordlist/anger.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("wordlist/disgust.txt")));
			BufferedReader br5 = new BufferedReader(new FileReader(new File("wordlist/fear.txt")));
			BufferedReader br6 = new BufferedReader(new FileReader(new File("wordlist/sad.txt")));
			//BufferedReader br7 = new BufferedReader(new FileReader(new File("output/getFilterWord/filterWord1%Longer2.txt")));
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getStimuli/stimuliNotSelf.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/StimulateResult/stimulateResultAll_lowerLonger2WithoutPuncNotSelf.txt")));
			// ����ʱ�happy surprise anger disgust fear sad
			Map<String, Integer> wordlist = new HashMap<String, Integer>();
			String line = "";
			while ((line = br1.readLine()) != null) {
				wordlist.put(line, 0);
			}
			br1.close();
			while ((line = br2.readLine()) != null) {
				wordlist.put(line, 1);
			}
			br2.close();
			while ((line = br3.readLine()) != null) {
				wordlist.put(line, 2);
			}
			br3.close();
			while ((line = br4.readLine()) != null) {
				wordlist.put(line, 3);
			}
			br4.close();
			while ((line = br5.readLine()) != null) {
				wordlist.put(line, 4);
			}
			br5.close();
			while ((line = br6.readLine()) != null) {
				wordlist.put(line, 5);
			}
			br6.close();
			// �����Ƶ��
			/*List<String> filterWord = new ArrayList<String>();
			while ((line = br7.readLine()) != null) {
				String part[] = line.split(" ");
				filterWord.add(part[0]);
			}
			br7.close();*/
			// ����һ������
			System.out.println(wordlist.size());
			int commentNum[] = {0, 0, 0, 0, 0, 0};
			int num = 0;
			int commentEmotionNum = 0;
			while ((line = br.readLine()) != null) {
				int fre[] = {0, 0, 0, 0, 0, 0};
				String []part = line.split(" ");
				String commentMakerAlias = part[0];
				String dateString = part[1];
				String imageId = part[2];
				String emotionType = part[3];
				String imageOwnerAlias = part[4];
				int len = 0;
				for (int i = 0; i < 5; i ++) {
					len += part[i].length() + 1;
				}
				// ȫ��ת��Сд��ȥ�������ţ����ȴ���2
				part = line.substring(len).toLowerCase().split("[\\p{Punct}\\s]+");
				for (int i = 0; i < part.length; i ++) {
					//if (wordlist.containsKey(part[i]) && !filterWord.contains(part[i]) && part[i].length() > 2) {
					if (wordlist.containsKey(part[i]) && part[i].length() > 2) {
						fre[wordlist.get(part[i])] ++;
					}
				}
				int maxFre = 0;
				int commentType = -1;
				for (int i = 0; i < 6; i ++) {
					if (fre[i] > maxFre) {
						maxFre = fre[i];
						commentType = i;
					}
				}
				if (commentType > -1) {
					commentNum[commentType] ++;
					bw.append(commentMakerAlias + " " + dateString + " " + imageId + " " + emotionType + " " + imageOwnerAlias + " " + commentType + "\n");
					bw.flush();
					commentEmotionNum ++;
				} else {
					bw.append(commentMakerAlias + " " + dateString + " " + imageId + " " + emotionType + " " + imageOwnerAlias + " -1\n");
					bw.flush();
				}
				num ++;
				if (num % 10000 == 0) {
					System.out.println(num);
				}
			}
			for (int i = 0; i < 6; i ++) {
				System.out.println(commentNum[i]);
			}
			br.close();
			bw.close();
			db.cleanCursors(true);
			System.out.println("comment num: " + num);
			System.out.println("commentEmotionNum: " + commentEmotionNum);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	// [emotion series] ����ĳ��ͼƬid�������������ͼƬ��ǰһ�ܣ����û����������۵����飬���뷢��ͼƬ����жԱ�
	// ��ʽΪ[����ͼƬid ͼƬ��� ͼƬ������ ͼƬ����ʱ�� [���۵�ͼƬ�ķ�����:���۵�ͼƬ���:�������]]��һ��Ϊһ��ͼƬ
	public static void getUserTemporalEmotionType() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("output/stimulateResult/stimulateResultAll_lowerLonger2withoutPuncNotSelf.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getUserTemporalEmotionType/temporalEmotion_oneDay_lowerLonger2withoutPuncNotSelf_All.txt")));
			// �������������keyΪ[�����û����ǳ�]��valueΪ[����]�������û����е�����,add by wenjing��
			Map<String, List<String>> dict = new HashMap<String, List<String>>();
			String line = "";
			while ((line = br.readLine()) != null) {
				String alias = line.split(" ")[0];
				String comment = line;
				if (dict.containsKey(alias)) {
					List<String> comments = dict.get(alias);
					comments.add(comment);
					dict.put(alias, comments);
				} else {
					List<String> comments = new ArrayList<String>();
					comments.add(comment);
					dict.put(alias, comments);
				}
			}
			// ����������ݵ���ȷ��
			int commentNum = 0;
			Iterator iter = dict.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next();  
			    commentNum += ((List<String>) entry.getValue()).size(); 
			}
			System.out.println(dict.size());
			System.out.println(commentNum);
			br.close();
			// ����ÿһ����Ҫ������ͼƬ
			int fre[] = {0, 0, 0, 0, 0, 0};
			while ((line = br2.readLine()) != null) {
				String []imageInfo = line.split(" ");
				String imageId = imageInfo[0];
				String emotionType = imageInfo[1];
				String publishUserAlias = imageInfo[2];
				String publishDateString = imageInfo[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				Date publishDate = df.parse(publishDateString);
				// ��������������
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.DAY_OF_MONTH, -1);//���ڼ�1��
		        Date leftDate = rightNow.getTime();
		        String output = imageId + " " + emotionType + " " + publishUserAlias + " " + publishDateString + " ";
		        boolean commentInTime = false;
		        // ������ͼƬ�����û��������۹�����е�ͼƬ
				if (dict.containsKey(publishUserAlias)) {
					List<String> comments = dict.get(publishUserAlias);
					// ������ÿһ�����ۣ��������ʱ��
					for (int i = 0; i < comments.size(); i ++) {
						String comment = comments.get(i); 
						String []commentInfo = comment.split(" ");
						String commentDateString = commentInfo[1];
						String commentImageEmotionType = commentInfo[3];
						String commentImageOwner = commentInfo[4];
						String commentEmotionType = commentInfo[5];
						Date commentDate = df.parse(commentDateString);
						if (commentDate.before(publishDate) && commentDate.after(leftDate)) {
							// ���۵�ͼƬ�ķ����ߣ����۹���ͼƬ�����������۱���������
							output += commentImageOwner + ":" + commentImageEmotionType + ":" + commentEmotionType + " ";
							commentInTime = true;
						}
					}
					output += "\n";
					if (commentInTime) {
						fre[Integer.parseInt(emotionType)] ++;
						bw.append(output);
						bw.flush();
					}
				}
			}
			br2.close();
			bw.close();
			for (int i = 0; i < 6; i ++) {
				System.out.print(fre[i] + " ");
			}
			System.out.println();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// [contact] ���û��ռ���ͳ���û��Ľ�����������ʽΪ[ͼƬ������ [ͼƬ������:������������]]
	public static void getContact2() {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getContact2/contactNotSelfWithTime.txt")));
			
			// �����û��б�
			Map <String, List<String>> contactMap = new HashMap<String, List<String>>();
			List<String> userAlias = new ArrayList<String>();
			String line = "";
			while ((line = br.readLine()) != null) {
				userAlias.add(line);
				List <String> contact = new ArrayList<String>();
				contactMap.put(line, contact);
			}
			br.close();
			System.out.println("userAlias.size(): " + userAlias.size());
			
			// ��ѯ���������۵�ͼƬ
			DBCollection imgCollection = db.getCollection("images");
			DBObject query = new BasicDBObject();
			query.put("comts", new BasicDBObject("$exists", true));
			DBObject field = new BasicDBObject();
			field.put("comts", true);
			field.put("ownid", true);
			DBCursor imgCursor = imgCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			System.out.println("image with comments num: " + imgCursor.count());
			
			int num = 0;
			while (imgCursor.hasNext()) {
				// �õ�ͼƬ������
				DBObject imgDbo = imgCursor.next();
				String imageOwnerAlias = imgDbo.get("ownid").toString();
				// ���ͼƬ�����߲������ǹ�ע���û�������������б�ǩ���з������ڵ�ͼƬ���û���ֻ���������û���contact��������һ��ͼƬ
				if (!userAlias.contains(imageOwnerAlias)) {
					continue;
				}
				// �õ�����ͼƬ�������б�
				List commentList = (List) imgDbo.get("comts");
				for (int i = 0; i < commentList.size(); i ++) {
					BasicBSONObject comment = (BasicBSONObject) commentList.get(i);
					String commentMakerAlias = comment.get("ownid").toString();
					Date commentDate = (Date) (((DBObject)comment).get("comtt"));
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					String commentDateString = df.format(commentDate);
					// ��������߲������ǹ�ע���û�������������б�ǩ���з������ڵ�ͼƬ���û���ֻ���������û���contact��������һ������
					if (!userAlias.contains(commentMakerAlias)) {
						continue;
					}
					// �����������ͼƬ�����ߣ�����һ������
					if (imageOwnerAlias.equals(commentMakerAlias)) {
						continue;
					}
					// ��������ϣ���¼�����ߺ�����ʱ��
					List <String> contact = contactMap.get(imageOwnerAlias);
					contact.add(commentMakerAlias + ":" + commentDateString);
					contactMap.put(imageOwnerAlias, contact);
				}
				num ++;
				if (num % 1000 == 0) {
					System.out.println(num);
				}
			}
			System.out.println(num);
			
			// ���
			Iterator iter = contactMap.entrySet().iterator(); 
			while (iter.hasNext()) {
			    Map.Entry entry = (Map.Entry) iter.next();
			    String imageOwnerAlias = entry.getKey().toString();
			    List <String> contact = (List <String>) entry.getValue(); 
			    // �����λ�û�û�к������û�����������һλ�û�
			    if (contact.size() == 0) {
			    	continue;
			    }
			    String output = "";
			    output += imageOwnerAlias + " ";
			    for (int i = 0; i < contact.size(); i ++) {
			    	String contactPiece = contact.get(i);
				    output += contactPiece + " ";
				}
				output += "\n";
				bw.append(output);
				bw.flush();
			}
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// ����࣬�õ�����ָ��������SVM��ʽ���ݣ�����ָ��Ϊÿ��ѡȡtrainAndPredictNum��ͼƬ����ȷ�����������еİ汾
	// trainAndPredictNum = 0��ʾȫ��
	public static void getSVMData3(int trainAndPredictNum) {
		try {
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			DBCollection feaCollection = db.getCollection("imgfeature");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date2.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("user_profile_bishe_2605.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/getUserTemporalEmotionType/temporalEmotion_oneDay_lowerLonger2withoutPuncNotSelf_All.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/getContact2/contactNotSelfWithTime.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getSVMData/svm" + trainAndPredictNum + "train.txt")));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("output/getSVMData/svm" + trainAndPredictNum + "predict.txt")));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File("output/getSVMData/svm" + trainAndPredictNum + "predictImageId.txt")));
			
			
			BufferedReader brr7 = new BufferedReader(new FileReader(new File("output/Groups/groupConnect.txt")));
			// ��������Ȧ�����Ϣ��keyΪimageId��valueΪ�û����������ǰһ������Ȧ������������
			String line = ""; 
			Map <String, Integer> imageFEs = new HashMap<String, Integer>();
			while ((line = br3.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				int maxCommentFre = 0;
				int maxCommentFreEmotionType = -1;
				int commentFre[] = {0, 0, 0, 0, 0, 0};
				for (int i = 4; i < part.length; i ++) {
					commentFre[Integer.parseInt(part[i].substring(part[i].length() - 1))] ++;
				}
				for (int i = 0; i < 6; i ++) {
					if (commentFre[i] > maxCommentFre) {
						maxCommentFre = commentFre[i];
						maxCommentFreEmotionType = i;
					}
				}
				imageFEs.put(imageId, maxCommentFreEmotionType);
			}
			System.out.println("imageFEs.size(): " + imageFEs.size());
			
			// ����ͼƬ��Ϣ��keyΪimageId��valueΪ�û�alias#����Ȧ���#ͼƬ���#��������#����ʱ��
			Map <String, String> imageIds = new HashMap<String, String>();
			// ͳ���û�����ͼƬ�����keyΪ�û���alias��valueΪ�û�����ͼƬimage#��������#����ʱ���list
			Map<String, List<String>> userImages = new HashMap<String, List<String>>();
			int fre[] = {0, 0, 0, 0, 0, 0};
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				Integer emotionType = Integer.parseInt(part[1]);
				String userAlias = part[2];
				String dateString = part[3];
				if (fre[emotionType] >= trainAndPredictNum) {
					continue;
				}
				int maxFE = -1;
				if (imageFEs.containsKey(imageId)) {
					maxFE = imageFEs.get(imageId);
				}
				imageIds.put(imageId, userAlias + "#" + maxFE + "#" + emotionType + "#" + dateString);
				if (userImages.containsKey(userAlias)) {
					List <String> images = userImages.get(userAlias);
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				} else {
					List <String> images = new ArrayList<String>();
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				}
				fre[emotionType] ++;
			}
			System.out.println("imageIds.size(): " + imageIds.size());
			System.out.println("userImages.size(): " + userImages.size());
			
			// �����û���Ϣ��keyΪuserAlias��valueΪ��#�ָ������û���Ϣ
			Map <String, String> userProfiles = new HashMap<String, String>();
			while ((line = br2.readLine()) != null) {
				String userAlias = line;
				int attrNum = Integer.parseInt(br2.readLine());
				String attrString = "";
				for (int i = 0; i < attrNum; i ++) {
					line = br2.readLine();
					attrString += line + "#";
				}
				userProfiles.put(userAlias, attrString);
			}
			System.out.println("userProfiles.size(): " + userProfiles.size());
			
			// �����û����������keyΪ�û�alias��valueΪ�û����۹�������ͼƬ������#����ʱ��
			Map<String, List<String>> contact = new HashMap<String, List<String>>();
			while ((line = br4.readLine()) != null) {
				String part[] = line.split(" ");
				String imageOwnerAlias = part[0];
				for (int i = 1; i < part.length; i ++) {
					String littlePart[] = part[i].split(":");
					String commentMakerAlias = littlePart[0];
					String commentDateString = littlePart[1];
					if (contact.containsKey(commentMakerAlias)) {
						List <String> contactList = contact.get(commentMakerAlias);
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					} else {
						List <String> contactList = new ArrayList <String>();
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					}
				}
			}
			System.out.println("contactSize: " + contact.size());
			
			int totalNum = trainAndPredictNum * 6;
			int limitNum = (int) (totalNum * 0.6);
			System.out.println("totalNum: " + totalNum);
			System.out.println("limitNum: " + limitNum);
			
			// ��ÿһ��ͼƬ
			int num = 0;
			int maxFriendSize = 0;
			Map <String, String> imageInfo = new HashMap<String, String>();
			Iterator iter3 = imageIds.entrySet().iterator();
			while (iter3.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter3.next();
				String imageId = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String friendEmotion = valuePart[1];
				String emotionType = valuePart[2];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				DBObject query = new BasicDBObject("_id", imageId);
				DBObject field = new BasicDBObject();
				DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
				while (feaCursor.hasNext()) {
					String output = "";
					DBObject feaDbo = feaCursor.next();
					// �������visual feature
					output += "1:" + feaDbo.get("saturation") 
							+ " 2:" + feaDbo.get("sat_con")
							+ " 3:" + feaDbo.get("bright") 
							+ " 4:" + feaDbo.get("bright_con") 
							+ " 5:" + feaDbo.get("dull_color_ratio")
							+ " 6:"	+ feaDbo.get("cool_color_ratio")
							+ " 7:" + feaDbo.get("fg_color_dif") 
							+ " 8:"	+ feaDbo.get("fg_area_dif") 
							+ " 9:" + feaDbo.get("fg_texture_src") 
							+ " 10:" + feaDbo.get("fg_texture_sal")
							+ " 11:" + feaDbo.get("c1_h") 
							+ " 12:" + feaDbo.get("c1_s") 
							+ " 13:" + feaDbo.get("c1_v") 
							+ " 14:" + feaDbo.get("c2_h") 
							+ " 15:" + feaDbo.get("c2_s") 
							+ " 16:" + feaDbo.get("c2_v") 
							+ " 17:" + feaDbo.get("c3_h") 
							+ " 18:" + feaDbo.get("c3_s") 
							+ " 19:" + feaDbo.get("c3_v") 
							+ " 20:" + feaDbo.get("c4_h") 
							+ " 21:" + feaDbo.get("c4_s") 
							+ " 22:" + feaDbo.get("c4_v") 
							+ " 23:" + feaDbo.get("c5_h") 
							+ " 24:" + feaDbo.get("c5_s") 
							+ " 25:" + feaDbo.get("c5_v");
					// ������п�ѧ��������ʾ��feature������һ��
					if (output.contains("E")) {
						continue;
					}
					// �û�������Ϣ��correlationThree
					// ��0��1�Լ���1��2����ǲ�һ���ģ���1��2������������ά�����������࣬����һ��Ĭ��Ϊ0������0��1������ã�������ά�����϶����ࣩ
					if (userProfiles.containsKey(userAlias)) {
						int gender = -1; // 0: female 1: male
						int marital = -1; // 0: single 1: taken
						int occupation = -1; // 0: engineer 1: artist
						String attrString = userProfiles.get(userAlias);
				    	String attrs[] = attrString.split("#");				
				    	for (int j = 0; j < attrs.length; j ++) {
				    		String part[] = attrs[j].split(":");
				    		if (part[0].equals("I am")) {
				    			if (part[1].contains("Female")) {
				    				gender = 0;
				    			} else if (part[1].contains("Male")) {
				    				gender = 1;
				    			}
				    			if (part[1].contains("Single")) {
				    				marital = 0;
				    			} else if (part[1].contains("Taken")) {
				    				marital = 1;
				    			}
				    		} else if (part[0].equals("Occupation")) {
				    			if (part[1].contains("software") || part[1].contains("IT") || part[1].contains("computer") ||
				    					part[1].contains("code") || part[1].contains("web") || part[1].contains("network") ||
				    					part[1].contains("engineer") || part[1].contains("tech") || part[1].contains("mechanical") ||
				    					part[1].contains("electronic") || part[1].contains("medical") || part[1].contains("market") ||
					    				part[1].contains("product") || part[1].contains("consultant") || part[1].contains("science")) {
					    				occupation = 0;
					    			} else if (part[1].contains("artist") || part[1].contains("writer") || part[1].contains("musician") ||
					    					part[1].contains("dancer") || part[1].contains("photographer") || part[1].contains("film") ||
					    					part[1].contains("designer") || part[1].contains("blog") || part[1].contains("editor") ||
					    					part[1].contains("freelancer")) {
						    			occupation = 1;
						    		}
				    		}
				    	}
				    	if (gender > -1) {
				    		output += " 26:" + gender;
				    	}
				    	if (marital > -1) {
				    		output += " 27:" + marital;
				    	}
				    	if (occupation > -1) {
				    		output += " 28:" + occupation;
				    	}
					}
					// �û�����Ȧ�Ĵ�С
					if (contact.containsKey(userAlias)) {
						List <String> contactList = contact.get(userAlias);
						List <String> friendList = new ArrayList<String>();
						for (int i = 0; i < contactList.size(); i ++) {
							String contactInfo = contactList.get(i);
							String part[] = contactInfo.split("#");
							String friendAlias = part[0];
							String contactDateString = part[1] + "000000";
							Date contactDate = df.parse(contactDateString);
							if (!friendList.contains(friendAlias) && !contactDate.after(publishDate)) {
								friendList.add(friendAlias);
							}
						}
						// 288ΪfriendList.size()�����ֵ
						if (friendList.size() > 0) {
							output += " 29:" + friendList.size() * 1.0 / 158;
						}
						if (friendList.size() > maxFriendSize) {
							maxFriendSize = friendList.size();
						}
					}
					// ����Ȧ��У�correlationTwo
					if (Integer.parseInt(friendEmotion) > -1) {
						output += " 30:" + friendEmotion;
					}
					imageInfo.put(imageId, emotionType + " " + output);
					num ++;
					if (num % 1000 == 0) {
						System.out.println(num);
					}
				}
			}
			System.out.println("num: " + num);
			System.out.println("maxFriendSize: " + maxFriendSize);
			
			// ���
			num = 0;
			Iterator iter4 = imageInfo.entrySet().iterator();
			while (iter4.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter4.next();
				String imageId = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				// ѵ�������Լ�
				if (num < limitNum) {
					bw.append(valueString + "\n");
					bw.flush();
				} else {
					bw2.append(valueString + "\n");
					bw2.flush();
					bw3.append(imageId + "\n");
					bw3.flush();
				}
				num ++;
			}
			
			br.close();
			br2.close();
			br3.close();
			br4.close();
			bw.close();
			bw2.close();
			bw3.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// trainAndPredictNum = 0��ʾȫ��
	public static void getNaiveBayesData(int trainAndPredictNum) {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			DBCollection feaCollection = db.getCollection("imgfeature");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("user_profile_bishe_2605.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/getUserTemporalEmotionType/temporalEmotion_oneDay_lowerLonger2withoutPuncNotSelf.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/getContact2/contactNotSelfWithTime.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getNaiveBayesData/NB" + trainAndPredictNum + "train.txt")));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("output/getNaiveBayesData/NB" + trainAndPredictNum + "predict.txt")));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File("output/getNaiveBayesData/NB" + trainAndPredictNum + "predictImageId.txt")));
			
			String line = "";
			
			// ��������Ȧ�����Ϣ��keyΪimageId��valueΪ�û����������ǰһ������Ȧ������������
			Map <String, Integer> imageFEs = new HashMap<String, Integer>();
			while ((line = br3.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				int maxCommentFre = 0;
				int maxCommentFreEmotionType = -1;
				int commentFre[] = {0, 0, 0, 0, 0, 0};
				for (int i = 4; i < part.length; i ++) {
					commentFre[Integer.parseInt(part[i].substring(part[i].length() - 1))] ++;
				}
				for (int i = 0; i < 6; i ++) {
					if (commentFre[i] > maxCommentFre) {
						maxCommentFre = commentFre[i];
						maxCommentFreEmotionType = i;
					}
				}
				imageFEs.put(imageId, maxCommentFreEmotionType);
			}
			System.out.println("imageFEs.size(): " + imageFEs.size());
			
			// ����ͼƬ��Ϣ��keyΪimageId��valueΪ�û�alias#����Ȧ���#ͼƬ���#��������#����ʱ��
			Map <String, String> imageIds = new HashMap<String, String>();
			// ͳ���û�����ͼƬ�����keyΪ�û���alias��valueΪ�û�����ͼƬimage#��������#����ʱ���list
			Map<String, List<String>> userImages = new HashMap<String, List<String>>();
			int fre[] = {0, 0, 0, 0, 0, 0};
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				Integer emotionType = Integer.parseInt(part[1]);
				String userAlias = part[2];
				String dateString = part[3];
				if (fre[emotionType] >= trainAndPredictNum) {
					continue;
				}
				int maxFE = -1;
				if (imageFEs.containsKey(imageId)) {
					maxFE = imageFEs.get(imageId);
				}
				imageIds.put(imageId, userAlias + "#" + maxFE + "#" + emotionType + "#" + dateString);
				if (userImages.containsKey(userAlias)) {
					List <String> images = userImages.get(userAlias);
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				} else {
					List <String> images = new ArrayList<String>();
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				}
				fre[emotionType] ++;
			}
			System.out.println("imageIds.size(): " + imageIds.size());
			System.out.println("userImages.size(): " + userImages.size());
			
			// �����û���Ϣ��keyΪuserAlias��valueΪ��#�ָ������û���Ϣ
			Map <String, String> userProfiles = new HashMap<String, String>();
			while ((line = br2.readLine()) != null) {
				String userAlias = line;
				int attrNum = Integer.parseInt(br2.readLine());
				String attrString = "";
				for (int i = 0; i < attrNum; i ++) {
					line = br2.readLine();
					attrString += line + "#";
				}
				userProfiles.put(userAlias, attrString);
			}
			System.out.println("userProfiles.size(): " + userProfiles.size());
			
			// �����û����������keyΪ�û�alias��valueΪ�û����۹�������ͼƬ������#����ʱ��
			Map<String, List<String>> contact = new HashMap<String, List<String>>();
			while ((line = br4.readLine()) != null) {
				String part[] = line.split(" ");
				String imageOwnerAlias = part[0];
				for (int i = 1; i < part.length; i ++) {
					String littlePart[] = part[i].split(":");
					String commentMakerAlias = littlePart[0];
					String commentDateString = littlePart[1];
					if (contact.containsKey(commentMakerAlias)) {
						List <String> contactList = contact.get(commentMakerAlias);
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					} else {
						List <String> contactList = new ArrayList <String>();
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					}
				}
			}
			System.out.println("contactSize: " + contact.size());
			
			int totalNum = trainAndPredictNum * 6;
			int limitNum = (int) (totalNum * 0.6);
			System.out.println("totalNum: " + totalNum);
			System.out.println("limitNum: " + limitNum);
			
			// ��ÿһ��ͼƬ
			int num = 0;
			int maxFriendSize = 0;
			Map <String, String> imageInfo = new HashMap<String, String>();
			Iterator iter3 = imageIds.entrySet().iterator();
			while (iter3.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter3.next();
				String imageId = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String friendEmotion = valuePart[1];
				String emotionType = valuePart[2];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				DBObject query = new BasicDBObject("_id", imageId);
				DBObject field = new BasicDBObject();
				DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
				while (feaCursor.hasNext()) {
					String output = "";
					DBObject feaDbo = feaCursor.next();
					// �������visual feature
					output += feaDbo.get("saturation") 
							+ " " + feaDbo.get("sat_con")
							+ " " + feaDbo.get("bright") 
							+ " " + feaDbo.get("bright_con") 
							+ " " + feaDbo.get("dull_color_ratio")
							+ " "	+ feaDbo.get("cool_color_ratio")
							+ " " + feaDbo.get("fg_color_dif") 
							+ " "	+ feaDbo.get("fg_area_dif") 
							+ " " + feaDbo.get("fg_texture_src") 
							+ " " + feaDbo.get("fg_texture_sal")
							+ " " + feaDbo.get("c1_h") 
							+ " " + feaDbo.get("c1_s") 
							+ " " + feaDbo.get("c1_v") 
							+ " " + feaDbo.get("c2_h") 
							+ " " + feaDbo.get("c2_s") 
							+ " " + feaDbo.get("c2_v") 
							+ " " + feaDbo.get("c3_h") 
							+ " " + feaDbo.get("c3_s") 
							+ " " + feaDbo.get("c3_v") 
							+ " " + feaDbo.get("c4_h") 
							+ " " + feaDbo.get("c4_s") 
							+ " " + feaDbo.get("c4_v") 
							+ " " + feaDbo.get("c5_h") 
							+ " " + feaDbo.get("c5_s") 
							+ " " + feaDbo.get("c5_v");
					// ������п�ѧ��������ʾ��feature������һ��
					if (output.contains("E")) {
						continue;
					}
					// �û�������Ϣ��correlationThree
					// ��0��1�Լ���1��2����ǲ�һ���ģ���1��2������������ά�����������࣬����һ��Ĭ��Ϊ0������0��1������ã�������ά�����϶����ࣩ
					int gender = -1; // 0: female 1: male
					int marital = -1; // 0: single 1: taken
					int occupation = -1; // 0: engineer 1: artist
					if (userProfiles.containsKey(userAlias)) {
						String attrString = userProfiles.get(userAlias);
				    	String attrs[] = attrString.split("#");				
				    	for (int j = 0; j < attrs.length; j ++) {
				    		String part[] = attrs[j].split(":");
				    		if (part[0].equals("I am")) {
				    			if (part[1].contains("Female")) {
				    				gender = 0;
				    			} else if (part[1].contains("Male")) {
				    				gender = 1;
				    			}
				    			if (part[1].contains("Single")) {
				    				marital = 0;
				    			} else if (part[1].contains("Taken")) {
				    				marital = 1;
				    			}
				    		} else if (part[0].equals("Occupation")) {
				    			if (part[1].contains("software") || part[1].contains("IT") || part[1].contains("computer") ||
				    					part[1].contains("code") || part[1].contains("web") || part[1].contains("network") ||
				    					part[1].contains("engineer") || part[1].contains("tech") || part[1].contains("mechanical") ||
				    					part[1].contains("electronic") || part[1].contains("medical") || part[1].contains("market") ||
					    				part[1].contains("product") || part[1].contains("consultant") || part[1].contains("science")) {
					    				occupation = 0;
					    			} else if (part[1].contains("artist") || part[1].contains("writer") || part[1].contains("musician") ||
					    					part[1].contains("dancer") || part[1].contains("photographer") || part[1].contains("film") ||
					    					part[1].contains("designer") || part[1].contains("blog") || part[1].contains("editor") ||
					    					part[1].contains("freelancer")) {
						    			occupation = 1;
						    		}
				    		}
				    	}
					}
					output += " " + gender;
		    		output += " " + marital;
		    		output += " " + occupation;
					// �û�����Ȧ�Ĵ�С
		    		float friendSize = -1;
					if (contact.containsKey(userAlias)) {
						List <String> contactList = contact.get(userAlias);
						List <String> friendList = new ArrayList<String>();
						for (int i = 0; i < contactList.size(); i ++) {
							String contactInfo = contactList.get(i);
							String part[] = contactInfo.split("#");
							String friendAlias = part[0];
							String contactDateString = part[1] + "000000";
							Date contactDate = df.parse(contactDateString);
							if (!friendList.contains(friendAlias) && !contactDate.after(publishDate)) {
								friendList.add(friendAlias);
							}
						}
						// 288ΪfriendList.size()�����ֵ
						if (friendList.size() > 0) {
							friendSize = (float) (friendList.size() * 1.0 / 158);
						}
						if (friendList.size() > maxFriendSize) {
							maxFriendSize = friendList.size();
						}
					}
					output += " " + friendSize;
					// ����Ȧ��У�correlationTwo
					int maxFriendEmotion = -1;
					if (Integer.parseInt(friendEmotion) > -1) {
						maxFriendEmotion = Integer.parseInt(friendEmotion);
					}
					output += " " + maxFriendEmotion;
					
					imageInfo.put(imageId, emotionType + " " + output);
					num ++;
					if (num % 1000 == 0) {
						System.out.println(num);
					}
				}
			}
			System.out.println("num: " + num);
			System.out.println("maxFriendSize: " + maxFriendSize);
			
			// ���
			num = 0;
			Iterator iter4 = imageInfo.entrySet().iterator();
			while (iter4.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter4.next();
				String imageId = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				// ѵ�������Լ�
				if (num < limitNum) {
					bw.append(valueString + "\n");
					bw.flush();
				} else {
					bw2.append(valueString + "\n");
					bw2.flush();
					bw3.append(imageId + "\n");
					bw3.flush();
				}
				num ++;
			}
			
			br.close();
			br2.close();
			br3.close();
			br4.close();
			bw.close();
			bw2.close();
			bw3.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// libsvmֻ�����������ȷ�ʣ�����ʹ��libsvm������Ԥ��������ȷ������бȽϣ��õ��������ȷ��
	public static void analyzeSVMResult(int trainAndPredictNum) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getSVMData/svm" + trainAndPredictNum + "predict.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getSVMData/svm" + trainAndPredictNum + "result.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getSVMData/svm" + trainAndPredictNum + "f1.txt")));
			
			
			String line = ""; 
			int cnt[][] = new int[6][6];
			int answer[] = new int[220000];
			int num = 0;
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				int label = Integer.parseInt(part[0]);
				answer[num] = label;
				num ++;
			}
			
			num = 0;
			while ((line = br2.readLine()) != null) {
				int result = Integer.parseInt(line.trim());
				int correct = answer[num];
				cnt[correct][result] ++;
				num ++;
			}
			System.out.println("num: " + num);
			
			int rowSum[] = {0, 0, 0, 0, 0, 0};
			int colSum[] = {0, 0, 0, 0, 0, 0};
			for (int i = 0; i < 6; i ++) {
				for (int j = 0; j < 6; j ++) {
					rowSum[i] += cnt[i][j];
					colSum[j] += cnt[i][j];
					bw.append(cnt[i][j] + " ");
					bw.flush();
				}
				bw.append("\n");
				bw.flush();
			}
			
			float averagePrecision = 0;
			float averageRecall = 0;
			float averageF1 = 0;
			
			for (int i = 0; i < 6; i ++) {
				float recall = (float) (cnt[i][i] * 1.0 / rowSum[i]);
				float precision = (float) (cnt[i][i] * 1.0 / colSum[i]);
				float f1 = 2 * precision * recall / (precision + recall);
				averagePrecision += precision;
				averageRecall += recall;
				averageF1 += f1;
				bw.append(precision + " " + recall + " " + f1 + "\n");
				bw.flush();
			}
			bw.append(averagePrecision / 6 + " " + averageRecall / 6 + " " + averageF1 / 6 + "\n");
			bw.flush();
			
			br.close();
			br2.close();
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void analyzeNaiveBayesResult(int trainAndPredictNum) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getNaiveBayesData/NB" + trainAndPredictNum + "result.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getNaiveBayesData/NB" + trainAndPredictNum + "f1.txt")));
			
			
			String line = ""; 
			int cnt[][] = new int[6][6];
			int num = 0;
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				for (int i = 0; i < part.length; i ++) {
					cnt[num][i] = Integer.parseInt(part[i]);
				}
				num ++;
			}
			
			int rowSum[] = {0, 0, 0, 0, 0, 0};
			int colSum[] = {0, 0, 0, 0, 0, 0};
			for (int i = 0; i < 6; i ++) {
				for (int j = 0; j < 6; j ++) {
					rowSum[i] += cnt[i][j];
					colSum[j] += cnt[i][j];
				}
			}
			
			float averagePrecision = 0;
			float averageRecall = 0;
			float averageF1 = 0;
			
			for (int i = 0; i < 6; i ++) {
				float precision = (float) (cnt[i][i] * 1.0 / rowSum[i]);
				float recall = (float) (cnt[i][i] * 1.0 / colSum[i]);
				float f1 = 2 * precision * recall / (precision + recall);
				averagePrecision += precision;
				averageRecall += recall;
				averageF1 += f1;
				bw.append(precision + " " + recall + " " + f1 + "\n");
				bw.flush();
			}
			bw.append(averagePrecision / 6 + " " + averageRecall / 6 + " " + averageF1 / 6 + "\n");
			bw.flush();
			
			br.close();
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// for aaai almost same as bishe
	// ������baseline ---wenjing ��������
	// ȥ�����п�ѧ��������feature����֪��Ϊʲô���mongodbȡ������ͬ������ͬһ��ͼ���ÿ�ѧ��������ʾ��
	// ����࣬�õ�����һ������BasicFGM��ʽ��data�������û���Ϣ����������Ȧ��������ѵ���У������û��佻��ǿ�ȣ�ÿ������ΪtrainAndPredictNum����һ��ÿ�Ŷ���feature�����ÿ���������ܱ����������һЩ��
	public static void getBasicFGMData4(int trainAndPredictNum) {
		try {
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			DBCollection feaCollection = db.getCollection("imgfeature");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date2.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("user_profile_bishe_2605.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/getUserTemporalEmotionType/temporalEmotion_oneDay_lowerLonger2withoutPuncNotSelf.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/getContact2/contactNotSelfWithTime.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/baseline_4_" + trainAndPredictNum + "_su10m_withoutUD.txt")));
			BufferedWriter bww = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/userIdused.txt")));//��¼�ú�����ѡȡ���û����Ա�֮�����group id����ʱ��֪����Щ�û���ʹ��
			
			// ��������Ȧ�����Ϣ��keyΪimageId��valueΪ�û����������ǰһ������Ȧ������������
			String line = ""; 
			Map <String, Integer> imageFEs = new HashMap<String, Integer>();
			while ((line = br3.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				int maxCommentFre = 0;
				int maxCommentFreEmotionType = -1;
				int commentFre[] = {0, 0, 0, 0, 0, 0};
				for (int i = 4; i < part.length; i ++) {
					commentFre[Integer.parseInt(part[i].substring(part[i].length() - 1))] ++; //���۵����
				}
				for (int i = 0; i < 6; i ++) {
					if (commentFre[i] > maxCommentFre) {
						maxCommentFre = commentFre[i];
						maxCommentFreEmotionType = i;
					}
				}
				imageFEs.put(imageId, maxCommentFreEmotionType);
			}
			System.out.println("imageFEs.size(): " + imageFEs.size());
			
			// ����ͼƬ��Ϣ��keyΪimageId��valueΪ�û�alias#����Ȧ���#ͼƬ���#��������#����ʱ��
			Map <String, String> imageIds = new HashMap<String, String>();
			// ͳ���û�����ͼƬ�����keyΪ�û���alias��valueΪ�û�����ͼƬimage#��������#����ʱ���list
			Map<String, List<String>> userImages = new HashMap<String, List<String>>();
			int fre[] = {0, 0, 0, 0, 0, 0};
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				Integer emotionType = Integer.parseInt(part[1]);
				String userAlias = part[2];
				String dateString = part[3];
				if (fre[emotionType] >= trainAndPredictNum) {
					continue;
				}
				int maxFE = -1;
				if (imageFEs.containsKey(imageId)) {
					maxFE = imageFEs.get(imageId);
				}
				imageIds.put(imageId, userAlias + "#" + maxFE + "#" + emotionType + "#" + dateString);
				if (userImages.containsKey(userAlias)) {
					List <String> images = userImages.get(userAlias);
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				} else {
					List <String> images = new ArrayList<String>();
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				}
				fre[emotionType] ++;
			}
			System.out.println("imageIds.size(): " + imageIds.size());
			System.out.println("userImages.size(): " + userImages.size());
			
			// �����û���Ϣ��keyΪuserAlias��valueΪ��#�ָ������û���Ϣ
			Map <String, String> userProfiles = new HashMap<String, String>();
			while ((line = br2.readLine()) != null) {
				String userAlias = line;
				int attrNum = Integer.parseInt(br2.readLine());
				String attrString = "";
				for (int i = 0; i < attrNum; i ++) {
					line = br2.readLine();
					attrString += line + "#";
				}
				userProfiles.put(userAlias, attrString);
			}
			System.out.println("userProfiles.size(): " + userProfiles.size());
			
			// �����û����������keyΪ�û�alias��valueΪ�û����۹�������ͼƬ������#����ʱ��
			Map<String, List<String>> contact = new HashMap<String, List<String>>();
			while ((line = br4.readLine()) != null) {
				String part[] = line.split(" ");
				String imageOwnerAlias = part[0];
				for (int i = 1; i < part.length; i ++) {
					String littlePart[] = part[i].split(":");
					String commentMakerAlias = littlePart[0];
					String commentDateString = littlePart[1];
					if (contact.containsKey(commentMakerAlias)) {
						List <String> contactList = contact.get(commentMakerAlias);
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					} else {
						List <String> contactList = new ArrayList <String>();
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					}
				}
			}
			System.out.println("contactSize: " + contact.size());
			
			// ������feature��Ϣ��ͼƬ����
			/*int totalNum = 0;
			Iterator iter = imageIds.entrySet().iterator();
			while (iter.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter.next();
				String imageId = (String) entry.getKey();
				DBObject feaQuery = new BasicDBObject("_id", imageId);
				DBCursor feaCursor = feaCollection.find(feaQuery).limit(1);
				while (feaCursor.hasNext()) {
					DBObject feaDbo = feaCursor.next();
					totalNum ++;
				}
			}
			
			// ����ѵ��������
			int limitNum = (int) (totalNum * 0.6);*/
			
			int totalNum = trainAndPredictNum * 6;
			int limitNum = (int) (totalNum * 0.6);
			System.out.println("totalNum: " + totalNum);
			System.out.println("limitNum: " + limitNum);
			
			int lineNum = 1;
			int num = 0;
			
			// ��ÿһ��ͼƬ
			int maxFriendSize = 0;
			Map<String, String> lineDict = new HashMap<String, String>();
			Iterator iter3 = imageIds.entrySet().iterator();
			while (iter3.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter3.next();
				String imageId = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String friendEmotion = valuePart[1];
				String emotionType = valuePart[2];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				DBObject query = new BasicDBObject("_id", imageId);
				DBObject field = new BasicDBObject();
				DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
				while (feaCursor.hasNext()) {
					String output = "";
					// ѵ�������Լ�
					if (num < limitNum) {
						output = "+";
					} else {
						output = "?";
					}
					DBObject feaDbo = feaCursor.next();
					// �������visual feature
					output += emotionType
							+ " saturation:" + feaDbo.get("saturation") 
							+ " sat_con:" + feaDbo.get("sat_con")
							+ " bright:" + feaDbo.get("bright") 
							+ " bright_con:" + feaDbo.get("bright_con") 
							+ " dull_color_ratio:" + feaDbo.get("dull_color_ratio")
							+ " cool_color_ratio:"	+ feaDbo.get("cool_color_ratio")
							+ " fg_color_df:" + feaDbo.get("fg_color_dif") 
							+ " fg_area_dif:"	+ feaDbo.get("fg_area_dif") 
							+ " fg_texture_src:" + feaDbo.get("fg_texture_src") 
							+ " fg_texture_sal:" + feaDbo.get("fg_texture_sal")
							+ " c1_h:" + feaDbo.get("c1_h") 
							+ " c1_s:" + feaDbo.get("c1_s") 
							+ " c1_v:" + feaDbo.get("c1_v") 
							+ " c2_h:" + feaDbo.get("c2_h") 
							+ " c2_s:" + feaDbo.get("c2_s") 
							+ " c2_v:" + feaDbo.get("c2_v") 
							+ " c3_h:" + feaDbo.get("c3_h") 
							+ " c3_s:" + feaDbo.get("c3_s") 
							+ " c3_v:" + feaDbo.get("c3_v") 
							+ " c4_h:" + feaDbo.get("c4_h") 
							+ " c4_s:" + feaDbo.get("c4_s") 
							+ " c4_v:" + feaDbo.get("c4_v") 
							+ " c5_h:" + feaDbo.get("c5_h") 
							+ " c5_s:" + feaDbo.get("c5_s") 
							+ " c5_v:" + feaDbo.get("c5_v");
					// ������п�ѧ��������ʾ��feature������һ��
					if (output.contains("E")) {
						continue;
					}
					// �û�������Ϣ��correlationThree  --wenjing
					if (userProfiles.containsKey(userAlias)) {
						int gender = -1; // 0: female 1: male
						int status = -1; // 0: single 1: taken
						String attrString = userProfiles.get(userAlias);
				    	String attrs[] = attrString.split("#");				
				    	for (int j = 0; j < attrs.length; j ++) {
				    		String part[] = attrs[j].split(":");
				    		if (part[0].equals("I am")) {
				    			if (part[1].contains("Female")) {
				    				gender = 0;
				    			} else if (part[1].contains("Male")) {
				    				gender = 1;
				    			}
				    			if (part[1].contains("Single")) {
				    				status = 0;
				    			} else if (part[1].contains("Taken")) {
				    				status = 1;
				    			}
				    		}
				    	}
				    	if (gender > -1) {
				    		output += " gender:" + gender;
				    	}
				    	if (status > -1) {
				    		output += " status:" + status;
				    	}
					}  //--wenjing
					// �û�����Ȧ�Ĵ�С
					if (contact.containsKey(userAlias)) {
						List <String> contactList = contact.get(userAlias);
						List <String> friendList = new ArrayList<String>();
						for (int i = 0; i < contactList.size(); i ++) {
							String contactInfo = contactList.get(i);
							String part[] = contactInfo.split("#");
							String friendAlias = part[0];
							String contactDateString = part[1] + "000000";
							Date contactDate = df.parse(contactDateString);
							if (!friendList.contains(friendAlias) && !contactDate.after(publishDate)) {
								friendList.add(friendAlias);
							}
						}
						// 288ΪfriendList.size()�����ֵ--wenjing 158 max
						if (friendList.size() > 0) {
							output += " friendSize:" + friendList.size() * 1.0 / 158;
						}
						if (friendList.size() > maxFriendSize) {
							maxFriendSize = friendList.size();
						}
					}
					// ����Ȧ��У�correlationTwo
					if (Integer.parseInt(friendEmotion) > -1) {
						output += " friendEmotion:" + friendEmotion;
					}
					output += " \n";
					bw.append(output);
					bw.flush();
					lineDict.put(imageId, String.valueOf(lineNum));
					bww.append(imageId + " " + lineNum + "\n");//wenjing add
					lineNum ++;
					num ++;
					if (num % 1000 == 0) {
						System.out.println(num);
					}
				}
			}
			System.out.println("num: " + num);
			System.out.println("maxFriendSize: " + maxFriendSize);
			
			// edge temporal correlation����ĳ��ʱ���ڣ��������ͼƬ��ͬһλ�û���������������ͼƬ�����һ����
			// ��ÿһ��ͼƬ��Ϣ���õ���ӦͼƬid
			Iterator iter5 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter5.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter5.next();
			    String lineId = entry.getValue().toString();
			    String imageId = entry.getKey().toString();
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
			    Date closestDate = leftDate;
		        String closestUserImageLineId = "-1";
		        // ���Ҹ��û�������ͼƬ
				List <String> images = userImages.get(userAlias);
				for (int i = 0; i < images.size(); i ++) {
					String imageIdAndDateString = images.get(i);
					String imageIdAndDatePart[] = imageIdAndDateString.split("#");
					String userImageId = imageIdAndDatePart[0];
					String userImageDateString = imageIdAndDatePart[1];
					Date userImageDate = df.parse(userImageDateString);
					// �����ͬһ��ͼƬ������һ��
					if (imageId.equals(userImageId)) {
						continue;
					}
			        // ����ںϷ���ʱ��������
			        if (userImageDate.after(leftDate) && userImageDate.before(publishDate) && userImageDate.after(closestDate)) {
			        	//System.out.println(df.format(userImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
			        	// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ�
			        	if (lineDict.containsKey(userImageId)) {
				        	String userImageLineId = lineDict.get(userImageId);
				        	closestDate = userImageDate;
				        	closestUserImageLineId = userImageLineId;
			        	}
			        }
				}//��¼�����һ��ͼƬ
				if (!closestUserImageLineId.equals("-1")) {
					String output = "#edge " + lineId + " " + closestUserImageLineId + " sameuser\n";
		        	bw.append(output);
		        	bw.flush();
				}
			}
			
			// edge correlationOne���õ�ͼƬ֮���Ӱ��������
			// <5 <10 ..... <50 >50 --wenjing
			int contactStatics[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			Iterator iter6 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter6.hasNext()) { 
			    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter6.next();
			    String imageId = entry.getKey().toString();
			    String lineId = entry.getValue().toString();
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				Date publishDate = df.parse(dateString);
				
				// �õ��Ϸ���ʱ������
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
				// ʹ��ͼƬ�����û����Ҹ��û����۹��������û�����һ�����Բ鵽
				if (contact.containsKey(userAlias)) {
					List<String> contactList = contact.get(userAlias);
					// ��ÿһλ�н��������ѣ����㽻����ǿ��
					Map <String, Integer> contactFre = new HashMap<String, Integer>();
					for(int i = 0; i < contactList.size(); i ++) {
						String contactContent = contactList.get(i);
						String part[] = contactContent.split("#");
						String friendAlias = part[0];
						String contactDateString = part[1] + "000000";
						Date contactDate = df.parse(contactDateString);
						// �������ʱ����ͼƬ����ʱ��ǰ����¼һ�ν���
						if (!contactDate.after(publishDate)) {
							if (contactFre.containsKey(friendAlias)) {
								int contactNum = contactFre.get(friendAlias);
								contactNum ++;
								contactFre.put(friendAlias, contactNum);
							} else {
								int contactNum = 1;
								contactFre.put(friendAlias, contactNum);
							}
						}
					} //--wenjing
					
					
					/*Iterator iter8 = contactFre.entrySet().iterator();
					while (iter8.hasNext()) {
						Map.Entry<String, Integer> entry8 = (Map.Entry<String, Integer>) iter8.next();
						int contactNum = entry8.getValue();
						if (contactNum >= 50) {
							contactStatics[10] ++;
						} else {
							contactStatics[contactNum / 5] ++;
						}
					}
					for (int i = 0; i < 10; i ++) {
						System.out.print(contactStatics[i] + " ");
					}
					System.out.println();*/
					
					// --wenjing
					Iterator iter7 = contactFre.entrySet().iterator();
					while (iter7.hasNext()) {
						Map.Entry<String, Integer> entry7 = (Map.Entry<String, Integer>) iter7.next();
						String friendAlias = entry7.getKey();
						int contactNum = entry7.getValue();
						int contactLevel = -1;
						if (contactNum < 10) {
							contactLevel = 1;
						} else if (contactNum < 20) {
							contactLevel = 2;
						} else {
							contactLevel = 3;
						}
						// ������λ���ѷ���������ͼƬ
						if (userImages.containsKey(friendAlias)) {
							List <String> friendImages = userImages.get(friendAlias);
							for (int  i = 0; i < friendImages.size(); i ++) {
								String friendImageIdAndDate = friendImages.get(i);
								String friendImageIdAndDatePart[] = friendImageIdAndDate.split("#");
								String friendImageId = friendImageIdAndDatePart[0];
								String friendImageDateString = friendImageIdAndDatePart[1];
								Date friendImageDate = df.parse(friendImageDateString);
								
						        // ����ںϷ���ʱ��������
						        if (friendImageDate.after(leftDate) && friendImageDate.before(publishDate)) {
						        	//System.out.println(df.format(friendImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
						        	// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ�
						        	if (lineDict.containsKey(friendImageId)) {
							        	String friendLineId = lineDict.get(friendImageId);
							        	String output = "#weight_edge " + lineId + " " + friendLineId + " friendimpact " + contactLevel + "\n";
							        	bw.append(output);
							        	bw.flush();
						        	}
						        }
							}
						}
					}
				}
		    } //--wenjing
			
			br.close();
			br2.close();
			br3.close();
			br4.close();
			bw.close();
			bww.close();
			db.cleanCursors(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//for wenjing bishe
	//��ʹ�ſ���ʱ�����ƣ�ֻ�����Ӻܶ�groupimpact = 1��weight edge
	public static void getFGMdataGroup(int trainAndPredictNum){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/baseline_8_" + trainAndPredictNum + "_less10_withoutE.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/userIdused.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/Groups/groupUserImages&Date.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/group1_8_groupimpact123_time1day_" + trainAndPredictNum + "_less10_withoutE.txt")));
			//0.123��ʾȺ��impact�ֳ�0.1 0.2 0.3
			//key is img id, value is index(1, 2, ...)
			Map<String, String> userId = new HashMap<String, String>();
			String line = "";
			while((line = br2.readLine())!= null){
				String p[] = line.split(" ");
				//System.out.println("line "+ line);
				userId.put(p[0], p[1]);
			}
			
			System.out.println("userId size : " + userId.size());//35089
			
			//ÿ��Ⱥ���ͼƬ��key��group, value ��img#img#img...
			Map<String, String> groupimgs = new HashMap<String, String>();
			//ÿ��ͼƬ����Ⱥ������������ʱ�䣬key is image id, value :publishdate#group#group2#group3...
			Map<String, String> imgsgroupDate = new HashMap<String, String>();
			while((line = br3.readLine())!= null){//for a group
				String pp[] = line.split(" ");
				String imgs = "";
				for(int i = 0; i < Integer.valueOf(pp[1]); i++){//for a user
					line = br3.readLine();
					String part[] = line.split(" ");
					for(int j = 1; j < part.length; j += 2){
						if(!imgsgroupDate.containsKey(part[j])){
							imgsgroupDate.put(part[j], part[j+1]+"#"+pp[0]);//ÿ��ͼƬ���Ⱥ����ô�죿
						}else{
							String dategs = imgsgroupDate.get(part[j]);
							dategs = dategs + "#" + pp[0];
							imgsgroupDate.put(part[j], dategs);
						}
						imgs = imgs + "#" + part[j];
					}
				}
				groupimgs.put(pp[0], imgs.substring(1, imgs.length()));
			}
			
			System.out.println("num of groups " + groupimgs.size());
			System.out.println("images of 60356848@N00 " + groupimgs.get("60356848@N00"));
			
			System.out.println("lineDict Size: " + userId.size());
			
			int num = 0;
			int w = 0;
			//ÿ��ͬȺ���û���������ͬȺ������key��lineId#imgs.get(img),value��������������ͬ��Ⱥ����Ŀ
			Map<String, Integer> imgGnum = new HashMap<String, Integer>();
			while((line = br.readLine()) != null){
				//if(!(line.split(" ")[0].equals("#weight_edge"))){
					//bw.append(line+"\n");//readline()�Զ���ȥ���з�
					//continue;
			}
			//bw.append(line+"\n");
			//}
				//System.out.println("same group");
				//same group
				Iterator iter = userId.entrySet().iterator(); 
				while (iter.hasNext()) { 
				    Map.Entry entry = (Map.Entry) iter.next();
				    String lineId = entry.getValue().toString();
				    String imageId = entry.getKey().toString();
				    //System.out.println("imageis "+imageId);
				    if(imgsgroupDate.containsKey(imageId)){//for each image
				    	String dategs[] = imgsgroupDate.get(imageId).split("#");
				    	String dateString= dategs[0];
				    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
						Date publishDate = df.parse(dateString);
						//System.out.println("publish date "+publishDate);
						Calendar rightNow = Calendar.getInstance();
				        rightNow.setTime(publishDate);
				        rightNow.add(Calendar.DATE, -10);//���ڼ�10���� -> ��1��
				        Date leftDate = rightNow.getTime();
				        
				    	for(int i = 1; i < dategs.length; i++){//for a group
				    		String group = dategs[i];
				    		//System.out.println("group "+group);
				    		String imgs[] = groupimgs.get(group).split("#");//��Ⱥ����������û�
				    		for(int u = 0; u < imgs.length; u++){
				    			String img = imgs[u];
				    			String imgDate = imgsgroupDate.get(img).split("#")[0];
				    			Date imageDate = df.parse(imgDate);
				    			//System.out.println("group img:------");
				    			//System.out.println(imageId + " " + img + " " + group);
				    			if(imageId.equals(img))
				    				continue;
				    			//��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
				    			if(!userId.containsKey(img))
				    				continue;
				    			 // ����ںϷ���ʱ��������
								if (imageDate.after(leftDate) && imageDate.before(publishDate)) {
									//String output = "#edge " + lineId + " " + userId.get(img) + " samegroup\n";
									//bw.append(output);
									//bw.flush();
									String lineIds = lineId+"#"+userId.get(img);
									if(imgGnum.containsKey(lineIds)){
										int number = imgGnum.get(lineIds);
										number++;
										imgGnum.put(lineIds, number);
									}else{
										imgGnum.put(lineIds,  1);
									}
									num++;
									//System.out.println(imageId + " " + img + " " + group);
									//System.out.println(lineId + " " + userId.get(img) + " " + group);
					    			if(num % 1000 == 0)
					    				System.out.println(num);
					    				//return;
								}
				    		}
				    	}
				    }
				}
				
				System.out.println(num);
				Iterator iter2 = imgGnum.entrySet().iterator();
				while(iter2.hasNext()){
					Map.Entry entry2 = (Map.Entry) iter2.next();
					String lineids[] = entry2.getKey().toString().split("#");
					String numString = entry2.getValue().toString();
					String impact = "";
					if(Integer.valueOf(numString) < 10)
						impact = "1";
					else{
						if(Integer.valueOf(numString) < 20)
							impact = "2";
						else
							impact = "3";
					}
					bw.append("#weight_edge " + lineids[0] + " " + lineids[1] + " groupimpact " + numString + "\n");
					bw.flush();
				}
			//br.close();
			br2.close();
			br3.close();
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//for wenjing bishe
	//�ú���������FGM������ĵ����Ϣ��
	//��getBasicFGMData4/8֮����޸ģ�����Ⱥ�����أ�����ͼƬ�����ϣ�ÿ��ͼƬ����Ⱥ����Ŀ���ͷ��ϵ�Ⱥ�����ԣ�social role������С - group emotion��
	//�����û�opinion leader��Ϣ��
	//���ӵ��ڲ�����percent���ж�ol��������ֵ,���������û��Ƿ�ol,��������ÿ��Ⱥ���ol����, minRatio�Ǹ���ol�����ж�ÿ��Ⱥ��ķ��ࡣ
	//�����Ӳ�����contactRatio:����ÿ��Ⱥ�����ͨ�ȴ�С����Ⱥ�����  ---2017-5-8
	//ȥ���û�opinion leader��Ϣ 2017-5-10
	//����structure hole spanner, percentҲ��sh spanner�ı�����ֵ
	public static void getFGMDataGroup2(int trainAndPredictNum, int percent, int minRatio, int contactRatio, int shRatio){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/baseline_8_" + trainAndPredictNum + "_less10_withoutE.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/userIdused.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/Groups/groupUserImages&Date.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/Groups/groupsEmotionRatioDate.txt")));
			BufferedReader br5 = new BufferedReader(new FileReader(new File("output/opinionLeader/userOpinionleaders_" +percent + ".txt")));
			BufferedReader br6 = new BufferedReader(new FileReader(new File("output/opinionLeader/groupOLratio_" + percent + ".txt")));
			
			BufferedReader br7 = new BufferedReader(new FileReader(new File("output/Groups/groupConnect.txt")));
			//BufferedReader br7 = new BufferedReader(new FileReader(new File("output/Groups/groupEdgeConnectivity.txt")));//�޸�Ⱥ����ͨ�ȵĶ��壬���ļ���¼ÿ��Ⱥ��ı���ͨ��
			BufferedReader br8 = new BufferedReader(new FileReader(new File("output/SHspanner/userSHspanner_" + percent + ".txt")));//user and 1(yes) / 0(no)
			BufferedReader br9 = new BufferedReader(new FileReader(new File("output/SHspanner/groupSHratio_" + percent + ".txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/group1_8_groupemotion_minpicture6_opinion01_shspanner01_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "_shRatio_" + shRatio + "_" + trainAndPredictNum + "_less10_withoutE.txt")));
			//ȥ��ol 
			//BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/group1_8_groupemotion_minpicture6_opinionpercent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "_" + trainAndPredictNum + "_less10_withoutE.txt")));
			
			//key is img id, value is index(1, 2, ...)
			Map<String, String> userId = new HashMap<String, String>();
			//key is index, value is img id
			Map<String, String> idImg = new HashMap<String, String>();
			String line = "";
			while((line = br2.readLine())!= null){
				String p[] = line.split(" ");
				//System.out.println("line "+ line);
				userId.put(p[0], p[1]);
				idImg.put(p[1], p[0]);
			}
			
			System.out.println("userId size : " + userId.size());//35089
			
			//key is user alias, value is 0 or 1, 0 is not opinion leader, 1 is ol.
			Map<String, String> userOpinionleader = new HashMap<String, String>();
			while((line = br5.readLine()) != null){
				String p2[] = line.split(" ");
				userOpinionleader.put(p2[0], p2[1]);
			}
			System.out.println("num of user " + userOpinionleader.size());
			
			//--- ����Ⱥ��pagerank ��������Ⱥ�����
			//key is group id, value is the ol ratio 
			Map<String, String> groupOLratio = new HashMap<String, String>();
			//key is group id, value is 1  or 0;
			Map<String, String> groupType = new HashMap<String, String>();
			while ((line = br6.readLine()) != null) {
				String p3[] = line.split(" ");
				String gid = p3[0];
				String ratio = p3[3];
				groupOLratio.put(gid, ratio);
				if(Float.valueOf(ratio) > (minRatio*1.0/100))
					groupType.put(gid, "1");
				else
					groupType.put(gid, "0");
				
			}
			
			//��ȡÿ��Ⱥ�����ͨ�ȣ�����Ⱥ�����
			Map<String, String> groupConnectType = new HashMap<String, String>();
			while((line = br7.readLine()) != null){
				String p7[] = line.split(" ");
				String gid = p7[0];
				// ֮ǰ����ͨ��
				Float ratio = Float.valueOf(p7[3]);
				if(ratio >= (contactRatio * 1.0 / 100))
					groupConnectType.put(gid, "1");
				else
					groupConnectType.put(gid, "0");
				
				//����ͨ��
				/*
				int edgeC = Integer.valueOf(p7[1]);
				if(edgeC == 0)
					groupConnectType.put(gid, "0");
				else 
					groupConnectType.put(gid, "1");
					*/
			}
			System.out.println("sized of groupConnectType " + groupConnectType.size());
			//��ͨ�� end
			
			//if user is a sh spanner or not   <user, 1 / 0>
			Map<String, String> userSH = new HashMap<String, String>();
			while((line = br8.readLine()) != null){
				String p8[] = line.split(" ");
				userSH.put(p8[0], p8[1]);
			}
			//���� shRatio��Ⱥ��sh���࣬ key is gid , value is 1 or 0
			Map<String, String> groupSHtype = new HashMap<String, String>();
			while((line = br9.readLine()) != null){
				String p9[] = line.split(" ");
				String gid = p9[0];
				Float ratio = Float.valueOf(p9[3]);
				if(ratio >= (shRatio * 1.0) / 100)
					groupSHtype.put(gid, "1");
				else 
					groupSHtype.put(gid, "0");
			}
			//sh end
			//ÿ��ͼƬ�ķ����ߣ�key is img , value is user alias
			Map<String, String> imgUser = new HashMap<String, String>();
			
			//ÿ��Ⱥ���ͼƬ��key��group, value ��img#img#img...
			Map<String, String> groupimgs = new HashMap<String, String>();
			//ÿ��ͼƬ����Ⱥ������������ʱ�䣬key is image id, value :publishdate#group#group2#group3...
			Map<String, String> imgsgroupDate = new HashMap<String, String>();
			while((line = br3.readLine())!= null){//for a group
				String pp[] = line.split(" ");
				String imgs = "";
				for(int i = 0; i < Integer.valueOf(pp[1]); i++){//for a user
					line = br3.readLine();
					String part[] = line.split(" ");
					for(int j = 1; j < part.length; j += 2){
						if(!imgsgroupDate.containsKey(part[j])){
							imgsgroupDate.put(part[j], part[j+1]+"#"+pp[0]);//ÿ��ͼƬ���Ⱥ����ô�죿
						}else{
							String dategs = imgsgroupDate.get(part[j]);
							dategs = dategs + "#" + pp[0];
							imgsgroupDate.put(part[j], dategs);
						}
						imgs = imgs + "#" + part[j];
						
						if(!imgUser.containsKey(part[j]))//ÿ��ͼƬֻ��һ���û�
							imgUser.put(part[j], part[0]);
					}
				}
				groupimgs.put(pp[0], imgs.substring(1, imgs.length()));
			}
			
			System.out.println("num of img with user " + imgUser.size());
			System.out.println("num of groups " + groupimgs.size());
			System.out.println("images of 60356848@N00 " + groupimgs.get("60356848@N00"));
			
			System.out.println("lineDict Size: " + userId.size());
			
			//groupEmotionRatioDate,key is group id, value is date#emotion#ratio list
			Map<String, List<String>> groupDateEmotionRatio = new HashMap<String, List<String>>();
			while((line = br4.readLine()) != null){
				String part4[] = line.split(" ");
				String group = part4[0];
				if(Integer.valueOf(part4[3]) < 3)//�ö�ʱ�䷢����ͼƬ���� ��С������ȥ
					continue;
				if(groupDateEmotionRatio.containsKey(group)){
					List glist = groupDateEmotionRatio.get(group);
					glist.add(part4[1]+"#"+part4[2]+"#"+part4[4]);//date emotion ratio
					groupDateEmotionRatio.put(group, glist);
				}else{
					List<String> gList = new ArrayList<String>();
					gList.add(part4[1]+"#"+part4[2]+"#"+part4[4]);
					groupDateEmotionRatio.put(group, gList);
				}
			}
			
			System.out.println(groupDateEmotionRatio.get("360134@N24"));
			
			int num = 0;
			int w = 0;
			int index = 1;//��1��ͼƬ
			int maxSize = 0;
			while((line = br.readLine()) != null){
				if((line.split(" ")[0].equals("#weight_edge")) || (line.split(" ")[0].equals("#edge"))){
					bw.append(line+"\n");//readline()�Զ���ȥ���з�
					continue;
				}
				//��ÿ��ͼƬ����
				String img = idImg.get(String.valueOf(index));
				index++;
				if(!imgsgroupDate.containsKey(img)){//��ͼƬû��Ⱥ����
					bw.append(line+"\n");
					continue;
				}//line���һ���ַ��ǿո�����������ܶ���ո�
				String groupdate[] = imgsgroupDate.get(img).split("#");//imgsgroupDate��value��date#group#group2..
				int groupNum = groupdate.length - 1;//��ͼƬ���ڵ�Ⱥ����Ŀ�Ƿָ�֮��groupdate�ĳ��ȼ�һ����ȥʱ����ռһ�����ȣ�
				if(groupNum > maxSize)
					maxSize = groupNum;
				if(groupNum > 0)
					line = line + " groupSize:" + groupNum*1.0/151;//������size,��groupSize��һ�� maxSize=151��6000����11500ʱ191
				
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(groupdate[0]);//ͼƬ����ʱ��
				
				int maxEmotion = -1;
				float maxRatio = 0;
				//Ⱥ����࣬�Ȳ���social role, �ȼ���Ⱥ����Ҫ��У�����groupEmotionRatioDate.txt
				for(int i = 0; i < groupNum; i++){// for each group
					String gString = groupdate[i+1];
					if(!groupDateEmotionRatio.containsKey(gString))
						continue;
					//System.out.println("has group");
					
					List<String> dateEmotionRatio = groupDateEmotionRatio.get(gString);
					for(int d = 0; d < dateEmotionRatio.size(); d++){
						String der[] = dateEmotionRatio.get(d).split("#");
						Date groupDate = df.parse(der[0]+"000000");
						float ratio = Float.valueOf(der[2]);
						int emotion = Integer.valueOf(der[1]);
						
						Calendar rightNow = Calendar.getInstance();
				        rightNow.setTime(groupDate);
				        rightNow.add(Calendar.DATE, 14);//���ڼ�14�죬���ܣ���Ϊ֮ǰ��emotion ratio��ʱ��Ƭ�������ܳ�
				        Date rightDate = rightNow.getTime();
				        //�Ϸ�ʱ��
				        if(groupDate.before(publishDate) && rightDate.after(publishDate)){
				        	if(ratio > maxRatio){
				        		maxRatio = ratio;
				        		maxEmotion = emotion;
				        	}
				        }
					}
				}
				if(maxEmotion > -1){
					line = line + " groupEmotion:" + maxEmotion;
					num++;
				}
				
				//����opinion leader
				if(imgUser.containsKey(img) && userOpinionleader.containsKey(imgUser.get(img))){
					line = line + " opinionleader:" + userOpinionleader.get(imgUser.get(img));
				}
				//���� sh spanner
				if(imgUser.containsKey(img) && userSH.containsKey(imgUser.get(img))){
					line = line + " shspanner:" + userSH.get(imgUser.get(img));
				}
				//group Type
				int num0 = 0;//type = 0 �ĸ���
				int num1 = 0;//type = 1 �ĸ���
				for(int i = 0; i < groupNum; i++){// for each group
					String gString = groupdate[i+1];
					if(!groupType.containsKey(gString))
						continue;
					if(groupType.get(gString).equals("1"))
						num1++;
					else {
						num0++;
					}
				}
				if(!(num0 == 0 && num1 == 0)){
					if(num1 >= num0)
						line = line + " groupType:" + "1";
					else
						line = line + " groupType:" + "0";
				}
				//����sh spanner��Ⱥ��ķ���
				int snum0 = 0;//type = 0 �ĸ���
				int snum1 = 0;//type = 1 �ĸ���
				for(int i = 0; i < groupNum; i++){// for each group
					String gString = groupdate[i+1];
					if(!groupSHtype.containsKey(gString))
						continue;
					if(groupSHtype.get(gString).equals("1"))
						snum1++;
					else {
						snum0++;
					}
				}
				if(!(snum0 == 0 && snum1 == 0)){
					if(snum1 >= snum0)
						line = line + " groupSHType:" + "1";
					else
						line = line + " groupSHType:" + "0";
				}
				//���ϸ�����ͨ�ȶ�Ⱥ��ķ���
				int cnum0 = 0;
				int cnum1 = 0;
				for(int i = 0; i < groupNum; i++){
					String gString = groupdate[i+1];
					if(!groupConnectType.containsKey(gString))
						continue;
					if(groupConnectType.get(gString).equals("1"))
						cnum1++;
					else
						cnum0++;
				}
				if(!(cnum0 == 0 && cnum1 == 0)){
					if(cnum1 >= cnum0)
						line = line + " groupConnectType:" + "1";
					else
						line = line + " groupConnectType:" + "0";
				}
				//��ͨ��end
				bw.append(line+"\n");
				
			}
			System.out.println("maxSize " + maxSize);
			System.out.println(num);
			
			br.close();
			br2.close();
			br3.close();
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	//for wenjing bishe
	//�õ�SVM���ݣ�������ֻ�ǰ�FGM���ݸ�ʽ�޸�һ�£����Ըú���ֻ�Ƕ�getFGMdataGroup����ļ��ĸ�ʽ���޸�,�ð�Ĵ����Ѿ���ʱ����Ϊ�����������ּ���sh spanner
	public static void getSVMdata4(int trainAndPredictNum, int percent, int minRatio, int contactRatio){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/group1_8_groupemotion_minpicture6_opinion01_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "_" + trainAndPredictNum + "_less10_withoutE.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/userIdused.txt")));
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getSVMData/wenjing/svm_"+trainAndPredictNum + "_percent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "train.txt")));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("output/getSVMData/wenjing/svm_"+trainAndPredictNum + "_percent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "predict.txt")));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File("output/getSVMData/wenjing/svm_"+trainAndPredictNum + "_percent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "predictImageId.txt")));
			
			//key is index, value is img id
			Map<String, String> idImg = new HashMap<String, String>();
			String line = "";
			while((line = br2.readLine())!= null){
				String p[] = line.split(" ");
				//System.out.println("line "+ line);
				idImg.put(p[1], p[0]);
			}
			
			System.out.println("idImg size : " + idImg.size());//35089 ok
			
			//��¼ÿ��������Ӧ��SVM��ʽ���id
			Map<String, String> featureId = new HashMap<String, String>();
			featureId.put("gender", "26");
			featureId.put("marital", "27");
			featureId.put("occupation", "28");
			featureId.put("friendSize", "29");//f3,num of friends
			featureId.put("friendEmotion", "30");//f4 , emotion of friends, f5 is weight edge, 
			featureId.put("groupSize", "31");
			featureId.put("groupEmotion", "32");
			featureId.put("opinionleader", "33");//social role
			featureId.put("groupType", "34");
			featureId.put("groupConnectType", "35");
			
			int index = 1;
			while ((line = br.readLine()) != null) {
				String output = "";
				String p2[] = line.split("\\s+");//��ֹ����ո�
				if(p2.length < 26)
					continue;
				String label= p2[0];
				if(label.length() != 2)
					continue;
				String labelType = label.substring(0, 1);
				String labelValue = label.substring(1, 2);
				output = labelValue;
				for(int i = 1; i <= 25; i++){
					String feature = p2[i].split(":")[1];
					output = output + " " + i + ":" + feature;
				}
				for(int i = 26; i < p2.length; i++){
					//System.out.println(p2[i]);
					String feaType = p2[i].split(":")[0];
					String feaValue = p2[i].split(":")[1];
					output = output + " " + featureId.get(feaType) + ":" + feaValue;
				}
				if(index == 21579){
					System.out.println("21579 " + output);
				}
				if(labelType.equals("+")){//train
					bw.append(output+"\n");
					bw.flush();
				}
				if(labelType.equals("?")){//predict
					bw2.append(output+"\n");
					bw2.flush();
					bw3.append(idImg.get(String.valueOf(index))+"\n");
					bw3.flush();
				}
				index++;
				if(index % 1000 == 0)
					System.out.println(index);
			}
			
			System.out.println(index);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//Factor analyse
	//��ģ�������ļ�group1_8_groupemotion_minpicture6_opinion01_shspanner01_28_minRatio_18_contactRatio_7_shRatio_10_6000_less10_withoutE.txt���д���
	//6/5, ����ȥ����social role���������ӷ����������ļ�����ԭ���ģ����ԣ�Ҫ�����е�����ļ���ȥ��social role
	public static void factorAnalyse(int srRatio, int minRatio, int contactRatio, int shRatio){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/group1_8_groupemotion_minpicture6_opinion01_shspanner01_" + srRatio + "_minRatio_" + minRatio + "_contactRatio_" + contactRatio + "_shRatio_" + shRatio + "_6000_less10_withoutE.txt")));
			
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f1.txt")));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f2.txt")));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f3.txt")));
			BufferedWriter bw4 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f4.txt")));
			BufferedWriter bw5 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f5.txt")));
			BufferedWriter bw6 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f6.txt")));
			BufferedWriter bw7 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f7-groupsize-emotion.txt")));
			BufferedWriter bw8 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f7-groupsocialrole.txt")));
			BufferedWriter bw9 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f7-groupconnect.txt")));
			BufferedWriter bw10 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole-f7.txt")));
			BufferedWriter bw11 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse/" + srRatio + minRatio + contactRatio + shRatio + "wenjing-socialrole.txt")));
			
			
			String line = "";
			String str1 = "";
			String str2 = "";//ʱ���ԣ���ȥsame user ��
			String str3 = "";
			String str4 = "";
			String str5 = "";
			String str6 = "";
			String str7 = "";
			String str8 = "";
			String str9 = "";
			String str10 = "";
			String str11 = "";
			/*
			 *featureId.put("gender", "26");
			featureId.put("marital", "27");
			featureId.put("occupation", "28");
			featureId.put("friendSize", "29");//f3,num of friends
			featureId.put("friendEmotion", "30");//f4 , emotion of friends, f5 is weight edge, 
			featureId.put("groupSize", "31");
			featureId.put("groupEmotion", "32");
			featureId.put("opinionleader", "33");//social role
			featureId.put("groupType", "34");
			featureId.put("groupConnectType", "35");
			 **/
			String types[] = {"gender", "marital", "occupation", "friendSize", "friendEmotion", "groupSize", "groupEmotion", "groupType", "groupSHType", "groupConnectType"};//"opinionleader", "shspanner",֮ǰ���е�
			while((line = br.readLine()) != null){
				String p[] = line.split(" "); //p[0] is label; p[1-25] is f1; 
				if(p.length < 6){
					bw1.append(line + "\n");
					bw3.append(line + "\n");
					bw4.append(line + "\n");
					bw6.append(line + "\n");
					bw7.append(line + "\n");
					bw8.append(line + "\n");
					bw9.append(line + "\n");
					bw10.append(line + "\n");
					bw11.append(line + "\n");
					if(!(p[0].equals("#edge"))){
						bw2.append(line+"\n");
					}else{
						bw5.append(line+"\n");
					}
					continue;
				}
				String label = p[0];
				int i = 0;
				str1 = label;
				str2 = label;
				str3 = label;
				str4 = label;
				str5 = label;
				str6 = label;
				str7 = label;
				str8 = label;
				str9 = label;
				str10 = label;
				str11 = label;
				for(i = 1; i < 26; i++){
					str2 = str2 + " " + p[i];
					str3 = str3 + " " + p[i];
					str4 = str4 + " " + p[i];
					str5 = str5 + " " + p[i];
					str6 = str6 + " " + p[i];
					str7 = str7 + " " + p[i];
					str8 = str8 + " " + p[i];
					str9 = str9 + " " + p[i];
					str10 = str10 + " " + p[i];
					str11 = str11 + " " + p[i];
					
				}
				Map<String, String> factor = new HashMap<String, String>();//key is attri type, value is attri value
				for(i = 26; i < p.length; i++){
					if(p[i].length() <= 1)
						continue;
					String attri[] = p[i].split(":");
					if(attri.length < 2){
						System.out.println("maybe something error!");
						continue;
					}
					String attriType = attri[0];
					String attriValue = attri[1];
					factor.put(attriType, attriValue);
				}
				//write bw1
				Iterator iter = factor.entrySet().iterator();
				for(i = 0; i < types.length; i++) { 
					String type = types[i];
					if(!factor.containsKey(type))
						continue;
					String value = factor.get(type);
					str1 = str1 + " " + type + ":" + value;
					str2 = str2 + " " + type + ":" + value;//�Ѽ�ȥsame user
					str5 = str5 + " " + type + ":" + value;//�Ѽ�ȥ weight edge
					
					
					if(!(type.equals("gender") || type.equals("marital") || type.equals("occupation")))
						str6 = str6 + " " + type + ":" + value;
					if(!(type.equals("friendSize"))){
						str3 = str3 + " " + type + ":" + value;
					}
					if(!(type.equals("friendEmotion")))
						str4 = str4 + " " + type + ":" + value;
					if(!(type.equals("groupSize") || type.equals("groupEmotion")))
						str7 = str7 + " " + type + ":" + value;
					if(!(type.equals("groupType") || type.equals("groupSHType")))
						str8 = str8 + " " + type + ":" + value;
					if(!(type.equals("groupConnectType")))
						str9 = str9 + " " + type + ":" + value;
					if(!(type.equals("groupSize") || type.equals("groupEmotion") || type.equals("groupType") || type.equals("groupSHType") || type.equals("groupConnectType")))
						str10 = str10 + " " + type + ":"+ value;
					if(!(type.equals("opinionleader") || type.equals("shspanner")))
						str11 = str11 + " " + type + ":"+ value;
				}
				bw1.append(str1 + "\n");
				bw2.append(str2 + "\n");
				bw3.append(str3 + "\n");
				bw4.append(str4 + "\n");
				bw5.append(str5 + "\n");
				bw6.append(str6 + "\n");
				bw7.append(str7 + "\n");
				bw8.append(str8 + "\n");
				bw9.append(str9 + "\n");
				bw10.append(str10 + "\n");
				bw11.append(str11 + "\n");
			}
			br.close();
			bw1.close();
			bw2.close();
			bw3.close();
			bw4.close();
			bw5.close();
			bw6.close();
			bw7.close();
			bw8.close();
			bw9.close();
			bw10.close();
			bw11.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	//for wenjing bishe
	//����FGM����ļ��õ���Ӧ��NB��ʽ���ļ�
	public static void getNDdata(int trainAndPredictNum, int percent, int minRatio, int contactRatio){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/group1_8_groupemotion_minpicture6_opinion01_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "_" + trainAndPredictNum + "_less10_withoutE.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/userIdused.txt")));
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getNaiveBayesData/wenjing/NB_"+trainAndPredictNum + "_percent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "train.txt")));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("output/getNaiveBayesData/wenjing/NB_"+trainAndPredictNum + "_percent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "predict.txt")));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File("output/getNaiveBayesData/wenjing/NB_"+trainAndPredictNum + "_percent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "predictImageId.txt")));
			
			//key is index, value is img id
			Map<String, String> idImg = new HashMap<String, String>();
			String line = "";
			while((line = br2.readLine())!= null){
				String p[] = line.split(" ");
				//System.out.println("line "+ line);
				idImg.put(p[1], p[0]);
			}
			
			System.out.println("idImg size : " + idImg.size());//35089 ok
			
			int index = 1;
			while ((line = br.readLine()) != null) {
				String output = "";
				String p2[] = line.split("\\s+");//��ֹ����ո�
				if(p2.length < 26)
					continue;
				String label= p2[0];
				if(label.length() != 2)
					continue;
				String labelType = label.substring(0, 1);
				String labelValue = label.substring(1, 2);
				output = labelValue;
				for(int i = 1; i <= 25; i++){
					String feature = p2[i].split(":")[1];
					output = output + " " + feature;
				}
				
				//ÿ�������Ľ����-1��Ĭ��ֵ
				Map<String, String> fea = new HashMap<String, String>();
				fea.put("gender", "-1");
				fea.put("marital", "-1");
				fea.put("occupation", "-1");
				fea.put("friendSize", "-1");
				fea.put("friendEmotion", "-1");
				fea.put("groupSize", "-1");
				fea.put("groupEmotion", "-1");
				fea.put("opinionleader", "-1");
				fea.put("groupType", "-1");
				fea.put("groupConnectType", "-1");
				for(int i = 26; i < p2.length; i++){
					//System.out.println(p2[i]);
					String feaType = p2[i].split(":")[0];
					String feaValue = p2[i].split(":")[1];
					fea.put(feaType, feaValue);
				}
				output = output + " " + fea.get("gender") + " " + fea.get("marital") + " " + fea.get("occupation") + " " + 
						fea.get("friendSize") + " " + fea.get("friendEmotion") + " " + 
						fea.get("groupSize") + " " + fea.get("groupEmotion") + " " + 
						fea.get("opinionleader") + " " + fea.get("groupType") + " " + fea.get("groupConnectType");
				if(index == 21579){
					System.out.println("21579 " + output);
				}
				if(labelType.equals("+")){//train
					bw.append(output+"\n");
					bw.flush();
				}
				if(labelType.equals("?")){//predict
					bw2.append(output+"\n");
					bw2.flush();
					bw3.append(idImg.get(String.valueOf(index))+"\n");
					bw3.flush();
				}
				index++;
				if(index % 1000 == 0)
					System.out.println(index);
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	//for wenjing bishe
	//�ı���6 ,�ð汾��Ч����getBasicFGMData4Ҫ�ã��������Ժ�ʹ�Ϊ����
	public static void getBasicFGMData8(int trainAndPredictNum) {
		try {
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			DBCollection feaCollection = db.getCollection("imgfeature");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date2.txt")));//baseline, no group
			BufferedReader br2 = new BufferedReader(new FileReader(new File("user_profile_bishe_2605.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/getUserTemporalEmotionType/temporalEmotion_oneDay_lowerLonger2withoutPuncNotSelf_All.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/getContact2/contactNotSelfWithTime.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/20170606_less6_time5/baseline_8_" + trainAndPredictNum + "_less6_withoutE.txt")));//֮ǰ��less 10, ��Ϊ���� sameUserImageNum < 10
			BufferedWriter bww = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/20170606_less6_time5/userIdused.txt")));//��¼�ú�����ѡȡ���û����Ա�֮�����group id����ʱ��֪����Щ�û���ʹ��
			
			
			// ��������Ȧ�����Ϣ��keyΪ[imageId]��valueΪ[�û����������ǰһ������Ȧ������������]
			String line = ""; 
			Map <String, Integer> imageFEs = new HashMap<String, Integer>();
			while ((line = br3.readLine()) != null) {
				// ��ʽΪ[����ͼƬid ͼƬ��� ͼƬ������ ͼƬ����ʱ�� [���۵�ͼƬ�ķ�����:���۵�ͼƬ���:������У�����Ϊ-1��]]��һ��Ϊһ��ͼƬ
				String []part = line.split(" ");
				String imageId = part[0];
				int maxCommentFre = 0;
				int maxCommentFreEmotionType = -1;
				int commentFre[] = {0, 0, 0, 0, 0, 0};
				for (int i = 4; i < part.length; i ++) {
					// �������
					//commentFre[Integer.parseInt(part[i].substring(part[i].length() - 1))] ++;
					// ͼƬ���
					commentFre[Integer.parseInt(part[i].substring(part[i].indexOf(":") + 1, part[i].indexOf(":") + 2))] ++;
				}
				for (int i = 0; i < 6; i ++) {
					if (commentFre[i] > maxCommentFre) {
						maxCommentFre = commentFre[i];
						maxCommentFreEmotionType = i;
					}
				}
				imageFEs.put(imageId, maxCommentFreEmotionType);
			}
			System.out.println("imageFEs.size(): " + imageFEs.size());
			
			// ����ͼƬ��Ϣ��keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
			Map <String, String> imageIds = new HashMap<String, String>();
			// ͳ���û�����ͼƬ�����keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
			Map<String, List<String>> userImages = new HashMap<String, List<String>>();
			int fre[] = {0, 0, 0, 0, 0, 0};
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				Integer emotionType = Integer.parseInt(part[1]);
				String userAlias = part[2];
				String dateString = part[3];
				
				if (fre[emotionType] >= trainAndPredictNum) {//br���ļ�����������ЧͼƬ������޶�û��ͼƬ��������ô�����ﵽ������ʱ���Ͳ��ٻ�ȡ������ͼƬ���ļ�������
					continue;
				}
				
				int maxFE = -1;
				if (imageFEs.containsKey(imageId)) {
					maxFE = imageFEs.get(imageId);
				}
				imageIds.put(imageId, userAlias + "#" + maxFE + "#" + emotionType + "#" + dateString);
				if (userImages.containsKey(userAlias)) {
					List <String> images = userImages.get(userAlias);
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				} else {
					List <String> images = new ArrayList<String>();
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				}
				
				fre[emotionType] ++;
			}
			System.out.println("imageIds.size(): " + imageIds.size());
			System.out.println("userImages.size(): " + userImages.size());
			
			// �����û���Ϣ��keyΪ[userAlias]��valueΪ[��#�ָ������û���Ϣ]
			Map <String, String> userProfiles = new HashMap<String, String>();
			while ((line = br2.readLine()) != null) {
				String userAlias = line;
				int attrNum = Integer.parseInt(br2.readLine());
				String attrString = "";
				for (int i = 0; i < attrNum; i ++) {
					line = br2.readLine();
					attrString += line + "#";
				}
				userProfiles.put(userAlias, attrString);
			}
			System.out.println("userProfiles.size(): " + userProfiles.size());
			
			// �����û����������keyΪ[userAlias]��valueΪ[�û����۹�������ͼƬ������#����ʱ��]
			Map<String, List<String>> contact = new HashMap<String, List<String>>();
			while ((line = br4.readLine()) != null) {
				String part[] = line.split(" ");
				String imageOwnerAlias = part[0];
				for (int i = 1; i < part.length; i ++) {
					String littlePart[] = part[i].split(":");
					String commentMakerAlias = littlePart[0];
					String commentDateString = littlePart[1];
					if (contact.containsKey(commentMakerAlias)) {
						List <String> contactList = contact.get(commentMakerAlias);
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					} else {
						List <String> contactList = new ArrayList <String>();
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					}
				}
			}
			System.out.println("contactSize: " + contact.size());
			
			// ������feature��Ϣ��ͼƬ����
			// totalNum = 215349; limitNum = 129209;
			/*int totalNum = 0;
			Iterator iter = imageIds.entrySet().iterator();
			while (iter.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter.next();
				String imageId = (String) entry.getKey();
				DBObject feaQuery = new BasicDBObject("_id", imageId);
				DBCursor feaCursor = feaCollection.find(feaQuery).limit(1);
				while (feaCursor.hasNext()) {
					DBObject feaDbo = feaCursor.next();
					totalNum ++;
				}
			}
			System.out.println("totalNum: " + totalNum);
			// ����ѵ��������
			int limitNum = (int) (totalNum * 0.6);
			System.out.println("limitNum: " + limitNum);*/
			
			// ȫ��
			//int totalNum = 215349;
			//int limitNum = 129209;
			
			// ÿ��trainAndPredictNum��
			int totalNum = trainAndPredictNum * 6;
			int limitNum = (int) (totalNum * 0.6);
			
			// ��ÿһ��ͼƬ
			int lineNum = 1;
			int num = 0;
			int maxFriendSize = 0;
			Map<String, String> lineDict = new HashMap<String, String>();
			Iterator iter3 = imageIds.entrySet().iterator();
			while (iter3.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter3.next();
				String imageId = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String friendEmotion = valuePart[1];
				String emotionType = valuePart[2];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				DBObject query = new BasicDBObject("_id", imageId);
				DBObject field = new BasicDBObject();
				DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
				while (feaCursor.hasNext()) {
					String output = "";
					// ѵ�������Լ�
					if (num < limitNum) {
						output = "+";
					} else {
						output = "?";
					}
					DBObject feaDbo = feaCursor.next();
					// �������visual feature
					output += emotionType
							+ " saturation:" + feaDbo.get("saturation") 
							+ " sat_con:" + feaDbo.get("sat_con")
							+ " bright:" + feaDbo.get("bright") 
							+ " bright_con:" + feaDbo.get("bright_con") 
							+ " dull_color_ratio:" + feaDbo.get("dull_color_ratio")
							+ " cool_color_ratio:"	+ feaDbo.get("cool_color_ratio")
							+ " fg_color_df:" + feaDbo.get("fg_color_dif") 
							+ " fg_area_dif:"	+ feaDbo.get("fg_area_dif") 
							+ " fg_texture_src:" + feaDbo.get("fg_texture_src") 
							+ " fg_texture_sal:" + feaDbo.get("fg_texture_sal")
							+ " c1_h:" + feaDbo.get("c1_h") + " c1_s:" + feaDbo.get("c1_s") + " c1_v:" + feaDbo.get("c1_v") 
							+ " c2_h:" + feaDbo.get("c2_h") + " c2_s:" + feaDbo.get("c2_s")	+ " c2_v:" + feaDbo.get("c2_v") 
							+ " c3_h:" + feaDbo.get("c3_h") + " c3_s:" + feaDbo.get("c3_s")	+ " c3_v:" + feaDbo.get("c3_v") 
							+ " c4_h:" + feaDbo.get("c4_h") + " c4_s:" + feaDbo.get("c4_s")	+ " c4_v:" + feaDbo.get("c4_v") 
							+ " c5_h:" + feaDbo.get("c5_h") + " c5_s:" + feaDbo.get("c5_s")	+ " c5_v:" + feaDbo.get("c5_v");
					// ȥ�������ÿ�ѧ��������ʾ��ͼƬ
					if (output.contains("E")) {
						continue;
					}
					// �û�������Ϣ��correlationThree
					// ��0��1�Լ���1��2����ǲ�һ���ģ���1��2������������ά�����������࣬����һ��Ĭ��Ϊ0������0��1������ã�������ά�����϶����ࣩ
					if (userProfiles.containsKey(userAlias)) {
						int gender = -1; // 0: female 1: male
						int marital = -1; // 0: single 1: taken
						int occupation = -1; // 0: engineer 1: artist
						String attrString = userProfiles.get(userAlias);
				    	String attrs[] = attrString.split("#");				
				    	for (int j = 0; j < attrs.length; j ++) {
				    		String part[] = attrs[j].split(":");
				    		if (part[0].equals("I am")) {
				    			if (part[1].contains("Female")) {
				    				gender = 0;
				    			} else if (part[1].contains("Male")) {
				    				gender = 1;
				    			}
				    			if (part[1].contains("Single")) {
				    				marital = 0;
				    			} else if (part[1].contains("Taken")) {
				    				marital = 1;
				    			}
				    		} else if (part[0].equals("Occupation")) {
				    			if (part[1].contains("software") || part[1].contains("IT") || part[1].contains("computer") ||
				    					part[1].contains("code") || part[1].contains("web") || part[1].contains("network") ||
				    					part[1].contains("engineer") || part[1].contains("tech") || part[1].contains("mechanical") ||
				    					part[1].contains("electronic") || part[1].contains("medical") || part[1].contains("market") ||
					    				part[1].contains("product") || part[1].contains("consultant") || part[1].contains("science")) {
					    				occupation = 0;
					    			} else if (part[1].contains("artist") || part[1].contains("writer") || part[1].contains("musician") ||
					    					part[1].contains("dancer") || part[1].contains("photographer") || part[1].contains("film") ||
					    					part[1].contains("designer") || part[1].contains("blog") || part[1].contains("editor") ||
					    					part[1].contains("freelancer")) {
						    			occupation = 1;
						    		}
				    		}
				    	}
				    	if (gender > -1) {
				    		output += " gender:" + gender;
				    	}
				    	if (marital > -1) {
				    		output += " marital:" + marital;
				    	}
				    	if (occupation > -1) {
				    		output += " occupation:" + occupation;
				    	}
					}
					// �û�����Ȧ�Ĵ�С
					if (contact.containsKey(userAlias)) {
						List <String> contactList = contact.get(userAlias);
						List <String> friendList = new ArrayList<String>();
						for (int i = 0; i < contactList.size(); i ++) {
							String contactInfo = contactList.get(i);
							String part[] = contactInfo.split("#");
							String friendAlias = part[0];
							String contactDateString = part[1] + "000000";
							Date contactDate = df.parse(contactDateString);
							if (!friendList.contains(friendAlias) && !contactDate.after(publishDate)) {
								friendList.add(friendAlias);
							}
						}
						// 313ΪfriendList.size()�����ֵ
						//207�����ֵ wenjing11600, 158 max 6000
						if (friendList.size() > 0) {
							output += " friendSize:" + friendList.size() * 1.0 / 158;
						}
						if (friendList.size() > maxFriendSize) {
							maxFriendSize = friendList.size();
						}
					}
					// ����Ȧ��У�correlationTwo
					if (Integer.parseInt(friendEmotion) > -1) {
						output += " friendEmotion:" + friendEmotion;
					}
					//output += " " + imageId;
					output += " \n";
					bw.append(output);
					bw.flush();
					lineDict.put(imageId, String.valueOf(lineNum));//imageId��ͼƬindx��Ӧ��ϵ��ͼƬindex�Ǹ������о�����ͼƬ�ı��0-max
					bww.append(imageId + " " + lineNum + "\n");//wenjing add
					lineNum ++;
					num ++;
					if (num % 1000 == 0) {
						System.out.println(num);
					}
				}
			}
			System.out.println("num: " + num);
			System.out.println("maxFriendSize: " + maxFriendSize);
			
			// edge temporal correlation����ĳ��ʱ���ڣ��������ͼƬ��ͬһλ�û���������������ͼƬ�����һ����
			// ��ÿһ��ͼƬ��Ϣ���õ���ӦͼƬid
			Iterator iter5 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());//shuchu 35089
			while (iter5.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter5.next();
			    String lineId = entry.getValue().toString();
			    String imageId = entry.getKey().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -5);//���ڼ�10����  20170606  ��5����
		        Date leftDate = rightNow.getTime();
		        
		        Calendar rightNow2 = Calendar.getInstance();
		        rightNow2.setTime(publishDate);
		        rightNow2.add(Calendar.MINUTE, 10);//���ڼ�10����
		        Date rightDate = rightNow2.getTime();
		        
			    Date closestDate = leftDate;
		        String closestUserImageLineId = "-1";
		        // ���Ҹ��û�������ͼƬ
		        // keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
				List <String> images = userImages.get(userAlias);
				int sameUserImageNum = 0;
				for (int i = 0; i < images.size(); i ++) {
					String imageIdAndDateString = images.get(i);
					String imageIdAndDatePart[] = imageIdAndDateString.split("#");
					String userImageId = imageIdAndDatePart[0];
					String userImageDateString = imageIdAndDatePart[1];
					Date userImageDate = df.parse(userImageDateString);
					// �����ͬһ��ͼƬ������һ��
					if (imageId.equals(userImageId)) {
						continue;
					}
					// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
		        	if (!lineDict.containsKey(userImageId)) {
		        		continue;
		        	}
			        // ����ںϷ���ʱ��������
					if (userImageDate.after(leftDate) && userImageDate.before(rightDate)) {
						sameUserImageNum ++;
						if (userImageDate.after(closestDate) && userImageDate.before(publishDate)) {
							//System.out.println(df.format(userImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
							String userImageLineId = lineDict.get(userImageId);
		        			closestDate = userImageDate;
		        			closestUserImageLineId = userImageLineId;
						}
					}
				}
				if (!closestUserImageLineId.equals("-1") && sameUserImageNum < 6) {  //wenjing 2017-6-6
					String output = "#edge " + lineId + " " + closestUserImageLineId + " sameuser\n";//ǰ�߱Ⱥ��߷���ʱ����
		        	bw.append(output);
		        	bw.flush();
				}
			}
			
			// edge correlationOne���õ�ͼƬ֮���Ӱ��������
			// <5 <10 ..... <50 >50
			int contactStatics[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			Iterator iter6 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter6.hasNext()) { 
			    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter6.next();
			    String imageId = entry.getKey().toString();
			    String lineId = entry.getValue().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				Date publishDate = df.parse(dateString);
				
				// �õ��Ϸ���ʱ������
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -5);//���ڼ�10����  20170606 ��5����
		        Date leftDate = rightNow.getTime();
		        
				// ʹ��ͼƬ�����û����Ҹ��û����۹��������û�����һ�����Բ鵽
				if (contact.containsKey(userAlias)) {
					List<String> contactList = contact.get(userAlias);
					// ��ÿһλ�н��������ѣ����㽻����ǿ��
					Map <String, Integer> contactFre = new HashMap<String, Integer>();
					for(int i = 0; i < contactList.size(); i ++) {
						String contactContent = contactList.get(i);
						String part[] = contactContent.split("#");
						String friendAlias = part[0];
						String contactDateString = part[1] + "000000";
						Date contactDate = df.parse(contactDateString);
						// �������ʱ����ͼƬ����ʱ��ǰ����¼һ�ν���
						if (!contactDate.after(publishDate)) {
							if (contactFre.containsKey(friendAlias)) {
								int contactNum = contactFre.get(friendAlias);
								contactNum ++;
								contactFre.put(friendAlias, contactNum);
							} else {
								int contactNum = 1;
								contactFre.put(friendAlias, contactNum);
							}
						}
					}
					
					
					/*Iterator iter8 = contactFre.entrySet().iterator();
					while (iter8.hasNext()) {
						Map.Entry<String, Integer> entry8 = (Map.Entry<String, Integer>) iter8.next();
						int contactNum = entry8.getValue();
						if (contactNum >= 50) {
							contactStatics[10] ++;
						} else {
							contactStatics[contactNum / 5] ++;
						}
					}
					for (int i = 0; i < 10; i ++) {
						System.out.print(contactStatics[i] + " ");
					}
					System.out.println();*/
					
					
					Iterator iter7 = contactFre.entrySet().iterator();
					while (iter7.hasNext()) {
						Map.Entry<String, Integer> entry7 = (Map.Entry<String, Integer>) iter7.next();
						String friendAlias = entry7.getKey();
						int contactNum = entry7.getValue();
						int contactLevel = -1;
						if (contactNum < 10) {
							contactLevel = 1;
						} else if (contactNum < 20) {
							contactLevel = 2;
						} else {
							contactLevel = 3;
						}
						// ������λ���ѷ���������ͼƬ
						// keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
						if (userImages.containsKey(friendAlias)) {
							// �����ܱ�֤�û�һ��������λ���ѵ�ͼƬ���������ۣ�
							List <String> friendImages = userImages.get(friendAlias);
							for (int i = 0; i < friendImages.size(); i ++) {
								String friendImageIdAndDate = friendImages.get(i);
								String friendImageIdAndDatePart[] = friendImageIdAndDate.split("#");
								String friendImageId = friendImageIdAndDatePart[0];
								String friendImageDateString = friendImageIdAndDatePart[1];
								Date friendImageDate = df.parse(friendImageDateString);
								
						        // ����ںϷ���ʱ��������
						        if (friendImageDate.after(leftDate) && friendImageDate.before(publishDate)) {
						        	//System.out.println(df.format(friendImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
						        	// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
						        	if (lineDict.containsKey(friendImageId)) {
							        	String friendLineId = lineDict.get(friendImageId);
							        	String output = "#weight_edge " + lineId + " " + friendLineId + " friendimpact " + contactLevel + "\n";
							        	bw.append(output);
							        	bw.flush();
						        	}
						        }
							}
						}
					}
				}
		    }
			
			br.close();
			br2.close();
			br3.close();
			br4.close();
			bw.close();
			bww.close();
			db.cleanCursors(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//shuchu:num: 66967  maxFriendSize: 207 lineDict Size: 66967  lineDict Size: 66967
	
	
	// for aaai wusen
	// ����࣬�õ�����һ������BasicFGM��ʽ��data�������û���Ϣ����������Ȧ��������ѵ���У������û��佻��ǿ�ȣ�ÿ������ΪtrainAndPredictNum����һ��ÿ�Ŷ���feature�����ÿ���������ܱ����������һЩ��
	public static void getBasicFGMData6(int trainAndPredictNum) {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			DBCollection feaCollection = db.getCollection("imgfeature");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("user_profile_bishe_2605.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/getUserTemporalEmotionType/temporalEmotion_oneDay_lowerLonger2withoutPuncNotSelf_All.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/getContact2/contactNotSelfWithTime.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/aaai15_visual_su10m10m_" + trainAndPredictNum + "_less10_withoutE.txt")));
			
			// ��������Ȧ�����Ϣ��keyΪ[imageId]��valueΪ[�û����������ǰһ������Ȧ������������]
			String line = ""; 
			Map <String, Integer> imageFEs = new HashMap<String, Integer>();
			while ((line = br3.readLine()) != null) {
				// ��ʽΪ[����ͼƬid ͼƬ��� ͼƬ������ ͼƬ����ʱ�� [���۵�ͼƬ�ķ�����:���۵�ͼƬ���:������У�����Ϊ-1��]]��һ��Ϊһ��ͼƬ
				String []part = line.split(" ");
				String imageId = part[0];
				int maxCommentFre = 0;
				int maxCommentFreEmotionType = -1;
				int commentFre[] = {0, 0, 0, 0, 0, 0};
				for (int i = 4; i < part.length; i ++) {
					// �������
					//commentFre[Integer.parseInt(part[i].substring(part[i].length() - 1))] ++;
					// ͼƬ���
					commentFre[Integer.parseInt(part[i].substring(part[i].indexOf(":") + 1, part[i].indexOf(":") + 2))] ++;
				}
				for (int i = 0; i < 6; i ++) {
					if (commentFre[i] > maxCommentFre) {
						maxCommentFre = commentFre[i];
						maxCommentFreEmotionType = i;
					}
				}
				imageFEs.put(imageId, maxCommentFreEmotionType);
			}
			System.out.println("imageFEs.size(): " + imageFEs.size());
			
			// ����ͼƬ��Ϣ��keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
			Map <String, String> imageIds = new HashMap<String, String>();
			// ͳ���û�����ͼƬ�����keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
			Map<String, List<String>> userImages = new HashMap<String, List<String>>();
			int fre[] = {0, 0, 0, 0, 0, 0};
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				Integer emotionType = Integer.parseInt(part[1]);
				String userAlias = part[2];
				String dateString = part[3];
				
				if (fre[emotionType] >= trainAndPredictNum) {//br���ļ�����������ЧͼƬ������޶�û��ͼƬ��������ô�����ﵽ������ʱ���Ͳ��ٻ�ȡ������ͼƬ���ļ�������
					continue;
				}
				
				int maxFE = -1;
				if (imageFEs.containsKey(imageId)) {
					maxFE = imageFEs.get(imageId);
				}
				imageIds.put(imageId, userAlias + "#" + maxFE + "#" + emotionType + "#" + dateString);
				if (userImages.containsKey(userAlias)) {
					List <String> images = userImages.get(userAlias);
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				} else {
					List <String> images = new ArrayList<String>();
					images.add(imageId + "#" + dateString);
					userImages.put(userAlias, images);
				}
				
				fre[emotionType] ++;
			}
			System.out.println("imageIds.size(): " + imageIds.size());
			System.out.println("userImages.size(): " + userImages.size());
			
			// �����û���Ϣ��keyΪ[userAlias]��valueΪ[��#�ָ������û���Ϣ]
			Map <String, String> userProfiles = new HashMap<String, String>();
			while ((line = br2.readLine()) != null) {
				String userAlias = line;
				int attrNum = Integer.parseInt(br2.readLine());
				String attrString = "";
				for (int i = 0; i < attrNum; i ++) {
					line = br2.readLine();
					attrString += line + "#";
				}
				userProfiles.put(userAlias, attrString);
			}
			System.out.println("userProfiles.size(): " + userProfiles.size());
			
			// �����û����������keyΪ[userAlias]��valueΪ[�û����۹�������ͼƬ������#����ʱ��]
			Map<String, List<String>> contact = new HashMap<String, List<String>>();
			while ((line = br4.readLine()) != null) {
				String part[] = line.split(" ");
				String imageOwnerAlias = part[0];
				for (int i = 1; i < part.length; i ++) {
					String littlePart[] = part[i].split(":");
					String commentMakerAlias = littlePart[0];
					String commentDateString = littlePart[1];
					if (contact.containsKey(commentMakerAlias)) {
						List <String> contactList = contact.get(commentMakerAlias);
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					} else {
						List <String> contactList = new ArrayList <String>();
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					}
				}
			}
			System.out.println("contactSize: " + contact.size());
			
			// ������feature��Ϣ��ͼƬ����
			// totalNum = 215349; limitNum = 129209;
			/*int totalNum = 0;
			Iterator iter = imageIds.entrySet().iterator();
			while (iter.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter.next();
				String imageId = (String) entry.getKey();
				DBObject feaQuery = new BasicDBObject("_id", imageId);
				DBCursor feaCursor = feaCollection.find(feaQuery).limit(1);
				while (feaCursor.hasNext()) {
					DBObject feaDbo = feaCursor.next();
					totalNum ++;
				}
			}
			System.out.println("totalNum: " + totalNum);
			// ����ѵ��������
			int limitNum = (int) (totalNum * 0.6);
			System.out.println("limitNum: " + limitNum);*/
			
			// ȫ��
			//int totalNum = 215349;
			//int limitNum = 129209;
			
			// ÿ��trainAndPredictNum��
			int totalNum = trainAndPredictNum * 6;
			int limitNum = (int) (totalNum * 0.6);
			
			// ��ÿһ��ͼƬ
			int lineNum = 1;
			int num = 0;
			int maxFriendSize = 0;
			Map<String, String> lineDict = new HashMap<String, String>();
			Iterator iter3 = imageIds.entrySet().iterator();
			while (iter3.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter3.next();
				String imageId = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String friendEmotion = valuePart[1];
				String emotionType = valuePart[2];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				DBObject query = new BasicDBObject("_id", imageId);
				DBObject field = new BasicDBObject();
				DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
				while (feaCursor.hasNext()) {
					String output = "";
					// ѵ�������Լ�
					if (num < limitNum) {
						output = "+";
					} else {
						output = "?";
					}
					DBObject feaDbo = feaCursor.next();
					// �������visual feature
					output += emotionType
							+ " saturation:" + feaDbo.get("saturation") 
							+ " sat_con:" + feaDbo.get("sat_con")
							+ " bright:" + feaDbo.get("bright") 
							+ " bright_con:" + feaDbo.get("bright_con") 
							+ " dull_color_ratio:" + feaDbo.get("dull_color_ratio")
							+ " cool_color_ratio:"	+ feaDbo.get("cool_color_ratio")
							+ " fg_color_df:" + feaDbo.get("fg_color_dif") 
							+ " fg_area_dif:"	+ feaDbo.get("fg_area_dif") 
							+ " fg_texture_src:" + feaDbo.get("fg_texture_src") 
							+ " fg_texture_sal:" + feaDbo.get("fg_texture_sal")
							+ " c1_h:" + feaDbo.get("c1_h") + " c1_s:" + feaDbo.get("c1_s") + " c1_v:" + feaDbo.get("c1_v") 
							+ " c2_h:" + feaDbo.get("c2_h") + " c2_s:" + feaDbo.get("c2_s")	+ " c2_v:" + feaDbo.get("c2_v") 
							+ " c3_h:" + feaDbo.get("c3_h") + " c3_s:" + feaDbo.get("c3_s")	+ " c3_v:" + feaDbo.get("c3_v") 
							+ " c4_h:" + feaDbo.get("c4_h") + " c4_s:" + feaDbo.get("c4_s")	+ " c4_v:" + feaDbo.get("c4_v") 
							+ " c5_h:" + feaDbo.get("c5_h") + " c5_s:" + feaDbo.get("c5_s")	+ " c5_v:" + feaDbo.get("c5_v");
					// ȥ�������ÿ�ѧ��������ʾ��ͼƬ
					if (output.contains("E")) {
						continue;
					}
					// �û�������Ϣ��correlationThree
					// ��0��1�Լ���1��2����ǲ�һ���ģ���1��2������������ά�����������࣬����һ��Ĭ��Ϊ0������0��1������ã�������ά�����϶����ࣩ
					if (userProfiles.containsKey(userAlias)) {
						int gender = -1; // 0: female 1: male
						int marital = -1; // 0: single 1: taken
						int occupation = -1; // 0: engineer 1: artist
						String attrString = userProfiles.get(userAlias);
				    	String attrs[] = attrString.split("#");				
				    	for (int j = 0; j < attrs.length; j ++) {
				    		String part[] = attrs[j].split(":");
				    		if (part[0].equals("I am")) {
				    			if (part[1].contains("Female")) {
				    				gender = 0;
				    			} else if (part[1].contains("Male")) {
				    				gender = 1;
				    			}
				    			if (part[1].contains("Single")) {
				    				marital = 0;
				    			} else if (part[1].contains("Taken")) {
				    				marital = 1;
				    			}
				    		} else if (part[0].equals("Occupation")) {
				    			if (part[1].contains("software") || part[1].contains("IT") || part[1].contains("computer") ||
				    					part[1].contains("code") || part[1].contains("web") || part[1].contains("network") ||
				    					part[1].contains("engineer") || part[1].contains("tech") || part[1].contains("mechanical") ||
				    					part[1].contains("electronic") || part[1].contains("medical") || part[1].contains("market") ||
					    				part[1].contains("product") || part[1].contains("consultant") || part[1].contains("science")) {
					    				occupation = 0;
					    			} else if (part[1].contains("artist") || part[1].contains("writer") || part[1].contains("musician") ||
					    					part[1].contains("dancer") || part[1].contains("photographer") || part[1].contains("film") ||
					    					part[1].contains("designer") || part[1].contains("blog") || part[1].contains("editor") ||
					    					part[1].contains("freelancer")) {
						    			occupation = 1;
						    		}
				    		}
				    	}
				    	if (gender > -1) {
				    		output += " gender:" + gender;
				    	}
				    	if (marital > -1) {
				    		output += " marital:" + marital;
				    	}
				    	if (occupation > -1) {
				    		output += " occupation:" + occupation;
				    	}
					}
					// �û�����Ȧ�Ĵ�С
					if (contact.containsKey(userAlias)) {
						List <String> contactList = contact.get(userAlias);
						List <String> friendList = new ArrayList<String>();
						for (int i = 0; i < contactList.size(); i ++) {
							String contactInfo = contactList.get(i);
							String part[] = contactInfo.split("#");
							String friendAlias = part[0];
							String contactDateString = part[1] + "000000";
							Date contactDate = df.parse(contactDateString);
							if (!friendList.contains(friendAlias) && !contactDate.after(publishDate)) {
								friendList.add(friendAlias);
							}
						}
						// 313ΪfriendList.size()�����ֵ
						if (friendList.size() > 0) {
							output += " friendSize:" + friendList.size() * 1.0 / 313;
						}
						if (friendList.size() > maxFriendSize) {
							maxFriendSize = friendList.size();
						}
					}
					// ����Ȧ��У�correlationTwo
					if (Integer.parseInt(friendEmotion) > -1) {
						output += " friendEmotion:" + friendEmotion;
					}
					//output += " " + imageId;
					output += " \n";
					bw.append(output);
					bw.flush();
					lineDict.put(imageId, String.valueOf(lineNum));//imageId��ͼƬindx��Ӧ��ϵ��ͼƬindex�Ǹ������о�����ͼƬ�ı��0-max
					lineNum ++;
					num ++;
					if (num % 1000 == 0) {
						System.out.println(num);
					}
				}
			}
			System.out.println("num: " + num);
			System.out.println("maxFriendSize: " + maxFriendSize);
			
			// edge temporal correlation����ĳ��ʱ���ڣ��������ͼƬ��ͬһλ�û���������������ͼƬ�����һ����
			// ��ÿһ��ͼƬ��Ϣ���õ���ӦͼƬid
			Iterator iter5 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter5.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter5.next();
			    String lineId = entry.getValue().toString();
			    String imageId = entry.getKey().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
		        Calendar rightNow2 = Calendar.getInstance();
		        rightNow2.setTime(publishDate);
		        rightNow2.add(Calendar.MINUTE, 10);//���ڼ�10����
		        Date rightDate = rightNow2.getTime();
		        
			    Date closestDate = leftDate;
		        String closestUserImageLineId = "-1";
		        // ���Ҹ��û�������ͼƬ
		        // keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
				List <String> images = userImages.get(userAlias);
				int sameUserImageNum = 0;
				for (int i = 0; i < images.size(); i ++) {
					String imageIdAndDateString = images.get(i);
					String imageIdAndDatePart[] = imageIdAndDateString.split("#");
					String userImageId = imageIdAndDatePart[0];
					String userImageDateString = imageIdAndDatePart[1];
					Date userImageDate = df.parse(userImageDateString);
					// �����ͬһ��ͼƬ������һ��
					if (imageId.equals(userImageId)) {
						continue;
					}
					// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
		        	if (!lineDict.containsKey(userImageId)) {
		        		continue;
		        	}
			        // ����ںϷ���ʱ��������
					if (userImageDate.after(leftDate) && userImageDate.before(rightDate)) {
						sameUserImageNum ++;
						if (userImageDate.after(closestDate) && userImageDate.before(publishDate)) {
							//System.out.println(df.format(userImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
							String userImageLineId = lineDict.get(userImageId);
		        			closestDate = userImageDate;
		        			closestUserImageLineId = userImageLineId;
						}
					}
				}
				if (!closestUserImageLineId.equals("-1") && sameUserImageNum < 10) {
					String output = "#edge " + lineId + " " + closestUserImageLineId + " sameuser\n";
		        	bw.append(output);
		        	bw.flush();
				}
			}
			
			// edge correlationOne���õ�ͼƬ֮���Ӱ��������
			// <5 <10 ..... <50 >50
			int contactStatics[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			Iterator iter6 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter6.hasNext()) { 
			    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter6.next();
			    String imageId = entry.getKey().toString();
			    String lineId = entry.getValue().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				Date publishDate = df.parse(dateString);
				
				// �õ��Ϸ���ʱ������
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
				// ʹ��ͼƬ�����û����Ҹ��û����۹��������û�����һ�����Բ鵽
				if (contact.containsKey(userAlias)) {
					List<String> contactList = contact.get(userAlias);
					// ��ÿһλ�н��������ѣ����㽻����ǿ��
					Map <String, Integer> contactFre = new HashMap<String, Integer>();
					for(int i = 0; i < contactList.size(); i ++) {
						String contactContent = contactList.get(i);
						String part[] = contactContent.split("#");
						String friendAlias = part[0];
						String contactDateString = part[1] + "000000";
						Date contactDate = df.parse(contactDateString);
						// �������ʱ����ͼƬ����ʱ��ǰ����¼һ�ν���
						if (!contactDate.after(publishDate)) {
							if (contactFre.containsKey(friendAlias)) {
								int contactNum = contactFre.get(friendAlias);
								contactNum ++;
								contactFre.put(friendAlias, contactNum);
							} else {
								int contactNum = 1;
								contactFre.put(friendAlias, contactNum);
							}
						}
					}
					
					
					/*Iterator iter8 = contactFre.entrySet().iterator();
					while (iter8.hasNext()) {
						Map.Entry<String, Integer> entry8 = (Map.Entry<String, Integer>) iter8.next();
						int contactNum = entry8.getValue();
						if (contactNum >= 50) {
							contactStatics[10] ++;
						} else {
							contactStatics[contactNum / 5] ++;
						}
					}
					for (int i = 0; i < 10; i ++) {
						System.out.print(contactStatics[i] + " ");
					}
					System.out.println();*/
					
					
					Iterator iter7 = contactFre.entrySet().iterator();
					while (iter7.hasNext()) {
						Map.Entry<String, Integer> entry7 = (Map.Entry<String, Integer>) iter7.next();
						String friendAlias = entry7.getKey();
						int contactNum = entry7.getValue();
						int contactLevel = -1;
						if (contactNum < 10) {
							contactLevel = 1;
						} else if (contactNum < 20) {
							contactLevel = 2;
						} else {
							contactLevel = 3;
						}
						// ������λ���ѷ���������ͼƬ
						// keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
						if (userImages.containsKey(friendAlias)) {
							// �����ܱ�֤�û�һ��������λ���ѵ�ͼƬ���������ۣ�
							List <String> friendImages = userImages.get(friendAlias);
							for (int i = 0; i < friendImages.size(); i ++) {
								String friendImageIdAndDate = friendImages.get(i);
								String friendImageIdAndDatePart[] = friendImageIdAndDate.split("#");
								String friendImageId = friendImageIdAndDatePart[0];
								String friendImageDateString = friendImageIdAndDatePart[1];
								Date friendImageDate = df.parse(friendImageDateString);
								
						        // ����ںϷ���ʱ��������
						        if (friendImageDate.after(leftDate) && friendImageDate.before(publishDate)) {
						        	//System.out.println(df.format(friendImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
						        	// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
						        	if (lineDict.containsKey(friendImageId)) {
							        	String friendLineId = lineDict.get(friendImageId);
							        	String output = "#weight_edge " + lineId + " " + friendLineId + " friendimpact " + contactLevel + "\n";
							        	bw.append(output);
							        	bw.flush();
						        	}
						        }
							}
						}
					}
				}
		    }
			
			br.close();
			br2.close();
			br3.close();
			br4.close();
			bw.close();
			db.cleanCursors(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// for aaai wusen final
	// �޸���һ�����ķ��������Ǻ��񲻴��
	// ����࣬�õ�����һ������BasicFGM��ʽ��data�������û���Ϣ����������Ȧ��������ѵ���У������û��佻��ǿ�ȣ�ÿ������ΪtrainAndPredictNum����һ��ÿ�Ŷ���feature�����ÿ���������ܱ����������һЩ��
	public static void getBasicFGMData7(int trainAndPredictNum) {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			DBCollection feaCollection = db.getCollection("imgfeature");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("user_profile_bishe_2605.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/getUserTemporalEmotionType/temporalEmotion_oneDay_lowerLonger2withoutPuncNotSelf_All.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/getContact2/contactNotSelfWithTime.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/visual_gmoff_su10m10m_fi10m_" + trainAndPredictNum + "_less10_withoutE.txt")));
			
			String line = ""; 
			// ��������Ȧ�����Ϣ��keyΪ[imageId]��valueΪ[�û����������ǰһ������Ȧ������������]
			Map <String, Integer> imageFEs = new HashMap<String, Integer>();
			while ((line = br3.readLine()) != null) {
				// ��ʽΪ[����ͼƬid ͼƬ��� ͼƬ������ ͼƬ����ʱ�� [���۵�ͼƬ�ķ�����:���۵�ͼƬ���:������У�����Ϊ-1��]]��һ��Ϊһ��ͼƬ
				String []part = line.split(" ");
				String imageId = part[0];
				int maxCommentFre = 0;
				int maxCommentFreEmotionType = -1;
				int commentFre[] = {0, 0, 0, 0, 0, 0};
				for (int i = 4; i < part.length; i ++) {
					// �������
					//commentFre[Integer.parseInt(part[i].substring(part[i].length() - 1))] ++;
					// ͼƬ���
					commentFre[Integer.parseInt(part[i].substring(part[i].indexOf(":") + 1, part[i].indexOf(":") + 2))] ++;
				}
				for (int i = 0; i < 6; i ++) {
					if (commentFre[i] > maxCommentFre) {
						maxCommentFre = commentFre[i];
						maxCommentFreEmotionType = i;
					}
				}
				imageFEs.put(imageId, maxCommentFreEmotionType);
			}
			System.out.println("imageFEs.size(): " + imageFEs.size());
			
			// �����û���Ϣ��keyΪ[userAlias]��valueΪ[��#�ָ������û���Ϣ]
			Map <String, String> userProfiles = new HashMap<String, String>();
			while ((line = br2.readLine()) != null) {
				String userAlias = line;
				int attrNum = Integer.parseInt(br2.readLine());
				String attrString = "";
				for (int i = 0; i < attrNum; i ++) {
					line = br2.readLine();
					attrString += line + "#";
				}
				userProfiles.put(userAlias, attrString);
			}
			System.out.println("userProfiles.size(): " + userProfiles.size());
			
			// �����û����������keyΪ[userAlias]��valueΪ[�û����۹�������ͼƬ������#����ʱ��]
			Map<String, List<String>> contact = new HashMap<String, List<String>>();
			while ((line = br4.readLine()) != null) {
				String part[] = line.split(" ");
				String imageOwnerAlias = part[0];
				for (int i = 1; i < part.length; i ++) {
					String littlePart[] = part[i].split(":");
					String commentMakerAlias = littlePart[0];
					String commentDateString = littlePart[1];
					if (contact.containsKey(commentMakerAlias)) {
						List <String> contactList = contact.get(commentMakerAlias);
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					} else {
						List <String> contactList = new ArrayList <String>();
						contactList.add(imageOwnerAlias + "#" + commentDateString);
						contact.put(commentMakerAlias, contactList);
					}
				}
			}
			System.out.println("contactSize: " + contact.size());
			
			// ÿ��trainAndPredictNum��
			int totalNum = trainAndPredictNum * 6;
			int limitNum = (int) (totalNum * 0.6);
			
			// ��ÿһ��ͼƬ
			int lineNum = 1;
			int num = 0;
			int maxFriendSize = 0;
			int fre[] = {0, 0, 0, 0, 0, 0};
			Map<String, String> imageIds = new HashMap<String, String>();
			Map<String, List<String>> userImages = new HashMap<String, List<String>>();
			Map<String, String> lineDict = new HashMap<String, String>();
			while ((line = br.readLine()) != null) {
				String []parts = line.split(" ");
				String imageId = parts[0];
				String emotionType = parts[1];
				String userAlias = parts[2];
				String dateString = parts[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				int friendEmotion = -1;
				if (imageFEs.containsKey(imageId)) {
					friendEmotion = imageFEs.get(imageId);
				}
				if (fre[Integer.parseInt(emotionType)] >= trainAndPredictNum) {
					continue;
				}
				
				DBObject query = new BasicDBObject("_id", imageId);
				DBObject field = new BasicDBObject();
				DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
				while (feaCursor.hasNext()) {
					String output = "";
					// ѵ�������Լ�
					if (num < limitNum) {
						output = "+";
					} else {
						output = "?";
					}
					DBObject feaDbo = feaCursor.next();
					// �������visual feature
					output += emotionType
							+ " saturation:" + feaDbo.get("saturation") 
							+ " sat_con:" + feaDbo.get("sat_con")
							+ " bright:" + feaDbo.get("bright") 
							+ " bright_con:" + feaDbo.get("bright_con") 
							+ " dull_color_ratio:" + feaDbo.get("dull_color_ratio")
							+ " cool_color_ratio:"	+ feaDbo.get("cool_color_ratio")
							+ " fg_color_df:" + feaDbo.get("fg_color_dif") 
							+ " fg_area_dif:"	+ feaDbo.get("fg_area_dif") 
							+ " fg_texture_src:" + feaDbo.get("fg_texture_src") 
							+ " fg_texture_sal:" + feaDbo.get("fg_texture_sal")
							+ " c1_h:" + feaDbo.get("c1_h") + " c1_s:" + feaDbo.get("c1_s") + " c1_v:" + feaDbo.get("c1_v") 
							+ " c2_h:" + feaDbo.get("c2_h") + " c2_s:" + feaDbo.get("c2_s")	+ " c2_v:" + feaDbo.get("c2_v") 
							+ " c3_h:" + feaDbo.get("c3_h") + " c3_s:" + feaDbo.get("c3_s")	+ " c3_v:" + feaDbo.get("c3_v") 
							+ " c4_h:" + feaDbo.get("c4_h") + " c4_s:" + feaDbo.get("c4_s")	+ " c4_v:" + feaDbo.get("c4_v") 
							+ " c5_h:" + feaDbo.get("c5_h") + " c5_s:" + feaDbo.get("c5_s")	+ " c5_v:" + feaDbo.get("c5_v");
					// ȥ�������ÿ�ѧ��������ʾ��ͼƬ
					if (output.contains("E")) continue;
					if (Double.parseDouble(feaDbo.get("saturation").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("sat_con").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("bright").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("bright_con").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("dull_color_ratio").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("cool_color_ratio").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("fg_color_dif").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("fg_area_dif").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("fg_texture_src").toString()) > 1.0 ||
							Double.parseDouble(feaDbo.get("fg_texture_sal").toString()) > 1.0) continue;
					// �û�������Ϣ��correlationThree
					// ��0��1�Լ���1��2����ǲ�һ���ģ���1��2������������ά�����������࣬����һ��Ĭ��Ϊ0������0��1������ã�������ά�����϶����ࣩ
					if (userProfiles.containsKey(userAlias)) {
						int gender = -1; // 0: female 1: male
						int marital = -1; // 0: single 1: taken
						int occupation = -1; // 0: engineer 1: artist
						String attrString = userProfiles.get(userAlias);
				    	String attrs[] = attrString.split("#");				
				    	for (int j = 0; j < attrs.length; j ++) {
				    		String part[] = attrs[j].split(":");
				    		if (part[0].equals("I am")) {
				    			if (part[1].contains("Female")) {
				    				gender = 0;
				    			} else if (part[1].contains("Male")) {
				    				gender = 1;
				    			}
				    			if (part[1].contains("Single")) {
				    				marital = 0;
				    			} else if (part[1].contains("Taken")) {
				    				marital = 1;
				    			}
				    		} else if (part[0].equals("Occupation")) {
				    			if (part[1].contains("software") || part[1].contains("IT") || part[1].contains("computer") ||
				    					part[1].contains("code") || part[1].contains("web") || part[1].contains("network") ||
				    					part[1].contains("engineer") || part[1].contains("tech") || part[1].contains("mechanical") ||
				    					part[1].contains("electronic") || part[1].contains("medical") || part[1].contains("market") ||
					    				part[1].contains("product") || part[1].contains("consultant") || part[1].contains("science")) {
					    				occupation = 0;
					    			} else if (part[1].contains("artist") || part[1].contains("writer") || part[1].contains("musician") ||
					    					part[1].contains("dancer") || part[1].contains("photographer") || part[1].contains("film") ||
					    					part[1].contains("designer") || part[1].contains("blog") || part[1].contains("editor") ||
					    					part[1].contains("freelancer")) {
						    			occupation = 1;
						    		}
				    		}
				    	}
				    	if (gender > -1) {
				    		output += " gender:" + gender;
				    	}
				    	if (marital > -1) {
				    		output += " marital:" + marital;
				    	}
				    	if (occupation > -1) {
				    		output += " occupation:" + occupation;
				    	}
					}
					// �û�����Ȧ�Ĵ�С
					if (contact.containsKey(userAlias)) {
						List <String> contactList = contact.get(userAlias);
						List <String> friendList = new ArrayList<String>();
						for (int i = 0; i < contactList.size(); i ++) {
							String contactInfo = contactList.get(i);
							String part[] = contactInfo.split("#");
							String friendAlias = part[0];
							String contactDateString = part[1] + "000000";
							Date contactDate = df.parse(contactDateString);
							if (!friendList.contains(friendAlias) && !contactDate.after(publishDate)) {
								friendList.add(friendAlias);
							}
						}
						// 313ΪfriendList.size()�����ֵ
						if (friendList.size() > 0) {
							output += " friendSize:" + friendList.size() * 1.0 / 313;
						}
						if (friendList.size() > maxFriendSize) {
							maxFriendSize = friendList.size();
						}
					}
					// ����Ȧ��У�correlationTwo
					if (friendEmotion > -1) {
						output += " friendEmotion:" + friendEmotion;
					}
					//output += " " + imageId;
					output += " \n";
					bw.append(output);
					bw.flush();
					// ��¼
					imageIds.put(imageId, userAlias + "#" + friendEmotion + "#" + emotionType + "#" + dateString);
					if (userImages.containsKey(userAlias)) {
						List <String> images = userImages.get(userAlias);
						images.add(imageId + "#" + dateString);
						userImages.put(userAlias, images);
					} else {
						List <String> images = new ArrayList<String>();
						images.add(imageId + "#" + dateString);
						userImages.put(userAlias, images);
					}
					lineDict.put(imageId, String.valueOf(lineNum));
					fre[Integer.parseInt(emotionType)] ++;
					lineNum ++;
					num ++;
					if (num % 1000 == 0) {
						System.out.println(num);
					}
				}
			}
			System.out.println("num: " + num);
			System.out.println("maxFriendSize: " + maxFriendSize);
			
			// edge temporal correlation����ĳ��ʱ���ڣ��������ͼƬ��ͬһλ�û���������������ͼƬ�����һ����
			// ��ÿһ��ͼƬ��Ϣ���õ���ӦͼƬid
			Iterator iter5 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter5.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter5.next();
			    String lineId = entry.getValue().toString();
			    String imageId = entry.getKey().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
		        Calendar rightNow2 = Calendar.getInstance();
		        rightNow2.setTime(publishDate);
		        rightNow2.add(Calendar.MINUTE, 10);//���ڼ�10����
		        Date rightDate = rightNow2.getTime();
		        
			    Date closestDate = leftDate;
		        String closestUserImageLineId = "-1";
		        // ���Ҹ��û�������ͼƬ
		        // keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
				List <String> images = userImages.get(userAlias);
				int sameUserImageNum = 0;
				for (int i = 0; i < images.size(); i ++) {
					String imageIdAndDateString = images.get(i);
					String imageIdAndDatePart[] = imageIdAndDateString.split("#");
					String userImageId = imageIdAndDatePart[0];
					String userImageDateString = imageIdAndDatePart[1];
					Date userImageDate = df.parse(userImageDateString);
					// �����ͬһ��ͼƬ������һ��
					if (imageId.equals(userImageId)) {
						continue;
					}
					// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
		        	if (!lineDict.containsKey(userImageId)) {
		        		continue;
		        	}
			        // ͳ���ڷ�����ͼƬ������ʮ�����ڷ��˶���ͼƬ
					if (userImageDate.after(leftDate) && userImageDate.before(rightDate)) {
						sameUserImageNum ++;
						if (userImageDate.after(closestDate) && userImageDate.before(publishDate)) {
							//System.out.println(df.format(userImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
							String userImageLineId = lineDict.get(userImageId);
		        			closestDate = userImageDate;
		        			closestUserImageLineId = userImageLineId;
						}
					}
				}
				if (!closestUserImageLineId.equals("-1") && sameUserImageNum < 10) {
					String output = "#edge " + lineId + " " + closestUserImageLineId + " sameuser\n";
		        	bw.append(output);
		        	bw.flush();
				}
			}
			
			// edge correlationOne���õ�ͼƬ֮���Ӱ��������
			// <5 <10 ..... <50 >50
			int contactStatics[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			Iterator iter6 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter6.hasNext()) { 
			    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter6.next();
			    String imageId = entry.getKey().toString();
			    String lineId = entry.getValue().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				Date publishDate = df.parse(dateString);
				
				// �õ��Ϸ���ʱ������
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
				// ʹ��ͼƬ�����û����Ҹ��û����۹��������û�����һ�����Բ鵽
				if (contact.containsKey(userAlias)) {
					List<String> contactList = contact.get(userAlias);
					// ��ÿһλ�н��������ѣ����㽻����ǿ��
					Map <String, Integer> contactFre = new HashMap<String, Integer>();
					for(int i = 0; i < contactList.size(); i ++) {
						String contactContent = contactList.get(i);
						String part[] = contactContent.split("#");
						String friendAlias = part[0];
						String contactDateString = part[1] + "000000";
						Date contactDate = df.parse(contactDateString);
						// �������ʱ����ͼƬ����ʱ��ǰ����¼һ�ν���
						if (!contactDate.after(publishDate)) {
							if (contactFre.containsKey(friendAlias)) {
								int contactNum = contactFre.get(friendAlias);
								contactNum ++;
								contactFre.put(friendAlias, contactNum);
							} else {
								int contactNum = 1;
								contactFre.put(friendAlias, contactNum);
							}
						}
					}
					
					Iterator iter7 = contactFre.entrySet().iterator();
					while (iter7.hasNext()) {
						Map.Entry<String, Integer> entry7 = (Map.Entry<String, Integer>) iter7.next();
						String friendAlias = entry7.getKey();
						int contactNum = entry7.getValue();
						int contactLevel = -1;
						if (contactNum < 10) {
							contactLevel = 1;
						} else if (contactNum < 20) {
							contactLevel = 2;
						} else {
							contactLevel = 3;
						}
						// ������λ���ѷ���������ͼƬ
						// keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
						if (userImages.containsKey(friendAlias)) {
							// �����ܱ�֤�û�һ��������λ���ѵ�ͼƬ���������ۣ�
							List <String> friendImages = userImages.get(friendAlias);
							for (int i = 0; i < friendImages.size(); i ++) {
								String friendImageIdAndDate = friendImages.get(i);
								String friendImageIdAndDatePart[] = friendImageIdAndDate.split("#");
								String friendImageId = friendImageIdAndDatePart[0];
								String friendImageDateString = friendImageIdAndDatePart[1];
								Date friendImageDate = df.parse(friendImageDateString);
								
						        // ����ںϷ���ʱ��������
						        if (friendImageDate.after(leftDate) && friendImageDate.before(publishDate)) {
						        	//System.out.println(df.format(friendImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
						        	// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
						        	if (lineDict.containsKey(friendImageId)) {
							        	String friendLineId = lineDict.get(friendImageId);
							        	String output = "#weight_edge " + lineId + " " + friendLineId + " friendimpact " + contactLevel + "\n";
							        	bw.append(output);
							        	bw.flush();
						        	}
						        }
							}
						}
					}
				}
		    }
			
			br.close();
			br2.close();
			br3.close();
			br4.close();
			bw.close();
			db.cleanCursors(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testEdge() {
		try {
			// ����ͼƬ��Ϣ��keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
			Map <String, String> imageIds = new HashMap<String, String>();
			imageIds.put("0", "stella#0#0#20140905083800");
			imageIds.put("1", "stella#0#0#20140905083900");
			imageIds.put("2", "stella#0#0#20140905084000");
			imageIds.put("3", "stella#0#0#20140905084400");
			imageIds.put("4", "stella#0#0#20140905085800");
			imageIds.put("5", "stella#0#0#20140905085900");
			imageIds.put("6", "wangyu#0#0#20140905083030");
			
			// ͳ���û�����ͼƬ�����keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
			Map<String, List<String>> userImages = new HashMap<String, List<String>>();
			List <String> imageIdAndDate = new ArrayList <String>();
			imageIdAndDate.add("0#20140905083800");
			imageIdAndDate.add("1#20140905083900");
			imageIdAndDate.add("2#20140905084000");
			imageIdAndDate.add("3#20140905084400");
			imageIdAndDate.add("4#20140905085800");
			imageIdAndDate.add("5#20140905085900");
			userImages.put("stella", imageIdAndDate);
			List <String> imageIdAndDate2 = new ArrayList <String>();
			imageIdAndDate2.add("6#20140905083030");
			userImages.put("wangyu", imageIdAndDate2);
			
			// keyΪ[imageId]��valueΪ[lineId]
			Map<String, String> lineDict = new HashMap<String, String>();
			lineDict.put("0", "0");
			lineDict.put("1", "1");
			lineDict.put("2", "2");
			lineDict.put("3", "3");
			lineDict.put("4", "4");
			lineDict.put("5", "5");
			lineDict.put("6", "6");
			
			// �����û����������keyΪ[userAlias]��valueΪ[�û����۹�������ͼƬ������#����ʱ��]
			Map<String, List<String>> contact = new HashMap<String, List<String>>();
			List <String> commentImageIdAndDate = new ArrayList <String>();
			commentImageIdAndDate.add("wangyu#20140905");
			contact.put("stella", commentImageIdAndDate);
			
			Iterator iter5 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter5.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter5.next();
			    String lineId = entry.getValue().toString();
			    String imageId = entry.getKey().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(dateString);
				
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
		        Calendar rightNow2 = Calendar.getInstance();
		        rightNow2.setTime(publishDate);
		        rightNow2.add(Calendar.MINUTE, 10);//���ڼ�10����
		        Date rightDate = rightNow2.getTime();
		        
			    Date closestDate = leftDate;
		        String closestUserImageLineId = "-1";
		        // ���Ҹ��û�������ͼƬ
		        // keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
				List <String> images = userImages.get(userAlias);
				int sameUserImageNum = 0;
				for (int i = 0; i < images.size(); i ++) {
					String imageIdAndDateString = images.get(i);
					String imageIdAndDatePart[] = imageIdAndDateString.split("#");
					String userImageId = imageIdAndDatePart[0];
					String userImageDateString = imageIdAndDatePart[1];
					Date userImageDate = df.parse(userImageDateString);
					// �����ͬһ��ͼƬ������һ��
					if (imageId.equals(userImageId)) {
						continue;
					}
					// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
		        	if (!lineDict.containsKey(userImageId)) {
		        		continue;
		        	}
			        // ����ںϷ���ʱ��������
					if (userImageDate.after(leftDate) && userImageDate.before(rightDate)) {
						sameUserImageNum ++;
						if (userImageDate.after(closestDate) && userImageDate.before(publishDate)) {
							//System.out.println(df.format(userImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
							String userImageLineId = lineDict.get(userImageId);
		        			closestDate = userImageDate;
		        			closestUserImageLineId = userImageLineId;
						}
					}
				}
				if (!closestUserImageLineId.equals("-1") && sameUserImageNum < 3) {
					String output = "#edge " + lineId + " " + closestUserImageLineId + " sameuser\n";
		        	System.out.println(output);
				}
			}
			
			Iterator iter6 = lineDict.entrySet().iterator(); 
			System.out.println("lineDict Size: " + lineDict.size());
			while (iter6.hasNext()) { 
			    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter6.next();
			    String imageId = entry.getKey().toString();
			    String lineId = entry.getValue().toString();
			    // keyΪ[imageId]��valueΪ�û�[alias#����Ȧ���#ͼƬ���#����ʱ��]
		    	String valueString = imageIds.get(imageId);
				String valuePart[] = valueString.split("#");
				String userAlias = valuePart[0];
				String dateString = valuePart[3];
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				Date publishDate = df.parse(dateString);
				
				// �õ��Ϸ���ʱ������
				Calendar rightNow = Calendar.getInstance();
		        rightNow.setTime(publishDate);
		        rightNow.add(Calendar.MINUTE, -10);//���ڼ�10����
		        Date leftDate = rightNow.getTime();
		        
				// ʹ��ͼƬ�����û����Ҹ��û����۹��������û�����һ�����Բ鵽
				if (contact.containsKey(userAlias)) {
					List<String> contactList = contact.get(userAlias);
					// ��ÿһλ�н��������ѣ����㽻����ǿ��
					Map <String, Integer> contactFre = new HashMap<String, Integer>();
					for(int i = 0; i < contactList.size(); i ++) {
						String contactContent = contactList.get(i);
						String part[] = contactContent.split("#");
						String friendAlias = part[0];
						String contactDateString = part[1] + "000000";
						Date contactDate = df.parse(contactDateString);
						// �������ʱ����ͼƬ����ʱ��ǰ����¼һ�ν���
						if (!contactDate.after(publishDate)) {
							if (contactFre.containsKey(friendAlias)) {
								int contactNum = contactFre.get(friendAlias);
								contactNum ++;
								contactFre.put(friendAlias, contactNum);
							} else {
								int contactNum = 1;
								contactFre.put(friendAlias, contactNum);
							}
						}
					}
					
					Iterator iter7 = contactFre.entrySet().iterator();
					while (iter7.hasNext()) {
						Map.Entry<String, Integer> entry7 = (Map.Entry<String, Integer>) iter7.next();
						String friendAlias = entry7.getKey();
						int contactNum = entry7.getValue();
						int contactLevel = -1;
						if (contactNum < 10) {
							contactLevel = 1;
						} else if (contactNum < 20) {
							contactLevel = 2;
						} else {
							contactLevel = 3;
						}
						// ������λ���ѷ���������ͼƬ
						// keyΪ[�û���alias]��valueΪ[[�û�����ͼƬimage#��������ʱ��]��list]
						if (userImages.containsKey(friendAlias)) {
							// �����ܱ�֤�û�һ��������λ���ѵ�ͼƬ���������ۣ�
							List <String> friendImages = userImages.get(friendAlias);
							for (int i = 0; i < friendImages.size(); i ++) {
								String friendImageIdAndDate = friendImages.get(i);
								String friendImageIdAndDatePart[] = friendImageIdAndDate.split("#");
								String friendImageId = friendImageIdAndDatePart[0];
								String friendImageDateString = friendImageIdAndDatePart[1];
								Date friendImageDate = df.parse(friendImageDateString);
								
						        // ����ںϷ���ʱ��������
						        if (friendImageDate.after(leftDate) && friendImageDate.before(publishDate)) {
						        	//System.out.println(df.format(friendImageDate) + " " + df.format(leftDate) + " " + df.format(publishDate));
						        	// ��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
						        	if (lineDict.containsKey(friendImageId)) {
							        	String friendLineId = lineDict.get(friendImageId);
							        	String output = "#weight_edge " + lineId + " " + friendLineId + " friendimpact " + contactLevel + "\n";
							        	System.out.println(output);
						        	}
						        }
							}
						}
					}
				}
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// ͳ�Ƹ����͵��û����ж����ˣ���¼�������û�������
	public static void getUserProfileStatistics() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("user_profile_bishe_2605.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getUserProfileStatistics/userProfileGender.txt")));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("output/getUserProfileStatistics/userProfileMaritalStatus.txt")));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File("output/getUserProfileStatistics/userProfileOccupation.txt")));
			Map <String, String> userProfiles = new HashMap<String, String>();

			// �����û���Ϣ
			String line = "";
			while ((line = br.readLine()) != null) {
				String userAlias = line;
				int attrNum = Integer.parseInt(br.readLine());
				String attrString = "";
				for (int i = 0; i < attrNum; i ++) {
					line = br.readLine();
					attrString += line + "#";
				}
				userProfiles.put(userAlias, attrString);
			}
			System.out.println(userProfiles.size());
			
			List<String> males = new ArrayList<String>();
			List<String> females = new ArrayList<String>();
			List<String> singles = new ArrayList<String>();
			List<String> takens = new ArrayList<String>();
			List<String> engineers = new ArrayList<String>();
			List<String> artists = new ArrayList<String>();
		    
			Iterator<Entry<String, String>> iter = userProfiles.entrySet().iterator(); 
			while (iter.hasNext()) {
			    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			    String userAlias = (String) entry.getKey();
			   
			    if (userProfiles.containsKey(userAlias)) {
			    	String attrString = userProfiles.get(userAlias);
			    	String attrs[] = attrString.split("#");
			    	for (int j = 0; j < attrs.length; j ++) {
			    		String part[] = attrs[j].split(":");
			    		if (part[0].equals("I am")) {
			    			if (part[1].contains("Female")) {
			    				females.add(userAlias);
			    			} else if (part[1].contains("Male")) {
			    				males.add(userAlias);
			    			}
			    			if (part[1].contains("Single")) {
			    				singles.add(userAlias);
			    			} else if (part[1].contains("Taken")) {
			    				takens.add(userAlias);
			    			}
			    		} else if (part[0].equals("Occupation")) {
			    			String occupation = part[1].toLowerCase();
			    			if (occupation.contains("software") || occupation.contains("IT") || occupation.contains("computer") ||
			    				occupation.contains("code") || occupation.contains("web") || occupation.contains("network") ||
			    				occupation.contains("engineer") || occupation.contains("tech") || occupation.contains("mechanical") ||
			    				occupation.contains("electronic") || occupation.contains("medical") || occupation.contains("market") ||
			    				occupation.contains("product") || occupation.contains("consultant") || occupation.contains("science")) {
			    				engineers.add(userAlias);
			    			} else if (occupation.contains("artist") || occupation.contains("writer") || occupation.contains("musician") ||
			    				occupation.contains("dancer") || occupation.contains("photographer") || occupation.contains("film") ||
			    				occupation.contains("designer") || occupation.contains("blog") || occupation.contains("editor") ||
			    				occupation.contains("freelancer")) {
				    			artists.add(userAlias);
				    		}
			    		}
			    	}
			    }
			}
			
			System.out.println(females.size());
			System.out.println(males.size());
			System.out.println(singles.size());
			System.out.println(takens.size());
			System.out.println(engineers.size());
			System.out.println(artists.size());
			
		    
			bw.append("females: ");
			bw.flush();
		    for (int i = 0; i < females.size(); i ++) {
		    	bw.append(females.get(i) + " ");
				bw.flush();
		    }
		    bw.append("\n");
			bw.flush();
			bw.append("males: ");
			bw.flush();
		    for (int i = 0; i < males.size(); i ++) {
		    	bw.append(males.get(i) + " ");
				bw.flush();
		    }
		    bw.append("\n");
			bw.flush();
			
			bw2.append("singles: ");
			bw2.flush();
		    for (int i = 0; i < singles.size(); i ++) {
		    	bw2.append(singles.get(i) + " ");
				bw2.flush();
		    }
		    bw2.append("\n");
			bw2.flush();
			bw2.append("taken: ");
			bw2.flush();
		    for (int i = 0; i < takens.size(); i ++) {
		    	bw2.append(takens.get(i) + " ");
				bw2.flush();
		    }
		    bw2.append("\n");
			bw2.flush();
			
			bw3.append("engineers: ");
			bw3.flush();
			for (int i = 0; i < engineers.size(); i ++) {
		    	bw3.append(engineers.get(i) + " ");
				bw3.flush();
		    }
		    bw3.append("\n");
			bw3.flush();
			bw3.append("artists: ");
			bw3.flush();
			for (int i = 0; i < artists.size(); i ++) {
		    	bw3.append(artists.get(i) + " ");
				bw3.flush();
		    }
		    bw3.append("\n");
			bw3.flush();
			
			br.close();
			bw.close();
			bw2.close();
			bw3.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// ͳ�ƾ�����Ů������ǵ�����Ϣ�û�֮��Ľ���
	public static void getContactForAllUser() {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getUserProfileStatistics/userProfileCategories.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getContactForAllUser/contactNotSelfWithTime.txt")));
			
			// �����û��б�
			Map <String, List<String>> contactMap = new HashMap<String, List<String>>();
			List<String> userAliases = new ArrayList<String>();
			String line = "";
			while ((line = br.readLine()) != null) {
				String part[] = line.split(" ");
				for (int i = 0; i < part.length; i ++) {
					String userAlias = part[i];
					userAliases.add(userAlias);
					List <String> contact = new ArrayList<String>();
					contactMap.put(userAlias, contact);
				}
			}
			br.close();
			System.out.println("userAliases.size(): " + userAliases.size());
			
			// ��ѯ���������۵�ͼƬ
			DBCollection imgCollection = db.getCollection("images");
			DBObject query = new BasicDBObject();
			query.put("comts", new BasicDBObject("$exists", true));
			DBObject field = new BasicDBObject();
			field.put("comts", true);
			field.put("ownid", true);
			DBCursor imgCursor = imgCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			System.out.println("image with comments num: " + imgCursor.count());
			
			int num = 0;
			while (imgCursor.hasNext()) {
				// �õ�ͼƬ������
				DBObject imgDbo = imgCursor.next();
				String imageOwnerAlias = imgDbo.get("ownid").toString();
				// ���ͼƬ�����߲������ǹ�ע���û�������������б�ǩ���з������ڵ�ͼƬ���û���ֻ���������û���contact��������һ��ͼƬ
				if (!userAliases.contains(imageOwnerAlias)) {
					continue;
				}
				// �õ�����ͼƬ�������б�
				List commentList = (List) imgDbo.get("comts");
				for (int i = 0; i < commentList.size(); i ++) {
					BasicBSONObject comment = (BasicBSONObject) commentList.get(i);
					String commentMakerAlias = comment.get("ownid").toString();
					Date commentDate = (Date) (((DBObject)comment).get("comtt"));
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					String commentDateString = df.format(commentDate);
					// ��������߲������ǹ�ע���û�������������б�ǩ���з������ڵ�ͼƬ���û���ֻ���������û���contact��������һ������
					if (!userAliases.contains(commentMakerAlias)) {
						continue;
					}
					// �����������ͼƬ�����ߣ�����һ������
					if (imageOwnerAlias.equals(commentMakerAlias)) {
						continue;
					}
					// ��������ϣ���¼�����ߺ�����ʱ��
					List <String> contact = contactMap.get(imageOwnerAlias);
					contact.add(commentMakerAlias + ":" + commentDateString);
					contactMap.put(imageOwnerAlias, contact);
				}
				num ++;
				if (num % 1000 == 0) {
					System.out.println(num);
				}
			}
			System.out.println(num);
			
			// ���
			Iterator iter = contactMap.entrySet().iterator(); 
			while (iter.hasNext()) {
			    Map.Entry entry = (Map.Entry) iter.next();
			    String imageOwnerAlias = entry.getKey().toString();
			    List <String> contact = (List <String>) entry.getValue(); 
			    // �����λ�û�û�к������û�����������һλ�û�
			    if (contact.size() == 0) {
			    	continue;
			    }
			    String output = "";
			    output += imageOwnerAlias + " ";
			    for (int i = 0; i < contact.size(); i ++) {
			    	String contactPiece = contact.get(i);
				    output += contactPiece + " ";
				}
				output += "\n";
				bw.append(output);
				bw.flush();
			}
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void pickDemographics(int emotionType, String feature, int type) {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/pickDemographics/probability_all_top3.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/pickDemographics/visual_gmoff_su10m10m_fi10m_11500_less10_withoutE_id.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/pickDemographics/" + emotionType + "_" + feature + "_" + type + "_url.txt")));

			DBCollection imgCollection = db.getCollection("images");
			Map <Integer, String> images = new HashMap<Integer, String>();
			// ��������
			String line = "";
			int lineNum = 1;
			while ((line = br2.readLine()) != null) {
				images.put(lineNum, line);
				lineNum ++;
			}
			System.out.println("lineNum: " + lineNum);
			System.out.println("images.size(): " +  images.size());
			
			lineNum = 1;
			while ((line = br.readLine()) != null) {
				String part[] = line.split(" ");
				int maxType = -1;
				float maxPro = 0;
				for (int i = 3; i < 9; i ++) {
					float tempPro = Float.parseFloat(part[i]);
					if (tempPro > maxPro) {
						maxPro = tempPro;
						maxType = i - 3;
					}
				}
				String part2[] = images.get(lineNum).split(" ");
				int correctType = Integer.parseInt(part2[0].substring(1, 2));
				// ���Ԥ������ȷ
				if (maxType == emotionType && correctType == emotionType) {
					for (int i = 1; i < part2.length; i ++) {
						String correctNameAndValue = feature + ":" + type;
						if (part2[i].equals(correctNameAndValue)) {
							String imageId = part2[part2.length-1];
							DBObject imgQuery = new BasicDBObject("_id", new BigInteger(imageId, 10).longValue());
							DBObject imgField = new BasicDBObject();
							imgField.put("imgur.url", true);
							//imgField.put("exif.datt", true);
							//imgField.put("ownid", true);
							DBCursor imgCursor = imgCollection.find(imgQuery, imgField);
							while (imgCursor.hasNext()) {
								DBObject imgDbo = imgCursor.next();
								String url = ((DBObject) imgDbo.get("imgur")).get("url").toString();
								//String date = ((DBObject)imgDbo.get("exif")).get("datt").toString();
								//String ownid = imgDbo.get("ownid").toString();
								//String output = lineNum + ": " + line + date + " " + ownid + " " + imageId + " " + url;
								//String output = lineNum + ": " + images.get(lineNum) + " " + url;
								String output = url;
								//System.out.println(output);
								bw.append(output + "\n");
								bw.flush();
							}
						}
					}
				}
				lineNum ++;
			}
			
			br.close();
			br2.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getSpecialUserImageFeatures() {
		try {
			Mongo mongo = new Mongo("166.111.139.44");
			DB db = mongo.getDB("flickr");
			DBCollection feaCollection = db.getCollection("imgfeature");
			
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/qualifiedImagesId&emotionType&userAlias&date.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getUserProfileStatistics/userProfileMaritalStatus.txt")));
			BufferedWriter bw0a = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_single_0.txt")));
			BufferedWriter bw0b = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_taken_0.txt")));
			BufferedWriter bw1a = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_single_1.txt")));
			BufferedWriter bw1b = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_taken_1.txt")));
			BufferedWriter bw2a = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_single_2.txt")));
			BufferedWriter bw2b = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_taken_2.txt")));
			BufferedWriter bw3a = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_single_3.txt")));
			BufferedWriter bw3b = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_taken_3.txt")));
			BufferedWriter bw4a = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_single_4.txt")));
			BufferedWriter bw4b = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_taken_4.txt")));
			BufferedWriter bw5a = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_single_5.txt")));
			BufferedWriter bw5b = new BufferedWriter(new FileWriter(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_taken_5.txt")));
			
			String line = "";
			
			List<String> userTypeA = new ArrayList<String>();
			List<String> userTypeB = new ArrayList<String>();
			
			line = br2.readLine();
			String names[] = line.split(" ");
			for (int i = 1; i < names.length; i ++) userTypeA.add(names[i]);
			System.out.println("userTypeA.size(): " + userTypeA.size());
			
			line = br2.readLine();
			names = line.split(" ");
			for (int i = 1; i < names.length; i ++) userTypeB.add(names[i]);
			System.out.println("userTypeB.size(): " + userTypeB.size());
						
			Map<String, Map<String, List<String>>> emotionTypeImages = new HashMap<String, Map<String, List<String>>>();
			for (int i = 0; i < 6; i ++) {
				Map<String, List<String>> typeImages = new HashMap<String, List<String>>();
				for (int j = 0; j < 2; j ++) {
				    List<String> images = new ArrayList<String>();
				    typeImages.put(String.valueOf(j), images);
				}
				emotionTypeImages.put(String.valueOf(i), typeImages);
			}
			
			while ((line = br.readLine()) != null) {
				String []part = line.split(" ");
				String imageId = part[0];
				String emotionType = part[1];
				String userAlias = part[2];
				if (userTypeA.contains(userAlias)) {
					Map<String, List<String>> typeImages = emotionTypeImages.get(emotionType);
					List<String> images = typeImages.get("0");
					images.add(imageId);
					typeImages.put("0", images);
					emotionTypeImages.put(emotionType, typeImages);
				} else if (userTypeB.contains(userAlias)) {
					Map<String, List<String>> typeImages = emotionTypeImages.get(emotionType);
					List<String> images = typeImages.get("1");
					images.add(imageId);
					typeImages.put("1", images);
					emotionTypeImages.put(emotionType, typeImages);
				}
			}
		
			Iterator<Entry<String, Map<String, List<String>>>> iter = emotionTypeImages.entrySet().iterator();
			while (iter.hasNext()) { 
				Map.Entry entry = (Map.Entry) iter.next();
				String emotionType = (String) entry.getKey();
				Map<String, List<String>> typeImages = (Map<String, List<String>>) entry.getValue();
				List<String> typeAImages = typeImages.get("0");
				List<String> typeBImages = typeImages.get("1");
				System.out.println("emotion: " + emotionType);
				System.out.println("typeAImages.size(): " + typeAImages.size());
				System.out.println("typeBImages.size(): " + typeBImages.size());
				
				int num = 0;
				boolean enough = false;
				for(int i = 0; i < typeAImages.size(); i ++) {
					if (enough) break;
					String imageId = typeAImages.get(i);
					DBObject query = new BasicDBObject("_id", imageId);
					DBObject field = new BasicDBObject();
					DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
					while (feaCursor.hasNext()) {
						String output = "";
						DBObject feaDbo = feaCursor.next();
						// �������visual feature
						output += feaDbo.get("saturation") 
								+ " " + feaDbo.get("sat_con")
								+ " " + feaDbo.get("bright") 
								+ " " + feaDbo.get("bright_con") 
								+ " " + feaDbo.get("dull_color_ratio")
								+ " " + feaDbo.get("cool_color_ratio")
								+ " " + feaDbo.get("fg_color_dif") 
								+ " " + feaDbo.get("fg_area_dif") 
								+ " " + feaDbo.get("fg_texture_src") 
								+ " " + feaDbo.get("fg_texture_sal")
								+ " \n";
						// ������п�ѧ��������ʾ��feature������һ��
						if (output.contains("E")) continue;
						if (output.contains("0.0 ")) continue;
						if (Double.parseDouble(feaDbo.get("saturation").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("sat_con").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("bright").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("bright_con").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("dull_color_ratio").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("cool_color_ratio").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_color_dif").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_area_dif").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_texture_src").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_texture_sal").toString()) > 1.0) continue;
						num ++;
						switch (Integer.parseInt(emotionType)) {
							case 0: bw0a.append(output); bw0a.flush(); break;
							case 1: bw1a.append(output); bw1a.flush(); break;
							case 2: bw2a.append(output); bw2a.flush(); break;
							case 3: bw3a.append(output); bw3a.flush(); break;
							case 4: bw4a.append(output); bw4a.flush(); break;
							case 5: bw5a.append(output); bw5a.flush(); break;
						}
						//if (num == 500) enough = true;
					}
				}
				
				num = 0;
				enough = false;
				for(int i = 0; i < typeBImages.size(); i ++) {
					if (enough) break;
					String imageId = typeBImages.get(i);
					DBObject query = new BasicDBObject("_id", imageId);
					DBObject field = new BasicDBObject();
					DBCursor feaCursor = feaCollection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT).limit(1);
					while (feaCursor.hasNext()) {
						String output = "";
						DBObject feaDbo = feaCursor.next();
						// �������visual feature
						output += feaDbo.get("saturation") 
								+ " " + feaDbo.get("sat_con")
								+ " " + feaDbo.get("bright") 
								+ " " + feaDbo.get("bright_con") 
								+ " " + feaDbo.get("dull_color_ratio")
								+ " " + feaDbo.get("cool_color_ratio")
								+ " " + feaDbo.get("fg_color_dif") 
								+ " " + feaDbo.get("fg_area_dif") 
								+ " " + feaDbo.get("fg_texture_src") 
								+ " " + feaDbo.get("fg_texture_sal")
								+ " \n";
						// ������п�ѧ��������ʾ��feature������һ��
						if (output.contains("E")) continue;
						if (output.contains("0.0 ")) continue;
						if (Double.parseDouble(feaDbo.get("saturation").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("sat_con").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("bright").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("bright_con").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("dull_color_ratio").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("cool_color_ratio").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_color_dif").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_area_dif").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_texture_src").toString()) > 1.0 ||
								Double.parseDouble(feaDbo.get("fg_texture_sal").toString()) > 1.0) continue;
						num ++;
						switch (Integer.parseInt(emotionType)) {
							case 0: bw0b.append(output); bw0b.flush(); break;
							case 1: bw1b.append(output); bw1b.flush(); break;
							case 2: bw2b.append(output); bw2b.flush(); break;
							case 3: bw3b.append(output); bw3b.flush(); break;
							case 4: bw4b.append(output); bw4b.flush(); break;
							case 5: bw5b.append(output); bw5b.flush(); break;
						}
						//if (num == 500) enough = true;;
					}
				}
			}
			
			br.close();
			br2.close();
			bw0a.close(); bw1a.close(); bw2a.close(); bw3a.close(); bw4a.close(); bw5a.close();
			bw0b.close(); bw1b.close(); bw2b.close(); bw3b.close(); bw4b.close(); bw5b.close();
			db.cleanCursors(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void calculateAverageFeature(int emotionType) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getSpecialUserImageFeatures/without0.0all/maritalStatus_taken_" + emotionType + ".txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/calculateAverageFeature/without0.0all/maritalStatus_taken_" + emotionType + ".txt")));
			
			String line = "";
			int num = 0;
			double feature[][] = new double[100000][10];
			double average[] = new double[10];
			double var[] = new double[10];
			for (int i = 0; i < 10; i ++) {
				average[i] = 0.0;
				var[i] = 0.0;
			}
			
			while ((line = br.readLine()) != null) {
				String part[] = line.split(" ");
				for (int i = 0; i < 10; i ++) {
					double value = Double.parseDouble(part[i]);
				    feature[num][i] = value;
				    average[i] += value;
				}
				num ++;
			}
			
			for (int i = 0; i < 10; i ++) {
				average[i] /= num;
			    bw.append(average[i] + " ");
			    bw.flush();
			}
			bw.append("\n");
			bw.flush();
			
			for (int i = 0; i < 10; i ++) {
				for (int j = 0; j < num; j ++) {
				    var[i] += (feature[j][i] - average[i]) * (feature[j][i] - average[i]);
				}
				var[i] /= num;
				var[i] = Math.sqrt(var[i]);
				bw.append(var[i] + " ");
				bw.flush();
			}
			bw.append("\n");
			bw.flush();
			
			br.close();
			bw.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testnothing(){
		Map<String, List<String>> test = new HashMap<String, List<String>>();
		List<String> a = new ArrayList<String>();
		a.add("aaa");
		a.add("bbb");
		test.put("a", a);
		List<String> b = test.get("a");
		b.add("bb");//�������ԣ��ò����Ѿ��޸���map test����
		System.out.println("after change the List a in test");
		System.out.println(test.get("a").toString());
		
		String dString = "20100805051417";
		int date = Integer.parseInt(dString.substring(0, 8));//bug for out of range����̫�󣬲���int��Χ��
		System.out.println(date);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		try{
			Date date2 = df.parse("20100101");
			String formatDate = df.format(date2);
			System.out.println("date2 " + date2);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	// David Ding
	// ��ȡͼƬ��Ϣ����ȡ����С���ʱ���ͼƬ��������id��������͡�������id������ʱ�䡢Ⱥ����Ϣ������ļ�
	public static void getImageStatics_dxh() {
		try {
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getImageStatics_dxh/qualifiedImagesId&emotionType&userAlias&date2&group.txt")));
			
			List<String> userAliases = new ArrayList<String>();
			
			// ��������е�ͼƬ
			DBCollection collection = db.getCollection("imgwordscores");
			DBCollection imgCollection = db.getCollection("images");
			DBObject query = new BasicDBObject("bestidx", new BasicDBObject("$gt", -1));
			DBObject field = new BasicDBObject("bestidx", true);
			DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			int num = 0;
			int []fre = {0, 0, 0, 0, 0, 0};
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				String imageId = dbo.get("_id").toString();
				String emotionType = dbo.get("bestidx").toString();
				// ���з���ʱ���ͼƬ��һ���з����ߣ�
				BasicDBList condList = new BasicDBList(); 
				DBObject cond1 = new BasicDBObject("_id", new BigInteger(imageId, 10).longValue());
				DBObject cond2 = new BasicDBObject("exif.datt", new BasicDBObject("$exists", true));
				condList.add(cond1);
				condList.add(cond2);
				BasicDBObject searchCond = new BasicDBObject();
				searchCond.put("$and", condList);
				DBObject imgField = new BasicDBObject();
				//imgField.put("ownid", true);
				//imgField.put("exif.datt", true);
				//note: imgField�Ƕ�find��ѯ�����ƣ�Ҳʹ�ò�ѯ�����ĿimgDbo.get()���������ȡ
				DBCursor imgCursor = imgCollection.find(searchCond, imgField);
				while (imgCursor.hasNext()) {
					DBObject imgDbo = imgCursor.next();
					String user = imgDbo.get("ownid").toString();
					String qualifiedImageId = imgDbo.get("_id").toString();
					Date date = (Date) ((DBObject)imgDbo.get("exif")).get("datt");
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
					String formatDate = df.format(date);
					List gr = (List) imgDbo.get("groups");
					if (!userAliases.contains(user)) {
						userAliases.add(user);
					}
					bw.append(qualifiedImageId + " " + emotionType + " " + user + " " + formatDate);
					//wenjing add , get group list
					for(int i = 0; i < gr.size(); i++){
						String gString = gr.get(i).toString();
						bw.append(" " + gString);
					}
					bw.append("\n");
					num ++;
					fre[Integer.parseInt(emotionType)] ++;
					if (num % 100 == 0) {
						System.out.println(num);
					}
				}
			}
			bw.close();
			db.cleanCursors(true);
			System.out.println("num: " + num);
			System.out.println("size: " + userAliases.size());
			System.out.println("fre: " + fre[0] + " " +  fre[1] + " " + fre[2] + " " + fre[3] + " " + fre[4] + " " + fre[5]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//���н��������ļ���ͬʱ�����num:218816; size:2605; fre:101189 21169 17491 11571 37791 29605
	
	//David Ding
	//��ȡ�û�֮������ܶȣ��Ȼ�ȡ�û�֮�佻���������洢��ȫ����������
	public static void getIntimacy() {
		try{
			Mongo mongo = new Mongo("166.111.139.7:27018");
			DB db = mongo.getDB("flickr");
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics/userAlias.txt")));
			BufferedReader brr = new BufferedReader(new FileReader(new File("output/Groups/groupUsersConnect.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getImageStatics_dxh/Intimacy.txt")));
			BufferedWriter bww = new BufferedWriter(new FileWriter(new File("output/getImageStatics_dxh/groupIntimacy.txt")));
			// �����û����ϵ�id-alias
			List <String> userAliasList = new ArrayList<String>();
			String line = "";
			while ((line = br.readLine()) != null) {
				userAliasList.add(line);
			}
			br.close();
			
			int userNum = userAliasList.size();
			int [][] userComtsCount = new int[userNum][userNum];	//�洢�û�֮���໥�����Ĵ���
			
			
			// ��������е�ͼƬ
			DBCollection collection = db.getCollection("imgwordscores");
			DBCollection imgCollection = db.getCollection("images");
			DBObject query = new BasicDBObject("bestidx", new BasicDBObject("$gt", -1));
			DBObject field = new BasicDBObject("bestidx", true);
			DBCursor cursor = collection.find(query, field).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			int num = 0;
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				String imageId = dbo.get("_id").toString();
				String emotionType = dbo.get("bestidx").toString();
				// ��������һ����������ʱ������۵�ͼƬ
				BasicDBList condList = new BasicDBList(); 
				DBObject cond1 = new BasicDBObject("_id", new BigInteger(imageId, 10).longValue());
				DBObject cond2 = new BasicDBObject("comts", new BasicDBObject("$exists", true));
				condList.add(cond1);
				condList.add(cond2);
				BasicDBObject searchCond = new BasicDBObject();
				searchCond.put("$and", condList);
				DBObject imgField = new BasicDBObject();
				imgField.put("ownid", true);
				imgField.put("comts", true);
				DBCursor imgCursor = imgCollection.find(searchCond, imgField);
				while (imgCursor.hasNext()) {
					DBObject imgDbo = imgCursor.next();
					String imageOwnerAlias = imgDbo.get("ownid").toString();
					String qualifiedImageId = imgDbo.get("_id").toString();
					// �õ������б�
					List commentList = (List) imgDbo.get("comts");
					for (int i = 0; i < commentList.size(); i ++) {
						BasicBSONObject comment = (BasicBSONObject) commentList.get(i);
						// ������� ��������������з���ʱ���ͼƬ�� �û������ۣ�����һ������
						// ��������߾��Ƿ����ߣ�����һ������
						String commentMakerAlias = comment.get("ownid").toString();
						if (!userAliasList.contains(commentMakerAlias) || commentMakerAlias.equals(imageOwnerAlias)) {
							continue;
						}
						int placeOwner = userAliasList.indexOf(imageOwnerAlias);
						int placeComt = userAliasList.indexOf(commentMakerAlias);
						if (placeOwner == -1 || placeComt == -1){
							continue;
						}else{
							userComtsCount[userAliasList.indexOf(imageOwnerAlias)][userAliasList.indexOf(commentMakerAlias)] ++;
						}
					}
					num ++;
				}
				if (num % 1000 == 0) {
					System.out.println(num);
				}
			}
			String str = "";
			while((str = brr.readLine()) != null){
				String p[] = str.split(" ");
				int size = p.length;
				for(int i=0; i<size; ++i){
					bww.append(p[i]+" ");
				}
				bww.append("\n");
				for(int i=1; i<size; ++i){
					for(int j=1; j<size; ++j){
						if(i==j){
							bww.append(0+" ");
							continue;
						}
						int place1 = userAliasList.indexOf(p[i]);
						int place2 = userAliasList.indexOf(p[j]);
						if(place1 == -1 || place2 == -1){
							bww.append(0+" ");
						}else{
							//bww.append(userComtsCount[place1][place2]+" ");
							int intimacy = userComtsCount[place1][place2];
							if(intimacy == 0){
								bww.append(0+" ");
							}else if(intimacy < 10 && intimacy > 0){
								bww.append(1+" ");
							}else if(intimacy >= 10 && intimacy < 20){
								bww.append(2+" ");
							}else if(intimacy >= 20 && intimacy < 30){
								bww.append(3+" ");
							}else{
								bww.append(4+" ");
							}
						}
					}
					bww.append("\n");
				}
				for(int i = 1; i < size; i++)
					str = brr.readLine();
			}
			for(int i=0; i<userNum; ++i){
				for(int j=0; j<userNum; ++j){
					bw.append(userComtsCount[i][j] + " ");
				}
				bw.append("\n");
			}
			bw.close();
			brr.close();
			bww.close();
			db.cleanCursors(true);
			System.out.println("num: " + num);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//David Ding
	//��ʹ�ſ���ʱ�����ƣ�ֻ�����Ӻܶ�groupimpact = 1��weight edge
	public static void getFGMdataGroup_dxh(int trainAndPredictNum){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/baseline_8_" + trainAndPredictNum + "_less10_withoutE.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/userIdused.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/Groups/groupUserImages&Date.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/group1_8_groupimpact123_time1day_" + trainAndPredictNum + "_less10_withoutE.txt")));
			//0.123��ʾȺ��impact�ֳ�0.1 0.2 0.3
			//key is img id, value is index(1, 2, ...)
			Map<String, String> userId = new HashMap<String, String>();
			String line = "";
			while((line = br2.readLine())!= null){
				String p[] = line.split(" ");
				//System.out.println("line "+ line);
				userId.put(p[0], p[1]);
			}
			
			System.out.println("userId size : " + userId.size());//35089
			
			//ÿ��Ⱥ���ͼƬ��key��group, value ��img#img#img...
			Map<String, String> groupimgs = new HashMap<String, String>();
			//ÿ��ͼƬ����Ⱥ������������ʱ�䣬key is image id, value :publishdate#group#group2#group3...
			Map<String, String> imgsgroupDate = new HashMap<String, String>();
			while((line = br3.readLine())!= null){//for a group
				String pp[] = line.split(" ");
				String imgs = "";
				for(int i = 0; i < Integer.valueOf(pp[1]); i++){//for a user
					line = br3.readLine();
					String part[] = line.split(" ");
					for(int j = 1; j < part.length; j += 2){
						if(!imgsgroupDate.containsKey(part[j])){
							imgsgroupDate.put(part[j], part[j+1]+"#"+pp[0]);//ÿ��ͼƬ���Ⱥ����ô�죿
						}else{
							String dategs = imgsgroupDate.get(part[j]);
							dategs = dategs + "#" + pp[0];
							imgsgroupDate.put(part[j], dategs);
						}
						imgs = imgs + "#" + part[j];
					}
				}
				groupimgs.put(pp[0], imgs.substring(1, imgs.length()));
			}
			
			System.out.println("num of groups " + groupimgs.size());
			System.out.println("images of 60356848@N00 " + groupimgs.get("60356848@N00"));
			
			System.out.println("lineDict Size: " + userId.size());
			
			int num = 0;
			int w = 0;
			//ÿ��ͬȺ���û���������ͬȺ������key��lineId#imgs.get(img),value��������������ͬ��Ⱥ����Ŀ
			Map<String, Integer> imgGnum = new HashMap<String, Integer>();
			while((line = br.readLine()) != null){
				//if(!(line.split(" ")[0].equals("#weight_edge"))){
					//bw.append(line+"\n");//readline()�Զ���ȥ���з�
					//continue;
			}
			//bw.append(line+"\n");
			//}
				//System.out.println("same group");
				//same group
				Iterator iter = userId.entrySet().iterator(); 
				while (iter.hasNext()) { 
				    Map.Entry entry = (Map.Entry) iter.next();
				    String lineId = entry.getValue().toString();
				    String imageId = entry.getKey().toString();
				    //System.out.println("imageis "+imageId);
				    if(imgsgroupDate.containsKey(imageId)){//for each image
				    	String dategs[] = imgsgroupDate.get(imageId).split("#");
				    	String dateString= dategs[0];
				    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
						Date publishDate = df.parse(dateString);
						//System.out.println("publish date "+publishDate);
						Calendar rightNow = Calendar.getInstance();
				        rightNow.setTime(publishDate);
				        rightNow.add(Calendar.DATE, -10);//���ڼ�10���� -> ��1��
				        Date leftDate = rightNow.getTime();
				        
				    	for(int i = 1; i < dategs.length; i++){//for a group
				    		String group = dategs[i];
				    		//System.out.println("group "+group);
				    		String imgs[] = groupimgs.get(group).split("#");//��Ⱥ����������û�
				    		for(int u = 0; u < imgs.length; u++){
				    			String img = imgs[u];
				    			String imgDate = imgsgroupDate.get(img).split("#")[0];
				    			Date imageDate = df.parse(imgDate);
				    			//System.out.println("group img:------");
				    			//System.out.println(imageId + " " + img + " " + group);
				    			if(imageId.equals(img))
				    				continue;
				    			//��������ͼƬ��lineNum����һ�����Բ鵽����Ϊ����ͼƬ����û�б����룬Ҳ����û�б�ѡΪѵ��������Լ���ȫ��ʱ��һ�����Բ鵽
				    			if(!userId.containsKey(img))
				    				continue;
				    			 // ����ںϷ���ʱ��������
								if (imageDate.after(leftDate) && imageDate.before(publishDate)) {
									//String output = "#edge " + lineId + " " + userId.get(img) + " samegroup\n";
									//bw.append(output);
									//bw.flush();
									String lineIds = lineId+"#"+userId.get(img);
									if(imgGnum.containsKey(lineIds)){
										int number = imgGnum.get(lineIds);
										number++;
										imgGnum.put(lineIds, number);
									}else{
										imgGnum.put(lineIds,  1);
									}
									num++;
									//System.out.println(imageId + " " + img + " " + group);
									//System.out.println(lineId + " " + userId.get(img) + " " + group);
					    			if(num % 1000 == 0)
					    				System.out.println(num);
					    				//return;
								}
				    		}
				    	}
				    }
				}
				
				System.out.println(num);
				Iterator iter2 = imgGnum.entrySet().iterator();
				while(iter2.hasNext()){
					Map.Entry entry2 = (Map.Entry) iter2.next();
					String lineids[] = entry2.getKey().toString().split("#");
					String numString = entry2.getValue().toString();
					String impact = "";
					if(Integer.valueOf(numString) < 10)
						impact = "1";
					else{
						if(Integer.valueOf(numString) < 20)
							impact = "2";
						else
							impact = "3";
					}
					bw.append("#weight_edge " + lineids[0] + " " + lineids[1] + " groupimpact " + numString + "\n");
					bw.flush();
				}
			//br.close();
			br2.close();
			br3.close();
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//David Ding
	//���ղ�ͬ�߲�ͬȨֵ�޸�groupConnect
	public static void getGroupConnect_dxh() {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getImageStatics_dxh/groupIntimacy.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/Groups_dxh/groupConnect.txt")));
			String str = "";
			while((str = br.readLine()) != null){
				String p[] = str.split(" ");
				int size = p.length;
				bw.append(p[0]+" "+(size-1)+" ");
				int sum = 0;	//��ǰȺ���ڲ�������Ա֮������̶ܳ�֮��
				int max_sum = 0;	//����0���1����ȫ��ͨʱ�����̶ܳ�֮��
				for(int i=1; i<size; i++){
					str = br.readLine();
					String score[] = str.split(" ");
					for(int j=0; j<score.length; ++j){
						//System.out.println(score[j]);
						int sc = Integer.parseInt(score[j]);
						sum += sc;
						if(sc == 0){
							max_sum += 1;
						}else{
							max_sum += sc;
						}
					}
				}
				bw.append(sum+" "+((double)(sum)/(double)(max_sum))+"\n");
			}
			br.close();
			bw.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//David Ding
	//�ú���������FGM������ĵ����Ϣ��
	//��getBasicFGMData4/8֮����޸ģ�����Ⱥ�����أ�����ͼƬ�����ϣ�ÿ��ͼƬ����Ⱥ����Ŀ���ͷ��ϵ�Ⱥ�����ԣ�social role������С - group emotion��
	//�����û�opinion leader��Ϣ��
	//���ӵ��ڲ�����percent���ж�ol��������ֵ,���������û��Ƿ�ol,��������ÿ��Ⱥ���ol����, minRatio�Ǹ���ol�����ж�ÿ��Ⱥ��ķ��ࡣ
	//�����Ӳ�����contactRatio:����ÿ��Ⱥ�����ͨ�ȴ�С����Ⱥ�����  ---2017-5-8
	//ȥ���û�opinion leader��Ϣ 2017-5-10
	//����structure hole spanner, percentҲ��sh spanner�ı�����ֵ
	//																																						percent=28, minRatio=18, contactRatio=7, shRatio=10, tAP=6000
	public static void getFGMDataGroup2_dxh(int trainAndPredictNum, int percent, int minRatio, float contactRatio, int shRatio){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/baseline_8_" + trainAndPredictNum + "_less10_withoutE.txt")));
			BufferedReader br2 = new BufferedReader(new FileReader(new File("output/getBasicFGMData/wenjing/userIdused.txt")));
			BufferedReader br3 = new BufferedReader(new FileReader(new File("output/Groups/groupUserImages&Date.txt")));
			BufferedReader br4 = new BufferedReader(new FileReader(new File("output/Groups/groupsEmotionRatioDate.txt")));
			BufferedReader br5 = new BufferedReader(new FileReader(new File("output/opinionLeader/userOpinionleaders_" +percent + ".txt")));
			BufferedReader br6 = new BufferedReader(new FileReader(new File("output/opinionLeader/groupOLratio_" + percent + ".txt")));
			
			BufferedReader br7 = new BufferedReader(new FileReader(new File("output/Groups_dxh/groupConnect.txt")));
			//BufferedReader br7 = new BufferedReader(new FileReader(new File("output/Groups/groupEdgeConnectivity.txt")));//�޸�Ⱥ����ͨ�ȵĶ��壬���ļ���¼ÿ��Ⱥ��ı���ͨ��
			BufferedReader br8 = new BufferedReader(new FileReader(new File("output/SHspanner/userSHspanner_" + percent + ".txt")));//user and 1(yes) / 0(no)
			BufferedReader br9 = new BufferedReader(new FileReader(new File("output/SHspanner/groupSHratio_" + percent + ".txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/DavidDing/shRatio/group1_8_groupemotion_minpicture6_shRatio_opinion01_shspanner01_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "_shRatio_" + shRatio + "_" + trainAndPredictNum + "_less10_withoutE.txt")));
			//ȥ��ol 
			//BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output/getBasicFGMData/wenjing/group1_8_groupemotion_minpicture6_opinionpercent_" + percent+ "_minRatio_" + minRatio+ "_contactRatio_" + contactRatio + "_" + trainAndPredictNum + "_less10_withoutE.txt")));
			
			//key is img id, value is index(1, 2, ...)
			Map<String, String> userId = new HashMap<String, String>();
			//key is index, value is img id
			Map<String, String> idImg = new HashMap<String, String>();
			String line = "";
			while((line = br2.readLine())!= null){
				String p[] = line.split(" ");
				//System.out.println("line "+ line);
				userId.put(p[0], p[1]);
				idImg.put(p[1], p[0]);
			}
			
			System.out.println("userId size : " + userId.size());//35089
			
			//key is user alias, value is 0 or 1, 0 is not opinion leader, 1 is ol.
			Map<String, String> userOpinionleader = new HashMap<String, String>();
			while((line = br5.readLine()) != null){
				String p2[] = line.split(" ");
				userOpinionleader.put(p2[0], p2[1]);
			}
			System.out.println("num of user " + userOpinionleader.size());
			
			//--- ����Ⱥ��pagerank ��������Ⱥ�����
			//key is group id, value is the ol ratio 
			Map<String, String> groupOLratio = new HashMap<String, String>();
			//key is group id, value is 1  or 0;
			Map<String, String> groupType = new HashMap<String, String>();
			while ((line = br6.readLine()) != null) {
				String p3[] = line.split(" ");
				String gid = p3[0];
				String ratio = p3[3];
				groupOLratio.put(gid, ratio);
				if(Float.valueOf(ratio) > (minRatio*1.0/100))
					groupType.put(gid, "1");
				else
					groupType.put(gid, "0");
				
			}
			
			//��ȡÿ��Ⱥ�����ͨ�ȣ�����Ⱥ�����
			Map<String, String> groupConnectType = new HashMap<String, String>();
			while((line = br7.readLine()) != null){
				String p7[] = line.split(" ");
				String gid = p7[0];
				// ֮ǰ����ͨ��
				Float ratio = Float.valueOf(p7[3]);
				if(ratio >= (contactRatio * 1.0 / 100))
					groupConnectType.put(gid, "1");
				else
					groupConnectType.put(gid, "0");
				
				//����ͨ��
				/*
				int edgeC = Integer.valueOf(p7[1]);
				if(edgeC == 0)
					groupConnectType.put(gid, "0");
				else 
					groupConnectType.put(gid, "1");
					*/
			}
			System.out.println("sized of groupConnectType " + groupConnectType.size());
			//��ͨ�� end
			
			//if user is a sh spanner or not   <user, 1 / 0>
			Map<String, String> userSH = new HashMap<String, String>();
			while((line = br8.readLine()) != null){
				String p8[] = line.split(" ");
				userSH.put(p8[0], p8[1]);
			}
			//���� shRatio��Ⱥ��sh���࣬ key is gid , value is 1 or 0
			Map<String, String> groupSHtype = new HashMap<String, String>();
			while((line = br9.readLine()) != null){
				String p9[] = line.split(" ");
				String gid = p9[0];
				Float ratio = Float.valueOf(p9[3]);
				if(ratio >= (shRatio * 1.0) / 100)
					groupSHtype.put(gid, "1");
				else 
					groupSHtype.put(gid, "0");
			}
			//sh end
			//ÿ��ͼƬ�ķ����ߣ�key is img , value is user alias
			Map<String, String> imgUser = new HashMap<String, String>();
			
			//ÿ��Ⱥ���ͼƬ��key��group, value ��img#img#img...
			Map<String, String> groupimgs = new HashMap<String, String>();
			//ÿ��ͼƬ����Ⱥ������������ʱ�䣬key is image id, value :publishdate#group#group2#group3...
			Map<String, String> imgsgroupDate = new HashMap<String, String>();
			while((line = br3.readLine())!= null){//for a group
				String pp[] = line.split(" ");
				String imgs = "";
				for(int i = 0; i < Integer.valueOf(pp[1]); i++){//for a user
					line = br3.readLine();
					String part[] = line.split(" ");
					for(int j = 1; j < part.length; j += 2){
						if(!imgsgroupDate.containsKey(part[j])){
							imgsgroupDate.put(part[j], part[j+1]+"#"+pp[0]);//ÿ��ͼƬ���Ⱥ����ô�죿
						}else{
							String dategs = imgsgroupDate.get(part[j]);
							dategs = dategs + "#" + pp[0];
							imgsgroupDate.put(part[j], dategs);
						}
						imgs = imgs + "#" + part[j];
						
						if(!imgUser.containsKey(part[j]))//ÿ��ͼƬֻ��һ���û�
							imgUser.put(part[j], part[0]);
					}
				}
				groupimgs.put(pp[0], imgs.substring(1, imgs.length()));
			}
			
			System.out.println("num of img with user " + imgUser.size());
			System.out.println("num of groups " + groupimgs.size());
			System.out.println("images of 60356848@N00 " + groupimgs.get("60356848@N00"));
			
			System.out.println("lineDict Size: " + userId.size());
			
			//groupEmotionRatioDate,key is group id, value is date#emotion#ratio list
			Map<String, List<String>> groupDateEmotionRatio = new HashMap<String, List<String>>();
			while((line = br4.readLine()) != null){
				String part4[] = line.split(" ");
				String group = part4[0];
				if(Integer.valueOf(part4[3]) < 3)//�ö�ʱ�䷢����ͼƬ���� ��С������ȥ
					continue;
				if(groupDateEmotionRatio.containsKey(group)){
					List glist = groupDateEmotionRatio.get(group);
					glist.add(part4[1]+"#"+part4[2]+"#"+part4[4]);//date emotion ratio
					groupDateEmotionRatio.put(group, glist);
				}else{
					List<String> gList = new ArrayList<String>();
					gList.add(part4[1]+"#"+part4[2]+"#"+part4[4]);
					groupDateEmotionRatio.put(group, gList);
				}
			}
			
			System.out.println(groupDateEmotionRatio.get("360134@N24"));
			
			int num = 0;
			int w = 0;
			int index = 1;//��1��ͼƬ
			int maxSize = 0;
			while((line = br.readLine()) != null){
				if((line.split(" ")[0].equals("#weight_edge")) || (line.split(" ")[0].equals("#edge"))){
					bw.append(line+"\n");//readline()�Զ���ȥ���з�
					continue;
				}
				//��ÿ��ͼƬ����
				String img = idImg.get(String.valueOf(index));
				index++;
				if(!imgsgroupDate.containsKey(img)){//��ͼƬû��Ⱥ����
					bw.append(line+"\n");
					continue;
				}//line���һ���ַ��ǿո�����������ܶ���ո�
				String groupdate[] = imgsgroupDate.get(img).split("#");//imgsgroupDate��value��date#group#group2..
				int groupNum = groupdate.length - 1;//��ͼƬ���ڵ�Ⱥ����Ŀ�Ƿָ�֮��groupdate�ĳ��ȼ�һ����ȥʱ����ռһ�����ȣ�
				if(groupNum > maxSize)
					maxSize = groupNum;
				if(groupNum > 0)
					line = line + " groupSize:" + groupNum*1.0/151;//������size,��groupSize��һ�� maxSize=151��6000����11500ʱ191
				
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date publishDate = df.parse(groupdate[0]);//ͼƬ����ʱ��
				
				int maxEmotion = -1;
				float maxRatio = 0;
				//Ⱥ����࣬�Ȳ���social role, �ȼ���Ⱥ����Ҫ��У�����groupEmotionRatioDate.txt
				for(int i = 0; i < groupNum; i++){// for each group
					String gString = groupdate[i+1];
					if(!groupDateEmotionRatio.containsKey(gString))
						continue;
					//System.out.println("has group");
					
					List<String> dateEmotionRatio = groupDateEmotionRatio.get(gString);
					for(int d = 0; d < dateEmotionRatio.size(); d++){
						String der[] = dateEmotionRatio.get(d).split("#");
						Date groupDate = df.parse(der[0]+"000000");
						float ratio = Float.valueOf(der[2]);
						int emotion = Integer.valueOf(der[1]);
						
						Calendar rightNow = Calendar.getInstance();
				        rightNow.setTime(groupDate);
				        rightNow.add(Calendar.DATE, 14);//���ڼ�14�죬���ܣ���Ϊ֮ǰ��emotion ratio��ʱ��Ƭ�������ܳ�
				        Date rightDate = rightNow.getTime();
				        //�Ϸ�ʱ��
				        if(groupDate.before(publishDate) && rightDate.after(publishDate)){
				        	if(ratio > maxRatio){
				        		maxRatio = ratio;
				        		maxEmotion = emotion;
				        	}
				        }
					}
				}
				if(maxEmotion > -1){
					line = line + " groupEmotion:" + maxEmotion;
					num++;
				}
				/*
				//����opinion leader
				if(imgUser.containsKey(img) && userOpinionleader.containsKey(imgUser.get(img))){
					line = line + " opinionleader:" + userOpinionleader.get(imgUser.get(img));
				}
				//���� sh spanner
				if(imgUser.containsKey(img) && userSH.containsKey(imgUser.get(img))){
					line = line + " shspanner:" + userSH.get(imgUser.get(img));
				}
				*/
				//group Type
				int num0 = 0;//type = 0 �ĸ���
				int num1 = 0;//type = 1 �ĸ���
				for(int i = 0; i < groupNum; i++){// for each group
					String gString = groupdate[i+1];
					if(!groupType.containsKey(gString))
						continue;
					if(groupType.get(gString).equals("1"))
						num1++;
					else {
						num0++;
					}
				}
				if(!(num0 == 0 && num1 == 0)){
					if(num1 >= num0)
						line = line + " groupType:" + "1";
					else
						line = line + " groupType:" + "0";
				}
				//����sh spanner��Ⱥ��ķ���
				int snum0 = 0;//type = 0 �ĸ���
				int snum1 = 0;//type = 1 �ĸ���
				for(int i = 0; i < groupNum; i++){// for each group
					String gString = groupdate[i+1];
					if(!groupSHtype.containsKey(gString))
						continue;
					if(groupSHtype.get(gString).equals("1"))
						snum1++;
					else {
						snum0++;
					}
				}
				if(!(snum0 == 0 && snum1 == 0)){
					if(snum1 >= snum0)
						line = line + " groupSHType:" + "1";
					else
						line = line + " groupSHType:" + "0";
				}
				//���ϸ�����ͨ�ȶ�Ⱥ��ķ���
				int cnum0 = 0;
				int cnum1 = 0;
				for(int i = 0; i < groupNum; i++){
					String gString = groupdate[i+1];
					if(!groupConnectType.containsKey(gString))
						continue;
					if(groupConnectType.get(gString).equals("1"))
						cnum1++;
					else
						cnum0++;
				}
				if(!(cnum0 == 0 && cnum1 == 0)){
					if(cnum1 >= cnum0)
						line = line + " groupConnectType:" + "1";
					else
						line = line + " groupConnectType:" + "0";
				}
				//��ͨ��end
				bw.append(line+"\n");
				
			}
			System.out.println("maxSize " + maxSize);
			System.out.println(num);
			
			br.close();
			br2.close();
			br3.close();
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//Factor analyse
	//��ģ�������ļ�group1_8_groupemotion_minpicture6_opinion01_shspanner01_28_minRatio_18_contactRatio_7_shRatio_10_6000_less10_withoutE.txt���д���
	//6/5, ����ȥ����social role���������ӷ����������ļ�����ԭ���ģ����ԣ�Ҫ�����е�����ļ���ȥ��social role
	//7/11���¹���
	public static void factorAnalyse_dxh(int srRatio, int minRatio, int contactRatio, int shRatio){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/DavidDing/delete_ol_sh/group1_8_groupemotion_minpicture6_delete_ol_sh_opinion01_shspanner01_" + srRatio + "_minRatio_" + minRatio + "_contactRatio_" + contactRatio + "_shRatio_" + shRatio + "_6000_less10_withoutE.txt")));
			
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.12_delete_ol_sh/" + srRatio + minRatio + contactRatio + shRatio + "davidding-groupConnect.txt")));
			
			String line = "";
			String str1 = "";
			//7/11	DavidDing ֻ����groupConnectType
			String type_dxh = "groupConnectType";
			//String types[] = {"gender", "marital", "occupation", "friendSize", "friendEmotion", "groupSize", "groupEmotion", "groupType", "groupSHType", "groupConnectType"};//"opinionleader", "shspanner",֮ǰ���е�
			while((line = br.readLine()) != null){
				String p[] = line.split(" "); //p[0] is label; p[1-25] is f1; 
				//p[26-34]  others
				if(p.length < 6){
					bw1.append(line + "\n");
					continue;
				}
				String label = p[0];
				int i = 0;
				str1 = label;
				for(i = 1; i < p.length; i++){
					if(p[i].length() <= 1)
						continue;
					String attri[] = p[i].split(":");
					if(attri.length < 2){
						System.out.println("maybe something error!");
						continue;
					}
					String attriType = attri[0];
					String attriValue = attri[1];
					if(attriType.equals(type_dxh))
						continue;
					str1 += " " + attriType + ":" + attriValue;
				}
				bw1.append(str1+"\n");
			}
			br.close();
			bw1.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//�������ں���֮��Factor analyse
	//��ģ�������ļ�group1_8_groupemotion_minpicture6_shRatio_opinion01_shspanner01_18_minRatio_20_contactRatio_4.0_shRatio_12_6000_less10_withoutE.txt���д���
	//2017/7/17
	public static void factorAnalyse_dxh_new(int srRatio, int minRatio, float contactRatio, int shRatio){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("output/getBasicFGMData/DavidDing/shRatio/group1_8_groupemotion_minpicture6_shRatio_opinion01_shspanner01_" + srRatio + "_minRatio_" + minRatio + "_contactRatio_" + contactRatio + "_shRatio_" + shRatio + "_6000_less10_withoutE.txt")));
			
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f1.txt")));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f2.txt")));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f3.txt")));
			BufferedWriter bw4 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f4.txt")));
			BufferedWriter bw5 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f5.txt")));
			BufferedWriter bw6 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f6.txt")));
			BufferedWriter bw7 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f7-groupsize-emotion.txt")));
			BufferedWriter bw8 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f7-groupsocialrole.txt")));
			BufferedWriter bw9 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f7-groupconnect.txt")));
			BufferedWriter bw10 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole-f7.txt")));
			BufferedWriter bw11 = new BufferedWriter(new FileWriter(new File("output/factorAnalyse_dxh/2017.7.17/" + srRatio + minRatio + contactRatio + shRatio + "davidding-socialrole.txt")));
			
			
			String line = "";
			String str1 = "";
			String str2 = "";//ʱ���ԣ���ȥsame user ��
			String str3 = "";
			String str4 = "";
			String str5 = "";
			String str6 = "";
			String str7 = "";
			String str8 = "";
			String str9 = "";
			String str10 = "";
			String str11 = "";
			/*
			 *featureId.put("gender", "26");
			featureId.put("marital", "27");
			featureId.put("occupation", "28");
			featureId.put("friendSize", "29");//f3,num of friends
			featureId.put("friendEmotion", "30");//f4 , emotion of friends, f5 is weight edge, 
			featureId.put("groupSize", "31");
			featureId.put("groupEmotion", "32");
			featureId.put("opinionleader", "33");//social role
			featureId.put("groupType", "34");
			featureId.put("groupConnectType", "35");
			 **/
			String types[] = {"gender", "marital", "occupation", "friendSize", "friendEmotion", "groupSize", "groupEmotion", "groupType", "groupSHType", "groupConnectType"};//"opinionleader", "shspanner",֮ǰ���е�
			while((line = br.readLine()) != null){
				String p[] = line.split(" "); //p[0] is label; p[1-25] is f1; 
				if(p.length < 6){
					bw1.append(line + "\n");
					bw3.append(line + "\n");
					bw4.append(line + "\n");
					bw6.append(line + "\n");
					bw7.append(line + "\n");
					bw8.append(line + "\n");
					bw9.append(line + "\n");
					bw10.append(line + "\n");
					bw11.append(line + "\n");
					if(!(p[0].equals("#edge"))){
						bw2.append(line+"\n");
					}else{
						bw5.append(line+"\n");
					}
					continue;
				}
				String label = p[0];
				int i = 0;
				str1 = label;
				str2 = label;
				str3 = label;
				str4 = label;
				str5 = label;
				str6 = label;
				str7 = label;
				str8 = label;
				str9 = label;
				str10 = label;
				str11 = label;
				for(i = 1; i < 26; i++){
					str2 = str2 + " " + p[i];
					str3 = str3 + " " + p[i];
					str4 = str4 + " " + p[i];
					str5 = str5 + " " + p[i];
					str6 = str6 + " " + p[i];
					str7 = str7 + " " + p[i];
					str8 = str8 + " " + p[i];
					str9 = str9 + " " + p[i];
					str10 = str10 + " " + p[i];
					str11 = str11 + " " + p[i];
					
				}
				Map<String, String> factor = new HashMap<String, String>();//key is attri type, value is attri value
				for(i = 26; i < p.length; i++){
					if(p[i].length() <= 1)
						continue;
					String attri[] = p[i].split(":");
					if(attri.length < 2){
						System.out.println("maybe something error!");
						continue;
					}
					String attriType = attri[0];
					String attriValue = attri[1];
					factor.put(attriType, attriValue);
				}
				//write bw1
				Iterator iter = factor.entrySet().iterator();
				for(i = 0; i < types.length; i++) { 
					String type = types[i];
					if(!factor.containsKey(type))
						continue;
					String value = factor.get(type);
					str1 = str1 + " " + type + ":" + value;
					str2 = str2 + " " + type + ":" + value;//�Ѽ�ȥsame user
					str5 = str5 + " " + type + ":" + value;//�Ѽ�ȥ weight edge
					
					
					if(!(type.equals("gender") || type.equals("marital") || type.equals("occupation")))
						str6 = str6 + " " + type + ":" + value;
					if(!(type.equals("friendSize"))){
						str3 = str3 + " " + type + ":" + value;
					}
					if(!(type.equals("friendEmotion")))
						str4 = str4 + " " + type + ":" + value;
					if(!(type.equals("groupSize") || type.equals("groupEmotion")))
						str7 = str7 + " " + type + ":" + value;
					if(!(type.equals("groupType") || type.equals("groupSHType")))
						str8 = str8 + " " + type + ":" + value;
					if(!(type.equals("groupConnectType")))
						str9 = str9 + " " + type + ":" + value;
					if(!(type.equals("groupSize") || type.equals("groupEmotion") || type.equals("groupType") || type.equals("groupSHType") || type.equals("groupConnectType")))
						str10 = str10 + " " + type + ":"+ value;
					if(!(type.equals("opinionleader") || type.equals("shspanner")))
						str11 = str11 + " " + type + ":"+ value;
				}
				bw1.append(str1 + "\n");
				bw2.append(str2 + "\n");
				bw3.append(str3 + "\n");
				bw4.append(str4 + "\n");
				bw5.append(str5 + "\n");
				bw6.append(str6 + "\n");
				bw7.append(str7 + "\n");
				bw8.append(str8 + "\n");
				bw9.append(str9 + "\n");
				bw10.append(str10 + "\n");
				bw11.append(str11 + "\n");
			}
			br.close();
			bw1.close();
			bw2.close();
			bw3.close();
			bw4.close();
			bw5.close();
			bw6.close();
			bw7.close();
			bw8.close();
			bw9.close();
			bw10.close();
			bw11.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// [experiment]
		//new ColorThemeExtractor().getImageStatics();
		//new ColorThemeExtractor().getGroupUserImages();
		//new ColorThemeExtractor().testnothing();
		//new ColorThemeExtractor().getUserContacts();
		//new ColorThemeExtractor().getUserConnect();
		//new ColorThemeExtractor().getEmotionRatioDate();
		//new ColorThemeExtractor().getGroupUsersConnect();
		//new ColorThemeExtractor().getImgEmotionGroup();
		//new ColorThemeExtractor().getImgUrl();
		//new ColorThemeExtractor().getStimuli();
		//new ColorThemeExtractor().stimulateResult();
		//new ColorThemeExtractor().getUserTemporalEmotionType();
		//new ColorThemeExtractor().getContact2();
		//new ColorThemeExtractor().getBasicFGMData4(6000);
		//new ColorThemeExtractor().getFGMdataGroup(6000); //wenjing--2
		//new ColorThemeExtractor().getFGMDataGroup2(6000, 18, 18, 7, 10);//wenjing --3. ��֤group emotion��ʵ���Ӱ��  -- 6��ʾ�ж�opiinion leaderʱ��������0.06�� 50��ʾol ratio >= 0.5��Ⱥ��type = 1
		                                                             //wenjing --4 ���ӵ��ĸ�����
		//new ColorThemeExtractor().getBasicFGMData6(11500);
		//new ColorThemeExtractor().getBasicFGMData6((int) (11500 * 0.2));
		//new ColorThemeExtractor().getBasicFGMData6((int) (11500 * 0.4));
		//new ColorThemeExtractor().getBasicFGMData6((int) (11500 * 0.6));
		//new ColorThemeExtractor().getBasicFGMData6((int) (11500 * 0.8));
		//new ColorThemeExtractor().getBasicFGMData7(0.2);
		//new ColorThemeExtractor().getBasicFGMData8(6000);//wenjing--1
		//new ColorThemeExtractor().getSVMData3(11500);
		//new ColorThemeExtractor().getSVMdata4(6000, 28, 15, 5);//wenjing SVM
		//new ColorThemeExtractor().getNDdata(11500, 28, 18, 7);//wenjing NB �����11500��С�����ݼ�����Ҫ�ȵõ�FGM��ʽ�ģ�getBasicFGMData8(11500)+getFGMDataGroup2��11500��28��18��7������
												//��Ϊ�ú����Ǹ���FGM��ʽ�޸ĵģ���������Ҫ��ô��������6000�͹���
		//new ColorThemeExtractor().getNaiveBayesData(11500);
		//new ColorThemeExtractor().analyzeSVMResult(11500);
		//new ColorThemeExtractor().analyzeNaiveBayesResult(11500);
		//new ColorThemeExtractor().factorAnalyse(30, 18, 7, 10);//wenjing
		//new ColorThemeExtractor().getImageStatics_dxh();//David Ding
		//new ColorThemeExtractor().getIntimacy();//David Ding
		//new ColorThemeExtractor().getGroupConnect_dxh();//David Ding
		//new ColorThemeExtractor().getFGMDataGroup2_dxh(6000, 18, 20, (float)4, 12);//David Ding
		new ColorThemeExtractor().factorAnalyse_dxh_new(18, 20, (float)4, 12);
		//new ColorThemeExtractor().factorAnalyse_dxh(28, 18, 80, 10);
		//new ColorThemeExtractor().testEdge();
		
		// [data observation]
		//new ColorThemeExtractor().getUserProfileStatistics();
		//new ColorThemeExtractor().getSpecialUserImageFeatures();
		/*for (int i = 0; i < 6; i ++) {
			new ColorThemeExtractor().calculateAverageFeature(i);
		}*/
		
		// [case study]
		/*for (int i = 0; i < 6; i ++) {
			for (int j = 0; j < 2; j ++) {
		        new ColorThemeExtractor().pickDemographics(i, "occupation", j);
			}
		}*/
		
		//new ColorThemeExtractor().getContactForAllUser();
	}

}
