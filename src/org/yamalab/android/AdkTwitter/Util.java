package org.yamalab.android.AdkTwitter;

import android.graphics.drawable.Drawable;

public class Util {
	static void centerAround(int x, int y, Drawable d) {
		int w = d.getIntrinsicWidth();
		int h = d.getIntrinsicHeight();
		int left = x - w / 2;
		int top = y - h / 2;
		int right = left + w;
		int bottom = top + h;
		d.setBounds(left, top, right, bottom);
	}
	static char[] i2c={'0','1','2','3','4','5','6','7',
			    '8','9','a','b','c','d','e','f'};
	static String b2h(byte x){
		int h1=(x >> 4)& 0x0f;
		int h2=x & 0x0f;
		return "0x0"+i2c[h1]+i2c[h2];
	}
	static String i2h(int x){
		String hex="";
		char c=i2c[x&0x0f];
		hex=""+c+hex;
		x=x>>4;
		for(int i=0;i<7;i++) {   // 4bit * 8 = 32 bit
			c=i2c[x&0x0f];
			hex=""+c+hex;
			x=x>>4;
		}
		return "0x0"+hex;
	}
	static boolean isEOL(String x){
		if(x==null) return true;
		if(x.equals("")) return true;
		if(x.charAt(0)=='\0'||
		   x.charAt(0)=='\n'||
		   x.charAt(0)=='\r') return true;
		return false;
	}
	static String skipSpace(String x){
		while(x.startsWith(" ")) x=x.substring(1);
		return x;
	}
	static boolean parseKeyWord(String x, String key, String [] rest){
		if(x.startsWith(key)){
			rest[0]=x.substring(key.length());
			return true;
		}
		return false;
	}
	static boolean parseInt(String x, int[] intrtn, String [] rest){
		char c=x.charAt(0);
		String ix="";
		while('0'<=c && c<='9'){
			ix=ix+c;
			x=x.substring(1);
			if(x.length()<=0) break;
			c=x.charAt(0);
		}
		if(!ix.equals("")){
		   int ixx=(new Integer(ix)).intValue();
		   intrtn[0]=ixx;
		   rest[0]=x;
		   return true;
		}
		return false;
	}
	static boolean parseStrConst(String x,String [] sconst, String [] rest){
		String xconst="";
		if(x.startsWith("\"")){
			x=x.substring(1);
			while(!x.startsWith("\"")){
				if(x.length()<1) return false;
				if(x.startsWith("\\")){
						xconst=xconst+x.charAt(0)+x.charAt(1);
						x=x.substring(2);
				}
				else{
					xconst=xconst+x.charAt(0);
					x=x.substring(1);
				}
			}
			x=x.substring(1);
			sconst[0]=xconst;
			rest[0]=x;
			return true;
		}
		return false;
	}	
	static boolean parseChar(String x, char[] chars, char[] charrtn, String [] rest){
		if(x==null) return false;
		if(x.length()<=0) return false;
		char c=x.charAt(0);
		for(int i=0;i<chars.length;i++){
			char d=chars[i];
			if(c==d) break;
			if(i==chars.length-1){
				return false;
			}
		}
		charrtn[0]=c;
		x=x.substring(1);
		rest[0]=x;
		return true;
	}
	static private String getUrlWithoutParameters(String url){
		int i=url.indexOf("?");
		if(i<0) return url;
		String rtn=url.substring(0,i);
		return rtn;
	}


}
