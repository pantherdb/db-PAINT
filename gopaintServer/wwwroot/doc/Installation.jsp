<%@page language="java" info = "Copyright (c) 2023 University of Southern California"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="/css/style.css" type="text/css" rel="stylesheet">
        <title>PAINT installation instructions</title>
    </head>
    <body>
        <div id="mainbody">
        <img src="/images/panther/panther_sm.gif" align=center>
        <BR>
        <BR>
        <div class="header1">PAINT client installation instructions</div>
        <BR><BR><BR>
        <ol>
            <li>
                Download the application by clicking on the download link from the browser.
            </li>
            <li>
                Copy the file into a directory and unzip using either the tar -xvf paintApp.tar for MAC or the unzip command in Windows.
            </li>
            <li>
                Ensure Java version 11 is installed and the path variable has been set to utilize Java version 11.
            </li>
            <li>
                Since the PAINT client exchanges information with the PAINT server via HTTPS, it is necessary for the PAINT client to ensure it can verify the validity of the information from the PAINT server via a certificate. This can be done by including the PAINT servers certificate in the list of certificates that are 'trusted' by Java.  See <a href="#Instructions for adding PAINT server certificate"> instructions</a> for adding certificate.
            </li>
            <li>If the terminal outputs Java exception similar to the following:<br>
                <code>Exception in thread "main" javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target</code>, the server certificate has not been installed for the Java version that is being used by the PAINT client application.
            </li>
        </ol>
        <BR>
        <BR>
        <h2><a name="Instructions for adding PAINT server certificate">Instructions for adding the PAINT server certificate into the list of certificates that are trusted by the JAVA application</a></h2>
        <ol type="I">
            <li>Save the certificate to your system.
                <ol>
                    <li>Open the web browser to the website from which the application was downloaded, for example https://go.paint.usc.edu.</li>
                    <li>Depending on the browser, the steps to save the certificate on your system are different:
                        <ol>
                            <li>For FireFox do as follows:
                                <ol>
                                    <li>Click on the lock icon on the address bar -> Connection secure-> More information-> Click on view Certificate button</li>
                                    <li>Under miscellaneous section in download, click 'PEM (Cert)' to download certificate.</li>
                                    <li>Note, location of download since it has to passed in as a parameter when adding into the truststore </li>
                                    <li>There are instructions on the following web site to obtain the certificate: https://www.baeldung.com/linux/ssl-certificates </li>
                                </ol>
                            </li>
                            <li>For Chrome do as follows:
                                <ol>
                                    <li>Click on the lock icon on the address bar -> Connection is secure-> Certificate is valid</li>
                                    <li>Click on Details tab and export button.</li>
                                    <li>This will bring up a save dialog.  For format, select “Base64-encoded ASCII, single certificate (*.pem, *.crt)”.</li>
                                    <li>Note, location of download since it has to passed in as a parameter when adding into the truststore </li>
                                </ol>
                            </li>
                            <li>For Safari, do as follows:
                                <ol>
                                    <li>Click the lock in the address bar.<BR>
                                        <img src="/images/other/safari_address_bar_lock.png"/>
                                    </li>
                                    <li>Click show certificate.<BR>
                                        <img src="/images/other/safari_show_certificate.png"/>
                                    </li>
                                    <li>Drag the certificate icon to a Finder folder or the desktop to download it.<BR>
                                        <img src="/images/other/safari_drag_certificate_to_finder_folder.png"/>
                                    </li>
                                    <li>Note, location of download since it has to passed in as a parameter when adding into the truststore </li>
                                </ol>
                            </li>                            
                            <li>For Microsoft Edge, do as follows:
                                <ol>
                                    <li>Click on the lock icon on the address bar -> Connection is secure-> Click on certificate icon.
                                        <img src="/images/other/edge_address_bar_lock.png"/><BR>
                                        <img src="/images/other/edge_click_on_lock.png"/><BR>
                                        <img src="/images/other/edge_click_on_certificate_icon.png"/>
                                    </li>
                                    <li>Click on Details tab and export button.<BR>
                                         <img src="/images/other/edge_details_tab_export_button.png"/>
                                    </li>
                                    <li>This will bring up a save dialog.  For format, select “Base64-encoded ASCII, single certificate (*.pem, *.crt)”.</li>
                                    <li>Note, location of download since it has to passed in as a parameter when adding into the truststore </li>
                                </ol>
                            </li>                            
                        </ol>
                    </li> 
                </ol>
            </li>    
            <li>Add the certificate to your system
                <ol>
                    <li>The certificate has to be added to the Java Truststore using a command. First, determine where it should be stored. Depending on the system, it should be stored in a Java lib/security directory in file cacerts as given in the table below:
                            <li>
                                <table border=0 class="tablesep"  cellspacing="0" cellpadding="3" style="width:600px">
                                    <tr><td class=tableheaderline><b>System</b></td><td class=tableheaderline><b>Location of Java truststore</b></td></tr>
                                    <tr><td valign=top class=listResult>Newer Mac OS (with JDK installed)</td><td>$(/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/lib/security/cacerts or /Library/Java/JavaVirtualMachines/openjdk-11.0.2.jdk/Contents/Home/lib/security/cacerts (could be jre directory) depending on version of Java 11</td></tr>                                   
                                    <tr><td valign=top class=listResult>Mac (with JRE installed only)</td><td>$(/usr/libexec/java_home)/lib/security/cacerts</td></tr>
                                    <tr><td valign=top class=listResult>Mac (with JDK installed)</td><td>$(/usr/libexec/java_home)/jre/lib/security/cacerts (could be jdk directory)</td></tr>
                                    <tr><td valign=top class=listResult>Windows</td><td>C:\Program Files (x86)\Java\jre<version>\lib\security\cacerts (could be jdk directory)</td></tr>                        
                                </table>
                            </li>
                            <li>Use the keytool command to add the certificate downloaded from step I. to the Java keystore:
                            <ol type="I">
                                <li>Determine where java is running from, by using the ‘echo $PATH ‘ (MAC) or ‘java -version’ command on Windows and ensure keyttool executable in same directory as java executable. If launch.sh/launch.bat has been modified to refer to another Java executable, use this executable path.</li>                           
                                <li>Open a terminal window or command prompt window with administrator privileges or execute the command with necessary privilege (sudo and enter password) to update the Java truststore</li>
                                <li>Syntax of the command is: keytool -import -alias CHOOSE-AN-ALIAS -file certificate.pem -keystore /path/to/your/truststore</li>
                                <li>To execute the keytool command from the Java executable directory, in MAC as root, is as follows, change directory location as necessary:
                                    <ul>
                                        <li>sudo keytool -import -alias gopaintuscedu -file ~/Downloads/go-paint-usc-edu.pem -keystore /Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/lib/security/cacerts</li>
                                        <li>Enter password for sudo</li>
                                        <li>password</li>
                                        <li>keystore</li>
                                        <li>changeit</li>
                                        <li>yes to add certificate</li>                                        
                                    </ul>
                                </li>                                
                                <li>To execute the keytool command from the Java executable directory, in Windows, is as follows:
                                    <ul>
                                        <li>C:\Program Files\Java\jdk-11.0.10\bin>keytool -import -alias gopaintuscedu -file C:\Users\my_user_name\Downloads\go-paint-usc-edu.pem -keystore "C:\Program Files\Java\jdk-11.0.10\lib\security\cacerts"</li>
                                    </ul>
                                </li>
                                <li>Note, the name of the alias for the certificate being added is 'gopaintuscedu'</li>
                                <li>Enter default keystore password ‘changeit’ unless it has been saved as something else</li>
                                <li>The system will display the certificate details and prompt to save.  Enter ‘yes’. System will indicate that the certificate has been added to the Java keystore.</li>
                            </ol>
                            </li>
                            <li>Verify the certificate has been added using command keytool with the list option.  For example, in Windows it is as follows:</li>
                            <ul>
                                <li>keytool -list -keystore "C:\Program Files\Java\jdk-11.0.10\lib\security\cacerts".  The trusted certificate list will have an entry for certificate with the specified alias name.</li>
                            </ul>
                </ol>    
            </li>
            <li>For additional details, refer to https://www.tutorialworks.com/java-trust-ssl/</li>           
        </ol>
        </div>
    </body>
</html>
