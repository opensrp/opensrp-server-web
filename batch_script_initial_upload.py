from pydrive.auth import GoogleAuth
from pydrive.drive import GoogleDrive
#Login to Google Drive and create drive object
g_login = GoogleAuth()
g_login.LocalWebserverAuth()
drive = GoogleDrive(g_login)
# Importing os and glob to find all PDFs inside subfolder
import glob, os
os.chdir("/docs")
for file in glob.glob("*.pdf"):
    print file
    with open(file,"r") as f:
     fn = os.path.basename(f.name)
     file_drive = drive.CreateFile({'title': fn })  
  file_drive.SetContentString(f.read()) 
  file_drive.Upload()
  print "The file: " + fn + " has been uploaded"
   
print "All files have been uploaded"
