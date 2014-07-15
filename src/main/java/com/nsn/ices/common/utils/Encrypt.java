package com.nsn.ices.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 描述: 加密密码
 * @author Mars
 *
 */
public class Encrypt{
	
	/**
	* 描述使用md5算法进行加密
	* @author: w7510209@126.com
	* @version: Sep 27, 2010 9:39:13 AM
	*/
	    
	public static String toMD5(String source){
		return encryption(source,"MD5");
	}
	/**
	* 描述：根据要加密的对象和加密算法进行加密
	* 可以使用的加密算法 MD5 or SHA
	* @author: w7510209@126.com
	* @version: Sep 27, 2010 9:34:09 AM
	*/
	    
	private static String encryption(String source, String encName)
	{
		MessageDigest md = null;
		// 加密后的字符串
		String target = null;
		// 要加密的字符串字节型数组
		byte[] bt = source.getBytes();
		try
		{
			if (encName == null || encName.equals(""))
			{
				encName = "MD5";
			}
			md = MessageDigest.getInstance(encName);
			md.update(bt);
			// 通过执行诸如填充之类的最终操作完成哈希计算
			target = bytes2Hex(md.digest()); // to HexString
		}
		catch (NoSuchAlgorithmException e)
		{
			return null;
		}
		return target;
	}

	/**
	* 描述：将字节数组转换成16进制的字符串
	* @author: w7510209@126.com
	* @version: Sep 27, 2010 9:27:19 AM
	*/
	    
	private static String bytes2Hex(byte[] bts)
	{
		String des = "";
		String tmp = null;
		
		for (int i = 0; i < bts.length; i++)
		{
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1)
			{
				des += "0";
			}
			des += tmp;
		}
		return des;
	}
	public static void main(String[] args)
	{
		System.out.println(toMD5("111111"));
	}
}

