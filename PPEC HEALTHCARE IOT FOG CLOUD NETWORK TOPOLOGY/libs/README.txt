Place the following JARs in this directory:

1. ifogsim2.jar
   - Build from: https://github.com/Cloudslab/iFogSim2
   - Command: cd iFogSim2 && mvn clean package -DskipTests
   - Copy:    cp target/iFogSim-2.0.0.jar ../PPEC-iFogSim2/libs/ifogsim2.jar

2. cloudsim-3.0.3.jar
   - Usually found in: ~/.m2/repository/org/cloudbus/cloudsim/cloudsim/3.0.3/
   - Or bundled inside iFogSim2's target JAR

Run setup_libs.sh (Linux/Mac) or setup_libs.bat (Windows) to automate this.
