# Google Safe Browsing 2 Finagle Service

Wraps [Google Safebrowsing2 Scala API](https://github.com/snopoke/google-safebrowsing2) in a [Finagle](http://twitter.github.com/finagle/)
service.

## Features
* Maintain local blacklist database according to [Google Safe Browsing API v2](https://developers.google.com/safe-browsing/)
* Expose HTTP service that implements the [Google Safebrowsing Lookup API](https://developers.google.com/safe-browsing/lookup_guide)

## Use cases
This service can be used to maintain a local blacklist database that can then be queried directly by using this 
[Google Safebrowsing2](https://github.com/snopoke/google-safebrowsing2) library.

The service can also be used to offer a local version of the Lookup API. This might be useful when you have a number of other 
application that need to do local lookup but don't want each one to have to maintain its own database.

## Installation
Assuming you have a database setup that you can connect to and that you are installing on Linux then you can use the following
as a guide to setup the Safebrowsing2 Finagle service.

1. Run sbt package-dist.
2. Copy dist/safebrowsing2.finagle-X.X.X/safebrowsing2.finagle-X.X.X.zip to the server you want to run it on.
3. Create the service user and folders:
	adduser --disabled-login --no-create-home safebrowsing
	mkdir -p /opt/safebrowsing/releases
	mkdir -p /var/log/safebrowsing
	chown -R safebrowsing /var/log/safebrowsing
	chown -R safebrowsing /opt/safebrowsing
4. Exctract the service archive:
	unzip safebrowsing2.finagle-x.x.x.zip /opt/safebrowsing/releases/X.X.X
	ln -s /opt/safebrowsing/releases/X.X.X /opt/safebrowsing/current
5. Make the startup script executable:
	chmod +x /opt/safebrowsing/current/scripts/*.sh
6. Edit the config:
	sudo vi /opt/safebrowsing/current/config/production.scala
7. Start the service and watch the log files:
	sudo /opt/safebrowsing/current/scripts/service.sh start
	tail -f /var/log/safebrowsing/safebrowsing.log /var/log/safebrowsing/safebrowsing.out

## Usage
Once the database is up to date (this may take some time) you can use the Safebrowsing2 Scala library (or any other library that 
implements the Lookup API to query the database.

	val api = new Lookup(apikey, "myapp", "http://<ip or host>:port/", "3.0")
	val resp = api.lookup(Array("http://www.google.com/", "http://ianfette.org/"))
	resp.foreach{
		case (url, result) => println(url+" -> "+result)
	}

	