package cld.navi.position.frame;
import cld.navi.position.frame.GpsDataParam;
interface IGetParamFromNavi
{
  /**
  *��ȡkuid
  */
  int getKuidFromNavi();
  /**
  *��ȡruid�������󾭶ȡ�������γ��
  */
  GpsDataParam getRuidXYFromNavi();
  
  /**
  *��ȡsession
  */
  String getSessionFromNavi();

  /**
  *��ȡduid
  */
 int getDuidFromNavi();
  
}