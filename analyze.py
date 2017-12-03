import cv2

backsub = cv2.bgsegm.createBackgroundSubtractorMOG()#background subtraction to isolate moving cars
capture = cv2.VideoCapture("fly.avi") #change to destination on your pc 
fourcc = cv2.VideoWriter_fourcc(*'XVID')
out = cv2.VideoWriter('output2.avi',fourcc, 20.0, (640,480))
i = 0
minArea=1

while capture.isOpened():
    ret, frame = capture.read()
    if ret == False:
        break
    if frame is None:
        continue
    fgmask = backsub.apply(frame)
    erode=cv2.erode(fgmask,None,iterations=3)     #erosion to erase unwanted small contours
    im2, contours, hierarchy = cv2.findContours(erode, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    for contour in contours:
        moments=cv2.moments(contour,True)               #moments method applied
        area=moments['m00']    
        if moments['m00'] >= minArea:
            x=int(moments['m10']/moments['m00'])
            y=int (moments['m01']/moments['m00'])
            if x>336 and x<700 and y>225 and y<227:       #range of line coordinates for values on left lane
               i=i+1
               print(i)
    cv2.putText(frame,'COUNT: %r' %i, (10,30), cv2.FONT_HERSHEY_SIMPLEX,
                           1, (255, 0, 0), 2)
    cv2.drawContours(frame, contours, -1, (0,255,0), 3)
    #cv2.imshow("Track", frame)
    out.write(frame)
capture.release()
out.release()
cv2.destroyAllWindows()
