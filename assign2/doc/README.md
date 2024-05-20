To run the server first compile 
``javac TimeServer`` 
After you can run it using the port 8000:
 ``java -classpath ".;sqlite-jdbc-3.45.3.0.jar;slf4j-api-1.7.36.jar" TimeServer 8000``
To run the client first compile it
``javac TimeClient``
Then run it, if you are on localhost like this:
``java TimeClient localhost 8000`` 