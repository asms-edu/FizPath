FizPath
=======

3D path calculation app using combination of accelerometer and GPS data.

Current functionality/usage:

	1. ensure that your device has the display
	   locked so it won't auto-rotate
	2. once the app is started, place it on a
	   level surface
	3. the top number is the current acceleration
	   value in the Y direction. Confirm this
	   direction by shaking the along along one
	   axis to see which direction causes this
	   reading to vary most. For phones, this is
	   along the long dimension of the screen.
	4. ideally, have the phone in a channel so it
	   can slide in the Y direction without rotating
	5. press the start button
	6. the top line of text will very briefly show
	   "Stage 0: waiting" then "Stage 1: Calibrating"
	7. the bottom line of text is the current
	   calibration value. Wait until this has
	   stabilised to about 3 decimal places
	   (about 30-60 seconds)
	8. give the device a quick push to make it slide
	   along the Y-axis. Try to minimise any
	   rotation of the device.
	9. as the device is in motion, the top line of
	   text will show "Stage 2: moving"
	10. as the device stops due to friction, the
	    top line will very briefly show "Stage 3:
	    slowing" then "Stage 4: stopped"
	11. the second last line of text will show the
	    calculated distance that the device has slid
	    (in metres)
