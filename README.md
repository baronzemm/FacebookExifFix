# FacebookExifFix
Re-add EXIF date data to facebook image downloads


Quick and dirty chunk of code that allows enables an easy transition from Facebook to google photos.

When you export your data from facebook the images come down without the date/time information.
When you then try to add those photos to google they will upload with the date/time of the day you uploaded them to google.

This is not desirable.

This code parses the index file that facebook provides with your photos, extracts the date/time of the photo and then embeds that time into the image file.

After running this process you can upload your images to google photos and they will be added with the correct date/time information.
