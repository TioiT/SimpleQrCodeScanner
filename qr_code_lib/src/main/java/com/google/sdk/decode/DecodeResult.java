package com.google.sdk.decode;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

public class DecodeResult implements Parcelable {
    public String format; //扫描结果的格式类型
    public String type;   //扫描结果的类型
    public long date;     //扫描结果的时间
    public String content;  //扫描结果的内容

    public DecodeResult(Result rawResult){
        //获取扫描结果的解析
        ParsedResult result =  ResultParser.parseResult(rawResult);
        this.format = rawResult.getBarcodeFormat().toString();
        this.type = result.getType().toString();
        long timestamp = rawResult.getTimestamp();
        this.date = timestamp;
        String contents = result.getDisplayResult();
        content = contents.replace("\r", "");

    }


    protected DecodeResult(Parcel in) {
        format = in.readString();
        type = in.readString();
        date = in.readLong();
        content = in.readString();
    }

    public static final Creator<DecodeResult> CREATOR = new Creator<DecodeResult>() {
        @Override
        public DecodeResult createFromParcel(Parcel in) {
            return new DecodeResult(in);
        }

        @Override
        public DecodeResult[] newArray(int size) {
            return new DecodeResult[size];
        }
    };

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(format);
        dest.writeString(type);
        dest.writeLong(date);
        dest.writeString(content);
    }
}
