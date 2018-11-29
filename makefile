JFLAGS = -d build/
JC = javac

.SUFFIXES: .java .class

.java.class:
				$(JC) $(JFLAGS) $*.java

CLASSES = \
        client/view/Client.java \
				client/view/Interpreter.java \
				client/view/SafeOutput.java \
				server/net/FileServer.java \
				server/model/JDBC.java \
				server/controller/Controller.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
				$(RM) *.class
