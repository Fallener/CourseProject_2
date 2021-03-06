package core.sakai.serviceWrapper;

import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import taper.util.DownloadTask4GUI;
import taper.util.EndEventHandler;
import taper.util.MIMEUtil;
import taper.util.SakaiBase64Decoder;
import core.sakai.objects.Resource;
import core.sakai.objects.Site;

/**
 * @deprecated Test only
 * @author we.taper
 *
 */
public class Tester{

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		testDownloadTask4GUI();
	}
	
	public static void testDownloadTask4GUI() throws RemoteException {
		class UpdateTextField extends JFrame implements PropertyChangeListener{

			private JTextField textField = new JTextField();
			private String prog = "Progress:";
			String url;
			String cookie_header = "JSESSIONID";
			String sesID;
			SakaiLogin log;

			String cookie;
			DownloadTask4GUI downloadTask4GUI;
			
			UpdateTextField() throws RemoteException {
				log = new SakaiLogin();
				sesID = log.login("admin", "admin");
				System.out.println(sesID);
				cookie = cookie_header + "=" + sesID+".localhost";
				url = "http://localhost:8080/access/content/group/mercury/%E5%8F%AF%E5%8F%A3%E5%8F%AF%E4%B9%90%E5%88%9B%E6%84%8F%E5%B9%BF%E5%91%8A--%E8%BE%B9%E7%95%8C%E7%BA%BF%E7%AF%87.flv";

				setSize(300, 300);
				setLayout(new FlowLayout());
				getContentPane().add(textField);
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setVisible(true);

				JOptionPane.showConfirmDialog(this, "Started?");
				downloadTask4GUI = new DownloadTask4GUI(this, url, cookie,
						"E:\\");
				downloadTask4GUI.addPropertyChangeListener(this);
				downloadTask4GUI.execute();
			}
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("progress")){
					textField.setText(prog+evt.getNewValue());
				}
				
			}
			
		}

		new UpdateTextField();
	}
	public static void testAsycUpContent() throws RemoteException {
		
		SakaiLogin log = new SakaiLogin();
		String sesID = log.login("admin", "admin");
		
		System.out.println("sesID:"+sesID);
		
		ContentHosting contentHosting = new ContentHosting(sesID);
		System.out.println("Start at:"+System.currentTimeMillis());
		contentHosting.getContentData("/1group/mercury", new EndEventHandler<String>() {
			
			@Override
			public void finishedWithoutError(String t) {
				System.out.println("Finished:"+t);
				
			}
			
			@Override
			public void errorHappened(Exception e) {
				e.printStackTrace();
				
			}
		});
	}
	public static void testSakaiScript() throws ParserConfigurationException, SAXException, IOException {
		SakaiLogin log = new SakaiLogin();
		SakaiScript sakaiScript = new SakaiScript();
		
		String sesID = log.login("admin", "admin");
		
		
		String whoami = sakaiScript.getUserId(sesID);
		System.out.println("Who am I:"+whoami);
		
		String usrEmail = sakaiScript.getUserEmail(sesID);
		System.out.println("Org Email:"+usrEmail);

		String changE = sakaiScript.changeUserEmail(sesID, "admin", "newEmail@SakaiLogin.com");
		System.out.println("Change email:"+changE);
		
		usrEmail = sakaiScript.getUserEmail(sesID);
		System.out.println("Chged Email:"+usrEmail);

		Site[] allSite4Usr = sakaiScript.getAllSitesForUser(sesID,"test");
		System.out.println("Sites4Usr: "+allSite4Usr.length);
		for(Site s:allSite4Usr) {
			System.out.println(s.toString());
		}
		
	}
	public static void testContentHosting() throws ParserConfigurationException, SAXException, IOException {
		
		SakaiLogin log = new SakaiLogin();
		String sesId = log.login("admin", "admin");
		ContentHosting contentHosting = new ContentHosting(sesId);
		Site[] sites = contentHosting.getAllSitesCollection();
		System.out.println("Sites size: "+sites.length);
		for(Site a:sites) {
			System.out.println("Site:  "+a.toString());
		}
		
		Resource rootCollectionResource[] = contentHosting.getRootCollection();
		Resource mercury = new Resource();
		for(Resource res: rootCollectionResource) {
			System.out.println("Root:"+res.toString());
			if(res.getID().equalsIgnoreCase("/group/mercury/")) {
				mercury = res;
			}
		}
		
		if(mercury.getID().equals("unknow")) {
			System.out.println(mercury);
			System.exit(1);
		}
		Resource testSmile = new Resource();
		Resource collectionResource[] = contentHosting.getResources(mercury.getID());
		for(Resource res: collectionResource) {
			System.out.println("Collection:"+res.toString());
			if(res.getName().equals("Test Smile.png")) {
				testSmile = res;
			}
		}
		
		
		
		if(testSmile.getName().equals("unknow")) {
			System.out.println(testSmile);
			System.exit(1);
		}
		Resource aResourceArray[] = contentHosting.getResources(testSmile.getID());
		assert aResourceArray.length == 1;
		System.out.println("aResArray:"+aResourceArray[0].toString());
		
		
		Resource fromGetInfo = contentHosting.getInfo(testSmile.getID());
		System.out.println("afromGetInfo:"+fromGetInfo.toString());
		
		
		long size = contentHosting.getSiteCollectionSize("mercury");	
		System.out.println("Size of mercury:"+size);
		
		File dest = new File(".\\Test\\test smile.png");
		
		String base64Data = contentHosting.getContentData(testSmile.getID());
		SakaiBase64Decoder.decodeAndWrite2File(base64Data, dest);
		
		String responseCreFold = contentHosting.createFolder(mercury.getID(), "CreatedBy Class Tester");
		System.out.println("Create Folder:"+responseCreFold);
		
		boolean responseCreCont = contentHosting.createContentItem("Named by Class Tester", mercury.getID(), base64Data, "Another test smile png", MIMEUtil.getMIMETypeFrom(dest), true);
		System.out.println("Create ContentItem:"+responseCreCont);
		
		boolean responseUpload = contentHosting.uploadFile("Named by Class Tester another", mercury.getID()+"d", dest, "Uploaded by methed uploadFile");
		System.out.println("Uplaod file:"+responseUpload);

	}
	public static void testLogin(String[] args) throws RemoteException {

		SakaiLogin login = new SakaiLogin();
		String sessionidString = login.login("admin", "admin");
		Scanner input = new Scanner(System.in);
		System.out.printf("To logout?");
		input.nextLine();

		login.logout(sessionidString);
		input.nextLine();
		login.logout(sessionidString);
		input.nextLine();

		input.close();
	}

}
