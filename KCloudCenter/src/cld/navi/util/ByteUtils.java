package cld.navi.util;

public class ByteUtils {
	/**
	 * 
	 * @Title: longToByteArray
	 * @Description: long转化为byte[]
	 * @param s
	 * @return
	 * @return: byte[]
	 */
    public static byte[] longToByteArray(long s) {  
        byte[] targets = new byte[8];  
        for (int i = 0; i < 8; i++) {  
            int offset = (targets.length - 1 - i) * 8;  
            targets[i] = (byte) ((s >> offset) & 0xff);  
        }  
        return targets;  
    }  
    
    /**
     * 
     * @Title: int2byte
     * @Description: 32位int转byte[]
     * @param res
     * @return
     * @return: byte[]
     */
    public static byte[] int2byte(int res) {  
        byte[] targets = new byte[4];  
        targets[0] = (byte)(int)(res & 0xff);         // 最低位  
        targets[1] = (byte)(int)((res >> 8) & 0xff);  // 次低位  
        targets[2] = (byte)(int)((res >> 16) & 0xff); // 次高位  
        targets[3] = (byte)(int)(res >> 24);          // 最高位,无符号右移。  
        return targets;
    }
    
    /**
     * 
     * @Title: short2byte
     * @Description: 16位int转byte[]
     * @param res
     * @return
     * @return: byte[]
     */
    public static byte[] short2byte(short res) {  
        byte[] targets = new byte[4];  
        targets[0] = (byte) (res & 0xff);        // 低位  
        targets[1] = (byte) ((res >> 8) & 0xff); // 高位  
        return targets;  
    }  
    
    public static byte[] charArray2byte(char[] array){
    	int size = array.length;
    	byte[] targets = new byte[size];
    	for(int i=0;i<size;i++){
    		targets[i] = (byte)array[i];
    	}
    	return targets;
    }
    
    public static byte[] String2byte(String array){
    	byte[] targets = array.getBytes();
    	return targets;
    }
}
