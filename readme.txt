To use this implemetation of a TFTP:

1. Start the TftpServer which will give you the port that the server is bound to
2. Start the Client to request a file passing the arguments in the order:
   A. hostname of server
   B. port of server
   C. File being requested

   For example:

   java TftpClient localhost 55824 requestFile.txt

   