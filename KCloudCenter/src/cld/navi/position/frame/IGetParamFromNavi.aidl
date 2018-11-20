package cld.navi.position.frame;
import cld.navi.position.frame.GpsDataParam;
interface IGetParamFromNavi
{
  /**
  *获取kuid
  */
  int getKuidFromNavi();
  /**
  *获取ruid、纠正后经度、纠正后纬度
  */
  GpsDataParam getRuidXYFromNavi();
  
  /**
  *获取session
  */
  String getSessionFromNavi();

  /**
  *获取duid
  */
 int getDuidFromNavi();
  
}