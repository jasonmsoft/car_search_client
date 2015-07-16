package com.example.CarSearch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.MediaType;

import java.io.IOException;


public class MyActivity extends Activity
{
	/**
	 * Called when the activity is first created.
	 */

	private final String mServerAddr = "http://115.47.47.252:8889";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

	}



	public void onSearch(View view)
	{
		String url = mServerAddr + "/search";
		String result = "";
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).build();

		try
		{
			Response response = client.newCall(request).execute();
			result = response.body().string();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	//get post user info and transfer to json format
	protected String getPostData()
	{
		EditText carNo =  (EditText)findViewById(R.id.car_no);
		EditText name = (EditText)findViewById(R.id.name);
		EditText phoneNo = (EditText)findViewById(R.id.phone_no);
		EditText houseNo = (EditText)findViewById(R.id.house_no);
		if(carNo.getText().toString().isEmpty())
		{
			return "";
		}

		if(houseNo.getText().toString().isEmpty())
		{
			return "";
		}

		return "{'data' : { "+
			"'carno': '" + carNo.getText().toString() + "', "+
			"'name': '" +name.getText().toString() + " ', " +
			"'phoneno' : '"+phoneNo.getText().toString()+ "', " +
			"'houseno' : '"+houseNo + "'"
			+ "}}";
	}

	public void onAdd(View view)
	{
		MediaType mediatype = MediaType.parse("application/json;charset=utf-8");
		String url = mServerAddr + "/add";
		OkHttpClient client = new OkHttpClient();
		String postData = getPostData();
		if(postData.isEmpty())
		{
			Toast.makeText(this, "关键信息[车牌号/房号]填写不完整", Toast.LENGTH_LONG);
			return;
		}

		String result = "";
		try
		{
			RequestBody body = RequestBody.create(mediatype, postData);
			Request request = new Request.Builder().url(url).post(body).build();
			Response response = client.newCall(request).execute();
			result = response.body().string();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
