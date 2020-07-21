# Wireless_EKG_Android_App

This app was built as a companion for a wireless EKG solution I developed as part on my final year Capstone project at University of Manitoba in 2016 - 2017. 

The app relies on the the following software components:
- Google Firebase for Oauth
- Google Firebase as tableless DB to save recorded signal and user data (Age, gender, weight)
- Adafruit UART BLE profile
- Adafruit BLE app skeleton
- GraphView-4.2.1 for graphing signals

All Firebase JSON settings have been removed from the repository.

The hardware was built as a Adafruit Feather compatible shield. The following hardware componets were used to build the sheild
- ADAS1000 analog front end amplifier by Analog Devices for biopotential signal (SPI communication)
- MAX30100 SpO2 sensor (I2C implementation)
- Adafruit Feather Bluefruit LE

