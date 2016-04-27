
'||''|.                        '||     '||    ||'         .|. 
 ||   ||    ....   ....      .. ||      |||  |||    ....  ||| 
 ||''|'   .|...|| '' .||   .'  '||      |'|..'||  .|...|| '|' 
 ||   |.  ||      .|' ||   |.   ||      | '|' ||  ||       |  
.||.  '|'  '|...' '|..'|'  '|..'||.    .|. | .||.  '|...'  .  
                                                          '|' 
														  
1) Run 702ObfuscationTool.jar by double clicking the file. If this does not work, run the jar file through the command line using java -jar 702ObfuscationTool.jar.
2) For the "Non-obfuscated Location" select the root directory of the android application. Please use GeofenceTasker(ManualObfuscation) as this includes manual obfuscation that we have done. 
3) For the "Obfuscated output Location" select a new and empty folder.
4) Run the obfuscator!
5) After the obfuscator is finished, run the app through ProGuard. Open the new obfuscated code in Android Studio.
6) In build.gradle (app), make sure that minifyEnabled is true. 
7) Click on "Build", then "Generate Signed APK". If you do not have a key store, click on "create new...". Type a password of your choice. You will also have to fill in the form to create a new key. Pick an alias and password of your choice for your key, then fill in your first and last name, and press ok. 
Click next, and make sure the build type is RELEASE. Then click finish and wait for the APK to build. 
7) Run the APK on your Android device. 										

IMPORTANT NOTE:
If you run a separate app from our submitted app, you will need to make some minor modifications to our tool. You will need to turn off the argument obfuscation. This is because there is a separate class which is manually created that is required for the argument obfuscation to work. To do this, navigate to the encryptorPackage in the obfuscation tool, and open ToolGUI.java (in Eclipse). Navigate to line 180 and comment out the following line:
ArgumentObfuscator.ObfuscateArguments(destDir.getAbsolutePath() + "\\app\\src\\main\\java");

Then, run ToolGUI.java, and go through steps 2-7 above. 
  