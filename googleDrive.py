import json
import requests
import datetime
import os
import pathlib

def get_correct_folder():
    fileList = []
    folder = ''

    if datetime.date.today().isoweekday() == 1:
        now = datetime.datetime.now()
        today12am = now.replace(hour=11, minute=59, second=0, microsecond=0)
        if now < today12am :
            pathFiles = sorted(pathlib.Path("C:\\Users\\Fred\\Documents\\EnregistrementCoursUQTR\\Reseau1").iterdir(), key=os.path.getctime)
            pathFiles.reverse()
            for files in pathFiles:
                fileList.append(files)
            folder = '1rI1IyJ5brz2NJkJyr-33cyPtODNBKKp1'
        else :
            pathFiles = sorted(pathlib.Path("C:\\Users\\Fred\\Documents\\EnregistrementCoursUQTR\\ConceptionLogiciel").iterdir(), key=os.path.getctime)
            pathFiles.reverse()
            for files in pathFiles:
                fileList.append(files)
            folder = '16DWkLYLCRs_VV6ZCrha29gVv2IjY0YPM'

    elif datetime.date.today().isoweekday() == 2:
        now = datetime.datetime.now()
        pathFiles = sorted(pathlib.Path("C:\\Users\\Fred\\Documents\\EnregistrementCoursUQTR\\BD2").iterdir(), key=os.path.getctime)
        pathFiles.reverse()

        for files in pathFiles:
            fileList.append(files)
        folder = '1P2WS0HSaVt-CMPPos--dvyLuQFvaYIcc'
    
    elif datetime.date.today().isoweekday() == 4:
        now = datetime.datetime.now()
        today15pm = now.replace(hour=15, minute=29, second=0, microsecond=0)
        if now < today15pm :
            pathFiles = sorted(pathlib.Path("C:\\Users\\Fred\\Documents\\EnregistrementCoursUQTR\\AnalyseAlgorithme").iterdir(), key=os.path.getctime)
            pathFiles.reverse()

            for files in pathFiles:
                fileList.append(files)
            folder = '1wA3GVs_HN9m6pxUGLjJfsav2Ck57m6RP'
        else :
            pathFiles = sorted(pathlib.Path("C:\\Users\\Fred\\Documents\\EnregistrementCoursUQTR\\Android1").iterdir(), key=os.path.getctime)
            pathFiles.reverse()

            for files in pathFiles:
                fileList.append(files)
            folder = '1hP0YJKlEl-aE6i2JsftdXKWHR_C9FFCA'
    
    fileInfo = {
            'path' : fileList[0],
            'nom' : fileList[0].name,
            'folder' : folder
        }
    return fileInfo


def get_access_token():

    #information to get a new token
    url = "https://oauth2.googleapis.com/token"
    data = {
    'client_id' : '1057464476728-6tv66sjvfb31fvblpmot5hua0dict6oj.apps.googleusercontent.com',
    'client_secret' : 'HHIEtfCUbcvyPRCm-vlmyEDT',
    'refresh_token' : '1//05ThqPyCmDFPWCgYIARAAGAUSNwF-L9IrysCDSDRQ1CLerlYIqub5qNRFRYGBfzruBlYFmrX8pgGxreOoY9HVWGhqzMzZlZFIEAQ',
    'grant_type' : 'refresh_token'
    }

    #request to get a new access token
    response = requests.post(url, data = data)
    accessToken = response.json()

    print(accessToken["access_token"])

    return accessToken["access_token"]



def request_upload():
    #prepare the upload
    accessToken = get_access_token()
    headers = {
        'Authorization' : 'Bearer ' + accessToken,
        'x-upload-content-type' : 'video/x-mastroka; charset=UTF-8',
        'content-type' : 'application/json'
    }

    fileInfo = get_correct_folder()
    date = (datetime.datetime.now()).strftime("%d/%m/%Y")

    #add the information to put the folder in the correct folder
    metadata = {"name":fileInfo["nom"],"mimeType":"video/x-matroska","parents":[fileInfo["folder"]],"modifiedDate":date}

    #request to get the location link that will contain all the correct information for the upload
    response = requests.post('https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable', headers = headers, json=metadata)

    location = response.headers['Location']

    filePath = fileInfo['path']
    print(filePath)
    #read the file
    f = open(filePath, 'rb')
    fileSize = str(os.path.getsize(filePath))
    body = f.read()


    headers = {
        'Content-Length' : fileSize
    }


    #jDump = json.dumps(metadata)

    response = requests.put(url = location, data = body, headers = headers)

    print(response.text)
 
request_upload()