package rainbow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class hash {

	/**
	 * Converts an array of bytes into a readable set of characters in the range ! through ~
	 * @param bytes The array of bytes
	 * @return A string with characters in the range ! through ~
	 */
	public static String makeReadable(byte[] bytes) {
		for (int ii=0; ii<bytes.length; ii++) {
			bytes[ii]=(byte) ((bytes[ii] & 0x5E)+32); // Convert to character ! through ~
		}
		return new String(bytes);
	}

	/**
	 * produce a hash of a given string
	 * @param str The string to hash
	 * @return Returns a collection of sixteen "readable" characters (! through ~) corresponding to this string.
	 */
	public static String compact(String str) {
		// setup the digest
		MessageDigest md = null;
		str += "foo"; // random text added to the string
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Hash digest format not known!");
			System.exit(-1);
		}
		return makeReadable(md.digest());
	}
}

public class Rainbow {
		
	static HashMap<String,String> table = new HashMap<String,String>();
	
	public static String reduceReadable(byte[] bytes) {
		for (int ii=0; ii<bytes.length; ii++)
			bytes[ii]=(byte) (Math.abs(bytes[ii] % 0x5E)+33); // Convert to character ! through ~
		return new String(bytes);
	}

	/**
	 * Computes a "reduce" function for a rainbow table
	 * @param num The index of the reduce function to call
	 * @param str The string to reduce
	 * @param len The ultimate length of the reduction
	 * @return A string of characters between ! and ~.
	 */
	public static String reduce(int num, String str, int len) {
		byte[] result = new byte[len];
		byte[] strB = str.getBytes();
		int temp;
		Random rand = new Random();
		rand.setSeed(0); // set the seed in order to make this predictable
		for (int ii = 0; ii < len; ii++) { // xor two characters
			result[ii] = (byte) num;	
			if (result[ii]< 0) result[ii] = (byte)-result[ii];
			result[ii] ^= (byte) strB[rand.nextInt(strB.length)]
					^ rand.nextInt(128);
			result[ii] ^= (byte) strB[rand.nextInt(strB.length)]
					^ rand.nextInt(128);
			
            temp = (int) result[ii];
			temp = temp % 62;
			if (temp < 10){ // map to alphanumeric
				result[ii] = (byte)(temp+15);
			} else if (temp < 36){
				result[ii] = (byte) (temp+ 22);
			} else {
				result[ii] = (byte)(temp + 28);
			}
		}
		// numbers are from 48 to 57, cap letters 65 to 90, lower case
		// 97 to 122
		
		return reduceReadable(result);
	}

	public static void main(String args[]) throws IOException {
		
		String hash1;
		String pw;
		String line;
		ArrayList<String> Names = readIn("names.out");
		int count = 0;
		Scanner inFile = new Scanner(new FileReader("rainbow.txt"));
		while (inFile.hasNextLine()){
			line = inFile.nextLine();
			hash1 = line.substring(0,16);
			pw = line.substring(17);
			table.put(hash1, pw);
		}
		for (String s: table.keySet())  {
			 System.out.println(table.get(s)+" " + s);
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 
		try{
			boolean done = false;
			while (!done){
				System.out.println("Enter base password or enter quit to stop");
				String text = in.readLine();// read a base password
				if (text.equals("quit")){
					done = true;
				} else {
					System.out.println(text + " maps to "+CrackIt(text));
				}	
			}
			System.out.println("done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String MakeChain(String h) {
		int chainsize = 100;
		int wordsize;
		String pw = h;
		String current;
		for (int c = 0; c < chainsize-1; c++) {
			current = hash.compact(pw);
			wordsize = 5 + c % 4;
			pw = reduce(c, current, wordsize);// reduce to alphanumeric wordsize
		}
		current = hash.compact(pw);
		return current;
	}
	

	public static String CrackIt(String h){
		// this is what you have to build...

		String start = startingPoint(h); //will return the starting point of the rainbow table
		
		int chainsize = 100;
		int wordsize;
		String pw = start;
		String current;
			for (int c = 0; c < chainsize-1; c++) {
				current = hash.compact(pw);
				if (h.equals(current))
					return pw;
				wordsize = 5 + c % 4;
				pw = reduce(c, current, wordsize);
			}
			
		current = hash.compact(pw);
		if (h.equals(current))
			return pw;
		else return "cannot crack " + pw;
	}
	
	
	public static String startingPoint(String h){
		//returns the starting point for the rainbow table
		int chainsize = 100;
		String start = h;
		while(true){
			for (int i = 0; i < chainsize; i++) {
				start = h;
				for (int c = i; c < chainsize; c++){
					for (String a: table.keySet()) {
						if (start.equals(a))
							return table.get(a);
					}
					int wordsize = 5 + c%4;
					start = reduce(c, start, wordsize);
					start = hash.compact(start);
				}				
			}//end for
		}//end while	
	}
	
	
	public static ArrayList<String> readIn(String textFile){
		ArrayList<String> res = new ArrayList<String>();
		File file = new File(textFile);

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				res.add(line);
			}
			br.close();
		} catch (Exception e) {

		}
		return res;
	}
}
