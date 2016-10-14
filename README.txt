Welcome to my first Data Communication Project 1
Progress:
Checkpoint 1: Over
Checkpoint 2: Over
Checkpoint 3: Currently

Checkpoint 1: 
Status:
Task —> to download file from the given url (including with chunk encoding)
This checkpoint Completed leaving with the cleaning of the code

To Use:
use the command in the following format to use:
./srget -o <desired file name> <desired url>

Your url should follow the following format:
http://<domain> <optional —> “:port”> <path>
if you don’t specify path, don’t worry we correct it for you 
Although if you don’t put http the program will complain.


The chunk encoding part may be a bit bugging since i have no way to check it.
Apologies
If there is no content length given, program will still work (maybe buggy, no test).

Checkpoint 2:
Firstly, the chunk encoding and no CL(this one has no test) part may still be buggy.
Secondly, the GET request and HEAD request we send no longer ask for 
Connection: Keep-Alive
but instead use
Connection: close
Thirdly, resumable is done and the following are the rules for resumable:

1) The file we are writing into will have word CRDOWNLOAD behind, indicating that download is not completed

2) There will be a file with exact name and word META behind if the download is not complete. (This is the information keeper file)

3) If CRDOWNLOAD and META does not exist, we redownload the file for you immediately

4) If CRDOWNLOAD exists but META does not (provided you aren’t the one deleting the META file), then that file is a CTE or has no CL given, we will redownload the file for you.

How Resumable is done:

1) We first check for CRDOWNLOAD and META, if they don’t exist we redownload

2) We then send HEAD req to the server, if it don’t have range-accept, we redownload
	-In addition, if we get error from HEAD (e.g. doesn’t exist and all)
	We will attempt to connect and redownload for you

3) We then receive back the information and do checking of Headers given back with our META file, here are the rule:
	1) if META ETag and Recv ETag exists we check them and see if there are 			differences, if there are we redownload
	2) if ETags don’t exist, we check Last-Modified, same rule again applies
	3) if ETags and Last-Modifieds don’t exist, we do a last resort by checking
		the content-length, same rules applies.
	Note that we always follow this rule
If we pass these rules, we do the redownload and resume the progress.

Notable Changes:
1) When writing into files, we now use Random Access file which is awesome
2) We always add previous content into the new content when resuming 
3) Downloader and ModReader face a lot of changes (tons, expand twice the size)
4) srget.java now only have downloader in it
5) HeadProc now only exists in Downloader (so does ModReader)
6) Cool Ass progress checker

Very Ver Notable:
I hate writing this code very very much (it’s ugly), ill try to change later.

To Use: Same thing as last check point







