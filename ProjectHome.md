## Introduction ##
If you're like me and organise your mp3's into directories by category then it can be very annoying when using [MTP](http://en.wikipedia.org/wiki/Media_Transfer_Protocol) media devices (most portable mp3 players) because they only allow you to browse your music by tag (album/artist/title/genre etc.). This can be somewhat limiting since the categorisation you impose with your directory structure is lost. This can make it difficult to find the music you're looking for while browsing on the device. It is especially difficult when the music is not tagged properly.

I wrote this utility as a workaround for this problem. The idea is that it will re-tag the mp3's while copying the files to the device. It will change the 'album' tag to reflect it's position in your directory structure on your pc. For example, if your file is stored in c:\music\rock\nirvana - smells like teen spirit.mp3, the album tag will be replaced with 'music\rock'. This means that all the mp3s in your c:\music\rock directory will appear under the same album name when you browse it on the mp3 player.

![http://omg.wthax.org/screen1.png](http://omg.wthax.org/screen1.png)

## Testing ##
This application has been tested on the following MTP devices:

  * Creative Zen
  * Creative Zen V Plus

It has been tested on the following operating systems:
  * Microsoft Windows 7

It is by no means finished yet but it is functional. Some features I would like to add are:
  * get working on Linux
  * prevent copying of duplicates to the device
  * check that there is enough space on the device before copying a file
  * allow user to pick which MTP device to use rather than picking the first one
  * configure the criteria for tagging/renaming using various tags
  * support for files other than mp3's
  * support drag and drop from Explorer to the file list

## How to install and use ##
  1. Ensure you have installed the [Sun Java Runtime Environment](http://www.java.com/en/download/manual.jsp)
  1. [Download](http://code.google.com/p/mtpretag/downloads/list) and extract the archive to a suitable location on your hard drive
  1. Execute the file `MtpRetag.bat` from where you extracted the archive
  1. Hit the 'enter' key on any mp3 file in the file browser to add it to the copy list at the bottom of the window
  1. When you have selected all the songs you wish to copy to the device select File menu -> Copy files to device


## Acknowledgements ##
The following 3rd party libraries were used to build this application:
  * [jusbpmp](http://code.google.com/p/jusbpmp) libary for communicating with MTP devices
  * [entagged](http://entagged.sourceforge.net/developer.php) library for reading mp3 tags

## Contact ##
If you would like to contact me you can send me an [email](mailto:paulfeaturesATgmail.com) or if you have a bug to report submit it in the [issue tracker](http://code.google.com/p/mtpretag/issues/list).


---


While this software is free to use, if you do find it useful and would like to contribute then you can do so here :)

[![](https://www.paypal.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=WXHTVVW9RS6M4&lc=IE&item_name=mtpretag&item_number=mtpretag&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)