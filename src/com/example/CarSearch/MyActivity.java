package com.example.CarSearch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;


public class MyActivity extends Activity
{
	/**
	 * Called when the activity is first created.
	 */

	//private final String mServerAddr = "http://115.47.47.252:8889";
	private final String mServerAddr = "http://10.28.163.92:8889";
	private OkHttpClient mHttpClient = new OkHttpClient();
	private final int HTTP_DO_SEARCH_FINAISHED = 1;
	private final int HTTP_DO_ADD_FINISHED = 2;
	private final int HTTP_DO_DEL_FINISHED = 3;

	private Handler mHttpHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case HTTP_DO_SEARCH_FINAISHED:
				String searchResult = (String)msg.obj;
				showSearchResult(searchResult);
				//Toast.makeText(MyActivity.this, searchResult, Toast.LENGTH_LONG).show();
				break;
			case HTTP_DO_ADD_FINISHED:
				String addResult = (String)msg.obj;
				showAddResult(addResult);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	protected void showAddResult(String result)
	{
		if(result.indexOf("ok") != -1)
		{
			Toast.makeText(this, "添加车辆信息成功", Toast.LENGTH_LONG).show();
			return;
		}
		else if(result.indexOf("error") != -1)
		{
			Toast.makeText(this, "添加车辆信息失败", Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(this, "添加车辆信息异常", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

	}



	protected String getSearchUrl(String baseUrl, String carNo, String houseNo)
	{
		String Url = baseUrl + "?carno="+carNo+"&houseno="+houseNo;
		return Url;
	}

	public void onSearch(View view)
	{
		String url = mServerAddr +"/search";
		String result = "";
		EditText carNo =  (EditText)findViewById(R.id.car_no);
		EditText houseNo = (EditText)findViewById(R.id.house_no);
		String strCarNo = carNo.getText().toString();
		String strHouseNo = houseNo.getText().toString();
		if(strCarNo.isEmpty() && strHouseNo.isEmpty())
		{
			Toast.makeText(this, "关键信息[车牌号/房号]填写不完整", Toast.LENGTH_LONG).show();
			return;
		}
		utils.log("carno is " + strCarNo + " houseno is "+strHouseNo);

		String Url = getSearchUrl(url,strCarNo, strHouseNo);

		utils.log("final url is " + Url);
		doSearch(Url);
		return;
	}


	protected void doSearch(String url)
	{
		Thread searchThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					String searchRet = "";
					Request request = new Request.Builder().url(url).build();
					Response response = mHttpClient.newCall(request).execute();
					searchRet = response.body().string();
					utils.log("request result: "+ searchRet);
					Message msg = mHttpHandler.obtainMessage(HTTP_DO_SEARCH_FINAISHED, searchRet);
					mHttpHandler.sendMessage(msg);
					return;
				}
				catch (Exception e)
				{
					utils.log("!!!!exception :" + e.getMessage());
					e.printStackTrace();
				}
			}
		});
		searchThread.start();
		return;
	}


	protected void showSearchResult(String result)
	{
		if(result.indexOf("error") != -1)
		{
			Toast.makeText(this, "未查找到对应的信息", Toast.LENGTH_LONG).show();
			return;
		}
		ArrayList<HashMap<String, String>> searchContent = new  ArrayList<HashMap<String, String>>();
		JsonReader reader = new JsonReader(new StringReader(result));
		try
		{
			reader.beginObject();
			while(reader.hasNext())
			{
				String key = reader.nextName();
				if(key.equals("ok"))
				{
					reader.beginArray();
					while(reader.hasNext())
					{
						reader.beginObject();
						HashMap<String, String> map = new HashMap<String, String>();
						while(reader.hasNext())
						{
							String tagName = reader.nextName();
							String tagValue = reader.nextString();
							map.put(tagName, tagValue);
						}
						searchContent.add(map);
						reader.endObject();
					}
					reader.endArray();

					if(searchContent.size() > 0)
					{
						Toast.makeText(this, "查找到"+searchContent.size() + " 条记录", Toast.LENGTH_LONG).show();
						ListView lv = (ListView) findViewById(R.id.lst_result);
						if(lv != null)
						{
							lv.setAdapter(new SimpleAdapter(this, searchContent, R.layout.list_view_item,
							                                new String[]{"id", "carno", "ownername", "phoneno", "houseno"},
							                                new int[]{R.id.id, R.id.carno, R.id.ownername, R.id.phoneno, R.id.houseno}));
						}
						return;
					}
					Toast.makeText(this, "查找的信息错误", Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(this, "解析数据失败", Toast.LENGTH_LONG).show();
					return;
				}
			}

		}
		catch (Exception e)
		{
			Toast.makeText(this, "解析数据失败", Toast.LENGTH_LONG).show();
			utils.log("parse failed " + e.getMessage());
			return;
		}


	}

	//get post user info and transfer to json format
	protected String getPostData()
	{
		EditText carNo =  (EditText)findViewById(R.id.car_no);
		EditText name = (EditText)findViewById(R.id.name);
		EditText phoneNo = (EditText)findViewById(R.id.phone_no);
		EditText houseNo = (EditText)findViewById(R.id.house_no);


		JSONObject obj = new JSONObject();
		try
		{
			obj.put("carno", carNo.getText().toString().toUpperCase());
			if(!name.getText().toString().isEmpty())
			{
				obj.put("name", name.getText().toString());
			}

			if(!phoneNo.getText().toString().isEmpty())
			{
				obj.put("phoneno", phoneNo.getText().toString());
			}
			obj.put("houseno", houseNo.getText().toString());
			return obj.toString();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	public void onAdd(View view)
	{
		EditText carNo =  (EditText)findViewById(R.id.car_no);
		EditText houseNo = (EditText)findViewById(R.id.house_no);
		if(carNo.getText().toString().isEmpty() && houseNo.getText().toString().isEmpty())
		{
			Toast.makeText(this, "提交的信息[车牌号/房号]不全", Toast.LENGTH_LONG).show();
			return;
		}
		String postData = getPostData();
		doAdd(postData);
	}

	protected void doAdd(String data)
	{
		Thread addThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				MediaType mediatype = MediaType.parse("application/json;charset=utf-8");
				String url = mServerAddr + "/add";
				String result = "";
				try
				{
					RequestBody body = RequestBody.create(mediatype, data);
					Request request = new Request.Builder().url(url).post(body).build();
					Response response = mHttpClient.newCall(request).execute();
					result = response.body().string();
					Message msg = mHttpHandler.obtainMessage(HTTP_DO_ADD_FINISHED, result);
					mHttpHandler.sendMessage(msg);
				}
				catch (IOException e)
				{
					utils.log("add data exception: "+ e.getMessage());
					e.printStackTrace();
				}
			}
		});
		addThread.start();
	}

}
