/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: ReturnCode.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kmarket.install
 * @Description: 安装结果返回值
 * @author: zhaoqy
 * @date: 2016年8月3日 下午3:54:08
 * @version: V1.0
 */

package cld.kmarket.install;

public class ReturnCode 
{
	/**
	 * 安装成功
	 */
	public static final int INSTALL_SUCCEEDED = 1;
	 
	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the package is already installed.
	 * 安装包已安装
	 */
	public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;
	
	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the package archive file is invalid.
	 * 安装包压缩文件无效
	 */
	public static final int INSTALL_FAILED_INVALID_APK = -2;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the URI passed in is invalid.
	 * 传递的URL无效
	 */
	public static final int INSTALL_FAILED_INVALID_URI = -3;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the package manager service found that the device didn't have enough
	 * storage space to install the app.
	 * 没有足够的存储空间来安装应用程序
	 */
	public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if a package is already installed with the same name.
	 * 安装包已经使用相同的名称安装
	 */
	public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the requested shared user does not exist.
	 * 所请求的共享用户不存在
	 */
	public static final int INSTALL_FAILED_NO_SHARED_USER = -6;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if a previously installed package of the same name has a different signature
	 * than the new package (and the old package's data was not removed).
	 * 以前安装的同名包具有比新包不同的签名（并没有删除旧的数据包的数据）
	 */
	public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package is requested a shared user which is already installed on
	 * the device and does not have matching signature.
	 * 新安装包与该设备上已安装包的共享用户和签名不匹配
	 */
	public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package uses a shared library that is not available.
	 * 新安装包使用的共享库不可用
	 */
	public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package uses a shared library that is not available.
	 * 
	 * @hide
	 */
	public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package failed while optimizing and validating its dex files,
	 * either because there was not enough storage or the validation failed.
	 * 空间不足或者验证失败
	 * @hide
	 */
	public static final int INSTALL_FAILED_DEXOPT = -11;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package failed because the current SDK version is older than that
	 * required by the package.
	 * 当前的SDK版本比安装包所需要的版本低
	 */
	public static final int INSTALL_FAILED_OLDER_SDK = -12;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package failed because it contains a content provider with the
	 * same authority as a provider already installed in the system.
	 * content provider已经在系统中被定义
	 */
	public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package failed because the current SDK version is newer than that
	 * required by the package.
	 * 当前的SDK版本比安装包所需要的版本高
	 */
	public static final int INSTALL_FAILED_NEWER_SDK = -14;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package failed because it has specified that it is a test-only
	 * package and the caller has not supplied the {@link #INSTALL_ALLOW_TEST}
	 * flag.
	 * 安装包只供测试
	 */
	public static final int INSTALL_FAILED_TEST_ONLY = -15;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the package being installed contains native code, but none that is
	 * compatible with the the device's CPU_ABI.
	 * 调用底层代码与CPU_ABI兼容
	 */
	public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package uses a feature that is not available.
	 * 新安装包使用了不可用功能
	 */
	public static final int INSTALL_FAILED_MISSING_FEATURE = -17;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if a secure container mount point couldn't be accessed on external media.
	 * 无法在外部媒体访问
	 */
	public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package couldn't be installed in the specified install location.
	 * 新安装包不能安装在指定的安装位置（安装路径不对）
	 */
	public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;

	/**
	 * Installation return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the new package couldn't be installed in the specified install location
	 * because the media is not available.
	 * 新安装包不能安装在指定的安装位置，因为媒体是不可用
	 */
	public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser was given a path that is not a file, or does not end with the
	 * expected '.apk' extension.
	 * 不是一个文件，或不以扩展名".apk"结束
	 */
	public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser was unable to retrieve the AndroidManifest.xml file.
	 * 无法检索AndroidManifest.xml文件
	 */
	public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser encountered an unexpected exception.
	 * 解析时遇到了意外的异常
	 */
	public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser did not find any certificates in the .apk.
	 * 在.apk文件中，没有发现签名
	 */
	public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser found inconsistent certificates on the files in the .apk.
	 * 签名不一致
	 * @hide
	 */
	public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser encountered a CertificateEncodingException in one of the files
	 * in the .apk.
	 * 签名解码失败   
	 */
	public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser encountered a bad or missing package name in the manifest.
	 * 在manifest中遇到损坏或丢失的包名
	 * @hide
	 */
	public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser encountered a bad shared user id name in the manifest.
	 * 在manifest中遇到损坏的共享用户ID名称
	 */
	public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser encountered some structural problem in the manifest.
	 * 在manifest中遇到的一些结构性问题
	 */
	public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;

	/**
	 * Installation parse return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the parser did not find any actionable tags (instrumentation or
	 * application) in the manifest.
	 * 在manifest中，没有找到任何可操作的标签（或应用程序）
	 */
	public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;

	/**
	 * Installation failed return code: this is passed to the
	 * {@link IPackageInstallObserver} by
	 * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} 
	 * if the system failed to install the package because of system issues.
	 * 系统错误
	 * @hide
	 */
	public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
}
