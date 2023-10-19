For hibernate to work correctly, it is necessary that the *.hbm.xml config files are in the same directory as the *.class files of the Java classes that are going to be persisted.
With the Eclipse default settings the class files are put into /bin, while the *.hbm.xml files are in /src.
The project needs to be configured in a way that the class files are directly put into the /src folder. 