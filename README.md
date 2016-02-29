# OscillatorDynamics
Java application for real time simulation of coupled phase oscillator networks. This project was written initially by John Wordsworth (@[JohnWordsworth](https://github.com/JohnWordsworth)) with later additions by Peter Ashwin (@[peterashwin](https://github.com/peterashwin)) and David Leppla-Weber (@[David96](https://github.com/David96)) at the University of Exeter, UK.

The system simulated consists of N identical phase oscillators of Kuramoto style, but with arbitrary coupling function.

Here is a screenshot of the program in action:

![Interface image](https://raw.githubusercontent.com/peterashwin/OscillatorDynamics/master/OscillatorDynamicsInterface.png "Interface image")

You can change the number of oscillators (N), the range of the coupling (R) as well as the coupling/phase interaction function via the "Parameters" or "Graph" windows. 

Have fun!

### Build and run

```shell
javac *.java # build all java files
java ODChim # run it
```

#### Create an executable jar file

```shell
javac *.java # build all java files
jar -cfe filename.jar ODChim *.class # create jar file with entry point ODChim
```
